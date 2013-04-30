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
import hu.openig.model.Production;
import hu.openig.multiplayer.model.EmpireStatuses;
import hu.openig.multiplayer.model.FleetStatus;
import hu.openig.multiplayer.model.FleetTransferMode;
import hu.openig.multiplayer.model.InventoryItemStatus;
import hu.openig.multiplayer.model.MultiplayerGameSetup;
import hu.openig.multiplayer.model.PlanetStatus;
import hu.openig.multiplayer.model.ResearchStatus;
import hu.openig.multiplayer.model.WelcomeResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The remote game API interface.
 * <p>Implementations should ensure that the methods are executed on the proper
 * thread, for example, in EDT.
 * @author akarnokd, 2013.04.22.
 */
public interface RemoteGameAPI {
	/** 
	 * Send a simple ping-pong request.
	 * @return the latency in milliseconds
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	long ping() throws IOException;
	/**
	 * Login.
	 * @param user the user object
	 * @param passphrase the passphrase
	 * @param version the caller's version
	 * @return the welcome message if successful.
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	WelcomeResponse login(String user, String passphrase, String version) throws IOException;
	/**
	 * Relogin into a running session.
	 * @param sessionId the session id
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void relogin(String sessionId) throws IOException;
	/**
	 * Indicate the intent to leave the game/connection.
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void leave() throws IOException;
	/**
	 * Retrieve the current game definition.
	 * @return the game definition
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	MultiplayerDefinition getGameDefinition() throws IOException;
	/**
	 * Ask the game host to use the given user settings for the game.
	 * @param user the user settings
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void choosePlayerSettings(MultiplayerUser user) throws IOException;
	/**
	 * Join the current match.
	 * @return the game settings to use
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	MultiplayerGameSetup join() throws IOException;
	/**
	 * Signal the server that the game has finished loading
	 * and initializing throws IOException; synchronizes multiple players.
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	void ready() throws IOException;
	/**
	 * Returns the empire status information.
	 * @return the empire statuses
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	EmpireStatuses getEmpireStatuses() throws IOException;
	/**
	 * Returns the own and known fleets list.
	 * @return the list of fleets.
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	List<FleetStatus> getFleets() throws IOException;
	/**
	 * Get information about a concrete fleet.
	 * @param fleetId the fleet identifier
	 * @return the fleet
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	FleetStatus getFleet(int fleetId) throws IOException;
	/**
	 * Retrieve current inventory status.
	 * @return the map from tech id to number.
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	Map<String, Integer> getInventory() throws IOException;
	/**
	 * Returns a list of active productions.
	 * @return the list of active productions
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	List<Production> getProductions() throws IOException;
	/**
	 * Returns the current research status, including
	 * available and in-progress technology info.
	 * @return the research status object
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	ResearchStatus getResearches() throws IOException;
	/**
	 * Returns a list of planet statuses for all visible
	 * planets.
	 * @return the list of planet statuses
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	List<PlanetStatus> getPlanetStatuses() throws IOException;
	/**
	 * Retrieve a concrete planet's status.
	 * @param id the planet identifier
	 * @return the planet status record
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	PlanetStatus getPlanetStatus(String id) throws IOException;
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
	 * Create a new fleet around the given planet.
	 * @param planet the planet
	 * @return the created fleet status object
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	FleetStatus newFleet(String planet) throws IOException;
	/**
	 * Create a new fleet around the given planet and
	 * deploy ships and equipment according to the specification
	 * given by the list of inventory item.
	 * @param planet the target planet
	 * @param inventory the target inventory
	 * @return the created fleet status
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	FleetStatus newFleet(String planet, List<InventoryItem> inventory) throws IOException;
	/**
	 * Create a new fleet next to the given fleet.
	 * @param id the fleet identifier
	 * @return the created fleet status object
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	FleetStatus newFleet(int id) throws IOException;
	/**
	 * Create a new fleet next to the given other fleet
	 * and transfer units from it according to the inventory item 
	 * specification (ignores slots).
	 * @param id the target fleet
	 * @param inventory the target inventory
	 * @return the created fleet status
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	FleetStatus newFleet(int id, List<InventoryItem> inventory) throws IOException;
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
	 * @return the inventory item status
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	InventoryItemStatus sellFleetItem(int id, int itemId) throws IOException;
	/**
	 * Deploy one unit of the given type into the target fleet.
	 * @param id the fleet id
	 * @param type the unit type
	 * @return the inventory item status
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	InventoryItemStatus deployFleetItem(int id, String type) throws IOException;
	/**
	 * Undeploy a single fleet item (such as fighters and vehicles).
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @return the inventory item status
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	InventoryItemStatus undeployFleetItem(int id, int itemId) throws IOException;
	/**
	 * Adds one unit of equipment into the given fleet's
	 * given inventory item's slot.
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @param slotId the slot id
	 * @param type the technology to add
	 * @return the inventory item status
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	InventoryItemStatus addFleetEquipment(int id, int itemId, String slotId, String type) throws IOException;
	/**
	 * Remove one unit of equipment from the given fleet's
	 * given inventory items' slot.
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @param slotId the slot id
	 * @return the inventory item status
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	InventoryItemStatus removeFleetEquipment(int id, int itemId, String slotId) throws IOException;
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
	Integer build(String planetId, String type, String race, int x, int y) throws IOException;
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
	Integer build(String planetId, String type, String race) throws IOException;
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
	InventoryItemStatus deployPlanetItem(String planetId, String type) throws IOException;
	/**
	 * Undeploy an unit from the planet's inventory, must be the
	 * current player's planet.
	 * @param planetId the planet id
	 * @param itemId the inventory item id
	 * @return the inventory status after the action
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	InventoryItemStatus undeployPlanetItem(String planetId, int itemId) throws IOException;
	/**
	 * Sell an unit from the planet's inventory.
	 * @param planetId the planet id
	 * @param itemId the inventory item id
	 * @return the inventory item status after the action
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	InventoryItemStatus sellPlanetItem(String planetId, int itemId) throws IOException;
	/**
	 * Add one unit of the given equipment type into the given slot
	 * of the given inventory item of the given planet.
	 * @param planetId the planet
	 * @param itemId the inventory item id
	 * @param slotId the slot id
	 * @param type the technology id
	 * @return the inventory item status after the action
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	InventoryItemStatus addPlanetEquipment(String planetId, int itemId, String slotId, String type) throws IOException;
	/**
	 * Remove one unit of the technology at the given slot, inventory item and planet.
	 * @param planetId the planet identifier
	 * @param itemId the inventory item id
	 * @param slotId the slot id
	 * @return the inventory item status after the action
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	InventoryItemStatus removePlanetEquipment(String planetId, int itemId, String slotId) throws IOException;
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
}
