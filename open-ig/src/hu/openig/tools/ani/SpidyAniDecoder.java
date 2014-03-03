/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools.ani;

import hu.openig.tools.ani.Framerates.Rates;
import hu.openig.tools.ani.SpidyAniFile.Algorithm;
import hu.openig.tools.ani.SpidyAniFile.Block;
import hu.openig.tools.ani.SpidyAniFile.Data;
import hu.openig.tools.ani.SpidyAniFile.Palette;
import hu.openig.tools.ani.SpidyAniFile.Sound;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class contains the algorithms to decode a spidyani file and uses a callback scheme 
 * to notify the client of a new frame or audio data.
 * @author karnokd
 */
public final class SpidyAniDecoder {
	/** Private constructor. */
	private SpidyAniDecoder() {
		// utility class
	}
	/**
	 * The callback interface for notifications.
	 * @author karnokd
	 */
	public interface SpidyAniCallback {
		/**
		 * Callback to ask for a new input stream, starting at the beginning of the ANI file.
		 * The player will close this input stream on exit
		 * @return the input stream
		 */
		InputStream getNewInputStream();
		/**
		 * Animation files have different framerate and audio delay options, which need to
		 * be looked up using the Framerates class. This method should return the
		 * filename the caller wants to play back.
		 * @return the file name of the playback
		 */
		String getFileName();
		/**
		 * Notify the caller about the animation with and height.
		 * @param width the image width
		 * @param height the image height
		 * @param frames the number of frames in the animation
		 * @param languageCode the animation file's language code (1=English, 2=Hungarian)
		 * @param fps the frames per second value
		 * @param audioDelay the number of frames to delay the audio playback
		 */
		void initialize(int width, int height, int frames, int languageCode, double fps, int audioDelay);
		/**
		 * Callback with the next segment of audio data.
		 * @param data the audio data
		 */
		void audioData(byte[] data);
		/**
		 * Callback with the next image frame data.
		 * @param image the array of RGBA image, do not modify the contents!
		 */
		void imageData(int[] image);
		/**
		 * Callback to notify if the playback should be stopped.
		 * @return true if the playback should be stopped
		 */
		boolean isStopped();
		/**
		 * Callback to notify if the playback should be paused.
		 * @return true if the playback should be paused
		 */
		boolean isPaused();
		/**
		 * Indicator that there are no more frames/audio available.
		 */
		void finished();
		/**
		 * Indicator that the playback was either stopped or the playback thread was interrupted.
		 */
		void stopped();
		/** 
		 * An unexpected and unrecoverable exception occurred during the playback.
		 * @param t the exception
		 */
		void fatal(Throwable t);
	}
	/**
	 * The actual decoder loop.
	 * @param callback the callback 
	 */
	public static void decodeLoop(SpidyAniCallback callback) {
		if (callback == null) {
			throw new IllegalArgumentException("callback null");
		}
		try (InputStream in = callback.getNewInputStream()) {
			final SpidyAniFile saf = new SpidyAniFile();
			saf.open(in);
			saf.load();
	
			Framerates fr = new Framerates();
			Rates r = fr.getRates(callback.getFileName(), saf.getLanguageCode());
			double fps = r.fps;
			int delay = r.delay;
			
			callback.initialize(saf.getWidth(), saf.getHeight(), saf.getFrameCount(), saf.getLanguageCode(), fps, delay);
			PaletteDecoder palette = null;
			// the raw image container used when decoding the image
			// Fix: It seems when the file contains a palette change, the differential coder retains the color index
			// between the frames, but not the color itself.
			byte[] rawImage = new byte[saf.getWidth() * saf.getHeight()];
			int[] rgbImage = new int[saf.getWidth() * saf.getHeight()];
			int imageHeight = 0;
			int dst = 0;
			int audioLength = 0;
			int imageIndex = 0;
			Algorithm alg = saf.getAlgorithm();
			try {
				while (!callback.isStopped() && !Thread.currentThread().isInterrupted()) {
					Block b = saf.next();
					if (b instanceof Palette) {
						palette = (Palette)b;
					} else
					if (b instanceof Sound) {
						audioLength += b.data.length;
						callback.audioData(b.data);
					} else
					if (b instanceof Data) {
						Data d = (Data)b;
						imageHeight += d.height;
						// decompress the image
						byte[] rleInput = d.data;
						if (saf.isLZSS() && !d.specialFrame) {
							rleInput = new byte[d.bufferSize];
							LZSS.decompress(d.data, 0, rleInput, 0);
						}
						switch (alg) {
						case RLE_TYPE_1:
							int newDst = RLE.decompress1(rleInput, 0, rawImage, dst);
							dst = newDst;
							break;
						case RLE_TYPE_2:
							newDst = RLE.decompress2(rleInput, 0, rawImage, dst);
							dst = newDst;
							break;
						default:
						}
						// we reached the number of subimages per frame?
						if (imageHeight >= saf.getHeight()) {
							// FIX: only the last step is to apply the current palette to transcode to RGBA
							// hopefully, the palette switch anomalies disappear
							for (int i = 0; i < rgbImage.length; i++) {
								rgbImage[i] = palette.getColor(rawImage[i] & 0xFF);
							}
							callback.imageData(rgbImage);
							imageHeight = 0;
							dst = 0;
							imageIndex++;
						}
					}
					
				}
			} catch (EOFException ex) {
				// we reached the end of data
			}
			int excessFrames = (int)Math.floor((audioLength / 22050.0 - (imageIndex - delay) / fps) * fps);
//			int totalExcess = excessFrames;
//			int[] alpha = rgbImage.clone();
			// we have the last raw image, fade it out to blackness
			while (!callback.isStopped() && !Thread.currentThread().isInterrupted() && excessFrames > 0) {
//				float factor = excessFrames * 1.0f / totalExcess;
//				for (int i = 0; i < alpha.length; i ++) {
//					alpha[i] = makeAlpha(rawImage[i], factor);
//				}
				callback.imageData(rgbImage);
				excessFrames--;
			}
			
			if (callback.isStopped() || Thread.currentThread().isInterrupted()) {
				callback.stopped();
			} else {
				callback.finished();
			}
			
		} catch (IOException ex) {
			callback.fatal(ex);
		}
	}
	/**
	 * Increase or decrease the distance from the black
	 * (e.g fade into white or black).
	 * @param original the original color
	 * @param alpha the percentage to change
	 * @return the modified color
	 */
	static int makeAlpha(int original, double alpha) {
		return (original & 0xFF000000) 
		| ((int)((original & 0xFF0000) * alpha) & 0xFF0000) 
		| ((int)((original & 0xFF00) * alpha) & 0xFF00) 
		| ((int)((original & 0xFF) * alpha) & 0xFF);
	}
}
