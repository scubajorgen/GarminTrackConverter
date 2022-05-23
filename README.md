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
  "gpxFileDownloadPath": "./development/gpx",
  "gpxFileUploadPath": "./development/gpxRoutes",
  "trackCompression": true,
  "trackCompressionMaxError": 0.01,
  "devices":
  [
    {
      "name": "fenix 7 Solar",
      "trackFilePath": "d:/gps/fenix/GARMIN/Activity",
      "routeFilePath": "d:/gps/fenix/GARMIN/Courses",
      "newFilePath": "d:/gps/fenix/GARMIN/NewFiles",
      "locationFilePath": "d:/gps/fenix/GARMIN/Location",
      "waypointFile": "d:/gps/fenix/GARMIN/Location/Lctns.fit",
      "deviceFile": "d:/gps/fenix/GARMIN/GarminDevice.xml",
      "syncCommand": "\"c:\\Program Files\\FreeFileSync\\FreeFileSync.exe\" SyncFenix.ffs_batch",
      "devicePriority": 2
    },
    {
      "name": "Edge 830",
      "trackFilePath": "f:/Garmin/Activities",
      "routeFilePath": "f:/Garmin/Courses",
      "newFilePath": "f:/Garmin/NewFiles",
      "locationFilePath": "f:/Garmin/Locations",
      "waypointFile": "f:/Garmin/Locations/Locations.fit",
      "deviceFile": "f:/Garmin/GarminDevice.xml",
      "syncCommand": "",
      "devicePriority": 1
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

Note that this program has only be tested with the Garmin **Edge810** and **Edge830** bike computers and the **Fenix 7**. 

## Compression
A feature is _track compression_ by means of the [Douglas-Peucker algorithm](https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm). Compressing a track means omitting trackpoints that do not contribute much to the track: if three trackpoints lie more or less on a line, the trackpoint that is in the middle can be omitted without changing the track to much. 

If on the device a log frequency of trackpoints is set to 'smart', the device compresses the track. However, on the Fenix 7 every second a trackpoint is logged even when 'smart logging' is chosen for Open Water Swimming. Here comes in the compression feature.

Compressing can be switched on by checking the 'Save compressed track' checkbox. The setting 'trackCompression' in the settings file can be used to have it checked by default. 

The algorithm requires a maximum allowable error value. This can be defined in the settings file by 'trackCompressionMaxError': the larger the value the higher the compression ratio but the more deviation occurs. A value of 0.01 gives good results.

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
Unfortunatelly, the Fenix 7 cannot be attached to USB as mass storage device. Instead, it is mounted using MTP (Media Transfer Protocol). Under Windows it is mapped under 'This PC' and **not** accessible from Java programs. 

I tried [Mtpdrive](https://www.mtpdrive.com/) which is a program that assigns a drive letter to an MTP device so it should be accessible as regular disk. However, it is quircky in combination with Java file I/O. Sometimes Java file I/O is exteremely slowly. And each time an MTP device is attached the mapping must be made manually. Not workable.

I came up with a solution using an external file synchronization program [FreeFileSync](https://freefilesync.org/) that syncs the device to a local directory structure on your HDD. You can define a commandline file sync command with each device in the settings file. If it is defined (i.e. not equal to ""), a sync button becomes visible which you can use to sync to and from the device. Enclosed in the source code is a FreeFileSync batch file that can be executed to sync. Adapt it for your own usage.

## Information
* [Blog](http://blog.studioblueplanet.net/?page_id=468)
* [Source](https://github.com/scubajorgen/GarminTrackConverter)
