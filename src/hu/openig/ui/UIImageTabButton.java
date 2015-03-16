/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.core.Action0;
import hu.openig.render.RenderTools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A three state image button with normal, pressed and hovered state.
 * handler when the user holds down a button.
 * The difference from <code>UIImageButton</code>
 * that when clicked, the button remains in the down state.
 * You must manually remove the down state.
 * 
 * @author akarnokd, 2011.02.26.
 */
public class UIImageTabButton extends UIComponent {
	/** The normal state image. */
	protected BufferedImage normal;
	/** The pressed state image. */
	protected BufferedImage pressed;
	/** The hovered state image. */
	protected BufferedImage hovered;
	/** The disabled pattern to use for the button. */
	protected BufferedImage disabledPattern;
	/** 
	 * The action to invoke when the button is pressed down.
	 * Can be used to use this button as a tab.
	 */
	public Action0 onPress;
	/** Is the mouse pressed down on this component. */
	public boolean down;
	/**
	 * Constructor with the default images.
	 * @param normal the normal state image
	 * @param pressed the pressed state image
	 * @param hovered the hovered state image, if null, the normal image is used instead
	 */
	public UIImageTabButton(BufferedImage normal, BufferedImage pressed, BufferedImage hovered) {
		this.normal = normal;
		this.pressed = pressed;
		this.hovered = hovered != null ? hovered : normal;
		this.width = normal.getWidth();
		this.height = normal.getHeight();
	}
	/**
	 * Creates an image button by using the elements of the supplied array.
	 * The first element is the normal image, the second should be the pressed image
	 * and an optional third image should be the hovered image. If
	 * no hovered image is specified, the normal image is used instead.
	 * You may use this constructor with the resource BufferedImage arrays of buttons
	 * @param images the array of images.
	 */
	public UIImageTabButton(BufferedImage[] images) {
		this.normal = images[0];
		this.pressed = images.length > 1 ? images[1] : images[0];
		this.hovered = images.length > 2 ? images[2] : images[0];
		this.width = normal.getWidth();
		this.height = normal.getHeight();
	}
	/** Call the press action if set. */
	protected void doPress() {
		if (onPress != null) {
			onPress.invoke();
		}
	}
	@Override
	public void draw(Graphics2D g2) {
		if (!enabled && disabledPattern != null) {
			g2.drawImage(normal, 0, 0, null);
			RenderTools.fill(g2, 0, 0, width, height, disabledPattern);
		} else
		if (down) {
			g2.drawImage(pressed, 0, 0, null);
		} else
		if (over) {
			g2.drawImage(hovered, 0, 0, null);
		} else {
			g2.drawImage(normal, 0, 0, null);
		}
	}
	@Override
	public boolean mouse(UIMouse e) {
		switch (e.type) {
		case DOWN:
			down = true;
			doPress();
			return true;
		case UP:
		case LEAVE:
			return true;
		case ENTER:
			return hovered != normal;
		default:
			return false;
		}
	}
	/**
	 * Set the disabled pattern for this button.
	 * @param pattern the pattern to fill with the area of the button when it is disabled
	 * @return this
	 */
	public UIImageTabButton setDisabledPattern(BufferedImage pattern) {
		this.disabledPattern = pattern;
		return this;
	}
}
