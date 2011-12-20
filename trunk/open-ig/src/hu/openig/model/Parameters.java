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
		return 35;
	}
	/**
	 * @return the multiplier for radar-range in pixels for fleet radars
	 */
	public int fleetRadarUnitSize() {
		return 25;
	}
}
