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

import java.io.File;
import javax.swing.JList;
import javax.swing.DefaultListModel;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.runner.RunWith;
/**
 *
 * @author jorgen
 */
@RunWith(MockitoJUnitRunner.class)
public class DirectoryListTest
{
    @Mock
    private File                        fileMock;
    @Mock
    private JList                       listMock;
    @Mock
    private DefaultListModel<String>    modelMock;
    
    private File[]                      fileList1;
    private File[]                      fileList2;
    private File[]                      fileList3;
    private File[]                      fileList4;
    @Mock
    private File                        file1;
    @Mock
    private File                        file2;
    @Mock
    private File                        file3;
    @Mock
    private File                        file4;
    @Mock
    private File                        file5;
    @Mock
    private File                        file6;
    
    public DirectoryListTest()
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
        fileList1=new File[2];
        fileList2=new File[2];
        fileList3=new File[0];
        fileList4=new File[2];
        
        when(file1.getName()).thenReturn("testfile1.fit");
        when(file1.getAbsolutePath()).thenReturn("testfile1.fit");
        when(file1.length()).thenReturn(100L);
        when(file1.isDirectory()).thenReturn(false);
        fileList1[0]=file1;

        when(file2.getName()).thenReturn("testfile2.fit");
        when(file2.getAbsolutePath()).thenReturn("testfile2.fit");
        when(file2.length()).thenReturn(100L);
        when(file2.isDirectory()).thenReturn(false);
        fileList1[1]=file2;

        when(file3.getName()).thenReturn("testfile3.fit");
        when(file3.getAbsolutePath()).thenReturn("testfile3.fit");
        when(file3.length()).thenReturn(100L);
        when(file3.isDirectory()).thenReturn(false);
        
        when(file4.getName()).thenReturn("testfile1.fit");
        when(file4.getAbsolutePath()).thenReturn("testfile1.fit");
        when(file4.length()).thenReturn(100L);
        when(file4.isDirectory()).thenReturn(false);
        
        when(file5.getName()).thenReturn("testfile5.gpx");
        when(file5.getAbsolutePath()).thenReturn("testfile5.gpx");
        when(file5.length()).thenReturn(100L);
        when(file5.isDirectory()).thenReturn(false);
        
        when(file5.getName()).thenReturn("testfile6.fit");
        when(file5.getAbsolutePath()).thenReturn("testfile6.fit");
        when(file5.length()).thenReturn(100L);
        when(file5.isDirectory()).thenReturn(false);
        
        fileList2[0]=file3;
        fileList2[1]=file4;

        fileList4[0]=file5;
        fileList4[1]=file6;
        
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of updateDirectoryList method, of class DirectoryList.
     */
    @Test
    public void testUpdateDirectoryList()
    {
        System.out.println("updateDirectoryList 1");
        // Ascending order sorting
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, true);
        
