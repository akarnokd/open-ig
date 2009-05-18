/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.SurfaceType;
import hu.openig.core.Tile;

import java.awt.image.BufferedImage;
import java.util.Set;

/**
 * Building model.
 * @author karnokd
 */
public class GameBuilding {
	/** The sample image for the building list and building info screens. */
	public BufferedImage image;
	/** The tile for the undamaged building for the planet renderer. */
	public Tile tile;
	/** The tile for the damaged building for the planet renderer. */
	public Tile tileDamaged;
	/** The building progress value. */
	public int buildProgress;
	/** The building health. */
	public int health;
	/** The identifier. */
	public String id;
	/** The building index. */
	public int index;
	/** The name. */
	public String name;
	/** The assigned race. */
	public GameRace race;
	/** On which surfaces cannot be built. */
	public Set<SurfaceType> notBuildableSurfaces;
	/** The textual description lines. */
	public final String[] description = new String[3];
	/** Build cost. */
	public int cost;
	/** Energy consumption. */
	public int energy;
	/** Worker requirements. */
	public int workers;
	/** The build limit per planet. */
	public int buildLimit;
	/** The tile coordinate. */
	public int x;
	/** The tile coordinate. */
	public int y;
	/** The width in tiles. */
	public int width;
	/** The height in tiles. */
	public int height;
}
