/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */package hu.openig.v1.model;

import hu.openig.v1.core.Tile;

import java.awt.image.BufferedImage;

/**
 * The entity describing a particular Location on the planet surface. For multi-tile and building entities, this
 * class is used to 'mediate' the inner segments of a tile.
 * @author karnokd
 */
public class SurfaceEntity {
	/** 
	 * The virtual row within the Tile object. A row is defined in the up-right direction and is always nonnegative (despite the surface coordinate
	 * system is basically on the negative axis).
	 */
	public int virtualRow;
	/**
	 * The virtual column within the tile object. The column is defined in the  down-right direction and is always nonnegative. 
	 */
	public int virtualColumn;
	/** The location of the bottom element, e.g the (0, height - 1) virtual row. */
	public int bottomRow;
	/**
	 * The referenced tile.
	 */
	public Tile tile;
	/**
	 * Return the image (strip) representing this surface entry.
	 * The default behavior returns the tile strips along its lower 'V' arc.
	 * This method to be overridden to handle the case of damaged or in-progress buildings 
	 * @return the buffered image belonging to the current location or null if no image should be drawn
	 */
	public BufferedImage getImage() {
		if (virtualColumn == 0 && virtualRow < tile.height) {
			return tile.getStrip(tile.alphaBlendImage(), virtualRow);
		} else
		if (virtualRow == tile.height - 1) {
			return tile.getStrip(tile.alphaBlendImage(), tile.height - 1 + virtualColumn);
		}
		return null;
	}
}
