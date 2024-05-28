/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import hu.openig.core.*;

/**
 * A spacewar layout.
 * @author akarnokd, 2011.08.31.
 */
public class BattleSpaceLayout {
    /** The image to display. */
    public BufferedImage image;
    /** The fighter color on the image. */
    public static final int FIGHTER_COLOR = 0xFF00F300;
    /** The medium ship color on the image. */
    public static final int CRUISER_COLOR = 0xFFFFBE00;
    /** The large ship color on the image. */
    public static final int BATTLESHIP_COLOR = 0xFFFF7F38;
    /** The base map from location to ship type. */
    public final Map<Location, ResearchSubCategory> baseMap = new HashMap<>();
    /** The current working map from location to ship type. */
    public  Map<Location, ResearchSubCategory>  map = new HashMap<>();
    /** The number of locations for fighter placements, needed for calculating fighter groupings. */
    public int fighterLocationsNum;
    /** The current width of the working locaiont map. */
    public int workingWidth;
    /** The current height of the working locaiont map. */
    public int workingHeight;
    /** Is the working location map aligned to the right side. */
    private boolean leftAligned = true;
    /**
     * Constructor, initializes the image but not the map.
     * @param image the layout image to use
     */
    public BattleSpaceLayout(BufferedImage image) {
        this.image = Objects.requireNonNull(image);
    }
    /**
     * Scan the image and build the base layout map.
     */
    public void parse() {
        baseMap.clear();
        fighterLocationsNum = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Location loc = Location.of(x, y);
                int c = image.getRGB(x, y);
                if (c == FIGHTER_COLOR) {
                    baseMap.put(loc, ResearchSubCategory.SPACESHIPS_FIGHTERS);
                    fighterLocationsNum++;
                } else if (c == CRUISER_COLOR) {
                    baseMap.put(Location.of(x, y), ResearchSubCategory.SPACESHIPS_CRUISERS);
                } else if (c == BATTLESHIP_COLOR) {
                    baseMap.put(Location.of(x, y), ResearchSubCategory.SPACESHIPS_BATTLESHIPS);
                }
            }
        }
        workingWidth = image.getWidth();
        workingHeight = image.getHeight();
        map = baseMap;
    }
    /**
     * Reset the current working layout map to the base layout map.
     */
    public void reset() {
        workingWidth = image.getWidth();
        workingHeight = image.getHeight();
        map = baseMap;
        leftAligned = true;
    }
    /**
     * Return an ordered list by descending X and ascending Y locations.
     * @return the order
     */
    public List<Pair<Location, ResearchSubCategory>> order() {
        List<Pair<Location, ResearchSubCategory>> result = new ArrayList<>(map.size());

        for (Map.Entry<Location, ResearchSubCategory> me : map.entrySet()) {
            result.add(Pair.of(me.getKey(), me.getValue()));
        }

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
        Collections.sort(result, new Comparator<Pair<Location, ResearchSubCategory>>() {
            @Override
            public int compare(Pair<Location, ResearchSubCategory> o1,
                    Pair<Location, ResearchSubCategory> o2) {
                int dist1 = distances.get(o1.first);
                int dist2 = distances.get(o2.first);
                if (dist1 < dist2) {
                    return -1;
                } else
                if (dist1 > dist2) {
                    return 1;
                } else {
                    int angle1 = angles.get(o1.first);
                    int angle2 = angles.get(o2.first);
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

    /**
     * Scale the working map locations.
     * The locations are scaled based on the ration of base map height and referenceHeight.
     * @param referenceHeight the height to scale the location to.
     */
    public void scalePositions(int referenceHeight) {
        if (referenceHeight == image.getHeight()) {
            return;
        }
        double ratio = referenceHeight / (float)image.getHeight();
        workingHeight = referenceHeight;
        workingWidth = (int)Math.floor(getWidth() * ratio);
        Map<Location, ResearchSubCategory> newMap = new HashMap<>();
        Point2D centerPoint = new Point2D.Double((getRightmostX()) / 2.0 , (image.getHeight()) / 2.0);
        Point2D newCenterPoint = new Point2D.Double(centerPoint.getX() * ratio , centerPoint.getY() * ratio);
        for (Location loc : map.keySet()) {
            double scaledYpoint = (loc.y - centerPoint.getY()) * ratio + newCenterPoint.getY();
            int newYpoint = (int) (Math.round(scaledYpoint) == Math.round(newCenterPoint.getY()) ? Math.round(scaledYpoint) : (scaledYpoint > newCenterPoint.getY() ? Math.ceil(scaledYpoint) : Math.floor(scaledYpoint)));
            double scaledXpoint = (loc.x - centerPoint.getX()) * ratio + newCenterPoint.getX();
            int newXpoint = (int) (Math.round(scaledXpoint) == Math.round(newCenterPoint.getX()) ? Math.round(scaledXpoint) : (scaledXpoint > newCenterPoint.getX() ? Math.ceil(scaledXpoint) : Math.floor(scaledXpoint)));
            Location newLocation = Location.of(newXpoint, newYpoint);
            newMap.put(newLocation, map.get(loc));
        }
        map = newMap;
    }

    /**
     * Return the X coordinate of the rightmost X locations. Used for aligning the layout map.
     * @return rightmost layout location's X coordinate.
     */
    private int getRightmostX() {
        Location rightmost = Collections.max(map.keySet(), new Comparator<Location>() {
            @Override
            public int compare(Location o1,
                               Location o2) {
                if (o1.x < o2.x) {
                    return -1;
                } else if (o1.x > o2.x) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        return rightmost.x;
    }

    /**
     * Mirror the working map locations using point reflection off the center of the map.
     */
    public void mirrorPositions() {
        int centerX = workingWidth / 2;
        int centerY = workingHeight / 2;
        Map<Location, ResearchSubCategory> newMap = new HashMap<>();
        for (Location loc : map.keySet()) {
            newMap.put(Location.of(2 * centerX - loc.x , 2 * centerY - loc.y), map.get(loc));
        }
        map = newMap;
        int alignmentOffsetX = workingWidth - getRightmostX();
        newMap = new HashMap<>();
        for (Location loc : map.keySet()) {
            newMap.put(Location.of(loc.x + alignmentOffsetX - 2 , loc.y), map.get(loc));
        }
        map = newMap;
        leftAligned = false;
    }

    /**
     * Add an offset to the X coordinate of all layout locations.
     * @param offsetX the X offset to add to all locations
     */
    public void addOffsetToPositions(int offsetX) {
        Map<Location, ResearchSubCategory> newMap = new HashMap<>();
        for (Location loc : map.keySet()) {
            newMap.put(Location.of(loc.x + (leftAligned ? offsetX : -offsetX) , loc.y), map.get(loc));
        }
        map = newMap;
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
