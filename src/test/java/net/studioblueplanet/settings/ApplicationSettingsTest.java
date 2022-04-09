/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.settings;

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
public class ApplicationSettingsTest
{
    private static ApplicationSettings instance;
    public ApplicationSettingsTest()
    {
        instance=ApplicationSettings.getInstance();
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
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getInstance method, of class ApplicationSettings.
     */
    @Test
    public void testGetInstance()
    {
        System.out.println("getInstance");
        assertNotNull(instance);
        ApplicationSettings result = ApplicationSettings.getInstance();
        assertEquals(instance, result);
    }

    /**
     * Test of getDevices method, of class ApplicationSettings.
     */
    @Test
    public void testGetDevices()
    {
        System.out.println("getDevices");
        List<SettingsDevice> result = instance.getDevices();
        assertEquals(2, result.size());
        assertEquals("fenix 7 Solar", result.get(0).getName());
        assertEquals("./development/GARMIN2/Location/Lctns.fit", result.get(0).getDeviceFile());
        assertEquals("./development/GARMIN2/GarminDevice.xml", result.get(0).getWaypointFile());
    }

    /**
     * Test of getGpxFilePath method, of class ApplicationSettings.
     */
    @Test
    public void testGetGpxFilePath()
    {
        System.out.println("getGpxFilePath");
        assertEquals("./development/gpx", instance.getGpxFilePath());
    }

    /**
     * Test of getDebugLevel method, of class ApplicationSettings.
     */
    @Test
    public void testGetDebugLevel()
    {
        System.out.println("getDebugLevel");
        assertEquals("info", instance.getDebugLevel());
    }
    
}
