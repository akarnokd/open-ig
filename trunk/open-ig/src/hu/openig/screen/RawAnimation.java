/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import hu.openig.core.ResourceType;
import hu.openig.model.ResourceLocator;
import hu.openig.utils.Exceptions;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * A raw animation record. 
 * @author akarnokd, 2012.03.17.
 */
public class RawAnimation {
	/** The list of image frames. */
	public List<byte[]> images = new ArrayList<>();
	/** The palettes. */
	public List<int[]> palettes = new ArrayList<>();
	/** The animation frames per second. */
	public double fps;
	/** The total number of frames. */
	public int frames;
	/** The current frame. */
	int index;
	/** The image width. */
	int w;
	/** The image height. */
	int h;
	/** The current image memory. */
	int[] currentImage;
	/** The current image at the start of the looping. */
	int[] keyframeLoopStart;
	/** The cached image. */
	BufferedImage cache;
	/** The current cached index. */
	int cacheIndex;
	/** The start loop index. */
	public int startLoop;
	/** The end loop index. */
	public int endLoop;
	/** Are we in loop mode? */
	public boolean loop;
	/** Animation is active? */
	public boolean active;
	/**
	 * @return the current image for the current frame.
	 */
	public BufferedImage get() {
		if (cache != null && cacheIndex == index) {
			return cache;
		}
		if (currentImage == null) {
			currentImage = new int[w * h];
		}
		byte[] data = images.get(index);
		int[] palette = palettes.get(index);
		for (int i = 0; i < data.length; i++) {
			int c0 = palette[data[i] & 0xFF];
			if (c0 != 0) {
				currentImage[i] = c0;
			}
		}
		if (index == startLoop && keyframeLoopStart == null) {
			keyframeLoopStart = currentImage.clone();
		}
		cache = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		cache.setRGB(0, 0, w, h, currentImage, 0, w);
		cacheIndex = index;
		return cache;
		
	}
	/**
	 * Move to the next frame.
	 * @return true if wrapped over
	 */
	public boolean moveNext() {
		index++;
		if (loop) {
			if (index >= endLoop) {
				index = startLoop;
				System.arraycopy(keyframeLoopStart, 0, currentImage, 0, keyframeLoopStart.length);
				return true;
			}
		} else
		if (index >= images.size()) {
			index = 0;
			return true;
		}
		return false;
	}
	/**
	 * Load a video resource into list of images.
	 * @param rl the resource locator
	 * @param resource the resource name
	 * @return the animation record with the frames
	 */
	public static RawAnimation load(ResourceLocator rl, String resource) {
		RawAnimation ha = new RawAnimation();
		if (resource == null) {
			ha.fps = 17.89;
			return ha;
		}
		try (DataInputStream in = new DataInputStream(
					new BufferedInputStream(
							new GZIPInputStream(rl.get(resource, ResourceType.VIDEO).open(), 1024 * 1024), 1024 * 1024))) {
			ha.w = Integer.reverseBytes(in.readInt());
			ha.h = Integer.reverseBytes(in.readInt());
			ha.frames = Integer.reverseBytes(in.readInt());
			ha.fps = Integer.reverseBytes(in.readInt()) / 1000.0;
			int[] palette = new int[256];
			int frameCount = 0;
			while (frameCount < ha.frames) {
				int c = in.read();
				if (c < 0 || c == 'X') {
					break;
				} else
				if (c == 'P') {
					int len = in.read();
					for (int j = 0; j < len; j++) {
						int r = in.read() & 0xFF;
						int g = in.read() & 0xFF;
						int b = in.read() & 0xFF;
						palette[j] = 0xFF000000 | (r << 16) | (g << 8) | b;
					}
				} else
				if (c == 'I') {
					byte[] bytebuffer = new byte[ha.w * ha.h];
					in.readFully(bytebuffer);
					
					ha.images.add(bytebuffer);
					ha.palettes.add(palette.clone());
					
					
	       			frameCount++;
				}
			}
		} catch (Throwable ex) {
			Exceptions.add(ex);
		}
		return ha;
	}
	/**
	 * @return the current frame index.
	 */
	public int index() {
		return index;
	}
}
