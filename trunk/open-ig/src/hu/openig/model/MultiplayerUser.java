/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.net.MessageObject;

import java.awt.image.BufferedImage;

/**
 * The record type of the user.
 * @author akarnokd, 2013.04.24.
 */
public class MultiplayerUser implements MessageObjectIO {
	/**  The AI mode, if null, this is a remote user. */
	public SkirmishAIMode ai;
	/** The user name. */
	public String userName;
	/** The user passphrase. */
	public String passphrase;
	/** The color. */
	public int color;
	/** The icon reference. */
	public String iconRef;
	/** The icon image. */
	public BufferedImage icon;
	/** The group. */
	public int group;
	/** Allow the user to change its icon? */
	public boolean changeIcon;
	/** Allow the user to change its race? */
	public boolean changeRace;
	/** Allow the user to change traits? */
	public boolean changeTraits;
	/** Allow the user to change groups? */
	public boolean changeGroup;
	/** The original id. */
	public String originalId;
	/** The diplomacy head. */
	public String diplomacyHead;
	/** No diplomacy. */
	public boolean nodiplomacy;
	/** No database. */
	public boolean nodatabase;
	/** The picture reference. */
	public String picture;
	/** The race. */
	public String race;
	/** The display name. */
	public String name;
	/** The description text. */
	public String description;
	/** Player has joined. */
	private boolean joined;
	/** The player's session id. */
	private String sessionId;
	/** The traits. */
	public final Traits traits = new Traits();
	/**
	 * Returns the join status of this player.
	 * @return has the player joined?
	 */
	public synchronized boolean joined() {
		return joined;
	}
	/**
	 * Returns the current session ID of the user.
	 * @return the session id or null if the user has not yet logged in
	 */
	public synchronized String sessionId() {
		return sessionId;
	}
	/**
	 * Set the joined state.
	 * @param value the new joined state
	 */
	public synchronized void joined(boolean value) {
		this.joined = value;
	}
	/**
	 * Set the session ID of the user.
	 * @param value the new session id
	 */
	public synchronized void sessionId(String value) {
		this.sessionId = value;
	}
	@Override
	public void fromMessage(MessageObject mo) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public MessageObject toMessage() {
		// TODO Auto-generated method stub
		return null;
	}
	// TODO fields
	@Override
	public String name() {
		return "MULTIPLAYER_USER";
	}
}