        reset(modelMock);
        ArgumentCaptor<String> stringCaptor=ArgumentCaptor.forClass(String.class);
        when(fileMock.listFiles()).thenReturn(fileList1);
        when(listMock.getSelectedIndex()).thenReturn(-1);
        boolean result=instance.updateDirectoryList(".fit");
        assertTrue(result);
        result=instance.updateListModel();
        assertFalse(result);
        verify(modelMock, times(2)).addElement(stringCaptor.capture());
        assertEquals("testfile1.fit", stringCaptor.getAllValues().get(0));
        assertEquals("testfile2.fit", stringCaptor.getAllValues().get(1));
    }

    /**
     * Test of updateDirectoryList method, of class DirectoryList.
     */
    @Test
    public void testUpdateDirectoryList2()
    {
        System.out.println("updateDirectoryList 2");
        // Ascending order sorting
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, true);
        
        reset(modelMock);
        ArgumentCaptor<String> stringCaptor=ArgumentCaptor.forClass(String.class);
        when(fileMock.listFiles()).thenReturn(fileList4);
        when(listMock.getSelectedIndex()).thenReturn(-1);
        boolean result=instance.updateDirectoryList(".fit");
        assertTrue(result);
        result=instance.updateListModel();
        assertFalse(result);
        verify(modelMock, times(1)).addElement(stringCaptor.capture());
        assertEquals("testfile6.fit", stringCaptor.getAllValues().get(0));

    }
    
    
    /**
     * Test of updateDirectoryList method, of class DirectoryList.
     */
    @Test
    public void testUpdateDirectoryList3()
    {
        System.out.println("updateDirectoryList 3");
        // Reversed order sorting
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, false);
        reset(modelMock);
        ArgumentCaptor<String> stringCaptor=ArgumentCaptor.forClass(String.class);
        when(fileMock.listFiles()).thenReturn(fileList1);
        when(listMock.getSelectedIndex()).thenReturn(0);
        when(modelMock.size()).thenReturn(2);
        boolean result=instance.updateDirectoryList(".fit");
        assertTrue(result);
        result=instance.updateListModel();
        assertFalse(result);
        verify(modelMock, times(2)).addElement(stringCaptor.capture());
        assertEquals("testfile2.fit", stringCaptor.getAllValues().get(0));
        assertEquals("testfile1.fit", stringCaptor.getAllValues().get(1));
    }

    
    /**
     * Test of updateDirectoryList method, of class DirectoryList.
     */
    @Test
    public void testUpdateDirectoryList4()
    {
        System.out.println("updateDirectoryList 4");
        // Reversed order sorting
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, false);
        when(listMock.getSelectedIndex()).thenReturn(-1);

        reset(modelMock);
        ArgumentCaptor<String> stringCaptor=ArgumentCaptor.forClass(String.class);
        when(fileMock.listFiles()).thenReturn(fileList1);
        boolean result=instance.updateDirectoryList(".fit");
        assertTrue(result);
        result=instance.updateListModel();
        assertFalse(result);
        verify(modelMock, times(2)).addElement(stringCaptor.capture());
        assertEquals("testfile2.fit", stringCaptor.getAllValues().get(0));
        assertEquals("testfile1.fit", stringCaptor.getAllValues().get(1));

        reset(modelMock);
        stringCaptor=ArgumentCaptor.forClass(String.class);
        when(fileMock.listFiles()).thenReturn(fileList2);
        result=instance.updateDirectoryList(".fit");
        assertTrue(result);
        result=instance.updateListModel();
        assertFalse(result);
        verify(modelMock, times(2)).addElement(stringCaptor.capture());
        assertEquals("testfile3.fit", stringCaptor.getAllValues().get(0));
        assertEquals("testfile1.fit", stringCaptor.getAllValues().get(1));
    }
    
    /**
     * Test of updateDirectoryList method, of class DirectoryList.
     */
    @Test
    public void testUpdateDirectoryList5()
    {
        System.out.println("updateDirectoryList 5");
        // Reversed order sorting
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, false);

        reset(modelMock);
        ArgumentCaptor<String> stringCaptor=ArgumentCaptor.forClass(String.class);
        when(listMock.getSelectedIndex()).thenReturn(-1);
        when(fileMock.listFiles()).thenReturn(fileList1);
        boolean result=instance.updateDirectoryList(".fit");
        assertTrue(result);
        result=instance.updateListModel();
        assertFalse(result);
        verify(modelMock, times(2)).addElement(stringCaptor.capture());
        assertEquals("testfile2.fit", stringCaptor.getAllValues().get(0));
        assertEquals("testfile1.fit", stringCaptor.getAllValues().get(1));
        
        // Now present empty list in a listbox that has row 0 selected
        reset(modelMock);
        stringCaptor=ArgumentCaptor.forClass(String.class);
        when(listMock.getSelectedIndex()).thenReturn(0);
        when(fileMock.listFiles()).thenReturn(fileList3);
        when(modelMock.size()).thenReturn(0);
        result=instance.updateDirectoryList(".fit");
        assertTrue(result);
        result=instance.updateListModel();
        assertTrue(result);
        verify(modelMock, times(0)).addElement(stringCaptor.capture());
    }    
        
    /**
     * Test of clear method, of class DirectoryList.
     */
    @Test
    public void testClear()
    {
        System.out.println("clear");
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, true);
        reset(modelMock);
        instance.clear();
        verify(modelMock, times(1)).clear();
    }

    /**
     * Test of setSelectedIndex method, of class DirectoryList.
     */
    @Test
    public void testSetSelectedIndex()
    {
        System.out.println("setSelectedIndex");
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, true);
        reset(listMock);
        instance.setSelectedIndex(5);
        verify(listMock, times(1)).setSelectedIndex(5);
    }

    /**
     * Test of clearSelection method, of class DirectoryList.
     */
    @Test
    public void testClearSelection()
    {
        System.out.println("clearSelection");
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, true);
        reset(listMock);
        instance.clearSelection();
        verify(listMock, times(1)).clearSelection();
    }

    /**
     * Test of hasSelection method, of class DirectoryList.
     */
    @Test
    public void testHasSelection()
    {
        System.out.println("hasSelection");
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, true);
        reset(listMock);
        when(listMock.getSelectedIndex()).thenReturn(-1);
        assertFalse(instance.hasSelection());
        when(listMock.getSelectedIndex()).thenReturn(4);
        assertTrue(instance.hasSelection());
    }

    /**
     * Test of getSelectedFileName method, of class DirectoryList.
     */
    @Test
    public void testGetSelectedFileName()
    {
        System.out.println("getSelectedFileName");
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, true);
        reset(listMock);
        when(fileMock.listFiles()).thenReturn(fileList1);
        instance.updateDirectoryList(".fit");
        when(listMock.getSelectedIndex()).thenReturn(0);
        assertEquals("testfile1.fit", instance.getSelectedFileName());
        when(listMock.getSelectedIndex()).thenReturn(1);
        assertEquals("testfile2.fit", instance.getSelectedFileName());

        instance=new DirectoryList(fileMock, listMock, modelMock, false);
        reset(listMock);
        when(fileMock.listFiles()).thenReturn(fileList1);
        instance.updateDirectoryList(".fit");
        when(listMock.getSelectedIndex()).thenReturn(0);
        assertEquals("testfile2.fit", instance.getSelectedFileName());
        when(listMock.getSelectedIndex()).thenReturn(1);
        assertEquals("testfile1.fit", instance.getSelectedFileName());
    }

    /**
     * Test of getFileName method, of class DirectoryList.
     */
    @Test
    public void testGetFileName()
    {
        System.out.println("getFileName");
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, false);
        reset(listMock);
        when(fileMock.listFiles()).thenReturn(fileList1);
        instance.updateDirectoryList(".fit");
        assertEquals("testfile2.fit", instance.getFileName(0));
        assertEquals("testfile1.fit", instance.getFileName(1));

    }

    /**
     * Test of addTrack method, of class DirectoryList.
     */
    @Test
    public void testTrack_Track()
    {
        System.out.println("addTrack Track, getTrack");
        // Reversed order sorting
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, false);
        when(listMock.getSelectedIndex()).thenReturn(1);

        // Check if we can set and read back a cache item
        reset(modelMock);
        ArgumentCaptor<String> stringCaptor=ArgumentCaptor.forClass(String.class);
        when(fileMock.listFiles()).thenReturn(fileList1);
        when(listMock.getSelectedIndex()).thenReturn(-1);
        instance.updateDirectoryList(".fit");
        instance.updateListModel();
        // Item 0: testfile2.fit
        // Item 1: testfile1.fit
        Track t=new Track(0.0, 0);
        when(listMock.getSelectedIndex()).thenReturn(1);
        instance.addTrack(t);
        when(listMock.getSelectedIndex()).thenReturn(0);
        assertNull(instance.getTrack());
        when(listMock.getSelectedIndex()).thenReturn(1);
        assertEquals(t, instance.getTrack());

        // Now, check if the cache persists after an directory update
        reset(modelMock);
        stringCaptor=ArgumentCaptor.forClass(String.class);
        when(fileMock.listFiles()).thenReturn(fileList2);
        instance.updateDirectoryList(".fit");
        instance.updateListModel();
        verify(modelMock, times(2)).addElement(stringCaptor.capture());
        // Item 0: testfile3.fit
        // Item 1: testfile1.fit   
        when(listMock.getSelectedIndex()).thenReturn(0);
        assertNull(instance.getTrack());
        when(listMock.getSelectedIndex()).thenReturn(1);
        assertEquals(t, instance.getTrack());
    }

    /**
     * Test of addTrack method, of class DirectoryList.
     */
    @Test
    @org.junit.Ignore
    public void testAddTrack_Track_int()
    {
        System.out.println("addTrack Track int, getTrack");
        // Reversed order sorting
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, false);
        when(listMock.getSelectedIndex()).thenReturn(1);

        // Check if we can set and read back a cache item
        reset(modelMock);
        ArgumentCaptor<String> stringCaptor=ArgumentCaptor.forClass(String.class);
        when(fileMock.listFiles()).thenReturn(fileList1);
        when(listMock.getSelectedIndex()).thenReturn(-1);
        instance.updateDirectoryList(".fit");
        instance.updateListModel();
        // Item 0: testfile2.fit
        // Item 1: testfile1.fit
        Track t=new Track(0.0, 0);
        when(listMock.getSelectedIndex()).thenReturn(0);
        instance.addTrack(t, 1);
        when(listMock.getSelectedIndex()).thenReturn(0);
        assertNull(instance.getTrack());
        when(listMock.getSelectedIndex()).thenReturn(1);
        assertEquals(t, instance.getTrack());
    }

    /**
     * Test of getNextNonCache method, of class DirectoryList.
     */
    @Test
    public void testGetNextNonCache()
    {
        System.out.println("getNextNonCache");
        // Reversed order sorting
        DirectoryList instance=new DirectoryList(fileMock, listMock, modelMock, false);
        when(listMock.getSelectedIndex()).thenReturn(1);

        // Check if we can set and read back a cache item
        reset(modelMock);
        ArgumentCaptor<String> stringCaptor=ArgumentCaptor.forClass(String.class);
        when(fileMock.listFiles()).thenReturn(fileList1);
        when(listMock.getSelectedIndex()).thenReturn(-1);
        instance.updateDirectoryList(".fit");
        instance.updateListModel();
        // Item 0: testfile2.fit
        // Item 1: testfile1.fit
        assertEquals(0, instance.getNextNonCache());
        Track t=new Track(0.0, 0);
        instance.addTrack(t, 0);
        assertEquals(1, instance.getNextNonCache());
    }
   
}
