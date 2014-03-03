/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Action2E;
import hu.openig.model.AutoBuild;
import hu.openig.model.BattleStatus;
import hu.openig.model.BuildingStatus;
import hu.openig.model.DeferredCall;
import hu.openig.model.DeferredInvoke;
import hu.openig.model.DeferredRunnable;
import hu.openig.model.DeferredTransform;
import hu.openig.model.FleetStatus;
import hu.openig.model.FleetTransferMode;
import hu.openig.model.GroundwarUnitStatus;
import hu.openig.model.InventoryItemStatus;
import hu.openig.model.MessageObjectIO;
import hu.openig.model.MultiplayerUser;
import hu.openig.model.PlanetStatus;
import hu.openig.model.RemoteGameAPI;
import hu.openig.model.SpaceBattleUnit;
import hu.openig.model.TaxLevel;
import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageConnection;
import hu.openig.net.MessageObject;
import hu.openig.net.MessageSerializable;
import hu.openig.net.MissingAttributeException;
import hu.openig.utils.Exceptions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

/**
 * Listens for and dispatches incoming message requests and produces
 * message responses.
 * @author akarnokd, 2013.04.23.
 */
public class RemoteGameListener implements Action2E<MessageConnection, Object, IOException> {
	/** The game API. */
	protected final RemoteGameAPI api;
	/** Use the EDT to execute the API methods? */
	protected final boolean useEDT;
	/**
	 * Constructor, takes a remote API entry point and uses the EDT to call its methods.
	 * @param api the API entry point
	 */
	public RemoteGameListener(RemoteGameAPI api) {
		this.api = api;
		this.useEDT = true;
	}
	/**
	 * Constructor, takes a remote game API and might use the EDT to call its methods.
	 * @param api the API entry point
	 * @param useEDT use EDT for the methods?
	 */
	public RemoteGameListener(RemoteGameAPI api, boolean useEDT) {
		this.api = api;
		this.useEDT = useEDT;
	}
	@Override
	public void invoke(MessageConnection conn, Object message)
			throws IOException {
		DeferredRunnable responseCall;
		try {
			responseCall = processMessageDeferred(message);
		} catch (MissingAttributeException ex) {
			conn.error(message, ErrorType.FORMAT.ordinal(), ex.toString());
			return;
		}			
		if (useEDT) {
			try {
				SwingUtilities.invokeAndWait(responseCall);
			} catch (InterruptedException ex) {
				conn.error(message, ErrorType.INTERRUPTED.ordinal(), ex.toString());
			} catch (InvocationTargetException ex) {
				conn.error(message, ErrorType.SERVER_BUG.ordinal(), ex.toString());
			}
		} else {
			responseCall.run();
		}
			
		responseCall.done();
			
		try {
			conn.send(message, responseCall.get());
		} catch (ErrorResponse ex) {
			conn.error(message, ex.code.ordinal(), ex.toString());
		} catch (IOException ex) {
			conn.error(message, ErrorType.SERVER_IO.ordinal(), ex.toString());
		} catch (Throwable ex) {
			conn.error(message, ErrorType.SERVER_BUG.ordinal(), ex.toString());
			Exceptions.add(ex);
		}
	}
	/**
	 * Composes a list of deferred calls which need to be executed
	 * in order on the EDT. The returned object is then used
	 * as the main result.
	 * @param message the original message
	 * @return the main deferred call
	 * @throws IOException on error
	 */
	protected DeferredRunnable processMessageDeferred(Object message) throws IOException {
		if (message instanceof MessageArray) {
			MessageArray ma = (MessageArray)message;
			if ("BATCH".equals(ma.name)) {
				final List<DeferredRunnable> calls = new ArrayList<>();
				for (Object o : ma) {
					if (o instanceof MessageArray) {
						calls.add(processMessageArrayDeferred((MessageArray)o));
					} else
					if (o instanceof MessageObject) {
						calls.add(processMessageObjectDeferred((MessageObject)o));
					} else {
						throw new ErrorResponse(ErrorType.UNKNOWN_MESSAGE, message.getClass().toString());
					}
				}
				return new DeferredTransform<Void>() {
					@Override
					protected Void invoke() throws IOException {
						for (DeferredRunnable dc : calls) {
							dc.run();
							if (dc.isError()) {
								break;
							}
						}
						return null;
					}
					@Override
					public MessageArray transform(Void intermediate) throws IOException {
						MessageArray result = new MessageArray("BATCH_RESPONSE");
						for (DeferredRunnable dc : calls) {
							dc.done();
							result.add(dc.get());
						}
						return result;
					}
				};
			}
			return processMessageArrayDeferred(ma);
		}
		if (message instanceof MessageObject) {
			return processMessageObjectDeferred((MessageObject)message);
		}
		throw new ErrorResponse(ErrorType.UNKNOWN_MESSAGE, message != null ? message.getClass().toString() : "null");
	}
	/**
	 * Process requests with message array as their outer elements.
	 * @param ma the message array
	 * @return the deferred call object
	 * @throws IOException on error
	 */
	protected DeferredRunnable processMessageArrayDeferred(MessageArray ma) throws IOException {
		throw new ErrorResponse(ErrorType.UNKNOWN_MESSAGE, ma != null ? ma.name : "null");
	}
	/**
	 * Process request with message object as their outer elements.
	 * @param mo the message object
	 * @return the deferred call object
	 * @throws IOException on error
	 */
	protected DeferredRunnable processMessageObjectDeferred(final MessageObject mo) throws IOException {
		switch (mo.name) {
		case "PING":
			return new DeferredInvoke("PONG") {
				@Override
				protected void invoke() throws IOException {
					api.ping();
				}
			};
		case "LOGIN":
			final String user = mo.getString("user");
			final String passphrase = mo.getString("passphrase");
			final String version = mo.getString("version");
			return new DeferredCall() {
				@Override
				protected MessageObjectIO invoke() throws IOException {
					return api.login(user, passphrase, version);
				}
			};
		case "RELOGIN":
			final String sessionId = mo.getString("session");
			return new DeferredInvoke("WELCOME_BACK") {
				@Override
				protected void invoke() throws IOException {
					api.relogin(sessionId);
				}
			};
		case "LEAVE":
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.leave();
				}
			};
		case "QUERY_GAME_DEFINITION":
			return new DeferredCall() {
				@Override
				protected MessageObjectIO invoke() throws IOException {
					return api.getGameDefinition();
				}
			};
		case "MULTIPLAYER_USER":
			final MultiplayerUser userSettings = new MultiplayerUser();
			userSettings.fromMessage(mo);
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.choosePlayerSettings(userSettings);
				}
			};
		case "JOIN":
			return new DeferredCall() {
				@Override
				protected MessageObjectIO invoke() throws IOException {
					return api.join();
				}
			};
		case "READY":
			return new DeferredInvoke("BEGIN") {
				@Override
				protected void invoke() throws IOException {
					api.ready();
				}
			};
		case "QUERY_EMPIRE_STATUSES":
			return new DeferredCall() {
				@Override
				protected MessageObjectIO invoke() throws IOException {
					return api.getEmpireStatuses();
				}
			};
		case "QUERY_FLEETS":
			return new DeferredTransform<List<FleetStatus>>() {
				@Override
				protected List<FleetStatus> invoke() throws IOException {
					return api.getFleets();
				}
				@Override
				protected MessageSerializable transform(
						List<FleetStatus> intermediate) throws IOException {
					return FleetStatus.toArray(intermediate);
				}
			};
		case "QUERY_FLEET": {
			final int fleetId = mo.getInt("fleetId");
			return new DeferredCall() {
				@Override
				protected MessageObjectIO invoke() throws IOException {
					return api.getFleet(fleetId);
				}
			};
		}
		case "QUERY_INVENTORIES":
			return new DeferredTransform<Map<String, Integer>>() {
				@Override
				protected Map<String, Integer> invoke() throws IOException {
					return api.getInventory();
				}
				@Override
				protected MessageSerializable transform(
						Map<String, Integer> intermediate) throws IOException {
					MessageArray ma = new MessageArray("INVENTORIES");
					for (Map.Entry<String, Integer> e : intermediate.entrySet()) {
						MessageObject mo = new MessageObject("ENTRY");
						mo.set("type", e.getKey());
						mo.set("count", e.getValue());
						ma.add(mo);
					}
					return ma;
				}
			};
		case "QUERY_PRODUCTIONS":
			return new DeferredCall() {
				@Override
				protected MessageObjectIO invoke() throws IOException {
					return api.getProductions();
				}
			};
		case "QUERY_RESEARCHES": 
			return new DeferredCall() {
				@Override
				protected MessageObjectIO invoke() throws IOException {
					return api.getResearches();
				}
			};
		case "QUERY_PLANET_STATUSES":
			return new DeferredTransform<List<PlanetStatus>>() {
				@Override
				protected List<PlanetStatus> invoke() throws IOException {
					return api.getPlanetStatuses();
				}
				@Override
				protected MessageSerializable transform(
						List<PlanetStatus> intermediate) throws IOException {
					return PlanetStatus.toArray(intermediate);
				}
			};
		case "QUERY_PLANET_STATUS": {
			final String planetId = mo.getString("planetId");
			return new DeferredCall() {
				@Override
				protected MessageObjectIO invoke() throws IOException {
					return api.getPlanetStatus(planetId);
				}
			};
		}
		case "MOVE_FLEET": {
			final int fleetId = mo.getInt("fleetId");
			final double x = mo.getDouble("x");
			final double y = mo.getDouble("y");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.moveFleet(fleetId, x, y);
				}
			};
		}	
		case "ADD_FLEET_WAYPOINT": {
			final int fleetId = mo.getInt("fleetId");
			final double x = mo.getDouble("x");
			final double y = mo.getDouble("y");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.addFleetWaypoint(fleetId, x, y);
				}
			};
		}
		case "MOVE_TO_PLANET": {
			final int fleetId = mo.getInt("fleetId");
			final String target = mo.getString("target");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.moveToPlanet(fleetId, target);
				}
			};
		}
		case "FOLLOW_FLEET": {
			final int fleetId = mo.getInt("fleetId");
			final int target = mo.getInt("target");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.followFleet(fleetId, target);
				}
			};
		}
		case "ATTACK_FLEET": {
			final int fleetId = mo.getInt("fleetId");
			final int target = mo.getInt("target");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.attackFleet(fleetId, target);
				}
			};
		}
		case "ATTACK_PLANET": {
			final int fleetId = mo.getInt("fleetId");
			final String target = mo.getString("target");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.attackPlanet(fleetId, target);
				}
			};
		}
		case "COLONIZE_FLEET": {
			final int fleetId = mo.getInt("fleetId");
			final String target = mo.getString("target");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.colonize(fleetId, target);
				}
			};
		}
		case "NEW_FLEET_AT_PLANET": {
			final String planetId = mo.getString("planetId");
			final List<InventoryItemStatus> list = InventoryItemStatus.fromArray(mo.getArray("inventory"));
			return new DeferredTransform<Integer>() {
				@Override
				protected Integer invoke() throws IOException {
					return api.newFleet(planetId, list);
				}
				@Override
				protected MessageSerializable transform(Integer intermediate)
						throws IOException {
					MessageObject r = new MessageObject(FleetStatus.OBJECT_NAME);
					r.set("id", intermediate);
					return r;
				}
			};
		}
		case "NEW_FLEET_AT_FLEET": {
			final String fleetId = mo.getString("fleetId");
			final List<InventoryItemStatus> list = InventoryItemStatus.fromArray(mo.getArray("inventory"));
			return new DeferredTransform<Integer>() {
				@Override
				protected Integer invoke() throws IOException {
					return api.newFleet(fleetId, list);
				}
				@Override
				protected MessageSerializable transform(Integer intermediate)
						throws IOException {
					MessageObject r = new MessageObject(FleetStatus.OBJECT_NAME);
					r.set("id", intermediate);
					return r;
				}
			};
		}
		case "DELETE_FLEET": {
			final int fleetId = mo.getInt("fleetId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.deleteFleet(fleetId);
				}
			};
		}
		case "RENAME_FLEET": {
			final int fleetId = mo.getInt("fleetId");
			final String name = mo.getString("name");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.renameFleet(fleetId, name);
				}
			};
		}
		case "SELL_FLEET_ITEM": {
			final int fleetId = mo.getInt("fleetId");
			final int itemId = mo.getInt("itemId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.sellFleetItem(fleetId, itemId);
				}
			};
		}
		case "DEPLOY_FLEET_ITEM": {
			final int fleetId = mo.getInt("fleetId");
			final String type = mo.getString("type");
			return new DeferredTransform<Integer>() {
				@Override
				protected Integer invoke() throws IOException {
					return api.deployFleetItem(fleetId, type);
				}
				@Override
				protected MessageSerializable transform(Integer intermediate)
						throws IOException {
					MessageObject mo = new MessageObject(InventoryItemStatus.OBJECT_NAME);
					mo.set("id", intermediate);
					return mo;
				}
			};
		}
		case "UNDEPLOY_FLEET_ITEM": {
			final int fleetId = mo.getInt("fleetId");
			final int itemId = mo.getInt("itemId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.undeployFleetItem(fleetId, itemId);
				}
			};
		}
		case "ADD_FLEET_EQUIPMENT": {
			final int fleetId = mo.getInt("fleetId");
			final int itemId = mo.getInt("itemId");
			final String slotId = mo.getString("slotId");
			final String type = mo.getString("type");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.addFleetEquipment(fleetId, itemId, slotId, type);
				}
			};
		}
		case "REMOVE_FLEET_EQUIPMENT": {
			final int fleetId = mo.getInt("fleetId");
			final int itemId = mo.getInt("itemId");
			final String slotId = mo.getString("slotId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.removeFleetEquipment(fleetId, itemId, slotId);
				}
			};
		}
		case "FLEET_UPGRADE": {
			final int fleetId = mo.getInt("fleetId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.fleetUpgrade(fleetId);
				}
			};
		}
		case "STOP_FLEET": {
			final int fleetId = mo.getInt("fleetId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.stopFleet(fleetId);
				}
			};
		}
		case "TRANSFER": {
			final int sourceFleet = mo.getInt("source");
			final int destinationFleet = mo.getInt("destination");
			final int itemId = mo.getInt("itemId");
			final FleetTransferMode mode = mo.getEnum("mode", FleetTransferMode.values());
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.transfer(sourceFleet, destinationFleet, itemId, mode);
				}
			};
		}
		case "COLONIZE": {
			final String planetId = mo.getString("planetId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.colonize(planetId);
				}
			};
		}
		case "CANCEL_COLONIZE": {
			final String planetId = mo.getString("planetId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.cancelColonize(planetId);
				}
			};
		}
		case "BUILD_AT": {
			final String planetId = mo.getString("planetId");
			final String type = mo.getString("type");
			final String race = mo.getString("race");
			final int x = mo.getInt("x");
			final int y = mo.getInt("y");
			return new DeferredTransform<Integer>() {
				@Override
				protected Integer invoke() throws IOException {
					return api.build(planetId, type, race, x, y);
				}
				@Override
				protected MessageSerializable transform(Integer intermediate)
						throws IOException {
					MessageObject o = new MessageObject(BuildingStatus.OBJECT_NAME);
					o.set("id", intermediate);
					return o;
				}
			};
		}
		case "BUILD": {
			final String planetId = mo.getString("planetId");
			final String type = mo.getString("type");
			final String race = mo.getString("race");
			return new DeferredTransform<Integer>() {
				@Override
				protected Integer invoke() throws IOException {
					return api.build(planetId, type, race);
				}
				@Override
				protected MessageSerializable transform(Integer intermediate)
						throws IOException {
					MessageObject o = new MessageObject(BuildingStatus.OBJECT_NAME);
					o.set("id", intermediate);
					return o;
				}
			};
		}
		case "ENABLE": {
			final String planetId = mo.getString("planetId");
			final int buildingId = mo.getInt("buildingId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.enable(planetId, buildingId);
				}
			};
		}
		case "DISABLE": {
			final String planetId = mo.getString("planetId");
			final int buildingId = mo.getInt("buildingId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.disable(planetId, buildingId);
				}
			};
		}
		case "REPAIR": {
			final String planetId = mo.getString("planetId");
			final int buildingId = mo.getInt("buildingId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.repair(planetId, buildingId);
				}
			};
		}
		case "REPAIR_OFF": {
			final String planetId = mo.getString("planetId");
			final int buildingId = mo.getInt("buildingId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.repairOff(planetId, buildingId);
				}
			};
		}
		case "DEMOLISH": {
			final String planetId = mo.getString("planetId");
			final int buildingId = mo.getInt("buildingId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.demolish(planetId, buildingId);
				}
			};
		}
		case "BUILDING_UPGRADE": {
			final String planetId = mo.getString("planetId");
			final int buildingId = mo.getInt("buildingId");
			final int level = mo.getInt("level");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.buildingUpgrade(planetId, buildingId, level);
				}
			};
		}
		case "DEPLOY_PLANET_ITEM": {
			final String planetId = mo.getString("planetId");
			final String type = mo.getString("type");
			return new DeferredTransform<Integer>() {
				@Override
				protected Integer invoke() throws IOException {
					return api.deployPlanetItem(planetId, type);
				}
				@Override
				protected MessageSerializable transform(Integer intermediate)
						throws IOException {
					MessageObject o = new MessageObject(InventoryItemStatus.OBJECT_NAME);
					o.set("id", intermediate);
					return o;
				}
			};
		}
		case "UNDEPLOY_PLANET_ITEM": {
			final String planetId = mo.getString("planetId");
			final int itemId = mo.getInt("itemId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.undeployPlanetItem(planetId, itemId);
				}
			};
		}
		case "SELL_PLANET_ITEM": {
			final String planetId = mo.getString("planetId");
			final int itemId = mo.getInt("itemId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.sellPlanetItem(planetId, itemId);
				}
			};
		}
		case "ADD_PLANET_EQUIPMENT": {
			final String planetId = mo.getString("planetId");
			final int itemId = mo.getInt("itemId");
			final String slotId = mo.getString("slotId");
			final String type = mo.getString("type");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.addPlanetEquipment(planetId, itemId, slotId, type);
				}
			};
		}
		case "REMOVE_PLANET_EQUIPMENT": {
			final String planetId = mo.getString("planetId");
			final int itemId = mo.getInt("itemId");
			final String slotId = mo.getString("slotId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.removePlanetEquipment(planetId, itemId, slotId);
				}
			};
		}
		case "PLANET_UPGRADE": {
			final String planetId = mo.getString("planetId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.planetUpgrade(planetId);
				}
			};
		}
		case "START_PRODUCTION": {
			final String type = mo.getString("type");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.startProduction(type);
				}
			};
		}
		case "STOP_PRODUCTION": {
			final String type = mo.getString("type");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.stopProduction(type);
				}
			};
		}
		case "SET_PRODUCTION_QUANTITY": {
			final String type = mo.getString("type");
			final int count = mo.getInt("count");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.setProductionQuantity(type, count);
				}
			};
		}
		case "SET_PRODUCTION_PRIORITY": {
			final String type = mo.getString("type");
			final int priority = mo.getInt("priority");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.setProductionQuantity(type, priority);
				}
			};
		}
		case "SELL_INVENTORY": {
			final String type = mo.getString("type");
			final int count = mo.getInt("count");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.sellInventory(type, count);
				}
			};
		}
		case "START_RESEARCH": {
			final String type = mo.getString("type");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.startResearch(type);
				}
			};
		}
		case "STOP_RESEARCH": {
			final String type = mo.getString("type");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.stopResearch(type);
				}
			};
		}
		case "SET_RESEARCH_MONEY": {
			final String type = mo.getString("type");
			final int money = mo.getInt("money");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.setResearchMoney(type, money);
				}
			};
		}
		case "PAUSE_RESEARCH": {
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.pauseResearch();
				}
			};
		}
		case "PAUSE_PRODUCTION": {
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.pauseProduction();
				}
			};
		}
		case "UNPAUSE_PRODUCTION": {
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.unpauseProduction();
				}
			};
		}
		case "UNPAUSE_RESEARCH": {
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.unpauseResearch();
				}
			};
		}
		case "STOP_SPACE_UNIT": {
			final int battleId = mo.getInt("battleId");
			final int unitId = mo.getInt("unitId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.stopSpaceUnit(battleId, unitId);
				}
			};
		}
		case "MOVE_SPACE_UNIT": {
			final int battleId = mo.getInt("battleId");
			final int unitId = mo.getInt("unitId");
			final double x = mo.getDouble("x");
			final double y = mo.getDouble("y");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.moveSpaceUnit(battleId, unitId, x, y);
				}
			};
		}
		case "ATTACK_SPACE_UNIT": {
			final int battleId = mo.getInt("battleId");
			final int unitId = mo.getInt("unitId");
			final int target = mo.getInt("target");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.attackSpaceUnit(battleId, unitId, target);
				}
			};
		}
		case "KAMIKAZE_SPACE_UNIT": {
			final int battleId = mo.getInt("battleId");
			final int unitId = mo.getInt("unitId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.kamikazeSpaceUnit(battleId, unitId);
				}
			};
		}
		case "FIRE_SPACE_ROCKET": {
			final int battleId = mo.getInt("battleId");
			final int unitId = mo.getInt("unitId");
			final int target = mo.getInt("target");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.fireSpaceRocket(battleId, unitId, target);
				}
			};
		}
		case "SPACE_RETREAT": {
			final int battleId = mo.getInt("battleId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.spaceRetreat(battleId);
				}
			};
		}
		case "STOP_SPACE_RETREAT": {
			final int battleId = mo.getInt("battleId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.stopSpaceRetreat(battleId);
				}
			};
		}
		case "FLEET_FORMATION": {
			final int fleetId = mo.getInt("fleetId");
			final int formation = mo.getInt("formation");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.fleetFormation(fleetId, formation);
				}
			};
		}
		case "QUERY_BATTLES": {
			return new DeferredTransform<List<BattleStatus>>() {
				@Override
				protected List<BattleStatus> invoke() throws IOException {
					return api.getBattles();
				}
				@Override
				protected MessageSerializable transform(
						List<BattleStatus> intermediate) throws IOException {
					return BattleStatus.toArray(intermediate);
				}
			};
		}
		case "QUERY_BATTLE": {
			final int battleId = mo.getInt("battleId");
			return new DeferredCall() {
				@Override
				protected MessageObjectIO invoke() throws IOException {
					return api.getBattle(battleId);
				}
			};
		}
		case "QUERY_SPACE_BATTLE_UNITS": {
			final int battleId = mo.getInt("battleId");
			return new DeferredTransform<List<SpaceBattleUnit>>() {
				@Override
				protected List<SpaceBattleUnit> invoke() throws IOException {
					return api.getSpaceBattleUnits(battleId);
				}
				@Override
				protected MessageSerializable transform(
						List<SpaceBattleUnit> intermediate) throws IOException {
					return SpaceBattleUnit.toArray(intermediate);
				}
			};
		}
		case "STOP_GROUND_UNIT": {
			final int battleId = mo.getInt("battleId");
			final int unitId = mo.getInt("unitId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.stopGroundUnit(battleId, unitId);
				}
			};
		}
		case "MOVE_GROUND_UNIT": {
			final int battleId = mo.getInt("battleId");
			final int unitId = mo.getInt("unitId");
			final int x = mo.getInt("x");
			final int y = mo.getInt("y");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.moveGroundUnit(battleId, unitId, x, y);
				}
			};
		}
		case "ATTACK_GROUND_UNIT": {
			final int battleId = mo.getInt("battleId");
			final int unitId = mo.getInt("unitId");
			final int target = mo.getInt("target");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.attackGroundUnit(battleId, unitId, target);
				}
			};
		}
		case "ATTACK_BUILDING": {
			final int battleId = mo.getInt("battleId");
			final int unitId = mo.getInt("unitId");
			final int buildingId = mo.getInt("buildingId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.attackBuilding(battleId, unitId, buildingId);
				}
			};
		}
		case "DEPLOY_MINE": {
			final int battleId = mo.getInt("battleId");
			final int unitId = mo.getInt("unitId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.deployMine(battleId, unitId);
				}
			};
		}
		case "GROUND_RETREAT": {
			final int battleId = mo.getInt("battleId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.groundRetreat(battleId);
				}
			};
		}
		case "STOP_GROUND_RETREAT": {
			final int battleId = mo.getInt("battleId");
			return new DeferredInvoke() {
				@Override
				protected void invoke() throws IOException {
					api.stopGroundRetreat(battleId);
				}
			};
		}
		case "QUERY_GROUND_BATTLE_UNITS": {
			final int battleId = mo.getInt("battleId");
			return new DeferredTransform<List<GroundwarUnitStatus>>() {
				@Override
				protected List<GroundwarUnitStatus> invoke() throws IOException {
					return api.getGroundBattleUnits(battleId);
				}
				@Override
				protected MessageSerializable transform(
						List<GroundwarUnitStatus> intermediate) throws IOException {
					return GroundwarUnitStatus.toArray(intermediate);
				}
			};
		}
		case "QUERY_FLEET_INVENTORY": {
			final int fleetId = mo.getInt("fleetId");
			final int itemId = mo.getInt("itemId");
			return new DeferredCall() {
				@Override
				protected MessageObjectIO invoke() throws IOException {
					return api.getInventoryStatus(fleetId, itemId);
				}
			};
		}
		case "QUERY_PLANET_INVENTORY": {
			final String planetId = mo.getString("planetId");
			final int itemId = mo.getInt("itemId");
			return new DeferredCall() {
				@Override
				protected MessageObjectIO invoke() throws IOException {
					return api.getInventoryStatus(planetId, itemId);
				}
			};
		}
		case "SET_PLANET_AUTOBUILD": {
			final String planetId = mo.getStringObject("planetId");
			final AutoBuild auto = mo.getEnum("auto", AutoBuild.values());
			return new DeferredInvoke() {
				@Override
				public void invoke() throws IOException {
					api.setAutoBuild(planetId, auto);
				}
			};
		}
		case "SET_PLANET_TAX": {
			final String planetId = mo.getStringObject("planetId");
			final TaxLevel tax = mo.getEnum("tax", TaxLevel.values());
			return new DeferredInvoke() {
				@Override
				public void invoke() throws IOException {
					api.setTaxLevel(planetId, tax);
				}
			};
		}
		default:
			throw new ErrorResponse(ErrorType.UNKNOWN_MESSAGE, mo != null ? mo.name : "null");
		}
	}
}
