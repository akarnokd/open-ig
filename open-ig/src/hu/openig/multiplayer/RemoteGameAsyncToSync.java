/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.AsyncResult;
import hu.openig.multiplayer.model.WelcomeResponse;

import java.io.IOException;

/**
 * An asynchronous game API implementation which turns
 * the calls into the synchronous API calls.
 * @author akarnokd, 2013.05.01.
 *
 */
public class RemoteGameAsyncToSync implements RemoteGameAsyncAPI {
	/** The wrapped API. */
	protected final RemoteGameAPI api;
	/**
	 * Constructor, sets the API object.
	 * @param api the synchronous game API
	 */
	public RemoteGameAsyncToSync(RemoteGameAPI api) {
		this.api = api;
	}

	@Override
	public void begin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void end() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void end(AsyncResult<? super Void, ? super IOException> out) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ping(final AsyncResult<? super Long, ? super IOException> out) {
		try {
			out.onSuccess(api.ping());
		} catch (IOException ex) {
			out.onError(ex);
		}
	}
	@Override
	public void login(String user, String passphrase, String version,
			AsyncResult<? super WelcomeResponse, ? super IOException> out) {
		try {
			out.onSuccess(api.login(user, passphrase, version));
		} catch (IOException ex) {
			out.onError(ex);
		}
	}

}
