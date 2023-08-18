/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.studioblueplanet.settings.ApplicationSettings;
import net.studioblueplanet.settings.SettingsDevice;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdesktop.application.ResourceMap;

/**
 * This class represents the main view of the application
 * @author Jorgen
 */
public class ConverterView extends javax.swing.JFrame implements Runnable
{
    private final static Logger             LOGGER = LogManager.getLogger(ConverterView.class);
    private final ApplicationSettings       settings;
    private SettingsDevice                  attachedDevice;
    private Locations                       waypoints;
    private Device                          device;
    private final String                    appName;
    private boolean                         hasSync;
    private boolean                         isDirty;
    private boolean                         uiUpdated;
    
    private final Thread                    thread;
    private final boolean                   threadExit;
    
    private final MapOsm                    map;

    private DirectoryList                   trackDirectoryList;
    private DirectoryList                   routeDirectoryList;
    private DirectoryList                   newFileDirectoryList;
    private DirectoryList                   locationDirectoryList;
    
    private ConverterAbout                  aboutBox;

    /**
     * Creates new form ConverterView
     */
    public ConverterView()
    {
        LOGGER.debug("Starting ConverterView");
        GitBuildInfo build;
        
        settings=ApplicationSettings.getInstance();
        setResizable(false);
        initComponents();
        hasSync         =false;
        isDirty         =true;
       
        jCheckBoxCompress.setSelected(ApplicationSettings.getInstance().isTrackCompression());
        
        // Initialize the map
        this.jMapPanel.setLayout(new BoxLayout(this.jMapPanel, BoxLayout.X_AXIS));
        map = new MapOsm(this.jMapPanel);
        this.textAreaOutput.setText("Please attach device\n");

        build=GitBuildInfo.getInstance();
        appName         ="GarminTrackConverter "+build.getGitCommitDescription()+" ("+build.getBuildTime()+")";        
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent e) 
            {
                exitProcedure();
            }
        });
        
        threadExit      =false;
        thread          =new Thread(this);
        thread.start();
    }

    /**
     * This method redefines the fonts on the UI. It replaces the fonts by
     * fonts incorporated in the application.
     */
    public void setFont()
    {
        Font monospace15pt;
        Font proportional16pt;
        
        proportional16pt=new Font("Raleway", Font.PLAIN, 16);
        monospace15pt   =new Font("DejaVu Sans Mono", Font.PLAIN, 15);
        
        buttonDelete.setFont(proportional16pt);  
        buttonSave.setFont(proportional16pt);
        buttonUpload.setFont(proportional16pt);
        buttonSync.setFont(proportional16pt);
        jCheckBoxCompress.setFont(proportional16pt);

        textAreaOutput.setFont(proportional16pt);

        jNewFilesList.setFont(monospace15pt);
        jRouteList.setFont(monospace15pt);
        jLocationList.setFont(monospace15pt);
        jTrackList.setFont(monospace15pt);

        jTextFieldDevice.setFont(proportional16pt);
        jTextFieldInfo.setFont(proportional16pt);

        jLabelDevice.setFont(proportional16pt);
        jLabelInfo.setFont(proportional16pt);
        jLabelLocations.setFont(proportional16pt);
        jLabelNewFiles.setFont(proportional16pt);
        jLabelRoutes.setFont(proportional16pt);
        jLabelTracks.setFont(proportional16pt);

        jMenuFile.setFont(proportional16pt);
        jMenuHelp.setFont(proportional16pt);
        jMenuItemAbout.setFont(proportional16pt);
        jMenuItemExit.setFont(proportional16pt);
    }
    
    /**
     * Exit procedure. If we have a device depending on syncing, check
     * if sync has been performed.
     */
    private void exitProcedure()
    {
        if (hasSync && isDirty)
        {
            if (showConfirmDialog("Are you sure to exit without syncing?"))
            {
                System.exit(0);
            }
            
        }
        else
        {
            System.exit(0);
        }        
    }
    
    /**
     * Execute an external command to sync a MTP (Media Transfer Protocol)
     * to synchronize between the device and a local directory
     * For example execute a FreeFileSync batch job.
     */
    public void executeSyncCommand()
    {
        String command;
        
        command=attachedDevice.getSyncCommand();
        
        LOGGER.info("Executing sync command {}", command);
        try
        {
            Runtime.getRuntime().exec(command);
        }
        catch(IOException e)
        {
            LOGGER.error("Sync error: ", e.getMessage());
        }           
    }
    
    /**
     * This method loads each track that not has been loaded into memory
     */
    private void updateUiForDevice()
    {
        boolean done=false;
        
        while (!done)
        {
            int index=trackDirectoryList.getNextNonCache();
            if (index<0)
            {
                done=true;
            }
            else
            {
                String fileName=trackDirectoryList.getFileName(index);
                String fullFileName=attachedDevice.getTrackFilePath()+File.separator+fileName;
                LOGGER.info("Reading track file {}", fileName);
                Track track=readTrack(fullFileName, true);
                trackDirectoryList.addCacheableItem(track, index);   
            }
            checkForDirectoryUpdates();
        }
    }
    
    /**
     * Initialize the User Interface when a new device is found
     */
    private void initializeUiForDevice()
    {
        LOGGER.info("Attached device {}", attachedDevice.getName());
        this.textAreaOutput.setText("Initializing "+attachedDevice.getName()+"...\n");
        readDevice();

        jTextFieldDevice.setText(device.getDeviceDescription());

        SwingUtilities.invokeLater(() ->
        {                      
            if (attachedDevice.getSyncCommand()!=null && !attachedDevice.getSyncCommand().equals(""))
            {
                buttonSync.setVisible(true);
                hasSync=true;
            }
            else
            {
                buttonSync.setVisible(false);
                hasSync=false;
            }
            isDirty=true;
                
            trackDirectoryList.updateListModel();
            trackDirectoryList.setSelectedIndex(0);
            locationDirectoryList.updateListModel();
            routeDirectoryList.updateListModel();
            newFileDirectoryList.updateListModel();
            textAreaOutput.append("Done!\n");
            synchronized(this)
            {
                uiUpdated=true;
            }
        });
    }

    /**
     * Clean up the UI when a device is removed
     */
    private void clearUi()
    {
        SwingUtilities.invokeLater(() ->
        {                        
            buttonSync.setVisible(false);
            hasSync=false;
            trackDirectoryList.clear();
            routeDirectoryList.clear();
            newFileDirectoryList.clear();
            locationDirectoryList.clear();
            jTextFieldDevice.setText("");
        });
        synchronized(this)
        {
            map.hideTrack();
            attachedDevice=null;
        }        
    }
    
    /**
     * Check if directory content is updated. If so, update the lists on the
     * User Interface
     */
    private void checkForDirectoryUpdates()
    {
        if (trackDirectoryList.updateDirectoryList())
        {
            SwingUtilities.invokeLater(() ->
            {
                if (trackDirectoryList.updateListModel())
                {
                    map.hideTrack();
                }
            });
        }
        if (locationDirectoryList.updateDirectoryList())
        {
            SwingUtilities.invokeLater(() ->
            {
                if (locationDirectoryList.updateListModel())
                {
                    map.hideTrack();
                }
            });
        }
        if (routeDirectoryList.updateDirectoryList())
        {
            SwingUtilities.invokeLater(() ->
            {
                if (routeDirectoryList.updateListModel())
                {
                    map.hideTrack();
                }
            });
        }
        if (newFileDirectoryList.updateDirectoryList())
        {
            SwingUtilities.invokeLater(() ->
            {
                if (newFileDirectoryList.updateListModel())
                {
                    map.hideTrack();
                }
            });
        }
    }
    
    /**
     * Thread function
     */
    @Override
    public void run()
    {
        String                          waypointFile;
        boolean                         localThreadExit;
        boolean                         localUiUpdated;
        File                            deviceFile;
        boolean                         tracksShownLocal;
        int                             deviceIndexShownLocal;
        SettingsDevice                  deviceFound;
        List<SettingsDevice>            devices;
        int                             minPrio;
        
        // TODO: revise the synchronized stuff throughout the application
        synchronized(this)
        {
            devices=settings.getDevices();
        }        

        LOGGER.info("Thread started");
        do
        {
            synchronized(this)
            {
                localThreadExit         =threadExit;
            }
            
            // Find an attached device; with multiple devices attached
            // the device with lowest prio value wins
            minPrio     =Integer.MAX_VALUE;
            deviceFound =null;
            for (SettingsDevice settingsDevice : devices)
            {
                deviceFile=new File(settingsDevice.getDeviceFile());
                if (deviceFile.exists())
                {
                    if (settingsDevice.getDevicePriority()<minPrio)
                    {
                        deviceFound=settingsDevice;
                        minPrio=deviceFound.getDevicePriority();
                    }
                }
            }          
            
            if (deviceFound!=attachedDevice)
            {
                attachedDevice=deviceFound;
                LOGGER.info("Found device {}", attachedDevice.getName());
                // We found a new device or device was removed
                if (deviceFound!=null)
                {
                    trackDirectoryList      =new DirectoryList(new File(attachedDevice.getTrackFilePath())   , 
                                                                jTrackList   , new DefaultListModel<>(), false);
                    locationDirectoryList   =new DirectoryList(new File(attachedDevice.getLocationFilePath()), 
                                                                jLocationList, new DefaultListModel<>(), true);
                    routeDirectoryList      =new DirectoryList(new File(attachedDevice.getRouteFilePath())   , 
                                                                jRouteList   , new DefaultListModel<>(), true);
                    newFileDirectoryList    =new DirectoryList(new File(attachedDevice.getNewFilePath())     , 
                                                                jNewFilesList, new DefaultListModel<>(), true);
                    trackDirectoryList.updateDirectoryList();
                    locationDirectoryList.updateDirectoryList();
                    routeDirectoryList.updateDirectoryList();
                    newFileDirectoryList.updateDirectoryList();
                    
                    synchronized(this)
                    {
                        uiUpdated=false;
                    }
                    initializeUiForDevice();
                }
                else
                {
                    clearUi();
                }
            }
            else
            {
                synchronized(this)
                {
                    localUiUpdated=uiUpdated;
                }
                if (localUiUpdated)
                {
                    // Update the directory list and the cache
                    updateUiForDevice();                      
                }
            }
            
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                LOGGER.error("Thread sleep interrupted");
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
        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        textAreaOutput = new javax.swing.JTextArea();
        buttonSave = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTrackList = new javax.swing.JList<>();
        jMapPanel = new javax.swing.JPanel();
        jLabelTracks = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jRouteList = new javax.swing.JList<>();
        jLabelRoutes = new javax.swing.JLabel();
        buttonUpload = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        jNewFilesList = new javax.swing.JList<>();
        buttonDelete = new javax.swing.JButton();
        jLabelNewFiles = new javax.swing.JLabel();
        jLabelLocations = new javax.swing.JLabel();
        jLabelDevice = new javax.swing.JLabel();
        jTextFieldDevice = new javax.swing.JTextField();
        jScrollPane8 = new javax.swing.JScrollPane();
        jLocationList = new javax.swing.JList<>();
        jLabelInfo = new javax.swing.JLabel();
        jTextFieldInfo = new javax.swing.JTextField();
        buttonSync = new javax.swing.JButton();
        jCheckBoxCompress = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemAbout = new javax.swing.JMenuItem();

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

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

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

        jLabelTracks.setText("Tracks");

        jRouteList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jRouteListValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jRouteList);

        jLabelRoutes.setText("Routes");

        buttonUpload.setText("Upload GPX");
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

        jLabelNewFiles.setText("New files");

        jLabelLocations.setText("Locations");

        jLabelDevice.setText("Device:");

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

        jLabelInfo.setText("Info:");

        jTextFieldInfo.setEnabled(false);

        buttonSync.setText("Sync");
        buttonSync.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                buttonSyncActionPerformed(evt);
            }
        });

        jCheckBoxCompress.setText("Compress");

        jMenuFile.setText("File");

        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar1.add(jMenuFile);

        jMenuHelp.setText("Help");

        jMenuItemAbout.setText("About");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemAbout);

        jMenuBar1.add(jMenuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jCheckBoxCompress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(14, 14, 14)
                                .addComponent(buttonSave)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonUpload)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonDelete)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSync))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jScrollPane2)
                                            .addComponent(jScrollPane8)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabelTracks)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabelLocations)
                                        .addGap(340, 340, 340)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane3)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabelNewFiles)
                                            .addComponent(jLabelRoutes))
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabelInfo)
                                .addGap(18, 18, 18)
                                .addComponent(jTextFieldInfo))
                            .addComponent(jMapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabelDevice)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextFieldDevice, javax.swing.GroupLayout.DEFAULT_SIZE, 767, Short.MAX_VALUE)))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonDelete, buttonSave, buttonSync, buttonUpload});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldDevice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelDevice))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelTracks)
                        .addComponent(jLabelRoutes)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
                            .addComponent(jScrollPane2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelLocations)
                            .addComponent(jLabelNewFiles))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane7)
                            .addComponent(jScrollPane8)))
                    .addComponent(jMapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonSave)
                    .addComponent(buttonUpload)
                    .addComponent(buttonDelete)
                    .addComponent(buttonSync)
                    .addComponent(jCheckBoxCompress, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelInfo)
                    .addComponent(jTextFieldInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Shows the file chooser requesting a GPX file 
     * @param directory Initial directory
     * @param fileNameProposal Suggested file name
     * @param buttonText Text on the action button
     * @return The file name entered or null if canceled
     */
    private String getGpxFileName(String directory, String fileNameProposal, String buttonText)
    {
        JFileChooser                fc;
        String                      returnFileName;
        String                      path;
        FileNameExtensionFilter     fitFileFilter;
        int                         returnValue;
        String                      extension;
        String                      gpxFile;
        
        fc= new JFileChooser();
        
        returnFileName=fileNameProposal;
        fc.setCurrentDirectory(new File(directory));
        if (!returnFileName.equals(""))
        {
            fc.setSelectedFile(new File(returnFileName));
        }
        
        fitFileFilter=new FileNameExtensionFilter("GPX files (*.gpx)", "GPX");

        // Set file extension filters
        fc.addChoosableFileFilter(fitFileFilter);
        fc.setFileFilter(fitFileFilter);
        
        returnValue=fc.showDialog(null, buttonText);
        
        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            path=fc.getCurrentDirectory().toString();
            
            // To do: set setting
            
            returnFileName=path+"/"+fc.getSelectedFile().getName();
            
            // Make sure the extension is .gpx
            extension=".gpx";
            if(!returnFileName.toLowerCase().endsWith(extension))
            {
                returnFileName +=extension;
            }

        }
        if (returnValue == JFileChooser.CANCEL_OPTION)
        {
            LOGGER.info("File save canceled");
            returnFileName=null;
        }         
        return returnFileName;
    }

    /**
     * Show confirm dialog.
     * @param message Message to show
     * @return True if confirmed, false if canceled.
     */
    private boolean showConfirmDialog(String message)
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
     * Shows the indicated track on the map
     * @param track The track to show
     */
    private void trackToMap(Track track)
    {
        if (track!=null)
        {
            if (track.getNumberOfSegments()>0)
            {
                map.showTrack(track);
                jTextFieldInfo.setText(track.getTrackInfo());
            }
            else if (track.getWaypoints().size()>0)
            {
                map.showWaypoints(track.getWaypoints());
                jTextFieldInfo.setText("Locations: "+track.getWaypoints().size());
            }
        }
    }
    
    /**
     * Reads the fit file into a Track
     * @param fileName Name of the fit file
     * @param addWayoints Indicates whether the waypoints recorded during the
     *                    track should be added to the track
     * @return The track
     */
    private Track readTrack(String fileName, boolean addWaypoints)
    {
        Track theTrack;
        theTrack=new Track(fileName, device.getDeviceDescription());

        if(addWaypoints && waypoints!=null)
        {
            theTrack.addTrackWaypoints(waypoints.getWaypoints());
        }
        return theTrack;
    }

    /**
     * Read the waypoints from the FIT file
     */
    private void readWaypoints()
    {
        String waypointsFile;

        waypointsFile=attachedDevice.getWaypointFile();
        if (new File(waypointsFile).exists())
        {
            waypoints=new Locations(waypointsFile);
            waypoints.dumpWaypoints();
            textAreaOutput.append("Waypoint file "+waypointsFile+" read\n");
        }
        else
        {
            textAreaOutput.append("Waypoint file "+waypointsFile+" not found\n");
        }
    }
    
    /**
     * Reads the Device from the device XML file
     */
    private void readDevice()
    {
        String deviceFile;
        deviceFile=attachedDevice.getDeviceFile();
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

    /**
     * Returns track from map
     * @param list List in which the track is selected
     * @param map Map containing the track
     * @return The track or null if not found
     */
    private Track getTrack(DirectoryList list)
    {
        Track track;
        track=null;

        String trackName=list.getSelectedFileName();
        if (trackName!=null)
        {
            track=(Track)list.getCacheableItem();
        }
        return track;
    }
    
    /**
     * Returns track from map
     * @param list List in which the track is selected
     * @param map Map containing the track
     * @return The track or null if not found
     */
    private Locations getWaypoints(DirectoryList list)
    {
        Locations locations;
        locations=null;
        
        String waypointsName=list.getSelectedFileName();
        if (waypointsName!=null)
        {
            locations=(Locations)list.getCacheableItem();
        }
        return locations;
    }
    
    
    /**
     * Handles the convert button
     * @param evt Button event
     */
    private void buttonSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonSaveActionPerformed
    {//GEN-HEADEREND:event_buttonSaveActionPerformed
        GpxWriter                   gpsWriter;  
        FileWriter                  fileWriter;
        String                      fileName;
        String                      trackName;
        Track                       track;
        Locations                   locations;
        String                      gpxFileName;
            
        if (trackDirectoryList.hasSelection())
        {
            track=getTrack(trackDirectoryList);
            gpxFileName=track.getSport()+"_"+track.getStartDate()+"_descripion.gpx";
            fileName=getGpxFileName(settings.getGpxFileDownloadPath(), gpxFileName, "Save");
            if (fileName!=null)
            {
                gpsWriter=GpxWriter.getInstance();
                try
                {
                    fileWriter=new FileWriter(fileName);
                    gpsWriter.writeTrackToFile(fileWriter, track, "Track", appName, jCheckBoxCompress.isSelected());
                    fileWriter.close();
                 }
                catch (IOException e)
                {
                    LOGGER.error("Error writing file {}: {}", fileName, e.getMessage());
                }
                this.textAreaOutput.setText("File saved to "+fileName);
                if (jCheckBoxCompress.isSelected())
                {
                    this.textAreaOutput.append(" (compressed)");
                }
                this.textAreaOutput.append("\n");
                LOGGER.info("Track saved to {}", fileName);
            }
        }
        else if (newFileDirectoryList.hasSelection())
        {
            fileName=getGpxFileName(settings.getGpxFileDownloadPath(), "", "Save");
            if (fileName!=null)
            {
                gpsWriter=GpxWriter.getInstance();
                track=getTrack(newFileDirectoryList);
                try
                {
                    fileWriter=new FileWriter(fileName);
                    gpsWriter.writeTrackToFile(fileWriter, track, "Track", appName, false);
                    fileWriter.close();
                }
                catch (IOException e)
                {
                    LOGGER.error("Error writing file {}: {}", fileName, e.getMessage());
                }
                this.textAreaOutput.setText("Route saved to "+fileName+"\n");
                LOGGER.info("GPX file saved to {}", fileName);
            }
        }
        else if (locationDirectoryList.hasSelection())
        {
            fileName=getGpxFileName(settings.getGpxFileDownloadPath(), "", "Save");
            if (fileName!=null)
            {
                gpsWriter=GpxWriter.getInstance();
                locations=getWaypoints(locationDirectoryList);
                try
                {
                    fileWriter=new FileWriter(fileName);
                    gpsWriter.writeWaypointsToFile(fileWriter, waypoints);
                    fileWriter.close();
                }
                catch (IOException e)
                {
                    LOGGER.error("Error writing file {}: {}", fileName, e.getMessage());
                }
                this.textAreaOutput.setText("Route saved to "+fileName+"\n");
                LOGGER.info("Locations saved to {}", fileName);
            }
        }
        else if (routeDirectoryList.hasSelection())
        {
            fileName=getGpxFileName(settings.getGpxFileDownloadPath(), "", "Save");
            if (fileName!=null)
            {
                gpsWriter=GpxWriter.getInstance();
                track=getTrack(routeDirectoryList);
                try
                {
                    fileWriter=new FileWriter(fileName);
                    gpsWriter.writeTrackToFile(fileWriter, track, "Track", appName, false);
                    fileWriter.close();
                }
                catch (IOException e)
                {
                    LOGGER.error("Error writing file {}: {}", fileName, e.getMessage());
                }
                this.textAreaOutput.setText("Route saved to "+fileName+"\n");
                LOGGER.info("Route saved to {}", fileName);
            }
        }
    }//GEN-LAST:event_buttonSaveActionPerformed

    private void jTrackListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jTrackListValueChanged
    {//GEN-HEADEREND:event_jTrackListValueChanged
        String  fullFileName;
        String  fileName;
        Track   track;
        
        if (!evt.getValueIsAdjusting() && trackDirectoryList.hasSelection())
        {
            synchronized(this)
            {
                LOGGER.info("Reading waypoints for track");
                readWaypoints();
                routeDirectoryList.clearSelection();
                newFileDirectoryList.clearSelection();
                locationDirectoryList.clearSelection();
                fileName=trackDirectoryList.getSelectedFileName();
                fullFileName=attachedDevice.getTrackFilePath()+File.separator+fileName;
                track=getTrack(trackDirectoryList);
                if (track!=null)
                {
                    LOGGER.info("Retrieved track {} from cache", fileName);
                    textAreaOutput.setText("Track retrieved from cache\n");
                }
                else
                {
                    LOGGER.info("Reading track file {}", fileName);
                    track=readTrack(fullFileName, true);
                    trackDirectoryList.addCacheableItem(track);

                    textAreaOutput.setText("Track read from FIT file\n");

                }
                textAreaOutput.append(track.getTrackInfo2()+"\n");
                jTextFieldInfo.setText(track.getTrackInfo());
                map.showTrack(track);
            }
        }
    }//GEN-LAST:event_jTrackListValueChanged

    private void jRouteListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jRouteListValueChanged
    {//GEN-HEADEREND:event_jRouteListValueChanged
        int     index;
        String  fullFileName;
        String  fileName;
        Track   track;
        
        if (!evt.getValueIsAdjusting() && routeDirectoryList.hasSelection())
        {
            synchronized(this)
            {
                trackDirectoryList.clearSelection();
                newFileDirectoryList.clearSelection();
                locationDirectoryList.clearSelection();
                fileName=routeDirectoryList.getSelectedFileName();
                fullFileName=attachedDevice.getRouteFilePath()+File.separator+fileName;
                track=getTrack(routeDirectoryList);
                if (track!=null)
                {
                    LOGGER.info("Retrieved route file {} from cache", fileName);
                    textAreaOutput.setText("Route retrieved from cache\n");
                }
                else
                {
                    LOGGER.info("Reading route file {}", fileName);
                    track=readTrack(fullFileName, false);
                    routeDirectoryList.addCacheableItem(track);
                    textAreaOutput.setText("Route read from FIT file\n");
                }
                trackToMap(track);
            }
        }
    }//GEN-LAST:event_jRouteListValueChanged

    private void jNewFilesListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jNewFilesListValueChanged
    {//GEN-HEADEREND:event_jNewFilesListValueChanged
        int     index;
        String  fullFileName;
        String  fileName;
        Track   track;
        
        if (!evt.getValueIsAdjusting() && newFileDirectoryList.hasSelection())
        {
            synchronized(this)
            {
                trackDirectoryList.clearSelection();
                routeDirectoryList.clearSelection();
                locationDirectoryList.clearSelection();

                fileName=newFileDirectoryList.getSelectedFileName();
                fullFileName=attachedDevice.getNewFilePath()+File.separator+fileName;

                track=getTrack(newFileDirectoryList);
                if (track!=null)
                {
                    LOGGER.info("Retrieved new file {} from cache", fileName);
                    textAreaOutput.setText("New file retrieved from cache\n");
                }
                else
                {
                    LOGGER.info("Reading new file {}", fileName);
                    track=GpxReader.getInstance().readRouteFromFile(fullFileName);
                    newFileDirectoryList.addCacheableItem(track);
                    textAreaOutput.setText("New file read from GPX file\n");
                }
                trackToMap(track);
            }
        }
    }//GEN-LAST:event_jNewFilesListValueChanged

    private void buttonUploadActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonUploadActionPerformed
    {//GEN-HEADEREND:event_buttonUploadActionPerformed
        String fileName;
        fileName=this.getGpxFileName(settings.getGpxFileUploadPath(), "", "Upload");
        if (fileName!=null)
        {
            String destinationPath=attachedDevice.getNewFilePath();
            String destinationFile=destinationPath+"//"+(new File(fileName).getName());
            File destination=new File(destinationPath);
            if (destination.exists())
            {
                Path copied         = Paths.get(destinationFile);
                Path originalPath   = Paths.get(fileName);
                try
                {
                    Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Copied {} to {}", fileName, destinationFile);
                    Track route=GpxReader.getInstance().readRouteFromFile(fileName);
                    if (route!=null)
                    {
                        trackToMap(route);
                        // TO DO: set index in file list, somewhere
                    }
                    textAreaOutput.setText("Uploaded "+fileName+"\n");
                    isDirty=true;
                }
                catch (IOException e)
                {
                    textAreaOutput.setText("Error uploading "+fileName+": "+e.getMessage()+"\n");
                    LOGGER.error("Error copying file: {}", e.getMessage());
                }
            }
        }

    }//GEN-LAST:event_buttonUploadActionPerformed

    private void jLocationListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jLocationListValueChanged
    {//GEN-HEADEREND:event_jLocationListValueChanged
        if (!evt.getValueIsAdjusting() && locationDirectoryList.hasSelection())
        {
            synchronized(this)
            {
                Locations       points;
                String          fileName;
                String          fullFileName;
                int             index;

                trackDirectoryList.clearSelection();
                routeDirectoryList.clearSelection();
                newFileDirectoryList.clearSelection();

                
                fileName=locationDirectoryList.getSelectedFileName();
                fullFileName=attachedDevice.getLocationFilePath()+File.separator+fileName;

                points=getWaypoints(locationDirectoryList);
                if (points!=null)
                {
                    LOGGER.info("Retrieved waypoints file {} from cache", fileName);
                    textAreaOutput.setText("Locations retrieved from cache\n");
                }
                else
                {
                    LOGGER.info("Reading waypoints file {}", fullFileName);
                    points=new Locations(fullFileName);
                    locationDirectoryList.addCacheableItem(points);
                    textAreaOutput.setText("Locations read from GPX file\n");
                }
                map.showWaypoints(points.getWaypoints());
                jTextFieldInfo.setText("Locations: "+points.getNumberOfWaypoints());
            }
        }
    }//GEN-LAST:event_jLocationListValueChanged

    private void buttonDeleteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonDeleteActionPerformed
    {//GEN-HEADEREND:event_buttonDeleteActionPerformed
        String  pathName;
        String  fileName;

        pathName=null;
        fileName=null;
        if (trackDirectoryList.hasSelection())
        {
            fileName=trackDirectoryList.getSelectedFileName();
            pathName=attachedDevice.getTrackFilePath()+"/"+fileName;
        }
        else if (newFileDirectoryList.hasSelection())
        {
            fileName=newFileDirectoryList.getSelectedFileName();
            pathName=attachedDevice.getNewFilePath()+"/"+fileName;
        }
        else if (locationDirectoryList.hasSelection())
        {
            fileName=locationDirectoryList.getSelectedFileName();
            pathName=attachedDevice.getLocationFilePath()+"/"+fileName;
        }
        else if (routeDirectoryList.hasSelection())
        {
            fileName=routeDirectoryList.getSelectedFileName();
            pathName=attachedDevice.getRouteFilePath()+"/"+fileName;
        }

        
        if (pathName!=null)
        {
            if (showConfirmDialog("Are you sure to delete "+pathName+"?"))
            {
                try
                {
                    LOGGER.info("Deleting {}", pathName);
                    Files.delete(Paths.get(pathName));
                    this.textAreaOutput.setText("Deleted "+fileName+"\n");
                    map.hideTrack();
                    isDirty=true;
                }
                catch (IOException e)
                {
                    LOGGER.error("Error deleting {}: {}", pathName, e.getMessage());
                    textAreaOutput.setText("Error deleting "+fileName+"\n");
                }
            }
        }
    }//GEN-LAST:event_buttonDeleteActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExitActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExitActionPerformed
        exitProcedure();
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemAboutActionPerformed
    {//GEN-HEADEREND:event_jMenuItemAboutActionPerformed
        GarminTrackConverter    app;
        GitBuildInfo            build;
        ResourceMap             appResourceMap;
        
        app=GarminTrackConverter.getApplication();
        build=GitBuildInfo.getInstance();

        appResourceMap=app.getContext().getResourceMap();

        if (aboutBox==null)
        {
            aboutBox=new ConverterAbout(this, true);
            aboutBox.setLocationRelativeTo(this);

            aboutBox.setVersion(build.getGitCommitDescription()+" ("+build.getBuildTime()+")");
            aboutBox.setAuthor(appResourceMap.getString("Application.author"));
            aboutBox.setHomePage(appResourceMap.getString("Application.homepage"));            
            aboutBox.setGithub(appResourceMap.getString("Application.github"));            
        }
        GarminTrackConverter.getApplication().show(aboutBox);

    }//GEN-LAST:event_jMenuItemAboutActionPerformed

    private void buttonSyncActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonSyncActionPerformed
    {//GEN-HEADEREND:event_buttonSyncActionPerformed
        executeSyncCommand();
        isDirty=false;
    }//GEN-LAST:event_buttonSyncActionPerformed

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonDelete;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton buttonSave;
    private javax.swing.JButton buttonSync;
    private javax.swing.JButton buttonUpload;
    private javax.swing.JCheckBox jCheckBoxCompress;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JLabel jLabelDevice;
    private javax.swing.JLabel jLabelInfo;
    private javax.swing.JLabel jLabelLocations;
    private javax.swing.JLabel jLabelNewFiles;
    private javax.swing.JLabel jLabelRoutes;
    private javax.swing.JLabel jLabelTracks;
    private javax.swing.JList<String> jLocationList;
    private javax.swing.JPanel jMapPanel;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JList<String> jNewFilesList;
    private javax.swing.JList<String> jRouteList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JTextField jTextFieldDevice;
    private javax.swing.JTextField jTextFieldInfo;
    private javax.swing.JList<String> jTrackList;
    private javax.swing.JTextArea textAreaOutput;
    // End of variables declaration//GEN-END:variables
}
