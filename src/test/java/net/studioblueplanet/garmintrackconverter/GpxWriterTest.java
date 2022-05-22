/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jorgen
 */
public class GpxWriterTest
{
    private static GpxWriter instance;
    
    public GpxWriterTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        instance=GpxWriter.getInstance();
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getInstance method, of class GpxWriter.
     */
    @Test
    public void testGetInstance()
    {
        System.out.println("getInstance");
        assertNotNull(GpxWriter.getInstance());
        assertEquals(instance, GpxWriter.getInstance());
    }
    /**
     * Test of setGpxVersion method, of class GpxWriter.
     */
    @Test
    public void testSetGpxVersion()
    {
        System.out.println("setGpxVersion");
        assertEquals("1.1", instance.getGpxVersion());
        instance.setGpxVersion("1.0");
        assertEquals("1.0", instance.getGpxVersion());
        instance.setGpxVersion("non_existent_version");
        assertEquals("1.0", instance.getGpxVersion());
    }

    /**
     * Test of writeTrackToFile method, of class GpxWriter.
     */
    @Test
    public void testWriteTrackToFile() throws Exception
    {
        TrackSegment    segment;
        TrackPoint      point;
        String          result;
        
        System.out.println("writeTrackToFile");
        Track track=new Track();
        
        List<TrackSegment> segments=track.getSegments();
        segment=new TrackSegment(ZonedDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")),
                                 ZonedDateTime.of(2022, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC")));
        
        point=new TrackPoint(ZonedDateTime.of(2022, 1, 1, 0, 0,10, 0, ZoneId.of("UTC")),
                             53.5, 6.5, 1.0, 2.0, 3.0, 4, 5, 6);

        segment.addTrackPoint(point);
        segments.add(segment);
        
        StringWriter writer=new StringWriter();
        instance.setGpxVersion("1.1");
        instance.writeTrackToFile(writer, track, "trackname", "appname", false);
        System.out.println(writer.toString());
        result=new String(Files.readAllBytes((new File("src/test/resources/result1a.txt")).toPath()));
        assertEquals(result, writer.toString());
        writer.close();

        writer=new StringWriter();
        instance.setGpxVersion("1.0");
        instance.writeTrackToFile(writer, track, "trackname", "appname", false);
        System.out.println(writer.toString());
        result=new String(Files.readAllBytes((new File("src/test/resources/result1b.txt")).toPath()));
        assertEquals(result, writer.toString());
        writer.close();
        
        // TO DO: test compressed GPX writing
    }

    /**
     * Test of writeWaypointsToFile method, of class GpxWriter.
     */
    @Test
    public void testWriteWaypointsToFile() throws Exception
    {
        List<Location>  locations;
        Location        loc;
        String          result;
        ZonedDateTime   dateTime;
        
        System.out.println("writeWaypointsToFile");

        Locations locationList=new Locations();
        locations=locationList.getWaypoints();
        dateTime=ZonedDateTime.of(2022, 05, 04, 14, 48, 30, 0, ZoneId.of("UTC"));
        loc=new Location("name1", "desc1", dateTime, 53.5, 6.5, 1.0, 2);
        locations.add(loc);
        loc=new Location("name2", "desc2", dateTime, 53.6, 6.6, 4.0, 5);
        locations.add(loc);
        
        StringWriter writer=new StringWriter();
        instance.setGpxVersion("1.1");
        instance.writeWaypointsToFile(writer, locationList);
        System.out.println(writer.toString());
        result=new String(Files.readAllBytes((new File("src/test/resources/result2a.txt")).toPath()));
        assertEquals(result, writer.toString());
        writer.close();

        writer=new StringWriter();
        instance.setGpxVersion("1.0");
        instance.writeWaypointsToFile(writer, locationList);
        System.out.println(writer.toString());
        result=new String(Files.readAllBytes((new File("src/test/resources/result2b.txt")).toPath()));
        assertEquals(result, writer.toString());
        writer.close();


    }    
}
