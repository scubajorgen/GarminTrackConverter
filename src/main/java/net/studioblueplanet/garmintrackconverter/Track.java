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

import net.studioblueplanet.logger.DebugLogger;
        

/**
 * This class represents a track consisting of track segments and optional
 * a number of logged waypoints
 * @author Jorgen
 */
public class Track
{
    private final List<TrackSegment>        segments;
    private final List<Waypoint>            waypoints;
    private final String                    lastError;
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
        FitMessage              lapRecord;
        FitMessage              trackRecord;
        
        segments        =new ArrayList<>();
        waypoints       =new ArrayList<>();
        lastError       ="Ok";
        
        reader          =FitReader.getInstance();
        repository      =reader.readFile(trackFileName);
        lapRecord       =repository.getFitMessage("lap");
        trackRecord     =repository.getFitMessage("record");

        repository.dumpMessageDefintions();
        
        if (lapRecord!=null && trackRecord!=null)
        {
            this.parseLaps(lapRecord);
            this.parseTrackPoints(trackRecord);

            this.deviceName=deviceName;
        }
    }

    /**
     * This method parses the FIT lap record and destilates the number of laps.
     * @param lapRecord The FIT record holding the 'lap' info
     */
    private void parseLaps(FitMessage lapRecord)
    {
        int i;
        int size;
        DateTime                startTime;
        DateTime                endTime;
        double                  elapsedTime;
        TrackSegment            segment;
        
        size            =lapRecord.getNumberOfRecords();
        i=0;
        while (i<size)
        {
            endTime     =lapRecord.getTimeValue(i, "timestamp");
            startTime   =lapRecord.getTimeValue(i, "start_time");
            elapsedTime =lapRecord.getIntValue(i, "total_elapsed_time")/1000;
       
            segment     =new TrackSegment(startTime, endTime, elapsedTime);
            segments.add(segment);
            
            DebugLogger.debug("Lap "+lapRecord.getIntValue(i, "message_index")+
                             " "+startTime.toString()+
                             " "+endTime.toString()+
                             " "+elapsedTime+" s"
                             );
            i++;
        }          
        
    }
    
    /**
     * This method parses the FIT activity record. It destiles the track points
     * @param trackRecord The record holding the 'activity' information
     */
    private void parseTrackPoints(FitMessage trackRecord)
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


        size            =trackRecord.getNumberOfRecords();
        i=0;
        while (i<size)
        {
            dateTime    =trackRecord.getTimeValue(i, "timestamp");
            lat         =trackRecord.getLatLonValue(i, "position_lat");
            lon         =trackRecord.getLatLonValue(i, "position_long");
            ele         =trackRecord.getAltitudeValue(i, "altitude");
            temp        =trackRecord.getIntValue(i, "temperature");
            speed       =trackRecord.getSpeedValue(i, "speed");
            distance    =trackRecord.getDistanceValue(i, "distance");
            
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
                DebugLogger.error("No segment found to add trackpoint to");
            }
            
            DebugLogger.debug("Track "+
                             " "+dateTime.toString()+
                             " ("+lat+","+lon+") ele "+ele+" "+speed+" km/h "+distance+" m "
                             );
            i++;
        }           
    }
    
    /**
     * Add the waypoints that were read from the locations.fit file. 
     * Only the waypoints that were recorded during the track are added 
     * to the track.
     * @param allWaypoints The waypoints read from the device
     */
    public void addTrackWaypoints(ArrayList<Waypoint> allWaypoints)
    {
        DateTime                dateTime;
        Iterator<Waypoint>      waypointIterator;
        Iterator<TrackSegment>  segmentIterator;
        Waypoint                waypoint;
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
        return this.segments.get(segment).getTrackPoints();
    }
    
    /**
     * Returns an array of waypoints that were recorded during the track
     * @return A list of waypoints
     */
    public List<Waypoint> getWayPoints()
    {
        return this.waypoints;
    }
    
}
