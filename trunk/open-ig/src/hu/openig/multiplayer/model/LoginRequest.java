/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer.model;

import hu.openig.net.MessageObject;

/**
 * The login request object.
 * @author akarnokd, 2013.04.22.
 */
public class LoginRequest implements MessageIO {
	/** The user. */
	public String user;
	/** The passphrase. */
	public String passphrase;
	/** The language. */
	public String language;
	/** The client version. */
	public String version;
	@Override
	public MessageObject toMessage() {
		MessageObject req = new MessageObject("LOGIN");
		
		req.set("user", user);
		req.set("passphrase", passphrase);
		req.set("language", language);
		req.set("version", version);

		return req;
	}
	@Override
	public void fromMessage(MessageObject mo) {
		user = mo.getString("user");
		passphrase = mo.getString("passphrase");
		language = mo.getString("language");
		version = mo.getString("version");
	}
}
