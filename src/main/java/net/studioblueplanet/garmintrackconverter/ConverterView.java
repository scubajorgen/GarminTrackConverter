/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import net.studioblueplanet.logger.DebugLogger;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.ImageIcon;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Jorgen
 */
public class ConverterView extends javax.swing.JFrame implements Runnable
{
    private final Settings  settings;
    private boolean         tracksShown;
    private Track           track;
    private Waypoints       waypoints;
    private Device          device;
    
    private final Thread    thread;
    private final boolean   threadExit;
    
    private final Map<String,Track>   tracks;
    
    private final MapOsm    map;

    /**
     * Creates new form ConverterView
     */
    public ConverterView()
    {
        //DefaultListModel<String> model;

        settings=Settings.getInstance();
        this.setResizable(false);
        initComponents();
      
        // Initialize the listbox
        this.jTrackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //model=new DefaultListModel<>();
        //jTrackList.setModel(model);
                
        tracks=new HashMap<>();
        
                // Initialize the map
        this.jMapPanel.setLayout(new BoxLayout(this.jMapPanel, BoxLayout.X_AXIS));
        map = new MapOsm(this.jMapPanel);
        this.textAreaOutput.setText("Please attach device\n");
 
        
        tracksShown =false;
        threadExit  =false;
        thread      =new Thread(this);
        thread.start();
    }
    
