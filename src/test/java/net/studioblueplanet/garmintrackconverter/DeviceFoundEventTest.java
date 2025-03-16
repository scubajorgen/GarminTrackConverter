/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

import java.util.HashMap;
import java.util.Map;
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
        Map<SettingsDevice, Boolean> devicesAttached1=new HashMap<>();
        devicesAttached1.put(device, true);
        Map<SettingsDevice, Boolean> devicesAttached2=new HashMap<>();
        devicesAttached2.put(device, false);
        
        instance1=new DeviceFoundEvent(DeviceFoundEvent.DeviceFoundEventType.NEWDEVICEFOUND, device, devicesAttached1);
        instance2=new DeviceFoundEvent(DeviceFoundEvent.DeviceFoundEventType.NEWDEVICEFOUND, device, devicesAttached2);
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
     * Test of getDevicesAttached method, of class DeviceFoundEvent.
     */
    @Test
    public void testgetDevicesAttached()
    {
        System.out.println("getDevicesAttached");
        assertEquals(true , instance1.getDevicesAttached().get(device));
        assertEquals(false, instance2.getDevicesAttached().get(device));
    }
    
}
