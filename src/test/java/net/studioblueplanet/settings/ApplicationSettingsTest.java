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
        assertEquals("./development/device_fenix7_sync/GARMIN/GarminDevice.xml", result.get(0).getDeviceFile());
        assertEquals("./development/device_fenix7_sync/GARMIN/Location/Lctns.fit", result.get(0).getWaypointFile());
    }

    /**
     * Test of getGpxFilePath method, of class ApplicationSettings.
     */
    @Test
    public void testGetGpxFilePath()
    {
        System.out.println("getGpxFilePath");
        assertEquals("./development/gpx", instance.getGpxFileDownloadPath());
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
   
    /**
     * Test of isTrackCompression method, of class ApplicationSettings.
     */
    @Test
    public void testIsTrackCompression()
    {
        System.out.println("isTrackCompression");
        assertEquals(true, instance.isTrackCompression());
    }    

    /**
     * Test of getTrackCompressionMaxError method, of class ApplicationSettings.
     */
    @Test
    public void testGetTrackCompressionMaxError()
    {
        System.out.println("getTrackCompressionMaxError");
        assertEquals(0.3, instance.getTrackCompressionMaxError(), 0.00001);
    }    

    /**
     * Test of getGpxFileUploadPath method, of class ApplicationSettings.
     */
    @Test
    public void testGetGpxFileUploadPath()
    {
        System.out.println("getGpxFileUploadPath");
        assertEquals("./development/gpxRoutes", instance.getGpxFileUploadPath());
    }

    /**
     * Test of getGpxFileDownloadPath method, of class ApplicationSettings.
     */
    @Test
    public void testGetGpxFileDownloadPath()
    {
        System.out.println("getGpxFileDownloadPath");
        assertEquals("./development/gpx", instance.getGpxFileDownloadPath());
    }

    /**
     * Test of isTrackSmoothing method, of class ApplicationSettings.
     */
    @Test
    public void testIsTrackSmoothing()
    {
        System.out.println("isTrackSmoothing");
        assertEquals(false, instance.isTrackSmoothing());
    }

    /**
     * Test of getTrackSmoothingAccuracy method, of class ApplicationSettings.
     */
    @Test
    public void testGetTrackSmoothingAccuracy()
    {
        System.out.println("getTrackSmoothingAccuracy");
        assertEquals(1.5, instance.getTrackSmoothingAccuracy(), 0.001);
    }

}
