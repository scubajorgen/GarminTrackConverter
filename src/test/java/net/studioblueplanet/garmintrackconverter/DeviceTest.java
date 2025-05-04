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
import static org.junit.Assert.*;

/**
 *
 * @author jorgen
 */
public class DeviceTest
{
    private static Device instance1;
    private static Device instance2;
    
    public DeviceTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        instance1=new Device("src/test/resources/GarminDevice.xml");
        instance2=new Device("Test model", "Test ID", "Test description");
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
     * Test of getDeviceDescription method, of class Device.
     */
    @Test
    public void testGetDeviceDescription()
    {
        System.out.println("getDeviceDescription");
        assertEquals("Edge 830 - 3366957239", instance1.getDeviceDescription());
        Device device=new Device("src/test/resources/NonExistent.xml");
        assertEquals("unknown - unknown", device.getDeviceDescription());
        assertEquals("Test description - Test ID", instance2.getDeviceDescription());
    }

    /**
     * Test of getId method, of class Device.
     */
    @Test
    public void testGetId()
    {
        System.out.println("getId");
        assertEquals("3366957239", instance1.getId());
        assertEquals("Test ID", instance2.getId());
    }

    /**
     * Test of getModel method, of class Device.
     */
    @Test
    public void testGetModel()
    {
        System.out.println("getModel");
        assertEquals("006-B3122-00910Edge 830", instance1.getModel());
        assertEquals("Test model", instance2.getModel());
    }

    /**
     * Test of getDescription method, of class Device.
     */
    @Test
    public void testGetDescription()
    {
        System.out.println("getDescription");
        assertEquals("Edge 830", instance1.getDescription());
        assertEquals("Test description", instance2.getDescription());
    }
    
}
