/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
public class TrackTest
{
    private Track instance;
    private static Track instanceTwoSegments;
    public TrackTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        instanceTwoSegments=new Track("src/test/resources/2022-03-20-11-57-12.fit", "TestDevice");
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
        List<Location> locations=new ArrayList<>();
        Location location=new Location("test", "", ZonedDateTime.parse("2021-05-08T08:20:00Z"), 5.0, 5.0, 0.0, 0);
        locations.add(location);

        instance=new Track("src/test/resources/2021-05-08-10-18-29.fit", "TestDevice");
        instance.addTrackWaypoints(locations);
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of addTrackWaypoints method, of class Track.
     */
    @Test
    public void testAddTrackWaypoints()
    {
        System.out.println("addTrackWaypoints");
        
        List<Location> waypoints=new ArrayList<>();
        Location location1=new Location("test", "", ZonedDateTime.parse("2020-05-08T08:18:30Z"), 0.0, 0.0, 0.0, 0);
        Location location2=new Location("test", "", ZonedDateTime.parse("2021-05-08T08:18:30Z"), 0.0, 0.0, 0.0, 0);
        Location location3=new Location("test", "", ZonedDateTime.parse("2022-05-08T08:18:30Z"), 0.0, 0.0, 0.0, 0);
        waypoints.add(location1);
        waypoints.add(location2);
        waypoints.add(location3);

        assertEquals(1, instance.getWaypoints().size());
        instance.addTrackWaypoints(waypoints);
        assertEquals(2, instance.getWaypoints().size());

        instance.addTrackWaypoints(waypoints);
        assertEquals(3, instance.getWaypoints().size());

        waypoints.clear();
        waypoints.add(location3);
        waypoints.add(location1);

        instance.addTrackWaypoints(waypoints);
        assertEquals(3, instance.getWaypoints().size());
        
    }

    /**
     * Test of getTrackInfo method, of class Track.
     */
    @Test
    public void testGetTrackInfo()
    {
        System.out.println("getTrackInfo");
        assertEquals("Track (cycling - gravel_cycling) with 1 segments (5023 points) and 1 waypoints", instance.getTrackInfo());
        assertEquals("Track (cycling - cyclocross) with 2 segments (10, 18 points) and 0 waypoints", instanceTwoSegments.getTrackInfo());
    }

    /**
     * Test of getDeviceName method, of class Track.
     */
    @Test
    public void testGetDeviceName()
    {
        System.out.println("getDeviceName");

        assertEquals("TestDevice", instance.getDeviceName());
    }

    /**
     * Test of getNumberOfSegments method, of class Track.
     */
    @Test
    public void testGetNumberOfSegments()
    {
        System.out.println("getNumberOfSegments");
        assertEquals(1, instance.getNumberOfSegments());
    }

    /**
     * Test of getTrackPoints method, of class Track.
     */
    @Test
    public void testGetTrackPoints()
    {
        System.out.println("getTrackPoints");
        assertNull(instance.getTrackPoints(2));
        assertNull(instance.getTrackPoints(-1));
        assertNotNull(instance.getTrackPoints(0));
        assertEquals(5023, instance.getTrackPoints(0).size());
    }

    /**
     * Test of getWayPoints method, of class Track.
     */
    @Test
    public void testGetWayPoints()
    {
        System.out.println("getWayPoints");

        assertNotNull(instance.getWaypoints());
        assertEquals(1, instance.getWaypoints().size());
    }

    /**
     * Test of appendTrackSegment method, of class Track.
     */
    @Test
    public void testAppendTrackSegment()
    {
        System.out.println("appendTrackSegment");
        assertNotNull(instance.appendTrackSegment());
    }

    /**
     * Test of clear method, of class Track.
     */
    @Test
    public void testClear()
    {
        System.out.println("clear");
        instance.clear();
        assertEquals(0, instance.getNumberOfSegments());
        assertEquals(0, instance.getWaypoints().size());
    }
    
    /**
     * Test of getSport method, of class Track.
     */
    @Test
    public void testGetSport()
    {
        System.out.println("getSport");
        assertEquals("cycling", instance.getSport());
    }

    /**
     * Test of getFilename method, of class Track.
     */
    @Test
    public void testGetFitFileName()
    {
        System.out.println("getFitFileName");
        assertEquals("2021-05-08-10-18-29.fit", instance.getFitFileName());
    }
    
    /**
     * Test of getStartTime method, of class Track.
     */
    @Test
    public void testGetStartTime()
    {
        System.out.println("getStartTime");
        // UTC
        assertEquals("2021-05-08 08:18:29", instance.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    /**
     * Test of getStartDate method, of class Track.
     */
    @Test
    public void testGetStartDate()
    {
        System.out.println("getStartDate");
        // UTC
        assertEquals("20210508", instance.getStartDate());
    }
    
    /**
     * Test of getEndTime method, of class Track.
     */
    @Test
    public void testGetEndTime()
    {
        System.out.println("getEndTime");
        // UTC
        assertEquals("2021-05-08 10:39:38", instance.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
