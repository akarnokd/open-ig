/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Result;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.MultiplayerUser;
import hu.openig.model.Production;
import hu.openig.model.ResearchType;
import hu.openig.multiplayer.model.EmpireStatuses;
import hu.openig.multiplayer.model.FleetStatus;
import hu.openig.multiplayer.model.FleetTransferMode;
import hu.openig.multiplayer.model.InventoryItemStatus;
import hu.openig.multiplayer.model.MultiplayerGameSetup;
import hu.openig.multiplayer.model.PlanetStatus;
import hu.openig.multiplayer.model.ResearchStatus;
import hu.openig.multiplayer.model.WelcomeResponse;
import hu.openig.net.MessageClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A remote game API client implementation based
 * on MessageObject exchanges.
 * @author akarnokd, 2013.04.22.
 */
public class RemoteGameAPIClient implements RemoteGameAPI {
	/** The message client object. */
	protected final MessageClient client;
	/**
	 * Constructor. Sets the message client object.
	 * @param client the client object
	 */
	public RemoteGameAPIClient(MessageClient client) {
		this.client = client;
	}
	@Override
	public Result<Long, IOException> ping() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<WelcomeResponse, IOException> login(String user,
			String passphrase, String version) {
		// TODO Auto-generated method stub
		return null;
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
