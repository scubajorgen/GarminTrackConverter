/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import org.jdesktop.application.SingleFrameApplication;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.util.ArrayList;
import org.jdesktop.application.Application;

/**
 *
 * @author Jorgen
 */
public class GarminTrackConverter extends SingleFrameApplication 
{
    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() 
    {
        ConverterView   view;
        ArrayList<Image>    iconList;
        ImageIcon           icon;  
        
 
        view=new ConverterView();
        view.setVisible(true);
/*        
        // Set the icons...
        ResourceMap resourceMap;
        resourceMap=this.getMainView().getResourceMap();
        System.out.println("Resource Dir "+resourceMap.getResourcesDir());
        
        iconList=new ArrayList();

        icon=resourceMap.getImageIcon("Application.icon16");
        iconList.add(icon.getImage());
        icon=resourceMap.getImageIcon("Application.icon24");
        iconList.add(icon.getImage());
        icon=resourceMap.getImageIcon("Application.icon32");
        iconList.add(icon.getImage());
        icon=resourceMap.getImageIcon("Application.icon42");
        iconList.add(icon.getImage());


        view.setIconImages(iconList);         
*/

        view.setIconImage(new ImageIcon(getClass().getResource("/net/studioblueplanet/garmintrackconverter/resources/icon48.png")).getImage());
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
