/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;


import hirondelle.date4j.DateTime;
import java.time.Month;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TimeZone;

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
                name        =record.getStringValue(i, "name");
                dateTime    =extractDateTime(record, i, name);
                description =record.getStringValue(i, "description");
                lat         =record.getLatLonValue(i, "position_lat");
                lon         =record.getLatLonValue(i, "position_long");
                ele         =record.getScaledValue(i, "altitude");
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
     * Extracts the datetime. In the Fenix the date time is not filled in in the
     * timestamp field. However, it is by default used as name, e.g. "Apr 09 9:23".
     * If the timestamp is not found, a try is made to extract it from the name
     * as second best.
     * @param record The FitMessage to use
     * @param index Index of the data record
     * @param name Name of the Location
     * @return 
     */
    private DateTime extractDateTime(FitMessage record, int index, String name)
    {
        DateTime    dateTime;
        Pattern     pattern;
        Matcher     matcher;
        long        dateTimeLong;

        dateTimeLong=record.getIntValue(index, "timestamp");
        LOGGER.info("Timestamp "+dateTimeLong+" "+name);
        if (dateTimeLong==0xFFFFFFFFL)
        {
            pattern=Pattern.compile("^([A-Z][a-z]{2}) (\\d{2}) (\\d{2}):(\\d{2})$");
            matcher=pattern.matcher(name);
            if (matcher.find())
            {
                // TO DO: construct datetime
                /*
                int day     =Integer.parseInt(matcher.group(2));
                int month   =Month.valueOf(matcher.group(1).toUpperCase()).getValue();
                int hour    =Integer.parseInt(matcher.group(3));
                int minute  =Integer.parseInt(matcher.group(4));
                DateTime now=DateTime.now(TimeZone.getDefault());
                
                // Best guess for the year
                int year    =now.getYear();
                if (now.getMonth()<month)
                {
                    year++;
                }
                dateTime=new DateTime(year, month, day, hour, minute, 0, 0);
                LOGGER.info("Datetime {}", dateTime.format("YYYY-MM-DD hh:mm:s"));
                */
            }
            
            // TO DO Remove
            dateTime    =record.getTimeValue(index, "timestamp");
        }
        else
        {
            dateTime    =record.getTimeValue(index, "timestamp");
        }        
        return dateTime;
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
    
    public void dumpWaypoints()
    {
        for (Location loc: locations)
        {
            LOGGER.info("Waypoint: {} - {} ({}, {})", loc.getDateTime().format("YYYY-MM-DD hh:mm:ss"), loc.getName(), loc.getLatitude(), loc.getLongitude());
        }
    }
}
