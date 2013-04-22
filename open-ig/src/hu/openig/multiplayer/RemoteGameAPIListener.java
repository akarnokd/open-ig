/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Action2E;
import hu.openig.multiplayer.model.ErrorResponse;
import hu.openig.multiplayer.model.LoginRequest;
import hu.openig.multiplayer.model.WelcomeResponse;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageConnection;
import hu.openig.net.MessageObject;
import hu.openig.net.MissingAttributeException;

import java.io.IOException;

/**
 * Listens for and dispatches incoming message requests and produces
 * message responses.
 * @author akarnokd, 2013.04.23.
 */
public class RemoteGameAPIListener implements Action2E<MessageConnection, Object, IOException> {
	/** The game API. */
	protected final RemoteGameAPI api;
	/**
	 * Constructor, takes a remote API entry point.
	 * @param api the API entry point
	 */
	public RemoteGameAPIListener(RemoteGameAPI api) {
		this.api = api;
	}
	@Override
	public void invoke(MessageConnection conn, Object message)
			throws IOException {
		try {
			if (message instanceof MessageObject) {
				MessageObject mo = (MessageObject)message;
				if ("PING".equals(mo.name)) {
					api.ping();
					conn.send(message, "PONG { }");
				} else
				if ("LOGIN".equals(mo.name)) {
					LoginRequest req = new LoginRequest();
					req.fromMessage(mo);
					
					WelcomeResponse resp = api.login(req);
					
					conn.send(message, resp.toMessage());
				}
			} else
			if (message instanceof MessageArray) {
				MessageArray ma = (MessageArray)message;
				
			} else {
				conn.error(message, ErrorResponse.ERROR_UNKNOWN_MESSAGE, message != null ? message.toString() : "null");
			}
		} catch (MissingAttributeException ex) {
			conn.error(message, ErrorResponse.ERROR_FORMAT, ex.toString());
		}
	}
}
