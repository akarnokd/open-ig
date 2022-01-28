/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.ui;

import hu.openig.render.RenderTools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Fill the component area with a start - fill - end image sequence.
 * @author akarnokd, 2011.03.27
 */
public class UIImageFill extends UIComponent {
    /** The start image. */
    private BufferedImage start;
    /** The filler image.*/
    private BufferedImage fill;
    /** The end image. */
    private BufferedImage end;
    /** The start-end should be drawn horizontally? */
    private boolean horizontal;
    /**
     * Constructor. Sets the images.
     * @param start the optional start image
     * @param fill the filler image
     * @param end the optional end image
     * @param horizontal the fill should be horizontal
     */
    public UIImageFill(BufferedImage start, BufferedImage fill,

            BufferedImage end, boolean horizontal) {
        if (fill == null) {
            throw new IllegalArgumentException("parameter fill must be non-null");
        }
        this.start = start;
        this.fill = fill;
        this.end = end;
        this.horizontal = horizontal;
    }
    @Override
    public void draw(Graphics2D g2) {
        int x0 = 0;
        int y0 = 0;
        int w0 = width;
        int h0 = height;
        if (horizontal) {
            if (start != null) {
                x0 += start.getWidth();
                w0 -= start.getWidth();
                g2.drawImage(start, 0, 0, null);
            }
            if (end != null) {
                w0 -= end.getWidth();
                g2.drawImage(end, x0 + w0, 0, null);
            }
        } else {
            if (start != null) {
                y0 += start.getHeight();
                h0 -= start.getHeight();
                g2.drawImage(start, 0, 0, null);
            }
            if (end != null) {
                h0 -= end.getHeight();
                g2.drawImage(end, 0, y0 + h0, null);
            }
        }

        if (w0 > 0 && h0 > 0) {
            RenderTools.fill(g2, x0, y0, w0, h0, fill);
        }
    }
}
