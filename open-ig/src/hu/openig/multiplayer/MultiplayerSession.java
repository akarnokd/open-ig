/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Result;
import hu.openig.model.Configuration;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.MultiplayerEnvironment;
import hu.openig.model.MultiplayerUser;
import hu.openig.model.Production;
import hu.openig.model.ResearchType;
import hu.openig.multiplayer.model.EmpireStatuses;
import hu.openig.multiplayer.model.ErrorResponse;
import hu.openig.multiplayer.model.ErrorType;
import hu.openig.multiplayer.model.FleetStatus;
import hu.openig.multiplayer.model.FleetTransferMode;
import hu.openig.multiplayer.model.InventoryItemStatus;
import hu.openig.multiplayer.model.MultiplayerGameSetup;
import hu.openig.multiplayer.model.PlanetStatus;
import hu.openig.multiplayer.model.ResearchStatus;
import hu.openig.multiplayer.model.WelcomeResponse;
import hu.openig.utils.U;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A multiplayer session object receiving requests from
 * remote clients.
 * @author akarnokd, 2013.04.26.
 */
public class MultiplayerSession implements RemoteGameAPI {
	/** The multiplayer definition. Effectively immutable at this point. */
	protected final MultiplayerDefinition definition;
	/** 
	 * The multiplayer environment. Note that accessing
	 * the world object needs to happen on the EDT.
	 */
	protected final MultiplayerEnvironment environment;
	/** The multiplayer user. */
	protected MultiplayerUser user;
	/** The session id of this connection. */
	protected String sessionId;
	/**
	 * Constructor. Sets the game and environment interfaces.
	 * @param definition the multiplayer game definition
	 * @param environment the common environment
	 */
	public MultiplayerSession(MultiplayerDefinition definition, MultiplayerEnvironment environment) {
		this.definition = definition;
		this.environment = environment;
	}
	
	@Override
	public Result<Long, IOException> ping() {
		return Result.newValue(0L);
	}

	@Override
	public Result<WelcomeResponse, IOException> login(String user, String passphrase, String version) {
		if (U.equal(version, Configuration.VERSION)) {
			for (MultiplayerUser mu : definition.players) {
				if (U.equal(mu.userName, user) 
						&& U.equal(mu.passphrase, passphrase)) {
					
					this.sessionId = generateSessionId();
					this.user = mu;
					this.user.sessionId(this.sessionId);
					
					WelcomeResponse result = new WelcomeResponse();
					result.sessionId = this.sessionId;
					
					return Result.newValue(result);
				}
			}
			return Result.newError(new ErrorResponse(ErrorType.ERROR_USER));
		}
		return Result.newError(new ErrorResponse(ErrorType.ERROR_VERSION, "Server: " + Configuration.VERSION + " - Client: " + version));
	}
	
	/**
	 * Generate a random session id.
	 * @return the session id
	 */
	protected String generateSessionId() {
		byte[] key = new byte[64];
		Random rnd = new Random();
		rnd.nextBytes(key);
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < key.length; i++) {
			b.append(String.format("%02X", key[i] & 0xFF));
		}
		return b.toString();
	}
	/**
	 * Checks if the user is logged in and this is still its only session.
	 * @throws ErrorResponse if the user is not logged in or its session is invalid
	 */
	protected void ensureLogin() throws ErrorResponse {
		if (user == null) {
			throw new ErrorResponse(ErrorType.ERROR_NOT_LOGGED_IN);
		}
		if (!U.equal(user.sessionId(), sessionId)) {
			throw new ErrorResponse(ErrorType.ERROR_SESSION_INVALID);
		}
	}

	@Override
	public void relogin(String sessionId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leave() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MultiplayerDefinition getGameDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void choosePlayerSettings(MultiplayerUser user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MultiplayerGameSetup join() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void ready() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EmpireStatuses getEmpireStatuses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FleetStatus> getFleets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FleetStatus getFleet(int fleetId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<ResearchType, Integer> getInventory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Production> getProductions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResearchStatus getResearches() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PlanetStatus> getPlanetStatuses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlanetStatus getPlanetStatus(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void moveFleet(int id, double x, double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addFleetWaypoint(int id, double x, double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveToPlanet(int id, String target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void followFleet(int id, int target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackFleet(int id, int target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackPlanet(int id, String target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void colonize(int id, String target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FleetStatus newFleet(String planet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FleetStatus newFleet(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteFleet(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renameFleet(int id, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InventoryItemStatus sellFleetItem(int id, int itemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus deployFleetItem(int id, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus undeployFleetItem(int id, int itemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus addFleetEquipment(int id, int itemId,
			String slotId, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus removeFleetEquipment(int id, int itemId,
			String slotId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fleetUpgrade(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transfer(int sourceFleet, int destinationFleet, int sourceItem,
			FleetTransferMode mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void colonize(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancelColonize(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int build(String planetId, String type, String race, int x, int y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int build(String planetId, String type, String race) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void enable(String planetId, int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disable(String planetId, int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repair(String planetId, int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repairOff(String planetId, int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void demolish(String planetId, int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fleetUpgrade(String planetId, int id, int level) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InventoryItemStatus deployPlanetItem(String planetId, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus undeployPlanetItem(String planetId, int itemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus sellPlanetItem(String planetId, int itemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus addPlanetEquipment(String planetId, int itemId,
			String slotId, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus removePlanetEquipment(String planetId,
			int itemId, String slotId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void planetUpgrade(String planetId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startProduction(String type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopProduction(String type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProductionQuantity(String type, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProductionPriority(String type, int priority) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sellInventory(String type, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startResearch(String type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopResearch(String type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setResearchMoney(String type, int money) {
		// TODO Auto-generated method stub
		
	}
}
