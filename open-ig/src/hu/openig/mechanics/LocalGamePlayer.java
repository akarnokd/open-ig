/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.BattleStatus;
import hu.openig.model.EmpireStatuses;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetStatus;
import hu.openig.model.FleetTransferMode;
import hu.openig.model.GameAPI;
import hu.openig.model.GroundBattleUnit;
import hu.openig.model.InventoryItemStatus;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetStatus;
import hu.openig.model.Player;
import hu.openig.model.ProductionStatuses;
import hu.openig.model.ResearchStatuses;
import hu.openig.model.ResearchType;
import hu.openig.model.SpaceBattleUnit;
import hu.openig.model.World;
import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Game API which executes requests
 * for a given player.
 * <p>Note that the methods should be executed on the EDT,
 * where the world data lives.</p>
 * @author akarnokd, 2013.05.04.
 */
public class LocalGamePlayer implements GameAPI {
	/** The world object. */
	protected final World world;
	/** The player object. */
	protected final Player player;
	/**
	 * Constructor, sets the player object.
	 * @param player the player
	 */
	public LocalGamePlayer(Player player) {
		this.player = player;
		this.world = player.world;
	}

	@Override
	public EmpireStatuses getEmpireStatuses() throws IOException {
		return world.toEmpireStatuses(player.id);
	}

	@Override
	public List<FleetStatus> getFleets() throws IOException {
		List<FleetStatus> result = new ArrayList<>(player.fleets.size() + 1);
		
		for (Fleet f : player.fleets.keySet()) {
			result.add(f.toFleetStatus());
		}
		
		return result;
	}

	@Override
	public FleetStatus getFleet(int fleetId) throws IOException {
		Fleet f = world.fleet(fleetId);
		if (f != null) {
			if (player.knowledge(f, FleetKnowledge.VISIBLE) >= 0) { 
				return f.toFleetStatus();
			}
		}
		throw new ErrorResponse(ErrorType.ERROR_UNKNOWN_FLEET);
	}

	@Override
	public Map<String, Integer> getInventory() throws IOException {
		HashMap<String, Integer> result = new HashMap<>();
		for (Map.Entry<ResearchType, Integer> e : player.inventory.entrySet()) {
			result.put(e.getKey().id, e.getValue());
		}
		return result;
	}

	@Override
	public ProductionStatuses getProductions() throws IOException {
		return player.toProductionStatuses();
	}

	@Override
	public ResearchStatuses getResearches() throws IOException {
		return player.toResearchStatuses();
	}

	@Override
	public List<PlanetStatus> getPlanetStatuses() throws IOException {
		List<PlanetStatus> result = new ArrayList<>();
		for (Map.Entry<Planet, PlanetKnowledge> pe : player.planets.entrySet()) {
			result.add(pe.getKey().toPlanetStatus(player));
		}
		return result;
	}

	@Override
	public PlanetStatus getPlanetStatus(String id) throws IOException {
		Planet p = world.planet(id);
		if (player.knowledge(p, PlanetKnowledge.VISIBLE) >= 0) {
			return p.toPlanetStatus(player);
		}
		throw new ErrorResponse(ErrorType.ERROR_UNKNOWN_PLANET, id);
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
	public int newFleet(String planet, List<InventoryItemStatus> inventory)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int newFleet(int id, List<InventoryItemStatus> inventory)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
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
	public void sellFleetItem(int id, int itemId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int deployFleetItem(int id, String type)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void undeployFleetItem(int id, int itemId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addFleetEquipment(int id, int itemId, String slotId, String type)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeFleetEquipment(int id, int itemId, String slotId)
			throws IOException {
		// TODO Auto-generated method stub

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
	public int build(String planetId, String type, String race, int x, int y)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int build(String planetId, String type, String race)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
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
	public int deployPlanetItem(String planetId, String type)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void undeployPlanetItem(String planetId, int itemId)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sellPlanetItem(String planetId, int itemId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPlanetEquipment(String planetId, int itemId, String slotId,
			String type) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePlanetEquipment(String planetId, int itemId, String slotId)
			throws IOException {
		// TODO Auto-generated method stub

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
	public void stopSpaceUnit(int battleId, int unitId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveSpaceUnit(int battleId, int unitId, double x, double y)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void attackSpaceUnit(int battleId, int unitId, int targetUnitId)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void kamikazeSpaceUnit(int battleId, int unitId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void fireSpaceRocket(int battleId, int unitId, int targetUnitId)
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
	public void stopGroundUnit(int battleId, int unitId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveGroundUnit(int battleId, int unitId, int x, int y)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void attackGroundUnit(int battleId, int unitId, int targetUnitId)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void attackBuilding(int battleId, int unitId, int buildingId)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deployMine(int battleId, int unitId) throws IOException {
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

	@Override
	public InventoryItemStatus getInventoryStatus(int fleetId, int itemId)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public InventoryItemStatus getInventoryStatus(String planetId, int itemId)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
