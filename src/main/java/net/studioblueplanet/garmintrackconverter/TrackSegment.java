/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jorgen
 */
public class TrackSegment
{
    private final static Logger         LOGGER = LogManager.getLogger(TrackSegment.class);
    private final DateTime              startTime;
    private final DateTime              endTime;
    private final double                elapsedTime;
    private final List<TrackPoint>      trackPoints;
    
    /**
     * Constructor. Sets the parameters that defines the lap
     * @param startTime Start time of the lap
     * @param endTime End time of the lap
     * @param elapsedTime Elapsed time in seconds
     */
    public TrackSegment(DateTime startTime, DateTime endTime, double elapsedTime)
    {
        this.startTime  =startTime;
        this.endTime    =endTime;
        this.elapsedTime=elapsedTime;
        trackPoints     =new ArrayList<TrackPoint>();
    }
    
    /**
     * This method returns whether the indicated timestamp lies within the lap.
     * @param time Timestamp to check
     * @return True if the timestamp is within the lap, false if not.
     */
    public boolean isInLap(DateTime time)
    {
        boolean inLap;
        
        inLap=false;
        
        if (time==null)
        {
            System.err.println("Null time");
        }
        
        if (time!=null && time.gteq(startTime) && time.lt(endTime))
        {
            inLap=true;
        }
        return inLap;
    }
    
    /**
     * Add a track point to the segment
     * @param trackPoint Track point to add
     */
    public void addTrackPoint(TrackPoint trackPoint)
    {
        trackPoints.add(trackPoint);
    }

    /**
     * Return the track point at given index
     * @param index The index
     * @return The track point or null if not found
     */
    public TrackPoint getTrackPoint(int index)
    {
        TrackPoint point;
        
        if (index>=0 && index<trackPoints.size())
        {
            point=trackPoints.get(index);
        }
        else
        {
            point=null;
        }
        return point;
    }
    
    /**
     * Get the array list with track points belonging to this segment
     * @return The array list with track points
     */
    public List<TrackPoint> getTrackPoints()
    {
        return this.trackPoints;
    }
    
    /**
     * Returns the number of track points in this segment
     * @return Number of points.
     */
    public int getNumberOfTrackPoints()
    {
        return this.trackPoints.size();
    }
}
