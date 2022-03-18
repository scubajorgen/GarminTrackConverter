/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;


import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.List;

import net.studioblueplanet.fitreader.FitReader;
import net.studioblueplanet.fitreader.FitMessageRepository;
import net.studioblueplanet.fitreader.FitMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This file represents the list of locations
 * @author Jorgen
 */
public class Locations
{
    private final static Logger         LOGGER = LogManager.getLogger(Locations.class);
    private final List<Location>        locations;
    
    
    public Locations(String waypointFileName)
    {
        FitReader               reader;
        FitMessageRepository    repository;
        FitMessage              record;
        double                  lat;
        double                  lon;
        double                  ele;
        DateTime                dateTime;
        String                  dateTimeString;
        int                     symbol;
        String                  name;
        String                  description;
        int                     i;
        int                     size;

        locations=new ArrayList<>();
        reader=FitReader.getInstance();
        repository=reader.readFile(waypointFileName);
        record=repository.getFitMessage("location");
        if (record!=null)
        {
            size=record.getNumberOfRecords();
            i=0;
            while (i<size)
            {
                dateTime    =record.getTimeValue(i, "timestamp");
                name        =record.getStringValue(i, "name");
                description =record.getStringValue(i, "description");
                lat         =record.getLatLonValue(i, "position_lat");
                lon         =record.getLatLonValue(i, "position_long");
                ele         =record.getAltitudeValue(i, "altitude");
                symbol      =(int)record.getIntValue(i, "symbol");
                this.locations.add(new Location(name, description, dateTime, lat, lon, ele, symbol));


                if (dateTime!=null)
                {
                    dateTimeString=dateTime.toString();
                }
                else
                {
                    dateTimeString="";
                }
                LOGGER.debug("Waypoint {}: {} ({}) {} ({},{}) symbol {} alt {}", 
                             record.getIntValue(i, "message_index"), name, description, dateTimeString, lat, lon, symbol, ele
                                 );
                i++;
            }        
        }        
    }
    
    /**
     * This method returns the number of waypoints
     * @return The number of waypoints
     */
    public int getNumberOfWaypoints()
    {
        return locations.size();
    }    
    
    public List<Location> getWaypoints()
    {
        return this.locations;
    }
}
