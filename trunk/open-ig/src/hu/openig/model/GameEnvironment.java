/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Configuration;
import hu.openig.core.Labels;

import java.util.Deque;

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
	/**
	 * Play a full screen video resource.
	 * @param name the video name
	 */
	void playVideo(String name);
	/**
	 * Play an audio resource.
	 * @param name the audio name
	 */
	void playAudio(String name);
	/**
	 * Play an sound type.
	 * @param type the sound type
	 */
	void playSound(SoundType type);
	/**
	 * The various game parameters.
	 * @return the game parameters
	 */
	Parameters params();
	/** 
	 * Returns the configuration.
	 * @return the configuration
	 */
	Configuration config();
	/**
	 * The place to issue the achievements.
	 * @return the queue
	 */
	Deque<String> achievementQueue();
	/** 
	 * Returns the current profile.
	 * @return the profile
	 */
	Profile profile();
}
