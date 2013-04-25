/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import java.awt.image.BufferedImage;

/**
 * The record type of the user.
 * @author akarnokd, 2013.04.24.
 */
public class MultiplayerUser {
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
	public boolean joined;
	/** The traits. */
	public final Traits traits = new Traits();
	// TODO fields
}
