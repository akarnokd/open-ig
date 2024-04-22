/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.render;

import hu.openig.core.Pair;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Utility class for common rendering tasks and algorithms.

 * @author akarnokd
 */
public final class RenderTools {
    /** Utility class. */
    private RenderTools() {
    }
    /** The top status bar height constant. */
    public static final int STATUS_BAR_TOP = 20;
    /** The bottom status bar height constant. */
    public static final int STATUS_BAR_BOTTOM = 18;
    /**
     * Set the rectangle (X, Y) so that the rectangle is centered on the screen
     * denoted by the screen width and height.
     * The centering may consider the effect of the top and bottom status bar
     * @param current the current rectangle
     * @param screenWidth the screen width
     * @param screenHeight the screen height
     * @param considerStatusbars consider, that the status bars take 20px top and 18 pixels bottom?
     * @return the updated current rectangle
     */
    public static Rectangle centerScreen(Rectangle current, int screenWidth, int screenHeight,
            boolean considerStatusbars) {
        if (current == null) {
            throw new IllegalArgumentException("current is null");
        }
        if (current.width == 0) {
            throw new IllegalArgumentException("current.width is zero");
        }
        if (current.height == 0) {
            throw new IllegalArgumentException("current.height is zero");
        }
        current.x = (screenWidth - current.width) / 2;
        if (considerStatusbars) {
            current.y = STATUS_BAR_TOP + (screenHeight - STATUS_BAR_TOP - STATUS_BAR_BOTTOM - current.height) / 2;
        } else {
            current.y = (screenHeight - current.height) / 2;
        }

        return current;
    }
    /**
     * Paint a semi-transparent area around the supplied panel.
     * @param panel the panel to paint around
     * @param screenWidth the target screen width
     * @param screenHeight the target screen height
     * @param g2 the target graphics object
     * @param alpha the transparency level
     * @param considerStatusbars consider, that the status bars take 20px top and 18 pixels bottom?
     */
    public static void darkenAround(
            Rectangle panel, int screenWidth, int screenHeight, Graphics2D g2, float alpha,
            boolean considerStatusbars) {
        Composite c = null;
        Paint p = null;
        if (fast) {
            p = g2.getPaint();
            BufferedImage fimg = holes((float)Math.min(1.0, alpha * 2));
            g2.setPaint(new TexturePaint(fimg, new Rectangle(panel.x, panel.y, fimg.getWidth(), fimg.getHeight())));
        } else {
            c = g2.getComposite();
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.setColor(Color.BLACK);

        }
        if (considerStatusbars) {
            fillRectAbsolute(0, STATUS_BAR_TOP, screenWidth - 1, panel.y - 1, g2);
            fillRectAbsolute(0, panel.y + panel.height, screenWidth - 1, screenHeight - 1 - STATUS_BAR_BOTTOM, g2);
        } else {
            fillRectAbsolute(0, 0, screenWidth - 1, panel.y - 1, g2);
            fillRectAbsolute(0, panel.y + panel.height, screenWidth - 1, screenHeight - 1, g2);
        }

        fillRectAbsolute(0, panel.y, panel.x - 1, panel.y + panel.height - 1, g2);
        fillRectAbsolute(panel.x + panel.width, panel.y, screenWidth - 1, panel.y + panel.height - 1, g2);
        if (fast) {
            g2.setPaint(p);
        } else {
            g2.setComposite(c);
        }
    }
    /** Use a faster drawing trick for alpha composition? */
    static boolean fast = false;
    /** The cache for the last alpha blending. */
    static float lastAlpha;
    /** The image to use for the alpha blending. */
    static BufferedImage lastHoles;
    /**
     * Create a transparent image which has holes.
     * @param alpha the effective transparency level
     * @return the image
     */
    static BufferedImage holes(float alpha) {
        if (alpha != lastAlpha) {
            int r = (int)(255 * (1 - alpha));
            int size = 16; // The pattern size
            int[] pixels = new int[size * size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if ((i % 2) == (j % 2)) {
                        pixels[i * size + j] = argb(255, r, r, r);
                    }
                }
            }
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            img.setRGB(0, 0, size, size, pixels, 0, size);
            lastHoles = img;
            lastAlpha = alpha;
            return img;
        }
        return lastHoles;
    }
    /**
     * Create an ARGB color from its components.
     * @param a The alpha channel value
     * @param r the RED component
     * @param g the GREEN component
     * @param b the BLUE component
     * @return the composite color
     */
    public static int argb(int a, int r, int g, int b) {
        return

            ((a << 24) & 0xFF000000)
            | ((r << 16) & 0x00FF0000)
            | ((g << 8) & 0x0000FF00)
            | ((b) & 0x000000FF)
        ;
    }
    /**
     * Fill the rectangle given by absolute coordinates.
     * @param x the left start of the filling inclusive
     * @param y the top start of the filling inclusive
     * @param x2 the right end of the filling inclusive
     * @param y2 the bottom end of the filling inclusive
     * @param g2 the graphics context
     */
    public static void fillRectAbsolute(int x, int y, int x2, int y2, Graphics2D g2) {
        if (x >= x2 || y >= y2) {
            return;
        }
        g2.fillRect(x, y, x2 - x + 1, y2 - y + 1);
    }
    /**
     * Paint a 5x5 ABC grid on the given rectangle by equally distributing its cells.
     * The caller should remember to apply a clipping region if necessary.
     * @param g2 the graphics object
     * @param rect the rectangle to fill in
     * @param gridColor the color of the grid
     * @param text the text renderer to print the labels
     */
    public static void paintGrid(Graphics2D g2, Rectangle rect, Color gridColor, TextRenderer text) {
        g2.setColor(gridColor);
        Stroke st = g2.getStroke();
        //FIXME the dotted line rendering is somehow very slow
//        g2.setStroke(gfx.gridStroke);

        float fw = rect.width;
        float fh = rect.height;
        float dx = fw / 5;
        float dy = fh / 5;
        float y0 = dy;
        float x0 = dx;
        for (int i = 1; i < 5; i++) {
            g2.drawLine((int)(rect.x + x0), rect.y, (int)(rect.x + x0), (int)(rect.y + fh));
            g2.drawLine(rect.x, (int)(rect.y + y0), (int)(rect.x + fw), (int)(rect.y + y0));
            x0 += dx;
            y0 += dy;
        }
        int i = 0;
        y0 = dy - 6;
        x0 = 2;
        for (char c = 'A'; c < 'Z'; c++) {
            text.paintTo(g2, (int)(rect.x + x0), (int)(rect.y + y0), 5, TextRenderer.GRAY, String.valueOf(c));
            x0 += dx;
            i++;
            if (i % 5 == 0) {
                x0 = 2;
                y0 += dy;
            }
        }

        g2.setStroke(st);
    }
    /** Star rendering starting color. */
    private static int startStars = 0x685CA4;
    /** Star rendering end color. */
    private static int endStars = 0xFCFCFC;
    /** Number of stars per layer. */
    private static final int STAR_COUNT = 512;
    /** Number of layers. */
    private static final int STAR_LAYER_COUNT = 4;
    /** A star object. */
    private static final class Star {
        /** The star proportional position. */
        public double x;
        /** The star proportional position. */
        public double y;
        /** The star color. */
        public Color color;
    }
    /** The list of stars. */
    private static List<Star> stars = new ArrayList<>();
    /**
     * Paint the multiple layer of stars.
     * @param g2 the graphics object
     * @param rect the target rectangle
     * @param view the viewport rectangle
     * @param starmapClip the clipping region to avoid even calling a graphics operation outside there for performance reasons
     * @param zoomIndex the current zoom index
     * @param zoomLevelCount the maximum zoom level
     */
    public static void paintStars(Graphics2D g2,

            Rectangle rect, Rectangle view,

            Rectangle starmapClip, int zoomIndex, int zoomLevelCount) {
        int starsize = zoomIndex < zoomLevelCount / 2.5 ? 1 : 2;
        double xf = (view.x - rect.x) * 1.0 / rect.width;
        double yf = (view.y - rect.y) * 1.0 / rect.height;
        Color last = null;
        for (int i = 0; i < stars.size(); i++) {
            Star s = stars.get(i);
            int layerIdx = i / STAR_COUNT;
            double layer = 0.9 - layerIdx * 0.10;
            double w = rect.width * layer;
            double h = rect.height * layer;
            double lx = rect.x;
            double ly = rect.y;

            int x = (int)(lx + xf * (rect.width - w) + s.x * rect.width);
            int y = (int)(ly + yf * (rect.height - h) + s.y * rect.height);
            if (starmapClip.contains(x, y)) {
                if (last != s.color) {
                    g2.setColor(s.color);
                    last = s.color;
                }
                g2.fillRect(x, y, starsize, starsize);
            }
        }
    }
    static {
        precalculateStars();
    }
    /**
     * Precalculates the star background locations and colors.
     */
    private static void precalculateStars() {
        Random random = new Random(0);
        Color[] colors = new Color[8];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = new Color(mixColors(startStars, endStars, random.nextFloat()));
        }
        for (int i = 0; i < STAR_COUNT * STAR_LAYER_COUNT; i++) {
            Star s = new Star();
            s.x = random.nextDouble();
            s.y = random.nextDouble();
            s.color = colors[random.nextInt(colors.length)];
            stars.add(s);
        }
        Collections.sort(stars, new Comparator<Star>() {
            @Override
            public int compare(Star o1, Star o2) {
                int c1 = o1.color.getRGB() & 0xFFFFFF;
                int c2 = o2.color.getRGB() & 0xFFFFFF;
                return c1 - c2;
            }
        });
    }
    /**
     * Mix two colors with a factor.
     * @param c1 the first color
     * @param c2 the second color
     * @param rate the mixing factor
     * @return the mixed color
     */
    public static int mixColors(int c1, int c2, float rate) {
        return
            ((int)((c1 & 0xFF0000) * rate + (c2 & 0xFF0000) * (1 - rate)) & 0xFF0000)
            | ((int)((c1 & 0xFF00) * rate + (c2 & 0xFF00) * (1 - rate)) & 0xFF00)
            | ((int)((c1 & 0xFF) * rate + (c2 & 0xFF) * (1 - rate)) & 0xFF);
    }
    /**
     * Flood-fill the area of the given rectangle with the given image.

     * @param g2 the graphics context
     * @param rect the target rectangle
     * @param image the image to use
     */
    public static void fill(Graphics2D g2, Rectangle rect, BufferedImage image) {
        fill(g2, rect.x, rect.y, rect.width, rect.height, image);
    }
    /**
     * Flood-fill the area of the given rectangle with the given image.

     * @param g2 the graphics context
     * @param x the left coordinate
     * @param y the top coordinate
     * @param width the area width
     * @param height the area height
     * @param image the image to use
     */
    public static void fill(Graphics2D g2, int x, int y, int width, int height, BufferedImage image) {
        Paint save = g2.getPaint();
        TexturePaint tp = new TexturePaint(image, new Rectangle(x, y, image.getWidth(), image.getHeight()));
        g2.setPaint(tp);
        g2.fillRect(x, y, width, height);
        g2.setPaint(save);
    }
    /**
     * Render the target image centered relative to the given rectangle.
     * @param g2 the graphics object
     * @param rect the target rectangle
     * @param image the image to render
     */
    public static void drawCentered(Graphics2D g2, Rectangle rect, BufferedImage image) {
        int dw = (rect.width - image.getWidth()) / 2;
        int dh = (rect.height - image.getHeight()) / 2;
        g2.drawImage(image, rect.x + dw, rect.y + dh, null);
    }
    /**
     * Enable and disable the bilinear interpolation mode for the graphics.
     * @param g2 the target graphics object.
     * @param active activate?
     */
    public static void setInterpolation(Graphics2D g2, boolean active) {
        if (active) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,

                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,

                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        }
    }
    /**
     * Enable and disable the bilinear interpolation mode for the graphics.
     * @param g2 the target graphics object.
     * @param active activate?
     */
    public static void setAntiailas(Graphics2D g2, boolean active) {
        if (active) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,

                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,

                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
    }
    /**
     * Check if a specified point is inside a specified rectangle.
     * @param x0 the upper left coordinate
     * @param y0 the upper left coordinate
     * @param x1 the lower right coordinate (exclusive)
     * @param y1 the lower right coordinate (exclusive)
     * @param x the point to check
     * @param y the point to check
     * @return                True if the point is inside the rectangle,
     *                        false otherwise.
     */
    public static boolean isPointInsideRectangle(double x0, double y0, double x1, double y1,
            double x, double y) {
        return x >= x0 && x < x1 && y >= y0 && y < y1;
    }

    /**
     * Return true if c is between a and b.
     * @param a range value (inclusive)
     * @param b range value (inclusive)
     * @param c the value to test
     * @return true if between
     */
    private static boolean isBetween(double a, double b, double c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
    /**
     * Check if two points are on the same side of a given line.
     * Algorithm from Sedgewick page 350.
     *
     * @param x0 the line start X
     * @param y0 the line start Y
     * @param x1 the line end X
     * @param y1 the line end Y
     * @param px0 the first point X
     * @param py0 the first point Y
     * @param px1 the second point X
     * @param py1 the second point Y
     * @return                <0 if points on opposite sides.
     *                        =0 if one of the points is exactly on the line
     *                        >0 if points on same side.
     */
    private static int sameSide(double x0, double y0, double x1, double y1,
            double px0, double py0, double px1, double py1)    {
        int  sameSide = 0;

        double dx  = x1  - x0;
        double dy  = y1  - y0;
        double dx1 = px0 - x0;
        double dy1 = py0 - y0;
        double dx2 = px1 - x1;
        double dy2 = py1 - y1;

        // Cross product of the vector from the endpoint of the line to the point
        double c1 = dx * dy1 - dy * dx1;
        double c2 = dx * dy2 - dy * dx2;

        if (c1 != 0 && c2 != 0) {
            sameSide = c1 < 0 != c2 < 0 ? -1 : 1;
        } else

        if (dx == 0 && dx1 == 0 && dx2 == 0) {
            sameSide = !isBetween(y0, y1, py0) && !isBetween(y0, y1, py1) ? 1 : 0;
        } else

        if (dy == 0 && dy1 == 0 && dy2 == 0) {
            sameSide = !isBetween(x0, x1, px0) && !isBetween(x0, x1, px1) ? 1 : 0;
        }
        return sameSide;
    }
    /**
     * Check if two line segments intersects.
     *
     * @param x0 the first line start X
     * @param y0 the first line start Y
     * @param x1 the first line end X
     * @param y1 the first line end Y
     * @param x2 the second line start X
     * @param y2 the second line start Y
     * @param x3 the second line end X
     * @param y3 the second line end Y
     * @return True if the two lines intersects.
     */
    public static boolean isLineIntersectingLine(double x0, double y0, double x1, double y1,
            double x2, double y2, double x3, double y3) {
        int s1 = sameSide(x0, y0, x1, y1, x2, y2, x3, y3);
        int s2 = sameSide(x2, y2, x3, y3, x0, y0, x1, y1);

        return s1 <= 0 && s2 <= 0;
    }
    /**
     * Check if a specified line intersects a specified rectangle.
     * @param lx0 the line start X
     * @param ly0 the line start Y
     * @param lx1 the line end X
     * @param ly1 the line end Y
     * @param x0 the rectangle left
     * @param y0 the rectangle top
     * @param x1 the rectangle right (excluded)
     * @param y1 the rectangle bottom (excluded)
     * @return                True if the line intersects the rectangle,
     *                        false otherwise.
     */
    public static boolean isLineIntersectingRectangle(double lx0, double ly0,
            double lx1, double ly1, double x0, double y0, double x1, double y1)    {
        // Is one of the line endpoints inside the rectangle
        if (isPointInsideRectangle(x0, y0, x1, y1, lx0, ly0)

                || isPointInsideRectangle(x0, y0, x1, y1, lx1, ly1)) {
            return true;
        }
        // If it intersects it goes through. Need to check three sides only.

        // Check against top rectangle line
        if (isLineIntersectingLine(lx0, ly0, lx1, ly1,
                x0, y0, x1, y0)) {
            return true;
        }
        // Check against left rectangle line
        if (isLineIntersectingLine(lx0, ly0, lx1, ly1,
                x0, y0, x0, y1)) {
            return true;
        }
        // Check against bottom rectangle line
        return isLineIntersectingLine(lx0, ly0, lx1, ly1,
                x0, y1, x1, y1);
    }
    /**
     * Compute the scaling factor and the top-left point where the rendering should scale from
     * in order to keep the content fit the enclosing window.

     * @param windowWidth the window width
     * @param windowHeight the window height
     * @param contentWidth the content width
     * @param contentHeight the content height
     * @return the origin and scale
     */
    public static Pair<Point, Double> fitWindow(int windowWidth, int windowHeight, int contentWidth, int contentHeight) {
        double sx = windowWidth * 1.0 / contentWidth;
        double sy = windowHeight * 1.0 / contentHeight;
        double scalex = Math.min(sx, sy);
        double scaley = scalex;

        return Pair.of(new Point(
                (int)(windowWidth - contentWidth * scalex) / 2,

                (int)(windowHeight - contentHeight * scaley) / 2
                ), scalex);
    }
}
