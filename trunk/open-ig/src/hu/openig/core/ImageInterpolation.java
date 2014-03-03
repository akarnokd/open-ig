/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.awt.RenderingHints;

/**
 * Enumeration for various starmap interpolation settings.
 * @author akarnokd
 */
public enum ImageInterpolation {
	/** No/default interpolation. */
	NONE(null),
	/** Use nearest neighbor interpolation. */
	NEIGHBOR(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR),
	/** Use bilinear interpolation. */
	BILINEAR(RenderingHints.VALUE_INTERPOLATION_BILINEAR),
	/** Use bicubic interpolation. */
	BICUBIC(RenderingHints.VALUE_INTERPOLATION_BICUBIC)
	;
	/** The rendering hint to submit to the graphics object. */
	public final Object hint;
	/**
	 * Constructor.
	 * @param hint the rendering hint object
	 */
	ImageInterpolation(Object hint) {
		this.hint = hint;
	}
}
