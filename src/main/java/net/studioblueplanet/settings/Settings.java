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
    @JsonProperty("gpxFileDownloadPath")
    private String               gpxFileDownloadPath;
    @JsonProperty("gpxFileUploadPath")
    private String               gpxFileUploadPath;
    @JsonProperty("trackSmoothing")
    private boolean              trackSmoothing;
    @JsonProperty("trackSmoothingAccuracy")
    private int                  trackSmoothingAccuracy;
    @JsonProperty("trackCompression")
    private boolean              trackCompression;
    @JsonProperty("trackCompressionMaxError")
    private double               trackCompressionMaxError;
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

    public String getGpxFileDownloadPath()
    {
        return gpxFileDownloadPath;
    }

    public void setGpxFileDownloadPath(String gpxFileDownloadPath)
    {
        this.gpxFileDownloadPath = gpxFileDownloadPath;
    }

    public String getGpxFileUploadPath()
    {
        return gpxFileUploadPath;
    }

    public void setGpxFileUploadPath(String gpxFileUploadPath)
    {
        this.gpxFileUploadPath = gpxFileUploadPath;
    }

    public List<SettingsDevice> getDevices()
    {
        return devices;
    }

    public void setDevices(List<SettingsDevice> devices)
    {
        this.devices = devices;
    }

    public boolean isTrackSmoothing()
    {
        return trackSmoothing;
    }

    public void setTrackSmoothing(boolean trackSmoothing)
    {
        this.trackSmoothing = trackSmoothing;
    }

    public int getTrackSmoothingAccuracy()
    {
        return trackSmoothingAccuracy;
    }

    public void setTrackSmoothingAccuracy(int trackSmoothingAccuracy)
    {
        this.trackSmoothingAccuracy = trackSmoothingAccuracy;
    }

    public boolean isTrackCompression()
    {
        return trackCompression;
    }

    public void setTrackCompression(boolean trackCompression)
    {
        this.trackCompression = trackCompression;
    }

    public double getTrackCompressionMaxError()
    {
        return trackCompressionMaxError;
    }

    public void setTrackCompressionMaxError(double trackCompressionMaxError)
    {
        this.trackCompressionMaxError = trackCompressionMaxError;
    }
    
   
}
