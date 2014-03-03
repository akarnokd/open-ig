/*
 * Copyright 2008-2014, David Karnok 
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
		result.assign(this);
		return result;
	}
	/**
	 * Assign from another production object.
	 * @param other the other production
	 */
	public void assign(Production other) {
		type = other.type;
		count = other.count;
		progress = other.progress;
		priority = other.priority;
	}
	/**
	 * Creates a production status object.
	 * @return the production status object
	 */
	public ProductionStatus toProductionStatus() {
		ProductionStatus result = new ProductionStatus();
		result.type = type.id;
		result.count = count;
		result.progress = progress;
		result.priority = priority;
		return result;
	}
	/**
	 * Loads the values from the given production status object.
	 * @param ps the productions status object
	 * @param lookup the model element lookup
	 */
	public void fromProductionStatus(ProductionStatus ps, ModelLookup lookup) {
		type = lookup.research(ps.type);
		count = ps.count;
		progress = ps.progress;
		priority = ps.priority;
	}
}
