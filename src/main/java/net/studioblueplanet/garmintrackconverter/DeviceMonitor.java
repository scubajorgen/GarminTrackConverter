/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.studioblueplanet.settings.ApplicationSettings;
import net.studioblueplanet.garmintrackconverter.DeviceFoundEvent.DeviceFoundEventType;
import net.studioblueplanet.settings.SettingsDevice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class runs a process that checks for devices to attach to the USB
 * @author jorgen
 */
public class DeviceMonitor extends Thread
{
    private enum MonitoringState
    {
        NODEVICE,
        DEVICE,
        DEVICEANDWAITING,
        NODEVICEANDWAITING
    }
    
    private class DeviceState
    {
        public MonitoringState              state;              // State of the monitoring process
        public SettingsDevice               deviceFound;        // Device to display
        public boolean                      isAttached;         // Indicates if the device found is attached via USB
        public Map<SettingsDevice, Boolean> devicesAttached;    // Full list of devices and if they are attached
    }
    
    private final static Logger             LOGGER      = LogManager.getLogger(DeviceMonitor.class);
    private final static String             USBSIMFILE  ="./usbsim.txt";
    private static DeviceMonitor            theInstance =null;   // The one and only singleton instance of this class
    private final ApplicationSettings       settings;           // The application settings    
    private final List<SettingsDevice>      devices;            // List of device definitions/settings

    private DeviceFoundListener             listener;

    private final Thread                    thread;             // Thread monitoring attached devices
    private boolean                         threadExit;         // Thread exit flag
    
    private SettingsDevice                  preferredDevice;    // Device chosen by the user
    
