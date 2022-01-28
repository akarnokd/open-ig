/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.Exceptions;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author akarnokd, 2011.02.25.
 */
public class UIContainer extends UIComponent {
    /** The list of sub-components. */
    protected final List<UIComponent> components = new ArrayList<>();
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
        sortComponents();
    }
    /** Sort the components array according to Z order. */
    private void sortComponents() {
        Collections.sort(this.components, new Comparator<UIComponent>() {
            @Override
            public int compare(UIComponent o1, UIComponent o2) {
                return o1.z < o2.z ? -1 : (o1.z > o2.z ? 1 : 0);
            }
        });
    }
    /**
     * Add an iterable set of components to this container.
     * It keeps the internal components list sorted by z order.
     * Please do not assign components to multiple containers, instead,
     * create multiple instances (which might use the same data model in the background).
     * @param components the components
     */
    public void add(Iterable<? extends UIComponent> components) {
        for (UIComponent c : components) {
            c.parent = this;
            this.components.add(c);
        }
        sortComponents();
    }
    @Override
    public void draw(Graphics2D g2) {
        for (UIComponent c : components) {
            if (c.visible) {
                drawComponent(g2, c);
            }
        }
    }
    /**
     * Draw a particular component.
     * @param g2 the graphics context
     * @param c the component
     */
    public void drawComponent(Graphics2D g2, UIComponent c) {
        int px = c.x;
        int py = c.y;
        g2.translate(px, py);
        c.draw(g2);
        g2.translate(-px, -py);
    }
    @Override
    public boolean mouse(UIMouse e) {
//        Point pt = absLocation();
        boolean result = false;
        UIComponent target = null;
        int zmax = Integer.MIN_VALUE;
        for (UIComponent c : components) {
            if (c.visible && c.enabled && e.within(c.x, c.y, c.width, c.height) && !e.has(Type.LEAVE)) {
                if (!c.over) {
                    result |= c.mouse(e.copy(Type.ENTER, -c.x, -c.y));
                }
                c.over = true;
                if (zmax <= c.z) {
                    target = c;
                    zmax = c.z;
                }
            } else {
                if (c.visible && c.over) {
                    result |= c.mouse(e.copy(Type.LEAVE, -c.x, -c.y));
                }
                c.over = false;
            }
        }
        if (target != null) {
            // Relativize to the target
            result |= target.mouse(e.translate(-target.x, -target.y));
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
    /**
     * Add declared fields of Type UIComponent.
     */
    public void addThis() {
        for (Field f : getClass().getDeclaredFields()) {
            if (UIComponent.class.isAssignableFrom(f.getType())
                    && f.getDeclaringClass() == getClass()
                    && !f.isSynthetic()) {
                try {
                    f.setAccessible(true); // FIXME why is this necessary?
                    UIComponent c = UIComponent.class.cast(f.get(this));
                    if (c != null && c.parent == null) {
                        add(c);
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    Exceptions.add(e);
                }
            }
        }
    }
    @Override
    public UIComponent componentAt(int x, int y) {
        for (int i = components.size() - 1; i >= 0; i--) {
            UIComponent c = components.get(i);
            if (c.visible()) {
                UIComponent c0 = c.componentAt(x - this.x, y - this.y);
                if (c0 != null) {
                    return c0;
                }
            }
        }
        return super.componentAt(x, y);
    }
    /**
     * Remove a component.
     * @param c the component to remove
     * @return true if the removal succeeded
     */
    public boolean remove(UIComponent c) {
        c.parent = null;
        if (focused == c) {
            focused = null;
        }
        return components.remove(c);
    }
    /**
     * @return the sequence of components
     */
    public Iterable<UIComponent> components() {
        return components;
    }
    /**
     * Remove all child components of this container.
     */
    public void clear() {
        components.clear();
        focused = null;
    }
}
