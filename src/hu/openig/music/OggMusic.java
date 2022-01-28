/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.music;

import hu.openig.sound.AudioThread;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

/**
 * Plays back ogg music. Based on the com.craft.jorbis JOrbisPlayer.
 * @author akarnokd
 */
public class OggMusic {
    /** The current playback thread. */
    private volatile Thread playbackThread;
    /** Ogg Player object. */
    private SyncState oy;
    /** Ogg Player object. */
    private StreamState os;
    /** Ogg Player object. */
    private Page og;
    /** Ogg Player object. */
    private Packet op;
    /** Ogg Player object. */
    private Info vi;
    /** Ogg Player object. */
    private Comment vc;
    /** Ogg Player object. */
    private DspState vd;
    /** Ogg Player object. */
    private Block vb;
    /** Ogg Player object. */
    private static final int BUFSIZE = 4096 * 2;
    /** Ogg Player object. */
    private int convsize = BUFSIZE * 2;
    /** Ogg Player object. */
    private byte[] convbuffer = new byte[convsize];

    /** Ogg Player object. */
    private int rate = 0;
    /** Ogg Player object. */
    private int channels = 0;
    /** The current playback source data line. */
    public volatile SourceDataLine outputLine = null;
    /** Current/initial sound gain. */
    private volatile int volume;
    /** Mute the output. */
    private volatile boolean mute;
    /**
     * Constructor.
     * @param me the playback thread running the run() method
     */
    public OggMusic(Thread me) {
        this.playbackThread = me;

    }
    /** Close playback thread. */
    public void close() {
        Thread th = playbackThread;
        if (th != null) {
            if (outputLine != null) {
                outputLine.close();
            }
            th.interrupt();
            playbackThread = null;
        }
    }
    /**
     * Initialize JOrbis.
     */
    void initJOrbis() {
        oy = new SyncState();
        os = new StreamState();
        og = new Page();
        op = new Packet();

        vi = new Info();
        vc = new Comment();
        vd = new DspState();
        vb = new Block(vd);

        oy.init();
    }
    /**
     * Initialize the audio subsystem.
     * @param channels the number of channels
     * @param rate the frequence
     */
    void initAudio(int channels, int rate) {
        try {
            // ClassLoader originalClassLoader=null;
            // try{
            // originalClassLoader=Thread.currentThread().getContextClassLoader();
            // Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
            // }
            // catch(Exception ee){
            // System.out.println(ee);
            // }
            AudioFormat audioFormat = new AudioFormat(rate, 16,
                    channels, true, // PCM_Signed
                    false // littleEndian
            );
            DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                    audioFormat, AudioSystem.NOT_SPECIFIED);
            if (!AudioSystem.isLineSupported(info)) {
                // System.out.println("Line " + info + " not supported.");
                return;
            }

            try {
                outputLine = (SourceDataLine) AudioSystem.getLine(info);
                // outputLine.addLineListener(this);
                outputLine.open(audioFormat);
            } catch (LineUnavailableException ex) {
                System.out.println("Unable to open the sourceDataLine: " + ex);
                return;
            } catch (IllegalArgumentException ex) {
                System.out.println("Illegal Argument: " + ex);
                return;
            }

            // if(originalClassLoader!=null)
            // Thread.currentThread().setContextClassLoader(originalClassLoader);

            this.rate = rate;
            this.channels = channels;
        } catch (Exception ee) {
            System.out.println(ee);
        }
    }
    /**
     * Create or return the output line.
     * @param channels the number of channels
     * @param rate the frequency
     * @return the current output line
     */
    SourceDataLine getOutputLine(int channels, int rate) {
        if (outputLine == null || this.rate != rate
                || this.channels != channels) {
            if (outputLine != null) {
                outputLine.close();
            }
            initAudio(channels, rate);
            outputLine.start();
        }
        return outputLine;
    }
    /**

     * Play the OGG file.

     * @param bitStream the opened file to play
     */
    public void playOgg(InputStream bitStream) {
        boolean chained = false;
        initJOrbis();
        int bytes;
        loop: while (true) {
            int eos = 0;

            int index = oy.buffer(BUFSIZE);
            byte[] buffer = oy.data;
            try {
                bytes = bitStream.read(buffer, index, BUFSIZE);
            } catch (Exception e) {
                System.err.println(e);
                return;
            }
            oy.wrote(bytes);

            if (chained) { //
                chained = false; //

            } else { //
                if (oy.pageout(og) != 1) {
                    if (bytes < BUFSIZE) {
                        break;
                    }
                    System.err.println("Input does not appear to be an Ogg bitstream.");
                    return;
                }
            } //
            os.init(og.serialno());
            os.reset();

            vi.init();
            vc.init();

            if (os.pagein(og) < 0) {
                // error; stream version mismatch perhaps
                System.err
                        .println("Error reading first page of Ogg bitstream data.");
                return;
            }

            if (os.packetout(op) != 1) {
                // no page? must not be vorbis
                System.err.println("Error reading initial header packet.");
                break;
                // return;
            }

            if (vi.synthesisHeaderin(vc, op) < 0) {
                // error case; not a vorbis header
                System.err
                        .println("This Ogg bitstream does not contain Vorbis audio data.");
                return;
            }

            int i = 0;

            while (i < 2) {
                while (i < 2) {
                    int result = oy.pageout(og);
                    if (result == 0) {
                        break; // Need more data
                    }
                    if (result == 1) {
                        os.pagein(og);
                        while (i < 2) {
                            result = os.packetout(op);
                            if (result == 0) {
                                break;
                            }
                            if (result == -1) {
                                System.err
                                        .println("Corrupt secondary header.  Exiting.");
                                // return;
                                break loop;
                            }
                            vi.synthesisHeaderin(vc, op);
                            i++;
                        }
                    }
                }

                index = oy.buffer(BUFSIZE);
                buffer = oy.data;
                try {
                    bytes = bitStream.read(buffer, index, BUFSIZE);
                } catch (Exception e) {
                    System.err.println(e);
                    return;
                }
                if (bytes == 0 && i < 2) {
                    System.err
                            .println("End of file before finding all Vorbis headers!");
                    return;
                }
                oy.wrote(bytes);
            }

            byte[][] ptr = vc.userComments;

            for (byte[] j : ptr) {
                if (j == null) {
                                    break;
                }
            }

            convsize = BUFSIZE / vi.channels;

            vd.synthesisInit(vi);
            vb.init(vd);

            float[][][] lPcmf = new float[1][][];
            int[] lIndex = new int[vi.channels];

            // Preset initial gain and mute
            SourceDataLine sdl = getOutputLine(vi.channels, vi.rate);

            int vol = mute ? 0 : volume;
            AudioThread.setVolume(sdl, vol);

            while (eos == 0) {
                while (eos == 0) {

                    if (playbackThread != Thread.currentThread()
                            || Thread.currentThread().isInterrupted()) {
                        try {
                            bitStream.close();
                            outputLine.close();
                            outputLine = null;
                        } catch (IOException ee) {
                        }
                        return;
                    }

                    int result = oy.pageout(og);
                    if (result == 0) {
                        break; // need more data
                    }
                    if (result != -1) {
                        os.pagein(og);

                        if (og.granulepos() == 0) { //
                            chained = true; //
                            eos = 1; //

                            break; //
                        } //

                        while (!Thread.currentThread().isInterrupted()) {
                            result = os.packetout(op);
                            if (result == 0) {
                                break; // need more data
                            }
                            if (result != -1) {
                                // we have a packet. Decode it
                                int samples;
                                if (vb.synthesis(op) == 0) { // test for
                                    // success!
                                    vd.synthesisBlockin(vb);
                                }
                                samples = vd.synthesisPcmout(lPcmf,
                                        lIndex);
                                while (samples > 0 && !Thread.currentThread().isInterrupted()) {
                                    float[][] pcmf = lPcmf[0];
                                    int bout = (samples < convsize ? samples
                                            : convsize);

                                    // convert doubles to 16 bit signed ints
                                    // (host order) and
                                    // interleave
                                    for (i = 0; i < vi.channels; i++) {
                                        int iptr = i * 2;
                                        // int ptr=i;
                                        int mono = lIndex[i];
                                        for (int j = 0; j < bout; j++) {
                                            int val = (int) (pcmf[i][mono + j] * 32767.);
                                            if (val > 32767) {
                                                val = 32767;
                                            }
                                            if (val < -32768) {
                                                val = -32768;
                                            }
                                            if (val < 0) {
                                                val = val | 0x8000;
                                            }
                                            convbuffer[iptr] = (byte) (val);
                                            convbuffer[iptr + 1] = (byte) (val >>> 8);
                                            iptr += 2 * (vi.channels);
                                        }
                                    }
                                    int len = 2 * vi.channels * bout;
                                    if (mute) {
                                        for (int m = 0; m < bout; m += 2) {
                                            convbuffer[m] = 0;
                                            convbuffer[m + 1] = 0;
                                        }
                                    }
                                    outputLine.write(convbuffer, 0, len);
                                    vd.synthesisRead(bout);
                                    samples = vd.synthesisPcmout(lPcmf,
                                            lIndex);
                                }
                            }
                        }
                        if (og.eos() != 0) {
                            eos = 1;
                        }
                    }
                }

                if (eos == 0) {
                    index = oy.buffer(BUFSIZE);
                    buffer = oy.data;
                    try {
                        bytes = bitStream.read(buffer, index, BUFSIZE);
                    } catch (Exception e) {
                        System.err.println(e);
                        return;
                    }
                    if (bytes == -1) {
                        break;
                    }
                    oy.wrote(bytes);
                    if (bytes == 0) {
                        eos = 1;
                    }
                }
            }

            os.clear();
            vb.clear();
            vd.clear();
            vi.clear();
        }

        oy.clear();

        try {
            bitStream.close();
        } catch (IOException e) {
        }
    }
    /**
     * Set the music playback volume.
     * @param volume the volume 0..100
     */
    public void setVolume(int volume) {
        this.volume = volume;
        if (outputLine != null) {
            int vol = mute ? 0 : volume;
            AudioThread.setVolume(outputLine, vol);
        }
    }
    /**
     * Toggle the mute state.
     * @param mute the mute state
     */
    public void setMute(boolean mute) {
        this.mute = mute;
        if (outputLine != null) {
            int vol = mute ? 0 : volume;
            AudioThread.setVolume(outputLine, vol);
        }
    }
}
