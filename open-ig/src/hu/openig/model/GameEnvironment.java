/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Labels;

/**
 * Represents a set of callback options to interact with the game environment
 * (i.e. the UI and other structures outside the game world).
 * @author akarnokd, 2011.12.15.
 */
public interface GameEnvironment {
	/** @return the labels for the current UI language. */
	Labels labels();
	/** @return the current world object. */
	World world();
	/** Start a battle. */
	void startBattle();
	/**
	 * Return an AI manager for the given player configuration.
	 * @param player the player
	 * @return the AI manager
	 */
	AIManager getAI(Player player);
	/** @return the game event interface. */
	GameEvents events();
}
