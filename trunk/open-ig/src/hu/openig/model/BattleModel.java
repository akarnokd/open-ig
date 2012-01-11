/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Pair;
import hu.openig.utils.U;

import java.util.List;
import java.util.Map;

/**
 * The model definition for battles.
 * @author akarnokd, Jul 31, 2011
 */
public class BattleModel {
	/** A map from building id to (map from race to list of turret definition). */
	public final Map<String, Map<String, List<BattleGroundTurret>>> turrets = U.newHashMap();
	/** The space entity definitions. */
	public final Map<String, BattleSpaceEntity> spaceEntities = U.newHashMap();
	/** The ground entity definitions. */
	public final Map<String, BattleGroundVehicle> groundEntities = U.newHashMap();
	/** The map from projectile ID to [rotation][fire-phase] images. */
	public final Map<String, BattleProjectile> projectiles = U.newHashMap();
	/** The ground projectors definitions. */
	public final Map<String, BattleGroundProjector> groundProjectors = U.newHashMap();
	/** The ground shield definitions. */
	public final Map<String, BattleGroundShield> groundShields = U.newHashMap();
	/** The space battle layouts. */
	public final List<BattleSpaceLayout> layouts = U.newArrayList();
	/** The ground hit points of buildings per player. */
	public final Map<Pair<String, String>, Integer> groundHitpoints = U.newHashMap();
	/** The space hitpoints of buildings. */
	public final Map<Pair<String, String>, Integer> spaceHitpoints = U.newHashMap();
	/** Additional technology properties. */
	public final Map<Pair<String, String>, Map<String, String>> properties = U.newHashMap();
	/**
	 * Add a turret definition to the {@code turrets} mapping.
	 * @param buildingId the building identifier.
	 * @param race the race name
	 * @param turret the turret definition object
	 */
	public void addTurret(String buildingId, String race, BattleGroundTurret turret) {
		Map<String, List<BattleGroundTurret>> bt = turrets.get(buildingId);
		if (bt == null) {
			bt = U.newHashMap();
			turrets.put(buildingId, bt);
		}
		List<BattleGroundTurret> ts = bt.get(race);
		if (ts == null) {
			ts = U.newArrayList();
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
			kv = U.newHashMap();
			properties.put(key, kv);
		}
		kv.put(property, value);
	}
}
