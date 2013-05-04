/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Action1;
import hu.openig.core.AsyncException;
import hu.openig.core.AsyncResult;
import hu.openig.core.AsyncTransform;
import hu.openig.core.Func0;
import hu.openig.core.Scheduler;
import hu.openig.mechanics.GameAsyncAPI;
import hu.openig.model.BattleStatus;
import hu.openig.model.EmpireStatuses;
import hu.openig.model.FleetStatus;
import hu.openig.model.FleetTransferMode;
import hu.openig.model.GroundBattleUnit;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventoryItemStatus;
import hu.openig.model.InventorySlot;
import hu.openig.model.MessageArrayAsync;
import hu.openig.model.MessageObjectAsync;
import hu.openig.model.MessageObjectIO;
import hu.openig.model.MessageUtils;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.MultiplayerGameSetup;
import hu.openig.model.MultiplayerUser;
import hu.openig.model.PlanetStatus;
import hu.openig.model.ProductionStatus;
import hu.openig.model.ResearchStatus;
import hu.openig.model.SpaceBattleUnit;
import hu.openig.model.VoidAsync;
import hu.openig.model.WelcomeResponse;
import hu.openig.net.ErrorResponse;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageClientAPI;
import hu.openig.net.MessageObject;
import hu.openig.net.MessageSerializable;
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
public class RemoteGameAsyncClient implements GameAsyncAPI {
	/**
	 * Dispatches a list of values to a list of async
	 * result receivers and notifies another async
	 * result when the whole batch has been dispatched.
	 * @author akarnokd, 2013.05.01.
	 */
	public static final class BatchResultAsync implements Runnable {
		/** The end notification async result if not null. */
		private final AsyncResult<? super Void, ? super IOException> out;
		/** The list of async results to notify. */
		private final List<AsyncResult<Object, ? super IOException>> callbacks;
		/** The list of results to dispatch. */
		private final List<Object> results;

		/**
		 * Constructor. Initializes the fields.
		 * @param out the end notification async result, may be null
		 * @param callbacks the list async results to notify
		 * @param ma the batch result array
		 */
		public BatchResultAsync(
				AsyncResult<? super Void, ? super IOException> out,
				List<AsyncResult<Object, ? super IOException>> callbacks,
				MessageArray ma) {
			this.out = out;
			this.callbacks = U.newArrayList(callbacks);
			this.results = new ArrayList<Object>(ma.size());
			
			int max = Math.max(ma.size(), this.callbacks.size());
			for (int i = 0; i < max; i++) {
				AsyncResult<Object, ? super IOException> ar = this.callbacks.get(i);
				Object o = ma.get(i);
				ErrorResponse er = ErrorResponse.asError(o);
				if (er != null) {
					results.add(er);
				} else {
					if (ar instanceof Action1<?>) {
						@SuppressWarnings("unchecked")
						Action1<Object> func1 = (Action1<Object>) ar;
						func1.invoke(o);
					}
					results.add(o);
				}
			}
		}

		@Override
		public void run() {
			for (int i = 0; i < results.size(); i++) {
				AsyncResult<Object, ? super IOException> ar = callbacks.get(i);
				Object o = results.get(i);
				if (o instanceof IOException) {
					ar.onError((IOException)o);
				} else {
					ar.onSuccess(o);
				}
			}
			if (out != null) {
				out.onSuccess(null);
			}
		}
	}

	/** The message client object. */
	protected final MessageClientAPI client;
	/** The scheduler used to dispatch results. */
	protected final Scheduler scheduler;
	/** The list of callbacks filled in during batch operation. */
	protected List<AsyncResult<Object, ? super IOException>> callbacks;
	/** The composed batch request. */
	protected MessageArray batchRequest;
	/**
	 * Constructor. Sets the message client object.
	 * @param client the client object
	 * @param scheduler the scheduler
	 */
	public RemoteGameAsyncClient(MessageClientAPI client, Scheduler scheduler) {
		this.client = client;
		this.scheduler = scheduler;
	}

