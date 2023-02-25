/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import net.studioblueplanet.fitreader.FitReader;
import net.studioblueplanet.fitreader.FitMessage;
import net.studioblueplanet.fitreader.FitMessageRepository;
import net.studioblueplanet.fitreader.FitGlobalProfile;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.studioblueplanet.settings.ApplicationSettings;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
        

/**
 * This class represents a track consisting of track segments and optional
 * a number of logged waypoints
 * @author Jorgen
 */
public class Track
{
    public static final int                 MS_PER_S    =1000;
    public static final int                 CM_PER_M    =100;
    public static final double              KMH_PER_MS  =3.6;
    private final static Logger             LOGGER      = LogManager.getLogger(Track.class);
    private final List<TrackSegment>        segments;
    private final List<Location>            waypoints;

    private String                          fitFileName;
    private String                          deviceName;
    private String                          sport;
    private String                          subSport;
    private String                          manufacturer;
    private String                          product;
    private Long                            serialNumber;
    private String                          fileType;
    private String                          softwareVersion;

    private ZonedDateTime                   startTime;
    private ZonedDateTime                   endTime;
    private Long                            elapsedTime;    // s
    private Long                            timedTime;      // s
    private Double                          startLat;
    private Double                          startLon;
    private Double                          distance;       // m
    private Double                          averageSpeed;   // km/h
    private Double                          maxSpeed;       // km/h
    private Integer                         ascent;         // m
    private Integer                         descent;        // m
    private Double                          grit;           // kGrit
    private Double                          flow;           // FLOW
    private Double                          calories;       // cal
    private Integer                         jumpCount;      // 
    private String                          mode;           //
    private double                          compressionMaxError;

    
    private static final int                TIMEREVENT=0;
    private static final int                TIMEREVENT_TIMERSTARTED=0;
    private static final int                TIMEREVENT_TIMERSTOPPED=4;

    private int                             invalidCoordinates;
    private int                             validCoordinates;
    
    /**
     * Constructor
     * @param trackFileName Track file
     * @param deviceName Description of the device that recorded the track
     */
    public Track(String trackFileName, String deviceName)
    {
        FitReader               reader;
        FitMessageRepository    repository;
        List<FitMessage>        lapMessages;
        List<FitMessage>        sessionMessages;
        List<FitMessage>        eventMessages;
        List<FitMessage>        trackMessages;
        FitMessage              message;
        int                     id;
        boolean                 isCourse;
        
        isCourse        =false;
        fitFileName     =new File(trackFileName).getName();
        segments        =new ArrayList<>();
        waypoints       =new ArrayList<>();
        
        reader          =FitReader.getInstance();
        repository      =reader.readFile(trackFileName);
        lapMessages     =repository.getAllMessages("lap");
        trackMessages   =repository.getAllMessages("record");
        sessionMessages =repository.getAllMessages("session");
        eventMessages   =repository.getAllMessages("event");
        
        //file_id
        message    =repository.getFitMessage("file_id");
        if (message!=null)
        {
            id          =(int)message.getIntValue(0, "manufacturer");
            manufacturer=FitGlobalProfile.getInstance().getTypeValueName("manufacturer", id);
            id          =(int)message.getIntValue(0, "product");
            product     =FitGlobalProfile.getInstance().getTypeValueName("garmin_product", id);
            serialNumber=message.getIntValue(0, "serial_number");
            id          =(int)message.getIntValue(0, "type");
            fileType    =FitGlobalProfile.getInstance().getTypeValueName("file", id);

            if ("training_center".equals(product))
            {
                isCourse=true;
            }
        }        

        //device_info
        message    =repository.getFitMessage("device_info");
        if (message!=null)
        {
            id              =(int)message.getIntValue(0, "manufacturer");
            manufacturer    =FitGlobalProfile.getInstance().getTypeValueName("manufacturer", id);
            id              =(int)message.getIntValue(0, "product");
            product         =FitGlobalProfile.getInstance().getTypeValueName("garmin_product", id);
            serialNumber    =message.getIntValue(0, "serial_number");
            double version  =message.getScaledValue(0, "software_version");
            softwareVersion =String.format("%.2f", version);
        }
        
        if (trackMessages!=null && trackMessages.size()>0)
        {
            // Parse sessions
            if (sessionMessages!=null && sessionMessages.size()>0)
            {
                // Get data from session
                this.parseSessions(sessionMessages);
            }            
            
            // Get track segments from timer start/stop; only works for tracks
            this.getSegmentsFromEvents(eventMessages);

            // If no segments found, try to get them from the laps
            if (segments.size()==0)
            {
                if (lapMessages!=null && lapMessages.size()>0)
                {
                    // Get data from session
                    this.parseLaps(lapMessages);
                }
                else
                {
                    LOGGER.error("Unable to extract segments from track or route");
                }
            }

            // Add trackpoints to segments
            this.addTrackpointsToSegments(trackMessages);

            this.deviceName=deviceName;

            // On the Fenix 7 it has been observed that not always 
            // trackpoints are stored in chronological order. Therefore
            // sort the segments after adding trackpoints
            // For courses we don't want this sorting because timestamps
            // of the course points may not have a meaning
            if (!isCourse)
            {
                this.sortSegments();
            }
            
            compressionMaxError=ApplicationSettings.getInstance().getTrackCompressionMaxError();
            compressTrack(compressionMaxError);
        }
        else 
        {
            compressionMaxError=0.0;
            
        }
    }
    
