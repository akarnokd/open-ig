/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.core.Tile;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.Planet;
import hu.openig.model.TaxLevel;
import hu.openig.utils.JavaUtils;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Colony planner. Constructs civilian buildings,
 * ensures the colony is in good health (similarly to autobuild).
 * Builds social buildings to keep the morale.
 * Adjusts taxes according to morale.
 * Builds factories and trading buildings.
 * May demolish damanged buildings.
 * May block other planners with empty action to gain money.
 * @author akarnokd, 2011.12.28.
 */
public class ColonyPlanner extends Planner {
	/** The energy building selector. */
	final BuildingSelector energy = new BuildingSelector() {
		@Override
		public boolean accept(AIPlanet planet, AIBuilding building) {
			return building.getEnergy() > 0 && count(planet, building.type) > 1;
		}
		@Override
		public boolean accept(AIPlanet planet, BuildingType buildingType) {
			return buildingType.hasResource("energy") && buildingType.getResource("energy") > 0;
		}
	};
	/** Morale boosting buildings. */
	final BuildingSelector morale = new BuildingSelector() {
		@Override
		public boolean accept(AIPlanet planet, AIBuilding value) {
			return value.hasResource("morale");
		}
		@Override
		public boolean accept(AIPlanet planet, BuildingType value) {
			return value.hasResource("morale") && count(planet, value) < 1;
		}
	};
	/** The living space selector. */
	final BuildingSelector livingSpace = new BuildingSelector() {
		@Override
		public boolean accept(AIPlanet planet, AIBuilding value) {
			return value.hasResource("house");
		}
		@Override
		public boolean accept(AIPlanet planet, BuildingType value) {
			return value.hasResource("house");
		}
	};
	
	/**
	 * Constructor.
	 * @param world the world
	 * @param controls the controls
	 */
	public ColonyPlanner(AIWorld world, AIControls controls) {
		super(world, controls);
	}

