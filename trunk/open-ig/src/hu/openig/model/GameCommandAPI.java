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
 * Interface for game commanding methods.
 * @author akarnokd, 2013.05.05.
 */
public interface GameCommandAPI {
	/**
	 * Move a fleet to the specified coordinates.
	 * @param id the fleet id
	 * @param x the target coordinate
	 * @param y the target coordinate
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void moveFleet(int id, double x, double y) throws IOException;
	/**
	 * Add a waypoint to the given fleet's movement order.
	 * @param id the fleet id
	 * @param x the new waypoint coordinate
	 * @param y the new waypoint coordinate
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void addFleetWaypoint(int id, double x, double y) throws IOException;
	/**
	 * Instruct the fleet to move to a specified planet.
	 * @param id the own fleet id
	 * @param target the target planet id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void moveToPlanet(int id, String target) throws IOException;
	/**
	 * Instruct the fleet to follow another fleet.
	 * @param id the own fleet id
	 * @param target the target fleet id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void followFleet(int id, int target) throws IOException;
	/**
	 * Issue an attack order against the other fleet.
	 * @param id the own fleet id
	 * @param target the target fleet id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void attackFleet(int id, int target) throws IOException;
	/**
	 * Attack a planet with the fleet.
	 * @param id the own fleet id
	 * @param target the target planet id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void attackPlanet(int id, String target) throws IOException;
	/**
	 * Instruct a fleet to colonize a planet by one of its
	 * colony ships.
	 * @param id the fleet id
	 * @param target the target planet
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void colonize(int id, String target) throws IOException;
	/**
	 * Create a new fleet around the given planet and
	 * deploy ships and equipment according to the specification
	 * given by the list of inventory items.
	 * @param planet the target planet
	 * @param inventory the target inventory
	 * @return the created fleet id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	int newFleet(String planet, List<InventoryItemStatus> inventory) throws IOException;
	/**
	 * Create a new fleet next to the given other fleet
	 * and transfer units from it according to the inventory item 
	 * specification (ignores slots).
	 * @param id the target fleet
	 * @param inventory the target inventory
	 * @return the created fleet id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	int newFleet(int id, List<InventoryItemStatus> inventory) throws IOException;
	/**
	 * Delete an empty fleet.
	 * @param id the target fleet
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void deleteFleet(int id) throws IOException;
	/**
	 * Rename the fleet.
	 * @param id the fleet id
	 * @param name the new name
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void renameFleet(int id, String name) throws IOException;
	/**
	 * Sells one unit from the given fleet inventory item.
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void sellFleetItem(int id, int itemId) throws IOException;
	/**
	 * Deploy one unit of the given type into the target fleet.
	 * @param id the fleet id
	 * @param type the unit type
	 * @return the inventory item id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	int deployFleetItem(int id, String type) throws IOException;
	/**
	 * Undeploy a single fleet item (such as fighters and vehicles).
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void undeployFleetItem(int id, int itemId) throws IOException;
	/**
	 * Adds one unit of equipment into the given fleet's
	 * given inventory item's slot.
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @param slotId the slot id
	 * @param type the technology to add
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void addFleetEquipment(int id, int itemId, String slotId, String type) throws IOException;
	/**
	 * Remove one unit of equipment from the given fleet's
	 * given inventory items' slot.
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @param slotId the slot id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void removeFleetEquipment(int id, int itemId, String slotId) throws IOException;
	/**
	 * Automatically upgrade and fill in fleet items.
	 * @param id the fleet id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void fleetUpgrade(int id) throws IOException;
	/**
	 * Stop the given fleet.
	 * @param id the fleet id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void stopFleet(int id) throws IOException;
	/**
	 * Transfer units between two fleets.
	 * @param sourceFleet the source fleet
	 * @param destinationFleet the destination fleet
	 * @param sourceItem the source inventory item id
	 * @param mode the transfer type the transfer mode
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void transfer(int sourceFleet, int destinationFleet, int sourceItem, FleetTransferMode mode) throws IOException;
	/**
	 * Mark the planet for colonization.
	 * @param id the planet id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void colonize(String id) throws IOException;
	/**
	 * Cancel the colonization of the given planet.
	 * @param id the planet id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void cancelColonize(String id) throws IOException;
	/**
	 * Place a building on the given planet at the specified location.
	 * @param planetId the planet identifier
	 * @param type the building type
	 * @param race the building race
	 * @param x the location
	 * @param y the location
	 * @return the created building id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	int build(String planetId, String type, String race, int x, int y) throws IOException;
	/**
	 * Place a building on the given planet at a suitable location
	 * (using the same placement logic as the AI would).
	 * @param planetId the planet identifier
	 * @param type the building type
	 * @param race the building race
	 * @return the created building id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	int build(String planetId, String type, String race) throws IOException;
	/**
	 * Enable a specific building on the planet.
	 * @param planetId the planet id
	 * @param id the building id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void enable(String planetId, int id) throws IOException;
	/**
	 * Disable a specific building on the planet.
	 * @param planetId the planet id
	 * @param id the building id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void disable(String planetId, int id) throws IOException;
	/**
	 * Activate the repair function on the target building.
	 * @param planetId the planet id
	 * @param id the building id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void repair(String planetId, int id) throws IOException;
	/**
	 * Deactivate the repair function on the target building.
	 * @param planetId the planet id
	 * @param id the building id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void repairOff(String planetId, int id) throws IOException;
	/**
	 * Demolish the given building on the planet.
	 * @param planetId the planet id
	 * @param id the building id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void demolish(String planetId, int id) throws IOException;
	/**
	 * Upgrade the given building to the specified level.
	 * @param planetId the planet id
	 * @param id the building id
	 * @param level the new level
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void buildingUpgrade(String planetId, int id, int level) throws IOException;
	/**
	 * Deploy an unit with the given type into the planet's inventory.
	 * The deployed item is owned by the current player.
	 * You may deploy spy satellites onto enemy planets, otherwise, 
	 * the deployment is rejected with ane error
	 * @param planetId the target planet id
	 * @param type the technology id
	 * @return the inventory status after the action
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	int deployPlanetItem(String planetId, String type) throws IOException;
	/**
	 * Undeploy an unit from the planet's inventory, must be the
	 * current player's planet.
	 * @param planetId the planet id
	 * @param itemId the inventory item id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void undeployPlanetItem(String planetId, int itemId) throws IOException;
	/**
	 * Sell an unit from the planet's inventory.
	 * @param planetId the planet id
	 * @param itemId the inventory item id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void sellPlanetItem(String planetId, int itemId) throws IOException;
	/**
	 * Add one unit of the given equipment type into the given slot
	 * of the given inventory item of the given planet.
	 * @param planetId the planet
	 * @param itemId the inventory item id
	 * @param slotId the slot id
	 * @param type the technology id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void addPlanetEquipment(String planetId, int itemId, String slotId, String type) throws IOException;
	/**
	 * Remove one unit of the technology at the given slot, inventory item and planet.
	 * @param planetId the planet identifier
	 * @param itemId the inventory item id
	 * @param slotId the slot id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void removePlanetEquipment(String planetId, int itemId, String slotId) throws IOException;
	/**
	 * Upgrade the planet inventory to the best technology and
	 * available item counts.
	 * @param planetId the target planet id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void planetUpgrade(String planetId) throws IOException;
	/**
	 * Add the given technology to the production list with zero
	 * quantity and default priority, if the production lines aren't full.
	 * @param type the technology id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void startProduction(String type) throws IOException;
	/**
	 * Remove the production line with the given technology.
	 * @param type the technology id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void stopProduction(String type) throws IOException;
	/**
	 * Set the production quantity on a production line.
	 * @param type the technology id
	 * @param count the new quantity
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void setProductionQuantity(String type, int count) throws IOException;
	/**
	 * Set the production priority on the given. 
	 * @param type the technology id
	 * @param priority the new priority
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void setProductionPriority(String type, int priority) throws IOException;
	/**
	 * Sell the given amount of units from the player's inventory.
	 * @param type the technology id
	 * @param count the number of items to sell
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void sellInventory(String type, int count) throws IOException;
	/**
	 * Start researching the given technology.
	 * @param type the technology id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void startResearch(String type) throws IOException;
	/**
	 * Stop researching the given technology.
	 * @param type the technology id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void stopResearch(String type) throws IOException;
	/**
	 * Set the money allocated to the running research.
	 * @param type the technology id
	 * @param money the allocated money
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void setResearchMoney(String type, int money) throws IOException;
	/**
	 * Globally pause the research without removing the current
	 * research.
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void pauseResearch() throws IOException;
	/**
	 * Globally pause the production without removing any production lines. 
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void pauseProduction() throws IOException;
	/**
	 * Globally unpause the production.
 	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void unpauseProduction() throws IOException;
	/**
	 * Globally unpause the research.
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void unpauseResearch() throws IOException;
	/**
	 * Stop a space unit.
	 * @param battleId the battle id
	 * @param unitId the unit id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void stopSpaceUnit(int battleId, int unitId) throws IOException;
	/**
	 * Move a space unit to the designated coordinates.
	 * @param battleId the battle id
	 * @param unitId the unit id
	 * @param x the coordinate
	 * @param y the coordinate
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void moveSpaceUnit(int battleId, int unitId, double x, double y) throws IOException;
	/**
	 * Attack another space unit.
	 * @param battleId the battle id
	 * @param unitId the unit id
	 * @param targetUnitId the target unit id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void attackSpaceUnit(int battleId, int unitId, int targetUnitId) throws IOException;
	/**
	 * Set the unit into kamikaze mode.
	 * @param battleId the battle id
	 * @param unitId the unit id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void kamikazeSpaceUnit(int battleId, int unitId) throws IOException;
	/**
	 * Instruct the unit to fire rockets and/or bombs at
	 * the target unit.
	 * @param battleId the battle id
	 * @param unitId the unit id
	 * @param targetUnitId the target unit id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void fireSpaceRocket(int battleId, int unitId, int targetUnitId) throws IOException;
	/**
	 * Perform retreat in the given space battle.
	 * @param battleId the battle id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void spaceRetreat(int battleId) throws IOException;
	/**
	 * Stop retreating in the given space battle.
	 * @param battleId the battle id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void stopSpaceRetreat(int battleId) throws IOException;
	/**
	 * Set the default fleet formation on the given fleet id.
	 * @param fleetId the fleet id
	 * @param formation the fleet formation index 
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void fleetFormation(int fleetId, int formation) throws IOException;
	/**
	 * Stop a ground unit.
	 * @param battleId the battle id
	 * @param unitId the unit id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void stopGroundUnit(int battleId, int unitId) throws IOException;
	/**
	 * Move a ground unit into the given cell.
	 * @param battleId the battle id
	 * @param unitId the unit id
	 * @param x the coordinate
	 * @param y the coordinate
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void moveGroundUnit(int battleId, int unitId, int x, int y) throws IOException;
	/**
	 * Attack another ground unit.
	 * @param battleId the battle id
	 * @param unitId the unit id
	 * @param targetUnitId the target unit id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void attackGroundUnit(int battleId, int unitId, int targetUnitId) throws IOException;
	/**
	 * Attack a building with a ground unit.
	 * @param battleId the battle id
	 * @param unitId the unit id
	 * @param buildingId the building id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void attackBuilding(int battleId, int unitId, int buildingId) throws IOException;
	/**
	 * Deploy a mine with the given mine layer.
	 * @param battleId the battle id
	 * @param unitId the unit id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void deployMine(int battleId, int unitId) throws IOException;
	/**
	 * Retreat from ground battle.
	 * @param battleId the battle id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void groundRetreat(int battleId) throws IOException;
	/**
	 * Stop retreating from ground battle.
	 * @param battleId the battle id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void stopGroundRetreat(int battleId) throws IOException;
	/**
	 * Set the tax level on the given planet.
	 * @param planetId the planet id, if null, all planets of the player.
	 * @param tax the tax level
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void setTaxLevel(String planetId, TaxLevel tax) throws IOException;
	/**
	 * Set the auto-build on the given planet.
	 * @param planetId the target planet id, if null, all planets of the player
	 * @param auto the state
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void setAutoBuild(String planetId, AutoBuild auto) throws IOException;
}
