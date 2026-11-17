/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;

/**
 *
 * @author jorgen
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettingsDevice
{
    // Device name
    private String name;

    // The Device Type: USBDevice or USBMassStorage
    private String type;
    
    // The Manufacturer ID of the device on the USB
    private Short usbVendorId;
    
    // The Device ID of the device on the USB
    private Short usbProductId;
    
    // directory where to find the tracks
    private String trackFilePath;
    
    // Directory where to find the courses
    private String routeFilePath;
    
    // Directory where to upload new files
    private String newFilePath;
    
    // Directory where to find locations/waypoints
    private String locationFilePath;
    
    // The full path of the locations/waypoints file
    private String waypointFile;
    
    // The full path of the device XML file
    private String deviceFile;
    
    // External command to sync MTP device like Fenix 7 to a local directory structure
    private String               syncCommand;
    
    // When multiple devices are connected the device with the lowest number is selected
    private int devicePriority;

    // Garmin Connect API Key
    private String               apiKey;
    
    // Last date synchronized
    String                       lastDateTimeSynced;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Short getUsbVendorId()
    {
        return usbVendorId;
    }

    public void setUsbVendorId(Short usbVendorId)
    {
        this.usbVendorId = usbVendorId;
    }

    public Short getUsbProductId()
    {
        return usbProductId;
    }

    public void setUsbProductId(Short usbProductId)
    {
        this.usbProductId = usbProductId;
    }

    public String getTrackFilePath()
    {
        return trackFilePath;
    }

    public void setTrackFilePath(String trackFilePath)
    {
        this.trackFilePath = trackFilePath;
    }

    public String getRouteFilePath()
    {
        return routeFilePath;
    }

    public void setRouteFilePath(String routeFilePath)
    {
        this.routeFilePath = routeFilePath;
    }

    public String getNewFilePath()
    {
        return newFilePath;
    }

    public void setNewFilePath(String newFilePath)
    {
        this.newFilePath = newFilePath;
    }

    public String getLocationFilePath()
    {
        return locationFilePath;
    }

    public void setLocationFilePath(String locationFilePath)
    {
        this.locationFilePath = locationFilePath;
    }

    public String getWaypointFile()
    {
        return waypointFile;
    }

    public void setWaypointFile(String waypointFile)
    {
        this.waypointFile = waypointFile;
    }

    public String getDeviceFile()
    {
        return deviceFile;
    }

    public void setDeviceFile(String deviceFile)
    {
        this.deviceFile = deviceFile;
    }

    public String getSyncCommand()
    {
        return syncCommand;
    }

    public void setSyncCommand(String syncCommand)
    {
        this.syncCommand = syncCommand;
    }

    public int getDevicePriority()
    {
        return devicePriority;
    }

    public void setDevicePriority(int devicePriority)
    {
        this.devicePriority = devicePriority;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public String getLastDateTimeSynced()
    {
        return lastDateTimeSynced;
    }

    public void setLastDateTimeSynced(String lastDateTimeSynced)
    {
        this.lastDateTimeSynced = lastDateTimeSynced;
    }

    @JsonIgnore
    public ZonedDateTime getLastDateTimeSyncedZoned()
    {
        ZonedDateTime dateTime=null;
        if (lastDateTimeSynced!=null)
        {
            dateTime=ZonedDateTime.parse(lastDateTimeSynced).withZoneSameInstant(ZoneOffset.UTC);
        }
        return dateTime;
    }

    @JsonIgnore
    public void setLastDateTimeSyncedZoned(ZonedDateTime lastDateTimeSynced)
    {
        this.lastDateTimeSynced = lastDateTimeSynced.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
    }

}
