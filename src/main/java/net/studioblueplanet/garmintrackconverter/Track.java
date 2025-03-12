/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import net.studioblueplanet.garmintrackconverter.TrackPoint.TrackPointBuilder;
import net.studioblueplanet.fitreader.FitReader;
import net.studioblueplanet.fitreader.FitMessage;
import net.studioblueplanet.fitreader.FitMessageRepository;
import net.studioblueplanet.fitreader.FitGlobalProfile;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
        
/**
 * This class represents a Track consisting of track segments (consisting of 
 * Track Points), waypoints or both. 
 * If the Track contains segments, each segment maintains 4 series of Track Points:
 * raw, raw compressed, smoothed, smoothed compressed. By means of setting 
 * the behaviour of the track, it behaves accordingly.
 * @author Jorgen
 */
public class Track
{
    public static final int                 MS_PER_S                =1000;
    public static final int                 CM_PER_M                =100;
    public static final double              KMH_PER_MS              =3.6;
    private static final long               MAX_TIMEDTIME_DEVIATION =2L;

    private final static Logger             LOGGER      = LogManager.getLogger(Track.class);

    private boolean                         behaviourSmoothed;
    private boolean                         behaviourCompressed;
    private TrackSession                    session;
    private final TrackSegmentList          segments;
    private final List<Location>            waypoints;
    
    private String                          fitFileName;
    
    // Info from device_info and file_id
    private String                          deviceName;
    private String                          manufacturer;
    private String                          product;
    private Long                            serialNumber;
    private String                          fileType;
    private String                          softwareVersion;

    // Device Settings
    private Long                            timeOffset;         // s - offset from system time

    // Device Info
    private String                          deviceExternalHrBle;
    private String                          deviceExternalHrAnt;
    private String                          deviceBarometer;
    private String                          deviceGps;
    
    private final double                    smoothingAccuracy;   // m
    private double                          compressionMaxError; // m
    
    private int                             invalidCoordinates;
    private int                             validCoordinates;
    
    private String                          segmentSource;
    
    /**
     * Constructor
     * @param trackFileName Track file
     * @param deviceName Description of the device that recorded the track
     * @param compressionMaxError Maximum allowed error in m when compressing track
     * @param smoothingAccuracy Assumed default value of GPS accuracy in m when the GPS does not provide it
     */
    public Track(String trackFileName, String deviceName, double compressionMaxError, double smoothingAccuracy)
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
        String                  deviceType;
        String                  source;
        String                  deviceIndex;
        
        this.compressionMaxError =compressionMaxError;
        this.smoothingAccuracy   =smoothingAccuracy;
        segmentSource            ="Unknown";
        
        isCourse        =false;
        fitFileName     =new File(trackFileName).getName();
        segments        =new TrackSegmentList();
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

        //device_settings
        message    =repository.getFitMessage("device_settings");
        if (message!=null)
        {
            if (message.getNumberOfRecords()>1)
            {
                LOGGER.info("Found more than one device_settings record");
            }
            timeOffset=message.getIntValue(0, "time_offset");
        }

