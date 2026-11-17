/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author jorgen
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GarminConnectActivityType
{
    private int         typeId;
    private String      typeKey;
    private int         parentTypeId;

    public int getTypeId()
    {
        return typeId;
    }

    public void setTypeId(int typeId)
    {
        this.typeId = typeId;
    }

    public String getTypeKey()
    {
        return typeKey;
    }

    public void setTypeKey(String typeKey)
    {
        this.typeKey = typeKey;
    }

    public int getParentTypeId()
    {
        return parentTypeId;
    }

    public void setParentTypeId(int parentTypeId)
    {
        this.parentTypeId = parentTypeId;
    }

}
