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
import hu.openig.model.BuildingType;
import hu.openig.model.ModelUtils;
import hu.openig.model.Planet;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		List<AIPlanet> planets = new ArrayList<>(world.ownPlanets);
		Collections.sort(planets, WORST_PLANET);
		Collections.reverse(planets);
		
		for (AIPlanet p : planets) {
			if (managePlanet(p)) {
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
	public boolean managePlanet(final AIPlanet planet) {
		if (planet.statistics.constructing) {
			return false;
		}
		// compute existing cost of building types on the planet
		long sumEconomy = 0;
		long sumFactory = 0;
		int nEconomy = 0;
		int nFactory = 0;
		for (AIBuilding value : planet.buildings) {
			if (value.hasResource("multiply") || value.hasResource("credit")) {
				nEconomy++;
				sumEconomy += value.type.cost;
			}
			if (hasTechnologyFor(value.type, "spaceship") 
						|| hasTechnologyFor(value.type, "equipment") 
						|| hasTechnologyFor(value.type, "weapon")) {
				nFactory++;
				sumFactory += value.type.cost;
			}
		}
		
		// don't upgrade unless we have a ton of money
		final boolean allowUpgrades = world.money >= 10000 + world.ownPlanets.size() * sumEconomy / Math.max(1, nEconomy);
		final boolean allowUpgrades2 = world.money >= 250000 + world.ownPlanets.size() * sumFactory / Math.max(1, nFactory);
		List<Pred1<AIPlanet>> functions = new ArrayList<>();
		functions.add(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet planet) {
				return checkEconomy(planet, allowUpgrades);
			}
		});
		if (world.money >= 30000 && checkPlanetPreparedness()) {
			functions.add(new Pred1<AIPlanet>() {
				@Override
				public Boolean invoke(AIPlanet planet) {
					return checkFactory(planet, allowUpgrades2);
				}
			});
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

		if (world.money >= 200000 || world.global.planetCount >= 2) {
			// random arbitration
			ModelUtils.shuffle(functions);
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
	 * @return if action taken
	 */
	boolean checkEconomy(AIPlanet planet, final boolean allowUpgrades) {
		BuildingSelector finances = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return allowUpgrades && (value.hasResource("multiply") || value.hasResource("credit"));
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("multiply") || value.hasResource("credit");
			}
		};
		if (planet.population > planet.statistics.workerDemand * 1.1
				&& planet.statistics.energyAvailable > planet.statistics.energyDemand
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
				if (hasTechnologyFor(value, "spaceship")) {
					return !hasSpaceship || hasAll;
				} else
				if (hasTechnologyFor(value, "equipment")) {
					return !hasEquipment || hasAll;
				}
				if (hasTechnologyFor(value, "weapon")) {
					return !hasWeapon || hasAll;
				}
				return false;
			}
		};
		if (planet.population > planet.statistics.workerDemand * 1.1
				&& planet.statistics.energyAvailable > planet.statistics.energyDemand
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
					return true;
				}
				return false;
			}
		};
		if (planet.population > planet.statistics.workerDemand * 1.1
				&& checkPlanetPreparedness()) {
			return manageBuildings(planet, social, costOrder, false);
		}
		return false;
	}
}
