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
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIMouse;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * A screen base class.
 * @author akarnokd, 2009.12.23.
 */
public abstract class ScreenBase extends UIContainer {
	/** The global configuration object. */
	protected Configuration config;
	/** The global resource locator. */
	protected ResourceLocator rl;
	/** The common resources. */
	protected CommonResources commons;
	/** 
	 * Initialize any resources that are required by the screen. 
	 * Called by the rendering system to initialize a screen at startup.
	 * @param commons the configuration object
	 */
	public final void initialize(CommonResources commons) {
		this.components.clear();
		this.commons = commons;
		this.config = commons.config;
		this.rl = commons.rl;
		onInitialize();
	}
	/** The custom initialization routine. Override this to perform additional initialization, i.e., create sub-components. */
	public abstract void onInitialize();
	/** 
	 * Perform actions when the player displays the screen (e.g start animation timers). 
	 * @param mode the object to pass in (e.g., the tab selection, etc.) 
	 */
	public abstract void onEnter(Object mode);
	/** Perform actions when the player leaves the screen (e.g. stop animation timers). */
	public abstract void onLeave();
	/** Release resources of the screen, and e.g. cancel any animation timers. */
	public abstract void onFinish();
	/** Called by the rendering system when the parent swing component changed its size. */
	public final void resize() {
		width = getInnerWidth();
		height = getInnerHeight();
		onResize();
	}
	/** Called if the component size changed since the last call. */
	public abstract void onResize();
	/** Ask for the parent JComponent to repaint itself. */
	@Override
	public void askRepaint() {
		askRepaint(this);
	}
	/**
	 * Ask for the repaint of the given  component area only.
	 * @param c the target component
	 */
	public void askRepaint(UIComponent c) {
		Point p = c.absLocation();
		commons.control.repaintInner(p.x, p.y, c.width, c.height);
	}
	/**
	 * Retrieve the parent swing component's width.
	 * @return the width
	 */
	public int getInnerWidth() {
		return commons.control.getInnerWidth();
	}
	/**
	 * Retrieve the parent swing component's height.
	 * @return the height
	 */
	public int getInnerHeight() {
		return commons.control.getInnerHeight();
	}
	/**
	 * @return The rectangle that represents the non-transparent region of this screen.
	 * It may be used to optimize rendering of any underlying screen.
	 */
	public Rectangle nontransparent() {
		return new Rectangle(0, 0, width, height);
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (super.mouse(e)) {
			askRepaint();
			return true;
		}
		return false;
	}
}
