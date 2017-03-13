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
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorFactory;

/**
 * 
 * Generates InjectorProcessors for every stream. However, all processors
 * draw images from the same ImageStreamer (a single .tiff file in first-come
 * first-serve fashion).
 *
 * @author Marcel Stefko
 */
public class InjectorFactory implements ProcessorFactory {
    private final InjectorContext context;
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