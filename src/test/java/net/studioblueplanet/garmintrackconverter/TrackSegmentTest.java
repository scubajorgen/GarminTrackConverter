/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
public class TrackSegmentTest
{
    private TrackSegment instance;
    private TrackSegment instanceRaw;
    private TrackSegment instanceSimple;
    
    
    public TrackSegmentTest()
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
    
    @Before
    public void setUp()
    {
        ZonedDateTime startDateTime;
        ZonedDateTime endDateTime;
        startDateTime   =ZonedDateTime.of(2022, 3, 4, 17, 54, 0, 0, ZoneId.of("UTC"));
        endDateTime     =ZonedDateTime.of(2022, 3, 4, 18, 54, 0, 0, ZoneId.of("UTC"));
        
        instance=new TrackSegment(startDateTime, endDateTime);
        instanceRaw=new TrackSegment(startDateTime, endDateTime);
        instanceSimple=new TrackSegment();
        // Now add a list of 14 test points
        List<TrackPoint> points=TestTrack.testPoints();
        for (TrackPoint point : points)
        {
            instance.addTrackPoint(point);
            instanceRaw.addTrackPoint(point);
        }
        instance.smooth();
        instance.compress(3.0);
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of isInSegment method, of class TrackSegment.
     */
    @Test
    public void testIsInSegment()
    {
        System.out.println("isInSegent");
        assertEquals(false, instance.isInSegment(ZonedDateTime.parse("2022-03-04T17:53:59Z")));
        assertEquals(true , instance.isInSegment(ZonedDateTime.parse("2022-03-04T17:54:00Z"))); // start time
        assertEquals(true , instance.isInSegment(ZonedDateTime.parse("2022-03-04T18:53:59Z")));
        assertEquals(true, instance.isInSegment(ZonedDateTime.parse("2022-03-04T18:54:00Z"))); // end time
        assertEquals(false, instance.isInSegment(ZonedDateTime.parse("2022-03-04T18:54:01Z")));
    }

    /**
     * Test of addTrackPoint method, of class TrackSegment.
     */
    @Test
    public void testAddTrackPoint()
    {
        System.out.println("addTrackPoint");
        TrackPoint point=new TrackPoint.TrackPointBuilder(6.0, 54.0).build();
        assertEquals(14, instance.getNumberOfTrackPoints());
        instance.addTrackPoint(point);
        assertEquals(15, instance.getNumberOfTrackPoints());
    }

    /**
     * Test of getTrackPoint method, of class TrackSegment.
     */
    @Test
    public void testGetTrackPoint()
    {
        System.out.println("getTrackPoint");
        TrackPoint point=instance.getTrackPoint(0);
        assertNotNull(point);
        assertEquals("2015-11-30T22:45:59Z", point.getDateTime().format(DateTimeFormatter.ISO_INSTANT));
        assertEquals(53.012562, point.getLatitude() , 0.000001);
        assertEquals( 6.725073, point.getLongitude(), 0.000001);
        point=instance.getTrackPoint(13);
        assertNotNull(point);
        assertEquals(53.013974, point.getLatitude() , 0.000001);
        assertEquals( 6.717725, point.getLongitude(), 0.000001);
        point=instance.getTrackPoint(14);
        assertNull(point);
    }

    /**
     * Test of getTrackPoints and setBehaviour method, of class TrackSegment.
     */
    @Test
    public void testGetTrackPoints()
    {
        System.out.println("getTrackPoints, setBehavour");
        
        // raw, uncompressed
        instance.setBehaviour(false, false);
        List<TrackPoint> points=instance.getTrackPoints();
        assertNotNull(points);
        assertEquals(14, points.size());
        assertEquals(53.012562, points.get(0).getLatitude() , 0.000001);
        assertEquals( 6.725073, points.get(0).getLongitude(), 0.000001);
        assertEquals(53.013974, points.get(13).getLatitude() , 0.000001);
        assertEquals( 6.717725, points.get(13).getLongitude(), 0.000001);
        
        // smoothed, uncompressed
        instance.setBehaviour(true, false);
        points=instance.getTrackPoints();
        assertEquals(14, points.size());
        assertEquals(53.012829, points.get(13).getLatitude() , 0.000001);
        assertEquals( 6.719117, points.get(13).getLongitude(), 0.000001);
        
        // raw, uncompressed
        instance.setBehaviour(false, true);
        points=instance.getTrackPoints();
        assertEquals(3, points.size());
        assertEquals(53.013974, points.get(2).getLatitude() , 0.000001);
        assertEquals( 6.717725, points.get(2).getLongitude(), 0.000001);       

        // smoothed, uncompressed
        instance.setBehaviour(true, true);
        points=instance.getTrackPoints();
        assertEquals(7, points.size());
        assertEquals(53.012829, points.get(6).getLatitude() , 0.000001);
        assertEquals( 6.719117, points.get(6).getLongitude(), 0.000001);      
    }

    /**
     * Test of getNumberOfTrackPoints and setBehaviour method, of class TrackSegment.
     */
    @Test
    public void testGetNumberOfTrackPoints()
    {
        System.out.println("getNumberOfTrackPoints, setBehaviour");
        // raw, uncompressed
        instance.setBehaviour(false, false);
        assertEquals(14, instance.getNumberOfTrackPoints());
        // smoothed, uncompressed
        instance.setBehaviour(true, false);
        assertEquals(14, instance.getNumberOfTrackPoints());
        // raw, uncompressed
        instance.setBehaviour(false, true);
        assertEquals(3, instance.getNumberOfTrackPoints());
        // smoothed, uncompressed
        instance.setBehaviour(true, true);
        assertEquals(7, instance.getNumberOfTrackPoints());
 
        assertEquals(0, instanceSimple.getNumberOfTrackPoints());
    }
    
    /**
     * Test of getStartTime method, of class TrackSegment.
     */
    @Test
    public void testGetStartTime()
    {
        System.out.println("getStartTime");
        assertEquals("2022-03-04 17:54:00", instance.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        assertNull(instanceSimple.getStartTime());
    }
    
    /**
     * Test of getEndTime method, of class TrackSegment.
     */
    @Test
    public void testGetEndTime()
    {
        System.out.println("getEndTime");
        assertEquals("2022-03-04 18:54:00", instance.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        assertNull(instanceSimple.getEndTime());
    }


    /**
     * Test of getNumberOfTrackPointsUncompressed method, of class TrackSegment.
     */
    @Test
    public void testGetNumberOfTrackPointsUncompressed()
    {
        System.out.println("getNumberOfTrackPointsUncompressed");
        // In all cases it should return the number of uncompressed track points
        // raw, uncompressed
        instance.setBehaviour(false, false);
        assertEquals(14, instance.getNumberOfTrackPointsUncompressed());
        // smoothed, uncompressed
        instance.setBehaviour(true, false);
        assertEquals(14, instance.getNumberOfTrackPointsUncompressed());
        // raw, uncompressed
        instance.setBehaviour(false, true);
        assertEquals(14, instance.getNumberOfTrackPointsUncompressed());
        // smoothed, uncompressed
        instance.setBehaviour(true, true);
        assertEquals(14, instance.getNumberOfTrackPointsUncompressed());
    }

    /**
     * Test of getNumberOfTrackPointsCompressed method, of class TrackSegment.
     */
    @Test
    public void testGetNumberOfTrackPointsCompressed()
    {
        System.out.println("getNumberOfTrackPointsCompressed");
        // raw, uncompressed
        instance.setBehaviour(false, false);
        assertEquals(3, instance.getNumberOfTrackPointsCompressed());
        // smoothed, uncompressed
        instance.setBehaviour(true, false);
        assertEquals(7, instance.getNumberOfTrackPointsCompressed());
        // raw, uncompressed
        instance.setBehaviour(false, true);
        assertEquals(3, instance.getNumberOfTrackPointsCompressed());
        // smoothed, uncompressed
        instance.setBehaviour(true, true);
        assertEquals(7, instance.getNumberOfTrackPointsCompressed());
    }

    /**
     * Test of sortOnDateTime method, of class TrackSegment.
     */
    @Test
    public void testSortOnDateTime()
    {
        System.out.println("sortOnDateTime");
        TrackSegment newInstance = new TrackSegment();
        // Now add 4 points in random order
        List<TrackPoint> points=TestTrack.testPoints();
        newInstance.addTrackPoint(points.get(13));
        newInstance.addTrackPoint(points.get(02));
        newInstance.addTrackPoint(points.get(03));
        newInstance.addTrackPoint(points.get(01));
        
        newInstance.setBehaviour(false, false);
        List<TrackPoint> thePoints=newInstance.getTrackPoints();
        // unsorted
        assertEquals("2015-11-30T22:47:56Z", thePoints.get(0).getDateTime().format(DateTimeFormatter.ISO_INSTANT));
        assertEquals("2015-11-30T22:46:17Z", thePoints.get(1).getDateTime().format(DateTimeFormatter.ISO_INSTANT));
        assertEquals("2015-11-30T22:46:26Z", thePoints.get(2).getDateTime().format(DateTimeFormatter.ISO_INSTANT));
        assertEquals("2015-11-30T22:46:08Z", thePoints.get(3).getDateTime().format(DateTimeFormatter.ISO_INSTANT));
        // sorted
        newInstance.sortOnDateTime();
        thePoints=newInstance.getTrackPoints();
        assertEquals("2015-11-30T22:46:08Z", thePoints.get(0).getDateTime().format(DateTimeFormatter.ISO_INSTANT));        
        assertEquals("2015-11-30T22:46:17Z", thePoints.get(1).getDateTime().format(DateTimeFormatter.ISO_INSTANT));
        assertEquals("2015-11-30T22:46:26Z", thePoints.get(2).getDateTime().format(DateTimeFormatter.ISO_INSTANT));
        assertEquals("2015-11-30T22:47:56Z", thePoints.get(3).getDateTime().format(DateTimeFormatter.ISO_INSTANT));
    }

    /**
     * Test of smooth method, of class TrackSegment.
     */
    @Test
    public void testSmooth()
    {
        System.out.println("smooth");
        // Instance raw only contains the raw track points; they are sorted
        instanceRaw.setBehaviour(true, false);
        assertEquals(0, instanceRaw.getNumberOfTrackPoints());
        instanceRaw.smooth();
        instanceRaw.setBehaviour(true, false);
        assertEquals(14, instanceRaw.getNumberOfTrackPoints());
        assertEquals(53.012829, instanceRaw.getTrackPoint(13).getLatitude() , 0.000001);
        assertEquals( 6.719117, instanceRaw.getTrackPoint(13).getLongitude(), 0.000001);
    }

    /**
     * Test of compress method, of class TrackSegment.
     */
    @Test
    public void testCompress()
    {
        System.out.println("compress");
        // Instance raw only contains the raw track points; they are sorted
        instanceRaw.setBehaviour(false, true);
        assertNull(instanceRaw.getTrackPoints());
        instanceRaw.setBehaviour(true, true);
        assertNull(instanceRaw.getTrackPoints());
        instanceRaw.smooth();
        instanceRaw.compress(3.0);
        instanceRaw.setBehaviour(false, true);
        assertEquals(3, instanceRaw.getNumberOfTrackPoints());
        instanceRaw.setBehaviour(true, true);
        assertEquals(7, instanceRaw.getNumberOfTrackPoints());
    }
    
}
