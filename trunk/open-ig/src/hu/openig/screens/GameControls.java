/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;

import java.awt.Rectangle;

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
	 */
	void displayPrimary(Screens screen);
	/**
	 * Display the given secondary screen.
	 * @param screen the screen to display as secondary
	 */
	void displaySecondary(Screens screen);
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
	/**
	 * Set the game window bounds to the specified size. 
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param width the width
	 * @param height the height
	 */
	void setWindowBounds(int x, int y, int width, int height);
	/** @return the current window bounds. */
	Rectangle getWindowBounds();
	/** Center the game window. */
	void center();
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
}
