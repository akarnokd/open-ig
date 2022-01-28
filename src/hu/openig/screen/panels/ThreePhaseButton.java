/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.screen.panels;

import hu.openig.core.Action0;
import hu.openig.render.RenderTools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** A three phase button. */
public class ThreePhaseButton {
    /** The X coordinate. */
    public int x;
    /** The Y coordinate. */
    public int y;
    /** The three phases: normal, selected, selected and pressed. */
    BufferedImage[] phases;
    /** Selected state. */
    public boolean selected;
    /** Pressed state. */
    public boolean pressed;
    /** The action to perform on the press. */
    public Action0 action;
    /** Is the button disabled? */
    public boolean enabled = true;
    /** The disabled pattern. */
    BufferedImage disabledPattern;
    /**
     * Constructor.
     * @param phases the phases
     * @param disabledPattern the disabled pattern
     */
    public ThreePhaseButton(BufferedImage[] phases, BufferedImage disabledPattern) {
        this.phases = phases;
        this.disabledPattern = disabledPattern;
    }
    /**
     * Constructor.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param phases the phases
     * @param disabledPattern the disabled pattern
     */
    public ThreePhaseButton(int x, int y, BufferedImage[] phases, BufferedImage disabledPattern) {
        this(phases, disabledPattern);
        this.x = x;
        this.y = y;
    }
    /**

     * Render the button.
     * @param g2 the graphics object
     */
    public void paintTo(Graphics2D g2) {
        if (!enabled) {
            g2.drawImage(phases[0], x, y, null);
            RenderTools.fill(g2, x, y, phases[0].getWidth(), phases[0].getHeight(), disabledPattern);
        } else
        if (pressed) {
            g2.drawImage(phases[1], x, y, null);
        } else
        if (selected) {
            g2.drawImage(phases[2], x, y, null);
        } else {
            g2.drawImage(phases[0], x, y, null);
        }
    }
    /**
     * Test if the mouse is within this button.
     * @param mx the mouse X coordinate
     * @param my the mouse Y coordinate
     * @return true if within the button
     */
    public boolean test(int mx, int my) {
        return enabled && mx >= x && my >= y && mx < x + phases[0].getWidth() && my < y + phases[0].getHeight();
    }
    /** Invoke the associated action if present. */
    public void invoke() {
        if (action != null) {
            action.invoke();
        }
    }
}
