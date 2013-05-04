/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.model.BattleStatus;
import hu.openig.model.EmpireStatuses;
import hu.openig.model.FleetStatus;
import hu.openig.model.FleetTransferMode;
import hu.openig.model.GroundBattleUnit;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventoryItemStatus;
import hu.openig.model.InventorySlot;
import hu.openig.model.MessageArrayItemFactory;
import hu.openig.model.MessageObjectIO;
import hu.openig.model.MessageUtils;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.MultiplayerGameSetup;
import hu.openig.model.MultiplayerUser;
import hu.openig.model.PlanetStatus;
import hu.openig.model.ProductionStatus;
import hu.openig.model.RemoteGameAPI;
import hu.openig.model.ResearchStatus;
import hu.openig.model.SpaceBattleUnit;
import hu.openig.model.WelcomeResponse;
import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageClientAPI;
import hu.openig.net.MessageObject;
import hu.openig.net.MessageSerializable;
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
		MessageUtils.expectObject(client.query("PING{}"), "PONG");
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
		MessageObject mo = MessageUtils.expectObject(r, response.name());
		try {
			response.fromMessage(mo);
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.ERROR_FORMAT, ex);
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
		List<T> result = new ArrayList<T>();
		try {
			for (Object o : ma) {
				if (o instanceof MessageObject) {
					T item = itemFactory.invoke();
					item.fromMessage((MessageObject)o);
					result.add(item);
				} else {
					throw new ErrorResponse(ErrorType.ERROR_FORMAT, o != null ? o.getClass().toString() : "null");
				}
			}
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.ERROR_FORMAT, ex);
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
		MessageObject request = new MessageObject("LOGIN", 
						"user", user, 
						"passphrase", passphrase, 
						"version", version);
		return query(request, new WelcomeResponse());
	}
	@Override
	public void relogin(String sessionId) throws IOException {
		MessageObject request = new MessageObject("RELOGIN", "session", sessionId);
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
		MessageObject request = new MessageObject("QUERY_FLEET", "fleetId", fleetId);
		return query(request, new FleetStatus());
	}

	@Override
	public Map<String, Integer> getInventory() throws IOException {
		MessageObject request = new MessageObject("QUERY_INVENTORIES");
		Object param1 = client.query(request);
		MessageArray ma = MessageUtils.expectArray(param1, "INVENTORIES");
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			for (Object o : ma) {
				MessageObject mo = MessageUtils.expectObject(o, "INVENTORY");
				
				map.put(mo.getString("type"), mo.getInt("count"));
			}
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.ERROR_FORMAT, ex.toString(), ex);
		}
		return map;
	}

	@Override
	public ProductionStatus getProductions() throws IOException {
		MessageObject request = new MessageObject("QUERY_PRODUCTIONS");
		return query(request, new ProductionStatus());
	}

	@Override
	public ResearchStatus getResearches() throws IOException {
		MessageObject request = new MessageObject("QUERY_RESEARCHES");
		return query(request, new ResearchStatus());
	}

	@Override
	public List<PlanetStatus> getPlanetStatuses() throws IOException {
		MessageObject request = new MessageObject("QUERY_PLANET_STATUSES");
		return queryList(request, new PlanetStatus());
	}

	@Override
	public PlanetStatus getPlanetStatus(String id) throws IOException {
		MessageObject request = new MessageObject("QUERY_PLANET_STATUS", "planetId", id);
		return query(request, new PlanetStatus());
	}

	@Override
	public void moveFleet(int id, double x, double y) throws IOException {
		MessageObject request = new MessageObject("MOVE_FLEET", "fleetId", id, "x", x, "y", y);
		send(request);
	}

	@Override
	public void addFleetWaypoint(int id, double x, double y) throws IOException {
		MessageObject request = new MessageObject("ADD_FLEET_WAYPOINT", "fleetId", id, "x", x, "y", y);
		send(request);
	}

	@Override
	public void moveToPlanet(int id, String target) throws IOException {
		MessageObject request = new MessageObject("MOVE_TO_PLANET", "fleetId", id, "target", target);
		send(request);
	}

	@Override
	public void followFleet(int id, int target) throws IOException {
		MessageObject request = new MessageObject("FOLLOW_FLEET", "fleetId", id, "target", target);
		send(request);
	}

	@Override
	public void attackFleet(int id, int target) throws IOException {
		MessageObject request = new MessageObject("ATTACK_FLEET", "fleetId", id, "target", target);
		send(request);
	}

	@Override
	public void attackPlanet(int id, String target) throws IOException {
		MessageObject request = new MessageObject("ATTACK_PLANET", "fleetId", id, "target", target);
		send(request);
	}

	@Override
	public void colonize(int id, String target) throws IOException {
		MessageObject request = new MessageObject("COLONIZE_FLEET", "fleetId", id, "target", target);
		send(request);
	}

	@Override
	public FleetStatus newFleet(String planet, List<InventoryItem> inventory) throws IOException {
		MessageObject mo = new MessageObject("NEW_FLEET_AT_PLANET", "planetId", planet);
		return sendFleetConfig(inventory, mo);
	}

	/**
	 * Send a fleet configuration request.
	 * @param inventory the inventory describing the fleet contents
	 * @param mo the request object to fill in and send
	 * @return the fleet status object response
	 * @throws IOException on format- or communication error
	 */
	FleetStatus sendFleetConfig(List<InventoryItem> inventory,
			MessageObject mo) throws IOException {
		MessageArray inv = new MessageArray(null);
		mo.set("inventory", inv);
		for (InventoryItem ii : inventory) {
			MessageObject mii = new MessageObject("INVENTORY");
			mii.setMany("type", ii.type.id, "tag", ii.tag,
					"count", ii.count, "nickname", ii.nickname,
					"nicknameIndex", ii.nicknameIndex);
			MessageArray eq = new MessageArray(null);
			mii.set("equipment", eq);
			for (InventorySlot is : ii.slots) {
				MessageObject miis = new MessageObject("EQUIPMENT");
				miis.setMany("id", is.slot.id, "type", is.type, "count", is.count);
				eq.add(miis);
			}
			inv.add(mii);
		}
		return query(mo, new FleetStatus());
	}

	@Override
	public FleetStatus newFleet(int id, List<InventoryItem> inventory) throws IOException {
		MessageObject mo = new MessageObject("NEW_FLEET_AT_FLEET", 
				"fleetId", id);
		return sendFleetConfig(inventory, mo);
	}

	@Override
	public void deleteFleet(int id) throws IOException {
		MessageObject request = new MessageObject("DELETE_FLEET", "fleetId", id);
		send(request);
	}

	@Override
	public void renameFleet(int id, String name) throws IOException {
		MessageObject request = new MessageObject("RENAME_FLEET", "fleetId", id, "name", name);
		send(request);
	}

	@Override
	public void sellFleetItem(int id, int itemId) throws IOException {
		MessageObject request = new MessageObject("SELL_FLEET_ITEM", "fleetId", id, "itemId", itemId);
		send(request);
	}

	@Override
	public InventoryItemStatus deployFleetItem(int id, String type) throws IOException {
		MessageObject request = new MessageObject("DEPLOY_FLEET_ITEM", "fleetId", id, "type", type);
		return query(request, new InventoryItemStatus());
	}

	@Override
	public void undeployFleetItem(int id, int itemId) throws IOException {
		MessageObject request = new MessageObject("UNDEPLOY_FLEET_ITEM", "fleetId", id, "itemId", itemId);
		send(request);
	}

	@Override
	public void addFleetEquipment(int id, int itemId, String slotId,
			String type) throws IOException {
		MessageObject request = new MessageObject("ADD_FLEET_EQUIPMENT", 
						"fleetId", id, "itemId", itemId, 
						"slotId", slotId, "type", type);
		send(request);
	}

	@Override
	public void removeFleetEquipment(int id, int itemId, String slotId) throws IOException {
		MessageObject request = new MessageObject("REMOVE_FLEET_EQUIPMENT", 
						"fleetId", id, "itemId", itemId, 
						"slotId", slotId);
		send(request);
	}

	@Override
	public void fleetUpgrade(int id) throws IOException {
		MessageObject request = new MessageObject("FLEET_UPGRADE", "fleetId", id);
		send(request);
	}

	@Override
	public void stopFleet(int id) throws IOException {
		MessageObject request = new MessageObject("STOP_FLEET", "fleetId", id);
		send(request);
	}

	@Override
	public void transfer(int sourceFleet, int destinationFleet, int sourceItem,
			FleetTransferMode mode) throws IOException {
		MessageObject request = new MessageObject("TRANSFER", 
						"source", sourceFleet, "destination", destinationFleet,
						"itemId", sourceItem, "mode", mode.toString()
						);
		send(request);
	}

	@Override
	public void colonize(String id) throws IOException {
		MessageObject request = new MessageObject("COLONIZE", "planetId", id);
		send(request);
	}

	@Override
	public void cancelColonize(String id) throws IOException {
		MessageObject request = new MessageObject("CANCEL_COLONIZE", "planetId", id);
		send(request);
	}

	@Override
	public int build(String planetId, String type, String race, int x, int y) throws IOException {
		MessageObject request = new MessageObject("BUILD_AT", "planetId", planetId,
				"type", type, "race", race,
				"x", x, "y", y);
		Object param1 = client.query(request);
		MessageObject mo = MessageUtils.expectObject(param1, "BUILDING");
		try {
			return mo.getInt("buildingId");
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.ERROR_FORMAT, ex.toString(), ex);
		}
	}

	@Override
	public int build(String planetId, String type, String race) throws IOException {
		MessageObject request = new MessageObject("BUILD", "planetId", planetId,
				"type", type, "race", race);
		Object param1 = client.query(request);
		MessageObject mo = MessageUtils.expectObject(param1, "BUILDING");
		try {
			return mo.getInt("buildingId");
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.ERROR_FORMAT, ex.toString(), ex);
		}
	}

	@Override
	public void enable(String planetId, int id) throws IOException {
		MessageObject request = new MessageObject("ENABLE", "planetId", planetId,
						"buildingId", id);
		send(request);
	}

	@Override
	public void disable(String planetId, int id) throws IOException {
		MessageObject request = new MessageObject("DISABLE", "planetId", planetId,
						"buildingId", id);
		send(request);
	}

	@Override
	public void repair(String planetId, int id) throws IOException {
		MessageObject request = new MessageObject("REPAIR", "planetId", planetId,
						"buildingId", id);
		send(request);
	}

	@Override
	public void repairOff(String planetId, int id) throws IOException {
		MessageObject request = new MessageObject("REPAIR_OFF", "planetId", planetId,
						"buildingId", id);
		send(request);
	}

	@Override
	public void demolish(String planetId, int id) throws IOException {
		MessageObject request = new MessageObject("DEMOLISH", "planetId", planetId,
						"buildingId", id);
		send(request);
	}

	@Override
	public void buildingUpgrade(String planetId, int id, int level) throws IOException {
		MessageObject request = new MessageObject("BUILDING_UPGRADE", "planetId", planetId,
						"buildingId", id, "level", level);
		send(request);
	}

	@Override
	public InventoryItemStatus deployPlanetItem(String planetId, String type) throws IOException {
		MessageObject request = new MessageObject("DEPLOY_PLANET_ITEM", 
						"planetId", planetId, "type", type
						);
		return query(request, new InventoryItemStatus());
	}

	@Override
	public void undeployPlanetItem(String planetId, int itemId) throws IOException {
		MessageObject request = new MessageObject("UNDEPLOY_PLANET_ITEM", 
						"planetId", planetId, "itemId", itemId
						);
		send(request);
	}

	@Override
	public void sellPlanetItem(String planetId, int itemId) throws IOException {
		MessageObject request = new MessageObject("SELL_PLANET_ITEM", 
						"planetId", planetId, "itemId", itemId
						);
		send(request);
	}

	@Override
	public void addPlanetEquipment(String planetId, int itemId, String slotId,
			String type) throws IOException {
		MessageObject request = new MessageObject("SELL_PLANET_ITEM", 
						"planetId", planetId, "itemId", itemId,
						"slotId", slotId, "type", type
						);
		send(request);
	}

	@Override
	public void removePlanetEquipment(String planetId, int itemId,
			String slotId) throws IOException {
		MessageObject request = new MessageObject("REMOVE_PLANET_ITEM", 
						"planetId", planetId, "itemId", itemId,
						"slotId", slotId
						);
		send(request);
	}

	@Override
	public void planetUpgrade(String planetId) throws IOException {
		MessageObject request = new MessageObject("PLANET_UPGRADE", 
						"planetId", planetId
						);
		send(request);
	}

	@Override
	public void startProduction(String type) throws IOException {
		MessageObject request = new MessageObject("START_PRODUCTION", 
						"type", type
						);
		send(request);
	}

	@Override
	public void stopProduction(String type) throws IOException {
		MessageObject request = new MessageObject("STOP_PRODUCTION", 
						"type", type
						);
		send(request);
	}

	@Override
	public void setProductionQuantity(String type, int count) throws IOException {
		MessageObject request = new MessageObject("SET_PRODUCTION_QUANTITY", 
						"type", type, "count", count
						);
		send(request);
	}

	@Override
	public void setProductionPriority(String type, int priority) throws IOException {
		MessageObject request = new MessageObject("SET_PRODUCTION_PRIORITY", 
						"type", type, "priority", priority
						);
		send(request);
	}

	@Override
	public void sellInventory(String type, int count) throws IOException {
		MessageObject request = new MessageObject("SELL_INVENTORY", 
						"type", type, "count", count
						);
		send(request);
	}

	@Override
	public void startResearch(String type) throws IOException {
		MessageObject request = new MessageObject("START_RESEARCH", 
						"type", type
						);
		send(request);
	}

	@Override
	public void stopResearch(String type) throws IOException {
		MessageObject request = new MessageObject("STOP_RESEARCH", 
						"type", type
						);
		send(request);
	}

	@Override
	public void setResearchMoney(String type, int money) throws IOException {
		MessageObject request = new MessageObject("SET_RESEARCH_MONEY", 
						"type", type, "money", money
						);
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
		MessageObject request = new MessageObject("STOP_SPACE_UNIT",
						"battleId", battleId,
						"unitId", unitId);
		send(request);
	}

	@Override
	public void moveSpaceUnit(int battleId, int unitId, double x, double y) throws IOException {
		MessageObject request = new MessageObject("MOVE_SPACE_UNIT",
						"battleId", battleId,
						"unitId", unitId,
						"x", x, "y", y
						);
		send(request);
	}

	@Override
	public void attackSpaceUnit(int battleId, int unitId, int targetUnitId) throws IOException {
		MessageObject request = new MessageObject("ATTACK_SPACE_UNIT",
						"battleId", battleId,
						"unitId", unitId,
						"target", targetUnitId
						);
		send(request);
	}

	@Override
	public void kamikazeSpaceUnit(int battleId, int unitId) throws IOException {
		MessageObject request = new MessageObject("KAMIKAZE_SPACE_UNIT",
						"battleId", battleId,
						"unitId", unitId);
		send(request);
	}

	@Override
	public void fireSpaceRocket(int battleId, int unitId, int targetUnitId) throws IOException {
		MessageObject request = new MessageObject("FIRE_SPACE_ROCKET",
						"battleId", battleId,
						"unitId", unitId,
						"target", targetUnitId);
		send(request);
	}

	@Override
	public void spaceRetreat(int battleId) throws IOException {
		MessageObject request = new MessageObject("SPACE_RETREAT",
						"battleId", battleId
						);
		send(request);
	}

	@Override
	public void stopSpaceRetreat(int battleId) throws IOException {
		MessageObject request = new MessageObject("STOP_SPACE_RETREAT",
						"battleId", battleId
						);
		send(request);
	}

	@Override
	public void fleetFormation(int fleetId, int formation) throws IOException {
		MessageObject request = new MessageObject("FLEET_FORMATION",
						"fleetId", fleetId,
						"formation", formation);
		send(request);
	}

	@Override
	public List<BattleStatus> getBattles() throws IOException {
		MessageObject request = new MessageObject("QUERY_BATTLES");
		return queryList(request, new BattleStatus());
	}

	@Override
	public BattleStatus getBattle(int battleId) throws IOException {
		MessageObject request = new MessageObject("QUERY_BATTLE", "battleId", battleId);
		return query(request, new BattleStatus());
	}

	@Override
	public List<SpaceBattleUnit> getSpaceBattleUnits(int battleId) throws IOException {
		MessageObject request = new MessageObject("QUERY_SPACE_BATTLE_UNITS", "battleId", battleId);
		return queryList(request, new SpaceBattleUnit());
	}

	@Override
	public void stopGroundUnit(int battleId, int unitId) throws IOException {
		MessageObject request = new MessageObject("STOP_GROUND_UNIT",
						"battleId", battleId,
						"unitId", unitId);
		send(request);
	}

	@Override
	public void moveGroundUnit(int battleId, int unitId, int x, int y) throws IOException {
		MessageObject request = new MessageObject("MOVE_GROUND_UNIT",
						"battleId", battleId,
						"unitId", unitId,
						"x", x, "y", y);
		send(request);
	}

	@Override
	public void attackGroundUnit(int battleId, int unitId, int targetUnitId) throws IOException {
		MessageObject request = new MessageObject("ATTACK_GROUND_UNIT",
						"battleId", battleId,
						"unitId", unitId,
						"target", targetUnitId);
		send(request);
	}

	@Override
	public void attackBuilding(int battleId, int unitId, int buildingId) throws IOException {
		MessageObject request = new MessageObject("ATTACK_BUILDING",
						"battleId", battleId,
						"unitId", unitId,
						"buildingId", buildingId);
		send(request);
	}

	@Override
	public void deployMine(int battleId, int unitId) throws IOException {
		MessageObject request = new MessageObject("DEPLOY_MINE",
						"battleId", battleId,
						"unitId", unitId);
		send(request);
	}

	@Override
	public void groundRetreat(int battleId) throws IOException {
		MessageObject request = new MessageObject("GROUND_RETREAT",
						"battleId", battleId);
		send(request);
	}

	@Override
	public void stopGroundRetreat(int battleId) throws IOException {
		MessageObject request = new MessageObject("STOP_GROUND_RETREAT",
						"battleId", battleId);
		send(request);
	}

	@Override
	public List<GroundBattleUnit> getGroundBattleUnits(int battleId) throws IOException {
		MessageObject request = new MessageObject("QUERY_GROUND_BATTLE_UNITS", "battleId", battleId);
		return queryList(request, new GroundBattleUnit());
	}
}
