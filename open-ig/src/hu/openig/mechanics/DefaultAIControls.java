/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Location;
import hu.openig.model.AIAttackMode;
import hu.openig.model.AIControls;
import hu.openig.model.AIResult;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.DiplomaticInteraction;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.InventoryItem;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.Production;
import hu.openig.model.Research;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.SelectionMode;
import hu.openig.model.TaxLevel;
import hu.openig.model.TileSet;
import hu.openig.model.World;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

/**
 * The default AI controls.
 * <p>The methods should be executed in EDT!</p>
 * @author akarnokd, 2011.12.29.
 */
public class DefaultAIControls implements AIControls {
	/** The target player. */
	protected final Player p;
	/** The world. */
	protected final World w;
	/**
	 * Constructor. Sets the default command target.
	 * @param player the player
	 */
	public DefaultAIControls(Player player) {
		this.p = player;
		w = p.world;
	}
	@Override
	public void actionStartResearch(ResearchType rt, double moneyFactor) {
		if (p.runningResearch != null) {
			Research r = p.research.get(p.runningResearch);
			r.state = ResearchState.STOPPED;
			log("PauseResearch, Type = %s", r.type.id);
		}
		p.runningResearch = rt;
		Research r = p.research.get(p.runningResearch);
		if (r == null) {
			r = new Research();
			r.type = rt;
			r.remainingMoney = r.type.researchCost;
			r.assignedMoney = (int)(r.type.researchCost * moneyFactor / 2);
			p.research.put(r.type, r);
		} else {
			r.assignedMoney = (int)(r.remainingMoney * moneyFactor / 2);
		}
		r.state = ResearchState.RUNNING;
		log("StartResearch, Type = %s, MoneyFactor = %s", rt.id, moneyFactor);
	}
	@Override
	public void actionDeployFleet(
			Planet location,
			double loadFactor, 
			double powerFactor, 
			Iterable<ResearchType> items) {
		// TODO implement
	}
	@Override
	public Fleet actionCreateFleet(String name, Planet location) {
		Fleet fleet = new Fleet();
		fleet.owner = p;
		fleet.id = w.fleetIdSequence++;
		if (name == null) {
			fleet.name = w.env.labels().get(p.race + ".new_fleet_name");
		} else {
			fleet.name = name;
		}
		fleet.x = location.x;
		fleet.y = location.y;

		p.fleets.put(fleet, FleetKnowledge.FULL);
		log("CreateFleet, Fleet = %s, Planet = %s", fleet.name, location.id);
		return fleet;
	}
	@Override
	public void actionDeploySatellite(Planet planet, ResearchType satellite) {
		if (!actionDeploySatellite(p, planet, satellite)) {
			log("DeploySatellite, Planet = %s, Type = %s, FAILED = not in inventory", planet.id, satellite.id);
//		} else {
//			log("DeploySatellite, Planet = %s, Type = %s", planet.id, satellite.id);
		}
	}
	@Override
	public void actionRemoveProduction(ResearchType rt) {
		actionRemoveProduction(p, rt);
		log("RemoveProduction, Type = %s", rt.id);
	}
	/**
	 * Remove the production line of the given research.
	 * @param player the player
	 * @param rt the research
	 */
	public static void actionRemoveProduction(Player player, ResearchType rt) {
		Map<ResearchType, Production> map = player.production.get(rt.category.main);
		if (map != null) {
			Production prod = map.get(rt);
			if (prod != null) {
				map.remove(rt);
				// update statistics
				int m = prod.progress / 2;
				player.money += m;
				player.statistics.moneyProduction -= m;
				player.statistics.moneySpent -= m;
				player.world.statistics.moneyProduction -= m;
				player.world.statistics.moneySpent -= m;
			}
		}
	}
	/**
	 * Deploy a satellite of the given player to the target planet.
	 * <p>Removes one item from inventory.</p>
	 * @param player the player
	 * @param planet the planet
	 * @param satellite the satellite type
	 * @return true if successful
	 */
	public static boolean actionDeploySatellite(Player player, Planet planet, ResearchType satellite) {
		// decomission any previous satellites:
		if (player.inventoryCount(satellite) > 0) {
			InventoryItem ii = new InventoryItem();
			ii.type = satellite;
			ii.owner = player;
			ii.count = 1;
			ii.hp = player.world.getHitpoints(satellite);
			planet.inventory.add(ii);
			int ttl = player.world.getSatelliteTTL(satellite);
			if (ttl > 0) {
				planet.timeToLive.put(ii, ttl);
			}
			player.changeInventoryCount(satellite, -1);
			return true;
		}
		return false;
	}
	@Override
	public void actionSellSatellite(Planet planet, ResearchType satellite,
			int count) {
		if (planet.inventoryCount(satellite, planet.owner) >= count) {
			planet.changeInventory(satellite, planet.owner, -count);
			
			int money = count * satellite.productionCost / 2; 
			planet.owner.money += money;
			planet.owner.statistics.sellCount += count;
			planet.owner.statistics.moneySellIncome += money;
			planet.owner.statistics.moneyIncome += money;
			planet.owner.world.statistics.sellCount += count;
			planet.owner.world.statistics.moneySellIncome += money;
			planet.owner.world.statistics.moneyIncome += money;
			
//			log("SellSatellite, Planet = %s, Type = %s, Count = %s", planet.id, satellite.id, count);
		}
	}

