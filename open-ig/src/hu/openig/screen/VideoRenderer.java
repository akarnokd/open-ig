/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import hu.openig.core.Action1;
import hu.openig.core.SwappableRenderer;
import hu.openig.model.ResourceLocator.ResourcePlace;
import hu.openig.utils.Exceptions;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.LockSupport;
import java.util.zip.GZIPInputStream;

/**
 * The video renderer thread.
 * @author akarnokd, 2010.01.17.
 */
public class VideoRenderer extends Thread {
	/** The audio synchronization. */
	protected final CyclicBarrier audioSync;
	/** The stopping flag. */
	protected volatile boolean stopped;
	/** The termination flag. */
	protected volatile boolean terminated;
	/** The movie rendering surface. */
	protected final SwappableRenderer surface;
	/** The audio length or -1 if no audio. */
	protected volatile int audioLength;
	/** The associated video. */
	protected final ResourcePlace video;
	/** Play the video indefinitely? */
	protected boolean repeat;
	/** The frames per second override. */
	protected Double fpsOverride;
	/** The completion action. */
	protected Action1<Void> onComplete;
	/**
	 * Constructor. Sets the synchronization and surface fields
	 * @param audioSync the audio synchronization
	 * @param surface the rendering surface
	 * @param video the resource place
	 * @param name the thread name
	 * @param onComplete the completion action
	 */
	public VideoRenderer(
			CyclicBarrier audioSync, 
			SwappableRenderer surface,
			ResourcePlace video, 
			String name,
			Action1<Void> onComplete
	) {
		super(name);
		this.audioSync = audioSync;
		this.surface = surface;
		this.video = video;
		this.onComplete = onComplete;
	}
	/**
	 * Returns the first frame of the video.
	 * @param video the video place
	 * @return the frame
	 */
	public static BufferedImage firstFrame(ResourcePlace video) {
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(video.open(), 32 * 1024), 64 * 1024))) {
			return firstFrame(in);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	}
	/**
	 * Returns the first frame of the given ani.gz file (new format).
	 * @param in the data input of the extracted ani
	 * @return the first frame or null if no frames present
	 * @throws IOException on error
	 */
	public static BufferedImage firstFrame(DataInputStream in) throws IOException {
		int w = Integer.reverseBytes(in.readInt());
		int h = Integer.reverseBytes(in.readInt());
		if (in.skipBytes(8) != 8) {
            throw new IOException("File structure problem.");
        }
		int[] palette = new int[256];
		byte[] bytebuffer = new byte[w * h];
		int[] currentImage = new int[w * h];
		while (!Thread.currentThread().isInterrupted()) {
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
				in.readFully(bytebuffer);
				for (int i = 0; i < bytebuffer.length; i++) {
					int c0 = palette[bytebuffer[i] & 0xFF];
					if (c0 != 0) {
						currentImage[i] = c0;
					}
				}
				BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				image.setRGB(0, 0, w, h, currentImage, 0, w);
				return image;
			}
		}
		return null;
	}
	/**
	 * The main decoding loop.
	 */
	@Override 
	public void run() {
		
		try  {
			do {
				try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(video.open(), 1024 * 1024), 1024 * 1024))) {
					int w = Integer.reverseBytes(in.readInt());
					int h = Integer.reverseBytes(in.readInt());
					final int frames = Integer.reverseBytes(in.readInt());
					double fps = Integer.reverseBytes(in.readInt()) / 1000.0;
					if (fpsOverride != null) {
						fps = fpsOverride;
					}
					surface.init(w, h);
					
					int[] palette = new int[256];
					byte[] bytebuffer = new byte[w * h];
					int[] currentImage = new int[w * h];
					int frameCount = 0;
					long starttime = 0;
					int frames2 = frames;
					while (!stopped) {
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
							in.readFully(bytebuffer);
							for (int i = 0; i < bytebuffer.length; i++) {
								int c0 = palette[bytebuffer[i] & 0xFF];
								if (c0 != 0) {
									currentImage[i] = c0;
								}
							}
							if (frameCount == 0) {
								try {
									audioSync.await();
								} catch (InterruptedException ex) {
									
								} catch (BrokenBarrierException ex) {
									if (!stopped) {
										Exceptions.add(ex);
									}
								}
								frames2 = (int)Math.ceil(audioLength * fps / 22050.0);
								starttime = System.nanoTime();
							}
							BufferedImage bb = surface.getBackbuffer();
							if (bb != null) {
								bb.setRGB(0, 0, w, h, currentImage, 0, w);
							}
							surface.swap();
							onFrame(fps, frameCount);
							// wait the frame/sec
							starttime += (1000000000.0 / fps);
			       			LockSupport.parkNanos((Math.max(0, starttime - System.nanoTime())));
			       			frameCount++;
						}
					}
					// continue to emit reposition events
					if (frames2 > frames && !stopped) {
						for (int i = frames; i < frames2 && !stopped; i++) {
							onFrame(fps, i);
							// wait the frame/sec
							starttime += (1000000000.0 / fps);
			       			LockSupport.parkNanos((Math.max(0, starttime - System.nanoTime())));
						}
					}
				}
			} while (repeat && !stopped);
		} catch (IOException ex) {
			// TODO log
			Exceptions.add(ex);
		} finally {
			if (!terminated && onComplete != null) {
				onComplete.invoke(null);
			}
		}
			
	}
	/**
	 * Stop the playback and invoke any completion synchronizing.
	 */
	public void stopPlayback() {
		this.stopped = true;
		this.interrupt();
	}
	/**
	 * Stop and terminate the playback without waiting on the completion sync or calling onComplete. 
	 */
	public void terminatePlayback() {
		this.terminated = true;
		stopPlayback();
	}
	/**
	 * Overridable callback called on each frame. Can be used to sync subtitle with the video.
	 * @param fps the frame rate
	 * @param frameIndex the current frame index
	 */
	public void onFrame(double fps, int frameIndex) {
		
	}
	/**
	 * Set the audio length.
	 * @param audioLength the audio length
	 */
	public void setAudioLength(int audioLength) {
		this.audioLength = audioLength;
	}
	/**
	 * Set the repeat value.
	 * @param value the value
	 */
	public void setRepeat(boolean value) {
		repeat = value;
	}
	/**
	 * Set the framerate override value.
	 * @param value the fps value or null to disable
	 */
	public void setFpsOverride(Double value) {
		fpsOverride = value;
	}
}
