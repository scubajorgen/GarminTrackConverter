/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.settings;

/**
 *
 * @author jorgen
 */
public class SettingsDevice
{
    // Device name
    public String name;
    
    // directory where to find the tracks
    public String trackFilePath;
    
    // Directory where to find the courses
    public String routeFilePath;
    
    // Directory where to upload new files
    public String newFilePath;
    
    // Directory where to find locations/waypoints
    public String locationFilePath;
    
    // The full path of the locations/waypoints file
    public String waypointFile;
    
    // The full path of the device XML file
    public String deviceFile;
    
    
    // When multiple devices are connected the device with the lowest number is selected
    public int devicePriority;
}
