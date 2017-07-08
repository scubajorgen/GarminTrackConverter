/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import hirondelle.date4j.DateTime;

import net.studioblueplanet.logger.DebugLogger;

/**
 * This class writes tracks and waypoints to GPX file.
 * The major difference between GPX 1.0 and GPX 1.1 is the <extensions> element.
 * This element is available in GPX 1.1 and not in GPX 1.0.
 * Minor difference is that <speed> and <course> no longer exist in GPX 1.1.
 * Therefore, tracks writen in GPX 1.1 use the extensions element. Private
 * fields are used in the <extensions> element in the u-gotMe namespace
 * @author Jorgen
 */
public class GpxWriter
{
    private static GpxWriter    theInstance=null;


    private int                 trackPoints;
    private int                 wayPoints;
    private String              gpxVersion;


    Document                    doc;
    Element                     gpxElement;


    /**
     * Constructor
     */
    private GpxWriter()
    {
        gpxVersion      =new String("1.1");
    }

    /**
     * This method returns the one and only instance of this singleton class
     * @return The instance
     */
    public static GpxWriter getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new GpxWriter();
        }

        return theInstance;
    }

    /**
     * This method sets the GPX version
     * @param newVersion The new version "1.0" and "1.1" allowed.
     */
    public void setGpxVersion(String newVersion)
    {
        if(newVersion.equals("1.0") || newVersion.equals("1.1"))
        {
            this.gpxVersion=newVersion;
        }
        else
        {
            DebugLogger.error("Illegal GPX version "+newVersion+
                              ". Version left to "+gpxVersion);
        }
    }


    /**
     * This method creates the XML document, adds the GPX headers and
     * creates the <gpx> element. The variables doc and gpxElement will
     * be global variables in this class.
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    private void createGpxDocument(String deviceType) throws ParserConfigurationException
    {
        String      creator;


        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // The document
        doc = docBuilder.newDocument();


        // gpx elements
        gpxElement = doc.createElement("gpx");
        doc.appendChild(gpxElement);


        if (gpxVersion.equals("1.0"))
        {
            this.addGpx1_0Header(doc, gpxElement, deviceType);
        }
        else if (gpxVersion.equals("1.1"))
        {
            this.addGpx1_1Header(doc, gpxElement, deviceType);
        }
    }

    /**
     * Add the GPX 1.0 header
     * @param doc The XML document
     * @param gpxElement The GPX element
     */
    private void addGpx1_0Header(Document doc, Element gpxElement, String creator)
    {
        Attr        attr;

        // GPX version 1.0

        attr = doc.createAttribute("creator");
        attr.setValue(creator);
        gpxElement.setAttributeNode(attr);

        attr = doc.createAttribute("version");
        attr.setValue("1.0");
        gpxElement.setAttributeNode(attr);

        // GPX namespace
        attr = doc.createAttribute("xmlns");
        attr.setValue("http://www.topografix.com/GPX/1/0");
        gpxElement.setAttributeNode(attr);

        // XMLSchema namespace
        attr = doc.createAttribute("xmlns:xsi");
        attr.setValue("http://www.w3.org/2001/XMLSchema-instance");
        gpxElement.setAttributeNode(attr);

         // Schema locations - just the GPX location
        attr = doc.createAttribute("xsi:schemaLocation");
        attr.setValue("http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd ");
        gpxElement.setAttributeNode(attr);

    }

    /**
     * Add the GPX 1.0 header
     * @param doc The XML document
     * @param gpxElement The GPX element
     */
    private void addGpx1_1Header(Document doc, Element gpxElement, String creator)
    {
        Comment     comment;
        Attr        attr;

        // GPX version 1.1
        attr = doc.createAttribute("creator");
        attr.setValue(creator);
        gpxElement.setAttributeNode(attr);

        attr = doc.createAttribute("version");
        attr.setValue("1.1");
        gpxElement.setAttributeNode(attr);

        // XMLSchema namespace
        attr = doc.createAttribute("xmlns:xsi");
        attr.setValue("http://www.w3.org/2001/XMLSchema-instance");
        gpxElement.setAttributeNode(attr);

        // GPX namespace
        attr = doc.createAttribute("xmlns");
        attr.setValue("http://www.topografix.com/GPX/1/1");
        gpxElement.setAttributeNode(attr);

        // u-gotMe namespace
        attr = doc.createAttribute("xmlns:u-gotMe");
        attr.setValue("http://u-gotme.deepocean.net");
        gpxElement.setAttributeNode(attr);

        // Schema locations
        attr = doc.createAttribute("xsi:schemaLocation");
        attr.setValue("http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd "+
                      "http://u-gotme.deepocean.net http://www.deepocean.net/u-gotme/u-gotme.xsd");
        gpxElement.setAttributeNode(attr);

    }

    /**
     * This method writes the GPX file
     * @param fileName Name of the file
     * @throws javax.xml.transform.TransformerException
     */
    void writeGpxDocument(String fileName) throws TransformerException
    {
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");


            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(fileName));


            transformer.transform(source, result);
    }

    /**
     * Appends the track points to the segment under GPX 1.0. GPX 1.0 supports
     * the elements 'course' and 'speed'. Under 'hdop' the ehpe value is
     * stored. In fact this is not correct, since ehpe is not hdop.
     * @param doc
     * @param segmentElement Document element representing the track segment
     * @param trackNo The track
     * @param segmentNo The segment
     */
    private void appendTrackGpx1_0(Document doc, Element segmentElement, Track track, int segmentNo)
    {
        ArrayList <TrackPoint>      points;
        Iterator<TrackPoint>        iterator;
        TrackPoint                  point;
        Element                     pointElement;
        Element                     element;
        Attr                        attr;
        DateTime                    dateTime;
        String                      dateTimeString;


        points=track.getTrackPoints(segmentNo);

        iterator=points.iterator();

        while (iterator.hasNext())
        {
            point           =iterator.next();
            pointElement    = doc.createElement("trkpt");
            segmentElement.appendChild(pointElement);

            element    = doc.createElement("ele");
            element.appendChild(doc.createTextNode(String.valueOf(point.getElevation())));
            pointElement.appendChild(element);

            element    = doc.createElement("time");
            dateTime=point.getDateTime();
            dateTimeString=dateTime.format("YYYY-MM-DD")+"T"+
                           dateTime.format("hh:mm:ss")+"Z";
            element.appendChild(doc.createTextNode(dateTimeString));
            pointElement.appendChild(element);
           

            // Extensions: speed
            element    = doc.createElement("speed");
            element.appendChild(doc.createTextNode(String.valueOf(point.getSpeed())));
            pointElement.appendChild(element);

            // TO DO ADD DISTANCE

            // set attribute 'lat' to element
            attr = doc.createAttribute("lat");
            attr.setValue(String.valueOf(point.getLatitude()));
            pointElement.setAttributeNode(attr);

            // set attribute 'lon' to element
            attr = doc.createAttribute("lon");
            attr.setValue(String.valueOf(point.getLongitude()));
            pointElement.setAttributeNode(attr);

            trackPoints++;

        }
    }

    /**
     * Appends the track points to the segment under GPX 1.1. GPX 1.1
     * does not support the elements 'course' and 'speed'.
     * The elements 'course', 'speed' and 'ehpe' are stored under the
     * 'extensions' element.
     * @param doc
     * @param segmentElement Document element representing the track segment
     * @param trackNo The track
     * @param segmentNo The segment
     */
    private void appendTrackGpx1_1(Document doc, Element segmentElement, Track track, int segmentNo)
    {
        ArrayList <TrackPoint>   points;
        Iterator<TrackPoint>     iterator;
        TrackPoint               point;
        Element                     pointElement;
        Element                     element;
        Element                     extensionsElement;
        Attr                        attr;
        DateTime                    dateTime;
        String                      dateTimeString;


        points=track.getTrackPoints(segmentNo);

        iterator=points.iterator();

        while (iterator.hasNext())
        {
            point           =iterator.next();
            pointElement    = doc.createElement("trkpt");
            segmentElement.appendChild(pointElement);

            // The elevation. For Pro versions it is the height sensor value. 
            // For non-Pro versions it is measured by GPS.
            element    = doc.createElement("ele");
            element.appendChild(doc.createTextNode(String.valueOf(point.getElevation())));
            pointElement.appendChild(element);

            element    = doc.createElement("time");
            dateTime=point.getDateTime();
            dateTimeString=dateTime.format("YYYY-MM-DD")+"T"+
                           dateTime.format("hh:mm:ss")+"Z";
            element.appendChild(doc.createTextNode(dateTimeString));
            pointElement.appendChild(element);

            extensionsElement    = doc.createElement("extensions");
            pointElement.appendChild(extensionsElement);

            // Extensions: speed
            element    = doc.createElement("u-gotMe:speed");
            element.appendChild(doc.createTextNode(String.valueOf(point.getSpeed())));
            extensionsElement.appendChild(element);

            // Extensions: speed
            element    = doc.createElement("u-gotMe:temp");
            element.appendChild(doc.createTextNode(String.valueOf(point.getTemperature())));
            extensionsElement.appendChild(element);



            
            
            // set attribute 'lat' to element
            attr = doc.createAttribute("lat");
            attr.setValue(String.valueOf(point.getLatitude()));
            pointElement.setAttributeNode(attr);

            // set attribute 'lon' to element
            attr = doc.createAttribute("lon");
            attr.setValue(String.valueOf(point.getLongitude()));
            pointElement.setAttributeNode(attr);

            trackPoints++;
        }
    }



    /**
     * Appends the waypoints belonging to the given track segment.
     * Assume GPX 1.0 or 1.1
     * @param doc The XML document
     * @param trackElement The element representing the track
     * @param trackNo The track identification
     * @param segmentNo The segment identification
     */
    private void appendWaypointsGpx(Document doc, Element trackElement, Track track)
    {
        ArrayList <Waypoint>        points;
        Iterator<Waypoint>          iterator;
        Waypoint                    point;
        Element                     pointElement;
        Element                     element;
        Element                     extensionElement;
        Attr                        attr;
        DateTime                    dateTime;
        String                      dateTimeString;


        // Retrieve the list of waypoints
        points=track.getWayPoints();

        // Parse the points
        iterator=points.iterator();

        while (iterator.hasNext())
        {
            point           =iterator.next();
            pointElement    = doc.createElement("wpt");
            trackElement.appendChild(pointElement);

            element    = doc.createElement("time");
            dateTime=point.getDateTime();
            dateTimeString=dateTime.format("YYYY-MM-DD")+"T"+
                           dateTime.format("hh:mm:ss")+"Z";
            element.appendChild(doc.createTextNode(dateTimeString));
            pointElement.appendChild(element);
            
            element    = doc.createElement("name");
            element.appendChild(doc.createTextNode(point.getName()));
            pointElement.appendChild(element);
           
            element    = doc.createElement("desc");
            element.appendChild(doc.createTextNode(point.getDescription()));
            pointElement.appendChild(element);
           
            element    = doc.createElement("sym");
            element.appendChild(doc.createTextNode("Waypoint"));
            pointElement.appendChild(element);
           
            // set attribute 'lat' to element
            attr = doc.createAttribute("lat");
            attr.setValue(String.valueOf(point.getLatitude()));
            pointElement.setAttributeNode(attr);

            // set attribute 'lon' to element
            attr = doc.createAttribute("lon");
            attr.setValue(String.valueOf(point.getLongitude()));
            pointElement.setAttributeNode(attr);

            element    = doc.createElement("ele");
            element.appendChild(doc.createTextNode(String.valueOf(point.getElevation())));
            pointElement.appendChild(element);


            wayPoints++;
        }

    }



    /**
     * This method adds the track segments to the track.
     * @param doc XML document
     * @param gpxElement The GPX element
     * @param trackNo The track identification
     * @param trackName The track name
     */
    private void addTrack(Document doc, Element gpxElement, Track track, String trackName)
    {
        int     i;
        int     numberOfSegments;
        Element trackElement;
        Element element;
        String  description;

        numberOfSegments=track.getNumberOfSegments();

        if (gpxVersion.equals("1.0"))
        {
            appendWaypointsGpx(doc, gpxElement, track);
        }
        else if (gpxVersion.equals("1.1"))
        {
            appendWaypointsGpx(doc, gpxElement, track);
        }

        // The track element
        trackElement = doc.createElement("trk");
        gpxElement.appendChild(trackElement);

        element = doc.createElement("name");
        element.appendChild(doc.createTextNode(trackName));
        trackElement.appendChild(element);

        description=track.getDeviceName() +" logged track";
        element = doc.createElement("desc");
        element.appendChild(doc.createTextNode(description));
        trackElement.appendChild(element);        
        
        // Add the track segments.
        i=0;
        while (i<numberOfSegments)
        {
            // segment
            Element segmentElement = doc.createElement("trkseg");
            trackElement.appendChild(segmentElement);

            if (gpxVersion.equals("1.0"))
            {
                appendTrackGpx1_0(doc, segmentElement, track, i);
            }
            else if (gpxVersion.equals("1.1"))
            {
                appendTrackGpx1_1(doc, segmentElement, track, i);
            }

            
            i++;
        }
    }







    /* ************************************************************************\
     * The interface functions
     * ************************************************************************/
    /**
     * Write the track to a GPX file
     * @param fileName Name of the file to write to
     */
    public void writeTrackToFile(String fileName, Track track, String trackName)
    {
        Element     trackElement;
        Element     element;
        Comment     comment;
        Attr        attr;
        String      creator;


        wayPoints=0;
        trackPoints=0;

        try
        {
            // create the GPX file
            createGpxDocument(track.getDeviceName());


            addTrack(doc, gpxElement, track, trackName);

            // write the content into xml file
            writeGpxDocument(fileName);

            DebugLogger.info("GpxWriter says: 'File saved to " + fileName + "!'");
            DebugLogger.info("Track: "+trackName+", track points: "+trackPoints+
                             ", wayPoints: "+wayPoints);

        }
        catch (ParserConfigurationException pce)
        {
            pce.printStackTrace();
        }
        catch (TransformerException tfe)
        {
            tfe.printStackTrace();
        }

    }

}
 