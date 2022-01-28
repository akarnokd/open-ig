/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A tiled object.
 * @author akarnokd, 2010.01.07.
 */
public class Tile0 {
    /** The tile width, points top-right, e.g. in &lt;> the length of the //. */
    public final int width;
    /** The tile height, points bottom-right, e.e. in &lt;> the length of the \\ sides. */
    public final int height;
    /** The entire reference tile image. Can be shared freely between copies. */
    public final int[] image;
    /** The overlay image for lights turned on. Can be shared freely between copies. */
    public final int[] lightMap;
    /** The image width. */
    public final int imageWidth;
    /** The image height. */
    public final int imageHeight;
    /** The current alpha level of the image. */
    public float alpha = 1;
    /** The alpha percent on which the light map should be applied. */
    protected static final float LIGHT_THRESHOLD = 0.65f;
    /** The shared working buffer. Therefore, the alpha adjustments should be done in a single thread! */
    private static ThreadLocal<int[][]> work = new ThreadLocal<int[][]>() {
        @Override
        protected int[][] initialValue() {
            int[][] workarea = new int[1][];
            workarea[0] = new int[512 * 512];
            return workarea;
        }
    };
    /** The strip cache. */
    private final BufferedImage[] stripCache;
    /** The alpha cache for this tile. */
    private final Map<Integer, BufferedImage[]> alphaCache = new HashMap<>();
    /**
     * Constructor. Sets the fields.
     * @param width the width in top-right angle.
     * @param height the height in bottom right angle.
     * @param image the entire tile image.
     * @param lightMap the image for lights turned on
     */
    public Tile0(int width, int height, BufferedImage image, BufferedImage lightMap) {
        this.width = width;
        this.height = height;
        this.imageWidth = image.getWidth();
        this.imageHeight = image.getHeight();
        // use ARGB images for the base
        this.image = new int[this.imageWidth * this.imageHeight];
        image.getRGB(0, 0, this.imageWidth, this.imageHeight, this.image, 0, this.imageWidth);
        if (work.get()[0].length < this.image.length) {
            work.get()[0] = new int[this.image.length];
        }
        stripCache = new BufferedImage[width + height - 1];
        if (lightMap != null) {
            this.lightMap = createLightmapRLE(lightMap);
        } else {
            this.lightMap = null;
        }
        prepareStripCache();
    }
    /**
     * Create a run-length encoded light map from the sparse buffered image.
     * @param lightMap the original buffered image
     * @return the array containing [index,color] pairs subsequently
     */
    private static int[] createLightmapRLE(BufferedImage lightMap) {
        int[] w = work.get()[0];
        lightMap.getRGB(0, 0, lightMap.getWidth(), lightMap.getHeight(), w, 0, lightMap.getWidth());
        int[] result = new int[512];
        int count = 0;
        for (int i = 0; i < lightMap.getWidth() * lightMap.getHeight(); i++) {
            if (w[i] != 0) {
                if (count + 2 >= result.length) {
                    result = Arrays.copyOf(result, result.length * 3 / 2);
                }
                result[count] = i;
                result[count + 1] = w[i];
                count += 2;
            }
        }
        return Arrays.copyOf(result, count);
    }
    /**
     * Copy constructor.
     * @param other the other tile
     */
    protected Tile0(Tile0 other) {
        this.width = other.width;
        this.height = other.height;
        this.image = other.image;
        this.imageWidth = other.imageWidth;
        this.imageHeight = other.imageHeight;
        this.lightMap = other.lightMap;
        this.alpha = other.alpha;
        this.stripCache = new BufferedImage[width + height - 1];
        prepareStripCache();
    }
    /**
     * Create a copy of this tile.
     * @return the tile
     */
    public Tile0 copy() {
        return new Tile0(this);
    }
    /**
     * Get a strip of the cached image.
     * @param stripIndex the strip index. Strips are indexed from left to right
     * @return the partial image
     */
    public BufferedImage getStrip(int stripIndex) {
        int a = (int)((alpha - 0.35f) / 0.7 * 32);
        BufferedImage[] ac = alphaCache.get(a);
        if (ac == null) {
            prepareStripCache();
            computeImageWithLights();
            alphaCache.put(a, stripCache.clone());
            ac = stripCache;
        }
        return ac[stripIndex];
    }
    /**
     * Create an alpha blent image from the reference tile image.
     */
    private void computeImageWithLights() {
        // apply light map if exists?
        applyLightMap();
        // compute strips
        createStrips();
    }
    /**
     * Create the rendering helper strips of the image.
     */
    private void createStrips() {
        int[] w = work.get()[0];
        if (stripCache.length == 1) {
            stripCache[0].setRGB(0, 0, Math.min(57, imageWidth), imageHeight, w, 0, imageWidth);
        } else {
            for (int stripIndex = 0; stripIndex < width + height - 1; stripIndex++) {
                int x0 = stripIndex >= height ? Tile0.toScreenX(stripIndex - height + 1, -height + 1) : Tile0.toScreenX(0, -stripIndex);
                int w0 = 57;
                if (stripIndex < height - 1) {
                    w0 = 28;
                }
                w0 = Math.min(w0, imageWidth - x0);
                BufferedImage stripImage = stripCache[stripIndex];
                stripImage.setRGB(0, 0, w0, imageHeight, w, x0, imageWidth);
            }
        }
    }
    /**
     * Apply the alpha and light map info on the base image.
     */
    private void applyLightMap() {
        int[] w = work.get()[0];
        for (int i = 0; i < image.length; i++) {
            w[i] = withAlpha(image[i]);
        }
        if (lightMap != null && alpha <= LIGHT_THRESHOLD) {
            for (int i = 0; i < lightMap.length; i += 2) {
                w[lightMap[i]] = lightMap[i + 1];
            }
        }
    }
    /**
     * @return returns a full, uncached, lighed image of this tile.
     */
    public BufferedImage getFullImage() {
        int[] w = work.get()[0];
        BufferedImage result = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        computeImageWithLights();
        result.setRGB(0, 0, imageWidth, imageHeight, w, 0, imageWidth);
        return result;
    }
    /** Allocate image memory for the strip cache. */
    private void prepareStripCache() {
        // compute strips
        for (int stripIndex = 0; stripIndex < width + height - 1; stripIndex++) {
            int x0 = stripIndex >= height ? Tile0.toScreenX(stripIndex - height + 1, -height + 1) : Tile0.toScreenX(0, -stripIndex);
            int w0 = 57;
            if (stripIndex < height - 1) {
                w0 = 28;
            }
            w0 = Math.min(w0, imageWidth - x0);
            stripCache[stripIndex] = new BufferedImage(w0, imageHeight, BufferedImage.TYPE_INT_ARGB);
            stripCache[stripIndex].setAccelerationPriority(1.0f);
        }
    }
    /**
     * Apply the alpha value to the supplied color.

     * @param c the input color
     * @return the output color
     */
    protected int withAlpha(int c) {
        if ((c & 0xFF000000) == 0) {
            return c;
        }
        if (alpha < LIGHT_THRESHOLD) {
            return 0xFF000000
            | (((int)((c & 0xFF0000) * alpha)) & 0xFF0000)
            | (((int)((c & 0xFF00) * alpha)) & 0xFF00)
            | (((int)((c & 0xFF) * ((alpha + LIGHT_THRESHOLD) / 2))) & 0xFF);
        }
        return 0xFF000000
        | (((int)((c & 0xFF0000) * alpha)) & 0xFF0000)
        | (((int)((c & 0xFF00) * alpha)) & 0xFF00)
        | (((int)((c & 0xFF) * alpha)) & 0xFF)
    ;
    }
    /**
     * Converts the tile coordinates to pixel coordinates, X component.
     * @param x the X tile coordinate
     * @param y the Y tile coordinate
     * @return the screen coordinate
     */
    public static int toScreenX(int x, int y) {
        return x * 30 - y * 28;
    }
    /**
     * Converts the tile coordinates to pixel coordinates, Y component.
     * @param x the X tile coordinate
     * @param y the Y tile coordinate
     * @return the screen Y coordinate
     */
    public static int toScreenY(int x, int y) {
        return -12 * x - 15 * y;
    }
    /**
     * Converts the screen coordinates to tile coordinates, X component.
     * @param x the X screen coordinate
     * @param y the Y screen coordinate
     * @return the tile X coordinate
     */
    public static float toTileX(int x, int y) {
        return (x + toTileY(x, y) * 28) / 30f;
    }
    /**
     * Converts the screen coordinates to tile coordinates, Y component.
     * @param x the screen X coordinate
     * @param y the screen Y coordinate
     * @return the tile Y coordinate
     */
    public static float toTileY(int x, int y) {
        return -(30 * y + 12 * x) / 786f;
    }
}
