/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.util.Map;
import net.studioblueplanet.settings.SettingsDevice;

/**
 *
 * @author jorgen
 */
public class DeviceFoundEvent
{
    enum DeviceFoundEventType
    {
        NEWDEVICEATTACHEDANDWAITING,
        NEWDEVICEFOUND,
        NONEWDEVICEFOUND,
        DEVICEREMOVED,
        ATTACHEDSTATECHANGED,
        NODEVICECONNECTED
    }
    
    private final DeviceFoundEventType          type;
    private final SettingsDevice                device;
    private final Map<SettingsDevice, Boolean>  devicesAttached;
    
    public DeviceFoundEvent(DeviceFoundEventType type, SettingsDevice newDevice, Map<SettingsDevice, Boolean>  devicesAttached)
    {
        this.type               =type;
        this.device             =newDevice;
        this.devicesAttached    =devicesAttached;
    }
    
    public DeviceFoundEventType getType()
    {
        return type;
    }
    
    public SettingsDevice getDevice()
    {
        return device;
    }

    public Map<SettingsDevice, Boolean> getDevicesAttached()
    {
        return devicesAttached;
    }

}
