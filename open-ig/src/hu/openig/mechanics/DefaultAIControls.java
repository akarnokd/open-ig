/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.AIAttackMode;
import hu.openig.model.AIControls;
import hu.openig.model.AIResult;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.Production;
import hu.openig.model.Research;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.TaxLevel;
import hu.openig.model.World;

import java.awt.Point;

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
		Research r = p.startResearch(rt);
		r.setMoneyFactor(moneyFactor);
//		log("StartResearch, Type = %s, MoneyFactor = %s", rt.id, moneyFactor);
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
		Fleet fleet = new Fleet(p);
		if (name == null) {
			fleet.name(w.env.labels().get(p.race + ".new_fleet_name"));
		} else {
			fleet.name(name);
		}
		fleet.x = location.x;
		fleet.y = location.y;

		p.fleets.put(fleet, FleetKnowledge.FULL);
		log(p, "CreateFleet, Fleet = %s, Planet = %s", fleet.name(), location.id);
		return fleet;
	}
	@Override
	public void actionDeploySatellite(Planet planet, ResearchType satellite) {
		if (!actionDeploySatellite(p, planet, satellite)) {
			log(p, "DeploySatellite, Planet = %s, Type = %s, FAILED = not in inventory", planet.id, satellite.id);
//		} else {
//			log("DeploySatellite, Planet = %s, Type = %s", planet.id, satellite.id);
		}
	}
	@Override
	public void actionRemoveProduction(ResearchType rt) {
		actionRemoveProduction(p, rt);
//		log("RemoveProduction, Type = %s", rt.id);
	}
	/**
	 * Remove the production line of the given research.
	 * @param player the player
	 * @param rt the research
	 */
	public static void actionRemoveProduction(Player player, ResearchType rt) {
		player.removeProduction(rt);
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
			int current = planet.inventoryCount(satellite.category, player);
			if (satellite.category == ResearchSubCategory.SPACESHIPS_STATIONS
					&& current >= player.world.params().stationLimit()) {
				return false;
			}
			InventoryItem ii = new InventoryItem(planet.world.newId(), player, satellite);
			ii.init();
			ii.count = 1;
			planet.inventory.add(ii);
			int ttl = player.world.getSatelliteTTL(satellite);
			if (ttl > 0) {
				planet.timeToLive.put(ii, ttl);
			}
			player.changeInventoryCount(satellite, -1);
			
			player.world.scripting.onDeploySatellite(planet, player, satellite);
			
			return true;
		}
		return false;
	}
	/**
	 * Deploy a satellite of the given player to the target planet.
	 * <p>Removes one item from inventory.</p>
	 * @param player the player
	 * @param planet the planet
	 * @param fighter the satellite type
	 * @param count the number of fighters.
	 * @return true if successful
	 */
	public static boolean actionDeployFighters(Player player, Planet planet, ResearchType fighter, int count) {
		// decomission any previous satellites:
		if (player.inventoryCount(fighter) >= count) {
			
			InventoryItem ii = planet.getInventoryItem(fighter, player);
			if (ii == null) {
				if (count > player.world.params().fighterLimit()) {
					return false;
				}
				ii = new InventoryItem(planet.world.newId(), player, fighter);
				ii.count = count;
				ii.init();
				planet.inventory.add(ii);
				int ttl = player.world.getSatelliteTTL(fighter);
				if (ttl > 0) {
					planet.timeToLive.put(ii, ttl);
				}
				player.changeInventoryCount(fighter, -count);
				
				player.world.scripting.onDeploySatellite(planet, player, fighter);
			} else {
				if (ii.count + count <= player.world.params().fighterLimit()) {
					ii.count += count;
				} else {
					return false;
				}
			}
			
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
			planet.owner.addMoney(money);
			planet.owner.statistics.sellCount.value += count;
			planet.owner.statistics.moneySellIncome.value += money;
			planet.owner.statistics.moneyIncome.value += money;
			planet.owner.world.statistics.sellCount.value += count;
			planet.owner.world.statistics.moneySellIncome.value += money;
			planet.owner.world.statistics.moneyIncome.value += money;
			
//			log("SellSatellite, Planet = %s, Type = %s, Count = %s", planet.id, satellite.id, count);
		}
	}

	@Override
	public void actionStartProduction(ResearchType rt, int count, int priority) {
		actionStartProduction(p, rt, count, priority);
	}
	/**
	 * Start or change production counts of a specific technology.
	 * @param p the player
	 * @param rt the technology
	 * @param count the count delta
	 * @param priority the new priority
	 * @return true if the action succeeded
	 */
	public static boolean actionStartProduction(Player p, ResearchType rt, int count, int priority) {
		Production prod = p.addProduction(rt);
		if (prod != null) {
			prod.priority = priority;
			prod.count = Math.max(prod.count + count, 0);
			return true;
		}
		return false;
	}
	@Override
	public void actionPauseProduction(ResearchType rt) {
		Production pr = p.getProduction(rt);
		if (pr != null) {
			pr.priority = 0;
		}
	}
	@Override
	public AIResult actionPlaceBuilding(Planet planet, BuildingType buildingType) {
		if (!planet.canBuild(buildingType)) {
			log(p, "PlaceBuilding, Planet = %s, Type = %s, FAIL = not supported or no colony hub", planet.id, buildingType.id);
			return AIResult.NO_AVAIL;
		} else
		if (p.money() >= buildingType.cost) {
			Point pt = planet.surface.placement.findLocation(planet.getPlacementDimensions(buildingType));
			if (pt != null) {
				AutoBuilder.construct(w, planet, buildingType, pt);
//				log("PlaceBuilding, Planet = %s, Type = %s", planet.id, buildingType.id);
				return AIResult.SUCCESS;
			}
			log(p, "PlaceBuilding, Planet = %s, Type = %s, FAIL = no room", planet.id, buildingType.id);
			return AIResult.NO_ROOM;
		} else {
			log(p, "PlaceBuilding, Planet = %s, Type = %s, FAIL = no money", planet.id, buildingType.id);
			return AIResult.NO_MONEY;
		}
	}
	@Override
	public void actionDemolishBuilding(Planet planet, Building b) {
		planet.demolish(b);
//		log("DemolishBuilding, Planet = %s, Building = %s", planet.id, b.type.id);
	}
	@Override
	public void actionUpgradeBuilding(Planet planet, Building building, int newLevel) {
		if (!AutoBuilder.upgrade(w, planet, building, newLevel)) {
			log(p, "UpgradeBuilding, Planet = %s, Building = %s, NewLevel = %s, FAILED = level limit", planet.id, building.type.id, newLevel);
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
		fleet.attack(planet);
		log(p, "AttackPlanet, Attacker = %s, Defender = %s", fleet.name(), planet.name());
	}
	@Override
	public void actionAttackFleet(Fleet fleet, Fleet enemy, boolean defense) {
		fleet.attack(enemy);
		log(p, "AttackFleet, Attacker = %s, Defender = %s", fleet.name(), enemy.name());
	}
	@Override
	public void actionMoveFleet(Fleet fleet, double x, double y) {
		if (fleet.task != FleetTask.SCRIPT) {
			fleet.moveTo(x, y);
//		log("MoveFleet, Fleet = %s, Location = %s;%s", fleet.name, x, y);
		}
	}
	@Override
	public void actionMoveFleet(Fleet fleet, Planet planet) {
		if (fleet.task != FleetTask.SCRIPT) {
			fleet.moveTo(planet);
			log(p, "MoveFleet, Fleet = %s (%d), Planet = %s", fleet.name(), fleet.id, planet.id);
		}
	}
	
	@Override
	public void actionColonizePlanet(Fleet fleet, Planet planet) {
		if (fleet.getStatistics().planet == planet) {
			if (fleet.colonize(planet)) {
				log(p, "ColonizePlanet, Fleet = %s, Planet = %s", fleet.name(), planet.id);
			} else {
				log(p, "ColonizePlanet, Fleet = %s, Planet = %s, FAILED = no room", fleet.name(), planet.id);
			}
		} else {
			log(p, "ColonizePlanet, Fleet = %s, Planet = %s, FAILED = not close enough", fleet.name(), planet.id);
		}
	}
	@Override
	public void actionStopResearch(ResearchType rt) {
		p.stopResearch(rt);
	}
	
	@Override
	public void actionRepairBuilding(Planet planet, Building building,
			boolean repair) {
		building.repairing = repair;
		log(p, "RepairBuilding, Planet = %s, Building = %s, Repair = %s", planet.id, building.type.id, repair);
	}
	@Override
	public void actionEnableBuilding(Planet planet, Building building,
			boolean enabled) {
		building.enabled = enabled;
		log(p, "EnableBuilding, Planet = %s, Building = %s, Enabled = %s", planet.id, building.type.id, enabled);
	}
	
	@Override
	public void actionSetTaxation(Planet planet, TaxLevel newLevel) {
//		log("SetTaxation, Planet = %s, Level = %s, NewLevel = %s", planet.id, planet.tax, newLevel);
		planet.tax = newLevel;
	}
	
	/**
	 * Display the action log.
	 * @param p the player
	 * @param message the message
	 * @param values the message parameters
	 */
	static void log(Player p, String message, Object... values) {
		if (p == p.world.player) {
			System.out.printf("AI:%s:", p.id);
			System.out.printf(message, values);
			System.out.println();
		}
	}

}
