/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
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
public class ToolboxTest
{
    
    public ToolboxTest()
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
    }
    
    @After
    public void tearDown()
    {
    }

    @Test
    public void testToTimeIntervalString()
    {
        String result=Toolbox.toTimeIntervalString(0);
        assertEquals("0h00'00\"", result);
        
        result=Toolbox.toTimeIntervalString(3661);
        assertEquals("1h01'01\"", result);

        result=Toolbox.toTimeIntervalString(7871);
        assertEquals("2h11'11\"", result);
    }
    
}
