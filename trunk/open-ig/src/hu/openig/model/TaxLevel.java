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
	VERY_LOW(12),
	/** 25%. */
	LOW(25),
	/** 40%. */
	NORMAL(37),
	/** 55%. */
	ABOVE_NORMAL(50),
	/** 70%. */
	HIGH(62),
	/** 85%. */
	VERY_HIGH(75),
	/** 100%. */
	OPPRESSIVE(87),
	/** 100%. */
	ULTIMATE(100)
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
