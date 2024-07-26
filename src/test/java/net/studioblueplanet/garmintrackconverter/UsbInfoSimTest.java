/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
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
public class UsbInfoSimTest
{
    private final UsbInfoSim instance;
    
    public UsbInfoSimTest()
    {
        instance=new UsbInfoSim("./src/test/resources/usbsim.txt");
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
     * Test of usbRefresh method, of class UsbInfoSim.
     */
    @Test
    public void testUsbRefresh()
    {
        System.out.println("usbRefresh");
        instance.usbRefresh();
    }

    /**
     * Test of isUsbDeviceConnected method, of class UsbInfoSim.
     */
    @Test
    public void testIsUsbDeviceConnected()
    {
        System.out.println("isUsbDeviceConnected");
        assertEquals(false, instance.isUsbDeviceConnected(0x91e, 0x0003));
        assertEquals(true , instance.isUsbDeviceConnected(0x91e, 0x261f));
        assertEquals(false, instance.isUsbDeviceConnected(0x91e, 0x2c32));
        assertEquals(true , instance.isUsbDeviceConnected(0x91e, 0x4f42));
        assertEquals(true , instance.isUsbDeviceConnected(0x91e, 0x2f03));
    }
    
}
