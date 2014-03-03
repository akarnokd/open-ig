/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Action1E;
import hu.openig.core.AsyncResult;
import hu.openig.core.AsyncTransform;
import hu.openig.model.AutoBuild;
import hu.openig.model.BattleStatus;
import hu.openig.model.EmpireStatuses;
import hu.openig.model.FleetStatus;
import hu.openig.model.FleetTransferMode;
import hu.openig.model.GroundwarUnitStatus;
import hu.openig.model.InventoryItemStatus;
import hu.openig.model.MessageArrayAsync;
import hu.openig.model.MessageArrayItemFactory;
import hu.openig.model.MessageObjectAsync;
import hu.openig.model.MessageObjectIO;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.MultiplayerGameSetup;
import hu.openig.model.MultiplayerUser;
import hu.openig.model.PlanetStatus;
import hu.openig.model.ProductionStatuses;
import hu.openig.model.RemoteGameAsyncAPI;
import hu.openig.model.ResearchStatuses;
import hu.openig.model.SpaceBattleUnit;
import hu.openig.model.TaxLevel;
import hu.openig.model.VoidAsync;
import hu.openig.model.WelcomeResponse;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageClientAPI;
import hu.openig.net.MessageObject;
import hu.openig.net.MessageSerializable;
import hu.openig.net.MessageUtils;
import hu.openig.utils.U;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Asynchronous game client.
 * @author akarnokd, 2013.04.30.
 *
 */
public class RemoteGameAsyncClient implements RemoteGameAsyncAPI {
	/**
	 * Dispatches a list of values to a list of async
	 * result receivers and notifies another async
	 * result when the whole batch has been dispatched.
	 * @author akarnokd, 2013.05.01.
	 */
	public static final class BatchResultAsync implements AsyncResult<Object, IOException>,
	Action1E<Object, IOException> {
		/** The list of async results to notify. */
		private final List<AsyncResult<Object, ? super IOException>> callbacks;
		/** The individually parsed results. */
		private final List<Object> results;
		/** The completion notification. */
		private final AsyncResult<? super Void, ? super IOException> out;
		
		/**
		 * Constructor. Initializes the fields.
		 * @param out the notification for the error response or
		 * the completion of the notification of all callbacks
		 * @param callbacks the list async results to notify
		 */
		public BatchResultAsync(
				AsyncResult<? super Void, ? super IOException> out,
				List<AsyncResult<Object, ? super IOException>> callbacks) {
			this.out = out;
			this.callbacks = U.newArrayList(callbacks);
			this.results = new ArrayList<>(callbacks.size());
		}
		@Override
		public void invoke(Object param1) throws IOException {
			MessageArray ma = MessageUtils.expectArray(param1, "BATCH_RESPONSE");
			int i = 0;
			for (Object o : ma) {
				AsyncResult<Object, ? super IOException> ar = callbacks.get(i);
				MessageUtils.applyTransform(ar, o);
				results.add(o);
				i++;
			}
		}
		@Override
		public void onSuccess(Object value) {
			int i = 0;
			for (Object o : results) {
				callbacks.get(i).onSuccess(o);
				i++;
			}
			if (out != null) {
				out.onSuccess(null);
			}
		}
		@Override
		public void onError(IOException ex) {
			if (out != null) {
				out.onError(ex);
			}
		}
	}

	/** The message client object. */
	protected final MessageClientAPI client;
	/** The list of callbacks filled in during batch operation. */
	protected List<AsyncResult<Object, ? super IOException>> callbacks;
	/** The composed batch request. */
	protected MessageArray batchRequest;
	/**
	 * Constructor. Sets the message client object.
	 * @param client the client object
	 */
	public RemoteGameAsyncClient(MessageClientAPI client) {
		this.client = client;
	}

	@Override
	public void begin() {
		if (isBatchMode()) {
			throw new IllegalStateException("Already in batch mode!");
		}
		callbacks = new ArrayList<>();
		batchRequest = new MessageArray("BATCH");
	}

	@Override
	public void end() throws IOException {
		if (!isBatchMode()) {
			throw new IllegalStateException("Not in batch mode!");
		}
		
		client.query(batchRequest, new BatchResultAsync(null, callbacks));

		batchRequest = null;
		callbacks = null;
	}

