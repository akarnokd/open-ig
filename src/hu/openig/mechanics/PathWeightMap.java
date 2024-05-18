/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

public class PathWeightMap {

    /** Matrix to store weighs on cells in a grid. */
    public short[][] weightMap;
    /** The width of the weighed a grid. */
    public int sizeX;
    /** The height of the weighed a grid. */
    public int sizeY;
    /** The width offset to use when lining up the weigh matrix with the underlining grid. */
    public int offsetX;
    /** The height offset to use when lining up the weigh matrix with the underlining grid. */
    public int offsetY;

    public PathWeightMap(int sizeX, int sizeY, int offsetX, int offsetY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.weightMap = new short[this.sizeX][this.sizeY];
    }
}
