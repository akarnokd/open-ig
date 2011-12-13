/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.core.Func1;
import hu.openig.render.TextRenderer;

import java.awt.Graphics2D;

/**
 * @author akarnokd, Apr 18, 2011
 */
public class UISpinner extends UIContainer {
	/** The the text to display. */
	public Func1<Void, String> getValue;
	/** The previous button. */
	public final UIImageButton prev;
	/** The next button. */
	public final UIImageButton next;
	/** The text renderer. */
	protected final TextRenderer tr;
	/** The text size. */
	protected int size;
	/** The text color. */
	protected int color = TextRenderer.GREEN;
	/**
	 * Construction.
	 * @param size the text height
	 * @param prev the previous button
	 * @param next the next button
	 * @param tr the text renderer
	 */
	public UISpinner(int size, UIImageButton prev, UIImageButton next, TextRenderer tr) {
		this.prev = prev;
		this.next = next;
		this.height = prev.height;
		this.tr = tr;
		this.size = size;
		add(prev);
		add(next);
	}
	@Override
	public void draw(Graphics2D g2) {
		prev.location(0, 0);
		next.location(width - next.width, 0);
		
		String n = getValue.invoke(null);
		int w = tr.getTextWidth(size, n);
		int dx = prev.width + (width - prev.width - next.width - w) / 2;
		int dy = (prev.height - size) / 2;
		tr.paintTo(g2, dx, dy, size, color, n);
		
		super.draw(g2);
	}
}
