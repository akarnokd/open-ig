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
import java.util.Map;

/**
 * The remote game API interface.
 * <p>Implementations should ensure that the methods are executed on the proper
 * thread, for example, in EDT.
 * @author akarnokd, 2013.04.22.
 */
public interface GameAPI extends GameCommandAPI {
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
	 * @return the production status
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	ProductionStatuses getProductions() throws IOException;
	/**
	 * Returns the current research status, including
	 * available and in-progress technology info.
	 * @return the research status object
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	ResearchStatuses getResearches() throws IOException;
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
	 * Returns a list of all ongoing space battles which affects the player.
	 * @return the list of space battles
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	List<BattleStatus> getBattles() throws IOException;
	/**
	 * Retrieve a concrete space battle status.
	 * @param battleId the battle identifier
	 * @return the battle status
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	BattleStatus getBattle(int battleId) throws IOException;
	/**
	 * Returns a list of space units for the given battle.
	 * @param battleId the battle id
	 * @return the list of space units
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	List<SpaceBattleUnit> getSpaceBattleUnits(int battleId) throws IOException;
	/**
	 * Returns a list of ground battle units (guns and vehicles) of
	 * the given battle.
	 * @param battleId the battle id
	 * @return the list of units.
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	List<GroundwarUnitStatus> getGroundBattleUnits(int battleId) throws IOException;
	/**
	 * Returns the inventory item status of the given fleet and inventory.
	 * @param fleetId the fleet id
	 * @param itemId the inventory id, if -1, the id of the last
	 * deployFleetItem() call.
	 * @return the inventory item stats
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	InventoryItemStatus getInventoryStatus(int fleetId, int itemId) throws IOException;
	/**
	 * Returns the inventory item status of the given planet and inventory.
	 * @param planetId the planet identifier
	 * @param itemId the inventory id, if -1, the id of the last
	 * deployFleetItem() call.
	 * @return the inventory item stats
	 * @throws IOException on communication error, a ErrorResponse indicates
	 * a gameplay related error result.
	 */
	InventoryItemStatus getInventoryStatus(String planetId, int itemId) throws IOException;
}
