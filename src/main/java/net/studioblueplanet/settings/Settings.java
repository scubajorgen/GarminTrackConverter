/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
/**
 *
 * @author jorgen
 */
public class Settings
{
    @JsonProperty("debugLevel")
    private String               debugLevel;
    @JsonProperty("gpxFilePath")
    private String               gpxFilePath;
    @JsonProperty("devices")
    private List<SettingsDevice> devices;

    public String getDebugLevel()
    {
        return debugLevel;
    }

    public void setDebugLevel(String debugLevel)
    {
        this.debugLevel = debugLevel;
    }

    public String getGpxFilePath()
    {
        return gpxFilePath;
    }

    public void setGpxFilePath(String gpxFilePath)
    {
        this.gpxFilePath = gpxFilePath;
    }

    public List<SettingsDevice> getDevices()
    {
        return devices;
    }

    public void setDevices(List<SettingsDevice> devices)
    {
        this.devices = devices;
    }
    
    
}
