/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package net.studioblueplanet.settings;

import java.time.ZonedDateTime;
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
public class SettingsDeviceTest
{
    
    public SettingsDeviceTest()
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
    }
    
    @After
    public void tearDown()
    {
    }


    /**
     * Test of getLastDateTimeSyncedZoned method, of class SettingsDevice.
     */
    @Test
    public void testGetLastDateTimeSyncedZoned()
    {
        System.out.println("getLastDateTimeSyncedZoned");
        SettingsDevice instance = new SettingsDevice();
        instance.setLastDateTimeSynced("2026-11-14T12:34:56Z");
        assertEquals("2026-11-14T12:34:56Z", instance.getLastDateTimeSyncedZoned().toString());
        instance.setLastDateTimeSynced("2026-11-14T13:34:56+01:00");
        assertEquals("2026-11-14T12:34:56Z", instance.getLastDateTimeSyncedZoned().toString());
    }

    /**
     * Test of setLastDateTimeSyncedZoned method, of class SettingsDevice.
     */
    @Test
    public void testSetLastDateTimeSyncedZoned()
    {
        System.out.println("setLastDateTimeSyncedZoned");
        SettingsDevice instance = new SettingsDevice();

        instance.setLastDateTimeSyncedZoned(ZonedDateTime.parse("2026-11-14T12:34:56Z"));
        assertEquals("2026-11-14T12:34:56Z", instance.getLastDateTimeSynced());

        instance.setLastDateTimeSyncedZoned(ZonedDateTime.parse("2026-11-14T13:34:56+01:00"));
        assertEquals("2026-11-14T12:34:56Z", instance.getLastDateTimeSynced());
    }
}
