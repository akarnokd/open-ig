/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.model.Configuration;
import hu.openig.model.InventoryItem;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.MultiplayerEnvironment;
import hu.openig.model.MultiplayerUser;
import hu.openig.multiplayer.model.BattleStatus;
import hu.openig.multiplayer.model.EmpireStatuses;
import hu.openig.multiplayer.model.FleetStatus;
import hu.openig.multiplayer.model.FleetTransferMode;
import hu.openig.multiplayer.model.GroundBattleUnit;
import hu.openig.multiplayer.model.InventoryItemStatus;
import hu.openig.multiplayer.model.MultiplayerGameSetup;
import hu.openig.multiplayer.model.PlanetStatus;
import hu.openig.multiplayer.model.ProductionStatus;
import hu.openig.multiplayer.model.ResearchStatus;
import hu.openig.multiplayer.model.SpaceBattleUnit;
import hu.openig.multiplayer.model.WelcomeResponse;
import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;
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
	public long ping() throws IOException {
		return 0L;
	}

	@Override
	public WelcomeResponse login(String user, String passphrase, String version) throws IOException {
		if (U.equal(version, Configuration.VERSION)) {
			for (MultiplayerUser mu : definition.players) {
				if (U.equal(mu.userName, user) 
						&& U.equal(mu.passphrase, passphrase)) {
					
					this.sessionId = generateSessionId();
					this.user = mu;
					this.user.sessionId(this.sessionId);
					
					WelcomeResponse result = new WelcomeResponse();
					result.sessionId = this.sessionId;
					
					return result;
				}
			}
			throw new ErrorResponse(ErrorType.ERROR_USER);
		}
		throw new ErrorResponse(ErrorType.ERROR_VERSION, "Server: " + Configuration.VERSION + " - Client: " + version);
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
	 * @throws IOException if the user is not logged in or has invalid session
	 */
	protected void ensureLogin() throws IOException {
		if (user == null) {
			throw new ErrorResponse(ErrorType.ERROR_NOT_LOGGED_IN);
		}
		if (!U.equal(user.sessionId(), sessionId)) {
			throw new ErrorResponse(ErrorType.ERROR_SESSION_INVALID);
		}
	}

	@Override
	public void leave() throws IOException {
		ensureLogin();
		// FIXME close
	}

	@Override
	public void relogin(String sessionId) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MultiplayerDefinition getGameDefinition() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void choosePlayerSettings(MultiplayerUser user) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MultiplayerGameSetup join() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void ready() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EmpireStatuses getEmpireStatuses() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FleetStatus> getFleets() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FleetStatus getFleet(int fleetId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Integer> getInventory() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProductionStatus getProductions() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResearchStatus getResearches() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PlanetStatus> getPlanetStatuses() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlanetStatus getPlanetStatus(String id) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void moveFleet(int id, double x, double y) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addFleetWaypoint(int id, double x, double y) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveToPlanet(int id, String target) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void followFleet(int id, int target) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackFleet(int id, int target) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackPlanet(int id, String target) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void colonize(int id, String target) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FleetStatus newFleet(String planet) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FleetStatus newFleet(String planet, List<InventoryItem> inventory)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FleetStatus newFleet(int id) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FleetStatus newFleet(int id, List<InventoryItem> inventory)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteFleet(int id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renameFleet(int id, String name) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InventoryItemStatus sellFleetItem(int id, int itemId)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus deployFleetItem(int id, String type)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus undeployFleetItem(int id, int itemId)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus addFleetEquipment(int id, int itemId,
			String slotId, String type) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus removeFleetEquipment(int id, int itemId,
			String slotId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fleetUpgrade(int id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopFleet(int id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transfer(int sourceFleet, int destinationFleet, int sourceItem,
			FleetTransferMode mode) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void colonize(String id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancelColonize(String id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer build(String planetId, String type, String race, int x, int y)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer build(String planetId, String type, String race)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enable(String planetId, int id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disable(String planetId, int id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repair(String planetId, int id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repairOff(String planetId, int id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void demolish(String planetId, int id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buildingUpgrade(String planetId, int id, int level)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InventoryItemStatus deployPlanetItem(String planetId, String type)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus undeployPlanetItem(String planetId, int itemId)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus sellPlanetItem(String planetId, int itemId)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus addPlanetEquipment(String planetId, int itemId,
			String slotId, String type) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryItemStatus removePlanetEquipment(String planetId,
			int itemId, String slotId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void planetUpgrade(String planetId) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startProduction(String type) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopProduction(String type) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProductionQuantity(String type, int count)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProductionPriority(String type, int priority)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sellInventory(String type, int count) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startResearch(String type) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopResearch(String type) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setResearchMoney(String type, int money) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pauseResearch() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pauseProduction() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unpauseProduction() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unpauseResearch() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopSpaceUnit(int unitId) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveSpaceUnit(int unitId, double x, double y)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackSpaceUnit(int unitId, int targetUnitId)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void kamikazeSpaceUnit(int unitId) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fireSpaceRocket(int unitId, int targetUnitId)
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
	public void stopGroundUnit(int unitId) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveGroundUnit(int unitId, int x, int y) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackGroundUnit(int unitId, int targetUnitId)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackBuilding(int unitId, int buildingId) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deployMine(int unitId) throws IOException {
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
	public List<GroundBattleUnit> getGroundBattleUnits(int battleId)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