        //device_info
        message    =repository.getFitMessage("device_info");
        if (message!=null)
        {
            for (int i=0; i<message.getNumberOfRecords();i++)
            {
                id              =(int)message.getIntValue(i, "device_index");
                deviceIndex     =FitGlobalProfile.getInstance().getTypeValueName("device_index", id);
                id              =(int)message.getIntValue(i, "source_type");
                source          =FitGlobalProfile.getInstance().getTypeValueName("source_type", id);
                id              =(int)message.getIntValue(i, "device_type");

                // source is used on Edge 1040 and Fenix 7, not on GPSMAP 67
                if ("local".equals(source) || source==null)
                {
                    deviceType=FitGlobalProfile.getInstance().getTypeValueName("local_device_type", id);

                    if ("creator".equals(deviceIndex))
                    {
                        id              =(int)message.getIntValue(i, "manufacturer");
                        manufacturer    =FitGlobalProfile.getInstance().getTypeValueName("manufacturer", id);
                        id              =(int)message.getIntValue(i, "product");
                        product         =FitGlobalProfile.getInstance().getTypeValueName("garmin_product", id);
                        serialNumber    =message.getIntValue(i, "serial_number");
                        double version  =message.getScaledValue(i, "software_version");
                        softwareVersion =String.format("%.2f", version);                    
                    }
                    if ("barometer".equals(deviceType))
                    {
                        id              =(int)message.getIntValue(i, "manufacturer");
                        deviceBarometer =FitGlobalProfile.getInstance().getTypeValueName("manufacturer", id);
                        deviceBarometer +=" ";  
                        id              =(int)message.getIntValue(i, "product");
                        deviceBarometer +=FitGlobalProfile.getInstance().getTypeValueName("garmin_product", id);
                        double version  =message.getScaledValue(i, "software_version");
                        deviceBarometer +=String.format(", software version: %.2f", version);                    
                    }
                    if ("gps".equals(deviceType) || "glonass".equals(deviceType))
                    {
                        id              =(int)message.getIntValue(i, "manufacturer");
                        deviceGps       =FitGlobalProfile.getInstance().getTypeValueName("manufacturer", id);
                        deviceGps       +=" ";  
                        id              =(int)message.getIntValue(i, "product");
                        deviceGps       +=FitGlobalProfile.getInstance().getTypeValueName("garmin_product", id);
                        double version  =message.getScaledValue(i, "software_version");
                        deviceGps       +=String.format(", software version: %.2f", version);                    
                    }
                }
                else if ("bluetooth_low_energy".equals(source))
                {
                    deviceType=FitGlobalProfile.getInstance().getTypeValueName("ble_device_type", id);
                    if ("heart_rate".equals(deviceType))
                    {
                        deviceExternalHrBle=processHrDeviceInfo(message, i, source);
                    }
                }
                else if ("antplus".equals(source))
                {
                    deviceType=FitGlobalProfile.getInstance().getTypeValueName("antplus_device_type", id);
                    if ("heart_rate".equals(deviceType))
                    {
                        deviceExternalHrAnt=processHrDeviceInfo(message, i, source);
                    }
                }
            }
        }
        