	@Override
	public void end(final AsyncResult<? super Void, ? super IOException> out) {
		if (!isBatchMode()) {
			throw new IllegalStateException("Not in batch mode!");
		}
		client.query(batchRequest, new BatchResultAsync(null, callbacks));
		
		batchRequest = null;
		callbacks = null;

	}
	@Override
	public void cancel() {
		if (!isBatchMode()) {
			throw new IllegalStateException("Not in batch mode!");
		}
		batchRequest = null;
		callbacks = null;
	}
	/**
	 * Test if we are in batch mode.
	 * @return true if in batch mode
	 */
	protected boolean isBatchMode() {
		return callbacks != null;
	}
	/**
	 * Sends out the request or adds it to the batch
	 * request list, depending on the current mode.
	 * @param request the request object
	 * @param out the async result callback
	 */
	protected void sendDirect(
			MessageSerializable request, 
			AsyncResult<Object, ? super IOException> out) {
		if (isBatchMode()) {
			batchRequest.add(request);
			callbacks.add(out);
		} else {
			client.query(request, out);
		}
	}
	/**
	 * Execute a query and parse the result into the given
	 * object.
	 * @param <T> the message object I/O type
	 * @param request the request message
	 * @param out the async result
	 * @param result the result object that will be loaded with the result value
	 */
	protected <T extends MessageObjectIO> void query(
			MessageSerializable request,
			AsyncResult<? super T, ? super IOException> out,
			T result) {
		sendDirect(request, new MessageObjectAsync<>(out, result, result.objectName()));
	}
	/**
	 * Execute a query and parse the result into a list.
	 * @param <T> the message object I/O type
	 * @param request the request message
	 * @param out the async result
	 * @param itemFactory the factory to produce list items
	 */
	protected <T extends MessageObjectIO> void queryList(
			MessageSerializable request,
			AsyncResult<? super List<T>, ? super IOException> out,
			MessageArrayItemFactory<? extends T> itemFactory) {
		sendDirect(request, new MessageArrayAsync<>(out, itemFactory, itemFactory.arrayName()));
	}
	/**
	 * Sends a requests and expects a single OK response.
	 * @param request the request object
	 * @param out the async result to notifiy
	 */
	protected void send(MessageSerializable request, AsyncResult<? super Void, ? super IOException> out) {
		send(request, out, "OK");
	}
	/**
	 * Sends a requests and expects a response which has name in the provided names list.
	 * @param request the request object
	 * @param out the async result to notifiy
	 * @param names the array of names to accept
	 */
	protected void send(MessageSerializable request, AsyncResult<? super Void, ? super IOException> out, String... names) {
		sendDirect(request, new VoidAsync(out, names));
	}
	@Override
	public void ping(AsyncResult<? super Long, ? super IOException> out) {
		final long time = System.nanoTime();
		MessageObject request = new MessageObject("PING");
		AsyncTransform<Object, Long, IOException> tr = new AsyncTransform<Object, Long, IOException>(out) {
			@Override
			public void invoke(Object value) {
				this.setValue((System.nanoTime() - time) / 1000000);
			}
		};
		sendDirect(request, tr);
	}
	@Override
	public void login(String user, String passphrase, String version,
			AsyncResult<? super WelcomeResponse, ? super IOException> out) {
		MessageObject request = new MessageObject("LOGIN")
		.set("user", user)
		.set("passphrase", passphrase)
		.set("version", version);
		query(request, out, new WelcomeResponse());
	}
	@Override
	public void relogin(String sessionId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("RELOGIN")
		.set("session", sessionId);
		send(request, out, "WELCOME_BACK");
	}

	@Override
	public void leave(AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("LEAVE");
		send(request, out);
	}

	@Override
	public void getGameDefinition(
			AsyncResult<? super MultiplayerDefinition, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_GAME_DEFINITION");
		query(request, out, new MultiplayerDefinition());
	}
	@Override
	public void choosePlayerSettings(MultiplayerUser user,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = user.toMessage();
		send(request, out);
	}

	@Override
	public void join(
			AsyncResult<? super MultiplayerGameSetup, ? super IOException> out) {
		MessageObject request = new MessageObject("JOIN");
		query(request, out, new MultiplayerGameSetup());
	}

	@Override
	public void ready(AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("READY");
		send(request, out, "BEGIN");
	}

	@Override
	public void getEmpireStatuses(
			AsyncResult<? super EmpireStatuses, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_EMPIRE_STATUSES");
		query(request, out, new EmpireStatuses());
	}

