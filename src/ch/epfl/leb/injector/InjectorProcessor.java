/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.injector;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.micromanager.PropertyMap;
import org.micromanager.data.Image;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorContext;

/**
 *
 * @author stefko
 */
public class InjectorProcessor extends Processor {
    private InjectorContext context;
    private PropertyMap property_map;
    int counter = 0;
    private final ImageStreamer image_factory;
    
    public InjectorProcessor(PropertyMap pm, InjectorContext context) {
        super();
        this.context = context;
        property_map = pm;
        image_factory = context.streamer;
}

    @Override
    public void processImage(Image image, ProcessorContext pc) {
        counter++;
        context.app.logs().logMessage(String.format(
                    "Would process image no. %d.", counter));
        try {
            Image out = image_factory.getRotatedImage();
            if (out == null) {
                pc.outputImage(image);
                return;
            } else {
                pc.outputImage(image_factory.getRotatedImage());
                return;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(InjectorProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}