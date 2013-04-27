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
	 */
	Result<Long, IOException> ping();
	/**
	 * Login.
	 * @param user the user object
	 * @param passphrase the passphrase
	 * @param version the caller's version
	 * @return the welcome message if successful.
	 */
	Result<WelcomeResponse, IOException> login(String user, String passphrase, String version);
	/**
	 * Relogin into a running session.
	 * @param sessionId the session id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> relogin(String sessionId);
	/**
	 * Indicate the intent to leave the game/connection.
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> leave();
	/**
	 * Retrieve the current game definition.
	 * @return the game definition
	 */
	Result<MultiplayerDefinition, IOException> getGameDefinition();
	/**
	 * Ask the game host to use the given user settings for the game.
	 * @param user the user settings
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> choosePlayerSettings(MultiplayerUser user);
	/**
	 * Join the current match.
	 * @return the game settings to use
	 */
	Result<MultiplayerGameSetup, IOException> join();
	/**
	 * Signal the server that the game has finished loading
	 * and initializing; synchronizes multiple players.
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> ready();
	/**
	 * Returns the empire status information.
	 * @return the empire statuses
	 */
	Result<EmpireStatuses, IOException> getEmpireStatuses();
	/**
	 * Returns the own and known fleets list.
	 * @return the list of fleets.
	 */
	Result<List<FleetStatus>, IOException> getFleets();
	/**
	 * Get information about a concrete fleet.
	 * @param fleetId the fleet identifier
	 * @return the fleet
	 */
	Result<FleetStatus, IOException> getFleet(int fleetId);
	/**
	 * Retrieve current inventory status.
	 * @return the map from tech id to number.
	 */
	Result<Map<String, Integer>, IOException> getInventory();
	/**
	 * Returns a list of active productions.
	 * @return the list of active productions
	 */
	Result<List<Production>, IOException> getProductions();
	/**
	 * Returns the current research status, including
	 * available and in-progress technology info.
	 * @return the research status object
	 */
	Result<ResearchStatus, IOException> getResearches();
	/**
	 * Returns a list of planet statuses for all visible
	 * planets.
	 * @return the list of planet statuses
	 */
	Result<List<PlanetStatus>, IOException> getPlanetStatuses();
	/**
	 * Retrieve a concrete planet's status.
	 * @param id the planet identifier
	 * @return the planet status record
	 */
	Result<PlanetStatus, IOException> getPlanetStatus(String id);
	/**
	 * Move a fleet to the specified coordinates.
	 * @param id the fleet id
	 * @param x the target coordinate
	 * @param y the target coordinate
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> moveFleet(int id, double x, double y);
	/**
	 * Add a waypoint to the given fleet's movement order.
	 * @param id the fleet id
	 * @param x the new waypoint coordinate
	 * @param y the new waypoint coordinate
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> addFleetWaypoint(int id, double x, double y);
	/**
	 * Instruct the fleet to move to a specified planet.
	 * @param id the own fleet id
	 * @param target the target planet id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> moveToPlanet(int id, String target);
	/**
	 * Instruct the fleet to follow another fleet.
	 * @param id the own fleet id
	 * @param target the target fleet id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> followFleet(int id, int target);
	/**
	 * Issue an attack order against the other fleet.
	 * @param id the own fleet id
	 * @param target the target fleet id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> attackFleet(int id, int target);
	/**
	 * Attack a planet with the fleet.
	 * @param id the own fleet id
	 * @param target the target planet id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> attackPlanet(int id, String target);
	/**
	 * Instruct a fleet to colonize a planet by one of its
	 * colony ships.
	 * @param id the fleet id
	 * @param target the target planet
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> colonize(int id, String target);
	/**
	 * Create a new fleet around the given planet.
	 * @param planet the planet
	 * @return the created fleet status object
	 */
	Result<FleetStatus, IOException> newFleet(String planet);
	/**
	 * Create a new fleet around the given planet and
	 * deploy ships and equipment according to the specification
	 * given by the list of inventory item.
	 * @param planet the target planet
	 * @param inventory the target inventory
	 * @return the created fleet status
	 */
	Result<FleetStatus, IOException> newFleet(String planet, List<InventoryItem> inventory);
	/**
	 * Create a new fleet next to the given fleet.
	 * @param id the fleet identifier
	 * @return the created fleet status object
	 */
	Result<FleetStatus, IOException> newFleet(int id);
	/**
	 * Create a new fleet next to the given other fleet
	 * and transfer units from it according to the inventory item 
	 * specification (ignores slots).
	 * @param id the target fleet
	 * @param inventory the target inventory
	 * @return the created fleet status
	 */
	Result<FleetStatus, IOException> newFleet(int id, List<InventoryItem> inventory);
	/**
	 * Delete an empty fleet.
	 * @param id the target fleet
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> deleteFleet(int id);
	/**
	 * Rename the fleet.
	 * @param id the fleet id
	 * @param name the new name
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> renameFleet(int id, String name);
	/**
	 * Sells one unit from the given fleet inventory item.
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @return the inventory item status
	 */
	Result<InventoryItemStatus, IOException> sellFleetItem(int id, int itemId);
	/**
	 * Deploy one unit of the given type into the target fleet.
	 * @param id the fleet id
	 * @param type the unit type
	 * @return the inventory item status
	 */
	Result<InventoryItemStatus, IOException> deployFleetItem(int id, String type);
	/**
	 * Undeploy a single fleet item (such as fighters and vehicles).
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @return the inventory item status
	 */
	Result<InventoryItemStatus, IOException> undeployFleetItem(int id, int itemId);
	/**
	 * Adds one unit of equipment into the given fleet's
	 * given inventory item's slot.
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @param slotId the slot id
	 * @param type the technology to add
	 * @return the inventory item status
	 */
	Result<InventoryItemStatus, IOException> addFleetEquipment(int id, int itemId, String slotId, String type);
	/**
	 * Remove one unit of equipment from the given fleet's
	 * given inventory items' slot.
	 * @param id the fleet id
	 * @param itemId the inventory item id within the fleet
	 * @param slotId the slot id
	 * @return the inventory item status
	 */
	Result<InventoryItemStatus, IOException> removeFleetEquipment(int id, int itemId, String slotId);
	/**
	 * Automatically upgrade and fill in fleet items.
	 * @param id the fleet id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> fleetUpgrade(int id);
	/**
	 * Stop the given fleet.
	 * @param id the fleet id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> stopFleet(int id);
	/**
	 * Transfer units between two fleets.
	 * @param sourceFleet the source fleet
	 * @param destinationFleet the destination fleet
	 * @param sourceItem the source inventory item id
	 * @param mode the transfer type the transfer mode
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> transfer(int sourceFleet, int destinationFleet, int sourceItem, FleetTransferMode mode);
	/**
	 * Mark the planet for colonization.
	 * @param id the planet id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> colonize(String id);
	/**
	 * Cancel the colonization of the given planet.
	 * @param id the planet id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> cancelColonize(String id);
	/**
	 * Place a building on the given planet at the specified location.
	 * @param planetId the planet identifier
	 * @param type the building type
	 * @param race the building race
	 * @param x the location
	 * @param y the location
	 * @return the created building id
	 */
	Result<Integer, IOException> build(String planetId, String type, String race, int x, int y);
	/**
	 * Place a building on the given planet at a suitable location
	 * (using the same placement logic as the AI would).
	 * @param planetId the planet identifier
	 * @param type the building type
	 * @param race the building race
	 * @return the created building id
	 */
	Result<Integer, IOException> build(String planetId, String type, String race);
	/**
	 * Enable a specific building on the planet.
	 * @param planetId the planet id
	 * @param id the building id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> enable(String planetId, int id);
	/**
	 * Disable a specific building on the planet.
	 * @param planetId the planet id
	 * @param id the building id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> disable(String planetId, int id);
	/**
	 * Activate the repair function on the target building.
	 * @param planetId the planet id
	 * @param id the building id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> repair(String planetId, int id);
	/**
	 * Deactivate the repair function on the target building.
	 * @param planetId the planet id
	 * @param id the building id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> repairOff(String planetId, int id);
	/**
	 * Demolish the given building on the planet.
	 * @param planetId the planet id
	 * @param id the building id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> demolish(String planetId, int id);
	/**
	 * Upgrade the given building to the specified level.
	 * @param planetId the planet id
	 * @param id the building id
	 * @param level the new level
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> buildingUpgrade(String planetId, int id, int level);
	/**
	 * Deploy an unit with the given type into the planet's inventory.
	 * The deployed item is owned by the current player.
	 * You may deploy spy satellites onto enemy planets, otherwise, 
	 * the deployment is rejected with ane error
	 * @param planetId the target planet id
	 * @param type the technology id
	 * @return the inventory status after the action
	 */
	Result<InventoryItemStatus, IOException> deployPlanetItem(String planetId, String type);
	/**
	 * Undeploy an unit from the planet's inventory, must be the
	 * current player's planet.
	 * @param planetId the planet id
	 * @param itemId the inventory item id
	 * @return the inventory status after the action
	 */
	Result<InventoryItemStatus, IOException> undeployPlanetItem(String planetId, int itemId);
	/**
	 * Sell an unit from the planet's inventory.
	 * @param planetId the planet id
	 * @param itemId the inventory item id
	 * @return the inventory item status after the action
	 */
	Result<InventoryItemStatus, IOException> sellPlanetItem(String planetId, int itemId);
	/**
	 * Add one unit of the given equipment type into the given slot
	 * of the given inventory item of the given planet.
	 * @param planetId the planet
	 * @param itemId the inventory item id
	 * @param slotId the slot id
	 * @param type the technology id
	 * @return the inventory item status after the action
	 */
	Result<InventoryItemStatus, IOException> addPlanetEquipment(String planetId, int itemId, String slotId, String type);
	/**
	 * Remove one unit of the technology at the given slot, inventory item and planet.
	 * @param planetId the planet identifier
	 * @param itemId the inventory item id
	 * @param slotId the slot id
	 * @return the inventory item status after the action
	 */
	Result<InventoryItemStatus, IOException> removePlanetEquipment(String planetId, int itemId, String slotId);
	/**
	 * Upgrade the planet inventory to the best technology and
	 * available item counts.
	 * @param planetId the target planet id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> planetUpgrade(String planetId);
	/**
	 * Add the given technology to the production list with zero
	 * quantity and default priority, if the production lines aren't full.
	 * @param type the technology id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> startProduction(String type);
	/**
	 * Remove the production line with the given technology.
	 * @param type the technology id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> stopProduction(String type);
	/**
	 * Set the production quantity on a production line.
	 * @param type the technology id
	 * @param count the new quantity
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> setProductionQuantity(String type, int count);
	/**
	 * Set the production priority on the given. 
	 * @param type the technology id
	 * @param priority the new priority
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> setProductionPriority(String type, int priority);
	/**
	 * Sell the given amount of units from the player's inventory.
	 * @param type the technology id
	 * @param count the number of items to sell
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> sellInventory(String type, int count);
	/**
	 * Start researching the given technology.
	 * @param type the technology id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> startResearch(String type);
	/**
	 * Stop researching the given technology.
	 * @param type the technology id
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> stopResearch(String type);
	/**
	 * Set the money allocated to the running research.
	 * @param type the technology id
	 * @param money the allocated money
	 * @return a void result or an exception 
	 */
	Result<Void, IOException> setResearchMoney(String type, int money);
}
