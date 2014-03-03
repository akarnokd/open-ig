/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.core.Action1;
import hu.openig.render.TextRenderer;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic list for options. 
 * @author akarnokd, 2012.03.17.
 */
public class OptionList extends UIContainer {
	/** An option item to display. */
	public static class OptionItem {
		/** The display label. */
		public String label;
		/** Is enabled? */
		public boolean enabled = true;
		/** Mouse over? */
		public boolean hover;
		/** Selected? */
		public boolean selected;
		/** The associated user object. */
		public Object userObject;
		/**
		 * Create a copy of this object.
		 * @return the copy
		 */
		public OptionItem copy() {
			OptionItem result = new OptionItem();
			result.label = label;
			result.hover = hover;
			result.enabled = enabled;
			result.selected = selected;
			return result;
		}
	}
	/** The text size. */
	public int textsize = 14;
	/** The distance after the text. */
	public int after = 3;
	/** The distance before the text. */
	public int before = 0;
	/** The list of items. */
	public final List<OptionItem> items = new ArrayList<>();
	/** The action to invoke when a menu item is selected. */
	public Action1<Integer> onSelect;
	/** Called when the given item is highlight changes. */
	public Action1<Integer> onHighlight;
	/** If mouse pressed. */
	boolean mouseDown;
	/** The text renderer. */
	TextRenderer text;
	/**
	 * Construct the list with the given text renderer.
	 * @param text the renderer
	 */
	public OptionList(TextRenderer text) {
		this.text = text;
	}
	/** Fit the control's width to accomodate all labels. */
	public void fit() {
		int w = 0;
		for (OptionItem oi : items) {
			w = Math.max(w, text.getTextWidth(textsize, oi.label));
		}
		this.width = w + 10;
		this.height = 10 + items.size() * (textsize + before + after);
		if (!items.isEmpty()) {
			this.height -= after;
		}
	}
	@Override
	public void draw(Graphics2D g2) {
		int dy = 5;
		for (OptionItem oi : items) {
			dy += before;
			int color = TextRenderer.GREEN;
			if (!oi.enabled) {
				color = TextRenderer.GRAY;
			} else
			if (oi.selected) {
				color = TextRenderer.ORANGE;
			} else
			if (oi.hover) {
				color = TextRenderer.WHITE;
			}
			
			text.paintTo(g2, 5, dy, textsize, color, oi.label);
			
			dy += after + textsize;
		}
	}
	@Override
	public boolean mouse(UIMouse e) {
		int idx = (e.y - 5) / (textsize + before + after);
		if (e.has(Type.MOVE) || e.has(Type.DRAG)) {
			hover(idx, e.has(Type.DRAG) || mouseDown);
			return true;
		} else
		if (e.has(Type.DOWN)) {
			hover(idx, true);
			mouseDown = true;
			return true;
		} else
		if (e.has(Type.UP)) {
			mouseDown = false;
			if (idx >= 0 && idx < items.size()) {
				OptionItem oi = items.get(idx);
				if (oi.enabled && oi.selected && onSelect != null) {
					onSelect.invoke(idx);
					oi.selected = false;
					return true;
				}
			}
		} else
		if (e.has(Type.LEAVE)) {
			hover(-1, false);
			mouseDown = false;
		}
		
		return super.mouse(e);
	}
	/**
	 * Hover or select a given indexth item.
	 * @param idx the index
	 * @param select should the item be selected
	 */
	public void hover(int idx, boolean select) {
		int i = 0;
		for (OptionItem oi : items) {
			
			boolean oldHover = oi.hover;
			
			oi.hover = oi.enabled && i == idx;
			oi.selected = oi.hover & select;
			
			if (oldHover != oi.hover && onHighlight != null) {
				onHighlight.invoke(i);
			}
			
			i++;
		}
	}
}