	@Override
	public void actionStartProduction(ResearchType rt, int count, int priority) {
		Map<ResearchType, Production> prodLine = p.production.get(rt.category.main);
		Production prod = prodLine.get(rt);
		if (prod == null) {
			prod = new Production();
			prod.type = rt;
			prodLine.put(rt, prod);
		}
		prod.priority = priority;
		prod.count += count;
		log("StartProduction, Type = %s, Count = %s, Priority = %s", rt.id, count, priority);
	}
	@Override
	public void actionPauseProduction(ResearchType rt) {
		Map<ResearchType, Production> prodLine = p.production.get(rt.category.main);
		Production prod = prodLine.get(rt);
		if (prod != null) {
			prod.priority = 0;
		}
		log("PauseProduction, Type = %s", rt.id);
	}
	@Override
	public AIResult actionPlaceBuilding(Planet planet, BuildingType buildingType) {
		if (!planet.canBuild(buildingType)) {
			log("PlaceBuilding, Planet = %s, Type = %s, FAIL = not supported or no colony hub", planet.id, buildingType.id);
			return AIResult.NO_AVAIL;
		} else
		if (p.money >= buildingType.cost) {
			Point pt = planet.surface.placement.findLocation(planet.getPlacementDimensions(buildingType));
			if (pt != null) {
				AutoBuilder.construct(w, planet, buildingType, pt);
				log("PlaceBuilding, Planet = %s, Type = %s", planet.id, buildingType.id);
				return AIResult.SUCCESS;
			} else {
				log("PlaceBuilding, Planet = %s, Type = %s, FAIL = no room", planet.id, buildingType.id);
				return AIResult.NO_ROOM;
			}
		} else {
			log("PlaceBuilding, Planet = %s, Type = %s, FAIL = no money", planet.id, buildingType.id);
			return AIResult.NO_MONEY;
		}
	}
	@Override
	public void actionDemolishBuilding(Planet planet, Building b) {
		demolishBuilding(w, planet, b);
		log("DemolishBuilding, Planet = %s, Building = %s", planet.id, b.type.id);
	}
	/**
	 * Demolish a building on the given planet, update statistics and get some money back.
	 * @param world the world object
	 * @param planet the target planet
	 * @param building the target building
	 */
	public static void demolishBuilding(World world, Planet planet, Building building) {
		planet.surface.removeBuilding(building);
		planet.surface.placeRoads(planet.race, world.buildingModel);
		
		int moneyBack = building.type.cost * (1 + building.upgradeLevel) / 2;
		
		planet.owner.money += moneyBack;
		
		planet.owner.statistics.demolishCount++;
		planet.owner.statistics.moneyDemolishIncome += moneyBack;
		planet.owner.statistics.moneyIncome += moneyBack;

		world.statistics.demolishCount++;
		world.statistics.moneyDemolishIncome += moneyBack;
		world.statistics.moneyDemolishIncome += moneyBack;

	}
	@Override
	public void actionUpgradeBuilding(Planet planet, Building building, int newLevel) {
		if (!AutoBuilder.upgrade(w, planet, building, newLevel)) {
			log("UpgradeBuilding, Planet = %s, Building = %s, NewLevel = %s, FAILED = level limit", planet.id, building.type.id, newLevel);
//		} else {
//			log("UpgradeBuilding, Planet = %s, Building = %s, NewLevel = %s", planet.id, building.type.id, newLevel);			
		}
		
	}
	@Override
	public void actionDeployUnits(double loadFactor, double powerFactor, Planet planet, Iterable<ResearchType> items) {
		// TODO implement
	}
	@Override
	public void actionAttackPlanet(Fleet fleet, Planet planet, AIAttackMode mode) {
		// TODO implement
	}
	@Override
	public void actionAttackFleet(Fleet fleet, Fleet enemy, boolean defense) {
		// TODO implement
	}
	@Override
	public void actionMoveFleet(Fleet fleet, double x, double y) {
		fleet.targetFleet = null;
		fleet.targetPlanet(null);
		fleet.waypoints.clear();
		fleet.waypoints.add(new Point2D.Double(x, y));
		fleet.mode = FleetMode.MOVE;
//		log("MoveFleet, Fleet = %s, Location = %s;%s", fleet.name, x, y);
	}
	@Override
	public void actionMoveFleet(Fleet fleet, Planet planet) {
		fleet.targetFleet = null;
		fleet.waypoints.clear();
		fleet.targetPlanet(planet);
		fleet.mode = FleetMode.MOVE;
		log("MoveFleet, Fleet = %s, Planet = %s", fleet.name, planet.id);
	}
	
