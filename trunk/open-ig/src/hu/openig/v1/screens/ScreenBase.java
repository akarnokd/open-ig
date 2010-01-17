/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.screens;

import hu.openig.v1.core.Configuration;
import hu.openig.v1.core.ResourceLocator;

import java.awt.Graphics2D;

import javax.swing.JComponent;

/**
 * A screen base class.
 * @author karnokd, 2009.12.23.
 * @version $Revision 1.0$
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
}
