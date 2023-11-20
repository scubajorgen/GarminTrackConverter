/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jorgen
 */
public class TestTrack
{
    /**
     * Returns a list of points we can use for testing compression and smoothing.
     * @return List of 14 points
     */
    public static List<TrackPoint> testPoints()
    {
        // We create a segment of 14 points, at 9 sec interval; speed is about 22 km/h = 6 m/s
        // The segment contains a straight angle
        // Accuracy is 500 cm
        List<TrackPoint> segment=new ArrayList<>();
        TrackPoint point;
        ZoneId zoneId = ZoneId.of("UTC+1");
        int accuracy=5000;
        ZonedDateTime zdt = ZonedDateTime.of(2015, 11, 30, 23, 45, 59, 0, zoneId);        
        point=new TrackPoint(zdt               , 53.012562,   6.725073, 0.0, 6.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds( 9), 53.012283,   6.724260, 0.0, 6.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds(18), 53.012058,   6.723537, 0.0, 6.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds(27), 53.011832,   6.722875, 0.0, 6.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds(36), 53.011589,   6.722063, 0.0, 6.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds(45), 53.011346,   6.721340, 0.0, 6.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds(54), 53.011138,   6.720648, 0.0, 6.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds(63), 53.011574,   6.720185, 0.0, 6.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds(72), 53.011938,   6.719808, 0.0, 6.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds(81), 53.012338,   6.719374, 0.0, 6.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds(90), 53.012774,   6.718970, 0.0, 6.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds(99), 53.013193,   6.718506, 0.0, 0.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds(108), 53.013575,   6.718100, 0.0, 0.0, 0.0, 0, 0, accuracy);
        segment.add(point);
        point=new TrackPoint(zdt.plusSeconds(117), 53.013974,   6.717725, 0.0, 0.0, 0.0, 0, 0, accuracy);
        segment.add(point);  
        
        return segment;
    }
    
    /**
     * Creates a simple track with one segment consisting of the points above
     */
    public static Track testTrack()
    {
        Track track=new Track(3.0, 5000);
        
        List<TrackSegment> segments=track.getSegments();
        ZonedDateTime zdt = ZonedDateTime.of(2015, 11, 30, 23, 45, 59, 0, ZoneId.of("UTC"));
        TrackSegment segment=new TrackSegment(zdt, zdt.plusSeconds(12));
        List<TrackPoint> points=testPoints();
        for (TrackPoint p : points)
        {
            segment.addTrackPoint(p);
        }
        segment.smooth();
        segment.compress(3.0);
        segments.add(segment);  
        return track;
    }
}
