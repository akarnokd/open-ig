/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer.model;

import hu.openig.model.Research;
import hu.openig.model.ResearchType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The available and in-progress
 * research status record.
 * @author akarnokd, 2013.04.27.
 */
public class ResearchStatus {
	/** Indicates that the research is globally paused. */
	public boolean paused;
	/** The current running research. */
	public ResearchType runningResearch;
	/** The completed research. */
	public final Map<ResearchType, List<ResearchType>> availableResearch = new LinkedHashMap<ResearchType, List<ResearchType>>();
	/** The in-progress research. */
	public final List<Research> research = new ArrayList<Research>();
}
