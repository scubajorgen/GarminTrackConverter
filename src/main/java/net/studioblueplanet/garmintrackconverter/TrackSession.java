/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

import java.time.ZonedDateTime;
import java.util.List;
import net.studioblueplanet.fitreader.FitGlobalProfile;
import net.studioblueplanet.fitreader.FitMessage;
import static net.studioblueplanet.garmintrackconverter.Track.KMH_PER_MS;
import static net.studioblueplanet.garmintrackconverter.Track.MS_PER_S;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author jorgen
 */
public class TrackSession
{
    private final static Logger             LOGGER      = LogManager.getLogger(TrackSession.class);

    // Info from session
    private String                          mode;               //
    private String                          sport;
    private String                          subSport;
    private ZonedDateTime                   startTime;
    private ZonedDateTime                   endTime;
    private Long                            elapsedTime;    // s
    private Long                            timedTime;      // s
    private Double                          startLat;       // degrees
    private Double                          startLon;       // degrees
    private Double                          distance;       // m
    private Double                          averageSpeed;   // km/h
    private Double                          maxSpeed;       // km/h
    private Integer                         ascent;         // m
    private Integer                         descent;        // m
    private Double                          grit;           // kGrit
    private Double                          flow;           // FLOW
    private Double                          calories;       // cal
    private Integer                         jumpCount;      //     
    
    private Integer                         avgHeartRate;           // bpm
    private Integer                         maxHeartRate;          
    private Double                          avgRespirationRate;     // breaths per minute
    private Double                          minRespirationRate;
    private Double                          maxRespirationRate;
    private Integer                         avgCadence;             // rpm or strids/min
    private Integer                         maxCadence;
    private Integer                         avgPower;               // Watt
    private Integer                         maxPower;
    private Integer                         avgTemperature;         // deg C
    private Integer                         minTemperature;
    private Integer                         maxTemperature;
    private Double                          avgStrokeDistance;
    private Integer                         totalCycles;
    private Double                          totalAerobicTrainingEffect;
    private Double                          totalAnaerobicTrainingEffect;
    private Double                          exerciseLoad;
    
    
    
    /**
     * Create empty session. Used for testing.
     */
    public TrackSession()
    {
    }
    
    /**
     * Return the int value if the field indicated exists
     * @param message Fit message
     * @param record  Record index
     * @param field Field tag
     * @return The integer value fo the
     */
    private Integer getIntIfFieldExists(FitMessage message, int record, String field, int min, int max)
    {
        Integer value=null;
        if (message.hasField(field))
        {
            value   =(int)message.getIntValue(record, field);
            if (value<min || value>max)
            {
                value=null;
            }
        }
        return value;
    }
    
    /**
     * Return the int value if the field indicated exists
     * @param message Fit message
     * @param record  Record index
     * @param field Field tag
     * @return The integer value fo the
     */
    private Double getScaledIfFieldExists(FitMessage message, int record, String field, double min, double max)
    {
        Double value=null;
        if (message.hasField(field))
        {
            value   =message.getScaledValue(record, field);
            if (value<min || value>max)
            {
                value=null;
            }
        }
        return value;
    }
    
