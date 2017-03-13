/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.injector;


import org.micromanager.PropertyMap;
import org.micromanager.Studio;
/**
 *
 * @author stefko
 */
public class InjectorContext {
    public final Studio app;
    public final ImageInjectorPlugin plugin;
    public final ImageStreamer streamer;
    public InjectorContext(Studio app, 
            ImageInjectorPlugin plugin, ImageStreamer streamer) {
        this.app = app;
        this.plugin = plugin;
        this.streamer = streamer;
    }
}
