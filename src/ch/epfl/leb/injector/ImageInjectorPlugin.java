/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.injector;

import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.Image;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorPlugin;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.data.ProcessorContext;
import org.micromanager.data.ProcessorFactory;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.plugin.Plugin;

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
        property_map = pm;
        app = studio;
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
        return new InjectorProcessor(app);
    }
    
}

class InjectorProcessor extends Processor {
    private final Studio app;
    int counter = 0;
    
    public InjectorProcessor(Studio studio) {
        super();
        app = studio;
}

    @Override
    public void processImage(Image image, ProcessorContext pc) {
        counter++;
        app.logs().logMessage(String.format(
                "Would process image no. %d. Just sending it along.", counter));
        pc.outputImage(image);
    }
}