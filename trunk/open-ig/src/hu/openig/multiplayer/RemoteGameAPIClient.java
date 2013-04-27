/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Result;
import hu.openig.model.InventoryItem;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.MultiplayerUser;
import hu.openig.model.Production;
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
	public Result<Void, IOException> relogin(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> leave() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<MultiplayerDefinition, IOException> getGameDefinition() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> choosePlayerSettings(MultiplayerUser user) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<MultiplayerGameSetup, IOException> join() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> ready() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<EmpireStatuses, IOException> getEmpireStatuses() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<List<FleetStatus>, IOException> getFleets() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<FleetStatus, IOException> getFleet(int fleetId) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Map<String, Integer>, IOException> getInventory() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<List<Production>, IOException> getProductions() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<ResearchStatus, IOException> getResearches() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<List<PlanetStatus>, IOException> getPlanetStatuses() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<PlanetStatus, IOException> getPlanetStatus(String id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> moveFleet(int id, double x, double y) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> addFleetWaypoint(int id, double x, double y) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> moveToPlanet(int id, String target) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> followFleet(int id, int target) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> attackFleet(int id, int target) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> attackPlanet(int id, String target) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> colonize(int id, String target) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<FleetStatus, IOException> newFleet(String planet) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<FleetStatus, IOException> newFleet(String planet,
			List<InventoryItem> inventory) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<FleetStatus, IOException> newFleet(int id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<FleetStatus, IOException> newFleet(int id,
			List<InventoryItem> inventory) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> deleteFleet(int id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> renameFleet(int id, String name) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<InventoryItemStatus, IOException> sellFleetItem(int id,
			int itemId) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<InventoryItemStatus, IOException> deployFleetItem(int id,
			String type) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<InventoryItemStatus, IOException> undeployFleetItem(int id,
			int itemId) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<InventoryItemStatus, IOException> addFleetEquipment(int id,
			int itemId, String slotId, String type) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<InventoryItemStatus, IOException> removeFleetEquipment(
			int id, int itemId, String slotId) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> fleetUpgrade(int id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> stopFleet(int id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> transfer(int sourceFleet,
			int destinationFleet, int sourceItem, FleetTransferMode mode) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> colonize(String id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> cancelColonize(String id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Integer, IOException> build(String planetId, String type,
			String race, int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Integer, IOException> build(String planetId, String type,
			String race) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> enable(String planetId, int id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> disable(String planetId, int id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> repair(String planetId, int id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> repairOff(String planetId, int id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> demolish(String planetId, int id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> buildingUpgrade(String planetId, int id,
			int level) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<InventoryItemStatus, IOException> deployPlanetItem(
			String planetId, String type) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<InventoryItemStatus, IOException> undeployPlanetItem(
			String planetId, int itemId) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<InventoryItemStatus, IOException> sellPlanetItem(
			String planetId, int itemId) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<InventoryItemStatus, IOException> addPlanetEquipment(
			String planetId, int itemId, String slotId, String type) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<InventoryItemStatus, IOException> removePlanetEquipment(
			String planetId, int itemId, String slotId) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> planetUpgrade(String planetId) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> startProduction(String type) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> stopProduction(String type) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> setProductionQuantity(String type,
			int count) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> setProductionPriority(String type,
			int priority) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> sellInventory(String type, int count) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> startResearch(String type) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> stopResearch(String type) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Result<Void, IOException> setResearchMoney(String type, int money) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
