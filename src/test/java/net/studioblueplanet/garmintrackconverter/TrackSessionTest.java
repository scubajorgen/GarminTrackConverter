/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

import java.util.List;
import net.studioblueplanet.fitreader.FitReader;
import net.studioblueplanet.fitreader.FitMessage;
import net.studioblueplanet.fitreader.FitMessageRepository;
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
public class TrackSessionTest
{
    private static List<FitMessage>     sessionMessages1;
    private static List<FitMessage>     sessionMessages2; // Cycling Edge 1040
    private static List<FitMessage>     sessionMessages3; 
    private static List<FitMessage>     sessionMessages4; 
    private static List<FitMessage>     sessionMessages5; 
    private static List<FitMessage>     sessionMessages6; 
    private static TrackSession         instance1;
    private static TrackSession         instance2;          // Cycling Edge 1040
    private static TrackSession         instance3;          // OW Swimming Fenix 7
    private static TrackSession         instance4;          // Run Fenix 7
    private static TrackSession         instance5;          // Hike Fenix 7
    private static TrackSession         instance6;          // Hike GPSMAP67
    
    public TrackSessionTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        FitReader reader                =FitReader.getInstance();
        
        FitMessageRepository repository =reader.readFile("src/test/resources/2021-05-08-10-18-29.fit");
        sessionMessages1                =repository.getAllMessages("session");
        instance1                       =new TrackSession(sessionMessages1);
        
        repository                      =reader.readFile("src/test/resources/2025-05-14-16-39-24_cycle_edge1040.fit");
        sessionMessages2                =repository.getAllMessages("session");
        instance2                       =new TrackSession(sessionMessages2);
        
        repository                      =reader.readFile("src/test/resources/2025-05-09-12-17-00_owswim_fenix7.fit");
        sessionMessages3                =repository.getAllMessages("session");
        instance3                       =new TrackSession(sessionMessages3);
        
        repository                      =reader.readFile("src/test/resources/2024-10-13-10-58-18_run_fenix7.fit");
        sessionMessages4                =repository.getAllMessages("session");
        instance4                       =new TrackSession(sessionMessages4);
        
        repository                      =reader.readFile("src/test/resources/2024-12-07-14-23-43_hike_fenix7.fit");
        sessionMessages5                =repository.getAllMessages("session");
        instance5                       =new TrackSession(sessionMessages5);
        
        repository                      =reader.readFile("src/test/resources/2025-05-13 21.46.01_hike_gpsmap67.fit");
        sessionMessages6                =repository.getAllMessages("session");
        instance6                       =new TrackSession(sessionMessages6);
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
     * Test of getMode method, of class TrackSession.
     */
    @Test
    public void testGetMode()
    {
        System.out.println("getMode");
        assertEquals("MOUNTAIN", instance1.getMode());
    }

    /**
     * Test of getSport method, of class TrackSession.
     */
    @Test
    public void testGetSport()
    {
        System.out.println("getSport");
        assertEquals("cycling", instance1.getSport());
    }

    /**
     * Test of getSubSport method, of class TrackSession.
     */
    @Test
    public void testGetSubSport()
    {
        System.out.println("getSubSport");
        assertEquals("gravel_cycling", instance1.getSubSport());
    }

    /**
     * Test of getStartTime method, of class TrackSession.
     */
    @Test
    public void testGetStartTime()
    {
        System.out.println("getStartTime");
        assertEquals("2021-05-08T08:18:29Z[UTC]", instance1.getStartTime().toString());
    }

    /**
     * Test of getEndTime method, of class TrackSession.
     */
    @Test
    public void testGetEndTime()
    {
        System.out.println("getEndTime");
        assertEquals("2021-05-08T10:39:38Z[UTC]", instance1.getEndTime().toString());
    }

    /**
     * Test of getElapsedTime method, of class TrackSession.
     */
    @Test
    public void testGetElapsedTime()
    {
        System.out.println("getElapsedTime");
        assertEquals(8457L, instance1.getElapsedTime().longValue());
    }

    /**
     * Test of getTimedTime method, of class TrackSession.
     */
    @Test
    public void testGetTimedTime()
    {
        System.out.println("getTimedTime");
        assertEquals(8457L, instance1.getTimedTime().longValue());
    }

    /**
     * Test of getStartLat method, of class TrackSession.
     */
    @Test
    public void testGetStartLat()
    {
        System.out.println("getStartLat");
        assertEquals(53.01285970956087, instance1.getStartLat(), 0.0000001);
    }

    /**
     * Test of getStartLon method, of class TrackSession.
     */
    @Test
    public void testGetStartLon()
    {
        System.out.println("getStartLon");
        assertEquals(6.724781217053533, instance1.getStartLon(), 0.0000001);
    }

    /**
     * Test of getDistance method, of class TrackSession.
     */
    @Test
    public void testGetDistance()
    {
        System.out.println("getDistance");
        assertEquals(50522.34, instance1.getDistance(), 0.001);
    }