	@Override
	public void getFleets(
			AsyncResult<? super List<FleetStatus>, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_FLEETS");
		queryList(request, out, new FleetStatus());
	}

	@Override
	public void getFleet(int fleetId,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_FLEET")
		.set("fleetId", fleetId);
		query(request, out, new FleetStatus());
	}

	@Override
	public void getInventory(
			AsyncResult<? super Map<String, Integer>, ? super IOException> out) {
		AsyncTransform<Object, Map<String, Integer>, IOException> at = new AsyncTransform<Object, Map<String, Integer>, IOException>(out) {
			@Override
			public void invoke(Object param1) throws IOException {
				MessageArray ma = MessageUtils.expectArray(param1, "INVENTORIES");
				
				Map<String, Integer> map = new HashMap<>();
				
				for (Object o : ma) {
					MessageObject mo = MessageUtils.expectObject(o, "ENTRY");
					
					map.put(mo.getString("type"), mo.getInt("count"));
				}
				
				setValue(map);
			}
		};
		MessageObject request = new MessageObject("QUERY_INVENTORIES");
		sendDirect(request, at);
	}

	@Override
	public void getProductions(
			AsyncResult<? super ProductionStatuses, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_PRODUCTIONS");
		query(request, out, new ProductionStatuses());
	}

	@Override
	public void getResearches(
			AsyncResult<? super ResearchStatuses, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_RESEARCHES");
		query(request, out, new ResearchStatuses());
	}

	@Override
	public void getPlanetStatuses(
			AsyncResult<? super List<PlanetStatus>, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_PLANET_STATUSES");
		queryList(request, out, new PlanetStatus());
	}

	@Override
	public void getPlanetStatus(String id,
			AsyncResult<? super PlanetStatus, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_PLANET_STATUS")
		.set("planetId", id);
		query(request, out, new PlanetStatus());
	}

	@Override
	public void moveFleet(int id, double x, double y,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("MOVE_FLEET")
		.set("fleetId", id)
		.set("x", x)
		.set("y", y);
		send(request, out);
		
	}

	@Override
	public void addFleetWaypoint(int id, double x, double y,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ADD_FLEET_WAYPOINT")
		.set("fleetId", id)
		.set("x", x)
		.set("y", y);
		send(request, out);
	}

	@Override
	public void moveToPlanet(int id, String target,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("MOVE_TO_PLANET")
		.set("fleetId", id)
		.set("target", target);
		send(request, out);
	}

	@Override
	public void followFleet(int id, int target,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("FOLLOW_FLEET")
		.set("fleetId", id)
		.set("target", target);
		send(request, out);
	}

	@Override
	public void attackFleet(int id, int target,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ATTACK_FLEET")
		.set("fleetId", id)
		.set("target", target);
		send(request, out);
	}

	@Override
	public void attackPlanet(int id, String target,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ATTACK_PLANET")
		.set("fleetId", id)
		.set("target", target);
		send(request, out);
	}

	@Override
	public void colonize(int id, String target,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("COLONIZE_FLEET")
		.set("fleetId", id)
		.set("target", target);
		send(request, out);
	}

	@Override
	public void newFleet(String planet, List<InventoryItemStatus> inventory,
			AsyncResult<? super Integer, ? super IOException> out) {
		AsyncTransform<Object, Integer, IOException> at = new AsyncTransform<Object, Integer, IOException>(out) {
			@Override
			public void invoke(Object param1) throws IOException {
				MessageObject mo = MessageUtils.expectObject(param1, "FLEET");
				setValue(mo.getInt("fleetId"));
			}
		};
		MessageObject request = new MessageObject("NEW_FLEET_AT_PLANET")
		.set("planetId", planet);
		sendFleetConfig(inventory, at, request);
	}

	/**
	 * Send a fleet configuration request.
	 * @param inventory the inventory describing the fleet contents
	 * @param out the async result
	 * @param mo the request object to fill in and send
	 */
	private void sendFleetConfig(List<InventoryItemStatus> inventory,
			AsyncTransform<Object, Integer, IOException> out,
			MessageObject mo) {
		MessageArray inv = new MessageArray(null);
		mo.set("inventory", inv);
		for (InventoryItemStatus ii : inventory) {
			inv.add(ii.toMessage());
		}
		sendDirect(mo, out);
	}

