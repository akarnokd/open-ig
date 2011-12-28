/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Location;
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIInventoryItem;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.Planet;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * The starmap discovery and satellite planner.
 * @author akarnokd, 2011.12.27.
 */
public class DiscoveryPlanner extends Planner {
	/** The current remaining exploration map. */
	final Set<Location> explorationMap;
	/** The cell size. */
	final int explorationCellSize;
	/**
	 * Constructor. Initializes the fields.
	 * @param world the world object
	 * @param controls the controls to affect the world in actions
	 */
	public DiscoveryPlanner(AIWorld world, AIControls controls) {
		super(world, controls);
		this.explorationMap = controls.explorationMap();
		this.explorationCellSize = controls.explorationCellSize();
	}
	@Override
	public void plan() {
		if (explorationMap.size() > 0) {
			// find a fleet which is not moving and has at least a decent radar range
			// and is among the fastest available
			AIFleet bestFleet = null;
			for (AIFleet f : world.ownFleets) {
				if (!f.isMoving() && f.radar >= w.params().fleetRadarUnitSize()) {
					if (bestFleet == null || bestFleet.statistics.speed < f.statistics.speed) {
						bestFleet = f;
					}
				}
			}
			
			if (bestFleet != null) {
				final AIFleet bf = bestFleet;
				final int ec = explorationCellSize;
				final Location loc = Collections.min(explorationMap, new Comparator<Location>() {
					@Override
					public int compare(Location o1, Location o2) {
						double d1 = Math.hypot(bf.x - (o1.x + 0.5) * ec, bf.y - (o1.y + 0.5) * ec);
						double d2 = Math.hypot(bf.x - (o2.x + 0.5) * ec, bf.y - (o2.y + 0.5) * ec);
						return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
					}
				});
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionMoveFleet(bf.fleet, (loc.x + 0.5) * ec, (loc.y + 0.5) * ec);
					}
				});
				return;
			}
		}
		
		// traverse all known planet and deploy satellites
		outer:
		for (final AIPlanet planet : world.unknownPlanets) {
			AIInventoryItem currentSatellite = null;
			for (AIInventoryItem ii : planet.inventory) {
				if (ii.owner == p && ii.type.has("detector") 
						&& ii.type.category == ResearchSubCategory.SPACESHIPS_SATELLITES) {
					currentSatellite = ii;
				}
			}
			// find the most advanced satellite
			ResearchType sat = null;
			for (Map.Entry<ResearchType, Integer> e : world.inventory.entrySet()) {
				ResearchType rt = e.getKey();
				int radar = rt.getInt("detector", 0);
				if (e.getValue() > 0 
						&& rt.category == ResearchSubCategory.SPACESHIPS_SATELLITES
						&& radar > 0) {
					if (sat == null || sat.getInt("detector") < radar) {
						sat = rt;
					}
				}
			}
			if (sat != null) {
				// if we couldn't find a better satellite
				if (currentSatellite != null 
						&& currentSatellite.type.getInt("detector") >= sat.getInt("detector")) {
					continue outer;
				}
				final ResearchType sat0 = sat;
				final Planet planet0 = planet.planet;
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionDeploySatellite(planet0, sat0);
					}
				});
				return;
			} else {
				// find the best available detector
				for (ResearchType rt : p.available().keySet()) {
					if (rt.has("detector")) {
						if (sat == null || sat.getInt("detector") < rt.getInt("detector")) {
							sat = rt;
						}
					}
				}
				if (sat != null) {
					new ProductionOrder(world, sat, applyActions, controls).invoke();
					return;
				}
			}
		}
	}
}
