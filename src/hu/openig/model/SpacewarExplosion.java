/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * An animation for a space explosion.
 * @author akarnokd, 2011.08.15.
 */
public class SpacewarExplosion extends SpacewarObject {
    /** The phase counter for the animation if any. */
    public int phase;
    /** The delay counter for the animation to start playing. */
    public int delay = 0;
    /** The phase image of the beam (e.g., the rotating meson bubble). */
    public final BufferedImage[] phases;
    /** The structure to remove when the explosion is at the middle. */
    public SpacewarStructure target;
    /**
     * Constructor, initializes the phase images.
     * @param phases the phase images
     */
    public SpacewarExplosion(BufferedImage[] phases) {
        this.phases = Objects.requireNonNull(phases);
    }
    @Override
    public BufferedImage get() {
        if (delay > 0) {
            return null;
        }
        return phases[(phase) % phases.length];
    }
    /** @return true if the animation is at the middle. */
    public boolean isMiddle() {
        return ((phase) % phases.length) == phases.length / 2;
    }
    /**

     * Move to the next phase.
     * @return true if the end is reached
     */
    public boolean next() {
        if (delay > 0) {
            delay--;
            return false;
        }
        return ++phase == phases.length;
    }
}
