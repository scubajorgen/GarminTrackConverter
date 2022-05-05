/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;

/**
 *
 * @author jorgen
 */
public class GpxReaderTest
{
    private static GpxReader instance;
    
    public GpxReaderTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        instance=GpxReader.getInstance();
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
     * Test of getInstance method, of class GpxReader.
     */
    @Test
    public void testGetInstance()
    {
        System.out.println("getInstance");
        GpxReader expResult = instance;
        GpxReader result = GpxReader.getInstance();
        assertEquals(expResult, result);
    }

    /**
     * Test of readRouteFromFile method, of class GpxReader.
     */
    @Test
    public void testReadRouteFromFileWaypoints()
    {
        System.out.println("readRouteFromFile - waypoints");

        Track result=instance.readRouteFromFile("src/test/resources/result2a.txt");
        assertEquals(0, result.getSegments().size());
        assertEquals(2, result.getWaypoints().size());
        assertEquals("name1", result.getWaypoints().get(0).getName());
        assertEquals("desc1", result.getWaypoints().get(0).getDescription());
        assertEquals("2022-05-04T14:48:30Z", result.getWaypoints().get(0).getDateTime().toString());
        assertEquals(53.6, result.getWaypoints().get(1).getLatitude(), 0.0000001);
        assertEquals(6.6, result.getWaypoints().get(1).getLongitude(), 0.0000001);
        assertEquals(4.0, result.getWaypoints().get(1).getElevation(), 0.001);
    }
    
    /**
     * Test of readRouteFromFile method, of class GpxReader.
     */
    @Test
    public void testReadRouteFromFileTrack()
    {
        System.out.println("readRouteFromFile - track");

        Track result=instance.readRouteFromFile("src/test/resources/result1a.txt");
        assertEquals(1, result.getSegments().size());
        assertEquals(1, result.getTrackPoints(0).size());
        assertEquals(53.5, result.getTrackPoints(0).get(0).getLatitude(), 0.0000001);
        assertEquals( 6.5, result.getTrackPoints(0).get(0).getLongitude(), 0.0000001);
    }    
    
    /**
     * Test of readRouteFromFile method, of class GpxReader.
     */
    @Test
    @Ignore
    public void testReadRouteFromFileRoute()
    {
        System.out.println("readRouteFromFile - route");


    }    
    
}