    public Track()
    {
        segments        =new ArrayList<>();
        waypoints       =new ArrayList<>();
        compressionMaxError        =0.0;
    }
    

    /**
     * This method parses the FIT lap record and destilates the number of laps.
     * @param lapMessages The FIT record holding the 'lap' info
     */
    private void parseLaps(List<FitMessage> lapMessages)
    {
        int i;
        int size;
        ZonedDateTime           segmentStartTime;
        ZonedDateTime           segmentEndTime;
        TrackSegment            segment;
        
        for (FitMessage message:lapMessages)
        {
            size                =message.getNumberOfRecords();
            i=0;
            while (i<size)
            {
                segmentStartTime=message.getTimeValue(i, "start_time");


                if (segmentStartTime!=null)
                {
                    elapsedTime     =message.getIntValue(i, "total_elapsed_time")/MS_PER_S;
                    segmentEndTime  =segmentStartTime.plusSeconds(elapsedTime);
                    segment     =new TrackSegment(segmentStartTime, segmentEndTime);
                    segments.add(segment);
                    LOGGER.debug("Lap {} {} - {} {} s", 
                                 message.getIntValue(i, "message_index"),
                                 segmentStartTime.toString(),
                                 segmentEndTime.toString(),
                                 elapsedTime);
                }
                else
                {
                    LOGGER.error("Lap does not contain start and end time");
                }
                i++;
            }   
        }
        
    }
    
