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

/**
 * Extends the Asynchronous Game API with methods related to
 * remote activities, such as login and
 * game definition.
 * @author akarnokd, 2013.05.04.
 */
public interface RemoteGameAsyncAPI extends GameAsyncAPI {
	/**
	 * A simple ping-pong request pair with the time elapsed in
	 * milliseconds.
	 * @param out the async result
	 */
	void ping(AsyncResult<? super Long, ? super IOException> out);
	/**
	 * Login.
	 * @param user the user object
	 * @param passphrase the passphrase
	 * @param version the caller's version
	 * @param out the async result or the error
	 */
	void login(String user, String passphrase, String version, AsyncResult<? super WelcomeResponse, ? super IOException> out);
	/**
	 * Relogin into a running session.
	 * @param sessionId the session id
	 * @param out the async result or the error
	 */
	void relogin(String sessionId, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Indicate the intent to leave the game/connection.
	 * @param out the async result or the error
	 */
	void leave(AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Retrieve the current game definition.
	 * @param out the async result or the error
	 */
	void getGameDefinition(AsyncResult<? super MultiplayerDefinition, ? super IOException> out);
	/**
	 * Ask the game host to use the given user settings for the game.
	 * @param user the user settings
	 * @param out the async result or the error
	 */
	void choosePlayerSettings(MultiplayerUser user, AsyncResult<? super Void, ? super IOException> out);
	/**
	 * Join the current match.
	 * @param out the async result or the error
	 */
	void join(AsyncResult<? super MultiplayerGameSetup, ? super IOException> out);
	/**
	 * Signal the server that the game has finished loading
	 * and initializing; synchronizes multiple players.
	 * @param out the async result or the error
	 */
	void ready(AsyncResult<? super Void, ? super IOException> out);
}
