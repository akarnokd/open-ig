/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.music;

import hu.openig.core.Action1;
import hu.openig.core.Func0;
import hu.openig.core.ResourceLocator;
import hu.openig.core.ResourceLocator.ResourcePlace;
import hu.openig.core.ResourceType;
import hu.openig.sound.AudioThread;
import hu.openig.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;

/**
 * Background music player.
 * 
 * @author akarnokd, 2009.02.23.
 */
public class Music {
	/** The audio output line. */
	private SourceDataLine sdl;
	/** The background playback thread. */
	private volatile Thread playbackThread;
	/** The resource manager. */
	private final ResourceLocator rl;
	/** The clip for sound playback. */
	private volatile Clip soundClip;
	/** Use soundClip for playback. */
	private final boolean useClip = false;
	/** OGG music player. */
	private volatile OggMusic oggMusic;
	/** The completion handler once a sound element completed. */
	public Action1<String> onComplete;

	/**
	 * Constructor.
	 * @param rl the resource locator instance.
	 */
	public Music(ResourceLocator rl) {
		this.rl = rl;
	}
	/** Initialize wave playback format. */
	private void initWave() {
		AudioFormat af = new AudioFormat(22050, 16, 1, true, false);
		DataLine.Info dli = new DataLine.Info(SourceDataLine.class, af);
		if (!AudioSystem.isLineSupported(dli)) {
			return;
		}
		try {
			sdl = (SourceDataLine) AudioSystem.getLine(dli);
			sdl.open(af);
		} catch (LineUnavailableException ex) {
		}
	}

	/**
	 * Start/continue the music playback.
	 */
	public void play() {
		if (sdl != null) {
			sdl.start();
		} else 
		if (soundClip != null) {
			soundClip.start();
		} else 
		if (oggMusic != null) {
			oggMusic.outputLine.start();
		}
	}

	/** Stop the music playback. */
	public void stop() {
		Thread th = playbackThread;
		if (th != null) {
			th.interrupt();
		}
		if (sdl != null) {
			sdl.close();
		} else 
		if (soundClip != null) {
			soundClip.close();
		} else 
		if (oggMusic != null) {
			oggMusic.outputLine.close();
		}
	}

	/**
	 * Stop the music playback and close the playback thread.
	 */
	public void close() {
		Thread th = playbackThread;
		if (th != null) {
			th.interrupt();
			playbackThread = null;
		}
		if (sdl != null) {
			sdl.close();
			sdl = null;
		}
		if (soundClip != null) {
			soundClip.close();
			soundClip = null;
		}
		if (oggMusic != null) {
			oggMusic.close();
			oggMusic = null;
		}
	}

	/**
	 * Play the given file list in the given sequence repeatedly.
	 * 
	 * @param fileName the array of filenames to play
	 * @param volume the initial playback volume
	 */
	public void playLooped(final Func0<Integer> volume, final String... fileName) {
		stop();
		Thread th = playbackThread;
		if (th != null) {
			th.interrupt();
			playbackThread = null;
		}
		th = new Thread(null, new Runnable() {
			@Override 
			public void run() {
				playbackLoop(volume, fileName);
			}
		}, "MusicPlaybackL-" + Arrays.toString(fileName));
		playbackThread = th;
		th.start();
	}
	/**
	 * Play the given file list in the given sequence once.
	 * 
	 * @param fileName the array of filenames to play
	 * @param volume the initial playback volume
	 */
	public void playSequence(final Func0<Integer> volume, final String... fileName) {
		stop();
		Thread th = playbackThread;
		if (th != null) {
			th.interrupt();
			playbackThread = null;
		}
		th = new Thread(null, new Runnable() {
			@Override 
			public void run() {
				playbackSequence(volume, fileName);
			}
		}, "MusicPlaybackS-" + Arrays.toString(fileName));
		playbackThread = th;
		th.start();
	}