    /**
     * This method parses the FIT session record and distils the number of sessions.
     * @param sessionMessages The FIT record holding the 'session' info
     */
    public TrackSession(List<FitMessage> sessionMessages)
    {
        int                     size;
        int                     id;
        
        for (FitMessage message:sessionMessages)
        {
            size                    =message.getNumberOfRecords();
            for(int i=0; i<size; i++)
            {
                endTime             =message.getTimeValue(i, "timestamp");
                startTime           =message.getTimeValue(i, "start_time");
                elapsedTime         =message.getIntValue(i, "total_elapsed_time")/MS_PER_S;
                timedTime           =message.getIntValue(i, "total_timer_time")/MS_PER_S;

                startLat            =message.getLatLonValue(i, "start_position_lat");
                startLon            =message.getLatLonValue(i, "start_position_long");
                
                distance            =message.getScaledValue(i, "total_distance");
                
                // We've seen an anomaly on the GPSMAP 67 on a 12 hours recording
                // The starttime was incorrect and 4 hours before the endtime i.s.o 12 hours
                
                if (message.hasField("enhanced_avg_speed"))
                {
                    averageSpeed    =message.getScaledValue(i, "enhanced_avg_speed")*KMH_PER_MS;
                }
                else if (message.hasField("avg_speed"))
                {
                    averageSpeed    =message.getScaledValue(i, "avg_speed")*KMH_PER_MS;
                }
                if (message.hasField("enhanced_max_speed"))
                {
                    maxSpeed        =message.getScaledValue(i, "enhanced_max_speed")*KMH_PER_MS;
                }
                else if (message.hasField("max_speed"))
                {
                    maxSpeed        =message.getScaledValue(i, "max_speed")*KMH_PER_MS;
                }
                
                grit                =message.getFloatValue(i, "total_grit");
                flow                =message.getFloatValue(i, "avg_flow");
                jumpCount           =getIntIfFieldExists(message, i, "jump_count", 0, 0xfffe);
                calories            =message.getScaledValue(i, "total_calories");

                ascent              =getIntIfFieldExists(message, i, "total_ascent", 0, 0xfffe);
                descent             =getIntIfFieldExists(message, i, "total_descent", 0, 0xfffe);
                mode                =message.getStringValue(i, "mode");
                
                minTemperature              =getIntIfFieldExists(message, i, "min_temperature", -126, 126);
                avgTemperature              =getIntIfFieldExists(message, i, "avg_temperature", -126, 126);
                maxTemperature              =getIntIfFieldExists(message, i, "max_temperature", -126, 126);
                
                avgHeartRate                =getIntIfFieldExists(message, i, "avg_heart_rate", 0, 254);
                maxHeartRate                =getIntIfFieldExists(message, i, "max_heart_rate", 0, 254);
                
                minRespirationRate          =getScaledIfFieldExists(message, i, "enhanced_min_respiration_rate", 0.0, 655.0);
                avgRespirationRate          =getScaledIfFieldExists(message, i, "enhanced_avg_respiration_rate", 0.0, 655.0);
                maxRespirationRate          =getScaledIfFieldExists(message, i, "enhanced_max_respiration_rate", 0.0, 655.0);
                avgCadence                  =getIntIfFieldExists(message, i, "avg_cadence", 0, 254);
                maxCadence                  =getIntIfFieldExists(message, i, "max_cadence", 0, 254);
                avgPower                    =getIntIfFieldExists(message, i, "avg_power", 0,0xfffe);
                maxPower                    =getIntIfFieldExists(message, i, "max_power", 0, 0xfffe);
                avgStrokeDistance           =getScaledIfFieldExists(message, i, "avg_stroke_distance", 0.0, 655.0);
                totalCycles                 =getIntIfFieldExists(message, i, "total_cycles", 0, 0x7ffffffe);
                totalAerobicTrainingEffect  =getScaledIfFieldExists(message, i, "total_training_effect", 0.0, 655.0);
                totalAnaerobicTrainingEffect=getScaledIfFieldExists(message, i, "total_anaerobic_training_effect", 0.0, 655.0);
                exerciseLoad                =getScaledIfFieldExists(message, i, "training_load_peak", 0.0, 655.0);
                
                id=(int)message.getIntValue(0, "sport");
                sport=FitGlobalProfile.getInstance().getTypeValueName("sport", id);
                id=(int)message.getIntValue(0, "sub_sport");
                subSport=FitGlobalProfile.getInstance().getTypeValueName("sub_sport", id);
                
                if (startTime!=null && endTime!=null)
                {
                    LOGGER.info("SESSION        : {}", message.getIntValue(i, "message_index"));
                    LOGGER.info("Time           : {}-{}", startTime.toString(), endTime.toString());
                    LOGGER.info("Duration       : {}/{} sec", elapsedTime, timedTime);
                    LOGGER.info("Distance       : {} km", distance);
                    LOGGER.info("Speed          : average {}, max {} km/h", averageSpeed, maxSpeed);
                    LOGGER.info("Ascent/Descent : {}/{} m", ascent, descent);
                    LOGGER.info("Sport          : {} - {}", sport, subSport);
                    LOGGER.info("Mode           : {}", mode);
                    LOGGER.info("Temperature    : min {} avg {} max {} C", minTemperature, avgTemperature, maxTemperature);
                    LOGGER.info("Heart rate     : avg {} max {} bpm", avgHeartRate, maxHeartRate);
                    LOGGER.info("Resp. rate     : min {} avg {} max {} pm", minRespirationRate, avgRespirationRate, maxRespirationRate);
                    LOGGER.info("Power          : avg {} max {} Watt", avgPower, maxPower);
                    LOGGER.info("Cadence        : avg {} max {} rpm", avgCadence, maxCadence);
                    LOGGER.info("Avg Stroke dist: {} m", avgStrokeDistance);
                    LOGGER.info("Total Cycles   : {}", totalCycles);
                    LOGGER.info("TrainingEffect : aerobic {} anaerobic {}", totalAerobicTrainingEffect, totalAnaerobicTrainingEffect);
                    LOGGER.info("Exercise Load  : {}", exerciseLoad);
                    
                    if ("OFF ROAD".equals(mode))
                    {
                        LOGGER.info("Grit           : {} kGrit", grit);
                        LOGGER.info("Flow           : {}", flow);
                        LOGGER.info("Jumps          : {}", jumpCount);
                    }
                    LOGGER.info("Calories       : {} cal", calories);
                }
                else
                {
                    LOGGER.error("Session does not contain start and end time");
                }
            }   
        }
    }

