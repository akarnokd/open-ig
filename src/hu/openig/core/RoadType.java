/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.util.HashMap;
import java.util.Map;

/**
 * The road tile directional type.
 * @author akarnokd, 2009.05.21.
 */
public enum RoadType {
    /** X axis linear road. _ */
    HORIZONTAL(1, Sides.LEFT | Sides.RIGHT),
    /** Top-right corner. |_ */
    TOP_TO_RIGHT(2, Sides.TOP | Sides.RIGHT),
    /** Top to left corner. _| */
    TOP_TO_LEFT(3, Sides.LEFT | Sides.TOP),
    /** Left to bottom corner. ^^| */
    LEFT_TO_BOTTOM(4, Sides.LEFT | Sides.BOTTOM),
    /** Right to bottom. |^^*/
    RIGHT_TO_BOTTOM(5, Sides.BOTTOM | Sides.RIGHT),
    /** Vertical. | */
    VERTICAL(6, Sides.TOP | Sides.BOTTOM),
    /** Horizontal bottom. -,- */
    HORIZONTAL_BOTTOM(7, Sides.LEFT | Sides.BOTTOM | Sides.RIGHT),
    /** Vertical to right. |- */
    VERTICAL_RIGHT(8, Sides.TOP | Sides.BOTTOM | Sides.RIGHT),
    /** Horizontal top. -'- */
    HORIZONTAL_TOP(9, Sides.LEFT | Sides.TOP | Sides.RIGHT),
    /** Vertical left. -| */
    VERTICAL_LEFT(10, Sides.LEFT | Sides.TOP | Sides.BOTTOM),
    /** Cross. -|- */
    CROSS(11, Sides.LEFT | Sides.TOP | Sides.BOTTOM | Sides.RIGHT)
    ;
    /** The road type index. */
    public final int index;
    /** The road pattern. Combined from the constants below */
    public final int pattern;
    /**
     * Constructor.
     * @param index the road type index.
     * @param pattern the road pattern
     */
    RoadType(int index, int pattern) {
        this.index = index;
        this.pattern = pattern;
    }
    /**
     * The road pattern to road type map.
     */
    private static final Map<Integer, RoadType> MAP;
    /**
     * The road index to road type map.
     */
    private static final Map<Integer, RoadType> INDEX_MAP;
    /** Initialize MAP. */
    static {
        MAP = new HashMap<>();
        INDEX_MAP = new HashMap<>();
        for (RoadType rt : values()) {
            MAP.put(rt.pattern, rt);
            INDEX_MAP.put(rt.index, rt);
        }
    }
    /**
     * Returns the road type belonging to the
     * given pattern.
     * @param pattern the pattern composed of Sides.* constants
     * @return the road type
     */
    public static RoadType get(int pattern) {
        return MAP.get(pattern);
    }
    /**
     * Get a road type by index.
     * @param index the index map
     * @return the road type
     */
    public static RoadType getByIndex(int index) {
        return INDEX_MAP.get(index);
    }
}
