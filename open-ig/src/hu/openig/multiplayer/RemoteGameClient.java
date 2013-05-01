/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.model.InventoryItem;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.MultiplayerUser;
import hu.openig.multiplayer.model.EmpireStatuses;
import hu.openig.multiplayer.model.FleetStatus;
import hu.openig.multiplayer.model.FleetTransferMode;
import hu.openig.multiplayer.model.InventoryItemStatus;
import hu.openig.multiplayer.model.MessageUtils;
import hu.openig.multiplayer.model.MultiplayerGameSetup;
import hu.openig.multiplayer.model.PlanetStatus;
import hu.openig.multiplayer.model.ProductionStatus;
import hu.openig.multiplayer.model.ResearchStatus;
import hu.openig.multiplayer.model.WelcomeResponse;
import hu.openig.net.MessageClient;
import hu.openig.net.MessageObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A remote game API client implementation based
 * on MessageObject exchanges.
 * @author akarnokd, 2013.04.22.
 */
public class RemoteGameClient implements RemoteGameAPI {
	/** The message client object. */
	protected final MessageClient client;
	/**
	 * Constructor. Sets the message client object.
	 * @param client the client object
	 */
	public RemoteGameClient(MessageClient client) {
		this.client = client;
	}
	@Override
	public long ping() throws IOException {
		long t = System.nanoTime();
		MessageUtils.expectObject(client.query("PING{}"), "PONG");
		return (System.nanoTime() - t) / 1000000L;
	}
	@Override
	public WelcomeResponse login(String user, String passphrase, String version)
			throws IOException {
		MessageObject response = MessageUtils.expectObject(client.query(
						new MessageObject("LOGIN", 
								"user", user, 
								"passphrase", passphrase, 
								"version", version)), "WELCOME");
		return WelcomeResponse.from(response);
	}
	@Override
	public void relogin(String sessionId) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void leave() throws IOException {
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
}
