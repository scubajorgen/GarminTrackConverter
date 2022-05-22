/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private String              appName;

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
    private void addChildElement(Element document, String tag, String string)
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
     * Adds a double value to the XML Element
     * @param document Element to add to
     * @param value Double
     * @param digits Number of digits following the dot 
     */
    private void addChildElement(Element document, String tag, Double value, int digits)
    {
        Element element;
        
        if (value!=null && !value.isNaN())
        {
            element    = doc.createElement(tag);
            element.appendChild(doc.createTextNode(String.format("%1."+digits+"f", value)));
            document.appendChild(element);
        }        
    }
    
    /**
     * Adds a int value to the XML Element
     * @param document Element to add to
     * @param value Double
     */
    private void addChildElement(Element document, String tag, Integer value)
    {
        Element element;
        
        if (value!=null)
        {
            element    = doc.createElement(tag);
            element.appendChild(doc.createTextNode(String.valueOf(value)));
            document.appendChild(element);
        }        
    }
    
    /**
     * Adds a int value to the XML Element
     * @param document Element to add to
     * @param value Double
     * @param divisor Divisor
     */
    private void addChildElement(Element document, String tag, Long value)
    {
        Element element;
        
        if (value!=null)
        {
            element    = doc.createElement(tag);
            element.appendChild(doc.createTextNode(String.valueOf(value)));
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
        addAttribute(gpxElement, "xmlns:u-gotMe", "http://tracklog.studioblueplanet.net/gpxextensions/v3");

        // Schema locations
        addAttribute(gpxElement, "xsi:schemaLocation", 
                                 "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd "+
                                 "http://tracklog.studioblueplanet.net/gpxextensions/v3 https://tracklog.studioblueplanet.net/gpxextensions/v3/ugotme-gpx.xsd");
    }

    /**
     * This method writes the GPX file
     * @param fileName Name of the file
     * @throws javax.xml.transform.TransformerException
     */
    private void writeGpxDocument(Writer writer) throws TransformerException, IOException
    {
        StringWriter stringWriter;
        
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        stringWriter=new StringWriter();
        StreamResult result = new StreamResult(stringWriter);
        transformer.transform(source, result);
        
        writer.write(stringWriter.toString());
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
    private void appendTrackGpx1_0(Document doc, Element segmentElement, Track track, int segmentNo, boolean compressed)
    {
        Element                     pointElement;
        Element                     element;
        Attr                        attr;
        ZonedDateTime               dateTime;
        String                      dateTimeString;
        List<TrackPoint>            segmentPoints;

        if (compressed)
        {
            segmentPoints=track.getCompressedTrackPoints(segmentNo);
        }
        else
        {
            segmentPoints=track.getTrackPoints(segmentNo);
        }

        for(TrackPoint point : segmentPoints)
        {
            pointElement    = doc.createElement("trkpt");
            segmentElement.appendChild(pointElement);

            addChildElement(pointElement, "ele", point.getElevation(), 1);

            dateTime=point.getDateTime();
            dateTimeString=dateTime.withZoneSameInstant(ZoneId.of("UTC"))
                                   .format( DateTimeFormatter.ISO_OFFSET_DATE_TIME );
            addChildElement(pointElement, "time", dateTimeString);
           

            // Extensions: speed
            addChildElement(pointElement, "speed", point.getSpeed(), 1);

            // TO DO ADD DISTANCE

            // set attribute 'lat' to element
            attr = doc.createAttribute("lat");
            attr.setValue(String.format("%1.7f", point.getLatitude()));
            pointElement.setAttributeNode(attr);

            // set attribute 'lon' to element
            attr = doc.createAttribute("lon");
            attr.setValue(String.format("%1.7f", point.getLongitude()));
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
    private void appendTrackGpx1_1(Document doc, Element segmentElement, Track track, int segmentNo, boolean compressed)
    {
        Element                     pointElement;
        Element                     element;
        Element                     extensionsElement;
        ZonedDateTime               dateTime;
        String                      dateTimeString;
        List<TrackPoint>            segmentPoints;

        if (compressed)
        {
            segmentPoints=track.getCompressedTrackPoints(segmentNo);
        }
        else
        {
            segmentPoints=track.getTrackPoints(segmentNo);
        }

        for(TrackPoint point : segmentPoints)
        {
            pointElement    = doc.createElement("trkpt");
            segmentElement.appendChild(pointElement);

            addChildElement(pointElement, "ele", point.getElevation(), 1);

            dateTime=point.getDateTime();
            dateTimeString=dateTime.withZoneSameInstant(ZoneId.of("UTC"))
                                   .format( DateTimeFormatter.ISO_OFFSET_DATE_TIME );
            addChildElement(pointElement, "time", dateTimeString);

            extensionsElement    = doc.createElement("extensions");
            pointElement.appendChild(extensionsElement);

            // Extensions: speed
            addChildElement(extensionsElement, "u-gotMe:speed", point.getSpeed(), 1);

            // Extensions: distance
            addChildElement(extensionsElement, "u-gotMe:dist", point.getDistance(), 1);
                        
            // Extensions: temperature
            addChildElement(extensionsElement, "u-gotMe:temp", point.getTemperature());
                        
            // Extensions: heartrate
            Integer heartrate=point.getHeartrate();
            if (heartrate!=null && heartrate<255)
            {
                addChildElement(extensionsElement, "u-gotMe:hr", heartrate);
            }

            // Extensions: temperature
            addChildElement(extensionsElement, "u-gotMe:ehpe", point.getGpsAccuracy());

            // set attribute 'lat' and 'lon' to element
            addAttribute(pointElement, "lat", String.format("%1.7f", point.getLatitude()));
            addAttribute(pointElement, "lon", String.format("%1.7f", point.getLongitude()));

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
        ZonedDateTime               dateTime;
        String                      dateTimeString;

        for(Location point : waypoints)
        {
            pointElement    = doc.createElement("wpt");
            trackElement.appendChild(pointElement);

            addChildElement(pointElement, "ele", point.getElevation(), 1);
            dateTime=point.getDateTime();
            if (dateTime!=null)
            {
                dateTimeString=dateTime.withZoneSameInstant(ZoneId.of("UTC"))
                                       .format( DateTimeFormatter.ISO_OFFSET_DATE_TIME );
                addChildElement(pointElement, "time", dateTimeString);
            }

            addChildElement(pointElement, "name", point.getName());
            addChildElement(pointElement, "desc", point.getDescription());
            addChildElement(pointElement, "sym", "Waypoint");
          
            // set attribute 'lat' and 'lon' to element
            addAttribute(pointElement, "lat", String.format("%1.7f", point.getLatitude()));
            addAttribute(pointElement, "lon", String.format("%1.7f", point.getLongitude()));

            wayPoints++;
        }
    }

    /**
     * This method adds the track segments to the track.
     * @param doc XML document
     * @param gpxElement The GPX element
     * @param trackNo The track identification
     * @param trackName The track name
     * @param compressed Indicates to write the compressed track (true) or uncompressed (false)
     */
    private void addTrack(Document doc, Element gpxElement, Track track, String trackName, boolean compressed)
    {
        int     i;
        int     numberOfSegments;
        Element trackElement;
        Element element;
        String  description;

        numberOfSegments=track.getNumberOfSegments();

        if (gpxVersion.equals("1.0"))
        {
            appendWaypointsGpx(doc, gpxElement, track.getWaypoints());
        }
        else if (gpxVersion.equals("1.1"))
        {
            appendWaypointsGpx(doc, gpxElement, track.getWaypoints());
        }

        // The track element
        if (track.getNumberOfSegments()>0)
        {
            trackElement = doc.createElement("trk");
            gpxElement.appendChild(trackElement);

            addChildElement(trackElement, "name", trackName);
            description="Created by: "+appName+". Logged by: "+track.getDeviceName();
            if (track.getSportDescription()!=null)
            {
                description+=". Logged as: "+track.getSportDescription()+".";
            }
            else
            {
                description+=".";
            }
            addChildElement(trackElement, "desc", description);

            // Add the track segments.
            i=0;
            while (i<numberOfSegments)
            {
                // segment
                Element segmentElement = doc.createElement("trkseg");
                trackElement.appendChild(segmentElement);

                if (gpxVersion.equals("1.0"))
                {
                    appendTrackGpx1_0(doc, segmentElement, track, i, compressed);
                }
                else if (gpxVersion.equals("1.1"))
                {
                    appendTrackGpx1_1(doc, segmentElement, track, i, compressed);
                }
                i++;
            }
            Element extensions=doc.createElement("extensions");
            gpxElement.appendChild(extensions);

            String activity=track.getSportDescription();
            addChildElement(extensions, "u-gotMe:device", track.getDeviceName()+" - sw "+track.getSoftwareVersion());
            addChildElement(extensions, "u-gotMe:software", appName);
            addChildElement(extensions, "u-gotMe:activity", activity);
            addChildElement(extensions, "u-gotMe:distance_m", track.getDistance(), 1);
            addChildElement(extensions, "u-gotMe:duration_s", track.getElapsedTime());
            addChildElement(extensions, "u-gotMe:timedDuration_s", track.getTimedTime());
            addChildElement(extensions, "u-gotMe:aveSpeed_kmh", track.getAverageSpeed(), 2);
            addChildElement(extensions, "u-gotMe:maxSpeed_kmh", track.getMaxSpeed(), 2);
            addChildElement(extensions, "u-gotMe:ascent_m", track.getAscent());
            addChildElement(extensions, "u-gotMe:descent_m", track.getDescent());
            addChildElement(extensions, "u-gotMe:calories_cal", track.getCalories(), 1);
            addChildElement(extensions, "u-gotMe:garminGrit_kgrit", track.getGrit(), 2);
            addChildElement(extensions, "u-gotMe:garminFlow", track.getFlow(), 2);
            Integer jumps=track.getJumpCount();
            if (jumps!=null && jumps!=0xFFFF)
            {
                addChildElement(extensions, "u-gotMe:garminJumpCount", jumps);
            }
        }
    }

    /* ************************************************************************\
     * The interface functions
     * ************************************************************************/
    /**
     * Write the track to a GPX file
     * @param writer Writer to use for writing the GPX
     * @param track Track to write
     * @param trackName Name for the track to use inside the GPX file 
     * @param appName Name of this application
     * @param compressed Indicates to write the compressed track (true) or uncompressed (false)
     */
    public void writeTrackToFile(Writer writer, Track track, String trackName, String appName, boolean compressed)
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
            this.appName=appName;
            // create the GPX file
            createGpxDocument(track.getDeviceName());

            addTrack(doc, gpxElement, track, trackName, compressed);

            // write the content into xml file
            writeGpxDocument(writer);

            LOGGER.info("Written track: {}, track points: {}, wayPoints: {}", trackName, trackPoints, wayPoints);

        }
        catch (ParserConfigurationException | TransformerException | IOException e)
        {
            LOGGER.error("Error writing GPX: {}", e.getMessage());
        }
    }

    /**
     * Write waypoints to a GPX file, as wpt elements
     * @param writer Writer to use for writing the GPX
     * @param waypoints Waypoints to write
     */
    public void writeWaypointsToFile(Writer writer, Locations waypoints)
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
            writeGpxDocument(writer);

            LOGGER.info("Waypoint File saved, saved {} waypoints!", waypoints.getNumberOfWaypoints());

        }
        catch (ParserConfigurationException | TransformerException | IOException e)
        {
            LOGGER.error("Error writing GPX: {}", e.getMessage());
        }
    }
}
 