    /**
     * Test of getAverageSpeed method, of class TrackSession.
     */
    @Test
    public void testGetAverageSpeed()
    {
        System.out.println("getAverageSpeed");
        assertEquals(21.5064, instance1.getAverageSpeed(), 0.001);
    }

    /**
     * Test of getMaxSpeed method, of class TrackSession.
     */
    @Test
    public void testGetMaxSpeed()
    {
        System.out.println("getMaxSpeed");
        assertEquals(36.2448, instance1.getMaxSpeed(), 0.001);
    }

    /**
     * Test of getAscent method, of class TrackSession.
     */
    @Test
    public void testGetAscent()
    {
        System.out.println("getAscent");
        assertEquals(111, instance1.getAscent().intValue());
    }

    /**
     * Test of getDescent method, of class TrackSession.
     */
    @Test
    public void testGetDescent()
    {
        System.out.println("getDescent");
        assertEquals(94, instance1.getDescent().intValue());
    }

    /**
     * Test of getGrit method, of class TrackSession.
     */
    @Test
    public void testGetGrit()
    {
        System.out.println("getGrit");
        assertEquals(15.90468, instance1.getGrit(), 0.001);
    }

    /**
     * Test of getFlow method, of class TrackSession.
     */
    @Test
    public void testGetFlow()
    {
        System.out.println("getFlow");
        assertEquals(4.599789, instance1.getFlow(), 0.001);
    }

    /**
     * Test of getCalories method, of class TrackSession.
     */
    @Test
    public void testGetCalories()
    {
        System.out.println("getCalories");
        assertEquals(1317.0, instance1.getCalories(), 0.001);
    }

    /**
     * Test of getJumpCount method, of class TrackSession.
     */
    @Test
    public void testGetJumpCount()
    {
        System.out.println("getJumpCount");
        assertEquals(0, instance1.getJumpCount().intValue());
        assertEquals(2, instance2.getJumpCount().intValue());
    }

    /**
     * Test of setSport method, of class TrackSession.
     */
    @Test
    public void testSetSport()
    {
        System.out.println("setSport");
        instance1.setSport("whistling");
        assertEquals("whistling", instance1.getSport());
    }

    /**
     * Test of getMinTemperature method, of class TrackSession.
     */
    @Test
    public void testMinTemperatue()
    {
        System.out.println("getMinTempreature");
        assertEquals(12, instance2.getMinTemperature().intValue()); // Cycling, Edge1040
    }

    /**
     * Test of getAvgTemperature method, of class TrackSession.
     */
    @Test
    public void testAvgTemperatue()
    {
        System.out.println("getAvgTemperature");
        assertEquals(15, instance2.getAvgTemperature().intValue());
    }

    /**
     * Test of getMaxTemperature method, of class TrackSession.
     */
    @Test
    public void testMaxTemperatue()
    {
        System.out.println("getMaxTempreature");
        assertEquals(21, instance2.getMaxTemperature().intValue());
    }

    /**
     * Test of getAvgHeartRate method, of class TrackSession.
     */
    @Test
    public void testAvgHeartRate()
    {
        System.out.println("getAvgHeartRate");
        assertEquals(138, instance2.getAvgHeartRate().intValue()); // cycle, Edge1040
        assertEquals(142, instance4.getAvgHeartRate().intValue()); // run, Fenix7
        assertEquals(87 , instance5.getAvgHeartRate().intValue()); // Hike, Fenix7
    }

    /**
     * Test of getMaxHeartRate method, of class TrackSession.
     */
    @Test
    public void testMaxHeartRate()
    {
        System.out.println("getMaxHeartRate");
        assertEquals(159, instance2.getMaxHeartRate().intValue()); // cycle
        assertEquals(165, instance4.getMaxHeartRate().intValue()); // run
        assertEquals(115, instance5.getMaxHeartRate().intValue()); // Hike, Fenix7
    }

    /**
     * Test of getMinRespirationRate method, of class TrackSession.
     */
    @Test
    public void testMinRespirationRate()
    {
        System.out.println("getMinRespirationRate");
        assertEquals(18.9, instance2.getMinRespirationRate().doubleValue(), 0.01);
    }

    /**
     * Test of getAvgRespirationRate method, of class TrackSession.
     */
    @Test
    public void testAvgRespirationRate()
    {
        System.out.println("getAvgRespirationRate");
        assertEquals(33.62, instance2.getAvgRespirationRate().doubleValue(), 0.01);
    }

    /**
     * Test of getMaxRespirationRate method, of class TrackSession.
     */
    @Test
    public void testMaxRespirationRate()
    {
        System.out.println("getMaxRespirationRate");
        assertEquals(41.14, instance2.getMaxRespirationRate().doubleValue(), 0.01);
    }

    /**
     * Test of getAvgPower method, of class TrackSession.
     */
    @Test
    public void testAvgPower()
    {
        System.out.println("getAvgPower");
        assertNull(instance2.getAvgPower());                    // Cycle Edge1040
        assertEquals(287, instance4.getAvgPower().intValue());  // Run, Fenix7
    }

