/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

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
     * Test of writeGpxDocument method, of class GpxWriter.
     */
    @Test
    @org.junit.Ignore
    public void testWriteGpxDocument() throws Exception
    {
        System.out.println("writeGpxDocument");
        String fileName = "";
        GpxWriter instance = null;
        instance.writeGpxDocument(fileName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeTrackToFile method, of class GpxWriter.
     */
    @Test
    @org.junit.Ignore
    public void testWriteTrackToFile()
    {
        System.out.println("writeTrackToFile");
        String fileName = "";
        Track track = null;
        String trackName = "";
        GpxWriter instance = null;
        instance.writeTrackToFile(fileName, track, trackName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
