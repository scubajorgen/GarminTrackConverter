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
public class TrackPoint
{
    private final static Logger   LOGGER = LogManager.getLogger(TrackPoint.class);
    private final ZonedDateTime   dateTime;
    private final Double          latitude;     // degree
    private final Double          longitude;    // degree
    private final Double          elevation;    // m
    private final Double          speed;        // m/s
    private final Double          distance;     // m
    private final Integer         temperature;  // deg C
    private final Integer         heartrate;    // bpm
    private final Integer         gpsAccuracy;  // cm
  
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
     * @param gpsAccuracy Accuracy of gps in m
     */
    public TrackPoint(ZonedDateTime dateTime, Double lat, Double lon, Double ele, Double speed, Double distance, Integer temp, Integer heartrate, Integer gpsAccuracy)
    {
        this.dateTime       =dateTime;
        this.latitude       =lat;
        this.longitude      =lon;
        this.elevation      =ele;
        this.speed          =speed;
        this.distance       =distance;
        this.temperature    =temp;
        this.heartrate      =heartrate;
        this.gpsAccuracy    =gpsAccuracy;
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

    public Double getDistance()
    {
        return distance;
    }

    public Integer getHeartrate()
    {
        return heartrate;
    }
    
    public Integer getGpsAccuracy()
    {
        return gpsAccuracy;
    }
}
