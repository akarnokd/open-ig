/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

/**
 * The manager of exploration map.
 * @author akarnokd, 2011.12.29.
 */
public class ExplorationMap {
    /** The set of cells undiscovered on the starmap. */
    public final Set<Location> map = new HashSet<>();
    /** The Cell width in pixels. */
    public final double cellWidth;
    /** The Cell height in pixels. */
    public final double cellHeight;
    /** The number of rows of the exploration map. */
    public final int rows;
    /** The number of columns of the exploration map. */
    public final int columns;
    /**
     * Constructor. Initializes the exploration map with everything unexplored.

     * @param p the player
     */
    public ExplorationMap(Player p) {
        int maxCellSize = (int)Math.floor(Math.sqrt(2)

                * p.world.params().fleetRadarUnitSize()
                /* * p.world.params().fleetRadarlessMultiplier() */) - 4;
        if (maxCellSize < 1) {
            maxCellSize = 1;
        }
        int w = p.world.galaxyModel.map.getWidth();
        int h = p.world.galaxyModel.map.getHeight();
        rows = (int)Math.ceil(h * 1.0 / maxCellSize);
        columns = (int)Math.ceil(w * 1.0 / maxCellSize);
        cellWidth = 1d * w / columns;
        cellHeight = 1d * h / rows;

        initExplorationMap();
    }
    /**
     * Computes the allowed exploration map, considering the player's exploration limits.
     * @param inner the inner rectangle
     * @param outer the outer rectangle
     * @return the updated map
     */
    public Set<Location> allowedMap(Rectangle inner, Rectangle outer) {
        if (inner == null && outer == null) {
            return map;
        }
        Set<Location> result = new HashSet<>();

        for (Location loc : map) {
            int cx = (int)((loc.x + 0.5) * cellWidth);
            int cy = (int)((loc.y + 0.5) * cellHeight);
            if (inner != null) {
                if (inner.contains(cx, cy)) {
                    continue;
                }
            }
            if (outer != null) {
                if (!outer.contains(cx, cy)) {
                    continue;
                }
            }
            result.add(loc);
        }

        return result;
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

        int colStart = (int)Math.ceil(1.0 * ux1 / cellWidth);
        int colEnd = (int)Math.floor(1.0 * ux2 / cellWidth);
        int rowStart = (int)Math.ceil(1.0 * uy1 / cellHeight);
        int rowEnd = (int)Math.floor(1.0 * uy2 / cellHeight);
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
    /**
     * Convert the cell coordinate to map coordinate.
     * @param cx the cell x
     * @param cy the cell y
     * @return the real map coordinates
     */
    public Point2D.Double toMap(int cx, int cy) {
        return new Point2D.Double(cx * cellWidth, cy * cellHeight);
    }
    /**
     * Convert the cell coordinates into map coordinates where the coordinates
     * should represent the center point of the cell.
     * @param cx the cell x
     * @param cy the cell y
     * @return the real map coordinates
     */
    public Point2D.Double toMapCenter(int cx, int cy) {
        return new Point2D.Double((cx + 0.5) * cellWidth, (cy + 0.5) * cellHeight);
    }
    /**
     * Convert the cell coordinates into map coordinates where the coordinates
     * should represent the center point of the cell.
     * @param loc the location
     * @return the real map coordinates
     */
    public Point2D.Double toMapCenter(Location loc) {
        return toMapCenter(loc.x, loc.y);
    }
}
