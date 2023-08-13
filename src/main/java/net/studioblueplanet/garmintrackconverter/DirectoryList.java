/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author jorgen
 */
public class DirectoryList
{
    private final static Logger             LOGGER = LogManager.getLogger(DirectoryList.class);
    private final String                    directory;
    private final List<DirectoryListItem>   fileList;
    private final File                      directoryFile;
    private final boolean                   sortAscending;
    private final DefaultListModel<String>  model;
    private final JList<String>             list;
    
    /**
     * Constructor; retrieves the files in given directory in the order indicated
     * @param directory Directory path
     * @param sortAscending True if the files must be sorted in ascending order, false
     *                      if to be sorted in REVERSE order
     */
    public DirectoryList(String directory, JList<String> list, boolean sortAscending)
    {
        this.directory      =directory;
        this.directoryFile  =new File(directory);   
        fileList=new ArrayList<>();
        this.updateDirectoryList();
        this.list           =list;
        this.model          =new DefaultListModel<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setModel(model);
        this.sortAscending  =sortAscending;
    }
    
    /**
     * Retrieves a list of files. Filter only files that have a size>0
     * to filter out files that are being transferred
     * @return The list of file names (without path)
     */
    private List<String> retrieveDirectoryFileList()
    {
        List<String> files;
               
        files=new ArrayList<>();
        Stream.of(directoryFile.listFiles())
                .filter(file -> !file.isDirectory() && file.length()>0 && file.getAbsolutePath().endsWith(".fit"))
                .map(File::getName)
                .forEach(file -> {files.add(file);});          
        return files;
    }      
    
    /**
     * 
     * @return 
     */
    private boolean fileExists(String fileName)
    {
        List<DirectoryListItem> files=fileList.stream()
                                              .filter(file -> file.getFilename().equals(fileName))
                                              .collect(Collectors.toList());
        if (files.size()>1)
        {
            LOGGER.error("Filename {} occurs multiple times in list", fileName);
        }
        return files.size()>0;
    }
    
    /**
     * Updates the file list
     * @return True if the current set of files differ from the previously retrieved
     *         list, false if they are equal
     */
    public boolean updateDirectoryList()
    {
        boolean         isUpdated=false;
        List<String>    files=retrieveDirectoryFileList();
               
        // Keep existing items (inner join)
        List<DirectoryListItem> existingItems= fileList
                .stream()
                .filter(item -> files.contains(item.getFilename()))
                .collect(Collectors.toList());
        
        if (existingItems.size()!=files.size() || files.size()!=fileList.size())
        {
            // New files found        
            List<String> newFilenames=files
                    .stream()
                    .filter(item -> !fileExists(item))
                    .collect(Collectors.toList());
            
            // Copy both to new list
            List<DirectoryListItem> allItems=existingItems
                    .stream()
                    .collect(Collectors.toList());
            newFilenames
                    .stream()
                    .forEach(filename -> allItems.add(new DirectoryListItem(filename))); 
            
            // Now sort and copy
            fileList.clear();
            Comparator<DirectoryListItem> c=Comparator.comparing(DirectoryListItem::getFilename);
            if (!sortAscending)
            {
                c=c.reversed();
            }
            allItems
                    .stream()
                    .sorted(c)
                    .forEach(item -> fileList.add(item));
            isUpdated=true;
        }

        return isUpdated;
    }
    
    /**
     * Add the files to the model
     * @param extension Extension of the files to add
     */
    public void addFilesToListModel(String extension)
    {
        fileList.stream()
                .filter(file -> file.getFilename().toLowerCase().endsWith(extension))
                .forEach(file -> {model.addElement(file.getDescription());});
    }
    
    /**
     * Clear the list model
     */
    public void clear()
    {
        model.clear();
    }
    
    /**
     * Set the index in the file list
     * @param index The index to set
     */
    public void setSelectedIndex(int index)
    {
        list.setSelectedIndex(index);
    }
    
    /**
     * Clear the selection, i.e. remove selected row highlight
     */
    public void clearSelection()
    {
        list.clearSelection();
    }
    
    /**
     * Indicates if a row has been selected in the list
     * @return True if selected, false if not
     */
    public boolean hasSelection()
    {
        return (list.getSelectedIndex()>=0);
    }
    
    /**
     * Retrieve the number of items in the list
     * @return The number of items
     */
    public int size()
    {
        return model.size();
    }
    
    /**
     * Get the filename of the indicated row
     * @return The filename
     */
    public String getSelectedFileName()
    {
        int index=list.getSelectedIndex();
        return getFileName(index);
    }
    
    /**
     * Get the filename of the entry at given index
     * @param index Index in the list
     * @return The filename of the item
     */
    public String getFileName(int index)
    {
        return fileList.get(index).getFilename();
    }
    
    /**
     * Add a track/waypoints to the selected index
     * @param item The track/waypoints to add
     */
    public void addCacheableItem(CacheableItem item)
    {
        int index=list.getSelectedIndex();
        if (index>=0)
        {
            fileList.get(index).setCachedItem(item);
     
            model.setElementAt(fileList.get(index).getDescription(), index);

        }       
    }
    
    /**
     * Add a track/lcoations to the indicated index
     * @param item The track/locations to add
     * @param index Index of the directory item to add the track to
     */
    public void addCacheableItem(CacheableItem item, int index)
    {
        if (index<fileList.size())
        {
            fileList.get(index).setCachedItem(item);
            model.setElementAt(fileList.get(index).getDescription(), index);

        }
        else
        {
            LOGGER.error("");
        }
    }

    /**
     * Get the track of the select index
     * @return The track or null if not defined
     */
    public CacheableItem getCacheableItem()
    {
        CacheableItem item=null;
        if (list.getSelectedIndex()>=0)
        {
            int index=list.getSelectedIndex();
            item=fileList.get(index).getCachedItem();
        }  
        return item;
    }
    
    /**
     * Get first position in the list that does not contain a cached item.
     * @return The first position or -1 if not found.
     */
    public int getNextNonCache()
    {
        int     index=-1;
        boolean found=false;
        
        for (int i=0; i<fileList.size() && !found;i++)
        {
            if (fileList.get(i).getCachedItem()==null)
            {
                index=i;
                found=true;
            }
        }
        return index;
    }
}
