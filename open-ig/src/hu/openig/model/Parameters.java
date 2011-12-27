/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Contains game simulation related parameters.
 * @author akarnokd, 2011.12.20.
 */
public class Parameters {
	/**
	 * @return the multiplier for radar-range in pixels for ground radars
	 */
	public int groundRadarUnitSize() {
		return 35; // DEFAULT: 35
	}
	/**
	 * @return the multiplier for radar-range in pixels for fleet radars
	 */
	public int fleetRadarUnitSize() {
		return 25; // DEFAULT: 25
	}
	/** @return the research speed in terms of money / simulation step. */
	public int researchSpeed() {
		return 80; // DEFAULT: 40
	}
	/** @return the production unit per simulation step. The lower the faster the production is. */
	public int productionUnit() {
		return 25; // DEFAULT: 50
	}
	/** @return the construction points per simulation step. */
	public int constructionSpeed() {
		return 200; // DEFAULT: 200
	}
	/** @return the construction points per simulation step. Not used now. */
	public int constructionCost() {
		return 200; // DEFAULT: 200
	}
	/** @return the construction points per simulation step. */
	public int repairSpeed() {
		return 100; // DEFAULT: 50
	}
	/** @return the construction points per simulation step. */
	public int repairCost() {
		return 20; // DEFAULT: 20
	}
}
