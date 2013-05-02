/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.AsyncResult;
import hu.openig.model.InventoryItem;
import hu.openig.model.MultiplayerDefinition;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * An asynchronous game API implementation which turns
 * the calls into the synchronous API calls.
 * @author akarnokd, 2013.05.01.
 *
 */
public class RemoteGameAsyncToSync implements RemoteGameAsyncAPI {
	/** The wrapped API. */
	protected final RemoteGameAPI api;
	/**
	 * Constructor, sets the API object.
	 * @param api the synchronous game API
	 */
	public RemoteGameAsyncToSync(RemoteGameAPI api) {
		this.api = api;
	}

	@Override
	public void begin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void end() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void end(AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ping(final AsyncResult<? super Long, ? super IOException> out) {
		try {
			out.onSuccess(api.ping());
		} catch (IOException ex) {
			out.onError(ex);
		}
	}
	@Override
	public void login(String user, String passphrase, String version,
			AsyncResult<? super WelcomeResponse, ? super IOException> out) {
		try {
			out.onSuccess(api.login(user, passphrase, version));
		} catch (IOException ex) {
			out.onError(ex);
		}
	}

	@Override
	public void relogin(String sessionId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leave(AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getGameDefinition(
			AsyncResult<? super MultiplayerDefinition, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void choosePlayerSettings(MultiplayerUser user,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void join(
			AsyncResult<? super MultiplayerGameSetup, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ready(AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getEmpireStatuses(
			AsyncResult<? super EmpireStatuses, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getFleets(
			AsyncResult<? super List<FleetStatus>, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getFleet(int fleetId,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getInventory(
			AsyncResult<? super Map<String, Integer>, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getProductions(
			AsyncResult<? super ProductionStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getResearches(
			AsyncResult<? super ResearchStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getPlanetStatuses(
			AsyncResult<? super List<PlanetStatus>, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getPlanetStatus(String id,
			AsyncResult<? super PlanetStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveFleet(int id, double x, double y,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addFleetWaypoint(int id, double x, double y,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveToPlanet(int id, String target,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void followFleet(int id, int target,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackFleet(int id, int target,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackPlanet(int id, String target,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void colonize(int id, String target,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newFleet(String planet,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newFleet(String planet, List<InventoryItem> inventory,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newFleet(int id,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newFleet(int id, List<InventoryItem> inventory,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteFleet(int id,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renameFleet(int id, String name,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sellFleetItem(int id, int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deployFleetItem(int id, String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undeployFleetItem(int id, int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addFleetEquipment(int id, int itemId, String slotId,
			String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeFleetEquipment(int id, int itemId, String slotId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fleetUpgrade(int id,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopFleet(int id,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transfer(int sourceFleet, int destinationFleet, int sourceItem,
			FleetTransferMode mode,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void colonize(String id,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancelColonize(String id,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void build(String planetId, String type, String race, int x, int y,
			AsyncResult<? super Integer, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void build(String planetId, String type, String race,
			AsyncResult<? super Integer, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enable(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disable(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repair(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repairOff(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void demolish(String planetId, int id,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buildingUpgrade(String planetId, int id, int level,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deployPlanetItem(String planetId, String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undeployPlanetItem(String planetId, int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sellPlanetItem(String planetId, int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addPlanetEquipment(String planetId, int itemId, String slotId,
			String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removePlanetEquipment(String planetId, int itemId,
			String slotId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void planetUpgrade(String planetId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startProduction(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopProduction(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProductionQuantity(String type, int count,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProductionPriority(String type, int priority,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sellInventory(String type, int count,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startResearch(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopResearch(String type,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setResearchMoney(String type, int money,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pauseResearch(AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pauseProduction(
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unpauseProduction(
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unpauseResearch(
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopSpaceUnit(int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveSpaceUnit(int unitId, double x, double y,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackSpaceUnit(int unitId, int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void kamikazeSpaceUnit(int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fireSpaceRocket(int unitId, int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void spaceRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopSpaceRetreat(int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fleetFormation(int fleetId, int formation,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getBattles(
			AsyncResult<? super List<BattleStatus>, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getBattle(int battleId,
			AsyncResult<? super BattleStatus, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getSpaceBattleUnits(int battleId,
			AsyncResult<? super List<SpaceBattleUnit>, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopGroundUnit(int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveGroundUnit(int unitId, int x, int y,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackGroundUnit(int unitId, int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attackBuilding(int unitId, int buildingId,
			AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deployMine(int unitId,
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
