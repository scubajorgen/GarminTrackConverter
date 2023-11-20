/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation according to https://github.com/Bresiu/KalmanFilter
 * @author Jorgen
 */
public class TrackSmoother
{
    public class KalmanLatLong 
    {
        private final float MINACCURACY = 10;

        private long        timetamp;
        private double      lat;
        private double      lng;
        private float       variance; // P matrix.  Negative means object uninitialised.  NB: units irrelevant, as long as same units used throughout

        public KalmanLatLong() 
        { 
            variance = -1; 
        }

        public long getTimeStamp() 
        { 
            return timetamp; 
        }

        public double geLat() 
        { 
            return lat; 
        }

        public double getLng() 
        { 
            return lng; 
        }

        public float getAccuracy() 
        { 
            return (float)Math.sqrt(variance); 
        }

        public void setState(double lat, double lng, float accuracy, long timestamp) 
        {
            this.lat                    =lat; 
            this.lng                    =lng; 
            variance                    =accuracy * accuracy; 
            this.timetamp               =timestamp;
        }


        /**
         * Kalman filter processing for latitude and longitude. Takes new measurement
         * and updates the state.
         * @param lat_measurement New measurement of latitude
         * @param lng_measurement New measurement of longitude
         * @param speed Speed in m/s
         * @param accuracy Accuracy of the measurement in cm
         * @param timetamp Timestamp in ms
         */
        public void process(double lat_measurement, double lng_measurement, float speed, float accuracy, long timetamp) 
        {
            long TimeInc_milliseconds;
            
            if (accuracy < MINACCURACY) 
            {
                accuracy = MINACCURACY;
            }

            if (variance < 0) 
            {
                // if variance < 0, object is unitialised, so initialise with current values
                this.timetamp = timetamp;
                lat                         = lat_measurement; 
                lng                         = lng_measurement; 
                variance                    = accuracy*accuracy; 
            } 
            else 
            {
                // else apply Kalman filter methodology

                TimeInc_milliseconds = timetamp - this.timetamp;
                if (TimeInc_milliseconds > 0) 
                {
                    // time has moved on, so the uncertainty in the current position increases
                    variance += (float)TimeInc_milliseconds * speed * speed / 1000.0f;
                    this.timetamp = timetamp;
                }

                // Kalman gain matrix K = Covarariance * Inverse(Covariance + MeasurementVariance)
                // NB: because K is dimensionless, it doesn't matter that variance has different units to lat and lng
                float K = variance / (variance + accuracy * accuracy);
                // apply K
                lat += K * (lat_measurement - lat);
                lng += K * (lng_measurement - lng);
                // new Covarariance  matrix is (IdentityMatrix - K) * Covarariance 
                variance = (1 - K) * variance;
            }
        }
    }    
    
    
    private static          TrackSmoother              theInstance;
    
    /**
     * Private constructor
     */
    private TrackSmoother()
    {
    }


    /**
     * Returns the one and only instance of this class.
     * @return The instance of this class
     */
    public static TrackSmoother getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new TrackSmoother();
        }
        return theInstance;
    }
    
    /**
     * This method smooths a segment, by applying a Kalman filter
     * @param points Segment of points to filter
     * @return List of smoothed points
     */
    public List<TrackPoint> smoothSegment(List<TrackPoint> points)
    {
        KalmanLatLong               filter;
        double                      lat;
        double                      lon;
        int                         ehpe;
        double                      speed;
        long                        timeInMs;
        List<TrackPoint>            smoothed;
        
        smoothed=new ArrayList<>();
        filter=new KalmanLatLong();
        for (TrackPoint gpsRecord : points)
        {
            if (gpsRecord.isValid() && gpsRecord.getSpeed()!=null && gpsRecord.getGpsAccuracy()>0)
            {
                lat=gpsRecord.getLatitude();
                lon=gpsRecord.getLongitude();
                ehpe=gpsRecord.getGpsAccuracy();
                speed=gpsRecord.getSpeed();
                // The timestamp in ms. The timezone doesn't matter actually...
                timeInMs           =gpsRecord.getDateTime().toEpochSecond()*1000;

                // Do the filter.
                filter.process((float)lat, (float)lon, (float)speed ,(float)ehpe/100.0f, timeInMs);

                // Get the filtered value and replace the original coordinate with it
                lat=filter.geLat();
                lon=filter.getLng();
                TrackPoint smoothedPoint=gpsRecord.clone();
                smoothedPoint.updateCoordinate(lat, lon);
                smoothed.add(smoothedPoint);
            }
            else
            {
                // TO DO: decide whether to add the point or just skip it
            }
        }
        return smoothed;
    }
}
