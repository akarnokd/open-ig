/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.AsyncSubject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Wraps a RemoteGameAsyncAPI object and
 * converts the synchron API calls to async
 * API calls. Calling methods on this
 * class is a blocking operation.
 * @author akarnokd, 2013.05.04.
 */
public class GameSyncToAsync implements GameAPI {
	/** The wrapped async api. */
	protected final GameAsyncAPI api;
	/**
	 * Constructor, sets the async API.
	 * @param api the async API
	 */
	public GameSyncToAsync(GameAsyncAPI api) {
		this.api = api;
	}
	/** A value result specific class. */
	protected static class Value<T> extends AsyncSubject<T, IOException> { }
	/**
	 * Creates an async subject which will receive the 
	 * asynchronous response.
	 * @param <T> the result value type
	 * @return the async subject object
	 */
	protected <T> Value<T> newSubject() {
		return new Value<>();
	}
	/**
	 * Extracts the value or exception from the given async subject,
	 * waiting indefinitely if necessary.
	 * @param <T> the value type
	 * @param as the async subject
	 * @return the value
	 * @throws IOException on error
	 */
	protected <T> T get(AsyncSubject<T, IOException> as) throws IOException {
		try {
			return as.get();
		} catch (Exception ex) {
			if (ex instanceof IOException) {
				throw (IOException)ex;
			}
			throw new IOException(ex);
		}
	}
	@Override
	public EmpireStatuses getEmpireStatuses() throws IOException {
		Value<EmpireStatuses> as = newSubject();
		api.getEmpireStatuses(as);
		return get(as);
	}
	@Override
	public List<FleetStatus> getFleets() throws IOException {
		Value<List<FleetStatus>> as = newSubject();
		api.getFleets(as);
		return get(as);
	}
	@Override
	public FleetStatus getFleet(int fleetId) throws IOException {
		Value<FleetStatus> as = newSubject();
		api.getFleet(fleetId, as);
		return get(as);
	}
	@Override
	public Map<String, Integer> getInventory() throws IOException {
		Value<Map<String, Integer>> as = newSubject();
		api.getInventory(as);
		return get(as);
	}
	@Override
	public ProductionStatuses getProductions() throws IOException {
		Value<ProductionStatuses> as = newSubject();
		api.getProductions(as);
		return get(as);
	}
	@Override
	public ResearchStatuses getResearches() throws IOException {
		Value<ResearchStatuses> as = newSubject();
		api.getResearches(as);
		return get(as);
	}
	@Override
	public List<PlanetStatus> getPlanetStatuses() throws IOException {
		Value<List<PlanetStatus>> as = newSubject();
		api.getPlanetStatuses(as);
		return get(as);
	}
	@Override
	public PlanetStatus getPlanetStatus(String id) throws IOException {
		Value<PlanetStatus> as = newSubject();
		api.getPlanetStatus(id, as);
		return get(as);
	}
	@Override
	public void moveFleet(int id, double x, double y) throws IOException {
		Value<Void> as = newSubject();
		api.moveFleet(id, x, y, as);
		get(as);
	}
	@Override
	public void addFleetWaypoint(int id, double x, double y) throws IOException {
		Value<Void> as = newSubject();
		api.addFleetWaypoint(id, x, y, as);
		get(as);
	}
	@Override
	public void moveToPlanet(int id, String target) throws IOException {
		Value<Void> as = newSubject();
		api.moveToPlanet(id, target, as);
		get(as);
	}
	@Override
	public void followFleet(int id, int target) throws IOException {
		Value<Void> as = newSubject();
		api.followFleet(id, target, as);
		get(as);
	}
	@Override
	public void attackFleet(int id, int target) throws IOException {
		Value<Void> as = newSubject();
		api.attackFleet(id, target, as);
		get(as);
	}
	@Override
	public void attackPlanet(int id, String target) throws IOException {
		Value<Void> as = newSubject();
		api.attackPlanet(id, target, as);
		get(as);
	}
	@Override
	public void colonize(int id, String target) throws IOException {
		Value<Void> as = newSubject();
		api.colonize(id, target, as);
		get(as);
	}
	@Override
	public int newFleet(String planet, List<InventoryItemStatus> inventory)
			throws IOException {
		Value<Integer> as = newSubject();
		api.newFleet(planet, inventory, as);
		return get(as);
	}
	@Override
	public int newFleet(int id, List<InventoryItemStatus> inventory)
			throws IOException {
		Value<Integer> as = newSubject();
		api.newFleet(id, inventory, as);
		return get(as);
	}
	@Override
	public void deleteFleet(int id) throws IOException {
		Value<Void> as = newSubject();
		api.deleteFleet(id, as);
		get(as);
	}
	@Override
	public void renameFleet(int id, String name) throws IOException {
		Value<Void> as = newSubject();
		api.renameFleet(id, name, as);
		get(as);
	}
	@Override
	public void sellFleetItem(int id, int itemId)
			throws IOException {
		Value<Void> as = newSubject();
		api.sellFleetItem(id, itemId, as);
		get(as);
	}
	@Override
	public int deployFleetItem(int id, String type)
			throws IOException {
		Value<Integer> as = newSubject();
		api.deployFleetItem(id, type, as);
		return get(as);
	}
	@Override
	public void undeployFleetItem(int id, int itemId)
			throws IOException {
		Value<Void> as = newSubject();
		api.undeployFleetItem(id, itemId, as);
		get(as);
	}
	@Override
	public void addFleetEquipment(int id, int itemId,
			String slotId, String type) throws IOException {
		Value<Void> as = newSubject();
		api.addFleetEquipment(id, itemId, slotId, type, as);
		get(as);
	}
	@Override
	public void removeFleetEquipment(int id, int itemId,
			String slotId) throws IOException {
		Value<Void> as = newSubject();
		api.removeFleetEquipment(id, itemId, slotId, as);
		get(as);
	}
	@Override
	public void fleetUpgrade(int id) throws IOException {
		Value<Void> as = newSubject();
		api.fleetUpgrade(id, as);
		get(as);
	}
	@Override
	public void stopFleet(int id) throws IOException {
		Value<Void> as = newSubject();
		api.stopFleet(id, as);
		get(as);
	}
	@Override
	public void transfer(int sourceFleet, int destinationFleet, int sourceItem,
			FleetTransferMode mode) throws IOException {
		Value<Void> as = newSubject();
		api.transfer(sourceFleet, destinationFleet, sourceItem, mode, as);
		get(as);
	}
	@Override
	public void colonize(String id) throws IOException {
		Value<Void> as = newSubject();
		api.colonize(id, as);
		get(as);
	}
	@Override
	public void cancelColonize(String id) throws IOException {
		Value<Void> as = newSubject();
		api.cancelColonize(id, as);
		get(as);
	}
	@Override
	public int build(String planetId, String type, String race, int x, int y)
			throws IOException {
		Value<Integer> as = newSubject();
		api.build(planetId, type, race, x, y, as);
		return get(as);
	}
	@Override
	public int build(String planetId, String type, String race)
			throws IOException {
		Value<Integer> as = newSubject();
		api.build(planetId, type, race, as);
		return get(as);
	}
	@Override
	public void enable(String planetId, int id) throws IOException {
		Value<Void> as = newSubject();
		api.enable(planetId, id, as);
		get(as);
	}
	@Override
	public void disable(String planetId, int id) throws IOException {
		Value<Void> as = newSubject();
		api.disable(planetId, id, as);
		get(as);
	}
	@Override
	public void repair(String planetId, int id) throws IOException {
		Value<Void> as = newSubject();
		api.repair(planetId, id, as);
		get(as);
	}
	@Override
	public void repairOff(String planetId, int id) throws IOException {
		Value<Void> as = newSubject();
		api.repairOff(planetId, id, as);
		get(as);
	}
	@Override
	public void demolish(String planetId, int id) throws IOException {
		Value<Void> as = newSubject();
		api.demolish(planetId, id, as);
		get(as);
	}
	@Override
	public void buildingUpgrade(String planetId, int id, int level)
			throws IOException {
		Value<Void> as = newSubject();
		api.buildingUpgrade(planetId, id, level, as);
		get(as);
	}
	@Override
	public int deployPlanetItem(String planetId, String type)
			throws IOException {
		Value<Integer> as = newSubject();
		api.deployPlanetItem(planetId, type, as);
		return get(as);
	}
	@Override
	public void undeployPlanetItem(String planetId, int itemId)
			throws IOException {
		Value<Void> as = newSubject();
		api.undeployPlanetItem(planetId, itemId, as);
		get(as);
	}
	@Override
	public void sellPlanetItem(String planetId, int itemId)
			throws IOException {
		Value<Void> as = newSubject();
		api.sellPlanetItem(planetId, itemId, as);
		get(as);
	}
	@Override
	public void addPlanetEquipment(String planetId, int itemId,
			String slotId, String type) throws IOException {
		Value<Void> as = newSubject();
		api.addPlanetEquipment(planetId, itemId, slotId, type, as);
		get(as);
	}
	@Override
	public void removePlanetEquipment(String planetId,
			int itemId, String slotId) throws IOException {
		Value<Void> as = newSubject();
		api.removePlanetEquipment(planetId, itemId, slotId, as);
		get(as);
	}
	@Override
	public void planetUpgrade(String planetId) throws IOException {
		Value<Void> as = newSubject();
		api.planetUpgrade(planetId, as);
		get(as);
	}
	@Override
	public void startProduction(String type) throws IOException {
		Value<Void> as = newSubject();
		api.startProduction(type, as);
		get(as);
	}
	@Override
	public void stopProduction(String type) throws IOException {
		Value<Void> as = newSubject();
		api.stopProduction(type, as);
		get(as);
	}
	@Override
	public void setProductionQuantity(String type, int count)
			throws IOException {
		Value<Void> as = newSubject();
		api.setProductionQuantity(type, count, as);
		get(as);
	}
	@Override
	public void setProductionPriority(String type, int priority)
			throws IOException {
		Value<Void> as = newSubject();
		api.setProductionPriority(type, priority, as);
		get(as);
	}
	@Override
	public void sellInventory(String type, int count) throws IOException {
		Value<Void> as = newSubject();
		api.sellInventory(type, count, as);
		get(as);
	}
	@Override
	public void startResearch(String type) throws IOException {
		Value<Void> as = newSubject();
		api.startResearch(type, as);
		get(as);
	}
	@Override
	public void stopResearch(String type) throws IOException {
		Value<Void> as = newSubject();
		api.stopResearch(type, as);
		get(as);
	}
	@Override
	public void setResearchMoney(String type, int money) throws IOException {
		Value<Void> as = newSubject();
		api.setResearchMoney(type, money, as);
		get(as);
	}
	@Override
	public void pauseResearch() throws IOException {
		Value<Void> as = newSubject();
		api.pauseResearch(as);
		get(as);
	}
	@Override
	public void pauseProduction() throws IOException {
		Value<Void> as = newSubject();
		api.pauseProduction(as);
		get(as);
	}
	@Override
	public void unpauseProduction() throws IOException {
		Value<Void> as = newSubject();
		api.unpauseProduction(as);
		get(as);
	}
	@Override
	public void unpauseResearch() throws IOException {
		Value<Void> as = newSubject();
		api.unpauseResearch(as);
		get(as);
	}
	@Override
	public void stopSpaceUnit(int battleId, int unitId) throws IOException {
		Value<Void> as = newSubject();
		api.stopSpaceUnit(battleId, unitId, as);
		get(as);
	}
	@Override
	public void moveSpaceUnit(int battleId, int unitId, double x, double y)
			throws IOException {
		Value<Void> as = newSubject();
		api.moveSpaceUnit(battleId, unitId, x, y, as);
		get(as);
	}
	@Override
	public void attackSpaceUnit(int battleId, int unitId, int targetUnitId)
			throws IOException {
		Value<Void> as = newSubject();
		api.attackSpaceUnit(battleId, unitId, targetUnitId, as);
		get(as);
	}
	@Override
	public void kamikazeSpaceUnit(int battleId, int unitId) throws IOException {
		Value<Void> as = newSubject();
		api.kamikazeSpaceUnit(battleId, unitId, as);
		get(as);
	}
	@Override
	public void fireSpaceRocket(int battleId, int unitId, int targetUnitId)
			throws IOException {
		Value<Void> as = newSubject();
		api.fireSpaceRocket(battleId, unitId, targetUnitId, as);
		get(as);
	}
	@Override
	public void spaceRetreat(int battleId) throws IOException {
		Value<Void> as = newSubject();
		api.spaceRetreat(battleId, as);
		get(as);
	}
	@Override
	public void stopSpaceRetreat(int battleId) throws IOException {
		Value<Void> as = newSubject();
		api.stopSpaceRetreat(battleId, as);
		get(as);
	}
	@Override
	public void fleetFormation(int fleetId, int formation) throws IOException {
		Value<Void> as = newSubject();
		api.fleetFormation(fleetId, formation, as);
		get(as);
	}
	@Override
	public List<BattleStatus> getBattles() throws IOException {
		Value<List<BattleStatus>> as = newSubject();
		api.getBattles(as);
		return get(as);
	}
	@Override
	public BattleStatus getBattle(int battleId) throws IOException {
		Value<BattleStatus> as = newSubject();
		api.getBattle(battleId, as);
		return get(as);
	}
	@Override
	public List<SpaceBattleUnit> getSpaceBattleUnits(int battleId)
			throws IOException {
		Value<List<SpaceBattleUnit>> as = newSubject();
		api.getSpaceBattleUnits(battleId, as);
		return get(as);
	}
	@Override
	public void stopGroundUnit(int battleId, int unitId) throws IOException {
		Value<Void> as = newSubject();
		api.stopGroundUnit(battleId, unitId, as);
		get(as);
	}
	@Override
	public void moveGroundUnit(int battleId, int unitId, int x, int y)
			throws IOException {
		Value<Void> as = newSubject();
		api.moveGroundUnit(battleId, unitId, x, y, as);
		get(as);
	}
	@Override
	public void attackGroundUnit(int battleId, int unitId, int targetUnitId)
			throws IOException {
		Value<Void> as = newSubject();
		api.attackGroundUnit(battleId, unitId, targetUnitId, as);
		get(as);
	}
	@Override
	public void attackBuilding(int battleId, int unitId, int buildingId)
			throws IOException {
		Value<Void> as = newSubject();
		api.attackBuilding(battleId, unitId, buildingId, as);
		get(as);
	}
	@Override
	public void deployMine(int battleId, int unitId) throws IOException {
		Value<Void> as = newSubject();
		api.deployMine(battleId, unitId, as);
		get(as);
	}
	@Override
	public void groundRetreat(int battleId) throws IOException {
		Value<Void> as = newSubject();
		api.groundRetreat(battleId, as);
		get(as);
	}
	@Override
	public void stopGroundRetreat(int battleId) throws IOException {
		Value<Void> as = newSubject();
		api.stopGroundRetreat(battleId, as);
		get(as);
	}
	@Override
	public List<GroundwarUnitStatus> getGroundBattleUnits(int battleId)
			throws IOException {
		Value<List<GroundwarUnitStatus>> as = newSubject();
		api.getGroundBattleUnits(battleId, as);
		return get(as);
	}
	@Override
	public InventoryItemStatus getInventoryStatus(int fleetId, int itemId)
			throws IOException {
		Value<InventoryItemStatus> as = newSubject();
		api.getInventoryStatus(fleetId, itemId, as);
		return get(as);
	}
	@Override
	public InventoryItemStatus getInventoryStatus(String planetId, int itemId)
			throws IOException {
		Value<InventoryItemStatus> as = newSubject();
		api.getInventoryStatus(planetId, itemId, as);
		return get(as);
	}
	@Override
	public void setAutoBuild(String planetId, AutoBuild auto)
			throws IOException {
		Value<Void> as = newSubject();
		api.setAutoBuild(planetId, auto, as);
		get(as);
	}
	@Override
	public void setTaxLevel(String planetId, TaxLevel tax) throws IOException {
		Value<Void> as = newSubject();
		api.setTaxLevel(planetId, tax, as);
		get(as);
	}
}
