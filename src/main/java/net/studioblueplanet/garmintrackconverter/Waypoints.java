/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;


import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.Iterator;

import net.studioblueplanet.fitreader.FitReader;
import net.studioblueplanet.fitreader.FitMessageRepository;
import net.studioblueplanet.fitreader.FitMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jorgen
 */
public class Waypoints
{
    private final static Logger         LOGGER = LogManager.getLogger(Waypoints.class);
    private ArrayList<Waypoint> waypoints;
    
    
    public Waypoints(String waypointFileName)
    {
        FitReader               reader;
        FitMessageRepository     repository;
        FitMessage               record;
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

        waypoints=new ArrayList<Waypoint>();
        reader=FitReader.getInstance();
        repository=reader.readFile(waypointFileName);
        record=repository.getFitMessage("waypoints");
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
                symbol      =record.getIntValue(i, "symbol");
                this.waypoints.add(new Waypoint(name, description, dateTime, lat, lon, ele, symbol));


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
        return waypoints.size();
    }    
    
    public ArrayList<Waypoint> getWaypoints()
    {
        return this.waypoints;
    }
    
}
