package hu.openig.sound;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Asynchronous thread to play audio data in parallel of the
 * rendered images. Send an empty array or interrupt to
 * close this thread. Will start and stop the playback automatically
 * @author karnokd, 2009.01.11.
 * @version $Revision 1.0$
 */
public class AudioThread extends Thread {
	/** The queue for asynchronus music play. */
	private final BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
	/** The output audio line. */
	private final SourceDataLine sdl;
	/** The start semaphore. */
	private final Lock lock = new ReentrantLock();
	/** The start condition. */
	private final Condition startCond = lock.newCondition();
	/** Start audio playback flag. */
	private boolean startAudio;
	/**
	 * Constructor. Initializes the audio output to 22050Hz, 8 bit PCM.
	 */
	public AudioThread() {
		sdl = createAudioOutput();
	}
	/**
	 * The main loop to enqueue and play audio.
	 */
	@Override
	public void run() {
		try {
			while (!isInterrupted()) {
				byte[] data = queue.take();
				if (data.length == 0) {
					break;
				}
				if (!sdl.isActive()) {
					// wait for start playing signal
					lock.lock();
					try {
						while (!startAudio) {
							startCond.await();
						}
						startAudio = false;
						sdl.start();
					} finally {
						lock.unlock();
					}
				}
				synchronized(sdl) {
					sdl.write(data, 0, data.length);
				}
			}
		} catch (InterruptedException ex) {
			// time to quit;
			interrupt();
		} finally {
			if (sdl != null) {
				sdl.stop();
				sdl.drain();
				sdl.close();
			}
		}
	}
	/**
	 * Creates the audio output stream. The returned output
	 * stream is opened but not started.
	 * @return the created audio output stream or null if no audio support
	 * is available.
	 */
	public static SourceDataLine createAudioOutput() {
		AudioFormat af = new AudioFormat(22050, 8, 1, true, false);
		SourceDataLine sdl = null;
		DataLine.Info dli = new DataLine.Info(SourceDataLine.class, af);
		if (!AudioSystem.isLineSupported(dli)) {
			return null;
		}
		try {
			sdl = (SourceDataLine)AudioSystem.getLine(dli);
			sdl.open(af);
			return sdl;
		} catch (LineUnavailableException ex) {
			return null;
		}
	}
	/**
	 * Send an audio sample to the audio player.
	 * Can be called from any thread.
	 * @param data the non null data to send
	 */
	public void submit(byte[] data) {
		queue.offer(data);
	}
	/**
	 * Stops the playback immediately.
	 */
	public void stopPlaybackNow() {
		synchronized(sdl) {
			sdl.stop();
			sdl.drain();
		}
	}
	/**
	 * Starts the playback immediately.
	 */
	public void startPlaybackNow() {
		lock.lock();
		try {
			startAudio = true;
			startCond.signalAll();
		} finally {
			lock.unlock();
		}
	}
}