/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.sound;

import hu.openig.res.GameResourceManager;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;


/**
 * User interface sound playback.
 * @author karnokd
 */
public class SoundFXPlayer {
	/** The audio sample map. */
	private final Map<String, byte[]> samples;
	/** Number of parallel sound lines. */
	private static final int SOUND_POOL_SIZE = 8;
	/** Shutdown all audio threads. */
	private volatile boolean shutdown;
	/** The executor service for parallel sound playback. */
	private volatile ExecutorService service;
	/** The master gain dB. */
	private volatile float masterGain = -10;
	/** The mute value. */
	private volatile boolean mute;
	/** The window size for the moving average filter. */
	private volatile int movingAverageWindow = 4;
	/** The sound pool. */
	private final BlockingQueue<SourceDataLine> soundPool = new LinkedBlockingQueue<SourceDataLine>(SOUND_POOL_SIZE);
	/** The array of source data lines used. */
	private final SourceDataLine[] lines;
	/**
	 * Constructor. Loads the user interface sound samples.
	 * @param grm the global game resouce manager.
	 */
	public SoundFXPlayer(GameResourceManager grm) {
		service = Executors.newFixedThreadPool(SOUND_POOL_SIZE);
		lines = new SourceDataLine[SOUND_POOL_SIZE];
		// Initialize sound pool
		for (int i = 0; i < SOUND_POOL_SIZE; i++) {
			SourceDataLine sdl = AudioThread.createAudioOutput();
			sdl.start();
			lines[i] = sdl;
			if (!soundPool.offer(sdl)) {
				throw new AssertionError("Queue problems");
			}
		}
		samples = grm.sounds.samples;
	}
	/**
	 * Plays the specified UI sound or throws an IllegalArgumentException if there is no such name.
	 * @param name the sound name
	 */
	public void playSound(String name) {
		byte[] data = samples.get(name);
		if (data == null) {
			throw new IllegalArgumentException("Sound sample not found " + name);
		}
		playSound(data);
	}
	/**
	 * Play the given sound data asynchronously.
	 * @param data the non null data to write
	 */
	private void playSound(final byte[] data) {
		service.submit(new Runnable() {
			@Override
			public void run() {
				try {
					SourceDataLine sdl = soundPool.poll();
					if (sdl != null) {
						FloatControl fc = (FloatControl)sdl.getControl(FloatControl.Type.MASTER_GAIN);
						fc.setValue(masterGain);
						BooleanControl bc = (BooleanControl)sdl.getControl(BooleanControl.Type.MUTE);
						bc.setValue(mute);
						byte[] data16 = movingAverageWindow > 0 ? AudioThread.split16To8(AudioThread.movingAverage(AudioThread.upscale8To16(data), movingAverageWindow)) : AudioThread.convert8To16(data);
						sdl.write(data16, 0, data16.length);
						sdl.drain();
						soundPool.put(sdl);
						if (shutdown) {
							sdl.stop();
							sdl.close();
						}
					} else {
						System.err.println("Sound resources exhausted at this moment");
					}
				} catch (InterruptedException e) {
					// ignored
				}
			}
		});
	}
	/** Close all audio lines. */
	public void close() {
		shutdown = true;
		SourceDataLine sdl = null;
		while ((sdl = soundPool.poll()) != null) {
			sdl.close();
		}
		service.shutdown();
	}
	/**
	 * Set the master gain on the source data line.
	 * @param gain the master gain in decibels, typically -80 to 0.
	 */
	public void setMasterGain(float gain) {
		masterGain = gain;
		for (SourceDataLine sdl : lines) {
			FloatControl fc = (FloatControl)sdl.getControl(FloatControl.Type.MASTER_GAIN);
			fc.setValue(masterGain);
		}
	}
	/**
	 * Mute or unmute the current playback.
	 * @param mute the mute status
	 */
	public void setMute(boolean mute) {
		this.mute = mute;
		for (SourceDataLine sdl : lines) {
			BooleanControl bc = (BooleanControl)sdl.getControl(BooleanControl.Type.MUTE);
			bc.setValue(mute);
		}
	}
	/**
	 * Sets the moving average window length used in sound filtering.
	 * @param movingAverageWindow the moving average window length in samples
	 */
	public void setMovingAverageWindow(int movingAverageWindow) {
		this.movingAverageWindow = movingAverageWindow;
	}
	/**
	 * Returns the moving average window length used in sound filtering.
	 * @return the moving average window length
	 */
	public int getMovingAverageWindow() {
		return movingAverageWindow;
	}
}
