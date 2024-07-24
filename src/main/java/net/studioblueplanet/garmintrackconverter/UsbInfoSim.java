/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author jorgen
 */
public class UsbInfoSim implements UsbInfo
{
    private class Device
    {
        public int vendorId;
        public int productId;
    }
    
    private final static    Logger             LOGGER = LogManager.getLogger(UsbInfoSim.class);
    public static String USBSIMFILE="./usbsim.txt";
    
    private final List<Device> devices;
    
    public UsbInfoSim()
    {
        devices=new ArrayList<>();
        usbRefresh();
    }

    /**
     * Updates the list of devices attached to the USB Root hub
     */
    @Override
    public void usbRefresh()
    {
        Pattern pattern = Pattern.compile("^([0-9A-Fa-f]{4}):([0-9A-Fa-f]{4}).*");
        
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(USBSIMFILE));
            String line = reader.readLine();
            while (line != null) 
            {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches())
                {
                    Device device=new Device();
                    device.vendorId=Integer.parseInt(matcher.group(1), 16);
                    device.productId=Integer.parseInt(matcher.group(2), 16);
                    devices.add(device);
                }
                line = reader.readLine();
            }

            reader.close();            
        }
        catch (IOException e)
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
    @Override
    public boolean isUsbDeviceConnected(int vendorId, int productId)
    {
        boolean connected=false;
        for (Device device : devices)
        {
            int usbProductId  =device.productId;
            int usbVendorId   =device.vendorId;
            if (usbProductId==productId &&
                usbVendorId==vendorId)
            {
                connected=true;
                LOGGER.debug("USB SIM Device {} found", String.format("%04x:%04x", usbVendorId, usbProductId));
            }
        }
        return connected;
    }
}
