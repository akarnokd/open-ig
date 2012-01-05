/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.model.AIControls;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.Fleet;
import hu.openig.model.Production;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plans the creation of various ships, equipment and vehicles.
 * @author akarnokd, 2012.01.05.
 */
public class OffensePlanner extends Planner {
	/**
	 * Orders the technology as expensives first.
	 */
	final Comparator<ResearchType> expensiveFirst = new Comparator<ResearchType>() {
		@Override
		public int compare(ResearchType o1, ResearchType o2) {
			return o2.productionCost - o1.productionCost;
		}
	};
	/**
	 * Initializes the planner.
	 * @param world the current world
	 * @param controls the controls
	 */
	public OffensePlanner(AIWorld world, AIControls controls) {
		super(world, controls);
	}

	@Override
	protected void plan() {
		if (world.money < 100000) {
			return;
		}

		// construct equipment
		
		List<ResearchType> equipments = new ArrayList<ResearchType>();
		List<ResearchType> weapons = new ArrayList<ResearchType>();
		List<ResearchType> vehicles = new ArrayList<ResearchType>();
		List<ResearchType> fighters = new ArrayList<ResearchType>();
		List<ResearchType> cruisers = new ArrayList<ResearchType>();
		List<ResearchType> battleships = new ArrayList<ResearchType>();
		
		for (ResearchType rt : world.availableResearch) {
			if (rt.category.main == ResearchMainCategory.EQUIPMENT) {
				equipments.add(rt);
			} else
			if (rt.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
				if (!rt.id.equals("ColonyShip")) {
					battleships.add(rt);
				}
			} else
			if (rt.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
				cruisers.add(rt);
			} else
			if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
				fighters.add(rt);
			} else
			if (rt.category == ResearchSubCategory.WEAPONS_TANKS
			|| rt.category == ResearchSubCategory.WEAPONS_VEHICLES) {
				vehicles.add(rt);
			} else
			if (rt.category.main == ResearchMainCategory.WEAPONS) {
				weapons.add(rt);
			}
		}
		
		final int weaponBatch = 50;
		final int vehicleBatch = 10;
		final int equipmentBatch = 10;
		final int cruiserBatch = 25;
		final int fighterBatch = 30;
		final int battleshipBatch = 3;
		
		if (checkProduction(weapons, weaponBatch)) {
			return;
		}
		if (checkProduction(vehicles, vehicleBatch)) {
			return;
		}
		if (checkProduction(equipments, equipmentBatch)) {
			return;
		}
		if (checkProduction(fighters, fighterBatch)) {
			return;
		}
		if (checkProduction(cruisers, cruiserBatch)) {
			return;
		}
		if (checkOrbitalFactory()) {
			return;
		}
		if (checkProduction(battleships, battleshipBatch)) {
			return;
		}
		
		if (checkMilitarySpaceport()) {
			return;
		}

		// have a fleet for every 3 planets
		if (world.ownFleets.size() >= world.ownPlanets.size() / 3 + 1) {
			return;
		}
		// check if we met the inventory level to deploy a fleet
		final List<ResearchType> bigShips = new ArrayList<ResearchType>();
		final List<ResearchType> mediumShip = new ArrayList<ResearchType>();
		final Map<ResearchType, Integer> smallShips = new HashMap<ResearchType, Integer>();
		
		Collections.sort(battleships, expensiveFirst);
		for (ResearchType rt : battleships) {
			if (bigShips.size() < 3) {
				if (world.inventoryCount(rt) > 0) {
					bigShips.add(rt);
				}
			}
		}
		Collections.sort(cruisers, expensiveFirst);
		for (ResearchType rt : cruisers) {
			if (mediumShip.size() < 25) {
				if (world.inventoryCount(rt) > 0) {
					mediumShip.add(rt);
				}
			}
		}
		Collections.sort(fighters, expensiveFirst);
		for (ResearchType rt : cruisers) {
			int invCount = world.inventoryCount(rt);
			if (invCount > 0) {
				smallShips.put(rt, Math.min(30, invCount));
			}
		}
		
		// select a spaceport
		final AIPlanet spaceport = Collections.min(world.ownPlanets, BEST_PLANET);
		
		// check load levels
		if (bigShips.size() >= 3 && mediumShip.size() >= 25
				&& smallShips.size() >= fighters.size()) {
			add(new Action0() {
				@Override
				public void invoke() {
					Fleet f = controls.actionCreateFleet(label(p.id + ".fleet"), spaceport.planet);
					for (ResearchType rt : bigShips) {
						if (f.owner.inventoryCount(rt) > 0) {
							f.addInventory(rt, 1);
							f.owner.changeInventoryCount(rt, -1);
						}
					}
					for (ResearchType rt : mediumShip) {
						if (f.owner.inventoryCount(rt) > 0) {
							f.addInventory(rt, 1);
							f.owner.changeInventoryCount(rt, -1);
						}
					}
					for (Map.Entry<ResearchType, Integer> rt : smallShips.entrySet()) {
						int n = Math.min(f.owner.inventoryCount(rt.getKey()), rt.getValue());
						if (n >= 0) {
							f.addInventory(rt.getKey(), n);
							f.owner.changeInventoryCount(rt.getKey(), -n);
						}
					}
					// inventory failed
					if (f.inventory.size() == 0) {
						f.owner.world.removeFleet(f);
					} else {
						f.upgradeAll();
					}
				}
			});
		}
	}
	/**
	 * Check if the inventory holds at least 20% of the batch size,
	 * and if not, issue a production order.
	 * @param rts the technologies
	 * @param batch the batch size
	 * @return true if action taken
	 */
	boolean checkProduction(List<ResearchType> rts, int batch) {
		if (!isAnyProduction(rts)) {
			Collections.sort(rts, expensiveFirst);
			for (ResearchType rt : rts) {
				if (world.inventoryCount(rt) < batch / 5) {
					placeProductionOrder(rt, batch);
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Check if there is any ongoing production of the given list of technologies.
	 * @param rts the list of technologies
	 * @return true if any of it is in production
	 */
	boolean isAnyProduction(List<ResearchType> rts) {
		for (ResearchType rt : rts) {
			Production pr = world.productions.get(rt);
			if (pr != null && pr.count > 0) {
				return true;
			}
		}
		return false;
	}
}
