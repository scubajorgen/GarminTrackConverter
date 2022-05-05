/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
public class LocationTest
{
    private static Location instance;
    public LocationTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        ZonedDateTime dateTime;
        
        dateTime=ZonedDateTime.of(2022, 5, 5, 8, 46, 57, 0, ZoneId.of("UTC"));
        instance=new Location("name", "description", dateTime, 53.6, 6.5, 10.0, 3);
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
     * Test of getName method, of class Location.
     */
    @Test
    public void testGetName()
    {
        System.out.println("getName");
        assertEquals("name", instance.getName());
    }

    /**
     * Test of getDescription method, of class Location.
     */
    @Test
    public void testGetDescription()
    {
        System.out.println("getDescription");
        assertEquals("description", instance.getDescription());
    }

    /**
     * Test of getDateTime method, of class Location.
     */
    @Test
    public void testGetDateTime()
    {
        System.out.println("getDateTime");
        assertEquals("2022-05-05 08:46:57", instance.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));
    }

    /**
     * Test of getLatitude method, of class Location.
     */
    @Test
    public void testGetLatitude()
    {
        System.out.println("getLatitude");
        assertEquals(53.6, instance.getLatitude(), 0.0000001);
    }

    /**
     * Test of getLongitude method, of class Location.
     */
    @Test
    public void testGetLongitude()
    {
        System.out.println("getLongitude");
        assertEquals(6.5, instance.getLongitude(), 0.0000001);
    }

    /**
     * Test of getElevation method, of class Location.
     */
    @Test
    public void testGetElevation()
    {
        System.out.println("getElevation");
        assertEquals(10.0, instance.getElevation(),0.0000001);
    }

    /**
     * Test of getSymbol method, of class Location.
     */
    @Test
    public void testGetSymbol()
    {
        System.out.println("getSymbol");
        assertEquals(3, instance.getSymbol());
    }
}