	@Override
	public void newFleet(int id, List<InventoryItemStatus> inventory,
			AsyncResult<? super Integer, ? super IOException> out) {
		AsyncTransform<Object, Integer, IOException> at = new AsyncTransform<Object, Integer, IOException>(out) {
			@Override
			public void invoke(Object param1) throws IOException {
				MessageObject mo = MessageUtils.expectObject(param1, "FLEET");
				setValue(mo.getInt("fleetId"));
			}
		};
		MessageObject mo = new MessageObject("NEW_FLEET_AT_FLEET")
		.set("fleetId", id);
		sendFleetConfig(inventory, at, mo);
	}

	@Override
	public void deleteFleet(int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("DELETE_FLEET")
		.set("fleetId", id);
		send(request, out);
	}

	@Override
	public void renameFleet(int id, String name,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("RENAME_FLEET")
		.set("fleetId", id)
		.set("name", name);
		send(request, out);
	}

	@Override
	public void sellFleetItem(int id, int itemId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SELL_FLEET_ITEM")
		.set("fleetId", id)
		.set("itemId", itemId);
		send(request, out);
	}

	@Override
	public void deployFleetItem(int id, String type,
			AsyncResult<? super Integer, ? super IOException> out) {
		AsyncTransform<Object, Integer, IOException> at = new AsyncTransform<Object, Integer, IOException>(out) {
			@Override
			public void invoke(Object param1) throws IOException {
				MessageObject mo = MessageUtils.expectObject(param1, "INVENTORY");
				setValue(mo.getInt("id"));
			}
		};
		MessageObject request = new MessageObject("DEPLOY_FLEET_ITEM")
		.set("fleetId", id)
		.set("type", type);
		sendDirect(request, at);
	}

	@Override
	public void undeployFleetItem(int id, int itemId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("UNDEPLOY_FLEET_ITEM")
		.set("fleetId", id)
		.set("itemId", itemId);
		send(request, out);
	}

	@Override
	public void addFleetEquipment(int id, int itemId, String slotId,
			String type,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ADD_FLEET_EQUIPMENT")
		.set("fleetId", id)
		.set("itemId", itemId)
		.set("slotId", slotId)
		.set("type", type);
		send(request, out);
	}

	@Override
	public void removeFleetEquipment(int id, int itemId, String slotId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("REMOVE_FLEET_EQUIPMENT")
		.set("fleetId", id)
		.set("itemId", itemId)
		.set("slotId", slotId);
		send(request, out);
	}

	@Override
	public void fleetUpgrade(int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("FLEET_UPGRADE")
		.set("fleetId", id);
		send(request, out);
	}

	@Override
	public void stopFleet(int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_FLEET")
		.set("fleetId", id);
		send(request, out);
	}

	@Override
	public void transfer(int sourceFleet, int destinationFleet, int sourceItem,
			FleetTransferMode mode,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("TRANSFER")
		.set("source", sourceFleet)
		.set("destination", destinationFleet)
		.set("itemId", sourceItem)
		.set("mode", mode);
		send(request, out);
	}

	@Override
	public void colonize(String id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("COLONIZE")
		.set("planetId", id);
		send(request, out);
	}

	@Override
	public void cancelColonize(String id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("CANCEL_COLONIZE")
		.set("planetId", id);
		send(request, out);
	}

	@Override
	public void build(String planetId, String type, String race, int x, int y,
			AsyncResult<? super Integer, ? super IOException> out) {
		AsyncTransform<Object, Integer, IOException> at = new AsyncTransform<Object, Integer, IOException>(out) {
			@Override
			public void invoke(Object param1) throws IOException {
				MessageObject mo = MessageUtils.expectObject(param1, "BUILDING");
				setValue(mo.getInt("buildingId"));
			}
		};
		MessageObject request = new MessageObject("BUILD_AT")
		.set("planetId", planetId)
		.set("type", type)
		.set("race", race)
		.set("x", x)
		.set("y", y);
		sendDirect(request, at);
	}

	@Override
	public void build(String planetId, String type, String race,
			AsyncResult<? super Integer, ? super IOException> out) {
		AsyncTransform<Object, Integer, IOException> at = new AsyncTransform<Object, Integer, IOException>(out) {
			@Override
			public void invoke(Object param1) throws IOException {
				MessageObject mo = MessageUtils.expectObject(param1, "BUILDING");
				setValue(mo.getInt("buildingId"));
			}
		};
		MessageObject request = new MessageObject("BUILD")
		.set("planetId", planetId)
		.set("type", type)
		.set("race", race);
		sendDirect(request, at);
	}

