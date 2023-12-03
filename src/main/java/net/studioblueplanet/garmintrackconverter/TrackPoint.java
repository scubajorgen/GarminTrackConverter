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
public static class TrackPointBuilder
{
    private ZonedDateTime       dateTime;
    private Double              latitude;         // degree
    private Double              longitude;        // degree
    private Double              elevation;        // m
    private Double              speed;            // m/s
    private Double              distance;         // m
    private Integer             temperature;      // deg C
    private Integer             heartrate;        // bpm
    private Double              respirationrate;  // breaths/min
    private Integer             ehpe;             // cm
    private Integer             stamina;          // Garmin Stamina in %
    private Integer             staminaPotential; // Garmin Stamina in %
    private Integer             unknown;

    private Integer             gpsAccuracy;      // cm - used for smoothing
    
    public TrackPointBuilder(double lat, double lon)
    {
        this.latitude=lat;
        this.longitude=lon;
    }
    
    public TrackPointBuilder dateTime(ZonedDateTime dateTime)
    {
        this.dateTime=dateTime;
        return this;
    }
    
    public TrackPointBuilder elevation(Double elevation)
    {
        this.elevation=elevation;
        return this;
    }

    public TrackPointBuilder temperature(int temperature)
    {
        this.temperature=temperature;
        return this;
    }

    public TrackPointBuilder heartrate(int heartrate)
    {
        this.heartrate=heartrate;
        return this;
    }

    public TrackPointBuilder respirationrate(double respirationrate)
    {
        this.respirationrate=respirationrate;
        return this;
    }

    public TrackPointBuilder distance(Double distance)
    {
        this.distance=distance;
        return this;
    }

    public TrackPointBuilder speed(Double speed)
    {
        this.speed=speed;
        return this;
    }

    public TrackPointBuilder gpsAccuracy(Integer gpsAccuracy)
    {
        this.gpsAccuracy=gpsAccuracy;
        return this;
    }

    public TrackPointBuilder ehpe(Integer ehpe)
    {
        this.ehpe=ehpe;
        return this;
    }

    public TrackPointBuilder stamina(Integer stamina)
    {
        this.stamina=stamina;
        return this;
    }

    public TrackPointBuilder staminaPotential(Integer staminaPotential)
    {
        this.staminaPotential=staminaPotential;
        return this;
    }

    public TrackPointBuilder unknown(Integer unknown)
    {
        this.unknown=unknown;
        return this;
    }

    public TrackPoint build() 
    {
        return new TrackPoint(this);
    }
}

    private final static Logger LOGGER = LogManager.getLogger(TrackPoint.class);
    private ZonedDateTime       dateTime;
    private Double              latitude;         // degree
    private Double              longitude;        // degree
    private Double              elevation;        // m
    private Double              speed;            // m/s
    private Double              distance;         // m
    private Integer             temperature;      // deg C
    private Integer             heartrate;        // bpm
    private Double              respirationrate;  // breaths/min
    private Integer             ehpe;             // cm
    private Integer             stamina;          // Garmin Stamina in %
    private Integer             staminaPotential; // Garmin Stamina in %
    private Integer             gpsAccuracy;      // cm - used for smoothing
    private Integer             unknown;          // to be found out what it is...
    
    private TrackPoint()
    {
        
    }
    
    private TrackPoint(TrackPointBuilder b)
    {
        this.dateTime           =b.dateTime;
        this.latitude           =b.latitude;
        this.longitude          =b.longitude;
        this.elevation          =b.elevation;
        this.speed              =b.speed;
        this.distance           =b.distance;
        this.temperature        =b.temperature;
        this.heartrate          =b.heartrate;
        this.respirationrate    =b.respirationrate;
        this.ehpe               =b.ehpe;
        this.gpsAccuracy        =b.gpsAccuracy;
        this.stamina            =b.stamina;
        this.staminaPotential   =b.staminaPotential;
        this.unknown            =b.unknown;
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
        TrackPoint point        =new TrackPoint();
        point.dateTime          =dateTime;                // TO DO: make it a clone of dateTime
        point.latitude          =latitude;
        point.longitude         =longitude;
        point.elevation         =elevation;
        point.distance          =distance;
        point.heartrate         =heartrate;
        point.respirationrate   =respirationrate;
        point.speed             =speed;
        point.temperature       =temperature;
        point.ehpe              =ehpe;
        point.gpsAccuracy       =gpsAccuracy;
        point.stamina           =stamina;
        point.staminaPotential  =staminaPotential;
        point.unknown           =unknown;
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
    
    public Double getRespirationrate()
    {
        return respirationrate;
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
    
    public Integer getStamina()
    {
        return stamina;
    }
    
    public Integer getStaminaPotential()
    {
        return staminaPotential;
    }
    
    public Integer getUnknown()
    {
        return unknown;
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
