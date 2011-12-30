/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import hu.openig.utils.JavaUtils;

import java.awt.image.BufferedImage;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;

/**
 * Contains an image cache for the computed alpha values.
 * @author akarnokd, 2011.12.30.
 */
public class TileCached extends Tile {
	/** The cached image strips. The key is maxCount times the alpha value. */
	protected final Map<Integer, Reference<BufferedImage[]>> cache = JavaUtils.newHashMap();
	/** The maximum alpha cache count. */
	protected final int maxCount;
	/**
	 * Constructor. Initializes the tile and cache.
	 * @param width the width of the tile
	 * @param height the height of the tile
	 * @param image the base image
	 * @param lightMap the optional light map
	 * @param maxCount the maximum alpha caching level
	 */
	public TileCached(int width, int height, BufferedImage image,
			BufferedImage lightMap, int maxCount) {
		super(width, height, image, lightMap);
		this.maxCount = maxCount;
	}
	/**
	 * Copy constructor.
	 * @param other the other tile cache
	 */
	protected TileCached(TileCached other) {
		super(other);
		this.maxCount = other.maxCount;
	}
	@Override
	public TileCached copy() {
		return new TileCached(this);
	}
	@Override
	public BufferedImage getStrip(int stripIndex) {
		if (hasAlphaChanged()) {
			int key = (int)(alpha * 16);
			Reference<BufferedImage[]> r = cache.get(key);
			BufferedImage[] strips = r != null ? r.get() : null;
			if (r == null || strips == null) {
				renew();
				computeImageWithLights();
				cache.put(key, createReference(stripCache));
			} else {
				stripCache = strips;
				cachedAlpha = alpha;
			}
		}
		return stripCache[stripIndex];
	}
	/**
	 * Create a fresh image in place of every array element.
	 */
	protected void renew() {
		BufferedImage[] newStrips = new BufferedImage[stripCache.length];
		for (int i = 0; i < newStrips.length; i++) {
			BufferedImage s = stripCache[i];
			final int w = s.getWidth();
			final int h = s.getHeight();
			BufferedImage d = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			d.setAccelerationPriority(1.0f);
			newStrips[i] = d;
		}
		stripCache = newStrips;
	}
	/**
	 * Create a reference to the given value.
	 * @param <T> the object type
	 * @param value the value
	 * @return the reference
	 */
	protected <T> Reference<T> createReference(T value) {
		return new SoftReference<T>(value);
	}
}
