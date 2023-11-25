/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author UserXP
 */
public class GpxReader
{
    private final static Logger         LOGGER = LogManager.getLogger(GpxReader.class);

    private static GpxReader theInstance=null;

    /**
     * Private constructor
     */
    private GpxReader()
    {
    }

    /**
     * This method returns the one and only instance of this singleton class
     * @return The instance
     */
    public static GpxReader getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new GpxReader();
        }
        return theInstance;
    }

    /**
     * This method returns an element
     * @param parent Parent from which the element is obtained
     * @param elementName Name of the element
     * @return The element or null if not found
     */
    private Element getChildElement(Element parent, String elementName)
    {
        Element     element;
        NodeList    nodeList;

        element=null;
        nodeList=parent.getElementsByTagName(elementName);

        if (nodeList.getLength()==1)
        {
            if (nodeList.item(0).getNodeType()==Node.ELEMENT_NODE)
            {
                element=(Element)nodeList.item(0);
            }
        }
        else
        {
            // We expect 0 or 1 elements in th list
            if (nodeList.getLength()>1)
            {
                LOGGER.error("Number of nodes <{}> tags in GPX file not equal to one as expected...", elementName);
            }
        }
        return element;
    }

    /**
     * This method returns the value of a child element
     * @param parent The parent element
     * @param tagName The name of the child element
     * @return The value of the child element
     */
    private String getChildElementValue(Element parent, String tagName)
    {
        Element  element;
        String   value;
        NodeList list;
        Node     node;

        element=this.getChildElement(parent, tagName);

        if (element!=null)
        {
            list=element.getChildNodes();
            node=list.item(0);
            if (node!=null)
            {
                value=node.getNodeValue();
            }
            else
            {
                value="";
            }
        }
        else
        {
            value="";
            LOGGER.error("Tag <"+tagName+"> not found in GPX file");
        }
        return value;
    }

    
    
    private void parseTrack(Track track, Element trackElement)
    {
        NodeList                segmentList;
        NodeList                nList;
        Element                 eElement;
        Node                    nNode;
        int                     i;        
        int                     j;
        double                  longitude;
        double                  latitude;
        TrackSegment            segment;
        TrackPoint              point;
        
        // If succeeded, get the segments from the track
        
        segmentList = trackElement.getElementsByTagName("trkseg");

        if ((segmentList!=null) && (segmentList.getLength()>0))
        {
            // Parse trkseg segments
            j=0;
            while (j<segmentList.getLength())
            {
                // If succeeded, get the points from the route
                nList = segmentList.item(j).getChildNodes();

                if ((nList!=null) && (nList.getLength()>0))
                {
                    segment=track.appendTrackSegment();
                    i=0;
                    while (i<nList.getLength())
                    {
                       nNode = nList.item(i);
                       if ((nNode.getNodeType() == Node.ELEMENT_NODE) &&
                           (nNode.getNodeName() == "trkpt"))    
                       {
                          eElement = (Element) nNode;

                          longitude =Double.parseDouble(eElement.getAttribute("lon"));
                          latitude  =Double.parseDouble(eElement.getAttribute("lat"));
                          point=new TrackPoint(latitude, longitude);
                          segment.addTrackPoint(point);
                       }
                       i++;
                    }
                }
                else
                {
                    LOGGER.error("No <trkpt> data in {}th <trkseg> in GPX file", (j+1));
                }        
                j++;
            }
        }
        else
        {
            LOGGER.error("No <rte> data in GPX file");
        }        
        
    }
    
    private void parseRoute(Track track, Element routeElement)
    {
        NodeList                nList;
        Element                 eElement;
        Node                    nNode;
        int                     i;        
        double                  longitude;
        double                  latitude;
        TrackSegment            segment;
        TrackPoint              point;
        
        // If succeeded, get the points from the route
        nList = routeElement.getElementsByTagName("rtept");
        
        if ((nList!=null) && (nList.getLength()>0))
        {
            segment=track.appendTrackSegment();
            i=0;
            while (i<nList.getLength())
            {
               nNode = nList.item(i);
               if (nNode.getNodeType() == Node.ELEMENT_NODE)
               {
                  eElement = (Element) nNode;

                  longitude =Double.parseDouble(eElement.getAttribute("lon"));
                  latitude  =Double.parseDouble(eElement.getAttribute("lat"));


                  point=new TrackPoint(latitude, longitude);
                  segment.addTrackPoint(point);
               }
               i++;
            }
        }
        else
        {
            LOGGER.error("No <rte> data in GPX file");
        }        
    }
    
    
    private void parseWaypoints(Track route, NodeList waypoints)
    {
        List<Location>  points;
        Location        point;
        String          name="";
        String          description="";
        String          symbol="";
        double          lat;
        double          lon;
        double          elevation=0.0;
        ZonedDateTime   time=null;
        Element         element;
        Element         waypoint;
        
        points=route.getWaypoints();
        for (int i=0; i<waypoints.getLength(); i++)
        {
            waypoint=(Element)waypoints.item(i);
            element=getChildElement(waypoint, "name");
            if (element!=null)
            {
                name=element.getTextContent();
            }
            element=getChildElement(waypoint, "desc");
            if (element!=null)
            {
                description=element.getTextContent();
            }
            element=getChildElement(waypoint, "sym");
            if (element!=null)
            {
                symbol=element.getTextContent();
            }
            element=getChildElement(waypoint, "ele");
            if (element!=null)
            {
                elevation=Double.parseDouble(element.getTextContent());
            }
            element=getChildElement(waypoint, "time");
            if (element!=null)
            {
                time=ZonedDateTime.parse(element.getTextContent(), DateTimeFormatter.ISO_DATE_TIME);
            }
            lat=Double.parseDouble(waypoint.getAttribute("lat"));
            lon=Double.parseDouble(waypoint.getAttribute("lon"));
            point=new Location(name, description, time, lat, lon, elevation, 0);
            points.add(point);
        }
    }
    
    /**
     * This method reads a route consisting of waypoints from file
     * @param fileName Name of the file to read from
     * @return The route read or null if an error occurred
     */
    public Track readRouteFromFile(String fileName)
    {
        File                    fXmlFile;
        DocumentBuilderFactory  dbFactory;
        DocumentBuilder         dBuilder;
        Document                doc;
        Element                 gpxElement;
        Element                 element;
        Track                   route;

        route=new Track(0.0, 0);
        try
        {
            fXmlFile = new File(fileName);
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            gpxElement=doc.getDocumentElement();
            if (gpxElement.getNodeName().equals("gpx"))
            {
                // First try to to load points from a <rte>
                element=this.getChildElement(gpxElement, "rte");
                
                if (element!=null)
                {
                    this.parseRoute(route, element);
                }
                else
                {
                    element=this.getChildElement(gpxElement, "trk");
                    if (element!=null)
                    {
                        this.parseTrack(route, element);
                    }
                    else
                    {
                        LOGGER.error("No <trk> or <rte> in GPX file");
                    }
                }
                NodeList nodes=gpxElement.getElementsByTagName("wpt");
                if (nodes!=null)
                {
                    parseWaypoints(route, nodes);
                }
                
            }
            else
            {
                route=null;
                LOGGER.error("{} does not seem to be a GPX file", fileName);
            }
        }
        catch (Exception e)
        {
            route=null;
            LOGGER.error("Error parsing gpx file: {}", e.getMessage());
        }
        return route;
    }
}
