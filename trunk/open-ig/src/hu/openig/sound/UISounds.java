/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.sound;

import hu.openig.utils.IOUtils;
import hu.openig.utils.ResourceMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;


/**
 * User interface sound registry.
 * @author karnokd
 */
public class UISounds {
	/** The audio sample map. */
	private final Map<String, byte[]> samples = new HashMap<String, byte[]>();
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
	 * @param resMap the resource mapper
	 */
	public UISounds(ResourceMapper resMap) {
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
		samples.put("IncomingMessage", IOUtils.load(resMap.get("SOUND/NOI01.SMP")));
		samples.put("CommanderMessage", IOUtils.load(resMap.get("SOUND/NOI02.SMP")));
		samples.put("Message", IOUtils.load(resMap.get("SOUND/NOI03.SMP")));
		samples.put("MessageBridge", IOUtils.load(resMap.get("SOUND/NOI04.SMP")));
		samples.put("DrAwaitsOnBridge", IOUtils.load(resMap.get("SOUND/NOI08.SMP")));
		samples.put("AlienVesselsDetected", IOUtils.load(resMap.get("SOUND/NOI09.SMP")));
		samples.put("TransportUnderAttack", IOUtils.load(resMap.get("SOUND/NOI10.SMP")));
		samples.put("BackupReceived", IOUtils.load(resMap.get("SOUND/NOI15.SMP")));
		samples.put("ReinforcementsArrived", IOUtils.load(resMap.get("SOUND/NOI16.SMP")));
		samples.put("Bridge", IOUtils.load(resMap.get("SOUND/NOI24.SMP")));
		samples.put("Starmap", IOUtils.load(resMap.get("SOUND/NOI25.SMP")));
		samples.put("Colony", IOUtils.load(resMap.get("SOUND/NOI26.SMP")));
		samples.put("Equipment", IOUtils.load(resMap.get("SOUND/NOI27.SMP")));
		samples.put("Production", IOUtils.load(resMap.get("SOUND/NOI28.SMP")));
		samples.put("Research", IOUtils.load(resMap.get("SOUND/NOI29.SMP")));
		samples.put("Information", IOUtils.load(resMap.get("SOUND/NOI30.SMP")));
		samples.put("StateRoom", IOUtils.load(resMap.get("SOUND/NOI31.SMP")));
		samples.put("Local", IOUtils.load(resMap.get("SOUND/NOI32.SMP")));
		samples.put("Diplomacy", IOUtils.load(resMap.get("SOUND/NOI33.SMP")));
		samples.put("Inventions", IOUtils.load(resMap.get("SOUND/NOI34.SMP")));
		samples.put("AlienRaces", IOUtils.load(resMap.get("SOUND/NOI35.SMP")));
		samples.put("FinancialInformation", IOUtils.load(resMap.get("SOUND/NOI36.SMP")));
		samples.put("MilitaryInformation", IOUtils.load(resMap.get("SOUND/NOI37.SMP")));
		samples.put("ColonyInformation", IOUtils.load(resMap.get("SOUND/NOI38.SMP")));
		samples.put("Fleets", IOUtils.load(resMap.get("SOUND/NOI39.SMP")));
		samples.put("Buildings", IOUtils.load(resMap.get("SOUND/NOI40.SMP")));
		samples.put("Planets", IOUtils.load(resMap.get("SOUND/NOI41.SMP")));
		samples.put("NewShipAdded", IOUtils.load(resMap.get("SOUND/NOI42.SMP")));
		samples.put("SplitFleet", IOUtils.load(resMap.get("SOUND/NOI49.SMP")));
		samples.put("JoinFleet", IOUtils.load(resMap.get("SOUND/NOI50.SMP")));
		samples.put("NewFleetCreated", IOUtils.load(resMap.get("SOUND/NOI51.SMP")));
		samples.put("AddedToProductionList", IOUtils.load(resMap.get("SOUND/NOI52.SMP")));
		samples.put("DeletedFromProductionList", IOUtils.load(resMap.get("SOUND/NOI53.SMP")));
		samples.put("ResearchStarted", IOUtils.load(resMap.get("SOUND/NOI54.SMP")));
		samples.put("ResearchStopped", IOUtils.load(resMap.get("SOUND/NOI55.SMP")));
		samples.put("ItemsProduced", IOUtils.load(resMap.get("SOUND/NOI56.SMP")));
		samples.put("ResearchCompleted", IOUtils.load(resMap.get("SOUND/NOI57.SMP")));
		samples.put("SatelliteDestroyed", IOUtils.load(resMap.get("SOUND/NOI58.SMP")));
		samples.put("UnidentifiedShipDetected", IOUtils.load(resMap.get("SOUND/NOI59.SMP")));
		samples.put("PlanetRevolveInProgress", IOUtils.load(resMap.get("SOUND/NOI60.SMP")));
		samples.put("NewFleetDetected", IOUtils.load(resMap.get("SOUND/NOI61.SMP")));
		samples.put("CampaignRecordMessageNotNow", IOUtils.load(resMap.get("SOUND/NOI80.SMP")));
		samples.put("CampaignRecordMessage", IOUtils.load(resMap.get("SOUND/NOI81.SMP")));
		samples.put("PlaceBuilding", IOUtils.load(resMap.get("SOUND/NOI82.SMP")));
		samples.put("DemolishBuilding", IOUtils.load(resMap.get("SOUND/NOI83.SMP")));
		samples.put("WelcomeToIG", IOUtils.load(resMap.get("SOUND/NOI84.SMP")));
		samples.put("GoodBye", IOUtils.load(resMap.get("SOUND/NOI85.SMP")));
		samples.put("DiplomacyShow", IOUtils.load(resMap.get("SOUND/NOI86.SMP")));
		samples.put("DiplomacyHide", IOUtils.load(resMap.get("SOUND/NOI87.SMP")));
		samples.put("SoundTest", IOUtils.load(resMap.get("MUSIC/SAMPLE.SMP")));
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
