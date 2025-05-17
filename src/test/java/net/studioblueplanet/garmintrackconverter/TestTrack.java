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
        // Accuracy is 5000 cm = 50 m
        List<TrackPoint> segment=new ArrayList<>();
        TrackPoint point;
        ZoneId zoneId = ZoneId.of("UTC+1");
        int accuracy=5000;
        int ehpe    =500;
        ZonedDateTime zdt = ZonedDateTime.of(2015, 11, 30, 23, 45, 59, 0, zoneId);        
        point=new TrackPoint.TrackPointBuilder(53.012562,   6.725073)
                            .dateTime(zdt)
                            .elevation(0.0)
                            .speed(6.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.012283,   6.724260)
                            .dateTime(zdt.plusSeconds( 9))
                            .elevation(0.0)
                            .speed(6.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.012058,   6.723537)
                            .dateTime(zdt.plusSeconds(18))
                            .elevation(0.0)
                            .speed(6.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();

        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.011832,   6.722875)
                            .dateTime(zdt.plusSeconds(27))
                            .elevation(0.0)
                            .speed(6.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.011589,   6.722063)
                            .dateTime(zdt.plusSeconds(36))
                            .elevation(0.0)
                            .speed(6.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.011346,   6.721340)
                            .dateTime(zdt.plusSeconds(45))
                            .elevation(0.0)
                            .speed(6.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.011138,   6.720648)
                            .dateTime(zdt.plusSeconds(54))
                            .elevation(0.0)
                            .speed(6.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.011574,   6.720185)
                            .dateTime(zdt.plusSeconds(63))
                            .elevation(0.0)
                            .speed(6.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.011938,   6.719808)
                            .dateTime(zdt.plusSeconds(72))
                            .elevation(0.0)
                            .speed(6.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.012338,   6.719374)
                            .dateTime(zdt.plusSeconds(81))
                            .elevation(0.0)
                            .speed(6.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.012774,   6.718970)
                            .dateTime(zdt.plusSeconds(90))
                            .elevation(0.0)
                            .speed(6.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.013193,   6.718506)
                            .dateTime(zdt.plusSeconds(99))
                            .elevation(0.0)
                            .speed(0.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.013575,   6.718100)
                            .dateTime(zdt.plusSeconds(108))
                            .elevation(0.0)
                            .speed(0.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);
        point=new TrackPoint.TrackPointBuilder(53.013974,   6.717725)
                            .dateTime(zdt.plusSeconds(117))
                            .elevation(0.0)
                            .speed(0.0)
                            .distance(0.0)
                            .temperature(0)
                            .heartrate(0)
                            .ehpe(ehpe)
                            .gpsAccuracy(accuracy)
                            .build();
        segment.add(point);  
        
        return segment;
    }
    
    /**
     * Creates a simple track with one segment consisting of the points above
     */
    public static Track testTrack()
    {
        Track track=new Track(3.0, 50.0);
        
        List<TrackSegment> segments=track.getSegments();
        ZonedDateTime zdt       = ZonedDateTime.of(2015, 11, 30, 23, 45, 59, 0, ZoneId.of("UTC"));
        TrackSegment segment    =new TrackSegment(zdt, zdt.plusSeconds(12));
        List<TrackPoint> points =testPoints();
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
