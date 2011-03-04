/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.core.Act;
import hu.openig.render.RenderTools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

/**
 * A three state image button with normal, pressed and hovered state.
 * Supports
 * the option to repeatedly call the <code>onClick</code>
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
	/** The action to invoke when the button is clicked. */
	public Act onClick;
	/** 
	 * The action to invoke when the button is pressed down.
	 * Can be used to use this button as a tab.
	 */
	public Act onPress;
	/** 
	 * The optional delay to fire onClick events when
	 * the mouse is pressed over the button.
	 * Unit is in milliseconds.
	 */
	protected int holdDelay = -1;
	/** The timer to send pressed events periodically. */
	protected Timer holdTimer;
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
		this.holdTimer = new Timer(100, new Act() {
			@Override
			public void act() {
				doClick();
				if (holdDelay < 0 || !enabled || !visible) {
					holdTimer.stop();
				}
			}
		});
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
		this.holdTimer = new Timer(100, new Act() {
			@Override
			public void act() {
				doClick();
				if (holdDelay < 0 || !enabled || !visible) {
					holdTimer.stop();
				}
			}
		});
	}
	/**
	 * Set the mouse-hold delay to repeatedly fire the onClick
	 * event. Use -1 to turn of repetition.
	 * @param delayMillis the delay in milliseconds
	 */
	public void setHoldDelay(int delayMillis) {
		this.holdDelay = delayMillis;
		if (holdDelay >= 0) {
			holdTimer.setInitialDelay(holdDelay);
			holdTimer.setDelay(holdDelay);
		}
	}
	/**
	 * Stop all internal timers to allow cleanup and thread exit.
	 */
	public void stop() {
		holdTimer.stop();
	}
	/**
	 * Call the click action if set.
	 */
	protected void doClick() {
		if (onClick != null) {
			onClick.act();
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
			if (holdDelay >= 0) {
				holdTimer.start();
			}
			if (onPress != null) {
				onPress.act();
			}
			return true;
		case CLICK:
		case DOUBLE_CLICK:
			for (int i = 0; i < e.z; i++) {
				doClick();
			}
			return false;
		case UP:
		case LEAVE:
			holdTimer.stop();
			return true;
		case ENTER:
			return true;
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
