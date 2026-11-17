/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import net.studioblueplanet.settings.SettingsDevice;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for GarminConnect. GarminConnect connects to the Garmin Personal API
 * and requires an API key and valid activity IDs.
 * @author jorgen
 */

// TO RUN THE TEST: ENTER API KEY AND VALID ACTIVITY IDs AND REMOVE IGNORE
@org.junit.Ignore
public class GarminConnectTest
{
    private static GarminConnect instance;
    public GarminConnectTest()
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
        SettingsDevice settings=new SettingsDevice();
        settings.setApiKey("<<ENTER API KEY>>");
        settings.setLastDateTimeSynced("2026-11-14T00:00:00Z");
        settings.setTrackFilePath("./test");
        instance = new GarminConnect(settings);
    }
    
    @After
    public void tearDown()
    {
    }

    void purgeDirectory(File dir) 
    {
    for (File file: dir.listFiles()) 
    {
        if (file.isDirectory())
        {
            purgeDirectory(file);
        }
        file.delete();
    }
}
    
    
    /**
     * Test of getActivityList method, of class GarminConnect.
     */
    @Test
    public void testGetActivityList()
    {
        System.out.println("getActivityList");
        List<GarminConnectActivity> activities=instance.getActivityList();
        assertEquals(25087810221L, activities.get(0).getActivityId().longValue());
    }
    
    /**
     * Test of getActivityList method, of class GarminConnect.
     */
    @Test
    public void testGetAssumedFilename()
    {
        System.out.println("getAssumedFilename");
        GarminConnectActivity activity=new GarminConnectActivity();
        activity.setStartTimeLocal("2026-11-01 09:43:12");
        assertEquals("2026-11-01-09-43-12.fit", instance.getAssumedFilename(activity));
        
        activity.setStartTimeLocal("2026-11-07 11:21:17");
        assertEquals("2026-11-07-11-21-17.fit", instance.getAssumedFilename(activity));
    }
    
    /**
     * Test of downloadActivity method, of class GarminConnect.
     */
    @Test
    public void testDownloadActivity()
    {
        System.out.println("downloadActivity");
        boolean error=instance.downloadActivity("test/2026-11-15-14-20-00.fit", 25087810221L);
        assertFalse(error);
        
        // Invalid ID
        error=instance.downloadActivity("test/2026-11-15-14-20-00.fit", 25087810222L);
        assertTrue(error);
    }
    
    /**
     * Test of processActivities method, of class GarminConnect.
     */
    @Test
    public void testProcessAcvitities()
    {
        System.out.println("processActivities");

        purgeDirectory(new File("./test"));

        List<GarminConnectActivity> list=new ArrayList<>();
        GarminConnectActivity activity=new GarminConnectActivity();
        activity.setActivityId(24995821587L);
        activity.setStartTimeLocal("2026-11-07 11:21:17");
        list.add(activity);
        activity=new GarminConnectActivity();
        activity.setActivityId(24998315322L);
        activity.setStartTimeLocal("2026-11-07 13:47:58");
        list.add(activity);
        activity=new GarminConnectActivity();
        activity.setActivityId(25087810221L);
        activity.setStartTimeLocal("2026-11-15 14:20:00");
        list.add(activity);
        
        // Download the files
        int count=instance.processActivities(list);
        assertEquals(3, count);
        
        // Since the files have been downloaded, they are not downloaded again 
        count=instance.processActivities(list);
        assertEquals(0, count);
    }
}