    /**
     * Test of getMaxPower method, of class TrackSession.
     */
    @Test
    public void testMaxPower()
    {
        System.out.println("getMaxPower");
        assertNull(instance2.getMaxPower());                    // Cycle Edge1040
        assertEquals(287, instance4.getAvgPower().intValue());  // Run, Fenix 7
    }

    /**
     * Test of getAvgCadence method, of class TrackSession.
     */
    @Test
    public void testAvgCadence()
    {
        System.out.println("getAvgCadence");
        assertNull(instance2.getAvgCadence());                      // Cycle Edge1040
        assertEquals(66, instance4.getAvgCadence().intValue());     // Run, Fenix 7. Note: cadence=2 steps
        assertEquals(46, instance5.getAvgCadence().intValue());     // Hike, Fenix 7. Note: cadence=2 steps (93 step/min)
    }

    /**
     * Test of getMaxCadence method, of class TrackSession.
     */
    @Test
    public void testMaxCadence()
    {
        System.out.println("getMaxCadence");  
        assertNull(instance2.getMaxCadence());                      // Cycle Edge1040
        assertEquals(115, instance4.getMaxCadence().intValue());    // Run, Fenix 7
        assertEquals(102, instance5.getMaxCadence().intValue());    // Hike, Fenix 7, 205 steps/min
    }

    /**
     * Test of getAvgStrokeSistance method, of class TrackSession.
     */
    @Test
    public void testAvgStrokeDistance()
    {
        System.out.println("getAvgStrokeDistance");
        assertNull(instance2.getAvgStrokeDistance());               // Cycling , Edge1040
        assertEquals(2.19, instance3.getAvgStrokeDistance(), 0.01); // Swimming, Fenix7
        assertNull(instance4.getAvgStrokeDistance());               // Running , Fenix7
        assertNull(instance5.getAvgStrokeDistance());               // Hiking  , Fenix7
        assertNull(instance6.getAvgStrokeDistance());               // Hiking  , gpamap67
    }

    /**
     * Test of getTotalCycles method, of class TrackSession.
     */
    @Test
    public void testTotalCycles()
    {
        System.out.println("getTotalCycles");
        assertNull(instance2.getTotalCycles());                    // cycle edge1040
        assertEquals(1366, instance3.getTotalCycles().intValue()); // swim Fenix 7
        assertEquals(4922, instance4.getTotalCycles().intValue()); // run  Fenix 7
        assertEquals(4577, instance5.getTotalCycles().intValue()); // hike Fenix 7, steps(9154)=2x cadence
    }

    /**
     * Test of getTotalAerobicTrainingEffect method, of class TrackSession.
     */
    @Test
    public void testGetTotalAerobicTrainingEffect()
    {
        System.out.println("getTotalAerobicTrainingEffect");
        assertEquals(4.0, instance2.getTotalAerobicTrainingEffect(), 0.1);      // Cycle   , Edge1040
        assertEquals(2.3, instance3.getTotalAerobicTrainingEffect(), 0.1);      // Swimming, Fenix 7
        assertEquals(3.8, instance4.getTotalAerobicTrainingEffect(), 0.1);      // Running , Fenix 7
        assertEquals(0.6, instance5.getTotalAerobicTrainingEffect(), 0.1);      // Hiking  , Fenix 7
        assertNull(instance6.getTotalAerobicTrainingEffect());                  // Hiking  , GPSMAP67
    }
    
    /**
     * Test of getTotalAneerobicTrainingEffect method, of class TrackSession.
     */
    @Test
    public void testGetTotalAneerobicTrainingEffect()
    {
        System.out.println("getTotalAneerobicTrainingEffect");
        assertEquals(1.4, instance2.getTotalAnaerobicTrainingEffect(), 0.1);    // Cycle   , Edge1040
        assertEquals(1.4, instance3.getTotalAnaerobicTrainingEffect(), 0.1);    // Swimming, Fenix 7
        assertEquals(0.0, instance4.getTotalAnaerobicTrainingEffect(), 0.1);    // Running , Fenix 7
        assertEquals(0.0, instance5.getTotalAnaerobicTrainingEffect(), 0.1);    // Hiking  , Fenix 7
        assertNull(instance6.getTotalAnaerobicTrainingEffect());                // Hiking  , GPSMAP67
    }
    
    /**
     * Test of getExerciseLoad method, of class TrackSession.
     */
    @Test
    public void testGetExerciseLoad()
    {
        System.out.println("getExerciseLoad");
        assertEquals(173.437, instance2.getExerciseLoad(), 0.001);  // Cycle   , Edge1040
        assertEquals( 54.690, instance3.getExerciseLoad(), 0.001);  // Swimming, Fenix 7
        assertEquals(142.088, instance4.getExerciseLoad(), 0.001);  // Running , Fenix 7
        assertEquals(  6.527, instance5.getExerciseLoad(), 0.001);  // Hiking  , Fenix 7
        assertNull(instance6.getExerciseLoad());                    // Hiking  , GPSMAP67
    }
}
