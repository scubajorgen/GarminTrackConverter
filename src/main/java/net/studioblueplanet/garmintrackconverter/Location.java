/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import java.time.ZonedDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jorgen
 */
public class Location
{
    private final static Logger LOGGER = LogManager.getLogger(Location.class);
    private final ZonedDateTime dateTime;
    private final double	latitude;
    private final double	longitude;
    private final double 	elevation;
    private final String 	name;
    private final String 	description;
    private final int    	symbol;    
    
    public Location(String name, String description, ZonedDateTime dateTime, double lat, double lon, double ele, int symbol)
    {
        this.latitude  		=lat;
        this.longitude          =lon;
        this.elevation          =ele;
        this.name               =name;
        this.description        =description;
        this.dateTime           =dateTime;
        this.symbol             =symbol;
        
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getDescription()
    {
        return description;
    }
        
    public ZonedDateTime getDateTime()
    {
        return dateTime;
    }
    
    public double getLatitude()
    {
        return latitude;
    }
    
    public double getLongitude()
    {
        return longitude;
    }

    public double getElevation()
    {
        return elevation;
    }
    
    public int getSymbol()
    {
        return symbol;
    }

}
