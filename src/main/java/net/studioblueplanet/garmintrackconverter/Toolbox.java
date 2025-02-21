/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

/**
 *
 * @author jorgen
 */
public class Toolbox
{
    /**
     * Converts seconds to the format 99h99'99"
     * @param intervalInSeconds Seconds to convert
     * @return The formatted string
     */
    public static String toTimeIntervalString(long intervalInSeconds)
    {
      String intervalString;
      long hours;
      long minutes;
      long seconds;

      hours   =intervalInSeconds/3600L;
      minutes =(intervalInSeconds-hours*3600)/60;
      seconds =(intervalInSeconds-hours*3600-minutes*60);

      intervalString=String.format("%dh%02d'%02d\"", hours, minutes, seconds);
      return intervalString;
    }   
}
