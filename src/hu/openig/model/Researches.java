/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The map of researches.
 * @author akarnokd, Oct 19, 2011
 */
public class Researches {
	/** The list of available researches. */
	public final Map<String, ResearchType> researches = new HashMap<>();
	/** 
	 * @return the researches map
	 */
	public Map<String, ResearchType> map() {
		return researches;
	}
	/**
	 * Clears the researches.
	 */
	public void clear() {
		researches.clear();
	}
	/**
	 * Retrieve a research.
	 * @param id the research id
	 * @return the research
	 */
	public ResearchType get(String id) {
		return researches.get(id);
	}
	/**
	 * Set a new research object with the given ID and return the previous entry.
	 * @param id the id
	 * @param p the research
	 * @return the previous research or null
	 */
	public ResearchType put(String id, ResearchType p) {
		return researches.put(id, p);
	}
	/**
	 * @return The researches as collection.
	 */
	public Collection<ResearchType> values() {
		return researches.values();
	}
}
