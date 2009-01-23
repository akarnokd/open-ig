/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.core;

import java.awt.Rectangle;

/**
 * The button position and state.
 * @author karnokd
 */
public class Btn {
	/** The location rectangle. */
	public final Rectangle rect;
	/** The button is disabled. */
	public boolean disabled;
	/** The button is pressed. */
	public boolean down;
	/** The action to perform when the button is clicked. */
	private BtnAction onClick;
	/**
	 * Constructor.
	 */
	public Btn() {
		rect = new Rectangle();
	}
	/**
	 * Constructor. Sets the onClick action.
	 * @param onClickAction the action
	 */
	public Btn(BtnAction onClickAction) {
		this();
		onClick = onClickAction;
	}
	/**
	 * Perform the click operation action.
	 */
	public void click() {
		if (onClick != null) {
			onClick.invoke();
		}
	}
//	/**
//	 * Set width and height from the image.
//	 * @param img the image to use
//	 */
//	public void setSizeFrom(BufferedImage img) {
//		rect.width = img.getWidth();
//		rect.height = img.getHeight();
//	}
}
