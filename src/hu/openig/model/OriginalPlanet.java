/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
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
    public final List<OriginalBuilding> buildings = new ArrayList<>();
    /** @return Create the map file name from the type and variants */
    public String getMapName() {
        switch (surfaceType) {
        case "Desert":
            return "map_a" + surfaceVariant;
        case "Neptoplasm":
            return "map_g" + surfaceVariant;
        case "Earth":
            return "map_f" + surfaceVariant;
        case "Rocky":
            return "map_d" + surfaceVariant;
        case "Cratered":
            return "map_c" + surfaceVariant;
        case "Frozen":
            return "map_b" + surfaceVariant;
        case "Liquid":
            return "map_e" + surfaceVariant;
        default:
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
    /** The default ordering of the original planets. */
    public static final Comparator<OriginalPlanet> DEFAULT_ORDER = new Comparator<OriginalPlanet>() {
        @Override
        public int compare(OriginalPlanet o1, OriginalPlanet o2) {
            int c = o1.surfaceType.compareTo(o2.surfaceType);
            if (c == 0) {
                c = o1.surfaceVariant < o2.surfaceVariant ? -1 : (o1.surfaceVariant > o2.surfaceVariant ? 1 : 0);
            }
            return c;
        }
    };
}
