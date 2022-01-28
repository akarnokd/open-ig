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
public class GenericMediumButton implements GenericButtonRenderer {
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
     * The original is expected to be 103x28 image.
     * @param name the resource name
     */
    public GenericMediumButton(String name) {
        try (InputStream in = GenericMediumButton.class.getResource(name).openStream()) {
            BufferedImage img = ImageIO.read(in);

            topLeft = ImageUtils.newSubimage(img, 0, 0, 10, 6);
            bottomLeft = ImageUtils.newSubimage(img, 0, 22, 10, 6);
            topRight = ImageUtils.newSubimage(img, 63 + 30, 0, 10, 6);
            bottomRight = ImageUtils.newSubimage(img, 63 + 30, 22, 10, 6);
            topCenter = ImageUtils.newSubimage(img, 39, 0, 1, 6);
            bottomCenter = ImageUtils.newSubimage(img, 40, 22, 1, 6);
            leftMiddle = ImageUtils.newSubimage(img, 0, 7, 4, 1);
            rightMiddle = ImageUtils.newSubimage(img, 99, 7, 4, 1);
        } catch (IOException ex) {
            Exceptions.add(ex);
        }
    }
    @Override
    public void paintTo(Graphics2D g1, int x, int y,

            int width, int height, boolean down, String text) {

        BufferedImage lookCache = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = lookCache.createGraphics();

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

        int dx = down ? 0 : -1;

        GradientPaint grad = new GradientPaint(
                new Point(x + leftMiddle.getWidth(), y + topLeft.getHeight() + dx),
                new Color(0xFF4C7098),
                new Point(x + leftMiddle.getWidth(), y + height - bottomLeft.getHeight()),
                new Color(0xFF805B71)
        );
        g2.setPaint(grad);
        g2.fillRect(x + leftMiddle.getWidth() + dx, y + topLeft.getHeight() + dx, width - leftMiddle.getWidth() - rightMiddle.getWidth() - 2 * dx, height - topCenter.getHeight() - bottomCenter.getHeight() - dx);
        g2.setPaint(save);
        g2.dispose();
        g1.drawImage(lookCache, x, y, null);

        FontMetrics fm = g1.getFontMetrics();
        int tw = fm.stringWidth(text);
        int th = fm.getHeight();

        int tx = x + (width - tw) / 2 + dx;
        int ty = y + (height - th) / 2 + dx;

        Color textColor = g1.getColor();
        g1.setColor(new Color(0x509090));
        g1.drawString(text, tx + 1, ty + 1 + fm.getAscent());
        g1.setColor(textColor);
        g1.drawString(text, tx, ty + fm.getAscent());

        if (down) {
            g1.setColor(new Color(0, 0, 0, 92));
            g1.fillRect(x + 3, y + 3, width - 5, 4);
            g1.fillRect(x + 3, y + 7, 4, height - 9);
        }
    }
    @Override
    public Dimension getPreferredSize(FontMetrics fm, String text) {
        int tw = fm.stringWidth(text);
        int th = fm.getHeight();
        int width = Math.max(103 - 60, tw + 12);
        int height = Math.max(28, th + 12);
        return new Dimension(width, height);
    }
}
