/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;


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
	/** The equipment image. */
	public BufferedImage equipment;
	/** The spacewar image. */
	public BufferedImage spacewar;
	/** The available tileset. */
	public Map<Integer, Tile> tiles = new HashMap<>();
	/** The surface map variations. */
	public final Map<Integer, PlanetSurface> surfaces = new HashMap<>();
	/** The pattern with a single %d element to resolve the surface definitions. */
	public String pattern;
	/** The start index of the preset surface models. */
	public int start;
	/** The end index of the preset surface models. */
	public int end;
	/** The weather frequency in minutes. */
	public int weatherFrequency = 3 * 24 * 60;
	/** The weather duration in minutes. */
	public int weatherDuration = 2 * 60;
	/** The drop type or null if no weather is on this kind of planet. */
	public WeatherType weatherDrop;
}
