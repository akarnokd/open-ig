/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Pred1;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.ModelUtils;
import hu.openig.model.Planet;
import hu.openig.model.PlanetProblems;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Build factories and economic buildings.
 * Builds social buildings to keep the morale.
 * Adjusts taxes according to morale.
 * May demolish damanged buildings.
 * May block other planners with empty action to gain money.
 * @author akarnokd, 2011.12.28.
 */
public class EconomyPlanner extends Planner {
	/**
	 * Constructor.
	 * @param world the world
	 * @param controls the controls
	 */
	public EconomyPlanner(AIWorld world, AIControls controls) {
		super(world, controls);
	}

	@Override
	protected void plan() {
//		List<AIPlanet> planets = new ArrayList<>(world.ownPlanets);
//		Collections.sort(planets, WORST_PLANET);
//		Collections.reverse(planets);
//		
//		for (AIPlanet p : planets) {
//			if (managePlanet(p)) {
//				if (world.mainPlayer == this.p && world.money < world.autoBuildLimit) {
//					return;
//				}
//			}
//		}
	    planHorizontally();
	}
	/** 
	 * Returns the actions to perform.
	 * @return the actions to perform
	 */
	public List<Action0> actions() {
		return applyActions;
	}
	/**
	 * Check if the building has a specific resource and add 1 to the map of counters.
	 * @param b the building
	 * @param resource the resource
	 * @param map the map
	 */
	void checkAndAdd(AIBuilding b, String resource, Map<String, Integer> map) {
		if (b.hasResource(resource)) {
			Integer c = map.get(resource);
			map.put(resource, c != null ? c + 1 : 1);
		}
	}
	/**
	 * Manage the given planet.
	 * @param planet the target planet
	 * @return true if action taken
	 */
	public boolean managePlanet(final AIPlanet planet) {
		if (planet.statistics.constructing) {
			return false;
		}
		
		final Set<BuildingType> mayBuild = new HashSet<>();
		
		final boolean allowUpgrades2 = world.money >= world.ownPlanets.size() * 40000;
		
		List<Pred1<AIPlanet>> functions = new ArrayList<>();
		
		boolean economyAllBuilt = true;
		boolean factoryAllBuilt = true;
		
		if (world.availableResourceBuildings.containsKey("credit")) {
			for (BuildingType bt : world.availableResourceBuildings.get("credit")) {
				economyAllBuilt &= planet.buildingCounts.containsKey(bt);
			}
		}
		if (world.availableResourceBuildings.containsKey("multiply")) {
			for (BuildingType bt : world.availableResourceBuildings.get("multiply")) {
				economyAllBuilt &= planet.buildingCounts.containsKey(bt);
			}
		}
		if (world.availableResourceBuildings.containsKey("spaceship")) {
			for (BuildingType bt : world.availableResourceBuildings.get("spaceship")) {
				factoryAllBuilt &= planet.buildingCounts.containsKey(bt);
			}
		}
		if (world.availableResourceBuildings.containsKey("weapon")) {
			for (BuildingType bt : world.availableResourceBuildings.get("weapon")) {
				factoryAllBuilt &= planet.buildingCounts.containsKey(bt);
			}
		}
		if (world.availableResourceBuildings.containsKey("equipment")) {
			for (BuildingType bt : world.availableResourceBuildings.get("equipment")) {
				factoryAllBuilt &= planet.buildingCounts.containsKey(bt);
			}
		}
		if (!economyAllBuilt) {
			if (world.availableResourceBuildings.containsKey("credit")) {
				for (BuildingType bt : world.availableResourceBuildings.get("credit")) {
					if (!planet.buildingCounts.containsKey(bt)) {
						mayBuild.add(bt);
					}
				}
			}
			if (world.availableResourceBuildings.containsKey("multiply")) {
				for (BuildingType bt : world.availableResourceBuildings.get("multiply")) {
					if (!planet.buildingCounts.containsKey(bt)) {
						mayBuild.add(bt);
					}
				}
			}
		} else
		if (factoryAllBuilt) {
			Set<BuildingType> set = world.availableResourceBuildings.get("credit");
			if (set != null) {
				mayBuild.addAll(set);
			}
			set = world.availableResourceBuildings.get("multiply");
			if (set != null) {
				mayBuild.addAll(set);
			}
		}
		final boolean allowUpgrades = world.money >= world.ownPlanets.size() * 20000;
		
		Pred1<AIPlanet> checkEconomyFn = new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet planet) {
				return checkEconomy(planet, allowUpgrades, mayBuild);
			}
		};
		
		if (!economyAllBuilt) {
			functions.add(checkEconomyFn);
		}
		Pred1<AIPlanet> checkFactoryFn = new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet planet) {
				return checkFactory(planet, allowUpgrades2);
			}
		};
		
		boolean buildFactoryFlag = false;
		
		if (world.money >= 30000 && checkPlanetPreparedness()) {
			buildFactoryFlag = true;
			if (!factoryAllBuilt) {
				functions.add(checkFactoryFn);
			}
			functions.add(new Pred1<AIPlanet>() {
				@Override
				public Boolean invoke(AIPlanet planet) {
					return checkSocial(planet);
				}
			});
			functions.add(new Pred1<AIPlanet>() {
				@Override
				public Boolean invoke(AIPlanet planet) {
					return checkRadar(planet);
				}
			});
		}
		if (ModelUtils.random() < 0.01) {
			if (ModelUtils.randomBool()) {
				if (factoryAllBuilt && buildFactoryFlag) {
					functions.add(checkFactoryFn);
				}
				if (economyAllBuilt) {
					functions.add(checkEconomyFn);
				}
			} else {
				if (economyAllBuilt) {
					functions.add(checkEconomyFn);
				}
				if (factoryAllBuilt && buildFactoryFlag) {
					functions.add(checkFactoryFn);
				}
			}
		}

		for (Pred1<AIPlanet> f : functions) {
			if (f.invoke(planet)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * If colony lacks radar, build one, if better available than the current
	 * demolish existing (the next turn will build one).
	 * If hubble2 is available place it, or produce it.
	 * @param planet the planet to manage
	 * @return if action taken
	 */
	boolean checkRadar(final AIPlanet planet) {
		// check if hubble2 is buildable
		BuildingType bestRadar = null;
		for (BuildingType bt : w.buildingModel.buildings.values()) {
			if (bt.hasResource("radar") 
					&& (bt.research == null || world.availableResearch.contains(bt.research))) {
				if (bestRadar == null || bestRadar.getResource("radar") < bt.getResource("radar")) {
					bestRadar = bt;
				}
			}
		}
		ResearchType hubble2 = null;
		for (ResearchType rt : world.availableResearch) {
			if (rt.has(ResearchType.PARAMETER_RADAR) && rt.category == ResearchSubCategory.SPACESHIPS_SATELLITES) {
				if (hubble2 == null || hubble2.getInt(ResearchType.PARAMETER_RADAR) < rt.getInt(ResearchType.PARAMETER_RADAR)) {
					hubble2 = rt;
				}
			}
		}
		int hubble2Count = hubble2 != null ? world.inventoryCount(hubble2) : 0; 
		
		if (planet.statistics.constructing) {
			return false;
		}
		// if hubble is in inventory
		if (hubble2 != null && planet.hasInventory(hubble2)) {
			// demolish remaining ground radars
			for (final AIBuilding b : planet.buildings) {
				if (b.hasResource("radar")) {
					planet.buildings.remove(b);
					add(new Action0() {
						@Override
						public void invoke() {
							controls.actionDemolishBuilding(planet.planet, b.building);
						}
					});
					return true;
				}
			}
			return false;
		}
		// if hubble in inventory, place one
		if (hubble2Count > 0) {
			final ResearchType fhubble2 = hubble2;
			add(new Action0() {
				@Override
				public void invoke() {
					controls.actionDeploySatellite(planet.planet, fhubble2);
				}
			});
			return true;
		}
		// if hubble available, produce some
		if (hubble2 != null) {
			placeProductionOrder(hubble2, 1);
			return true;
		}
		
		if (bestRadar != null) {
			// check if already present
			for (AIBuilding b : planet.buildings) {
				if (b.type == bestRadar) {
					return false;
				}
			}
			if (bestRadar.cost <= world.money 
				&& planet.findLocation(bestRadar) != null) {
				// demolish undertech radars
				for (final AIBuilding b : planet.buildings) {
					if (b.hasResource("radar")) {
						if (b.getResource("radar") < bestRadar.getResource("radar")) {
							planet.buildings.remove(b);
							add(new Action0() {
								@Override
								public void invoke() {
									controls.actionDemolishBuilding(planet.planet, b.building);
								}
							});
							return true;
						}
					}
				}
				// construct the best radar
				build(planet, bestRadar);
				return true;
			}
		}
		return false;
	}
	/**
	 * Check if there is shortage on police.
	 * @param planet the planet to manage
	 * @param allowUpgrades allow upgrading?
	 * @param mayBuild the set of buildings that may be built.
	 * @return if action taken
	 */
	boolean checkEconomy(AIPlanet planet, final boolean allowUpgrades, 
			final Set<BuildingType> mayBuild) {
		BuildingSelector finances = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return allowUpgrades && (value.hasResource("multiply") || value.hasResource("credit"));
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return mayBuild.contains(value) 
						&& planet.population >= planet.statistics.nativeWorkerDemand + value.getResource("worker");
			}
		};
		if (planet.statistics.energyAvailable > planet.statistics.energyDemand
				&& planet.statistics.houseAvailable > planet.population
				&& planet.statistics.foodAvailable > planet.population
				&& planet.statistics.hospitalAvailable > planet.population
				&& planet.statistics.policeAvailable > planet.population) {
			return manageBuildings(planet, finances, costOrder, false);
		}
		return false;
	}
	/**
	 * Check if there is shortage on police.
	 * @param planet the planet to manage
	 * @param allowUpgrades allow upgrades?
	 * @return if action taken
	 */
	boolean checkFactory(AIPlanet planet, final boolean allowUpgrades) {
		final boolean hasSpaceship = world.global.production.spaceship > 0;
		final boolean hasEquipment = world.global.production.equipment > 0;
		final boolean hasWeapon = world.global.production.weapons > 0;
		final boolean hasAll = hasSpaceship && hasEquipment && hasWeapon;
		BuildingSelector factory = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return allowUpgrades 
						&& (hasTechnologyFor(value.type, "spaceship") 
								|| hasTechnologyFor(value.type, "equipment") 
								|| hasTechnologyFor(value.type, "weapon"));
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				boolean r = false;
				if (hasTechnologyFor(value, "spaceship")) {
					r = !hasSpaceship || hasAll;
				} else
				if (hasTechnologyFor(value, "equipment")) {
					r = !hasEquipment || hasAll;
				}
				if (hasTechnologyFor(value, "weapon")) {
					r = !hasWeapon || hasAll;
				}
				
				return r && planet.population >= planet.statistics.nativeWorkerDemand + value.getResource("worker");
			}
		};
		if (planet.statistics.energyAvailable > planet.statistics.energyDemand
				&& planet.statistics.houseAvailable > planet.population
				&& planet.statistics.foodAvailable > planet.population
				&& planet.statistics.hospitalAvailable > planet.population
				&& planet.statistics.policeAvailable > planet.population) {
			return manageBuildings(planet, factory, costOrder, false);
		}
		return false;
	}
	/**
	 * Check if we have any technology requiring the given factory.
	 * @param value the potential building type
	 * @param factory the factory type to check
	 * @return true if building should be built
	 */
	boolean hasTechnologyFor(BuildingType value, String factory) {
		if (value.hasResource(factory)) {
			for (ResearchType rt : w.researches.values()) {
				if (rt.race.contains(p.race) && rt.level <= w.level 
						&& factory.equals(rt.factory)) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Check if there is shortage on police.
	 * @param planet the planet to manage
	 * @return if action taken
	 */
	boolean checkSocial(AIPlanet planet) {
		BuildingSelector social = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				
				return value.type.kind.equals(BuildingType.KIND_SOCIAL) && value.hasResource(BuildingType.RESOURCE_MORALE);
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				if (value.kind.equals(BuildingType.KIND_SOCIAL) 
						&& value.hasResource(BuildingType.RESOURCE_MORALE) 
						&& count(planet, value) < 1) {
					if (value.id.equals("Stadium") && planet.population < 40000) {
						return false;
					}
					return planet.population >= planet.statistics.nativeWorkerDemand + value.getResource("worker");
				}
				return false;
			}
		};
		if (checkPlanetPreparedness()) {
			return manageBuildings(planet, social, costOrder, false);
		}
		return false;
	}
    /**
     * Plans the construction of economy and factory buildings horizontally across
     * all player planets, then does the radar & social checks.
     * Methodology:
     * <ul>
     * <li>Build each type of economy building on each planet, one by one.</li>
     * <li>Wait until at least one set of factories and military spaceports are built (probably by someone else).</li>
     * <li>Upgrade each economy building on each planet, one by one.</li>
     * <li>Construct each factory type on the best planet first, then on less valued planets.</li>
     * <li>Check and build radar and social buildings, best planet first.</li>
     * </ul>
     */
    void planHorizontally() {
        List<AIPlanet> planets = new ArrayList<>(world.ownPlanets);
        Collections.sort(planets, BEST_PLANET);

        Set<BuildingType> economySet = new HashSet<>();
        
        economySet.addAll(world.availableResourceBuildings.get("credit"));
        economySet.addAll(world.availableResourceBuildings.get("multiply"));
        
        List<BuildingType> economyList = new ArrayList<>(economySet);
        Collections.sort(economyList, BuildingType.COST);
        
        boolean economyAll = true;
        
        for (BuildingType bt : economyList) {
            for (AIPlanet p : planets) {
                if (p.statistics.constructing) {
                    continue;
                }
                if (!p.buildingCounts.containsKey(bt)) {
                    if (world.money >= bt.cost) {
                        if (p.findLocation(bt) != null) {
                            build(p, bt);
                            return;
                        }
                    } else {
                        economyAll = false;
                    }
                }
            }
        }
        
        // make sure there is at least one military spaceport and one of 
        if (world.global.hasMilitarySpaceport 
                && world.global.production.equipment > 0
                && world.global.production.spaceship > 0
                && world.global.production.weapons > 0) {
            boolean upgradeAvailable = false;
            for (BuildingType bt : economyList) {
                for (final AIPlanet p : planets) {
                    if (p.statistics.constructing) {
                        continue;
                    }
                    List<AIBuilding> toUpgrade = new ArrayList<>();
                    for (final AIBuilding b : p.buildings) {
                        if (b.type == bt && b.canUpgrade()) {
                            upgradeAvailable = true;
                        }
                        if (b.type == bt && b.canUpgrade() && world.money >= bt.cost) {
                            toUpgrade.add(b);
                        }
                    }
                    
                    if (!toUpgrade.isEmpty()) {
                        AIBuilding b = Collections.min(toUpgrade, AIBuilding.COMPARE_LEVEL);
                        world.money -= b.type.cost;
                        final Planet p0 = p.planet;
                        final Building b0 = b.building;
                        add(new Action0() {
                            @Override
                            public void invoke() {
                                controls.actionUpgradeBuilding(p0, b0, b0.upgradeLevel + 1);
                            }
                        });
                        return;
                    }
                }
            }
            // if we weren't able to upgrade now due to lack of money
            if (!economyList.isEmpty() && upgradeAvailable) {
                economyAll = false;
            }
        }
        
        if (economyAll && !planets.isEmpty()) {
            Set<BuildingType> factorySet = new HashSet<>();
            
            if (world.global.production.spaceship == 0) {
                factorySet.addAll(world.availableResourceBuildings.get("spaceship"));
            } else
            if (world.global.production.weapons == 0) {
                factorySet.addAll(world.availableResourceBuildings.get("weapon"));
            } else
            if (world.global.production.equipment == 0) {
                factorySet.addAll(world.availableResourceBuildings.get("equipment"));
            } else 
            if (((world.global.problems.isEmpty() 
                    && !world.global.warnings.contains(PlanetProblems.WORKFORCE)) || world.money >= 500_000)
                    && (world.global.labCount() > 0 || world.remainingResearch.isEmpty())) {
                factorySet.addAll(world.availableResourceBuildings.get("spaceship"));
                factorySet.addAll(world.availableResourceBuildings.get("weapon"));
                factorySet.addAll(world.availableResourceBuildings.get("equipment"));
            }
    
            List<BuildingType> factoryList = new ArrayList<>(factorySet);
            Collections.sort(factoryList, BuildingType.COST);
            
            for (AIPlanet p : planets) {
                if (p.statistics.constructing) {
                    continue;
                }
                for (BuildingType bt : factoryList) {
                    if (!p.buildingCounts.containsKey(bt)) {
                        if (world.money >= bt.cost) {
                            if (p.population >= Math.abs(p.statistics.nativeWorkerDemand) + Math.abs(bt.getResource("worker"))) {
                                if (p.findLocation(bt) != null) {
                                    build(p, bt);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            
            if (((world.global.warnings.isEmpty()
                        && world.global.problems.isEmpty()) || world.money >= 500_000)
                        && (world.global.labCount() > 0 || world.remainingResearch.isEmpty())) {
                for (AIPlanet p : planets) {
                    if (p.statistics.constructing) {
                        continue;
                    }
                    List<AIBuilding> toUpgrade = new ArrayList<>();
                    for (BuildingType bt : factoryList) {
                        for (final AIBuilding b : p.buildings) {
                            if (b.type == bt && b.canUpgrade() && world.money >= bt.cost
                                    && p.population >= Math.abs(p.statistics.nativeWorkerDemand) + Math.abs(bt.getResource("worker"))) {
                                toUpgrade.add(b);
                            }
                        }
                    }
                    
                    if (!toUpgrade.isEmpty()) {
                        AIBuilding b = Collections.min(toUpgrade, AIBuilding.COMPARE_LEVEL);
                        world.money -= b.type.cost;
                        final Planet p0 = p.planet;
                        final Building b0 = b.building;
                        add(new Action0() {
                            @Override
                            public void invoke() {
                                controls.actionUpgradeBuilding(p0, b0, b0.upgradeLevel + 1);
                            }
                        });
                        return;
                    }
                }
            }            
            // do the remaining checks
            
            if (world.global.hasMilitarySpaceport
                    && world.global.production.equipment > 0
                    && world.global.production.spaceship > 0
                    && world.global.production.weapons > 0) {
                for (AIPlanet p : planets) {
                    if (checkRadar(p)) {
                        return;
                    }
                    if (checkSocial(p)) {
                        return;
                    }
                }
            }
        }
    }
}
