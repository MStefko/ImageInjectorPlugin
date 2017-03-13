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
    private Image current_image;
    private final ImageStreamer image_factory;
    
    private long last_time;
    
    public InjectorProcessor(PropertyMap pm, InjectorContext context) {
        super();
        this.context = context;
        property_map = pm;
        image_factory = context.streamer;
        last_time = System.currentTimeMillis();
}

    @Override
    public void processImage(Image image, ProcessorContext pc) {
        counter++;
        if (counter % 100 == 0) {
            long time = System.currentTimeMillis() - last_time;
            double FPS = 100000.0 / (double) time;
            context.app.logs().logDebugMessage(String.format("100 images processed in %d milliseconds. (%4.2f FPS)", time, FPS));
            last_time = System.currentTimeMillis();
        }
        try {
            current_image = image_factory.getRotatedImage();
            if (current_image == null) {
                pc.outputImage(image);
                return;
            } else {
                pc.outputImage(current_image);
                return;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(InjectorProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}