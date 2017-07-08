/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import net.studioblueplanet.fitreader.FitReader;
import net.studioblueplanet.fitreader.FitRecord;
import net.studioblueplanet.fitreader.FitRecordRepository;
import hirondelle.date4j.DateTime;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

import net.studioblueplanet.logger.DebugLogger;
        

/**
 * This class represents a track consisting of track segments and optional
 * a number of logged waypoints
 * @author Jorgen
 */
public class Track
{
    private ArrayList<TrackSegment>     segments;
    private ArrayList<Waypoint>         waypoints;
    private String                      lastError;
    private String                      deviceName;
    
    public Track(String trackFileName, String deviceFileName)
    {
        FitReader               reader;
        FitRecordRepository     repository;
        FitRecord               lapRecord;
        FitRecord               trackRecord;

        
        segments        =new ArrayList<TrackSegment>();
        waypoints       =new ArrayList<Waypoint>();
        lastError       ="Ok";
        
        reader          =FitReader.getInstance();
        repository      =reader.readFile(trackFileName);
        lapRecord       =repository.getFitRecord("lap");
        trackRecord     =repository.getFitRecord("record");

        if (lapRecord!=null && trackRecord!=null)
        {
            this.parseLaps(lapRecord);
            this.parseTrackPoints(trackRecord);

            this.parseDeviceFile(deviceFileName);
        }
    
    }

    /**
     * This method parses the FIT lap record and destilates the number of laps.
     * @param lapRecord The FIT record holding the 'lap' info
     */
    private void parseLaps(FitRecord lapRecord)
    {
        int i;
        int size;
        DateTime                startTime;
        DateTime                endTime;
        double                  elapsedTime;
        TrackSegment            segment;
        
        size            =lapRecord.getNumberOfRecordValues();
        i=0;
        while (i<size)
        {
            endTime     =lapRecord.getTimeValue(i, "timestamp");
            startTime   =lapRecord.getTimeValue(i, "start_time");
            elapsedTime =lapRecord.getIntValue(i, "total_elapsed_time")/1000;
       
            segment     =new TrackSegment(startTime, endTime, elapsedTime);
            segments.add(segment);
            
            DebugLogger.debug("Lap "+lapRecord.getIntValue(i, "message_index")+
                             " "+startTime.toString()+
                             " "+endTime.toString()+
                             " "+elapsedTime+" s"
                             );
            i++;
        }          
        
    }
    
    /**
     * This method parses the FIT activity record. It destiles the track points
     * @param trackRecord The record holding the 'activity' information
     */
    private void parseTrackPoints(FitRecord trackRecord)
    {
        double                  lat;
        double                  lon;
        double                  ele;
        DateTime                dateTime;

        double                  speed;
        double                  distance;
        int                     temp;
        int                     i;
        int                     size;
        TrackPoint              point;
        Iterator<TrackSegment>  it;
        boolean                 found;
        TrackSegment            segment;


        size            =trackRecord.getNumberOfRecordValues();
        i=0;
        while (i<size)
        {
            dateTime    =trackRecord.getTimeValue(i, "timestamp");
            lat         =trackRecord.getLatLonValue(i, "position_lat");
            lon         =trackRecord.getLatLonValue(i, "position_long");
            ele         =trackRecord.getAltitudeValue(i, "altitude");
            temp        =trackRecord.getIntValue(i, "temperature");
            speed       =trackRecord.getSpeedValue(i, "speed");
            distance    =trackRecord.getDistanceValue(i, "distance");
            
            point       =new TrackPoint(dateTime, lat, lon, ele, speed, distance, temp);
            
            found=false;
            it=this.segments.iterator();
            while (it.hasNext() && !found)
            {
                segment=it.next();
                if (segment.isInLap(dateTime))
                {
                    segment.addTrackPoint(point);
                    found=true;
                }
            }
            if (!found)
            {
                DebugLogger.error("No segment found to add trackpoint to");
            }
            
            DebugLogger.debug("Track "+
                             " "+dateTime.toString()+
                             " ("+lat+","+lon+") ele "+ele+" "+speed+" km/h "+distance+" m "
                             );
            i++;
        }           
        
    }
    
    /**
     * Parse the device file for the name of the device
     * @param deviceFileName XML file containing device info
     */
    public void parseDeviceFile(String deviceFileName)
    {
        File                    xmlFile;
        Document                doc;
        DocumentBuilder         dBuilder;
        DocumentBuilderFactory  dbFactory;
        Element                 device;
        Element                 model;
        Element                 id;
        Element                 description;
        
        xmlFile=new File(deviceFileName);
	dbFactory = DocumentBuilderFactory.newInstance();
	
        try
        {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile);
            id          =(Element)doc.getElementsByTagName("Id").item(0);
            model       =(Element)doc.getElementsByTagName("Model").item(0);
            description =(Element)model.getElementsByTagName("Description").item(0);
            deviceName=description.getTextContent()+" - "+id.getTextContent();
            DebugLogger.info("Found "+deviceName);
        }
        catch(Exception e)
        {
            DebugLogger.error("Error parsing device file");
        }
               
    }
    
    public void addTrackWaypoints(ArrayList<Waypoint> allWaypoints)
    {
        DateTime                dateTime;
        Iterator<Waypoint>      waypointIterator;
        Iterator<TrackSegment>  segmentIterator;
        Waypoint                waypoint;
        TrackSegment            segment;
        boolean                 found;
        
        waypointIterator=allWaypoints.iterator();
        while (waypointIterator.hasNext())
        {
            waypoint=waypointIterator.next();
            dateTime=waypoint.getDateTime();
            segmentIterator=segments.iterator();
            found=false;
            while (segmentIterator.hasNext() && !found)
            {
                // If the date time stamp is in the range of the segment,
                // add this waypoint to the track and don't look any further
                segment=segmentIterator.next();
                if (segment.isInLap(dateTime))
                {
                    found=true;
                    waypoints.add(waypoint);
                }
            }
        }
            
    }
    
    /**
     * Return some info about this track
     * @return The info
     */
    public String getTrackInfo()
    {
        String          info;
        int             i;
        int             noOfSegments;
        TrackSegment    segment;
        
        noOfSegments=segments.size();
        
        info="Track with "+this.segments.size()+" segments (";
        i=0;
        while (i<noOfSegments)
        {
            segment=segments.get(i);
            info+=segment.getNumberOfTrackPoints();
            if (i<noOfSegments-1)
            {
                info+=", ";
            }
            i++;
        }
        info+=" points)";
        return info;
    }
    
    /**
     * Get the device name
     * @return The device name
     */
    public String getDeviceName()
    {
        return this.deviceName;
    }
    
    /**
     * Returns the number of segments in this track
     * @return The number of segments
     */
    public int getNumberOfSegments()
    {
        return segments.size();
    }
    
    /**
     * Returns the array list with track points belonging to indicated segment
     * @param segment The segment to request the track points for
     * @return The array list with track points
     */
    public ArrayList<TrackPoint> getTrackPoints(int segment)
    {
        return this.segments.get(segment).getTrackPoints();
    }
    
    public ArrayList<Waypoint> getWayPoints()
    {
        return this.waypoints;
    }
    
}
