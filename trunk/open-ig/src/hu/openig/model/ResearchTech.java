/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a research technology.
 * @author karnokd
 *
 */
public class ResearchTech {
	/** The research identifier. */
	public String id;
	/**
	 * The technology level for this technology. If the player level is below this value, 
	 * the research won't even show up in the dialog. 
	 */
	public int level;
	/** The technologycal prerequisites. */
	public final Set<ResearchTech> prerequisites = new HashSet<ResearchTech>();
}
