/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.ani;

import hu.openig.core.SwappableRenderer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JComponent;

/**
 * Component which uses buffer swapping to render an image asynchronously.
 * Please call init() before attempting to use the 
 * @author karnokd, 2009.01.19.
 * @version $Revision 1.0$
 */
public class MovieSurface extends JComponent implements SwappableRenderer {
	/** */
	private static final long serialVersionUID = 6083662951581621963L;
	/**
	 * Defines the scaling mode. 
	 * @author karnokd, 2009.01.19.
	 * @version $Revision 1.0$
	 */
	public enum ScalingMode {
		/** Do not perform any scaling. Image will be rendered to 0, 0. */
		NONE,
		/** Zoom or shrink by keeping the aspect ratio. */
		KEEP_ASPECT,
		/** Zoom or shring by scaling to the actual component size. */
		WINDOW_SIZE
	}
	/** The lock that protects the swapping and drawing of the frontbuffer. */
	private Lock swapLock = new ReentrantLock();
	/** The backbuffer image. */
	private BufferedImage backbuffer;
	/** The frontbuffer image.*/
	private BufferedImage frontbuffer;
	/** The scaling mode when rendering the image. */
	private MovieSurface.ScalingMode scalingMode = ScalingMode.KEEP_ASPECT;
	/**
	 * Returns the back buffer which is safe to draw to at any time.
	 * The get should be initiated by the party who is supplying the images.
	 * @return the backbuffer image.
	 */
	public BufferedImage getBackbuffer() {
		if (backbuffer == null) {
			throw new IllegalStateException("init() not called");
		}
		return backbuffer;
	}
	/**
	 * Retrieves the current scaling mode.
	 * @return the current scaling mode
	 */
	public MovieSurface.ScalingMode getScalingMode() {
		return scalingMode;
	}
	/**
	 * Sets the scaling mode. This should be called from the event thread.
	 * @param value the scaling mode to set
	 */
	public void setScalingMode(MovieSurface.ScalingMode value) {
		this.scalingMode = value;
		repaint();
	}
	/**
	 * Swap the front and backbuffers.
	 * The swap must be initiated by the party who is supplying the images.
	 */
	public void swap() {
		swapLock.lock();
		try {
			BufferedImage tmp = frontbuffer;
			frontbuffer = backbuffer;
			backbuffer = tmp;
		} finally {
			swapLock.unlock();
		}
		repaint();
	}
	/**
	 * Initialize the drawing buffers with the defined size.
	 * Use setScalingMode() to enable resizing of the actual image.
	 * @param width the image width
	 * @param height the image height.
	 */
	public void init(int width, int height) {
		swapLock.lock();
		try {
			backbuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			frontbuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		} finally {
			swapLock.unlock();
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		swapLock.lock();
		try {
			if (frontbuffer != null) {
				
				double scalex = 1;
				double scaley = 1;
				if (scalingMode == ScalingMode.WINDOW_SIZE) {
					scalex = (double)getWidth() / frontbuffer.getWidth();
					scaley = (double)getHeight() / frontbuffer.getHeight();
				} else
				if (scalingMode == ScalingMode.KEEP_ASPECT) {
					double sx = (double)getWidth() / frontbuffer.getWidth();
					double sy = (double)getHeight() / frontbuffer.getHeight();
					scalex = Math.min(sx, sy);
					scaley = scalex;
					// center the image
					g2.translate((getWidth() - (frontbuffer.getWidth() * scalex)) / 2,
							(getHeight() - (frontbuffer.getHeight() * scaley)) / 2);
				}
				g2.scale(scalex, scaley);
				g2.drawImage(frontbuffer, 0, 0, null);
			}
		} finally {
			swapLock.unlock();
		}
	}
}
