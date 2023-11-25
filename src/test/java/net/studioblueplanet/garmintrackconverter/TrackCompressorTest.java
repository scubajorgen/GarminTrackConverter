/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.time.ZonedDateTime;
import java.time.ZoneId;
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
public class TrackCompressorTest
{
    
    public TrackCompressorTest()
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

    /**
     * Test of dpAlgorithm method, of class DPUtil.
     */
    @Test
    public void testDpAlgorithm()
    {
        System.out.println("dpAlgorithm");
        
        // Three points example. Distance to segement is 22263.9 m
        List<TrackPoint> originPoints = new ArrayList<>();
        ZonedDateTime zdt = ZonedDateTime.of(2015, 11, 30, 23, 45, 59, 1234, ZoneId.of("UTC"));
        originPoints.add(new TrackPoint(zdt.plusSeconds(0), -0.1, 0.0,  0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds(1),  0.1, 0.1,  0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds(2), -0.1, 0.2,  0.0, 0.0, 0.0, 0,0,0,0));
        
        List<TrackPoint> result;
        // Point is not ommitted
        result = TrackCompressor.dpAlgorithm(originPoints, 22263.0);
        assertEquals(3, result.size());
        // Mid point is ommitted since the distance is lower than the dMax
        result = TrackCompressor.dpAlgorithm(originPoints, 22264.0);
        assertEquals(2, result.size());
    }

    /**
     * Test of dpAlgorithm method, of class DPUtil.
     */
    @Test
    public void testDpAlgorithm2()
    {
        System.out.println("dpAlgorithm 2");
        
        // A real life example
        List<TrackPoint> originPoints = new ArrayList<>();
        ZonedDateTime zdt = ZonedDateTime.of(2015, 11, 30, 23, 45, 59, 1234, ZoneId.of("UTC"));
        
        originPoints.add(new TrackPoint(zdt.plusSeconds( 0), 53.012544,   6.725102, 0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds( 1), 53.013256,   6.727270, 0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds( 2), 53.013863,   6.729256, 0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds( 3), 53.013552,   6.729753, 0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds( 4), 53.013241,   6.730250, 0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds( 5), 53.013615,   6.730530, 0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds( 6), 53.014080,   6.730753, 0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds( 7), 53.014669,   6.731129, 0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds( 8), 53.014935,   6.729826, 0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds( 9), 53.015219,   6.730103, 0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds(10), 53.015483,   6.730677, 0.0, 0.0, 0.0, 0,0,0,0));
        originPoints.add(new TrackPoint(zdt.plusSeconds(11), 53.015694,   6.731101, 0.0, 0.0, 0.0, 0,0,0,0));      
        
        List<TrackPoint> result;
        // 4 m acceptable error
        result = TrackCompressor.dpAlgorithm(originPoints, 4.0);
        assertEquals(7, result.size());
        assertEquals(originPoints.get( 0), result.get(0));
        assertEquals(originPoints.get( 2), result.get(1));
        assertEquals(originPoints.get( 4), result.get(2));
        assertEquals(originPoints.get( 7), result.get(3));
        assertEquals(originPoints.get( 8), result.get(4));
        assertEquals(originPoints.get( 9), result.get(5));
        assertEquals(originPoints.get(11), result.get(6));

        // 2.5 m acceptable error
        result = TrackCompressor.dpAlgorithm(originPoints, 2.5);
        assertEquals(9, result.size());
        assertEquals(originPoints.get( 0), result.get(0));
        assertEquals(originPoints.get( 2), result.get(1));
        assertEquals(originPoints.get( 4), result.get(2));
        assertEquals(originPoints.get( 5), result.get(3));
        assertEquals(originPoints.get( 6), result.get(4));
        assertEquals(originPoints.get( 7), result.get(5));
        assertEquals(originPoints.get( 8), result.get(6));
        assertEquals(originPoints.get( 9), result.get(7));
        assertEquals(originPoints.get(11), result.get(8));
        
        // 10 m acceptable error
        result = TrackCompressor.dpAlgorithm(originPoints, 10.0);
        assertEquals(6, result.size());
        assertEquals(originPoints.get( 0), result.get(0));
        assertEquals(originPoints.get( 2), result.get(1));
        assertEquals(originPoints.get( 4), result.get(2));
        assertEquals(originPoints.get( 7), result.get(3));
        assertEquals(originPoints.get( 8), result.get(4));
        assertEquals(originPoints.get(11), result.get(5));
    }

    /**
     * Test of distToSegment method, of class DPUtil.
     */
    @Test
    public void testDistToSegment()
    {
        System.out.println("distToSegment");

        TrackPoint pA = new TrackPoint(-0.1, 0.0);
        TrackPoint pB = new TrackPoint(-0.1, 0.1);
        TrackPoint pX = new TrackPoint(0.1,  0.2);

        System.out.println("Dist "+TrackCompressor.distToSegment(pA, pB, pX));
        System.out.println("Dist "+0.2*6378137.0/360.0*2*Math.PI);
        
        assertEquals(0.2*2*Math.PI*6378137.0/360.0, TrackCompressor.distToSegment(pA, pB, pX), 0.05);
    }

    /**
     * Test of geoDist method, of class DPUtil.
     */
    @Test
    //@org.junit.Ignore
    public void testGeoDist()
    {
        System.out.println("geoDist");
        TrackPoint pA = new TrackPoint(53.0, 6.5);
        TrackPoint pB = new TrackPoint(54.0, 7.5);
        assertEquals(129521.084, TrackCompressor.geoDist (pA, pB), 0.001);
    }

    /**
     * Test of geoDist method, of class DPUtil.
     */
    @Test
    //@org.junit.Ignore
    public void testGeoDist2()
    {
        System.out.println("geoDist2");
        TrackPoint pA = new TrackPoint(53.0, 6.5);
        TrackPoint pB = new TrackPoint(54.0, 7.5);
        assertEquals(129521.084, TrackCompressor.geoDist2(pA, pB), 0.001);
    }

    /**
     * Test of geoDist method, of class DPUtil.
     */
    @Test
    //@org.junit.Ignore
    public void testGeoDist3()
    {
        System.out.println("geoDist3");
        TrackPoint pA = new TrackPoint(53.0, 6.5);
        TrackPoint pB = new TrackPoint(54.0, 7.5);
        assertEquals(129521.084, TrackCompressor.geoDist3(pA, pB), 0.001);
    }
}
