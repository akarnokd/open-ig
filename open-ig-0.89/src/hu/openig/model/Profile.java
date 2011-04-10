/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.HashSet;
import java.util.Set;

/**
 * The current user's profile.
 * @author akarnokd, Apr 9, 2011
 */
public class Profile {
	/** The profile's name. */
	public String name = "default";
	/** The acquired achievements. */
	private final Set<String> achievements = new HashSet<String>();
	/**
	 * Test if the given achievement is taken.
	 * @param name the achievement name
	 * @return the status
	 */
	public boolean hasAchievement(String name) {
		return achievements.contains(name);
	}
}
