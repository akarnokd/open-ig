/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;

/** The surface cell image. */
public class SurfaceCell {
    /** The tile target. */
    public int a;
    /** The tile target. */
    public int b;
    /** The image to render. */
    public BufferedImage image;
    /** The Y coordinate compensation. */
    public int yCompensation;
}
