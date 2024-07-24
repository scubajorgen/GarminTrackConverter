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
public interface UsbInfo
{
    /**
     * Updates the list of devices attached to the USB Root hub
     */
    public void usbRefresh();
            
    /**
     * Checks if the device is connected to the USB. Use usbRefresh() to get 
     * the current state from the USB port.
     * @param vendorId Manufacturer ID of the USB device
     * @param productId Device ID of the USB device
     * @return True if found, false if not
     */
    public boolean isUsbDeviceConnected(int vendorId, int productId);            
}
