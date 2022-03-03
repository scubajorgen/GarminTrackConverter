/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the device as defined in the Garmin device XML fil.
 * @author jorgen
 */
public class Device
{
    private final static Logger LOGGER = LogManager.getLogger(Device.class);
    private String              id;
    private String              model;
    private String              description;
    
    public Device(String deviceFileName)
    {
        File                    xmlFile;
        Document                doc;
        DocumentBuilder         dBuilder;
        DocumentBuilderFactory  dbFactory;
        Element                 deviceElement;
        Element                 modelElement;
        Element                 idElement;
        Element                 descriptionElement;
        
	dbFactory = DocumentBuilderFactory.newInstance();
	
        try
        {
            xmlFile=new File(deviceFileName);

            dBuilder            = dbFactory.newDocumentBuilder();
            doc                 = dBuilder.parse(xmlFile);
            idElement           =(Element)doc.getElementsByTagName("Id").item(0);
            id                  =idElement.getTextContent();
            modelElement        =(Element)doc.getElementsByTagName("Model").item(0);
            model               =modelElement.getTextContent();
            descriptionElement  =(Element)modelElement.getElementsByTagName("Description").item(0);
            description         =descriptionElement.getTextContent();
            LOGGER.info("Found ID: {}, Model: {}, Description: {}", id, model, description);
        }
        catch(Exception e)
        {
            LOGGER.error("Error parsing device file");
            id="unknown";
            model="unknown";
            description="unknown";
        }
    }

    /**
     * Return a characterisation of the device
     * @return Description
     */
    public String getDeviceDescription()
    {
        return description+" - "+id;
    }

    /**
     * Returns the ID of the device
     * @return The ID
     */
    public String getId()
    {
        return id;
    }

    /**
     * Returns the device model
     * @return The model
     */
    public String getModel()
    {
        return model;
    }

    /**
     * Returns the description of the device
     * @return The description as String
     */
    public String getDescription()
    {
        return description;
    }


}
