/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import net.studioblueplanet.fitreader.FitMessage;
import net.studioblueplanet.fitreader.FitMessageRepository;
import net.studioblueplanet.fitreader.FitReader;
import net.studioblueplanet.garmintrackconverter.TrackPoint.TrackPointBuilder;
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
public class TrackSegmentListTest
{
    private static List<FitMessage>     eventMessages;
    private static List<FitMessage>     lapMessages;
    private static TrackSegmentList     instance;
    private static ZoneId               zone; 
    private static TrackPoint           point00;
    private static TrackPoint           point01;
    private static TrackPoint           point02;
    private static TrackPoint           point03;
    private static TrackPoint           point10;
    private static TrackPoint           point11;
    private static TrackPoint           point12;
    private static TrackSegment         segment0;
    private static TrackSegment         segment1;
    
    public TrackSegmentListTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    /**
     * Test points:
     * ...
     *   .
     */
    @Before
    public void setUp()
    {
        FitReader reader                =FitReader.getInstance();
        FitMessageRepository repository =reader.readFile("src/test/resources/2021-05-08-10-18-29.fit");
        eventMessages                   =repository.getAllMessages("event");
        lapMessages                     =repository.getAllMessages("lap");
        instance                        =new TrackSegmentList();   
        
        zone                    =ZoneId.of("UTC");
        ZonedDateTime pointTime =ZonedDateTime.of(2025, 3, 11, 1, 0, 0, 0, zone);
        TrackPointBuilder b     =new TrackPointBuilder(52.0, 6.0).dateTime(pointTime);
        point00                 =b.build();
        
        pointTime               =ZonedDateTime.of(2025, 3, 11, 1, 0, 1, 0, zone);
        b                       =new TrackPointBuilder(52.1, 6.0).dateTime(pointTime);
        point01                 =b.build();

        pointTime               =ZonedDateTime.of(2025, 3, 11, 1, 0, 2, 0, zone);
        b                       =new TrackPointBuilder(52.2, 6.0).dateTime(pointTime);
        point02                 =b.build();        

        pointTime               =ZonedDateTime.of(2025, 3, 11, 1, 0, 3, 0, zone);
        b                       =new TrackPointBuilder(52.2, 6.1).dateTime(pointTime);
        point03                 =b.build();        
        
        ZonedDateTime start     =ZonedDateTime.of(2025, 3, 11, 1, 0, 0, 0, zone);
        ZonedDateTime end       =ZonedDateTime.of(2025, 3, 11, 1, 0, 4, 0, zone);
        segment0                =new TrackSegment(start, end);        
        
        pointTime               =ZonedDateTime.of(2025, 3, 11, 2, 0, 0, 0, zone);
        b                       =new TrackPointBuilder(52.2, 6.1).dateTime(pointTime);
        point10                 =b.build();        
        
        pointTime               =ZonedDateTime.of(2025, 3, 11, 2, 0, 1, 0, zone);
        b                       =new TrackPointBuilder(52.2, 6.1).dateTime(pointTime);
        point11                 =b.build();        
        
        pointTime               =ZonedDateTime.of(2025, 3, 11, 2, 0, 2, 0, zone);
        b                       =new TrackPointBuilder(52.2, 6.1).dateTime(pointTime);
        point12                 =b.build();        
        
        start                   =ZonedDateTime.of(2025, 3, 11, 2, 0, 0, 0, zone);
        end                     =ZonedDateTime.of(2025, 3, 11, 2, 0, 2, 0, zone);
        segment1                =new TrackSegment(start, end);        

    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of isEmpty method, of class TrackSegmentList.
     */
    @Test
    public void testIsEmpty()
    {
        System.out.println("isEmpty");
        assertTrue(instance.isEmpty());
        instance.getSegmentsFromEvents(eventMessages);
        assertFalse(instance.isEmpty());
    }

    /**
     * Test of setBehaviour method, of class TrackSegmentList.
     */
    @Test
    public void testSetBehaviour()
    {
        System.out.println("setBehaviour");
        instance.getSegmentsFromEvents(eventMessages);
        boolean smoothed = false;
        boolean compressed = false;
        TrackSegmentList instance = new TrackSegmentList();
        instance.setBehaviour(smoothed, compressed);
        // no sensible test
    }

    /**
     * Test of parseLaps method, of class TrackSegmentList.
     */
    @Test
    public void testParseLaps()
    {
        System.out.println("parseLaps");
        instance.parseLaps(lapMessages);
        assertEquals(1, instance.size());
        assertEquals("2021-05-08T08:18:29Z[UTC]", instance.getSegments().get(0).getStartTime().toString());
        assertEquals("2021-05-08T10:39:26Z[UTC]", instance.getSegments().get(0).getEndTime().toString());
    }

    /**
     * Test of getSegmentsFromEvents method, of class TrackSegmentList.
     */
    @Test
    public void testGetSegmentsFromEvents()
    {
        System.out.println("getSegmentsFromEvents");
        instance.getSegmentsFromEvents(eventMessages);
        assertEquals(1, instance.size());
        assertEquals("2021-05-08T08:18:29Z[UTC]", instance.getSegments().get(0).getStartTime().toString());
        assertEquals("2021-05-08T10:39:27Z[UTC]", instance.getSegments().get(0).getEndTime().toString());
    }
    
    /**
     * Test of getSegmentsFromEvents method, of class TrackSegmentList.
     */
    @Test
    public void testGetSegmentsFromTrackPoints()
    {
        System.out.println("getSegmentsFromTrackPoints");
        // Create a list of TrackPoints
        List<TrackPoint> points=new ArrayList<>();
        points.add(point00);
        points.add(point01);
        points.add(point02);
        points.add(point03);
        points.add(point10);
        points.add(point11);
        points.add(point12);
        // It should result in two segments
        instance.getSegmentsFromTrackPoints(points);
        assertEquals(2, instance.size());
        // Check first segment
        assertEquals("2025-03-11T01:00Z[UTC]"   , instance.get(0).getStartTime().toString());
        assertEquals("2025-03-11T01:00:03Z[UTC]", instance.get(0).getEndTime().toString());
        assertEquals(4, instance.get(0).getNumberOfTrackPoints());
        assertEquals(point00, instance.get(0).getTrackPoint(0));
        assertEquals(point01, instance.get(0).getTrackPoint(1));
        assertEquals(point02, instance.get(0).getTrackPoint(2));
        assertEquals(point03, instance.get(0).getTrackPoint(3));
        // Check second segment
        assertEquals("2025-03-11T02:00Z[UTC]"   , instance.get(1).getStartTime().toString());
        assertEquals("2025-03-11T02:00:02Z[UTC]", instance.get(1).getEndTime().toString());
        assertEquals(3, instance.get(1).getNumberOfTrackPoints());
        assertEquals(point10, instance.get(1).getTrackPoint(0));
        assertEquals(point11, instance.get(1).getTrackPoint(1));
        assertEquals(point12, instance.get(1).getTrackPoint(2));
    }

    /**
     * Test of addTrackPointToSegment method, of class TrackSegmentList.
     */
    @Test
    public void testAddTrackPointToSegment()
    {
        System.out.println("addTrackPointToSegment");
        instance.getSegmentsFromEvents(eventMessages);
        
        // Not
        ZonedDateTime pointTime =ZonedDateTime.of(2021, 5, 8, 8, 18, 28, 0, zone);
        TrackPointBuilder b     =new TrackPointBuilder(0, 0).dateTime(pointTime);
        TrackPoint point        =b.build();
        assertFalse(instance.addTrackPointToSegment(point));
        
        pointTime               =ZonedDateTime.of(2021, 5, 8, 8, 18, 29, 0, zone);
        b.dateTime(pointTime);
        point                   =b.build();
        assertTrue(instance.addTrackPointToSegment(point));

        pointTime               =ZonedDateTime.of(2021, 5, 8, 9, 00, 00, 0, zone);
        b.dateTime(pointTime);
        point                   =b.build();
        assertTrue(instance.addTrackPointToSegment(point));

        pointTime               =ZonedDateTime.of(2021, 5, 8,10, 39, 27, 0, zone);
        b.dateTime(pointTime);
        point                   =b.build();
        assertTrue(instance.addTrackPointToSegment(point));

        // Not
        pointTime               =ZonedDateTime.of(2021, 5, 8,10, 39, 28, 0, zone);
        b.dateTime(pointTime);
        point                   =b.build();
        assertFalse(instance.addTrackPointToSegment(point));
    }

    /**
     * Test of isDatetimeInSegment method, of class TrackSegmentList.
     */
    @Test
    public void testIsDatetimeInSegment()
    {
        System.out.println("isDatetimeInSegment");
        instance.getSegmentsFromEvents(eventMessages);
        // Not
        ZonedDateTime time =ZonedDateTime.of(2021, 5, 8, 8, 18, 28, 0, zone);
        assertEquals(false, instance.isDatetimeInSegment(time));

        time =ZonedDateTime.of(2021, 5, 8, 8, 18, 29, 0, zone);
        assertEquals(true, instance.isDatetimeInSegment(time));
        
        time =ZonedDateTime.of(2021, 5, 8, 9, 00, 00, 0, zone);
        assertEquals(true, instance.isDatetimeInSegment(time));
        
        time =ZonedDateTime.of(2021, 5, 8, 10, 39, 27, 0, zone);
        assertEquals(true, instance.isDatetimeInSegment(time));
        
        // not
        time =ZonedDateTime.of(2021, 5, 8, 10, 39, 28, 0, zone);
        assertEquals(false, instance.isDatetimeInSegment(time));
        
    }

    /**
     * Test of size method, of class TrackSegmentList.
     */
    @Test
    public void testSize()
    {
        System.out.println("size");
        assertEquals(0, instance.size());
        instance.getSegmentsFromEvents(eventMessages);
        assertEquals(1, instance.size());
    }

    /**
     * Test of get method, of class TrackSegmentList.
     */
    @Test
    public void testGet()
    {
        System.out.println("get");
        instance.getSegmentsFromEvents(eventMessages);
        assertNotNull(instance.get(0));
        try
        {
            instance.get(1);
            assertTrue(false); // never reached
        }
        catch(Exception e)
        {
            assertTrue(e instanceof java.lang.IndexOutOfBoundsException);
        }
    }

    /**
     * Test of add method, of class TrackSegmentList.
     */
    @Test
    public void testAdd()
    {
        System.out.println("add");
        TrackSegment segment = new TrackSegment();
        instance.add(segment);
        assertEquals(segment, instance.get(0));
        instance.getSegmentsFromEvents(eventMessages);
        TrackSegment segment2 = new TrackSegment();
        instance.add(segment2);
        assertEquals(segment2, instance.get(2));
    }

    /**
     * Test of getSegments method, of class TrackSegmentList.
     */
    @Test
    public void testGetSegments()
    {
        System.out.println("getSegments");
        assertEquals(0, instance.getSegments().size());
        instance.getSegmentsFromEvents(eventMessages);
        assertEquals(1, instance.getSegments().size());
    }

    /**
     * Test of sortSegments method, of class TrackSegmentList.
     */
    @Test
    public void testSortSegments()
    {
        System.out.println("sortSegments");

        instance.add(segment0);
        segment0.addTrackPoint(point02);
        segment0.addTrackPoint(point03);
        segment0.addTrackPoint(point00);
        segment0.addTrackPoint(point01);
        
        assertEquals(point02, instance.get(0).getTrackPoint(0));
        assertEquals(point03, instance.get(0).getTrackPoint(1));
        assertEquals(point00, instance.get(0).getTrackPoint(2));
        assertEquals(point01, instance.get(0).getTrackPoint(3));

        instance.sortSegments();
        
        assertEquals(point00, instance.get(0).getTrackPoint(0));
        assertEquals(point01, instance.get(0).getTrackPoint(1));
        assertEquals(point02, instance.get(0).getTrackPoint(2));
        assertEquals(point03, instance.get(0).getTrackPoint(3));
    }

    /**
     * Test of smoothSegments method, of class TrackSegmentList.
     */
    @Test
    public void testSmoothSegments()
    {
        System.out.println("smoothSegments");
        instance.add(segment0);
        segment0.addTrackPoint(point00);
        segment0.addTrackPoint(point01);
        segment0.addTrackPoint(point02);
        segment0.addTrackPoint(point03);
        instance.smoothSegments();
        // no sensible test
    }

    /**
     * Test of compressTrackSegments method, of class TrackSegmentList.
     */
    @Test
    public void testCompressTrackSegments()
    {
        System.out.println("compressTrackSegments");
        instance.add(segment0);
        segment0.addTrackPoint(point00);
        segment0.addTrackPoint(point01);
        segment0.addTrackPoint(point02);
        segment0.addTrackPoint(point03);
        instance.compressTrackSegments(1.0);
        assertEquals(3, instance.get(0).getNumberOfTrackPointsCompressed());
        instance.setBehaviour(false, true);
        assertEquals(3, instance.get(0).getTrackPoints().size());
        
    }

    /**
     * Test of clear method, of class TrackSegmentList.
     */
    @Test
    public void testClear()
    {
        System.out.println("clear");
        instance.getSegmentsFromEvents(eventMessages);
        assertFalse(instance.isEmpty());
        instance.clear();
        assertTrue(instance.isEmpty());
    }

    /**
     * Test of getTimedTime method, of class TrackSegmentList.
     */
    @Test
    public void testGetTimedTime()
    {
        System.out.println("getTimedTime");
        instance.getSegmentsFromEvents(eventMessages);
        assertEquals(8458, instance.getTimedTime());
        // add another two segment of 4+2 seconds
        instance.add(segment0);
        instance.add(segment1);
        assertEquals(8464, instance.getTimedTime());
    }
}
