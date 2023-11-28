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

    private boolean                 behaviourSmoothed;
    private final List<TrackPoint>  trackPointsRaw;                 // raw segment
    private List<TrackPoint>        trackPointsSmoothed;            // smoothed segment
    private List<TrackPoint>        trackPointsRawCompressed;       // compressed segment
    private List<TrackPoint>        trackPointsSmoothedCompressed;  // compressed smoothed segment
    private List<TrackPoint>        currentPoints;
    private final ZonedDateTime     startTime;
    private final ZonedDateTime     endTime;

    /**
     * Constructor. Sets the parameters that defines the lap
     * @param startTime Start time of the lap
     * @param endTime End time of the lap
     */
    public TrackSegment(ZonedDateTime startTime, ZonedDateTime endTime)
    {
        this.startTime      =startTime;
        this.endTime        =endTime;
        trackPointsRaw      =new ArrayList<>();
        trackPointsSmoothed =new ArrayList<>();
        setBehaviour(false, false);
    }
    
    /**
     * Constructor, simple version
     */
    public TrackSegment()
    {
        this.startTime      =null;
        this.endTime        =null;
        trackPointsRaw      =new ArrayList<>();        
        trackPointsSmoothed =new ArrayList<>();
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
        if (compressed)
        {
            if (smoothed)
            {
                currentPoints=trackPointsSmoothedCompressed;
            }
            else
            {
                currentPoints=trackPointsRawCompressed;
            }
        }
        else
        {
            if (smoothed)
            {
                currentPoints=trackPointsSmoothed;
            }
            else
            {
                currentPoints=trackPointsRaw;
            }
        }
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
     * Add a raw track point to the segment. 
     * @param trackPoint Track point to add
     */
    public void addTrackPoint(TrackPoint trackPoint)
    {
        trackPointsRaw.add(trackPoint);
    }

    /**
     * Return the track point at given index
     * @param index The index
     * @return The track point or null if not found
     */
    public TrackPoint getTrackPoint(int index)
    {
        TrackPoint point;
        
        currentPoints=getTrackPoints();
        if (index>=0 && index<currentPoints.size())
        {
            point=currentPoints.get(index);
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
        return currentPoints;
    }
    
    /**
     * Returns the number of track points in this segment
     * @return Number of points.
     */
    public int getNumberOfTrackPoints()
    {
        return currentPoints.size();
    }
    
    /**
     * Returns the number of uncompressed track points in this segment
     * @return Number of points.
     */
    public int getNumberOfTrackPointsUncompressed()
    {
        int size;
        if (behaviourSmoothed)
        {
            size=trackPointsSmoothed.size();
        }
        else
        {
            size=trackPointsRaw.size();
        }
        return size;
    }
    
    /**
     * Returns the number of compressed track points in this segment
     * @return Number of points.
     */
    public int getNumberOfTrackPointsCompressed()
    {
        int size;
        if (behaviourSmoothed)
        {
            size=trackPointsSmoothedCompressed.size();
        }
        else
        {
            size=trackPointsRawCompressed.size();
        }
        return size;
    }
    /**
     * Returns the start time of the segment
     * @return The start time
     */
    public ZonedDateTime getStartTime()
    {
        return startTime;
    }

    /**
     * Returns the end time of the segment
     * @return The end time
     */
    public ZonedDateTime getEndTime()
    {
        return endTime;
    }
    
    /**
     * Sort the list of track points on datetime of the track points.
     * Note: this only applies to the raw points.
     */
    public void sortOnDateTime()
    {
        trackPointsRaw.sort((o1, o2) -> o1.compareTo(o2));
    }
    
    /**
     * Smooth the track points of the segment. This results in a new list of track points.
     */
    public void smooth()
    {
        // Create a copy of the trackpoints
        for (TrackPoint point : trackPointsRaw)
        {
            trackPointsSmoothed.add(point.clone());
        }
        TrackSmoother s     =TrackSmoother.getInstance();
        trackPointsSmoothed =s.smoothSegment(trackPointsRaw);            
    }
    
    /**
     * Compresses the segment using the Douglas-Peucker method.
     * The raw as well as the smoothed segment are compressed.
     * Hence it must be called after smooth().
     * @param maxError Measure for the maximum error
     */
    public void compress(double maxError)
    {
        this.trackPointsRawCompressed       =compress(trackPointsRaw, maxError);
        this.trackPointsSmoothedCompressed  =compress(trackPointsSmoothed, maxError);
    }
    
    /**
     * Compress the segment
     * @param points Segment points
     * @param maxError Allowable maximum error in m
     * @return The compressed segment points
     */
    private List<TrackPoint> compress(List<TrackPoint> points, double maxError)
    {
        int                     before;
        int                     after;
        TrackPoint              maxSpeed;
        TrackPoint              maxHeartrate;
        List<TrackPoint>        recs;
        
        
        List<TrackPoint> compressedPoints=new ArrayList<>();
        
        // Find the max speed in the original data
        maxSpeed    =points.stream()
                        .max(Comparator.nullsFirst(Comparator.comparing(TrackPoint::getSpeedNotNull)))
                        .orElse(null);
        maxHeartrate=points.stream()
                        .max(Comparator.nullsFirst(Comparator.comparing(TrackPoint::getHeartrateNotNull)))
                        .orElse(null);
        recs=points.stream()
                .collect(Collectors.toList());
        
        if (maxError>0.0 && recs.size()>0)
        {
            before=points.size();
            // Douglas Peucker compression
            compressedPoints=TrackCompressor.dpAlgorithm(recs, maxError);
            
            // Check if the max speed record is included in the result
            if (maxSpeed!=null && compressedPoints.stream().filter(r -> r.getDateTime().equals(maxSpeed.getDateTime())).count()==0)
            {
                // add if not
                compressedPoints.add(maxSpeed);
                Collections.sort(compressedPoints); // sort points on datetime
            }
            // Check if the max speed heartrate is included in the result
            if (maxHeartrate!=null && compressedPoints.stream().filter(r -> r.getDateTime().equals(maxHeartrate.getDateTime())).count()==0)
            {
                // add if not
                compressedPoints.add(maxHeartrate);
                Collections.sort(compressedPoints); // sort points on datetime
            }
            after=compressedPoints.size();
            LOGGER.info("DP Compression applied. Size before {}, after {} ({}%)", before, after, (100*after/before));
        }
        else
        {
            LOGGER.error("Compression maximum error value must be larger than 0.0");
        }
        return compressedPoints;
    }
}
