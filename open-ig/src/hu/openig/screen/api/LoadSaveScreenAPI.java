/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.api;

/**
 * API to control the save screen.
 * @author akarnokd, 2012.08.16.
 */
public interface LoadSaveScreenAPI {
	/**
	 * Allow the user to save the game?
	 * @param value true or false
	 */
	void maySave(boolean value);
	/**
	 * Display the specified page.
	 * @param page the page
	 */
	void displayPage(SettingsPage page);
}