	@Override
	public void actionColonizePlanet(Fleet fleet, Planet planet) {
		if (fleet.getStatistics().planet == planet) {
			if (colonizeWithFleet(fleet, planet)) {
				log("ColonizePlanet, Fleet = %s, Planet = %s", fleet.name, planet.id);
			} else {
				log("ColonizePlanet, Fleet = %s, Planet = %s, FAILED = no room", fleet.name, planet.id);
			}
		} else {
			log("ColonizePlanet, Fleet = %s, Planet = %s, FAILED = not close enough", fleet.name, planet.id);
		}
	}
	@Override
	public void actionStopResearch(ResearchType rt) {
		Research r = p.research.get(rt);
		if (r != null) {
			r.state = ResearchState.STOPPED;
			if (r.type == p.runningResearch) {
				p.runningResearch = null;
			}
		}
	}
	/**
	 * Apply colonizationc changes to the given planet and reduce the colony ship count in the fleet.
	 * @param f the fleet
	 * @param p the planet
	 * @return true if colonization successful
	 */
	public static boolean colonizeWithFleet(Fleet f, Planet p) {
		World w = f.owner.world;
		for (BuildingType bt : w.buildingModel.buildings.values()) {
			if ("MainBuilding".equals(bt.kind)) {
				TileSet ts = bt.tileset.get(f.owner.race);
				if (ts != null) {
					Point pt = p.surface.placement.findLocation(ts.normal.width + 2, ts.normal.height + 2);
					if (pt != null) {
						// remove colony ship from fleet
						f.changeInventory(w.researches.get("ColonyShip"), -1);
						
						// remove empty fleet
						if (f.inventory.isEmpty()) {
							w.removeFleet(f);
							List<Fleet> of = f.owner.ownFleets();
							if (of.size() > 0) {
								f.owner.currentFleet = of.iterator().next();
							} else {
								f.owner.currentFleet = null;
								f.owner.selectionMode = SelectionMode.PLANET;
							}
						}
						// place building
						Building b = new Building(bt, f.owner.race);
						p.owner = f.owner;
						p.race = f.owner.race;
						p.population = 5000;
						p.morale = 50;
						p.lastMorale = 50;
						p.lastPopulation = 5000;
						b.location = Location.of(pt.x + 1, pt.y - 1);
						
						p.surface.placeBuilding(ts.normal, b.location.x, b.location.y, b);
						p.surface.placeRoads(p.owner.race, w.buildingModel);
						
						p.owner.planets.put(p, PlanetKnowledge.BUILDING);
						p.owner.currentPlanet = p;
						
						p.owner.statistics.planetsColonized++;
						
						// uninstall satellites
						p.removeOwnerSatellites();
						
						return true;
					} else {
						System.err.printf("Could not colonize planet %s, not enough initial space for colony hub of race %s.", p.id, f.owner.race);
					}
				}
			}
		}
		return false;
	}
	@Override
	public void actionDiplomaticInteraction(Player other, DiplomaticInteraction offer) {
		// TODO implement
	}
	
	@Override
	public void actionRepairBuilding(Planet planet, Building building,
			boolean repair) {
		building.repairing = repair;
		log("RepairBuilding, Planet = %s, Building = %s, Repair = %s", planet.id, building.type.id, repair);
	}
	@Override
	public void actionEnableBuilding(Planet planet, Building building,
			boolean enabled) {
		building.enabled = enabled;
		log("EnableBuilding, Planet = %s, Building = %s, Enabled = %s", planet.id, building.type.id, enabled);
	}
	
	@Override
	public void actionSetTaxation(Planet planet, TaxLevel newLevel) {
//		log("SetTaxation, Planet = %s, Level = %s, NewLevel = %s", planet.id, planet.tax, newLevel);
		planet.tax = newLevel;
	}
	
	/**
	 * Display the action log.
	 * @param message the message
	 * @param values the message parameters
	 */
	void log(String message, Object... values) {
		if (p == p.world.player) {
			System.out.printf("AI:%s:", p.id);
			System.out.printf(message, values);
			System.out.println();
		}
	}

}