	@Override
	protected void plan() {
		List<AIPlanet> planets = new ArrayList<AIPlanet>(world.ownPlanets);
		Collections.sort(planets, WORST_PLANET);
		for (AIPlanet planet : planets) {
			if (managePlanet(planet)) {
				return;
			}
		}
	}
	/**
	 * Manage a planet. Used by the AutoBuilder.
	 * @param planet the target planet
	 */
	public void managePlanet(Planet planet) {
		for (AIPlanet p : world.ownPlanets) {
			if (p.planet == planet) {
				managePlanet(p);
				return;
			}
		}
	}
	/** 
	 * Returns the actions to perform.
	 * @return the actions to perform
	 */
	public List<Action0> actions() {
		return applyActions;
	}
	/**
	 * Manage the given planet.
	 * @param planet the target planet
	 * @return true if action taken
	 */
	public boolean managePlanet(AIPlanet planet) {
		if (checkColonyHub(planet)) {
			return true;
		}
		if (checkBootstrap(planet)) {
			return true;
		}
		if (checkTax(planet)) {
			return true;
		}
		if (checkBuildingHealth(planet)) {
			return true;
		}
		if (!planet.statistics.constructing) {
			if (checkPower(planet)) {
				return true;
			}
		}
		if (!planet.statistics.constructing) {
			if (checkWorker(planet)) {
				return true;
			}
		}
		if (!planet.statistics.constructing) {
			if (checkLivingSpace(planet)) {
				return true;
			}
		}
		if (!planet.statistics.constructing) {
			if (checkFood(planet)) {
				return true;
			}
		}
		if (!planet.statistics.constructing) {
			if (checkHospital(planet)) {
				return true;
			}
		}
		if (!planet.statistics.constructing) {
			if (checkMorale(planet)) {
				return true;
			}
		}
		if (!planet.statistics.constructing) {
			if (checkPolice(planet)) {
				return true;
			}
		}
		if (!planet.statistics.constructing) {
			if (checkFireBrigade(planet)) {
				return true;
			}
		}
		// if very low morale, yield
		if (planet.morale >= 10 && planet.morale < 35 
				&& planet.statistics.problems.size() + planet.statistics.warnings.size() > 0
				&& world.money < 100000) {
			addEmpty();
			return true;
		}
		return false;
	}
	/** 
	 * Count the number of completed buildings.
	 * @param planet the target planet
	 * @return count 
	 */
	int builtCount(AIPlanet planet) {
		int result = 0;
		for (AIBuilding b : planet.buildings) {
			if (b.isComplete()) {
				result++;
			}
		}
		return result;
	}
	/**
	 * Check if the colony has only the colony hub. Help bootstrap new colonies.
	 * @param planet the planet to work with
	 * @return if action taken
	 */
	boolean checkBootstrap(final AIPlanet planet) {
		if (planet.buildings.size() < 2) {
			if (manageBuildings(planet, livingSpace, costOrder, false)) {
				return true;
			}
		}
		if (builtCount(planet) < 1 && world.money < 500000) {
			addEmpty();
			return true;
		}
		return false;
	}
	/**
	 * Ensure that buildings are repaired. 
	 * @param planet the planet to work with
	 * @return true if action taken
	 */
	boolean checkBuildingHealth(final AIPlanet planet) {
		// demolish severely damanged buildings, faster to create a new one
		for (final AIBuilding b : planet.buildings) {
			if (b.isDamaged() && !b.isConstructing() && b.health() < 0.5) {
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionDemolishBuilding(planet.planet, b.building);
					}
				});
				return true;
			}
		}
		// if low on money
		if (world.money < 1000) {
			// stop repairing
			boolean anyConstruction = false;
			if (planet.statistics.constructing) {
				anyConstruction = true;
				for (final AIBuilding b : planet.buildings) {
					if (b.repairing) {
						add(new Action0() {
							@Override
							public void invoke() {
								controls.actionRepairBuilding(planet.planet, b.building, false);
							}
						});
						return true;
					}
				}
			}
			if (anyConstruction) {
				return true;
			}
		}
		// find and start repairing the cheapest damaged building per planet
		for (final AIBuilding b : planet.buildings) {
			if (b.repairing) {
				return true; // don't let other things continue
			}
		}
		AIBuilding toRepair = null;
		for (final AIBuilding b : planet.buildings) {
			if (b.isDamaged() && !b.repairing) {
				if (b.hasResource("repair")) {
					toRepair = b;
					break;
				} else
				if (b.hasResource("energy") && b.getResource("energy") > 0) {
					toRepair = b;
					break;
				}
				if (toRepair == null || toRepair.type.cost > b.type.cost) {
					toRepair = b;
				}
			}
		}
		if (toRepair != null) {
			final AIBuilding b = toRepair;
			add(new Action0() {
				@Override
				public void invoke() {
					controls.actionRepairBuilding(planet.planet, b.building, true);
				}
			});
			return true;
		}
		// enable cheapest building if worker ratio is okay
		if (planet.statistics.workerDemand * 1.1 < planet.population) {
			AIBuilding min = null;
			for (AIBuilding b : planet.buildings) {
				if (!b.enabled) {
					if (min == null || min.getWorkers() > b.getWorkers()) {
						min = b;
					}
				}
			}
			if (min != null) {
				final AIBuilding fmin = min;
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionEnableBuilding(planet.planet, fmin.building, true);
					}
				});
				return true;
			}
		}
		return false;
	}
	/** 
	 * Ensure that no living space shortage present.
	 * @param planet the planet to work with
	 * @return if action taken
	 */
	boolean checkFireBrigade(final AIPlanet planet) {
		BuildingSelector fire = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("repair");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("repair") && count(planet, value) < 1;
			}
		};
		if (planet.population >= 25000 && planet.population > planet.statistics.workerDemand * 1.1) {
			return manageBuildings(planet, fire, costOrder, true);
		}
		return false;
	}
	/**
	 * Issue a change taxation command.
	 * @param planet the target planet
	 * @param tax the new tax level
	 */
	void setTaxLevelAction(final AIPlanet planet, final TaxLevel tax) {
		add(new Action0() {
			@Override
			public void invoke() {
				controls.actionSetTaxation(planet.planet, tax);
			}
		});
	}
	/**
	 * Check for low or high morale and adjust taxation to take advantage of it.
	 * @param planet the planet to work with
	 * @return true if action taken
	 */
	boolean checkTax(final AIPlanet planet) {
		// try changing the tax
		int moraleNow = planet.morale;
		TaxLevel tax = planet.tax;
		TaxLevel newLevel = null;
		if (moraleNow < 25 || planet.population < 4500) {
			newLevel = TaxLevel.NONE;
		} else
		if (moraleNow < 38 || planet.population < 5000) {
			newLevel = TaxLevel.VERY_LOW;
		} else
		if (moraleNow < 55 || planet.population < 5500) {
			newLevel = TaxLevel.LOW;
		} else
		if (moraleNow < 60) {
			newLevel = TaxLevel.MODERATE;
		} else
		if (moraleNow < 65) {
			newLevel = TaxLevel.ABOVE_MODERATE;
		} else
		if (moraleNow < 70) {
			newLevel = TaxLevel.HIGH;
		} else
		if (moraleNow < 78) {
			newLevel = TaxLevel.VERY_HIGH;
		} else
		if (moraleNow < 85) {
			newLevel = TaxLevel.OPPRESSIVE;
		} else
		if (moraleNow < 95) {
			newLevel = TaxLevel.EXPLOITER;
		} else {
			newLevel = TaxLevel.SLAVERY;
		}
		// reduce tax level if worker shortage
		if (newLevel != TaxLevel.NONE && planet.population < planet.statistics.workerDemand) {
			newLevel = TaxLevel.values()[newLevel.ordinal() - 1];
		}
		// reduce tax further if even lower worker shortage
		if (newLevel != TaxLevel.NONE && planet.population * 5 < planet.statistics.workerDemand * 4) {
			newLevel = TaxLevel.values()[newLevel.ordinal() - 1];
		}
		
		if (tax != newLevel) {
			setTaxLevelAction(planet, newLevel);
			return true;
		}
		return false;
	}
	/** 
	 * Check the morale level and build social buildings in necessary.
	 * @param planet the planet to work with
	 * @return action taken 
	 */
	boolean checkMorale(final AIPlanet planet) {
		int moraleNow = planet.morale;
		int moraleLast = planet.lastMorale;
		// only if there is energy available
		if (planet.statistics.energyAvailable >= planet.statistics.energyDemand * 1.1) {
			if (moraleNow < 21 && moraleLast < 27 && !planet.statistics.constructing) {
				if (boostMoraleWithBuilding(planet, false)) {
					return true;
				}
			}
			if (moraleNow < 50 && moraleLast < 50 && planet.population <= 5000) {
				BuildingType booster = null;
				for (BuildingType bt : w.buildingModel.buildings.values()) {
					if (planet.canBuild(bt) && bt.cost < world.money && bt.hasResource("morale")) {
						if (booster == null || booster.cost > bt.cost) {
							booster = bt;
						}
					}
				}
				if (booster != null && count(planet, booster) < 1) {
					if (planet.findLocation(booster) != null) {
						final BuildingType fbooster = booster;
						add(new Action0() {
							@Override
							public void invoke() {
								controls.actionPlaceBuilding(planet.planet, fbooster);
							}
						});
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * Try building/upgrading a morale boosting building.
	 * @param planet the target planet
	 * @param cheap use reverse cost order?
	 * @return if action taken
	 */
	boolean boostMoraleWithBuilding(final AIPlanet planet, boolean cheap) {
		return manageBuildings(planet, morale, cheap ? costOrderReverse : costOrder, true);
	}
	/** 
	 * Ensure that no living space shortage present.
	 * @param planet the target planet
	 * @return if action taken
	 */
	boolean checkFood(final AIPlanet planet) {
		BuildingSelector food = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("food");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("food");
			}
		};
		if (planet.population > planet.statistics.foodAvailable) {
			return manageBuildings(planet, food, costOrder, true);
		}
		return false;
	}
	/**
	 * Check if there is shortage on police.
	 * @param planet the target planet
	 * @return if action taken
	 */
	boolean checkPolice(final AIPlanet planet) {
		BuildingSelector police = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("police");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("police");
			}
		};
		if (planet.population > planet.statistics.policeAvailable) {
			return manageBuildings(planet, police, costOrder, true);
		}
		return false;
	}
	/**
	 * Check if there is shortage on hospitals.
	 * @param planet the target planet
	 * @return if action taken
	 */
	boolean checkHospital(final AIPlanet planet) {
		BuildingSelector hospital = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("hospital");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("hospital");
			}
		};
		if (planet.population > planet.statistics.hospitalAvailable) {
			return manageBuildings(planet, hospital, costOrder, true);
		}
		return false;
	}
	/** 
	 * Ensure that no living space shortage present.
	 * @param planet the target planet
	 * @return if action taken
	 */
	boolean checkLivingSpace(final AIPlanet planet) {
		if (planet.population > planet.statistics.houseAvailable) {
			return manageBuildings(planet, livingSpace, costOrder, true);
		}
		return false;
	}
	/**
	 * Check if there is some worker demand issues,
	 * if so, try building morale and population growth boosting buildings.
	 * @param planet the target planet
	 * @return true if action taken
	 */
	boolean checkWorker(final AIPlanet planet) {
		if (planet.population < planet.statistics.workerDemand) {
			// try disabling buildings
			AIBuilding max = null;
			for (AIBuilding b : planet.buildings) {
				if (b.enabled && !b.type.kind.equals("MainBuilding") && b.getEnergy() < 0) {
					int mw = max != null ? max.getWorkers() : 0;
					int w = b.getWorkers();
					if (max == null || mw > w) {
						max = b;
					}
				}
			}
			if (max != null) {
				final AIBuilding fb = max;
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionEnableBuilding(planet.planet, fb.building, false);
					}
				});
				return true;
			}
			BuildingSelector moraleGrowth = new BuildingSelector() {
				@Override
				public boolean accept(AIPlanet planet, AIBuilding value) {
					return value.hasResource("morale") || value.hasResource("population-growth");
				}
				@Override
				public boolean accept(AIPlanet planet, BuildingType value) {
					return (value.hasResource("morale") || value.hasResource("population-growth")) 
							&& count(planet, value) < 1;
				}
			};
			return manageBuildings(planet, moraleGrowth, costOrder, true);
		}
		return false;
	}

	/**
	 * Check if there are enough power on the planet,
	 * if not, try adding morale and growth increasing buildings.
	 * @param planet the target planet
	 * @return true if action taken
	 */
	boolean checkPower(final AIPlanet planet) {
		// sell power plants if too much energy
		if (planet.statistics.energyAvailable > planet.statistics.energyDemand * 2) {
			for (final AIBuilding b : planet.buildings) {
				if (b.getEnergy() > 0 && planet.statistics.energyAvailable - b.getEnergy() > planet.statistics.energyDemand) {
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
		if (planet.statistics.energyAvailable < planet.statistics.energyDemand
				&& planet.statistics.workerDemand < planet.population) {
			if (manageBuildings(planet, energy, costOrder, true)) {
				return true;
			}
			return checkReplacePP(planet);
		}
		return false;
	}
	/**
	 * Check for the case if the planet is full, then demolish a
	 * building to get space for the power plant.
	 * @param planet the target planet
	 * @return true if action taken
	 */
	boolean checkReplacePP(final AIPlanet planet) {
		BuildingType roomFor = null;
		BuildingType moneyFor = null;
		for (BuildingType bt : w.buildingModel.buildings.values()) {
			if (planet.canBuild(bt) && energy.accept(planet, bt)) {
				Point pt = planet.findLocation(bt);
				if (pt != null) {
					roomFor = bt;
				}
				if (moneyFor == null || moneyFor.cost < bt.cost) {
					moneyFor = bt;
				}
			}
		}
		// if no room but have money for one
		if (roomFor == null && moneyFor != null) {
			// find a cheaper power plant and demolish it
			for (final AIBuilding b : planet.buildings) {
				if (energy.accept(planet, b) && b.type.cost < moneyFor.cost) {
					add(new Action0() {
						@Override
						public void invoke() {
							controls.actionDemolishBuilding(planet.planet, b.building);
						}
					});
					return true;
				}
			}
			Tile b1 = moneyFor.tileset.get(planet.planet.race).normal; 
			// if no such building, demolish something with large enough footprint
			// except colony hub
			for (final AIBuilding b : planet.buildings) {
				if (!b.type.kind.equals("MainBuilding") && !energy.accept(planet, b)) {
					Tile b0 = b.tileset.normal;
					if (b0.width >= b1.width && b0.height >= b1.height) {
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
		}
		return false;
	}
	/**
	 * Compares the numerical levels of the values and returns which one of it is worse.
	 * @param firstAvail the first availability
	 * @param firstDemand the first demand
	 * @param secondAvail the second availability
	 * @param secondDemand the second demand
	 * @return -1, 0 or 1
	 */
	int worst(int firstAvail, int firstDemand, int secondAvail, int secondDemand) {
		boolean firstOk = firstAvail >= firstDemand;
		boolean secondOk = secondAvail >= secondDemand;
		if (firstOk && !secondOk) {
			return 1;
		} else
		if (!firstOk && secondOk) {
			return -1;
		} else
		if (firstOk && secondOk) {
			return (secondAvail - secondDemand) - (firstAvail - firstDemand);
		}
		double r1 = firstAvail > 0 ? 1.0 * firstDemand / firstAvail : Double.POSITIVE_INFINITY;
		double r2 = firstAvail > 0 ? 1.0 * secondDemand / secondAvail : Double.POSITIVE_INFINITY;
		return r1 > r2 ? -1 : (r1 < r2 ? 1 : 0);
	}
	/**
	 * Check if a colony hub is available on planets,
	 * if not, try to build one.
	 * @param planet the target planet
	 * @return true if action taken
	 */
	boolean checkColonyHub(final AIPlanet planet) {
		boolean found = false;
		for (Building b : planet.buildings) {
			if (b.type.kind.equals("MainBuilding")) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			final BuildingType bt = findBuildingKind("MainBuilding");
			if (world.money < bt.cost) {
				if (getMoreMoney(planet)) {
					return true;
				} else {
					// if no money could be gained, simply wait for the next day
					addEmpty();
					return true;
				}
			}
			add(new Action0() {
				@Override
				public void invoke() {
					controls.actionPlaceBuilding(planet.planet, bt);
				}
			});
			return true;
		}
		// check planets with damaged colony hub
		for (final AIBuilding b : planet.buildings) {
			if (b.type.kind.equals("MainBuilding")) {
				if (b.isDamaged() && !b.repairing) {
					controls.actionRepairBuilding(planet.planet, b.building, true);
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Try to get more money by selling buildings.
	 * @param current the current planet where to look for buildings first
	 * @return true if action taken
	 */
	boolean getMoreMoney(final AIPlanet current) {
		List<Func1<Building, Boolean>> functions = JavaUtils.newArrayList();
		// severly damaged
		functions.add(new Func1<Building, Boolean>() {
			@Override
			public Boolean invoke(Building value) {
				return value.health() < 0.5;
			}
		});
		// damaged
		functions.add(new Func1<Building, Boolean>() {
			@Override
			public Boolean invoke(Building value) {
				return value.health() < 0.15;
			}
		});
		// any
		functions.add(new Func1<Building, Boolean>() {
			@Override
			public Boolean invoke(Building value) {
				return true;
			}
		});
		for (Func1<Building, Boolean> f : functions) {
			if (findDamaged(current, f)) {
				return true;
			}
			for (AIPlanet planet : world.ownPlanets) {
				if (planet != current) {
					if (findDamaged(planet, f)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Find the cheapest damaged building and issue a demolish order.
	 * @param current the current planet
	 * @param check the check function.
	 * @return true if action taken
	 */
	boolean findDamaged(final AIPlanet current, final Func1<Building, Boolean> check) {
		AIBuilding cheapest = null;
		for (AIBuilding b : current.buildings) {
			if (check.invoke(b)) {
				if (cheapest == null || cheapest.type.cost < b.type.cost) {
					cheapest = b;
				}
			}
		}
		if (cheapest != null) {
			final AIBuilding fcheapest = cheapest;
			add(new Action0() {
				@Override
				public void invoke() {
					controls.actionDemolishBuilding(current.planet, fcheapest.building);
				}
			});
			return true;
		}
		return false;
	}
}
