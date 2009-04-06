/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.music;

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
 * @author karnokd
 */
public class OggMusic {
	private volatile Thread playbackThread;
	private SyncState oy;
	private StreamState os;
	private Page og;
	private Packet op;
	private Info vi;
	private Comment vc;
	private DspState vd;
	private Block vb;
	private static final int BUFSIZE = 4096 * 2;
	private static int convsize = BUFSIZE * 2;
	private byte[] convbuffer = new byte[convsize];

	private int rate = 0;
	private int channels = 0;
	public volatile SourceDataLine outputLine = null;
	public OggMusic(Thread me) {
		this.playbackThread = me;
	}
	/** Close playback thread. */
	public void close() {
		Thread th = playbackThread;
		if (th != null) {
			th.interrupt();
			playbackThread = null;
		}
	}
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

	SourceDataLine getOutputLine(int channels, int rate) {
		if (outputLine == null || this.rate != rate
				|| this.channels != channels) {
			if (outputLine != null) {
				outputLine.drain();
				outputLine.stop();
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
		int bytes = 0;
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
			} //
			else { //
				if (oy.pageout(og) != 1) {
					if (bytes < BUFSIZE)
						break;
					System.err
							.println("Input does not appear to be an Ogg bitstream.");
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

			if (vi.synthesis_headerin(vc, op) < 0) {
				// error case; not a vorbis header
				System.err
						.println("This Ogg bitstream does not contain Vorbis audio data.");
				return;
			}

			int i = 0;

			while (i < 2) {
				while (i < 2) {
					int result = oy.pageout(og);
					if (result == 0)
						break; // Need more data
					if (result == 1) {
						os.pagein(og);
						while (i < 2) {
							result = os.packetout(op);
							if (result == 0)
								break;
							if (result == -1) {
								System.err
										.println("Corrupt secondary header.  Exiting.");
								// return;
								break loop;
							}
							vi.synthesis_headerin(vc, op);
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

			{
				byte[][] ptr = vc.user_comments;
				StringBuffer sb = null;

				for (int j = 0; j < ptr.length; j++) {
					if (ptr[j] == null)
						break;
					// System.err.println("Comment: "
					// + new String(ptr[j], 0, ptr[j].length - 1));
					if (sb != null)
						sb.append(" "
								+ new String(ptr[j], 0, ptr[j].length - 1));
				}
				// System.err.println("Bitstream is " + vi.channels +
				// " channel, "
				// + vi.rate + "Hz");
				// System.err
				// .println("Encoded by: "
				// + new String(vc.vendor, 0, vc.vendor.length - 1)
				// + "\n");
			}

			convsize = BUFSIZE / vi.channels;

			vd.synthesis_init(vi);
			vb.init(vd);

			float[][][] _pcmf = new float[1][][];
			int[] _index = new int[vi.channels];

			getOutputLine(vi.channels, vi.rate);

			while (eos == 0) {
				while (eos == 0) {

					if (playbackThread != Thread.currentThread()
							|| Thread.currentThread().isInterrupted()) {
						try {
							bitStream.close();
							outputLine.drain();
							outputLine.stop();
							outputLine.close();
							outputLine = null;
						} catch (Exception ee) {
						}
						return;
					}

					int result = oy.pageout(og);
					if (result == 0)
						break; // need more data
					if (result == -1) { // missing or corrupt data at this page
						// position
						// System.err.println("Corrupt or missing data in bitstream; continuing...");
					} else {
						os.pagein(og);

						if (og.granulepos() == 0) { //
							chained = true; //
							eos = 1; // 
							break; //
						} //

						while (true) {
							result = os.packetout(op);
							if (result == 0)
								break; // need more data
							if (result == -1) { // missing or corrupt data at
								// this page position
								// no reason to complain; already complained
								// above

								// System.err.println("no reason to complain; already complained above");
							} else {
								// we have a packet. Decode it
								int samples;
								if (vb.synthesis(op) == 0) { // test for
									// success!
									vd.synthesis_blockin(vb);
								}
								while ((samples = vd.synthesis_pcmout(_pcmf,
										_index)) > 0) {
									float[][] pcmf = _pcmf[0];
									int bout = (samples < convsize ? samples
											: convsize);

									// convert doubles to 16 bit signed ints
									// (host order) and
									// interleave
									for (i = 0; i < vi.channels; i++) {
										int ptr = i * 2;
										// int ptr=i;
										int mono = _index[i];
										for (int j = 0; j < bout; j++) {
											int val = (int) (pcmf[i][mono + j] * 32767.);
											if (val > 32767) {
												val = 32767;
											}
											if (val < -32768) {
												val = -32768;
											}
											if (val < 0)
												val = val | 0x8000;
											convbuffer[ptr] = (byte) (val);
											convbuffer[ptr + 1] = (byte) (val >>> 8);
											ptr += 2 * (vi.channels);
										}
									}
									outputLine.write(convbuffer, 0, 2
											* vi.channels * bout);
									vd.synthesis_read(bout);
								}
							}
						}
						if (og.eos() != 0)
							eos = 1;
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
					if (bytes == 0)
						eos = 1;
				}
			}

			os.clear();
			vb.clear();
			vd.clear();
			vi.clear();
		}

		oy.clear();

		try {
			if (bitStream != null)
				bitStream.close();
		} catch (Exception e) {
		}
	}
}
