/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;



import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import net.studioblueplanet.settings.ConfigSettings;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 *
 * @author Jorgen
 */
public class ConverterView extends javax.swing.JFrame implements Runnable
{
    private final static Logger         LOGGER = LogManager.getLogger(ConverterView.class);
    private final ConfigSettings        settings;
    private boolean                     tracksShown;
    private boolean                     newFilesShown;
    private Track                       track;
    private Waypoints                   waypoints;
    private Device                      device;
    
    private final Thread                thread;
    private final boolean               threadExit;
    
    private final Map<String,Track>     tracks;
    private final Map<String,Track>     routes;
    
    private final MapOsm                map;

    final DefaultListModel<String>  trackModel;
    final DefaultListModel<String>  routeModel;
    final DefaultListModel<String>  newFileModel;
    final DefaultListModel<String>  locationModel;

    /**
     * Creates new form ConverterView
     */
    public ConverterView()
    {
        LOGGER.debug("Starting ConverterView");
        //DefaultListModel<String> model;

        settings=ConfigSettings.getInstance();
        this.setResizable(false);
        initComponents();
      
        // Initialize the listbox
        trackModel      =new DefaultListModel<>();
        routeModel      =new DefaultListModel<>();
        newFileModel    =new DefaultListModel<>();
        locationModel   =new DefaultListModel<>();

        jTrackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTrackList.setModel(trackModel);
        jRouteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jRouteList.setModel(routeModel);
        jNewFilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jNewFilesList.setModel(newFileModel);
        jLocationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jLocationList.setModel(locationModel);
/*        
        DefaultListModel model=new DefaultListModel<>();
        jTrackList.setModel(model);
        model=new DefaultListModel<>();
        jRouteList.setModel(model);
        model=new DefaultListModel<>();
        jNewFilesList.setModel(model);
*/                
        tracks=new HashMap<>();
        routes=new HashMap<>();
        
                // Initialize the map
        this.jMapPanel.setLayout(new BoxLayout(this.jMapPanel, BoxLayout.X_AXIS));
        map = new MapOsm(this.jMapPanel);
        this.textAreaOutput.setText("Please attach device\n");
 
        
        newFilesShown   =false;
        tracksShown     =false;
        threadExit      =false;
        thread          =new Thread(this);
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
        File                            routeFile;
        File                            newFile;
        File                            locationFile;
        boolean                         newFilesShownLocal;
        
        
        //trackModel=(DefaultListModel)jTrackList.getModel();
        //routeModel=(DefaultListModel)jTrackList.getModel();
        //newFileModel=(DefaultListModel)jTrackList.getModel();

        //model=(DefaultListModel<String>)jTrackList.getModel();
        synchronized(this)
        {
            localThreadExit =threadExit;
            trackFile       =new File(settings.getStringValue("trackFilePath"));
            routeFile       =new File(settings.getStringValue("routeFilePath"));
            newFile         =new File(settings.getStringValue("newFilePath"));
            locationFile    =new File(settings.getStringValue("locationFilePath"));
        }        
        
        do
        {
            synchronized(this)
            {
                localThreadExit     =threadExit;
                newFilesShownLocal  =newFilesShown;
            }
            
            if (!tracksShown)
            {
                if (trackFile.exists())
                {
                    this.textAreaOutput.append("Initializing...\n");
                    readWaypoints();
                    
                    readDevice();
                    jTextFieldDevice.setText(device.getDeviceDescription());

                    textAreaOutput.append("File "+trackFile.getAbsolutePath()+"\n");
                    SwingUtilities.invokeLater(new Runnable() {public void run() 
                    {                          
                        Stream.of(trackFile.listFiles())
                        .filter(file -> !file.isDirectory())
                        .sorted()
                        .map(File::getName).forEach(file -> {trackModel.addElement(file);});

                        Stream.of(locationFile.listFiles())
                        .filter(file -> !file.isDirectory())
                        .sorted()
                        .map(File::getName).forEach(file -> {locationModel.addElement(file);});

                        Stream.of(routeFile.listFiles())
                        .filter(file -> !file.isDirectory())
                        .sorted()
                        .map(File::getName).forEach(file -> {routeModel.addElement(file);});

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
                        trackModel.clear();
                    }});
                    tracksShown=false;
                }
            }
            
            if (!newFilesShownLocal)
            {
                if (newFile.exists())
                {
                    SwingUtilities.invokeLater(new Runnable() {public void run() 
                    {                          
                        newFileModel.clear();
                        Stream.of(newFile.listFiles())
                        .filter(file -> !file.isDirectory())
                        .filter(file -> file.getName().toLowerCase().endsWith(".gpx"))
                        .sorted()
                        .map(File::getName).forEach(file -> {newFileModel.addElement(file);});
                    }});
                    synchronized(this)
                    {
                        newFilesShown=true;
                    }
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
        jFrame1 = new javax.swing.JFrame();
        jScrollPane4 = new javax.swing.JScrollPane();
        textAreaOutput1 = new javax.swing.JTextArea();
        buttonSave1 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jMapPanel1 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jLabel6 = new javax.swing.JLabel();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        textAreaOutput = new javax.swing.JTextArea();
        buttonSave = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTrackList = new javax.swing.JList<>();
        jMapPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jRouteList = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        buttonUpload = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        jNewFilesList = new javax.swing.JList<>();
        buttonDelete = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldDevice = new javax.swing.JTextField();
        jScrollPane8 = new javax.swing.JScrollPane();
        jLocationList = new javax.swing.JList<>();

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

        jFrame1.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        textAreaOutput1.setColumns(20);
        textAreaOutput1.setRows(5);
        jScrollPane4.setViewportView(textAreaOutput1);

        buttonSave1.setText("Save GPX");
        buttonSave1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                buttonSave1ActionPerformed(evt);
            }
        });

        jMapPanel1.setBackground(new java.awt.Color(200, 200, 240));

        javax.swing.GroupLayout jMapPanel1Layout = new javax.swing.GroupLayout(jMapPanel1);
        jMapPanel1.setLayout(jMapPanel1Layout);
        jMapPanel1Layout.setHorizontalGroup(
            jMapPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 513, Short.MAX_VALUE)
        );
        jMapPanel1Layout.setVerticalGroup(
            jMapPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jList2.setModel(new javax.swing.AbstractListModel<String>()
        {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane6.setViewportView(jList2);

        jLabel6.setText("Routes");

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrame1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator4)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jFrame1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jFrame1Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jFrame1Layout.createSequentialGroup()
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jMapPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jFrame1Layout.createSequentialGroup()
                        .addComponent(buttonSave1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame1Layout.createSequentialGroup()
                .addGap(107, 107, 107)
                .addComponent(jLabel5)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jFrame1Layout.createSequentialGroup()
                .addGap(0, 11, Short.MAX_VALUE)
                .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addComponent(jMapPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSave1)
                .addGap(23, 23, 23)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jMapPanelLayout.setVerticalGroup(
            jMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jLabel1.setText("Tracks");

        jRouteList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jRouteListValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jRouteList);

        jLabel2.setText("Routes");

        buttonUpload.setText("Upload");
        buttonUpload.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                buttonUploadActionPerformed(evt);
            }
        });

