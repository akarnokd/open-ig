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
import hu.openig.core.Pred1;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.ResearchType;
import hu.openig.model.TaxLevel;
import hu.openig.utils.JavaUtils;

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
	/**
	 * Interface for selecting a building or building type.
	 * @author akarnokd, 2011.12.28.
	 */
	public interface BuildingSelector {
		/**
		 * Accept the given building for upgrading.
		 * @param building the building
		 * @return true if accept
		 */
		boolean accept(AIBuilding building);
		/**
		 * Accept the given building type for construction.
		 * @param buildingType the building type
		 * @return true if accept
		 */
		boolean accept(BuildingType buildingType);
	}
	/**
	 * Interface for comparing values of buildings. 
	 * @author akarnokd, 2011.12.28.
	 */
	public interface BuildingOrder {
		/**
		 * Compare the values of two concrete buildings.
		 * @param b1 the first building
		 * @param b2 the second building
		 * @return -1, 0 or 1
		 */
		int compare(AIBuilding b1, AIBuilding b2);
		/**
		 * Compare the values of two building types.
		 * @param b1 the first building type
		 * @param b2 the second building type
		 * @return -1, 0 or 1
		 */
		int compare(BuildingType b1, BuildingType b2);
	}
	/** Default incremental cost-order. */
	final BuildingOrder costOrder = new BuildingOrder() {
		@Override
		public int compare(AIBuilding o1, AIBuilding o2) {
			return o1.type.cost < o2.type.cost ? -1 : (o1.type.cost > o2.type.cost ? 1 : 0);
		}
		@Override
		public int compare(BuildingType o1, BuildingType o2) {
			return o1.cost < o2.cost ? -1 : (o1.cost > o2.cost ? 1 : 0);
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
		if (checkColonyHub()) {
			return;
		}
		if (checkPower()) {
			return;
		}
		if (checkWorker()) {
			return;
		}
		if (checkLivingSpace()) {
			return;
		}
		if (checkFood()) {
			return;
		}
		if (checkHospital()) {
			return;
		}
		if (checkPolice()) {
			return;
		}
		if (checkMorale()) {
			return;
		}
		if (checkBuildingHealth()) {
			return;
		}
		if (checkRadar()) {
			return;
		}
		if (checkEconomy()) {
			return;
		}
		if (checkFactory()) {
			return;
		}
		if (checkFireBrigade()) {
			return;
		}
	}
	/**
	 * Ensure that buildings are repaired. 
	 * @return true if action taken
	 */
	boolean checkBuildingHealth() {
		for (final AIPlanet planet : world.ownPlanets) {
			for (final AIBuilding b : planet.buildings) {
				if (b.isDamaged() && !b.repairing) {
					add(new Action0() {
						@Override
						public void invoke() {
							controls.actionRepairBuilding(planet.planet, b.building, true);
						}
					});
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * If colony lacks radar, build one, if better available than the current
	 * demolish existing (the next turn will build one).
	 * If hubble2 is available place it, or produce it.
	 * @return if action taken
	 */
	boolean checkRadar() {
		// check if hubble2 is buildable
		BuildingType bestRadar = null;
		for (BuildingType bt : w.buildingModel.buildings.values()) {
			if (bt.hasResource("radar") && (bt.research == null || world.availableResearch.contains(bt.research))) {
				if (bestRadar == null || bestRadar.getResource("radar") < bt.getResource("radar")) {
					bestRadar = bt;
				}
			}
		}
		ResearchType hubble2 = null;
		for (ResearchType rt : world.availableResearch) {
			if (rt.has("radar")) {
				if (hubble2 == null || hubble2.getInt("radar") < rt.getInt("radar")) {
					hubble2 = rt;
				}
			}
		}
		int hubble2Count = hubble2 != null ? world.inventory.get(hubble2) : 0; 
		
		outer:
		for (final AIPlanet planet : world.ownPlanets) {
			if (planet.statistics.constructing) {
				continue;
			}
			// if hubble is in inventory
			if (hubble2 != null && planet.hasInventory(hubble2)) {
				// demolish remaining ground radars
				for (final AIBuilding b : planet.buildings) {
					if (b.hasResource("radar")) {
						add(new Action0() {
							@Override
							public void invoke() {
								controls.actionDemolishBuilding(planet.planet, b.building);
							}
						});
						return true;
					}
				}
				continue;
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
				new ProductionOrder(world, hubble2, applyActions, controls).invoke();
				return true;
			}
			
			if (bestRadar != null) {
				// check if already present
				for (AIBuilding b : planet.buildings) {
					if (b.type == bestRadar) {
						continue outer;
					}
				}
				if (bestRadar.cost <= world.money 
					&& planet.findLocation(bestRadar) != null) {
					// demolish undertech radars
					for (final AIBuilding b : planet.buildings) {
						if (b.hasResource("radar")) {
							if (b.getResource("radar") < bestRadar.getResource("radar")) {
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
					final BuildingType fbestRadar = bestRadar;
					add(new Action0() {
						@Override
						public void invoke() {
							controls.actionPlaceBuilding(planet.planet, fbestRadar);
						}
					});
				}
			}
		}
		return false;
	}
	/** 
	 * Ensure that no living space shortage present.
	 * @return if action taken
	 */
	boolean checkFireBrigade() {
		BuildingSelector food = new BuildingSelector() {
			@Override
			public boolean accept(AIBuilding value) {
				return value.hasResource("repair");
			}
			@Override
			public boolean accept(BuildingType value) {
				return value.hasResource("repair");
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				// check if there is no or at least one upgradable fire department
				for (Building b : value.buildings) {
					if (b.hasResource("repair") && !b.canUpgrade() && !b.isDamaged()) {
						return false;
					}
				}
				return value.population > value.statistics.workerDemand * 1.1;
			}
		}, food, costOrder, true);
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
	 * @return true if action taken
	 */
	boolean checkMorale() {
		// try changing the tax
		for (AIPlanet planet : world.ownPlanets) {
			int moraleNow = planet.morale;
			TaxLevel tax = planet.tax;
			
			if (moraleNow < 20) {
				if (tax != TaxLevel.NONE) {
					setTaxLevelAction(planet, TaxLevel.NONE);
					return true;
				}
			} else
			if (moraleNow < 36) {
				if (tax != TaxLevel.VERY_LOW) {
					setTaxLevelAction(planet, TaxLevel.VERY_LOW);
					return true;
				}
			} else
			if (moraleNow < 45) {
				if (tax != TaxLevel.LOW) {
					setTaxLevelAction(planet, TaxLevel.LOW);
					return true;
				}
			} else
			if (moraleNow < 55) {
				if (tax != TaxLevel.MODERATE) {
					setTaxLevelAction(planet, TaxLevel.MODERATE);
					return true;
				}
			} else
			if (moraleNow < 60) {
				if (tax != TaxLevel.ABOVE_MODERATE) {
					setTaxLevelAction(planet, TaxLevel.ABOVE_MODERATE);
					return true;
				}
			} else
			if (moraleNow < 70) {
				if (tax != TaxLevel.HIGH) {
					setTaxLevelAction(planet, TaxLevel.HIGH);
					return true;
				}
			} else
			if (moraleNow < 78) {
				if (tax != TaxLevel.VERY_HIGH) {
					setTaxLevelAction(planet, TaxLevel.VERY_HIGH);
					return true;
				}
			} else
			if (moraleNow < 85) {
				if (tax != TaxLevel.OPPRESSIVE) {
					setTaxLevelAction(planet, TaxLevel.OPPRESSIVE);
					return true;
				}
			} else
			if (moraleNow < 95) {
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
			
		}
		// if morale is still low, build a morale boosting building
		for (AIPlanet planet : world.ownPlanets) {
			int moraleNow = planet.morale;
			int moraleLast = planet.lastMorale;
			
			if (moraleNow < 35 && moraleLast < 45 && !planet.statistics.constructing) {
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
			public boolean accept(AIBuilding value) {
				return value.hasResource("morale");
			}
			@Override
			public boolean accept(BuildingType value) {
				return value.hasResource("morale");
			}
		};
		return manageBuildings(planet, morale, costOrder, true);
	}
	/** 
	 * Ensure that no living space shortage present.
	 * @return if action taken
	 */
	boolean checkFood() {
		BuildingSelector food = new BuildingSelector() {
			@Override
			public boolean accept(AIBuilding value) {
				return value.hasResource("food");
			}
			@Override
			public boolean accept(BuildingType value) {
				return value.hasResource("food");
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.foodAvailable;
			}
		}, food, costOrder, true);
	}
	/**
	 * Check if there is shortage on police.
	 * @return if action taken
	 */
	boolean checkEconomy() {
		BuildingSelector police = new BuildingSelector() {
			@Override
			public boolean accept(AIBuilding value) {
				return value.hasResource("multiply") || value.hasResource("credit");
			}
			@Override
			public boolean accept(BuildingType value) {
				return value.hasResource("multiply") || value.hasResource("credit");
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.workerDemand * 1.1;
			}
		}, police, costOrder, false);
	}
	/**
	 * Check if there is shortage on police.
	 * @return if action taken
	 */
	boolean checkFactory() {
		BuildingSelector police = new BuildingSelector() {
			@Override
			public boolean accept(AIBuilding value) {
				return value.hasResource("spaceship") || value.hasResource("equipment") || value.hasResource("weapon");
			}
			@Override
			public boolean accept(BuildingType value) {
				return  value.hasResource("spaceship") || value.hasResource("equipment") || value.hasResource("weapon");
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.workerDemand * 1.1;
			}
		}, police, costOrder, false);
	}
	/**
	 * Check if there is shortage on police.
	 * @return if action taken
	 */
	boolean checkPolice() {
		BuildingSelector police = new BuildingSelector() {
			@Override
			public boolean accept(AIBuilding value) {
				return value.hasResource("hospital");
			}
			@Override
			public boolean accept(BuildingType value) {
				return value.hasResource("hospital");
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.policeAvailable * 1.1;
			}
		}, police, costOrder, true);
	}
	/**
	 * Check if there is shortage on hospitals.
	 * @return if action taken
	 */
	boolean checkHospital() {
		BuildingSelector hospital = new BuildingSelector() {
			@Override
			public boolean accept(AIBuilding value) {
				return value.hasResource("hospital");
			}
			@Override
			public boolean accept(BuildingType value) {
				return value.hasResource("hospital");
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.hospitalAvailable * 1.1;
			}
		}, hospital, costOrder, true);
	}
	/** 
	 * Ensure that no living space shortage present.
	 * @return if action taken
	 */
	boolean checkLivingSpace() {
		BuildingSelector livingSpace = new BuildingSelector() {
			@Override
			public boolean accept(AIBuilding value) {
				return value.hasResource("house");
			}
			@Override
			public boolean accept(BuildingType value) {
				return value.hasResource("house");
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.houseAvailable;
			}
		}, livingSpace, costOrder, true);
	}
	/**
	 * Check if there is some worker demand issues,
	 * if so, try building morale and population growth boosting buildings.
	 * @return true if action taken
	 */
	boolean checkWorker() {
		BuildingSelector morale = new BuildingSelector() {
			@Override
			public boolean accept(AIBuilding value) {
				return value.hasResource("morale") || value.hasResource("population-growth");
			}
			@Override
			public boolean accept(BuildingType value) {
				return value.hasResource("morale") || value.hasResource("population-growth");
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population < value.statistics.workerDemand;
			}
		}, morale, costOrder, true);
	}
	/**
	 * Check if there are enough power on the planet,
	 * if not, try adding morale and growth increasing buildings.
	 * @return true if action taken
	 */
	boolean checkPower() {
		BuildingSelector energy = new BuildingSelector() {
			@Override
			public boolean accept(AIBuilding building) {
				return building.getEnergy() > 0;
			}
			@Override
			public boolean accept(BuildingType buildingType) {
				return buildingType.hasResource("energy");
			}
		};
		
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.statistics.energyAvailable < value.statistics.energyDemand;
			}
		}, energy, costOrder, true);	
	}
	/**
	 * Try to choose an upgrade option.
	 * @param planet the target planet
	 * @param selector the building selector
	 * @param order the building order
	 * @return true if action taken
	 */
	boolean manageUpgrade(final AIPlanet planet, 
			final BuildingSelector selector,
			final BuildingOrder order) {
		for (final AIBuilding b : planet.buildings) {
			if (!b.enabled && selector.accept(b)) {
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionEnableBuilding(planet.planet, b.building, true);
					}
				});
				return true;
			}
		}
		// scan for the most affordable upgrade
		AIBuilding upgrade = null;
		for (final AIBuilding b : planet.buildings) {
			if (selector.accept(b) && b.canUpgrade() && b.type.cost <= world.money) {
				if (upgrade == null || order.compare(upgrade, b) > 0) {
					upgrade = b;
				}
			}
		}
		if (upgrade != null) {
			final Building fupgrade = upgrade.building;
			add(new Action0() {
				@Override
				public void invoke() {
					controls.actionUpgradeBuilding(planet.planet, fupgrade, fupgrade.upgradeLevel + 1);
					
				}
			});
			return true;
		}
		return false;
	}
	/**
	 * Try to choose a construction option.
	 * @param planet the target planet
	 * @param selector the building selector
	 * @param order the building order
	 * @return true if action taken
	 */
	boolean manageConstruction(final AIPlanet planet, 
			final BuildingSelector selector,
			final BuildingOrder order) {
		// try building a new one
		BuildingType create = null;
		for (final BuildingType bt : w.buildingModel.buildings.values()) {
			if (selector.accept(bt) && planet.planet.canBuild(bt) && bt.cost <= world.money) {
				if (planet.findLocation(bt) != null) {
					if (create == null || order.compare(create, bt) > 0) {
						create = bt;
					}
				}
			}
		}
		if (create != null) {
			final BuildingType fcreate = create;
			add(new Action0() {
				@Override
				public void invoke() {
					controls.actionPlaceBuilding(planet.planet, fcreate);
				}
			});
			return true;
		}
		return false;
	}
	/**
	 * Try to choose a repair option.
	 * @param planet the target planet
	 * @param selector the building selector
	 * @param order the building order
	 * @return true if action taken
	 */
	boolean manageRepair(final AIPlanet planet, 
			final BuildingSelector selector,
			final BuildingOrder order) {
		// try repairing existing
		for (final AIBuilding b : planet.buildings) {
			if (!b.repairing && b.isDamaged() && selector.accept(b)) {
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionRepairBuilding(planet.planet, b.building, true);
					}
				});
				return true;
			}
		}
		return false;
	}
	/**
	 * Build or upgrade a power plant on the planet.
	 * @param planet the target planet
	 * @param selector to select the building type
	 * @param order the comparator for order
	 * @param upgradeFirst try upgrading first?
	 * @return true if action taken 
	 */
	boolean manageBuildings(final AIPlanet planet, 
			final BuildingSelector selector,
			final BuildingOrder order, boolean upgradeFirst) {
		if (upgradeFirst) {
			if (manageUpgrade(planet, selector, order)) {
				return true;
			}
			if (manageConstruction(planet, selector, order)) {
				return true;
			}
		} else {
			if (manageConstruction(planet, selector, order)) {
				return true;
			}
			if (manageUpgrade(planet, selector, order)) {
				return true;
			}
		}
		if (manageRepair(planet, selector, order)) {
			return true;
		}
		return false;
	}
	/**
	 * Check if a colony hub is available on planets,
	 * if not, try to build one.
	 * @return true if action taken
	 */
	boolean checkColonyHub() {
		// check for planets without colony hub first
		for (final AIPlanet planet : world.ownPlanets) {
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
		}
		// check planets with damaged colony hub
		for (final AIPlanet planet : world.ownPlanets) {
			for (final AIBuilding b : planet.buildings) {
				if (b.type.kind.equals("MainBuilding")) {
					if (b.isDamaged() && !b.repairing) {
						controls.actionRepairBuilding(planet.planet, b.building, true);
						return true;
					}
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
			if (findCheapestDamaged(current, f)) {
				return true;
			}
			for (AIPlanet planet : world.ownPlanets) {
				if (planet != current) {
					if (findCheapestDamaged(planet, f)) {
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
	boolean findCheapestDamaged(final AIPlanet current, final Func1<Building, Boolean> check) {
		Building cheapest = null;
		for (Building b : current.buildings) {
			if (check.invoke(b)) {
				if (cheapest == null || cheapest.type.cost < b.type.cost) {
					cheapest = b;
				}
			}
		}
		if (cheapest != null) {
			final Building fcheapest = cheapest;
			add(new Action0() {
				@Override
				public void invoke() {
					controls.actionDemolishBuilding(current.planet, fcheapest);
				}
			});
			return true;
		}
		return false;
	}
	/**
	 * Plan a specific category of building actions.
	 * @param condition the condition to check for the planet
	 * @param selector the selector of candidate buildings
	 * @param order the order between alternative buildings
	 * @param upgradeFirst try upgrading first?
	 * @return true if action taken
	 */
	boolean planCategory(Func1<AIPlanet, Boolean> condition, 
			BuildingSelector selector, BuildingOrder order, boolean upgradeFirst) {
		// try to upgrade or build a new power plant
		for (final AIPlanet planet : world.ownPlanets) {
			if (planet.statistics.constructing) {
				continue;
			}
			if (condition.invoke(planet)) {
				if (manageBuildings(planet, selector, order, upgradeFirst)) {
					return true;
				}
			}
		}
		return false;
	}
}
