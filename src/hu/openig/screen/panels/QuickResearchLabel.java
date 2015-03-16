/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.render.TextRenderer;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

/**
 * A quick research label with extra mouse behavior. 
 * @author akarnokd, 2012.06.23.
 */
public class QuickResearchLabel extends UILabel {
	/** Mouse pressed. */
	boolean dragOver;
	/**
	 * Initialize the label.
	 * @param text the text to display
	 * @param size the text size
	 * @param tr the text renderer
	 */
	public QuickResearchLabel(String text, int size, TextRenderer tr) {
		super(text, size, tr);
	}
	/**
	 * Initialize the label.
	 * @param text the text to display
	 * @param size the text size
	 * @param width the width of the label
	 * @param tr the text renderer
	 */
	public QuickResearchLabel(String text, int size, int width, TextRenderer tr) {
		super(text, size, width, tr);
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.ENTER) || e.has(Type.LEAVE)) {
			super.mouse(e);
			return true;
		}
		if (e.has(Type.DRAG)) {
			dragOver = true;
		}
		if (e.has(Type.LEAVE)) {
			dragOver = false;
		}
		if (e.has(Type.UP) && dragOver) {
			dragOver = false;
			if (onPress != null) {
				onPress.invoke();
			}
		}
		return super.mouse(e);
	}
}
