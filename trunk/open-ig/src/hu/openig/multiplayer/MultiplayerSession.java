/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Action1E;
import hu.openig.mechanics.DirectGamePlayer;
import hu.openig.model.AutoBuild;
import hu.openig.model.BattleStatus;
import hu.openig.model.Configuration;
import hu.openig.model.EmpireStatuses;
import hu.openig.model.FleetStatus;
import hu.openig.model.FleetTransferMode;
import hu.openig.model.GameAPI;
import hu.openig.model.GameEnvironment;
import hu.openig.model.GroundwarUnitStatus;
import hu.openig.model.InventoryItemStatus;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * A multiplayer session object receiving requests from
 * remote clients.
 * @author akarnokd, 2013.04.26.
 */
public class MultiplayerSession implements RemoteGameAPI {
	/** The multiplayer definition. Effectively immutable at this point. */
	protected final MultiplayerDefinition definition;
	/** The game API. */
	protected GameAPI api;
	/** The multiplayer user. */
	protected MultiplayerUser user;
	/** The session id of this connection. */
	protected String sessionId;
	/** The global game environment. */
	protected GameEnvironment env;
	/**
	 * Constructor. Sets the game and environment interfaces.
	 * @param definition the multiplayer game definition
	 * @param env the game environment
	 */
	public MultiplayerSession(MultiplayerDefinition definition, GameEnvironment env) {
		this.definition = definition;
		this.env = env;
	}
	
	@Override
	public long ping() throws IOException {
		return 0L;
	}

	@Override
	public WelcomeResponse login(String user, String passphrase, String version) throws IOException {
		if (Objects.equals(version, Configuration.VERSION)) {
			for (MultiplayerUser mu : definition.players) {
				if (Objects.equals(mu.userName, user) 
						&& Objects.equals(mu.passphrase, passphrase)) {
					
					this.sessionId = generateSessionId();
					this.user = mu;
					this.user.sessionId(this.sessionId);
					
					WelcomeResponse result = new WelcomeResponse();
					result.sessionId = this.sessionId;
					
					return result;
				}
			}
			throw new ErrorResponse(ErrorType.USER);
		}
		throw new ErrorResponse(ErrorType.VERSION, "Server: " + Configuration.VERSION + " - Client: " + version);
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
        for (byte aKey : key) {
            b.append(String.format("%02X", aKey & 0xFF));
        }
		return b.toString();
	}
	/**
	 * Checks if the user is logged in and this is still its only session.
	 * @throws IOException if the user is not logged in or has invalid session
	 */
	protected void ensureLogin() throws IOException {
		if (user == null) {
			ErrorType.NOT_LOGGED_IN.raise();
		}
		if (!Objects.equals(user.sessionId(), sessionId)) {
			ErrorType.SESSION_INVALID.raise();
		}
	}
	/**
	 * Check if there is actually a game going on.
	 * @throws IOException on error
	 */
	public void ensureGame() throws IOException {
		ensureLogin();
		if (env.isLoading()) {
			ErrorType.NOT_READY.raise();
		}
		if (env.world() == null) {
			ErrorType.NO_GAME_RUNNING.raise();
		}
		if (api == null) {
			api = new DirectGamePlayer(env.world().player(user.id));
		}
	}

	@Override
	public void relogin(String sessionId) throws IOException {
		if (user.sessionId().equals(sessionId)) {
			this.sessionId = sessionId;
		} else {
			ErrorType.SESSION_INVALID.raise();
		}
	}

	@Override
	public void leave() throws IOException {
		user = null;
		api = null;
		sessionId = null;
	}

	@Override
	public MultiplayerDefinition getGameDefinition() throws IOException {
		return definition;
	}

