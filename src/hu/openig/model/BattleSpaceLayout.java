/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * A spacewar layout.
 * @author akarnokd, 2011.08.31.
 */
public class BattleSpaceLayout {
	/** The image to display. */
	public BufferedImage image;
	/** The fighter color on the image. */
	public static final int FIGHTER_COLOR = 0xFF00F300;
	/** The medium and large ship color on the image. */
	public static final int SHIP_COLOR = 0xFFFFBE00;
	/** The map from location to fighter (true) or ship (false). */
	public final Map<Location, Boolean> map = new HashMap<>();
    /**
     * Constructor, initializes the image but not the map.
     * @param image the layout image to use
     */
    public BattleSpaceLayout(BufferedImage image) {
        this.image = Objects.requireNonNull(image);
    }
	/**
	 * Scan the image and build the layout map.
	 */
	public void parse() {
		map.clear();
		Set<Location> ignore = new HashSet<>();
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				Location loc = Location.of(x, y);
				if (ignore.add(loc)) {
					int c = image.getRGB(x, y);
					if (c == FIGHTER_COLOR) {
						map.put(loc, true);
						ignore.add(Location.of(x + 1, y));
						ignore.add(Location.of(x + 1, y + 1));
						ignore.add(Location.of(x, y + 1));
					} else
					if (c == SHIP_COLOR) {
						map.put(Location.of(x, y), false);
						ignore.add(Location.of(x + 1, y));
						ignore.add(Location.of(x + 1, y + 1));
						ignore.add(Location.of(x, y + 1));
					}
				}
			}
		}
	}
	/**
	 * Return an ordered list by descending X and ascending Y locations.
	 * @return the order
	 */
	public List<Map.Entry<Location, Boolean>> order() {
		List<Map.Entry<Location, Boolean>> result = new ArrayList<>(map.entrySet());
		
		double gx = 0;
		double gy = 0;
		for (Location loc : map.keySet()) {
			gx += loc.x + 0.5;
			gy += loc.y + 0.5;
		}
		gx /= map.size();
		gy /= map.size();
		
		final Map<Location, Integer> distances = new HashMap<>();
		final Map<Location, Integer> angles = new HashMap<>();
		for (Location loc : map.keySet()) {
			distances.put(loc, (int)(World.dist(gx, gy, loc.x + 0.5, loc.y + 0.5) * 100));
			angles.put(loc, (int)Math.abs((Math.atan2(loc.y - gy + 0.5, loc.x - gx + 0.5) * 180)));
		}
		Collections.sort(result, new Comparator<Map.Entry<Location, Boolean>>() {
			@Override
			public int compare(Entry<Location, Boolean> o1,
					Entry<Location, Boolean> o2) {
				int dist1 = distances.get(o1.getKey());
				int dist2 = distances.get(o2.getKey());
				if (dist1 < dist2) {
					return -1;
				} else
				if (dist1 > dist2) {
					return 1;
				} else {
					int angle1 = angles.get(o1.getKey());
					int angle2 = angles.get(o2.getKey());
					if (angle1 < angle2) {
						return -1;
					} else
					if (angle1 > angle2) {
						return 1;
					}
					return 0;
				}
			}
		});
		
		return result;
	}
	/** @return the layout total width. */
	public int getWidth() {
		return image.getWidth();
	}
	/** @return the layout total height. */
	public int getHeight() {
		return image.getHeight();
	}
}
