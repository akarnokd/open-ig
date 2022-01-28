/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Skirmish player configuration.
 * @author akarnokd, 2012.08.20.
 */
public class SkirmishPlayer {
    /** The original id. */
    public String originalId;
    /** The display name. */
    public String name;
    /** The description text. */
    public String description;
    /** The color. */
    public int color;
    /** The diplomacy head. */
    public String diplomacyHead;
    /** The icon reference. */
    public String iconRef;
    /** The icon image. */
    public BufferedImage icon;
    /** The race. */
    public String race;
    /**  The AI mode. */
    public SkirmishAIMode ai;
    /** The group. */
    public int group;
    /** No diplomacy. */
    public boolean nodiplomacy;
    /** No database. */
    public boolean nodatabase;
    /** The traits. */
    public final Traits traits = new Traits();
    /** The picture reference. */
    public String picture;
    /** The preferred initial planet id. */
    public String initialPlanet;
    /**
     * @return a copy of this player
     */
    public SkirmishPlayer copy() {
        SkirmishPlayer result = new SkirmishPlayer();

        result.originalId = originalId;
        result.name = name;
        result.description = description;
        result.color = color;
        result.icon = icon;
        result.iconRef = iconRef;
        result.race = race;
        result.ai = ai;
        result.group = group;
        result.nodatabase = nodatabase;
        result.nodiplomacy = nodiplomacy;
        result.diplomacyHead = diplomacyHead;
        result.picture = picture;
        result.initialPlanet = initialPlanet;
        result.traits.replace(traits);

        return result;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ai == null) ? 0 : ai.hashCode());
        result = prime * result + color;
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result
                + ((diplomacyHead == null) ? 0 : diplomacyHead.hashCode());
        result = prime * result + group;
        result = prime * result + ((iconRef == null) ? 0 : iconRef.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nodatabase ? 1231 : 1237);
        result = prime * result + (nodiplomacy ? 1231 : 1237);
        result = prime * result
                + ((originalId == null) ? 0 : originalId.hashCode());
        result = prime * result + ((picture == null) ? 0 : picture.hashCode());
        result = prime * result + ((race == null) ? 0 : race.hashCode());
        result = prime * result + ((traits == null) ? 0 : traits.hashCode());
        result = prime * result + ((initialPlanet == null) ? 0 : initialPlanet.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SkirmishPlayer)) {
            return false;
        }
        SkirmishPlayer other = (SkirmishPlayer) obj;
        if (ai != other.ai) {
            return false;
        }
        if (color != other.color) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (diplomacyHead == null) {
            if (other.diplomacyHead != null) {
                return false;
            }
        } else if (!diplomacyHead.equals(other.diplomacyHead)) {
            return false;
        }
        if (group != other.group) {
            return false;
        }
        if (iconRef == null) {
            if (other.iconRef != null) {
                return false;
            }
        } else if (!iconRef.equals(other.iconRef)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (nodatabase != other.nodatabase) {
            return false;
        }
        if (nodiplomacy != other.nodiplomacy) {
            return false;
        }
        if (originalId == null) {
            if (other.originalId != null) {
                return false;
            }
        } else if (!originalId.equals(other.originalId)) {
            return false;
        }
        if (picture == null) {
            if (other.picture != null) {
                return false;
            }
        } else if (!picture.equals(other.picture)) {
            return false;
        }
        if (race == null) {
            if (other.race != null) {
                return false;
            }
        } else if (!race.equals(other.race)) {
            return false;
        }
        if (traits == null) {
            if (other.traits != null) {
                return false;
            }
        } else if (!traits.equals(other.traits)) {
            return false;
        }
        if (!Objects.equals(initialPlanet, other.initialPlanet)) {
            return false;
        }
        return true;
    }
    /**
     * Compute the icon color of the image.
     * @param bimg the image
     * @return the color
     */
    public int iconColor(BufferedImage bimg) {
        int[] pixels = bimg.getRGB(0, 0, bimg.getWidth(), bimg.getHeight(), null, 0, bimg.getWidth());
        long r = 0;
        long g = 0;
        long b = 0;
        int pxc = 0;
        for (int i : pixels) {
            if ((i & 0xFF000000) != 0) {
                r += ((i & 0xFF0000) >> 16);
                g += ((i & 0xFF00) >> 8);
                b += ((i & 0xFF));
                pxc++;
            }
        }
        r /= pxc;
        g /= pxc;
        b /= pxc;

        return (int)(0xFF000000 | (r << 16) | (g << 8) | (b));
    }

}
