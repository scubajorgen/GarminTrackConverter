Garmin Track Converter
======================
The Garmin Track Converter is intended to convert ANT/Garmin .FIT track log files to GPX 1.1 format. 
It has been created for and tested with the Garmin Edge 810 bike computer, but it might be useful for other 
Garmins as well.

Garmin .FIT tracks do not contain marked waypoints. These are stored in a separate file (max. 100 waypoints).
On the Garmin Edge810 this file is Locations.fit.
During conversion of the track, the converter checks the waypoint file and incorporates waypoints in the GPX
that were logged during the track.


Building
========
Use Maven to compile the source files into /target. The project is recognized by Netbeans as Maven project and 
can be imported.


Running
=======
Go to /target directory. Run 'java -jar GarminTrackConverter<x.y>.jar, where x.y is the version.


Configuration
=============
Configuration can be done with the garmintrackconverter.properties file. There are two version:
- /garmintrackconverter.properties                             Used during development. You can use testdirs
- /src/main/resources_run/garmintrackconverter.properties      Used for 'production'. This one ends up in the /target dir.

The configuration requests 4 paths:
- trackFilePath         Path where the tracks are stored
- deviceFile            The location of the device file, used for the serial of the Garmin device
- waypointFile          File storing the waypoints
- gpxFilePath           Path to store the GPX output files

Configuration files are self explanatory.

Dependencies
============
The software uses 
- the FitReader project (https://github.com/scubajorgen/FitReader)
- hirondelle-date4j-1.5.1.jar
- appframework-1.0.3.jar
- swing-worker-1.1.jar


Information
===========
Blog:   http://blog.studioblueplanet.net/?page_id=468
Source: https://github.com/scubajorgen/GarminTrackConverter