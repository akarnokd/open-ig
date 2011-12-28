/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Pred0;
import hu.openig.core.Pred1;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.BuildingType;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
		
		List<Pred0> functions = new ArrayList<Pred0>();
		functions.add(new Pred0() {
			@Override
			public Boolean invoke() {
				return checkRadar();
			}
		});
		functions.add(new Pred0() {
			@Override
			public Boolean invoke() {
				return checkEconomy();
			}
		});
		functions.add(new Pred0() {
			@Override
			public Boolean invoke() {
				return checkFactory();
			}
		});
		functions.add(new Pred0() {
			@Override
			public Boolean invoke() {
				return checkSocial();
			}
		});

		// random arbitration
		Collections.shuffle(functions, w.random.get());
		for (Pred0 f : functions) {
			if (f.invoke()) {
				return;
			}
		}
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
			if (bt.hasResource("radar") 
					&& (bt.research == null || world.availableResearch.contains(bt.research))) {
				if (bestRadar == null || bestRadar.getResource("radar") < bt.getResource("radar")) {
					bestRadar = bt;
				}
			}
		}
		ResearchType hubble2 = null;
		for (ResearchType rt : world.availableResearch) {
			if (rt.has("radar") && rt.category == ResearchSubCategory.SPACESHIPS_SATELLITES) {
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
				placeProductionOrder(hubble2, 5);
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
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Check if there is shortage on police.
	 * @return if action taken
	 */
	boolean checkEconomy() {
		BuildingSelector police = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("multiply") || value.hasResource("credit");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return value.hasResource("multiply") || value.hasResource("credit");
			}
		};
		Comparator<AIPlanet> planetOrder = new Comparator<AIPlanet>() {
			@Override
			public int compare(AIPlanet o1, AIPlanet o2) {
				int v1 = o1.statistics.workerDemand - o1.population;
				int v2 = o2.statistics.workerDemand - o2.population;
				return v1 < v2 ? -1 : (v1 > v2 ? 1 : o1.morale - o2.morale);
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.workerDemand * 1.1;
			}
		}, planetOrder, police, costOrder, false);
	}
	/**
	 * Check if there is shortage on police.
	 * @return if action taken
	 */
	boolean checkFactory() {
		BuildingSelector police = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				return value.hasResource("spaceship") || value.hasResource("equipment") || value.hasResource("weapon");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return  value.hasResource("spaceship") || value.hasResource("equipment") || value.hasResource("weapon");
			}
		};
		Comparator<AIPlanet> planetOrder = new Comparator<AIPlanet>() {
			@Override
			public int compare(AIPlanet o1, AIPlanet o2) {
				int v1 = o1.statistics.workerDemand - o1.population;
				int v2 = o2.statistics.workerDemand - o2.population;
				return v1 < v2 ? -1 : (v1 > v2 ? 1 : o1.morale - o2.morale);
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.workerDemand * 1.1;
			}
		}, planetOrder, police, costOrder, false);
	}
	/**
	 * Check if there is shortage on police.
	 * @return if action taken
	 */
	boolean checkSocial() {
		BuildingSelector police = new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding value) {
				
				return value.type.kind.equals("Social") && value.hasResource("morale");
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType value) {
				return  value.kind.equals("Social") && value.hasResource("morale") && limit(planet, value, 1);
			}
		};
		Comparator<AIPlanet> planetOrder = new Comparator<AIPlanet>() {
			@Override
			public int compare(AIPlanet o1, AIPlanet o2) {
				return o1.morale - o2.morale;
			}
		};
		return planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return value.population > value.statistics.workerDemand * 1.1;
			}
		}, planetOrder, police, costOrder, false);
	}
}
