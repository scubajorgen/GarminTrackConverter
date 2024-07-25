/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.io.File;
import java.util.List;
import net.studioblueplanet.settings.ApplicationSettings;
import net.studioblueplanet.settings.SettingsDevice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class runs a process that checks for devices to attach to the USB
 * @author jorgen
 */
public class DeviceMonitor extends Thread
{
    private final static Logger             LOGGER = LogManager.getLogger(DeviceMonitor.class);
    private static DeviceMonitor            theInstance=null;   // The one and only singleton instance of this class
    private final ApplicationSettings       settings;           // The application settings    
    private DeviceFoundListener             listener;

    private final Thread                    thread;             // Thread monitoring attached devices
    private boolean                         threadExit;         // Thread exit flag

    private SettingsDevice                  currentDevice;      // The device of which currently info is shown
    private boolean                         isAttached;         // Indicates if the current device is attached to USB
    
    /**
     * Constructor; starts the process
     */
    private DeviceMonitor()
    {
        settings        =ApplicationSettings.getInstance();
        threadExit      =false;
        thread          =new Thread(this);
        thread.start();
    }
    
    /**
     * Stops the process
     */
    public void stopProcess()
    {
        synchronized(this)
        {
            threadExit=true;
        }
    }
    
    /**
     * Returns the one and only instance of this class (singleton)
     * @return The instance
     */
    public static DeviceMonitor getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new DeviceMonitor();
        }
        return theInstance;
    }
    
    
    /**
     * Sets the DeviceFound listener. Only on listener can be subscribed.
     * @param listener Listener to subscribe
     */
    public void setDeviceFoundListener(DeviceFoundListener listener)
    {
        this.listener=listener;
    }
    
    /**
     * Calls the listener method and sends the event
     * @param event Event to send
     */
    private void sendEvent(DeviceFoundEvent event)
    {
        if (listener!=null)
        {
            listener.deviceFound(event);
        }
    }
    
    /**
     * Thread function. The responsibility of this thread function is to 
     * monitor whether there are devices attached or removed
     */
    @Override
    public void run()
    {
        boolean                         localThreadExit;
        SettingsDevice                  deviceFound;
        boolean                         attachedFound;
        List<SettingsDevice>            devices;
        int                             minPrio;
        
        // TODO: revise the synchronized stuff throughout the application
        synchronized(this)
        {
            devices=settings.getDevices();
        }        

        LOGGER.info("Thread started");
        do
        {
            // Ugly work-around: start with a wait to give the UI thread a chance 
            // to start the UI so it runs before this trhead continues
            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
                LOGGER.error("Thread sleep interrupted");
            }
            
            // Find the current device. This may be
            // * An device that is attached to the USB port
            // * A device of type USBDevice that has a local buffer that is synced
            // With multiple devices attached
            // * An USB attached device always gets priority
            // * With equal device type: the device with lowest prio value wins
            minPrio         =Integer.MAX_VALUE;
            deviceFound     =null;
            attachedFound   =false;
            
            // Check if there is a device physically attached to USB as USB Mass Storage or USB Device
            UsbInfo usbInfo; 
            if (settings.isDebugSimulateUsb())
            {
                // Use simulation
                usbInfo=new UsbInfoSim();
            }
            else
            {
                // Monitor the USB
                usbInfo=new UsbInfoImpl();
            }
            for (SettingsDevice settingsDevice : devices)
            {
                if (usbInfo.isUsbDeviceConnected(settingsDevice.getUsbVendorId(), settingsDevice.getUsbProductId()))
                {
                    if (settingsDevice.getDevicePriority()<minPrio)
                    {
                        // We found a known device attached to USB; 
                        // now check if the mass storage is already mounted ;
                        // if not, skip it for now
                        File deviceFile=new File(settingsDevice.getDeviceFile());
                        if (deviceFile.exists())
                        {
                            deviceFound     =settingsDevice;    // We found a device to display
                            attachedFound   =true;              // It is attached
                            minPrio         =deviceFound.getDevicePriority();
                        }
                    }                    
                }
            }
            
            // Check if a Garmin device is being connected. First all device attaches with ID 091e:0003 (product ID 3)
            // Then after a while it attaches with the proper product ID
            if (deviceFound==null)
            {
                if (usbInfo.isUsbDeviceConnected(settings.getUsbConnectionStartVendorId(), settings.getUsbConnectionStartProductId()))
                {
                    DeviceFoundEvent e=new DeviceFoundEvent(DeviceFoundEvent.DeviceFoundEventType.NEWDEVICEATTACHEDANDWAITING, currentDevice, isAttached);
                    sendEvent(e);
                }
            }
                
            // If there a Device is not connected, we may show the sync buffer of a device of type USBDevice
            // This is only donw when the setting showSyncWhenNoDevicesAttached=true
            if (deviceFound==null && settings.isShowSyncWhenNoDeviceAttached())
            {
                for (SettingsDevice settingsDevice : devices)
                {
                    if (settingsDevice.getType().equals("USBDevice"))
                    {
                        if (settingsDevice.getDevicePriority()<minPrio)
                        {
                            deviceFound=settingsDevice;
                            minPrio=deviceFound.getDevicePriority();
                        }                    
                    }
                }  
            }
            
            // If we found a change in USB attachment...
            if (attachedFound!=isAttached)
            {

                isAttached=attachedFound;

                // ... check if the same device is attached/detached
                // If so update the sync button
                // If another device is attached, it is handled by the next code
                if (deviceFound==currentDevice)
                {
                    DeviceFoundEvent e=new DeviceFoundEvent(DeviceFoundEvent.DeviceFoundEventType.ATTACHEDSTATECHANGED, currentDevice, isAttached);
                    sendEvent(e);
                }
            }
            
            // If we found any current device...
            if (deviceFound!=null)
            {
                // ... check if the device found has changed. If so, we found a new current device
                // so lets initialise it
                if (deviceFound!=currentDevice)
                {
                    LOGGER.info("Found new device {}, is attached to USB: {}", deviceFound.getName(), isAttached);

                    // We found a new device
                    currentDevice   =deviceFound;
                    DeviceFoundEvent e=new DeviceFoundEvent(DeviceFoundEvent.DeviceFoundEventType.NEWDEVICEFOUND, currentDevice, isAttached);
                    sendEvent(e);
                }
                // Same device still attached
                else
                { 
                    DeviceFoundEvent e=new DeviceFoundEvent(DeviceFoundEvent.DeviceFoundEventType.NONEWDEVICEFOUND, currentDevice, isAttached);
                    sendEvent(e);
                }
            }
            // No current device found
            else
            {
                if (currentDevice!=null)
                {
                    currentDevice=null;
                    DeviceFoundEvent e=new DeviceFoundEvent(DeviceFoundEvent.DeviceFoundEventType.DEVICEREMOVED, currentDevice, isAttached);
                    sendEvent(e);
                }
            }

            synchronized(this)
            {
                localThreadExit         =threadExit;
            }
        }        
        while (!localThreadExit);
    }
}
