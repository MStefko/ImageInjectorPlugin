/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.injector;

import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorPlugin;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.data.ProcessorContext;
import org.micromanager.data.ProcessorFactory;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.plugin.Plugin;
import java.util.Comparator;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stefko
 */
@Plugin(type = ProcessorPlugin.class)
public class ImageInjectorPlugin implements 
                org.micromanager.data.ProcessorPlugin,
                org.scijava.plugin.SciJavaPlugin {

    public static final String menuName = "CameraInjector";
    private Studio app;
    private ProcessorFactory factory;
    
    @Override
    public ProcessorConfigurator createConfigurator(PropertyMap pm) {
        return new InjectorConfigurator(pm, app);
    }

    @Override
    public ProcessorFactory createFactory(PropertyMap pm) {
        return new InjectorFactory(pm, app);
    }

    @Override
    public void setContext(Studio studio) {
        app = studio;
    }

    
    
    @Override
    public String getName() {
        return menuName;
    }

    @Override
    public String getHelpText() {
        return "You must be really desperate, coming to me for help.";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    @Override
    public String getCopyright() {
        return "No licence. Don't use this, it's not good.";
    }
    
}

class InjectorConfigurator implements ProcessorConfigurator {
    private PropertyMap property_map;
    private final Studio app;

    public InjectorConfigurator(PropertyMap pm, Studio studio) {
        PropertyMap.PropertyMapBuilder builder = pm.copy();
        builder.putInt("framesPerSecond", 15);
        setPropertyMap(builder.build());
        app = studio;
    }
    
    public void setPropertyMap(PropertyMap pm) {
        property_map = pm;
    }
    
    public PropertyMap getPropertyMap() {
        return property_map;
    }
    
    @Override
    public void showGUI() {
        app.logs().showMessage("This is supposed to be the GUI.");
    }

    @Override
    public void cleanup() {
         
    }

    @Override
    public PropertyMap getSettings() {
        return property_map;
    }
    
}


class InjectorFactory implements ProcessorFactory {
    private PropertyMap property_map;
    private final Studio app;
    public InjectorFactory(PropertyMap pm, Studio studio) {
        property_map = pm;
        app = studio;
    }
    
    @Override
    public Processor createProcessor() {
        return new InjectorProcessor(property_map, app);
    }
}

class InjectorProcessor extends Processor {
    private PropertyMap property_map;
    private final Studio app;
    int counter = 0;
    private ImageFactory factory;
    
    public InjectorProcessor(PropertyMap pm, Studio studio) {
        super();
        app = studio;
        property_map = pm;
        factory = new ImageFactory(property_map, app);
}

    @Override
    public void processImage(Image image, ProcessorContext pc) {
        counter++;
        app.logs().logMessage(String.format(
                    "Would process image no. %d.", counter));
        try {
            pc.outputImage(factory.getRotatedImage());
        } catch (InterruptedException ex) {
            Logger.getLogger(InjectorProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class ImageFactory {
    private final Studio app;
    private PropertyMap property_map;
    private Datastore store;
    private CoordsList coords_list;
    private int img_count = 0;
    private long lastFrameTimeMs = 0l;
    private long frameLengthMs = 10;
    private long now;
    
    public ImageFactory(PropertyMap pm, Studio studio) {
        app = studio;
        property_map = pm;
        try {
            store = app.data().loadData("C:\\aest.tif", false);
        } catch (java.io.IOException ex) {
            app.logs().showMessage("Failed to load tif file.");
        }
        coords_list = new CoordsList(store.getUnorderedImageCoords());
        setFramesPerSecond(property_map.getInt("framesPerSecond"));
    }
    
    public void setFramesPerSecond(int framesPerSecond) {
        frameLengthMs = 1000 / framesPerSecond;
        app.logs().logMessage(String.format(
            "Set ImageFactory frameLengthMs to %d from %d FPS. (%d leftover)", 
            frameLengthMs, framesPerSecond, frameLengthMs % framesPerSecond));
    }
    
    public int getNoOfLeftImages() {
        return store.getNumImages();
    }
    
    public Image anyImage() {
        return store.getAnyImage();
    }
    
    public Image getRotatedImage() throws InterruptedException {
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
    
    private static Comparator<Coords> CoordsComparator = new Comparator<Coords>() {

        @Override
        public int compare(Coords o1, Coords o2) {
            return o1.toString().compareTo(o2.toString());
        }
        
    };
}