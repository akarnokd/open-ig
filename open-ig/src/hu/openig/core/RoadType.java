/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * The road tile directional type.
 * @author karnokd, 2009.05.21.
 * @version $Revision 1.0$
 */
public enum RoadType {
	/** X axis linear road. _ */
	HORIZONTAL(1),
	/** Top-right corner. |_ */
	TOP_TO_RIGHT(2),
	/** Top to left corner. _| */
	TOP_TO_LEFT(3),
	/** Left to bottom corner. ^^| */
	LEFT_TO_BOTTOM(4),
	/** Right to borrom. |^^*/
	RIGHT_TO_BOTTOM(5),
	/** Vertical. | */
	VERTICAL(6),
	/** Horizontal bottom. -,- */
	HORIZONTAL_BOTTOM(7),
	/** Vertical to right. |- */
	VERTICAL_RIGHT(8),
	/** Horizontal top. -'- */
	HORIZONAL_TOP(9),
	/** Vertical left. -| */
	VERTICAL_LEFT(10),
	/** Cross. -|- */
	CROSS(11)
	;
	/** The road type index. */
	public final int index;
	/**
	 * Constructor.
	 * @param index the road type index.
	 */
	RoadType(int index) {
		this.index = index;
	}
}
