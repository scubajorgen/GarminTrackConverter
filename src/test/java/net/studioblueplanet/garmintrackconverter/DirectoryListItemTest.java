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
public class DirectoryListItemTest
{
    private DirectoryListItem instance;
    private static Track track;
    
    public DirectoryListItemTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        track=new Track("src/test/resources/2022-03-20-11-57-12.fit", "TestDevice", 3.0, 500);
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
        instance=new DirectoryListItem("testfilename");
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getFilename method, of class DirectoryListItem.
     */
    @Test
    public void testGetFilename()
    {
        System.out.println("getFilename");
        assertEquals("testfilename", instance.getFilename());
    }

    /**
     * Test of getCachedItem method, of class DirectoryListItem.
     */
    @Test
    public void testGetSetCachedItem()
    {
        System.out.println("get/setCachedItem");
        assertNull(instance.getCachedItem());
        Track track=new Track(3.0, 500);
        instance.setCachedItem(track);
        assertEquals(track, instance.getCachedItem());
        Locations locations=new Locations();
        instance.setCachedItem(locations);
        assertEquals(locations, instance.getCachedItem());
    }

    /**
     * Test of getDescription method, of class DirectoryListItem.
     */
    @Test
    public void testGetDescription()
    {
        System.out.println("getDescription");
        assertEquals("testfilename", instance.getDescription());
        instance.setCachedItem(track);
        assertEquals("testfilename   cycling   0.0 km", instance.getDescription());
    }
    
}
