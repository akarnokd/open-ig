/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

/**
 * Interface for interacting with the game window or other objects in a global manner.
 * @author karnokd, 2010.01.06.
 * @version $Revision 1.0$
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
	void displayPrimary(ScreenBase screen);
	/**
	 * Display the given secondary screen.
	 * @param screen the screen to display as secondary
	 */
	void displaySecondary(ScreenBase screen);
	/** Hide the secondary screen. */
	void hideSecondary();
	/**
	 * Play the given set of videos.
	 * @param videos the list of videos to play
	 */
	void playVideos(String... videos);
	/** Display the status bar. */
	void displayStatusbar();
	/** Hide the statusbar. */
	void hideStatusbar();
}
