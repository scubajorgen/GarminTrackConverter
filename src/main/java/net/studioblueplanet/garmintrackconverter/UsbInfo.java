/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides information on whether a particular device is connected
 * to USB. First it enumerates all USB devices attached, then you can use
 * this list to search for a particular vendor/product ID
 * @author jorgen
 */
public class UsbInfo
{
    private final static    Logger             LOGGER = LogManager.getLogger(UsbInfo.class);
    private List<UsbDevice> theDevices;
    
    /**
     * Constructor. Reads the current list of devices from the USB Root Hub.
     */
    public UsbInfo()
    {
        usbRefresh();
    }

    /**
     * Enumerate the devices attached to the hub. If a device is a USB Hub, 
     * the method is used recursively to find the devices attached to the new
     * hub.
     * @param hub The hub
     * @return List of USB devices
     */
    private List<UsbDevice> listUsbDevices(UsbHub hub)
    {
        List<UsbDevice> devices=new ArrayList<>();
        //List all the USBs attached
        List perepheriques = hub.getAttachedUsbDevices();
        Iterator iterator = perepheriques.iterator();

        while (iterator.hasNext()) 
        {

          UsbDevice perepherique = (UsbDevice) iterator.next();

          if (perepherique.isUsbHub())
          {
            List<UsbDevice> subDevices=listUsbDevices((UsbHub) perepherique);
            devices.addAll(subDevices);
          }
          else
          {
              devices.add(perepherique);
          }
        }
        return devices;
    }
    
    private void dumpDevices()
    {
        for (UsbDevice usbDevice : theDevices)
        {
            short usbProductId=usbDevice.getUsbDeviceDescriptor().idProduct();
            short usbVendorId=usbDevice.getUsbDeviceDescriptor().idVendor();
            LOGGER.debug("USB Device {}", String.format("%04x:%04x", usbVendorId, usbProductId));            
        }
    }
    
    /**
     * Updates the list of devices attached to the USB Root hub
     */
    public void usbRefresh()
    {
        try
        {
            //Get UsbHub
            UsbServices services = UsbHostManager.getUsbServices();
            UsbHub root = services.getRootUsbHub();
            theDevices=listUsbDevices(root);  
            dumpDevices();
        }
        catch (UsbException e)
        {
            LOGGER.error("USB Error: {}", e.getMessage());            
        }
    }
    
    
    /**
     * Checks if the device is connected to the USB. Use usbRefresh() to get 
     * the current state from the USB port.
     * @param vendorId Manufacturer ID of the USB device
     * @param productId Device ID of the USB device
     * @return True if found, false if not
     */
    public boolean isUsbDeviceConnected(int vendorId, int productId)
    {
        boolean connected=false;
        for (UsbDevice usbDevice : theDevices)
        {
            short usbProductId=usbDevice.getUsbDeviceDescriptor().idProduct();
            short usbVendorId=usbDevice.getUsbDeviceDescriptor().idVendor();
            if (usbProductId==productId &&
                usbVendorId==vendorId)
            {
                connected=true;
                LOGGER.debug("USB Device {} found", String.format("%04x:%04x", usbVendorId, usbProductId));
            }
        }
        return connected;
    }
}
