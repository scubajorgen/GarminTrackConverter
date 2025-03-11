/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import net.studioblueplanet.fitreader.FitMessage;
import static net.studioblueplanet.garmintrackconverter.Track.MS_PER_S;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author jorgen
 */
public class TrackSegmentList
{
    private static final long               SECONDS_BETWEEN_SEGMENTS=60L;
    private static final int                TIMEREVENT              =0;
    private static final int                TIMEREVENT_TIMERSTARTED =0;
    private static final int                TIMEREVENT_TIMERSTOPPED =4;
    
    private final static Logger             LOGGER      = LogManager.getLogger(TrackSegmentList.class);
    private final List<TrackSegment>        segments;   
    private final DateTimeFormatter         formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
    
    /**
     * Constructor. Constructs an empty segment list 
     */
    public TrackSegmentList()
    {
        segments        =new ArrayList<>();
    }
    
    /**
     * Indicates whether the list of segments is empty.
     * @return True if empty, false if not
     */
    public boolean isEmpty()
    {
        return segments.isEmpty();
    }

    /**
     * This method sets the behaviour of the track. Both parameters may be set.
     * @param smoothed The track is smoothed
     * @param compressed The track is compressed
     */
    public void setBehaviour(boolean smoothed, boolean compressed)
    {
        for (TrackSegment segment : segments)
        {
            segment.setBehaviour(smoothed, compressed);
        }
    }

    /**
     * This method parses the FIT lap record and distils the number of laps.
     * @param lapMessages The FIT record holding the 'lap' info
     */
    public void parseLaps(List<FitMessage> lapMessages)
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
                    long elapsedTime    =message.getIntValue(i, "total_elapsed_time")/MS_PER_S;
                    segmentEndTime      =segmentStartTime.plusSeconds(elapsedTime);
                    segment             =new TrackSegment(segmentStartTime, segmentEndTime);
                    segments.add(segment);
                    LOGGER.info("Segment found on Laps: {} - {}", 
                            segmentStartTime.format(formatter), 
                            segmentEndTime.format(formatter));
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
     * This method parses the FIT lap record and destilles the number of sessions.
     * @param eventMessages The FIT record holding the 'event' info
     */
    public void getSegmentsFromEvents(List<FitMessage> eventMessages)
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
                            LOGGER.info("Segment found on Events: {} - {}", start.format(formatter), end.format(formatter));
                        }
                    }
                    else
                    {
                        if (eventType==TIMEREVENT_TIMERSTARTED)
                        {
                            started=true;
                            start=message.getTimeValue(i, "timestamp");
                            // It has been observed with GPSMAP67 with 12 hour activity
                            // that the start timestamp is incorrect
                        }
                    }
                }
            }   
        }
    }    
    
    /**
     * This method tries to derive the segments from the list of trackpoints.
     * It assumes a new segment to start when the time difference between
     * the timestamp of a point and its predecesor is larger than a limit
     * @param points List of points to use.
     */
    public void getSegmentsFromTrackPoints(List<TrackPoint> points)
    {
        segments.clear();
        ZonedDateTime   prevTimestamp   =null;
        boolean         firstPoint      =true;
        TrackSegment    segment         =null;
        for(TrackPoint p:points)
        {
            ZonedDateTime pointTimestamp=p.getDateTime();
            if(firstPoint)
            {
                firstPoint=false;
                segment=new TrackSegment();
                segment.setStartTime(pointTimestamp);
                segments.add(segment);
            }
            else
            {
                long seconds=Duration.between(prevTimestamp, pointTimestamp).getSeconds();
                if (seconds>SECONDS_BETWEEN_SEGMENTS)
                {
                    segment.setEndTime(prevTimestamp);
                    LOGGER.info("Segment found on TrackPoints: {} - {}", 
                            segment.getStartTime().format(formatter), 
                            segment.getEndTime().format(formatter));
                    segment=new TrackSegment();
                    segments.add(segment);
                    segment.setStartTime(pointTimestamp);
                }
            }
            segment.addTrackPoint(p);
            prevTimestamp=pointTimestamp;
        }
        segment.setEndTime(prevTimestamp);
        LOGGER.info("Segment found on TrackPoints: {} - {}", 
                segment.getStartTime().format(formatter), 
                segment.getEndTime().format(formatter));
    }
    
    /**
     * Adds a TrackPoint to the adequate segment, based on the datetime of 
     * the point
     * @param point TrackPoint to add
     * @return True if succeeded, false if not
     */
    public boolean addTrackPointToSegment(TrackPoint point)
    {
        boolean found           =false;
        ZonedDateTime dateTime  =point.getDateTime();
        for (int j=0; j<segments.size() && !found; j++)
        {
            TrackSegment segment=segments.get(j);
            if (segment.isInSegment(dateTime))
            {
                segment.addTrackPoint(point);
                found=true;
            }
        }
        return found;
    }
    
    /**
     * This method checks of the given dateTime is in one of the segments.
     * @param dateTime Zoned datetimestamp
     * @return True if the dateTime is in one of the segments, false if not
     */
    public boolean isDatetimeInSegment(ZonedDateTime dateTime)
    {
        boolean found=false;
        for (int j=0; j<segments.size() && !found; j++)
        {
            // If the date time stamp is in the range of the segment,
            // add this waypoint to the track and don't look any further
            TrackSegment segment=segments.get(j);
            if (segment.isInSegment(dateTime))
            {
                found=true;
            }
        }      
        return found;
    }
    
    /**
     * Returns the number of TrackSegments in this list
     * @return The number of TrackSegments.
     */
    public int size()
    {
        return segments.size();
    }
    
    /**
     * Return the TrackSegment indicated by the given index. 
     * @param index Index in the list. Must be larger or equal to 0 and 
     *              smaller than size()
     * @return The TrackSegment
     * @throws IndexOutOfBoundsException if index invalid.
     */
    public TrackSegment get(int index)
    {
        return segments.get(index);
    }
    
    /**
     * Adds given TrackSegment to the list
     * @param segment TrackSegment to add
     */
    public void add(TrackSegment segment)
    {
        segments.add(segment);
    }
    
    /**
     * Returns the TrackSegments as list
     * @return The List with TrackSegments
     */
    public List<TrackSegment> getSegments()
    {
        return segments;
    }
    
    /**
     * Sort the segments on start DateTime
     */
    public void sortSegments()
    {
        segments.forEach((segment) ->
        {
            segment.sortOnDateTime();
        });     
    }
    
    /**
     * Smoothes each of the TrackSegments in this list
     */
    public void smoothSegments()
    {
        segments.forEach((segment) ->
        {
            segment.smooth();
        });        
    }
    
    /**
     * Compresses the TrackSegments in the list based on the maximum allowed
     * error.
     * @param maxError Maximum allowed error in meter. 
     */
    public void compressTrackSegments(double maxError)
    {
        segments.forEach((segment) ->
        {
            segment.compress(maxError);
        });        
    }
    
    /**
     * Clears the list of TrackSegmens
     */
    public void clear()
    {
        segments.clear();
    }
    
    /**
     * Returns the total number of logged seconds, i.e. the sum
     * over the segments
     * @return The timed time in seconds.
     */
    public long getTimedTime()
    {
        long timedTime=0;
        for (TrackSegment s:segments)
        {
            ZonedDateTime start =s.getStartTime();
            ZonedDateTime end   =s.getEndTime();
            
            timedTime+=Duration.between(start, end).getSeconds();
        }
        return timedTime;
    }
}
