/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Action2E;
import hu.openig.model.DeferredCall;
import hu.openig.model.DeferredInvoke;
import hu.openig.model.DeferredRunnable;
import hu.openig.model.DeferredTransform;
import hu.openig.model.FleetStatus;
import hu.openig.model.InventoryItemStatus;
import hu.openig.model.MessageObjectIO;
import hu.openig.model.MultiplayerUser;
import hu.openig.model.PlanetStatus;
import hu.openig.model.RemoteGameAPI;
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
		DeferredRunnable responseCall = null;
		try {
			responseCall = processMessageDeferred(message);
		} catch (MissingAttributeException ex) {
			conn.error(message, ErrorType.ERROR_FORMAT.ordinal(), ex.toString());
			return;
		}			
		if (useEDT) {
			try {
				SwingUtilities.invokeAndWait(responseCall);
			} catch (InterruptedException ex) {
				conn.error(message, ErrorType.ERROR_INTERRUPTED.ordinal(), ex.toString());
			} catch (InvocationTargetException ex) {
				conn.error(message, ErrorType.ERROR_SERVER_BUG.ordinal(), ex.toString());
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
			conn.error(message, ErrorType.ERROR_SERVER_IO.ordinal(), ex.toString());
		} catch (Throwable ex) {
			conn.error(message, ErrorType.ERROR_SERVER_BUG.ordinal(), ex.toString());
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
						throw new ErrorResponse(ErrorType.ERROR_UNKNOWN_MESSAGE, message != null ? message.getClass().toString() : "null");
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
		throw new ErrorResponse(ErrorType.ERROR_UNKNOWN_MESSAGE, message != null ? message.getClass().toString() : "null");
	}
	/**
	 * Process requests with message array as their outer elements.
	 * @param ma the message array
	 * @return the deferred call object
	 * @throws IOException on error
	 */
	protected DeferredRunnable processMessageArrayDeferred(MessageArray ma) throws IOException {
		throw new ErrorResponse(ErrorType.ERROR_UNKNOWN_MESSAGE, ma != null ? ma.name : "null");
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
					MessageObject r = new MessageObject("FLEET");
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
					MessageObject r = new MessageObject("FLEET");
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
		default:
			throw new ErrorResponse(ErrorType.ERROR_UNKNOWN_MESSAGE, mo != null ? mo.name : "null");
		}
	}
}
