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
import java.util.Locale;

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
        Locale.setDefault(Locale.US);
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
        Track track=new Track(0.0, 0);
        
        List<TrackSegment> segments=track.getSegments();
        segment=new TrackSegment(ZonedDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")),
                                 ZonedDateTime.of(2022, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC")));
        
        point=new TrackPoint(ZonedDateTime.of(2022, 1, 1, 0, 0,10, 0, ZoneId.of("UTC")),
                             53.5, 6.5, 1.0, 2.0, 3.0, 4, 5, 6, 7);

        segment.addTrackPoint(point);
        segments.add(segment);
        
        StringWriter writer=new StringWriter();
        instance.setGpxVersion("1.1");
        track.setBehaviour(false, false);
        instance.writeTrackToFile(writer, track, "trackname", "appname");
        System.out.println(writer);
        result=new String(Files.readAllBytes((new File("src/test/resources/result1a.txt")).toPath()));
        assertEquals(result, writer.toString());
        writer.close();

        writer=new StringWriter();
        instance.setGpxVersion("1.0");
        instance.writeTrackToFile(writer, track, "trackname", "appname");
        System.out.println(writer);
        result=new String(Files.readAllBytes((new File("src/test/resources/result1b.txt")).toPath()));
        assertEquals(result, writer.toString());
        writer.close();
    }

    /**
     * Test of writeTrackToFile method, of class GpxWriter.
     */
    @Test
    public void testWriteTrackToFile2() throws Exception
    {
        String          result;
        
        System.out.println("writeTrackToFile");
        Track track=TestTrack.testTrack();
        
        // Non compressed, non smoothed: 14 points
        StringWriter writer=new StringWriter();
        instance.setGpxVersion("1.1");
        track.setBehaviour(false, false);
        instance.writeTrackToFile(writer, track, "trackname", "appname");
        System.out.println(writer);
        result=new String(Files.readAllBytes((new File("src/test/resources/result3a.txt")).toPath()));
        assertEquals(result, writer.toString());
        writer.close();

        // Compressed, non smoothed: 3 points
        writer=new StringWriter();
        track.setBehaviour(false, true);
        instance.writeTrackToFile(writer, track, "trackname", "appname");
        System.out.println(writer);
        result=new String(Files.readAllBytes((new File("src/test/resources/result3b.txt")).toPath()));
        System.out.println(writer.toString());       
        assertEquals(result, writer.toString());
        writer.close();
        
        // Non compressed, non smoothed:14 points
        writer=new StringWriter();
        track.setBehaviour(true, false);
        instance.writeTrackToFile(writer, track, "trackname", "appname");
        System.out.println(writer);
        result=new String(Files.readAllBytes((new File("src/test/resources/result3c.txt")).toPath()));
        System.out.println(writer.toString());       
        assertEquals(result, writer.toString());
        writer.close();

        // Compressed, non smoothed: 7 points
        writer=new StringWriter();
        track.setBehaviour(true, true);
        instance.writeTrackToFile(writer, track, "trackname", "appname");
        System.out.println(writer);
        result=new String(Files.readAllBytes((new File("src/test/resources/result3d.txt")).toPath()));
        System.out.println(writer.toString());       
        assertEquals(result, writer.toString());
        writer.close();
    }
    
    /**
     * Test of writeTrackToFile method, of class GpxWriter.
     */
    @Test
    public void testWriteTrackToFile3() throws Exception
    {
        String          result;
        
        System.out.println("writeTrackToFile - real file");
        Track track=new Track("src/test/resources/2023-11-22-19-57-27-external.fit", "test", 1.0, 5.0);
        
        // Non compressed, non smoothed
        StringWriter writer=new StringWriter();
        instance.setGpxVersion("1.1");
        track.setBehaviour(false, false);
        instance.writeTrackToFile(writer, track, "trackname", "appname");
        System.out.println(writer);
        result=new String(Files.readAllBytes((new File("src/test/resources/result4.txt")).toPath()));
        assertEquals(result, writer.toString());
        writer.close();
    }

    /**
     * Test of writeTrackToFile method, of class GpxWriter.
     */
    @Test
    public void testWriteTrackToFile5() throws Exception
    {
        String          result;
        
        System.out.println("writeTrackToFile only waypoints");
        Track track=new Track(0.0, 0.0);
        
        ZoneId zoneId = ZoneId.of("UTC+2");
        ZonedDateTime zdt = ZonedDateTime.of(2015, 11, 30, 23, 45, 59, 0, zoneId);        
        
        Location waypoint1=new Location("Waypoint 1", "description1", zdt, 5.0, 53.0, 0.0, 0);
        track.addWaypoint(waypoint1);
        Location waypoint2=new Location("Waypoint 2", "description2", zdt, 6.0, 54.0, 1.0, 1);
        track.addWaypoint(waypoint2);
        
        StringWriter writer=new StringWriter();
        instance.setGpxVersion("1.1");
        track.setBehaviour(false, false);
        instance.writeTrackToFile(writer, track, "trackname", "appname");
        System.out.println(writer);
        result=new String(Files.readAllBytes((new File("src/test/resources/result5.txt")).toPath()));
        assertEquals(result, writer.toString());
        writer.close();

    }

}
