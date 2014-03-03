/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.model.AutoBuild;
import hu.openig.model.BattleStatus;
import hu.openig.model.BuildingStatus;
import hu.openig.model.EmpireStatuses;
import hu.openig.model.FleetStatus;
import hu.openig.model.FleetTransferMode;
import hu.openig.model.GroundwarUnitStatus;
import hu.openig.model.InventoryItemStatus;
import hu.openig.model.MessageArrayItemFactory;
import hu.openig.model.MessageObjectIO;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.MultiplayerGameSetup;
import hu.openig.model.MultiplayerUser;
import hu.openig.model.PlanetStatus;
import hu.openig.model.ProductionStatuses;
import hu.openig.model.RemoteGameAPI;
import hu.openig.model.ResearchStatuses;
import hu.openig.model.SpaceBattleUnit;
import hu.openig.model.TaxLevel;
import hu.openig.model.WelcomeResponse;
import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageClientAPI;
import hu.openig.net.MessageObject;
import hu.openig.net.MessageSerializable;
import hu.openig.net.MessageUtils;
import hu.openig.net.MissingAttributeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A remote game API client implementation based
 * on MessageObject exchanges.
 * @author akarnokd, 2013.04.22.
 */
public class RemoteGameClient implements RemoteGameAPI {
	/** The message client object. */
	protected final MessageClientAPI client;
	/**
	 * Constructor. Sets the message client object.
	 * @param client the client object
	 */
	public RemoteGameClient(MessageClientAPI client) {
		this.client = client;
	}
	@Override
	public long ping() throws IOException {
		long t = System.nanoTime();
		MessageUtils.expectObject(client.query(new MessageObject("PING")), "PONG");
		return (System.nanoTime() - t) / 1000000L;
	}
	/**
	 * Sends a message and receives a response message object
	 * which is filled in the response object if it
	 * matches the given message type.
	 * @param <T> the result object type
	 * @param request the request message
	 * @param response the response object
	 * @return the response object
	 * @throws IOException on communication error
	 */
	protected <T extends MessageObjectIO> T query(
			MessageSerializable request, 
			T response) throws IOException {
		Object r = client.query(request);
		MessageObject mo = MessageUtils.expectObject(r, response.objectName());
		try {
			response.fromMessage(mo);
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.FORMAT, ex);
		}
		return response;
	}
	/**
	 * Sends a message and receives a response message object
	 * which is filled in the response object if it
	 * matches the given message type.
	 * @param <T> the result object type
	 * @param request the request message
	 * @param itemFactory the factory for each list item
	 * @return the response object
	 * @throws IOException on communication error
	 */
	protected <T extends MessageObjectIO> List<T> queryList(
			MessageSerializable request, 
			MessageArrayItemFactory<? extends T> itemFactory) throws IOException {
		Object r = client.query(request);
		MessageArray ma = MessageUtils.expectArray(r, itemFactory.arrayName());
		List<T> result = new ArrayList<>();
		try {
			for (Object o : ma) {
				if (o instanceof MessageObject) {
					T item = itemFactory.invoke();
					item.fromMessage((MessageObject)o);
					result.add(item);
				} else {
					throw new ErrorResponse(ErrorType.FORMAT, o != null ? o.getClass().toString() : "null");
				}
			}
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.FORMAT, ex);
		}
		return result;
	}
	/**
	 * Send a request and expect a single OK response.
	 * @param request the request object
	 * @throws IOException on error
	 */
	protected void send(MessageSerializable request) throws IOException {
		send(request, "OK");
	}
	/**
	 * Send a request and expect a set of object responses with the given names.
	 * @param request the request
	 * @param names the names to accept
	 * @throws IOException on error
	 */
	protected void send(MessageSerializable request, String... names) throws IOException {
		Object r = client.query(request);
		MessageUtils.expectObject(r, names);
	}
	
	@Override
	public WelcomeResponse login(String user, String passphrase, String version)
			throws IOException {
		MessageObject request = new MessageObject("LOGIN") 
		.set("user", user) 
		.set("passphrase", passphrase) 
		.set("version", version);
		return query(request, new WelcomeResponse());
	}
	@Override
	public void relogin(String sessionId) throws IOException {
		MessageObject request = new MessageObject("RELOGIN")
		.set("session", sessionId);
		send(request, "WELCOME_BACK");
	}

	@Override
	public void leave() throws IOException {
		MessageObject request = new MessageObject("LEAVE");
		send(request);
	}

	@Override
	public MultiplayerDefinition getGameDefinition() throws IOException {
		MessageObject request = new MessageObject("QUERY_GAME_DEFINITION");
		return query(request, new MultiplayerDefinition());
	}
	@Override
	public void choosePlayerSettings(MultiplayerUser user) throws IOException {
		MessageObject request = user.toMessage();
		send(request);
	}

	@Override
	public MultiplayerGameSetup join() throws IOException {
		MessageObject request = new MessageObject("JOIN");
		return query(request, new MultiplayerGameSetup());
	}

	@Override
	public void ready() throws IOException {
		MessageObject request = new MessageObject("READY");
		send(request, "BEGIN");
	}

	@Override
	public EmpireStatuses getEmpireStatuses() throws IOException {
		MessageObject request = new MessageObject("QUERY_EMPIRE_STATUSES");
		return query(request, new EmpireStatuses());
	}

	@Override
	public List<FleetStatus> getFleets() throws IOException {
		MessageObject request = new MessageObject("QUERY_FLEETS");
		return queryList(request, new FleetStatus());
	}

	@Override
	public FleetStatus getFleet(int fleetId) throws IOException {
		MessageObject request = new MessageObject("QUERY_FLEET")
		.set("fleetId", fleetId);
		return query(request, new FleetStatus());
	}

	@Override
	public Map<String, Integer> getInventory() throws IOException {
		MessageObject request = new MessageObject("QUERY_INVENTORIES");
		Object param1 = client.query(request);
		MessageArray ma = MessageUtils.expectArray(param1, "INVENTORIES");
		
		Map<String, Integer> map = new HashMap<>();
		try {
			for (Object o : ma) {
				MessageObject mo = MessageUtils.expectObject(o, "ENTRY");
				
				map.put(mo.getString("type"), mo.getInt("count"));
			}
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.FORMAT, ex.toString(), ex);
		}
		return map;
	}

	@Override
	public ProductionStatuses getProductions() throws IOException {
		MessageObject request = new MessageObject("QUERY_PRODUCTIONS");
		return query(request, new ProductionStatuses());
	}

	@Override
	public ResearchStatuses getResearches() throws IOException {
		MessageObject request = new MessageObject("QUERY_RESEARCHES");
		return query(request, new ResearchStatuses());
	}

	@Override
	public List<PlanetStatus> getPlanetStatuses() throws IOException {
		MessageObject request = new MessageObject("QUERY_PLANET_STATUSES");
		return queryList(request, new PlanetStatus());
	}

	@Override
	public PlanetStatus getPlanetStatus(String id) throws IOException {
		MessageObject request = new MessageObject("QUERY_PLANET_STATUS")
		.set("planetId", id);
		return query(request, new PlanetStatus());
	}

	@Override
	public void moveFleet(int id, double x, double y) throws IOException {
		MessageObject request = new MessageObject("MOVE_FLEET")
		.set("fleetId", id)
		.set("x", x)
		.set("y", y);
		send(request);
	}

	@Override
	public void addFleetWaypoint(int id, double x, double y) throws IOException {
		MessageObject request = new MessageObject("ADD_FLEET_WAYPOINT")
		.set("fleetId", id)
		.set("x", x)
		.set("y", y);
		send(request);
	}

	@Override
	public void moveToPlanet(int id, String target) throws IOException {
		MessageObject request = new MessageObject("MOVE_TO_PLANET")
		.set("fleetId", id)
		.set("target", target);
		send(request);
	}

	@Override
	public void followFleet(int id, int target) throws IOException {
		MessageObject request = new MessageObject("FOLLOW_FLEET")
		.set("fleetId", id)
		.set("target", target);
		send(request);
	}

	@Override
	public void attackFleet(int id, int target) throws IOException {
		MessageObject request = new MessageObject("ATTACK_FLEET")
		.set("fleetId", id)
		.set("target", target);
		send(request);
	}

	@Override
	public void attackPlanet(int id, String target) throws IOException {
		MessageObject request = new MessageObject("ATTACK_PLANET")
		.set("fleetId", id)
		.set("target", target);
		send(request);
	}

	@Override
	public void colonize(int id, String target) throws IOException {
		MessageObject request = new MessageObject("COLONIZE_FLEET")
		.set("fleetId", id)
		.set("target", target);
		send(request);
	}

	/**
	 * Send a fleet configuration request.
	 * @param inventory the inventory describing the fleet contents
	 * @param request the request object to fill in and send
	 * @return the fleet status object response
	 * @throws IOException on format- or communication error
	 */
	int sendFleetConfig(List<InventoryItemStatus> inventory,
			MessageObject request) throws IOException {
		MessageArray inv = new MessageArray(null);
		request.set("inventory", inv);
		for (InventoryItemStatus ii : inventory) {
			inv.add(ii.toMessage());
		}
		Object param1 = client.query(request);
		MessageObject mo = MessageUtils.expectObject(param1, "FLEET");
		try {
			return mo.getInt("id");
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.FORMAT, ex.toString(), ex);
		}
	}

	@Override
	public int newFleet(String planet, List<InventoryItemStatus> inventory) throws IOException {
		MessageObject mo = new MessageObject("NEW_FLEET_AT_PLANET")
		.set("planetId", planet);
		return sendFleetConfig(inventory, mo);
	}

	@Override
	public int newFleet(int id, List<InventoryItemStatus> inventory) throws IOException {
		MessageObject mo = new MessageObject("NEW_FLEET_AT_FLEET") 
		.set("fleetId", id);
		return sendFleetConfig(inventory, mo);
	}

	@Override
	public void deleteFleet(int id) throws IOException {
		MessageObject request = new MessageObject("DELETE_FLEET")
		.set("fleetId", id);
		send(request);
	}

	@Override
	public void renameFleet(int id, String name) throws IOException {
		MessageObject request = new MessageObject("RENAME_FLEET")
		.set("fleetId", id)
		.set("name", name);
		send(request);
	}

	@Override
	public void sellFleetItem(int id, int itemId) throws IOException {
		MessageObject request = new MessageObject("SELL_FLEET_ITEM")
		.set("fleetId", id)
		.set("itemId", itemId);
		send(request);
	}

	@Override
	public int deployFleetItem(int id, String type) throws IOException {
		MessageObject request = new MessageObject("DEPLOY_FLEET_ITEM")
		.set("fleetId", id)
		.set("type", type);
		Object param1 = client.query(request);
		MessageObject mo = MessageUtils.expectObject(param1, "INVENTORY");
		try {
			return mo.getInt("id");
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.FORMAT, ex.toString(), ex);
		}
	}

	@Override
	public void undeployFleetItem(int id, int itemId) throws IOException {
		MessageObject request = new MessageObject("UNDEPLOY_FLEET_ITEM")
		.set("fleetId", id)
		.set("itemId", itemId);
		send(request);
	}

	@Override
	public void addFleetEquipment(int id, int itemId, String slotId,
			String type) throws IOException {
		MessageObject request = new MessageObject("ADD_FLEET_EQUIPMENT") 
		.set("fleetId", id)
		.set("itemId", itemId)
		.set("slotId", slotId)
		.set("type", type);
		send(request);
	}

	@Override
	public void removeFleetEquipment(int id, int itemId, String slotId) throws IOException {
		MessageObject request = new MessageObject("REMOVE_FLEET_EQUIPMENT")
		.set("fleetId", id)
		.set("itemId", itemId)
		.set("slotId", slotId);
		send(request);
	}

	@Override
	public void fleetUpgrade(int id) throws IOException {
		MessageObject request = new MessageObject("FLEET_UPGRADE")
		.set("fleetId", id);
		send(request);
	}

	@Override
	public void stopFleet(int id) throws IOException {
		MessageObject request = new MessageObject("STOP_FLEET")
		.set("fleetId", id);
		send(request);
	}

	@Override
	public void transfer(int sourceFleet, int destinationFleet, int sourceItem,
			FleetTransferMode mode) throws IOException {
		MessageObject request = new MessageObject("TRANSFER")
		.set("source", sourceFleet)
		.set("destination", destinationFleet)
		.set("itemId", sourceItem)
		.set("mode", mode.toString());
		send(request);
	}

	@Override
	public void colonize(String id) throws IOException {
		MessageObject request = new MessageObject("COLONIZE")
		.set("planetId", id);
		send(request);
	}

	@Override
	public void cancelColonize(String id) throws IOException {
		MessageObject request = new MessageObject("CANCEL_COLONIZE")
		.set("planetId", id);
		send(request);
	}

	@Override
	public int build(String planetId, String type, String race, int x, int y) throws IOException {
		MessageObject request = new MessageObject("BUILD_AT")
		.set("planetId", planetId)
		.set("type", type)
		.set("race", race)
		.set("x", x)
		.set("y", y);
		Object param1 = client.query(request);
		MessageObject mo = MessageUtils.expectObject(param1, BuildingStatus.OBJECT_NAME);
		try {
			return mo.getInt("id");
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.FORMAT, ex.toString(), ex);
		}
	}

	@Override
	public int build(String planetId, String type, String race) throws IOException {
		MessageObject request = new MessageObject("BUILD")
		.set("planetId", planetId)
		.set("type", type)
		.set("race", race);
		Object param1 = client.query(request);
		MessageObject mo = MessageUtils.expectObject(param1, BuildingStatus.OBJECT_NAME);
		try {
			return mo.getInt("id");
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.FORMAT, ex.toString(), ex);
		}
	}

	@Override
	public void enable(String planetId, int id) throws IOException {
		MessageObject request = new MessageObject("ENABLE")
		.set("planetId", planetId)
		.set("buildingId", id);
		send(request);
	}

	@Override
	public void disable(String planetId, int id) throws IOException {
		MessageObject request = new MessageObject("DISABLE")
		.set("planetId", planetId)
		.set("buildingId", id);
		send(request);
	}

	@Override
	public void repair(String planetId, int id) throws IOException {
		MessageObject request = new MessageObject("REPAIR")
		.set("planetId", planetId)
		.set("buildingId", id);
		send(request);
	}

	@Override
	public void repairOff(String planetId, int id) throws IOException {
		MessageObject request = new MessageObject("REPAIR_OFF")
		.set("planetId", planetId)
		.set("buildingId", id);
		send(request);
	}

	@Override
	public void demolish(String planetId, int id) throws IOException {
		MessageObject request = new MessageObject("DEMOLISH")
		.set("planetId", planetId)
		.set("buildingId", id);
		send(request);
	}

	@Override
	public void buildingUpgrade(String planetId, int id, int level) throws IOException {
		MessageObject request = new MessageObject("BUILDING_UPGRADE")
		.set("planetId", planetId)
		.set("buildingId", id)
		.set("level", level);
		send(request);
	}

	@Override
	public int deployPlanetItem(String planetId, String type) throws IOException {
		MessageObject request = new MessageObject("DEPLOY_PLANET_ITEM") 
		.set("planetId", planetId)
		.set("type", type);
		Object param1 = client.query(request);
		MessageObject mo = MessageUtils.expectObject(param1, InventoryItemStatus.OBJECT_NAME);
		try {
			return mo.getInt("id");
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.FORMAT, ex.toString(), ex);
		}
	}

	@Override
	public void undeployPlanetItem(String planetId, int itemId) throws IOException {
		MessageObject request = new MessageObject("UNDEPLOY_PLANET_ITEM")
		.set("planetId", planetId)
		.set("itemId", itemId);
		send(request);
	}

	@Override
	public void sellPlanetItem(String planetId, int itemId) throws IOException {
		MessageObject request = new MessageObject("SELL_PLANET_ITEM") 
		.set("planetId", planetId)
		.set("itemId", itemId);
		send(request);
	}

	@Override
	public void addPlanetEquipment(String planetId, int itemId, String slotId,
			String type) throws IOException {
		MessageObject request = new MessageObject("ADD_PLANET_EQUIPMENT") 
		.set("planetId", planetId)
		.set("itemId", itemId)
		.set("slotId", slotId)
		.set("type", type);
		send(request);
	}

	@Override
	public void removePlanetEquipment(String planetId, int itemId,
			String slotId) throws IOException {
		MessageObject request = new MessageObject("REMOVE_PLANET_EQUIPMENT") 
		.set("planetId", planetId)
		.set("itemId", itemId)
		.set("slotId", slotId);
		send(request);
	}

	@Override
	public void planetUpgrade(String planetId) throws IOException {
		MessageObject request = new MessageObject("PLANET_UPGRADE") 
		.set("planetId", planetId);
		send(request);
	}

	@Override
	public void startProduction(String type) throws IOException {
		MessageObject request = new MessageObject("START_PRODUCTION") 
		.set("type", type);
		send(request);
	}

	@Override
	public void stopProduction(String type) throws IOException {
		MessageObject request = new MessageObject("STOP_PRODUCTION") 
		.set("type", type);
		send(request);
	}

	@Override
	public void setProductionQuantity(String type, int count) throws IOException {
		MessageObject request = new MessageObject("SET_PRODUCTION_QUANTITY") 
		.set("type", type)
		.set("count", count);
		send(request);
	}

	@Override
	public void setProductionPriority(String type, int priority) throws IOException {
		MessageObject request = new MessageObject("SET_PRODUCTION_PRIORITY") 
		.set("type", type)
		.set("priority", priority);
		send(request);
	}

	@Override
	public void sellInventory(String type, int count) throws IOException {
		MessageObject request = new MessageObject("SELL_INVENTORY") 
		.set("type", type)
		.set("count", count);
		send(request);
	}

	@Override
	public void startResearch(String type) throws IOException {
		MessageObject request = new MessageObject("START_RESEARCH") 
		.set("type", type);
		send(request);
	}

	@Override
	public void stopResearch(String type) throws IOException {
		MessageObject request = new MessageObject("STOP_RESEARCH") 
		.set("type", type);
		send(request);
	}

	@Override
	public void setResearchMoney(String type, int money) throws IOException {
		MessageObject request = new MessageObject("SET_RESEARCH_MONEY") 
		.set("type", type)
		.set("money", money);
		send(request);
	}

	@Override
	public void pauseResearch() throws IOException {
		MessageObject request = new MessageObject("PAUSE_RESEARCH");
		send(request);
	}

	@Override
	public void pauseProduction() throws IOException {
		MessageObject request = new MessageObject("PAUSE_PRODUCTION");
		send(request);
	}

	@Override
	public void unpauseProduction() throws IOException {
		MessageObject request = new MessageObject("UNPAUSE_PRODUCTION");
		send(request);
	}

	@Override
	public void unpauseResearch() throws IOException {
		MessageObject request = new MessageObject("UNPAUSE_RESEARCH");
		send(request);
	}

	@Override
	public void stopSpaceUnit(int battleId, int unitId) throws IOException {
		MessageObject request = new MessageObject("STOP_SPACE_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId);
		send(request);
	}

	@Override
	public void moveSpaceUnit(int battleId, int unitId, double x, double y) throws IOException {
		MessageObject request = new MessageObject("MOVE_SPACE_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId)
		.set("x", x)
		.set("y", y);
		send(request);
	}

	@Override
	public void attackSpaceUnit(int battleId, int unitId, int targetUnitId) throws IOException {
		MessageObject request = new MessageObject("ATTACK_SPACE_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId)
		.set("target", targetUnitId);
		send(request);
	}

	@Override
	public void kamikazeSpaceUnit(int battleId, int unitId) throws IOException {
		MessageObject request = new MessageObject("KAMIKAZE_SPACE_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId);
		send(request);
	}

	@Override
	public void fireSpaceRocket(int battleId, int unitId, int targetUnitId) throws IOException {
		MessageObject request = new MessageObject("FIRE_SPACE_ROCKET")
		.set("battleId", battleId)
		.set("unitId", unitId)
		.set("target", targetUnitId);
		send(request);
	}

	@Override
	public void spaceRetreat(int battleId) throws IOException {
		MessageObject request = new MessageObject("SPACE_RETREAT")
		.set("battleId", battleId);
		send(request);
	}

	@Override
	public void stopSpaceRetreat(int battleId) throws IOException {
		MessageObject request = new MessageObject("STOP_SPACE_RETREAT")
		.set("battleId", battleId);
		send(request);
	}

	@Override
	public void fleetFormation(int fleetId, int formation) throws IOException {
		MessageObject request = new MessageObject("FLEET_FORMATION")
		.set("fleetId", fleetId)
		.set("formation", formation);
		send(request);
	}

	@Override
	public List<BattleStatus> getBattles() throws IOException {
		MessageObject request = new MessageObject("QUERY_BATTLES");
		return queryList(request, new BattleStatus());
	}

	@Override
	public BattleStatus getBattle(int battleId) throws IOException {
		MessageObject request = new MessageObject("QUERY_BATTLE")
		.set("battleId", battleId);
		return query(request, new BattleStatus());
	}

	@Override
	public List<SpaceBattleUnit> getSpaceBattleUnits(int battleId) throws IOException {
		MessageObject request = new MessageObject("QUERY_SPACE_BATTLE_UNITS")
		.set("battleId", battleId);
		return queryList(request, new SpaceBattleUnit());
	}

	@Override
	public void stopGroundUnit(int battleId, int unitId) throws IOException {
		MessageObject request = new MessageObject("STOP_GROUND_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId);
		send(request);
	}

	@Override
	public void moveGroundUnit(int battleId, int unitId, int x, int y) throws IOException {
		MessageObject request = new MessageObject("MOVE_GROUND_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId)
		.set("x", x)
		.set("y", y);
		send(request);
	}

	@Override
	public void attackGroundUnit(int battleId, int unitId, int targetUnitId) throws IOException {
		MessageObject request = new MessageObject("ATTACK_GROUND_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId)
		.set("target", targetUnitId);
		send(request);
	}

	@Override
	public void attackBuilding(int battleId, int unitId, int buildingId) throws IOException {
		MessageObject request = new MessageObject("ATTACK_BUILDING")
		.set("battleId", battleId)
		.set("unitId", unitId)
		.set("buildingId", buildingId);
		send(request);
	}

	@Override
	public void deployMine(int battleId, int unitId) throws IOException {
		MessageObject request = new MessageObject("DEPLOY_MINE")
		.set("battleId", battleId)
		.set("unitId", unitId);
		send(request);
	}

	@Override
	public void groundRetreat(int battleId) throws IOException {
		MessageObject request = new MessageObject("GROUND_RETREAT")
		.set("battleId", battleId);
		send(request);
	}

	@Override
	public void stopGroundRetreat(int battleId) throws IOException {
		MessageObject request = new MessageObject("STOP_GROUND_RETREAT")
		.set("battleId", battleId);
		send(request);
	}

	@Override
	public List<GroundwarUnitStatus> getGroundBattleUnits(int battleId) throws IOException {
		MessageObject request = new MessageObject("QUERY_GROUND_BATTLE_UNITS")
		.set("battleId", battleId);
		return queryList(request, new GroundwarUnitStatus());
	}
	@Override
	public InventoryItemStatus getInventoryStatus(int fleetId, int itemId)
			throws IOException {
		MessageObject request = new MessageObject("QUERY_FLEET_INVENTORY")
		.set("fleetId", fleetId)
		.set("itemId", itemId)
		;
		return query(request, new InventoryItemStatus());
	}
	@Override
	public InventoryItemStatus getInventoryStatus(String planetId, int itemId)
			throws IOException {
		MessageObject request = new MessageObject("QUERY_PLANET_INVENTORY")
		.set("planetId", planetId)
		.set("itemId", itemId)
		;
		return query(request, new InventoryItemStatus());
	}
	@Override
	public void setAutoBuild(String planetId, AutoBuild auto)
			throws IOException {
		MessageObject request = new MessageObject("SET_PLANET_AUTOBUILD")
		.set("planetId", planetId)
		.set("auto", auto)
		;
		send(request);
	}
	@Override
	public void setTaxLevel(String planetId, TaxLevel tax) throws IOException {
		MessageObject request = new MessageObject("SET_PLANET_TAX")
		.set("planetId", planetId)
		.set("tax", tax)
		;
		send(request);
	}
}
