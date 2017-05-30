/*
 * Copyright (C) 2017 Laboratory of Experimental Biophysics
 * Ecole Polytechnique Federale de Lausanne
 *
 * Author: Marcel Stefko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.epfl.leb.injector;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.Opener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.DatastoreFrozenException;
import org.micromanager.data.DatastoreRewriteException;
import org.micromanager.data.Image;
import org.micromanager.data.Metadata;

/**
 *
 * @author stefko
 */
public class TiffParser {
    private BusyIndicatorWindow window = new BusyIndicatorWindow(null, true);
    private Studio app;
    private File tiff_file;
    private Datastore store;
    private SortedCoordsList coords_list;
    private long frameLengthMs;
    private Coords.CoordsBuilder c_builder;
    private Metadata.MetadataBuilder m_builder;
    
    public TiffParser(Studio studio, long frameLengthMs) {
        this.frameLengthMs = frameLengthMs;
        app = studio;
    }
    
    public final void loadMMtiff(File file) throws IOException {
        app.logs().logMessage("Trying to open MM tiff.");
        Datastore disk_store = app.data().loadData(file.getAbsolutePath(), true);
        m_builder = disk_store.getAnyImage().getMetadata().copy();
        m_builder.imageNumber(0l).camera("ImageInjector").exposureMs(1.0d)
                .xPositionUm(0.0).yPositionUm(0.0).zPositionUm(0.0)
                .positionName("Injection");
        
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
                app.logs().logDebugMessage(String.format("Loaded %d images from MM tiff stack.",i));
            }
            // Increment progressbar
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    window.increment();
                }
            });

        }
    }
    
    public final void loadGeneralTiff(File file) {
        // Open the tiff via ImageJ
        app.logs().logMessage("Trying to open general tiff.");
        Opener o = new Opener();
        ImagePlus win = o.openTiff(file.getParent().concat("\\"),file.getName());
        ImageStack stack = win.getImageStack();
        // Build up metadata from scratch
        m_builder = app.data().getMetadataBuilder();
        m_builder.imageNumber(0l).camera("ImageInjector").exposureMs(1.0d)
                .xPositionUm(0.0).yPositionUm(0.0).zPositionUm(0.0)
                .positionName("Injection");
        
        // 
        int width = stack.getWidth();
        int height = stack.getHeight();
        int bytesPerPixel;
        if(stack.getPixels(win.getSlice()) instanceof short[]) {
            bytesPerPixel = 2;
        } else {
            throw new ArrayStoreException("Wrong image bit depth.");
        }
        
        // Set up progressbar
        window.setMaximum(stack.getSize());
        window.setProgress(0);
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                window.setVisible(true);
            }
        });
        
        Image ram_image;
        Coords coords; Metadata metadata;
        for (int i=1; i<=stack.getSize(); i++) {
            // get immutable types from builders
            coords = c_builder.build();
            metadata = m_builder.build();
            
            ram_image = app.data().createImage(stack.getPixels(i), width, height, bytesPerPixel, 1, coords, metadata);
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
                app.logs().logDebugMessage(String.format("Loaded %d images from general tiff stack.",i));
            }
            // Increment progressbar
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    window.increment();
                }
            });
        }
    }
    
    
    public final void loadScrubbedData(File file, boolean isMMtiff) {
        /**
         * Loads data from a .tiff file from disk, cleans  the Coords and
         * Metadata information, and loads it up into a RAM datastore.
         */
        
        app.logs().logDebugMessage(String.format(
            "Parsing .tiff file: %s", file.getAbsolutePath()));
        
        // Containers for cleaned up final data
        store = app.data().createRAMDatastore();
        coords_list = new SortedCoordsList();
        
        // Get a coords builder, but scrap all coords info.
        c_builder = app.data().getCoordsBuilder();
        c_builder.channel(0).stagePosition(0).time(0).z(0);
        
        // Get metadata from .tiff file, but scrub the position and other information
        // which doesn't pertain to actual image.
        
        
        Datastore disk_store = null;
        if (isMMtiff){
            try {
                loadMMtiff(file);
            } catch (IOException ex) {
                Logger.getLogger(TiffParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                loadGeneralTiff(file);
            } catch (Exception ex2) {
                Logger.getLogger(ImageStreamer.class.getName()).log(Level.SEVERE, null, ex2);
                app.logs().showError("Loading of .tiff file failed.");
                return;
            }
        }
        tiff_file = file;

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