/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import hu.openig.model.PlanetSurface;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * The planet prototype.
 * @author akarnokd, 2010.01.07.
 */
public class PlanetType {
	/** The planet type. */
	public String type;
	/** The label for this planet type. */
	public String label;
	/** The body animation phases. */
	public BufferedImage[] body;
	/** The available tileset. */
	public Map<Integer, Tile> tiles = new HashMap<Integer, Tile>();
	/** The surface map variations. */
	public Map<Integer, PlanetSurface> surfaces = new HashMap<Integer, PlanetSurface>();
	/** The pattern with a single %d element to resolve the surface definitions. */
	public String pattern;
	/** The start index of the preset surface models. */
	public int start;
	/** The end index of the preset surface models. */
	public int end;
}
