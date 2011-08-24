/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.sound;

import hu.openig.core.Configuration;
import hu.openig.core.Func1;
import hu.openig.core.ResourceLocator;
import hu.openig.core.ResourceType;
import hu.openig.model.SoundType;
import hu.openig.utils.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author akarnokd, Apr 18, 2011
 */
public class Sounds {
	/** The sound map. */
	final Map<SoundType, byte[]> soundMap = new HashMap<SoundType, byte[]>();
	/** The sound map. */
	final Map<SoundType, AudioFormat> soundFormat = new HashMap<SoundType, AudioFormat>();
	/** The sound pool. */
	ExecutorService exec;
	/** Function to retrieve the current volume. */
	private Func1<Void, Integer> getVolume;
	/**
	 * Initialize the sound pool.
	 * @param rl the resource locator
	 */
	public Sounds(ResourceLocator rl) {
		for (SoundType st : SoundType.values()) {
			try {
				AudioInputStream ain = AudioSystem.getAudioInputStream(new ByteArrayInputStream(rl.get(st.resource, ResourceType.AUDIO).get()));
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
					soundFormat.put(st, af);
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
	public void initialize(int channels, final Func1<Void, Integer> getVolume) {
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
				},
				new RejectedExecutionHandler() {
					@Override
					public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
						// ignore
					}
				} 
				
		);
		exec = tpe;
	}
	/** Close all audio lines. */
	public void close() {
		if (exec != null) {
			exec.shutdown();
		}
	}
	/**
	 * Play the given sound effect.
	 * @param effect the sound to play
	 */
	public void play(final SoundType effect) {
		final int vol = getVolume.invoke(null);
		if (vol > 0) {
			exec.execute(new Runnable() {
				@Override
				public void run() {
					byte[] data = soundMap.get(effect);
					AudioFormat af = soundFormat.get(effect);
					final CyclicBarrier barrier = new CyclicBarrier(2);
					try {
						Clip c = AudioSystem.getClip();
						c.open(af, data, 0, data.length);
						
						AudioThread.setVolume(c, vol);
						LineListener ll = new LineListener() {
							@Override
							public void update(LineEvent event) {
								if (event.getType() == Type.STOP || event.getType() == Type.CLOSE) {
									try {
										barrier.await();
									} catch (InterruptedException ex) {
										// ignore
									} catch (BrokenBarrierException ex) {
										// ignore
									}
								}
							}
						};
						c.addLineListener(ll);
						c.start();
						
						barrier.await();
						c.removeLineListener(ll);
						c.close();
					} catch (LineUnavailableException ex) {
						ex.printStackTrace();
					} catch (InterruptedException ex) {
						// ignore
					} catch (BrokenBarrierException ex) {
						// ignore
					}
				}
			});
		}
	}
	
	/**
	 * Test program for sound effects.
	 * @param args no arguments
	 * @throws IOException on error
	 */
	public static void main(String[] args) throws IOException {
		final Configuration config = new Configuration("open-ig-config.xml");
		config.load();
		Sounds s = new Sounds(config.newResourceLocator());
		s.initialize(8, new Func1<Void, Integer>() {
			@Override
			public Integer invoke(Void value) {
				return config.effectVolume;
			}
		});
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		int i = 0;
		SoundType[] values = SoundType.values();
		while ((line = in.readLine()) != null) {
			if (i >= values.length || line.equalsIgnoreCase("q")) {
				break;
			}
			System.out.println("Playing " + values[i]);
			s.play(values[i]);
			i++;
		}
		s.close();
	}
}
