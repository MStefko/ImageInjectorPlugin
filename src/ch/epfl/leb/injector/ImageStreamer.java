/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.injector;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;

/**
 *
 * @author stefko
 */
public class ImageStreamer {
    private final Studio app;
    private File tiff_file;
    private Datastore store;
    private CoordsList coords_list;
    private int img_count = 0;
    private long lastFrameTimeMs = 0l;
    private long frameLengthMs = 10;
    private long now;
    
    public ImageStreamer(Studio studio) {
        app = studio;
        setFile(null);
        setFramesPerSecond(10);
    }
    
    public final void setFile(File file) {
        // clean up memory
        tiff_file = null;
        store = null;
        coords_list = null;
        System.gc();
        if (file == null) {
            return;
        }
        try {
            Datastore new_store = app.data().loadData(file.getAbsolutePath(), false);
            tiff_file = file;
            store = new_store;
        } catch (java.io.IOException ex) {
            app.logs().showMessage("Failed to load tiff file.");
        }
        coords_list = new CoordsList(store.getUnorderedImageCoords());
    }
    
    public final void setFramesPerSecond(int framesPerSecond) {
        frameLengthMs = 1000 / framesPerSecond;
        app.logs().logMessage(String.format(
            "Set ImageFactory frameLengthMs to %d from %d FPS. (%d leftover)", 
            frameLengthMs, framesPerSecond, frameLengthMs % framesPerSecond));
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

class CoordsList extends java.util.ArrayList<Coords> {
    public CoordsList(java.lang.Iterable<Coords> iter) {
        super();
        for (Coords item: iter) {
            this.add(item);
        }
        Collections.sort(this, CoordsList.CoordsComparator);
    }
    
    private static final Comparator<Coords> CoordsComparator = new Comparator<Coords>() {

        @Override
        public int compare(Coords o1, Coords o2) {
            return o1.toString().compareTo(o2.toString());
        }
        
    };
}