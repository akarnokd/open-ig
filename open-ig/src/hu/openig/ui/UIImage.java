/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.ui;

import hu.openig.core.Action0;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
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
	/** Stretch the image? */
	private boolean stretch;
	/** The border color. */
	protected int borderColor;
	/** The background color. */
	protected int backgroundColor;
	/** The click action. */
	public Action0 onClick;
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
		if (backgroundColor != 0) {
			g2.setColor(new Color(backgroundColor, true));
			g2.fillRect(0, 0, width, height);
		}
		if (image != null) {
			if (stretch) {
				g2.drawImage(image, 0, 0, width, height, null);
			} else
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
		if (borderColor != 0) {
			g2.setColor(new Color(borderColor, true));
			g2.drawRect(0, 0, width - 1, height - 1);
		}
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.DOWN)) {
			if (onClick != null) {
				onClick.invoke();
				return true;
			}
		}
		return super.mouse(e);
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
	/**
	 * @return The associated image. 
	 */
	public BufferedImage image() {
		return image;
	}
	/**
	 * Size to content.
	 */
	public void sizeToContent() {
		if (image != null) {
			width = image.getWidth();
			height = image.getHeight();
		} else {
			width = 0;
			height = 0;
		}
	}
	/**
	 * @return the current border color ARGB
	 */
	public int borderColor() {
		return borderColor;
	}
	/**
	 * Set the border color.
	 * @param newColor new color ARGB
	 * @return this
	 */
	public UIImage borderColor(int newColor) {
		this.borderColor = newColor;
		return this;
	}
	/**
	 * @return the current background color ARGB
	 */
	public int backgroundColor() {
		return backgroundColor;
	}
	/**
	 * Set the background color.
	 * @param newColor the new color ARGB
	 * @return this
	 */
	public UIImage backgroundColor(int newColor) {
		this.backgroundColor = newColor;
		return this;
	}
	/**
	 * Stretch the image to the component size?
	 * @param stretch enable stretch
	 * @return this
	 */
	public UIImage stretch(boolean stretch) {
		this.stretch = stretch;
		return this;
	}
	/** @return the current stretch state. */
	public boolean stretch() {
		return this.stretch;
	}
}
