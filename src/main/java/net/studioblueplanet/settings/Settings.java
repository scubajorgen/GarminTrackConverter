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
    @JsonProperty("gpxFileExtensions")
    private String               gpxFileExtensions;
    @JsonProperty("trackSmoothing")
    private boolean              trackSmoothing;
    @JsonProperty("trackSmoothingAccuracy")
    private double               trackSmoothingAccuracy;
    @JsonProperty("trackCompression")
    private boolean              trackCompression;
    @JsonProperty("trackCompressionMaxError")
    private double               trackCompressionMaxError;
    @JsonProperty("showSyncWhenNoDeviceAttached")
    private boolean              showSyncWhenNoDeviceAttached;
    @JsonProperty("usbConnectionStartVendorId")
    private int                  usbConnectionStartVendorId;
    @JsonProperty("usbConnectionStartProductId")
    private int                  usbConnectionStartProductId;          
    @JsonProperty("simulateUsb")
    private boolean              simulateUsb;
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

    public double getTrackSmoothingAccuracy()
    {
        return trackSmoothingAccuracy;
    }

    public void setTrackSmoothingAccuracy(double trackSmoothingAccuracy)
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

    public String getGpxFileExtensions()
    {
        return gpxFileExtensions;
    }

    public void setGpxFileExtensions(String gpxFileExtensions)
    {
        this.gpxFileExtensions = gpxFileExtensions;
    }

    public boolean isShowSyncWhenNoDeviceAttached()
    {
        return showSyncWhenNoDeviceAttached;
    }

    public void setShowSyncWhenNoDeviceAttached(boolean showSyncIfNoDeviceAttached)
    {
        this.showSyncWhenNoDeviceAttached = showSyncIfNoDeviceAttached;
    }

    public int getUsbConnectionStartVendorId()
    {
        return usbConnectionStartVendorId;
    }

    public void setUsbConnectionStartVendorId(int usbConnectionStartVendorId)
    {
        this.usbConnectionStartVendorId = usbConnectionStartVendorId;
    }

    public int getUsbConnectionStartProductId()
    {
        return usbConnectionStartProductId;
    }

    public void setUsbConnectionStartProductId(int usbConnectionStartProductId)
    {
        this.usbConnectionStartProductId = usbConnectionStartProductId;
    }

    public boolean isSimulateUsb()
    {
        return simulateUsb;
    }

    public void setSimulateUsb(boolean simulateUsb)
    {
        this.simulateUsb = simulateUsb;
    }
}
