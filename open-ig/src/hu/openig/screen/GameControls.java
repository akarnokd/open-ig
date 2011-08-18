/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import hu.openig.core.Act;
import hu.openig.model.Screens;
import hu.openig.model.World;

import java.awt.FontMetrics;
import java.io.Closeable;

/**
 * Interface for interacting with the game window or other objects in a global manner.
 * @author akarnokd, 2010.01.06.
 */
public interface GameControls {
	/**
	 * Switches the current display language to the new one.
	 * Reloads all language dependant resources and refreshes the screen.
	 * @param newLanguage the new language to change to
	 */
	void switchLanguage(String newLanguage);
	/**
	 * Display the given screen as the primary object. The secondary object, if any, will be removed.
	 * @param screen the new screen to display
	 * @return the reference to the new screen.
	 */
	ScreenBase displayPrimary(Screens screen);
	/**
	 * Display the given secondary screen.
	 * @param screen the screen to display as secondary
	 * @return the reference to the new screen.
	 */
	ScreenBase displaySecondary(Screens screen);
	/** Hide the secondary screen. */
	void hideSecondary();
	/**
	 * Play the given set of videos.
	 * @param videos the list of videos to play
	 */
	void playVideos(String... videos);
	/**
	 * Play the given list of animations then call the given completion handler.
	 * @param onComplete the completion handler
	 * @param videos the videos to play
	 */
	void playVideos(Act onComplete, String... videos);
	/** Display the status bar. */
	void displayStatusbar();
	/** Hide the statusbar. */
	void hideStatusbar();
	/** Exit the game. */
	void exit();
	/** Repaint the window. */
	void repaintInner();
	/** @return Get the width of the rendering component. */
	int getInnerWidth();
	/** @return Get the height of the rendering component. */
	int getInnerHeight();
	/**
	 * Repaint a particular region within the rendering component.
	 * @param x the region X
	 * @param y the region Y
	 * @param w the region width
	 * @param h the region height
	 */
	void repaintInner(int x, int y, int w, int h);
	/**
	 * Ask for a font metrics.
	 * @param size the target font size
	 * @return the default font metrics object
	 */
	FontMetrics fontMetrics(int size);
	/** Save the world state. */
	void save();
	/**
	 * Load the world state.
	 * @param name the save name or null to load the most recent.
	 */
	void load(String name);
	/**
	 * Register a periodic timer action with the given delay.
	 * @param delay the delay in milliseconds.
	 * @param action the action
	 * @return the handler to cancel the registration
	 */
	Closeable register(int delay, Act action);
	/** @return The current world. */
	World world();
	/** @return the current primary screen type or null if none. */
	Screens primary();
	/** @return the current secondary screen type or null if none. */
	Screens secondary();
	/** End the current game. */
	void endGame();
	/** Start the next battle from the pending battles. */
	void startBattle();
	/** 
	 * Creates a fake mouse move event by sending a MOVE to the current screens.
	 * Can be used to update event listeners when the screen changes under the mouse (e.g., ship
	 * walk transition). 
	 */
	void moveMouse();
}
