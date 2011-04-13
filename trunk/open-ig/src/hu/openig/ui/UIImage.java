/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A static image display without any event handling.
 * @author akarnokd
 */
public class UIImage extends UIComponent {
	/** The image object. */
	private BufferedImage image;
	/** Scale the image to the defined width and height? */
	private boolean scale;
	/** Center the image? */
	private boolean center;
	/**
	 * Default constructor without any image. The component will have zero size.
	 */
	public UIImage() {
		
	}
	/**
	 * Create a non-scaled image component out of the supplied image.
	 * @param image the image to use
	 */
	public UIImage(BufferedImage image) {
		this.image = image;
		this.width = image.getWidth();
		this.height = image.getHeight();
	}
	/**
	 * Create a scaled image component out of the supplied image.
	 * @param image the image
	 * @param width the target width
	 * @param height the target height
	 */
	public UIImage(BufferedImage image, int width, int height) {
		this.image = image;
		this.width = width;
		this.height = height;
		this.scale = true;
	}
	@Override
	public void draw(Graphics2D g2) {
		if (image != null) {
			if (center) {
				int dx = (width - image.getWidth()) / 2;
				int dy = (height - image.getHeight()) / 2;
				g2.drawImage(image, dx, dy, null);
			} else
			if (scale) {
				float fx = width * 1.0f / image.getWidth();
				float fy = height * 1.0f / image.getHeight();
				float f = Math.min(fx, fy);
				int dx = (int)((width - image.getWidth() * f) / 2);
				int dy = (int)((height - image.getHeight() * f) / 2);
				g2.drawImage(image, dx, dy, (int)(image.getWidth() * f), (int)(image.getHeight() * f), null);
			} else {
				g2.drawImage(image, 0, 0, null);
			}
		}
	}
	/**
	 * Set the image content. Does not change the component dimensions.
	 * @param image the image to set
	 * @return this
	 */
	public UIImage image(BufferedImage image) {
		this.image = image;
		return this;
	}
	/** 
	 * Center the image.
	 * @param state center?
	 * @return this
	 */
	public UIImage center(boolean state) {
		this.center = state;
		return this;
	}
	/** 
	 * Scale the image to the current size.
	 * @param state the state
	 * @return this
	 */
	public UIImage scale(boolean state) {
		this.scale = state;
		return this;
	}
}
