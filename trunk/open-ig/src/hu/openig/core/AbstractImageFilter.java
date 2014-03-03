/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

/**
 * Base class to perform filtering operations on buffered images.
 * @author akarnokd, 2012.06.07.
 */
public abstract class AbstractImageFilter implements BufferedImageOp {

	@Override
	public Rectangle2D getBounds2D(BufferedImage src) {
		return new Rectangle(0, 0, src.getWidth(), src.getHeight());
	}

	@Override
	public BufferedImage createCompatibleDestImage(BufferedImage src,
			ColorModel destCM) {
		if (destCM == null) {
			destCM = src.getColorModel();
		}
		return new BufferedImage(destCM, destCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()),
				destCM.isAlphaPremultiplied(), null);
	}

	@Override
	public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
		return (Point2D)srcPt.clone();
	}

	@Override
	public RenderingHints getRenderingHints() {
		return null;
	}

}
