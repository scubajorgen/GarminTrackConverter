/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
public class TrackTest
{
    private Track instance;
    private static Track instanceTwoSegments;
    private static Track instanceExternalHr;
    
    public TrackTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        instanceTwoSegments    =new Track("src/test/resources/2022-03-20-11-57-12.fit", "TestDevice", 3.0, 0.5);
        instanceExternalHr     =new Track("src/test/resources/2023-11-22-19-57-27-external.fit", "TestDevice", 3.0, 0.5);
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
        List<Location> locations=new ArrayList<>();
        Location location=new Location("test", "", ZonedDateTime.parse("2021-05-08T08:20:00Z"), 5.0, 5.0, 0.0, 0);
        locations.add(location);

        instance=new Track("src/test/resources/2021-05-08-10-18-29.fit", "TestDevice", 3.0, 0.5);
        instance.addTrackWaypoints(locations);
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of addTrackWaypoints, getWaypoints method, of class Track.
     */
    @Test
    public void testAddTrackWaypoints()
    {
        System.out.println("addTrackWaypoints, getWaypoints");
        
        List<Location> waypoints=new ArrayList<>();
        Location location1=new Location("test", "", ZonedDateTime.parse("2020-05-08T08:18:30Z"), 0.0, 0.0, 0.0, 0);
        Location location2=new Location("test", "", ZonedDateTime.parse("2021-05-08T08:18:30Z"), 0.0, 0.0, 0.0, 0);
        Location location3=new Location("test", "", ZonedDateTime.parse("2022-05-08T08:18:30Z"), 0.0, 0.0, 0.0, 0);
        waypoints.add(location1);
        waypoints.add(location2);
        waypoints.add(location3);

        assertEquals(1, instance.getWaypoints().size());
        instance.addTrackWaypoints(waypoints);
        assertEquals(2, instance.getWaypoints().size());

        instance.addTrackWaypoints(waypoints);
        assertEquals(3, instance.getWaypoints().size());

        waypoints.clear();
        waypoints.add(location3);
        waypoints.add(location1);

        instance.addTrackWaypoints(waypoints);
        assertEquals(3, instance.getWaypoints().size());
        
    }

   /**
     * Test of setTrackWaypoints, getWaypoints method, of class Track.
     */
    @Test
    public void testSetTrackWaypoints()
    {
        System.out.println("setTrackWaypoints, getWaypoints");
        
        List<Location> waypoints=new ArrayList<>();
        Location location1=new Location("test1", "", ZonedDateTime.parse("2020-05-08T08:18:30Z"), 0.0, 0.0, 0.0, 0);
        Location location2=new Location("test2", "", ZonedDateTime.parse("2021-05-08T08:18:30Z"), 0.0, 0.0, 0.0, 0);
        Location location3=new Location("test3", "", ZonedDateTime.parse("2022-05-08T08:18:30Z"), 0.0, 0.0, 0.0, 0);
        waypoints.add(location1);
        waypoints.add(location2);
        waypoints.add(location3);

        assertEquals(1, instance.getWaypoints().size());
        assertEquals("test", instance.getWaypoints().get(0).getName());
        instance.setTrackWaypoints(waypoints);
        assertEquals(1, instance.getWaypoints().size());
        assertEquals("test2", instance.getWaypoints().get(0).getName());
    }
    
    
    /**
     * Test of getTrackInfo method, of class Track.
     */
    @Test
    public void testGetTrackInfo()
    {
        System.out.println("getTrackInfo");
        assertEquals("Track (cycling - gravel_cycling) with 1 segments (5023 points) and 1 waypoints", instance.getTrackInfo());
        assertEquals("Track (cycling - cyclocross) with 2 segments (10, 18 points) and 0 waypoints", instanceTwoSegments.getTrackInfo());
    }

    /**
     * Test of getDeviceName method, of class Track.
     */
    @Test
    public void testGetDeviceName()
    {
        System.out.println("getDeviceName");

        assertEquals("TestDevice", instance.getDeviceName());
    }

