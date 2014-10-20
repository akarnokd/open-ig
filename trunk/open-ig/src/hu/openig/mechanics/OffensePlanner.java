/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Difficulty;
import hu.openig.core.Pred1;
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIInventoryItem;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.BattleGroundVehicle;
import hu.openig.model.BattleProjectile;
import hu.openig.model.BattleProjectile.Mode;
import hu.openig.model.EquipmentSlot;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.GroundwarUnitType;
import hu.openig.model.InventorySlot;
import hu.openig.model.Planet;
import hu.openig.model.Production;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.VehiclePlan;
import hu.openig.utils.U;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Plans the creation of various ships, equipment and vehicles.
 * @author akarnokd, 2012.01.05.
 */
public class OffensePlanner extends Planner {
	/** Comparator for firepower, ascending order. */
	final Comparator<AIFleet> firepowerAsc = new Comparator<AIFleet>() {
		@Override
		public int compare(AIFleet o1, AIFleet o2) {
			return Double.compare(o1.statistics.firepower, o2.statistics.firepower);
		}
	};
	/** Comparator for firepower, descending order. */
    final Comparator<AIFleet> firepowerDesc = new Comparator<AIFleet>() {
        @Override
        public int compare(AIFleet o1, AIFleet o2) {
            return Double.compare(o2.statistics.firepower, o1.statistics.firepower);
        }
    };
	/**
	 * Compare effective firepower of two cruiser/destroyer technologies.
	 */
	final Comparator<ResearchType> effectiveFirepower = new Comparator<ResearchType>() {
		@Override
		public int compare(ResearchType o1, ResearchType o2) {
			int v1 = firepower(o1);
			int v2 = firepower(o2);
			return v2 - v1;
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
//		if (checkSellOldTech()) {
//			return;
//		}
		if (world.level < 2) {
			return;
		}
		
		if (world.global.militarySpaceportCount == 0) {
			if (checkMilitarySpaceport(false)) {
				return;
			}
		}
		
		if (upgradeFleets()) {
			return;
		}

		if (world.money < 100000) {
			return;
		}

		// create 1 fleet for every 10 planets
		int nPlanets = world.ownPlanets.size();
		int nFleets = nPlanets / 10 + 1;
		// have 2 fleets  between 3 and 10
		if (nPlanets > 3 && nPlanets < 10) {
		    nFleets++;
		}
		
		// construct fleets
		if (nFleets > world.ownFleets.size()) {
			createNewFleet();
		}
	}
	/** 
	 * Create a new fleet.
	 * @return action taken 
	 */
	boolean createNewFleet() {
		final List<ResearchType> fighters = U.sort2(availableResearchOf(EnumSet.of(ResearchSubCategory.SPACESHIPS_FIGHTERS)), ResearchType.EXPENSIVE_FIRST);
		final List<ResearchType> cruisers = U.sort2(availableResearchOf(EnumSet.of(ResearchSubCategory.SPACESHIPS_CRUISERS)), effectiveFirepower);
		final List<ResearchType> battleships = U.sort2(availableResearchOf(EnumSet.of(ResearchSubCategory.SPACESHIPS_BATTLESHIPS)), ResearchType.EXPENSIVE_FIRST);
		battleships.remove(world.isAvailable("ColonyShip"));
		
		if (!fighters.isEmpty()) {
			ResearchType rt = fighters.get(0);
			UpgradeResult r = checkProduction(rt, 1, 0, world.inventoryCount(rt), 1);
			if (r == UpgradeResult.ACTION) {
				return true;
			} else
			if (r == UpgradeResult.WAIT) {
				return false;
			}
		}
		if (!cruisers.isEmpty()) {
			ResearchType rt = cruisers.get(0);
			UpgradeResult r = checkProduction(rt, 1, 0, world.inventoryCount(rt), 1);
			if (r == UpgradeResult.ACTION) {
				return true;
			} else
			if (r == UpgradeResult.WAIT) {
				return false;
			}
		}
		if (!battleships.isEmpty()) {
			ResearchType rt = battleships.get(0);
			UpgradeResult r = checkProduction(rt, 1, 0, world.inventoryCount(rt), 1);
			if (r == UpgradeResult.ACTION) {
				return true;
			} else
			if (r == UpgradeResult.WAIT) {
				return false;
			}
		}

		if (checkMilitarySpaceport(false)) {
			return true;
		}
		
		final Planet spaceport = findBestMilitarySpaceport().planet;
		
		if (!fighters.isEmpty()) {
			world.addInventoryCount(fighters.get(0), -1);
		}		
		if (!cruisers.isEmpty()) {
			world.addInventoryCount(cruisers.get(0), -1);
		}		
		if (!battleships.isEmpty()) {
			world.addInventoryCount(battleships.get(0), -1);
		}		
		
		add(new Action0() {
			@Override
			public void invoke() {
				Fleet f = controls.actionCreateFleet(format("fleet", p.shortName), spaceport);
				if (!fighters.isEmpty()) {
					ResearchType rt = fighters.get(0);
					if (f.owner.inventoryCount(rt) > 0) {
						f.deployItem(rt, p, 1);
					}
				}
				if (!cruisers.isEmpty()) {
					ResearchType rt = cruisers.get(0);
					if (f.owner.inventoryCount(rt) > 0) {
						f.deployItem(rt, p, 1);
					}
				}
				if (!battleships.isEmpty()) {
					ResearchType rt = battleships.get(0);
					if (f.owner.inventoryCount(rt) > 0) {
						f.deployItem(rt, p, 1);
					}
				}
				if (f.inventory.isEmpty()) {
					w.removeFleet(f);
					log("DeployFleet, Fleet = %s, Planet = %s, Failed = Not enough inventory", f.name(), spaceport.id);
				}
			}
		});
		
		return true;
	}
	/**
	 * Compute the effective firepower of the ship by considering the best available weapon technology.
	 * @param ship the ship type
	 * @return the reachable firepower
	 */
	int firepower(ResearchType ship) {
		int result = 0;
		for (EquipmentSlot es : ship.slots.values()) {
			ResearchType w = null;
			if (es.fixed) {
				w = es.items.get(0);
			} else {
				for (ResearchType rt0 : es.items) {
					if (world.isAvailable(rt0)) {
						w = rt0;
					}
				}
			}
			if (w != null) {
				BattleProjectile proj = this.w.battle.projectiles.get(w.id);
				if (proj != null) {
					result += proj.damage(p) * es.max;
				}
			}
		}
		return result;
	}
	/**
	 * Compute the equipment demands for the best available technologies to fill-in the ship.
	 * @param ship the ship technology
	 * @param demands the map from equipment to demand
	 * @param c the multiplier
	 */
	void equipmentDemands(ResearchType ship, Map<ResearchType, Integer> demands, int c) {
		for (EquipmentSlot es : ship.slots.values()) {
			if (!es.fixed) {
				ResearchType w = null;
				for (ResearchType rt0 : es.items) {
					if (world.isAvailable(rt0)) {
						w = rt0;
					}
				}
				if (w != null) {
					Integer cnt = demands.get(w);
					demands.put(w, cnt != null ? cnt + es.max * c : es.max * c);
				}
			}
		}		
	}
	/** The upgrade result. */
	enum UpgradeResult {
		/** Wait, return with false. */
		WAIT,
		/** Action taken, return with true. */
		ACTION,
		/** Bring in fleet for upgrades. */
		DEPLOY,
		/** Continue with further checks. */
		CONTINUE
	}
	/**
	 * Organize the upgrade of fleets.
	 * @return action taken
	 */
	boolean upgradeFleets() {
		if (world.ownFleets.size() == 0) {
			return false;
		}

		final List<ResearchType> fighters = U.sort2(availableResearchOf(EnumSet.of(ResearchSubCategory.SPACESHIPS_FIGHTERS)), ResearchType.EXPENSIVE_FIRST);
		final List<ResearchType> cruisers = U.sort2(availableResearchOf(EnumSet.of(ResearchSubCategory.SPACESHIPS_CRUISERS)), effectiveFirepower);
		final List<ResearchType> battleships = U.sort2(availableResearchOf(EnumSet.of(ResearchSubCategory.SPACESHIPS_BATTLESHIPS)), ResearchType.EXPENSIVE_FIRST);
		battleships.remove(world.isAvailable("ColonyShip"));

		if (checkDeploy(cruisers, battleships)) {
			return true;
		}
		if (world.money < 10000) {
			return false;
		}
		if (world.money < 35000) {
			cruisers.clear();
		}
		if (world.money < 70000) {
			battleships.clear();
		}
        return checkCounts(fighters, cruisers, battleships);

    }
	/**
	 * Check if a fleet is in upgrade position over a planet.
	 * @param cruisers the list of cruiser/destroyer technology ordered by expense
	 * @param battleships the list of battleships ordered by expense
	 * @return true if action taken
	 */
	boolean checkDeploy(final List<ResearchType> cruisers,
			final List<ResearchType> battleships) {

		List<AIFleet> upgradeTasks = findFleetsWithTask(FleetTask.UPGRADE, new Pred1<AIFleet>() {
			@Override
			public Boolean invoke(AIFleet value) {
				return !value.isMoving() && value.statistics.planet != null;
			}
		});
		if (upgradeTasks.isEmpty()) {
            return !findFleetsWithTask(FleetTask.UPGRADE, null).isEmpty();
        }
		
		final Fleet fleet = Collections.min(upgradeTasks, firepowerAsc).fleet;
		
		final int cl = world.cruiserLimit;
		final int bl = world.battleshipLimit;
		
		add(new Action0() {
			@Override
			public void invoke() {
				if (fleet.task == FleetTask.SCRIPT) {
					return;
				}
				fleet.upgradeAll();
				if (!cruisers.isEmpty()) {
					fleet.replaceWithShip(cruisers.get(0), cl);
				}
				if (!battleships.isEmpty()) {
					fleet.replaceWithShip(battleships.get(0), bl);
				}
				fleet.task = FleetTask.IDLE;
				log("UpgradeFleet, Fleet = %s (%d)", fleet.name(), fleet.id);
			}
		});
		
		return true;
	}
	/**
	 * Check if the ship or equipment counts and levels are okay.
	 * @param fighters the list of fighter technology ordered by expense
	 * @param cruisers the list of cruiser/destroyer technology ordered by expense
	 * @param battleships the list of battleships ordered by expense
	 * @return true if action taken
	 */
	boolean checkCounts(final List<ResearchType> fighters,
			final List<ResearchType> cruisers,
			final List<ResearchType> battleships) {
		List<AIFleet> upgradeCandidates = findFleetsFor(FleetTask.UPGRADE, null);
		if (upgradeCandidates.isEmpty()) {
			return false;
		}

        Collections.sort(upgradeCandidates, firepowerDesc);

        Map<ResearchType, Integer> demands = new HashMap<>();
		
        AIFleet fleetSelected = null; 
        
        for (AIFleet fleet :  upgradeCandidates) {
            demands.clear();
            
	        for (ResearchType rt : fighters) {
	            demands.put(rt, world.fighterLimit);
	        }
	        if (!cruisers.isEmpty()) {
	            ResearchType rt = cruisers.get(0);
	            demands.put(rt, world.cruiserLimit);
	        }
	        if (!battleships.isEmpty()) {
	            ResearchType rt = battleships.get(0);
	            demands.put(rt, world.battleshipLimit);
	        }
	        
	        setFleetEquipmentDemands(fleet, demands);
	        
	        // plan for vehicles
	        VehiclePlan plan = new VehiclePlan();
	        plan.calculate(world.availableResearch, w.battle, 
	                fleet.statistics.vehicleMax, 
	                p == world.mainPlayer ? Difficulty.HARD : world.difficulty);
	        
	        demands.putAll(plan.demand);
	        
	        if (!demands.isEmpty()) {
	            fleetSelected = fleet;
//	            log("Fleet %s (%d) selected for upgrades (%d kinds).", fleetSelected, (int)fleetSelected.statistics.firepower, demands.size());
	            break;
	        }
		}
        
        if (fleetSelected == null) {
            return false;
        }
		
		// create equipment and upgrade the fleet
		Set<ResearchMainCategory> running = new HashSet<>();
		boolean bringin = false;
		
		List<Map.Entry<ResearchType, Integer>> demandList = U.sort(demands.entrySet(), new Comparator<Map.Entry<ResearchType, Integer>>() {
			@Override
			public int compare(Entry<ResearchType, Integer> o1,
					Entry<ResearchType, Integer> o2) {
				double v1 = o1.getKey().productionCost * 1.0 * o1.getValue();
				double v2 = o2.getKey().productionCost * 1.0 * o2.getValue();
				return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
			}
		});
		Set<ResearchType> upgradeSet = new HashSet<>();
		for (Map.Entry<ResearchType, Integer> e : demandList) {
			ResearchType rt = e.getKey();
			if (!running.contains(rt.category.main)) {
				int limit = 30;
				if (rt.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
					limit = 1;
				} else
				if (rt.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
					limit = 5;
				} else
				if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
					limit = 10;
				} else
				if (rt.category == ResearchSubCategory.WEAPONS_TANKS) {
					limit = 5;
				} else
				if (rt.category == ResearchSubCategory.WEAPONS_VEHICLES) {
					limit = 5;
				}
				
				int inventory = world.inventoryCount(rt);
				int available = fleetSelected.inventoryCount(rt);
				int demand = e.getValue();
				
				UpgradeResult r = checkProduction(rt, demand, available, inventory, limit);
				if (r == UpgradeResult.ACTION) {
					return true;
				} else
				if (r == UpgradeResult.WAIT) {
					running.add(rt.category.main);
				} else
				if (r == UpgradeResult.DEPLOY) {
					bringin = true;
					upgradeSet.add(rt);
				}
			}
		}
		if (bringin) {
			if (bringinFleet(fleetSelected)) {
				log("Upgrade set: %s", upgradeSet);
			}
			return true;
		}

		if (running.size() > 0) {
			return false;
		}
		
		return false;
	}

