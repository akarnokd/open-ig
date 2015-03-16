/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.editors;

import hu.openig.model.BuildingType;
import hu.openig.model.Tile;

import java.util.Comparator;

import javax.swing.ImageIcon;

/**
 * A tile entry.
 * @author akarnokd
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
    /** The default tile ordering. */
    public static final Comparator<TileEntry> DEFAULT_ORDER = new Comparator<TileEntry>() {
        @Override
        public int compare(TileEntry o1, TileEntry o2) {
            int c = o1.surface.compareTo(o2.surface);
            return c != 0 ? c : Integer.compare(o1.id, o2.id);
        }
    };
}
