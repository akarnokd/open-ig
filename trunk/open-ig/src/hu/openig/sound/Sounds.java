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
import hu.openig.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author akarnokd, Apr 18, 2011
 */
public class Sounds {
	/** The sound pool. */
	final List<AudioThread> pool = new ArrayList<AudioThread>();
	/** The sound map. */
	final Map<SoundType, byte[]> soundMap = new HashMap<SoundType, byte[]>();
	/** The sound queue. */
	final BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
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
	 * Initialize the audio pool.
	 * @param poolSize the pool size
	 */
	public void start(int poolSize) {
		for (int i = 0; i < poolSize; i++) {
			AudioThread at = new AudioThread(i, queue);
			at.startPlaybackNow();
			at.start();
			pool.add(at);
		}
	}
	/** Terminate the audio pool. */
	public void stop() {
		for (AudioThread at : pool) {
			at.stopPlayback();
		}
		pool.clear();
		queue.clear();
	}
	/**
	 * Set the volume of the audio pool.
	 * @param volume the volume 0..100
	 */
	public void setVolume(int volume) {
		for (AudioThread at : pool) {
			at.setVolume(volume);
		}
	}
	/**
	 * Play the given sound effect.
	 * @param sound the sound to play
	 */
	public void play(SoundType sound) {
		queue.add(soundMap.get(sound));
	}
}
