/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.sound;

import hu.openig.core.Action0;
import hu.openig.core.Configuration;
import hu.openig.core.Func0;
import hu.openig.core.ResourceLocator;
import hu.openig.core.ResourceLocator.ResourcePlace;
import hu.openig.core.ResourceType;
import hu.openig.model.SoundType;
import hu.openig.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;

/**
 * @author akarnokd, Apr 18, 2011
 */
public class Sounds {
	/** The audio format type providing the proper equals and hashcode. */
	static final class AudioFormatType {
		/** The audio format. */
		public final AudioFormat format;
		/**
		 * Constructur.
		 * @param format the format to store
		 */
		public AudioFormatType(AudioFormat format) {
			this.format = format;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AudioFormatType) {
				AudioFormatType aft = (AudioFormatType)obj;
				return format.matches(aft.format);
			}
			return false;
		}
		@Override
		public int hashCode() {
			return Arrays.asList(format.getSampleRate(), format.getSampleSizeInBits(), 
					format.getChannels(), format.getEncoding()).hashCode();
		}
	}
	/** The sound map. */
	final Map<SoundType, byte[]> soundMap = new HashMap<SoundType, byte[]>();
	/** The sound map. */
	final Map<SoundType, AudioFormatType> soundFormat = new HashMap<SoundType, AudioFormatType>();
	/** The sound pool. */
	final Map<AudioFormatType, BlockingQueue<SourceDataLine>> soundPool = new ConcurrentHashMap<AudioFormatType, BlockingQueue<SourceDataLine>>();
	/** The sound pool. */
	ExecutorService exec;
	/** The parallel sound effect semaphore. */
	Semaphore effectSemaphore;
	/** Function to retrieve the current volume. */
	private Func0<Integer> getVolume;
	/** The open lines. */
	final BlockingQueue<SourceDataLine> lines = new LinkedBlockingQueue<SourceDataLine>();
	/** Should the sound map cache lines? */
	static final boolean CACHE_LINES = false;
	/**
	 * Initialize the sound pool.
	 * @param rl the resource locator
	 */
	public Sounds(ResourceLocator rl) {
		for (SoundType st : SoundType.values()) {
			try {
				ResourcePlace rp = rl.get(st.resource, ResourceType.AUDIO);
				if (rp == null) {
					throw new AssertionError("Missing resource: " + rl.language + " " + st.resource);
				}
				AudioInputStream ain = AudioSystem.getAudioInputStream(new ByteArrayInputStream(rp.get()));
				try {
					AudioFormat af = ain.getFormat();
					byte[] snd = IOUtils.load(ain);
					// upscale an 8 bit sample to 16 bit
					if (af.getSampleSizeInBits() == 8) {
						// signify if unsigned, because the upscaling works on signed data
						if (af.getEncoding() == Encoding.PCM_UNSIGNED) {
							for (int i = 0; i < snd.length; i++) {
								snd[i] = (byte)((snd[i] & 0xFF) - 128);
							}
						}
						snd = AudioThread.convert8To16(snd);
						af = new AudioFormat(af.getSampleRate(), 16, af.getChannels(), true, af.isBigEndian());
					}
					soundMap.put(st, snd);
					soundFormat.put(st, new AudioFormatType(af));
				} finally {
					ain.close();
				}
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}			
		}
	}
	/**
	 * Initialize all channels.
	 * @param channels the number of parallel effects
	 * @param getVolume the function which returns the current volume, 
	 * asked once before an effect plays. Volume of 0 means no sound
	 */
	public void initialize(int channels, final Func0<Integer> getVolume) {
		this.getVolume = getVolume;
		ThreadPoolExecutor tpe = new ThreadPoolExecutor(
				channels, channels, 5, TimeUnit.SECONDS, 
				new LinkedBlockingQueue<Runnable>(channels),
				new ThreadFactory() {
					/** The thread numbering. */
					final AtomicInteger tid = new AtomicInteger();
					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r, "UISounds-" + tid.incrementAndGet());
						return t;
					}
				}
				/*
				,
				new RejectedExecutionHandler() {
					@Override
					public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
						System.err.println("Rejected");
					}
				}
				*/
				
		);
		exec = tpe;
		effectSemaphore = new Semaphore(channels);
		// initialize the sound pool
		for (AudioFormatType aft : soundFormat.values()) {
			if (!soundPool.containsKey(aft)) {
				soundPool.put(aft, new LinkedBlockingQueue<SourceDataLine>());
			}
		}
	}
	/**
	 * Add a new SourceDataLine with the specified format to the sound pool.
	 * @param aft the audio format to add
	 * @return the data line created
	 */
	SourceDataLine addLine(AudioFormatType aft) {
		try {
			synchronized (this) { // FIX for Linux PulseAudio Mixer theading issue 
				SourceDataLine sdl = AudioSystem.getSourceDataLine(aft.format);
				sdl.open(aft.format);
				return sdl;
			}
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	/**
	 * Get or create a line for the specified audio format.
	 * @param aft the audio format
	 * @return the data line
	 */
	SourceDataLine getLine(AudioFormatType aft) {
		SourceDataLine result = null; 
		if (CACHE_LINES) {
			result = soundPool.get(aft).poll();
		}
		if (result == null) {
			result = addLine(aft);
			if (CACHE_LINES) {
				soundPool.get(aft).add(result);
				lines.add(result);
			}
		}
		return result;
	}
	/**
	 * Places back the data line into the pool.
	 * @param aft the audio format
	 * @param sdl the source data line
	 */
	void putBackLine(AudioFormatType aft, SourceDataLine sdl) {
		if (CACHE_LINES) {
			BlockingQueue<SourceDataLine> queue = soundPool.get(aft);
			if (queue != null) {
				queue.add(sdl);
			}
		} else {
			sdl.close();
		}
	}
	/** Close all audio lines. */
	public void close() {
		if (exec != null) {
			exec.shutdown();
			try {
				exec.awaitTermination(60, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				// ignored
			}
			exec.shutdownNow();
			for (SourceDataLine sdl : lines) {
				sdl.close();
			}
			lines.clear();
			soundPool.clear();
			soundMap.clear();
			soundFormat.clear();
			exec = null;
		}
		
	}
	/**
	 * Play the given sound effect.
	 * @param effect the sound to play
	 * @param action the action to invoke once the sound completed.
	 */
	public void playSound(final SoundType effect, final Action0 action) {
		if (effect == null) {
			new IllegalArgumentException("Null effect").printStackTrace();
			return;
		}
		final int vol = getVolume.invoke();
		if (vol > 0) {
			if (effectSemaphore.tryAcquire()) {
				try {
					exec.execute(new Runnable() {
						@Override
						public void run() {
							try {
								playSoundAsync(effect, vol);
							} finally {
								edt(action);
							}
						}
					});
				} catch (RejectedExecutionException ex) {
					effectSemaphore.release();
				}
			}
		} else {
			edt(action);
		}
	}
	/**
	 * Execute an action on the edt.
	 * @param action the action
	 */
	public static void edt(final Action0 action) {
		if (action != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					action.invoke();
				}
			});
		}
	}
	
	/**
	 * Play the sound type with the given volume asynchronously.
	 * @param effect the effect
	 * @param vol the volume
	 */
	void playSoundAsync(final SoundType effect, final int vol) {
		String n = Thread.currentThread().getName();
		Thread.currentThread().setName(n + "-" + effect);
		
		byte[] data = soundMap.get(effect);
		AudioFormatType aft = soundFormat.get(effect);
		try {
			SourceDataLine sdl = getLine(aft);
			if (sdl != null) {
				try {
					AudioThread.setVolume(sdl, vol);
					sdl.start();
					sdl.write(data, 0, data.length);
					sdl.drain();
					sdl.stop();
				} finally {
					putBackLine(aft, sdl);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			Thread.currentThread().setName(n);

			effectSemaphore.release();
		}
	}
	/**
	 * Test program for sound effects.
	 * @param args no arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		final Configuration config = new Configuration("open-ig-config.xml");
		config.load();
		Sounds s = new Sounds(config.newResourceLocator());
		s.initialize(config.audioChannels, new Func0<Integer>() {
			@Override
			public Integer invoke() {
				return 100;
			}
		});
		Random rnd = new Random();
		for (int i = 0; i < 1000; i++) {
			s.playSound(rnd.nextBoolean() ? SoundType.FIRE_1 : SoundType.GROUND_FIRE_1, null);
			Thread.sleep(100);
		}
		Thread.sleep(1000);
		s.close();
	}
}
