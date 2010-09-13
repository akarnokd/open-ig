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

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * @author karnok, 2010.01.15.
 * @version $Revision 1.0$
 */
public class ClickLabel extends Button {
	/** The label. */
	public String label;
	/** The text size. */
	public int size;
	/** The action to invoke on press. */
	public Act onPressed;
	/** The action to invoke on release. */
	public Act onReleased;
	/** The action to perform on mouse enter. */
	public Act onEnter;
	/** The action to perform on mouse leave. */
	public Act onLeave;
	/** Is the label selected? */
	public boolean selected;
	/** The common resources. */
	public CommonResources commons;
	/**
	 * Paint the label.
	 * @param g2 the graphics object
	 * @param x0 the reference
	 * @param y0 the reference
	 */
	public void paintTo(Graphics2D g2, int x0, int y0) {
		g2.setColor(Color.BLACK);
		g2.fillRect(x0 + x, y0 + y, getWidth(), getHeight());
		int color = selected ? 0xFFFFCC00 : (mouseOver ? 0xFFFFEE00 : 0xFF00CC00);
		commons.text.paintTo(g2, x0 + x + 5, y0 + y + 2, size, color, commons.labels.get(label));
	}
	/**
	 * @return the text width
	 */
	public int getWidth() {
		return commons.text.getTextWidth(size, commons.labels.get(label)) + 10;
	}
	/**
	 * @return the text height
	 */
	public int getHeight() {
		return size + 4;
	}
	/**
	 * Invoke the action.
	 */
	public void onPressed() {
		if (onPressed != null) {
			onPressed.act();
		}
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.render.Button#onEnter()
	 */
	@Override
	public void onEnter() {
		if (onEnter != null) {
			onEnter.act();
		}
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.render.Button#onLeave()
	 */
	@Override
	public void onLeave() {
		if (onLeave != null) {
			onLeave.act();
		}
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.render.Button#onReleased()
	 */
	@Override
	public void onReleased() {
		if (onReleased != null) {
			onReleased.act();
		}
	}

}
