/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

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
import net.studioblueplanet.settings.ApplicationSettings;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
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
    public enum GpxExtensions
    {
        garmin, 
        studioblueplanet,
        none
    }
    private final static Logger LOGGER = LogManager.getLogger(GpxWriter.class);
    private static GpxWriter    theInstance=null;

    private int                 trackPoints;
    private int                 wayPoints;
    private String              gpxVersion;
    private String              appName;
    private GpxExtensions       gpxExtensions;
    
    private Document            doc;
    private Element             gpxElement;
    

    /**
     * Constructor
     */
    public GpxWriter()
    {
        gpxVersion      ="1.1";
        ApplicationSettings settings=ApplicationSettings.getInstance();
        String extensions=settings.getGpxExtensions();
        if ("studioblueplanet".equals(extensions))
        {
            gpxExtensions=GpxExtensions.studioblueplanet;
        }
        else if ("garmin".equals(extensions))
        {
            gpxExtensions=GpxExtensions.garmin;
        }
        else
        {
            gpxExtensions=GpxExtensions.none;
        }
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
     * Set the GPX extensions. By default they are read from the settings file.
     * @param extensions New extensions
     */
    public void setGpxExtensions(GpxExtensions extensions)
    {
        this.gpxExtensions=extensions;
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
    private void createGpxDocument(String creator) throws ParserConfigurationException
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // The document
        doc = docBuilder.newDocument();


        // gpx elements
        gpxElement = doc.createElement("gpx");
        doc.appendChild(gpxElement);


        if (gpxVersion.equals("1.0"))
        {
            this.addGpx1_0Header(doc, gpxElement, creator);
        }
        else if (gpxVersion.equals("1.1"))
        {
            this.addGpx1_1Header(doc, gpxElement, creator);
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
     * @param value Integer value
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
     * @param value Boolean value
     */
    private void addChildElement(Element document, String tag, Boolean value)
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
        addAttribute(gpxElement, "xsi:schemaLocation", "http://www.topografix.com/GPX/1/0 "+
                                                       "http://www.topografix.com/GPX/1/0/gpx.xsd ");
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
        addAttribute(gpxElement, "version", "1.1");

        // XMLSchema namespace
        addAttribute(gpxElement, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

        // GPX namespace
        addAttribute(gpxElement, "xmlns", "http://www.topografix.com/GPX/1/1");
        
        String schemaLocation="http://www.topografix.com/GPX/1/1 "+
                              "https://www.topografix.com/GPX/1/1/gpx.xsd";
        
        // u-gotMe namespace
        if (gpxExtensions==GpxExtensions.studioblueplanet)
        {
            addAttribute(gpxElement, "xmlns:u-gotMe", "http://tracklog.studioblueplanet.net/gpxextensions/v4");
            
            schemaLocation  +=" http://tracklog.studioblueplanet.net/gpxextensions/v4 "+
                              "https://tracklog.studioblueplanet.net/gpxextensions/v4/gpxextensions.xsd";
        }
        else if (gpxExtensions==GpxExtensions.garmin)
        {
            addAttribute(gpxElement, "xmlns:gpxtx" , "http://www.garmin.com/xmlschemas/GpxExtensions/v3");
            addAttribute(gpxElement, "xmlns:gpxtpx", "http://www.garmin.com/xmlschemas/TrackPointExtension/v1");

            schemaLocation  +=" http://tracklog.studioblueplanet.net/gpxextensions/v4 "+
                              "https://tracklog.studioblueplanet.net/gpxextensions/v4/gpxextensions.xsd";

            schemaLocation  +=" http://www.garmin.com/xmlschemas/GpxExtensions/v3 "+
                              "https://www8.garmin.com/xmlschemas/GpxExtensionsv3.xsd"+ 
                              " http://www.garmin.com/xmlschemas/TrackPointExtension/v1 "+
                              "https://www8.garmin.com/xmlschemas/TrackPointExtensionv1.xsd";            
        }

        // Schema locations
        addAttribute(gpxElement, "xsi:schemaLocation", schemaLocation);
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
    private void appendTrackGpx1_0(Document doc, Element segmentElement, Track track, int segmentNo)
    {
        Element                     pointElement;
        Element                     element;
        Attr                        attr;
        ZonedDateTime               dateTime;
        String                      dateTimeString;
        List<TrackPoint>            segmentPoints;


        segmentPoints=track.getTrackPoints(segmentNo);
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
    private void appendTrackGpx1_1(Document doc, Element segmentElement, Track track, int segmentNo)
    {
        Element                     pointElement;
        Element                     gpxExtensionsElement;
        Element                     extensionsElement;
        ZonedDateTime               dateTime;
        String                      dateTimeString;
        List<TrackPoint>            segmentPoints;


        segmentPoints=track.getTrackPoints(segmentNo);
        for(TrackPoint point : segmentPoints)
        {
            pointElement    = doc.createElement("trkpt");
            segmentElement.appendChild(pointElement);

            addChildElement(pointElement, "ele", point.getElevation(), 1);

            dateTime=point.getDateTime();
            dateTimeString=dateTime.withZoneSameInstant(ZoneId.of("UTC"))
                                   .format( DateTimeFormatter.ISO_OFFSET_DATE_TIME );
            addChildElement(pointElement, "time", dateTimeString);

            if (gpxExtensions==GpxExtensions.studioblueplanet)
            {
                gpxExtensionsElement    = doc.createElement("extensions");
                pointElement.appendChild(gpxExtensionsElement);

                extensionsElement    = doc.createElement("u-gotMe:trackpointExtension");
                gpxExtensionsElement.appendChild(extensionsElement);
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

                // Extensions: respirationrate
                Double respiration=point.getRespirationrate();
                if (respiration!=null)
                {
                    addChildElement(extensionsElement, "u-gotMe:resp", respiration, 2);
                }

                // Extensions: temperature
                addChildElement(extensionsElement, "u-gotMe:ehpe", point.getEhpe());

                // Extensions: stamina
                Integer stamina=point.getStamina();
                if (stamina!=null && stamina<255)
                {
                    addChildElement(extensionsElement, "u-gotMe:sta", stamina);
                }
                // Extensions: stamina
                Integer staminaPotential=point.getStaminaPotential();
                if (staminaPotential!=null && staminaPotential<255)
                {
                    addChildElement(extensionsElement, "u-gotMe:staP", staminaPotential);
                }
            }
            else if (gpxExtensions==GpxExtensions.garmin)
            {
                gpxExtensionsElement    = doc.createElement("extensions");
                pointElement.appendChild(gpxExtensionsElement);

                extensionsElement    = doc.createElement("gpxtpx:TrackPointExtension");
                gpxExtensionsElement.appendChild(extensionsElement);                
                
                // Extensions: temperature; atemp for air temperature, wtemp for water temperature
                if ("swimming".equals(track.getSport()))
                {
                    addChildElement(extensionsElement, "gpxtpx:wtemp", point.getTemperature());
                }
                else
                {
                    addChildElement(extensionsElement, "gpxtpx:atemp", point.getTemperature());
                }
                // Extensions: heartrate
                Integer heartrate=point.getHeartrate();
                if (heartrate!=null && heartrate<255)
                {
                    addChildElement(extensionsElement, "gpxtpx:hr", heartrate);
                }                
            }

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
     * @param track The track to write
     * @param trackName The track name
     */
    private void addTrack(Document doc, Element gpxElement, Track track, String trackName)
    {
        int     i;
        int     numberOfSegments;
        Element trackElement;
        String  description;
        double  compressionMaxErr;

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
            
            description+=" Original file: "+track.getFitFileName();

            if (track.getBehaviourSmoothing())
            {
                description+=" (smoothed, ";
            }
            else
            {
                description+=" (raw, ";
            }
            
            if (track.getBehaviourCompression())
            {
                description+="compressed).";
                compressionMaxErr=track.getCompressionMaxError();
            }
            else
            {
                description+="uncompressed).";
                compressionMaxErr=0.0;
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
                    appendTrackGpx1_0(doc, segmentElement, track, i);
                }
                else if (gpxVersion.equals("1.1"))
                {
                    appendTrackGpx1_1(doc, segmentElement, track, i);
                }
                i++;
            }
            if (gpxVersion.equals("1.1") && gpxExtensions==GpxExtensions.studioblueplanet)
            {
                Element gpxExtensions=doc.createElement("extensions");
                gpxElement.appendChild(gpxExtensions);
                Element extensions=doc.createElement("u-gotMe:trackExtension");
                gpxExtensions.appendChild(extensions);

                String activity=track.getSportDescription();
                addChildElement(extensions, "u-gotMe:device"            , track.getDeviceName());
                addChildElement(extensions, "u-gotMe:deviceFirmware"    , track.getSoftwareVersion());
                String gps=track.getDeviceGps();
                if (gps!=null)
                {
                    addChildElement(extensions, "u-gotMe:deviceGps", gps);
                }
                String barometer=track.getDeviceBarometer();
                if (barometer!=null)
                {
                    addChildElement(extensions, "u-gotMe:deviceBarometer"       , barometer);
                }
                String externalHr=track.getDeviceExternalHr();
                if (externalHr!=null)
                {
                    addChildElement(extensions, "u-gotMe:deviceExternalHr"      , externalHr);
                }
                addChildElement(extensions, "u-gotMe:software"                  , appName);
                addChildElement(extensions, "u-gotMe:activity"                  , activity);
                addChildElement(extensions, "u-gotMe:sourceFile"                , track.getFitFileName());
                addChildElement(extensions, "u-gotMe:smoothing"                 , track.getBehaviourSmoothing());
                addChildElement(extensions, "u-gotMe:compression"               , track.getBehaviourCompression());
                addChildElement(extensions, "u-gotMe:compressionMaxErr"         , compressionMaxErr, 4);
                addChildElement(extensions, "u-gotMe:distance_m"                , track.getDistance(), 1);
                addChildElement(extensions, "u-gotMe:duration_s"                , track.getElapsedTime());
                addChildElement(extensions, "u-gotMe:timedDuration_s"           , track.getTimedTime());
                addChildElement(extensions, "u-gotMe:aveSpeed_kmh"              , track.getAverageSpeed(), 2);
                addChildElement(extensions, "u-gotMe:maxSpeed_kmh"              , track.getMaxSpeed(), 2);
                addChildElement(extensions, "u-gotMe:ascent_m"                  , track.getAscent());
                addChildElement(extensions, "u-gotMe:descent_m"                 , track.getDescent());
                addChildElement(extensions, "u-gotMe:minTemperature_C"          , track.getMinTemperature());
                addChildElement(extensions, "u-gotMe:maxTemperature_C"          , track.getMaxTemperature());
                addChildElement(extensions, "u-gotMe:aveTemperature_C"          , track.getAvgTemperature());
                addChildElement(extensions, "u-gotMe:maxHeartRate_bpm"          , track.getMaxHeartRate());
                addChildElement(extensions, "u-gotMe:aveHeartRate_bpm"          , track.getAvgHeartRate());
                addChildElement(extensions, "u-gotMe:minRespRate_bpm"           , track.getMinRespirationRate(), 2);
                addChildElement(extensions, "u-gotMe:maxRespRate_bpm"           , track.getMaxRespirationRate(), 2);
                addChildElement(extensions, "u-gotMe:aveRespRate_bpm"           , track.getAvgRespirationRate(), 2);
                addChildElement(extensions, "u-gotMe:maxCadence_rpm"            , track.getMaxCadence());
                addChildElement(extensions, "u-gotMe:aveCadence_rpm"            , track.getAvgCadence());
                addChildElement(extensions, "u-gotMe:maxPower_W"                , track.getMaxPower());
                addChildElement(extensions, "u-gotMe:avePower_W"                , track.getAvgPower());
                addChildElement(extensions, "u-gotMe:aveStrokeDistance_m"       , track.getAvgStrokeDistance(), 2);
                addChildElement(extensions, "u-gotMe:totalCycles"               , track.getTotalCycles());
                addChildElement(extensions, "u-gotMe:calories_cal"              , track.getCalories(), 1);
                addChildElement(extensions, "u-gotMe:garminGrit_kgrit"          , track.getGrit(), 2);
                addChildElement(extensions, "u-gotMe:garminFlow"                , track.getFlow(), 2);
                addChildElement(extensions, "u-gotMe:garminJumpCount"           , track.getJumpCount());
                addChildElement(extensions, "u-gotMe:garminAerobic"             , track.getTotalAerobicTrainingEffect(), 2);
                addChildElement(extensions, "u-gotMe:garminAnaerobic"           , track.getTotalAnaerobicTrainingEffect(), 2);
                addChildElement(extensions, "u-gotMe:garminExerciseLoad"        , track.getExerciseLoad(), 2);
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
     */
    public void writeTrackToFile(Writer writer, Track track, String trackName, String appName)
    {
        wayPoints   =0;
        trackPoints =0;
        try
        {
            this.appName=appName;
            // create the GPX file
            String creator=appName;
            String deviceName=track.getDeviceName();
            if (deviceName!=null && deviceName.length()>0)
            {
                creator+= " (using "+deviceName+")";
            }
            createGpxDocument(creator);

            addTrack(doc, gpxElement, track, trackName);

            // write the content into xml file
            writeGpxDocument(writer);

            LOGGER.info("Written track: {}, track points: {}, wayPoints: {}", trackName, trackPoints, wayPoints);

        }
        catch (ParserConfigurationException | TransformerException | IOException e)
        {
            LOGGER.error("Error writing GPX: {}", e.getMessage());
        }
    }
}
 