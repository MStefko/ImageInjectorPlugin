/*
 * The MIT License
 *
 * Copyright 2017 Marcel Stefko, LEB EPFL.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ch.epfl.leb.injector;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.DatastoreFrozenException;
import org.micromanager.data.DatastoreRewriteException;
import org.micromanager.data.Image;
import org.micromanager.data.Metadata;

/**
 * 
 * Handles opening and parsing of images and providing them on demand to
 * the InjectorProcessor.
 *
 * @author Marcel Stefko
 */
public class ImageStreamer {
    private final Studio app;
    private File tiff_file;
    private Datastore store;
    private SortedCoordsList coords_list;
    private int img_count = 0;
    private long lastFrameTimeMs = 0l;
    private long frameLengthMs = 100;
    private long now;
    
    public ImageStreamer(Studio studio) {
        // Initiate with no file and default FPS
        app = studio;
        setFile(null);
        setFPS(10);
    }
    
    public final void setFile(final File file) {
        // The file pointers can be null.
        String new_path = ""; String old_path = "";
        try {
            new_path = file.getAbsolutePath();
        } catch (NullPointerException ex) {}
        try {
            old_path = tiff_file.getAbsolutePath();
        } catch (NullPointerException ex) {}
        
        // Clean up memory if no file inserted
        if (new_path.equalsIgnoreCase("")) {
            tiff_file = null;
            store = null;
            coords_list = null;
            System.gc();
            return;
        // Do nothing if the file is the same
        } else if (old_path.equals(new_path)) {
            return;
        // Clean up memory and load up new image otherwise
        } else {
            tiff_file = null;
            store = null;
            coords_list = null;
            System.gc();
            
            final TiffParser parser = new TiffParser(app, frameLengthMs);
            new Thread(new Runnable(){
                @Override
                public void run() {
                    parser.loadScrubbedData(file);
                    store = parser.getDatastore();
                    coords_list = parser.getCoordsList();
                }
            }).start();
        }
    }
    

    
    public final long getFPS() {
        // This rounds down to the nearest integer
        return 1000 / frameLengthMs;
    }
    
    public final void setFPS(long framesPerSecond) {
        // Again, rounds down to nearest integer
        frameLengthMs = 1000 / framesPerSecond;
        // Log the value so we can see the leftover.
        app.logs().logMessage(String.format(
            "Set ImageFactory frameLengthMs to %d from %d FPS. (%d leftover)", 
            frameLengthMs, framesPerSecond, 1000 % framesPerSecond));
    }
    
    public int getNoOfImages() {
        return store.getNumImages();
    }
    
    public Image getNextImage() throws InterruptedException {
        /**
         * Continuously returns references to images from store,
         * (when it comes to the end, it starts over).
         * 
         * This function does not return copies, only references!
         */
        if (store == null) { return null; }
        // Get time for FPS limiter
        now = System.currentTimeMillis();
        // If not enough time has elapsed since last return, sleep for the
        // duration missing.
        long diff = frameLengthMs - (now - lastFrameTimeMs);
        if (diff > 0) {
            Thread.sleep(diff);
        }
        // Increment (and rotate) the counter
        img_count++;
        if (img_count >= coords_list.size()) {
            img_count = 0;
        }
        // Get timestamp for FPS limiter
        lastFrameTimeMs = System.currentTimeMillis();
        // Return image from store
        return store.getImage(coords_list.get(img_count));
    }
}

class SortedCoordsList extends java.util.ArrayList<Coords> {
    /**
     * Takes in a Coords iterable from Datastore, sorts it,
     * and turns it into a list.
     */
    public SortedCoordsList() {
        // Empty constructor, create an empty list.
        super();
    }
    
    public SortedCoordsList(java.lang.Iterable<Coords> iter) {
        super();
        // Just add the unordered coords into the list
        for (Coords item: iter) {
            this.add(item);
        }
        // Sort the list using the CoordsComparator
        Collections.sort(this, SortedCoordsList.CoordsComparator);
    }
    
