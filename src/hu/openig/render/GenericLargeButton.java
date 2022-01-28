/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.render;

import hu.openig.utils.Exceptions;
import hu.openig.utils.ImageUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/** Button image's zones. */
public class GenericLargeButton implements GenericButtonRenderer {
    /** The top-left corner image. */
    public BufferedImage topLeft;
    /** The top-center filler image. */
    public BufferedImage topCenter;
    /** The top-right corner image. */
    public BufferedImage topRight;
    /** The left-middle filler image. */
    public BufferedImage leftMiddle;
    /** The right-middle filler image. */
    public BufferedImage rightMiddle;
    /** The bottom-left corner image. */
    public BufferedImage bottomLeft;
    /** The bottom-center filler image. */
    public BufferedImage bottomCenter;
    /** The bottom-right corner image. */
    public BufferedImage bottomRight;
    /**
     * Load and split the given base button image.
     * The origina is expected to be 103x28 image.
     * @param name the resource name
     */
    public GenericLargeButton(String name) {
        try (InputStream in = GenericLargeButton.class.getResource(name).openStream()) {
            BufferedImage img = ImageIO.read(in);

            topLeft = ImageUtils.newSubimage(img, 0, 0, 40, 7);
            bottomLeft = ImageUtils.newSubimage(img, 0, 32, 40, 7);
            topRight = ImageUtils.newSubimage(img, 62, 0, 40, 7);
            bottomRight = ImageUtils.newSubimage(img, 62, 32, 40, 7);
            topCenter = ImageUtils.newSubimage(img, 39, 0, 1, 7);
            bottomCenter = ImageUtils.newSubimage(img, 40, 32, 1, 7);
            leftMiddle = ImageUtils.newSubimage(img, 0, 8, 5, 1);
            rightMiddle = ImageUtils.newSubimage(img, 98, 7, 4, 1);

        } catch (IOException ex) {
            Exceptions.add(ex);
        }
    }
    @Override
    public void paintTo(Graphics2D g2, int x, int y,

            int width, int height, boolean down, String text) {
        Color textColor = g2.getColor();
        g2.drawImage(topLeft, x, y, null);
        g2.drawImage(topRight, x + width - topRight.getWidth(), y, null);
        g2.drawImage(bottomLeft, x, y + height - bottomLeft.getHeight(), null);
        g2.drawImage(bottomRight, x + width - bottomRight.getWidth(), y + height - bottomRight.getHeight(), null);

        Paint save = g2.getPaint();

        g2.setPaint(new TexturePaint(topCenter, new Rectangle(x, y, topCenter.getWidth(), topCenter.getHeight())));
        g2.fillRect(x + topLeft.getWidth(), y, width - topLeft.getWidth() - topRight.getWidth(), topCenter.getHeight());

        g2.setPaint(new TexturePaint(bottomCenter, new Rectangle(x, y + height - bottomLeft.getHeight(), bottomCenter.getWidth(), bottomCenter.getHeight())));
        g2.fillRect(x + bottomLeft.getWidth(), y + height - bottomLeft.getHeight(),

                width - bottomLeft.getWidth() - bottomRight.getWidth(), bottomCenter.getHeight());

        g2.setPaint(new TexturePaint(leftMiddle, new Rectangle(x, y + topLeft.getHeight(), leftMiddle.getWidth(), leftMiddle.getHeight())));
        g2.fillRect(x, y + topLeft.getHeight(), leftMiddle.getWidth(), height - topLeft.getHeight() - bottomLeft.getHeight());

        g2.setPaint(new TexturePaint(rightMiddle, new Rectangle(x + width - rightMiddle.getWidth(), y + topLeft.getHeight(), rightMiddle.getWidth(), rightMiddle.getHeight())));
        g2.fillRect(x + width - rightMiddle.getWidth(), y + topRight.getHeight(), rightMiddle.getWidth(), height - topRight.getHeight() - bottomRight.getHeight());

        if (down) {
            GradientPaint grad = new GradientPaint(
                    new Point(x + 5, y + 7),
                    new Color(0xFF7a3c5d),
                    new Point(x + 5, y + height - 14),
                    new Color(0xFF4f6cb5)
            );
            g2.setPaint(grad);
            g2.fillRect(x + 5, y + 7, width - 9, height - 14);
        } else {
            GradientPaint grad = new GradientPaint(
                    new Point(x + 3, y + 6),
                    new Color(0xFF7a3c5d),
                    new Point(x + 3, y + height - 12),
                    new Color(0xFF4f6cb5)
            );
            g2.setPaint(grad);
            g2.fillRect(x + 3, y + 6, width - 5, height - 12);
        }

        g2.setPaint(save);

        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int th = fm.getHeight();

        int tx = x + (width - tw) / 2;
        int ty = y + (height - th) / 2;

        g2.setColor(new Color(0x509090));
        g2.drawString(text, tx + 1, ty + 1 + fm.getAscent());
        g2.setColor(textColor);
        g2.drawString(text, tx, ty + fm.getAscent());

        if (down) {
            g2.setColor(new Color(0, 0, 0, 92));
            g2.fillRect(x + 3, y + 3, width - 5, 4);
            g2.fillRect(x + 3, y + 7, 4, height - 9);
        }
    }
    @Override
    public Dimension getPreferredSize(FontMetrics fm, String text) {
        int tw = fm.stringWidth(text);
        int th = fm.getHeight();
        int width = Math.max(102, tw + 12);
        int height = Math.max(39, th + 12);
        return new Dimension(width, height);
    }
}
