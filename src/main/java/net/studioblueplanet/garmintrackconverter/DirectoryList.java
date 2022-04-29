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
import java.util.stream.Stream;
import javax.swing.DefaultListModel;

/**
 *
 * @author jorgen
 */
public class DirectoryList
{
    private final String            directory;
    private final List<String>      fileList;
    private final File              directoryFile;
    private final boolean           sortAscending;
    
    /**
     * Constructor; retrieves the files in given directory in the order indicated
     * @param directory Directory path
     * @param sortAscending True if the files must be sorted in ascending order, false
     *                      if to be sorted in REVERSE order
     */
    public DirectoryList(String directory, boolean sortAscending)
    {
        this.directory      =directory;
        this.directoryFile  =new File(directory);        
        this.fileList       =retrieveDirectoryFileList();
        this.sortAscending  =sortAscending;
    }
    
    /**
     * Retrieves a list of files
     * @return The list of file names (without path)
     */
    private List<String> retrieveDirectoryFileList()
    {
        List<String> files;
        Comparator<File> c;
                
        files=new ArrayList<>();

        c = Comparator.comparing((File x) -> x.getName());
        if (!sortAscending)
        {
            c=c.reversed();
        }
        Stream.of(directoryFile.listFiles())
                .filter(file -> !file.isDirectory())
                .sorted(c)
                .map(File::getName).forEach(file -> {files.add(file);});          
        return files;
    }      
    
    /**
     * Updates the file list
     * @return True if the current set of files differ from the previously retrieved
     *         list, false if they are equal
     */
    public boolean updateDirectoryList()
    {
        boolean         isUpdated;
        List<String>    files;
        
        isUpdated=false;
        files=retrieveDirectoryFileList();
        
        if (files.size()==fileList.size())
        {
            if (!files.equals(fileList))
            {
                isUpdated=true;
            }
        }
        else
        {
            isUpdated=true;
        }
               
        if (isUpdated)
        {
            fileList.clear();
            fileList.addAll(files);
        }
        return isUpdated;
    }
    
    public void addFilesToListModel(DefaultListModel<String> model, String extension)
    {
        fileList.stream().filter(file -> file.toLowerCase().endsWith(extension)).forEach(file -> {model.addElement(file);});
    }
            
}
