/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.AsyncResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The asynchronous version of the RemoteGameAPI.
 * @author akarnokd, 2013.04.27.
 */
public interface GameAsyncAPI {
	/**
	 * Start a batch request.
	 */
	void begin();
	/**
	 * Finish a batch request and send out the composite requests.
	 * @throws IOException in case the send and receive fails
	 */
	void end() throws IOException;
	/**
	 * Finish a batch request and send out the composite requests.
	 * @param out the async completion handler when all responses have
	 * been received and processed.
	 */
	void end(AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Cancel the building of batch requests.
	 */
	void cancel();
	/**
	 * Returns the empire status information.
	 * @param out the async result or the error
	 */
	void getEmpireStatuses(AsyncResult<? super EmpireStatuses, ? super IOException> out);
	/**
	 * Returns the own and known fleets list.
	 * @param out the async result or the error
	 */
	void getFleets(AsyncResult<? super List<FleetStatus>, ? super IOException> out);
	/**
	 * Get information about a concrete fleet.
	 * @param fleetId the fleet identifier
	 * @param out the async result or the error
	 */
	void getFleet(int fleetId, AsyncResult<? super FleetStatus, ? super IOException> out);
	/**
	 * Retrieve current inventory status.
	 * @param out the async result or the error
	 */
	void getInventory(AsyncResult<? super Map<String, Integer>, ? super IOException> out);
	/**
	 * Returns a list of active productions.
	 * @param out the async result or the error
	 */
	void getProductions(AsyncResult<? super ProductionStatuses, ? super IOException> out);
	/**
	 * Returns the current research status, including
	 * available and in-progress technology info.
	 * @param out the async result or the error
	 */
	void getResearches(AsyncResult<? super ResearchStatuses, ? super IOException> out);
	/**
	 * Returns a list of planet statuses for all visible
	 * planets.
	 * @param out the async result or the error
	 */
	void getPlanetStatuses(AsyncResult<? super List<PlanetStatus>, ? super IOException> out);
	/**
	 * Retrieve a concrete planet's status.
	 * @param id the planet identifier
	 * @param out the async result or the error
	 */
	void getPlanetStatus(String id, AsyncResult<? super PlanetStatus, ? super IOException> out);
	/**
	 * Move a fleet to the specified coordinates.
	 * @param id the fleet id
	 * @param x the target coordinate
	 * @param y the target coordinate
	 * @param out the async result or the error
	 */
	void moveFleet(int id, double x, double y, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Add a waypoint to the given fleet's movement order.
	 * @param id the fleet id
	 * @param x the new waypoint coordinate
	 * @param y the new waypoint coordinate
	 * @param out the async result or the error
	 */
	void addFleetWaypoint(int id, double x, double y, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Instruct the fleet to move to a specified planet.
	 * @param id the own fleet id
	 * @param target the target planet id
	 * @param out the async result or the error
	 */
	void moveToPlanet(int id, String target, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Instruct the fleet to follow another fleet.
	 * @param id the own fleet id
	 * @param target the target fleet id
	 * @param out the async result or the error
	 */
	void followFleet(int id, int target, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Issue an attack order against the other fleet.
	 * @param id the own fleet id
	 * @param target the target fleet id
	 * @param out the async result or the error
	 */
	void attackFleet(int id, int target, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Attack a planet with the fleet.
	 * @param id the own fleet id
	 * @param target the target planet id
	 * @param out the async result or the error
	 */
	void attackPlanet(int id, String target, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Instruct a fleet to colonize a planet by one of its
	 * colony ships.
	 * @param id the fleet id
	 * @param target the target planet
	 * @param out the async result or the error
	 */
	void colonize(int id, String target, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Create a new fleet around the given planet and
	 * deploy ships and equipment according to the specification
	 * given by the list of inventory item.
	 * @param planet the target planet
	 * @param inventory the target inventory
	 * @param out the async result or the error
	 */
	void newFleet(String planet, List<InventoryItemStatus> inventory, AsyncResult<? super Integer, ? super IOException> out);
	/**
	 * Create a new fleet next to the given other fleet
	 * and transfer units from it according to the inventory item 
	 * specification (ignores slots).
	 * @param id the target fleet
	 * @param inventory the target inventory
	 * @param out the async result or the error
	 */
	void newFleet(int id, List<InventoryItemStatus> inventory, AsyncResult<? super Integer, ? super IOException> out);
	/**
	 * Delete an empty fleet.
	 * @param id the target fleet
	 * @param out the async result or the error
	 */
	void deleteFleet(int id, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Rename the fleet.
	 * @param id the fleet id
	 * @param name the new name
	 * @param out the async result or the error
	 */
	void renameFleet(int id, String name, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Sells one unit from the given fleet inventory item.
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @param out the async result or the error
	 */
	void sellFleetItem(int id, int itemId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Deploy one unit of the given type into the target fleet.
	 * @param id the fleet id
	 * @param type the unit type
	 * @param out the async result or the error
	 */
	void deployFleetItem(int id, String type, AsyncResult<? super Integer, ? super IOException> out);
	/**
	 * Undeploy a single fleet item (such as fighters and vehicles).
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @param out the async result or the error
	 */
	void undeployFleetItem(int id, int itemId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Adds one unit of equipment into the given fleet's
	 * given inventory item's slot.
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @param slotId the slot id
	 * @param type the technology to add
	 * @param out the async result or the error
	 */
	void addFleetEquipment(int id, int itemId, String slotId, String type, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Remove one unit of equipment from the given fleet's
	 * given inventory items' slot.
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @param slotId the slot id
	 * @param out the async result or the error
	 */
	void removeFleetEquipment(int id, int itemId, String slotId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Automatically upgrade and fill in fleet items.
	 * @param id the fleet id
	 * @param out the async result or the error
	 */
	void fleetUpgrade(int id, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Stop the given fleet.
	 * @param id the fleet id
	 * @param out the async result or the error
	 */
	void stopFleet(int id, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Transfer units between two fleets.
	 * @param sourceFleet the source fleet
	 * @param destinationFleet the destination fleet
	 * @param sourceItem the source inventory item id
	 * @param mode the transfer type the transfer mode
	 * @param out the async result or the error
	 */
	void transfer(int sourceFleet, int destinationFleet, int sourceItem, FleetTransferMode mode, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Mark the planet for colonization.
	 * @param id the planet id
	 * @param out the async result or the error
	 */
	void colonize(String id, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Cancel the colonization of the given planet.
	 * @param id the planet id
	 * @param out the async result or the error
	 */
	void cancelColonize(String id, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Place a building on the given planet at the specified location.
	 * @param planetId the planet identifier
	 * @param type the building type
	 * @param race the building race
	 * @param x the location
	 * @param y the location
	 * @param out the async result or the error
	 */
	void build(String planetId, String type, String race, int x, int y, AsyncResult<? super Integer, ? super IOException> out);
	/**
	 * Place a building on the given planet at a suitable location
	 * (using the same placement logic as the AI would).
	 * @param planetId the planet identifier
	 * @param type the building type
	 * @param race the building race
	 * @param out the async result or the error
	 */
	void build(String planetId, String type, String race, AsyncResult<? super Integer, ? super IOException> out);
	/**
	 * Enable a specific building on the planet.
	 * @param planetId the planet id
	 * @param id the building id
	 * @param out the async result or the error
	 */
	void enable(String planetId, int id, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Disable a specific building on the planet.
	 * @param planetId the planet id
	 * @param id the building id
	 * @param out the async result or the error
	 */
	void disable(String planetId, int id, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Activate the repair function on the target building.
	 * @param planetId the planet id
	 * @param id the building id
	 * @param out the async result or the error
	 */
	void repair(String planetId, int id, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Deactivate the repair function on the target building.
	 * @param planetId the planet id
	 * @param id the building id
	 * @param out the async result or the error
	 */
	void repairOff(String planetId, int id, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Demolish the given building on the planet.
	 * @param planetId the planet id
	 * @param id the building id
	 * @param out the async result or the error
	 */
	void demolish(String planetId, int id, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Upgrade the given building to the specified level.
	 * @param planetId the planet id
	 * @param id the building id
	 * @param level the new level
	 * @param out the async result or the error
	 */
	void buildingUpgrade(String planetId, int id, int level, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Deploy an unit with the given type into the planet's inventory.
	 * The deployed item is owned by the current player.
	 * You may deploy spy satellites onto enemy planets, otherwise, 
	 * the deployment is rejected with ane error
	 * @param planetId the target planet id
	 * @param type the technology id
	 * @param out the async result or the error
	 */
	void deployPlanetItem(String planetId, String type, AsyncResult<? super Integer, ? super IOException> out);
	/**
	 * Undeploy an unit from the planet's inventory, must be the
	 * current player's planet.
	 * @param planetId the planet id
	 * @param itemId the inventory item id
	 * @param out the async result or the error
	 */
	void undeployPlanetItem(String planetId, int itemId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Sell an unit from the planet's inventory.
	 * @param planetId the planet id
	 * @param itemId the inventory item id
	 * @param out the async result or the error
	 */
	void sellPlanetItem(String planetId, int itemId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Add one unit of the given equipment type into the given slot
	 * of the given inventory item of the given planet.
	 * @param planetId the planet
	 * @param itemId the inventory item id
	 * @param slotId the slot id
	 * @param type the technology id
	 * @param out the async result or the error
	 */
	void addPlanetEquipment(String planetId, int itemId, String slotId, String type, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Remove one unit of the technology at the given slot, inventory item and planet.
	 * @param planetId the planet identifier
	 * @param itemId the inventory item id
	 * @param slotId the slot id
	 * @param out the async result or the error
	 */
	void removePlanetEquipment(String planetId, int itemId, String slotId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Upgrade the planet inventory to the best technology and
	 * available item counts.
	 * @param planetId the target planet id
	 * @param out the async result or the error
	 */
	void planetUpgrade(String planetId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Add the given technology to the production list with zero
	 * quantity and default priority, if the production lines aren't full.
	 * @param type the technology id
	 * @param out the async result or the error
	 */
	void startProduction(String type, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Remove the production line with the given technology.
	 * @param type the technology id
	 * @param out the async result or the error
	 */
	void stopProduction(String type, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Set the production quantity on a production line.
	 * @param type the technology id
	 * @param count the new quantity
	 * @param out the async result or the error
	 */
	void setProductionQuantity(String type, int count, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Set the production priority on the given. 
	 * @param type the technology id
	 * @param priority the new priority
	 * @param out the async result or the error
	 */
	void setProductionPriority(String type, int priority, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Sell the given amount of units from the player's inventory.
	 * @param type the technology id
	 * @param count the number of items to sell
	 * @param out the async result or the error
	 */
	void sellInventory(String type, int count, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Start researching the given technology.
	 * @param type the technology id
	 * @param out the async result or the error
	 */
	void startResearch(String type, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Stop researching the given technology.
	 * @param type the technology id
	 * @param out the async result or the error
	 */
	void stopResearch(String type, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Set the money allocated to the running research.
	 * @param type the technology id
	 * @param money the allocated money
	 * @param out the async result or the error
	 */
	void setResearchMoney(String type, int money, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Globally pause the research without removing the current
	 * research.
	 * @param out the async result or the error
	 */
	void pauseResearch(AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Globally pause the production without removing any production lines. 
	 * @param out the async result or the error
	 */
	void pauseProduction(AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Globally unpause the production.
	 * @param out the async result or the error
	 */
	void unpauseProduction(AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Globally unpause the research.
	 * @param out the async result or the error
	 */
	void unpauseResearch(AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Stop a space unit.
	 * @param battleId the battle identifier
	 * @param unitId the unit id
	 * @param out the async result or the error
	 */
	void stopSpaceUnit(int battleId, int unitId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Move a space unit to the designated coordinates.
	 * @param battleId the battle identifier
	 * @param unitId the unit id
	 * @param x the coordinate
	 * @param y the coordinate
	 * @param out the async result or the error
	 */
	void moveSpaceUnit(int battleId, int unitId, double x, double y, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Attack another space unit.
	 * @param battleId the battle identifier
	 * @param unitId the unit id
	 * @param targetUnitId the target unit id
	 * @param out the async result or the error
	 */
	void attackSpaceUnit(int battleId, int unitId, int targetUnitId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Set the unit into kamikaze mode.
	 * @param battleId the battle identifier
	 * @param unitId the unit id
	 * @param out the async result or the error
	 */
	void kamikazeSpaceUnit(int battleId, int unitId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Instruct the unit to fire rockets and/or bombs at
	 * the target unit.
	 * @param battleId the battle identifier
	 * @param unitId the unit id
	 * @param targetUnitId the target unit id
	 * @param out the async result or the error
	 */
	void fireSpaceRocket(int battleId, int unitId, int targetUnitId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Perform retreat in the given space battle.
	 * @param battleId the battle id
	 * @param out the async result or the error
	 */
	void spaceRetreat(int battleId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Stop retreating in the given space battle.
	 * @param battleId the battle id
	 * @param out the async result or the error
	 */
	void stopSpaceRetreat(int battleId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Set the default fleet formation on the given fleet id.
	 * @param fleetId the fleet id
	 * @param formation the fleet formation index 
	 * @param out the async result or the error
	 */
	void fleetFormation(int fleetId, int formation, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Returns a list of all ongoing space battles which affects the player.
	 * @param out the async result or the error
	 */
	void getBattles(AsyncResult<? super List<BattleStatus>, ? super IOException> out);
	/**
	 * Retrieve a concrete space battle status.
	 * @param battleId the battle identifier
	 * @param out the async result or the error
	 */
	void getBattle(int battleId, AsyncResult<? super BattleStatus, ? super IOException> out);
	/**
	 * Returns a list of space units for the given battle.
	 * @param battleId the battle id
	 * @param out the async result or the error
	 */
	void getSpaceBattleUnits(int battleId, AsyncResult<? super List<SpaceBattleUnit>, ? super IOException> out);
	/**
	 * Stop a ground unit.
	 * @param battleId the battle identifier
	 * @param unitId the unit id
	 * @param out the async result or the error
	 */
	void stopGroundUnit(int battleId, int unitId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Move a ground unit into the given cell.
	 * @param battleId the battle identifier
	 * @param unitId the unit id
	 * @param x the coordinate
	 * @param y the coordinate
	 * @param out the async result or the error
	 */
	void moveGroundUnit(int battleId, int unitId, int x, int y, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Attack another ground unit.
	 * @param battleId the battle identifier
	 * @param unitId the unit id
	 * @param targetUnitId the target unit id
	 * @param out the async result or the error
	 */
	void attackGroundUnit(int battleId, int unitId, int targetUnitId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Attack a building with a ground unit.
	 * @param battleId the battle identifier
	 * @param unitId the unit id
	 * @param buildingId the building id
	 * @param out the async result or the error
	 */
	void attackBuilding(int battleId, int unitId, int buildingId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Deploy a mine with the given mine layer.
	 * @param battleId the battle identifier
	 * @param unitId the unit id
	 * @param out the async result or the error
	 */
	void deployMine(int battleId, int unitId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Retreat from ground battle.
	 * @param battleId the battle id
	 * @param out the async result or the error
	 */
	void groundRetreat(int battleId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Stop retreating from ground battle.
	 * @param battleId the battle id
	 * @param out the async result or the error
	 */
	void stopGroundRetreat(int battleId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Returns a list of ground battle units (guns and vehicles) of
	 * the given battle.
	 * @param battleId the battle id
	 * @param out the async result or the error
	 */
	void getGroundBattleUnits(int battleId, AsyncResult<? super List<GroundwarUnitStatus>, ? super IOException> out);
	/**
	 * Returns the inventory item status of the given fleet and inventory.
	 * @param fleetId the fleet id
	 * @param itemId the inventory id, if -1, the id of the last
	 * deployFleetItem() call.
	 * @param out the async result or the error
	 */
	void getInventoryStatus(int fleetId, int itemId, AsyncResult<? super InventoryItemStatus, ? super IOException> out);
	/**
	 * Returns the inventory item status of the given planet and inventory.
	 * @param planetId the planet identifier
	 * @param itemId the inventory id, if -1, the id of the last
	 * deployFleetItem() call.
	 * @param out the async result or the error
	 */
	void getInventoryStatus(String planetId, int itemId, AsyncResult<? super InventoryItemStatus, ? super IOException> out);
	/**
	 * Set the tax level on the given planet.
	 * @param planetId the planet id, if null, all planets of the player.
	 * @param tax the tax level
	 * @param out the async result or the error
	 */
	void setTaxLevel(String planetId, TaxLevel tax, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Set the auto-build on the given planet.
	 * @param planetId the target planet id, if null, all planets of the player
	 * @param auto the state
	 * @param out the async result or the error
	 */
	void setAutoBuild(String planetId, AutoBuild auto, AsyncResult<? super Void, ? super IOException> out);
}
