/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.garmintrackconverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
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
    private final static Logger         LOGGER = LogManager.getLogger(ConverterView.class);
    private ApplicationSettings         settings;
    private SettingsDevice              attachedDevice;
    private boolean                     tracksShown;
    private Locations                   waypoints;
    private Device                      device;
    private final String                appName;
    
    private final Thread                thread;
    private final boolean               threadExit;
    
    // Caching of converted FIT/GPX files
    private final Map<String,Track>     tracksCache;
    private final Map<String,Track>     routesCache;
    private final Map<String,Track>     newFilesCache;
    private final Map<String,Locations> locationsCache;
    
    private final MapOsm                map;

    final DefaultListModel<String>      trackModel;
    final DefaultListModel<String>      routeModel;
    final DefaultListModel<String>      newFileModel;
    final DefaultListModel<String>      locationModel;
    
    private ConverterAbout              aboutBox;

    /**
     * Creates new form ConverterView
     */
    public ConverterView()
    {
        LOGGER.debug("Starting ConverterView");
        //DefaultListModel<String> model;
        GitBuildInfo build;
        
        settings=ApplicationSettings.getInstance();
        setResizable(false);
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
                
        tracksCache      =new HashMap<>();
        routesCache      =new HashMap<>();
        newFilesCache    =new HashMap<>();
        locationsCache   =new HashMap<>();
        
        // Initialize the map
        this.jMapPanel.setLayout(new BoxLayout(this.jMapPanel, BoxLayout.X_AXIS));
        map = new MapOsm(this.jMapPanel);
        this.textAreaOutput.setText("Please attach device\n");

        build=GitBuildInfo.getInstance();
        appName="GarminTrackConverter "+build.getGitCommitDescription()+" ("+build.getBuildTime()+")";        
        
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
        boolean                         localThreadExit;
        File                            deviceFile;
        boolean                         tracksShownLocal;
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
                localThreadExit     =threadExit;
                tracksShownLocal     =tracksShown;
            }
            
            if (!tracksShownLocal)
            {
                // Find an attached device; with multiple devices attached
                // the device with lowest prio value wins
                minPrio=Integer.MAX_VALUE;
                for (SettingsDevice settingsDevice : devices)
                {
                    deviceFile=new File(settingsDevice.deviceFile);
                    if (deviceFile.exists())
                    {
                        LOGGER.info("Found device {}", settingsDevice.name);
                        if (settingsDevice.devicePriority<minPrio)
                        {
                            attachedDevice  =settingsDevice;
                            minPrio=attachedDevice.devicePriority;
                        }
                    }
                }
                if (attachedDevice!=null)
                {
                    LOGGER.info("Attached device {}", attachedDevice.name);
                    this.textAreaOutput.append("Initializing...\n");
                    readWaypoints();

                    readDevice();
                    jTextFieldDevice.setText(device.getDeviceDescription());

                    SwingUtilities.invokeLater(() ->
                    {                         
                        File trackFile       =new File(attachedDevice.trackFilePath);
                        File routeFile       =new File(attachedDevice.routeFilePath);
                        File newFile         =new File(attachedDevice.newFilePath);
                        File locationFile    =new File(attachedDevice.locationFilePath);
                        trackModel.clear();
                        Comparator<File> c = Comparator.comparing((File x) -> x.getName());
                        Stream.of(trackFile.listFiles())
                                .filter(file -> !file.isDirectory())
                                .sorted(c.reversed())
                                .map(File::getName).forEach(file -> {trackModel.addElement(file);});

                        locationModel.clear();
                        Stream.of(locationFile.listFiles())
                                .filter(file -> !file.isDirectory())
                                .sorted(c)
                                .map(File::getName).forEach(file -> {locationModel.addElement(file);});

                        routeModel.clear();
                                    Stream.of(routeFile.listFiles())
                                .filter(file -> !file.isDirectory())
                                .sorted(c)
                                .map(File::getName).forEach(file -> {routeModel.addElement(file);});

                        newFileModel.clear();
                        Stream.of(newFile.listFiles())
                                .filter(file -> !file.isDirectory())
                                .filter(file -> file.getName().toLowerCase().endsWith(".gpx"))
                                .sorted(c)
                                .map(File::getName).forEach(file -> {newFileModel.addElement(file);});
                        jTrackList.setSelectedIndex(0);
                        
                    });

                    synchronized(this)
                    {
                        tracksShown=true;
                    }
                }
            } 
            else
            {
                deviceFile=new File(attachedDevice.deviceFile);
                if (!deviceFile.exists())
                {
                    this.textAreaOutput.setText("Please attach device\n");
                    SwingUtilities.invokeLater(() ->
                    {                          
                        trackModel.clear();
                        routeModel.clear();
                        newFileModel.clear();
                        locationModel.clear();
                        jTextFieldDevice.setText("");
                    });
                    synchronized(this)
                    {
                        map.hideTrack();
                        tracksShown=false;
                        attachedDevice=null;
                    }
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
        jLabel9 = new javax.swing.JLabel();
        jTextInfo = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

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

        jLabel9.setText("Info");

        jTextInfo.setEnabled(false);

        jMenu1.setText("File");

        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemExit);

        jMenuBar1.add(jMenu1);

        jMenuHelp.setText("Help");

        jMenuItem1.setText("About");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItem1);

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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                                    .addComponent(jLabel3)))
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(buttonSave)
                                .addGap(117, 117, 117)
                                .addComponent(buttonUpload)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonDelete)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldDevice, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 295, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextInfo))
                            .addComponent(jMapPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)))
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
                    .addComponent(jMapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                            .addComponent(jScrollPane2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel7))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonSave)
                    .addComponent(buttonUpload)
                    .addComponent(buttonDelete)
                    .addComponent(jLabel9)
                    .addComponent(jTextInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        
        // TO DO: generate a sensible filename
        returnFileName=fileNameProposal;
        if (returnFileName.equals(""))
        {
            fc.setCurrentDirectory(new File(directory));
        }
        else
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
                jTextInfo.setText(track.getTrackInfo());
            }
            else if (track.getWaypoints().size()>0)
            {
                map.showWaypoints(track.getWaypoints());
                jTextInfo.setText("Locations: "+track.getWaypoints().size());
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

        waypointsFile=attachedDevice.waypointFile;
        if (new File(waypointsFile).exists())
        {
            waypoints=new Locations(waypointsFile);
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
        deviceFile=attachedDevice.deviceFile;
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
    private Track getTrack(JList list, Map<String,Track> map)
    {
        Track track;
        track=null;
        
        int index=list.getSelectedIndex();
        if (index>=0)
        {
            String trackName=((DefaultListModel<String>)list.getModel()).elementAt(index);
            if (map.containsKey(trackName))
            {
                track=map.get(trackName);
            }
        }
        return track;
    }
    
    /**
     * Returns track from map
     * @param list List in which the track is selected
     * @param map Map containing the track
     * @return The track or null if not found
     */
    private Locations getWaypoints(JList list, Map<String,Locations> map)
    {
        Locations locations;
        locations=null;
        
        int index=list.getSelectedIndex();
        if (index>=0)
        {
            String waypointsName=((DefaultListModel<String>)list.getModel()).elementAt(index);
            if (map.containsKey(waypointsName))
            {
                locations=map.get(waypointsName);
            }
        }
        return locations;
    }
    
    /**
     * Handles the convert button
     * @param evt Button event
     */
    private void buttonSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonSaveActionPerformed
    {//GEN-HEADEREND:event_buttonSaveActionPerformed
        GpxWriter                   writer;        
        String                      fileName;
        String                      trackName;
        Track                       track;
        Locations                   locations;
            
        if (jTrackList.getSelectedIndex()>=0)
        {
            fileName=getGpxFileName(settings.getGpxFilePath(), "", "Save");
            if (fileName!=null)
            {
                writer=GpxWriter.getInstance();
                track=getTrack(jTrackList, tracksCache);
                writer.writeTrackToFile(fileName, track, "Track", appName);
                this.textAreaOutput.append("File saved to "+fileName+"\n");
                LOGGER.info("File saved to {}", fileName);
            }
        }
        else if (jNewFilesList.getSelectedIndex()>=0)
        {
            fileName=getGpxFileName(settings.getGpxFilePath(), "", "Save");
            if (fileName!=null)
            {
                writer=GpxWriter.getInstance();
                track=getTrack(jNewFilesList, newFilesCache);
                writer.writeTrackToFile(fileName, track, "Track", appName);
                this.textAreaOutput.append("Route saved to "+fileName+"\n");
                LOGGER.info("Route saved to {}", fileName);
            }
        }
        else if (jLocationList.getSelectedIndex()>=0)
        {
            fileName=getGpxFileName(settings.getGpxFilePath(), "", "Save");
            if (fileName!=null)
            {
                writer=GpxWriter.getInstance();
                locations=getWaypoints(jLocationList, locationsCache);
                writer.writeWaypointsToFile(fileName, waypoints);
                this.textAreaOutput.append("Route saved to "+fileName+"\n");
                LOGGER.info("Route saved to {}", fileName);
            }
        }
        else if (jRouteList.getSelectedIndex()>=0)
        {
            fileName=getGpxFileName(settings.getGpxFilePath(), "", "Save");
            if (fileName!=null)
            {
                writer=GpxWriter.getInstance();
                track=getTrack(jRouteList, routesCache);
                writer.writeTrackToFile(fileName, track, "Track", appName);
                this.textAreaOutput.append("Route saved to "+fileName+"\n");
                LOGGER.info("Route saved to {}", fileName);
            }
        }
    }//GEN-LAST:event_buttonSaveActionPerformed

    private void jTrackListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jTrackListValueChanged
    {//GEN-HEADEREND:event_jTrackListValueChanged
        int     index;
        String  fullFileName;
        String  fileName;
        Track   track;
        
        if (!evt.getValueIsAdjusting() && jTrackList.getSelectedIndex()>=0)
        {
            jRouteList.clearSelection();
            jNewFilesList.clearSelection();
            jLocationList.clearSelection();
            index=jTrackList.getSelectedIndex();
            fileName=trackModel.elementAt(index);
            fullFileName=attachedDevice.trackFilePath+"\\"+fileName;
            if (tracksCache.containsKey(fileName))
            {
                track=tracksCache.get(fileName);
            }
            else
            {
                track=readTrack(fullFileName, true);
                tracksCache.put(fileName, track);
            }
            jTextInfo.setText(track.getTrackInfo());
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
        Track   track;
        
        if (!evt.getValueIsAdjusting() && jRouteList.getSelectedIndex()>=0)
        {
            jTrackList.clearSelection();
            jNewFilesList.clearSelection();
            jLocationList.clearSelection();
            index=jRouteList.getSelectedIndex();
            fileName=routeModel.elementAt(index);
            fullFileName=attachedDevice.routeFilePath+"\\"+fileName;
            if (routesCache.containsKey(fileName))
            {
                track=routesCache.get(fileName);
            }
            else
            {
                track=readTrack(fullFileName, false);
                routesCache.put(fileName, track);
            }
            trackToMap(track);
        }
    }//GEN-LAST:event_jRouteListValueChanged

    private void jNewFilesListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jNewFilesListValueChanged
    {//GEN-HEADEREND:event_jNewFilesListValueChanged
        int     index;
        String  fullFileName;
        String  fileName;
        Track   track;
        
        if (!evt.getValueIsAdjusting() && jNewFilesList.getSelectedIndex()>=0)
        {
            jTrackList.clearSelection();
            jRouteList.clearSelection();
            jLocationList.clearSelection();
            
            index=jNewFilesList.getSelectedIndex();
            fileName=newFileModel.getElementAt(index);
            fullFileName=attachedDevice.newFilePath+"\\"+fileName;

            if (newFilesCache.containsKey(fileName))
            {
                track=newFilesCache.get(fileName);
            }
            else
            {
                track=GpxReader.getInstance().readRouteFromFile(fullFileName);
                newFilesCache.put(fileName, track);
            }
            trackToMap(track);
        }
    }//GEN-LAST:event_jNewFilesListValueChanged

    private void buttonUploadActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonUploadActionPerformed
    {//GEN-HEADEREND:event_buttonUploadActionPerformed
        String fileName;
        fileName=this.getGpxFileName(settings.getGpxFilePath(), "", "Upload");
        if (fileName!=null)
        {
            String destinationPath=attachedDevice.newFilePath;
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
                    synchronized(this)
                    {
                        tracksShown=false;
                    }
                    Track route=GpxReader.getInstance().readRouteFromFile(fileName);
                    if (route!=null)
                    {
                        trackToMap(route);
                        // TO DO: set index in file list, somewhere
                    }
                }
                catch (IOException e)
                {
                    LOGGER.error("Error copying file: {}", e.getMessage());
                }
            }
        }

    }//GEN-LAST:event_buttonUploadActionPerformed

    private void jLocationListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jLocationListValueChanged
    {//GEN-HEADEREND:event_jLocationListValueChanged
        if (!evt.getValueIsAdjusting() && jLocationList.getSelectedIndex()>=0)
        {
            Locations       points;
            String          fileName;
            String          fullFileName;
            int             index;
            
            jTrackList.clearSelection();
            jRouteList.clearSelection();
            jNewFilesList.clearSelection();
            
            index=jLocationList.getSelectedIndex();
            fileName=locationModel.getElementAt(index);
            fullFileName=attachedDevice.locationFilePath+"\\"+fileName;
            
            if (locationsCache.containsKey(fileName))
            {
                points=locationsCache.get(fileName);
            }
            else
            {
                LOGGER.info("Reading waypoints from {}", fullFileName);
                points=new Locations(fullFileName);
                locationsCache.put(fileName, points);
            }
            map.showWaypoints(points.getWaypoints());
            jTextInfo.setText("Locations: "+points.getNumberOfWaypoints());

        }
    }//GEN-LAST:event_jLocationListValueChanged

    private void buttonDeleteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonDeleteActionPerformed
    {//GEN-HEADEREND:event_buttonDeleteActionPerformed
        int     index; 
        String  pathName;
        String  fileName;

        pathName=null;
        fileName=null;
        if (jTrackList.getSelectedIndex()>=0)
        {
            fileName=jTrackList.getSelectedValue();
            pathName=attachedDevice.trackFilePath+"/"+fileName;
        }
        else if (jNewFilesList.getSelectedIndex()>=0)
        {
            fileName=jNewFilesList.getSelectedValue();
            pathName=attachedDevice.newFilePath+"/"+fileName;
        }
        else if (jLocationList.getSelectedIndex()>=0)
        {
            fileName=jLocationList.getSelectedValue();
            pathName=attachedDevice.locationFilePath+"/"+fileName;
        }
        else if (jRouteList.getSelectedIndex()>=0)
        {
            fileName=jRouteList.getSelectedValue();
            pathName=attachedDevice.routeFilePath+"/"+fileName;
        }

        
        if (pathName!=null)
        {
            if (showConfirmDialog("Are you sure to delete "+pathName+"?"))
            {
                try
                {
                    LOGGER.info("Deleting {}", pathName);
                    Files.delete(Paths.get(pathName));
                    this.textAreaOutput.append("Deleted "+fileName+"\n");
                    map.hideTrack();
                    this.tracksShown    =false;
                }
                catch (IOException e)
                {
                    LOGGER.error("Error deleting {}: {}", pathName, e.getMessage());
                    textAreaOutput.append("Error deleting "+fileName+"\n");
                }
            }
        }
    }//GEN-LAST:event_buttonDeleteActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExitActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExitActionPerformed
        this.dispose();
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem1ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem1ActionPerformed
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

    }//GEN-LAST:event_jMenuItem1ActionPerformed

    

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
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList<String> jList2;
    private javax.swing.JList<String> jLocationList;
    private javax.swing.JPanel jMapPanel;
    private javax.swing.JPanel jMapPanel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JList<String> jNewFilesList;
    private javax.swing.JList<String> jRouteList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTextField jTextFieldDevice;
    private javax.swing.JTextField jTextInfo;
    private javax.swing.JList<String> jTrackList;
    private javax.swing.JTextArea textAreaOutput;
    private javax.swing.JTextArea textAreaOutput1;
    // End of variables declaration//GEN-END:variables
}
