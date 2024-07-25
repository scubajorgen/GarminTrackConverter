/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.util.EventObject;
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
        ATTACHEDSTATECHANGED
    }
    
    private final DeviceFoundEventType  type;
    private final SettingsDevice        device;
    private final Boolean               isAttached;
    
    public DeviceFoundEvent(DeviceFoundEventType type, SettingsDevice newDevice, boolean isAttached)
    {
        this.type       =type;
        this.device     =newDevice;
        this.isAttached =isAttached;
    }
    
    public DeviceFoundEventType getType()
    {
        return type;
    }
    
    public SettingsDevice getDevice()
    {
        return device;
    }
    
    public boolean isAttached()
    {
        return isAttached;
    }
}
