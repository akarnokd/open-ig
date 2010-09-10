/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.v1.editors;

import hu.openig.v1.core.Tile;
import hu.openig.v1.model.BuildingType;

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
	/** The referenced building type if any. */
	public BuildingType buildingType;
}