    /**
     * Test of getNumberOfSegments method, of class Track.
     */
    @Test
    public void testGetNumberOfSegments()
    {
        System.out.println("getNumberOfSegments - events");
        assertEquals(1, instance.getNumberOfSegments());
        assertEquals(2, instanceTwoSegments.getNumberOfSegments());

        System.out.println("getNumberOfSegments - laps");
        Track instance2=new Track("src/test/resources/Gerolsteiner_Felsenpfad.fit", "TestDevice", 3.0, 0.5);
        assertEquals(1, instance2.getNumberOfSegments());
        assertEquals("2022-11-08T19:00:39Z[UTC]", instance2.getSegments().get(0).getStartTime().toString());
        assertEquals("2022-11-08T21:52:54Z[UTC]", instance2.getSegments().get(0).getEndTime().toString());
    }

    /**
     * Test of getTrackPoints method, of class Track.
     */
    @Test
    public void testGetTrackPoints()
    {
        System.out.println("getTrackPoints");
        assertNull(instance.getTrackPoints(2));
        assertNull(instance.getTrackPoints(-1));
        assertNotNull(instance.getTrackPoints(0));
        assertEquals(5023, instance.getTrackPoints(0).size());
    }

    /**
     * Test of getWayPoints method, of class Track.
     */
    @Test
    public void testGetWayPoints()
    {
        System.out.println("getWayPoints");

        assertNotNull(instance.getWaypoints());
        assertEquals(1, instance.getWaypoints().size());
    }

    /**
     * Test of appendTrackSegment method, of class Track.
     */
    @Test
    public void testAppendTrackSegment()
    {
        System.out.println("appendTrackSegment");
        assertNotNull(instance.appendTrackSegment());
    }

    /**
     * Test of clear method, of class Track.
     */
    @Test
    public void testClear()
    {
        System.out.println("clear");
        instance.clear();
        assertEquals(0, instance.getNumberOfSegments());
        assertEquals(0, instance.getWaypoints().size());
    }
    
    /**
     * Test of getSport method, of class Track.
     */
    @Test
    public void testGetSport()
    {
        System.out.println("getSport");
        assertEquals("cycling", instance.getSport());
    }

    /**
     * Test of getSubSport method, of class Track.
     */
    @Test
    public void testGetSubSport()
    {
        System.out.println("getSubSport");
        assertEquals("gravel_cycling", instance.getSubSport());
    }

    /**
     * Test of getSportDescription method, of class Track.
     */
    @Test
    public void testGetSportDescription()
    {
        System.out.println("getSportDescription");
        assertEquals("cycling - gravel_cycling", instance.getSportDescription());
    }
    /**
     * Test of getFilename method, of class Track.
     */
    @Test
    public void testGetFitFileName()
    {
        System.out.println("getFitFileName");
        assertEquals("2021-05-08-10-18-29.fit", instance.getFitFileName());
    }
    
