/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;
import hu.openig.core.Pair;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The model definition for battles.
 * @author akarnokd, Jul 31, 2011
 */
public class BattleModel {
    /** A map from building id to (map from race to list of turret definition). */
    public final Map<String, Map<String, List<BattleGroundTurret>>> turrets = new HashMap<>();
    /** The space entity definitions. */
    public final Map<String, BattleSpaceEntity> spaceEntities = new HashMap<>();
    /** The ground entity definitions. */
    public final Map<String, BattleGroundVehicle> groundEntities = new HashMap<>();
    /** The map from projectile ID to [rotation][fire-phase] images. */
    public final Map<String, BattleProjectile> projectiles = new HashMap<>();
    /** The ground projectors definitions. */
    public final Map<String, BattleGroundProjector> groundProjectors = new HashMap<>();
    /** The ground shield definitions. */
    public final Map<String, BattleGroundShield> groundShields = new HashMap<>();
    /** The space battle layouts. */
    public final List<BattleSpaceLayout> layouts = new ArrayList<>();
    /** The ground hit points of buildings per player. */
    public final Map<Pair<String, String>, Integer> groundHitpoints = new HashMap<>();
    /** The space hitpoints of buildings. */
    public final Map<Pair<String, String>, Integer> spaceHitpoints = new HashMap<>();
    /** Additional technology properties. */
    public final Map<Pair<String, String>, Map<String, String>> properties = new HashMap<>();
    /** A pair of anti-ecm, ecm to hit probability per difficulty. */
    public final Map<Difficulty, Map<Pair<Integer, Integer>, Double>> ecmMatrix = new HashMap<>();
    /** The probabilities for backfiring, if not present, there is no limit. */
    public final Map<Difficulty, Double> backfires = new HashMap<>();
    /** The explosion images. */
    public final Map<ExplosionType, BufferedImage[]> explosionAnimations = new HashMap<>();
    /** The flying rocket [phase][angle]. */
    public BufferedImage[][] groundRocket;
    /** The direct attack units. */
    public final EnumSet<GroundwarUnitType> directAttackUnits = EnumSet.of(
            GroundwarUnitType.ARTILLERY,
            GroundwarUnitType.TANK,
            GroundwarUnitType.ROCKET_SLED,
            GroundwarUnitType.SELF_REPAIR_TANK,
            GroundwarUnitType.KAMIKAZE,
            GroundwarUnitType.PARALIZER,
            GroundwarUnitType.ROCKET_JAMMER
    );
    /** The set of units that may attempt to get closer to their target. */
    public final EnumSet<GroundwarUnitType> getCloserUnits = EnumSet.of(
            GroundwarUnitType.TANK,

            GroundwarUnitType.KAMIKAZE,
            GroundwarUnitType.SELF_REPAIR_TANK,
            GroundwarUnitType.PARALIZER,
            GroundwarUnitType.ROCKET_JAMMER
    );
    /**
     * Add a turret definition to the {@code turrets} mapping.
     * @param buildingId the building identifier.
     * @param race the race name
     * @param turret the turret definition object
     */
    public void addTurret(String buildingId, String race, BattleGroundTurret turret) {
        Map<String, List<BattleGroundTurret>> bt = turrets.get(buildingId);
        if (bt == null) {
            bt = new HashMap<>();
            turrets.put(buildingId, bt);
        }
        List<BattleGroundTurret> ts = bt.get(race);
        if (ts == null) {
            ts = new ArrayList<>();
            bt.put(race, ts);
        }
        ts.add(turret);
    }
    /**
     * Get the turret positions based on the building type and race.
     * @param buildingId the building id
     * @param race the race
     * @return the list of turret ports
     */
    public List<BattleGroundTurret> getTurrets(String buildingId, String race) {
        return turrets.get(buildingId).get(race);
    }
    /**
     * Retrieve a technology property.
     * @param technology the technology id
     * @param player the optional player id
     * @param property the property name
     * @return the property value
     */
    public String getProperty(String technology, String player, String property) {
        Pair<String, String> key = Pair.of(technology, player);
        Map<String, String> kv = properties.get(key);
        if (kv != null) {
            String v = kv.get(property);
            if (v != null) {
                return v;
            }
        }
        key = Pair.of(technology, null);
        kv = properties.get(key);
        if (kv != null) {
            String v = kv.get(property);
            if (v != null) {
                return v;
            }
        }
        return null;
    }
    /**
     * Retrieve a technology property.
     * @param technology the technology id
     * @param player the optional player id
     * @param property the property name
     * @return the property value
     */
    public int getIntProperty(String technology, String player, String property) {
        return Integer.parseInt(getProperty(technology, player, property));
    }
    /**
     * Retrieve a technology property.
     * @param technology the technology id
     * @param player the optional player id
     * @param property the property name
     * @return the property value
     */
    public double getDoubleProperty(String technology, String player, String property) {
        return Double.parseDouble(getProperty(technology, player, property));
    }
    /**
     * Check if a property exists.
     * @param technology the technology id
     * @param player the optional player id
     * @param property the property name
     * @return true if thee property exists
     */
    public boolean hasProperty(String technology, String player, String property) {
        return getProperty(technology, player, property) != null;
    }
    /**
     * Add a property value.
     * @param technology the technology
     * @param player the optional player id
     * @param property the property name
     * @param value the property value
     */
    public void addProperty(String technology, String player, String property, String value) {
        Pair<String, String> key = Pair.of(technology, player);
        Map<String, String> kv = properties.get(key);
        if (kv == null) {
            kv = new HashMap<>();
            properties.put(key, kv);
        }
        kv.put(property, value);
    }
    /**
     * Returns the probability that the given Anti-ECM leveled rocket fighting against the
     * given ECM leveled enemy will hit the target.
     * @param diff the difficulty
     * @param antiEcmLevel the anti-ecm level
     * @param ecmLevel the ecm level
     * @return the hit probability
     */
    public double getAntiECMProbability(Difficulty diff, int antiEcmLevel, int ecmLevel) {
        Map<Pair<Integer, Integer>, Double> matrix = ecmMatrix.get(diff);
        if (matrix != null) {
            Double d = matrix.get(Pair.of(antiEcmLevel, ecmLevel));
            if (d != null) {
                return d;
            }
        }
        // default
        return antiEcmLevel > ecmLevel ? 1d : antiEcmLevel < ecmLevel ? 0 : 0.5;
    }
    /**
     * Returns the probability that the given Anti-ECM leveled rocket fighting against the
     * given ECM leveled enemy will hit the target, given by the average levels.
     * @param diff the difficulty
     * @param antiEcmLevel the anti-ecm level
     * @param ecmLevel the ecm level
     * @return the hit probability
     */
    public double getAntiECMProbabilityAvg(Difficulty diff, double antiEcmLevel, double ecmLevel) {
        double lowLow = getAntiECMProbability(diff, (int)antiEcmLevel, (int)ecmLevel);
        double highHigh = getAntiECMProbability(diff, (int)Math.ceil(antiEcmLevel), (int)Math.ceil(ecmLevel));

        double antiFrac = Math.ceil(antiEcmLevel) - (int)antiEcmLevel;
        double frac = Math.ceil(ecmLevel) - (int)ecmLevel;

        return lowLow + (highHigh - lowLow) * (antiFrac + frac) / 2; // FIXME for now

    }
}
