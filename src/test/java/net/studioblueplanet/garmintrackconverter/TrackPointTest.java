/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

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
public class TrackPointTest
{
    private TrackPoint instanceSimple;
    private TrackPoint instance;
    
    public TrackPointTest()
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
        instanceSimple  =new TrackPoint(12.34, 34.12);
        instance        =new TrackPoint(new DateTime("2022-03-03 09:58:00"), 21.43, 43.21, -1.0, 5.0, 100.0, 2);
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getDateTime method, of class TrackPoint.
     */
    @Test
    public void testGetDateTime()
    {
        System.out.println("getDateTime");
        assertNull(instanceSimple.getDateTime());
        assertEquals("2022-03-03 09:58:00", instance.getDateTime().format("YYYY-MM-DD hh:mm:ss"));
    }

    /**
     * Test of getLatitude method, of class TrackPoint.
     */
    @Test
    public void testGetLatitude()
    {
        System.out.println("getLatitude");
        assertEquals(12.34, instanceSimple.getLatitude(), 0.0000001);
        assertEquals(21.43, instance.getLatitude(), 0.0000001);
    }

    /**
     * Test of getLongitude method, of class TrackPoint.
     */
    @Test
    public void testGetLongitude()
    {
        System.out.println("getLongitude");
        assertEquals(34.12, instanceSimple.getLongitude(), 0.0000001);
        assertEquals(43.21, instance.getLongitude(), 0.0000001);
    }

    /**
     * Test of getElevation method, of class TrackPoint.
     */
    @Test
    public void testGetElevation()
    {
        System.out.println("getElevation");
        assertEquals(0, instanceSimple.getElevation(), 0.0001);
        assertEquals(-1.0, instance.getElevation(), 0.0001);
    }

    /**
     * Test of getTemperature method, of class TrackPoint.
     */
    @Test
    public void testGetTemperature()
    {
        System.out.println("getTemperature");
        assertEquals(0, instanceSimple.getTemperature());
        assertEquals(2, instance.getTemperature());
    }

    /**
     * Test of getSpeed method, of class TrackPoint.
     */
    @Test
    public void testGetSpeed()
    {
        System.out.println("getSpeed");
        assertEquals(0, instanceSimple.getSpeed(), 0.0001);
        assertEquals(5.0, instance.getSpeed(), 0.0001);
    }

    /**
     * Test of getDistance method, of class TrackPoint.
     */
    @Test
    public void testGetDistance()
    {
        System.out.println("getDistance");
        assertEquals(0, instanceSimple.getDistance(), 0.0001);
        assertEquals(100.0, instance.getDistance(), 0.0001);
    }
    
}
