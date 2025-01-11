/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

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
public class LocationsTest
{
    private static Locations instanceEdge;
    private static Locations instanceFenix;
    private static Locations instanceGpsmap67;
    private static Locations instanceGpx;
    
    public LocationsTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        instanceEdge    =new Locations("src/test/resources/Locations.fit");
        instanceFenix   =new Locations("src/test/resources/Lctns.fit");
        List<String> fileList=new ArrayList<>();
        fileList.add("src/test/resources/Waypoints_2025-01-10.gpx");
        fileList.add("src/test/resources/Waypoints_2025-01-11.gpx");
        instanceGpsmap67=new Locations(fileList);
        instanceGpx     =new Locations("src/test/resources/Waypoints_2025-01-10.gpx");
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getNumberOfWaypoints method, of class Locations.
     */
    @Test
    public void testGetNumberOfWaypoints()
    {
        System.out.println("getNumberOfWaypoints");
        assertEquals(18, instanceEdge.getNumberOfWaypoints());
        assertEquals(4, instanceGpsmap67.getNumberOfWaypoints());
        assertEquals(3, instanceGpx.getNumberOfWaypoints());
    }

    /**
     * Test of getWaypoints method, of class Locations.
     */
    @Test
    public void testGetWaypoints()
    {
        System.out.println("getWaypoints");
        List<Location> result = instanceEdge.getWaypoints();
        assertEquals(18, result.size());
        Location waypoint=result.get(0);
        assertEquals("2022-02-12 09:17:14", waypoint.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        assertNull(waypoint.getLocalDateTime());
        assertEquals("Location 002", waypoint.getName());
        assertEquals(94, waypoint.getSymbol());
        assertEquals("", waypoint.getDescription());
        assertEquals(13.8, waypoint.getElevation(), 0.001);
        assertEquals(53.012594506144524, waypoint.getLatitude(), 0.0000001);
        assertEquals(6.724643586203456, waypoint.getLongitude(), 0.0000001);
    }

    /**
     * Test of getWaypoints method, of class Locations.
     */
    @Test
    public void testGetWaypointsFenix()
    {
        System.out.println("getWaypoints");
        List<Location> result = instanceFenix.getWaypoints();
        // The Fenix does not fill in the timestamp field
        // If fills the local datetime in the name
        assertEquals(85, result.size());
        Location waypoint=result.get(0);
        assertNull(waypoint.getDateTime());
        assertEquals("Tra-2-2-Plane spotters nest", waypoint.getName());
        assertEquals(65535, waypoint.getSymbol());
        assertNull(waypoint.getDescription());
        assertEquals(12607.0, waypoint.getElevation(), 0.001);
        assertEquals(28.940999982878566, waypoint.getLatitude(), 0.0000001);
        assertEquals(-13.606899976730347, waypoint.getLongitude(), 0.0000001);

        waypoint=result.get(84);
        assertNull(waypoint.getDateTime());
        assertEquals("05-03 12:36:00", waypoint.getLocalDateTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")));
        // To Do: add year test
        assertEquals("May 03 12:36", waypoint.getName());
    }

    /**
     * Test of getWaypoints method, of class Locations.
     */
    @Test
    public void testGetWaypointsGpsmap67()
    {
        System.out.println("getWaypoints Gpsmap 67");
        List<Location> result = instanceGpsmap67.getWaypoints();
        assertEquals(4, result.size());
        Location waypoint=result.get(0);
        assertEquals("2025-01-10 12:53:26", waypoint.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        assertNull(waypoint.getLocalDateTime());
        assertEquals("0001", waypoint.getName());
        assertEquals(0, waypoint.getSymbol());
        assertEquals("", waypoint.getDescription());
        assertEquals(15.806191, waypoint.getElevation(), 0.001);
        assertEquals(53.012683, waypoint.getLatitude(), 0.0000001);
        assertEquals(6.724983, waypoint.getLongitude(), 0.0000001);
    }
}
