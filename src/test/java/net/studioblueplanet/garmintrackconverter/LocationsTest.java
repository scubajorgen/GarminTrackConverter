/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public class LocationsTest
{
    private static Locations instance;
    
    public LocationsTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        instance=new Locations("src/test/resources/Locations.fit");
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
        assertEquals(18, instance.getNumberOfWaypoints());
    }

    /**
     * Test of getWaypoints method, of class Locations.
     */
    @Test
    public void testGetWaypoints()
    {
        System.out.println("getWaypoints");
        List<Location> result = instance.getWaypoints();
        assertEquals(18, result.size());
        Location waypoint=result.get(0);
        assertEquals("2022-02-12 09:17:14", waypoint.getDateTime().format("YYYY-MM-DD hh:mm:ss"));
        assertEquals("Location 002", waypoint.getName());
        assertEquals(94, waypoint.getSymbol());
        assertEquals("", waypoint.getDescription());
        assertEquals(13.8, waypoint.getElevation(), 0.001);
        assertEquals(53.012594506144524, waypoint.getLatitude(), 0.0000001);
        assertEquals(6.724643586203456, waypoint.getLongitude(), 0.0000001);
    }
    
}
