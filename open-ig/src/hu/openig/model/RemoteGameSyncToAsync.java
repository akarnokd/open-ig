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
 * Extension to the basic game sync to async converter
 * containing the remote API calls.
 * @author akarnokd, 2013.05.04.
 */
public class RemoteGameSyncToAsync extends GameSyncToAsync implements RemoteGameAPI {
	/** The wrapped remote async API. */
	protected final RemoteGameAsyncAPI rapi;
	/**
	 * Constructor, sets the remote async API.
	 * @param api the remote async API
	 */
	public RemoteGameSyncToAsync(RemoteGameAsyncAPI api) {
		super(api);
		this.rapi = api;
	}
	@Override
	public long ping() throws IOException {
		Value<Long> as = newSubject();
		rapi.ping(as);
		return get(as);
	}
	@Override
	public WelcomeResponse login(String user, String passphrase, String version)
			throws IOException {
		Value<WelcomeResponse> as = newSubject();
		rapi.login(user, passphrase, version, as);
		return get(as);
	}
	@Override
	public void relogin(String sessionId) throws IOException {
		Value<Void> as = newSubject();
		rapi.relogin(sessionId, as);
		get(as);
	}
	@Override
	public void leave() throws IOException {
		Value<Void> as = newSubject();
		rapi.leave(as);
		get(as);
	}
	@Override
	public MultiplayerDefinition getGameDefinition() throws IOException {
		Value<MultiplayerDefinition> as = newSubject();
		rapi.getGameDefinition(as);
		return get(as);
	}
	@Override
	public void choosePlayerSettings(MultiplayerUser user) throws IOException {
		Value<Void> as = newSubject();
		rapi.choosePlayerSettings(user, as);
		get(as);
	}
	@Override
	public MultiplayerGameSetup join() throws IOException {
		Value<MultiplayerGameSetup> as = newSubject();
		rapi.join(as);
		return get(as);
	}
	@Override
	public void ready() throws IOException {
		Value<Void> as = newSubject();
		rapi.ready(as);
		get(as);
	}
}
