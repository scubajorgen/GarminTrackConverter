/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author jorgen
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GarminConnectActivity 
{
    private Long                        activityId;
    private String                      activityName;
    private String                      description;
    private String                      startTimeLocal;
    @JsonProperty("startTimeGMT")
    private String                      startTimeGmt;
    private GarminConnectActivityType   activityType;
    private Double                      elapsedDuration; // Duration of activity in s

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    public String getActivityName()
    {
        return activityName;
    }

    public void setActivityName(String activityName)
    {
        this.activityName = activityName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getStartTimeLocal()
    {
        return startTimeLocal;
    }

    public void setStartTimeLocal(String startTimeLocal)
    {
        this.startTimeLocal = startTimeLocal;
    }

    public String getStartTimeGmt()
    {
        return startTimeGmt;
    }

    public void setStartTimeGmt(String startTimeGmt)
    {
        this.startTimeGmt = startTimeGmt;
    }

    public GarminConnectActivityType getActivityType()
    {
        return activityType;
    }

    public void setActivityType(GarminConnectActivityType activityType)
    {
        this.activityType = activityType;
    }

    public Double getElapsedDuration()
    {
        return elapsedDuration;
    }

    public void setElapsedDuration(Double elapsedDuration)
    {
        this.elapsedDuration = elapsedDuration;
    }
}
