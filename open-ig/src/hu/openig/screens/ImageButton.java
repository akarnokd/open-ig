/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.core.Button;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

/**
 * A three phase image button.
 * @author akarnokd, 2010.01.15.
 */
public class ImageButton extends Button {
	/** The selected image. */
	public BufferedImage selectedImage;
	/** The normal image. */
	public BufferedImage normalImage;
	/** The pressed image. */
	public BufferedImage pressedImage;
	/** The action to invoke when pressed. */
	public Act onPress;
	/** The action to invoke when released. */
	public Act onRelease;
	/** The action to invoke when leave. */
	public Act onLeave;
	/** The action to invoke when enter. */
	public Act onEnter;
	/** The common resources. */
	public CommonResources commons;
	@Override
	public int getWidth() {
		return normalImage.getWidth();
	}
	@Override
	public int getHeight() {
		return normalImage.getHeight();
	}
	/**
	 * Paint the button.
	 * @param g2 the graphics target
	 * @param x0 the reference
	 * @param y0 the reference
	 */
	@Override 
	public void paintTo(Graphics2D g2, int x0, int y0) {
		if (visible) {
			if (!enabled) {
				g2.drawImage(normalImage, x0 + x, y0 + y, null);
				TexturePaint tp = new TexturePaint(commons.disabledPattern, new Rectangle(x0 + x, y0 + y, 3, 3));
				Paint sp = g2.getPaint();
				g2.setPaint(tp);
				g2.fillRect(x0 + x, y0 + y, normalImage.getWidth(), normalImage.getHeight());
				g2.setPaint(sp);
			} else
			if (pressed) {
				g2.drawImage(pressedImage, x0 + x, y0 + y, null);
			} else
			if (mouseOver) {
				g2.drawImage(selectedImage, x0 + x, y0 + y, null);
			} else {
				g2.drawImage(normalImage, x0 + x, y0 + y, null);
			}
		}
	}
	@Override
	public void onPressed() {
		if (onPress != null) {
			onPress.act();
		}
	}
	@Override
	public void onReleased() {
		if (onRelease != null) {
			onRelease.act();
		}
	}
	@Override
	public void onEnter() {
		if (onEnter != null) {
			onEnter.act();
		}
	}
	@Override
	public void onLeave() {
		if (onLeave != null) {
			onLeave.act();
		}
	}
}
