/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import net.studioblueplanet.fitreader.FitReader;
import net.studioblueplanet.fitreader.FitMessage;
import net.studioblueplanet.fitreader.FitMessageRepository;
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
        List<FitMessage>        trackMessages;
        
        segments        =new ArrayList<>();
        waypoints       =new ArrayList<>();
        
        reader          =FitReader.getInstance();
        repository      =reader.readFile(trackFileName);
        lapMessages     =repository.getAllMessages("lap");
        trackMessages   =repository.getAllMessages("record");

        repository.dumpMessageDefintions();
        
        if (lapMessages!=null && trackMessages!=null)
        {
            this.parseLaps(lapMessages);
            this.parseTrackPoints(trackMessages);

            this.deviceName=deviceName;
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

                segment     =new TrackSegment(startTime, endTime, elapsedTime);
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
     * This method parses the FIT activity record. It destiles the track points
     * @param trackMessages The record holding the 'activity' information
     */
    private void parseTrackPoints(List<FitMessage> trackMessages)
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
                ele         =message.getAltitudeValue(i, "altitude");
                temp        =message.getIntValue(i, "temperature");
                speed       =message.getSpeedValue(i, "speed");
                distance    =message.getDistanceValue(i, "distance");

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
                    LOGGER.error("No segment found to add trackpoint to");
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
        
        info="Track with "+this.segments.size()+" segments (";
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
     * Returns an array of waypoints that were recorded during the track
     * @return A list of waypoints
     */
    public List<Location> getWayPoints()
    {
        return this.waypoints;
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
     * Empties the track.
     */
    public void clear()
    {
        segments.clear();
        waypoints.clear();
    }
    
}
