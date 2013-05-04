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
import hu.openig.core.Scheduler;
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
import hu.openig.net.MessageClient;
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
public class RemoteGameAsyncClient implements RemoteGameAsyncAPI {
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
	protected final MessageClient client;
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
	public RemoteGameAsyncClient(MessageClient client, Scheduler scheduler) {
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
	protected void send(MessageSerializable request, 
			AsyncResult<Object, ? super IOException> out) {
		if (isBatchMode()) {
			batchRequest.add(request);
			callbacks.add(out);
		} else {
			client.query(request, scheduler, out);
		}
	}
	@Override
	public void ping(AsyncResult<? super Long, ? super IOException> out) {
		MessageObject request = new MessageObject("PING");
		AsyncTransform<Object, Long, IOException> tr = new AsyncTransform<Object, Long, IOException>(out) {
			@Override
			public void invoke(Object value) {
				this.setValue(0L);
			}
		};
		send(request, tr);
	}
	@Override
	public void login(String user, String passphrase, String version,
			AsyncResult<? super WelcomeResponse, ? super IOException> out) {
		MessageObject request = new MessageObject("LOGIN",
				"user", user, "passphrase", passphrase, "version", version);
		send(request, new MessageObjectAsync<WelcomeResponse>(out, new WelcomeResponse(), "WELCOME"));
	}
	@Override
	public void relogin(String sessionId,
			AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("RELOGIN", "session", sessionId);
		send(request, new VoidAsync(out, "WELCOME_BACK"));
	}

	@Override
	public void leave(AsyncResult<? super Void, ? super IOException> out) {
		MessageObject request = new MessageObject("LEAVE");
		send(request, voidOk(out));
	}

	@Override
	public void getGameDefinition(
			AsyncResult<? super MultiplayerDefinition, ? super IOException> out) {
		send(new MessageObject("QUERY_GAME_DEFINITION"), 
				new MessageObjectAsync<MultiplayerDefinition>(out, new MultiplayerDefinition(), "GAME_DEFINITION"));
	}
	/**
	 * Creates a new void async result object.
	 * @param out the async result callback
	 * @return the void async
	 */
	protected VoidAsync voidOk(AsyncResult<? super Void, ? super IOException> out) {
		return new VoidAsync(out, "OK");
	}
	@Override
	public void choosePlayerSettings(MultiplayerUser user,
			AsyncResult<? super Void, ? super IOException> out) {
		send(user.toMessage(), voidOk(out));
	}

	@Override
	public void join(
			AsyncResult<? super MultiplayerGameSetup, ? super IOException> out) {
		send(new MessageObject("JOIN"), new MessageObjectAsync<MultiplayerGameSetup>(out, new MultiplayerGameSetup(), "LOAD"));
	}

	@Override
	public void ready(AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("READY"), new VoidAsync(out, "BEGIN"));
	}

	@Override
	public void getEmpireStatuses(
			AsyncResult<? super EmpireStatuses, ? super IOException> out) {
		send(new MessageObject("QUERY_EMPIRE_STATUSES"), 
				new MessageObjectAsync<EmpireStatuses>(out, new EmpireStatuses(), "EMPIRE_STATUSES"));
	}

	@Override
	public void getFleets(
			AsyncResult<? super List<FleetStatus>, ? super IOException> out) {
		send(new MessageObject("QUERY_FLEETS"),
				new MessageArrayAsync<FleetStatus>(out, new FleetStatus(), "FLEET_STATUSES"));
	}

	@Override
	public void getFleet(int fleetId,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		send(new MessageObject("QUERY_FLEET", "fleetId", fleetId),
				new MessageObjectAsync<FleetStatus>(out, new FleetStatus(), "FLEET_STATUS"));
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
		send(new MessageObject("QUERY_INVENTORIES"), at);
	}

	@Override
	public void getProductions(
			AsyncResult<? super ProductionStatus, ? super IOException> out) {
		send(new MessageObject("QUERY_PRODUCTIONS"),
				new MessageObjectAsync<ProductionStatus>(out, new ProductionStatus(), "PRODUCTIONS"));
	}

	@Override
	public void getResearches(
			AsyncResult<? super ResearchStatus, ? super IOException> out) {
		send(new MessageObject("QUERY_RESEARCHES"),
				new MessageObjectAsync<ResearchStatus>(out, new ResearchStatus(), "RESEARCHES"));
	}

