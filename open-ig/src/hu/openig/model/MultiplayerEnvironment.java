/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Interface describing methods required from
 * the common resources for multiplayer purposes.
 * @author akarnokd, 2013.04.26.
 */
public interface MultiplayerEnvironment {
	/**
	 * Returns the current world object or null
	 * if no game is running.
	 * @return the world object
	 */
	World world();
	/**
	 * Is the game currently loading, if true, the world object should
	 * not be accessed.
	 * @return true if the game is loading
	 */
	boolean isLoading();
}
