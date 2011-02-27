/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.core.Act;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

/**
 * A three state image button with normal, pressed and hovered state.
 * It does not support the enabled state but supports
 * the option to repeatedly call the <code>onClick</code>
 * handler when the user holds down a button.
 * 
 * @author akarnokd, 2011.02.26.
 */
public class UIImageButton extends UIComponent {
	/** The normal state image. */
	protected BufferedImage normal;
	/** The pressed state image. */
	protected BufferedImage pressed;
	/** The hovered state image. */
	protected BufferedImage hovered;
	/** The action to invoke when the button is clicked. */
	public Act onClick;
	/** 
	 * The optional delay to fire onClick events when
	 * the mouse is pressed over the button.
	 * Unit is in milliseconds.
	 */
	protected int holdDelay = -1;
	/** The timer to send pressed events periodically. */
	protected Timer holdTimer;
	/** Is the mouse pressed down on this component. */
	protected boolean down;
	/**
	 * Constructor with the default images.
	 * @param normal the normal state image
	 * @param pressed the pressed state image
	 * @param hovered the hovered state image, if null, the normal image is used instead
	 */
	public UIImageButton(BufferedImage normal, BufferedImage pressed, BufferedImage hovered) {
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
			return true;
		case CLICK:
		case DOUBLE_CLICK:
			for (int i = 0; i < e.z; i++) {
				doClick();
			}
			return false;
		case UP:
		case LEAVE:
			down = false;
			holdTimer.stop();
			return true;
		case ENTER:
			return true;
		default:
			return false;
		}
	}
}
