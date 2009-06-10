/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Contains production progress related information.
 * @author karnokd, 2009.06.10.
 * @version $Revision 1.0$
 */
public class ProductionProgress {
	/** The technology in production. */
	public ResearchTech tech;
	/** The logical priority of the production. */
	public int priority;
	/** The assigned production capacity. */
	public int capacity;
	/** The assigned production capacity in percents. */
	public int capacityPercent;
	/** The number of items to produce. */
	public int count;
	/** The progress within the current item. */
	public int progress;
	/** The total money spent on current production. */
	public int spent;
	/**
	 * @return the total cost of the count items
	 */
	public int getCost() {
		return tech.buildCost * count;
	}
}