	@Override
	public void enable(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ENABLE")
		.set("planetId", planetId)
		.set("buildingId", id);
		send(request, out);
	}

	@Override
	public void disable(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("DISABLE")
		.set("planetId", planetId)
		.set("buildingId", id);
		send(request, out);
	}

	@Override
	public void repair(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("REPAIR")
		.set("planetId", planetId)
		.set("buildingId", id);
		send(request, out);
	}

	@Override
	public void repairOff(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("REPAIR_OFF")
		.set("planetId", planetId)
		.set("buildingId", id);
		send(request, out);
	}

	@Override
	public void demolish(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("DEMOLISH")
		.set("planetId", planetId)
		.set("buildingId", id);
		send(request, out);
	}

	@Override
	public void buildingUpgrade(String planetId, int id, int level,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("BUILDING_UPGRADE")
		.set("planetId", planetId)
		.set("buildingId", id)
		.set("level", level);
		send(request, out);
	}

	@Override
	public void deployPlanetItem(String planetId, String type,
			AsyncResult<? super Integer, ? super IOException> out) {
		AsyncTransform<Object, Integer, IOException> at = new AsyncTransform<Object, Integer, IOException>(out) {
			@Override
			public void invoke(Object param1) throws IOException {
				MessageObject mo = MessageUtils.expectObject(param1, InventoryItemStatus.OBJECT_NAME);
				setValue(mo.getInt("id"));
			}
		};
		MessageObject request = new MessageObject("DEPLOY_PLANET_ITEM")
		.set("planetId", planetId)
		.set("type", type);
		sendDirect(request, at);
	}

	@Override
	public void undeployPlanetItem(String planetId, int itemId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("UNDEPLOY_PLANET_ITEM")
		.set("planetId", planetId)
		.set("itemId", itemId);
		send(request, out);
	}

	@Override
	public void sellPlanetItem(String planetId, int itemId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SELL_PLANET_ITEM")
		.set("planetId", planetId)
		.set("itemId", itemId);
		send(request, out);
	}

	@Override
	public void addPlanetEquipment(String planetId, int itemId, String slotId,
			String type,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ADD_PLANET_EQUIPMENT")
		.set("planetId", planetId)
		.set("itemId", itemId)
		.set("slotId", slotId)
		.set("type", type);
		send(request, out);
	}

	@Override
	public void removePlanetEquipment(String planetId, int itemId,
			String slotId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("REMOVE_PLANET_EQUIPMENT")
		.set("planetId", planetId)
		.set("itemId", itemId)
		.set("slotId", slotId);
		send(request, out);
	}

	@Override
	public void planetUpgrade(String planetId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("PLANET_UPGRADE")
		.set("planetId", planetId);
		send(request, out);
	}

	@Override
	public void startProduction(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("START_PRODUCTION")
		.set("type", type);
		send(request, out);
	}

	@Override
	public void stopProduction(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_PRODUCTION")
		.set("type", type);
		send(request, out);
	}

	@Override
	public void setProductionQuantity(String type, int count,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SET_PRODUCTION_QUANTITY")
		.set("type", type)
		.set("count", count);
		send(request, out);
	}

	@Override
	public void setProductionPriority(String type, int priority,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SET_PRODUCTION_PRIORITY")
		.set("type", type)
		.set("priority", priority);
		send(request, out);
	}

	@Override
	public void sellInventory(String type, int count,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SELL_INVENTORY")
		.set("type", type)
		.set("count", count);
		send(request, out);
	}

	@Override
	public void startResearch(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("START_RESEARCH")
		.set("type", type);
		send(request, out);
	}

	@Override
	public void stopResearch(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_RESEARCH")
		.set("type", type);
		send(request, out);
	}

	@Override
	public void setResearchMoney(String type, int money,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SET_RESEARCH_MONEY")
		.set("type", type)
		.set("money", money);
		send(request, out);
	}

	@Override
	public void pauseResearch(AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("PAUSE_RESEARCH");
		send(request, out);
	}

	@Override
	public void pauseProduction(
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("PAUSE_PRODUCTION");
		send(request, out);
	}

	@Override
	public void unpauseProduction(
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("UNPAUSE_PRODUCTION");
		send(request, out);
	}

	@Override
	public void unpauseResearch(
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("UNPAUSE_RESEARCH");
		send(request, out);
	}

