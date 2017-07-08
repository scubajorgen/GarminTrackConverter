/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;


import hirondelle.date4j.DateTime;
import net.studioblueplanet.logger.DebugLogger;

/**
 *
 * @author Jorgen
 */
public class Waypoint
{
    private DateTime 	dateTime;
    private double	latitude;
    private double	longitude;
    private double 	elevation;
    private String 	name;
    private String 	description;
    private int    	symbol;    
    
    public Waypoint(String name, String description, DateTime dateTime, double lat, double lon, double ele, int symbol)
    {
        this.latitude  		=lat;
        this.longitude         =lon;
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
        
    public DateTime getDateTime()
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
