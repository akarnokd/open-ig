/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.sound;

import hu.openig.core.Action0;
import hu.openig.core.Func0;
import hu.openig.core.ResourceType;
import hu.openig.model.Configuration;
import hu.openig.model.ResourceLocator;
import hu.openig.model.ResourceLocator.ResourcePlace;
import hu.openig.model.SoundType;
import hu.openig.utils.Exceptions;
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
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
		 * Constructor.
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
	final Map<SoundType, byte[]> soundMap = new HashMap<>();
	/** The sound map. */
	final Map<SoundType, AudioFormatType> soundFormat = new HashMap<>();
	/** The sound pool. */
	final Map<AudioFormatType, BlockingQueue<SourceDataLine>> soundPool = new ConcurrentHashMap<>();
	/** The sound pool. */
	ExecutorService exec;
	/** The parallel sound effect semaphore. */
	Semaphore effectSemaphore;
	/** Function to retrieve the current volume. */
	private Func0<Integer> getVolume;
	/** The open lines. */
	final BlockingQueue<SourceDataLine> lines = new LinkedBlockingQueue<>();
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
				try (AudioInputStream ain = AudioSystem.getAudioInputStream(new ByteArrayInputStream(rp.get()))) {
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
				}
			} catch (UnsupportedAudioFileException | IOException e) {
				Exceptions.add(e);
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
						return new Thread(r, "UISounds-" + tid.incrementAndGet());
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
			synchronized (this) { // FIX for Linux PulseAudio Mixer threading issue
				SourceDataLine sdl = AudioSystem.getSourceDataLine(aft.format);
				sdl.open(aft.format);
				lines.add(sdl);
				return sdl;
			}
		} catch (LineUnavailableException ex) {
			Exceptions.add(ex);
		}
		return null;
	}
	/**
	 * Get or create a line for the specified audio format.
	 * @param aft the audio format
	 * @return the data line
	 */
	SourceDataLine getLine(AudioFormatType aft) {
		return addLine(aft);
	}
	/**
	 * Places back the data line into the pool.
	 * @param aft the audio format
	 * @param sdl the source data line
	 */
	void putBackLine(AudioFormatType aft, SourceDataLine sdl) {
        sdl.close();
	}
	/** Close all audio lines. */
	public void close() {
		if (exec != null) {
			exec.shutdown();
			for (SourceDataLine sdl : lines) {
				sdl.close();
			}
			try {
				exec.awaitTermination(60, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				// ignored
			}
			exec.shutdownNow();
			lines.clear();
			soundPool.clear();
			soundMap.clear();
			soundFormat.clear();
			exec = null;
		}
		
	}
	/**
	 * Change the volume on all lines.
	 * @param volume the new volume
	 */
	public void setVolume(int volume) {
		for (SourceDataLine sdl : lines) {
			AudioThread.setVolume(sdl, volume);
		}
	}
	/**
	 * Play the given sound effect.
	 * @param effect the sound to play
	 * @param action the action to invoke once the sound completed.
	 * @return the future object to cancel the sound or null if no sound was played
	 */
	public Action0 playSound(final SoundType effect, final Action0 action) {
		return playSound(effect, action, false);
	}
	/**
	 * Play the given sound effect.
	 * @param effect the sound to play
	 * @param action the action to invoke once the sound completed.
	 * @param loop until cancelled?
	 * @return the future object to cancel the sound or null if no sound was played
	 */
	public Action0 playSound(final SoundType effect, final Action0 action, final boolean loop) {
		if (effect == null) {
			Exceptions.add(new IllegalArgumentException("Null effect"));
			return null;
		}
		final int vol = getVolume.invoke();
		if (vol > 0) {
			if (effectSemaphore.tryAcquire()) {
				try {
					
					final byte[] data = soundMap.get(effect);
					final AudioFormatType aft = soundFormat.get(effect);
					final SourceDataLine sdl = getLine(aft);

					if (sdl != null) {
						final Object cancelSync = new Object();
						final AtomicBoolean done = new AtomicBoolean();
						
						final Future<?> f = exec.submit(new Runnable() {
							@Override
							public void run() {
								try {
									String n = Thread.currentThread().getName();
									Thread.currentThread().setName(n + "-" + effect);
									try {
										AudioThread.setVolume(sdl, vol);
										sdl.start();
										do {
											sdl.write(data, 0, data.length);
										} while (loop && !done.get() && !Thread.currentThread().isInterrupted());
										sdl.drain();
										sdl.stop();
									} catch (Throwable t) {
										Exceptions.add(t);
									} finally {
										Thread.currentThread().setName(n);
										
										synchronized (cancelSync) {
											putBackLine(aft, sdl);
											done.set(true);
										}
										effectSemaphore.release();
									}
								} finally {
									edt(action);
								}
							}
						});
						return new Action0() {
							@Override
							public void invoke() {
								synchronized (cancelSync) {
									if (!done.get()) {
										done.set(true);
										sdl.flush();
										f.cancel(true);
									}
								}
							}
						};
					}
				} catch (RejectedExecutionException ex) {
					effectSemaphore.release();
				}
			}
		}
		edt(action);
		return null;
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
