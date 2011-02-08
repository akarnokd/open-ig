/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.core;

/**
 * Describes popularity regions and names.
 * Popularity index is a 0-1 floating number.
 * @author karnokd
 */
public enum PopularityType {
	/** People are actually in revolution. */
	REVOLUTION("Revolution", 0, 0.05f),
	/** People are revolting. */
	REVOLTIG("Revolting", 0.05f, 0.1f),
	/** People hate you. */
	HATE("Hate", 0.1f, 0.25f),
	/** People dislike you. */
	DISLIKE("Dislike", 0.25f, 0.45f),
	/** People are neutral. */
	NEUTRAL("Neutral", 0.45f, 0.55f),
	/** People like you. */
	LIKE("Like", 0.55f, 0.65f),
	/** People are happy with you. */
	HAPPY("Happy", 0.65f, 0.80f),
	/** People simpatize with you. */
	SIMPATIZE("Simpatize", 0.80f, 0.90f),
	/** People support you. */
	SUPPORT("Support", 0.9f, 1.01f)
	;
	/** The popularity id for label lookup. */
	public final String id;
	/** The popularity index start. */
	public final float start;
	/** The popularity index end. */
	public final float end;
	/**
	 * Constructor. Sets the private fields.
	 * @param id the popularity id for label lookup
	 * @param start the start index
	 * @param end the end index
	 */
	PopularityType(String id, float start, float end) {
		this.id = id;
		this.start = start;
		this.end = end;
	}
	/** 
	 * Find a popularity enum value containing the given popularity value.
	 * @param popularity the popularity index (0.0-1.0)
	 * @return the popularity enum
	 */
	public static PopularityType find(float popularity) {
		for (PopularityType value : values()) {
			if (value.start >= popularity && value.end > popularity) {
				return value;
			}
		}
		throw new AssertionError("Popularty not found for " + popularity);
	}
}
