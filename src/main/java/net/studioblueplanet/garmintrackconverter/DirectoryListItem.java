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
    private String           filename;
    private CacheableItem    cachedItem;

    
    /**
     * Constructor. Each DirectoryListItem at least must have a filename
     * @param filename The filename
     */
    public DirectoryListItem(String filename)
    {
        this.filename=filename;
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
     * Returns the cached item added
     * @return The cached item or null if no item had been added.
     */
    public CacheableItem getCachedItem()
    {
        return cachedItem;
    }

    /**
     * Set the cached item to the DirectoryListItem
     * @param cachedItem Cached item to add
     */
    public void setCachedItem(CacheableItem cachedItem)
    {
        this.cachedItem = cachedItem;
    }
    
    /**
     * Returns the descriptionof the DirectoryListItem
     * @return A string. Filename + sport + distance
     */
    public String getDescription()
    {
        String description=filename;
        if (cachedItem!=null)
        {
            if (cachedItem instanceof Track)
            {
                Track t=(Track)cachedItem;
                String sport=t.getSport();
                Double dist =t.getDistance();
                if (dist!=null && sport!=null)
                {
                    description+=String.format("%10s %5.1f km", sport, dist/1000);
                }
                else
                {
                    description+=" c";
                }
            }
        }
        return description;
    }
}
