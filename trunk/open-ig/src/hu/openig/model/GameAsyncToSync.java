/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.AsyncResult;
import hu.openig.core.DeferredAction;
import hu.openig.core.DeferredVoid;
import hu.openig.utils.U;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An asynchronous game API implementation which turns
 * the calls into the synchronous API calls.
 * @author akarnokd, 2013.05.01.
 *
 */
public class GameAsyncToSync implements GameAsyncAPI {
	/** The wrapped API. */
	protected final GameAPI api;
	/** The list of deferred action calls. */
	protected List<Runnable> batch;
	/**
	 * Constructor, sets the API object.
	 * @param api the synchronous game API
	 */
	public GameAsyncToSync(GameAPI api) {
		this.api = api;
	}

	/**
	 * Checks if this sync object is in batch mode.
	 * @return true if in batch mode
	 */
	protected boolean isBatch() {
		return batch != null;
	}
	
	@Override
	public void begin() {
		if (isBatch()) {
			throw new IllegalStateException("is batch");
		}
		batch = new ArrayList<>();
	}

	@Override
	public void end() throws IOException {
		end(null);
	}

	@Override
	public void end(AsyncResult<? super Void, ? super IOException> out) {
		if (!isBatch()) {
			throw new IllegalStateException("not in batch");
		}
		List<Runnable> bs = U.newArrayList(batch);
		batch = null;
		for (Runnable r : bs) {
			r.run();
		}
		if (out != null) {
			out.onSuccess(null);
		}
	}
	@Override
	public void cancel() {
		if (!isBatch()) {
			throw new IllegalStateException("not in batch");
		}
		batch = null;
	}
	/**
	 * Executes an action or adds it to the batch list.
	 * @param action the action to queue or execute
	 */
	protected void execute(Runnable action) {
		if (isBatch()) {
			batch.add(action);
		} else {
			action.run();
		}
	}

	@Override
	public void getEmpireStatuses(
			AsyncResult<? super EmpireStatuses, ? super IOException> out) {
		execute(new DeferredAction<EmpireStatuses, IOException>(out) {
			@Override
			public EmpireStatuses invoke() throws IOException {
				return api.getEmpireStatuses();
			}
		});
		
	}

	@Override
	public void getFleets(
			AsyncResult<? super List<FleetStatus>, ? super IOException> out) {
		execute(new DeferredAction<List<FleetStatus>, IOException>(out) {
			@Override
			public List<FleetStatus> invoke() throws IOException {
				return api.getFleets();
			}
		});
		
	}

	@Override
	public void getFleet(final int fleetId,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		execute(new DeferredAction<FleetStatus, IOException>(out) {
			@Override
			public FleetStatus invoke() throws IOException {
				return api.getFleet(fleetId);
			}
		});
		
	}

	@Override
	public void getInventory(
			AsyncResult<? super Map<String, Integer>, ? super IOException> out) {
		execute(new DeferredAction<Map<String, Integer>, IOException>(out) {
			@Override
			public Map<String, Integer> invoke() throws IOException {
				return api.getInventory();
			}
		});
		
	}

	@Override
	public void getProductions(
			AsyncResult<? super ProductionStatuses, ? super IOException> out) {
		execute(new DeferredAction<ProductionStatuses, IOException>(out) {
			@Override
			public ProductionStatuses invoke() throws IOException {
				return api.getProductions();
			}
		});
		
	}

	@Override
	public void getResearches(
			AsyncResult<? super ResearchStatuses, ? super IOException> out) {
		execute(new DeferredAction<ResearchStatuses, IOException>(out) {
			@Override
			public ResearchStatuses invoke() throws IOException {
				return api.getResearches();
			}
		});
		
	}

	@Override
	public void getPlanetStatuses(
			AsyncResult<? super List<PlanetStatus>, ? super IOException> out) {
		execute(new DeferredAction<List<PlanetStatus>, IOException>(out) {
			@Override
			public List<PlanetStatus> invoke() throws IOException {
				return api.getPlanetStatuses();
			}
		});
		
	}

