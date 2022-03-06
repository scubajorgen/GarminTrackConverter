/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import hirondelle.date4j.DateTime;
import java.io.File;
import java.util.List;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * This class writes tracks and waypoints to GPX file.
 * The major difference between GPX 1.0 and GPX 1.1 is the &lt;extensions&gt; element.
 * This element is available in GPX 1.1 and not in GPX 1.0.
 * Minor difference is that &lt;speed&gt; and &lt;course&gt; no longer exist in GPX 1.1.
 * Therefore, tracks written in GPX 1.1 use the &lt;extensions&gt; element. Private
 * fields are used in the extensions element in the u-gotMe namespace
 * @author Jorgen
 */
public class GpxWriter
{
    private final static Logger LOGGER = LogManager.getLogger(GpxWriter.class);
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
        gpxVersion      ="1.1";
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
            LOGGER.error("Illegal GPX version {}. Version left to {}", newVersion, gpxVersion);
        }
    }
    
    public String getGpxVersion()
    {
        return gpxVersion;
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
     * Adds a string value to the XML Element
     * @param document Element to add to
     * @param string String value
     */
    private void addString(Element document, String tag, String string)
    {
        Element element;
        
        if (string!=null && !string.equals(""))
        {
            element    = doc.createElement(tag);
            element.appendChild(doc.createTextNode(string));
            document.appendChild(element);
        }        
    }
    
    /**
     * This method adds an attribute to the element
     * @param document Element to append to
     * @param tag Tag of the attribute
     * @param value Value of the attribute
     */
    private void addAttribute(Element document, String tag, String value)
    {
        Attr attr;
        
        attr = doc.createAttribute(tag);
        attr.setValue(value);
        document.setAttributeNode(attr);        
    }
    
    
    /**
     * Add the GPX 1.0 header
     * @param doc The XML document
     * @param gpxElement The GPX element
     */
    private void addGpx1_0Header(Document doc, Element gpxElement, String creator)
    {
        // GPX version 1.0
        addAttribute(gpxElement, "creator", creator);
        addAttribute(gpxElement, "version", "1.0");

        // GPX namespace
        addAttribute(gpxElement, "xmlns", "http://www.topografix.com/GPX/1/0");
        
        // XMLSchema namespace
        addAttribute(gpxElement, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

         // Schema locations - just the GPX location
        addAttribute(gpxElement, "xsi:schemaLocation", "http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd ");
    }

    /**
     * Add the GPX 1.0 header
     * @param doc The XML document
     * @param gpxElement The GPX element
     */
    private void addGpx1_1Header(Document doc, Element gpxElement, String creator)
    {
        // GPX version 1.1
        addAttribute(gpxElement, "creator", creator);
        addAttribute(gpxElement, "version", "1.1");;

        // XMLSchema namespace
        addAttribute(gpxElement, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

        // GPX namespace
        addAttribute(gpxElement, "xmlns", "http://www.topografix.com/GPX/1/1");
        
        // u-gotMe namespace
        addAttribute(gpxElement, "xmlns:u-gotMe", "http://tracklog.studioblueplanet.net/gpxextensions/v2");

        // Schema locations
        addAttribute(gpxElement, "xsi:schemaLocation", 
                                 "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd "+
                                 "http://tracklog.studioblueplanet.net/gpxextensions/v2 https://tracklog.studioblueplanet.net/gpxextensions/v2/ugotme-gpx.xsd");
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
        Element                     pointElement;
        Element                     element;
        Attr                        attr;
        DateTime                    dateTime;
        String                      dateTimeString;

        for (TrackPoint point : track.getTrackPoints(segmentNo))
        {
            pointElement    = doc.createElement("trkpt");
            segmentElement.appendChild(pointElement);

            addString(pointElement, "ele", String.valueOf(point.getElevation()));

            dateTime=point.getDateTime();
            dateTimeString=dateTime.format("YYYY-MM-DD")+"T"+
                           dateTime.format("hh:mm:ss")+"Z";
            addString(pointElement, "time", dateTimeString);
           

            // Extensions: speed
            addString(pointElement, "speed", String.valueOf(point.getSpeed()));

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
        Element                     pointElement;
        Element                     element;
        Element                     extensionsElement;
        DateTime                    dateTime;
        String                      dateTimeString;


        for(TrackPoint point : track.getTrackPoints(segmentNo))
        {
            pointElement    = doc.createElement("trkpt");
            segmentElement.appendChild(pointElement);

            addString(pointElement, "ele", String.valueOf(point.getElevation()));

            dateTime=point.getDateTime();
            dateTimeString=dateTime.format("YYYY-MM-DD")+"T"+
                           dateTime.format("hh:mm:ss")+"Z";
            addString(pointElement, "time", dateTimeString);

            extensionsElement    = doc.createElement("extensions");
            pointElement.appendChild(extensionsElement);

            // Extensions: speed
            addString(extensionsElement, "u-gotMe:speed", String.valueOf(point.getSpeed()));

            // Extensions: temperature
            addString(extensionsElement, "u-gotMe:temp", String.valueOf(point.getTemperature()));
            
            // set attribute 'lat' and 'lon' to element
            addAttribute(pointElement, "lat", String.valueOf(point.getLatitude()));
            addAttribute(pointElement, "lon", String.valueOf(point.getLongitude()));

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
    private void appendWaypointsGpx(Document doc, Element trackElement, List<Location> waypoints)
    {
        Element                     pointElement;
        Element                     element;
        DateTime                    dateTime;
        String                      dateTimeString;

        for(Location point : waypoints)
        {
            pointElement    = doc.createElement("wpt");
            trackElement.appendChild(pointElement);

            addString(pointElement, "ele", String.valueOf(point.getElevation()));
            dateTime=point.getDateTime();
            if (dateTime!=null)
            {
                dateTimeString=dateTime.format("YYYY-MM-DD")+"T"+
                               dateTime.format("hh:mm:ss")+"Z";
                addString(pointElement, "time", dateTimeString);
            }

            addString(pointElement, "name", point.getName());
            addString(pointElement, "desc", point.getDescription());
            addString(pointElement, "sym", "Waypoint");
          
            // set attribute 'lat' and 'lon' to element
            addAttribute(pointElement, "lat", String.valueOf(point.getLatitude()));
            addAttribute(pointElement, "lon", String.valueOf(point.getLongitude()));

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
            appendWaypointsGpx(doc, gpxElement, track.getWayPoints());
        }
        else if (gpxVersion.equals("1.1"))
        {
            appendWaypointsGpx(doc, gpxElement, track.getWayPoints());
        }

        // The track element
        if (track.getNumberOfSegments()>0)
        {
            trackElement = doc.createElement("trk");
            gpxElement.appendChild(trackElement);

            addString(trackElement, "name", trackName);
            addString(trackElement, "desc", track.getDeviceName() +" logged track");

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
    }

    /* ************************************************************************\
     * The interface functions
     * ************************************************************************/
    /**
     * Write the track to a GPX file
     * @param fileName Name of the file to write to
     * @param track Track to write
     * @param trackName Name for the track to use inside the GPX file 
     */
    public void writeTrackToFile(String fileName, Track track, String trackName)
    {
        Element     trackElement;
        Element     element;
        Comment     comment;
        Attr        attr;
        String      creator;

        wayPoints   =0;
        trackPoints =0;

        try
        {
            // create the GPX file
            createGpxDocument(track.getDeviceName());

            addTrack(doc, gpxElement, track, trackName);

            // write the content into xml file
            writeGpxDocument(fileName);

            LOGGER.info("File saved to {}!", fileName);
            LOGGER.info("Written track: {}, track points: {}, wayPoints: {}", trackName, trackPoints, wayPoints);

        }
        catch (ParserConfigurationException | TransformerException e)
        {
            LOGGER.error("Error writing GPX: {}", e.getMessage());
        }
    }

    /**
     * Write waypoints to a GPX file, as wpt elements
     * @param fileName Name of the file to write to
     * @param waypoints Waypoints to write
     */
    public void writeWaypointsToFile(String fileName, Locations waypoints)
    {
        Element     trackElement;
        Element     element;
        Comment     comment;
        Attr        attr;
        String      creator;

        wayPoints   =0;
        trackPoints =0;

        try
        {
            // create the GPX file
            createGpxDocument("Locations.fit");

            this.appendWaypointsGpx(doc, gpxElement, waypoints.getWaypoints());

            // write the content into xml file
            writeGpxDocument(fileName);

            LOGGER.info("Waypoint File saved to {}, saved {} waypoints!", fileName, waypoints.getNumberOfWaypoints());

        }
        catch (ParserConfigurationException | TransformerException e)
        {
            LOGGER.error("Error writing GPX: {}", e.getMessage());
        }
    }
}
 