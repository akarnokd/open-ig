/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.ui.UIMouse.Type;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author akarnokd, 2011.02.25.
 */
public class UIContainer extends UIComponent {
	/** The list of sub-components. */
	protected final List<UIComponent> components = new ArrayList<UIComponent>();
	/** The currently focused component, which receives keyboard events. */
	protected UIComponent focused;
	/**
	 * Add an array of components to this container.
	 * It keeps the internal components list sorted by z order.
	 * Please do not assign components to multiple containers, instead,
	 * create multiple instances (which might use the same data model in the background).
	 * @param components the components
	 */
	public void add(UIComponent... components) {
		for (UIComponent c : components) {
			c.parent = this;
			this.components.add(c);
		}
		Collections.sort(this.components, new Comparator<UIComponent>() {
			@Override
			public int compare(UIComponent o1, UIComponent o2) {
				return o1.z < o2.z ? -1 : (o1.z > o1.z ? 1 : 0);
			}
		});
	}
	@Override
	public void draw(Graphics2D g2) {
		for (UIComponent c : components) {
			if (c.visible) {
				g2.translate(c.x, c.y);
				c.draw(g2);
				g2.translate(-c.x, -c.y);
			}
		}
	}
	@Override
	public boolean mouse(UIMouse e) {
		Point pt = absLocation();
		boolean result = false;
		UIComponent target = null;
		int zmax = -1;
		for (UIComponent c : components) {
			if (c.visible && c.enabled && e.within(pt.x + c.x, pt.y + c.y, c.width, c.height)) {
				if (!c.over) {
					result |= c.mouse(e.copy(Type.ENTER));
				}
				c.over = true;
				if (zmax < c.z) {
					target = c;
					zmax = c.z;
				}
			} else {
				if (c.visible && c.over) {
					result |= c.mouse(e.copy(Type.LEAVE));
				}
				c.over = false;
			}
		}
		if (target != null) {
			result |= target.mouse(e);
		}
		return result;
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		boolean result = false;
		for (UIComponent c : components) {
			if (c == focused) {
				result |= c.keyboard(e);
			}
		}
		return result;
	}
}
