/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.io.IOException;
import java.util.List;

/**
 * A Game API implementation which
 * dispatches methods to two other Game API
 * instances. Both instances
 * are executed and the thrown exceptions
 * combined into one.
 * @author akarnokd, 2013.05.05.
 */
public class GameCommandAPIDispatcher implements GameCommandAPI {
	/** The first wrapped API. */
	protected final GameCommandAPI first;
	/** The second wrapped API. */
	protected final GameCommandAPI second;
	/**
	 * Constructor, sets both APIs.
	 * @param first the first game API
	 * @param second the second game API
	 */
	public GameCommandAPIDispatcher(GameCommandAPI first, GameCommandAPI second) {
		this.first = first;
		this.second = second;
	}
	@Override
	public void moveFleet(int id, double x, double y) throws IOException {
		first.moveFleet(id, x, y);
		second.moveFleet(id, x, y);
	}
	@Override
	public void addFleetWaypoint(int id, double x, double y) throws IOException {
		first.addFleetWaypoint(id, x, y);
		second.addFleetWaypoint(id, x, y);
	}
	@Override
	public void moveToPlanet(int id, String target) throws IOException {
		first.moveToPlanet(id, target);
		second.moveToPlanet(id, target);
	}
	@Override
	public void followFleet(int id, int target) throws IOException {
		first.followFleet(id, target);
		second.followFleet(id, target);
	}
	@Override
	public void attackFleet(int id, int target) throws IOException {
		first.attackFleet(id, target);
		second.attackFleet(id, target);
	}
	@Override
	public void attackPlanet(int id, String target) throws IOException {
		first.attackPlanet(id, target);
		second.attackPlanet(id, target);
	}
	@Override
	public void colonize(int id, String target) throws IOException {
		first.colonize(id, target);
		second.colonize(id, target);
	}
	@Override
	public int newFleet(String planet, List<InventoryItemStatus> inventory)
			throws IOException {
		// FIXME which id to return?
		int id = first.newFleet(planet, inventory);
		second.newFleet(planet, inventory);
		return id;
	}
	@Override
	public int newFleet(int id, List<InventoryItemStatus> inventory)
			throws IOException {
		// FIXME which id to return?
		int nid = first.newFleet(id, inventory);
		second.newFleet(id, inventory);
		return nid;
	}
	@Override
	public void deleteFleet(int id) throws IOException {
		first.deleteFleet(id);
		second.deleteFleet(id);
	}
	@Override
	public void renameFleet(int id, String name) throws IOException {
		first.renameFleet(id, name);
		second.renameFleet(id, name);
	}
	@Override
	public void sellFleetItem(int id, int itemId) throws IOException {
		first.sellFleetItem(id, itemId);
		second.sellFleetItem(id, itemId);
	}
	@Override
	public int deployFleetItem(int id, String type) throws IOException {
		// FIXME which id to return?
		int nid = first.deployFleetItem(id, type);
		second.deployFleetItem(id, type);
		return nid;
	}
	@Override
	public void undeployFleetItem(int id, int itemId) throws IOException {
		first.undeployFleetItem(id, itemId);
		second.undeployFleetItem(id, itemId);
	}
	@Override
	public void addFleetEquipment(int id, int itemId, String slotId, String type)
			throws IOException {
		first.addFleetEquipment(id, itemId, slotId, type);
		second.addFleetEquipment(id, itemId, slotId, type);
	}
	@Override
	public void removeFleetEquipment(int id, int itemId, String slotId)
			throws IOException {
		first.removeFleetEquipment(id, itemId, slotId);
		second.removeFleetEquipment(id, itemId, slotId);
	}
	@Override
	public void fleetUpgrade(int id) throws IOException {
		first.fleetUpgrade(id);
		second.fleetUpgrade(id);
	}
	@Override
	public void stopFleet(int id) throws IOException {
		first.stopFleet(id);
		second.stopFleet(id);
	}
	@Override
	public void transfer(int sourceFleet, int destinationFleet, int sourceItem,
			FleetTransferMode mode) throws IOException {
		first.transfer(sourceFleet, destinationFleet, sourceItem, mode);
		second.transfer(sourceFleet, destinationFleet, sourceItem, mode);
	}
	@Override
	public void colonize(String id) throws IOException {
		first.colonize(id);
		second.colonize(id);
	}
	@Override
	public void cancelColonize(String id) throws IOException {
		first.cancelColonize(id);
		second.cancelColonize(id);
	}
	@Override
	public int build(String planetId, String type, String race, int x, int y)
			throws IOException {
		// FIXME which id to return?
		int id = first.build(planetId, type, race, x, y);
		second.build(planetId, type, race, x, y);
		return id;
	}
	@Override
	public int build(String planetId, String type, String race)
			throws IOException {
		// FIXME which id to return?
		int id = first.build(planetId, type, race);
		second.build(planetId, type, race);
		return id;
	}
	@Override
	public void enable(String planetId, int id) throws IOException {
		first.enable(planetId, id);
		second.enable(planetId, id);
	}
	@Override
	public void disable(String planetId, int id) throws IOException {
		first.disable(planetId, id);
		second.disable(planetId, id);
	}
	@Override
	public void repair(String planetId, int id) throws IOException {
		first.repair(planetId, id);
		second.repair(planetId, id);
	}
	@Override
	public void repairOff(String planetId, int id) throws IOException {
		first.repairOff(planetId, id);
		second.repairOff(planetId, id);
	}
	@Override
	public void demolish(String planetId, int id) throws IOException {
		first.demolish(planetId, id);
		second.demolish(planetId, id);
	}
	@Override
	public void buildingUpgrade(String planetId, int id, int level)
			throws IOException {
		first.buildingUpgrade(planetId, id, level);
		second.buildingUpgrade(planetId, id, level);
	}
	@Override
	public int deployPlanetItem(String planetId, String type)
			throws IOException {
		// FIXME which id to return
		int id = first.deployPlanetItem(planetId, type);
		second.deployPlanetItem(planetId, type);
		return id;
	}
	@Override
	public void undeployPlanetItem(String planetId, int itemId)
			throws IOException {
		first.undeployPlanetItem(planetId, itemId);
		second.undeployPlanetItem(planetId, itemId);
	}
	@Override
	public void sellPlanetItem(String planetId, int itemId) throws IOException {
		first.sellPlanetItem(planetId, itemId);
		second.sellPlanetItem(planetId, itemId);
	}
	@Override
	public void addPlanetEquipment(String planetId, int itemId, String slotId,
			String type) throws IOException {
		first.addPlanetEquipment(planetId, itemId, slotId, type);
		second.addPlanetEquipment(planetId, itemId, slotId, type);
	}
	@Override
	public void removePlanetEquipment(String planetId, int itemId, String slotId)
			throws IOException {
		first.removePlanetEquipment(planetId, itemId, slotId);
		second.removePlanetEquipment(planetId, itemId, slotId);
	}
	@Override
	public void planetUpgrade(String planetId) throws IOException {
		first.planetUpgrade(planetId);
		second.planetUpgrade(planetId);
	}
	@Override
	public void startProduction(String type) throws IOException {
		first.startProduction(type);
		second.startProduction(type);
	}
	@Override
	public void stopProduction(String type) throws IOException {
		first.stopProduction(type);
		second.stopProduction(type);
	}
	@Override
	public void setProductionQuantity(String type, int count)
			throws IOException {
		first.setProductionQuantity(type, count);
		second.setProductionQuantity(type, count);
	}
	@Override
	public void setProductionPriority(String type, int priority)
			throws IOException {
		first.setProductionPriority(type, priority);
		second.setProductionPriority(type, priority);
	}
	@Override
	public void sellInventory(String type, int count) throws IOException {
		first.sellInventory(type, count);
		second.sellInventory(type, count);
	}
	@Override
	public void startResearch(String type) throws IOException {
		first.startResearch(type);
		second.startResearch(type);
	}
	@Override
	public void stopResearch(String type) throws IOException {
		first.stopResearch(type);
		second.stopResearch(type);
	}
	@Override
	public void setResearchMoney(String type, int money) throws IOException {
		first.setResearchMoney(type, money);
		second.setResearchMoney(type, money);
	}
	@Override
	public void pauseResearch() throws IOException {
		first.pauseResearch();
		second.pauseResearch();
	}
	@Override
	public void pauseProduction() throws IOException {
		first.pauseProduction();
		second.pauseProduction();
	}
	@Override
	public void unpauseProduction() throws IOException {
		first.unpauseProduction();
		second.unpauseProduction();
	}
	@Override
	public void unpauseResearch() throws IOException {
		first.unpauseResearch();
		second.unpauseResearch();
	}
	@Override
	public void stopSpaceUnit(int battleId, int unitId) throws IOException {
		first.stopSpaceUnit(battleId, unitId);
		second.stopSpaceUnit(battleId, unitId);
	}
	@Override
	public void moveSpaceUnit(int battleId, int unitId, double x, double y)
			throws IOException {
		first.moveSpaceUnit(battleId, unitId, x, y);
		second.moveSpaceUnit(battleId, unitId, x, y);
	}
	@Override
	public void attackSpaceUnit(int battleId, int unitId, int targetUnitId)
			throws IOException {
		first.attackSpaceUnit(battleId, unitId, targetUnitId);
		second.attackSpaceUnit(battleId, unitId, targetUnitId);
	}
	@Override
	public void kamikazeSpaceUnit(int battleId, int unitId) throws IOException {
		first.kamikazeSpaceUnit(battleId, unitId);
		second.kamikazeSpaceUnit(battleId, unitId);
	}
	@Override
	public void fireSpaceRocket(int battleId, int unitId, int targetUnitId)
			throws IOException {
		first.fireSpaceRocket(battleId, unitId, targetUnitId);
		second.fireSpaceRocket(battleId, unitId, targetUnitId);
	}
	@Override
	public void spaceRetreat(int battleId) throws IOException {
		first.spaceRetreat(battleId);
		second.spaceRetreat(battleId);
	}
	@Override
	public void stopSpaceRetreat(int battleId) throws IOException {
		first.stopSpaceRetreat(battleId);
		second.stopSpaceRetreat(battleId);
	}
	@Override
	public void fleetFormation(int fleetId, int formation) throws IOException {
		first.fleetFormation(fleetId, formation);
		second.fleetFormation(fleetId, formation);
	}
	@Override
	public void stopGroundUnit(int battleId, int unitId) throws IOException {
		first.stopGroundUnit(battleId, unitId);
		second.stopGroundUnit(battleId, unitId);
	}
	@Override
	public void moveGroundUnit(int battleId, int unitId, int x, int y)
			throws IOException {
		first.moveGroundUnit(battleId, unitId, x, y);
		second.moveGroundUnit(battleId, unitId, x, y);
	}
	@Override
	public void attackGroundUnit(int battleId, int unitId, int targetUnitId)
			throws IOException {
		first.attackGroundUnit(battleId, unitId, targetUnitId);
		second.attackGroundUnit(battleId, unitId, targetUnitId);
	}
	@Override
	public void attackBuilding(int battleId, int unitId, int buildingId)
			throws IOException {
		first.attackBuilding(battleId, unitId, buildingId);
		second.attackBuilding(battleId, unitId, buildingId);
	}
	@Override
	public void deployMine(int battleId, int unitId) throws IOException {
		first.deployMine(battleId, unitId);
		second.deployMine(battleId, unitId);
	}
	@Override
	public void groundRetreat(int battleId) throws IOException {
		first.groundRetreat(battleId);
		second.groundRetreat(battleId);
	}
	@Override
	public void stopGroundRetreat(int battleId) throws IOException {
		first.stopGroundRetreat(battleId);
		second.stopGroundRetreat(battleId);
	}
	@Override
	public void setAutoBuild(String planetId, AutoBuild auto)
			throws IOException {
		first.setAutoBuild(planetId, auto);
		second.setAutoBuild(planetId, auto);
	}
	@Override
	public void setTaxLevel(String planetId, TaxLevel tax) throws IOException {
		first.setTaxLevel(planetId, tax);
		second.setTaxLevel(planetId, tax);
	}
}
