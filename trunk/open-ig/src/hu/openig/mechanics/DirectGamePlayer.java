/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.AutoBuild;
import hu.openig.model.BattleStatus;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.EmpireStatuses;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetStatus;
import hu.openig.model.FleetTransferMode;
import hu.openig.model.GameAPI;
import hu.openig.model.GroundwarUnitStatus;
import hu.openig.model.HasInventory;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventoryItemStatus;
import hu.openig.model.InventorySlotStatus;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetStatus;
import hu.openig.model.Player;
import hu.openig.model.Production;
import hu.openig.model.ProductionStatuses;
import hu.openig.model.Research;
import hu.openig.model.ResearchStatuses;
import hu.openig.model.ResearchType;
import hu.openig.model.SpaceBattleUnit;
import hu.openig.model.TaxLevel;
import hu.openig.model.World;
import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Game API which executes requests
 * for a given player.
 * <p>Note that the methods should be executed on the EDT,
 * where the world data lives.</p>
 * @author akarnokd, 2013.05.04.
 */
public class DirectGamePlayer implements GameAPI {
	/** The world object. */
	protected final World world;
	/** The player object. */
	protected final Player player;
	/** The id of the last fleet created. */
	protected int lastFleet = -1;
	/** The id of the last fleet inventory created. */
	protected int lastFleetInventoryItem = -1;
	/** The id of the last planet inventory created. */
	protected int lastPlanetInventoryItem = -1;
	/** The last deployed building id. */
	protected int lastBuilding = -1;
	/**
	 * Constructor, sets the player object.
	 * @param player the player
	 */
	public DirectGamePlayer(Player player) {
		this.player = player;
		this.world = player.world;
	}

	@Override
	public EmpireStatuses getEmpireStatuses() throws IOException {
		return world.toEmpireStatuses(player.id);
	}

	@Override
	public List<FleetStatus> getFleets() throws IOException {
		List<FleetStatus> result = new ArrayList<>(player.fleets.size() + 1);
		
		for (Fleet f : player.fleets.keySet()) {
			result.add(f.toFleetStatus());
		}
		
		return result;
	}

	@Override
	public FleetStatus getFleet(int fleetId) throws IOException {
		Fleet f = world.fleet(fleetId);
		if (f != null) {
			if (player.knowledge(f, FleetKnowledge.VISIBLE) >= 0) { 
				return f.toFleetStatus();
			}
		}
		ErrorType.UNKNOWN_FLEET.raise();
		return null;
	}

	@Override
	public Map<String, Integer> getInventory() throws IOException {
		HashMap<String, Integer> result = new HashMap<>();
		for (Map.Entry<ResearchType, Integer> e : player.inventory.entrySet()) {
			result.put(e.getKey().id, e.getValue());
		}
		return result;
	}

	@Override
	public ProductionStatuses getProductions() throws IOException {
		return player.toProductionStatuses();
	}

	@Override
	public ResearchStatuses getResearches() throws IOException {
		return player.toResearchStatuses();
	}

	@Override
	public List<PlanetStatus> getPlanetStatuses() throws IOException {
		List<PlanetStatus> result = new ArrayList<>();
		for (Map.Entry<Planet, PlanetKnowledge> pe : player.planets.entrySet()) {
			result.add(pe.getKey().toPlanetStatus(player));
		}
		return result;
	}

	@Override
	public PlanetStatus getPlanetStatus(String id) throws IOException {
		Planet p = world.planet(id);
		if (p !=  null && player.knowledge(p, PlanetKnowledge.VISIBLE) >= 0) {
			return p.toPlanetStatus(player);
		}
		ErrorType.UNKNOWN_PLANET.raise(id);
		return null;
	}

