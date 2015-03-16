/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.api;

import hu.openig.model.Building;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.PlanetSurface;
import hu.openig.model.SelectionBoxMode;

/**
 * API for several callbacks from the surface
 * renderer.
 * @author akarnokd, 2013.06.01.
 */
public interface SurfaceEvents {
	/**
	 * Callback to indicate a beginning or
	 * end of a drag operation.
	 * @param begin true if beginning
	 */
	void onDrag(boolean begin);
	/**
	 * Place an unit at the specified location.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 */
	void placeUnitAt(int x, int y);
	/**
	 * Remove an unit at the specified location.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 */
	void removeUnitAt(int x, int y);
	/**
	 * Perform a selection based on the unit at the given
	 * coordinates and with the given selection mode.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param mode the selection mode
	 */
	void selectUnitType(int x, int y, SelectionBoxMode mode);
	/**
	 * Move the selected units to a given location.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 */
	void unitsMove(int x, int y);
	/**
	 * Attack with the selected units.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 */
	void unitsAttack(int x, int y);
	/**
	 * Attack-move with the selected nits.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 */
	void unitsAttackMove(int x, int y);
	/**
	 * Toggle the unit placement at the specified coordinates. 
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 */
	void toggleUnitPlacement(int x, int y);
	/**
	 * Callback to place a building at the current placement rectangle.
	 * @param multiple if the placement mode should be kept
	 */
	void placeBuilding(boolean multiple);
	/**
	 * Perform unit selection based on the current selection rectangle.
	 * @param mode the mode
	 */
	void selectUnits(SelectionBoxMode mode);
	/**
	 * Select a building.
	 * @param b the building to select, or null to deselect
	 */
	void selectBuilding(Building b);
	/**
	 * Compute statistics for the given surface.
	 * @param surface the current surface
	 * @return the planet statistics
	 */
	PlanetStatistics update(PlanetSurface surface);
}