        if (trackMessages!=null && trackMessages.size()>0)
        {
            // Extract the list of trackpoints
            List<TrackPoint> thePoints=getTrackPoints(trackMessages);
            // On the Fenix 7 it has been observed that not always 
            // trackpoints are stored in chronological order. Therefore
            // sort the trackpoints to get them in chronological order.
            // For courses we don't want this sorting because timestamps
            // of the course points may not have a meaning
            if (!isCourse)
            {
                thePoints.sort((o1, o2) -> o1.compareTo(o2));
            }

            // Parse sessions
            if (sessionMessages!=null && sessionMessages.size()>0)
            {
                // Get data from session
                session=new TrackSession(sessionMessages);
            }            
            
            // Get track segments from timer start/stop; only works for tracks
            segments.getSegmentsFromEvents(eventMessages);
            segmentSource="Events";

            // If no segments found, try to get them from the laps
            if (segments.isEmpty())
            {
                if (lapMessages!=null && lapMessages.size()>0)
                {
                    // Get data from session
                    segments.parseLaps(lapMessages);
                    segmentSource="Laps";
                }
                else
                {
                    LOGGER.error("Unable to extract segments from track or route");
                }
            }
            
            // When logging long segments (>3h) on the GPSMAP 66sr and GPSMAP 67 the 
            // times of the segments are incorrect. Only the end time of last segment
            // is correct.
            // The timedTime in the session however is 
            // correct. We use this to detect if this is the case: if the 
            // the summed timeTime over the segments deviates to much from the 
            // session timedTime we assume the GPSMAP error
            // If it is, we derive segments from the list of trackpoints.
            long timedTime=segments.getTimedTime();
            if (session!=null && Math.abs(timedTime-session.getTimedTime())>MAX_TIMEDTIME_DEVIATION)
            {
                LOGGER.warn("Timed time in segments ({} sec) differs from session ({} sec)", timedTime, session.getTimedTime());
                // Get the segments from the TrackPoints and add the points to these segments
                segments.getSegmentsFromTrackPoints(thePoints);
                segmentSource="TrackPoints";
            }
            else
            {
                // Add trackpoints to segments
                addTrackpointsToSegments(thePoints);                
            }

            this.deviceName=deviceName;
           
            smoothTrack();
            
            compressTrack(compressionMaxError);
        }
        else 
        {
            compressionMaxError=0.0;
        }
    }
    
    /**
     * Processes DeviceInfo message containing external HR info
     * @param message Fit DeviceInfo message
     * @param index Index in message
     * @param source Source value
     * @return String with info
     */
    private String processHrDeviceInfo(FitMessage message, int index, String source)
    {
        FitGlobalProfile p=FitGlobalProfile.getInstance();
        String  info        ="";
        
        int id              =(int)message.getIntValue(index, "manufacturer");
        if (id!=65535)
        {
            info            +=p.getTypeValueName("manufacturer", id)+" ";
            if (id==1)
            {
                id          =(int)message.getIntValue(index, "product");
                if (id!=65535)
                {
                    info    +=p.getTypeValueName("garmin_product", id)+" ";
                }
            }
        }
        info                +="serial: "         +message.getIntValue(index, "serial_number");
        
        info                +=" battery: ";
        id                  =(int)message.getIntValue(index, "battery_status");
        if (id!=65535)
        {
            info            += p.getTypeValueName("battery_status", id)+" ";
        }
        double battVoltage  =message.getScaledValue(index, "battery_voltage");
        if (battVoltage<255.0)
        {
            info            +=String.format("%4.3f V ", battVoltage);
        }
        int    battLevel    =(int)message.getIntValue(index, "battery_level");
        if (battLevel<=100)
        {
            info            +=battLevel+"% ";
        }
        
        long time           =message.getIntValue(index, "cum_operating_time");
        if (time<4294967295L)
        {
            info            +="operating time: "+Toolbox.toTimeIntervalString(time)+" ";
        }
        
        id                  =(int)message.getIntValue(index, "sensor_position"); 
        if (id<255)
        {
            info            +="body position: "+p.getTypeValueName("body_location", id)+" ";
        }

        id                  =(int)message.getIntValue(index, "ant_network");
        info                +="type: "+source+"/"+FitGlobalProfile.getInstance().getTypeValueName("ant_network", id);
        
        LOGGER.info("External HR: {}", info);  
        return info;
    }
    
    
    /**
     * Constructor for a simple track.
     * @param compressionMaxError Maximum allowed error in m when compressing 
     *        track; not used
     * @param smoothingAccuracy Assumed default value of GPS accuracy in m when 
     *        the GPS does not provide it; not used
     */
    public Track(double compressionMaxError, double smoothingAccuracy)
    {
        session                 =new TrackSession();
        segments                =new TrackSegmentList();
        waypoints               =new ArrayList<>();
        this.compressionMaxError=compressionMaxError;
        this.smoothingAccuracy  =smoothingAccuracy;
        setBehaviour(false, false);
    }
    
    /**
     * This method sets the behaviour of the track. Both parameters may be set.
     * @param smoothed The track is smoothed
     * @param compressed The track is compressed
     */
    public void setBehaviour(boolean smoothed, boolean compressed)
    {
        behaviourSmoothed   =smoothed;
        behaviourCompressed =compressed;
        segments.setBehaviour(smoothed, compressed);
    }
    
    /**
     * Returns whether this track behaves like a smoothed track
     * @return True if smoothed, false if not
     */
    public boolean getBehaviourSmoothing()
    {
        return behaviourSmoothed;
    }

    /**
     * Returns whether this track behaves like a compressed track
     * @return True if compressed, false if not
     */
    public boolean getBehaviourCompression()
    {
        return behaviourCompressed;
    }
    
    /**
     * This method parses the FIT activity record. It destiles the track points
     * @param trackMessages The record holding the 'activity' information
     * @return List of all the valid points found
     */
    private List<TrackPoint> getTrackPoints(List<FitMessage> trackMessages)
    {
        Double                  lat;
        Double                  lon;
        ZonedDateTime           dateTime;
        int                     size;
        TrackPoint              point;
        boolean                 found;

        List<TrackPoint> thePoints  =new ArrayList<>();
        invalidCoordinates          =0;
        validCoordinates            =0;
        for (FitMessage message:trackMessages)
        {
            size            =message.getNumberOfRecords();
            for (int i=0; i<size; i++)
            {
                dateTime                    =message.getTimeValue(i, "timestamp");
                lat                         =message.getLatLonValue(i, "position_lat");
                lon                         =message.getLatLonValue(i, "position_long");
                TrackPointBuilder builder   =new TrackPoint.TrackPointBuilder(lat, lon);
                builder.dateTime(dateTime);
                
                if (message.hasField("enhanced_altitude"))
                {
                    builder.elevation(message.getScaledValue(i, "enhanced_altitude"));
                }
                else if (message.hasField("corrected_altitude"))
                {
                    builder.elevation(message.getScaledValue(i, "corrected_altitude"));
                }
                    
                if (message.hasField("temperature"))
                {
                    int temp=(int)message.getIntValue(i, "temperature");
                    // quick and dirty check if value is valid (GPSMAP67)
                    if (temp<127)
                    {
                        builder.temperature(temp);
                    }
                }
                
                if (message.hasField("enhanced_speed"))
                {
                    builder.speed(message.getScaledValue(i, "enhanced_speed"));
                }
                else if (message.hasField("speed"))
                {
                    builder.speed(message.getScaledValue(i, "speed"));
                }
                    
                if (message.hasField("distance"))
                {
                    double dist=message.getScaledValue(i, "distance");
                    // quick and dirty check if value is valid (GPSMAP67)
                    if (dist>42949672.0)
                    {
                        dist=0.0;
                    }
                    builder.distance(dist);
                }

                if (message.hasField("heart_rate"))
                {
                    builder.heartrate((int)message.getIntValue(i, "heart_rate"));
                }

                if (message.hasField("enhanced_respiration_rate"))
                {
                    builder.respirationrate((double)message.getScaledValue(i, "enhanced_respiration_rate"));
                }

                if (message.hasField("stamina"))
                {
                    builder.stamina((int)message.getIntValue(i, "stamina"));
                }

                if (message.hasField("stamina_potential"))
                {
                    builder.staminaPotential((int)message.getIntValue(i, "stamina_potential"));
                }

                if (message.hasField("gps_accuracy"))
                {
                    builder.gpsAccuracy((int)message.getIntValue(i, "gps_accuracy")*CM_PER_M); // in cm
                    builder.ehpe((int)message.getIntValue(i, "gps_accuracy")*CM_PER_M);
                }
                else
                {
                    // If no gps accuracy, use the default value set
                    builder.gpsAccuracy((int)(smoothingAccuracy*CM_PER_M)); // in cm
                }
                
                if (message.hasField("not found 143"))
                {
                    builder.unknown((int)message.getIntValue(i, "not found 143"));
                }
                
                point       =builder.build();
                if (point.isValid())
                {
                    thePoints.add(point);
                    validCoordinates++;
                }
                else
                {
                    LOGGER.error(String.format("Illegal lat/lon at %s [%7.4f, %7.4f]", 
                                 dateTime.toString(), lat, lon));
                    invalidCoordinates++;
                }
                LOGGER.debug("Trackpoint {} ({}, {})",
                                 dateTime.toString(),
                                 lat, lon);
            }
        }
        LOGGER.info("Good coordinates {}, wrong coordinates: {}", validCoordinates, invalidCoordinates);
        return thePoints;
    }

    /**
     * This method parses the list of found TrackPoints and tries to add
     * each point to a segment.
     * @param points Points to add to the segments
     */
    private void addTrackpointsToSegments(List<TrackPoint> points)
    {
        for(TrackPoint p:points)
        {
            boolean found=segments.addTrackPointToSegment(p);
            if (!found)
            {
                LOGGER.error(String.format("No segment found to add trackpoint @%s} [%7.4f, %7.4f] to", 
                             p.getDateTime().toString(), p.getLatitude(), p.getLongitude()));
            }            
        }
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
        
        for (Location waypoint : allWaypoints)
        {
            dateTime=waypoint.getDateTime();
            if (dateTime==null)
            {
                LocalDateTime localDateTime =waypoint.getLocalDateTime();
                if (localDateTime!=null)
                {
                    ZoneId zone             =session.getStartTime().getZone();
                    dateTime                =ZonedDateTime.ofInstant(localDateTime, ZoneOffset.ofTotalSeconds(timeOffset.intValue()), zone);
                }
            }
            
            if (segments.isDatetimeInSegment(dateTime))
            {
                // Now we have assigned the waypoint to this track, 
                // we can set the zoned date time using the timezone of the track
                waypoint.setDateTime(dateTime);
                waypoints.add(waypoint);                
            }
        }
    }

    /**
     * Set the waypoints that were read from the locations.fit file. 
     * Only the waypoints that were recorded during the track are added 
     * to the track. Existing waypoints on the track will be removed
     * @param allWaypoints The waypoints read from the device
     */
    public void setTrackWaypoints(List<Location> allWaypoints)
    {
        waypoints.clear();
        addTrackWaypoints(allWaypoints);
    }
    
    /**
     * Adds a waypoint to the track
     * @param waypoint Waypoint to add
     */
    public void addWaypoint(Location waypoint)
    {
        waypoints.add(waypoint);
    }
    
    /**
     * Return some info about this track
     * @return The info
     */
    public String getTrackInfo()
    {
        int             noOfSegments;
        TrackSegment    segment;
        
        noOfSegments    =segments.size();
        
        String sport    =session.getSport();
        String subSport =session.getSubSport();
        String info     ="Track ";
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
        for (int i=0; i<noOfSegments; i++)
        {
            segment=segments.get(i);
            info+=segment.getNumberOfTrackPoints();
            if (i<noOfSegments-1)
            {
                info+=", ";
            }
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
        String sport    =session.getSport();
        String subSport =session.getSubSport();
        String info     ="Activity: ";
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
        if (behaviourSmoothed)
        {
            info+=" (smoothed)";
        }
        
        int points  =segments.getSegments().stream()
                             .map(seg -> seg.getNumberOfTrackPointsUncompressed())
                             .mapToInt(Integer::valueOf)
                             .sum();
        int cpoints =segments.getSegments().stream()
                             .map(seg -> seg.getNumberOfTrackPointsCompressed())
                             .mapToInt(Integer::valueOf)
                             .sum();
        int percentage=0;
        if (points>0)
        {
            percentage=(cpoints*100/points);
        }
        info+="\nSegments: "+this.segments.size()+" (based on "+segmentSource+"), points: "+points+
              ", compressed: "+cpoints+" ("+
               percentage+"%), waypoints: "+waypoints.size();
        
        if (invalidCoordinates+validCoordinates>0)
        {
            percentage=100*invalidCoordinates/(invalidCoordinates+validCoordinates);
        }
        else
        {
            percentage=0;
        }
        
        info+="\nValid points: "+validCoordinates+", invalid points: "+invalidCoordinates+
              " ("+percentage+"%, omitted)";
        info+="\nDevice: "+this.deviceName+", sw: "+this.softwareVersion;
        if (deviceExternalHrBle!=null)
        {
            info+=" with external HR sensor: "+deviceExternalHrBle;
        }
        else if (deviceExternalHrAnt!=null)
        {
            info+=" with external HR sensor: "+deviceExternalHrAnt;
        }

        return info;
    }

    
    private void sortSegments()
    {
        segments.sortSegments();
    }
    
    private void smoothTrack()
    {
        segments.smoothSegments();
    }
    
    
    /* ******************************************************************************************* *\
     * TRACK COMPRESSING - DOUGLASS-PEUCKER ALGORITHM
    \* ******************************************************************************************* */
    private void compressTrack(double maxError)
    {
        compressionMaxError=maxError;
        segments.compressTrackSegments(maxError);

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
    
    /**
     * Returns the number of waypoints associated with this track
     * @return The number of waypoints
     */
    public int getNumberOfWaypoints()
    {
        return waypoints.size();
    }

    public List<TrackSegment> getSegments()
    {
        return segments.getSegments();
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
        return session.getElapsedTime();
    }

    public Long getTimedTime()
    {
        return session.getTimedTime();
    }

    public Double getStartLat()
    {
        return session.getStartLat();
    }

    public Double getStartLon()
    {
        return session.getStartLon();
    }

    public Double getDistance()
    {
        return session.getDistance();
    }

    public Double getAverageSpeed()
    {
        return session.getAverageSpeed();
    }

    public Double getMaxSpeed()
    {
        return session.getMaxSpeed();
    }

    public Integer getAscent()
    {
        return session.getAscent();
    }

    public Integer getDescent()
    {
        return session.getDescent();
    }

    public void setSport(String sport)
    {
        session.setSport(sport);
    }
    
    public String getSport()
    {
        return session.getSport();
    }
    
    public String getSubSport()
    {
        return session.getSubSport();
    }
    
    public String getSportDescription()
    {
        String desc;
        String sport    =session.getSport();
        String subSport =session.getSubSport();
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
        return session.getGrit();
    }

    public Double getFlow()
    {
        return session.getFlow();
    }

    public Double getCalories()
    {
        return session.getCalories();
    }

    public Integer getJumpCount()
    {
        return session.getJumpCount();
    }

    public String getMode()
    {
        return session.getMode();
    }

    public String getFitFileName()
    {
        return fitFileName;
    }

    public ZonedDateTime getStartTime()
    {
        return session.getStartTime();
    }

    public ZonedDateTime getEndTime()
    {
        return session.getEndTime();
    }

    public String getStartDate()
    {
        return session.getStartTime().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
    
    public int getInvalidCoordinates()
    {
        return invalidCoordinates;
    }

    public int getValidCoordinates()
    {
        return validCoordinates;
    }
    
    public String getDeviceExternalHr()
    {
        String returnValue=null;
        if (deviceExternalHrBle!=null)
        {
            returnValue=deviceExternalHrBle;
        }
        else if (deviceExternalHrAnt!=null)
        {
            returnValue=deviceExternalHrAnt;
        }
        return returnValue;
    }
    
    /**
     * Return device info Barometer
     * @return Description of the barometer
     */
    public String getDeviceBarometer()
    {
        return this.deviceBarometer;
    }
    
    /**
     * Return device info Gps
     * @return Description of the Gps
     */
    public String getDeviceGps()
    {
        return this.deviceGps;
    }
    
    /**
     * Returns the maxError value used for compressing the track
     * @return The maxError value.
     */
    public double getCompressionMaxError()
    {
        return compressionMaxError;
    }

    /**
     * Get a description of how the sessions were derived.
     * @return 'Unknown', 'Events', 'Laps', 'TrackPoints'
     */
    public String getSegmentSource()
    {
        return segmentSource;
    }
}
