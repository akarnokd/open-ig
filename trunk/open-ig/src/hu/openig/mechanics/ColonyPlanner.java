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
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.PlanetProblems;
import hu.openig.model.TaxLevel;
import hu.openig.utils.JavaUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	/** Compares planets and chooses the worst overall condition. */
	static final Comparator<AIPlanet> WORST_PLANET = new Comparator<AIPlanet>() {
		@Override
		public int compare(AIPlanet o1, AIPlanet o2) {
			return status(o2) - status(o1);
		}
		/** 
		 * Computes the health status.
		 * @param o1 the planet
		 * @return the status number
		 */
		int status(AIPlanet p) {
			int value = 0;
			value += required(p.morale, 50) * 500;
			value += required(p.buildings.size(), 2) * 1000;
			if (p.statistics.hasProblem(PlanetProblems.COLONY_HUB) || p.statistics.hasWarning(PlanetProblems.COLONY_HUB)) {
				value += 20000;
			}
			value += required(p.statistics.houseAvailable, p.population) * 2;
			value += required(p.statistics.energyAvailable, p.statistics.energyDemand) * 2;
			value += required(p.statistics.foodAvailable, p.population);
			value += required(p.statistics.hospitalAvailable, p.population);
			value += required(p.statistics.policeAvailable, p.population);
			return value;
		}
		/**
		 * If available is less than the demand, return the difference, return zero otherwise
		 * @param available the available amount
		 * @param demand the demand amount
		 * @return the required
		 */
		int required(int available, int demand) {
			if (available < demand) {
				return demand - available;
			}
			return 0;
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
			if (checkColonyHub(planet)) {
				return;
			}
			if (checkBootstrap(planet)) {
				return;
			}
			if (checkTax(planet)) {
				return;
			}
			if (checkBuildingHealth(planet)) {
				return;
			}
			if (checkPower(planet)) {
				return;
			}
			if (checkWorker(planet)) {
				return;
			}
			if (checkLivingSpace(planet)) {
				return;
			}
			if (checkFood(planet)) {
				return;
			}
			if (checkHospital(planet)) {
				return;
			}
			if (checkMorale(planet)) {
				return;
			}
			if (checkPolice(planet)) {
				return;
			}
			if (checkFireBrigade(planet)) {
				return;
			}
		}
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
			if (b.isSeverlyDamaged()) {
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
				addEmpty();
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
				return value.hasResource("repair") && limit(planet, value, 1);
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
		
		if (moraleNow < 25 || planet.population < 4500) {
			if (tax != TaxLevel.NONE) {
				setTaxLevelAction(planet, TaxLevel.NONE);
				return true;
			}
		} else
		if (moraleNow < 38 || planet.population < 5000) {
			if (tax != TaxLevel.VERY_LOW) {
				setTaxLevelAction(planet, TaxLevel.VERY_LOW);
				return true;
			}
		} else
		if (moraleNow < 55 || planet.population < 5500) {
			if (tax != TaxLevel.LOW) {
				setTaxLevelAction(planet, TaxLevel.LOW);
				return true;
			}
		} else
		if (moraleNow < 60) {
			if (tax != TaxLevel.MODERATE) {
				setTaxLevelAction(planet, TaxLevel.MODERATE);
				return true;
			}
		} else
		if (moraleNow < 65) {
			if (tax != TaxLevel.ABOVE_MODERATE) {
				setTaxLevelAction(planet, TaxLevel.ABOVE_MODERATE);
				return true;
			}
		} else
		if (moraleNow < 70 && planet.population > 10000) {
			if (tax != TaxLevel.HIGH) {
				setTaxLevelAction(planet, TaxLevel.HIGH);
				return true;
			}
		} else
		if (moraleNow < 78 && planet.population > 15000) {
			if (tax != TaxLevel.VERY_HIGH) {
				setTaxLevelAction(planet, TaxLevel.VERY_HIGH);
				return true;
			}
		} else
		if (moraleNow < 85 && planet.population > 20000) {
			if (tax != TaxLevel.OPPRESSIVE) {
				setTaxLevelAction(planet, TaxLevel.OPPRESSIVE);
				return true;
			}
		} else
		if (moraleNow < 95 && planet.population > 25000) {
			if (tax != TaxLevel.EXPLOITER) {
				setTaxLevelAction(planet, TaxLevel.EXPLOITER);
				return true;
			}
		} else {
			if (tax != TaxLevel.SLAVERY) {
				setTaxLevelAction(planet, TaxLevel.SLAVERY);
				return true;
			}
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
		if (planet.statistics.energyAvailable * 2 >= planet.statistics.energyDemand) {
			if (moraleNow < 21 && moraleLast < 27 && !planet.statistics.constructing) {
				if (boostMoraleWithBuilding(planet)) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Try building/upgrading a morale boosting building.
	 * @param planet the target planet
	 * @return if action taken
	 */
	boolean boostMoraleWithBuilding(final AIPlanet planet) {
		BuildingSelector morale = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("morale");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("morale") && limit(planet, value, 1);
			}
		};
		return manageBuildings(planet, morale, costOrder, true);
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
		if (planet.population > planet.statistics.policeAvailable * 1.1) {
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
		if (planet.population > planet.statistics.hospitalAvailable * 1.1) {
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
		BuildingSelector morale = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("morale") || value.hasResource("population-growth");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return (value.hasResource("morale") || value.hasResource("population-growth")) && limit(planet, value, 1);
			}
		};
		if (planet.population < planet.statistics.workerDemand) {
			return manageBuildings(planet, morale, costOrder, true);
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
		BuildingSelector energy = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding building) {
				return building.getEnergy() > 0;
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType buildingType) {
				return buildingType.hasResource("energy") && buildingType.getResource("energy") > 0;
			}
		};
		if (planet.statistics.energyAvailable < planet.statistics.energyDemand) {
			return manageBuildings(planet, energy, costOrder, true);
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
				return value.isSeverlyDamaged();
			}
		});
		// damaged
		functions.add(new Func1<Building, Boolean>() {
			@Override
			public Boolean invoke(Building value) {
				return value.isDamaged();
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