    /**
     * This method parses the FIT lap record and destillates the number of sessions.
     * @param lapMessages The FIT record holding the 'lap' info
     */
    private void parseSessions(List<FitMessage> sessionMessages)
    {
        int                     i;
        int                     size;
        TrackSegment            segment;
        int                     id;
        
        for (FitMessage message:sessionMessages)
        {
            size            =message.getNumberOfRecords();
            i=0;
            while (i<size)
            {
                endTime     =message.getTimeValue(i, "timestamp");
                startTime   =message.getTimeValue(i, "start_time");
                elapsedTime =message.getIntValue(i, "total_elapsed_time")/MS_PER_S;
                timedTime   =message.getIntValue(i, "total_timer_time")/MS_PER_S;

                startLat    =message.getLatLonValue(i, "start_position_lat");
                startLon    =message.getLatLonValue(i, "start_position_lon");
                
                distance    =message.getScaledValue(i, "total_distance");
                
                if (message.hasField("enhanced_avg_speed"))
                {
                    averageSpeed=message.getScaledValue(i, "enhanced_avg_speed")*KMH_PER_MS;
                }
                else if (message.hasField("avg_speed"))
                {
                    averageSpeed=message.getScaledValue(i, "avg_speed")*KMH_PER_MS;
                }
                if (message.hasField("enhanced_max_speed"))
                {
                    maxSpeed    =message.getScaledValue(i, "enhanced_max_speed")*KMH_PER_MS;
                }
                else if (message.hasField("max_speed"))
                {
                    maxSpeed    =message.getScaledValue(i, "max_speed")*KMH_PER_MS;
                }
                grit        =message.getFloatValue(i, "total_grit");
                flow        =message.getFloatValue(i, "avg_flow");
                if (message.hasField("jump_count"))
                {
                    jumpCount   =(int)message.getIntValue(i, "jump_count");
                }
                else
                {
                    jumpCount   =null;
                }
                calories    =message.getScaledValue(i, "total_calories");
                ascent      =(int)message.getIntValue(i, "total_ascent");
                if (ascent==0xffff)
                {
                    ascent=null;
                }
                descent     =(int)message.getIntValue(i, "total_descent");
                if (descent==0xffff)
                {
                    descent=null;
                }
                mode        =message.getStringValue(i, "mode");
                
                id=(int)message.getIntValue(0, "sport");
                sport=FitGlobalProfile.getInstance().getTypeValueName("sport", id);
                id=(int)message.getIntValue(0, "sub_sport");
                subSport=FitGlobalProfile.getInstance().getTypeValueName("sub_sport", id);
                
                if (startTime!=null && endTime!=null)
                {
                    LOGGER.info("SESSION        : {}", message.getIntValue(i, "message_index"));
                    LOGGER.info("Time           : {}-{}", startTime.toString(), endTime.toString());
                    LOGGER.info("Duration       : {}/{} sec", elapsedTime, timedTime);
                    LOGGER.info("Distance       : {} km", distance);
                    LOGGER.info("Speed          : average {}, max {} km/h", averageSpeed, maxSpeed);
                    LOGGER.info("Ascent/Descent : {}/{} m", ascent, descent);
                    LOGGER.info("Sport          : {} - {}", sport, subSport);
                    LOGGER.info("Mode           : {}", mode);
                    if ("OFF ROAD".equals(mode))
                    {
                        LOGGER.info("Grit           : {} kGrit", grit);
                        LOGGER.info("Flow           : {}", flow);
                        LOGGER.info("Jumps          : {}", jumpCount);
                    }
                    LOGGER.info("Calories       : {} cal", calories);
                }
                else
                {
                    LOGGER.error("Session does not contain start and end time");
                }
                i++;
            }   
        }
    }    
    
    /**
     * This method parses the FIT lap record and destilles the number of sessions.
     * @param lapMessages The FIT record holding the 'lap' info
     */
    private void getSegmentsFromEvents(List<FitMessage> eventMessages)
    {
        int i;
        int size;
        ZonedDateTime           startTime;
        ZonedDateTime           endTime;
        TrackSegment            segment;
        boolean                 started;
        int                     event;
        int                     eventType;
        
        started     =false;
        startTime   =null;
        endTime =null;
        for (FitMessage message:eventMessages)
        {
            size            =message.getNumberOfRecords();
            i=0;
            while (i<size)
            {
                event       =(int)message.getIntValue(i, "event");
                eventType   =(int)message.getIntValue(i, "event_type");
                if (event==TIMEREVENT)
                {
                    if (started)
                    {
                        if (eventType==TIMEREVENT_TIMERSTOPPED)
                        {
                            started=false;
                            endTime=message.getTimeValue(i, "timestamp");
                            segment     =new TrackSegment(startTime, endTime);
                            segments.add(segment);
                            LOGGER.info("Segment found: {} - {}", startTime.toString(), endTime.toString());
                        }
                    }
                    else
                    {
                        if (eventType==TIMEREVENT_TIMERSTARTED)
                        {
                            started=true;
                            startTime=message.getTimeValue(i, "timestamp");
                        }
                    }
                }
                i++;
            }   
        }
    }    
    
