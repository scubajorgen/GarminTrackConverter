/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.garmintrackconverter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;

/**
 *
 * @author jorgen
 */
public class MapOsm extends Map
{
    /**
     * Inner class representing a track segment to display
     */
    private class OsmTrackSegment
    {
        private final   List<GeoPosition>   segment;
        private         double              leftBound;
        private         double              rightBound;
        private         double              upperBound;
        private         double              lowerBound;
        
        public OsmTrackSegment()
        {
            segment     =new ArrayList<>();
            leftBound   =180.0;
            rightBound  =-180.0;
            lowerBound  =180.0;
            upperBound  =-180.0;
        }
        
        public void add(GeoPosition position)
        {
            double lat;
            double lon;
            
            segment.add(position);
            
            lat=position.getLatitude();
            lon=position.getLongitude();
            if (lat<lowerBound)
            {
                lowerBound=lat;
            }
            if (lat>upperBound)
            {
                upperBound=lat;
            }
            if (lon>rightBound)
            {
                rightBound=lon;
            }
            if (lon<leftBound)
            {
                leftBound=lon;
            }
        }
        
        public List<GeoPosition> getPositions()
        {
            return segment;
        }
        
        public int size()
        {
            return segment.size();
        }
        
        public double getLeftBound()
        {
            return leftBound;
        }
        
        public double getRightBound()
        {
            return rightBound;
        }
        
        public double getUpperBound()
        {
            return upperBound;
        }

        public double getLowerBound()
        {
            return lowerBound;
        }
    }

    
    /**
     * Inner class representing a track to display
     */
    private class OsmTrack
    {
        private final List<OsmTrackSegment> segments;
        private final Set<Waypoint>         waypoints;
        
        public OsmTrack()
        {
            segments       =new ArrayList<>();
            // Create waypoints from the geo-positions
            waypoints               = new HashSet<>();
        }
        
        public void add(OsmTrackSegment segment)
        {
            segments.add(segment);
        }
      
        public void add(Waypoint waypoint)
        {
            waypoints.add(waypoint);
        }
        
        public List<OsmTrackSegment> getSegments()
        {
            return segments;
        }
        
        public Set<Waypoint> getWaypoints()
        {
            return waypoints;
        }


        public int segmentSize()
        {
            return segments.size();
        }
        
        public int waypointSize()
        {
            return waypoints.size();
        }
        
        /**
         * Return the bounds of the track as a list of two coordinates: 
         * upper left and lower right
         * @return The bounds 
         */
        public List<GeoPosition> getBounds()
        {
            ArrayList<GeoPosition> bounds;
            
            bounds=new ArrayList<GeoPosition>();
            
            for (OsmTrackSegment segment : segments)
            {
                bounds.add(new GeoPosition(segment.getUpperBound(), segment.getLeftBound()));
                bounds.add(new GeoPosition(segment.getLowerBound(), segment.getRightBound()));
            }
            
            for (Waypoint waypoint : waypoints)
            {
                bounds.add(waypoint.getPosition());
            }
            return bounds;
        }
    }
    
    /**
     * Paints a track
     * @author Martin Steiger, Jörgen 
     */
    private class RoutePainter implements Painter<JXMapViewer>
    {
        private final Color     color = Color.RED;
        private final boolean   antiAlias = true;
        private final OsmTrack  track;

        /**
         * @param track the track
         */
        public RoutePainter(OsmTrack track)
        {
            this.track = track;
        }

        @Override
        public void paint(Graphics2D g, JXMapViewer map, int w, int h)
        {
            g = (Graphics2D) g.create();

            // convert from viewport to world bitmap
            Rectangle rect = map.getViewportBounds();
            g.translate(-rect.x, -rect.y);

            if (antiAlias)
            {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            
            // do the drawing
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(4));

            drawRoute(g, map, false);

            // do the drawing again
            g.setStroke(new BasicStroke(2));

            drawRoute(g, map, true);

            g.dispose();
        }

