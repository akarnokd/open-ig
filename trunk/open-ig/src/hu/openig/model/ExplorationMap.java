/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.utils.JavaUtils;

import java.util.Set;

/**
 * The manager of exploration map.
 * @author akarnokd, 2011.12.29.
 */
public class ExplorationMap {
	/** The set of cells undiscovered on the starmap. */
	public final Set<Location> map = JavaUtils.newHashSet();
	/** The cell size used for the exploration map cells. */
	public final int cellSize;
	/** The number of rows of the exploration map. */
	public final int rows;
	/** The number of columns of the exploration map. */
	public final int columns;
	/**
	 * Constructor. Initializes the exploration map with everything unexplored. 
	 * @param w the world
	 */
	public ExplorationMap(World w) {
		cellSize = (int)Math.floor(Math.sqrt(2) * w.env.params().fleetRadarUnitSize()) - 4;
		rows = (int)Math.ceil(w.galaxyModel.map.getHeight() / cellSize);
		columns = (int)Math.ceil(w.galaxyModel.map.getWidth() / cellSize);
		initExplorationMap();
	}
	/**
	 * Set all cells to undiscovered.
	 */
	private void initExplorationMap() {
		map.clear();
		for (int x = 0; x < columns; x++) {
			for (int y = 0; y < rows; y++) {
				Location loc = Location.of(x, y);
				map.add(loc);
			}
		}
	}
	/**
	 * Remove the covered exploration cells.
	 * @param cx the circle center
	 * @param cy the circle center
	 * @param r the circle radius
	 */
	public void removeCoverage(int cx, int cy, int r) {
		if (r > 200) {
			System.err.println("Impossible radar size!");
			return;
		}
		// inner rectangle
		int ux1 = (int)Math.ceil(cx - Math.sqrt(2) * r / 2);
		int uy1 = (int)Math.ceil(cy - Math.sqrt(2) * r / 2);
		int ux2 = (int)Math.floor(cx + Math.sqrt(2) * r / 2);
		int uy2 = (int)Math.floor(cy + Math.sqrt(2) * r / 2);
		
		int colStart = (int)Math.ceil(1.0 * ux1 / cellSize);
		int colEnd = (int)Math.floor(1.0 * ux2 / cellSize);
		int rowStart = (int)Math.ceil(1.0 * uy1 / cellSize);
		int rowEnd = (int)Math.floor(1.0 * uy2 / cellSize);
		// remove whole enclosed cells
		for (int x = colStart; x < colEnd; x++) {
			for (int y = rowStart; y < rowEnd; y++) {
				Location loc = Location.of(x, y);
				if (map.contains(loc)) {
					map.remove(loc);
				}
			}
		}
	}

}
