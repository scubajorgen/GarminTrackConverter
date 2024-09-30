/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final Track                 waypoints;
    
    /**
     * Reads the global waypoints file into the Track waypoints.
     * @param waypointFileName File to load the waypoints from
     */
    public Locations(String waypointFileName)
    {
        FitReader               reader;
        FitMessageRepository    repository;
        FitMessage              record;
        double                  lat;
        double                  lon;
        double                  ele;
        ZonedDateTime           dateTime;
        LocalDateTime           localDateTime;
        String                  dateTimeString;
        int                     symbol;
        String                  name;
        String                  description;
        int                     i;
        int                     size;

        waypoints=new Track(0.0, 0.0);
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
                
                // Get the day time. In some Garmins it is present in the 'timestamp'
                // field. The Fenix however does not fill in this field. The Fenix
                // puts the *local* time in the name of name of the waypoint.
                // Complicating factor is that we don't know the timezone...
                // So we try to derive the normal dateTime and the local dateTime
                // from the name. Of course, the normal dateTime prevails if both
                // are available
                dateTime        =getDateTime(record, i);
                localDateTime   =extractLocalDateTime(name);
                
                
                if (record.hasField("description"))
                {
                    description =record.getStringValue(i, "description");
                }
                else
                {
                    description=null;
                }
                lat         =record.getLatLonValue(i, "position_lat");
                lon         =record.getLatLonValue(i, "position_long");
                ele         =record.getScaledValue(i, "altitude");
                symbol      =(int)record.getIntValue(i, "symbol");
                waypoints.addWaypoint(new Location(name, description, localDateTime, dateTime, lat, lon, ele, symbol));


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
     * Returns the Locations as track
     * @return The waypoints as Track
     */
    public Track getLocations()
    {
        return waypoints;
    }
    
    /**
     * Returns the timestamp of the waypoint, it it is present. Otherwise null
     * is returned
     * @param record The FitMessage to use
     * @param index Index of the data record
     * @param name Name of the Location
     * @return 
     */
    private ZonedDateTime getDateTime(FitMessage record, int index)
    {
        ZonedDateTime   dateTime;
        long            dateTimeLong;

        dateTimeLong=record.getIntValue(index, "timestamp");
        if (dateTimeLong!=0xFFFFFFFFL)
        {
            dateTime    =record.getTimeValue(index, "timestamp");
        }
        else
        {
            dateTime=null;
        }             
        return dateTime;
    }
    
    
    
    
    /**
     * Extracts the local datetime of the waypoint, assuming the Fenix. 
     * In some Garmins it is filled in in the timestamp field of the waypoint. 
     * In the Fenix however the date time is not filled in in the timestamp field. 
     * However, the local date time is by default used as name, e.g. "Apr 09 9:23".
     * The issue hiere is that we do not know the timezone of this local date time.
     * If the timestamp is not found, a try is made to extract it from the name
     * as second best.
     * @param record The FitMessage to use
     * @param index Index of the data record
     * @param name Name of the Location
     * @return 
     */
    private LocalDateTime extractLocalDateTime(String name)
    {
        LocalDateTime   localDateTime;
        LocalDateTime   now;
        Pattern         pattern;
        Matcher         matcher;

        pattern=Pattern.compile("^([A-Z][a-z]{2}) (\\d{2}) (\\d{2}):{0,1}(\\d{2})$");
        matcher=pattern.matcher(name);
        if (matcher.find())
        {
            // Create dateTime from the name assuming year is current year
            // We also assume default time zone
            now=LocalDateTime.now();
            localDateTime=LocalDateTime.parse(String.valueOf(now.getYear())+" "+
                                              matcher.group(1)+" "+matcher.group(2)+" "+matcher.group(3)+":"+matcher.group(4), 
                                              DateTimeFormatter.ofPattern("yyyy MMM dd HH:mm"));

            // If it appears that now is before the dateTime, the assumption was not correct; 
            // best we can do is assume it was from previous year
            if (now.isBefore(localDateTime))
            {
                localDateTime.minusYears(1);
            }
        }
        else
        {
            localDateTime    =null;
        }
     
        return localDateTime;
    }
    
    /**
     * This method returns the number of waypoints
     * @return The number of waypoints
     */
    public int getNumberOfWaypoints()
    {
        return waypoints.getWaypoints().size();
    }    
    
    public List<Location> getWaypoints()
    {
        return waypoints.getWaypoints();
    }
    
    /**
     * Debugging: print a list of waypoints
     */
    public void dumpWaypoints()
    {
        ZonedDateTime   dateTime;
        String          dateTimeString;
        for (Location loc: waypoints.getWaypoints())
        {
            dateTime=loc.getDateTime();
            if (dateTime!=null)
            {
                dateTimeString=loc.getDateTime().toString();
            }
            else
            {
                dateTimeString="----";
            }
            LOGGER.info("Waypoint: {} - {} ({}, {})", dateTimeString, loc.getName(), loc.getLatitude(), loc.getLongitude());
        }
    }
}
