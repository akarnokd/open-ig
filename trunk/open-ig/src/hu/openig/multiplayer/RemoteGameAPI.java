/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.multiplayer.model.LoginRequest;
import hu.openig.multiplayer.model.WelcomeResponse;

import java.io.IOException;

/**
 * The remote game API interface.
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
}