	@Override
	public void stopSpaceUnit(int battleId, int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_SPACE_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId);
		send(request, out);
	}

	@Override
	public void moveSpaceUnit(int battleId, int unitId, double x, double y,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("MOVE_SPACE_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId)
		.set("x", x)
		.set("y", y);
		send(request, out);
	}

	@Override
	public void attackSpaceUnit(int battleId, int unitId, int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ATTACK_SPACE_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId)
		.set("target", targetUnitId);
		send(request, out);
	}

	@Override
	public void kamikazeSpaceUnit(int battleId, int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("KAMIKAZE_SPACE_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId);
		send(request, out);
	}

	@Override
	public void fireSpaceRocket(int battleId, int unitId, int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("FIRE_SPACE_ROCKET")
		.set("battleId", battleId)
		.set("unitId", unitId)
		.set("target", targetUnitId);
		send(request, out);
	}

	@Override
	public void spaceRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SPACE_RETREAT")
		.set("battleId", battleId);
		send(request, out);
	}

	@Override
	public void stopSpaceRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_SPACE_RETREAT")
		.set("battleId", battleId);
		send(request, out);
	}

	@Override
	public void fleetFormation(int fleetId, int formation,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("FLEET_FORMATION")
		.set("fleetId", fleetId)
		.set("formation", formation);
		send(request, out);
	}

	@Override
	public void getBattles(
			AsyncResult<? super List<BattleStatus>, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_BATTLES");
		queryList(request, out, new BattleStatus());
	}

	@Override
	public void getBattle(int battleId,
			AsyncResult<? super BattleStatus, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_BATTLE")
		.set("battleId", battleId);
		query(request, out, new BattleStatus());
	}

	@Override
	public void getSpaceBattleUnits(int battleId,
			AsyncResult<? super List<SpaceBattleUnit>, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_SPACE_BATTLE_UNITS")
		.set("battleId", battleId);
		queryList(request, out, new SpaceBattleUnit());
	}

	@Override
	public void stopGroundUnit(int battleId, int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_GROUND_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId);
		send(request, out);
	}

	@Override
	public void moveGroundUnit(int battleId, int unitId, int x, int y,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("MOVE_GROUND_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId)
		.set("x", x)
		.set("y", y);
		send(request, out);
	}

	@Override
	public void attackGroundUnit(int battleId, int unitId, int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ATTACK_GROUND_UNIT")
		.set("battleId", battleId)
		.set("unitId", unitId)
		.set("target", targetUnitId);
		send(request, out);
	}

	@Override
	public void attackBuilding(int battleId, int unitId, int buildingId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ATTACK_BUILDING")
		.set("battleId", battleId)
		.set("unitId", unitId)
		.set("buildingId", buildingId);
		send(request, out);
	}

	@Override
	public void deployMine(int battleId, int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("DEPLOY_MINE")
		.set("battleId", battleId)
		.set("unitId", unitId);
		send(request, out);
	}

	@Override
	public void groundRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("GROUND_RETREAT")
		.set("battleId", battleId);
		send(request, out);
	}

	@Override
	public void stopGroundRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_GROUND_RETREAT")
		.set("battleId", battleId);
		send(request, out);
	}

	@Override
	public void getGroundBattleUnits(int battleId,
			AsyncResult<? super List<GroundwarUnitStatus>, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_GROUND_BATTLE_UNITS")
		.set("battleId", battleId);
		queryList(request, out, new GroundwarUnitStatus());
	}
	@Override
	public void getInventoryStatus(int fleetId, int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_FLEET_INVENTORY")
		.set("fleetId", fleetId)
		.set("itemId", itemId)
		;
		query(request, out, new InventoryItemStatus());
	}
	@Override
	public void getInventoryStatus(String planetId, int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_PLANET_INVENTORY")
		.set("planetId", planetId)
		.set("itemId", itemId)
		;
		query(request, out, new InventoryItemStatus());
	}
	@Override
	public void setAutoBuild(String planetId, AutoBuild auto,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SET_PLANET_AUTOBUILD")
		.set("planetId", planetId)
		.set("auto", auto)
		;
		send(request, out);
		
	}
	@Override
	public void setTaxLevel(String planetId, TaxLevel tax,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SET_PLANET_TAX")
		.set("planetId", planetId)
		.set("tax", tax)
		;
		send(request, out);
	}
}
