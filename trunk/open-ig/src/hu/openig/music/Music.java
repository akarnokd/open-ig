/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.music;

import hu.openig.utils.IOUtils;
import hu.openig.utils.ResourceMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Background music player.
 * 
 * @author karnokd, 2009.02.23.
 * @version $Revision 1.0$
 */
public class Music {
	/** The audio output line. */
	private SourceDataLine sdl;
	/** The background playback thread. */
	private volatile Thread playbackThread;
	/** Adjust master gain in dB. */
	private volatile float gain;
	/** Mute sound. */
	private volatile boolean mute;
	/** The resource manager. */
	private final ResourceMapper resMap;
	/** The clip for sound playback. */
	private volatile Clip soundClip;
	/** Use soundClip for playback. */
	private final boolean useClip = false;
	/** OGG music player. */
	private volatile OggMusic oggMusic;

	/**
	 * Constructor. Initializes the audio output.
	 * @param resMap the resource mapper
	 */
	public Music(ResourceMapper resMap) {
		this.resMap = resMap;
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
			setMasterGain(0);
		} catch (LineUnavailableException ex) {
		}
	}

	/**
	 * Start/continue the music playback.
	 */
	public void play() {
		if (sdl != null) {
			sdl.start();
		} else if (soundClip != null) {
			soundClip.start();
		} else if (oggMusic != null) {
			oggMusic.outputLine.start();
		}
	}

	/** Stop the music playback. */
	public void stop() {
		if (sdl != null) {
			sdl.stop();
		} else if (soundClip != null) {
			soundClip.stop();
		} else if (oggMusic != null) {
			oggMusic.outputLine.stop();
		}
	}

	/**
	 * Stop the music playback and close the playback thread.
	 */
	public void close() {
		if (sdl != null) {
			sdl.stop();
			sdl.drain();
			sdl.close();
			sdl = null;
		}
		if (soundClip != null) {
			soundClip.stop();
			soundClip.drain();
			soundClip.close();
			soundClip = null;
		}
		if (oggMusic != null) {
			oggMusic.close();
			oggMusic = null;
		}
		if (playbackThread != null) {
			playbackThread.interrupt();
			playbackThread = null;
		}
	}

	/**
	 * Play the given file list in the given sequence.
	 * 
	 * @param fileName the array of filenames to play
	 */
	public void playFile(final String... fileName) {
		stop();
		Thread th = playbackThread;
		if (th != null) {
			th.interrupt();
			playbackThread = null;
		}
		th = new Thread(null, new Runnable() {
			public void run() {
				playbackLoop(fileName);
			}
		}, "MusicPlayback-" + Arrays.toString(fileName));
		playbackThread = th;
		th.start();
	}

	/**
	 * The audio playback loop.
	 * 
	 * @param fileNames
	 *            the audio files to play back
	 */
	private void playbackLoop(String... fileNames) {
		int fails = 0;
		while (checkStop() && fails < fileNames.length) {
			for (String fileName : fileNames) {
				if (!checkStop()) {
					break;
				}
				if (useClip) {
					playBackClip(fileName);
				} else {
					try {
						if (fileName.toUpperCase().endsWith(".WAV")) {
							if (!playbackWav(fileName)) {
								fails++;
							}
						} else if (fileName.toUpperCase().endsWith(".OGG")) {
							if (!playbackOgg(fileName)) {
								fails++;
							}
						} else {
							fails++;
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
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
	 * @param fileName the file or resource to play
	 * @return true if the file was accessible
	 * @throws IOException on IO error
	 */
	private boolean playbackOgg(String fileName) throws IOException {
		InputStream raf = null;
		try {
			if (fileName.startsWith("res:")) {
				raf = Music.class.getResourceAsStream(fileName.substring(4));
			} else {
				raf = new FileInputStream(resMap.get(fileName));
			}
			if (raf != null) {
				oggMusic = new OggMusic(Thread.currentThread(), gain, mute);
				oggMusic.playOgg(raf);
				return true;
			} else {
				System.err.println("Music inaccessible: " + fileName);
			}
			return false;
		} finally {
			if (raf != null) {
				raf.close();
			}
		}
	}
	/**
	 * Plays back the given filename as a WAV file.
	 * @param fileName the wav file name to play
	 * @return true if the file is accessible
	 * @throws IOException if there is problem with the IO
	 */
	private boolean playbackWav(String fileName) throws IOException {
		InputStream raf = null;
		if (fileName.startsWith("res:")) {
			raf = Music.class.getResourceAsStream(fileName.substring(4));
		} else {
			raf = new FileInputStream(resMap.get(fileName));
		}
		if (raf != null) {
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
					if (fileName.startsWith("res:")) {
						raf = Music.class.getResourceAsStream(fileName.substring(4));
					} else {
						raf = new FileInputStream(resMap.get(fileName));
					}
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
		return false;
	}
	/**
	 * Play back music using the Clip object.
	 * @param fileName the file or resource to playback
	 */
	private void playBackClip(String fileName) {
		try {
			AudioInputStream soundStream = null;
			if (fileName.startsWith("res:")) {
				soundStream = AudioSystem.getAudioInputStream(Music.class.getResourceAsStream(fileName.substring(4)));
			} else {
				File audioFile = resMap.get(fileName);
				soundStream = AudioSystem.getAudioInputStream(audioFile);
			}
			AudioFormat streamFormat = soundStream.getFormat();
			DataLine.Info clipInfo = new DataLine.Info(Clip.class,
					streamFormat);

			Clip clip = (Clip) AudioSystem.getLine(clipInfo);
			soundClip = clip;
			clip.open(soundStream);
			clip.setLoopPoints(0, -1);
			setMasterGain(gain);
			setMute(mute);
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
	 * Set the master gain in dB.
	 * 
	 * @param gain
	 *            the gain to set
	 */
	public void setMasterGain(float gain) {
		if (sdl != null) {
			FloatControl f = (FloatControl) sdl
					.getControl(FloatControl.Type.MASTER_GAIN);
			f.setValue(gain);
		} else if (soundClip != null) {
			FloatControl f = (FloatControl) soundClip
					.getControl(FloatControl.Type.MASTER_GAIN);
			f.setValue(gain);
		} else if (oggMusic != null) {
			FloatControl f = (FloatControl) oggMusic.outputLine
			.getControl(FloatControl.Type.MASTER_GAIN);
			f.setValue(gain);
		}
		this.gain = gain;
	}

	/**
	 * @return the gain
	 */
	public float getGain() {
		return gain;
	}

	/**
	 * Mute or unmute the sound.
	 * 
	 * @param mute
	 *            the mute to set
	 */
	public void setMute(boolean mute) {
		if (sdl != null) {
			BooleanControl b = (BooleanControl) sdl
					.getControl(BooleanControl.Type.MUTE);
			b.setValue(mute);
		} else if (soundClip != null) {
			BooleanControl b = (BooleanControl) soundClip
					.getControl(BooleanControl.Type.MUTE);
			b.setValue(mute);
		} else if (oggMusic != null && oggMusic.outputLine != null) {
			BooleanControl b = (BooleanControl) oggMusic.outputLine
					.getControl(BooleanControl.Type.MUTE);
			b.setValue(mute);
		}
		this.mute = mute;
	}

	/**
	 * @return the mute
	 */
	public boolean isMute() {
		return mute;
	}
}
