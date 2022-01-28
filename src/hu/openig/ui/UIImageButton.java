/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import java.awt.Graphics2D;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

import hu.openig.core.*;
import hu.openig.render.RenderTools;

/**
 * A three state image button with normal, pressed and hovered state.
 * Supports
 * the option to repeatedly call the <code>onClick</code>
 * handler when the user holds down a button.
 *
 * @author akarnokd, 2011.02.26.
 */
public class UIImageButton extends UIComponent {
    /** The normal state image. */
    protected BufferedImage normal;
    /** The pressed state image. */
    protected BufferedImage pressed;
    /** The hovered state image. */
    protected BufferedImage hovered;
    /** The disabled pattern to use for the button. */
    protected BufferedImage disabledPattern;
    /** The action to invoke when the button is clicked. */
    public Action0 onClick;
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
    /** The last mouse event. */
    public UIMouse lastEvent;
    /**
     * Constructor with the default images.
     * @param normal the normal state image
     * @param pressed the pressed state image
     * @param hovered the hovered state image, if null, the normal image is used instead
     */
    public UIImageButton(BufferedImage normal, BufferedImage pressed, BufferedImage hovered) {
        this.normal = normal;
        this.pressed = pressed;
        this.hovered = hovered != null ? hovered : normal;
        this.width = normal.getWidth();
        this.height = normal.getHeight();
        this.holdTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (holdDelay < 0 || !enabled || !visible) {
                    holdTimer.stop();
                } else {
                    doClick();
                }
            }
        });
    }
    /**
     * Creates an image button by using the elements of the supplied array.
     * The first element is the normal image, the second should be the pressed image
     * and an optional third image should be the hovered image. If
     * no hovered image is specified, the normal image is used instead.
     * You may use this constructor with the resource BufferedImage arrays of buttons
     * @param images the array of images.
     */
    public UIImageButton(BufferedImage... images) {
        this.normal = images[0];
        this.pressed = images.length > 1 ? images[1] : images[0];
        this.hovered = images.length > 2 ? images[2] : images[0];
        this.width = normal.getWidth();
        this.height = normal.getHeight();
        this.holdTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (holdDelay < 0 || !enabled || !visible) {
                    holdTimer.stop();
                } else {
                    doClick();
                }
            }
        });
    }
    /**
     * Set the mouse-hold delay to repeatedly fire the onClick
     * event. Use -1 to turn repetition off.
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
        if (!enabled && disabledPattern != null) {
            g2.drawImage(normal, 0, 0, null);
            RenderTools.fill(g2, 0, 0, width, height, disabledPattern);
        } else
        if (down) {
            g2.drawImage(pressed, 0, 0, null);
        } else
        if (over) {
            g2.drawImage(hovered, 0, 0, null);
        } else {
            g2.drawImage(normal, 0, 0, null);
        }
    }
    @Override
    public boolean mouse(UIMouse e) {
        this.lastEvent = e;
        switch (e.type) {
        case DOWN:
            down = true;
            if (holdDelay >= 0) {
                holdTimer.start();
                doClick();
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
        case ENTER:
            return hovered != normal;
        default:
            return false;
        }
    }
    /**
     * Set the disabled pattern for this button.
     * @param pattern the pattern to fill with the area of the button when it is disabled
     * @return this
     */
    public UIImageButton setDisabledPattern(BufferedImage pattern) {
        this.disabledPattern = pattern;
        return this;
    }
    @Override
    public UIComponent visible(boolean state) {
        boolean pd = down;
        down &= state;
        over &= state;
        if (!down && pd) {
            stop();
        }
        return super.visible(state);
    }
    @Override
    public UIComponent enabled(boolean state) {
        boolean pd = down;
        down &= state;
        over &= state;
        if (!down && pd) {
            stop();
        }
        return super.enabled(state);
    }
    /**
     * Set the normal image of this button.
     * @param image the image
     * @return this
     */
    public UIImageButton normal(BufferedImage image) {
        this.normal = image;
        return this;
    }
    /**
     * Set the pressed image of this button.
     * @param image the image
     * @return this
     */
    public UIImageButton pressed(BufferedImage image) {
        this.pressed = image;
        return this;
    }
    /**
     * Set the hovered image of this button.
     * @param image the image
     * @return this
     */
    public UIImageButton hovered(BufferedImage image) {
        this.hovered = image;
        return this;
    }
}
