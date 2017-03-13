/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.injector;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        app = studio;
        setFile(null);
        setFPS(10);
    }
    
    public final void setFile(File file) {
        // clean up memory
        if (file == null) {
            tiff_file = null;
            store = null;
            coords_list = null;
            System.gc();
            return;
        } else if (file.getAbsolutePath().equals(tiff_file.getAbsolutePath())) {
            return;
        } else {
            tiff_file = null;
            store = null;
            coords_list = null;
            System.gc();
            loadScrubbedData(file);
        }
    }
    
    public final void loadScrubbedData(File file) {
        Datastore disk_store = null;
        tiff_file = file;
        try {
            disk_store = app.data().loadData(file.getAbsolutePath(), true);
        } catch (IOException ex) {
            Logger.getLogger(ImageStreamer.class.getName()).log(Level.SEVERE, null, ex);
            app.logs().showMessage("Failed to load tiff file.");
            return;
        }
        store = app.data().createRAMDatastore();
        coords_list = new SortedCoordsList();
        
        Metadata.MetadataBuilder m_builder = disk_store.getAnyImage().getMetadata().copy();
        m_builder.imageNumber(0l).camera("ImageInjector").exposureMs(1.0d)
                .xPositionUm(0.0).yPositionUm(0.0).zPositionUm(0.0)
                .positionName("Injection");
        
        Coords.CoordsBuilder c_builder = disk_store.getAnyImage().getCoords().copy();
        c_builder.channel(0).stagePosition(0).time(0).z(0);
        SortedCoordsList c_list = new SortedCoordsList(disk_store.getUnorderedImageCoords());
        
        Image disk_image;
        Image ram_image;
        Coords coords; Metadata metadata;
        for (int i=0; i<c_list.size(); i++) {
            coords = c_builder.build();
            metadata = m_builder.build();
            
            disk_image = disk_store.getImage(c_list.get(i));
            ram_image = disk_image.copyWith(coords, metadata);
            try {
                store.putImage(ram_image);
            } catch (DatastoreFrozenException ex) {
                Logger.getLogger(ImageStreamer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DatastoreRewriteException ex) {
                Logger.getLogger(ImageStreamer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(ImageStreamer.class.getName()).log(Level.SEVERE, null, ex);
            }
            coords_list.add(coords);
            
            c_builder.offset("time", (int) frameLengthMs);
            m_builder.imageNumber((long) i).elapsedTimeMs((double) i*frameLengthMs);
            if (i%100==0) {
                app.logs().logDebugMessage(String.format("Loaded %d images.",i));
            }
        }
    }
    
    public final long getFPS() {
        return 1000 / frameLengthMs;
    }
    
    public final void setFPS(long framesPerSecond) {
        frameLengthMs = 1000 / framesPerSecond;
        try {
        app.logs().logMessage(String.format(
            "Set ImageFactory frameLengthMs to %d from %d FPS. (%d leftover)", 
            frameLengthMs, framesPerSecond, frameLengthMs % framesPerSecond));
        } catch (NullPointerException ex) {
            Logger.getLogger(InjectorProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int getNoOfImages() {
        return store.getNumImages();
    }
    
    public Image getRotatedImage() throws InterruptedException {
        if (store == null) { return null; }
        now = System.currentTimeMillis();
        long diff = frameLengthMs - (now - lastFrameTimeMs);
        if (diff > 0) {
            Thread.sleep(diff);
        }
        img_count++;
        if (img_count >= coords_list.size()) {
            img_count = 0;
        }
        lastFrameTimeMs = System.currentTimeMillis();
        return store.getImage(coords_list.get(img_count));
    }
}

class SortedCoordsList extends java.util.ArrayList<Coords> {
    public SortedCoordsList() {
        super();
    }
    
    public SortedCoordsList(java.lang.Iterable<Coords> iter) {
        super();
        for (Coords item: iter) {
            this.add(item);
        }
        Collections.sort(this, SortedCoordsList.CoordsComparator);
    }
    
    private static final Comparator<Coords> CoordsComparator = new Comparator<Coords>() {

        @Override
        public int compare(Coords o1, Coords o2) {
            return o1.toString().compareTo(o2.toString());
        }
        
    };
}