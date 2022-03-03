/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.util.ArrayList;
import java.util.List;
import hirondelle.date4j.DateTime;
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
    public TrackTest()
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
        List<Location> locations=new ArrayList<>();
        Location location=new Location("test", "", new DateTime("2021-05-08 08:20:00"), 5.0, 5.0, 0.0, 0);
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
        Location location1=new Location("test", "", new DateTime("2020-05-08 08:18:30"), 0.0, 0.0, 0.0, 0);
        Location location2=new Location("test", "", new DateTime("2021-05-08 08:18:30"), 0.0, 0.0, 0.0, 0);
        Location location3=new Location("test", "", new DateTime("2022-05-08 08:18:30"), 0.0, 0.0, 0.0, 0);
        waypoints.add(location1);
        waypoints.add(location2);
        waypoints.add(location3);

        assertEquals(1, instance.getWayPoints().size());
        instance.addTrackWaypoints(waypoints);
        assertEquals(2, instance.getWayPoints().size());

        instance.addTrackWaypoints(waypoints);
        assertEquals(3, instance.getWayPoints().size());

        waypoints.clear();
        waypoints.add(location3);
        waypoints.add(location1);

        instance.addTrackWaypoints(waypoints);
        assertEquals(3, instance.getWayPoints().size());
        
    }

    /**
     * Test of getTrackInfo method, of class Track.
     */
    @Test
    public void testGetTrackInfo()
    {
        System.out.println("getTrackInfo");

        assertEquals("Track with 1 segments (5023 points) and 1 waypoints", instance.getTrackInfo());
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

        assertNotNull(instance.getWayPoints());
        assertEquals(1, instance.getWayPoints().size());
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
        assertEquals(0, instance.getWayPoints().size());
    }
    
}
