/*
 * The MIT License
 *
 * Copyright 2017 Marcel Stefko, LEB EPFL.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
 * ProcessorPlugin for MicroManager API version 2.0,
 * which replaces images from the camera with a stream of images from a .tif
 * stack.
 *
 * @author Marcel Stefko
 */
@Plugin(type = ProcessorPlugin.class)
public class ImageInjectorPlugin implements 
                org.micromanager.data.ProcessorPlugin,
                org.scijava.plugin.SciJavaPlugin {

    public static final String menuName = "CameraInjector";
    private Studio app;
    private ImageStreamer streamer;
    private InjectorContext context;
    
    public ImageStreamer getImageStreamer() {
        return streamer;
    }
    
    @Override
    public ProcessorConfigurator createConfigurator(PropertyMap pm) {
        return new InjectorConfigurator(pm, context);
    }

    @Override
    public ProcessorFactory createFactory(PropertyMap pm) {
        return new InjectorFactory(pm, context);
    }

    @Override
    public void setContext(Studio studio) {
        app = studio;
        streamer = new ImageStreamer(app);
        context = new InjectorContext(app, this, streamer);
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
        return "No licence. Don't use this, it's no good.";
    }
}
