/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.core.Action0;
import hu.openig.render.GenericButtonRenderer;
import hu.openig.render.RenderTools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

/**
 * An Imperium Galactica styled button component.
 * @author akarnokd, 2011.03.04.
 */
public class UIGenericButton extends UIComponent {
    /** The disabled pattern to use for the button. */
    protected BufferedImage disabledPattern;
    /** The action to invoke when the button is clicked. */
    public Action0 onClick;
    /**

     * The action to invoke when the button is pressed down.
     * Can be used to use this button as a tab.
     */
    public Action0 onPress;
    /**

     * The optional delay to fire onClick events when
     * the mouse is pressed over the button.
     * Unit is in milliseconds.
     */
    protected int holdDelay = -1;
    /** The timer to send pressed events periodically. */
    protected Timer holdTimer;
    /** Is the mouse pressed down on this component. */
    protected boolean down;
    /** The text to display. */
    protected String text;
    /** The text color. */
    protected int color = 0xFF000000;
    /** The button renderer. */
    protected final GenericButtonRenderer normal;
    /** The button renderer. */
    protected final GenericButtonRenderer pressed;
    /** The font size. */
    protected int size;
    /** The icon to display. */
    protected BufferedImage icon;
    /** The font metrics. */
    final FontMetrics fm;
    /**
     * Constructor with the default images.
     * @param text the text label
     * @param fm the font metrics used to compute the size
     * @param normal the actual button renderer
     * @param pressed the pressed state renderer
     */
    public UIGenericButton(String text, FontMetrics fm,

            GenericButtonRenderer normal, GenericButtonRenderer pressed) {
        this.text = text;
        this.fm = fm;
        this.normal = normal;
        this.pressed = pressed;
        Dimension d = normal.getPreferredSize(fm, text);
        size = fm.getFont().getSize();
        this.width = d.width;
        this.height = d.height;
        this.holdTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doClick();
                if (holdDelay < 0 || !enabled || !visible) {
                    holdTimer.stop();
                }
            }
        });
    }
    /**
     * Set the mouse-hold delay to repeatedly fire the onClick
     * event. Use -1 to turn of repetition.
     * @param delayMillis the delay in milliseconds
     */
    public void setHoldDelay(int delayMillis) {
        this.holdDelay = delayMillis;
        if (holdDelay >= 0) {
            holdTimer.setInitialDelay(2 * holdDelay);
            holdTimer.setDelay(holdDelay);
        }
    }
    /**
     * Compute the preferred size in respect to the given font metrics.
     * @param fm the font metrics
     * @return the preferred dimension
     */
    public Dimension getPreferredSize(FontMetrics fm) {
        return normal.getPreferredSize(fm, text);
    }
    /**
     * Stop all internal timers to allow cleanup and thread exit.
     */
    public void stop() {
        holdTimer.stop();
    }
    /**
     * Call the click action if set.
     */
    protected void doClick() {
        if (onClick != null) {
            onClick.invoke();
        }
    }
    @Override
    public void draw(Graphics2D g2) {
        Font f1 = g2.getFont();
        g2.setFont(f1.deriveFont(Font.BOLD).deriveFont((float)size));
        g2.setColor(new Color(color));
        if (!enabled && disabledPattern != null) {
            normal.paintTo(g2, 0, 0, width, height, false, text);
            RenderTools.fill(g2, 0, 0, width, height, disabledPattern);
        } else
        if (down) {
            pressed.paintTo(g2, 0, 0, width, height, true, text);
        } else {
            normal.paintTo(g2, 0, 0, width, height, false, text);
        }
        if (icon != null) {
            int iw = icon.getWidth();
            int ih = icon.getHeight();
            g2.drawImage(icon, (width - iw) / 2, (height - ih) / 2, null);
        }
        g2.setFont(f1);
    }
    @Override
    public boolean mouse(UIMouse e) {
        switch (e.type) {
        case DOWN:
            down = true;
            if (holdDelay >= 0) {
                holdTimer.start();
                doClick();
            } else
            if (onPress != null) {
                onPress.invoke();
            }
            return true;
        case UP:
            if (down) {
                down = false;
                holdTimer.stop();
                if (holdDelay < 0) {
                    doClick();
                }
            }
            return true;
        case LEAVE:
            down = false;
            holdTimer.stop();
            return true;
        default:
            return false;
        }
    }
    /**
     * Set the disabled pattern for this button.
     * @param pattern the pattern to fill with the area of the button when it is disabled
     * @return this
     */
    public UIGenericButton disabledPattern(BufferedImage pattern) {
        this.disabledPattern = pattern;
        return this;
    }
    /**
     * Change the text of the button.
     * @param text the new text
     * @return this
     */
    public UIGenericButton text(String text) {
        return text(text, false);
    }
    /**
     * Change the text of the button.
     * @param text the new text
     * @param resize adjust button size?
     * @return this
     */
    public UIGenericButton text(String text, boolean resize) {
        this.text = text;
        if (resize) {
            Dimension d = normal.getPreferredSize(fm, text);
            size = fm.getFont().getSize();
            this.width = d.width;
            this.height = d.height;
        }
        return this;
    }
    /** @return the text color ARGB. */
    public int color() {
        return color;
    }
    /**
     * Sets the text color.
     * @param color the color ARGB
     * @return this
     */
    public UIGenericButton color(int color) {
        this.color = color;
        return this;
    }
    /**
     * Sets the icon to display.
     * @param icon the icon
     * @return this
     */
    public UIGenericButton icon(BufferedImage icon) {
        this.icon = icon;
        return this;
    }
    @Override
    public String toString() {
        return "UIGenericButton: " + text;
    }
}