	@Override
	public void begin() {
		if (isBatchMode()) {
			throw new IllegalStateException("Already in batch mode!");
		}
		callbacks = U.newArrayList();
		batchRequest = new MessageArray("BATCH");
	}

	@Override
	public void end() throws IOException {
		if (!isBatchMode()) {
			throw new IllegalStateException("Not in batch mode!");
		}
		// query batch
		MessageArray ma = MessageUtils.expectArray(client.query(batchRequest), "BATCH_RESPONSE");

		
		scheduler.schedule(new BatchResultAsync(null, callbacks, ma));

		batchRequest = null;
		callbacks = null;
	}

	@Override
	public void end(final AsyncResult<? super Void, ? super IOException> out) {
		if (!isBatchMode()) {
			throw new IllegalStateException("Not in batch mode!");
		}
		try {
			// query batch
			MessageArray ma = MessageUtils.expectArray(client.query(batchRequest), "BATCH_RESPONSE");

			scheduler.schedule(new BatchResultAsync(out, callbacks, ma));
		} catch (IOException ex) {
			scheduler.schedule(new AsyncException(ex, out));
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
			client.query(request, scheduler, out);
		}
	}
	/**
	 * Execute a query and parse the result into the given
	 * object.
	 * @param <T> the message object I/O type
	 * @param request the request message
	 * @param out the async result
	 * @param result the result object that will be loaded with the result value
	 * @param names the array of names to accept
	 */
	protected <T extends MessageObjectIO> void query(
			MessageSerializable request,
			AsyncResult<? super T, ? super IOException> out,
			T result,
			String... names
			) {
		sendDirect(request, new MessageObjectAsync<T>(out, result, names));
	}
	/**
	 * Execute a query and parse the result into a list.
	 * @param <T> the message object I/O type
	 * @param request the request message
	 * @param out the async result
	 * @param itemFactory the factory to produce list items
	 * @param names the array of names to accept
	 */
	protected <T extends MessageObjectIO> void queryList(
			MessageSerializable request,
			AsyncResult<? super List<T>, ? super IOException> out,
			Func0<? extends T> itemFactory,
			String... names
			) {
		sendDirect(request, new MessageArrayAsync<T>(out, itemFactory, names));
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
		MessageObject request = new MessageObject("LOGIN",
				"user", user, "passphrase", passphrase, "version", version);
		query(request, out, new WelcomeResponse(), "WELCOME");
	}
	@Override
	public void relogin(String sessionId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("RELOGIN", "session", sessionId);
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
		query(request, out, new MultiplayerDefinition(), "GAME_DEFINITION");
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
		query(request, out, new MultiplayerGameSetup(), "LOAD");
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
		query(request, out, new EmpireStatuses(), "EMPIRE_STATUSES");
	}

	@Override
	public void getFleets(
			AsyncResult<? super List<FleetStatus>, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_FLEETS");
		queryList(request, out, new FleetStatus(), "FLEET_STATUSES");
	}

	@Override
	public void getFleet(int fleetId,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_FLEET", "fleetId", fleetId);
		query(request, out, new FleetStatus(), "FLEET_STATUS");
	}

