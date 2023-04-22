/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import org.jdesktop.application.SingleFrameApplication;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.application.Application;

/**
 *
 * @author Jorgen
 */
public class GarminTrackConverter extends SingleFrameApplication 
{
    private final String        FONT1="net/studioblueplanet/garmintrackconverter/resources/Raleway-Regular.ttf";
    private final String        FONT2="net/studioblueplanet/garmintrackconverter/resources/DejaVuSansMono.ttf";    
    private final static Logger LOGGER = LogManager.getLogger(GarminTrackConverter.class);
    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() 
    {
        ConverterView       view;
        
        view=new ConverterView();
        view.setVisible(true);
        view.setIconImage(new ImageIcon(getClass().getResource("/net/studioblueplanet/garmintrackconverter/resources/icon48.png")).getImage());

        // Register new fonts and apply them to the UI
        loadFonts(); 
        view.setFont();
    }
    
    /**
     * This method loads and registers the fonts used by the UI. Fonts are
     * incorporated as resources in the application. By using incorporated fonts
     * the application will look the same independent of the Java platform
     */
    public void loadFonts() 
    {
        InputStream mainFontIn;
        Font    font;
        boolean registered;
        GraphicsEnvironment ge;
        
        font=null;
        try 
        {
            ge          = GraphicsEnvironment.getLocalGraphicsEnvironment();

            mainFontIn  = GarminTrackConverter.class.getClassLoader().getResourceAsStream(FONT1);
            font        = Font.createFont(Font.TRUETYPE_FONT, mainFontIn);
            registered              =ge.registerFont(font);
            if (registered)
            {
                LOGGER.info("Registered font "+FONT1+": "+font.getFontName());
            }

            mainFontIn  = GarminTrackConverter.class.getClassLoader().getResourceAsStream(FONT2);
            font        = Font.createFont(Font.TRUETYPE_FONT, mainFontIn);
            registered              =ge.registerFont(font);
            if (registered)
            {
                LOGGER.info("Registered font "+FONT2+": "+font.getFontName());
            }
	} 
        catch (IOException | FontFormatException e) 
        {
            LOGGER.error("Error loading font: "+e.getMessage());
            System.exit(-1);
        }
    }    

 
    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) 
    {
    }
    
    /**
     * Returns the application instance
     * @return The instance
     */
    public static GarminTrackConverter getApplication()
    {
        return Application.getInstance(GarminTrackConverter.class);
    }    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(GarminTrackConverter.class, args);
        // TODO code application logic here
    }
    
}
