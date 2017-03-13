/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.injector;

import org.micromanager.PropertyMap;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorFactory;

/**
 *
 * @author stefko
 */
public class InjectorFactory implements ProcessorFactory {
    private InjectorContext context;
    private PropertyMap property_map;
    public InjectorFactory(PropertyMap pm, InjectorContext context) {
        super();
        property_map = pm;
        this.context = context;
    }
    
    @Override
    public Processor createProcessor() {
        return new InjectorProcessor(property_map, context);
    }
}