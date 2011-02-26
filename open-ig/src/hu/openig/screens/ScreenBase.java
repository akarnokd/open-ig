/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Configuration;
import hu.openig.core.ResourceLocator;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

/**
 * A screen base class.
 * @author akarnokd, 2009.12.23.
 */
public abstract class ScreenBase {
	/** The global configuration object. */
	protected Configuration config;
	/** The global resource locator. */
	protected ResourceLocator rl;
	/** The component used to render the screen. */
	protected JComponent parent;
	/** The common resources. */
	protected CommonResources commons;
	/** The memorized last width of the parent component. */
	protected int lastWidth;
	/** The memorized last height of the parent component. */
	protected int lastHeight;
	/** If the called method requires a repaint. */
	protected boolean repaintRequested;
	/**
	 * Render the screen's content based.
	 * @param g2 the graphics object
	 */
	public abstract void paintTo(Graphics2D g2);
	/**
	 * Handle a mouse pressed event.
	 * @param button the button, see MouseEvent.BUTTON_x
	 * @param x the mouse coordinate
	 * @param y the mouse coordinate
	 * @param modifiers the optional modifiers, see MouseEvent.CTRL_DOWN_MASK etc.
	 */
	public abstract void mousePressed(int button, int x, int y, int modifiers);
	/**
	 * Handle a mouse released event.
	 * @param button the button, see MouseEvent.BUTTON_x
	 * @param x the mouse coordinate
	 * @param y the mouse coordinate
	 * @param modifiers the optional modifiers, see MouseEvent.CTRL_DOWN_MASK etc.
	 */
	public abstract void mouseReleased(int button, int x, int y, int modifiers);
	/**
	 * Handle a mouse double click event.
	 * @param button the button, see MouseEvent.BUTTON_x
	 * @param x the mouse coordinate
	 * @param y the mouse coordinate
	 * @param modifiers the optional modifiers, see MouseEvent.CTRL_DOWN_MASK etc.
	 */
	public abstract void mouseDoubleClicked(int button, int x, int y, int modifiers);
	/**
	 * Handle mouse scroll event.
	 * @param direction the scroll direction: -1 or +1
	 * @param x the mouse coordinate
	 * @param y the mouse coordinate
	 * @param modifiers the optional modifiers, see MouseEvent.CTRL_DOWN_MASK etc.
	 */
	public abstract void mouseScrolled(int direction, int x, int y, int modifiers);
	/**
	 * Handle screen specific keyboard events.
	 * @param key the key code, see KeyEvent.VK_x
	 * @param modifiers the optional modifiers, see KeyEvent.CTRL_DOWN_MASK etc
	 */
	public abstract void keyTyped(int key, int modifiers);
	/** 
	 * Initialize any resources that are required by the screen. 
	 * @param commons the configuration object
	 * @param parent the parent component for resizing
	 */
	public void initialize(CommonResources commons, JComponent parent) {
		this.commons = commons;
		this.config = commons.config;
		this.rl = commons.rl;
		this.parent = parent;
		initialize();
	}
	/** The custom initialization routine. */
	public abstract void initialize();
	/** Perform actions when the player displays the screen (e.g start animation timers). */
	public abstract void onEnter();
	/** Perform actions when the player leaves the screen (e.g. stop animation timers). */
	public abstract void onLeave();
	/** Release resources of the screen, and e.g. cancel any animation timers. */
	public abstract void finish();
	/** Called when the parent component changed the size. */
	public void onResize() {
		if (lastWidth == parent.getWidth() && lastHeight == parent.getHeight()) {
			return;
		}
		lastWidth = parent.getWidth();
		lastHeight = parent.getHeight();
		doResize();
	}
	/** Called if the component size changed since the last call. */
	public abstract void doResize();
	/** Ask for the parent to repaint itself. */
	public void repaint() {
		parent.repaint();
	}
	/**
	 * Signal to repaint after the event handler completed.
	 */
	public void requestRepaint() {
		repaintRequested = true;
	}
	/**
	 * Handle if the repaint request is set.
	 */
	public void handleRepaint() {
		if (repaintRequested) {
			repaintRequested = false;
			repaint();
		}
	}
	/**
	 * Handle a mouse movement event.
	 * @param button the button, see MouseEvent.BUTTON_x
	 * @param x the mouse coordinate
	 * @param y the mouse coordinate
	 * @param modifiers the optional modifiers, see MouseEvent.CTRL_DOWN_MASK etc.
	 */
	public abstract void mouseMoved(int button, int x, int y, int modifiers);
	/** Display this screen as the primary. */
	public void displayPrimary() {
		commons.control.displayPrimary(this);
	}
	/** Display this screen as the secondary. */
	public void displaySecondary() {
		commons.control.displaySecondary(this);
	}
	/**
	 * Retrieve the screen width.
	 * @return the width
	 */
	public int getWidth() {
		return parent.getWidth();
	}
	/**
	 * Retrieve the screen height.
	 * @return the height
	 */
	public int getHeight() {
		return parent.getHeight();
	}
	/**
	 * Is the left mouse button pressed?
	 * @param button the buttons
	 * @return is the left pressed
	 */
	public boolean isLeftButton(int button) {
		return (button == MouseEvent.BUTTON1);
	}
	/**
	 * Is the right button pressed?
	 * @param button the buttons
	 * @return is the right pressed
	 */
	public boolean isRightButton(int button) {
		return (button == MouseEvent.BUTTON3);
	}
	/**
	 * Is the middle button pressed?
	 * @param button the buttons
	 * @return is the middle pressed
	 */
	public boolean isMiddleButton(int button) {
		return (button == MouseEvent.BUTTON2);
	}
	/**
	 * Is the CTRL down?
	 * @param modifiers the modifiers
	 * @return is the CTRL down
	 */
	public boolean isCtrl(int modifiers) {
		return ((modifiers & MouseEvent.CTRL_DOWN_MASK) != 0) || ((modifiers & MouseEvent.CTRL_MASK) != 0);
	}
	/**
	 * Is the SHIFT down?
	 * @param modifiers the modifiers
	 * @return is the SHIFT down
	 */
	public boolean isShift(int modifiers) {
		return ((modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0) || ((modifiers & MouseEvent.SHIFT_MASK) != 0);
	}
}
