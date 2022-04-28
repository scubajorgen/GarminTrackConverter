/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
        ZonedDateTime dateTime=ZonedDateTime.of(2022, 3, 3, 13, 58, 0, 0, ZoneId.of("UTC"));
        instance        =new TrackPoint(dateTime, 21.43, 43.21, -1.0, 5.0, 100.0, 2, 77, 3);
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
        assertEquals("2022-03-03 13:58:00", instance.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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
        assertNull(instanceSimple.getElevation());
        assertEquals(-1.0, instance.getElevation(), 0.0001);
    }

    /**
     * Test of getTemperature method, of class TrackPoint.
     */
    @Test
    public void testGetTemperature()
    {
        System.out.println("getTemperature");
        assertNull(instanceSimple.getTemperature());
        assertEquals(2, instance.getTemperature().intValue());
    }

    /**
     * Test of getSpeed method, of class TrackPoint.
     */
    @Test
    public void testGetSpeed()
    {
        System.out.println("getSpeed");
        assertNull(instanceSimple.getSpeed());
        assertEquals(5.0, instance.getSpeed(), 0.0001);
    }

    /**
     * Test of getDistance method, of class TrackPoint.
     */
    @Test
    public void testGetDistance()
    {
        System.out.println("getDistance");
        assertNull(instanceSimple.getDistance());
        assertEquals(100.0, instance.getDistance(), 0.0001);
    }
    
    /**
     * Test of getHeartrate method, of class TrackPoint.
     */
    @Test
    public void testGetHeartrate()
    {
        System.out.println("getHeartrate");
        assertNull(instanceSimple.getHeartrate());
        assertEquals(77, instance.getHeartrate().intValue());
    }
    
    /**
     * Test of getHeartrate method, of class TrackPoint.
     */
    @Test
    public void testGetGpsAccuracy()
    {
        System.out.println("getGpsAccuracy");
        assertNull(instanceSimple.getGpsAccuracy());
        assertEquals(3, instance.getGpsAccuracy().intValue());
    }
    
    
    
}