	/**
	 * The audio playback loop.
	 * 
	 * @param fileNames
	 *            the audio files to play back
   	 * @param volume the initial playback volume
	 */
	private void playbackLoop(final Func0<Integer> volume, String... fileNames) {
		int fails = 0;
		while (checkStop() && fails < fileNames.length) {
			fails += playbackSequence(volume, fileNames);
		}
	}
	/**
	 * Play the sequence of audio data and return when all completed.
	 * @param volumeFunc the audio volume function
	 * @param fileNames the list of resource names to get
	 * @return the failure count
	 */
	int playbackSequence(final Func0<Integer> volumeFunc, String... fileNames) {
		int fails = 0;
		for (final String name : fileNames) {
			try {
				ResourcePlace rp = rl.get(name, ResourceType.AUDIO);
				String fileName = rp.getFileName();
				if (!checkStop()) {
					break;
				}
				int volume = volumeFunc.invoke();
				if (useClip) {
					playBackClip(rp, volume);
				} else {
					try {
						if (fileName.toUpperCase().endsWith(".WAV")) {
							if (!playbackWav(rp, volume)) {
								fails++;
							}
						} else 
						if (fileName.toUpperCase().endsWith(".OGG")) {
							if (!playbackOgg(rp, volume)) {
								fails++;
							}
						} else {
							fails++;
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			} finally {
				if (onComplete != null) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							onComplete.invoke(name);
							onComplete = null;
						}
					});
				}
			}
		}
		return fails;
	}
	/**
	 * Returns true if the playback can continue.
	 * @return true if the playback can continue
	 */
	private boolean checkStop() {
		return playbackThread == Thread.currentThread()
		&& !Thread.currentThread().isInterrupted();
	}
	/**
	 * Plays back the given filename as an OGG audio file.
	 * @param res the resource place representing the music
	 * @param volume the initial playback volume
	 * @return true if the file was accessible
	 * @throws IOException on IO error
	 */
	private boolean playbackOgg(ResourcePlace res, int volume) throws IOException {
		InputStream raf = res.openNew();
		try {
			oggMusic = new OggMusic(Thread.currentThread(), volume);
			oggMusic.playOgg(raf);
			return true;
		} finally {
			if (raf != null) {
				raf.close();
			}
		}
	}
	/**
	 * Plays back the given filename as a WAV file.
	 * @param rp the resource place
	 * @param volume the initial playback volume
	 * @return true if the file is accessible
	 * @throws IOException if there is problem with the IO
	 */
	private boolean playbackWav(ResourcePlace rp, int volume) throws IOException {
		InputStream raf = rp.openNew();
		// skip chunks
		try {
			long startOffset = findData(raf);
			raf.close();
			byte[] buffer = new byte[16384];
			// compensate for signed
			// playback loop
			initWave();
			sdl.start();
			while (checkStop()) {
				raf = rp.openNew();
				// skip wav header
				IOUtils.skipFully(raf, startOffset);
				int read = 0;
				do {
					read = raf.read(buffer);
					if (read > 0) {
						// signifySound(buffer, read);
						sdl.write(buffer, 0, read);
					}
				} while (checkStop() && read >= 0);
			}
		} finally {
			raf.close();
		}
		return true;
	}
	/**
	 * Play back music using the Clip object.
	 * @param rp the resource to play back
	 * @param volume the initial playback volume
	 */
	private void playBackClip(ResourcePlace rp, int volume) {
		try {
			AudioInputStream soundStream = AudioSystem.getAudioInputStream(rp.openNew());
			
			AudioFormat streamFormat = soundStream.getFormat();
			DataLine.Info clipInfo = new DataLine.Info(Clip.class,
					streamFormat);

			Clip clip = (Clip) AudioSystem.getLine(clipInfo);
			soundClip = clip;
			clip.open(soundStream);
			clip.setLoopPoints(0, -1);
			setVolume(volume);
			clip.start();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Finds the 'data' chunk in a RIFF wav file.
	 * 
	 * @param raf
	 *            the random access file
	 * @return the offset of the actual data chunk
	 * @throws IOException
	 *             if an IO error occurs
	 */
	static long findData(InputStream raf) throws IOException {
		IOUtils.skipFully(raf, 12);
		long offset = 12;
		while (true) {
			int type = IOUtils.readIntLE(raf);
			offset += 4;
			if (type == 0x61746164) {
				IOUtils.skipFully(raf, 4);
				return offset + 4;
			} else {
				int count = IOUtils.readIntLE(raf);
				IOUtils.skipFully(raf, count);
				offset += count;
			}
		}
	}
	/**
	 * Finds the 'data' chunk in a RIFF wav file.
	 * 
	 * @param raf
	 *            the random access file
	 * @return the offset of the actual data chunk
	 * @throws IOException
	 *             if an IO error occurs
	 */
	static long findData(RandomAccessFile raf) throws IOException {
		raf.seek(12);
		long offset = 12;
		while (true) {
			int type = IOUtils.readIntLE(raf);
			offset += 4;
			if (type == 0x61746164) {
				IOUtils.skipFullyD(raf, 4);
				return offset + 4;
			} else {
				int count = IOUtils.readIntLE(raf);
				IOUtils.skipFullyD(raf, count);
				offset += count;
			}
		}
	}
	/**
	 * Set the linear volume.
	 * @param volume the volume 0..100, volume 0 mutes the sound
	 */
	public void setVolume(int volume) {
		if (sdl != null) {
			AudioThread.setVolume(sdl, volume);
		} else 
		if (soundClip != null) {
			AudioThread.setVolume(soundClip, volume);
		} else 
		if (oggMusic != null && oggMusic.outputLine != null) {
			AudioThread.setVolume(oggMusic.outputLine, volume);
		}
	}
	/** @return is a music playing? */
	public boolean isRunning() {
		return (sdl != null && sdl.isActive()) || (soundClip != null && soundClip.isActive()) || (oggMusic != null && oggMusic.outputLine != null && oggMusic.outputLine.isActive());
	}
}
