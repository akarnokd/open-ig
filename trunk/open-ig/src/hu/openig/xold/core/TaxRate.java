/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.xold.core;

/**
 * Enumeration for various tax types and percents.
 * @author karnokd
 */
public enum TaxRate {
	/** No tax. */
	NONE(0f, "None"),
	/** Very low tax. */
	VERY_LOW(1f / 7, "VeryLow"),
	/** Low tax. */
	LOW(2f / 7, "Low"),
	/** Moderate tax. */
	MODERATE(3f / 7, "Moderate"),
	/** High tax. */
	HIGH(4f / 7, "High"),
	/** Very high tax. */
	VERY_HIGH(5f / 7, "VeryHigh"),
	/** Demandig tax. */
	DEMANDING(6f / 7, "Demanding"),
	/** Oppressive tax. */
	OPPRESSIVE(1.0f, "Oppressive")
	;
	/** The tax rate 0-1. */
	public final float rate;
	/** The identifier for label lookup. */
	public final String id;
	/**
	 * Constructor.
	 * @param rate the tax rate
	 * @param id the identifier for label lookup
	 */
	TaxRate(float rate, String id) {
		this.rate = rate;
		this.id = id;
	}
}
