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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
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
    private static final int                TIMEREVENT              =0;
    private static final int                TIMEREVENT_TIMERSTARTED =0;
    private static final int                TIMEREVENT_TIMERSTOPPED =4;

    private final static Logger             LOGGER      = LogManager.getLogger(Track.class);

    private boolean                         behaviourSmoothed;
    private boolean                         behaviourCompressed;
    private final List<TrackSegment>        segments;
    private final List<Location>            waypoints;
    
    private String                          fitFileName;
    
    // Info from device_info and file_id
    private String                          deviceName;
    private String                          manufacturer;
    private String                          product;
    private Long                            serialNumber;
    private String                          fileType;
    private String                          softwareVersion;

    // Info from session
    private String                          sport;
    private String                          subSport;
    private ZonedDateTime                   startTime;
    private ZonedDateTime                   endTime;
    private Long                            elapsedTime;    // s
    private Long                            timedTime;      // s
    private Double                          startLat;       // degrees
    private Double                          startLon;       // degrees
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
    private String                          deviceExternalHr;
    private String                          deviceBarometer;
    private String                          deviceGps;
    
    private final double                    smoothingAccuracy;   // m
    private double                          compressionMaxError; // m
    
    private int                             invalidCoordinates;
    private int                             validCoordinates;
    
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
        String                  antNetwork;
        
        this.compressionMaxError =compressionMaxError;
        this.smoothingAccuracy   =smoothingAccuracy;
        
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
            for (int i=0; i<message.getNumberOfRecords();i++)
            {
                id              =(int)message.getIntValue(i, "device_index");
                deviceIndex     =FitGlobalProfile.getInstance().getTypeValueName("device_index", id);
                id              =(int)message.getIntValue(i, "source_type");
                source          =FitGlobalProfile.getInstance().getTypeValueName("source_type", id);
                id              =(int)message.getIntValue(i, "device_type");

                if ("local".equals(source))
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
                        id                  =(int)message.getIntValue(i, "manufacturer");
                        deviceBarometer =FitGlobalProfile.getInstance().getTypeValueName("manufacturer", id);
                        deviceBarometer +=" ";  
                        id                  =(int)message.getIntValue(i, "product");
                        deviceBarometer +=FitGlobalProfile.getInstance().getTypeValueName("garmin_product", id);
                        double version  =message.getScaledValue(i, "software_version");
                        deviceBarometer +=String.format(", software version: %.2f", version);                    
                    }
                    if ("gps".equals(deviceType))
                    {
                        id                  =(int)message.getIntValue(i, "manufacturer");
                        deviceGps       =FitGlobalProfile.getInstance().getTypeValueName("manufacturer", id);
                        deviceGps       +=" ";  
                        id                  =(int)message.getIntValue(i, "product");
                        deviceGps       +=FitGlobalProfile.getInstance().getTypeValueName("garmin_product", id);
                        double version      =message.getScaledValue(i, "software_version");
                        deviceGps       +=String.format(", software version: %.2f", version);                    
                    }
                }
                else if ("bluetooth_low_energy".equals(source))
                {
                    deviceType=FitGlobalProfile.getInstance().getTypeValueName("ble_device_type", id);
                    if ("heart_rate".equals(deviceType))
                    {
                        id              =(int)message.getIntValue(i, "ant_network");
                        antNetwork      =FitGlobalProfile.getInstance().getTypeValueName("ant_network", id);

                        deviceExternalHr="serial: "+message.getIntValue(i, "serial_number")+
                                         " battery: "+message.getIntValue(i, "battery_level")+
                                         "% source: "+source+
                                         "/"+antNetwork;
                    }
                }
            }
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
            if (segments.isEmpty())
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
                sortSegments();
            }
            
            smoothTrack();
            
            compressTrack(compressionMaxError);
        }
        else 
        {
            compressionMaxError=0.0;
        }
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
        segments                =new ArrayList<>();
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
        for (TrackSegment segment : segments)
        {
            segment.setBehaviour(smoothed, compressed);
        }
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
     * This method parses the FIT lap record and distils the number of laps.
     * @param lapMessages The FIT record holding the 'lap' info
     */
    private void parseLaps(List<FitMessage> lapMessages)
    {
        int size;
        ZonedDateTime           segmentStartTime;
        ZonedDateTime           segmentEndTime;
        TrackSegment            segment;
        
        for (FitMessage message:lapMessages)
        {
            size                =message.getNumberOfRecords();
            for (int i=0; i<size; i++)
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
            }   
        }
        
    }
    
    /**
     * This method parses the FIT lap record and distils the number of sessions.
     * @param lapMessages The FIT record holding the 'lap' info
     */
    private void parseSessions(List<FitMessage> sessionMessages)
    {
        int                     size;
        int                     id;
        
        for (FitMessage message:sessionMessages)
        {
            size            =message.getNumberOfRecords();
            for(int i=0; i<size; i++)
            {
                endTime     =message.getTimeValue(i, "timestamp");
                startTime   =message.getTimeValue(i, "start_time");
                elapsedTime =message.getIntValue(i, "total_elapsed_time")/MS_PER_S;
                timedTime   =message.getIntValue(i, "total_timer_time")/MS_PER_S;

                startLat    =message.getLatLonValue(i, "start_position_lat");
                startLon    =message.getLatLonValue(i, "start_position_long");
                
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
            }   
        }
    }    
    
    /**
     * This method parses the FIT lap record and destilles the number of sessions.
     * @param lapMessages The FIT record holding the 'lap' info
     */
    private void getSegmentsFromEvents(List<FitMessage> eventMessages)
    {
        int                     size;
        ZonedDateTime           start;
        ZonedDateTime           end;
        TrackSegment            segment;
        boolean                 started;
        int                     event;
        int                     eventType;
        
        started     =false;
        start       =null;
        end         =null;
        for (FitMessage message:eventMessages)
        {
            size            =message.getNumberOfRecords();
            for(int i=0; i<size; i++)
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
                            end=message.getTimeValue(i, "timestamp");
                            segment     =new TrackSegment(start, end);
                            segments.add(segment);
                            LOGGER.info("Segment found: {} - {}", start.toString(), end.toString());
                        }
                    }
                    else
                    {
                        if (eventType==TIMEREVENT_TIMERSTARTED)
                        {
                            started=true;
                            start=message.getTimeValue(i, "timestamp");
                        }
                    }
                }
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
        ZonedDateTime           dateTime;
        int                     size;
        TrackPoint              point;
        boolean                 found;
        TrackSegment            segment;

        invalidCoordinates=0;
        validCoordinates=0;
        for (FitMessage message:trackMessages)
        {
            size            =message.getNumberOfRecords();
            for (int i=0; i<size; i++)
            {
                dateTime    =message.getTimeValue(i, "timestamp");
                lat         =message.getLatLonValue(i, "position_lat");
                lon         =message.getLatLonValue(i, "position_long");
                TrackPointBuilder builder=new TrackPoint.TrackPointBuilder(lat, lon);
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
                    builder.temperature((int)message.getIntValue(i, "temperature"));
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
                    builder.distance(message.getScaledValue(i, "distance"));
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
                    found=false;
                    for (int j=0; j<segments.size() && !found; j++)
                    {
                        segment=segments.get(j);
                        if (segment.isInSegment(dateTime))
                        {
                            segment.addTrackPoint(point);
                            found=true;
                        }
                    }
                    validCoordinates++;
                    if (!found)
                    {
                        LOGGER.error(String.format("No segment found to add trackpoint @%s} [%7.4f, %7.4f] to", 
                                     dateTime.toString(), lat, lon));
                    }
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
        Iterator<TrackSegment>  segmentIterator;
        TrackSegment            segment;
        boolean                 found;
        
        for (Location waypoint : allWaypoints)
        {
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
        String          info;
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
        if (behaviourSmoothed)
        {
            info+=" (smoothed)";
        }
        
        int points  =segments.stream()
                             .map(seg -> seg.getNumberOfTrackPointsUncompressed())
                             .mapToInt(Integer::valueOf)
                             .sum();
        int cpoints =segments.stream()
                             .map(seg -> seg.getNumberOfTrackPointsCompressed())
                             .mapToInt(Integer::valueOf)
                             .sum();
        int percentage=0;
        if (points>0)
        {
            percentage=(cpoints*100/points);
        }
        info+="\nSegments: "+this.segments.size()+", points: "+points+", compressed: "+cpoints+" ("+
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
        if (deviceExternalHr!=null)
        {
            info+=" with external HR sensor: "+deviceExternalHr;
        }
        return info;
    }

    
    private void sortSegments()
    {
        segments.forEach((segment) ->
        {
            segment.sortOnDateTime();
        });        
    }
    
    private void smoothTrack()
    {
        segments.forEach((segment) ->
        {
            segment.smooth();
        });
    }
    
    
    /* ******************************************************************************************* *\
     * TRACK COMPRESSING - DOUGLASS-PEUCKER ALGORITHM
    \* ******************************************************************************************* */
    private void compressTrack(double maxError)
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

    public void setSport(String sport)
    {
        this.sport=sport;
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
    
    public String getDeviceExternalHr()
    {
        return this.deviceExternalHr;
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
}
