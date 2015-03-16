/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.api;

/**
 * The Equipment screen API.
 * @author akarnokd, 2013.01.23.
 */
public interface EquipmentScreenAPI {
	/** Notify the screen that the current research changed. */
	void onResearchChanged();
}
