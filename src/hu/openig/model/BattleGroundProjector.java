/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * The space battle's ground projectors (e.g., planetary guns).
 * @author akarnokd, 2011.08.16.
 */
public class BattleGroundProjector {
    /** The normal image. */
    public BufferedImage[] normal;
    /** The alien/alternative image. */
    public BufferedImage[] alternative;
    /** The destruction sound. */
    public SoundType destruction;
    /** The projectile used. */
    public String projectile;
    /** The information image. */
    public String infoImageName;
    /** The inflicted damage. */
    public int baseDamage;
    /** The rotation speed. */
    public int rotationTime;
    /** The battle efficiency settings of this projector. */
    public final List<BattleEfficiencyModel> efficiencies = new ArrayList<>();
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
}
