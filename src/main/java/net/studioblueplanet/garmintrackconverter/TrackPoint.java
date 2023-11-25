/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import java.time.ZonedDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents a track point.
 * @author Jorgen
 */
public class TrackPoint implements Comparable<TrackPoint>
{
    private final static Logger   LOGGER = LogManager.getLogger(TrackPoint.class);
    private final ZonedDateTime   dateTime;
    private Double                latitude;         // degree
    private Double                longitude;        // degree
    private final Double          elevation;        // m
    private final Double          speed;            // m/s
    private final Double          distance;         // m
    private final Integer         temperature;      // deg C
    private final Integer         heartrate;        // bpm
    private final Integer         ehpe;             // cm

    private final Integer         gpsAccuracy;      // cm - used for smoothing
    
    /**
     * Constructor, simple version
     * @param lat Latitude
     * @param lon Longitude
     */
    public TrackPoint(double lat, double lon)
    {
        this.dateTime       =null;
        this.latitude       =lat;
        this.longitude      =lon;
        this.elevation      =null;
        this.speed          =null;
        this.distance       =null;
        this.temperature    =null;  
        this.heartrate      =null;
        this.ehpe           =null;
        this.gpsAccuracy    =null;
    }

    /**
     * Constructor, full version
     * @param dateTime Datetime of the point
     * @param lat Latitude in deg
     * @param lon Longitude in deg
     * @param ele Elevation in m
     * @param speed Speed in m/s
     * @param distance Distance in m
     * @param temp Temperature in degC
     * @param heartrate Heart rate in bpm
     * @param ehpe Accuracy of gps in cm
     * @param gpsAccuracy Accuracy of gps in cm, only used for smoothing
     */
    public TrackPoint(ZonedDateTime dateTime, Double lat, Double lon, Double ele, 
                      Double speed, Double distance, Integer temp, Integer heartrate, Integer ehpe,
                      Integer gpsAccuracy)
    {
        this.dateTime       =dateTime;
        this.latitude       =lat;
        this.longitude      =lon;
        this.elevation      =ele;
        this.speed          =speed;
        this.distance       =distance;
        this.temperature    =temp;
        this.heartrate      =heartrate;
        this.ehpe           =ehpe;
        this.gpsAccuracy    =gpsAccuracy;
    }

    /**
     * Update the coordinate
     * @param lat New latitude
     * @param lon New longitude
     */
    public void updateCoordinate(Double lat, Double lon)
    {
        this.latitude=lat;
        this.longitude=lon;
    }
    
    /**
     * Returns a clone of the track point
     * @return New instance of the track point
     */
    @Override
    public TrackPoint clone()
    {
        TrackPoint point=new TrackPoint(dateTime, latitude, longitude, elevation, speed, distance, 
                                        temperature, heartrate, ehpe, gpsAccuracy);
        return point;
    }
    
    
    public ZonedDateTime getDateTime()
    {
        return dateTime;
    }

  
    public Double getLatitude()
    {
        return latitude;
    }

    public Double getLongitude()
    {
        return longitude;
    }

    public Double getElevation()
    {
        return this.elevation;
    }

    public Integer getTemperature()
    {
        return this.temperature;
    }

    public Double getSpeed()
    {
        return speed;
    }
    
    public double getSpeedNotNull()
    {
        double speedValue;
        if (speed==null)
        {
            speedValue=0.0;
        }
        else
        {
            speedValue=speed.doubleValue();
        }
        return speedValue;
    }

    public Double getDistance()
    {
        return distance;
    }

    public Integer getHeartrate()
    {
        return heartrate;
    }
    
    public int getHeartrateNotNull()
    {
        int rate;
        if (heartrate==null)
        {
            rate=0;
        }
        else
        {
            rate=heartrate.intValue();
        }
        return rate;
    }
    
    public Integer getGpsAccuracy()
    {
        return gpsAccuracy;
    }
    
    public Integer getEhpe()
    {
        return ehpe;
    }
    
    /**
     * It has been encountered that the FIT file contains records with 
     * latitude and longitude equal to 0.0 or 180, 180
     * @return True if the point appears valid, false if it is an erronous point
     */
    public boolean isValid()
    {
        return (Math.round(latitude)!=180 && Math.round(longitude)!=180 && 
                latitude!=0.0 && longitude!=0.0);
    }
    
    @Override
    public int compareTo(TrackPoint other)
    {
        return dateTime.compareTo(other.dateTime);
    }     
}
