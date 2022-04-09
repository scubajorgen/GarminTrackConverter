# Garmin Track Converter
## Introduction
The Garmin Track Converter is an application intended to convert ANT/Garmin .FIT track log files from an attached Garmin device to GPX 1.1 format. 
It has been created for and tested with the Garmin Edge 810/830 bike computer and Garmin Fenix 7, but it might be useful for other Garmins devices as well.

Garmin .FIT tracks do not contain marked waypoints. These are stored in a separate file(max. 100 waypoints).
On the Garmin Edge810 this file is Locations.fit, on the Fenix Lctns.fit.
During conversion of the track, the converter checks the waypoint file and incorporates waypoints in the GPX that were logged during recording of the track.

![](image/GarminTrackConverter.png)

Features
* Conversion of activity fit files to GPX 1.1
* Including waypoints logged during the activity
* Start/stop events are written to trkseg GPX segments
* Device ID/serial is included in the GPX
* Upload of waypoints and routers in GPX format (New Files)

## Building
Use Maven to compile the source files into /target. The project is recognized by Netbeans as Maven project and can be imported. It uses the [FitReader library](https://github.com/scubajorgen/FitReader), so be sure to import and build this project first. Manually building:

```
mvn clean install
```

## Configuring
The application requires a configuration file garmintrackconverter.json. 
In this file the directories are defined on the device and where the GPX files should be written to. It appears that various types of Garmin devices have slightly different file structures. Therefore, multiple devices can be defined 
in the configuration.

```
{
    "debugLevel":"info",
    "gpxFilePath": "./development/gpx",
    "devices":
    [
      {
        "name": "fenix 7 Solar",
        "trackFilePath": "./development/device_fenix7/GARMIN/Activity",
        "routeFilePath": "./development/device_fenix7/GARMIN/Courses",
        "newFilePath": "./development/device_fenix7/GARMIN/NewFiles",
        "locationFilePath": "./development/device_fenix7/GARMIN/Location",
        "waypointFile": "./development/device_fenix7/GARMIN/Location/Lctns.fit",
        "deviceFile": "./development/device_fenix7/GARMIN/GarminDevice.xml",
        "devicePriority": 3
      },
      {
        "name": "Edge 830",
        "trackFilePath": "./development/device_edge830/Garmin/Activities",
        "routeFilePath": "./development/device_edge830/Garmin/Courses",
        "newFilePath": "./development/device_edge830/Garmin/NewFiles",
        "locationFilePath": "./development/device_edge830/Garmin/Locations",
        "waypointFile": "./development/device_edge830/Garmin/Location/Locations.fit",
        "deviceFile": "./development/device_edge830/Garmin/GarminDevice.xml",
        "devicePriority": 2
      }
    ]
}

```

It requires from the device:
* The directory containing the tracks (activities, \Garmin\activities)
* The file containing the device info (Garmin\GarminDevice.xml)
* The waypoint file (\Garmin\Locations\Locations.fit)

In the directory /development example file structures are available for the Garmin Edge 830 (/development/device_edge830) and Garmin Fenix 7 (/development/device_fenix7). The files and directories are (partly) copied from real devices.

## Executing
Go to /target directory. Run 'java -jar GarminTrackConverter<x.y>.jar, where x.y is the version. There is also a jar file containing all depenedencies.

If all is configured properly, you see four panels to the left, one map panel to the right and one info box at the bottom.

The four panels represent:
* The logged activities (top left)
* The courses on the the device (top right)
* The logged waypoints file (bottom left)
* The new uploaded files (bottom right)

Clicking any of the files shown shows the contents on the map on the right side of the screen.

Buttons: 
* Save GPX: saves the last clicked track/activity
* Upload: Uploads a .gpx file containing trk, rte or wpt
* Delete: Delete the selected file

Note that this program has only be tested with the Garmin **Edge810** and **Edge830** bike computers and the Fenix 7. 

## Development
The software was developed using Apache Netbeans. The Maven project can be run or debugged from Netbeans. For developement, a directory /development is available. It contains in /development/Garmin a copy of the filestructure from a Garmin Edge830 device, including some logged activities, courses and locations. The folder /development/gpx can be used to store GPX files.

The GarminTrackConverter.properties file in the root of the project is used by Netbeans and is configured to use the /development folder 

## Dependencies
The software uses 
- [the FitReader project](https://github.com/scubajorgen/FitReader)
- hirondelle-date4j-1.5.1.jar
- appframework-1.0.3.jar
- swing-worker-1.1.jar
- ...

## Remark on the Fenix 7
Unfortunatelly, the Fenix 7 cannot be attached to USB as mass storage device. Instead, it is mounted using MTP (Media Transfer Protocol). Under Windows it is mapped under 'This PC' and not accessible from Java programs. Therefore I use [Mtpdrive](https://www.mtpdrive.com/). Mtpdrive is a commercial tool by which it is possible to assign a drive letter to an MTP device. When assigned, the file structure is accessible by GarminTrackConverter.

## Information
* [Blog](http://blog.studioblueplanet.net/?page_id=468)
* [Source](https://github.com/scubajorgen/GarminTrackConverter)
