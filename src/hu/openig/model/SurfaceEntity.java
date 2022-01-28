/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

/**
 * The entity describing a particular Location on the planet surface. For multi-tile and building entities, this
 * class is used to 'mediate' the inner segments of a tile.
 * @author akarnokd
 */
public final class SurfaceEntity {
    /**

     * The virtual row within the Tile object. A row is defined in the up-right direction and is always nonnegative (despite the surface coordinate
     * system is basically on the negative axis).
     */
    public int virtualRow;
    /**
     * The virtual column within the tile object. The column is defined in the  down-right direction and is always nonnegative.

     */
    public int virtualColumn;
    /** The referenced tile. */
    public Tile tile;
    /** The attached building object if any. */
    public Building building;
    /** The entity type. */
    public SurfaceEntityType type;

}
