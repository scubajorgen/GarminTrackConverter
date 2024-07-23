/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.settings;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author jorgen
 */
public class ApplicationSettings
{
    private final static Logger         LOGGER = LogManager.getLogger(ApplicationSettings.class);
    private static final String         DEFAULTSETTINGSFILE="garmintrackconverter.json";
    private static ApplicationSettings  theInstance;

    private static String               settingsFile=DEFAULTSETTINGSFILE;
    private Settings                    settings;

    /**
     * Private constructor
     */
    private ApplicationSettings()
    {
        rereadSettings();
    }
    
    /**
     * Set the settings file. By default it is "garmintrackconverter.json". This
     * method can be used to use another setting file. It should be called
     * prior to the first getInstance() or a rereadSettings() must be called
     * afterwards
     * @param filename Filename to use 
     */
    public static void setSettingsFile(String filename)
    {
        settingsFile=filename;
    }
    
    /**
     * Re-read the settings file.
     */
    public void rereadSettings()
    {
        File         configFile;
        ObjectMapper mapper = new ObjectMapper();
        
        configFile=new File(settingsFile);
        try
        {
            settings=mapper.readValue(configFile, Settings.class);
        }
        catch (IOException e)
        {
            LOGGER.error("Error reading configuration {}: {}", configFile.getAbsolutePath(), e.getMessage());
        }        
    }
    
    /**
     * Returns the one and only instance of this class; singleton pattern.
     * @return The instance
     */
    public static ApplicationSettings getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new ApplicationSettings();
        }
        return theInstance;
    }
    
    /**
     * Return list of configured devices
     * @return The list of configured devices
     */
    public List<SettingsDevice> getDevices()
    {
        return settings.getDevices();
    }
    
    /**
     * Return the configured GPX file upload path, i.e. the directory
     * that is selected when uploading a new GPX file to the device
     * @return The GPX file path
     */
    public String getGpxFileUploadPath()
    {
        return settings.getGpxFileUploadPath();
    }
    
    /**
     * Return the configured GPX file download path, i.e. the directory
     * that is selected when downloading a fit file as GPX from the device
     * @return The GPX file path
     */
    public String getGpxFileDownloadPath()
    {
        return settings.getGpxFileDownloadPath();
    }
    
    /**
     * Returns which extensions to use: 'garmin', 'studioblueplanet' or 'none'
     * @return The extensions type
     */
    public String getGpxExtensions()
    {
        return settings.getGpxFileExtensions().toLowerCase();
    }
    
    /**
     * Get the required debug level
     * @return The level
     */
    public String getDebugLevel()
    {
        return settings.getDebugLevel();
    }
    
    /**
     * Indicates the default setting for track smoothing checkbox
     * @return True if enabled
     */
    public boolean isTrackSmoothing()
    {
        return settings.isTrackSmoothing();
    }
    
    /**
     * Returns the default GPS accuracy to be used if the GPS does not provide
     * it. 
     * @return The accuracy in m 
     */
    public double getTrackSmoothingAccuracy()
    {
        return settings.getTrackSmoothingAccuracy();
    }
    
    /**
     * Indicates the default setting for track smoothing checkbox
     * @return True if enabled
     */
    public boolean isTrackCompression()
    {
        return settings.isTrackCompression();
    }
    
    /**
     * Returns the maximum allowed error when compressing the track
     * @return The maximum error in m
     */
    public double getTrackCompressionMaxError()
    {
        return settings.getTrackCompressionMaxError();
    }
    
    /**
     * Returns if the application should display the synced files of the first 
     * Device in the list that is of type 'USBDevice'.
     * @return 
     */
    public boolean isShowSyncWhenNoDeviceAttached()
    {
        return settings.isShowSyncWhenNoDeviceAttached();
    }

    public int getUsbConnectionStartVendorId()
    {
        return settings.getUsbConnectionStartVendorId();
    }

    public int getUsbConnectionStartProductId()
    {
        return settings.getUsbConnectionStartProductId();
    }

    public boolean isSimulateUsb()
    {
        return settings.isSimulateUsb();
    }
}
