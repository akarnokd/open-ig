/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The battle efficiency settings.
 * @author akarnokd, 2012.08.15.
 */
public class BattleEfficiencyModel {
    /** The owner, null if any.*/
    public String owner;
    /** The target's technology category, if any. */
    public ResearchSubCategory category;
    /** The target's concrete id. */
    public String id;
    /** The damage multiplier. */
    public double damageMultiplier;

    /**
     * Check if the given structure matches this efficiency model.
     * @param s the structure
     * @return true if matches
     */
    public boolean matches(SpacewarStructure s) {
        if (owner != null && !owner.equals(s.owner.id)) {
            return false;
        }
        if (category != null && s.category != category) {
            return false;
        }
        return !(id != null && !id.equals(s.techId));
    }
}
