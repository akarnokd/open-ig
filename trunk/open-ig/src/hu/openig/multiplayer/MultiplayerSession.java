/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.model.Configuration;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.MultiplayerEnvironment;
import hu.openig.model.MultiplayerUser;
import hu.openig.multiplayer.model.ErrorResponse;
import hu.openig.multiplayer.model.LoginRequest;
import hu.openig.multiplayer.model.WelcomeResponse;
import hu.openig.utils.U;

import java.io.IOException;
import java.util.Random;

/**
 * A multiplayer session object receiving requests from
 * remote clients.
 * @author akarnokd, 2013.04.26.
 */
public class MultiplayerSession implements RemoteGameAPI {
	/** The multiplayer definition. Effectively immutable at this point. */
	protected final MultiplayerDefinition definition;
	/** 
	 * The multiplayer environment. Note that accessing
	 * the world object needs to happen on the EDT.
	 */
	protected final MultiplayerEnvironment environment;
	/** The multiplayer user. */
	protected MultiplayerUser user;
	/** The session id of this connection. */
	protected String sessionId;
	/**
	 * Constructor. Sets the game and environment interfaces.
	 * @param definition the multiplayer game definition
	 * @param environment the common environment
	 */
	public MultiplayerSession(MultiplayerDefinition definition, MultiplayerEnvironment environment) {
		this.definition = definition;
		this.environment = environment;
	}
	@Override
	public long ping() throws IOException {
		return 0;
	}

	@Override
	public WelcomeResponse login(LoginRequest request) throws IOException {
		if (U.equal(request.version, Configuration.VERSION)) {
			for (MultiplayerUser mu : definition.players) {
				if (U.equal(mu.userName, request.user) 
						&& U.equal(mu.passphrase, request.passphrase)) {
					
					this.sessionId = generateSessionId();
					user = mu;
					user.sessionId(this.sessionId);
					
					WelcomeResponse result = new WelcomeResponse();
					result.sessionId = this.sessionId;
					
					return result;
				}
			}
			throw new ErrorResponse(ErrorResponse.ERROR_USER);
		}
		throw new ErrorResponse(ErrorResponse.ERROR_VERSION, "Server: " + Configuration.VERSION + " - Client: " + request.version);
	}
	/**
	 * Generate a random session id.
	 * @return the session id
	 */
	protected String generateSessionId() {
		byte[] key = new byte[64];
		Random rnd = new Random();
		rnd.nextBytes(key);
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < key.length; i++) {
			b.append(String.format("%02X", key[i] & 0xFF));
		}
		return b.toString();
	}
	/**
	 * Checks if the user is logged in and this is still its only session.
	 * @throws ErrorResponse if the user is not logged in or its session is invalid
	 */
	protected void ensureLogin() throws ErrorResponse {
		if (user == null) {
			throw new ErrorResponse(ErrorResponse.ERROR_NOT_LOGGED_IN);
		}
		if (!U.equal(user.sessionId(), sessionId)) {
			throw new ErrorResponse(ErrorResponse.ERROR_SESSION_INVALID);
		}
	}
}
