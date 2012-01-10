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
}
