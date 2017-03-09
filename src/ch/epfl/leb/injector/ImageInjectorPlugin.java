/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.injector;

import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorPlugin;
import org.micromanager.data.ProcessorConfigurator;
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
    
    
    @Override
    public ProcessorConfigurator createConfigurator(PropertyMap pm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ProcessorFactory createFactory(PropertyMap pm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setContext(Studio studio) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
