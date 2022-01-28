/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.sound;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Asynchronous thread to play audio data in parallel of the
 * rendered images. Send an empty array or interrupt to
 * close this thread. Will start and stop the playback automatically
 * @author akarnokd, 2009.01.11.
 */
public class AudioThread extends Thread {
    /** The audio data configuration. */
    public static class Data {
        /** The data bytes. */
        public final byte[] data;
        /** The data needs to be upscaled to 16 bit. */
        public final boolean upscale;
        /**
         * Constructor.
         * @param data the data bytes
         * @param upscale need upscaling?
         */
        public Data(byte[] data, boolean upscale) {
            this.data = data;
            this.upscale = upscale;
        }
    }
    /** The queue for asynchronous music play. */
    private final BlockingQueue<Data> queue;
    /** The output audio line. */
    private final SourceDataLine sdl;
    /** The start semaphore. */
    private final Lock lock = new ReentrantLock();
    /** The start condition. */
    private final Condition startCond = lock.newCondition();
    /** Start audio playback flag. */
    private volatile boolean startAudio;
    /**
     * If audio playback is delayed, setting this flag to true will terminate the playback loop.
     */
    private volatile boolean stop;
    /**
     * Constructor. Initializes the audio output to 22050Hz, 8 bit PCM.
     */
    public AudioThread() {
        super("AudioPlayback");
        queue = new LinkedBlockingQueue<>();
        sdl = createAudioOutput();
    }
    /**
     * Constructor. Use the given queue.
     * @param idx the index
     * @param queue the queue to use
     */
    public AudioThread(int idx, BlockingQueue<Data> queue) {
        super("AudioPlayback-" + idx);
        this.queue = queue;
        sdl = createAudioOutput();
    }
    /**
     * The main loop to enqueue and play audio.
     */
    @Override
    public void run() {
        try {
            while (!stop && !isInterrupted()) {
                Data data = queue.take();
                if (data.data.length == 0) {
                    sdl.drain();
                    break;
                }
                if (!sdl.isActive()) {
                    // wait for start playing signal
                    lock.lock();
                    try {
                        while (!startAudio && !stop) {
                            startCond.await();
                        }
                        if (stop) {
                            stop = false;
                            break;
                        }
                        startAudio = false;
                        sdl.start();
                    } finally {
                        lock.unlock();
                    }
                }
                if (data.upscale) {
                    byte[] data16 = convert8To16(data.data); //split16To8(movingAverage(upscale8To16(data), ));
                    sdl.write(data16, 0, data16.length);
                } else {
                    sdl.write(data.data, 0, data.data.length);
                }
//                sdl.drain();
            }
        } catch (InterruptedException ex) {
            // time to quit;
            interrupt();
        } finally {
            if (sdl != null) {
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
        SourceDataLine sdl;
        AudioFormat af = createAudioFormat16();
        DataLine.Info dli = createAudioInfo(af);
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
     * The audio line specification.
     * @param af the audio format
     * @return the appropriate DataLine.Info object
     */
    public static DataLine.Info createAudioInfo(AudioFormat af) {
        return new DataLine.Info(SourceDataLine.class, af);
    }
//    /**
//     * Create the default audio format.
//     * @return the audio format
//     */
//    public static AudioFormat createAudioFormat() {
//        return new AudioFormat(22050, 8, 1, true, false);
//    }
    /**
     * Create the default audio format.
     * @return the audio format
     */
    public static AudioFormat createAudioFormat16() {
        return new AudioFormat(22050, 16, 1, true, false);
    }
    /**
     * Upsample the audio data from signed 8 bits to signed 16 bits.
     * @param data the source data
     * @return the conversion result
     */
    public static byte[] convert8To16(byte[] data) {
        byte[] result = new byte[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            int s = data[i] * 256;
            result[i * 2] = (byte)(s & 0xFF);
            result[i * 2 + 1] = (byte)((s & 0xFF00) >> 8);
        }
        return result;
    }
    /**
     * Upscale the 8 bit signed values to 16 bit signed values.
     * @param data the data to upscale
     * @return the upscaled data
     */
    public static short[] upscale8To16(byte[] data) {
        short[] result = new short[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (short)(data[i] * 256);
        }
        return result;
    }
    /**
     * Split the 16 bit signed values into little endian byte array format.
     * @param data the data to split
     * @return the splitted data
     */
    public static byte[] split16To8(short[] data) {
        byte[] result = new byte[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            result[i * 2] = (byte)(data[i] & 0xFF);
            result[i * 2 + 1] = (byte)((data[i] & 0xFF00) >> 8);
        }
        return result;
    }
    /**
     * Calculates the moving average of the given sample data using the defined window.
     * @param data the data to be smoothed by moving average
     * @param windowSize the averaging window size
     * @return the transformed data
     */
    public static short[] movingAverage(short[] data, int windowSize) {
        if (windowSize == 1) {
            return data.clone();
        }
        short[] result = new short[data.length];
        int acc = 0;
        for (int i = 0, count = windowSize / 2; i < count; i++) {
            acc += data[i];
        }
        result[0] = (short)(acc / windowSize);
        for (int i = 1, count = data.length; i < count; i++) {
            acc = acc + (i < data.length ? data[i] : 0) - ((i - windowSize / 2 - 1 >= 0) ? data[i - windowSize / 2 - 1] : 0);
            result[i] = (short)(acc / windowSize);
        }
        return result;
    }
    /**
     * Send an audio sample to the audio player.
     * Can be called from any thread.
     * @param data the non null data to send
     * @param upscale the data needs to be upscaled to 16 bits?
     */
    public void submit(byte[] data, boolean upscale) {
        if (!queue.offer(new Data(data, upscale))) {
            throw new AssertionError("Queue problems");
        }
    }
    /**
     * Stops the playback immediately.
     */
    public void stopPlaybackNow() {
        queue.clear();
        sdl.close();
        // should always return true
        stopPlayback();
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
    /**
     * Stops the playback.
     */
    public void stopPlayback() {
        if (!queue.offer(new Data(new byte[0], false))) {
            throw new AssertionError("Queue problems");
        }
        lock.lock();
        try {
            stop = true;
            startCond.signalAll();
        } finally {
            lock.unlock();
        }
        interrupt();
    }
    /**
     * Set the master gain on the source data line.
     * @param gain the master gain in decibels, typically -80 to 0.
     */
    public void setMasterGain(float gain) {
        FloatControl fc = (FloatControl)sdl.getControl(FloatControl.Type.MASTER_GAIN);
        fc.setValue(gain);
    }
    /**
     * Mute or unmute the current playback.
     * @param mute the mute status
     */
    public void setMute(boolean mute) {
        BooleanControl bc = (BooleanControl)sdl.getControl(BooleanControl.Type.MUTE);
        if (bc != null) {
            bc.setValue(mute);
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
            FloatControl fc = (FloatControl)sdl.getControl(FloatControl.Type.MASTER_GAIN);
            if (fc != null) {
                fc.setValue(computeGain(fc, volume));
            }
        }
    }
    /**
     * Compute the gain of the given gain control based on a linear mapping of the
     * volume.
     * @param fc The target float control
     * @param volume the linear volume of 0..100
     * @return the gain value
     */
    public static float computeGain(FloatControl fc, int volume) {
//        double minLinear = Math.pow(10, fc.getMinimum() / 20);
//        double maxLinear = Math.pow(10, fc.getMaximum() / 20);
//        return (float)(20 * Math.log10(minLinear + volume * (maxLinear - minLinear) / 100));
//        return fc.getMinimum() + (fc.getMaximum() - fc.getMinimum()) * volume / 100;
        float min = Math.max(fc.getMinimum(), -43);
        float max = Math.min(fc.getMaximum(), 0);
        return min + (max - min) * volume / 100;
    }
    /**
     * Set the volume on an audio line by trying various controls.
     * @param line the data line
     * @param volume the volume between 0 and 100. 0 means mute
     */
    public static void setVolume(Line line, int volume) {
        boolean muteFailed = false;
        try {
            if (line.isControlSupported(BooleanControl.Type.MUTE)) {
                BooleanControl bc = (BooleanControl)line.getControl(BooleanControl.Type.MUTE);
                bc.setValue(volume == 0);
            } else {
                muteFailed = volume == 0;
            }
        } catch (Throwable ex) {
            // some linux implementation throws exception
            muteFailed = volume == 0;
            System.out.println(ex);
        }
        try {
            // try master gain
            if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl fc = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
                if (muteFailed) {
                    // set to the lowest gain possible
                    fc.setValue(fc.getMinimum());
                } else {
                    fc.setValue(computeGain(fc, volume));
                }
                return;
            }
        } catch (Exception ex) {
            // some linux implementation throws exception, give up
        }
        try {
            // try volume
            if (line.isControlSupported(FloatControl.Type.VOLUME)) {
                FloatControl fc = (FloatControl)line.getControl(FloatControl.Type.VOLUME);
                if (muteFailed) {
                    fc.setValue(fc.getMinimum());
                } else {
                    float low = fc.getMinimum();
                    float high = fc.getMaximum();
                    fc.setValue(low + (high - low) * volume / 100);
                }
            }
        } catch (Exception ex) {
            // some linux implementation throws exception, give up
        }
    }
}