	@Override
	public void choosePlayerSettings(MultiplayerUser user) throws IOException {
		ensureLogin();
		Action1E<MultiplayerUser, IOException> joinCallback = env.joinCallback();
		if (joinCallback != null) {
			mergeUser(user);
			joinCallback.invoke(this.user);
		}
		ErrorType.NO_GAME_AVAILABLE.raise();
	}
	/**
	 * Merge the settings of the given user with the current user.
	 * @param u the settings to merge into
	 */
	protected void mergeUser(MultiplayerUser u) {
		user.group = u.group;
		user.iconRef = u.iconRef;
		user.race = u.race;
		user.originalId = u.originalId;
		user.traits.clear();
		user.traits.addAll(u.traits);
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
		ensureGame();
		return api.getEmpireStatuses();
	}

	@Override
	public List<FleetStatus> getFleets() throws IOException {
		ensureGame();
		return api.getFleets();
	}

	@Override
	public FleetStatus getFleet(int fleetId) throws IOException {
		ensureGame();
		return api.getFleet(fleetId);
	}

	@Override
	public Map<String, Integer> getInventory() throws IOException {
		ensureGame();
		return api.getInventory();
	}

	@Override
	public ProductionStatuses getProductions() throws IOException {
		ensureGame();
		return api.getProductions();
	}

	@Override
	public ResearchStatuses getResearches() throws IOException {
		ensureGame();
		return api.getResearches();
	}

	@Override
	public List<PlanetStatus> getPlanetStatuses() throws IOException {
		ensureGame();
		return api.getPlanetStatuses();
	}

	@Override
	public PlanetStatus getPlanetStatus(String id) throws IOException {
		ensureGame();
		return api.getPlanetStatus(id);
	}

	@Override
	public void moveFleet(int id, double x, double y) throws IOException {
		ensureGame();
		api.moveFleet(id, x, y);
	}

	@Override
	public void addFleetWaypoint(int id, double x, double y) throws IOException {
		ensureGame();
		api.addFleetWaypoint(id, x, y);
	}

	@Override
	public void moveToPlanet(int id, String target) throws IOException {
		ensureGame();
		api.moveToPlanet(id, target);
	}

	@Override
	public void followFleet(int id, int target) throws IOException {
		ensureGame();
		api.followFleet(id, target);
	}

	@Override
	public void attackFleet(int id, int target) throws IOException {
		ensureGame();
		api.attackFleet(id, target);
	}

	@Override
	public void attackPlanet(int id, String target) throws IOException {
		ensureGame();
		api.attackPlanet(id, target);
	}

	@Override
	public void colonize(int id, String target) throws IOException {
		ensureGame();
		api.colonize(id, target);
	}

	@Override
	public int newFleet(String planet, List<InventoryItemStatus> inventory)
			throws IOException {
		ensureGame();
		return api.newFleet(planet, inventory);
	}

	@Override
	public int newFleet(int id, List<InventoryItemStatus> inventory)
			throws IOException {
		ensureGame();
		return api.newFleet(id, inventory);
	}

	@Override
	public void deleteFleet(int id) throws IOException {
		ensureGame();
		api.deleteFleet(id);
	}

	@Override
	public void renameFleet(int id, String name) throws IOException {
		ensureGame();
		api.renameFleet(id, name);
	}

	@Override
	public void sellFleetItem(int id, int itemId) throws IOException {
		ensureGame();
		api.sellFleetItem(id, itemId);
	}

	@Override
	public int deployFleetItem(int id, String type)
			throws IOException {
		ensureGame();
		return api.deployFleetItem(id, type);
	}

	@Override
	public void undeployFleetItem(int id, int itemId) throws IOException {
		ensureGame();
		api.undeployFleetItem(id, itemId);
	}

	@Override
	public void addFleetEquipment(int id, int itemId, String slotId, String type)
			throws IOException {
		ensureGame();
		api.addFleetEquipment(id, itemId, slotId, type);
	}

	@Override
	public void removeFleetEquipment(int id, int itemId, String slotId)
			throws IOException {
		ensureGame();
		api.removeFleetEquipment(id, itemId, slotId);
	}

	@Override
	public void fleetUpgrade(int id) throws IOException {
		ensureGame();
		api.fleetUpgrade(id);
	}

	@Override
	public void stopFleet(int id) throws IOException {
		ensureGame();
		api.stopFleet(id);
	}

