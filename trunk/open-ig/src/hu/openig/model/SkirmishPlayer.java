/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;

/**
 * Skirmish player configuration.
 * @author akarnokd, 2012.08.20.
 */
public class SkirmishPlayer {
	/** The new unique id. */
	public String newId;
	/** The original id. */
	public String originalId;
	/** The display name. */
	public String name;
	/** The description text. */
	public String description;
	/** The color. */
	public int color;
	/** The icon reference. */
	public String iconRef;
	/** The icon image. */
	public BufferedImage icon;
	/** The race. */
	public String race;
	/**  The AI mode. */
	public SkirmishAIMode ai;
	/** The group. */
	public int group;
	/** No diplomacy. */
	public boolean nodiplomacy;
	/** No database. */
	public boolean nodatabase;
	/** The traits. */
	public final Traits traits = new Traits();
	/**
	 * @return a copy of this player
	 */
	public SkirmishPlayer copy() {
		SkirmishPlayer result = new SkirmishPlayer();
		
		result.originalId = originalId;
		result.newId = newId;
		result.name = name;
		result.description = description;
		result.color = color;
		result.icon = icon;
		result.iconRef = iconRef;
		result.race = race;
		result.ai = ai;
		result.group = group;
		result.nodatabase = nodatabase;
		result.nodiplomacy = nodiplomacy;
		
		return result;
	}
}
