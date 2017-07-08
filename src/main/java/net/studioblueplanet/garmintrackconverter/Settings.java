/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.studioblueplanet.logger.DebugLogger;
/**
 *
 * @author Jorgen
 */
public class Settings
{
    private static  Settings theInstance=null;

    private String  propertyFileName="GarminTrackConverter.properties";
    
    private String  trackFilePath;
    private String  waypointFile;
    private String  deviceFile;
    private String  gpxFilePath;
    private int     debugLevel;
    
    
    private Settings()
    {
        readSettings();
    }

    private void readSettings()
    {
        Properties      properties;
        String          setting;


        // Read properties file.
        properties = new Properties();
        try
        {
            properties.load(new FileInputStream(propertyFileName));

            setting=properties.getProperty("debugLevel");
            this.setDebugLevel(setting);
            // As a side effect, set the DebugLogger accordingly, so
            // debug logging starts right away
            DebugLogger.setDebugLevel(debugLevel);


            setting=properties.getProperty("trackFilePath");
            this.setTrackFilePath(setting);

            setting=properties.getProperty("waypointFile");
            this.setWaypointFile(setting);

            setting=properties.getProperty("deviceFile");
            this.setDeviceFile(setting);

            setting=properties.getProperty("gpxFilePath");
            this.setGpxFilePath(setting);

            
            
            DebugLogger.info("Settings read");
            dumpSettings();
        }
        catch (IOException e)
        {
            DebugLogger.error("Error reading settings from "+propertyFileName);
            debugLevel              =DebugLogger.DEBUGLEVEL_ERROR;
            trackFilePath           ="";
            waypointFile            ="";
            deviceFile              ="";
            gpxFilePath             ="";
        }

    }

    /**
     * Writes current settings to the properties file
     */
    public void writeSettings()
    {
        Properties      properties;
        // Read properties file.
        properties = new Properties();
        try
        {
            properties.setProperty("debugLevel"         , DebugLogger.debugLevelToString(debugLevel));
            properties.setProperty("trackFilePath"      , trackFilePath);
            properties.setProperty("waypointfile"       , waypointFile);
            properties.setProperty("devicefile"         , deviceFile);
            properties.setProperty("gpxFilePath"        , gpxFilePath);


            properties.store(new FileOutputStream(propertyFileName), "");


            DebugLogger.info("Settings written");
            dumpSettings();

        }
        catch (IOException e)
        {
            DebugLogger.error("Error writing properties file "+propertyFileName);
        }
    }

    /**
     * Output settings to the debug output
     */
    private void dumpSettings()
    {
        DebugLogger.info("SETTINGS FROM PROPERTY FILE");
        DebugLogger.info("Setting debug level      (debugLevel)         : "+DebugLogger.debugLevelToString(debugLevel));
        DebugLogger.info("Track File Path          (trackFilePath)      : "+trackFilePath);
        DebugLogger.info("Waypoint File            (waypointFile)       : "+waypointFile);
        DebugLogger.info("Device File              (deviceFile)         : "+deviceFile);
        DebugLogger.info("GPX File Path            (gpxFilePath)        : "+gpxFilePath);

        
        
    }


    /**
     * Returns the property file name
     * @return The filename
     */
    public String getPropertyFileName()
    {
        return propertyFileName;
    }

    /**
     * Returns the one and only singleton instance of this class
     * @return The instance
     */
    public static Settings getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new Settings();
        }
        return theInstance;
    }
    
    /**
     * Gets the debugging level setting
     * @return The debug level conform the definition in DebugLogger.
     */
    public int getDebugLevel()
    {
        return debugLevel;
    }

    /**
     * Sets the debugging setting
     * @param debugLevel The debug level conform the definition in DebugLogger.
     */
    public void setDebugLevel(int debugLevel)
    {
        this.debugLevel=debugLevel;
    }    
    
    /**
     * Sets the debugging setting
     * @param debugLevel 'off', 'debug', 'info' or 'error'
     */
    public void setDebugLevel(String debugLevel)
    {
        // Default debug level: only show the errors
        this.debugLevel=DebugLogger.DEBUGLEVEL_ERROR;
        
        if (debugLevel!=null)
        {
            if (debugLevel.toLowerCase().equals("off"))
            {
                this.debugLevel=DebugLogger.DEBUGLEVEL_OFF;
            }
            else if (debugLevel.toLowerCase().equals("debug"))
            {
                this.debugLevel=DebugLogger.DEBUGLEVEL_DEBUG;
            }
            else if (debugLevel.toLowerCase().equals("info"))
            {
                this.debugLevel=DebugLogger.DEBUGLEVEL_INFO;
            }
            else if (debugLevel.toLowerCase().equals("error"))
            {
                this.debugLevel=DebugLogger.DEBUGLEVEL_ERROR;
            }
        }
    }
    
    
    /**
     * This method returns the default path where track FIT files are stored.
     * @return The path or "" if the path is not defined
     */
    public String getTrackFilePath()
    {
        return trackFilePath;
    }

    /**
     * This method sets the default path where track files are stored. 
     * If null is passed the default path will become ""
     * @param newPath The new path
     */
    public void setTrackFilePath(String newPath)
    {
        if (newPath!=null)
        {
            this.trackFilePath=newPath;
        }
        else
        {
            this.trackFilePath="";
        }
    }     

    /**
     * This method returns the waypoint file.
     * @return The file or "" if the file is not defined
     */
    public String getWaypointFile()
    {
        return waypointFile;
    }

    /**
     * This method sets the waypoint file. If null is passed
     * the default path will become ""
     * @param file The new path
     */
    public void setWaypointFile(String file)
    {
        if (file!=null)
        {
            this.waypointFile=file;
        }
        else
        {
            this.waypointFile="";
        }
    }     
    
    /**
     * This method returns the device file.
     * @return The file or "" if the file is not defined
     */
    public String getDeviceFile()
    {
        return deviceFile;
    }

    /**
     * This method sets the Device file. If null is passed
     * the default path will become ""
     * @param file The new file
     */
    public void setDeviceFile(String file)
    {
        if (file!=null)
        {
            this.deviceFile=file;
        }
        else
        {
            this.deviceFile="";
        }
    }     
    
    /**
     * This method returns the default path for saving GPX files.
     * @return The path or "" if the path is not defined
     */
    public String getGpxFilePath()
    {
        return gpxFilePath;
    }

    /**
     * This method sets the default path for saving GPX files. If null is passed
     * the default path will become ""
     * @param newPath The new path
     */
    public void setGpxFilePath(String newPath)
    {
        if (newPath!=null)
        {
            this.gpxFilePath=newPath;
        }
        else
        {
            this.gpxFilePath="";
        }
    }    
}