    /**
     * This method parses the FIT activity record. It destiles the track points
     * @param trackMessages The record holding the 'activity' information
     */
    private void addTrackpointsToSegments(List<FitMessage> trackMessages)
    {
        Double                  lat;
        Double                  lon;
        Double                  ele;
        ZonedDateTime           dateTime;

        Double                  speed;
        Double                  distance;
        Integer                 temp;
        Integer                 heartrate;
        Integer                 gpsAccuracy;
        int                     i;
        int                     size;
        TrackPoint              point;
        Iterator<TrackSegment>  it;
        boolean                 found;
        TrackSegment            segment;

        invalidCoordinates=0;
        validCoordinates=0;
        for (FitMessage message:trackMessages)
        {
            size            =message.getNumberOfRecords();
            i=0;
            while (i<size)
            {
                dateTime    =message.getTimeValue(i, "timestamp");
                lat         =message.getLatLonValue(i, "position_lat");
                lon         =message.getLatLonValue(i, "position_long");
                
                if (message.hasField("enhanced_altitude"))
                {
                    ele         =message.getScaledValue(i, "enhanced_altitude");
                }
                else if (message.hasField("corrected_altitude"))
                {
                    ele         =message.getScaledValue(i, "corrected_altitude");
                }
                else
                {
                    ele         =null;
                }
                    
                if (message.hasField("temperature"))
                {
                    temp        =(int)message.getIntValue(i, "temperature");
                }
                else
                {
                    temp        =null;
                }
                
                if (message.hasField("enhanced_speed"))
                {
                    speed       =message.getScaledValue(i, "enhanced_speed");
                }
                else if (message.hasField("speed"))
                {
                    speed       =message.getScaledValue(i, "speed");
                }
                else
                {
                    speed       =null;
                }
                    
                if (message.hasField("distance"))
                {
                    distance    =message.getScaledValue(i, "distance");
                }
                else
                {
                    distance    =null;
                }
                if (message.hasField("heart_rate"))
                {
                    heartrate   =(int)message.getIntValue(i, "heart_rate");
                }
                else
                {
                    heartrate   =null;
                }

                if (message.hasField("gps_accuracy"))
                {
                    gpsAccuracy =(int)message.getIntValue(i, "gps_accuracy")*CM_PER_M; // in cm
                }
                else
                {
                    gpsAccuracy =null;
                }

                if (Math.round(lat)!=180 && Math.round(lon)!=180)
                {
                    point       =new TrackPoint(dateTime, lat, lon, ele, speed, distance, temp, heartrate, gpsAccuracy);

                    found=false;
                    it=this.segments.iterator();
                    while (it.hasNext() && !found)
                    {
                        segment=it.next();
                        if (segment.isInSegment(dateTime))
                        {
                            segment.addTrackPoint(point);
                            found=true;
                        }
                    }
                    validCoordinates++;
                    if (!found)
                    {
                        LOGGER.error("No segment found to add trackpoint @ {} [{}, {}] to", dateTime.toString(), lat, lon);
                    }
                }
                else
                {
                    LOGGER.error("Illegal lat/lon at {} [{}, {}]", dateTime.toString(), lat, lon);
                    invalidCoordinates++;
                }
                LOGGER.debug("Trackpoint {} ({}, {}) ele {}, {} km/h, {} m",
                                 dateTime.toString(),
                                 lat, lon,
                                 ele, 
                                 speed, 
                                 distance);
                i++;
            }
        }
        LOGGER.info("Good coordinates {}, wrong coordinates: {} ({}%)", validCoordinates, invalidCoordinates, 100*invalidCoordinates/(invalidCoordinates+validCoordinates));
    }
    