	@Override
	public void transfer(int sourceFleet, int destinationFleet, int sourceItem,
			FleetTransferMode mode) throws IOException {
		ensureGame();
		api.transfer(sourceFleet, destinationFleet, sourceItem, mode);
	}

	@Override
	public void colonize(String id) throws IOException {
		ensureGame();
		api.colonize(id);
	}

	@Override
	public void cancelColonize(String id) throws IOException {
		ensureGame();
		api.cancelColonize(id);
	}

	@Override
	public int build(String planetId, String type, String race, int x, int y)
			throws IOException {
		ensureGame();
		return api.build(planetId, type, race, x, y);
	}

	@Override
	public int build(String planetId, String type, String race)
			throws IOException {
		ensureGame();
		return api.build(planetId, type, race);
	}

	@Override
	public void enable(String planetId, int id) throws IOException {
		ensureGame();
		api.enable(planetId, id);
	}

	@Override
	public void disable(String planetId, int id) throws IOException {
		ensureGame();
		api.disable(planetId, id);
	}

	@Override
	public void repair(String planetId, int id) throws IOException {
		ensureGame();
		api.repair(planetId, id);
	}

	@Override
	public void repairOff(String planetId, int id) throws IOException {
		ensureGame();
		api.repairOff(planetId, id);
	}

	@Override
	public void demolish(String planetId, int id) throws IOException {
		ensureGame();
		api.demolish(planetId, id);
	}

	@Override
	public void buildingUpgrade(String planetId, int id, int level)
			throws IOException {
		ensureGame();
		api.buildingUpgrade(planetId, id, level);
	}

	@Override
	public int deployPlanetItem(String planetId, String type)
			throws IOException {
		ensureGame();
		return api.deployPlanetItem(planetId, type);
	}

	@Override
	public void undeployPlanetItem(String planetId, int itemId)
			throws IOException {
		ensureGame();
		api.undeployPlanetItem(planetId, itemId);
	}

	@Override
	public void sellPlanetItem(String planetId, int itemId) throws IOException {
		ensureGame();
		api.sellPlanetItem(planetId, itemId);
	}

	@Override
	public void addPlanetEquipment(String planetId, int itemId, String slotId,
			String type) throws IOException {
		ensureGame();
		api.addPlanetEquipment(planetId, itemId, slotId, type);
	}

	@Override
	public void removePlanetEquipment(String planetId, int itemId, String slotId)
			throws IOException {
		ensureGame();
		api.removePlanetEquipment(planetId, itemId, slotId);
	}

	@Override
	public void planetUpgrade(String planetId) throws IOException {
		ensureGame();
		api.planetUpgrade(planetId);
	}

	@Override
	public void startProduction(String type) throws IOException {
		ensureGame();
		api.startProduction(type);
	}

	@Override
	public void stopProduction(String type) throws IOException {
		ensureGame();
		api.stopProduction(type);
	}

	@Override
	public void setProductionQuantity(String type, int count)
			throws IOException {
		ensureGame();
		api.setProductionPriority(type, count);
	}

	@Override
	public void setProductionPriority(String type, int priority)
			throws IOException {
		ensureGame();
		api.setProductionPriority(type, priority);
	}

	@Override
	public void sellInventory(String type, int count) throws IOException {
		ensureGame();
		api.sellInventory(type, count);
	}

	@Override
	public void startResearch(String type) throws IOException {
		ensureGame();
		api.startResearch(type);
	}

	@Override
	public void stopResearch(String type) throws IOException {
		ensureGame();
		api.stopResearch(type);
	}

	@Override
	public void setResearchMoney(String type, int money) throws IOException {
		ensureGame();
		api.setResearchMoney(type, money);
	}

	@Override
	public void pauseResearch() throws IOException {
		ensureGame();
		api.pauseResearch();
	}

	@Override
	public void pauseProduction() throws IOException {
		ensureGame();
		api.pauseProduction();
	}

	@Override
	public void unpauseProduction() throws IOException {
		ensureGame();
		api.unpauseProduction();
	}

	@Override
	public void unpauseResearch() throws IOException {
		ensureGame();
		api.unpauseResearch();
	}

