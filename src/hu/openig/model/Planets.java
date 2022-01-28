/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The map of planets.
 * @author akarnokd, Oct 19, 2011
 */
public class Planets {
    /** All planets on the starmap. */
    public final Map<String, Planet> planets = new LinkedHashMap<>();
    /**
     * Retrieve a planets.
     * @param id the planet id
     * @return the planet
     */
    public Planet get(String id) {
        return planets.get(id);
    }
    /**
     * Set a new planet object with the given ID and return the previous entry.
     * @param id the id
     * @param p the planet
     * @return the previous planet or null
     */
    public Planet put(String id, Planet p) {
        return planets.put(id, p);
    }
    /**

     * @return the planet map
     */
    public Map<String, Planet> map() {
        return planets;
    }
    /**
     * Clears the planets.
     */
    public void clear() {
        planets.clear();
    }
    /**
     * @return The planets as collection.
     */
    public Collection<Planet> values() {
        return planets.values();
    }
    /**
     * @return the set of planet ids
     */
    public Set<String> keySet() {
        return planets.keySet();
    }
}
