/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.awt.image.BufferedImage;

/**
 * Applies a night color replacement.
 * @author akarnokd, 2012.06.07.
 */
public class NightFilter extends AbstractImageFilter {
	/** The night-factor. */
	protected float alpha;
	/** The light threshold value. */
	protected float lightThreshold;
	/** The lightmap. */
	protected int[] lightMap;
	/**
	 * Constructs a night filter.
	 * @param alpha the alpha values
	 * @param lightThreshold where to switch to blue-shifting
	 * @param lightMap the optional lightmap pairs of position+color
	 */
	public NightFilter(float alpha, float lightThreshold, int[] lightMap) {
		this.alpha = alpha;
		this.lightThreshold = lightThreshold;
		this.lightMap = lightMap;
	}
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		if (dest == null) {
			dest = createCompatibleDestImage(src, null);
		}
		
		int w = src.getWidth();
		int h = src.getHeight();
		int[] pixels = new int[w * h]; 
		src.getRaster().getDataElements(0, 0, w, h, pixels);
		if (alpha < lightThreshold) {
			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = withAlphaNight(pixels[i]);
			}
			if (lightMap != null) {
				for (int i = 0; i < lightMap.length; i += 2) {
					pixels[lightMap[i]] = pixels[i + 1];
				}
			}
		} else {
			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = withAlphaNight(pixels[i]);
			}
		}
		
		dest.getRaster().setDataElements(0, 0, w, h, pixels);
		
		return dest;
	}
	/**
	 * Apply the alpha value to the supplied color. 
	 * @param c the input color
	 * @return the output color
	 */
	protected int withAlphaDay(int c) {
		if ((c & 0xFF000000) == 0) {
			return c;
		}
		return 0xFF000000
		| (((int)((c & 0xFF0000) * alpha)) & 0xFF0000)
		| (((int)((c & 0xFF00) * alpha)) & 0xFF00)
		| (((int)((c & 0xFF) * alpha)) & 0xFF)
	;
	}
	/**
	 * Apply the alpha value to the supplied color. 
	 * @param c the input color
	 * @return the output color
	 */
	protected int withAlphaNight(int c) {
		if ((c & 0xFF000000) == 0) {
			return c;
		}
		return 0xFF000000
		| (((int)((c & 0xFF0000) * alpha)) & 0xFF0000)
		| (((int)((c & 0xFF00) * alpha)) & 0xFF00)
		| (((int)((c & 0xFF) * ((alpha + lightThreshold) / 2))) & 0xFF);
	}
}
