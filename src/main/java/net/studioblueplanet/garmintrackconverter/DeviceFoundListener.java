/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

/**
 *
 * @author jorgen
 */
public interface DeviceFoundListener
{
    /**
     * Event listeren method, called when a new device has been found
     * @param e Associated event
     */
    public void deviceFound(DeviceFoundEvent e);
}
