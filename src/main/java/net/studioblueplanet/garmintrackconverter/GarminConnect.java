/*

 */
package net.studioblueplanet.garmintrackconverter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.studioblueplanet.settings.SettingsDevice;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * This class periodically retrieves the latest activity fit files from Garmin Connect
 * @author jorgen
 */
public class GarminConnect extends Thread
{
    private final static Logger             LOGGER                  = LogManager.getLogger(GarminConnect.class);
    
    // Maximum files in list; up to 100 supported by API; it must be higher that the maximum expected .fit files per day (which are usually only a few)
    // Note the ratelimit of the API is 100 files in 30 seconds; we download the list plus 2x 40 files in shortest period 30 seeconds
    private final static int                MAX_FILES               = 40;
    
    // Time in seconds between subsequent calls for file list, if no more files to download
    private final static int                TIMEOUTSECONDSSTART     = 30;       // Recheck API timeout at start     
    private final static int                TIMEOUTSECONDSMAX       = 300;      // Recheck API timeout max, for backoff
    private final static int                TIMEOUTBACKOFFINCREASE  = 30;       // Back off: timeout increase when no new files    
    private final static int                TIMEROVERFLOW           = 3600;     // token is refreshed at this frequency      
    
    // Garmin Connect Personal API endpoints
    private final static String             ENDPOINT_TOKEN          ="https://personal.connectapi.garmin.com/personalapitoken-service/personal-api/personal-api-key/token";
    private final static String             ENDPOINT_ACTIVITYLIST   ="https://personal.connectapi.garmin.com/activitylist-service/activities/search/activities";
    private final static String             ENDPOINT_ACTIVITYDOWNL  ="https://personal.connectapi.garmin.com/download-service/files/activity/";
    private final static String             ENDPOINT_ACTIVITY       ="https://personal.connectapi.garmin.com/activity-service/activity/";

    private Thread                          thread;
    private final SettingsDevice            settings;
    private ZonedDateTime                   lastSyncDateTime;
    private final String                    apiKey;
    private String                          jwtToken;
    private final String                    fileDirectory;
    private final HttpClient                httpClient;
    
    public GarminConnect(SettingsDevice settings)
    {
        this.settings   =settings;
        apiKey          =settings.getApiKey();
        lastSyncDateTime=settings.getLastDateTimeSyncedZoned();
        fileDirectory   =settings.getTrackFilePath();
        this.httpClient = HttpClient.newBuilder()
                                    .connectTimeout(Duration.ofSeconds(15))
                                    .build();
    }

    /**
     * Stops the process
     */
    public void startProcess()
   {
        thread          =new Thread(this);
        thread.start();      
    }    

    /**
     * Stops the process
     */
    public void stopProcess()
    {
        // 1. Signal the thread to stop
        LOGGER.info("Signaling GarminConnect thread to finish!");
        thread.interrupt();

        // 2. Wait for the thread to finish
        try 
        {
            thread.join(5000); // Timeout 5 seconds
        } 
        catch (InterruptedException e) 
        {
            // Restore interrupt status of the calling thread
            Thread.currentThread().interrupt();
            LOGGER.error("GarminConnect Thread did not finish");
        }
        LOGGER.info("GarminConnect Thread finished!");
    }    
    
    /**
     * This method requests the API JWT token and stores it in jwtToken.
     */
    public void getJwtToken()
    {
        LOGGER.info("Request JWT token for Garmin API");
        if (apiKey == null || apiKey.isBlank()) 
        {
            LOGGER.error("Personal API key must not be null or blank.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT_TOKEN))
                .header("Authorization", "ApiKey " + apiKey.trim())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try
        {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to obtain access token. HTTP status: " 
                        + response.statusCode() + " - " + response.body());
            }

            jwtToken=response.body();     
            LOGGER.info("JWT Token retrieved {}...", jwtToken.substring(0, 20));
        }
        catch (Exception e)
        {
            LOGGER.error("Exception when requesting token: {}", e.getMessage());
        }
    }
    
