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
    private TrackPoint builtInstance;
    
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
        TrackPoint.TrackPointBuilder builder=new TrackPoint.TrackPointBuilder(12.34, 34.12);
        instanceSimple  =builder.build();
        ZonedDateTime dateTime=ZonedDateTime.of(2022, 3, 3, 13, 58, 0, 0, ZoneId.of("UTC"));
        builder=new TrackPoint.TrackPointBuilder(21.43, 43.21)
                .dateTime(dateTime)
                .elevation(-1.0)
                .speed(5.0)
                .distance(100.0)
                .temperature(2)
                .heartrate(77)
                .ehpe(4)
                .gpsAccuracy(3);
        instance        =builder.build();
        
        builder=new TrackPoint.TrackPointBuilder(1.0, 2.0)
                .dateTime(ZonedDateTime.of(2023, 12, 2, 9, 30, 0, 0, ZoneId.of("UTC")))
                .distance(3.0)
                .ehpe(4)
                .elevation(5.0)
                .gpsAccuracy(6)
                .heartrate(7)
                .speed(8.0)
                .stamina(9)
                .staminaPotential(10)
                .temperature(11)
                .respirationrate(12.0)
                .unknown(13);
        builtInstance=builder.build();
    }
    
    @After
    public void tearDown()
    {
    }

    
    
    
    /**
     * Test of getDateTime method, of class TrackPoint.
     */
    @Test
    public void testTrackPointBuilder()
    {
        System.out.println("TrackPointBuilder");

        assertEquals("2023-12-02 09:30:00", builtInstance.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        assertEquals(3.0, builtInstance.getDistance(), 0.0001);
        assertEquals(4, builtInstance.getEhpe().intValue());
        assertEquals(5.0, builtInstance.getElevation(), 0.0001);
        assertEquals(6, builtInstance.getGpsAccuracy().intValue());
        assertEquals(7, builtInstance.getHeartrate().intValue());
        assertEquals(8.0, builtInstance.getSpeed(), 0.0001);
        assertEquals(9, builtInstance.getStamina().intValue());
        assertEquals(10, builtInstance.getStaminaPotential().intValue());
        assertEquals(11, builtInstance.getTemperature().intValue());
        assertEquals(12.0, builtInstance.getRespirationrate(), 0.001);
        assertEquals(13, builtInstance.getUnknown().intValue());
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
     * Test of getSpeedNotNull method, of class TrackPoint.
     */
    @Test
    public void testGetSpeedNotNull()
    {
        System.out.println("getSpeedNotNull");
        assertEquals(0.0, instanceSimple.getSpeedNotNull(), 0.0001);
        assertEquals(5.0, instance.getSpeedNotNull(), 0.0001);
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
     * Test of getHeartrateNotNull method, of class TrackPoint.
     */
    @Test
    public void testGetHeartrateNotNull()
    {
        System.out.println("getHeartrateNotNull");
        assertEquals(0, instanceSimple.getHeartrateNotNull());
        assertEquals(77, instance.getHeartrateNotNull());
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
    
    /**
     * Test of getEhpe method, of class TrackPoint.
     */
    @Test
    public void testGeEhpe()
    {
        System.out.println("getEhpe");
        assertNull(instanceSimple.getEhpe());
        assertEquals(4, instance.getEhpe().intValue());
    }
    
    /**
     * Test of compareTo method, of class TrackPoint.
     */
    @Test
    public void testCompareTo()
    {
        ZonedDateTime dateTime;
        TrackPoint    comparePoint;
        
        System.out.println("compareTo");
        
        dateTime=ZonedDateTime.of(2022, 3, 3, 13, 58, 0, 0, ZoneId.of("UTC"));
        comparePoint =new TrackPoint.TrackPointBuilder (21.43, 43.21).dateTime(dateTime).build();
        assertEquals(0, instance.compareTo(comparePoint));

        dateTime=ZonedDateTime.of(2022, 3, 3, 13, 58, 01, 0, ZoneId.of("UTC"));
        comparePoint =new TrackPoint.TrackPointBuilder (21.43, 43.21).dateTime(dateTime).build();
        assertEquals(-1, instance.compareTo(comparePoint));

        dateTime=ZonedDateTime.of(2022, 3, 3, 13, 57, 58, 0, ZoneId.of("UTC"));
        comparePoint =new TrackPoint.TrackPointBuilder (21.43, 43.21).dateTime(dateTime).build();
        assertEquals(1, instance.compareTo(comparePoint));

        dateTime=ZonedDateTime.of(2021, 3, 3, 13, 58, 0, 0, ZoneId.of("UTC"));
        comparePoint =new TrackPoint.TrackPointBuilder (21.43, 43.21).dateTime(dateTime).build();
        assertEquals(1, instance.compareTo(comparePoint));
    }    

    /**
     * Test of updateCoordinate method, of class TrackPoint.
     */
    @Test
    public void testUpdateCoordinate()
    {
        System.out.println("updateCoordinate");
        instance.updateCoordinate(33.33, 44.44);
        assertEquals(33.33, instance.getLatitude(), 0.0000001);
        assertEquals(44.44, instance.getLongitude(), 0.0000001);
    }

    /**
     * Test of clone method, of class TrackPoint.
     */
    @Test
    public void testClone()
    {
        System.out.println("clone");
        TrackPoint clone    =builtInstance.clone();
        assertEquals("2023-12-02 09:30:00", clone.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        assertEquals(3.0, clone.getDistance(), 0.0001);
        assertEquals(4, clone.getEhpe().intValue());
        assertEquals(5.0, clone.getElevation(), 0.0001);
        assertEquals(6, clone.getGpsAccuracy().intValue());
        assertEquals(7, clone.getHeartrate().intValue());
        assertEquals(8.0, clone.getSpeed(), 0.0001);
        assertEquals(9, clone.getStamina().intValue());
        assertEquals(10, clone.getStaminaPotential().intValue());
        assertEquals(11, clone.getTemperature().intValue());
        assertEquals(12.0, clone.getRespirationrate(), 0.001);
        assertEquals(13, clone.getUnknown().intValue());
    }

    /**
     * Test of getEhpe method, of class TrackPoint.
     */
    @Test
    public void testGetEhpe()
    {
        System.out.println("getEhpe");
        assertEquals(4, builtInstance.getEhpe().intValue());
    }

    /**
     * Test of isValid method, of class TrackPoint.
     */
    @Test
    public void testIsValid()
    {
        System.out.println("isValid");
        assertEquals(true, builtInstance.isValid());
        
        TrackPoint invalid;
        invalid=new TrackPoint.TrackPointBuilder(0.0, 5.1).build();
        assertEquals(false, invalid.isValid());
        invalid=new TrackPoint.TrackPointBuilder(5.1, 0.0).build();
        assertEquals(false, invalid.isValid());
        invalid=new TrackPoint.TrackPointBuilder(180.0, 5.1).build();
        assertEquals(false, invalid.isValid());
        invalid=new TrackPoint.TrackPointBuilder(5.1, 180.0).build();
        assertEquals(false, invalid.isValid());
    }
    
}