        /**
         * @param g the graphics object
         * @param map the map
         */
        private void drawRoute(Graphics2D g, JXMapViewer map, boolean useColor)
        {
            int     lastX;
            int     lastY;
            int     segmentCount;

            boolean first;
            
            segmentCount=0;
            for (OsmTrackSegment segment : track.getSegments())
            {
                first   =true;
                lastX   =0;
                lastY   =0;

                // Choose color per segment
                if (useColor)
                {
                    if (segmentCount%2>0)
                    {
                        g.setColor(Color.BLUE);
                    }
                    else
                    {
                        g.setColor(Color.RED);
                    }
                }
                for (GeoPosition gp : segment.getPositions())
                {
                    // convert geo-coordinate to world bitmap pixel
                    Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
                    }

                    lastX = (int) pt.getX();
                    lastY = (int) pt.getY();
                }
                segmentCount++;
            }
        }
    }
    
    private JXMapViewer             mapViewer;    
    
    /**
     * Constructor
     * @param panel Panel to show the map on 
     */
    public MapOsm(JPanel panel)
    {
        super(panel);
        mapViewer = new JXMapViewer();

        // Create a TileFactoryInfo for OpenStreetMap
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
        
        
        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));        
        panel.add(mapViewer);

        hideTrack();
    }
            
    
    
    
    
    private void initializeOverlayPainter(OsmTrack track)
    {
        RoutePainter                routePainter;
        WaypointPainter<Waypoint>   waypointPainter;
        
        // Create a waypoint painter that takes all the waypoints
        waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(track.getWaypoints());         

        routePainter    = new RoutePainter(track);        
        
        // Set the focus
        mapViewer.zoomToBestFit(new HashSet<>(track.getBounds()), 0.9);
        
        // Create a compound painter that uses both the route-painter and the waypoint-painter
        List<Painter<JXMapViewer>> painters;
        painters = new ArrayList<>();
        painters.add(routePainter);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);         
    }
    
    
   /**
     * This method show the track in this frame on a google map
     * @param activity The activity data structure containing the track (Activity) to show
     * @return A string indicating the result of the showing (ToDo: remove or make sensible value).
     */
    @Override
    public String showTrack(Track activity)
    {
        double                  lat;
        double                  lon;

        List<TrackPoint>        trackPoints;
        //TrackPoint              gpsRecord;
        int                     numOfSegments;
        int                     segment;
        int                     numOfRecords;
        //int                     record;
        boolean                 firstWaypointShown;
        OsmTrack                track;
        OsmTrackSegment         trackSegment;
        int                     points;
        
        // Create a track from the geo-positions
        track                   =new OsmTrack();

        firstWaypointShown      =false;
        lat                     =0.0;
        lon                     =0.0;
        points                  =0;

        numOfSegments=activity.getNumberOfSegments();
        segment=0;
        while (segment<numOfSegments)
        {
            trackPoints         =activity.getTrackPoints(segment);
            numOfRecords        =trackPoints.size();
            trackSegment        =new OsmTrackSegment();
            for (TrackPoint gpsRecord : trackPoints)
            {
                lat         =gpsRecord.getLatitude();
                lon         =gpsRecord.getLongitude();
                
                // Sometime the tomtom registers a (0,0) coordinate
                // Only show normal, non zero, coordinates
                if ((lat!=0.0) && (lon!=0.0))
                {
                    trackSegment.add(new GeoPosition(lat, lon));
                    // Show first point of each segment
                    if (!firstWaypointShown)
                    {
                        track.add(new DefaultWaypoint(lat, lon));
                        firstWaypointShown=true;
                    }
                    points++;
                }
            }
            
            track.add(trackSegment);
            segment++;
        }     
        
        // Show last waypoint
        if (firstWaypointShown && lat!=0.0 && lon!=0.0)
        {
            track.add(new DefaultWaypoint(lat, lon));
        }
        
        for (Location point : activity.getWaypoints())
        {
            track.add(new DefaultWaypoint(point.getLatitude(), point.getLongitude()));
        }
        
        // No points = show default image
        if (points==0 && track.waypointSize()==0)
        {
            this.hideTrack();
        }
        else
        {
            this.initializeOverlayPainter(track);
        }
        return "";
    }
    
    @Override
    public String showWaypoints(List<Location> waypoints)
    {
        OsmTrack                track;
        
        track=new OsmTrack();
        
        if (waypoints.size()>0)
        {
            waypoints.forEach((point) ->
            {
                track.add(new DefaultWaypoint(point.getLatitude(), point.getLongitude()));
            });
            this.initializeOverlayPainter(track);
        }
        else
        {
            this.hideTrack();
        }
        return "";
    }


    /**
     * Hides the track
     */
    @Override
    public void hideTrack()
    {
        mapViewer.setZoom(15);
        mapViewer.setAddressLocation(new GeoPosition(53.252, 6.588));          
        mapViewer.setOverlayPainter(null);
    }
}