	@Override
	public void getInventory(
			AsyncResult<? super Map<String, Integer>, ? super IOException> out) {
		AsyncTransform<Object, Map<String, Integer>, IOException> at = new AsyncTransform<Object, Map<String, Integer>, IOException>(out) {
			@Override
			public void invoke(Object param1) throws IOException {
				MessageArray ma = MessageUtils.expectArray(param1, "INVENTORIES");
				
				Map<String, Integer> map = new HashMap<String, Integer>();
				
				for (Object o : ma) {
					MessageObject mo = MessageUtils.expectObject(o, "INVENTORY");
					
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
			AsyncResult<? super ProductionStatus, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_PRODUCTIONS");
		query(request, out, new ProductionStatus(), "PRODUCTIONS");
	}

	@Override
	public void getResearches(
			AsyncResult<? super ResearchStatus, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_RESEARCHES");
		query(request, out, new ResearchStatus(), "RESEARCHES");
	}

	@Override
	public void getPlanetStatuses(
			AsyncResult<? super List<PlanetStatus>, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_PLANET_STATUSES");
		queryList(request, out, new PlanetStatus(), "PLANET_STATUSES");
	}

	@Override
	public void getPlanetStatus(String id,
			AsyncResult<? super PlanetStatus, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_PLANET_STATUS", "planetId", id);
		query(request, out, new PlanetStatus(), "PLANET_STATUS");
	}

	@Override
	public void moveFleet(int id, double x, double y,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("MOVE_FLEET", "fleetId", id, "x", x, "y", y);
		send(request, out);
		
	}

	@Override
	public void addFleetWaypoint(int id, double x, double y,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ADD_FLEET_WAYPOINT", "fleetId", id, "x", x, "y", y);
		send(request, out);
	}

	@Override
	public void moveToPlanet(int id, String target,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("MOVE_TO_PLANET", "fleetId", id, "target", target);
		send(request, out);
	}

	@Override
	public void followFleet(int id, int target,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("FOLLOW_FLEET", "fleetId", id, "target", target);
		send(request, out);
	}

	@Override
	public void attackFleet(int id, int target,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ATTACK_FLEET", "fleetId", id, "target", target);
		send(request, out);
	}

	@Override
	public void attackPlanet(int id, String target,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ATTACK_PLANET", "fleetId", id, "target", target);
		send(request, out);
	}

	@Override
	public void colonize(int id, String target,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("COLONIZE_FLEET", "fleetId", id, "target", target);
		send(request, out);
	}

	@Override
	public void newFleet(String planet, List<InventoryItem> inventory,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		MessageObject mo = new MessageObject("NEW_FLEET_AT_PLANET", "planetId", planet);
		sendFleetConfig(inventory, out, mo);
	}

	/**
	 * Send a fleet configuration request.
	 * @param inventory the inventory describing the fleet contents
	 * @param out the async result
	 * @param mo the request object to fill in and send
	 */
	void sendFleetConfig(List<InventoryItem> inventory,
			AsyncResult<? super FleetStatus, ? super IOException> out,
			MessageObject mo) {
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
		query(mo, out, new FleetStatus(), "FLEET_STATUS");
	}

	@Override
	public void newFleet(int id, List<InventoryItem> inventory,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		MessageObject mo = new MessageObject("NEW_FLEET_AT_FLEET", 
				"fleetId", id);
		sendFleetConfig(inventory, out, mo);
	}

	@Override
	public void deleteFleet(int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("DELETE_FLEET", "fleetId", id);
		send(request, out);
	}

	@Override
	public void renameFleet(int id, String name,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("RENAME_FLEET", "fleetId", id, "name", name);
		send(request, out);
	}

	@Override
	public void sellFleetItem(int id, int itemId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SELL_FLEET_ITEM", "fleetId", id, "itemId", itemId);
		send(request, out);
	}

	@Override
	public void deployFleetItem(int id, String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		MessageObject request = new MessageObject("DEPLOY_FLEET_ITEM", "fleetId", id, "type", type);
		query(request, out, new InventoryItemStatus(), "INVENTORY");
	}

	@Override
	public void undeployFleetItem(int id, int itemId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("UNDEPLOY_FLEET_ITEM", "fleetId", id, "itemId", itemId);
		send(request, out);
	}

	@Override
	public void addFleetEquipment(int id, int itemId, String slotId,
			String type,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ADD_FLEET_EQUIPMENT", 
						"fleetId", id, "itemId", itemId, 
						"slotId", slotId, "type", type);
		send(request, out);
	}

	@Override
	public void removeFleetEquipment(int id, int itemId, String slotId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("REMOVE_FLEET_EQUIPMENT", 
						"fleetId", id, "itemId", itemId, 
						"slotId", slotId);
		send(request, out);
	}

	@Override
	public void fleetUpgrade(int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("FLEET_UPGRADE", "fleetId", id);
		send(request, out);
	}

	@Override
	public void stopFleet(int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_FLEET", "fleetId", id);
		send(request, out);
	}

	@Override
	public void transfer(int sourceFleet, int destinationFleet, int sourceItem,
			FleetTransferMode mode,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("TRANSFER", 
						"source", sourceFleet, "destination", destinationFleet,
						"itemId", sourceItem, "mode", mode.toString()
						);
		send(request, out);
	}

	@Override
	public void colonize(String id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("COLONIZE", "planetId", id);
		send(request, out);
	}

	@Override
	public void cancelColonize(String id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("CANCEL_COLONIZE", "planetId", id);
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
		MessageObject request = new MessageObject("BUILD_AT", "planetId", planetId,
				"type", type, "race", race,
				"x", x, "y", y);
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
		MessageObject request = new MessageObject("BUILD", "planetId", planetId,
				"type", type, "race", race);
		sendDirect(request, at);
	}

	@Override
	public void enable(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ENABLE", "planetId", planetId,
						"buildingId", id);
		send(request, out);
	}

	@Override
	public void disable(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("DISABLE", "planetId", planetId,
						"buildingId", id);
		send(request, out);
	}

	@Override
	public void repair(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("REPAIR", "planetId", planetId,
						"buildingId", id);
		send(request, out);
	}

	@Override
	public void repairOff(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("REPAIR_OFF", "planetId", planetId,
						"buildingId", id);
		send(request, out);
	}

	@Override
	public void demolish(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("DEMOLISH", "planetId", planetId,
						"buildingId", id);
		send(request, out);
	}

	@Override
	public void buildingUpgrade(String planetId, int id, int level,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("BUILDING_UPGRADE", "planetId", planetId,
						"buildingId", id, "level", level);
		send(request, out);
	}

	@Override
	public void deployPlanetItem(String planetId, String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		MessageObject request = new MessageObject("DEPLOY_PLANET_ITEM", 
						"planetId", planetId, "type", type
						);
		query(request, out, new InventoryItemStatus(), "INVENTORY");
	}

	@Override
	public void undeployPlanetItem(String planetId, int itemId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("UNDEPLOY_PLANET_ITEM", 
						"planetId", planetId, "itemId", itemId
						);
		send(request, out);
	}

	@Override
	public void sellPlanetItem(String planetId, int itemId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SELL_PLANET_ITEM", 
						"planetId", planetId, "itemId", itemId
						);
		send(request, out);
	}

