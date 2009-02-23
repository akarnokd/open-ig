package hu.openig.music;

import hu.openig.compress.CompUtils;
import hu.openig.utils.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

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
	/** The root directory. */
	private final String root;
	/** The clip for sound playback. */
	private volatile Clip soundClip;
	/** Use soundClip for playback. */
	private final boolean useClip = true;
	/**
	 * Constructor. Initializes the audio output.
	 * @param root the root directory
	 */
	public Music(String root) {
		this.root = root;
		if (!useClip) {
			AudioFormat af = new AudioFormat(22050, 16, 1, true, false);
			DataLine.Info dli = new DataLine.Info(SourceDataLine.class, af);
			if (!AudioSystem.isLineSupported(dli)) {
				return;
			}
			try {
				sdl = (SourceDataLine)AudioSystem.getLine(dli);
				sdl.open(af);
				setGain(0);
			} catch (LineUnavailableException ex) {
			}
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
		}
	}
	/** Stop the music playback. */
	public void stop() {
		if (sdl != null) {
			sdl.stop();
		} else
		if (soundClip != null) {
			soundClip.stop();
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
		}
		if (soundClip != null) {
			soundClip.stop();
			soundClip.drain();
			soundClip.close();
		}
		if (playbackThread != null) {
			playbackThread.interrupt();
			playbackThread = null;
		}
	}
	/**
	 * Play the given file.
	 * @param fileName
	 */
	public void playFile(final String fileName) {
		stop();
		Thread th = playbackThread;
		if (th != null) {
			th.interrupt();
			playbackThread = null;
		}
		th = new Thread(new Runnable() {
			public void run() {
				if (sdl != null || useClip) {
					playbackLoop(fileName);
				}
			}
		});
		playbackThread = th;
		th.start();
	}
	/**
	 * The audio playback loop.
	 * @param fileName the audio file to play back
	 */
	private void playbackLoop(String fileName) {
		if (useClip) {
	        try { 
	            File audioFile = new File(root + "/" + fileName);
	            AudioInputStream soundStream = AudioSystem.getAudioInputStream(audioFile);
	            AudioFormat streamFormat = soundStream.getFormat();
	            DataLine.Info clipInfo = new DataLine.Info(Clip.class, streamFormat);
	 
	            Clip clip = (Clip)AudioSystem.getLine(clipInfo);
	            soundClip = clip;
	            clip.open(soundStream);
	            clip.setLoopPoints(0, -1);
	            clip.loop(Clip.LOOP_CONTINUOUSLY);
	            setGain(gain);
	            setMute(mute);
	            clip.start();
	        } catch ( UnsupportedAudioFileException e ) { 
	        	e.printStackTrace();
	        } catch ( IOException e ) { 
	        	e.printStackTrace();
	        } catch ( LineUnavailableException e ) {
	        	e.printStackTrace();
	        }
		} else {
			try {
				RandomAccessFile raf = new RandomAccessFile(root + "/" + fileName, "r");
				
				// skip chunks
				long startOffset = findData(raf);
				byte[] buffer = new byte[16384];
				// compensate for signed
				// playback loop
				sdl.start();
				while (playbackThread == Thread.currentThread() 
						&& !Thread.currentThread().isInterrupted()) {
					// skip wav header
					raf.seek(startOffset);
					int read = 0;
					do {
						read = raf.read(buffer);
						if (read > 0) {
							//signifySound(buffer, read);
							sdl.write(buffer, 0, read);
						}
					} while (playbackThread == Thread.currentThread() 
						&& !Thread.currentThread().isInterrupted() && read >= 0);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}
	/**
	 * Finds the 'data' chunk in a RIFF wav file.
	 * @param raf the random access file
	 * @return the offset of the actual data chunk
	 * @throws IOException if an IO error occurs
	 */
	long findData(RandomAccessFile raf) throws IOException {
		raf.seek(12);
		while (raf.getFilePointer() < raf.length()) {
			int type = raf.readInt();
			if (type == 0x64617461) {
				raf.readInt();
				return raf.getFilePointer();
			} else {
				int count = rotate(raf.readInt());
				raf.seek(raf.getFilePointer() + count);
			}
		}
		return raf.length();
	}
	/**
	 * Signifiy the 16 bit sound data.
	 * @param buffer the buffer
	 */
	void signifySound(byte[] buffer, int len) {
		for (int i = 0; i < len; i += 2) {
			short v = (short)((buffer[i] & 0xFF) | (buffer[i + 1] & 0xFF) << 8);
			v -= 32768;
			buffer[i] = (byte)(v & 0xFF);
			buffer[i + 1] = (byte)((v & 0xFF00) >> 8);
		}
	}
	/**
	 * Switch between big endian and little endian byte order
	 * @param val the value to switch
	 * @return the switched value
	 */
	private static int rotate(int val) {
		return (val & 0xFF000000) >> 24 | (val & 0xFF0000) >> 8 
		| (val & 0xFF00) << 8 | (val & 0xFF) << 24;
	}
	/**
	 * Convert the audio.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	static void convertMusic() throws FileNotFoundException,
			IOException {
		File f = new File("music2.wav");
		byte[] sound = new byte[(int)(f.length() - 0x2C)];
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		raf.seek(0x2C);
		raf.read(sound);
		raf.close();
		sound = CompUtils.interleave16(CompUtils.difference16(sound));
		GZIPOutputStream zout = new GZIPOutputStream(new FileOutputStream("music2.snd"));
		zout.write(sound);
		zout.close();
	}
	/** Find the minimum of repeated compression loops. */
	public static void compressLoop(byte[] sound) {
		byte[] comp;
		double ratio = 1;
		byte[] inp = sound;
		int loop = 1;
		while (true) {
			  byte[] diff = CompUtils.difference16(inp);
			  comp = CompUtils.compressGZIP(diff);
			  double newRatio = comp.length / (double)inp.length;
			  System.out.printf("Compress %d: %d -> %d, %.2f%%%n", loop, inp.length, comp.length, newRatio * 100);
			  if (newRatio > 1 || (Math.abs(newRatio - ratio) < 0.001)) {
				  break;
			  }
			  ratio = newRatio;
			  // re differentiate
			  inp = diff;
			  loop++;
		}
	}
	/**
	 * Test compressions.
	 */
	public static void testCompress() {
		byte[] orig = IOUtils.load("music1.wav");
		byte[] sound = Arrays.copyOfRange(orig, 0x2C, orig.length);
		System.out.printf("Original: %d%n", sound.length);
		byte[] comp;
		byte[] rest;
		long time;
		time = System.nanoTime();
		comp = CompUtils.compress8(sound);
		System.out.printf("Compress8: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
		
		time = System.nanoTime();
		rest = CompUtils.decompress8(comp);
		System.out.printf("Decompress8: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
		if (!Arrays.equals(sound, rest)) {
			System.err.println("Differs!");
		}

		time = System.nanoTime();
		comp = CompUtils.compress16(sound);
		System.out.printf("Compress16: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
		
		time = System.nanoTime();
		rest = CompUtils.decompress16(comp);
		System.out.printf("Decompress16: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
		if (!Arrays.equals(sound, rest)) {
			System.err.println("Differs!");
		}
		
		time = System.nanoTime();
		comp = CompUtils.compressGZIP(sound);
		System.out.printf("CompressGZIP: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
		
		time = System.nanoTime();
		rest = CompUtils.decompressGZIP(comp);
		System.out.printf("DecompressGZIP: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
		if (!Arrays.equals(sound, rest)) {
			System.err.println("Differs!");
		}
		
		time = System.nanoTime();
		comp = CompUtils.compressGZIP(CompUtils.interleave16(CompUtils.difference16(sound.clone())));
		System.out.printf("Compress16+IL: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
		
		time = System.nanoTime();
		rest = CompUtils.undifference16(CompUtils.uninterleave16(CompUtils.decompressGZIP(comp)));
		System.out.printf("Decompress16+IL: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
		if (!Arrays.equals(sound, rest)) {
			System.err.println("Differs!");
		}
		
		time = System.nanoTime();
		comp = CompUtils.compressGZIP(CompUtils.difference8(CompUtils.interleave16(CompUtils.difference16(sound.clone()))));
		System.out.printf("Compress16+IL+D8: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
		
		time = System.nanoTime();
		rest = CompUtils.undifference16(CompUtils.uninterleave16(CompUtils.undifference8(CompUtils.decompressGZIP(comp))));
		System.out.printf("Decompress16+IL+D8: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
		if (!Arrays.equals(sound, rest)) {
			System.err.println("Differs!");
		}

		time = System.nanoTime();
		comp = CompUtils.rle(CompUtils.interleave16(sound));
		System.out.printf("RLE: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
		
		time = System.nanoTime();
		rest = CompUtils.uninterleave16(CompUtils.unRle(comp));
		System.out.printf("UNRLE: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
		if (!Arrays.equals(sound, rest)) {
			System.err.println("Differs!");
		}
		//compressLoop(sound);
		time = System.nanoTime();
		comp = CompUtils.compressGZIP(CompUtils.rle(CompUtils.interleave16(sound)));
		System.out.printf("RLE+Gzip: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
		
		time = System.nanoTime();
		rest = CompUtils.uninterleave16(CompUtils.unRle(CompUtils.decompressGZIP(comp)));
		System.out.printf("UNRLE+Gzip: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
		if (!Arrays.equals(sound, rest)) {
			System.err.println("Differs!");
		}
	}
	/**
	 * Set the master gain in dB.
	 * @param gain the gain to set
	 */
	public void setGain(float gain) {
		if (sdl != null) {
			FloatControl f = (FloatControl)sdl.getControl(FloatControl.Type.MASTER_GAIN);
			f.setValue(gain);
		} else
		if (soundClip != null) {
			FloatControl f = (FloatControl)soundClip.getControl(FloatControl.Type.MASTER_GAIN);
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
	 * Mute or unmute the sound
	 * @param mute the mute to set
	 */
	public void setMute(boolean mute) {
		if (sdl != null) {
			BooleanControl b = (BooleanControl)sdl.getControl(BooleanControl.Type.MUTE);
			b.setValue(mute);
		} else
		if (soundClip != null) {
			BooleanControl b = (BooleanControl)soundClip.getControl(BooleanControl.Type.MUTE);
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