    /**
     * Add the waypoints that were read from the locations.fit file. 
     * Only the waypoints that were recorded during the track are added 
     * to the track.
     * @param allWaypoints The waypoints read from the device
     */
    public void addTrackWaypoints(List<Location> allWaypoints)
    {
        ZonedDateTime           dateTime;
        Iterator<Location>      waypointIterator;
        Iterator<TrackSegment>  segmentIterator;
        Location                waypoint;
        TrackSegment            segment;
        boolean                 found;
        
        waypointIterator=allWaypoints.iterator();
        while (waypointIterator.hasNext())
        {
            waypoint=waypointIterator.next();
            dateTime=waypoint.getDateTime();
            segmentIterator=segments.iterator();
            found=false;
            while (segmentIterator.hasNext() && !found)
            {
                // If the date time stamp is in the range of the segment,
                // add this waypoint to the track and don't look any further
                segment=segmentIterator.next();
                if (segment.isInSegment(dateTime))
                {
                    found=true;
                    waypoints.add(waypoint);
                }
            }
        }
    }
    
    /**
     * Return some info about this track
     * @return The info
     */
    public String getTrackInfo()
    {
        String          info;
        int             i;
        int             noOfSegments;
        TrackSegment    segment;
        
        noOfSegments=segments.size();
        
        info="Track ";
        if (sport!=null)
        {
            info+="("+sport;
            if (subSport!=null)
            {
                info+=" - "+subSport;
            }
            info+=") ";
        }
        info+="with "+this.segments.size()+" segments (";
        i=0;
        while (i<noOfSegments)
        {
            segment=segments.get(i);
            info+=segment.getNumberOfTrackPoints();
            if (i<noOfSegments-1)
            {
                info+=", ";
            }
            i++;
        }
        info+=" points)";
        info+=" and "+waypoints.size()+" waypoints";
        return info;
    }

    /**
     * Return some info about this track
     * @return The info
     */
    public String getTrackInfo2()
    {
        String          info;
        int             i;
        int             noOfSegments;
        TrackSegment    segment;
        
        noOfSegments=segments.size();
        
        info="Activity: ";
        if (sport!=null)
        {
            info+=sport;
            if (subSport!=null)
            {
                info+=" - "+subSport;
            }
        }
        else
        {
            info+=" unknown";
        }
        
        int points  =segments.stream()
                             .map(seg -> seg.getNumberOfTrackPoints())
                             .mapToInt(Integer::valueOf)
                             .sum();
        int cpoints =segments.stream()
                             .map(seg -> seg.getNumberOfCompressedTrackPoints())
                             .mapToInt(Integer::valueOf)
                             .sum();
        
        info+="\nSegments: "+this.segments.size()+", points: "+points+", compressed: "+cpoints+" ("+
               (cpoints*100/points)+"%), waypoints: "+waypoints.size();
        info+="\nValid points: "+validCoordinates+", invalid points: "+invalidCoordinates+
              " ("+(100*invalidCoordinates/(invalidCoordinates+validCoordinates))+"%, omitted)";
        info+="\nDevice: "+this.deviceName+", sw: "+this.softwareVersion;
        return info;
    }

    
    private void sortSegments()
    {
        segments.forEach((segment) ->
        {
            segment.sortOnDateTime();
        });        
    }
    
    /* ******************************************************************************************* *\
     * TRACK COMPRESSING - DOUGLASS-PEUCKER ALGORITHM
    \* ******************************************************************************************* */
    public void compressTrack(double maxError)
    {
        compressionMaxError=maxError;
        segments.forEach((segment) ->
        {
            segment.compress(maxError);
        });
    }    
    
    /**
     * Get the device name
     * @return The device name
     */
    public String getDeviceName()
    {
        return this.deviceName;
    }
    
