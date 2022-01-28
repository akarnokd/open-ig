/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.core.Action0;
import hu.openig.render.RenderTools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * A two state image button to toggle between.
 * @author akarnokd, 2011.02.26.
 */
public class UIImageToggleButton extends UIComponent {
    /** The normal state image. */
    protected BufferedImage normalImage;
    /** The hovered state image. */
    protected BufferedImage selectedImage;
    /** The disabled pattern to use for the button. */
    protected BufferedImage disabledPattern;
    /** The action to invoke when the button is clicked. */
    public Action0 onClick;
    /** Is the mouse pressed down on this component. */
    protected boolean down;
    /** Indicates the button is in selected state. */
    public boolean selected;
    /**

    public boolean mayDeselect;
    /**
     * Constructor with the default images.
     * @param normalImage the normal state image
     * @param selectedImage the selected image
     */
    public UIImageToggleButton(BufferedImage normalImage,

            BufferedImage selectedImage) {
        this.normalImage = normalImage;
        this.selectedImage = selectedImage;
        this.width = normalImage.getWidth();
        this.height = normalImage.getHeight();
    }
    /**
     * Creates an image button by using the elements of the supplied array.
     * The first element is the normal image, the second should be the pressed image
     * and an optional third image should be the hovered image. If
     * no hovered image is specified, the normal image is used instead.
     * You may use this constructor with the resource BufferedImage arrays of buttons
     * @param images the array of images.
     */
    public UIImageToggleButton(BufferedImage[] images) {
        this.normalImage = images[0];
        this.selectedImage = images[1];
        this.width = normalImage.getWidth();
        this.height = normalImage.getHeight();
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
            g2.drawImage(normalImage, 0, 0, null);
            RenderTools.fill(g2, 0, 0, width, height, disabledPattern);
        } else
        if (selected) {
            g2.drawImage(selectedImage, 0, 0, null);
        } else {
            g2.drawImage(normalImage, 0, 0, null);
        }
    }
    @Override
    public boolean mouse(UIMouse e) {
        switch (e.type) {
        case DOWN:
            if (!selected) {
                selected = true;
                doClick();
            } else {
                down = true;
            }
            return true;
        case UP:
            if (down) {
                selected = false;
                doClick();
                down = false;
            }
            return true;
        case LEAVE:
            down = false;
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
    public UIImageToggleButton setDisabledPattern(BufferedImage pattern) {
        this.disabledPattern = pattern;
        return this;
    }
}
