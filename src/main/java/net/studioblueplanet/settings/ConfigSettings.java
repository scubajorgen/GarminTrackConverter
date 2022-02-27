/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.settings;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author jorgen.van.der.velde
 */
public class ConfigSettings
{
    public enum SettingType
    {
        STRING, INT, DOUBLE, BOOLEAN
    }
        
    class Setting
    {

        // Name of the setting in the property file
        public String       name;               
        
        // Type of the value
        public SettingType  type;
        
        // Short description
        public String       description;       
        
        // Default value
        public String       defaultValue;       
        
        // Current value
        public String       value;              
        
        // Allowed value: INT, DOUBLE: "min,max", STRING: "value1,value2,..." BOOLEAN: no meaning; Leave empty "" for no check
        public String       allowed;            
        
        
        public Setting(String name, String description, String defaultValue, String allowed, SettingType type)
        {
            this.name           =name;
            this.description    =description;
            this.defaultValue   =defaultValue;
            this.value          =defaultValue;
            this.allowed        =allowed;
            this.type           =type;
        }
    }
    
    private final static Logger LOGGER = LogManager.getLogger(ConfigSettings.class);

    private static String               propertyFileName="garmintrackconverter.properties";

    private final Setting[]             settings=
    {
        new Setting("debugLevel"                , "DebugLogger log level            " , "error"                             , "off,debug,info,error"   , SettingType.STRING ),
        new Setting("deviceFile"                , "Location of device file          " , "//Garmin/device.xml"               , ""                       , SettingType.STRING ),
        new Setting("waypointFile"              , "Location of the location file    " , "//Garmin/Locations/Locations.fit"  , ""                       , SettingType.STRING ),
        new Setting("trackFilePath"             , "Location of the activities       " , "//Garmin/Activities"               , ""                       , SettingType.STRING ),
        new Setting("routeFilePath"             , "Location of the courses          " , "//Garmin/Courses"                  , ""                       , SettingType.STRING ),
        new Setting("newFilePath"               , "Location of the new files        " , "//Garmin/Newfiles"                 , ""                       , SettingType.STRING ),
        new Setting("locationFilePath"          , "Location of the location files   " , "//Garmin/Locations"                , ""                       , SettingType.STRING ),
        new Setting("gpxFilePath"               , "Location of gpx files            " , "//"                                , ""                       , SettingType.STRING )
    };
    
    private static ConfigSettings       theInstance=null;        
    
    /**
     * Constructor, reads the setting values
     */
    private ConfigSettings()
    {
        this.readSettings();
    }
    
