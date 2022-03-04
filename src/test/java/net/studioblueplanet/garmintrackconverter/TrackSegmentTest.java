/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import hirondelle.date4j.DateTime;
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
public class TrackSegmentTest
{
    private TrackSegment instance;
    private TrackSegment instanceSimple;
    
    
    public TrackSegmentTest()
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
        instance=new TrackSegment(new DateTime("2022-03-04 17:54:00"), new DateTime("2022-03-04 18:54:00"), 3600);
        instanceSimple=new TrackSegment();
        TrackPoint point=new TrackPoint(5.0, 53.0);
        instance.addTrackPoint(point);
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of isInLap method, of class TrackSegment.
     */
    @Test
    public void testIsInLap()
    {
        System.out.println("isInLap");
        assertEquals(false, instance.isInLap(new DateTime("2022-03-04 17:53:59")));
        assertEquals(true , instance.isInLap(new DateTime("2022-03-04 17:54:00"))); // start time
        assertEquals(true , instance.isInLap(new DateTime("2022-03-04 18:53:59")));
        assertEquals(false, instance.isInLap(new DateTime("2022-03-04 18:54:00"))); // end time
        assertEquals(false, instance.isInLap(new DateTime("2022-03-04 18:54:01")));
    }

    /**
     * Test of addTrackPoint method, of class TrackSegment.
     */
    @Test
    public void testAddTrackPoint()
    {
        System.out.println("addTrackPoint");
        TrackPoint point=new TrackPoint(6.0, 54.0);
        assertEquals(1, instance.getNumberOfTrackPoints());
        instance.addTrackPoint(point);
        assertEquals(2, instance.getNumberOfTrackPoints());
    }

    /**
     * Test of getTrackPoint method, of class TrackSegment.
     */
    @Test
    public void testGetTrackPoint()
    {
        System.out.println("getTrackPoint");
        TrackPoint point=instance.getTrackPoint(0);
        assertNotNull(point);
        assertEquals(5.0, point.getLatitude(), 0.0000001);
    }

    /**
     * Test of getTrackPoints method, of class TrackSegment.
     */
    @Test
    public void testGetTrackPoints()
    {
        System.out.println("getTrackPoints");
        List<TrackPoint> points=instance.getTrackPoints();
        assertNotNull(points);
        assertEquals(1, points.size());
    }

    /**
     * Test of getNumberOfTrackPoints method, of class TrackSegment.
     */
    @Test
    public void testGetNumberOfTrackPoints()
    {
        System.out.println("getNumberOfTrackPoints");
        assertEquals(1, instance.getNumberOfTrackPoints());
        assertEquals(0, instanceSimple.getNumberOfTrackPoints());
    }
    
    /**
     * Test of getElapsedTime method, of class TrackSegment.
     */
    @Test
    public void testGetElapsedTime()
    {
        System.out.println("getElapsedTime");
        assertEquals(3600.0, instance.getElapsedTime()      , 0.0001);
        assertEquals(   0.0, instanceSimple.getElapsedTime(), 0.0001);
    }
    
    /**
     * Test of getStartTime method, of class TrackSegment.
     */
    @Test
    public void testGetStartTime()
    {
        System.out.println("getStartTime");
        assertEquals("2022-03-04 17:54:00", instance.getStartTime().format("YYYY-MM-DD hh:mm:ss"));
        assertNull(instanceSimple.getStartTime());
    }
    
    /**
     * Test of getEndTime method, of class TrackSegment.
     */
    @Test
    public void testGetEndTime()
    {
        System.out.println("getEndTime");
        assertEquals("2022-03-04 18:54:00", instance.getEndTime().format("YYYY-MM-DD hh:mm:ss"));
        assertNull(instanceSimple.getEndTime());
    }
    
}
