/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jorgen
 */
public class TrackSegment
{
    private final static Logger     LOGGER = LogManager.getLogger(TrackSegment.class);
    private List<TrackPoint>        trackPoints;
    private List<TrackPoint>        compressedTrackPoints;
    private final ZonedDateTime     startTime;
    private final ZonedDateTime     endTime;

    /**
     * Constructor. Sets the parameters that defines the lap
     * @param startTime Start time of the lap
     * @param endTime End time of the lap
     */
    public TrackSegment(ZonedDateTime startTime, ZonedDateTime endTime)
    {
        this.startTime  =startTime;
        this.endTime    =endTime;
        trackPoints     =new ArrayList<>();
    }
    
    /**
     * Constructor, simple version
     */
    public TrackSegment()
    {
        this.startTime  =null;
        this.endTime    =null;
        trackPoints     =new ArrayList<>();        
    }
    
    /**
     * This method returns whether the indicated timestamp lies within the lap.
     * @param time Timestamp to check
     * @return True if the timestamp is within the lap, false if not.
     */
    public boolean isInSegment(ZonedDateTime time)
    {
        boolean inLap;
        
        inLap=false;

        if (time!=null && (time.isEqual(startTime)||time.isAfter(startTime)) 
                       && (time.isEqual(endTime)  ||time.isBefore(endTime)))
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
     * Get the array list with track points belonging to this segment
     * @return The array list with track points
     */
    public List<TrackPoint> getCompressedTrackPoints()
    {
        return this.compressedTrackPoints;
    }
    
    /**
     * Returns the number of track points in this segment
     * @return Number of points.
     */
    public int getNumberOfTrackPoints()
    {
        return this.trackPoints.size();
    }
    
    /**
     * Returns the number of compressed track points in this segment
     * @return Number of points.
     */
    public int getNumberOfCompressedTrackPoints()
    {
        return this.compressedTrackPoints.size();
    }
    
    public ZonedDateTime getStartTime()
    {
        return startTime;
    }

    public ZonedDateTime getEndTime()
    {
        return endTime;
    }
    
    /**
     * Sort the list of trackpoints on datetime of the trackpoints.
     */
    public void sortOnDateTime()
    {
        trackPoints=trackPoints.stream().sorted().collect(Collectors.toList());
    }
    
    /**
     * Compress segment using the Douglas-Peucker method.
     * Note: this results in a list containing only records that contain
     * latitude and longitude. Other records are removed.
     * @param maxError Measure for the maximum error
     */
    public void compress(double maxError)
    {
        int                     before;
        int                     after;
        TrackPoint              maxSpeed;
        TrackPoint              maxHeartrate;
        List<TrackPoint>    recs;
        
        // Find the max speed in the original data
        maxSpeed    =trackPoints.stream()
                        .max(Comparator.nullsFirst(Comparator.comparing(TrackPoint::getSpeedNotNull)))
                        .orElse(null);
        maxHeartrate=trackPoints.stream()
                        .max(Comparator.nullsFirst(Comparator.comparing(TrackPoint::getHeartrateNotNull)))
                        .orElse(null);
        recs=trackPoints.stream()
                .collect(Collectors.toList());
        
        if (maxError>0.0 && recs.size()>0)
        {
            before=trackPoints.size();
            // Douglas Peucker compression
            compressedTrackPoints=DPUtil.dpAlgorithm(recs, maxError);
            
            // Check if the max speed record is included in the result
            if (maxSpeed!=null && compressedTrackPoints.stream().filter(r -> r.getDateTime().equals(maxSpeed.getDateTime())).count()==0)
            {
                // add if not
                compressedTrackPoints.add(maxSpeed);
                Collections.sort(compressedTrackPoints); // sort points on datetime
            }
            // Check if the max speed heartrate is included in the result
            if (maxHeartrate!=null && compressedTrackPoints.stream().filter(r -> r.getDateTime().equals(maxHeartrate.getDateTime())).count()==0)
            {
                // add if not
                compressedTrackPoints.add(maxHeartrate);
                Collections.sort(compressedTrackPoints); // sort points on datetime
            }
            after=compressedTrackPoints.size();
            LOGGER.info("DP Compression applied. Size before {}, after {} ({}%)", before, after, (100*after/before));
        }
        else
        {
            LOGGER.error("Compression maximum error value must be larger than 0.0");
        }
    }
    
}