	@Override
	public void stopSpaceUnit(int battleId, int unitId) throws IOException {
		ensureGame();
		api.stopSpaceUnit(battleId, unitId);
	}

	@Override
	public void moveSpaceUnit(int battleId, int unitId, double x, double y)
			throws IOException {
		ensureGame();
		api.moveSpaceUnit(battleId, unitId, x, y);
	}

	@Override
	public void attackSpaceUnit(int battleId, int unitId, int targetUnitId)
			throws IOException {
		ensureGame();
		api.attackSpaceUnit(battleId, unitId, targetUnitId);
	}

	@Override
	public void kamikazeSpaceUnit(int battleId, int unitId) throws IOException {
		ensureGame();
		api.kamikazeSpaceUnit(battleId, unitId);
	}

	@Override
	public void fireSpaceRocket(int battleId, int unitId, int targetUnitId)
			throws IOException {
		ensureGame();
		api.fireSpaceRocket(battleId, unitId, targetUnitId);
	}

	@Override
	public void spaceRetreat(int battleId) throws IOException {
		ensureGame();
		api.spaceRetreat(battleId);
	}

	@Override
	public void stopSpaceRetreat(int battleId) throws IOException {
		ensureGame();
		api.stopSpaceRetreat(battleId);
	}

	@Override
	public void fleetFormation(int fleetId, int formation) throws IOException {
		ensureGame();
		api.fleetFormation(fleetId, formation);
	}

	@Override
	public List<BattleStatus> getBattles() throws IOException {
		ensureGame();
		return api.getBattles();
	}

	@Override
	public BattleStatus getBattle(int battleId) throws IOException {
		ensureGame();
		return api.getBattle(battleId);
	}

	@Override
	public List<SpaceBattleUnit> getSpaceBattleUnits(int battleId)
			throws IOException {
		ensureGame();
		return api.getSpaceBattleUnits(battleId);
	}

	@Override
	public void stopGroundUnit(int battleId, int unitId) throws IOException {
		ensureGame();
		api.stopGroundUnit(battleId, unitId);
	}

	@Override
	public void moveGroundUnit(int battleId, int unitId, int x, int y)
			throws IOException {
		ensureGame();
		api.moveGroundUnit(battleId, unitId, x, y);
	}

	@Override
	public void attackGroundUnit(int battleId, int unitId, int targetUnitId)
			throws IOException {
		ensureGame();
		api.attackGroundUnit(battleId, unitId, targetUnitId);
	}

	@Override
	public void attackBuilding(int battleId, int unitId, int buildingId)
			throws IOException {
		ensureGame();
		api.attackBuilding(battleId, unitId, buildingId);
	}

	@Override
	public void deployMine(int battleId, int unitId) throws IOException {
		ensureGame();
		api.deployMine(battleId, unitId);
	}

	@Override
	public void groundRetreat(int battleId) throws IOException {
		ensureGame();
		api.groundRetreat(battleId);
	}

	@Override
	public void stopGroundRetreat(int battleId) throws IOException {
		ensureGame();
		api.stopGroundRetreat(battleId);
	}

	@Override
	public List<GroundwarUnitStatus> getGroundBattleUnits(int battleId)
			throws IOException {
		ensureGame();
		return api.getGroundBattleUnits(battleId);
	}
	@Override
	public InventoryItemStatus getInventoryStatus(int fleetId, int itemId)
			throws IOException {
		ensureGame();
		return api.getInventoryStatus(fleetId, itemId);
	}
	@Override
	public InventoryItemStatus getInventoryStatus(String planetId, int itemId)
			throws IOException {
		ensureGame();
		return api.getInventoryStatus(planetId, itemId);
	}
	@Override
	public void setAutoBuild(String planetId, AutoBuild auto)
			throws IOException {
		ensureGame();
		api.setAutoBuild(planetId, auto);
	}
	@Override
	public void setTaxLevel(String planetId, TaxLevel tax) throws IOException {
		ensureGame();
		api.setTaxLevel(planetId, tax);
	}
}
