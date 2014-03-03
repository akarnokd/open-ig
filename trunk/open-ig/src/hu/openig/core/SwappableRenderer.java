/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.awt.image.BufferedImage;

/**
 * Interface to specify a class supports front and back buffer swapping
 * rendering strategy. This modell is designed to be driven from one thread, where
 * one thread issues the getBackbuffer() and swap() calls. The underlying
 * synchronization will take care of the drawing in the event thread. The
 * init() method should be called before any attempt to use the getBackbuffer() method.
 * The renderer and controller should use another means of communicating how to render the
 * supplied images (e.g. scaling, positioning).
 * @author akarnokd, 2009.01.19.
 */
public interface SwappableRenderer {
	/**
	 * Returns the now off screen back buffer.
	 * The image is in TYPE_INT_RGBA format.
	 * @return the back buffer image.
	 */
	BufferedImage getBackbuffer();
	/**
	 * Swap the back and the front buffer. The next call to getBackbuffer()
	 * will return the previously front buffer.
	 */
	void swap();
	/**
	 * Initializes the back and front buffer images to be of the
	 * given size. The images will be created using the TYPE_INT_RGBA color model.
	 * @param width the width of the image
	 * @param height the height of the image
	 */
	void init(int width, int height);
}
