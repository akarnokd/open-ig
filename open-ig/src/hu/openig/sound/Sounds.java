/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.sound;

import hu.openig.core.ResourceLocator;
import hu.openig.core.ResourceType;
import hu.openig.model.SoundType;
import hu.openig.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author akarnokd, Apr 18, 2011
 */
public class Sounds {
	/** The list of audio outputs. */
	final List<SourceDataLine> outputs = new ArrayList<SourceDataLine>();
	/** The available sound output lines. */
	final BlockingQueue<SourceDataLine> available = new LinkedBlockingQueue<SourceDataLine>();
	/** The sound map. */
	final Map<SoundType, byte[]> soundMap = new HashMap<SoundType, byte[]>();
	/** The sound pool. */
	ExecutorService exec;
	/**
	 * Initialize the sound pool.
	 * @param rl the resource locator
	 */
	public Sounds(ResourceLocator rl) {
		for (SoundType st : SoundType.values()) {
			try {
				AudioInputStream ain = AudioSystem.getAudioInputStream(new ByteArrayInputStream(rl.get(st.resource, ResourceType.AUDIO).get()));
				try {
					byte[] snd = IOUtils.load(ain);
					// signify the
					if (ain.getFormat().getEncoding() == Encoding.PCM_UNSIGNED) {
						for (int i = 0; i < snd.length; i++) {
							snd[i] = (byte)((snd[i] & 0xFF) - 128);
						}
					}
					soundMap.put(st, snd);
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
	 * @param initialVolume the initial effect volume
	 */
	public void initialize(int channels, int initialVolume) {
		for (int i = 0; i < channels; i++) {
			AudioFormat af = new AudioFormat(22050, 16, 1, true, false);
			DataLine.Info dli = new DataLine.Info(SourceDataLine.class, af);
			if (AudioSystem.isLineSupported(dli)) {
				try {
					SourceDataLine sdl = (SourceDataLine)AudioSystem.getLine(dli);
					sdl.open(af);
					outputs.add(sdl);
					available.add(sdl);
				} catch (LineUnavailableException ex) {
					ex.printStackTrace();
				}
			} else {
				System.err.println("No 22050/16/1 audio support for effects");
			}
		}
		setVolume(initialVolume);
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
		for (SourceDataLine sdl : outputs) {
			sdl.stop();
			sdl.close();
		}
		outputs.clear();
		available.clear();
		if (exec != null) {
			exec.shutdown();
		}
	}
	/**
	 * Mute or unmute the current playback.
	 * @param mute the mute status
	 */
	public void setMute(boolean mute) {
		for (SourceDataLine sdl : outputs) {
			BooleanControl bc = (BooleanControl)sdl.getControl(BooleanControl.Type.MUTE);
			if (bc != null) {
				bc.setValue(mute);
			}
		}
	}
	/**
	 * Set the linear volume.
	 * @param volume the volume 0..100, volume 0 mutes the sound
	 */
	public void setVolume(int volume) {
		if (volume == 0) {
			setMute(true);
		} else {
			setMute(false);
			for (SourceDataLine sdl : outputs) {
				FloatControl fc = (FloatControl)sdl.getControl(FloatControl.Type.MASTER_GAIN);
				if (fc != null) {
					fc.setValue(AudioThread.computeGain(fc, volume));
				}
			}
		}
	}
	/**
	 * Play the given sound effect.
	 * @param effect the sound to play
	 */
	public void play(final SoundType effect) {
		final SourceDataLine sdl = available.poll();
		if (sdl != null) {
			exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						byte[] data = soundMap.get(effect);
						sdl.start();
						sdl.write(data, 0, data.length);
						sdl.drain();
					} finally {
						available.add(sdl);
					}
				}
			});
		}
	}
}
