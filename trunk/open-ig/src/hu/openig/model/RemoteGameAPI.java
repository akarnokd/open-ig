/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.io.IOException;

/**
 * Extends the Game API with methods related to
 * remote activities, such as login and
 * game definition.
 * @author akarnokd, 2013.05.04.
 */
public interface RemoteGameAPI extends GameAPI {
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
}
