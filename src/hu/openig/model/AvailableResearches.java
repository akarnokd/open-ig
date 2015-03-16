/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the available researches.
 * @author akarnokd, 2013.06.02.
 */
public class AvailableResearches {
	/** The completed research by Id. */
	private final Map<String, List<ResearchType>> mapById = new LinkedHashMap<>();
	/** The completed research by ref. */
	private final Map<ResearchType, List<ResearchType>> map = new LinkedHashMap<>();
	/**
	 * Test if the given research-id is is available.
	 * @param researchId the research id
	 * @return true if available
	 */
	public boolean containsKey(String researchId) {
		return mapById.containsKey(researchId);
	}
	/**
	 * Test if the given technology is available.
	 * @param rt the technology
	 * @return true if available
	 */
	public boolean containsKey(ResearchType rt) {
		return map.containsKey(rt);
	}
	/**
	 * Add a new technology to the available maps.
	 * @param rt the technology
	 * @param others the usable related technologies at the time
	 */
	public void put(ResearchType rt, List<ResearchType> others) {
		mapById.put(rt.id, others);
		map.put(rt, others);
	}
	/**
	 * Returns the related technologies for the technology.
	 * @param rt the technology
	 * @return the list of related technologies
	 */
	public List<ResearchType> get(ResearchType rt) {
		return map.get(rt);
	}
	/**
	 * Returns the related technologies for the research-id.
	 * @param researchId the research-id
	 * @return the list of related technologies
	 */
	public List<ResearchType> get(String researchId) {
		return mapById.get(researchId);
	}
	/**
	 * Returns the available technology map.
	 * @return the available technology map
	 */
	public Map<ResearchType, List<ResearchType>> map() {
		return map;
	}
	/**
	 * Clears all available researches.
	 */
	public void clear() {
		map.clear();
		mapById.clear();
	}
	/**
	 * Removes the given research.
	 * @param researchId the research to remove
	 */
	public void remove(String researchId) {
		mapById.remove(researchId);
		Iterator<ResearchType> it = map.keySet().iterator();
		while (it.hasNext()) {
			ResearchType rt = it.next();
			if (rt.id.equals(researchId)) {
				it.remove();
				break;
			}
		}
	}
	/**
	 * Removes the given research.
	 * @param rt the research to remove
	 */
	public void remove(ResearchType rt) {
		map.remove(rt);
		mapById.remove(rt.id);
	}
}
