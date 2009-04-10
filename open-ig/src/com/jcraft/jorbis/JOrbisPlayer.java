package com.jcraft.jorbis;

/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/* JOrbisPlayer -- pure Java Ogg Vorbis player
 *
 * Copyright (C) 2000 ymnk, JCraft,Inc.
 *
 * Written by: 2000 ymnk<ymnk@jcraft.com>
 *
 * Many thanks to 
 *   Monty <monty@xiph.org> and 
 *   The XIPHOPHORUS Company http://www.xiph.org/ .
 * JOrbis has been based on their awesome works, Vorbis codec and
 * JOrbisPlayer depends on JOrbis.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;

public class JOrbisPlayer extends JApplet implements ActionListener, Runnable {

	private static final long serialVersionUID = 1L;

	boolean running_as_applet = true;

	Thread player = null;
	InputStream bitStream = null;

	int udp_port = -1;
	String udp_baddress = null;

	static AppletContext acontext = null;

	static final int BUFSIZE = 4096 * 2;
	static int convsize = BUFSIZE * 2;
	static byte[] convbuffer = new byte[convsize];

	private int RETRY = 3;
	int retry = RETRY;

	String playlistfile = "playlist";

	boolean icestats = false;

	SyncState oy;
	StreamState os;
	Page og;
	Packet op;
	Info vi;
	Comment vc;
	DspState vd;
	Block vb;

	byte[] buffer = null;
	int bytes = 0;

	int format;
	int rate = 0;
	int channels = 0;
	int left_vol_scale = 100;
	int right_vol_scale = 100;
	SourceDataLine outputLine = null;
	String current_source = null;

	int frameSizeInBytes;
	int bufferLengthInBytes;

	boolean playonstartup = false;

	public void init() {
		running_as_applet = true;

		acontext = getAppletContext();

		String s = getParameter("jorbis.player.playlist");
		playlistfile = s;

		s = getParameter("jorbis.player.icestats");
		if (s != null && s.equals("yes")) {
			icestats = true;
		}

		loadPlaylist();
		initUI();

		if (playlist.size() > 0) {
			s = getParameter("jorbis.player.playonstartup");
			if (s != null && s.equals("yes")) {
				playonstartup = true;
			}
		}

		setBackground(Color.lightGray);
		// setBackground(Color.white);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panel);
	}

	public void start() {
		super.start();
		if (playonstartup) {
			play_sound();
		}
	}

	void init_jorbis() {
		oy = new SyncState();
		os = new StreamState();
		og = new Page();
		op = new Packet();

		vi = new Info();
		vc = new Comment();
		vd = new DspState();
		vb = new Block(vd);

		buffer = null;
		bytes = 0;

		oy.init();
	}

	SourceDataLine getOutputLine(int channels, int rate) {
		if (outputLine == null || this.rate != rate
				|| this.channels != channels) {
			if (outputLine != null) {
				outputLine.drain();
				outputLine.stop();
				outputLine.close();
			}
			init_audio(channels, rate);
			outputLine.start();
		}
		return outputLine;
	}

	void init_audio(int channels, int rate) {
		try {
			// ClassLoader originalClassLoader=null;
			// try{
			// originalClassLoader=Thread.currentThread().getContextClassLoader();
			// Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
			// }
			// catch(Exception ee){
			// System.out.println(ee);
			// }
			AudioFormat audioFormat = new AudioFormat(rate, 16, channels, true, // PCM_Signed
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

			frameSizeInBytes = audioFormat.getFrameSize();
			int bufferLengthInFrames = outputLine.getBufferSize()
					/ frameSizeInBytes / 2;
			bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;

			// if(originalClassLoader!=null)
			// Thread.currentThread().setContextClassLoader(originalClassLoader);

			this.rate = rate;
			this.channels = channels;
		} catch (Exception ee) {
			System.out.println(ee);
		}
	}

	private int item2index(String item) {
		for (int i = 0; i < cb.getItemCount(); i++) {
			String foo = (String) (cb.getItemAt(i));
			if (item.equals(foo))
				return i;
		}
		cb.addItem(item);
		return cb.getItemCount() - 1;
	}

	public void run() {
		Thread me = Thread.currentThread();
		String item = (String) (cb.getSelectedItem());
		int current_index = item2index(item);
		while (true) {
			item = (String) (cb.getItemAt(current_index));
			cb.setSelectedIndex(current_index);
			bitStream = selectSource(item);
			if (bitStream != null) {
				if (udp_port != -1) {
					play_udp_stream(me);
				} else {
					play_stream(me);
				}
			} else if (cb.getItemCount() == 1) {
				break;
			}
			if (player != me) {
				break;
			}
			bitStream = null;
			current_index++;
			if (current_index >= cb.getItemCount()) {
				current_index = 0;
			}
			if (cb.getItemCount() <= 0)
				break;
		}
		player = null;
		start_button.setText("start");
	}

	private void play_stream(Thread me) {

		boolean chained = false;

		init_jorbis();

		retry = RETRY;

		// System.out.println("play_stream>");

		loop: while (true) {
			int eos = 0;

			int index = oy.buffer(BUFSIZE);
			buffer = oy.data;
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

			retry = RETRY;

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
				if (acontext != null)
					sb = new StringBuffer();

				for (int j = 0; j < ptr.length; j++) {
					if (ptr[j] == null)
						break;
					System.err.println("Comment: "
							+ new String(ptr[j], 0, ptr[j].length - 1));
					if (sb != null)
						sb.append(" "
								+ new String(ptr[j], 0, ptr[j].length - 1));
				}
				System.err.println("Bitstream is " + vi.channels + " channel, "
						+ vi.rate + "Hz");
				System.err
						.println("Encoded by: "
								+ new String(vc.vendor, 0, vc.vendor.length - 1)
								+ "\n");
				if (sb != null)
					acontext.showStatus(sb.toString());
			}

			convsize = BUFSIZE / vi.channels;

			vd.synthesis_init(vi);
			vb.init(vd);

			float[][][] _pcmf = new float[1][][];
			int[] _index = new int[vi.channels];

			getOutputLine(vi.channels, vi.rate);

			while (eos == 0) {
				while (eos == 0) {

					if (player != me) {
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

	private void play_udp_stream(Thread me) {
		init_jorbis();

		try {
			loop: while (true) {
				int index = oy.buffer(BUFSIZE);
				buffer = oy.data;
				try {
					bytes = bitStream.read(buffer, index, BUFSIZE);
				} catch (Exception e) {
					System.err.println(e);
					return;
				}

				oy.wrote(bytes);
				if (oy.pageout(og) != 1) {
					// if(bytes<BUFSIZE)break;
					System.err
							.println("Input does not appear to be an Ogg bitstream.");
					return;
				}

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
					// break;
					return;
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

					if (i == 2)
						break;

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
				break;
			}
		} catch (Exception e) {
		}

		try {
			bitStream.close();
		} catch (Exception e) {
		}

		UDPIO io = null;
		try {
			io = new UDPIO(udp_port);
		} catch (Exception e) {
			return;
		}

		bitStream = io;
		play_stream(me);
	}

	public void stop() {
		if (player == null) {
			try {
				outputLine.drain();
				outputLine.stop();
				outputLine.close();
				outputLine = null;
				if (bitStream != null)
					bitStream.close();
			} catch (Exception e) {
			}
		}
		player = null;
	}

	Vector<String> playlist = new Vector<String>();

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == stats_button) {
			String item = (String) (cb.getSelectedItem());
			if (!item.startsWith("http://"))
				return;
			if (item.endsWith(".pls")) {
				item = fetch_pls(item);
				if (item == null)
					return;
			} else if (item.endsWith(".m3u")) {
				item = fetch_m3u(item);
				if (item == null)
					return;
			}
			byte[] foo = item.getBytes();
			for (int i = foo.length - 1; i >= 0; i--) {
				if (foo[i] == '/') {
					item = item.substring(0, i + 1) + "stats.xml";
					break;
				}
			}
			System.out.println(item);
			try {
				URL url = null;
				if (running_as_applet)
					url = new URL(getCodeBase(), item);
				else
					url = new URL(item);
				BufferedReader stats = new BufferedReader(
						new InputStreamReader(url.openConnection()
								.getInputStream()));
				while (true) {
					String bar = stats.readLine();
					if (bar == null)
						break;
					System.out.println(bar);
				}
			} catch (Exception ee) {
				// System.err.println(ee);
			}
			return;
		}

		String command = ((JButton) (e.getSource())).getText();
		if (command.equals("start") && player == null) {
			play_sound();
		} else if (player != null) {
			stop_sound();
		}
	}

	public String getTitle() {
		return (String) (cb.getSelectedItem());
	}

	public void play_sound() {
		if (player != null)
			return;
		player = new Thread(this);
		start_button.setText("stop");
		player.start();
	}

	public void stop_sound() {
		if (player == null)
			return;
		player = null;
		start_button.setText("start");
	}

	InputStream selectSource(String item) {
		if (item.endsWith(".pls")) {
			item = fetch_pls(item);
			if (item == null)
				return null;
			// System.out.println("fetch: "+item);
		} else if (item.endsWith(".m3u")) {
			item = fetch_m3u(item);
			if (item == null)
				return null;
			// System.out.println("fetch: "+item);
		}

		if (!item.endsWith(".ogg")) {
			return null;
		}

		InputStream is = null;
		URLConnection urlc = null;
		try {
			URL url = null;
			if (running_as_applet)
				url = new URL(getCodeBase(), item);
			else
				url = new URL(item);
			urlc = url.openConnection();
			is = urlc.getInputStream();
			current_source = url.getProtocol() + "://" + url.getHost() + ":"
					+ url.getPort() + url.getFile();
		} catch (Exception ee) {
			System.err.println(ee);
		}

		if (is == null && !running_as_applet) {
			try {
				is = new FileInputStream(System.getProperty("user.dir")
						+ System.getProperty("file.separator") + item);
				current_source = null;
			} catch (Exception ee) {
				System.err.println(ee);
			}
		}

		if (is == null)
			return null;

		System.out.println("Select: " + item);

		{
			boolean find = false;
			for (int i = 0; i < cb.getItemCount(); i++) {
				String foo = (String) (cb.getItemAt(i));
				if (item.equals(foo)) {
					find = true;
					break;
				}
			}
			if (!find) {
				cb.addItem(item);
			}
		}

		int i = 0;
		String s = null;
		String t = null;
		udp_port = -1;
		udp_baddress = null;
		while (urlc != null && true) {
			s = urlc.getHeaderField(i);
			t = urlc.getHeaderFieldKey(i);
			if (s == null)
				break;
			i++;
			if (t != null && t.equals("udp-port")) {
				try {
					udp_port = Integer.parseInt(s);
				} catch (Exception ee) {
					System.err.println(ee);
				}
			} else if (t != null && t.equals("udp-broadcast-address")) {
				udp_baddress = s;
			}
		}
		return is;
	}

	String fetch_pls(String pls) {
		InputStream pstream = null;
		if (pls.startsWith("http://")) {
			try {
				URL url = null;
				if (running_as_applet)
					url = new URL(getCodeBase(), pls);
				else
					url = new URL(pls);
				URLConnection urlc = url.openConnection();
				pstream = urlc.getInputStream();
			} catch (Exception ee) {
				System.err.println(ee);
				return null;
			}
		}
		if (pstream == null && !running_as_applet) {
			try {
				pstream = new FileInputStream(System.getProperty("user.dir")
						+ System.getProperty("file.separator") + pls);
			} catch (Exception ee) {
				System.err.println(ee);
				return null;
			}
		}

		String line = null;
		while (true) {
			try {
				line = readline(pstream);
			} catch (Exception e) {
			}
			if (line == null)
				break;
			if (line.startsWith("File1=")) {
				byte[] foo = line.getBytes();
				int i = 6;
				for (; i < foo.length; i++) {
					if (foo[i] == 0x0d)
						break;
				}
				return line.substring(6, i);
			}
		}
		return null;
	}

	String fetch_m3u(String m3u) {
		InputStream pstream = null;
		if (m3u.startsWith("http://")) {
			try {
				URL url = null;
				if (running_as_applet)
					url = new URL(getCodeBase(), m3u);
				else
					url = new URL(m3u);
				URLConnection urlc = url.openConnection();
				pstream = urlc.getInputStream();
			} catch (Exception ee) {
				System.err.println(ee);
				return null;
			}
		}
		if (pstream == null && !running_as_applet) {
			try {
				pstream = new FileInputStream(System.getProperty("user.dir")
						+ System.getProperty("file.separator") + m3u);
			} catch (Exception ee) {
				System.err.println(ee);
				return null;
			}
		}

		String line = null;
		while (true) {
			try {
				line = readline(pstream);
			} catch (Exception e) {
			}
			if (line == null)
				break;
			return line;
		}
		return null;
	}

	void loadPlaylist() {

		if (running_as_applet) {
			String s = null;
			for (int i = 0; i < 10; i++) {
				s = getParameter("jorbis.player.play." + i);
				if (s == null)
					break;
				playlist.addElement(s);
			}
		}

		if (playlistfile == null) {
			return;
		}

		try {
			InputStream is = null;
			try {
				URL url = null;
				if (running_as_applet)
					url = new URL(getCodeBase(), playlistfile);
				else
					url = new URL(playlistfile);
				URLConnection urlc = url.openConnection();
				is = urlc.getInputStream();
			} catch (Exception ee) {
			}
			if (is == null && !running_as_applet) {
				try {
					is = new FileInputStream(System.getProperty("user.dir")
							+ System.getProperty("file.separator")
							+ playlistfile);
				} catch (Exception ee) {
				}
			}

			if (is == null)
				return;

			while (true) {
				String line = readline(is);
				if (line == null)
					break;
				byte[] foo = line.getBytes();
				for (int i = 0; i < foo.length; i++) {
					if (foo[i] == 0x0d) {
						line = new String(foo, 0, i);
						break;
					}
				}
				playlist.addElement(line);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private String readline(InputStream is) {
		StringBuffer rtn = new StringBuffer();
		int temp;
		do {
			try {
				temp = is.read();
			} catch (Exception e) {
				return (null);
			}
			if (temp == -1) {
				String str = rtn.toString();
				if (str.length() == 0)
					return (null);
				return str;
			}
			if (temp != 0 && temp != '\n' && temp != '\r')
				rtn.append((char) temp);
		} while (temp != '\n' && temp != '\r');
		return (rtn.toString());
	}

	public JOrbisPlayer() {
	}

	JPanel panel;
	JComboBox cb;
	JButton start_button;
	JButton stats_button;

	void initUI() {
		panel = new JPanel();

		cb = new JComboBox(playlist);
		cb.setEditable(true);
		panel.add(cb);

		start_button = new JButton("start");
		start_button.addActionListener(this);
		panel.add(start_button);

		if (icestats) {
			stats_button = new JButton("IceStats");
			stats_button.addActionListener(this);
			panel.add(stats_button);
		}
	}

	class UDPIO extends InputStream {
		InetAddress address;
		DatagramSocket socket = null;
		DatagramPacket sndpacket;
		DatagramPacket recpacket;
		byte[] buf = new byte[1024];
		// String host;
		int port;
		byte[] inbuffer = new byte[2048];
		byte[] outbuffer = new byte[1024];
		int instart = 0, inend = 0, outindex = 0;

		UDPIO(int port) {
			this.port = port;
			try {
				socket = new DatagramSocket(port);
			} catch (Exception e) {
				System.err.println(e);
			}
			recpacket = new DatagramPacket(buf, 1024);
		}

		void setTimeout(int i) {
			try {
				socket.setSoTimeout(i);
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		int getByte() throws java.io.IOException {
			if ((inend - instart) < 1) {
				read(1);
			}
			return inbuffer[instart++] & 0xff;
		}

		int getByte(byte[] array) throws java.io.IOException {
			return getByte(array, 0, array.length);
		}

		int getByte(byte[] array, int begin, int length)
				throws java.io.IOException {
			int i = 0;
			int foo = begin;
			while (true) {
				if ((i = (inend - instart)) < length) {
					if (i != 0) {
						System.arraycopy(inbuffer, instart, array, begin, i);
						begin += i;
						length -= i;
						instart += i;
					}
					read(length);
					continue;
				}
				System.arraycopy(inbuffer, instart, array, begin, length);
				instart += length;
				break;
			}
			return begin + length - foo;
		}

		int getShort() throws java.io.IOException {
			if ((inend - instart) < 2) {
				read(2);
			}
			int s = 0;
			s = inbuffer[instart++] & 0xff;
			s = ((s << 8) & 0xffff) | (inbuffer[instart++] & 0xff);
			return s;
		}

		int getInt() throws java.io.IOException {
			if ((inend - instart) < 4) {
				read(4);
			}
			int i = 0;
			i = inbuffer[instart++] & 0xff;
			i = ((i << 8) & 0xffff) | (inbuffer[instart++] & 0xff);
			i = ((i << 8) & 0xffffff) | (inbuffer[instart++] & 0xff);
			i = (i << 8) | (inbuffer[instart++] & 0xff);
			return i;
		}

		void getPad(int n) throws java.io.IOException {
			int i;
			while (n > 0) {
				if ((i = inend - instart) < n) {
					n -= i;
					instart += i;
					read(n);
					continue;
				}
				instart += n;
				break;
			}
		}

		void read(int n) throws java.io.IOException {
			if (n > inbuffer.length) {
				n = inbuffer.length;
			}
			instart = inend = 0;
			int i;
			while (true) {
				recpacket.setData(buf, 0, 1024);
				socket.receive(recpacket);

				i = recpacket.getLength();
				System.arraycopy(recpacket.getData(), 0, inbuffer, inend, i);
				if (i == -1) {
					throw new java.io.IOException();
				}
				inend += i;
				break;
			}
		}

		public void close() throws java.io.IOException {
			socket.close();
		}

		public int read() throws java.io.IOException {
			return 0;
		}

		public int read(byte[] array, int begin, int length)
				throws java.io.IOException {
			return getByte(array, begin, length);
		}
	}

	public static void main(String[] arg) {

		JFrame frame = new JFrame("JOrbisPlayer");
		frame.setBackground(Color.lightGray);
		frame.setBackground(Color.white);
		frame.getContentPane().setLayout(new BorderLayout());

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		JOrbisPlayer player = new JOrbisPlayer();
		player.running_as_applet = false;

		if (arg.length > 0) {
			for (int i = 0; i < arg.length; i++) {
				player.playlist.addElement(arg[i]);
			}
		}

		player.loadPlaylist();
		player.initUI();

		frame.getContentPane().add(player.panel);
		frame.pack();
		frame.setVisible(true);
	}
}
