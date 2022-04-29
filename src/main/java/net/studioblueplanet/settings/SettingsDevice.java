/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author jorgen
 */
public class SettingsDevice
{
    // Device name
    private String name;
    
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
    
    // When multiple devices are connected the device with the lowest number is selected
    private int devicePriority;

    // External command to sync MTP device like Fenix 7 to a local directory structure
    private String               syncCommand;

    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

    public int getDevicePriority()
    {
        return devicePriority;
    }

    public void setDevicePriority(int devicePriority)
    {
        this.devicePriority = devicePriority;
    }

    public String getSyncCommand()
    {
        return syncCommand;
    }

    public void setSyncCommand(String syncCommand)
    {
        this.syncCommand = syncCommand;
    }
}
