/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * The original planet definition (from v0.72 and before).
 * @author akarnokd
 */
public class OriginalPlanet {
	/** The planet name. */
	public String name;
	/** The surface type. */
	public String surfaceType;
	/** The surface variant. */
	public int surfaceVariant;
	/** The race name. */
	public String race;
	/** The location on the galaxy map. */
	public final Point location = new Point();
	/** The list of buildings. */
	public final List<OriginalBuilding> buildings = new ArrayList<OriginalBuilding>();
	/** @return Create the map file name from the type and variants */
	public String getMapName() {
		if ("Desert".equals(surfaceType)) {
			return "map_a" + surfaceVariant;
		} else
		if ("Neptoplasm".equals(surfaceType)) {
			return "map_g" + surfaceVariant;
		} else
		if ("Earth".equals(surfaceType)) {
			return "map_f" + surfaceVariant;
		} else
		if ("Rocky".equals(surfaceType)) {
			return "map_d" + surfaceVariant;
		} else
		if ("Cratered".equals(surfaceType)) {
			return "map_c" + surfaceVariant;
		} else
		if ("Frozen".equals(surfaceType)) {
			return "map_b" + surfaceVariant;
		} else
		if ("Liquid".equals(surfaceType)) {
			return "map_e" + surfaceVariant;
		}
		return "";
	}
	/** @return the new race name from the old. */
	public String getRaceTechId() {
		return convertRaceTechId(race);
	}
	/**
	 * Convert an original race description to the new technology id.
	 * @param race the original race
	 * @return the new technology
	 */
	public static String convertRaceTechId(String race) {
		if ("Empire".equals(race)) {
			return "human";
		}
		if ("Garthog".equals(race)) {
			return "garthog";
		}
		if ("Morgath".equals(race)) {
			return "morgath";
		}
		if ("Ychom".equals(race)) {
			return "ychom";
		}
		if ("Dribs".equals(race)) {
			return "dribs";
		}
		if ("Sullep".equals(race)) {
			return "sullep";
		}
		if ("Dargslan".equals(race)) {
			return "dargslan";
		}
		if ("Ecalep".equals(race)) {
			return "ecalep";
		}
		if ("FreeTraders".equals(race)) {
			return "human";
		}
		if ("FreeNations".equals(race)) {
			return "human";
		}
		return "";
	}
}
