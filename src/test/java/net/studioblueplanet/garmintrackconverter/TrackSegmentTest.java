/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
        ZonedDateTime startDateTime;
        ZonedDateTime endDateTime;
        startDateTime   =ZonedDateTime.of(2022, 3, 4, 17, 54, 0, 0, ZoneId.of("UTC"));
        endDateTime     =ZonedDateTime.of(2022, 3, 4, 18, 54, 0, 0, ZoneId.of("UTC"));
        
        instance=new TrackSegment(startDateTime, endDateTime);
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
        assertEquals(false, instance.isInLap(ZonedDateTime.parse("2022-03-04T17:53:59Z")));
        assertEquals(true , instance.isInLap(ZonedDateTime.parse("2022-03-04T17:54:00Z"))); // start time
        assertEquals(true , instance.isInLap(ZonedDateTime.parse("2022-03-04T18:53:59Z")));
        assertEquals(true, instance.isInLap(ZonedDateTime.parse("2022-03-04T18:54:00Z"))); // end time
        assertEquals(false, instance.isInLap(ZonedDateTime.parse("2022-03-04T18:54:01Z")));
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
     * Test of getStartTime method, of class TrackSegment.
     */
    @Test
    public void testGetStartTime()
    {
        System.out.println("getStartTime");
        assertEquals("2022-03-04 17:54:00", instance.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        assertNull(instanceSimple.getStartTime());
    }
    
    /**
     * Test of getEndTime method, of class TrackSegment.
     */
    @Test
    public void testGetEndTime()
    {
        System.out.println("getEndTime");
        assertEquals("2022-03-04 18:54:00", instance.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        assertNull(instanceSimple.getEndTime());
    }
    
}
