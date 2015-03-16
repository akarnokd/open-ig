/*
 * Copyright 2008-2014, David Karnok 
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
	NONE,
	/** Very low. */
	VERY_LOW,
	/** Low. */
	LOW,
	/** Moderate. */
	MODERATE,
	/** Above moderate. */
	ABOVE_MODERATE,
	/** High.*/
	HIGH,
	/** Very hight. */
	VERY_HIGH,
	/** Oppressive. */
	OPPRESSIVE,
	/** Exploiter. */
	EXPLOITER,
	/** Slavery. */
	SLAVERY
	;
	/** The taxation percent. */
	public final int percent;
	/**
	 * Constructor.
	 */
	TaxLevel() {
		this.percent = 100 * ordinal() / 9;
	}
}