        jNewFilesList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jNewFilesListValueChanged(evt);
            }
        });
        jScrollPane7.setViewportView(jNewFilesList);

        buttonDelete.setText("Delete");
        buttonDelete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                buttonDeleteActionPerformed(evt);
            }
        });

        jLabel3.setText("New files");

        jLabel7.setText("Locations");

        jLabel8.setText("Device:");

        jTextFieldDevice.setEnabled(false);

        jLocationList.setModel(new javax.swing.AbstractListModel<String>()
        {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jLocationList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jLocationListValueChanged(evt);
            }
        });
        jScrollPane8.setViewportView(jLocationList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator3)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel7)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                            .addComponent(jScrollPane8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldDevice, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 299, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jMapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(buttonSave)
                                .addGap(117, 117, 117)
                                .addComponent(buttonUpload)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonDelete)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel8)
                    .addComponent(jTextFieldDevice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jMapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                            .addComponent(jScrollPane2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel7))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonSave)
                    .addComponent(buttonUpload)
                    .addComponent(buttonDelete))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            fc.setCurrentDirectory(new File(settings.getStringValue("gpxFilePath")));
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
            
            // To do: set setting
            
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
        
        if (!evt.getValueIsAdjusting() && jTrackList.getSelectedIndex()>=0)
        {
            jRouteList.clearSelection();
            jNewFilesList.clearSelection();
            jLocationList.clearSelection();
            index=jTrackList.getSelectedIndex();
            fileName=getTrack(index);
            fullFileName=settings.getStringValue("trackFilePath")+"\\"+fileName;
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

    private void buttonSave1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonSave1ActionPerformed
    {//GEN-HEADEREND:event_buttonSave1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonSave1ActionPerformed

    private void jTrackList1ValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jTrackList1ValueChanged
    {//GEN-HEADEREND:event_jTrackList1ValueChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jTrackList1ValueChanged

    private void jRouteListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jRouteListValueChanged
    {//GEN-HEADEREND:event_jRouteListValueChanged
        int     index;
        String  fullFileName;
        String  fileName;
        
        if (!evt.getValueIsAdjusting() && jRouteList.getSelectedIndex()>=0)
        {
            jTrackList.clearSelection();
            jNewFilesList.clearSelection();
            jLocationList.clearSelection();
            index=jRouteList.getSelectedIndex();
            fileName=getRoute(index);
            fullFileName=settings.getStringValue("routeFilePath")+"\\"+fileName;
            if (routes.containsKey(fileName))
            {
                track=routes.get(fileName);
            }
            else
            {
                track=readTrack(fullFileName);
                routes.put(fileName, track);
            }
            textAreaOutput.append(track.getTrackInfo()+"\n");
            map.showTrack(track);
        }
    }//GEN-LAST:event_jRouteListValueChanged

    private void jNewFilesListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jNewFilesListValueChanged
    {//GEN-HEADEREND:event_jNewFilesListValueChanged
        if (!evt.getValueIsAdjusting() && jNewFilesList.getSelectedIndex()>=0)
        {
            jTrackList.clearSelection();
            jRouteList.clearSelection();
            jLocationList.clearSelection();
        }
    }//GEN-LAST:event_jNewFilesListValueChanged

    private void buttonUploadActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonUploadActionPerformed
    {//GEN-HEADEREND:event_buttonUploadActionPerformed
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
            fc.setCurrentDirectory(new File(settings.getStringValue("gpxFilePath")));
        }
        else
        {
            fc.setSelectedFile(new File(fileName));
        }
        fitFileFilter=new FileNameExtensionFilter("GPX files (*.gpx)", "GPX");

        // Set file extension filters
        fc.addChoosableFileFilter(fitFileFilter);
        fc.setFileFilter(fitFileFilter);
        
        returnValue=fc.showDialog(null, "Upload");
        
        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            path=fc.getCurrentDirectory().toString();
            //settings.setGpxFilePath(path);
            fileName=path+"/"+fc.getSelectedFile().getName();
            
            // Make sure the extension is .gpx
            extension=".gpx";
            if(!fileName.toLowerCase().endsWith(extension))
            {
                fileName +=extension;
            }
            
            String destinationPath=settings.getStringValue("newFilePath");
            String destinationFile=destinationPath+"//"+fc.getSelectedFile().getName();
            File destination=new File(destinationPath);
            if (destination.exists())
            {
                Path copied = Paths.get(destinationFile);
                Path originalPath = Paths.get(fileName);
                try
                {
                    Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Copied {} to {}", fileName, destinationFile);
                    synchronized(this)
                    {
                        newFilesShown=false;
                    }
                }
                catch (IOException e)
                {
                    LOGGER.error("Error copying file: {}", e.getMessage());
                }
            }
        }
        if (returnValue == JFileChooser.CANCEL_OPTION)
        {

        }  
    }//GEN-LAST:event_buttonUploadActionPerformed

    private void jLocationListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jLocationListValueChanged
    {//GEN-HEADEREND:event_jLocationListValueChanged
        if (!evt.getValueIsAdjusting() && jLocationList.getSelectedIndex()>=0)
        {
            jTrackList.clearSelection();
            jRouteList.clearSelection();
            jNewFilesList.clearSelection();
        }
    }//GEN-LAST:event_jLocationListValueChanged

    private void buttonDeleteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonDeleteActionPerformed
    {//GEN-HEADEREND:event_buttonDeleteActionPerformed
        int     index; 
        String  pathName;
        String  fileName;

        pathName=null;
        if (jTrackList.getSelectedIndex()>=0)
        {
            fileName=jTrackList.getSelectedValue();
            pathName=settings.getStringValue("trackFilePath")+"/"+fileName;
        }
        else if (jNewFilesList.getSelectedIndex()>=0)
        {
            fileName=jNewFilesList.getSelectedValue();
            pathName=settings.getStringValue("newFilePath")+"/"+fileName;
        }
        else if (jLocationList.getSelectedIndex()>=0)
        {
            fileName=jLocationList.getSelectedValue();
            pathName=settings.getStringValue("locationFilePath")+"/"+fileName;
        }
        else if (jRouteList.getSelectedIndex()>=0)
        {
            fileName=jRouteList.getSelectedValue();
            pathName=settings.getStringValue("routeFilePath")+"/"+fileName;
        }

        
        if (pathName!=null)
        {
            if (showConfirmDialog("Are you sure to delete "+pathName+"?"))
            {
                try
                {
                    LOGGER.info("Deleting {}", pathName);
                    Files.delete(Paths.get(pathName));
                    this.tracksShown    =false;
                    this.newFilesShown  =false;
                }
                catch (IOException e)
                {
                    LOGGER.error("Error deleting {}: {}", pathName, e.getMessage());
                }
            }
        }
    }//GEN-LAST:event_buttonDeleteActionPerformed

    /**
     * Show confirm dialog.
     * @param message Message to show
     * @return True if confirmed, false if canceled.
     */
    public boolean showConfirmDialog(String message)
    {
        int     response;
        boolean yesPressed;
        
        yesPressed=false;
        response = JOptionPane.showConfirmDialog(null, message, "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION)
        {
            yesPressed=true;
        }
        return yesPressed;
    }    
    
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
     * Get the track at given index in the list
     * @param index The index
     * @return The track
     */
    private String getRoute(int index)
    {
        DefaultListModel<String> model;
        model=(DefaultListModel)jRouteList.getModel();
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

        waypointsFile=settings.getStringValue("waypointFile");
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

        deviceFile=settings.getStringValue("deviceFile");
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
    private javax.swing.JButton buttonDelete;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton buttonSave;
    private javax.swing.JButton buttonSave1;
    private javax.swing.JButton buttonUpload;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JList<String> jList2;
    private javax.swing.JList<String> jLocationList;
    private javax.swing.JPanel jMapPanel;
    private javax.swing.JPanel jMapPanel1;
    private javax.swing.JList<String> jNewFilesList;
    private javax.swing.JList<String> jRouteList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTextField jTextFieldDevice;
    private javax.swing.JList<String> jTrackList;
    private javax.swing.JTextArea textAreaOutput;
    private javax.swing.JTextArea textAreaOutput1;
    // End of variables declaration//GEN-END:variables
}
