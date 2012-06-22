/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.model.ResourceLocator.ResourcePlace;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.LockSupport;
import java.util.zip.GZIPInputStream;

import javax.swing.SwingUtilities;

/**
 * The video renderer thread for technology screens.
 * @author akarnokd, 2010.01.17.
 */
public class TechnologyVideoRenderer {
	/** The stopping flag. */
	protected volatile boolean stopped;
	/** The associated video. */
	protected final ResourcePlace video;
	/** The technology frames. */
	protected List<BufferedImage> images = new ArrayList<BufferedImage>();
	/** The frames per second override. */
	public Double fpsOverride;
	/** The action to invoke on the EDT for each frame. */
	public final Action1<BufferedImage> onFrame;
	/** The future for the video runnable. */
	protected Future<Closeable> videoRun;
	/** The common resources. */
	private final CommonResources commons;
	/**
	 * Constructor. Sets the synchronization and surface fields
	 * @param commons the common resources
	 * @param video the resource place
	 * @param onFrame the action to invoke for every frame
	 */
	public TechnologyVideoRenderer(CommonResources commons, 
			ResourcePlace video, final Action1<BufferedImage> onFrame) {
		this.commons = commons;
		this.video = video;
		this.onFrame = onFrame;
	}
	/**
	 * Start the processing playback on the given scheduler.
	 * @param pool the scheduler
	 */
	public void start(final ScheduledExecutorService pool) {
		videoRun = pool.submit(new Callable<Closeable>() {
			@Override
			public Closeable call() throws Exception {
				return prepare(pool);
			}
		});
	}
	/** Stop the playback. */
	public void stop() {
		stopped = true;
		try {
			Closeable loop = videoRun.get();
			if (loop != null) {
				try {
					loop.close();
				} catch (IOException ex) {
					// ignored
				}
			}
		} catch (InterruptedException ex) {
			
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * The main decoding loop.
	 * @param pool the scheduler pool
	 * @return the future representing the frame loop
	 */
	Closeable prepare(final ScheduledExecutorService pool) {
		double fps = 0.0;
		int frames = 0;
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(video.open(), 1024 * 1024), 1024 * 1024));
			try {
				int w = Integer.reverseBytes(in.readInt());
				int h = Integer.reverseBytes(in.readInt());
				frames = Integer.reverseBytes(in.readInt());
				fps = Integer.reverseBytes(in.readInt()) / 1000.0;
				if (fpsOverride != null) {
					fps = fpsOverride;
				}
				int[] palette = new int[256];
				byte[] bytebuffer = new byte[w * h];
				int[] currentImage = new int[w * h];
				int frameCount = 0;
				long starttime = System.nanoTime();
				while (frameCount < frames && !stopped) {
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
						final BufferedImage fimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
						fimg.setRGB(0, 0, w, h, currentImage, 0, w);
						fimg.setAccelerationPriority(1.0f);

						if (frameCount == 0) {
							starttime = System.nanoTime();
						}
						
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								if (!stopped) {
									onFrame.invoke(fimg);
								}
							}
						});
						starttime += (1000000000.0 / fps);
		       			LockSupport.parkNanos((Math.max(0, starttime - System.nanoTime())));

						images.add(fimg);
		       			frameCount++;
					}
				}
			} finally {
				try { in.close(); } catch (IOException ex) {  }
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		if (images.size() > 0 && !stopped) {
			final int frameSize = frames; 
			int delay = (((int)(1000 / fps)) / 25) * 25; // round frames down
			Closeable t = commons.register(delay, new Action0() {
				int frameIndex = 0;
				@Override
				public void invoke() {
					if (!stopped) {
						final BufferedImage fimg = images.get(frameIndex);
						onFrame.invoke(fimg);
						frameIndex = (frameIndex < frameSize - 1) ? frameIndex + 1 : 0;
					}
				}
			});
			return t;
		}
		return null;
	}
}
