/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

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
        // Accuracy is 5000 cm
        List<TrackPoint> segmentTrackPoints=TestTrack.testPoints();
        
        List<TrackPoint> smoothedSegment=instance.smoothSegment(segmentTrackPoints);

        System.out.println("Original:");
        for (TrackPoint sp : segmentTrackPoints)
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
