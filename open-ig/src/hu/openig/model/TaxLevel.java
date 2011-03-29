/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The taxation level.
 * @author akarnokd, Mar 29, 2011
 */
public enum TaxLevel {
	/** None. */
	NONE(0),
	/** 10%. */
	VERY_LOW(10),
	/** 25%. */
	LOW(25),
	/** 40%. */
	NORMAL(40),
	/** 55%. */
	ABOVE_NORMAL(55),
	/** 70%. */
	HIGH(70),
	/** 85%. */
	VERY_HIGH(85),
	/** 100%. */
	OPPRESSIVE(100)
	;
	/** The taxation percent. */
	public final int percent;
	/**
	 * Constructor.
	 * @param percent the taxation percent.
	 */
	TaxLevel(int percent) {
		this.percent = percent;
	}
}
