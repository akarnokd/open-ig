/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.core.Action0;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * A scrollable box with a single viewport referencing
 * another UIComponent and offers automatic

 * scroll button management.
 * @author akarnokd, 2011.02.26.
 */
public class UIScrollBox extends UIContainer {
    /** The up button. */
    protected UIImageButton upButton;
    /** The down button. */
    protected UIImageButton downButton;
    /** The content. */
    protected UIComponent content;
    /** The scroll delta in pixels. */
    protected int delta;
    /** The gap between the scroll viewport and the scroll buttons. */
    protected int gaps = 5;
    /** The border color. */
    protected int borderColor;
    /**
     * Construct the scroll box.
     * @param content the scroll content
     * @param delta the scroll delta
     * @param upButton the up button
     * @param downButton the down button
     */
    public UIScrollBox(final UIComponent content, final int delta,

            UIImageButton upButton, UIImageButton downButton) {
        this.upButton = upButton;
        this.downButton = downButton;
        this.delta = delta;
        this.content = content;
        this.add(upButton, downButton, content);
        adjustButtons();
        upButton.onClick = new Action0() {
            @Override
            public void invoke() {
                scrollBy(delta);
                askRepaint();
            }
        };
        upButton.setHoldDelay(150);
        downButton.onClick = new Action0() {
            @Override
            public void invoke() {
                scrollBy(-delta);
                askRepaint();
            }
        };
        downButton.setHoldDelay(150);
        upButton.z = 1;
        downButton.z = 1;
    }
    /** Adjust the visibility of the buttons. */
    public void adjustButtons() {
        upButton.visible = content.y < 0;
        upButton.down &= upButton.visible;
        upButton.over &= upButton.visible;
        downButton.visible = content.y + content.height > height;
        downButton.down &= downButton.visible;
        downButton.over &= downButton.visible;
    }
    @Override
    public void draw(Graphics2D g2) {
        int hgap = (height - upButton.height - downButton.height) / 3;
        upButton.x = width - upButton.width /*  - gaps */;
        downButton.x = width - downButton.width /* - gaps */;
        upButton.y = hgap;
        downButton.y = hgap * 2 + upButton.height;
        Shape save0 = g2.getClip();
        g2.clipRect(0, 0, width, height);
        super.draw(g2);
        if (borderColor != 0) {
            g2.setColor(new Color(borderColor, true));
            g2.drawRect(0, 0, width - 1, height - 1);
        }
        g2.setClip(save0);
    }
    @Override
    public boolean mouse(UIMouse e) {
        boolean result = false;
        if (e.type == UIMouse.Type.WHEEL) {
            if (!super.mouse(e)) {
                result |= scrollBy(-e.z * delta);
            }
        }
        return super.mouse(e) || result;
    }
    /**
     * Scroll the content by the given amount.
     * @param dy the amount to scroll.
     * @return need for repaint?
     */
    public boolean scrollBy(int dy) {
        int y0 = content.y;

        content.y += dy;
        if (content.y + content.height <= height) {
            content.y = height - content.height;
        }
        if (content.y > 0) {
            content.y = 0;
        }

        if (y0 != content.y) {
            adjustButtons();
            return true;
        }
        return false;
    }
    /**
     * @return the current border color ARGB
     */
    public int borderColor() {
        return borderColor;
    }
    /**
     * Set the border color.
     * @param newColor new color ARGB
     * @return this
     */
    public UIScrollBox borderColor(int newColor) {
        this.borderColor = newColor;
        return this;
    }
    /**
     * Scroll the box until the sub-component becomes visible.
     * @param c the component
     */
    public void scrollToVisible(UIComponent c) {
        int py = 0;
        int ph = c.height;
        while (c.parent != null) {
            if (c.parent != this) {
                py += c.y;
                c = c.parent;
            } else {
                break;
            }
        }
        if (c == null) {
            throw new AssertionError("Not in the hierarchy of this box!");
        }
        int visibleTop = -content.y;
        int visibleBottom = visibleTop + height;
        if (py < visibleTop) {
            scrollBy(visibleTop - py);
        } else
        if (py + ph >= visibleBottom) {
            scrollBy(visibleBottom - py - ph);
        }
    }
}
