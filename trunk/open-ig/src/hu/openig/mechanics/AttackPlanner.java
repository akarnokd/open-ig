/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.model.AIAttackMode;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIInventoryItem;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.ExplorationMap;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetTask;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.ResearchSubCategory;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * The starmap exploration and satellite planner.
 * @author akarnokd, 2011.12.27.
 */
public class AttackPlanner extends Planner {
	/** The exploration map. */
	final ExplorationMap exploration;
	/** Sets the new attack date. */
	final Action1<Date> setNewAttack;
	/**
	 * Constructor. Initializes the fields.
	 * @param world the world object
	 * @param controls the controls to affect the world in actions
	 * @param exploration the exploration map
	 * @param setNewAttack set the new attack date
	 */
	public AttackPlanner(AIWorld world, AIControls controls, 
			ExplorationMap exploration, Action1<Date> setNewAttack) {
		super(world, controls);
		this.exploration = exploration;
		this.setNewAttack = setNewAttack;
	}
	@Override
	public void plan() {
		// don't bother when exploring
		for (AIFleet f : world.ownFleets) {
			if (f.task.ordinal() <= FleetTask.EXPLORE.ordinal()) {
				return;
			}
		}
		if (world.nextAttack != null && world.nextAttack.compareTo(world.now) < 0) {
			world.nextAttack = null;
			
			List<AIFleet> fleets = findFleetsFor(FleetTask.ATTACK, null);
			if (!fleets.isEmpty()) {
				final AIFleet ownFleet = Collections.max(fleets, new Comparator<AIFleet>() {
					@Override
					public int compare(AIFleet o1, AIFleet o2) {
						return o1.statistics.firepower - o2.statistics.firepower;
					}
				});
				AIFleet targetFleet = null;
				AIPlanet targetPlanet = null;
				if (world.mayConquer) {
					if (w.random().nextBoolean()) {
						targetFleet = selectTargetFleet();
						if (targetFleet == null && w.random().nextBoolean()) {
							targetPlanet = selectTargetPlanet();
						}
					} else {
						targetPlanet = selectTargetPlanet();
					}
				} else {
					targetFleet = selectTargetFleet();
				}
				if (targetFleet != null) {
					final AIFleet ftargetFleet = targetFleet;
					add(new Action0() {
						@Override
						public void invoke() {
							controls.actionAttackFleet(ownFleet.fleet, ftargetFleet.fleet, false);
						}
					});
				} else 
				if (targetPlanet != null) {
					final AIPlanet ftargetPlanet = targetPlanet;
					add(new Action0() {
						@Override
						public void invoke() {
							controls.actionAttackPlanet(ownFleet.fleet, ftargetPlanet.planet, AIAttackMode.CAPTURE);
						}
					});
				}
			}
		}
		if (world.nextAttack == null) {
			if (world.mayConquer) {
				int base = 4;
				long next = 1L * (w.random().nextInt(3) + base) * (8 + 2 * w.random().nextInt(6)) * 60 * 60 * 1000;
				world.nextAttack = new Date(world.now.getTime() + next);
				setNewAttack.invoke(world.nextAttack);
			}
		}
	}
	/**
	 * Select the weakest fleet based on our knowledge.
	 * @return the weakest fleet or null if no fleet available
	 */
	AIFleet selectTargetFleet() {
		List<AIFleet> candidates = new ArrayList<AIFleet>();
		for (AIFleet f : world.enemyFleets) {
			if (!(f.fleet.owner.ai instanceof AITrader) && !(f.fleet.owner.ai instanceof AIPirate)) {
				candidates.add(f);
			}
		}
		if (!candidates.isEmpty()) {
			final Point2D.Double center = world.center();
			return Collections.min(candidates, new Comparator<AIFleet>() {
				@Override
				public int compare(AIFleet o1, AIFleet o2) {
					double v1 = fleetValue(o1);
					double v2 = fleetValue(o2);
					
					double d1 = Math.hypot(o1.x - center.x, o1.y - center.y);
					double d2 = Math.hypot(o2.x - center.x, o2.y - center.y);
					if (v1 < v2) {
						return -1;
					} else
					if (v1 > v2) {
						return 1;
					} else
					if (d1 < d2) {
						return -1;
					} else
					if (d1 > d2) {
						return 1;
					}
					return 0;
				}
			});
		}
		return null;
	}
	/**
	 * Select an enemy planet.
	 * @return the planet or null if none found
	 */
	AIPlanet selectTargetPlanet() {
		List<AIPlanet> candidates = new ArrayList<AIPlanet>();
		for (AIPlanet p : world.enemyPlanets) {
			if (p.owner != null) {
				if (world.explorationInnerLimit != null && world.explorationInnerLimit.contains(p.planet.x, p.planet.y)) {
					continue;
				}
				if (world.explorationOuterLimit != null && !world.explorationOuterLimit.contains(p.planet.x, p.planet.y)) {
					continue;
				}
				candidates.add(p);
			}
		}
		if (!candidates.isEmpty()) {
			final Point2D.Double center = world.center();
			return Collections.min(candidates, new Comparator<AIPlanet>() {
				@Override
				public int compare(AIPlanet o1, AIPlanet o2) {
					double v1 = planetValue(o1);
					double v2 = planetValue(o2);
					
					double d1 = Math.hypot(o1.planet.x - center.x, o1.planet.y - center.y);
					double d2 = Math.hypot(o2.planet.x - center.x, o2.planet.y - center.y);
					if (v1 < v2) {
						return -1;
					} else
					if (v1 > v2) {
						return 1;
					} else
					if (d1 < d2) {
						return -1;
					} else
					if (d1 > d2) {
						return 1;
					}
					return 0;
				}
			});
		}
		return null;
	}
	/**
	 * Estimate fleet value.
	 * @param f the target fleet
	 * @return the value
	 */
	double fleetValue(AIFleet f) {
		if (f.knowledge == FleetKnowledge.VISIBLE) {
			return 0;
		}
		double v = 0;
		if (f.knowledge == FleetKnowledge.COMPOSITION) {
			for (AIInventoryItem ii : f.inventory) {
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS
						|| ii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS
						|| ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
					v += ii.type.productionCost * ((ii.count + 9 / 10) * 10);
				}
			}
		} else {
			for (AIInventoryItem ii : f.inventory) {
				v += ii.type.productionCost * ii.count;
			}
		}
		return v;
	}
	/**
	 * Computes the planet value.
	 * @param p the planet
	 * @return the value
	 */
	double planetValue(AIPlanet p) {
		if (p.knowledge == PlanetKnowledge.OWNER) {
			return 0;
		}
		double v = 0;
		if (p.knowledge.ordinal() >= PlanetKnowledge.STATIONS.ordinal()) {
			for (AIInventoryItem ii : p.inventory) {
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					v += ii.type.productionCost * ii.count;
				}
			}
		}
		if (p.knowledge.ordinal() >= PlanetKnowledge.BUILDING.ordinal()) {
			for (AIBuilding b : p.buildings) {
				if (b.type.kind.equals("Defensive") 
						|| b.type.kind.equals("Gun")
						|| b.type.kind.equals("Shield")) {
					v += b.type.cost;
				}
			}
			v += p.statistics.vehicleMax * 5000;
		}
		return v;
	}
}
