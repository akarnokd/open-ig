/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.AsyncResult;
import hu.openig.core.DeferredAction;
import hu.openig.core.DeferredVoid;

import java.io.IOException;

/**
 * Converts the remote async game API calls
 * to synchronous API calls. 
 * @author akarnokd, 2013.05.04.
 */
public class RemoteGameAsyncToSync extends GameAsyncToSync implements
		RemoteGameAsyncAPI {
	/** The wrapped remote API. */
	protected final RemoteGameAPI rapi;
	/**
	 * Constructor, sets the remote API.
	 * @param api the remote API
	 */
	public RemoteGameAsyncToSync(RemoteGameAPI api) {
		super(api);
		this.rapi = api;
	}
	@Override
	public void ping(final AsyncResult<? super Long, ? super IOException> out) {
		execute(new DeferredAction<Long, IOException>(out) {
			@Override
			public Long invoke() throws IOException {
				return rapi.ping();
			}
		});
	}
	@Override
	public void login(final String user, final String passphrase, final String version,
			final AsyncResult<? super WelcomeResponse, ? super IOException> out) {
		execute(new DeferredAction<WelcomeResponse, IOException>(out) {
			@Override
			public WelcomeResponse invoke() throws IOException {
				return rapi.login(user, passphrase, version);
			}
		});
	}

	@Override
	public void relogin(final String sessionId,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid< IOException>(out) {
			@Override
			public void invoke() throws IOException {
				rapi.relogin(sessionId);
			}
		});
	}

	@Override
	public void leave(AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				rapi.leave();
			}
		});
	}

	@Override
	public void getGameDefinition(
			AsyncResult<? super MultiplayerDefinition, ? super IOException> out) {
		execute(new DeferredAction<MultiplayerDefinition, IOException>(out) {
			@Override
			public MultiplayerDefinition invoke() throws IOException {
				return rapi.getGameDefinition();
			}
		});
	}

	@Override
	public void choosePlayerSettings(final MultiplayerUser user,
			AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				rapi.choosePlayerSettings(user);
			}
		});
	}

	@Override
	public void join(
			AsyncResult<? super MultiplayerGameSetup, ? super IOException> out) {
		execute(new DeferredAction<MultiplayerGameSetup, IOException>(out) {
			@Override
			public MultiplayerGameSetup invoke() throws IOException {
				return rapi.join();
			}
		});
		
	}

	@Override
	public void ready(AsyncResult<? super Void, ? super IOException> out) {
		execute(new DeferredVoid<IOException>(out) {
			@Override
			public void invoke() throws IOException {
				rapi.ready();
			}
		});
	}

}
