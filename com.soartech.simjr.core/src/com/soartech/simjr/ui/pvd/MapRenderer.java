/*
 * Copyright (c) 2010, Soar Technology, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of Soar Technology, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without the specific prior written permission of Soar Technology, Inc.
 *
 * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on Jun 14, 2007
 */
package com.soartech.simjr.ui.pvd;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.AttributionSupport;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JobDispatcher;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.Scalar;

public class MapRenderer implements TileLoaderListener
{
    private static final Logger logger = Logger.getLogger(MapRenderer.class);
    
    //Vectors for clock-wise tile painting
    protected static final Point[] move = { new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1) };
    
    private static final int DOWNLOAD_THREAD_COUNT = 8;
    
    private static final boolean tileGridVisible = true;
    private static final boolean scrollWrapEnabled = false;
    
    public static final int MAX_ZOOM = 22;
    public static final int MIN_ZOOM = 0;
    
    private TileController tileController;
    private TileSource tileSource;
    
    //private Point center = new Point(0,0);
    private int zoom = 10;
    
    private final PlanViewDisplay renderTarget;
    
    //Responsible for displaying correct map imagery attribution
    private AttributionSupport attribution = new AttributionSupport();
    
    public MapRenderer(PlanViewDisplay renderTarget)
    {
        JobDispatcher.setMaxWorkers(DOWNLOAD_THREAD_COUNT);
        tileSource = new OsmTileSource.Mapnik();
        tileController = new TileController(tileSource, new MemoryTileCache(), this);
        
        this.renderTarget = renderTarget;
    }
    
    public int getZoom() { return this.zoom; }
    
    public void setZoom(int zoom) 
    {
        logger.info("Setting zoom level to: " + zoom);
        this.zoom = zoom;
    }
    
    public void setTileSource(TileSource source)
    {
        if (tileSource.getMaxZoom() > MAX_ZOOM) {
            throw new RuntimeException("Maximum zoom level too high");
        }
        if (tileSource.getMinZoom() < MIN_ZOOM) {
            throw new RuntimeException("Minumim zoom level too low");
        }
        
        //Coordinate position = getPosition();
        this.tileSource = source;
        tileController.setTileSource(tileSource);
        //zoomSlider.setMinimum(tileSource.getMinZoom());
        //zoomSlider.setMaximum(tileSource.getMaxZoom());
        tileController.cancelOutstandingJobs();
        if (zoom > tileSource.getMaxZoom()) {
            setZoom(tileSource.getMaxZoom());
        }
        attribution.initialize(tileSource);
        //setDisplayPosition(position, zoom);
        renderTarget.repaint();
    }
    
    public void setTileLoader(TileLoader loader)
    {
        tileController.setTileLoader(loader);
    }

    @Override
    public void tileLoadingFinished(Tile tile, boolean success)
    {
        renderTarget.repaint();
    }

    @Override
    public TileCache getTileCache()
    {
        return tileController.getTileCache();
    }
    
    public int getTileSize()
    {
        return tileSource.getTileSize();
    }
    
    public void paint(Graphics2D g)
    {
        AffineTransform current = g.getTransform();
        double scale = getScaleFactor();
        g.transform(AffineTransform.getScaleInstance(scale, scale));
        
        Point center = getCenter();              // center pt in tile grid pixels
        int tilesize = tileSource.getTileSize(); // size of each tile in pixels
        int tilex = center.x / tilesize;         // center x in tile coords
        int tiley = center.y / tilesize;         // center y in tile coords
        int off_x = (center.x % tilesize);       // distance in px from center pt to left edge of tile
        int off_y = (center.y % tilesize);       // distance in px from center pt to top edge of tile

        int w2 = (int) ((renderTarget.getWidth() / 2) / scale);  // half screen width
        int h2 = (int) ((renderTarget.getHeight() / 2) / scale); // half screen height
        int posx = w2 - off_x;                   // x coord in screen px of left edge of tile 
        int posy = h2 - off_y;                   // y coord in screen px of upper edge of tile

        int diff_left = off_x;                   // distance in px from center pt to left edge of tile
        int diff_right = tilesize - off_x;       // distance in px from center pt to right edge of tile
        int diff_top = off_y;                    // distance in px from center pt to top edge of tile
        int diff_bottom = tilesize - off_y;      // distance in px from center pt to bottom edge of tile

        //Determine an initial direction for rendering
        boolean start_left = diff_left < diff_right;
        boolean start_top = diff_top < diff_bottom;
        int iMove = 0;
        if (start_top) {
            if (start_left) {
                iMove = 2;
            } else {
                iMove = 3;
            }
        } else {
            if (start_left) {
                iMove = 1;
            } else {
                iMove = 0;
            }
        } 
        
        // calculate the visibility borders
        //pretend the canvas is larger than it really is, as we may be skewed from scale
        double x_min = -tilesize * scale; //TODO: What is correct, here?
        double y_min = -tilesize * scale;
        double x_max = renderTarget.getWidth() / scale; 
        double y_max = renderTarget.getHeight() / scale;

        // calculate the length of the grid (number of squares per edge)
        //int gridLength = 1 << zoom;  Used for scroll wrap only

        // paint the tiles in a spiral, starting from center of the map
        boolean painting = true;
        int x = 0;
        while (painting) 
        {
            painting = false;
            for (int i = 0; i < 4; i++) 
            { 
                //when i = 0, 2 bump up x
                if (i % 2 == 0) {  x++; }
                
                for (int j = 0; j < x; j++) 
                {
                    if (x_min <= posx && posx <= x_max && y_min <= posy && posy <= y_max) 
                    {
                        // tile is visible
                        Tile tile;
                        //if (scrollWrapEnabled) { TODO: Re-enable this later, if desired
                        //    // in case tilex is out of bounds, grab the tile to use for wrapping
                        //    int tilexWrap = (((tilex % gridLength) + gridLength) % gridLength);
                        //    tile = tileController.getTile(tilexWrap, tiley, zoom);
                        //} else {
                            tile = tileController.getTile(tilex, tiley, zoom);
                        //}
                        if (tile != null) {
                            tile.paint(g, posx, posy);
                            if (tileGridVisible) {
                                g.drawRect(posx, posy, tilesize, tilesize);
                            }
                        }
                        painting = true;
                    }
                    
                    //Increment to the next tile
                    Point p = move[iMove];
                    posx += p.x * tilesize;
                    posy += p.y * tilesize;
                    tilex += p.x;
                    tiley += p.y;
                    
                }//end for j
                
                //change direction if necessary (spiral)
                iMove = (iMove + 1) % move.length;
                
            }//end for i
        }
        // outer border of the map
        int mapSize = tilesize << zoom;
        if (scrollWrapEnabled) {
            g.drawLine(0, h2 - center.y, renderTarget.getWidth(), h2 - center.y);
            g.drawLine(0, h2 - center.y + mapSize, renderTarget.getWidth(), h2 - center.y + mapSize);
        } else {
            g.drawRect(w2 - center.x, h2 - center.y, mapSize, mapSize);
        }

        // g.drawString("Tiles in cache: " + tileCache.getTileCount(), 50, 20);

        // keep x-coordinates from growing without bound if scroll-wrap is enabled
        if (scrollWrapEnabled) {
            center.x = center.x % mapSize;
        }
        
        g.setTransform(current);

        attribution.paintAttribution(g, renderTarget.getWidth(), renderTarget.getHeight(), 
                getPosition(0, 0), getPosition(renderTarget.getWidth(), renderTarget.getHeight()), zoom, renderTarget);
    }
    
    /**
     * Calculates the latitude/longitude coordinate of the center of the
     * currently displayed map area.
     *
     * @return latitude / longitude
     */
    public Coordinate getPosition() 
    {
        Point center = getCenter();
        double lon = tileSource.XToLon(center.x, zoom);
        double lat = tileSource.YToLat(center.y, zoom);
        return new Coordinate(lat, lon);
    }
    
    /**
     * Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate
     *
     * @param mapPoint
     *            relative pixel coordinate regarding the top left corner of the
     *            displayed map
     * @return latitude / longitude
     */
    public Coordinate getPosition(Point mapPoint) {
        return getPosition(mapPoint.x, mapPoint.y);
    }
    
    /**
     * Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate
     *
     * @param mapPointX
     * @param mapPointY
     * @return latitude / longitude
     */
    public Coordinate getPosition(int mapPointX, int mapPointY) 
    {
        Point center = getCenter();
        int x = center.x + mapPointX - renderTarget.getWidth() / 2;
        int y = center.y + mapPointY - renderTarget.getHeight() / 2;
        double lon = tileSource.XToLon(x, zoom);
        double lat = tileSource.YToLat(y, zoom);
        return new Coordinate(lat, lon);
    }
    
    /**
     * Gets the meter per pixel.
     *
     * @return the meter per pixel
     * @author Jason Huntley
     */
    public double getMeterPerPixel() 
    {
        Point origin=new Point(5,5);
        Point center=new Point(renderTarget.getWidth()/2, renderTarget.getHeight()/2);

        double pDistance=center.distance(origin);

        Coordinate originCoord=getPosition(origin);
        Coordinate centerCoord=getPosition(center);

        double mDistance = tileSource.getDistance(originCoord.getLat(), originCoord.getLon(),
                centerCoord.getLat(), centerCoord.getLon());

        return mDistance/pDistance;
    }

    /**
     * Determines the center point of the screen in JOSM "pixelspace", pixel coordinates of the tile grid.
     * @return
     */
    public Point getCenter()
    {
        //this is by no means efficient
        CoordinateTransformer transformer = renderTarget.getTransformer();
        Vector3 metersCenter = transformer.screenToMeters(renderTarget.getWidth()/2, renderTarget.getHeight()/2);
        Geodetic.Point latLonCenter = renderTarget.getTerrain().toGeodetic(metersCenter);
        Point center = new Point(tileSource.LonToX(Math.toDegrees(latLonCenter.longitude), zoom), 
                                 tileSource.LatToY(Math.toDegrees(latLonCenter.latitude), zoom));
        logger.debug("Meters center: " + metersCenter.x + "," + metersCenter.y +  
                    " Lat/Lon center: " + latLonCenter.latitude + "," + latLonCenter.longitude +  
                    " OSM Center: " + center);
        return center;
    }
    
    public double getScaleFactor()
    {
        double simPpm = renderTarget.getTransformer().scalarToPixels(Scalar.createMeter(1));
        //logger.info("scale factor: " + getMeterPerPixel() + " * " + simMpp + " = " + getMeterPerPixel() * simMpp);
        return getMeterPerPixel() * simPpm;
    }
}
