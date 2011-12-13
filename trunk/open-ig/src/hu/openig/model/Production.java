/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * A concrete in-progress production.
 * @author akarnokd, 2010.01.07.
 */
public class Production {
	/** The research type. */
	public ResearchType type;
	/** The number of items to produce. */
	public int count;
	/** The progress into the current item. */
	public int progress;
	/** The priority value. */
	public int priority;
	/**
	 * Create a copy of this production status.
	 * @return the copy
	 */
	public Production copy() {
		Production result = new Production();
		result.type = type;
		result.count = count;
		result.progress = progress;
		result.priority = priority;
		return result;
	}
}
