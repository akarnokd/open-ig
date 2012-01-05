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
import hu.openig.model.BattleGroundVehicle;
import hu.openig.model.EquipmentSlot;
import hu.openig.model.Fleet;
import hu.openig.model.GroundwarUnitType;
import hu.openig.model.Production;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.utils.JavaUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		// have a fleet for every 3 planets
		if (world.ownFleets.size() >= world.ownPlanets.size() / 3 + 1) {
			return;
		}

		// construct fleets
		
		List<ResearchType> fighters = new ArrayList<ResearchType>();
		List<ResearchType> cruisers = new ArrayList<ResearchType>();
		List<ResearchType> battleships = new ArrayList<ResearchType>();
		
		for (ResearchType rt : world.availableResearch) {
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
			}
		}
		
		final int cruiserBatch = 1;
		final int fighterBatch = 10;
		final int battleshipBatch = 1;
		
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

		// check if we met the inventory level to deploy a fleet
		final List<ResearchType> bigShips = new ArrayList<ResearchType>();
		final List<ResearchType> mediumShip = new ArrayList<ResearchType>();
		final Map<ResearchType, Integer> smallShips = new HashMap<ResearchType, Integer>();
		
		Collections.sort(battleships, expensiveFirst);
		for (ResearchType rt : battleships) {
			int count = world.inventoryCount(rt);
			while (bigShips.size() < 3 && count > 0) {
				bigShips.add(rt);
				count--;
			}
		}
		Collections.sort(cruisers, expensiveFirst);
		for (ResearchType rt : cruisers) {
			int count = world.inventoryCount(rt);
			while (mediumShip.size() < 25 && count > 0) {
				mediumShip.add(rt);
				count--;
			}
		}
		Collections.sort(fighters, expensiveFirst);
		int totalFighters = 0;
		for (ResearchType rt : fighters) {
			int invCount = world.inventoryCount(rt);
			if (invCount > 0) {
				int ij = Math.min(30, invCount);
				smallShips.put(rt, ij);
				totalFighters += ij;
			}
		}
		
		// check load levels
		if (bigShips.size() >= 3 && mediumShip.size() >= 25
				&& totalFighters >= fighters.size() * 30) {
			// count required equipment
			List<ResearchType> rts = new ArrayList<ResearchType>(mediumShip);
			rts.addAll(bigShips);
			Map<ResearchType, Integer> equipmentDemands = countEquipments(rts);
	
			List<ResearchType> equipments = new ArrayList<ResearchType>();
			List<ResearchType> weapons = new ArrayList<ResearchType>();
			for (Map.Entry<ResearchType, Integer> e : equipmentDemands.entrySet()) {
				ResearchType rt = e.getKey();
				int count = e.getValue();
				if (count > world.inventoryCount(rt)) {
					if (rt.category.main == ResearchMainCategory.EQUIPMENT) {
						equipments.add(rt);
					} else
					if (rt.category.main == ResearchMainCategory.WEAPONS) {
						weapons.add(rt);
					}
				}
			}
			
			if (checkProduction(equipments, equipmentDemands)) {
				return;
			}
	
			if (checkProduction(weapons, equipmentDemands)) {
				return;
			}
	
			// check if all demand met
			for (Map.Entry<ResearchType, Integer> e : equipmentDemands.entrySet()) {
				ResearchType rt = e.getKey();
				int count = e.getValue();
				if (count > world.inventoryCount(rt)) {
					return;
				}
			}		
			
			// count vehicle capacity
			int vehicleCount = 0;
			for (ResearchType rt : bigShips) {
				if (rt.has("vehicles")) {
					vehicleCount += rt.getInt("vehicles");
				}
				for (EquipmentSlot es : rt.slots.values()) {
					ResearchType bay = null;
					if (es.fixed) {
						if (es.items.get(0).has("vehicles")) {
							bay = es.items.get(0);
						}
					} else {
						for (ResearchType rt0 : es.items) {
							if (rt0.has("vehicles") && world.isAvailable(rt0)) {
								bay = rt0;
							}
						}
					}
					if (bay != null) {
						vehicleCount += bay.getInt("vehicles");
					}
				}
			}
			
			ResearchType bestTank = null;
			final List<ResearchType> vehicles = new ArrayList<ResearchType>();
			for (ResearchType rt : world.availableResearch) {
				if (rt.category == ResearchSubCategory.WEAPONS_TANKS) {
					if (bestTank == null || bestTank.productionCost < rt.productionCost) {
						bestTank = rt;
					}
				} else
				if (rt.category == ResearchSubCategory.WEAPONS_VEHICLES) {
					vehicles.add(rt);
				}
			}
			// 2/3 best tank
			
			int tankCount = vehicleCount * 2 / 3;
	
			if (bestTank != null) {
				int ic = world.inventoryCount(bestTank);
				if (ic < tankCount) {
					if (!isAnyProduction(Collections.singletonList(bestTank))) {
						placeProductionOrder(bestTank, tankCount - ic);
						return;
					}
				}
			} else {
				tankCount = 0;
			}
			
			// 1/3 all kinds of vehicles
			final int otherCount = vehicleCount - tankCount;
			final Map<ResearchType, Integer> vehicleConfig = JavaUtils.newHashMap();
			final Set<ResearchType> onePerFleet = JavaUtils.newHashSet();
			if (otherCount > 0) {
				// limit radar car to 1
				ResearchType bestRocketSled = null;
				for (ResearchType rt : vehicles) {
					if (rt.has("one-per-fleet") && "true".equals(rt.get("one-per-fleet"))) {
						vehicleConfig.put(rt, 1);
						onePerFleet.add(rt);
					} else {
						BattleGroundVehicle v = w.battle.groundEntities.get(rt.id);
						if (v != null && v.type == GroundwarUnitType.ROCKET_SLED) {
							if (bestRocketSled == null || bestRocketSled.productionCost < rt.productionCost) {
								bestRocketSled = rt;
							}
						}
					}
				}
				// remove worst rocket sleds
				for (ResearchType rt : new ArrayList<ResearchType>(vehicles)) {
					BattleGroundVehicle v = w.battle.groundEntities.get(rt.id);
					if (v != null && v.type == GroundwarUnitType.ROCKET_SLED && rt != bestRocketSled) {
						vehicles.remove(rt);
					}
				}
				
				// distribute remaining slots evenly among non-radar cars
				int vc = otherCount - 1;
				int j = 0;
				while (vc > 0) {
					ResearchType rt = vehicles.get(j);
					if (!onePerFleet.contains(rt)) {
						Integer d = vehicleConfig.get(rt);
						vehicleConfig.put(rt, d != null ? d + 1 : 1);
						vc--;
					}
					j++;
					if (j == vehicles.size()) {
						j = 0;
					}
				}
				
				// issue production orders
				if (checkProduction(vehicles, vehicleConfig)) {
					return;
				}
			}
			// enough tanks built?
			if (world.inventoryCount(bestTank) < tankCount) {
				return;
			}
			// enough units built?
			for (ResearchType rt : vehicles) {
				Integer demand = vehicleConfig.get(rt);
				if (demand != null && demand > world.inventoryCount(rt)) {
					return;
				}
			}
			
			final ResearchType fbestTank = bestTank;
			final int ftankCount = tankCount;
			
			// select a spaceport
			final AIPlanet spaceport = Collections.min(world.ownPlanets, BEST_PLANET);
			
			add(new Action0() {
				@Override
				public void invoke() {
					Fleet f = controls.actionCreateFleet(label(p.id + ".fleet"), spaceport.planet);
					boolean success = true;
					for (ResearchType rt : bigShips) {
						if (f.owner.inventoryCount(rt) > 0) {
							f.addInventory(rt, 1);
							f.owner.changeInventoryCount(rt, -1);
						} else {
							success = false;
							break;
						}
					}
					for (ResearchType rt : mediumShip) {
						if (f.owner.inventoryCount(rt) > 0) {
							f.addInventory(rt, 1);
							f.owner.changeInventoryCount(rt, -1);
						} else {
							success = false;
							break;
						}
					}
					for (Map.Entry<ResearchType, Integer> cfg : smallShips.entrySet()) {
						int cnt = cfg.getValue();
						ResearchType rt = cfg.getKey();
						if (cnt <= f.owner.inventoryCount(rt)) {
							f.addInventory(rt, cnt);
							f.owner.changeInventoryCount(rt, -cnt);
						} else {
							success = false;
							break;
						}
					}
					if (fbestTank != null) {
						if (ftankCount <= f.owner.inventoryCount(fbestTank)) {
							f.addInventory(fbestTank, ftankCount);
							f.owner.changeInventoryCount(fbestTank, -ftankCount);
						} else {
							success = false;
						}
					}
					for (Map.Entry<ResearchType, Integer> cfg : vehicleConfig.entrySet()) {
						int cnt = cfg.getValue();
						ResearchType rt = cfg.getKey();
						if (cnt <= f.owner.inventoryCount(rt)) {
							f.addInventory(rt, cnt);
							f.owner.changeInventoryCount(rt, -cnt);
						} else {
							success = false;
							break;
						}
					}
					
					// inventory failed
					if (f.inventory.size() == 0 || !success) {
						log("DeployFleet, Failed = Inventory insufficient");
						f.owner.world.removeFleet(f);
					} else {
						f.upgradeAll();
					}
				}
			});
		}
	}
	/**
	 * Count the required equipments.
	 * @param ships the ships
	 * @return the map of technology to count
	 */
	Map<ResearchType, Integer> countEquipments(List<ResearchType> ships) {
		Map<ResearchType, Integer> result = JavaUtils.newHashMap();
		
		for (ResearchType rt : ships) {
			for (EquipmentSlot es : rt.slots.values()) {
				if (!es.fixed) {
					ResearchType req = null;
					// find best available tech
					for (ResearchType rt0 : es.items) {
						if (world.isAvailable(rt0)) {
							req = rt0;
						}
					}
					if (req != null) {
						Integer v = result.get(req);
						result.put(req, v != null ? v + es.max : es.max);
					}
				}
			}
		}
		
		return result;
	}
	/**
	 * Check if the inventory holds the demand amount of any item
	 * and if not, issue a production order.
	 * @param rts the technologies
	 * @param demand the technology demand
	 * @return true if action taken
	 */
	boolean checkProduction(List<ResearchType> rts, Map<ResearchType, Integer> demand) {
		if (!isAnyProduction(rts)) {
			Collections.sort(rts, expensiveFirst);
			for (ResearchType rt : rts) {
				int count = demand.get(rt);
				int ic = world.inventoryCount(rt); 
				if (ic < count) {
					placeProductionOrder(rt, count - ic);
					return true;
				}
			}
		}
		return false;
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
				if (world.inventoryCount(rt) <= batch / 5) {
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
