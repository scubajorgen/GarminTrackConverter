/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;


import hirondelle.date4j.DateTime;
import net.studioblueplanet.logger.DebugLogger;

import java.util.ArrayList;
import java.util.Iterator;

import net.studioblueplanet.fitreader.FitReader;
import net.studioblueplanet.fitreader.FitRecordRepository;
import net.studioblueplanet.fitreader.FitRecord;

/**
 *
 * @author Jorgen
 */
public class Waypoints
{
    private ArrayList<Waypoint> waypoints;
    
    
    public Waypoints(String waypointFileName)
    {
        FitReader               reader;
        FitRecordRepository     repository;
        FitRecord               record;
        double                  lat;
        double                  lon;
        double                  ele;
        DateTime                dateTime;
        int                     symbol;
        String                  name;
        String                  description;
        int                     i;
        int                     size;

        waypoints=new ArrayList<Waypoint>();
        reader=FitReader.getInstance();
        repository=reader.readFile(waypointFileName);
        record=repository.getFitRecord("waypoints");
        if (record!=null)
        {
            size=record.getNumberOfRecordValues();
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


                DebugLogger.debug("Waypoint "+record.getIntValue(i, "message_index")+
                                 ": "+name +"("+description+") "+
                                 dateTime.toString()+
                                 "("+lat+","+lon+")"+
                                 " symbol "+symbol+
                                 " alt "+ele
                                 );
                i++;
            }        
        }        
        
    }
    
    /**
     * This method returns the number of waypoints
     * @return 
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
