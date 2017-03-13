/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.injector;

import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;

/**
 *
 * @author stefko
 */
public class InjectorConfigurator implements ProcessorConfigurator {
    public InjectorContext context;
    private PropertyMap property_map;
    private InjectorSetupFrame frame;
    
    public InjectorConfigurator(PropertyMap pm, InjectorContext context) {
        this.context = context;
        PropertyMap.PropertyMapBuilder builder = pm.copy();
        builder.putInt("framesPerSecond", 15);
        setPropertyMap(builder.build());
        frame = new InjectorSetupFrame(new javax.swing.JFrame(), true, this);
    }
    
    public final void setPropertyMap(PropertyMap pm) {
        property_map = pm;
    }
    
    public Studio getApp() {
        return context.app;
    }
    
    public ImageInjectorPlugin getPlugin() {
        return context.plugin;
    }
    
    
    @Override
    public void showGUI() {
        frame.setVisible(true);
    }

    @Override
    public void cleanup() {
         
    }

    @Override
    public PropertyMap getSettings() {
        return property_map;
    }
}
