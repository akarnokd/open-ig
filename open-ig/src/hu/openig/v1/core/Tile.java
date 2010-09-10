/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.core;

import java.awt.image.BufferedImage;

/**
 * A tiled object.
 * @author karnokd, 2010.01.07.
 * @version $Revision 1.0$
 */
public class Tile {
	/** The tile width, points top-right, e.g. in &lt;> the length of the //. */
	public final int width;
	/** The tile height, points bottom-right, e.e. in &lt;> the length of the \\ sides. */
	public final int height;
	/** The entire reference tile image. */
	public final int[] image;
	/** The image width. */
	public final int imageWidth;
	/** The image height. */
	public final int imageHeight;
	/** The overlay image for lights turned on. */
	public int[] lightMap;
	/** The current alpha level of the image. */
	public float alpha = 1;
	/** The alpha percent on which the light map should be applied. */
	protected final float lightThreshold = 0.5f;
	/** The cached image. */
	private BufferedImage cached;
	/** The cached alpha level. */
	private float cachedAlpha = -1;
	/**
	 * Constructor. Sets the fields.
	 * @param width the width in top-right angle.
	 * @param height the height in bottom right angle.
	 * @param image the entire tile image.
	 * @param lightMap the image for lights turned on
	 */
	public Tile(int width, int height, BufferedImage image, BufferedImage lightMap) {
		this.width = width;
		this.height = height;
		this.imageWidth = image.getWidth();
		this.imageHeight = image.getHeight();
		// use ARGB images for the base
		this.image = new int[this.imageWidth * this.imageHeight];
		image.getRGB(0, 0, this.imageWidth, this.imageHeight, this.image, 0, this.imageWidth);
		if (lightMap != null) {
			this.lightMap = new int[lightMap.getWidth() * lightMap.getHeight()];
			lightMap.getRGB(0, 0, lightMap.getWidth(), lightMap.getHeight(), this.lightMap, 0, lightMap.getWidth());
		}
//		this.strips = new BufferedImage[width + height - 1];
	}
	/**
	 * Copy constructor.
	 * @param other the other tile
	 */
	protected Tile(Tile other) {
		this.width = other.width;
		this.height = other.height;
		this.image = other.image;
		this.imageWidth = other.imageWidth;
		this.imageHeight = other.imageHeight;
		this.lightMap = other.lightMap;
		this.alpha = other.alpha;
		this.cached = other.cached;
		this.cachedAlpha = other.cachedAlpha;
	}
	/**
	 * Create a copy of this tile.
	 * @return the tile
	 */
	public Tile copy() {
		return new Tile(this);
	}
	/**
	 * Get a strip of the given base image.
	 * @param baseImage the base image
	 * @param stripIndex the strip index. Strips are indexed from left to right
	 * @return the partial image
	 */
	public BufferedImage getStrip(BufferedImage baseImage, int stripIndex) {
		if (width + height <= 2) {
			return baseImage;
		}
		int x0 = stripIndex >= height ? Tile.toScreenX(stripIndex - height + 1, -height + 1) : Tile.toScreenX(0, -stripIndex);
//		int x0 = Tile.toScreenX(0, -stripIndex);
		int w0 = Math.min(57, imageWidth - x0);
		return baseImage.getSubimage(x0, 0, w0, baseImage.getHeight());
	}
	/**
	 * @return the number of rendering strips
	 */
	public int getStripCount() {
		return width + height + 1;
	}
	/**
	 * Create an alpha blent image from the reference tile image.
	 * @return the manipulated image.
	 */
	public BufferedImage alphaBlendImage() {
		if (alpha != cachedAlpha || cached == null) {
			int[] work = new int[image.length];
			// apply light map if exists?
			boolean applyLM = lightMap != null && alpha <= lightThreshold;
			for (int i = 0; i < work.length; i++) {
				int c = withAlpha(image[i]);
				if (applyLM) {
					int d = lightMap[i];
					if (d != 0) {
						c = d;
					}
				}
				work[i] = c;
			}
			BufferedImage result = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
			result.setRGB(0, 0, imageWidth, imageHeight, work, 0, imageWidth);
			cached = result;
			cachedAlpha = alpha;
		}
		return cached;
	}
	/**
	 * Apply the alpha value to the supplied color. 
	 * @param c the input color
	 * @return the output color
	 */
	protected int withAlpha(int c) {
		return (c & 0xFF000000)
			| (((int)((c & 0xFF0000) * alpha)) & 0xFF0000)
			| (((int)((c & 0xFF00) * alpha)) & 0xFF00)
			| ((int)((c & 0xFF) * alpha))
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
	 * Converts the screen coordinates to tile coordinates, Y comonent.
	 * @param x the screen X coordinate
	 * @param y the screen Y coordinate
	 * @return the tile Y coordinate
	 */
	public static float toTileY(int x, int y) {
		return -(30 * y + 12 * x) / 786f;
	}
}