    /**
     * Constructor; starts the process
     */
    private DeviceMonitor()
    {
        preferredDevice =null;
        settings        =ApplicationSettings.getInstance();
        devices         =settings.getDevices();
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
     * Set the device that the user wants to see
     * @param preferredDevice The preferred device
     */
    public void setPreferredDevice(SettingsDevice preferredDevice)
    {
        synchronized (this)
        {
            this.preferredDevice=preferredDevice;
        }
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
     * If a listener is attached, send the event of given type
     * @param type Type of the event
     * @param device Device found
     * @param deviesAttached List of devices with their attached state
     */
    private void sendEvent(DeviceFoundEventType type, SettingsDevice device, Map<SettingsDevice, Boolean> devicesAttached)
    {
        DeviceFoundEvent event=new DeviceFoundEvent(type, device, devicesAttached);
        DeviceFoundListener localListener;
        synchronized (this)
        {
            localListener=listener;
        }
        if (localListener!=null)
        {
            localListener.deviceFound(event);
        }
    }
    
    /**
     * This method establishes the current state. 
     * @return Current state
     */
    private DeviceState getCurrentState(UsbInfo usbInfo, SettingsDevice preferred)
    {
        DeviceState state=new DeviceState();

        int minPrio         =Integer.MAX_VALUE;
        state.deviceFound   =null;
        state.isAttached    =false;
        state.state         =MonitoringState.NODEVICE;

        // Check if there is a device physically attached to USB as USB Mass Storage or USB Device
        usbInfo.usbRefresh();

        // Find the attached device with the lowest priority value
        state.devicesAttached=new HashMap<>();
        for (SettingsDevice settingsDevice : devices)
        {
            if (usbInfo.isUsbDeviceConnected(settingsDevice.getUsbVendorId(), settingsDevice.getUsbProductId()))
            {
                int prio=settingsDevice.getDevicePriority();
                if (settingsDevice==preferred)
                {
                    prio=-1;
                }
                if (prio<minPrio)
                {
                    // We found a known device attached to USB; 
                    // now check if the mass storage is already mounted ;
                    // if not, skip it for now
                    File deviceFile=new File(settingsDevice.getDeviceFile());
                    String type=settingsDevice.getType();
                    if ("USBMassStorage".equals(type) && (deviceFile.exists()) || ("USBDevice".equals(type)))
                    {
                        state.deviceFound   =settingsDevice;    // We found a device to display
                        state.isAttached    =true;              // It is attached
                        state.state         =MonitoringState.DEVICE;
                        minPrio             =prio;
                    }
                }
                state.devicesAttached.put(settingsDevice, true);
            }
            else
            {
                if (settingsDevice==preferred && settingsDevice.getType().equals("USBDevice"))
                {
                    state.deviceFound   =settingsDevice;            // We choose the user preferred device
                    state.isAttached    =false;                     // It is not attached
                    state.state         =MonitoringState.DEVICE;
                    minPrio             =-1;                        // Set to max prio
                }
                state.devicesAttached.put(settingsDevice, false);
            }
        }

        // If there no Device connected, we may still show the sync buffer/cache 
        // of a device of type USBDevice.
        // This is only done when the setting showSyncWhenNoDevicesAttached=true
        if (state.deviceFound==null && settings.isShowSyncWhenNoDeviceAttached())
        {
            for (SettingsDevice settingsDevice : devices)
            {
                if (settingsDevice.getType().equals("USBDevice"))
                {
                    int prio=settingsDevice.getDevicePriority();
                    if (prio<minPrio)
                    {
                        state.deviceFound   =settingsDevice;
                        state.isAttached    =false;
                        state.state         =MonitoringState.DEVICE;
                        minPrio             =prio;
                    }                    
                }
            }  
        }

        // Check if a Garmin device is being connected. First all device attaches with ID 091e:0003 (product ID 3)
        // Then after a while it attaches with the proper product ID
        if (usbInfo.isUsbDeviceConnected(settings.getUsbConnectionStartVendorId(), settings.getUsbConnectionStartProductId()))
        {
            if (state.state==MonitoringState.DEVICE)
            {
                state.state=MonitoringState.DEVICEANDWAITING;
            }
            else
            {
                state.state=MonitoringState.NODEVICEANDWAITING;
            }
        }
        return state;
    }
    
    /**
     * This method sends events based on previous state and the current state
     * @param previousState  The previous state
     * @param currentState   The new state
     */
    private void processStateAndSendEvent(DeviceState previousState, DeviceState currentState)
    {
        // No process the state and send events
        // If we found any current device...
        if (currentState.deviceFound!=null)
        {
            // ... check if the device found has changed. 
            // If so, we found a new current device so lets initialise it
            if (currentState.deviceFound!=previousState.deviceFound)
            {
                LOGGER.info("Monitor: Found new device {}, is attached to USB: {}", currentState.deviceFound.getName(), currentState.isAttached);
                // We found a new device
                sendEvent(DeviceFoundEventType.NEWDEVICEFOUND, currentState.deviceFound, currentState.devicesAttached);
            }
            // Same device still attached
            else
            { 
                // If we found a change in USB attachment...
                if (currentState.isAttached!=previousState.isAttached)
                {
                    // Send an ATTACHEDSTATECHANGED

                    LOGGER.info("Monitor: Same device still {} but attachment state changed: {}", currentState.deviceFound.getName(), currentState.isAttached);
                    sendEvent(DeviceFoundEventType.ATTACHEDSTATECHANGED, currentState.deviceFound, currentState.devicesAttached);
                }    
                else
                {
                    // Attachment not changed: just update that no new device has been found
                    sendEvent(DeviceFoundEventType.NONEWDEVICEFOUND, currentState.deviceFound, currentState.devicesAttached);
                }
            }
        }
        // No new current device found
        else
        {
            if (previousState.deviceFound!=null)
            {
                LOGGER.info("Monitor: Device {} removed. Attached {}", previousState.deviceFound.getName(), currentState.isAttached);
                sendEvent(DeviceFoundEventType.DEVICEREMOVED, previousState.deviceFound, currentState.devicesAttached);
            }
            else
            {
                sendEvent(DeviceFoundEventType.NODEVICECONNECTED, currentState.deviceFound, currentState.devicesAttached);
                  
            }
        }            
        if ( (currentState.state==MonitoringState.DEVICEANDWAITING || currentState.state==MonitoringState.NODEVICEANDWAITING) &&
            !(previousState.state==MonitoringState.DEVICEANDWAITING || previousState.state==MonitoringState.NODEVICEANDWAITING))
        {
            LOGGER.info("Monitor: First device attachment found");
            sendEvent(DeviceFoundEventType.NEWDEVICEATTACHEDANDWAITING, currentState.deviceFound, currentState.devicesAttached);
        }        
    }

    /**
     * Thread function. The responsibility of this thread function is to 
     * monitor whether there are devices attached or removed
     */
    @Override
    public void run()
    {
        SettingsDevice                  preferred=null;
        boolean                         localThreadExit;
        DeviceState                     previousState=new DeviceState();
        
        LOGGER.info("Thread started");
        UsbInfo usbInfo; 
        if (settings.isDebugSimulateUsb())
        {
            // Use simulation
            usbInfo=new UsbInfoSim(USBSIMFILE);
        }
        else
        {
            // Monitor the USB
            usbInfo=new UsbInfoImpl();
        }

        do
        {
            // Ugly work-around: start with a wait to give the UI thread a chance 
            // to start the UI so it runs before this trhead continues
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                LOGGER.error("Thread sleep interrupted");
            }
            
            // Find the current device. This may be (in this order):
            // 1. A real device that is attached to the USB port (or simulated)
            // 2. The local cache of a device of type USBDevice if no device connected
            // If multiple devices are attached or if multiple local caches are present
            // the device/cache with the lowest priority value wins

            DeviceState state=getCurrentState(usbInfo, preferred);
            
            processStateAndSendEvent(previousState, state);
            
            previousState=state;
            
            synchronized(this)
            {
                preferred               =preferredDevice;
                localThreadExit         =threadExit;
            }
        }        
        while (!localThreadExit);
    }
}