	@Override
	public void getPlanetStatus(final String id,
			AsyncResult<? super PlanetStatus, ? super IOException> out) {
		execute(new DeferredAction<PlanetStatus, IOException>(out) {
			@Override
			public PlanetStatus invoke() throws IOException {
				return api.getPlanetStatus(id);
			}
		});
		
	}

	@Override
	public void moveFleet(final int id, final double x, final double y,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.moveFleet(id, x, y);
			}
		});
	}

	@Override
	public void addFleetWaypoint(final int id, final double x, final double y,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.addFleetWaypoint(id, x, y);
			}
		});
	}

	@Override
	public void moveToPlanet(final int id, final String target,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.moveToPlanet(id, target);
			}
		});
		
	}

	@Override
	public void followFleet(final int id, final int target,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.followFleet(id, target);
			}
		});
	}

	@Override
	public void attackFleet(final int id, final int target,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.attackFleet(id, target);
			}
		});
		
	}

	@Override
	public void attackPlanet(final int id, final String target,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.attackPlanet(id, target);
			}
		});
	}

	@Override
	public void colonize(final int id, final String target,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.colonize(id, target);
			}
		});
	}

	@Override
	public void newFleet(final String planet, final List<InventoryItemStatus> inventory,
			AsyncResult<? super Integer, ? super IOException> out) {
		execute(new DeferredAction<Integer, IOException>(out) {
			@Override
			public Integer invoke() throws IOException {
				return api.newFleet(planet, inventory);
			}
		});
		
	}

	@Override
	public void newFleet(final int id, final List<InventoryItemStatus> inventory,
			AsyncResult<? super Integer, ? super IOException> out) {
		execute(new DeferredAction<Integer, IOException>(out) {
			@Override
			public Integer invoke() throws IOException {
				return api.newFleet(id, inventory);
			}
		});
		
	}

	@Override
	public void deleteFleet(final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.deleteFleet(id);
			}
		});
	}

	@Override
	public void renameFleet(final int id, final String name,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.renameFleet(id, name);
			}
		});
	}

	@Override
	public void sellFleetItem(final int id, final int itemId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.sellFleetItem(id, itemId);
			}
		});
		
	}

	@Override
	public void deployFleetItem(final int id, final String type,
			AsyncResult<? super Integer, ? super IOException> out) {
		execute(new DeferredAction<Integer, IOException>(out) {
			@Override
			public Integer invoke() throws IOException {
				return api.deployFleetItem(id, type);
			}
		});
		
	}

	@Override
	public void undeployFleetItem(final int id, final int itemId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.undeployFleetItem(id, itemId);
			}
		});
		
	}

	@Override
	public void addFleetEquipment(final int id, final int itemId, final String slotId,
			final String type,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.addFleetEquipment(id, itemId, slotId, type);
			}
		});
		
	}

	@Override
	public void removeFleetEquipment(final int id, final int itemId, final String slotId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.removeFleetEquipment(id, itemId, slotId);
			}
		});
		
	}

	@Override
	public void fleetUpgrade(final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.fleetUpgrade(id);
			}
		});
	}

	@Override
	public void stopFleet(final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.stopFleet(id);
			}
		});
	}

	@Override
	public void transfer(final int sourceFleet, final int destinationFleet, 
			final int sourceItem,
			final FleetTransferMode mode,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.transfer(sourceFleet, destinationFleet, sourceItem, mode);
			}
		});
	}

	@Override
	public void colonize(final String id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.colonize(id);
			}
		});
	}

	@Override
	public void cancelColonize(final String id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.cancelColonize(id);
			}
		});
	}

	@Override
	public void build(final String planetId, final String type, 
			final String race, final int x, final int y,
			AsyncResult<? super Integer, ? super IOException> out) {
		execute(new DeferredAction<Integer, IOException>(out) {
			@Override
			public Integer invoke() throws IOException {
				return api.build(planetId, type, race, x, y);
			}
		});
	}

	@Override
	public void build(final String planetId, final String type, final String race,
			AsyncResult<? super Integer, ? super IOException> out) {
		execute(new DeferredAction<Integer, IOException>(out) {
			@Override
			public Integer invoke() throws IOException {
				return api.build(planetId, type, race);
			}
		});
	}

	@Override
	public void enable(final String planetId, final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.enable(planetId, id);
			}
		});
	}

	@Override
	public void disable(final String planetId, final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.disable(planetId, id);
			}
		});
	}

	@Override
	public void repair(final String planetId, final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.repair(planetId, id);
			}
		});
	}

	@Override
	public void repairOff(final String planetId, final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.repairOff(planetId, id);
			}
		});
		
	}

	@Override
	public void demolish(final String planetId, final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.demolish(planetId, id);
			}
		});
	}

	@Override
	public void buildingUpgrade(final String planetId, final int id, 
			final int level,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.buildingUpgrade(planetId, id, level);
			}
		});
		
	}

	@Override
	public void deployPlanetItem(final String planetId, final String type,
			AsyncResult<? super Integer, ? super IOException> out) {
		execute(new DeferredAction<Integer, IOException>(out) {
			@Override
			public Integer invoke() throws IOException {
				return api.deployPlanetItem(planetId, type);
			}
		});
		
	}

	@Override
	public void undeployPlanetItem(final String planetId, final int itemId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.undeployPlanetItem(planetId, itemId);
			}
		});
		
	}

	@Override
	public void sellPlanetItem(final String planetId, final int itemId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.sellPlanetItem(planetId, itemId);
			}
		});
		
	}

	@Override
	public void addPlanetEquipment(final String planetId, final int itemId, 
			final String slotId, final String type,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.addPlanetEquipment(planetId, itemId, slotId, type);
			}
		});
		
	}

	@Override
	public void removePlanetEquipment(final String planetId, final int itemId,
			final String slotId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.removePlanetEquipment(planetId, itemId, slotId);
			}
		});
		
	}

	@Override
	public void planetUpgrade(final String planetId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.planetUpgrade(planetId);
			}
		});
	}

	@Override
	public void startProduction(final String type,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.startProduction(type);
			}
		});
	}

	@Override
	public void stopProduction(final String type,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.stopProduction(type);
			}
		});
	}

	@Override
	public void setProductionQuantity(final String type, final int count,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.setProductionQuantity(type, count);
			}
		});
	}

	@Override
	public void setProductionPriority(final String type, final int priority,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.setProductionPriority(type, priority);
			}
		});
	}

	@Override
	public void sellInventory(final String type, final int count,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.sellInventory(type, count);
			}
		});
		
	}

	@Override
	public void startResearch(final String type,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.startResearch(type);
			}
		});
	}

	@Override
	public void stopResearch(final String type,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.stopResearch(type);
			}
		});
		
	}

	@Override
	public void setResearchMoney(final String type, final int money,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.setResearchMoney(type, money);
			}
		});
	}

	@Override
	public void pauseResearch(AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.pauseResearch();
			}
		});
	}

	@Override
	public void pauseProduction(
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.pauseProduction();
			}
		});
	}

	@Override
	public void unpauseProduction(
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.unpauseProduction();
			}
		});
	}

	@Override
	public void unpauseResearch(
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.unpauseResearch();
			}
		});
	}

	@Override
	public void stopSpaceUnit(final int battleId, final int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.stopSpaceUnit(battleId, unitId);
			}
		});
		
	}

	@Override
	public void moveSpaceUnit(final int battleId, final int unitId, final double x, final double y,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.moveSpaceUnit(battleId, unitId, x, y);
			}
		});
		
	}

	@Override
	public void attackSpaceUnit(final int battleId, final int unitId, final int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.attackSpaceUnit(battleId, unitId, targetUnitId);
			}
		});
		
	}

	@Override
	public void kamikazeSpaceUnit(final int battleId, final int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.kamikazeSpaceUnit(battleId, unitId);
			}
		});
	}

	@Override
	public void fireSpaceRocket(final int battleId, final int unitId, final int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.fireSpaceRocket(battleId, unitId, targetUnitId);
			}
		});
		
	}

	@Override
	public void spaceRetreat(final int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.spaceRetreat(battleId);
			}
		});
		
	}

	@Override
	public void stopSpaceRetreat(final int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.stopSpaceRetreat(battleId);
			}
		});
	}

	@Override
	public void fleetFormation(final int fleetId, final int formation,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.fleetFormation(fleetId, formation);
			}
		});
	}

	@Override
	public void getBattles(
			AsyncResult<? super List<BattleStatus>, ? super IOException> out) {
		execute(new DeferredAction<List<BattleStatus>, IOException>(out) {
			@Override
			public List<BattleStatus> invoke() throws IOException {
				return api.getBattles();
			}
		});
		
	}

	@Override
	public void getBattle(final int battleId,
			AsyncResult<? super BattleStatus, ? super IOException> out) {
		execute(new DeferredAction<BattleStatus, IOException>(out) {
			@Override
			public BattleStatus invoke() throws IOException {
				return api.getBattle(battleId);
			}
		});
		
	}

	@Override
	public void getSpaceBattleUnits(final int battleId,
			AsyncResult<? super List<SpaceBattleUnit>, ? super IOException> out) {
		execute(new DeferredAction<List<SpaceBattleUnit>, IOException>(out) {
			@Override
			public List<SpaceBattleUnit> invoke() throws IOException {
				return api.getSpaceBattleUnits(battleId);
			}
		});
		
	}

	@Override
	public void stopGroundUnit(final int battleId, final int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.stopGroundUnit(battleId, unitId);
			}
		});
		
	}

	@Override
	public void moveGroundUnit(final int battleId, final int unitId, final int x, final int y,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.moveGroundUnit(battleId, unitId, x, y);
			}
		});
		
	}

	@Override
	public void attackGroundUnit(final int battleId, final int unitId, final int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.attackGroundUnit(battleId, unitId, targetUnitId);
			}
		});
		
	}

	@Override
	public void attackBuilding(final int battleId, final int unitId, final int buildingId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.attackBuilding(battleId, unitId, buildingId);
			}
		});
		
	}

	@Override
	public void deployMine(final int battleId, final int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.deployMine(battleId, unitId);
			}
		});
		
	}

	@Override
	public void groundRetreat(final int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.groundRetreat(battleId);
			}
		});
		
	}

	@Override
	public void stopGroundRetreat(final int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.stopGroundRetreat(battleId);
			}
		});
		
	}

	@Override
	public void getGroundBattleUnits(final int battleId,
			AsyncResult<? super List<GroundwarUnitStatus>, ? super IOException> out) {
		execute(new DeferredAction<List<GroundwarUnitStatus>, IOException>(out) {
			@Override
			public List<GroundwarUnitStatus> invoke() throws IOException {
				return api.getGroundBattleUnits(battleId);
			}
		});
	}

	@Override
	public void getInventoryStatus(final int fleetId, final int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		execute(new DeferredAction<InventoryItemStatus, IOException>(out) {
			@Override
			public InventoryItemStatus invoke() throws IOException {
				return api.getInventoryStatus(fleetId, itemId);
			}
		});
	}
	@Override
	public void getInventoryStatus(final String planetId, final int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		execute(new DeferredAction<InventoryItemStatus, IOException>(out) {
			@Override
			public InventoryItemStatus invoke() throws IOException {
				return api.getInventoryStatus(planetId, itemId);
			}
		});
	}
	@Override
	public void setAutoBuild(final String planetId, final AutoBuild auto,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.setAutoBuild(planetId, auto);
			}
		});
	}
	@Override
	public void setTaxLevel(final String planetId, final TaxLevel tax,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				api.setTaxLevel(planetId, tax);
			}
		});
	}
}
