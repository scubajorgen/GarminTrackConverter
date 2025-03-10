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
    
    
    /**
     * Create empty session. Used for testing.
     */
    public TrackSession()
    {
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
            size            =message.getNumberOfRecords();
            for(int i=0; i<size; i++)
            {
                endTime     =message.getTimeValue(i, "timestamp");
                startTime   =message.getTimeValue(i, "start_time");
                elapsedTime =message.getIntValue(i, "total_elapsed_time")/MS_PER_S;
                timedTime   =message.getIntValue(i, "total_timer_time")/MS_PER_S;

                startLat    =message.getLatLonValue(i, "start_position_lat");
                startLon    =message.getLatLonValue(i, "start_position_long");
                
                distance    =message.getScaledValue(i, "total_distance");
                
                // We've seen an anomaly on the GPSMAP 67 on a 12 hours recording
                // The starttime was incorrect and 4 hours before the endtime i.s.o 12 hours
                
                if (message.hasField("enhanced_avg_speed"))
                {
                    averageSpeed=message.getScaledValue(i, "enhanced_avg_speed")*KMH_PER_MS;
                }
                else if (message.hasField("avg_speed"))
                {
                    averageSpeed=message.getScaledValue(i, "avg_speed")*KMH_PER_MS;
                }
                if (message.hasField("enhanced_max_speed"))
                {
                    maxSpeed    =message.getScaledValue(i, "enhanced_max_speed")*KMH_PER_MS;
                }
                else if (message.hasField("max_speed"))
                {
                    maxSpeed    =message.getScaledValue(i, "max_speed")*KMH_PER_MS;
                }
                grit        =message.getFloatValue(i, "total_grit");
                flow        =message.getFloatValue(i, "avg_flow");
                if (message.hasField("jump_count"))
                {
                    jumpCount   =(int)message.getIntValue(i, "jump_count");
                }
                else
                {
                    jumpCount   =null;
                }
                calories    =message.getScaledValue(i, "total_calories");
                ascent      =(int)message.getIntValue(i, "total_ascent");
                if (ascent==0xffff)
                {
                    ascent=null;
                }
                descent     =(int)message.getIntValue(i, "total_descent");
                if (descent==0xffff)
                {
                    descent=null;
                }
                mode        =message.getStringValue(i, "mode");
                
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

}
