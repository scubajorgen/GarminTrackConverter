# Garmin Track Converter
## Introduction
The Garmin Track Converter is an application intended to convert ANT/Garmin .FIT track log files from an attached Garmin device to GPX 1.1 format. 
It has been created for and tested with the Garmin Edge 810/830 bike computer, but it might be useful for other Garmins devices as well.

Garmin .FIT tracks do not contain marked waypoints. These are stored in a separate (max. 100 waypoints).
On the Garmin Edge810 this file is Locations.fit.
During conversion of the track, the converter checks the waypoint file and incorporates waypoints in the GPX that were logged during recording of the track.

![](image/GarminTrackConverter.png)

Features
* Conversion of activity fit files to GPX 1.1
* Including waypoints logged during the activity
* Laps are written to trkseg GPX segments
* Device ID/serial is included in the GPX


## Building
Use Maven to compile the source files into /target. The project is recognized by Netbeans as Maven project and can be imported. It uses the [FitReader library](https://github.com/scubajorgen/FitReader), so be sure to import and build this project first.

## Configuring
The application requires a configuration file garmintrackconverter.properties. 
In this file the directories are defined on the device and where the GPX files should be written to.

```
###################################################################################################
# This setting defines the device file (xml)
# Usually it is in the directory \GarminDevice.xml
deviceFile=f\:\\Garmin\\GarminDevice.xml

###################################################################################################
# This setting defines the default waypoint file
# Usually it is in the directory \garmin\Locations\Locations.fit
waypointFile=f\:\\Garmin\\Locations\\Locations.fit

###################################################################################################
# This setting defines the default path where to find the garmin track files
# Usually it is in the directory \garmin\activities
trackFilePath=f\:\\Garmin\\activities\\

###################################################################################################
# This setting defines the default path where to write the GPX file to
gpxFilePath=d\:\\gps\\gpx\\

```

It requires from the device:
* The directory containing the tracks (activities, \Garmin\activities)
* The file containing the device info (Garmin\GarminDevice.xml)
* The waypoint file (\Garmin\Locations\Locations.fit)

## Executing
Go to /target directory. Run 'java -jar GarminTrackConverter<x.y>.jar, where x.y is the version. There is also a jar file containing all depenedencies.


## Dependencies
The software uses 
- [the FitReader project](https://github.com/scubajorgen/FitReader)
- hirondelle-date4j-1.5.1.jar
- appframework-1.0.3.jar
- swing-worker-1.1.jar


## Information
* [Blog](http://blog.studioblueplanet.net/?page_id=468)
* [Source](https://github.com/scubajorgen/GarminTrackConverter)
