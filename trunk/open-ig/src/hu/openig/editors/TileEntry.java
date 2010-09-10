/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.editors;

import hu.openig.core.Tile;
import hu.openig.model.BuildingType;

import javax.swing.ImageIcon;

/**
 * A tile entry.
 * @author karnokd
 */
public class TileEntry {
	/** A smaller preview image. */
	public ImageIcon preview;
	/** The identifier. */
	public int id;
	/** The user-definable name. */
	public String name;
	/** The surface name. */
	public String surface;
	/** The related tile object. */
	public Tile tile;
	/** The tile cused for the preview. */
	public Tile previewTile;
	/** The referenced building type if any. */
	public BuildingType buildingType;
}