    /**
     * Retrieves the list of activities since date part of lastSyncDateTime.
     * @return The list of activities or null if something failed
     */
    public List<GarminConnectActivity> getActivityList()
    {
        List<GarminConnectActivity> activityList=null;
        if (jwtToken == null || jwtToken.isBlank()) 
        {
            LOGGER.error("JWT key must not be null or blank.");
        }

        // Format: startDate=[YYYY]-[MM]-[DD]
        String formattedDate=lastSyncDateTime.toLocalDate().toString();
        LOGGER.info("Request Activity list from Garmin API, all activities from {}", formattedDate);

        String url = String.format("%s?sortOrder=asc&sortBy=startLocal&startDate=%s&start=0&limit=%d", ENDPOINT_ACTIVITYLIST, formattedDate, MAX_FILES);

        try
        {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + jwtToken.trim())
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200)
            {
                ObjectMapper mapper=new ObjectMapper();
                activityList= mapper.readValue(response.body(), new TypeReference<List<GarminConnectActivity>>() {});
                LOGGER.info("List with size {} downloaded", activityList.size());
            }
            else
            {
                LOGGER.error("Failed to fetch activities. HTTP status: {} - {}", response.statusCode(), response.body());
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error requesting Garmin Connect activity list: {}", e.getMessage());
        }
        return activityList;
    }

    /**
     * Given the activity, derive the filename of format
     * [yyyy]-[MM]-[dd]-[hh]-[mm]-[ss].fit
     * @param activity Activity to process
     * @return The filename
     */
    public String getAssumedFilename(GarminConnectActivity activity)
    {
        String filename=null;
        
        String localDateTimeString=activity.getStartTimeLocal();
        if (localDateTimeString!=null)
        {
            filename=localDateTimeString.replaceAll("[:\\s]", "-")+".fit";
        }
        else
        {
            LOGGER.error("Activity does not have a local date time");
        }
        
        return filename;
    }
    
    /**
     * From the list of Garmin activities, filter the new ones and add them
     * to the activities list; new activities are downloaded and stored in the 
     * fileDirectory, with file name yyyy-MM-dd-hh-mm-ss.fit, based on the 
     * local
     * @param newActivities List of activities retrieved from the Garmin site
     * @return Number of activities actually downloaded and stored
     */
    public int processActivities(List<GarminConnectActivity> newActivities)
    {
        ZonedDateTime   dateTimeLastFile=null;
        Double          durationLastFile=null;
        
        LOGGER.info("Processing {} activities", newActivities.size());
        int count=0;
        if (newActivities!=null)
        {
            for(GarminConnectActivity activity : newActivities)
            {
                // Get the start time of the file, GMT/UTC
                String timeString               =activity.getStartTimeGmt();
                if (timeString!=null)
                {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
                    ZoneId zone                 = ZoneId.of("UTC");
                    ZonedDateTime startTime     = LocalDateTime.parse(timeString, formatter).atZone(zone);
                    
                    if (dateTimeLastFile==null || startTime.isAfter(dateTimeLastFile))
                    {
                        dateTimeLastFile        =startTime;
                        durationLastFile        =activity.getElapsedDuration();
                    }
                    String filename             =fileDirectory+"/"+getAssumedFilename(activity);
                    File file=new File(filename);
                    if (!file.exists())
                    {
                        LOGGER.info("File {} does not exist, downloading...", filename);
                        boolean error=downloadActivity(filename, activity.getActivityId());
                        error|=downloadActivityInfo(filename.replace(".fit", ".json"), activity.getActivityId());
                        if (!error)
                        {
                            count++;
                        }
                    }
                    else
                    {
                        LOGGER.info("File {} already exists, skipping...", filename);
                    }
                }
                else
                {
                    LOGGER.error("Activity is not timestamped, skipping");
                }
            }

            // If the maximum number of files is downloaded, there may be more.
            // If less than the maximum number is downloaded, there are no more till now
            if (newActivities.size()>=MAX_FILES)
            {
                // We check next time since last know file. We always find this file though
                lastSyncDateTime=dateTimeLastFile.plusSeconds(durationLastFile.longValue());                
            }
            else
            {
                // We synced up till now. It is more efficient.
                // However, files that arrive at Garmin Connect *before* today after this check
                // will not be seen next check
                lastSyncDateTime=ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);                
            }

            
            settings.setLastDateTimeSyncedZoned(lastSyncDateTime);
            LOGGER.info("Downloaded all files with starttime till {}", lastSyncDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        }
        else
        {
            LOGGER.error("Activity list passed for processing is NULL...");
        }
        LOGGER.info("Checked {} activities; activities downloaded: {}", newActivities.size(), count);
        return count;
    }
    
    /**
     * Download the activity .fit file and store it
     * @param filename Full filename for storage of the .fit file
     * @param fileId Garmin ID of the file\
     * @return True if error, false if succeeded
     */
    public boolean downloadActivity(String filename, long fileId)
    {
        boolean error=false;
        LOGGER.info("Donwloading activity {} and storing it to {}", fileId, filename);
        if (jwtToken == null || jwtToken.isBlank()) 
        {
            error=true;
            LOGGER.error("JWT key must not be null or blank.");
        }

        String url = ENDPOINT_ACTIVITYDOWNL+fileId;

        try
        {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + jwtToken.trim())
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200)
            {
                byte[] payload = response.body();
                Path targetPath = Path.of(filename);

                // Garmin returns a ZIP archive containing "<activityId>_ACTIVITY.fit".
                // Check for ZIP magic header ('P', 'K') and extract the .fit entry.
                if (payload.length >= 2 && payload[0] == 0x50 && payload[1] == 0x4B) 
                {
                    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(payload))) 
                    {
                        ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) 
                        {
                            if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".fit")) 
                            {
                                Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                                return error;
                            }
                        }
                        error=true;
                        LOGGER.error("ZIP archive for activity {} did not contain a .fit file.", fileId);
                    }
                } 
                else 
                {
                    // Fallback in case the endpoint returns raw uncompressed .fit bytes
                    try (InputStream in = new ByteArrayInputStream(payload)) 
                    {
                        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }                
            }
            else
            {
                error=true;
                LOGGER.error("Failed to fetch activities. HTTP status: {}", response.statusCode());
            }
        }
        catch (Exception e)
        {
            error=true;
            LOGGER.error("Error requesting Garmin Connect activity list: {}", e.getMessage());
        }
        return error;
    }
    
    /**
     * Retrieves the info from Garmin Connect API
     */
    public boolean downloadActivityInfo(String filename, long activityId)
    {
        boolean error=true;
        if (jwtToken == null || jwtToken.isBlank()) 
        {
            LOGGER.error("JWT key must not be null or blank.");
        }

        String url = String.format("%s%d", ENDPOINT_ACTIVITY, activityId);

        try
        {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + jwtToken.trim())
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200)
            {
                LOGGER.info("Activity Info {} downloaded", activityId);
                response.body();
                Path filePath = Path.of(filename);
                Files.writeString(filePath, response.body());
                error=false;
            }
            else
            {
                LOGGER.error("Failed to fetch activity info. HTTP status: {} - {}", response.statusCode(), response.body());
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error requesting Garmin Connect activity info: {}", e.getMessage());
        }
        return error;
    }
    
    /**
     * The thread function. The function requests the list of available files  
     * from the personal GarminConnect API since previous query (up till now). 
     * The list is processed: if any file has not been downloaded, it is 
     * downloaded. If the list contains the maximum number of files (MAX_FILES), 
     * it assumed that there may be more.
     * Next loop (after 1 second) a next query is made for the remaining files.
     * If less than the maximum number is returned, it is assumed that there
     * are no more files to download. The thread sleeps longer for the next 
     * query (up till TIMEOUTSECONDS)
     */
    @Override
    public void run()
    {
        LOGGER.info("GarminConnect Thread started");
        int     secondCount         =0;
        int     downloadTimeout     =TIMEOUTSECONDSSTART;
        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                // At start and each TIMEROVERFLOW seconds (refresh)
                if (secondCount==0)
                {
                    getJwtToken();
                }
                
                // Always use the timeout when downloading many files
                // to prevent running into the rate limit of the API
                if (secondCount%downloadTimeout==0)
                {
                    // Request the activities since (and including) the day of 
                    // last known activity; this means at least the last known
                    // activity is returned and maybe more
                    List<GarminConnectActivity> activities=getActivityList();
                    
                    int newActivitiesAdded=0;
                    if (activities!=null && activities.size()>0)
                    {
                        // Process the list of activities and store non known activities
                        newActivitiesAdded  =processActivities(activities);
                    }
                        
                    // If no new activities were found, increase back off
                    if (newActivitiesAdded==0)
                    {
                        // No new activities: 
                        downloadTimeout+=TIMEOUTBACKOFFINCREASE;
                        downloadTimeout=Math.min(downloadTimeout, TIMEOUTSECONDSMAX);
                    }
                    else
                    {
                        // Reset backoff
                        downloadTimeout=TIMEOUTSECONDSSTART;
                    }
                    LOGGER.info("Time {} Timeout {}", secondCount, downloadTimeout);
                }
                // 1 second tick
                Thread.sleep(1000);
                
                // count seconds
                secondCount++;
                if (secondCount>=TIMEROVERFLOW)
                {
                    secondCount=0;
                }
            }
            catch (InterruptedException e)
            {
                // Catching InterruptedException clears the interrupt flag!
                // Restore it so the while-loop condition exits cleanly.
                Thread.currentThread().interrupt();
                LOGGER.info("GarminConnect Thread interrupted");
            }   
        }
    }
}
