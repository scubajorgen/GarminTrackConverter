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
    private static List<FitMessage>     sessionMessages;
    private static TrackSession         instance;
    
    public TrackSessionTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        FitReader reader                =FitReader.getInstance();
        FitMessageRepository repository =reader.readFile("src/test/resources/2021-05-08-10-18-29.fit");
        sessionMessages                 =repository.getAllMessages("session");
        instance                        =new TrackSession(sessionMessages);
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
        assertEquals("MOUNTAIN", instance.getMode());
    }

    /**
     * Test of getSport method, of class TrackSession.
     */
    @Test
    public void testGetSport()
    {
        System.out.println("getSport");
        assertEquals("cycling", instance.getSport());
    }

    /**
     * Test of getSubSport method, of class TrackSession.
     */
    @Test
    public void testGetSubSport()
    {
        System.out.println("getSubSport");
        assertEquals("gravel_cycling", instance.getSubSport());
    }

    /**
     * Test of getStartTime method, of class TrackSession.
     */
    @Test
    public void testGetStartTime()
    {
        System.out.println("getStartTime");
        assertEquals("2021-05-08T08:18:29Z[UTC]", instance.getStartTime().toString());
    }

    /**
     * Test of getEndTime method, of class TrackSession.
     */
    @Test
    public void testGetEndTime()
    {
        System.out.println("getEndTime");
        assertEquals("2021-05-08T10:39:38Z[UTC]", instance.getEndTime().toString());
    }

    /**
     * Test of getElapsedTime method, of class TrackSession.
     */
    @Test
    public void testGetElapsedTime()
    {
        System.out.println("getElapsedTime");
        assertEquals(8457L, instance.getElapsedTime().longValue());
    }

    /**
     * Test of getTimedTime method, of class TrackSession.
     */
    @Test
    public void testGetTimedTime()
    {
        System.out.println("getTimedTime");
        assertEquals(8457L, instance.getTimedTime().longValue());
    }

    /**
     * Test of getStartLat method, of class TrackSession.
     */
    @Test
    public void testGetStartLat()
    {
        System.out.println("getStartLat");
        assertEquals(53.01285970956087, instance.getStartLat(), 0.0000001);
    }

    /**
     * Test of getStartLon method, of class TrackSession.
     */
    @Test
    public void testGetStartLon()
    {
        System.out.println("getStartLon");
        assertEquals(6.724781217053533, instance.getStartLon(), 0.0000001);
    }

    /**
     * Test of getDistance method, of class TrackSession.
     */
    @Test
    public void testGetDistance()
    {
        System.out.println("getDistance");
        assertEquals(50522.34, instance.getDistance(), 0.001);
    }

    /**
     * Test of getAverageSpeed method, of class TrackSession.
     */
    @Test
    public void testGetAverageSpeed()
    {
        System.out.println("getAverageSpeed");
        assertEquals(21.5064, instance.getAverageSpeed(), 0.001);
    }

    /**
     * Test of getMaxSpeed method, of class TrackSession.
     */
    @Test
    public void testGetMaxSpeed()
    {
        System.out.println("getMaxSpeed");
        assertEquals(36.2448, instance.getMaxSpeed(), 0.001);
    }

    /**
     * Test of getAscent method, of class TrackSession.
     */
    @Test
    public void testGetAscent()
    {
        System.out.println("getAscent");
        assertEquals(111, instance.getAscent().intValue());
    }

    /**
     * Test of getDescent method, of class TrackSession.
     */
    @Test
    public void testGetDescent()
    {
        System.out.println("getDescent");
        assertEquals(94, instance.getDescent().intValue());
    }

    /**
     * Test of getGrit method, of class TrackSession.
     */
    @Test
    public void testGetGrit()
    {
        System.out.println("getGrit");
        assertEquals(15.90468, instance.getGrit(), 0.001);
    }

    /**
     * Test of getFlow method, of class TrackSession.
     */
    @Test
    public void testGetFlow()
    {
        System.out.println("getFlow");
        assertEquals(4.599789, instance.getFlow(), 0.001);
    }

    /**
     * Test of getCalories method, of class TrackSession.
     */
    @Test
    public void testGetCalories()
    {
        System.out.println("getCalories");
        assertEquals(1317.0, instance.getCalories(), 0.001);
    }

    /**
     * Test of getJumpCount method, of class TrackSession.
     */
    @Test
    public void testGetJumpCount()
    {
        System.out.println("getJumpCount");
        assertEquals(0, instance.getJumpCount().intValue());
    }

    /**
     * Test of setSport method, of class TrackSession.
     */
    @Test
    public void testSetSport()
    {
        System.out.println("setSport");
        instance.setSport("whistling");
        assertEquals("whistling", instance.getSport());
    }
}