    /**
     * Returns the one and only instance of this class
     * @return The instance
     */
    public static ConfigSettings getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new ConfigSettings();
        }
        return theInstance;
    }
    
    public static void setPropertiesFile(String fileName)
    {
        propertyFileName=fileName;
    }
    
    /**
     * Reads the settings from the property file. If no file found, 
     * the default values are used
     */
    private void readSettings()
    {
        Properties      properties;
        int             i;
        String          settingValue;
        boolean         error;
        Setting         setting;
        
        

        // Read properties file.
        properties = new Properties();
        try
        {
            properties.load(new FileInputStream(propertyFileName));

            // Get each setting from the array
            i=0;
            while (i<settings.length)
            {
                setting=settings[i];
                settingValue=properties.getProperty(setting.name);
                if (settingValue!=null)
                {
                    setting.value=settingValue;
                    error=validateSetting(setting);
                    if (error)
                    {
                        setting.value=setting.defaultValue;
                        LOGGER.error("Error validating setting '{}'. Using default value '{}'", setting.name, setting.value);
                    }
                }
                else
                {
                    LOGGER.error("Setting {} not found, assuming default value", setting.name);
                    setting.value=setting.defaultValue;
                    error=true;
                }
                i++;
            }

            LOGGER.info("Settings read");
            dumpSettings();
        }    
        catch (IOException e)
        {
            LOGGER.error("Error reading settings, using defaults. Error: {}");
        }
    }
    
    private boolean validateSetting(Setting setting)
    {
        boolean     error;
        int         intValue;
        int         intMin;
        int         intMax;
        double      doubleValue;
        double      doubleMin;
        double      doubleMax;
        String[]    subStrings;
        int         i;
        
        error=false;
        
        // Remove leading and trailing spaces
        setting.value=setting.value.trim();
        switch (setting.type)
        {
            case INT:
                try
                {
                    intValue    =Integer.parseInt(setting.value);
                    subStrings  =setting.allowed.split("[,]");
                    if (subStrings.length==2)
                    {
                        intMin=Integer.parseInt(subStrings[0]);
                        intMax=Integer.parseInt(subStrings[1]);
                        if ((intValue>intMax) || (intValue<intMin))
                        {
                            LOGGER.error("Setting value '{}' of setting '{}' out of bounds", intValue, setting.name);
                            error=true;
                        }
                    }
                }
                catch (Exception e)
                {
                    LOGGER.error("Illegal value '{}' of setting '{}'", setting.value, setting.name);
                    error=true;
                } 
                break;
            case DOUBLE:
                try
                {
                    doubleValue =Double.parseDouble(setting.value);
                    subStrings  =setting.allowed.split("[,]");
                    if (subStrings.length==2)
                    {
                        doubleMin=Double.parseDouble(subStrings[0]);
                        doubleMax=Double.parseDouble(subStrings[1]);
                        if ((doubleValue>doubleMax) || (doubleValue<doubleMin))
                        {
                            LOGGER.error("Setting value '{}' of setting '{}' out of bounds", doubleValue, setting.name);
                            error=true;
                        }
                    }
                }
                catch (Exception e)
                {
                    LOGGER.error("Illegal value '{}' of setting '{}'", setting.value, setting.name);
                    error=true;
                } 
                break;
            case BOOLEAN:
                if (!setting.value.equals("true") && !setting.value.equals("false") &&
                    !setting.value.equals("1") && !setting.value.equals("0") &&
                    !setting.value.equals("yes") && !setting.value.equals("no"))
                {
                    LOGGER.error("Setting value '{}' of setting '{}' not allowed (use: yes, no, true, false, 1, 0)", setting.value, setting.name);
                    error=true;                    
                }
                break;
            case STRING:
                // Check if the value equals one of the allowed values,
                // unless no allowed values defined
                if (!setting.allowed.equals(""))
                {
                    subStrings  =setting.allowed.split("[,]");
                    i=0;
                    error=true;
                    while (i<subStrings.length && error)
                    {
                        if (setting.value.equals(subStrings[i]))
                        {
                            error=false;
                        }
                        i++;
                    }
                    if (error)
                    {
                        LOGGER.error("Setting value '{}' of setting {} not allowed", setting.value, setting.name);
                    }
                }
                break;
                
        }
        
        return error;
    }
    
    public void dumpSettings()
    {
        int i;
        i=0;
        while (i<settings.length)
        {
            LOGGER.info("{}={}", settings[i].name, settings[i].value);
            i++;
        }
    }
    
    /**
     * Returns the setting value as int value
     * @param name Setting name
     * @return The integer value or -1 if the setting is not the INT type or
     *         if the setting does not exist.
     */
    public int getIntValue(String name)
    {
        int         i;
        int         value;
        boolean     found;
        
        i       =0;
        found   =false;
        value   =-1;
        while (i<settings.length)
        {
            if (settings[i].name.equals(name))
            {
                if (settings[i].type==SettingType.INT)
                {
                    value=Integer.parseInt(settings[i].value);
                }
                else
                {
                    LOGGER.error("Requesting int value of non-int setting '{}'" ,settings[i].name);
                }
                found=true;
            }
            i++;
        }
        if (!found)
        {
            LOGGER.error("Requesting value of non-existing setting '{}'", name);
        }
        return value;
    }
    
    /**
     * Returns the setting value as double value
     * @param name Setting name
     * @return The double value or -1 if the setting is not the DOUBLE type or
     *         if the setting does not exist.
     */
    public double getDoubleValue(String name)
    {
        int         i;
        double      value;
        boolean     found;
        
        i       =0;
        found   =false;
        value   =-1;
        while (i<settings.length)
        {
            if (settings[i].name.equals(name))
            {
                if (settings[i].type==SettingType.DOUBLE)
                {
                    value=Double.parseDouble(settings[i].value);
                }
                else
                {
                    LOGGER.error("Requesting double value of non-double setting '{}'", settings[i].name);
                }
                found=true;
            }
            i++;
        }
        if (!found)
        {
            LOGGER.error("Requesting value of non-existing setting '{}'", name);
        }
        return value;
    }    

    /**
     * Returns the setting value as String value
     * @param name Setting name
     * @return The string value or "" if the setting is not the STRING type or
     *         if the setting does not exist.
     */
    public String getStringValue(String name)
    {
        int         i;
        String      value;
        boolean     found;
        
        i       =0;
        found   =false;
        value   ="";
        while (i<settings.length)
        {
            if (settings[i].name.equals(name))
            {
                if (settings[i].type==SettingType.STRING)
                {
                    value=settings[i].value;
                }
                else
                {
                    LOGGER.error("Requesting string value of non-string setting '{}'", settings[i].name);
                }
                found=true;
            }
            i++;
        }
        if (!found)
        {
            LOGGER.error("Requesting value of non-existing setting ''", name);
        }
        return value;
    }
    
    /**
     * Returns the setting value as boolean value
     * @param name Setting name
     * @return The boolean value or false if the setting is not the BOOLEAN type or
     *         if the setting does not exist.
     */
    public boolean getBooleanValue(String name)
    {
        int         i;
        boolean     value;
        boolean     found;
        
        i       =0;
        found   =false;
        value   =false;
        while (i<settings.length)
        {
            if (settings[i].name.equals(name))
            {
                if (settings[i].type==SettingType.BOOLEAN)
                {
                    if ((settings[i].value.equals("true")) || 
                        (settings[i].value.equals("yes") ) ||
                        (settings[i].value.equals("1")   ))
                    {
                        value=true;
                    }
                }
                else
                {
                    LOGGER.error("Requesting boolean value of non-boolean setting '{}'", settings[i].name);
                }
                found=true;
            }
            i++;
        }
        if (!found)
        {
            LOGGER.error("Requesting value of non-existing setting '{}'", name);
        }
        return value;
    }     
    
            
}