    public static Logger getLOGGER()
    {
        return LOGGER;
    }

    public String getMode()
    {
        return mode;
    }

    public String getSport()
    {
        return sport;
    }

    public String getSubSport()
    {
        return subSport;
    }

    public ZonedDateTime getStartTime()
    {
        return startTime;
    }

    public ZonedDateTime getEndTime()
    {
        return endTime;
    }

    public Long getElapsedTime()
    {
        return elapsedTime;
    }

    public Long getTimedTime()
    {
        return timedTime;
    }

    public Double getStartLat()
    {
        return startLat;
    }

    public Double getStartLon()
    {
        return startLon;
    }

    public Double getDistance()
    {
        return distance;
    }

    public Double getAverageSpeed()
    {
        return averageSpeed;
    }

    public Double getMaxSpeed()
    {
        return maxSpeed;
    }

    public Integer getAscent()
    {
        return ascent;
    }

    public Integer getDescent()
    {
        return descent;
    }

    public Double getGrit()
    {
        return grit;
    }

    public Double getFlow()
    {
        return flow;
    }

    public Double getCalories()
    {
        return calories;
    }

    public Integer getJumpCount()
    {
        return jumpCount;
    }

    public void setSport(String sport)
    {
        this.sport = sport;
    }

    public Integer getAvgHeartRate()
    {
        return avgHeartRate;
    }

    public Integer getMaxHeartRate()
    {
        return maxHeartRate;
    }

    public Double getAvgRespirationRate()
    {
        return avgRespirationRate;
    }

    public Double getMinRespirationRate()
    {
        return minRespirationRate;
    }

    public Double getMaxRespirationRate()
    {
        return maxRespirationRate;
    }

    public Integer getAvgCadence()
    {
        return avgCadence;
    }

    public Integer getMaxCadence()
    {
        return maxCadence;
    }

    public Integer getAvgPower()
    {
        return avgPower;
    }

    public Integer getMaxPower()
    {
        return maxPower;
    }

    public Integer getAvgTemperature()
    {
        return avgTemperature;
    }

    public Integer getMinTemperature()
    {
        return minTemperature;
    }

    public Integer getMaxTemperature()
    {
        return maxTemperature;
    }

    public Double getAvgStrokeDistance()
    {
        return avgStrokeDistance;
    }

    public Integer getTotalCycles()
    {
        return totalCycles;
    }

    public Double getTotalAerobicTrainingEffect()
    {
        return totalAerobicTrainingEffect;
    }

    public Double getTotalAnaerobicTrainingEffect()
    {
        return totalAnaerobicTrainingEffect;
    }

    public Double getExerciseLoad()
    {
        return exerciseLoad;
    }
}
