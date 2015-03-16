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
import hu.openig.model.AutoBuild;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.Message;
import hu.openig.model.Planet;
import hu.openig.model.PlanetProblems;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Resource;
import hu.openig.model.World;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The automatic colony builder.
 * @author akarnokd, 2011.12.29.
 */
public final class AutoBuilder {
	/** Singleton. */
	private AutoBuilder() {
		// singleton
	}
	/**
	 * Perform the auto-build if necessary.
	 * @param world the world
	 * @param planet the planet
	 * @param ps the planet statistics
	 */
	public static void performAutoBuild(final World world, final Planet planet, final PlanetStatistics ps) {
		if (planet.autoBuild != AutoBuild.AI) {
			// do not interfere with hubless colonies
			if (ps.constructing 
					|| ps.hasWarning(PlanetProblems.COLONY_HUB)
					|| ps.hasProblem(PlanetProblems.COLONY_HUB)
			) {
				return;
			}			
			// skip worker problematic planets
			if (ps.workerDemand > planet.population()) {
				return;
			}
			// if there is a worker shortage, it may be the root clause
			// if energy shortage
			if (ps.energyAvailable < ps.energyDemand) {
				findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return b.getEnergy() > 0 && planet.owner.money() >= b.type.cost;
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						Resource res = value.resources.get("energy");
						return res != null && res.amount > 0;
					}
				}
				);
				sendIfAutoBuildOffMessage(world, planet);
				return;
			}
		}
		if (planet.autoBuild == AutoBuild.CIVIL) {
			AutoBuild ab = planet.autoBuild;
			int offRequest = 0;
			int buildCount = 0;
			buildCount++;
			// if living space shortage
			if (ps.houseAvailable < planet.population() || (planet.population() <= 5000 && ps.houseAvailable <= 5000)) {
				findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return b.hasResource("house");
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						Resource res = value.resources.get("house");
						return res != null;
					}
				}
				);
				if (planet.autoBuild == AutoBuild.OFF) {
					offRequest++;
					planet.autoBuild = ab;
				}
			}
			buildCount++;
			// if food shortage
			if (ps.foodAvailable < planet.population()) {
				findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return b.hasResource("food");
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						Resource res = value.resources.get("food");
						return res != null;
					}
				}
				);
				if (planet.autoBuild == AutoBuild.OFF) {
					offRequest++;
					planet.autoBuild = ab;
				}
			}
			buildCount++;
			// if hospital shortage
			if (ps.hospitalAvailable < planet.population()) {
				findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return b.hasResource("hospital");
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						Resource res = value.resources.get("hospital");
						return res != null;
					}
				}
				);
				if (planet.autoBuild == AutoBuild.OFF) {
					offRequest++;
					planet.autoBuild = ab;
				}
			}
			buildCount++;
			// if living space shortage
			if (ps.policeAvailable < planet.population()) {
				findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return b.hasResource("police");
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						Resource res = value.resources.get("police");
						return res != null;
					}
				}
				);
				if (planet.autoBuild == AutoBuild.OFF) {
					offRequest++;
					planet.autoBuild = ab;
				}
			}
			buildCount++;
			// build stadium
			if (ps.hasProblem(PlanetProblems.STADIUM) || ps.hasWarning(PlanetProblems.STADIUM)) {
				findOptions(world, planet, 
					new Func1<Building, Boolean>() {
						@Override
						public Boolean invoke(Building b) {
							return b.type.id.equals("Stadium");
						}
					},
					new Func1<BuildingType, Boolean>() {
						@Override
						public Boolean invoke(BuildingType value) {
							return value.id.equals("Stadium");
						}
					}
				);
				if (planet.autoBuild == AutoBuild.OFF) {
					offRequest++;
					planet.autoBuild = ab;
				}
			}
			buildCount++;
			if (ps.hasProblem(PlanetProblems.FIRE_BRIGADE) || ps.hasWarning(PlanetProblems.FIRE_BRIGADE)) {
				findOptions(world, planet, 
					new Func1<Building, Boolean>() {
						@Override
						public Boolean invoke(Building b) {
							return b.type.id.equals("FireBrigade");
						}
					},
					new Func1<BuildingType, Boolean>() {
						@Override
						public Boolean invoke(BuildingType value) {
							return value.id.equals("FireBrigade");
						}
					}
				);
				if (planet.autoBuild == AutoBuild.OFF) {
					offRequest++;
					planet.autoBuild = ab;
				}
			}
			if (offRequest == buildCount) {
				planet.autoBuild = AutoBuild.OFF;
			}
			sendIfAutoBuildOffMessage(world, planet);
		} else
		if (planet.autoBuild == AutoBuild.ECONOMIC) {
			AutoBuild ab = planet.autoBuild;
			if (!findOptions(world, planet, 
					new Func1<Building, Boolean>() {
						@Override
						public Boolean invoke(Building b) {
							return false;
						}
					},
					new Func1<BuildingType, Boolean>() {
						@Override
						public Boolean invoke(BuildingType value) {
							return value.kind.equals("Economic");
						}
					}
				)) {
					planet.autoBuild = ab;
					findOptions(world, planet, 
						new Func1<Building, Boolean>() {
							@Override
							public Boolean invoke(Building b) {
								return b.type.kind.equals("Economic");
							}
						},
						new Func1<BuildingType, Boolean>() {
							@Override
							public Boolean invoke(BuildingType value) {
								return false;
							}
						}
					);
				}
			sendIfAutoBuildOffMessage(world, planet);
		} else
		if (planet.autoBuild == AutoBuild.FACTORY) {
			// save mode, because construction failure will turn off the status unnecessary
			AutoBuild ab = planet.autoBuild;
			// construct first before upgrading
			if (!findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return false;
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						return value.kind.equals("Factory");
					}
				}
			)) {
				planet.autoBuild = ab;
				findOptions(world, planet, 
					new Func1<Building, Boolean>() {
						@Override
						public Boolean invoke(Building b) {
							return b.type.kind.equals("Factory");
						}
					},
					new Func1<BuildingType, Boolean>() {
						@Override
						public Boolean invoke(BuildingType value) {
							return false;
						}
					}
				);
			}
			sendIfAutoBuildOffMessage(world, planet);
		} else
		if (planet.autoBuild == AutoBuild.UPGRADE) {
			findOptions(world, planet, 
					new Func1<Building, Boolean>() {
						@Override
						public Boolean invoke(Building b) {
							return !b.isDamaged() && b.canUpgrade();
						}
					},
					new Func1<BuildingType, Boolean>() {
						@Override
						public Boolean invoke(BuildingType value) {
							return false;
						}
					}
				);
			sendIfAutoBuildOffMessage(world, planet);
		} else
		if (planet.autoBuild == AutoBuild.SOCIAL) {
			findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return b.type.kind.equals("Social");
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						return value.kind.equals("Social") && planet.countBuilding(value) == 0;
					}
				}
			);
			sendIfAutoBuildOffMessage(world, planet);
		}
	}
	/**
	 * Apply actions.
	 * @param actions the actions
	 * @return true if actions was non-empty
	 */
	static boolean applyAI(List<Action0> actions) {
		for (Action0 a : actions) {
			a.invoke();
		}
		return !actions.isEmpty();
	}
	/**
	 * Find and invoke options for the given world and planet and use
	 * the building upgrade and build selectors to find suitable buildings.
	 * @param world the world
	 * @param planet the planet
	 * @param upgradeSelector the selector to find upgradable buildings
	 * @param buildSelector the selector to find building types to construct
	 * @return was there a construction/upgrade?
	 */
	static boolean findOptions(World world, Planet planet, 
			Func1<Building, Boolean> upgradeSelector, 
			Func1<BuildingType, Boolean> buildSelector) {
		List<Building> upgr = findUpgradables(planet, upgradeSelector);
		if (upgr.size() > 0) {
			doUpgrade(world, planet, upgr.get(0));
			return true;
		}
		// look for energy producing buildings in the model
		List<BuildingType> bts = findBuildables(world, planet, buildSelector);
		// build the most costly building if it can be placed
		for (BuildingType bt : bts) {
			Point pt = planet.surface.placement.findLocation(planet.getPlacementDimensions(bt));
			if (pt != null) {
				construct(world, planet, bt, pt);
				return true;
			}
		}
		// no room at all, turn off autobuild and let the user do it
		if (bts.size() > 0) {
			planet.autoBuild = AutoBuild.OFF;
		}
		return false;
	}
	/**
	 * Locate buildings which satisfy a given filter and have available upgrade levels.
	 * @param planet the target planet
	 * @param filter the building filter
	 * @return the list of potential buildings
	 */
	static List<Building> findUpgradables(Planet planet, Func1<Building, Boolean> filter) {
		List<Building> result = new ArrayList<>();
		for (Building b : planet.surface.buildings.iterable()) {
			if (b.upgradeLevel < b.type.upgrades.size()
				&& b.type.cost <= planet.owner.money()
				&& filter.invoke(b)
			) {
				result.add(b);
			}
		}
		return result;
	}
	/**
	 * Locate building types which satisfy a given filter.
	 * @param world the world
	 * @param planet the target planet
	 * @param filter the building filter
	 * @return the list of potential building types
	 */
	static List<BuildingType> findBuildables(World world, Planet planet, Func1<BuildingType, Boolean> filter) {
		List<BuildingType> result = new ArrayList<>();
		for (BuildingType bt : world.buildingModel.buildings.values()) {
			if (
					planet.owner.money() >= bt.cost
					&& planet.canBuild(bt)
					&& filter.invoke(bt)
			) {
				result.add(bt);
			}
		}
		Collections.sort(result, new Comparator<BuildingType>() {
			@Override
			public int compare(BuildingType o1, BuildingType o2) {
				return o2.cost - o1.cost;
			}
		});
		return result;
	}
	/**
	 * Send a message if the auto-build was turned off.
	 * @param world the world object
	 * @param planet the planet object
	 */
	static void sendIfAutoBuildOffMessage(World world, Planet planet) {
		if (planet.autoBuild == AutoBuild.OFF) {
			Message msg = world.newMessage("autobuild.no_more_building_space");
			msg.priority = 50;
			msg.targetPlanet = planet;
			
			planet.owner.addMessage(msg);
		}
	}
	/**
	 * Construct a building on the given planet.
	 * <p>Note that the build order does not check for money or placement requirements.</p>
	 * @param world the world for the model
	 * @param planet the target planet
	 * @param bt the building type to build
	 * @param pt the place to build (the top-left corner of the roaded building base rectangle).
	 */
	public static void construct(World world, Planet planet, BuildingType bt,
			Point pt) {
		planet.build(bt.id, planet.race, pt.x + 1, pt.y - 1);
	}
	/**
	 * Increase the level of the given building by one.
	 * @param world the world for the models
	 * @param planet the target planet
	 * @param b the building to upgrade
	 */
	static void doUpgrade(World world, Planet planet, Building b) {
		do {
			// maximize upgrade level if the player has enough money relative to the building's cost
			if (!upgrade(world, planet, b, b.upgradeLevel + 1)) {
				break;
			}
		} while (b.upgradeLevel < b.type.upgrades.size() && planet.owner.money() >= 30L * b.type.cost);
	}
	/**
	 * Update the building to the given new level.
	 * @param world the world
	 * @param planet the planet
	 * @param building the building
	 * @param newLevel the new level
	 * @return true if the upgrade was successful
	 */
	public static boolean upgrade(World world, Planet planet, Building building, int newLevel) {
		return planet.upgrade(building, newLevel);
	}
}
