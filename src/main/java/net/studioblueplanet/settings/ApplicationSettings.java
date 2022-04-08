/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.settings;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
    private static ApplicationSettings  theInstance;
    private Settings                    settings;

    /**
     * Private constructor
     */
    private ApplicationSettings()
    {
        File         configFile;
        ObjectMapper mapper = new ObjectMapper();
        
        configFile=new File("garmintrackconverter.json");
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
        return settings.devices;
    }
    
    /**
     * Return the configured GPX file path
     * @return The GPX file path
     */
    public String getGpxFilePath()
    {
        return settings.gpxFilePath;
    }
    
    /**
     * Get the required debug level
     * @return The level
     */
    public String getDebugLevel()
    {
        return settings.debugLevel;
    }
}