    /**
     * Returns the number of segments in this track
     * @return The number of segments
     */
    public int getNumberOfSegments()
    {
        return segments.size();
    }
    
    /**
     * Returns the array list with track points belonging to indicated segment
     * @param segment The segment to request the track points for
     * @return The array list with track points
     */
    public List<TrackPoint> getTrackPoints(int segment)
    {
        List<TrackPoint> points;
        if (segment>=0 && segment<segments.size())
        {
            points=segments.get(segment).getTrackPoints();
        }
        else
        {
            LOGGER.error("Illegal segment number {} while requesting trackpoints", segment);
            points=null;
        }
        return points;
    }

    /**
     * Returns the array list with track points belonging to indicated segment
     * @param segment The segment to request the track points for
     * @return The array list with track points
     */
    public List<TrackPoint> getCompressedTrackPoints(int segment)
    {
        List<TrackPoint> points;
        if (segment>=0 && segment<segments.size())
        {
            points=segments.get(segment).getCompressedTrackPoints();
        }
        else
        {
            LOGGER.error("Illegal segment number {} while requesting trackpoints", segment);
            points=null;
        }
        return points;
    }

    /**
     * Empties the track.
     */
    public void clear()
    {
        segments.clear();
        waypoints.clear();
    }

    /**
     * Returns an array of waypoints that were recorded during the track
     * @return A list of waypoints
     */
    public List<Location> getWaypoints()
    {
        return this.waypoints;
    }

    public List<TrackSegment> getSegments()
    {
        return segments;
    }

    /**
     * Create and apend a new segment to the track
     * @return segment 
     */
    public TrackSegment appendTrackSegment()
    {
        TrackSegment segment;
        segment=new TrackSegment();
        segments.add(segment);
        return segment;
    }
    
    /**
     * Returns the elapsed time in seconds
     * @return Elapsed time in seconds
     */
    public Long getElapsedTime()
    {
        return elapsedTime;
    }

    public Long getTimedTime()
    {
        return timedTime;
    }

    public Double getStartLat()
    {
        return startLat;
    }

    public Double getStartLon()
    {
        return startLon;
    }

    public Double getDistance()
    {
        return distance;
    }

    public Double getAverageSpeed()
    {
        return averageSpeed;
    }

    public Double getMaxSpeed()
    {
        return maxSpeed;
    }

    public Integer getAscent()
    {
        return ascent;
    }

    public Integer getDescent()
    {
        return descent;
    }

    public String getSport()
    {
        return this.sport;
    }
    
    public String getSubSport()
    {
        return subSport;
    }
    
    public String getSportDescription()
    {
        String desc;
        if (sport!=null && subSport!=null)
        {
            desc=sport+" - "+subSport;
        }
        else
        {
            desc=null;
        }
        return desc;
    }

    public String getManufacturer()
    {
        return manufacturer;
    }

    public String getProduct()
    {
        return product;
    }

    public Long getSerialNumber()
    {
        return serialNumber;
    }

    public String getFileType()
    {
        return fileType;
    }

    public String getSoftwareVersion()
    {
        return softwareVersion;
    }

    public Double getGrit()
    {
        return grit;
    }

    public Double getFlow()
    {
        return flow;
    }

    public Double getCalories()
    {
        return calories;
    }

    public Integer getJumpCount()
    {
        return jumpCount;
    }

    public String getMode()
    {
        return mode;
    }

    public String getFitFileName()
    {
        return fitFileName;
    }

    public ZonedDateTime getStartTime()
    {
        return startTime;
    }

    public ZonedDateTime getEndTime()
    {
        return endTime;
    }

    public String getStartDate()
    {
        return startTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
    
    public int getInvalidCoordinates()
    {
        return invalidCoordinates;
    }

    public int getValidCoordinates()
    {
        return validCoordinates;
    }
    
    /**
     * Returns the maxError value used for compressing the track
     * @return The maxError value.
     */
    public double getMaxError()
    {
        return compressionMaxError;
    }
}
