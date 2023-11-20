/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jorgen
 */
public class TrackSmootherTest
{
    private TrackSmoother instance;
    
    public TrackSmootherTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
        instance=TrackSmoother.getInstance();
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getInstance method, of class TrackSmoother.
     */
    @Test
    public void testGetInstance()
    {
        System.out.println("getInstance");
        assertNotNull(instance);
        assertEquals(instance, TrackSmoother.getInstance());
    }

    /**
     * Test of smoothSegment method, of class TrackSmoother.
     */
    @Test
    public void testSmoothSegment()
    {
        System.out.println("smoothSegment");
        
        // We create a segment of 14 points, at 9 sec interval; speed is about 22 km/h = 6 m/s
        // The segment contains a straight angle
        // Accuracy is 500 cm
        List<TrackPoint> segment=new ArrayList<>();
        TrackPoint point;
        ZoneId zoneId = ZoneId.of("UTC+1");
        int accuracy=5000;
        ZonedDateTime zdt = ZonedDateTime.of(2015, 11, 30, 23, 45, 59, 1234, zoneId);        
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
        
        List<TrackPoint> smoothedSegment=instance.smoothSegment(segment);

        System.out.println("Original:");
        for (TrackPoint sp : segment)
        {
            System.out.println(sp.getLatitude()+", "+sp.getLongitude());
        }
        System.out.println("Smoothed:");
        for (TrackPoint sp : smoothedSegment)
        {
            System.out.println(sp.getLatitude()+", "+sp.getLongitude());
        }
        assertEquals(53.012562, smoothedSegment.get(0).getLatitude(), 0.000001);    // First point is original
        assertEquals(6.725073 , smoothedSegment.get(0).getLongitude(), 0.000001);
        assertEquals(53.012829, smoothedSegment.get(13).getLatitude(), 0.000001);   // Last smoothed point
        assertEquals(6.719117 , smoothedSegment.get(13).getLongitude(), 0.000001);
    }
}
