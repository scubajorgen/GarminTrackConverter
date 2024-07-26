/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

import net.studioblueplanet.settings.SettingsDevice;
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
public class DeviceFoundEventTest
{
    private final SettingsDevice   device;
    private final DeviceFoundEvent instance1;
    private final DeviceFoundEvent instance2;
    public DeviceFoundEventTest()
    {
        device=new SettingsDevice();
        instance1=new DeviceFoundEvent(DeviceFoundEvent.DeviceFoundEventType.NEWDEVICEFOUND, device, true);
        instance2=new DeviceFoundEvent(DeviceFoundEvent.DeviceFoundEventType.NEWDEVICEFOUND, device, false);
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
     * Test of getType method, of class DeviceFoundEvent.
     */
    @Test
    public void testGetType()
    {
        System.out.println("getType");
        assertEquals(DeviceFoundEvent.DeviceFoundEventType.NEWDEVICEFOUND, instance1.getType());
    }

    /**
     * Test of getDevice method, of class DeviceFoundEvent.
     */
    @Test
    public void testGetDevice()
    {
        System.out.println("getDevice");
        assertEquals(device, instance1.getDevice());
    }

    /**
     * Test of isAttached method, of class DeviceFoundEvent.
     */
    @Test
    public void testIsAttached()
    {
        System.out.println("isAttached");
        assertEquals(true, instance1.isAttached());
        assertEquals(false, instance2.isAttached());
    }
    
}