	/**
	 * Verify the existence and accessibility of the given fleet.
	 * @param id the fleet id
	 * @return the fleet object or null to do nothing
	 * @throws ErrorResponse on error
	 */
	protected Fleet fleetCheck(int id) throws ErrorResponse {
		if (id < 0) {
			id = lastFleet;
		}
		Fleet f = world.fleet(id);
		if (f == null) {
			f = player.fleet(id);
		}
		if (f == null) {
			ErrorType.UNKNOWN_FLEET.raise("" + id);
		}
		if (f.owner != null) {
			ErrorType.NOT_YOUR_FLEET.raise("" + id);
		}
		if (world.scripting.mayControlFleet(f)) {
			return f;
		}
		return null;
	}
	/**
	 * Check if the given planet exists and belongs to the player.
	 * @param id the planet id
	 * @return the planet object
	 * @throws ErrorResponse on error
	 */
	protected Planet checkPlanet(String id) throws ErrorResponse {
		Planet p = world.planet(id);
		if (p != null) {
			if (p.owner == player) {
				return p;
			}
			if (player.knowledge(p, PlanetKnowledge.OWNER) >= 0) {
				ErrorType.NOT_YOUR_PLANET.raise(id);
			}
		}
		ErrorType.UNKNOWN_PLANET.raise(id);
		return null;
	}
	@Override
	public void moveFleet(int id, double x, double y) throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			f.moveTo(x, y);
		}
	}
	
	@Override
	public void addFleetWaypoint(int id, double x, double y) throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			f.addWaypoint(x, y);
		}
	}

	@Override
	public void moveToPlanet(int id, String target) throws IOException {
		Fleet f = fleetCheck(id);
		Planet p = world.planet(target);
		if (p == null || player.knowledge(p, PlanetKnowledge.VISIBLE) < 0) {
			ErrorType.UNKNOWN_PLANET.raise(target);
		}
		if (f != null) {
			f.moveTo(p);
		}
	}

	@Override
	public void followFleet(int id, int target) throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			Fleet f2 = world.fleet(target);
			if (f2 == null || player.knowledge(f2, FleetKnowledge.VISIBLE) < 0) {
				ErrorType.UNKNOWN_FLEET.raise("" + target);
			}
			if (f != f2) {
				f.follow(f2);
			}
		}

	}

	@Override
	public void attackFleet(int id, int target) throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			Fleet f2 = world.fleet(target);
			if (f2 == null || player.knowledge(f2, FleetKnowledge.VISIBLE) < 0) {
				ErrorType.UNKNOWN_FLEET.raise("" + target);
			}
			if (f == f2 || f.owner == f2.owner || player.isStrongAlliance(f2.owner)) {
				ErrorType.FRIENDLY_FLEET.raise("" + target);
			}
			f.attack(f2);
		}
	}

	@Override
	public void attackPlanet(int id, String target) throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			Planet p = world.planet(target);
			if (p == null || p.owner == null || player.knowledge(p, PlanetKnowledge.OWNER) < 0) {
				ErrorType.UNKNOWN_PLANET.raise(target);
			}
			if (player.isStrongAlliance(p.owner)) {
				ErrorType.FRIENDLY_PLANET.raise("" + target);
			}
			f.attack(p);
		}
	}

	@Override
	public void colonize(int id, String target) throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			Planet p = world.planet(target);
			if (p == null || player.knowledge(p, PlanetKnowledge.OWNER) < 0) {
				ErrorType.UNKNOWN_PLANET.raise(target);
				return;
			}
			if (f.nearbyPlanet() != p) {
				ErrorType.NO_PLANET_NEARBY.raise(target);
				return;
			}
			if (p.owner != null) {
				ErrorType.PLANET_OCCUPIED.raise("" + target);
				return;
			}
			f.colonize(p);
		}
		
	}
	/**
	 * Add the given ships and equipment to the fleet, deducing from
	 * the player's inventory.
	 * @param f the target fleet
	 * @param inventory the inventory settings
	 */
	protected void setupFleet(Fleet f, List<InventoryItemStatus> inventory) {
		for (InventoryItemStatus iis : inventory) {
			ResearchType rt = world.research(iis.type);
			if (rt != null) {
				for (InventoryItem ii : f.deployItem(rt, f.owner, iis.count)) {
					ii.tag = iis.tag;
					for (InventorySlotStatus iss : iis.slots) {
						ResearchType rt0 = world.research(iss.type);
						if (rt0 != null) {
							ii.deployEquipment(iss.id, rt0, iss.count);
						}
					}
				}
			}
		}
	}
	
	@Override
	public int newFleet(String planet, List<InventoryItemStatus> inventory)
			throws IOException {
		Planet p = world.planet(planet);
		if (p == null || player.knowledge(p, PlanetKnowledge.VISIBLE) < 0) {
			ErrorType.UNKNOWN_PLANET.raise(planet);
		} else
		if (p.owner == player) {
			ErrorType.NOT_YOUR_PLANET.raise(planet);
		} else
		if (!p.hasMilitarySpaceport()) {
			ErrorType.NO_SPACEPORT.raise(planet);
		}
		Fleet f = p.newFleet();
		lastFleet = f.id;
		setupFleet(f, inventory);
		return f.id;
	}

	@Override
	public int newFleet(int id, List<InventoryItemStatus> inventory)
			throws IOException {
		Fleet f0 = fleetCheck(id);
		if (f0 != null) {
			Fleet f = f0.newFleet();
			lastFleet = f.id;
			setupFleet(f, inventory);
			return f.id;
		}
		ErrorType.CANT_CREATE_FLEET.raise();
		return 0;
	}

	@Override
	public void deleteFleet(int id) throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			if (f.inventory.isEmpty()) {
				world.removeFleet(f);
			} else {
				ErrorType.FLEET_ISNT_EMPTY.raise("" + id);
			}
		}
	}

	@Override
	public void renameFleet(int id, String name) throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			f.name(name);
		}
	}
	/**
	 * Sell the given item from the inventory-having object.
	 * @param inv the inventory
	 * @param itemId the item id
	 * @throws IOException if the item can't be found
	 */
	protected void sellItem(HasInventory inv, int itemId) throws IOException {
		if (inv.contains(itemId)) {
			inv.sell(itemId, 1);
		} else {
			ErrorType.UNKNOWN_INVENTORY_ITEM.raise("" + itemId);
		}
	}
	/**
	 * Deploy a new inventory item.
	 * @param inv the inventory-having object
	 * @param type the technology
	 * @return the inventory id
	 * @throws IOException if the technology is missing or the deploy failed
	 */
	protected int deployItem(HasInventory inv, String type) throws IOException {
		ResearchType rt = world.research(type);
		if (rt != null && player.isAvailable(rt)) {
			if (inv.canDeploy(rt)) {
				List<InventoryItem> iil = inv.deployItem(rt, player, 1);
				for (InventoryItem ii : iil) {
					return ii.id;
				}
			}
			ErrorType.CANT_DEPLOY_INVENTORY.raise(type);
		} else {
			ErrorType.UNKNOWN_RESEARCH.raise(type);
		}
		return 0;
	}
	/**
	 *  Undeploy one item from the inventory.
	 * @param inv the inventory-having object.
	 * @param itemId the item id
	 * @throws IOException if the item can't be undeployed or is missing.
	 */
	protected void undeployItem(HasInventory inv, int itemId) throws IOException {
		InventoryItem ii = inv.inventory().findById(itemId);
		if (ii != null) {
			if (inv.canUndeploy(ii.type)) {
				inv.undeployItem(itemId, 1);
			} else {
				ErrorType.CANT_UNDEPLOY_INVENTORY.raise();
			}
		} else {
			ErrorType.UNKNOWN_INVENTORY_ITEM.raise("" + itemId);
		}
	}
	@Override
	public void sellFleetItem(int id, int itemId) throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			sellItem(f, checkLast(itemId, lastFleetInventoryItem));
		}
	}
	/**
	 * Returns the lastValue if value is less than zero, or the value if it is non-negative.
	 * @param value the value to check
	 * @param lastValue the last value if value is negative
	 * @return the checked value
	 */
	protected int checkLast(int value, int lastValue) {
		return value < 0 ? lastValue : value;
	}
	
	@Override
	public int deployFleetItem(int id, String type)
			throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			int r = deployItem(f, type);
			lastFleetInventoryItem = r;
			return r;
		}
		ErrorType.CANT_DEPLOY_INVENTORY.raise(type);
		return 0;
	}

	@Override
	public void undeployFleetItem(int id, int itemId) throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			undeployItem(f, checkLast(itemId, lastFleetInventoryItem));
		}		
	}
	/**
	 * Adds an equipment to the inventory-having object's inventory item.
	 * @param inv the inventory-having object
	 * @param itemId the inventory item id
	 * @param slotId the slot id
	 * @param type the technology
	 * @throws IOException on invalid equipment, technology or item
	 */
	protected void addEquipment(HasInventory inv, int itemId, String slotId, String type) throws IOException {
		InventoryItem ii = inv.inventory().findById(itemId);
		if (ii != null) {
			ResearchType rt = world.research(type);
			if (rt != null && player.isAvailable(rt)) {
				if (ii.canDeployEquipment(slotId, rt)) {
					ii.deployEquipment(slotId, rt, 1);
				} else {
					ErrorType.UNKNOWN_EQUIPMENT.raise(slotId);
				}
			} else {
				ErrorType.UNKNOWN_RESEARCH.raise(type);
			}
		} else {
			ErrorType.UNKNOWN_INVENTORY_ITEM.raise("" + itemId);
		}
	}
	/**
	 * Removes an equipment from the target inventory-having objects inventory.
	 * @param inv the inventory-having object
	 * @param itemId the inventory item id
	 * @param slotId the slot id
	 * @throws IOException if the slot can't be undeployed or the inventory item is missing
	 */
	protected void removeEquipment(HasInventory inv, int itemId, String slotId) throws IOException {
		InventoryItem ii = inv.inventory().findById(itemId);
		if (ii != null) {
			if (ii.canUndeployEquipment(slotId)) {
				ii.undeployEquipment(slotId, 1);
			} else {
				ErrorType.UNKNOWN_EQUIPMENT.raise(slotId);
			}
		} else {
			ErrorType.UNKNOWN_INVENTORY_ITEM.raise("" + itemId);
		}
	}
	
	@Override
	public void addFleetEquipment(int id, int itemId, String slotId, String type)
			throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			addEquipment(f, checkLast(itemId, lastFleetInventoryItem), slotId, type);
		}
	}

	@Override
	public void removeFleetEquipment(int id, int itemId, String slotId)
			throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			removeEquipment(f, checkLast(itemId, lastFleetInventoryItem), slotId);
		}

	}

	@Override
	public void fleetUpgrade(int id) throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			f.upgradeAll();
		}
	}

	@Override
	public void stopFleet(int id) throws IOException {
		Fleet f = fleetCheck(id);
		if (f != null) {
			f.stop();
		}
	}

	@Override
	public void transfer(int sourceFleet, int destinationFleet, int sourceItem,
			FleetTransferMode mode) throws IOException {
		Fleet f = fleetCheck(sourceFleet);
		if (f != null) {
			Fleet f2 = fleetCheck(destinationFleet);
			if (f2 != null && f != f2) {
				InventoryItem ii = f2.inventory().findById(checkLast(sourceItem, lastFleetInventoryItem));
				if (ii != null) {
					f.transferTo(f2, sourceItem, mode);
				} else {
					ErrorType.UNKNOWN_INVENTORY_ITEM.raise("" + sourceItem);
				}
			}
		}
	}

	@Override
	public void colonize(String id) throws IOException {
		Planet p = world.planet(id);
		if (p == null || player.knowledge(p, PlanetKnowledge.OWNER) < 0) {
			ErrorType.UNKNOWN_PLANET.raise(id);
			return;
		}
		if (p.owner != null) {
			ErrorType.PLANET_OCCUPIED.raise(id);
			return;
		}
		player.colonizationTargets.add(id);
	}

	@Override
	public void cancelColonize(String id) throws IOException {
		player.colonizationTargets.remove(id);
	}

	@Override
	public int build(String planetId, String type, String race, int x, int y)
			throws IOException {
		Planet p = checkPlanet(planetId);
		
		BuildingType bt = world.building(type);
		if (bt == null) {
			ErrorType.UNKNOWN_BUILDING.raise(type);
			return 0;
		}
		if (!bt.tileset.containsKey(race)) {
			ErrorType.UNKNOWN_BUILDING_RACE.raise(type + " " + race);
			return 0;
		}
		if (!p.canBuild(type)) {
			ErrorType.CANT_BUILD.raise(type);
			return 0;
		}
		if (!p.canPlace(type, race, x, y)) {
			ErrorType.CANT_PLACE_BUILDING.raise(x + ", " + y);
			return 0;
		}
		
		Building bid = p.build(type, race, x, y);
		lastBuilding = bid.id;
		return bid.id;
	}

	@Override
	public int build(String planetId, String type, String race)
			throws IOException {
		Planet p = checkPlanet(planetId);
		
		BuildingType bt = world.building(type);
		if (bt == null) {
			ErrorType.UNKNOWN_BUILDING.raise(type);
			return 0;
		}
		if (!bt.tileset.containsKey(race)) {
			ErrorType.UNKNOWN_BUILDING_RACE.raise(type + " " + race);
			return 0;
		}
		if (!p.canBuild(type)) {
			ErrorType.CANT_BUILD.raise(type);
			return 0;
		}
		Building bid = p.build(type, race);
		if (bid != null) {
			lastBuilding = bid.id;
			return bid.id;
		}
		ErrorType.NOT_ENOUGH_ROOM.raise(type);
		return 0;
	}

	@Override
	public void setAutoBuild(String planetId, AutoBuild auto)
			throws IOException {
		if (planetId != null) {
			Planet p = checkPlanet(planetId);
			p.autoBuild = auto;
		} else {
			for (Planet p : player.ownPlanets()) {
				p.autoBuild = auto;
			}
		}
	}
	@Override
	public void setTaxLevel(String planetId, TaxLevel tax) throws IOException {
		if (planetId != null) {
			Planet p = checkPlanet(planetId);
			p.tax = tax;
		} else {
			for (Planet p : player.ownPlanets()) {
				p.tax = tax;
			}
		}
	}
	/**
	 * Returns the building from the planet.
	 * @param p the planet
	 * @param id the building id, or -1 for the last built building id.
	 * @return the building
	 */
	protected Building building(Planet p, int id) {
		if (id < 0) {
			id = lastBuilding;
		}
		return p.findBuilding(id);
	}
	@Override
	public void enable(String planetId, int id) throws IOException {
		Planet p = checkPlanet(planetId);
		Building b = building(p, id);
		if (b != null) {
			b.enabled = true;
		} else {
			ErrorType.UNKNOWN_BUILDING.raise("" + id);
		}
	}

	@Override
	public void disable(String planetId, int id) throws IOException {
		Planet p = checkPlanet(planetId);
		Building b = building(p, id);
		if (b != null) {
			b.enabled = false;
		} else {
			ErrorType.UNKNOWN_BUILDING.raise("" + id);
		}
	}

	@Override
	public void repair(String planetId, int id) throws IOException {
		Planet p = checkPlanet(planetId);
		Building b = building(p, id);
		if (b != null) {
			b.repairing = true;
		} else {
			ErrorType.UNKNOWN_BUILDING.raise("" + id);
		}
	}

	@Override
	public void repairOff(String planetId, int id) throws IOException {
		Planet p = checkPlanet(planetId);
		Building b = building(p, id);
		if (b != null) {
			b.repairing = false;
		} else {
			ErrorType.UNKNOWN_BUILDING.raise("" + id);
		}
	}

	@Override
	public void demolish(String planetId, int id) throws IOException {
		Planet p = checkPlanet(planetId);
		Building b = building(p, id);
		if (b != null) {
			p.demolish(b);
		}
		ErrorType.UNKNOWN_BUILDING.raise("" + id);
	}

	@Override
	public void buildingUpgrade(String planetId, int id, int level)
			throws IOException {
		Planet p = checkPlanet(planetId);
		Building b = building(p, id);
		if (b != null) {
			if (b.canUpgrade(level)) {
				if (b.upgradeCost(level) <= player.money()) {
					p.upgrade(b, level);
				} else {
					ErrorType.NOT_ENOUGH_MONEY.raise("" + level);
				}
			} else {
				ErrorType.CANT_UPGRADE_BUILDING.raise();
			}
		}
		ErrorType.UNKNOWN_BUILDING.raise("" + id);
	}

	@Override
	public int deployPlanetItem(String planetId, String type)
			throws IOException {
		Planet p = world.planet(planetId);
		if (p == null || player.knowledge(p, PlanetKnowledge.VISIBLE) < 0) {
			ErrorType.UNKNOWN_PLANET.raise(planetId);
		}
		int piid = deployItem(p, type);
		lastPlanetInventoryItem = piid;
		return piid;
	}

	@Override
	public void undeployPlanetItem(String planetId, int itemId)
			throws IOException {
		Planet p = checkPlanet(planetId);
		undeployItem(p, checkLast(itemId, lastPlanetInventoryItem));
	}

	@Override
	public void sellPlanetItem(String planetId, int itemId) throws IOException {
		Planet p = checkPlanet(planetId);
		sellItem(p, checkLast(itemId, lastPlanetInventoryItem));
	}

	@Override
	public void addPlanetEquipment(String planetId, int itemId, String slotId,
			String type) throws IOException {
		Planet p = checkPlanet(planetId);
		addEquipment(p, checkLast(itemId, lastPlanetInventoryItem), slotId, type);
	}

	@Override
	public void removePlanetEquipment(String planetId, int itemId, String slotId)
			throws IOException {
		Planet p = checkPlanet(planetId);
		removeEquipment(p, checkLast(itemId, lastPlanetInventoryItem), slotId);
	}

	@Override
	public void planetUpgrade(String planetId) throws IOException {
		Planet p = checkPlanet(planetId);
		p.upgradeAll();
	}

	@Override
	public void startProduction(String type) throws IOException {
		ResearchType rt = world.research(type);
		if (rt == null || rt.nobuild || !player.isAvailable(rt)) {
			ErrorType.UNKNOWN_RESEARCH.raise(type);
		}
		if (player.addProduction(rt) == null) {
			ErrorType.PRODUCTION_LINES_FULL.raise();
		}
	}

	@Override
	public void stopProduction(String type) throws IOException {
		ResearchType rt = world.research(type);
		if (rt == null || rt.nobuild || !player.isAvailable(rt)) {
			ErrorType.UNKNOWN_RESEARCH.raise(type);
		}
		player.removeProduction(rt);
	}

	@Override
	public void setProductionQuantity(String type, int count)
			throws IOException {
		ResearchType rt = world.research(type);
		if (rt == null || rt.nobuild || !player.isAvailable(rt)) {
			ErrorType.UNKNOWN_RESEARCH.raise(type);
		}
		Production prod = player.getProduction(rt);
		if (prod != null) {
			prod.count = count;
		} else {
			ErrorType.NOT_PRODUCING.raise(type);
		}
	}

	@Override
	public void setProductionPriority(String type, int priority)
			throws IOException {
		ResearchType rt = world.research(type);
		if (rt == null || rt.nobuild || !player.isAvailable(rt)) {
			ErrorType.UNKNOWN_RESEARCH.raise(type);
		}
		Production prod = player.getProduction(rt);
		if (prod != null) {
			prod.priority = Math.max(0, Math.min(100, priority));
		} else {
			ErrorType.NOT_PRODUCING.raise(type);
		}
	}

	@Override
	public void sellInventory(String type, int count) throws IOException {
		ResearchType rt = world.research(type);
		if (rt == null || !player.isAvailable(rt)) {
			ErrorType.UNKNOWN_RESEARCH.raise(type);
		}
		player.sellInventory(rt, count);
	}

	@Override
	public void startResearch(String type) throws IOException {
		ResearchType rt = world.research(type);
		if (rt == null || rt.level > world.level) {
			ErrorType.UNKNOWN_RESEARCH.raise(type);
		}
		if (player.canResearch(rt)) {
			player.startResearch(rt);
		} else {
			ErrorType.PREREQUISITES_NOT_MET.raise(type);
		}
	}

	@Override
	public void stopResearch(String type) throws IOException {
		ResearchType rt = world.research(type);
		if (rt == null) {
			ErrorType.UNKNOWN_RESEARCH.raise(type);
		}
		player.stopResearch(rt);
	}

	@Override
	public void setResearchMoney(String type, int money) throws IOException {
		ResearchType rt = world.research(type);
		if (rt == null) {
			ErrorType.UNKNOWN_RESEARCH.raise(type);
		}
		Research rs = player.getResearch(rt);
		if (rs != null) {
			rs.setAssignedMoney(money);
		} else {
			ErrorType.NOT_RESEARCHING.raise(type);
		}
	}

	@Override
	public void pauseResearch() throws IOException {
		player.pauseResearch = true;
	}

	@Override
	public void pauseProduction() throws IOException {
		player.pauseProduction = true;
	}

	@Override
	public void unpauseProduction() throws IOException {
		player.pauseProduction = false;
	}

	@Override
	public void unpauseResearch() throws IOException {
		player.pauseResearch = false;
	}
	/**
	 * Retrieves the inventory item status of a given inventory item on an
	 * object having inventory.
	 * @param inv the inventory-having object
	 * @param itemId the inventory item id
	 * @return the item status
	 * @throws IOException if the inventory item is missing
	 */
	protected InventoryItemStatus getInventoryStatus(HasInventory inv, int itemId) throws IOException {
		InventoryItem ii = inv.inventory().findById(checkLast(itemId, lastFleetInventoryItem));
		if (ii != null) {
			return ii.toInventoryItemStatus();
		}
		ErrorType.UNKNOWN_INVENTORY_ITEM.raise("" + itemId);
		return null;
	}
	
	@Override
	public InventoryItemStatus getInventoryStatus(int fleetId, int itemId)
			throws IOException {
		Fleet f = fleetCheck(fleetId);
		if (f != null) {
			return getInventoryStatus(f, itemId);
		}
		ErrorType.UNKNOWN_FLEET.raise();
		return null;
	}
	
	@Override
	public InventoryItemStatus getInventoryStatus(String planetId, int itemId)
			throws IOException {
		Planet p = checkPlanet(planetId);
		return getInventoryStatus(p, itemId);
	}

	@Override
	public void stopSpaceUnit(int battleId, int unitId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveSpaceUnit(int battleId, int unitId, double x, double y)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void attackSpaceUnit(int battleId, int unitId, int targetUnitId)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void kamikazeSpaceUnit(int battleId, int unitId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void fireSpaceRocket(int battleId, int unitId, int targetUnitId)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void spaceRetreat(int battleId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopSpaceRetreat(int battleId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void fleetFormation(int fleetId, int formation) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<BattleStatus> getBattles() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BattleStatus getBattle(int battleId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SpaceBattleUnit> getSpaceBattleUnits(int battleId)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stopGroundUnit(int battleId, int unitId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveGroundUnit(int battleId, int unitId, int x, int y)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void attackGroundUnit(int battleId, int unitId, int targetUnitId)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void attackBuilding(int battleId, int unitId, int buildingId)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deployMine(int battleId, int unitId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void groundRetreat(int battleId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopGroundRetreat(int battleId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<GroundwarUnitStatus> getGroundBattleUnits(int battleId)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
