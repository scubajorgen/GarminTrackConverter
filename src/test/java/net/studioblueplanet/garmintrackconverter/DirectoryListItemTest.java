/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

/**
 *
 * @author jorgen
 */
@RunWith(MockitoJUnitRunner.class)
public class DirectoryListItemTest
{
    @Mock
    private File                file;    
    
    private DirectoryListItem   instance;
    private static Track        track;
    private static Track        track2;
    
    public DirectoryListItemTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        track=new Track("src/test/resources/2022-03-20-11-57-12.fit", "TestDevice", 3.0, 500);
        track2=new Track(1.0, 2.0);
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
        when(file.getName()).thenReturn("testfilename");
        when(file.length()).thenReturn(121L);
        instance=new DirectoryListItem(file);
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
     * Test of getTrack, setTrack method, of class DirectoryListItem.
     */
    @Test
    public void testGetTrack()
    {
        System.out.println("get/setCachedItem");
        assertNull(instance.getTrack());
        instance.setTrack(track);
        assertEquals(track, instance.getTrack());
    }

    /**
     * Test of getFilesize method, of class DirectoryListItem.
     */
    @Test
    public void testGetFilesize()
    {
        System.out.println("get/Filesize");
        assertEquals(121L, instance.getFilesize());
    }    
    
    /**
     * Test of getDescription method, of class DirectoryListItem.
     */
    @Test
    public void testGetDescription()
    {
        System.out.println("getDescription");
        assertEquals("testfilename", instance.getDescription());
        instance.setTrack(track);
        assertEquals("testfilename   cycling   0.0 km", instance.getDescription());
        instance.setTrack(track2);
        assertEquals("testfilename c", instance.getDescription());
    }
}
