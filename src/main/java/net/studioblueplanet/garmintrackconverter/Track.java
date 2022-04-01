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
import hirondelle.date4j.DateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
        

/**
 * This class represents a track consisting of track segments and optional
 * a number of logged waypoints
 * @author Jorgen
 */
public class Track
{
    private final static Logger             LOGGER = LogManager.getLogger(Track.class);
    private final List<TrackSegment>        segments;
    private final List<Location>            waypoints;

    private String                          deviceName;
    private String                          sport;
    private String                          subSport;
    private String                          manufacturer;
    private String                          product;
    private long                            serialNumber;
    private String                          fileType;
    private String                          softwareVersion;

    private double                          elapsedTime;    // ms
    private double                          timedTime;      // ms
    private double                          startLat;
    private double                          startLon;
    private double                          distance;       // m
    private double                          averageSpeed;   // m/s
    private double                          maxSpeed;       // m/s
    private int                             ascent;         // m
    private int                             descent;        // m
    private double                          grit;           // kGrit
    private double                          flow;           // FLOW
    private double                          calories;       // cal
    private int                             jumpCount;      // 
    private String                          mode;           //
    
    
    private static final int                TIMEREVENT=0;
    private static final int                TIMEREVENT_TIMERSTARTED=0;
    private static final int                TIMEREVENT_TIMERSTOPPED=4;
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
        
        segments        =new ArrayList<>();
        waypoints       =new ArrayList<>();
        
        reader          =FitReader.getInstance();
        repository      =reader.readFile(trackFileName);
        lapMessages     =repository.getAllMessages("lap");
        trackMessages   =repository.getAllMessages("record");
        sessionMessages =repository.getAllMessages("session");
        eventMessages   =repository.getAllMessages("event");
        
