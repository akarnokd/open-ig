/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;

/**
 * A building turret graphics and location description.
 * @author akarnokd, Jul 31, 2011
 */
public class BattleGroundTurret {
    /** The rendering cell X relative to the building. */
    public int rx;
    /** The rendering cell Y relative to the building. */
    public int ry;
    /** The pixel offset from the cells on-screen coordinates. */

    public int px;
    /** The pixel offset from the cells on-screen coordinates. */
    public int py;
    /** The [rotation][fire phase] images of the turret. */

    public BufferedImage[][] matrix;
    /** The firing sound effect. */
    public SoundType fire;
    /** The firing range. */
    public double maxRange;
    /** The damage. */
    public int baseDamage;
    /** The rotation time per angle-segment. */
    public int rotationTime;
    /** The delay between firing. */
    public int delay;
    /**
     * Returns the damage for the given owner.
     * @param owner the owner
     * @return the damage
     */
    public double damage(Player owner) {
        double dmg = baseDamage;
        Trait t = owner.traits.trait(TraitKind.WEAPONS);
        if (t != null) {
            dmg *= 1 + t.value / 100;
        }
        return dmg;
    }
    /** @return the number of rotation images. */
    public int angles() {
        return matrix[0].length;
    }
}
