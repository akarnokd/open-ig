/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Action0;
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
	 * @param onComplete the optional completion handler
	 */
	void playVideo(String name, Action0 onComplete);
	/**
	 * Play an audio resource.
	 * @param name the audio name
	 * @param action the completion action
	 */
	void playAudio(String name, Action0 action);
	/**
	 * Play a sound as a given target and invoke an action once it has been played.
	 * @param target the sound target
	 * @param type the sound type
	 * @param action the optional completion action
	 */
	void playSound(SoundTarget target, SoundType type, Action0 action);
	/** Stops the music playback. */
	void stopMusic();
	/** Stops the music playback. */
	void playMusic();
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
	/**
	 * Force display of the given message on bridge.
	 * @param messageId the message ID
	 * @param onSeen the action to perform once the user saw the message
	 */
	void forceMessage(String messageId, Action0 onSeen);
	/**
	 * Lose the current game. Game over.
	 */
	void loseGame();
	/**
	 * Win the current game.
	 */
	void winGame();
	/**
	 * Display or hide the objectives.
	 * @param state the state
	 */
	void showObjectives(boolean state);
	/**
	 * Returns the current simulation step time in milliseconds.
	 * @return the step length in milliseconds
	 */
	int simulationSpeed();
	/** Pause the game. */
	void pause();
	/** Set speed back to 1. */
	void speed1();
	/** @return Are we in battle mode? */
	boolean isBattle();
}
