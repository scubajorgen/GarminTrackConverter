/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;


import hirondelle.date4j.DateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents a track point.
 * @author Jorgen
 */
public class TrackPoint
{
  private final static Logger   LOGGER = LogManager.getLogger(TrackPoint.class);
  private final DateTime        dateTime;
  private final double          latitude;
  private final double          longitude;
  private final double          elevation;
  private final double          speed;
  private final double          distance;
  private final int             temperature;
  
  /**
   * Constructor, simple version
   * @param lat Latitude
   * @param lon Longitude
   */
  public TrackPoint(double lat, double lon)
  {
    this.dateTime	=null;
    this.latitude       =lat;
    this.longitude      =lon;
    this.elevation      =0;
    this.speed          =0;
    this.distance       =0;
    this.temperature    =0;     
  }
  
  /**
   * Constructor, full version
   * @param dateTime Datetime of the point
   * @param lat Latitude
   * @param lon Longitude
   * @param ele Elevation
   * @param speed Speed
   * @param distance Distance
   * @param temp Temperature
   */
  public TrackPoint(DateTime dateTime, double lat, double lon, double ele, double speed, double distance, int temp)
  {
    this.dateTime	=dateTime;
    this.latitude       =lat;
    this.longitude      =lon;
    this.elevation      =ele;
    this.speed          =speed;
    this.distance       =distance;
    this.temperature    =temp;
  }
  
  public DateTime getDateTime()
  {
    return dateTime;
  }
  
  public double getLatitude()
  {
      return latitude;
  }
  
  public double getLongitude()
  {
      return longitude; 
  }
  
  public double getElevation()
  {
      return this.elevation;
  }
  
  public int getTemperature()
  {
      return this.temperature;
  }
  
  public double getSpeed()
  {
      return speed;
  }
  
  public double getDistance()
  {
      return distance;
  }
  

}
