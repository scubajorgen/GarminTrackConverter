/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class represents the map function, showing a map with a route.
 * @author jorgen
 */
public abstract class Map 
{
    protected final JPanel              panel;
    protected final JLabel              label;
    protected final int                 panelWidth;
    protected final int                 panelHeight;

    /**
     * Constructor. Initializes variables.
     * @param panel Panel to add the map to.
     */
    public Map(JPanel panel)
    {
        this.panel  =panel;
        label       = new JLabel();
        panel.add(label);     
        panelWidth  =panel.getWidth();
        panelHeight =panel.getHeight();
    }
    
   /**
     * This method show the track in this frame on a map
     * @param activity The activity data structure containing the track (Activity) to show
     * @param fitTrack Indicates whether or not to zoom to the track
     * @return A string indicating the result of the showing (ToDo: remove or make sensible value).
     */
    public abstract String showTrack(Track activity, boolean fitTrack);

    /**
     * This method shows waypoints on a map
     * @param waypoints List of waypoints
     * @param fitWaypoints Indicates whether to fit the waypoints or keep existing pan/zoom
     * @return A string indicating the result of the showing (ToDo: remove or make sensible value).
     */
    public abstract String showWaypoints(List<Location> waypoints, boolean fitWaypoints);

    /**
     * Hides the track
     * @param unzoom If true, zoom out; otherwise keep zoomlevel
     */
    public abstract void hideTrack(boolean unzoom);
    
}