    private static final Comparator<Coords> CoordsComparator = new Comparator<Coords>() {
        // Compare the String outputs of Coords.toString method.
        @Override
        public int compare(Coords o1, Coords o2) {
            return o1.toString().compareTo(o2.toString());
        }
        
    };
}

class TiffParser {
    private BusyIndicatorWindow window = new BusyIndicatorWindow(null, true);
    private Studio app;
    private File tiff_file;
    private Datastore store;
    private SortedCoordsList coords_list;
    private long frameLengthMs;
    
    public TiffParser(Studio studio, long frameLengthMs) {
        this.frameLengthMs = frameLengthMs;
        app = studio;
    }
    
    public final void loadScrubbedData(File file) {
        /**
         * Loads data from a .tiff file from disk, cleans  the Coords and
         * Metadata information, and loads it up into a RAM datastore.
         */
        
        
        Datastore disk_store = null;
        tiff_file = file;
        try {
            // Load as virtual (images are loaded on-demand)
            disk_store = app.data().loadData(file.getAbsolutePath(), true);
        } catch (IOException ex) {
            Logger.getLogger(ImageStreamer.class.getName()).log(Level.SEVERE, null, ex);
            app.logs().showMessage("Failed to load tiff file.");
            return;
        }
        
        
        
        // Containers for cleaned up final data
        store = app.data().createRAMDatastore();
        coords_list = new SortedCoordsList();
        
        // Get metadata from .tiff file, but scrub the position and other information
        // which doesn't pertain to actual image.
        Metadata.MetadataBuilder m_builder = disk_store.getAnyImage().getMetadata().copy();
        m_builder.imageNumber(0l).camera("ImageInjector").exposureMs(1.0d)
                .xPositionUm(0.0).yPositionUm(0.0).zPositionUm(0.0)
                .positionName("Injection");
        
        // Get a coords builder, but scrap all coords info.
        Coords.CoordsBuilder c_builder = disk_store.getAnyImage().getCoords().copy();
        c_builder.channel(0).stagePosition(0).time(0).z(0);
        
        // Create a sorted list (of old Coord values) so we can load the images
        // in a smart order.
        final SortedCoordsList c_list = new SortedCoordsList(disk_store.getUnorderedImageCoords());
        // Set up progressbar
        window.setMaximum(c_list.size());
        window.setProgress(0);
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                window.setVisible(true);
            }
        });

        // Load the images one by one, replace Coords and Metadata with new
        // values, and put them into final containers.
        Image disk_image;
        Image ram_image;
        Coords coords; Metadata metadata;
        // Iterate over ordered list
        for (int i=0; i<c_list.size(); i++) {
            // get immutable types from builders
            coords = c_builder.build();
            metadata = m_builder.build();
            
            // get old image, and make a copy with new coords and metadata
            disk_image = disk_store.getImage(c_list.get(i));
            ram_image = disk_image.copyWith(coords, metadata);
            // put the image into new datastore
            try {
                store.putImage(ram_image);
            } catch (DatastoreFrozenException ex) {
                Logger.getLogger(ImageStreamer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DatastoreRewriteException ex) {
                Logger.getLogger(ImageStreamer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(ImageStreamer.class.getName()).log(Level.SEVERE, null, ex);
            }
            // put the relevant coords into an ordered array
            coords_list.add(coords);
            
            // Prepare builders for next image by incrementing values
            c_builder.offset("time", (int) frameLengthMs);
            m_builder.imageNumber((long) i).elapsedTimeMs((double) i*frameLengthMs);
            // Log the process
            if (i%100==0) {
                app.logs().logDebugMessage(String.format("Loaded %d images.",i));
            }
            // Increment progressbar
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    window.increment();
                }
            });

        }
        SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    window.dispose();                
                }
            });
    }
    
    public Datastore getDatastore() {
        return store;
    } 
    
    public SortedCoordsList getCoordsList() {
        return coords_list;
    }
}