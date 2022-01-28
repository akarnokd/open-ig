/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import java.awt.image.BufferedImage;

/** The tileset for complete buildings. */
public class TileSet {
    /** The normal building tile. */
    public Tile normal;
    /** The no-light version of the tile. */
    public Tile nolight;
    /** The damaged building tile. */
    public Tile damaged;
    /** The preview image. */
    public BufferedImage preview;
}