	@Override
	public void addPlanetEquipment(String planetId, int itemId, String slotId,
			String type,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SELL_PLANET_ITEM", 
						"planetId", planetId, "itemId", itemId,
						"slotId", slotId, "type", type
						);
		send(request, out);
	}

	@Override
	public void removePlanetEquipment(String planetId, int itemId,
			String slotId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("REMOVE_PLANET_ITEM", 
						"planetId", planetId, "itemId", itemId,
						"slotId", slotId
						);
		send(request, out);
	}

	@Override
	public void planetUpgrade(String planetId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("PLANET_UPGRADE", 
						"planetId", planetId
						);
		send(request, out);
	}

	@Override
	public void startProduction(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("START_PRODUCTION", 
						"type", type
						);
		send(request, out);
	}

	@Override
	public void stopProduction(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_PRODUCTION", 
						"type", type
						);
		send(request, out);
	}

	@Override
	public void setProductionQuantity(String type, int count,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SET_PRODUCTION_QUANTITY", 
						"type", type, "count", count
						);
		send(request, out);
	}

	@Override
	public void setProductionPriority(String type, int priority,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SET_PRODUCTION_PRIORITY", 
						"type", type, "priority", priority
						);
		send(request, out);
	}

	@Override
	public void sellInventory(String type, int count,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SELL_INVENTORY", 
						"type", type, "count", count
						);
		send(request, out);
	}

	@Override
	public void startResearch(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("START_RESEARCH", 
						"type", type
						);
		send(request, out);
	}

	@Override
	public void stopResearch(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_RESEARCH", 
						"type", type
						);
		send(request, out);
	}

	@Override
	public void setResearchMoney(String type, int money,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SET_RESEARCH_MONEY", 
						"type", type, "money", money
						);
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
		MessageObject request = new MessageObject("STOP_SPACE_UNIT",
						"battleId", battleId,
						"unitId", unitId);
		send(request, out);
	}

	@Override
	public void moveSpaceUnit(int battleId, int unitId, double x, double y,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("MOVE_SPACE_UNIT",
						"battleId", battleId,
						"unitId", unitId,
						"x", x, "y", y
						);
		send(request, out);
	}

	@Override
	public void attackSpaceUnit(int battleId, int unitId, int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ATTACK_SPACE_UNIT",
						"battleId", battleId,
						"unitId", unitId,
						"target", targetUnitId
						);
		send(request, out);
	}

	@Override
	public void kamikazeSpaceUnit(int battleId, int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("KAMIKAZE_SPACE_UNIT",
						"battleId", battleId,
						"unitId", unitId);
		send(request, out);
	}

	@Override
	public void fireSpaceRocket(int battleId, int unitId, int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("FIRE_SPACE_ROCKET",
						"battleId", battleId,
						"unitId", unitId,
						"target", targetUnitId);
		send(request, out);
	}

	@Override
	public void spaceRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("SPACE_RETREAT",
						"battleId", battleId
						);
		send(request, out);
	}

	@Override
	public void stopSpaceRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_SPACE_RETREAT",
						"battleId", battleId
						);
		send(request, out);
	}

	@Override
	public void fleetFormation(int fleetId, int formation,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("FLEET_FORMATION",
						"fleetId", fleetId,
						"formation", formation);
		send(request, out);
	}

	@Override
	public void getBattles(
			AsyncResult<? super List<BattleStatus>, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_BATTLES");
		queryList(request, out, new BattleStatus(), "BATTLES");
	}

	@Override
	public void getBattle(int battleId,
			AsyncResult<? super BattleStatus, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_BATTLE", "battleId", battleId);
		query(request, out, new BattleStatus(), "BATTLE");
	}

	@Override
	public void getSpaceBattleUnits(int battleId,
			AsyncResult<? super List<SpaceBattleUnit>, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_SPACE_BATTLE_UNITS", "battleId", battleId);
		queryList(request, out, new SpaceBattleUnit(), "SPACE_BATTLE_UNITS");
	}

	@Override
	public void stopGroundUnit(int battleId, int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_GROUND_UNIT",
						"battleId", battleId,
						"unitId", unitId);
		send(request, out);
	}

	@Override
	public void moveGroundUnit(int battleId, int unitId, int x, int y,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("MOVE_GROUND_UNIT",
						"battleId", battleId,
						"unitId", unitId,
						"x", x, "y", y);
		send(request, out);
	}

	@Override
	public void attackGroundUnit(int battleId, int unitId, int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ATTACK_GROUND_UNIT",
						"battleId", battleId,
						"unitId", unitId,
						"target", targetUnitId);
		send(request, out);
	}

	@Override
	public void attackBuilding(int battleId, int unitId, int buildingId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("ATTACK_BUILDING",
						"battleId", battleId,
						"unitId", unitId,
						"buildingId", buildingId);
		send(request, out);
	}

	@Override
	public void deployMine(int battleId, int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("DEPLOY_MINE",
						"battleId", battleId,
						"unitId", unitId);
		send(request, out);
	}

	@Override
	public void groundRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("GROUND_RETREAT",
						"battleId", battleId);
		send(request, out);
	}

	@Override
	public void stopGroundRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("STOP_GROUND_RETREAT",
						"battleId", battleId);
		send(request, out);
	}

	@Override
	public void getGroundBattleUnits(int battleId,
			AsyncResult<? super List<GroundBattleUnit>, ? super IOException> out) {
		MessageObject request = new MessageObject("QUERY_GROUND_BATTLE_UNITS", "battleId", battleId);
		queryList(request, out, new GroundBattleUnit(), "GROUND_BATTLE_UNITS");
	}
}
