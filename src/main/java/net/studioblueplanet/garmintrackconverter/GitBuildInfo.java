/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;


import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author jorgen
 */
public class GitBuildInfo
{
    private final static Logger LOGGER = LogManager.getLogger(GitBuildInfo.class);
    private Properties          buildProperties;

    private static GitBuildInfo theInstance;
    
    /**
     * Constructor
     */
    private GitBuildInfo()
    {
        try 
        {
            buildProperties=new Properties();
            buildProperties.load(getClass().getClassLoader().getResourceAsStream("build.properties"));
        } 
        catch (IOException e) 
        {
            LOGGER.error("Error loading build properties: "+e.getMessage());
        }        
    }
    
    /**
     * Get the one and only instance of this Singleton class
     * @return The instance
     */
    public static GitBuildInfo getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new GitBuildInfo();
        }
        return theInstance;
    }
    
    /**
     * Get the closest commit ID
     * @return Commit ID
     */
    public String getGitCommit()
    {
        return buildProperties.getProperty("git.commit.id");
    }
    
    /**
     * Get a description of the commit
     * @return Description of the commit
     */
    public String getGitCommitDescription()
    {
        return buildProperties.getProperty("git.commit.id.describe");
    }
    
    /**
     * Return the time of the build
     * @return Build time
     */
    public String getBuildTime()
    {
        return buildProperties.getProperty("git.build.time");
    }
    
    /**
     * Return the closest tag name
     * @return The tag name.
     */
    public String getClosestTagName()
    {
        return buildProperties.getProperty("git.closest.tag.name");
    }
    
}
