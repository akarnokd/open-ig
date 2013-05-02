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
import hu.openig.multiplayer.model.DeferredAction;
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
import hu.openig.utils.U;

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
	/** The list of deferred action calls. */
	protected List<DeferredAction<?, ? super IOException>> batch;
	/**
	 * Constructor, sets the API object.
	 * @param api the synchronous game API
	 */
	public RemoteGameAsyncToSync(RemoteGameAPI api) {
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
		batch = U.newArrayList();
	}

	@Override
	public void end() throws IOException {
		end(null);
	}

	@Override
	public void end(AsyncResult<? super Void, ? super IOException> out) {
		List<DeferredAction<?, ? super IOException>> bs = U.newArrayList(batch);
		batch = null;
		for (Runnable r : bs) {
			r.run();
		}
		if (out != null) {
			out.onSuccess(null);
		}
	}
	/**
	 * Executes an action or adds it to the batch list.
	 * @param action the action to queue or execute
	 */
	protected void execute(DeferredAction<?, ? super IOException> action) {
		if (isBatch()) {
			batch.add(action);
		} else {
			action.run();
		}
	}
	@Override
	public void ping(final AsyncResult<? super Long, ? super IOException> out) {
		execute(new DeferredAction<Long, IOException>(out) {
			@Override
			public Long invoke() throws IOException {
				return api.ping();
			}
		});
	}
	@Override
	public void login(final String user, final String passphrase, final String version,
			final AsyncResult<? super WelcomeResponse, ? super IOException> out) {
		execute(new DeferredAction<WelcomeResponse, IOException>(out) {
			@Override
			public WelcomeResponse invoke() throws IOException {
				return api.login(user, passphrase, version);
			}
		});
	}

	@Override
	public void relogin(final String sessionId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.relogin(sessionId);
				return null;
			}
		});
	}

	@Override
	public void leave(AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.leave();
				return null;
			}
		});
	}

	@Override
	public void getGameDefinition(
			AsyncResult<? super MultiplayerDefinition, ? super IOException> out) {
		execute(new DeferredAction<MultiplayerDefinition, IOException>(out) {
			@Override
			public MultiplayerDefinition invoke() throws IOException {
				return api.getGameDefinition();
			}
		});
	}

	@Override
	public void choosePlayerSettings(final MultiplayerUser user,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.choosePlayerSettings(user);
				return null;
			}
		});
	}

	@Override
	public void join(
			AsyncResult<? super MultiplayerGameSetup, ? super IOException> out) {
		execute(new DeferredAction<MultiplayerGameSetup, IOException>(out) {
			@Override
			public MultiplayerGameSetup invoke() throws IOException {
				return api.join();
			}
		});
		
	}

	@Override
	public void ready(AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.ready();
				return null;
			}
		});
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
			AsyncResult<? super ProductionStatus, ? super IOException> out) {
		execute(new DeferredAction<ProductionStatus, IOException>(out) {
			@Override
			public ProductionStatus invoke() throws IOException {
				return api.getProductions();
			}
		});
		
	}

	@Override
	public void getResearches(
			AsyncResult<? super ResearchStatus, ? super IOException> out) {
		execute(new DeferredAction<ResearchStatus, IOException>(out) {
			@Override
			public ResearchStatus invoke() throws IOException {
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
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.moveFleet(id, x, y);
				return null;
			}
		});
	}

	@Override
	public void addFleetWaypoint(final int id, final double x, final double y,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.addFleetWaypoint(id, x, y);
				return null;
			}
		});
	}

	@Override
	public void moveToPlanet(final int id, final String target,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.moveToPlanet(id, target);
				return null;
			}
		});
		
	}

	@Override
	public void followFleet(final int id, final int target,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.followFleet(id, target);
				return null;
			}
		});
	}

	@Override
	public void attackFleet(final int id, final int target,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.attackFleet(id, target);
				return null;
			}
		});
		
	}

	@Override
	public void attackPlanet(final int id, final String target,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.attackPlanet(id, target);
				return null;
			}
		});
	}

	@Override
	public void colonize(final int id, final String target,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.colonize(id, target);
				return null;
			}
		});
	}

	@Override
	public void newFleet(final String planet,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		execute(new DeferredAction<FleetStatus, IOException>(out) {
			@Override
			public FleetStatus invoke() throws IOException {
				return api.newFleet(planet);
			}
		});
		
	}

	@Override
	public void newFleet(final String planet, final List<InventoryItem> inventory,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		execute(new DeferredAction<FleetStatus, IOException>(out) {
			@Override
			public FleetStatus invoke() throws IOException {
				return api.newFleet(planet, inventory);
			}
		});
		
	}

	@Override
	public void newFleet(final int id,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		execute(new DeferredAction<FleetStatus, IOException>(out) {
			@Override
			public FleetStatus invoke() throws IOException {
				return api.newFleet(id);
			}
		});
		
	}

	@Override
	public void newFleet(final int id, final List<InventoryItem> inventory,
			AsyncResult<? super FleetStatus, ? super IOException> out) {
		execute(new DeferredAction<FleetStatus, IOException>(out) {
			@Override
			public FleetStatus invoke() throws IOException {
				return api.newFleet(id, inventory);
			}
		});
		
	}

	@Override
	public void deleteFleet(final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.deleteFleet(id);
				return null;
			}
		});
	}

	@Override
	public void renameFleet(final int id, final String name,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.renameFleet(id, name);
				return null;
			}
		});
	}

	@Override
	public void sellFleetItem(final int id, final int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		execute(new DeferredAction<InventoryItemStatus, IOException>(out) {
			@Override
			public InventoryItemStatus invoke() throws IOException {
				return api.sellFleetItem(id, itemId);
			}
		});
		
	}

	@Override
	public void deployFleetItem(final int id, final String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		execute(new DeferredAction<InventoryItemStatus, IOException>(out) {
			@Override
			public InventoryItemStatus invoke() throws IOException {
				return api.deployFleetItem(id, type);
			}
		});
		
	}

	@Override
	public void undeployFleetItem(final int id, final int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		execute(new DeferredAction<InventoryItemStatus, IOException>(out) {
			@Override
			public InventoryItemStatus invoke() throws IOException {
				return api.undeployFleetItem(id, itemId);
			}
		});
		
	}

	@Override
	public void addFleetEquipment(final int id, final int itemId, final String slotId,
			final String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		execute(new DeferredAction<InventoryItemStatus, IOException>(out) {
			@Override
			public InventoryItemStatus invoke() throws IOException {
				return api.addFleetEquipment(id, itemId, slotId, type);
			}
		});
		
	}

	@Override
	public void removeFleetEquipment(final int id, final int itemId, final String slotId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		execute(new DeferredAction<InventoryItemStatus, IOException>(out) {
			@Override
			public InventoryItemStatus invoke() throws IOException {
				return api.removeFleetEquipment(id, itemId, slotId);
			}
		});
		
	}

	@Override
	public void fleetUpgrade(final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.fleetUpgrade(id);
				return null;
			}
		});
	}

	@Override
	public void stopFleet(final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.stopFleet(id);
				return null;
			}
		});
	}

	@Override
	public void transfer(final int sourceFleet, final int destinationFleet, 
			final int sourceItem,
			final FleetTransferMode mode,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.transfer(sourceFleet, destinationFleet, sourceItem, mode);
				return null;
			}
		});
	}

	@Override
	public void colonize(final String id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.colonize(id);
				return null;
			}
		});
	}

	@Override
	public void cancelColonize(final String id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.cancelColonize(id);
				return null;
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
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.enable(planetId, id);
				return null;
			}
		});
	}

	@Override
	public void disable(final String planetId, final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.disable(planetId, id);
				return null;
			}
		});
	}

	@Override
	public void repair(final String planetId, final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.repair(planetId, id);
				return null;
			}
		});
	}

	@Override
	public void repairOff(final String planetId, final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.repairOff(planetId, id);
				return null;
			}
		});
		
	}

	@Override
	public void demolish(final String planetId, final int id,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.demolish(planetId, id);
				return null;
			}
		});
	}

	@Override
	public void buildingUpgrade(final String planetId, final int id, 
			final int level,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.buildingUpgrade(planetId, id, level);
				return null;
			}
		});
		
	}

	@Override
	public void deployPlanetItem(final String planetId, final String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		execute(new DeferredAction<InventoryItemStatus, IOException>(out) {
			@Override
			public InventoryItemStatus invoke() throws IOException {
				return api.deployPlanetItem(planetId, type);
			}
		});
		
	}

	@Override
	public void undeployPlanetItem(final String planetId, final int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		execute(new DeferredAction<InventoryItemStatus, IOException>(out) {
			@Override
			public InventoryItemStatus invoke() throws IOException {
				return api.undeployPlanetItem(planetId, itemId);
			}
		});
		
	}

	@Override
	public void sellPlanetItem(final String planetId, final int itemId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		execute(new DeferredAction<InventoryItemStatus, IOException>(out) {
			@Override
			public InventoryItemStatus invoke() throws IOException {
				return api.sellPlanetItem(planetId, itemId);
			}
		});
		
	}

	@Override
	public void addPlanetEquipment(final String planetId, final int itemId, 
			final String slotId, final String type,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		execute(new DeferredAction<InventoryItemStatus, IOException>(out) {
			@Override
			public InventoryItemStatus invoke() throws IOException {
				return api.addPlanetEquipment(planetId, itemId, slotId, type);
			}
		});
		
	}

	@Override
	public void removePlanetEquipment(final String planetId, final int itemId,
			final String slotId,
			AsyncResult<? super InventoryItemStatus, ? super IOException> out) {
		execute(new DeferredAction<InventoryItemStatus, IOException>(out) {
			@Override
			public InventoryItemStatus invoke() throws IOException {
				return api.removePlanetEquipment(planetId, itemId, slotId);
			}
		});
		
	}

	@Override
	public void planetUpgrade(final String planetId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.planetUpgrade(planetId);
				return null;
			}
		});
	}

	@Override
	public void startProduction(final String type,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.startProduction(type);
				return null;
			}
		});
	}

	@Override
	public void stopProduction(final String type,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.stopProduction(type);
				return null;
			}
		});
	}

	@Override
	public void setProductionQuantity(final String type, final int count,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.setProductionQuantity(type, count);
				return null;
			}
		});
	}

	@Override
	public void setProductionPriority(final String type, final int priority,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.setProductionPriority(type, priority);
				return null;
			}
		});
	}

	@Override
	public void sellInventory(final String type, final int count,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.sellInventory(type, count);
				return null;
			}
		});
		
	}

	@Override
	public void startResearch(final String type,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.startResearch(type);
				return null;
			}
		});
	}

	@Override
	public void stopResearch(final String type,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.stopResearch(type);
				return null;
			}
		});
		
	}

	@Override
	public void setResearchMoney(final String type, final int money,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.setResearchMoney(type, money);
				return null;
			}
		});
	}

	@Override
	public void pauseResearch(AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.pauseResearch();
				return null;
			}
		});
	}

	@Override
	public void pauseProduction(
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.pauseProduction();
				return null;
			}
		});
	}

	@Override
	public void unpauseProduction(
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.unpauseProduction();
				return null;
			}
		});
	}

	@Override
	public void unpauseResearch(
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.unpauseResearch();
				return null;
			}
		});
	}

	@Override
	public void stopSpaceUnit(final int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.stopSpaceUnit(unitId);
				return null;
			}
		});
		
	}

	@Override
	public void moveSpaceUnit(final int unitId, final double x, final double y,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.moveSpaceUnit(unitId, x, y);
				return null;
			}
		});
		
	}

	@Override
	public void attackSpaceUnit(final int unitId, final int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.attackSpaceUnit(unitId, targetUnitId);
				return null;
			}
		});
		
	}

	@Override
	public void kamikazeSpaceUnit(final int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.kamikazeSpaceUnit(unitId);
				return null;
			}
		});
	}

	@Override
	public void fireSpaceRocket(final int unitId, final int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.fireSpaceRocket(unitId, targetUnitId);
				return null;
			}
		});
		
	}

	@Override
	public void spaceRetreat(final int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.spaceRetreat(battleId);
				return null;
			}
		});
		
	}

	@Override
	public void stopSpaceRetreat(final int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.stopSpaceRetreat(battleId);
				return null;
			}
		});
	}

	@Override
	public void fleetFormation(final int fleetId, final int formation,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.fleetFormation(fleetId, formation);
				return null;
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
	public void stopGroundUnit(final int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.stopGroundUnit(unitId);
				return null;
			}
		});
		
	}

	@Override
	public void moveGroundUnit(final int unitId, final int x, final int y,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.moveGroundUnit(unitId, x, y);
				return null;
			}
		});
		
	}

	@Override
	public void attackGroundUnit(final int unitId, final int targetUnitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.attackGroundUnit(unitId, targetUnitId);
				return null;
			}
		});
		
	}

	@Override
	public void attackBuilding(final int unitId, final int buildingId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.attackBuilding(unitId, buildingId);
				return null;
			}
		});
		
	}

	@Override
	public void deployMine(final int unitId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.deployMine(unitId);
				return null;
			}
		});
		
	}

	@Override
	public void groundRetreat(final int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.groundRetreat(battleId);
				return null;
			}
		});
		
	}

	@Override
	public void stopGroundRetreat(final int battleId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredAction<Void, IOException>(out) {
			@Override
			public Void invoke() throws IOException {
				api.stopGroundRetreat(battleId);
				return null;
			}
		});
		
	}

	@Override
	public void getGroundBattleUnits(final int battleId,
			AsyncResult<? super List<GroundBattleUnit>, ? super IOException> out) {
		execute(new DeferredAction<List<GroundBattleUnit>, IOException>(out) {
			@Override
			public List<GroundBattleUnit> invoke() throws IOException {
				return api.getGroundBattleUnits(battleId);
			}
		});
		
	}

}
