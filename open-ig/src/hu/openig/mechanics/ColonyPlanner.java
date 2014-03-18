/*
 * Copyright 2008-2014, David Karnok 
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
import hu.openig.model.Planet;
import hu.openig.model.TaxLevel;
import hu.openig.model.Tile;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
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
			return value.hasResource(BuildingType.RESOURCE_MORALE);
		}
		@Override
		public boolean accept(AIPlanet planet, BuildingType value) {
			if (value.hasResource(BuildingType.RESOURCE_MORALE) && count(planet, value) < 1) {
				// don't build stadium if the morale is moderate and population is low
				if ("Stadium".equals(value.id)) {
					return planet.morale < 25 || planet.population >= 40000;
				}
				return true;
			}
			return false;
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
		List<AIPlanet> planets = new ArrayList<>(world.ownPlanets);
		Collections.sort(planets, WORST_PLANET);
		for (AIPlanet planet : planets) {
			if (managePlanet(planet)) {
				if (world.mainPlayer == this.p && world.money < world.autoBuildLimit) {
					return;
				}
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
		if (!planet.statistics.constructing) {
			if (checkGrowth(planet)) {
				return true;
			}
		}
		// if very low morale, yield
		if (planet.morale >= 10 && planet.morale < 35 
				&& planet.statistics.problems.size() + planet.statistics.warnings.size() > 0
				&& world.money < 100000) {
			world.money = 0L; // do not let money-related tasks to continue
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
		if (builtCount(planet) < 1 && world.money < 150000) {
			world.money = 0L; // do not let money-related tasks to continue
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
		if (planet.statistics.workerDemand < planet.population) {
			AIBuilding min = null;
			for (AIBuilding b : planet.buildings) {
				if (!b.enabled) {
					if (min == null || min.getWorkers() < b.getWorkers()) {
						min = b;
					}
				}
			}
			if (min != null && planet.statistics.workerDemand - min.getWorkers() < planet.population) {
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
		if (planet.population >= 28000 && planet.population > planet.statistics.workerDemand * 1.1) {
			return manageBuildings(planet, fire, costOrder, true);
		}
		return false;
	}
	/** 
	 * Ensure that no living space shortage present.
	 * @param planet the planet to work with
	 * @return if action taken
	 */
	boolean checkGrowth(final AIPlanet planet) {
		BuildingSelector growth = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource(BuildingType.RESOURCE_POPULATION_GROWTH);
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource(BuildingType.RESOURCE_POPULATION_GROWTH) 
						&& count(planet, value) < 1;
			}
		};
		if (planet.population >= 7500 
				&& planet.population > planet.statistics.workerDemand * 1.1
				&& world.money >= 75000) {
			return manageBuildings(planet, growth, costOrder, true);
		}
		return false;
	}
	/**
	 * Issue a change taxation command.
	 * @param planet the target planet
	 * @param tax the new tax level
	 */
	void setTaxLevelAction(final AIPlanet planet, final TaxLevel tax) {
		add(new SetTaxAction(planet, tax));
	}
	/**
	 * Action to set the taxation level on a planet.
	 * @author akarnokd, 2013.09.03.
	 */
	public class SetTaxAction implements Action0 {
		/** The planet. */
		protected final Planet planet;
		/** The new tax level. */
		protected final TaxLevel tax;
		/**
		 * Constructor.
		 * @param planet the planet
		 * @param tax the tax
		 */
		public SetTaxAction(AIPlanet planet, TaxLevel tax) {
			this.planet = planet.planet;
			this.tax = tax;
		}
		@Override
		public void invoke() {
			controls.actionSetTaxation(planet, tax);
		}
	}
	/**
	 * Check for low or high morale and adjust taxation to take advantage of it.
	 * @param planet the planet to work with
	 * @return true if action taken
	 */
	boolean checkTax(final AIPlanet planet) {
		// try changing the tax
		// but only once per day
		double moraleNow = planet.lastMorale; // planet.morale
		TaxLevel tax = planet.tax;
		TaxLevel newLevel;
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
		if (newLevel != TaxLevel.NONE && planet.population < planet.statistics.nativeWorkerDemand) {
			newLevel = TaxLevel.values()[newLevel.ordinal() - 1];
		}
		// reduce tax further if even lower worker shortage
		if (newLevel != TaxLevel.NONE && planet.population * 5 < planet.statistics.nativeWorkerDemand * 4) {
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
		if (!world.isNewDay) {
			return false;
		}
		double moraleNow = planet.morale;
		double moraleLast = planet.lastMorale;
		// only if there is energy available
		if (planet.statistics.energyAvailable >= planet.statistics.energyDemand) {
			if (moraleNow < 21 && moraleLast < 27 && !planet.statistics.constructing) {
				if (manageBuildings(planet, morale, costOrder, true)) {
					return true;
				}
			}
			if (moraleNow < 50 && moraleLast < 55 && planet.population <= 5100) {
				
				List<BuildingType> candidates = new ArrayList<>();
				
				for (BuildingType bt : w.buildingModel.buildings.values()) {
					if (bt.cost < world.money
							&& morale.accept(planet, bt)
							&& planet.canBuild(bt)
							&& planet.findLocation(bt) != null
							) {
						candidates.add(bt);
					}
				}
				
				Collections.sort(candidates, BuildingType.COST);
				
				boolean noMoraleYet = true;
				
				if (!candidates.isEmpty() && noMoraleYet) {
					final BuildingType bt = candidates.get(0);
					build(planet, bt);
					return true;
				}
				
				for (final BuildingType bt : candidates) {
					if (count(planet, bt) < 1) {
						build(planet, bt);
					}
				}
			}
		}
		return false;
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
	 * Returns a list of buildings candidate for disablement.
	 * @param planet the target planet
	 * @return the list of buildings
	 */
	List<AIBuilding> disableCandidates(AIPlanet planet) {
		List<AIBuilding> result = new ArrayList<>();
		for (AIBuilding b : planet.buildings) {
			if (b.enabled && b.isComplete() 
					&& !b.type.kind.equals(BuildingType.KIND_MAIN_BUILDING) 
					&& b.getEnergy() < 0) {
				result.add(b);
			}
		}
		return result;
	}
	/**
	 * The disable order.
	 */
	final Comparator<AIBuilding> disableOrder = new Comparator<AIBuilding>() {
		/** The global kind order. */
		final List<String> kinds = Arrays.asList(
				"Defensive",
				"Gun", 
				"Shield", 
				"Social", 
				"Radar", 
				"Economic", 
				"MilitarySpaceport", 
				"Factory"
			);
		@Override
		public int compare(AIBuilding o1, AIBuilding o2) {
			int i1 = kinds.indexOf(o1.type.kind);
			int i2 = kinds.indexOf(o2.type.kind);
			if (i1 < i2) {
				return -1;
			} else
			if (i1 > i2) {
				return 1;
			}
			int w1 = -o1.getWorkers();
			int w2 = -o2.getWorkers();
			return w2 - w1;
		}
	};
	/**
	 * Check if there is some worker demand issues,
	 * if so, try building morale and population growth boosting buildings.
	 * @param planet the target planet
	 * @return true if action taken
	 */
	boolean checkWorker(final AIPlanet planet) {
		if (planet.population < planet.statistics.workerDemand) {
			// try disabling buildings
			List<AIBuilding> candidates = disableCandidates(planet);
			if (!candidates.isEmpty()) {
				AIBuilding max = Collections.max(candidates, disableOrder);
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
						return value.hasResource(BuildingType.RESOURCE_MORALE) || value.hasResource(BuildingType.RESOURCE_POPULATION_GROWTH);
					}
					@Override
					public boolean accept(AIPlanet planet, BuildingType value) {
						return (value.hasResource(BuildingType.RESOURCE_MORALE) || value.hasResource(BuildingType.RESOURCE_POPULATION_GROWTH)) 
								&& count(planet, value) < 1;
					}
				};
				return manageBuildings(planet, moraleGrowth, costOrder, true);
			}
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
			Tile b1 = moneyFor.tileset.get(planet.planet.race).normal; 
			// if no such building, demolish something with large enough footprint
			// except colony hub
			for (final AIBuilding b : planet.buildings) {
				if (!b.type.kind.equals("MainBuilding") && !energy.accept(planet, b)) {
					Tile b0 = b.tileset.normal;
					if (b0.width >= b1.width && b0.height >= b1.height) {
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
			if (world.money < bt.cost && world.money > 0) {
				if (getMoreMoney(planet)) {
					return true;
				}
				// if no money could be gained, simply wait for the next day
				world.money = 0L; // do not let money-related tasks to continue
				return true;
			}
			build(planet, bt);
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
		List<Func1<Building, Boolean>> functions = new ArrayList<>();
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
			current.buildings.remove(cheapest); // do not sell twice
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