        // Sport
        /*
        message    =repository.getFitMessage("sport");
        if (message!=null)
        {
            id=(int)message.getIntValue(0, "sport");
            sport=FitGlobalProfile.getInstance().getTypeValueName("sport", id);
        }
        */
        
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
            softwareVersion =String.format("%5.2f", version);
        }
        
        if (lapMessages!=null && trackMessages!=null)
        {
            // Get data from session
            this.parseSessions(sessionMessages);
            // Get track segments from timer start/stop
            this.getSegmentsFromEvents(eventMessages);
            // Add trackpoints to segments
            this.addTrackpointsToSegments(trackMessages);

            this.deviceName=deviceName;
            //repository.dumpMessageDefintions();
        }
    }
    
    public Track()
    {
        segments        =new ArrayList<>();
        waypoints       =new ArrayList<>();
    }
    

    /**
     * This method parses the FIT lap record and destilates the number of laps.
     * @param lapMessages The FIT record holding the 'lap' info
     */
    private void parseLaps(List<FitMessage> lapMessages)
    {
        int i;
        int size;
        DateTime                startTime;
        DateTime                endTime;
        double                  elapsedTime;
        TrackSegment            segment;
        
        for (FitMessage message:lapMessages)
        {
            size            =message.getNumberOfRecords();
            i=0;
            while (i<size)
            {
                endTime     =message.getTimeValue(i, "timestamp");
                startTime   =message.getTimeValue(i, "start_time");
                elapsedTime =message.getIntValue(i, "total_elapsed_time")/1000;

                segment     =new TrackSegment(startTime, endTime);
                segments.add(segment);

                if (startTime!=null && endTime!=null)
                {
                    LOGGER.debug("Lap {} {} - {} {} s", 
                                 message.getIntValue(i, "message_index"),
                                 startTime.toString(),
                                 endTime.toString(),
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
        DateTime                startTime;
        DateTime                endTime;
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
                elapsedTime =message.getIntValue(i, "total_elapsed_time");
                timedTime   =message.getIntValue(i, "total_timer_time");

                startLat    =message.getLatLonValue(i, "start_position_lat");
                startLon    =message.getLatLonValue(i, "start_position_lon");
                
                distance    =message.getScaledValue(i, "total_distance");
                averageSpeed=message.getScaledValue(i, "avg_speed");
                maxSpeed    =message.getScaledValue(i, "max_speed");
                grit        =message.getFloatValue(i, "total_grit");
                flow        =message.getFloatValue(i, "avg_flow");
                calories    =message.getScaledValue(i, "total_calories");
                ascent      =(int)message.getIntValue(i, "total_ascent");
                descent     =(int)message.getIntValue(i, "total_descent");
                jumpCount   =(int)message.getIntValue(i, "jump_count");
                mode        =message.getStringValue(i, "mode");
                
                id=(int)message.getIntValue(0, "sport");
                sport=FitGlobalProfile.getInstance().getTypeValueName("sport", id);
                id=(int)message.getIntValue(0, "sub_sport");
                subSport=FitGlobalProfile.getInstance().getTypeValueName("sub_sport", id);
                
                if (startTime!=null && endTime!=null)
                {
                    LOGGER.info("SESSION        : {}", message.getIntValue(i, "message_index"));
                    LOGGER.info("Time           : {}-{}", startTime.format("YYYY-MM-DD hh:mm:ss"), endTime.format("YYYY-MM-DD hh:mm:ss"));
                    LOGGER.info("Duration       : {}/{} sec", elapsedTime/1000, timedTime/1000);
                    LOGGER.info("Distance       : {} km", distance);
                    LOGGER.info("Speed          : average {}, max {} km/h", averageSpeed*3.6, maxSpeed*3.6);
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
     * This method parses the FIT lap record and destillates the number of sessions.
     * @param lapMessages The FIT record holding the 'lap' info
     */
    private void getSegmentsFromEvents(List<FitMessage> eventMessages)
    {
        int i;
        int size;
        DateTime                startTime;
        DateTime                endTime;
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
                            LOGGER.info("Segment found: {} - {}", startTime.format("YYYY-MM-DD hh:mm:ss"), endTime.format("YYYY-MM-DD hh:mm:ss"));
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
        double                  lat;
        double                  lon;
        double                  ele;
        DateTime                dateTime;

        double                  speed;
        double                  distance;
        int                     temp;
        int                     i;
        int                     size;
        TrackPoint              point;
        Iterator<TrackSegment>  it;
        boolean                 found;
        TrackSegment            segment;


        for (FitMessage message:trackMessages)
        {
            size            =message.getNumberOfRecords();
            i=0;
            while (i<size)
            {
                dateTime    =message.getTimeValue(i, "timestamp");
                lat         =message.getLatLonValue(i, "position_lat");
                lon         =message.getLatLonValue(i, "position_long");
                ele         =message.getScaledValue(i, "altitude");
                temp        =(int)message.getIntValue(i, "temperature");
                speed       =message.getScaledValue(i, "speed");
                distance    =message.getScaledValue(i, "distance");

                point       =new TrackPoint(dateTime, lat, lon, ele, speed, distance, temp);

                found=false;
                it=this.segments.iterator();
                while (it.hasNext() && !found)
                {
                    segment=it.next();
                    if (segment.isInLap(dateTime))
                    {
                        segment.addTrackPoint(point);
                        found=true;
                    }
                }
                if (!found)
                {
                    LOGGER.error("No segment found to add trackpoint @ {} to", dateTime.format("YYYY-MM-DD hh:mm:ss"));
                }

                LOGGER.debug("Track {} ({}, {}) ele {}, {} km/h, {} m",
                                 dateTime.toString(),
                                 lat, lon,
                                 ele, 
                                 speed, 
                                 distance);
                i++;
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
        DateTime                dateTime;
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
                if (segment.isInLap(dateTime))
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

    public List<TrackSegment> getSegments()
    {
        return segments;
    }

    /**
     * Returns the name of the sport
     * @return Sports name or null if not retrieved from the FIT file
     */
    public String getSport()
    {
        return this.sport;
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
    public double getElapsedTime()
    {
        return elapsedTime;
    }

    public double getTimedTime()
    {
        return timedTime;
    }

    public double getStartLat()
    {
        return startLat;
    }

    public double getStartLon()
    {
        return startLon;
    }

    public double getDistance()
    {
        return distance;
    }

    public double getAverageSpeed()
    {
        return averageSpeed;
    }

    public double getMaxSpeed()
    {
        return maxSpeed;
    }

    public int getAscent()
    {
        return ascent;
    }

    public int getDescent()
    {
        return descent;
    }

    public String getSubSport()
    {
        return subSport;
    }

    public String getManufacturer()
    {
        return manufacturer;
    }

    public String getProduct()
    {
        return product;
    }

    public long getSerialNumber()
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

    public double getGrit()
    {
        return grit;
    }

    public double getFlow()
    {
        return flow;
    }

    public double getCalories()
    {
        return calories;
    }

    public int getJumpCount()
    {
        return jumpCount;
    }

    public String getMode()
    {
        return mode;
    }

}
