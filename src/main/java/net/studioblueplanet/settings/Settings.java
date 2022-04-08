/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
/**
 *
 * @author jorgen
 */
public class Settings
{
    @JsonProperty("debugLevel")
    public String               debugLevel;
    @JsonProperty("gpxFilePath")
    public String               gpxFilePath;
    @JsonProperty("devices")
    public List<SettingsDevice> devices;
}
