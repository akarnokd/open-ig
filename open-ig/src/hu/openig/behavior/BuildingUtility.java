/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.behavior;

import hu.openig.model.GameBuilding;

/**
 * Callback function to return a building's utility value.
 * Used in the resource allocation.
 * @author karnokd
 *
 */
public interface BuildingUtility {
	/**
	 * Returns the utility value of the given building.
	 * @param building the building
	 * @return the utility value, should be at least zero
	 */
	float getUtility(GameBuilding building);
}