    /**
     * Thread function
     */
    @Override
    public void run()
    {
        String                          waypointFile;
        String                          deviceFile;
        boolean                         localThreadExit;
        File                            trackFile;
        final DefaultListModel<String>  model;
        
        model=new DefaultListModel<>();
        //model=(DefaultListModel<String>)jTrackList.getModel();
        
        synchronized(this)
        {
            localThreadExit=threadExit;
            trackFile=new File(settings.getTrackFilePath());
        }        
        
        do
        {
            synchronized(this)
            {
                localThreadExit=threadExit;
            }
            
            if (!tracksShown)
            {
                if (trackFile.exists())
                {
                    this.textAreaOutput.append("Initializing...\n");
                    readWaypoints();
                    
                    readDevice();

                    textAreaOutput.append("File "+trackFile.getAbsolutePath()+"\n");
                    Stream.of(trackFile.listFiles())
                    .filter(file -> !file.isDirectory())
                    .sorted()
                    .map(File::getName).forEach(file -> {model.addElement(file);});
                    SwingUtilities.invokeLater(new Runnable() {public void run() 
                    {                          
                        jTrackList.setModel(model);
                    }});
                    tracksShown=true;
                }
            } 
            else
            {
                if (!trackFile.exists())
                {
                    this.textAreaOutput.setText("Please attach device\n");
                    tracks.clear();
                    SwingUtilities.invokeLater(new Runnable() {public void run() 
                    {                          
                        ((DefaultListModel)(jTrackList.getModel())).clear();
                    }});
                    tracksShown=false;
                }
            }
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {


            }
        }        
        while (!localThreadExit);
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jDesktopPane1 = new javax.swing.JDesktopPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        textAreaOutput = new javax.swing.JTextArea();
        buttonSave = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTrackList = new javax.swing.JList<>();
        jMapPanel = new javax.swing.JPanel();

        javax.swing.GroupLayout jDesktopPane1Layout = new javax.swing.GroupLayout(jDesktopPane1);
        jDesktopPane1.setLayout(jDesktopPane1Layout);
        jDesktopPane1Layout.setHorizontalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jDesktopPane1Layout.setVerticalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        textAreaOutput.setColumns(20);
        textAreaOutput.setRows(5);
        jScrollPane1.setViewportView(textAreaOutput);

        buttonSave.setText("Save GPX");
        buttonSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                buttonSaveActionPerformed(evt);
            }
        });

        jTrackList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jTrackListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jTrackList);

        jMapPanel.setBackground(new java.awt.Color(200, 200, 240));

        javax.swing.GroupLayout jMapPanelLayout = new javax.swing.GroupLayout(jMapPanel);
        jMapPanel.setLayout(jMapPanelLayout);
        jMapPanelLayout.setHorizontalGroup(
            jMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 513, Short.MAX_VALUE)
        );
        jMapPanelLayout.setVerticalGroup(
            jMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator3)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 486, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonSave)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(107, 107, 107)
                .addComponent(jLabel4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                    .addComponent(jMapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSave)
                .addGap(7, 7, 7)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Handles the convert button
     * @param evt Button event
     */
    private void buttonSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonSaveActionPerformed
    {//GEN-HEADEREND:event_buttonSaveActionPerformed
        JFileChooser                fc;
        String                      fileName;
        String                      path;
        FileNameExtensionFilter     fitFileFilter;
        int                         returnValue;
        String                      extension;
        String                      gpxFile;
        GpxWriter                   writer;        
        
        fc= new JFileChooser();
        
        // TO DO: generate a sensible filename
        fileName="";
        if (fileName.equals(""))
        {
            fc.setCurrentDirectory(new File(settings.getGpxFilePath()));
        }
        else
        {
            fc.setSelectedFile(new File(fileName));
        }
        fitFileFilter=new FileNameExtensionFilter("GPX files (*.gpx)", "GPX");

        // Set file extension filters
        fc.addChoosableFileFilter(fitFileFilter);
        fc.setFileFilter(fitFileFilter);
        
        returnValue=fc.showDialog(null, "Save");
        
        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            path=fc.getCurrentDirectory().toString();
            settings.setGpxFilePath(path);
            fileName=path+"/"+fc.getSelectedFile().getName();
            
            // Make sure the extension is .gpx
            extension=".gpx";
            if(!fileName.toLowerCase().endsWith(extension))
            {
                fileName +=extension;
            }
            
            writer=GpxWriter.getInstance();
            writer.writeTrackToFile(fileName, track, "Track");
            this.textAreaOutput.setText(textAreaOutput.getText()+"File "+fileName+" written\n");
        }
        if (returnValue == JFileChooser.CANCEL_OPTION)
        {

        }  
    }//GEN-LAST:event_buttonSaveActionPerformed

    private void jTrackListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jTrackListValueChanged
    {//GEN-HEADEREND:event_jTrackListValueChanged
        int     index;
        String  fullFileName;
        String  fileName;
        
        if (!evt.getValueIsAdjusting())
        {
            index=jTrackList.getSelectedIndex();
            fileName=getTrack(index);
            fullFileName=settings.getTrackFilePath()+"\\"+fileName;
            if (tracks.containsKey(fileName))
            {
                track=tracks.get(fileName);
            }
            else
            {
                track=readTrack(fullFileName);
                tracks.put(fileName, track);
            }
            textAreaOutput.append(track.getTrackInfo()+"\n");
            map.showTrack(track);
        }
    }//GEN-LAST:event_jTrackListValueChanged

    /**
     * Clear the track/activity list
     */
    private void clearTracks()
    {
        DefaultListModel<String> model;
        model=(DefaultListModel)jTrackList.getModel();
        model.clear();
    }
    
    /**
     * Get the track at given index in the list
     * @param index The index
     * @return The track
     */
    private String getTrack(int index)
    {
        DefaultListModel<String> model;
        model=(DefaultListModel)jTrackList.getModel();
        return model.getElementAt(index);
    }
    
    /**
     * Reads the fit file into a Track
     * @param fileName Name of the fit file
     * @return The track
     */
    private Track readTrack(String fileName)
    {
        Track theTrack;
        theTrack=new Track(fileName, device.getDeviceDescription());

        theTrack.addTrackWaypoints(waypoints.getWaypoints());

        return theTrack;
    }

    
    private void readWaypoints()
    {
        String waypointsFile;

        waypointsFile=settings.getWaypointFile();
        if (new File(waypointsFile).exists())
        {
            waypoints=new Waypoints(waypointsFile);
            textAreaOutput.append("Waypoint file "+waypointsFile+" read\n");
        }
        else
        {
            textAreaOutput.append("Waypoint file "+waypointsFile+" not found\n");
        }
    }
    
    private void readDevice()
    {
        String deviceFile;

        deviceFile=settings.getDeviceFile();
        if (new File(deviceFile).exists())
        {
            device=new Device(deviceFile);
            textAreaOutput.append("Device file "+deviceFile+" read\n");
        }
        else
        {
            textAreaOutput.append("Device file "+deviceFile+" not found\n");
        }        
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonSave;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jMapPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JList<String> jTrackList;
    private javax.swing.JTextArea textAreaOutput;
    // End of variables declaration//GEN-END:variables
}
