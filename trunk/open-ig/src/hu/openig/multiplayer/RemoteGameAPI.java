/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.MultiplayerUser;
import hu.openig.model.Production;
import hu.openig.model.ResearchType;
import hu.openig.multiplayer.model.EmpireStatuses;
import hu.openig.multiplayer.model.FleetStatus;
import hu.openig.multiplayer.model.LoginRequest;
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
	 * @throws IOException on communication error 
	 */
	long ping() throws IOException;
	/**
	 * Login.
	 * @param request the login details
	 * @return the welcome message if successful.
	 * @throws IOException on communication error
	 */
	WelcomeResponse login(LoginRequest request) throws IOException;
	/**
	 * Relogin into a running session.
	 * @param sessionId the session id
	 * @throws IOException on communication error
	 */
	void relogin(String sessionId) throws IOException;
	/**
	 * Indicate the intent to leave the game/connection.
	 * @throws IOException on error
	 */
	void leave() throws IOException;
	/**
	 * Retrieve the current game definition.
	 * @return the game definition
	 * @throws IOException on error
	 */
	MultiplayerDefinition getGameDefinition() throws IOException;
	/**
	 * Ask the game host to use the given user settings for the game.
	 * @param user the user settings
	 * @throws IOException on error
	 */
	void choosePlayerSettings(MultiplayerUser user) throws IOException;
	/**
	 * Join the current match.
	 * @return the game settings to use
	 * @throws IOException on error
	 */
	MultiplayerGameSetup join() throws IOException;
	/**
	 * Signal the server that the game has finished loading
	 * and initializing; synchronizes multiple players.
	 * @throws IOException on error
	 */
	void ready() throws IOException;
	/**
	 * Returns the empire status information.
	 * @return the empire statuses
	 * @throws IOException on error
	 */
	EmpireStatuses getEmpireStatuses() throws IOException;
	/**
	 * Returns the own and known fleets list.
	 * @return the list of fleets.
	 * @throws IOException on error
	 */
	List<FleetStatus> getFleets() throws IOException;
	/**
	 * Get information about a concrete fleet.
	 * @param fleetId the fleet identifier
	 * @return the fleet
	 * @throws IOException on error
	 */
	FleetStatus getFleet(int fleetId) throws IOException;
	/**
	 * Retrieve current inventory status.
	 * @return the map from tech id to number.
	 * @throws IOException on error
	 */
	Map<ResearchType, Integer> getInventory() throws IOException;
	/**
	 * Returns a list of active productions.
	 * @return the list of active productions
	 * @throws IOException on error
	 */
	List<Production> getProductions() throws IOException;
	/**
	 * Returns the current research status, including
	 * available and in-progress technology info.
	 * @return the research status object
	 * @throws IOException on error
	 */
	ResearchStatus getResearches() throws IOException;
	/**
	 * Returns a list of planet statuses for all visible
	 * planets.
	 * @return the list of planet statuses
	 * @throws IOException on error
	 */
	List<PlanetStatus> getPlanetStatuses() throws IOException;
	/**
	 * Retrieve a concrete planet's status.
	 * @param id the planet identifier
	 * @return the planet status record
	 * @throws IOException on error
	 */
	PlanetStatus getPlanetStatus(String id) throws IOException;
	/**
	 * Move a fleet to the specified coordinates.
	 * @param id the fleet id
	 * @param x the target coordinate
	 * @param y the target coordinate
	 * @throws IOException on error
	 */
	void moveFleet(int id, double x, double y) throws IOException;
	/**
	 * Add a waypoint to the given fleet's movement order.
	 * @param id the fleet id
	 * @param x the new waypoint coordinate
	 * @param y the new waypoint coordinate
	 * @throws IOException on error
	 */
	void addFleetWaypoint(int id, double x, double y) throws IOException;
	/**
	 * Instruct the fleet to move to a specified planet.
	 * @param id the own fleet id
	 * @param target the target planet id
	 * @throws IOException on error
	 */
	void moveToPlanet(int id, String target) throws IOException;
	/**
	 * Instruct the fleet to follow another fleet.
	 * @param id the own fleet id
	 * @param target the target fleet id
	 * @throws IOException on error
	 */
	void followFleet(int id, int target) throws IOException;
	/**
	 * Issue an attack order against the other fleet.
	 * @param id the own fleet id
	 * @param target the target fleet id
	 * @throws IOException on error
	 */
	void attackFleet(int id, int target) throws IOException;
	/**
	 * Attack a planet with the fleet.
	 * @param id the own fleet id
	 * @param target the target planet id
	 * @throws IOException on error
	 */
	void attackPlanet(int id, String target) throws IOException;
	/**
	 * Instruct a fleet to colonize a planet by one of its
	 * colony ships.
	 * @param id the fleet id
	 * @param target the target planet
	 * @throws IOException on error
	 */
	void colonizePlanet(int id, String target) throws IOException;
	/**
	 * Create a new fleet around the given planet.
	 * @param planet the planet
	 * @throws IOException on error
	 */
	void newFleet(String planet) throws IOException;
	/**
	 * Create a new fleet next to the given fleet.
	 * @param id the fleet identifier
	 * @throws IOException on error
	 */
	void newFleet(int id) throws IOException;
	/**
	 * Delete an empty fleet.
	 * @param id the target fleet
	 * @throws IOException on error
	 */
	void deleteFleet(int id) throws IOException;
}
