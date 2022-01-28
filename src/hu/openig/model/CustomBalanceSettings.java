/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.XElement;

/**
 * Represents custom settings for affecting the game balance.
 * @author akarnokd, 2022. jan. 28.
 */
public final class CustomBalanceSettings {

    /**
     * Non-player races get this amount of credits per day.
     * <p>
     * Rationale: alien technology and economy is quite imbalance due to
     * lack of similar buildings to the humans, thus they tend to struggle
     * to build up.
     */
    public int nonPlayerBonusMoneyPerDay;

    /**
     * Non-player races get a percentage boost to their production speed.
     * <p>
     * Rationale: AI tends to lag behind in production of its fleets, hence
     * they tend to attack rarely.
     */
    public int nonPlayerProductionBoostPercent;

    /**
     * Non-player races will lose relationship points per 100 days.
     * <p>
     * Rationale: diplomacy is barely used and is not reasonably implemented yet,
     * thus aliens who are not scripted to hate the player will not bother the
     * player unless the player attacks. This will slowly turn them against the
     * main player.
     */
    public int nonPlayerRelationshipDeterioration;

    /**
     * Specifies how multiples of morale-boosting buildings contribute to the
     * overall morale of the planet.
     * @author akarnokd, 2022. jan. 28.
     */
    public enum BuildingMoraleCalculationMode {
        /**
         * Each building instance contributes linearly, i.e., if one Bar adds
         * 10% boost, two bars add 20% boost, and so on.
         * The default mode as implemented in Open-IG from the start.
         */
        LINEAR,
        /**
         * Each building can be built multiple times but only the one with the
         * best condition (health, energy) will be accounted per type.
         */
        BEST_PER_TYPE,
        /**
         * Overrides the game definition for buildings that have morale boost as
         * their output and limits them to one building per planet; one Bar, one
         * Stadium, etc.
         */
        ONE_PER_TYPE,
        /**
         * Each additional building instance contributes less and less boosts,
         * but otherwise the total combined amount is not bound.
         */
        DEGRESSIVE,
        /**
         * Each additional building contributes half of amount the previous building's
         * boost so that an infinite amount of buildings would contribute exactly double
         * bonus.
         */
        ASYMPTOTIC
    }

    /**
     * Specifies how multiple morale-boosting buildings are considered when calculating
     * the overall planet morale.
     * <p>
     * Rationale: the default linear is a bit exploitable but currently it is not clear
     * what it should be replaced with knowing how hard is to get money in the beginning
     * of the game. This will allow some experimentation by the player base.
     */
    public BuildingMoraleCalculationMode buildingMoraleCalculationMode = BuildingMoraleCalculationMode.LINEAR;

    /**
     * Saves the settings into an XElement as attributes.
     * @param world the target to save into
     */
    public void save(XElement world) {
        world.saveFields(this);
    }

    /**
     * Loads the settings from XElement attributes.
     * @param world the source to load from
     */
    public void load(XElement world) {
        world.loadFields(this);
    }

    /**
     * Restores the settings to their default values.
     */
    public void clear() {
        nonPlayerBonusMoneyPerDay = 0;
        nonPlayerProductionBoostPercent = 0;
        nonPlayerRelationshipDeterioration = 0;
        buildingMoraleCalculationMode = BuildingMoraleCalculationMode.LINEAR;
    }

    /**
     * Copy the settings from another instance of this class.
     * @param other the other instance
     */
    public void copyFrom(CustomBalanceSettings other) {
        nonPlayerBonusMoneyPerDay = other.nonPlayerBonusMoneyPerDay;
        nonPlayerProductionBoostPercent = other.nonPlayerProductionBoostPercent;
        nonPlayerRelationshipDeterioration = other.nonPlayerRelationshipDeterioration;
        buildingMoraleCalculationMode = other.buildingMoraleCalculationMode;
    }
}
