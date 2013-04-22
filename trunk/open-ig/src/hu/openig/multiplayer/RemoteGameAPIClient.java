/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.multiplayer.model.ErrorResponse;
import hu.openig.multiplayer.model.LoginRequest;
import hu.openig.multiplayer.model.MessageUtils;
import hu.openig.multiplayer.model.WelcomeResponse;
import hu.openig.net.MessageClient;
import hu.openig.net.MessageObject;
import hu.openig.net.MissingAttributeException;

import java.io.IOException;

/**
 * A remote game API client implementation based
 * on MessageObject exchanges.
 * @author akarnokd, 2013.04.22.
 */
public class RemoteGameAPIClient implements RemoteGameAPI {
	/** The message client object. */
	protected final MessageClient client;
	/**
	 * Constructor. Sets the message client object.
	 * @param client the client object
	 */
	public RemoteGameAPIClient(MessageClient client) {
		this.client = client;
	}
	@Override
	public long ping() throws IOException {
		long timestamp = System.nanoTime();
		
		MessageUtils.expectObject(client.query("PING { }"), "PONG");
		
		return System.nanoTime() - timestamp;
	}

	@Override
	public WelcomeResponse login(LoginRequest request) throws IOException {
		
		MessageObject resp = MessageUtils.expectObject(client.query(request.toMessage()), "WELCOME");
		
		WelcomeResponse result = new WelcomeResponse();
		try {
			result.fromMessage(resp);
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorResponse.ERROR_FORMAT, ex.toString());
		}
		
		return result;
	}
}
