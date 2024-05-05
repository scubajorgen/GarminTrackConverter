/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.studioblueplanet.garmintrackconverter;

/**
 * Item in the DirectoryList. It contains the filename and the cached file can
 * be added.
 * @author jorgen
 */
public class DirectoryListItem
{
    private final String     filename;
    private Track            track;
    private final long       filesize;
    
    /**
     * Constructor. Each DirectoryListItem at least must have a filename
     * @param filename The filename
     * @param filesize Size of the file
     */
    public DirectoryListItem(String filename, long filesize)
    {
        this.filename    =filename;
        this.filesize=filesize;
    }
    
    /**
     * Returns the filename
     * @return The filename as string
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * Returns the file size of the file
     * @return The file size
     */
    public long getFilesize()
    {
        return filesize;
    }
    
    /**
     * Returns the cached item added
     * @return The cached item or null if no item had been added.
     */
    public Track getTrack()
    {
        return track;
    }

    /**
     * Set the cached item to the DirectoryListItem
     * @param track Cached item to add
     */
    public void setTrack(Track track)
    {
        this.track = track;
    }
    
    /**
     * Returns the description of the DirectoryListItem
     * @return A string. Filename + sport + distance
     */
    public String getDescription()
    {
        String description=filename;
        if (track!=null)
        {
            Track t=(Track)track;
            String sport=t.getSport();
            Double dist =t.getDistance();
            if (dist!=null && sport!=null)
            {
                description+=String.format("%10s %5.1f km", sport, dist/1000);
            }
            else if (dist!=null)
            {
                description+=String.format("%5.1f km", dist/1000);
            }
            else
            {
                description+=" c";
            }
        }
        return description;
    }
}