	/**
	 * Set the fleet equipment demands, e.g., how much more equipment is needed per type for this fleet.
	 * @param fleet the fleet
	 * @param demands the map from technology to demand count
	 */
	void setFleetEquipmentDemands(final AIFleet fleet,
			Map<ResearchType, Integer> demands) {
		// scan for upgradable slots
		for (AIInventoryItem ii : fleet.inventory) {
			for (InventorySlot is : ii.slots) {
				if (!is.slot.fixed) {
					// find best
					ResearchType best = null;
					for (ResearchType rt : is.slot.items) {
						if (world.isAvailable(rt)) {
							if (best == null || best.productionCost < rt.productionCost) {
								best = rt;
							}
						}
					}
					if (best != null) {
						int cnt = nvl(demands.get(best));
						// if we have better, add full max demand
						demands.put(best, cnt + is.slot.max * ii.count);
					}
				}
			}
		}
	}
	/**
	 * Bring in the fleet to the closest spaceport for upgrades.
	 * @param fleet the target fleet
	 * @return true if bringin succeeded
	 */
	boolean bringinFleet(final AIFleet fleet) {
		if (!checkMilitarySpaceport(false)) {
			final AIPlanet spaceport = findClosestMilitarySpaceport(fleet.x, fleet.y);
			add(new Action0() {
				@Override
				public void invoke() {
					if (fleet.fleet.task != FleetTask.SCRIPT) {
						controls.actionMoveFleet(fleet.fleet, spaceport.planet);
						fleet.fleet.task = FleetTask.UPGRADE;
					}
				}
			});
			return true;
		}
		return false;
	}
	/**
	 * Check the inventory and production status of the given technology.
	 * @param rt the target technology
	 * @param demand the total demand of this technology
	 * @param available the local availability
	 * @param inventory the global availability
	 * @param limit the production limit
	 * @return the result
	 */
	UpgradeResult checkProduction(ResearchType rt, int demand, int available, int inventory, int limit) {
		if (demand > available) {
			int required = demand - available;
			if (required > inventory) {
				if (world.productionCount(rt) > 0) {
					return UpgradeResult.WAIT;
				}
				int produce = required - inventory;
				produce = Math.min(produce, limit);
				
				placeProductionOrder(rt, limitProduction(rt, produce));
				return UpgradeResult.ACTION;
			}
			return UpgradeResult.DEPLOY; 
		}
		return UpgradeResult.CONTINUE;
	}
	/**
	 * Returns 0 if {@code i} is null, or the value itself.
	 * @param value the value
	 * @return the int value
	 */
	int nvl(Integer value) {
		return value != null ? value : 0;
	}
	/**
	 * Sell old technologies from inventory.
	 * @return true if action taken
	 */
	boolean checkSellOldTech() {
		Set<ResearchType> inuse = new HashSet<>();
		for (Production prod : world.productions.values()) {
			inuse.add(prod.type);
		}
		
		Map<String, Pred1<ResearchType>> filters = new HashMap<>();
		Map<String, ResearchType> bestValue = new HashMap<>();
		filters.put("Tank", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				return value.category == ResearchSubCategory.WEAPONS_TANKS;
			}
		});
		filters.put("Laser", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				return value.category == ResearchSubCategory.WEAPONS_LASERS;
			}
		});
		filters.put("Cannon", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				return value.category == ResearchSubCategory.WEAPONS_CANNONS;
			}
		});
		filters.put("Rocket", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				if (value.category == ResearchSubCategory.WEAPONS_PROJECTILES) {
					BattleProjectile e = w.battle.projectiles.get(value.id);
					if (e != null && (e.mode == Mode.ROCKET || e.mode == Mode.MULTI_ROCKET)) {
						return true;
					}
				}
				return false;
			}
		});
		filters.put("Bomb", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				if (value.category == ResearchSubCategory.WEAPONS_PROJECTILES) {
					BattleProjectile e = w.battle.projectiles.get(value.id);
					if (e != null && (e.mode == Mode.BOMB || e.mode == Mode.VIRUS)) {
						return true;
					}
				}
				return false;
			}
		});
		filters.put("Radar", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				return value.category == ResearchSubCategory.EQUIPMENT_RADARS;
			}
		});
		filters.put("Shield", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				return value.category == ResearchSubCategory.EQUIPMENT_SHIELDS;
			}
		});
		filters.put("Hyperdrive", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				return value.category == ResearchSubCategory.EQUIPMENT_HYPERDRIVES;
			}
		});
		filters.put("ECM", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				if (value.category == ResearchSubCategory.EQUIPMENT_MODULES) {
					if (value.has(ResearchType.PARAMETER_ECM)) {
						return true;
					}
				}
				return false;
			}
		});
		filters.put("Storage", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				if (value.category == ResearchSubCategory.EQUIPMENT_MODULES) {
					if (value.has(ResearchType.PARAMETER_VEHICLES)) {
						return true;
					}
				}
				return false;
			}
		});
		filters.put("Station", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				if (value.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					return !value.id.equals("OrbitalFactory");
				}
				return false;
			}
		});
		filters.put("OrbitalFactory", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				if (value.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					return value.id.equals("OrbitalFactory");
				}
				return false;
			}
		});
		filters.put("Spysatellites", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				if (value.category == ResearchSubCategory.SPACESHIPS_SATELLITES) {
					return value.has(ResearchType.PARAMETER_DETECTOR);
				}
				return false;
			}
		});
		filters.put("RadarSatellite", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				if (value.category == ResearchSubCategory.SPACESHIPS_SATELLITES) {
					return value.has("radar");
				}
				return false;
			}
		});
		filters.put("Sled", new Pred1<ResearchType>() {
			@Override
			public Boolean invoke(ResearchType value) {
				if (value.category == ResearchSubCategory.WEAPONS_VEHICLES) {
					BattleGroundVehicle veh = w.battle.groundEntities.get(value.id);
					if (veh != null && veh.type == GroundwarUnitType.ROCKET_SLED) {
						return true;
					}
				}
				return false;
			}
		});
		
		for (ResearchType rt : world.availableResearch) {
			for (Map.Entry<String, Pred1<ResearchType>> f : filters.entrySet()) {
				if (f.getValue().invoke(rt)) {
					ResearchType best = bestValue.get(f.getKey());
					if (best == null || best.productionCost < rt.productionCost) {
						bestValue.put(f.getKey(), rt);
					}
				}
			}
			if (rt.category == ResearchSubCategory.WEAPONS_VEHICLES) {
				BattleGroundVehicle veh = w.battle.groundEntities.get(rt.id);
				if (veh == null || veh.type != GroundwarUnitType.ROCKET_SLED) {
					inuse.add(rt);
				}
			}
			
			if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
				inuse.add(rt);
			} else
			if (rt.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
				inuse.add(rt);
			} else
			if (rt.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
				inuse.add(rt);
			}
		}
		for (AIFleet f : world.ownFleets) {
			for (AIInventoryItem ii : f.inventory) {
				for (InventorySlot is : ii.slots) {
					if (is.type != null) {
						inuse.add(is.type);
					}
				}
			}
		}
		inuse.addAll(bestValue.values());
		
		for (Map.Entry<ResearchType, Integer> e : world.inventory.entrySet()) {
			final ResearchType key = e.getKey();
			final int value = e.getValue();
			if (!inuse.contains(key) && value > 0) {
				add(new Action0() {
					@Override
					public void invoke() {
						p.changeInventoryCount(key, -value);
						
						long money = key.productionCost * 1L * value / 2;
						p.addMoney(money);
						p.statistics.moneyIncome.value += money;
						p.statistics.moneySellIncome.value += money;
						p.world.statistics.moneyIncome.value += money;
						p.world.statistics.moneySellIncome.value += money;
						
						log("SellOldTechnology, Technology = %s, Amount = %s, Money = %s", key.id, value, money);
					}
				});
				return true;
			}
		}
		
		return false;
	}
}