    /**
     * Test of getStartTime method, of class Track.
     */
    @Test
    public void testGetStartTime()
    {
        System.out.println("getStartTime");
        // UTC
        assertEquals("2021-05-08 08:18:29", instance.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    /**
     * Test of getStartDate method, of class Track.
     */
    @Test
    public void testGetStartDate()
    {
        System.out.println("getStartDate");
        // UTC
        assertEquals("20210508", instance.getStartDate());
    }
    
    /**
     * Test of getEndTime method, of class Track.
     */
    @Test
    public void testGetEndTime()
    {
        System.out.println("getEndTime");
        // UTC
        assertEquals("2021-05-08 10:39:38", instance.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    /**
     * Test of setBehaviour, getBehaviourSmoothing, getBehaviourCompression method, of class Track.
     */
    @Test
    public void testSetBehaviour()
    {
        System.out.println("setBehaviour, getBehaviourSmoothing, getBehaviourCompression");
        // raw, no compress
        boolean smoothed    = false;
        boolean compressed  = false;
        instance.setBehaviour(smoothed, compressed);
        assertEquals(smoothed, instance.getBehaviourSmoothing());
        assertEquals(compressed, instance.getBehaviourCompression());
        assertEquals(5023, instance.getSegments().get(0).getNumberOfTrackPoints());
        // raw, compress
        smoothed            = false;
        compressed          = true;
        instance.setBehaviour(smoothed, compressed);
        assertEquals(smoothed, instance.getBehaviourSmoothing());
        assertEquals(compressed, instance.getBehaviourCompression());
        assertEquals(666, instance.getSegments().get(0).getNumberOfTrackPoints());
        // smoothed, no compress
        smoothed            = true;
        compressed          = false;
        instance.setBehaviour(smoothed, compressed);
        assertEquals(smoothed, instance.getBehaviourSmoothing());
        assertEquals(compressed, instance.getBehaviourCompression());
        assertEquals(5023, instance.getSegments().get(0).getNumberOfTrackPoints());
        // smoothed, compress
        smoothed            = true;
        compressed          = true;
        instance.setBehaviour(smoothed, compressed);
        assertEquals(smoothed, instance.getBehaviourSmoothing());
        assertEquals(compressed, instance.getBehaviourCompression());
        assertEquals(545, instance.getSegments().get(0).getNumberOfTrackPoints());
     }

    /**
     * Test of getTrackInfo2 method, of class Track.
     */
    @Test
    public void testGetTrackInfo2()
    {
        System.out.println("getTrackInfo2");
        instance.setBehaviour(false, false);
        assertEquals("Activity: cycling - gravel_cycling\n" +
                     "Segments: 1, points: 5023, compressed: 666 (13%), waypoints: 1\n" +
                     "Valid points: 5023, invalid points: 0 (0%, omitted)\n" +
                     "Device: TestDevice, sw: 7.10", instance.getTrackInfo2());
        instance.setBehaviour(true, false);
        assertEquals("Activity: cycling - gravel_cycling (smoothed)\n" +
                     "Segments: 1, points: 5023, compressed: 545 (10%), waypoints: 1\n" +
                     "Valid points: 5023, invalid points: 0 (0%, omitted)\n" +
                     "Device: TestDevice, sw: 7.10", instance.getTrackInfo2());
        instance.setBehaviour(false, false);
        assertEquals("Activity: cycling - mountain\n" +
                     "Segments: 1, points: 123, compressed: 5 (4%), waypoints: 0\n" +
                     "Valid points: 123, invalid points: 0 (0%, omitted)\n" +
                     "Device: TestDevice, sw: 14.68 "+
                     "with external HR sensor: serial: 2017225 battery: 100% source: bluetooth_low_energy/antplus", 
                     instanceExternalHr.getTrackInfo2());
    }

    /**
     * Test of getSegments method, of class Track.
     */
    @Test
    public void testGetSegments()
    {
        System.out.println("getSegments");
        assertEquals(1, instance.getSegments().size());
        assertEquals(2, instanceTwoSegments.getSegments().size());
    }

    /**
     * Test of getElapsedTime method, of class Track.
     */
    @Test
    public void testGetElapsedTime()
    {
        System.out.println("getElapsedTime");
        assertEquals(8457L, instance.getElapsedTime().longValue());
        assertEquals(61L, instanceTwoSegments.getElapsedTime().longValue());
    }

    /**
     * Test of getTimedTime method, of class Track.
     */
    @Test
    public void testGetTimedTime()
    {
        System.out.println("getTimedTime");
        assertEquals(8457L, instance.getTimedTime().longValue());
        assertEquals(45L, instanceTwoSegments.getTimedTime().longValue());
    }

    /**
     * Test of getStartLat method, of class Track.
     */
    @Test
    public void testGetStartLat()
    {
        System.out.println("getStartLat");
        assertEquals(53.0128597, instance.getStartLat(), 0.0000001);
    }

    /**
     * Test of getStartLon method, of class Track.
     */
    @Test
    public void testGetStartLon()
    {
        System.out.println("getStartLon");
        assertEquals(6.7247812, instance.getStartLon(), 0.0000001);
    }

    /**
     * Test of getDistance method, of class Track.
     */
    @Test
    public void testGetDistance()
    {
        System.out.println("getDistance");
        assertEquals(50522.34, instance.getDistance().doubleValue(), 0.01);
    }

    /**
     * Test of getAverageSpeed method, of class Track.
     */
    @Test
    public void testGetAverageSpeed()
    {
        System.out.println("getAverageSpeed");
        assertEquals(21.5064, instance.getAverageSpeed().doubleValue(), 0.001);
    }

    /**
     * Test of getMaxSpeed method, of class Track.
     */
    @Test
    public void testGetMaxSpeed()
    {
        System.out.println("getMaxSpeed");
        assertEquals(36.2448, instance.getMaxSpeed().doubleValue(), 0.001);
    }

    /**
     * Test of getAscent method, of class Track.
     */
    @Test
    public void testGetAscent()
    {
        System.out.println("getAscent");
        assertEquals(111, instance.getAscent().intValue());
    }

    /**
     * Test of getDescent method, of class Track.
     */
    @Test
    public void testGetDescent()
    {
        System.out.println("getDescent");
        assertEquals(94, instance.getDescent().intValue());
    }


    /**
     * Test of getManufacturer method, of class Track.
     */
    @Test
    public void testGetManufacturer()
    {
        System.out.println("getManufacturer");
        assertEquals("garmin", instance.getManufacturer());
    }

    /**
     * Test of getProduct method, of class Track.
     */
    @Test
    public void testGetProduct()
    {
        System.out.println("getProduct");
        assertEquals("edge_830", instance.getProduct());
        assertEquals("edge_830", instanceTwoSegments.getProduct());
    }

    /**
     * Test of getSerialNumber method, of class Track.
     */
    @Test
    public void testGetSerialNumber()
    {
        System.out.println("getSerialNumber");
        assertEquals(3366957239L, instance.getSerialNumber().longValue());
    }

    /**
     * Test of getFileType method, of class Track.
     */
    @Test
    public void testGetFileType()
    {
        System.out.println("getFileType");
        assertEquals("activity", instance.getFileType());
    }

    /**
     * Test of getSoftwareVersion method, of class Track.
     */
    @Test
    public void testGetSoftwareVersion()
    {
        System.out.println("getSoftwareVersion");
        assertEquals("7.10", instance.getSoftwareVersion());
    }

    /**
     * Test of getGrit method, of class Track.
     */
    @Test
    public void testGetGrit()
    {
        System.out.println("getGrit");
        assertEquals(15.904688, instance.getGrit(), 0.000001);
    }

    /**
     * Test of getFlow method, of class Track.
     */
    @Test
    public void testGetFlow()
    {
        System.out.println("getFlow");
        assertEquals(4.5997896, instance.getFlow(), 0.000001);
    }

    /**
     * Test of getCalories method, of class Track.
     */
    @Test
    public void testGetCalories()
    {
        System.out.println("getCalories");
        assertEquals(1317, instance.getCalories(), 0.000001);
    }

    /**
     * Test of getJumpCount method, of class Track.
     */
    @Test
    public void testGetJumpCount()
    {
        System.out.println("getJumpCount");
        assertEquals(0, instance.getJumpCount().intValue());
    }

    /**
     * Test of getMode method, of class Track.
     */
    @Test
    public void testGetMode()
    {
        System.out.println("getMode");
        assertEquals("MOUNTAIN", instance.getMode());
    }

    /**
     * Test of getInvalidCoordinates method, of class Track.
     */
    @Test
    public void testGetInvalidCoordinates()
    {
        System.out.println("getInvalidCoordinates");
        assertEquals(0, instance.getInvalidCoordinates());
        Track instanceInvalidPoints   =new Track("src/test/resources/2023-06-06-17-18-00.fit", "TestDevice", 3.0, 0.5);
        assertEquals(748, instanceInvalidPoints.getInvalidCoordinates());
    }

    /**
     * Test of getDeviceExternalHrSensor method, of class Track.
     */
    @Test
    public void testGetDeviceExternalHrSensor()
    {
        System.out.println("getDeviceExternalHrSensor");
        assertNull(instance.getDeviceExternalHr());
        assertEquals("serial: 2017225 battery: 100% source: bluetooth_low_energy/antplus", 
                     instanceExternalHr.getDeviceExternalHr());
    }

    /**
     * Test of getDeviceGps method, of class Track.
     */
    @Test
    public void testGetDeviceGps()
    {
        System.out.println("getDeviceGps");
        assertEquals("garmin airoha, software version: 5.00", 
                     instanceExternalHr.getDeviceGps());
    }

    /**
     * Test of getDeviceGps method, of class Track.
     */
    @Test
    public void testGetDeviceBarometer()
    {
        System.out.println("getDeviceBarmeter");
         assertEquals("garmin fenix7, software version: 14.68", 
                     instanceExternalHr.getDeviceBarometer());
    }



    /**
     * Test of getValidCoordinates method, of class Track.
     */
    @Test
    public void testGetValidCoordinates()
    {
        System.out.println("getValidCoordinates");
        assertEquals(5023, instance.getValidCoordinates());
        assertEquals(28, instanceTwoSegments.getValidCoordinates());
    }

    /**
     * Test of getMaxError method, of class Track.
     */
    @Test
    public void testGetCompressionMaxError()
    {
        System.out.println("getMaxError");
        assertEquals(3.0, instance.getCompressionMaxError(), 0.0000001);
    }
}