	@Override
	public void getPlanetStatuses(
			AsyncResult<? super List<PlanetStatus>, ? super IOException> out) {
		send(new MessageObject("QUERY_PLANET_STATUSES"),
				new MessageArrayAsync<PlanetStatus>(out, new PlanetStatus(), "PLANET_STATUSES"));
	}

	@Override
	public void getPlanetStatus(String id,
			AsyncResult<? super PlanetStatus, ? super IOException> out) {
		send(new MessageObject("QUERY_PLANET_STATUS", "planetId", id),
				new MessageObjectAsync<PlanetStatus>(out, new PlanetStatus(), "PLANET_STATUS"));
	}

	@Override
	public void moveFleet(int id, double x, double y,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("MOVE_FLEET", "fleetId", id, "x", x, "y", y),
				voidOk(out));
		
	}

	@Override
	public void addFleetWaypoint(int id, double x, double y,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("ADD_FLEET_WAYPOINT", "fleetId", id, "x", x, "y", y),
				voidOk(out));
	}

	@Override
	public void moveToPlanet(int id, String target,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("MOVE_TO_PLANET", "fleetId", id, "target", target),
				voidOk(out));
	}

	@Override
	public void followFleet(int id, int target,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("FOLLOW_FLEET", "fleetId", id, "target", target),
				voidOk(out));
	}

	@Override
	public void attackFleet(int id, int target,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("ATTACK_FLEET", "fleetId", id, "target", target),
				voidOk(out));
	}

	@Override
	public void attackPlanet(int id, String target,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("ATTACK_PLANET", "fleetId", id, "target", target),
				voidOk(out));
	}

	@Override
	public void colonize(int id, String target,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("COLONIZE_FLEET", "fleetId", id, "target", target),
				voidOk(out));
	}

	@Override
	public void newFleet(String planet,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		send(new MessageObject("NEW_FLEET_AT_PLANET", "planetId", planet),
				new MessageObjectAsync<FleetStatus>(out, new FleetStatus(), "FLEET_STATUS"));
	}

	@Override
	public void newFleet(String planet, List<InventoryItem> inventory,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		MessageObject mo = new MessageObject("NEW_FLEET_AT_PLANET_CONFIG", "planetId", planet);
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
		send(mo, new MessageObjectAsync<FleetStatus>(out, new FleetStatus(), "FLEET_STATUS"));
	}

	@Override
	public void newFleet(int id,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		send(new MessageObject("NEW_FLEET_AT_FLEET", "fleetId", id),
				new MessageObjectAsync<FleetStatus>(out, new FleetStatus(), "FLEET_STATUS"));
	}

	@Override
	public void newFleet(int id, List<InventoryItem> inventory,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		MessageObject mo = new MessageObject("NEW_FLEET_AT_PLANET_CONFIG", 
				"fleetId", id);
		sendFleetConfig(inventory, out, mo);
	}

	@Override
	public void deleteFleet(int id,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("DELETE_FLEET", "fleetId", id),
				voidOk(out));
	}

	@Override
	public void renameFleet(int id, String name,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("RENAME_FLEET", "fleetId", id, "name", name),
				voidOk(out));
	}

	@Override
	public void sellFleetItem(int id, int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		send(new MessageObject("SELL_FLEET_ITEM", "fleetId", id, "itemId", itemId),
				new MessageObjectAsync<InventoryItemStatus>(out, new InventoryItemStatus(), "INVENTORY"));
	}

	@Override
	public void deployFleetItem(int id, String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		send(new MessageObject("DEPLOY_FLEET_ITEM", "fleetId", id, "type", type),
				new MessageObjectAsync<InventoryItemStatus>(out, new InventoryItemStatus(), "INVENTORY"));
	}

	@Override
	public void undeployFleetItem(int id, int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		send(new MessageObject("UNDEPLOY_FLEET_ITEM", "fleetId", id, "itemId", itemId),
				new MessageObjectAsync<InventoryItemStatus>(out, new InventoryItemStatus(), "INVENTORY"));
	}

	@Override
	public void addFleetEquipment(int id, int itemId, String slotId,
			String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		send(new MessageObject("ADD_FLEET_EQUIPMENT", 
				"fleetId", id, "itemId", itemId, 
				"slotId", slotId, "type", type),
				new MessageObjectAsync<InventoryItemStatus>(out, new InventoryItemStatus(), "INVENTORY"));
	}

	@Override
	public void removeFleetEquipment(int id, int itemId, String slotId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		send(new MessageObject("REMOVE_FLEET_EQUIPMENT", 
				"fleetId", id, "itemId", itemId, 
				"slotId", slotId),
				new MessageObjectAsync<InventoryItemStatus>(out, new InventoryItemStatus(), "INVENTORY"));
	}

	@Override
	public void fleetUpgrade(int id,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("FLEET_UPGRADE", "fleetId", id), voidOk(out));
	}

	@Override
	public void stopFleet(int id,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("STOP_FLEET", "fleetId", id), voidOk(out));
	}

	@Override
	public void transfer(int sourceFleet, int destinationFleet, int sourceItem,
			FleetTransferMode mode,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("TRANSFER", 
				"source", sourceFleet, "destination", destinationFleet,
				"itemId", sourceItem, "mode", mode.toString()
				), voidOk(out));
	}

	@Override
	public void colonize(String id,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("COLONIZE", "planetId", id), voidOk(out));
	}

	@Override
	public void cancelColonize(String id,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("CANCEL_COLONIZE", "planetId", id), voidOk(out));
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
		send(new MessageObject("BUILD_AT", "planetId", planetId,
				"type", type, "race", race,
				"x", x, "y", y), at);
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
		send(new MessageObject("BUILD", "planetId", planetId,
				"type", type, "race", race), at);
	}

	@Override
	public void enable(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("ENABLE", "planetId", planetId,
				"buildingId", id), voidOk(out));
	}

	@Override
	public void disable(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("DISABLE", "planetId", planetId,
				"buildingId", id), voidOk(out));
	}

	@Override
	public void repair(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("REPAIR", "planetId", planetId,
				"buildingId", id), voidOk(out));
	}

	@Override
	public void repairOff(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("REPAIR_OFF", "planetId", planetId,
				"buildingId", id), voidOk(out));
	}

	@Override
	public void demolish(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("DEMOLISH", "planetId", planetId,
				"buildingId", id), voidOk(out));
	}

	@Override
	public void buildingUpgrade(String planetId, int id, int level,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("BUILDING_UPGRADE", "planetId", planetId,
				"buildingId", id, "level", level), voidOk(out));
	}

	@Override
	public void deployPlanetItem(String planetId, String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		send(new MessageObject("DEPLOY_PLANET_ITEM", 
				"planetId", planetId, "type", type
				),
				new MessageObjectAsync<InventoryItemStatus>(out, new InventoryItemStatus(), "INVENTORY"));
		
	}

	@Override
	public void undeployPlanetItem(String planetId, int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		send(new MessageObject("UNDEPLOY_PLANET_ITEM", 
				"planetId", planetId, "itemId", itemId
				),
				new MessageObjectAsync<InventoryItemStatus>(out, new InventoryItemStatus(), "INVENTORY"));
	}

	@Override
	public void sellPlanetItem(String planetId, int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		send(new MessageObject("SELL_PLANET_ITEM", 
				"planetId", planetId, "itemId", itemId
				),
				new MessageObjectAsync<InventoryItemStatus>(out, new InventoryItemStatus(), "INVENTORY"));
	}

	@Override
	public void addPlanetEquipment(String planetId, int itemId, String slotId,
			String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		send(new MessageObject("SELL_PLANET_ITEM", 
				"planetId", planetId, "itemId", itemId,
				"slotId", slotId, "type", type
				),
				new MessageObjectAsync<InventoryItemStatus>(out, new InventoryItemStatus(), "INVENTORY"));
	}

	@Override
	public void removePlanetEquipment(String planetId, int itemId,
			String slotId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		send(new MessageObject("REMOVE_PLANET_ITEM", 
				"planetId", planetId, "itemId", itemId,
				"slotId", slotId
				),
				new MessageObjectAsync<InventoryItemStatus>(out, new InventoryItemStatus(), "INVENTORY"));
	}

	@Override
	public void planetUpgrade(String planetId,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("PLANET_UPGRADE", 
				"planetId", planetId
				), voidOk(out));
	}

	@Override
	public void startProduction(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("START_PRODUCTION", 
				"type", type
				), voidOk(out));
	}

	@Override
	public void stopProduction(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("STOP_PRODUCTION", 
				"type", type
				), voidOk(out));
	}

	@Override
	public void setProductionQuantity(String type, int count,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("SET_PRODUCTION_QUANTITY", 
				"type", type, "count", count
				), voidOk(out));
	}

	@Override
	public void setProductionPriority(String type, int priority,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("SET_PRODUCTION_PRIORITY", 
				"type", type, "priority", priority
				), voidOk(out));
	}

	@Override
	public void sellInventory(String type, int count,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("SELL_INVENTORY", 
				"type", type, "count", count
				), voidOk(out));
	}

	@Override
	public void startResearch(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("START_RESEARCH", 
				"type", type
				), voidOk(out));
	}

	@Override
	public void stopResearch(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("STOP_RESEARCH", 
				"type", type
				), voidOk(out));
	}

	@Override
	public void setResearchMoney(String type, int money,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("SET_RESEARCH_MONEY", 
				"type", type, "money", money
				), voidOk(out));
	}

	@Override
	public void pauseResearch(AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("PAUSE_RESEARCH"), voidOk(out));
	}

	@Override
	public void pauseProduction(
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("PAUSE_PRODUCTION"), voidOk(out));
	}

	@Override
	public void unpauseProduction(
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("UNPAUSE_PRODUCTION"), voidOk(out));
	}

	@Override
	public void unpauseResearch(
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("UNPAUSE_RESEARCH"), voidOk(out));
	}

	@Override
	public void stopSpaceUnit(int battleId, int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("STOP_SPACE_UNIT",
				"battleId", battleId,
				"unitId", unitId), voidOk(out));
	}

	@Override
	public void moveSpaceUnit(int battleId, int unitId, double x, double y,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("MOVE_SPACE_UNIT",
				"battleId", battleId,
				"unitId", unitId,
				"x", x, "y", y
				), voidOk(out));
	}

	@Override
	public void attackSpaceUnit(int battleId, int unitId, int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("ATTACK_SPACE_UNIT",
				"battleId", battleId,
				"unitId", unitId,
				"target", targetUnitId
				), voidOk(out));
	}

	@Override
	public void kamikazeSpaceUnit(int battleId, int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("KAMIKAZE_SPACE_UNIT",
				"battleId", battleId,
				"unitId", unitId), voidOk(out));
	}

	@Override
	public void fireSpaceRocket(int battleId, int unitId, int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("FIRE_SPACE_ROCKET",
				"battleId", battleId,
				"unitId", unitId,
				"target", targetUnitId), voidOk(out));
	}

	@Override
	public void spaceRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("SPACE_RETREAT",
				"battleId", battleId
				), voidOk(out));
	}

	@Override
	public void stopSpaceRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("STOP_SPACE_RETREAT",
				"battleId", battleId
				), voidOk(out));
	}

	@Override
	public void fleetFormation(int fleetId, int formation,
			AsyncResult<? super Void, ? super IOException> out) {
		send(new MessageObject("FLEET_FORMATION",
				"fleetId", fleetId,
				"formation", formation), voidOk(out));
	}

	@Override
	public void getBattles(
			AsyncResult<? super List<BattleStatus>, ? super IOException> out) {
		send(new MessageObject("QUERY_BATTLES"),
				new MessageArrayAsync<BattleStatus>(out, new BattleStatus(), "BATTLES")
				);
	}

	@Override
	public void getBattle(int battleId,
			AsyncResult<? super BattleStatus, ? super IOException> out) {
		send(new MessageObject("QUERY_BATTLE", "battleId", battleId),
				new MessageObjectAsync<BattleStatus>(out, new BattleStatus(), "BATTLE")
				);
	}

	@Override
	public void getSpaceBattleUnits(int battleId,
			AsyncResult<? super List<SpaceBattleUnit>, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopGroundUnit(int battleId, int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveGroundUnit(int battleId, int unitId, int x, int y,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackGroundUnit(int battleId, int unitId, int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackBuilding(int battleId, int unitId, int buildingId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deployMine(int battleId, int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void groundRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopGroundRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getGroundBattleUnits(int battleId,
			AsyncResult<? super List<GroundBattleUnit>, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}
}
