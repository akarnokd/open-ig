/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * A simple panel with the ability to set background and border colors.
 * @author akarnokd, 2012.08.20.
 */
public class UIPanel extends UIContainer {
    /** The border width. */
    protected float borderWidth = 1f;
    /** The border color. */
    protected int borderColor;
    /** The background color. */
    protected int backgroundColor;
    @Override
    public void draw(Graphics2D g2) {
        if (backgroundColor != 0) {
            g2.setColor(new Color(backgroundColor, true));
            g2.fillRect(0, 0, width, height);
        }
        super.draw(g2);
        if (borderColor != 0) {
            g2.setColor(new Color(borderColor, true));
            Stroke st = g2.getStroke();
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawRect(0, 0, width - 1, height - 1);
            g2.setStroke(st);
        }
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
    public UIPanel borderColor(int newColor) {
        this.borderColor = newColor;
        return this;
    }
    /**
     * @return the current background color ARGB
     */
    public int backgroundColor() {
        return backgroundColor;
    }
    /**
     * Set the background color.
     * @param newColor the new color ARGB
     * @return this
     */
    public UIPanel backgroundColor(int newColor) {
        this.backgroundColor = newColor;
        return this;
    }
    /**
     * @return the current border width in pixels
     */
    public float borderWidth() {
        return borderWidth;
    }
    /**
     * Set the border width.
     * @param newWidth the new width in pixels
     * @return this
     */
    public UIPanel borderWidth(float newWidth) {
        this.borderWidth = newWidth;
        return this;
    }
    /**
     * Change the size of the panel to encompass its contents.
     * @return this
     */
    public UIPanel pack() {
        int maxx = 0;
        int maxy = 0;
        for (UIComponent c : components) {
            maxx = Math.max(c.x + c.width, maxx);
            maxy = Math.max(c.y + c.height, maxy);
        }
        this.width = maxx;
        this.height = maxy;
        return this;
    }
}
