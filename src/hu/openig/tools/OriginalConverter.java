/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.core.Func1E;
import hu.openig.core.Pair;
import hu.openig.model.Chats.Node;
import hu.openig.tools.ChatConverter.Choice;
import hu.openig.tools.ChatConverter.IGScript;
import hu.openig.tools.ChatConverter.Message;
import hu.openig.tools.ChatConverter.Procedure;
import hu.openig.tools.ani.Framerates;
import hu.openig.tools.ani.Framerates.Rates;
import hu.openig.tools.ani.SpidyAniDecoder;
import hu.openig.tools.ani.SpidyAniDecoder.SpidyAniCallback;
import hu.openig.tools.ani.SpidyAniFile;
import hu.openig.tools.ani.SpidyAniFile.Block;
import hu.openig.tools.ani.SpidyAniFile.Sound;
import hu.openig.utils.IOUtils;
import hu.openig.utils.PACFile;
import hu.openig.utils.PACFile.PACEntry;
import hu.openig.utils.PCXImage;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamException;

/**
 * Takes an original game install and extracts all language-specific resource, then names
 * them correctly.
 * @author akarnokd, 2012.10.20.
 */
public final class OriginalConverter {
	/** Utility class. */
	private OriginalConverter() { }
	/** The source directory. */
	static String source;
	/** The destination directory. */
	static String destination;
	/** The conversion instructions. */
	static XElement instructions;
	/** The target language. */
	static String language;
	/** The plaintext file encoding. */
	static String encoding; // russian CP-855, french CP-863
	/**
	 * Convert an SMP file into a WAV file.
	 * @param src the source file
	 * @param dst the destination WAV
	 * @throws IOException on error
	 */
	static void convertSMP(File src, File dst) throws IOException {
		dst.getParentFile().mkdirs();
		
		byte[] sample = IOUtils.load(src);
		if (sample.length == 0) {
			return;
		}
		
		writeWav(dst, sample);
	}
	/**
	 * Writes the byte data as a wav file.
	 * @param dst the destination file
	 * @param sample the sample
	 * @throws IOException on error
	 */
	static void writeWav(File dst, byte[] sample)
			throws IOException {
		try (DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dst), 1024 * 1024))) {
			writeWav(sample, dout);
		}
	}
	/**
	 * Write the wav into an output stream.
	 * @param sample the sample
	 * @param dout the stream
	 * @throws IOException on error
	 */
	public static void writeWav(byte[] sample, DataOutputStream dout)
			throws IOException {
		int dataLen = sample.length + (sample.length % 2 == 0 ? 0 : 1);
		// HEADER
		dout.write("RIFF".getBytes("ISO-8859-1"));
		dout.writeInt(Integer.reverseBytes(36 + dataLen)); // chunk size
		dout.write("WAVE".getBytes("ISO-8859-1"));
		
		// FORMAT
		dout.write("fmt ".getBytes("ISO-8859-1"));
		dout.writeInt(Integer.reverseBytes(16)); // chunk size
		dout.writeShort(Short.reverseBytes((short)1)); // Format: PCM = 1
		dout.writeShort(Short.reverseBytes((short)1)); // Channels = 1
		dout.writeInt(Integer.reverseBytes(22050)); // Sample Rate = 22050
		dout.writeInt(Integer.reverseBytes(22050)); // Byte Rate = 22050
		dout.writeShort(Short.reverseBytes((short)1)); // Block alignment = 1
		dout.writeShort(Short.reverseBytes((short)8)); // Bytes per sample = 8

		// DATA
		dout.write("data".getBytes("ISO-8859-1"));
		dout.writeInt(Integer.reverseBytes(dataLen));
        for (byte aSample : sample) {
            dout.write(aSample + 128);
        }
		for (int i = sample.length; i < dataLen; i++) {
			dout.write(0x80);
		}
		dout.flush();
	}
	/**
	 * Create a renamed copy of the source file.
	 * @param src the source
	 * @param dst the destination
	 */
	static void copyFile(File src, File dst) {
		dst.getParentFile().mkdirs();
		byte[] srcData = IOUtils.load(src);
		if (srcData.length > 0) {
			IOUtils.save(dst, srcData);
		}
	}
	/**
	 * Extract the sound from an original ANI file.
	 * @param src the source file
	 * @param dst the destination file
	 */
	static void convertANISound(File src, File dst) {
		byte[] sample = extractWav(src);
		dst.getParentFile().mkdirs();
		try {
			writeWav(dst, sample);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Extract the wav from the given ANI file.
	 * @param src the source file
	 * @return the byte data
	 */
	public static byte[] extractWav(File src) {
		List<byte[]> data = new ArrayList<>();
		int len = 0;
		
		final SpidyAniFile saf = new SpidyAniFile();
		try {
            try (InputStream in = new BufferedInputStream(new FileInputStream(src), 1024 * 1024)) {
                saf.open(in);
                saf.load();

                Framerates fr = new Framerates();
                Rates r = fr.getRates(src.getName().toUpperCase(Locale.ENGLISH), saf.getLanguageCode());
                int delay = (int) (r.delay / r.fps * 22050);

                byte[] silence = new byte[delay];
                for (int i = 0; i < silence.length; i++) {
                    silence[i] = 0;
                }
                data.add(silence);
                len += delay;

                while (true) {
                    Block b = saf.next();
                    if (b instanceof Sound) {
                        data.add(b.data.clone());
                        len += b.data.length;
                    }
                }

            }
		} catch (IOException ex) {
			// ignored
		}
		if (len % 2 != 0) {
			byte[] pad = new byte[] { 0 };
			data.add(pad);
			len++;
		}
		byte[] sample = new byte[len];
		len = 0;
		for (byte[] d : data) {
			System.arraycopy(d, 0, sample, len, d.length);
			len += d.length;
		}
		return sample;
	}
	/**
	 * Extract a subimage from the given image and save it as PNG.
	 * @param img the image
	 * @param fragments the source rectangle and the destination point
	 * @param dst the destination filename
	 */
	static void extractImage(BufferedImage img, 
			Iterable<Pair<Rectangle, Point>> fragments, File dst) {
		dst.getParentFile().mkdirs();
		int w = 0;
		int h = 0;
		for (Pair<Rectangle, Point> f : fragments) {
			w = Math.max(w, f.first.width + f.second.x);
			h = Math.max(h, f.first.height + f.second.y);
		}
		
		BufferedImage img2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img2.createGraphics();
		for (Pair<Rectangle, Point> f : fragments) {
			g2.drawImage(img.getSubimage(f.first.x, f.first.y, f.first.width, f.first.height), 
					f.second.x, f.second.y, null);
		}
		g2.dispose();
		
		try {
			ImageIO.write(img2, "png", dst);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Convert the given original ANI file to the new format.
	 * @param src the source
	 * @param dst the destination
	 */
	static void convertAni(final File src, final File dst) {
		final CountDownLatch latch = new CountDownLatch(1); 
		SpidyAniCallback callback = new SpidyAniCallback() {
			/** The image width. */
			private int width;
			/** The image height. */
			private int height;
			/** The output. */
			Ani2009Writer out;
			/** The image queue. */
			BlockingDeque<BufferedImage> images = new LinkedBlockingDeque<>(1);
			@Override
			public void audioData(byte[] data) {
				// ignored
			}

			@Override
			public void fatal(Throwable t) {
				t.printStackTrace();
				stopped();
			}

			@Override
			public void finished() {
			}

			@Override
			public String getFileName() {
				return src.getAbsolutePath();
			}

			@Override
			public InputStream getNewInputStream() {
				try {
					return new FileInputStream(src);
				} catch (FileNotFoundException ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public void imageData(int[] image) {

				BufferedImage img2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				img2.setRGB(0, 0, width, height, image, 0, width);

				try {
					images.put(img2);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}

			@Override
			public void initialize(int width, int height, int frames,
					int languageCode, double fps, int audioDelay) {
				this.width = width;
				this.height = height;
				try {
					out = new Ani2009Writer(dst, fps, frames, new Func1E<Integer, BufferedImage, IOException>() {
						@Override
						public BufferedImage invoke(Integer value)
								throws IOException {
							try {
								return images.take();
							} catch (InterruptedException ex) {
								throw new InterruptedIOException();
							}
						}
					});
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								out.run();
								out.close();
							} catch (IOException ex) {
								ex.printStackTrace();
							} finally {
								latch.countDown();
							}
						}
					});
					t.start();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			@Override
			public boolean isPaused() {
				return false;
			}

			@Override
			public boolean isStopped() {
				return false;
			}

			@Override
			public void stopped() {
				finished();
			}
			
		};
		SpidyAniDecoder.decodeLoop(callback);
		try {
			latch.await();
		} catch (InterruptedException ex) {
			
		}
	}
	/**
	 * Convert the labels.
	 * @throws XMLStreamException on error
	 * @throws IOException on error
	 */
	public static void convertLabels() throws XMLStreamException, IOException {
		// -------------------------------------------------------
		// translations
		Map<String, PACEntry> pacs = PACFile.mapByName(PACFile.parseFully(new File(source + "DATA/TEXT.PAC")));
		Map<String, String> labels = new LinkedHashMap<>();
		File dest = createDestination("data", "labels.xml");
		XElement xlabels = XElement.parseXML(dest);
		for (XElement xentry : xlabels.childrenWithName("entry")) {
			String key = xentry.get("key");
			if (!key.isEmpty()) {
				labels.put(key, xentry.content);
			}
		}
		Map<String, Pair<String, String>> codepages = new HashMap<>();
		for (XElement xcodepage : instructions.childrenWithName("codepage")) {
			codepages.put(xcodepage.get("id"), Pair.of(xcodepage.get("latin"), xcodepage.get("utf")));
		}

		Pair<String, String> codes = codepages.get(encoding);
		
		List<String> buildings = new ArrayList<>();
		for (XElement xbuildingMap : instructions.childrenWithName("building-map")) {
			if (xbuildingMap.getBoolean("enabled", true)) {
				for (XElement xentry : xbuildingMap.childrenWithName("entry")) {
					buildings.add(xentry.get("key"));
				}
			}
		}

		if (!buildings.isEmpty()) {
			System.out.println("LABELS: Building names.");
			List<String> epulNev = getText(pacs, "EPUL_NEV.TXT", codes);
			for (int i = 0; i < epulNev.size(); i++) {
				labels.put(buildings.get(i), epulNev.get(i));
			}
			
			System.out.println("LABELS: Building details.");
			List<String> epulInfo = getText(pacs, "EPUL_INF.TXT", codes);
			for (int i = 0; i < epulNev.size(); i++) {
				labels.put(buildings.get(i) + ".desc", 
						U.join(epulInfo.subList(i * 3, Math.min(i * 3 + 3, epulInfo.size())), " ").replaceAll("\\s{2,}", " ")
				);
			}
		}

		List<String> techs = new ArrayList<>();
		for (XElement techMap : instructions.childrenWithName("tech-map")) {
			if (techMap.getBoolean("enabled", true)) {
				for (XElement xentry : techMap.childrenWithName("entry")) {
					techs.add(xentry.get("key"));
				}
			}
		}

		if (!techs.isEmpty()) {
			System.out.println("LABELS: Inventions.");
			List<String> talNev = getText(pacs, "TAL_NEV.TXT", codes);
			for (int i = 0; i < techs.size(); i++) {
				String key = techs.get(i);
				if (!key.isEmpty()) {
					labels.put(key + ".name", talNev.get(i));
				}
			}
			System.out.println("LABELS: Invention description.");
			List<String> eqTxt = getText(pacs, "EQTXT.TXT", codes);
			int eqIdx = 0;
            for (String key : techs) {
                if (!key.isEmpty()) {
                    labels.put(key + ".longname", eqTxt.get(eqIdx * 3));

                    List<String> desc = eqTxt.subList(eqIdx * 3 + 1, eqIdx * 3 + 3);

                    labels.put(key + ".description", U.join(desc, " ").replaceAll("\\s{2,}", " "));

                    eqIdx++;
                }
            }
		}
		
		if (instructions.childElement("diplomacy") != null) {
			System.out.println("LABELS: diplomacy.");
			// diplomacy text
			Map<String, String> dipLabels = new LinkedHashMap<>();
			convertDiplomacy(pacs, dipLabels);
			
			for (Map.Entry<String, String> e : dipLabels.entrySet()) {
				labels.put(e.getKey(), transcode(e.getValue(), codes));
			}
		}		
		// convert chat programs
		
		for (XElement xchat : instructions.childrenWithName("chat")) {
			Map<String, String> chatLabels = new LinkedHashMap<>();
			
			String src = xchat.get("src");
			String labelPattern = xchat.get("labels");

			System.out.printf("LABELS: Chat: %s -> %s.%n", src, labelPattern);

			convertChat(pacs, src, labelPattern, chatLabels);

			for (Map.Entry<String, String> e : chatLabels.entrySet()) {
				labels.put(e.getKey(), transcode(e.getValue(), codes));
			}

		}
		
		convertOtherText(pacs, codes);
		
		// --------------------- store labels
		xlabels.clear();
		for (Map.Entry<String, String> e : labels.entrySet()) {
			XElement xentry = xlabels.add("entry");
			xentry.set("key", e.getKey());
			xentry.content = e.getValue();
		}
		xlabels.save(dest);
	}
	/**
	 * Simply extract other text into a single big file.
	 * @param pacs the package content
	 * @param codes the translator codes
	 * @throws IOException on error
	 */
	static void convertOtherText(Map<String, PACEntry> pacs, Pair<String, String> codes) throws IOException {
		List<String> out = new ArrayList<>();
		List<String> entries = U.newArrayList(pacs.keySet());
		Collections.sort(entries);
		for (String key : entries) {
			String keyLower = key.toLowerCase();
			if (keyLower.endsWith(".p")) {
				List<String> lines = getLines(pacs.get(key).data, Charset.forName("CP437"));
				for (int i = lines.size() - 1; i >= 0; i--) {
					String line = lines.get(i);
					if (line.startsWith("@")) {
						lines.remove(i);
					} else {
						lines.set(i, transcode(line, codes));
					}
				}
				if (!lines.isEmpty()) {
					out.add(key);
					out.addAll(lines);
					out.add("----------------------------------------------------------------------------");
				}
			}
			if (keyLower.startsWith("k") && keyLower.endsWith("_duma.txt")) {
				List<String> lines = getLines(pacs.get(key).data, Charset.forName("CP437"));
				for (int i = lines.size() - 1; i >= 0; i--) {
					String line = lines.get(i);
					if (line.startsWith("-")) {
						lines.set(i, transcode(line.substring(8), codes));
					} else {
						lines.remove(i);
					}
				}				
				out.add(key);
				out.addAll(lines);
				out.add("----------------------------------------------------------------------------");
			}
			if (keyLower.startsWith("k") && keyLower.endsWith("_text.txt")) {
				List<String> lines = getLines(pacs.get(key).data, Charset.forName("CP437"));
				for (int i = lines.size() - 1; i >= 0; i--) {
					String line = lines.get(i);
					if (line.length() >= 5 && line.charAt(2) != ' ') {
						lines.set(i, transcode(line.substring(4), codes));
					} else {
						lines.remove(i);
					}
				}				
				out.add(key);
				out.addAll(lines);
				out.add("----------------------------------------------------------------------------");
			}
		}
		Map<String, String> messageCodes = new LinkedHashMap<>();
		for (XElement mc : instructions.childrenWithName("message-codes")) {
			if (mc.getBoolean("enabled")) {
				for (XElement xe : mc.childrenWithName("entry")) {
					messageCodes.put(xe.get("src").toLowerCase(), xe.get("dst"));
				}
			}
		}
		
		if (!messageCodes.isEmpty()) {
			// message transcripts
			List<String> lines = getLines(pacs.get("MESSTXT.TXT").data, Charset.forName("CP437"));
			for (int i = 0; i < lines.size();) {
				String line = lines.get(i);
				if (line.startsWith("# Digi")) {
					int idx0 = line.indexOf(' ', 7);
					if (idx0 < 0) {
						idx0 = line.length();
					}
					String number = line.substring(7, idx0).trim().toLowerCase();
					
					String e = messageCodes.get("digi" + number + ".ani");
					if (e == null) {
						e = messageCodes.get("digi0" + number + ".ani");
					}
					if (e == null) {
						e = messageCodes.get("digi00" + number + ".ani");
					}
					if (e == null) {
						e = messageCodes.get(number + ".ani");
					}
					if (e == null) {
						System.err.println("Missing " + line + " (" + number + ")");
					}
					out.add(e);
					i++;
					int j = i;
					while (!lines.get(i).startsWith("# Digi") && !lines.get(i).startsWith("@end") && i < lines.size()) {
						i++;
					}
					for (int k = j; k < i; k++) {
						line = lines.get(k);
						if (line.length() >= 11 && (line.startsWith("A ") || line.startsWith("B "))
								&& Character.isDigit(line.charAt(2))
								&& Character.isDigit(line.charAt(7))) {
							out.add(transcode(line.substring(10), codes));
						}
					}
					
					out.add("----------------------------------------------------------------------------");
				} else {
					i++;
				}
			}
		}
		
		Files.write(Paths.get("./othertext.txt"), out, Charset.forName("UTF-8"));
	}
	/**
	 * Convert an array of bytes into a multiline string.
	 * @param data the data bytes
	 * @param cs the charset
	 * @return the lines
	 * @throws IOException on error
	 */
	static List<String> getLines(byte[] data, Charset cs) throws IOException {
		List<String> result = new ArrayList<>();
		
		String line;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), cs))) {
			while ((line = in.readLine()) != null) {
				result.add(line);
			}
		}
		return result;
	}
	/**
	 * Convert the diplomatic text.
	 * @param mapByName the original file package
	 * @param name the source file name
	 * @param labelPattern the label pattern
	 * @param labels the translation map
	 * @throws IOException on error
	 */
	static void convertChat(Map<String, PACEntry> mapByName,
			String name,
			String labelPattern,
			Map<String, String> labels) throws IOException {

		IGScript scr = parseScript(mapByName.get(name).data, "Cp437"); //hu: Cp850, de: Cp1250, other: Cp437
		
		List<Node> nodes = new ArrayList<>();
		
		for (Procedure proc : scr.procedures.values()) {
			if (!proc.messages.isEmpty()) {
				Message m0 = proc.messages.get(0);

				String n0n = proc.name + "-p";
				if (proc.name.equals(scr.start)) {
					n0n = "0";
				}
				
				String opt = null;
				
				for (Procedure p2 : scr.procedures.values()) {
					for (Choice c : p2.choices) {
						if (c.proc.equals(proc.name)) {
							opt = c.text;
							if (p2.name.equals(scr.start)) {
								n0n = "0";
							}
							if (m0.text.isEmpty()) {
								m0.text = c.text;
							}
						}
					}
				}
				Node n0 = new Node(n0n);
				n0.message = m0.text;
				n0.option = opt;
				
				n0.transitions.add(proc.name + "-e");
				
				nodes.add(n0);
				
				//**********************************************************
				
				Message m1 = proc.messages.get(1);
				Node n1 = new Node(proc.name + "-e");
				n1.retreat = proc.retreat;

				if (m1.text.isEmpty() && proc.next != null) {
					Procedure p2 = scr.procedures.get(proc.next);
					m1 = p2.messages.get(1);
					n1.retreat |= p2.retreat;
				}
				
				n1.enemy = true;
				n1.message = m1.text;
				
				if (proc.next != null) {
					Procedure p2 = scr.procedures.get(proc.next);
					for (Choice c : p2.choices) {
						n1.transitions.add(c.proc + "-p");
					}
				}
				
				nodes.add(n1);
			}
		}
		int idx = 0;
		Map<String, Integer> nodeMap = new HashMap<>();
		List<Node> nodes2 = U.newArrayList(nodes);
		nodes.clear();
		for (Node n : nodes2) {
			if (!n.message.isEmpty()) {
				nodeMap.put(n.id, idx);
				idx++;
				nodes.add(n);
			}
		}
		
		for (Node n : nodes) {
			boolean found = false;
			outer:
			for (Node n2 : nodes) {
				for (String tr0 : n2.transitions) {
					if (tr0.equals(n.id)) {
						found = true;
						break outer;
					}
				}
			}
			if (!found && !n.id.equals("0")) {
				continue;
			}
			
			int nidx = nodeMap.get(n.id);
			
			if (n.option != null) {
				
				String lbl = String.format(labelPattern, "n" + nidx + ".o");
				
				labels.put(lbl, n.option);
				
			}

			String lbl = String.format(labelPattern, "n" + nidx);
			labels.put(lbl, n.message);
			
			if (!n.transitions.isEmpty()) {
				for (String tr : n.transitions) {
					Integer trs = nodeMap.get(tr);
					if (trs == null) {
						System.err.printf("Transition %s missing %n", tr);
						System.err.flush();
					}
				}
			}
		}
	}
	/**
	 * Parse an IG script file.
	 * @param data the data bytes
	 * @param charset the charset
	 * @return the parsed script
	 * @throws IOException on error
	 */
	static IGScript parseScript(byte[] data, String charset) throws IOException {
		IGScript result = new IGScript();
		String txt = new String(data, charset);
		int pi = 0;
		// parse procedures
		while (pi >= 0) {
			int pi2 = txt.indexOf("procedure ", pi);
			if (pi2 >= 0) {
				int pi3 = txt.indexOf('\r', pi2);
				Procedure proc = new Procedure();
				
				proc.name = txt.substring(pi2 + 10, pi3).trim();
				
				int pi2e1 = txt.indexOf("procedure ", pi3);
				int pi2e = txt.indexOf("end\r", pi3);
				
				int pinc = 4;
				// check if the end is missing
				if (pi2e1 >= 0 && pi2e1 < pi2e) {
					pi2e = pi2e1;
					pinc = 0;
				}
				
				proc.body = txt.substring(pi3 + 1, pi2e).trim();
				
				result.procedures.put(proc.name, proc);

				pi = pi2e + pinc;
			} else {
				break;
			}
		}
		// parse each procedures.
		for (Procedure proc : result.procedures.values()) {
			int choiceIdx = proc.body.indexOf("valaszt ");
			// manage choices
			if (choiceIdx >= 0) {
				int ce = proc.body.indexOf("\r", choiceIdx);
				String cn = proc.body.substring(choiceIdx + 8, ce).trim();
				int choiceCount = Integer.parseInt(cn);
				
				for (int i = 0; i < choiceCount; i++) {
					int ce2 = proc.body.indexOf("\r", ce + 1);
					if (ce2 < 0) {
						ce2 = proc.body.length();
					}
					
					String choiceEntryStr = proc.body.substring(ce + 1, ce2).trim();
					
					int sep = choiceEntryStr.lastIndexOf(' ');
					int qidx0 = choiceEntryStr.indexOf('"');
					int qidx = choiceEntryStr.lastIndexOf('"');

					Choice c = new Choice();

					c.text = noquot(choiceEntryStr.substring(qidx0, qidx + 1));
					
					if (qidx < sep) {
						c.proc = choiceEntryStr.substring(sep + 1).trim();
					} else {
						// the next line contains the target
						int ce3 = proc.body.indexOf("\r", ce2 + 1);
						if (ce3 < 0) {
							ce3 = proc.body.length();
						}
						c.proc = proc.body.substring(ce2 + 1, ce3).trim();
						ce2 = ce3 - 1;
					}
					
					proc.choices.add(c);
					
					ce = ce2 + 1;
				}
			} else {
				Message m0 = new Message();
				Message m1 = new Message();
				m1.party = 1;
				
				for (String s0 : proc.body.split("\r")) {
					s0 = s0.trim();
					
					if (s0.startsWith("message0")) {
						m0.text += " " + noquot(s0.substring(9));
					} else
					if (s0.startsWith("message1")) {
						m1.text += " " + noquot(s0.substring(9));
					} else 
					if (s0.startsWith("visszavonulas")) {
						proc.retreat = true;
					} else {
						if (result.procedures.containsKey(s0)) {
							proc.next = s0;
						}
					}
				}
				
				m0.text = m0.text.trim();
				m1.text = m1.text.trim();
				proc.messages.add(m0);
				proc.messages.add(m1);
			}
		}
		
		// find the entry point
		int pi0 = txt.indexOf("procedure ");
		
		for (String s0 : txt.substring(0, pi0).split("\r")) {
			int s0i = s0.indexOf(' ');
			if (s0i < 0) {
				s0i = s0.length();
			}
			
			String n0 = s0.substring(0, s0i).trim();
			if (result.procedures.containsKey(n0)) {
				result.start = n0;
			}
		}
		
		return result;
	}
	/**
	 * Removes the leading and trailing quotation marks.
	 * @param s the string
	 * @return the cleaned string
	 */
	static String noquot(String s) {
		if (s.startsWith("\"")) {
			s = s.substring(1);
		}
		if (s.endsWith("\"")) {
			s = s.substring(0, s.length() - 1);
		}
		return s.trim();
	}
	/**
	 * Convert the diplomatic text.
	 * @param mapByName the original file package
	 * @param labels the translation map
	 * @throws IOException on error
	 */
	static void convertDiplomacy(Map<String, PACEntry> mapByName, 
			Map<String, String> labels) throws IOException {
		for (Map.Entry<String, PACEntry> e : mapByName.entrySet()) {
            String s = e.getKey();
			String st = new String(e.getValue().data, "CP437");
			if (st.contains("This is a template text fot Jason")
					|| st.contains("This is a template text for Jason")) {
				parse(s, st, labels);
			}
		}

	}
	/**
	 * Parse the plain diplomacy content.
	 * @param name the race name
	 * @param content the content
	 * @param map the label mapping
	 * @return the element
	 */
	static XElement parse(String name, String content, Map<String, String> map) {
		XElement result = new XElement("player");
		if (name.startsWith("ALIEN3")) {
			result.set("id", "Morgath");
		} else
		if (name.startsWith("ALIEN4")) {
			result.set("id", "Ychom");
		} else
		if (name.startsWith("ALIEN5")) {
			result.set("id", "Dribs");
		} else
		if (name.startsWith("ALIEN6")) {
			result.set("id", "Sullep");
		} else
		if (name.startsWith("ALIEN7")) {
			result.set("id", "Dargslan");
		} else
		if (name.startsWith("ALIEN8")) {
			result.set("id", "Ecalep");
		} else
		if (name.startsWith("ALIEN9")) {
			result.set("id", "FreeTraders");
		} else
		if (name.startsWith("ALIEN10")) {
			result.set("id", "FreeNations");
		}
		
		List<String> ts = split(content);
		XElement neg = result.add("negotiate");
		neg.set("type", "DIPLOMATIC_RELATIONS");
		XElement appr;
		
		for (int i = 1; i <= 3; i++) {
			appr = neg.add("approach");
			appr.set("type", "AGGRESSIVE");
			appr.content = label(result, neg, appr, i, ts.get(i - 1), map);
		}			
		for (int i = 4; i <= 6; i++) {
			appr = neg.add("approach");
			appr.set("type", "NEUTRAL");
			appr.content = label(result, neg, appr, i, ts.get(i - 1), map);
		}			
		for (int i = 7; i <= 9; i++) {
			appr = neg.add("approach");
			appr.set("type", "HUMBLE");
			appr.content = label(result, neg, appr, i, ts.get(i - 1), map);
		}			
		
		for (int i = 10; i <= 18; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			resp.set("type", "AGGRESSIVE");
			if (i < 13) {
				resp.set("mode", "YES");
			} else
			if (i < 16) {
				resp.set("mode", "MAYBE");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		for (int i = 19; i <= 27; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			resp.set("type", "NEUTRAL");
			if (i < 22) {
				resp.set("mode", "YES");
			} else
			if (i < 25) {
				resp.set("mode", "MAYBE");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		for (int i = 28; i <= 36; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			resp.set("type", "AGGRESSIVE");
			if (i < 31) {
				resp.set("mode", "YES");
			} else
			if (i < 34) {
				resp.set("mode", "MAYBE");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		neg = result.add("negotiate");
		neg.set("type", "MONEY");
		appr = neg.add("approach");
		appr.content = label(result, neg, 37, ts.get(36), map);
		
		for (int i = 38; i <= 43; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			if (i < 41) {
				resp.set("mode", "YES");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		neg = result.add("negotiate");
		neg.set("type", "TRADE");
		appr = neg.add("approach");
		appr.content = label(result, neg, 44, ts.get(43), map);
		
		for (int i = 45; i <= 50; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			if (i < 48) {
				resp.set("mode", "YES");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		neg = result.add("negotiate");
		neg.set("type", "ALLY");
		appr = neg.add("approach");
		appr.content = label(result, neg, 51, ts.get(50), map);
		
		for (int i = 52; i <= 57; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			if (i < 55) {
				resp.set("mode", "YES");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		neg = result.add("negotiate");
		neg.set("type", "DARGSLAN");
		appr = neg.add("approach");
		appr.content = label(result, neg, 58, ts.get(57), map);
		
		for (int i = 59; i <= 64; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			if (i < 62) {
				resp.set("mode", "YES");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		neg = result.add("negotiate");
		neg.set("type", "SURRENDER");
		appr = neg.add("approach");
		appr.content = label(result, neg, 65, ts.get(64), map);
		
		for (int i = 66; i <= 71; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			if (i < 69) {
				resp.set("mode", "YES");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		neg = result.add("terminate");
		neg.content = label(result, neg, 72, ts.get(71), map);

		neg = result.add("call");
		neg.set("type", "SURRENDER");
		for (int i = 73; i <= 75; i++) {
			appr = neg.add("approach");
			appr.content = label(result, neg, i, ts.get(i - 1), map);
		}
		
		neg = result.add("call");
		neg.set("type", "ALLIANCE");
		for (int i = 76; i <= 78; i++) {
			appr = neg.add("approach");
			appr.content = label(result, neg, i, ts.get(i - 1), map);
		}
		
		neg = result.add("call");
		neg.set("type", "PEACE");
		for (int i = 79; i <= 81; i++) {
			appr = neg.add("approach");
			appr.content = label(result, neg, i, ts.get(i - 1), map);
		}
		
		neg = result.add("call");
		neg.set("type", "MONEY");
		for (int i = 82; i <= 84; i++) {
			appr = neg.add("approach");
			appr.content = label(result, neg, i, ts.get(i - 1), map);
		}
		
		neg = result.add("call");
		neg.set("type", "WAR");
		for (int i = 85; i <= 87; i++) {
			appr = neg.add("approach");
			appr.content = label(result, neg, i, ts.get(i - 1), map);
		}
		
		neg = result.add("call");
		neg.set("type", "RESIGN");
		for (int i = 88; i <= 90; i++) {
			appr = neg.add("approach");
			appr.content = label(result, neg, i, ts.get(i - 1), map);
		}
		
		return result;
	}
	/** 
	 * Select the last non-letter character. 
	 * @param s the string
	 * @return the index
	 */
	static int nonDigit(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c != '-' && c != '+' && c != ' ' && !Character.isDigit(c)) {
				return i - 1;
			}
		}
		return -1;
	}
	/**
	 * Create a label entry for the given raw content.
	 * @param race the race element
	 * @param neg the negotiation/call element
	 * @param appr the approach/response element
	 * @param i the index
	 * @param content the content
	 * @param map the map for the labels
	 * @return the key
	 */
	public static String label(XElement race, XElement neg, XElement appr, int i, String content, Map<String, String> map) {
		String key = "diplomacy."
			+ race.get("id")
			+ "."
			+ neg.get("type")
		;
		if (appr.has("type")) {
			key += "." + appr.get("type");
		}
		if (appr.has("mode")) {
			key += "." + appr.get("mode");
		}
		key += "." + i;
		content = content
		.replaceAll("\r\n", " ")
		.replaceAll("\\s+", " ")
		.replaceAll("zzzzzzz", "%s")
		.replaceAll("xxxxx", "%s")
		.replaceAll("aaaaa", "%s")
		.trim();
		
		if (map.put(key, content) != null) {
			System.err.printf("duplicate: %s = %s%n", key, content);
		}
		
		return key;
	}
	/**
	 * Create a label entry for the given raw content.
	 * @param race the race element
	 * @param neg the negotiation/call element
	 * @param i the index
	 * @param content the content
	 * @param map the map for the labels
	 * @return the key
	 */
	public static String label(XElement race, XElement neg, int i, String content, Map<String, String> map) {
		String key = "diplomacy."
			+ race.get("id")
		;
		if (neg.has("type")) {
			key += "." + neg.get("type");
		} else {
			key += "." + neg.name;
		}

		key += "." + i;
		content = content
		.replaceAll("\r\n", " ")
		.replaceAll("\\s+", " ")
		.replaceAll("zzzzzzz", "%s")
		.replaceAll("xxxxx", "%s")
		.replaceAll("aaaaa", "%s")
		.trim();
		
		if (content.startsWith("*")) {
			content = content.substring(1);
		}
		
		if (map.put(key, content) != null) {
			System.err.printf("duplicate: %s = %s%n", key, content);
		}
		
		return key;
	}
	/**
	 * Split the content by the @ symbols.
	 * @param content the content
	 * @return the talks
	 */
	static List<String> split(String content) {
		List<String> result = new ArrayList<>();
		
		content = content.replaceAll("#.*?\r\n", "").trim();
		
		int start = 0;
		do {
			int next = content.indexOf('@', start);
			if (next > 0) {
				result.add(content.substring(start, next));
			} else 
			if (next < 0) {
				result.add(content.substring(start));
				break;
			}
			start = next + 1;
		} while (true);
		
		return result;
	}
	/**
	 * Transcode a latin text into a proper UTF-8 text.
	 * @param text the source text
	 * @param codes the code table
	 * @return the transcoded text
	 */
	public static String transcode(String text, Pair<String, String> codes) {
		if (codes == null) {
			return text;
		}
		Map<Character, Character> map = new HashMap<>();
		for (int i = 0; i < codes.first.length(); i++) {
			map.put(codes.first.charAt(i), codes.second.charAt(i));
		}
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			Character c2 = map.get(c);
			if (c2 != null) {
				b.append(c2);
			} else {
				if (c >= 'A') {
					System.err.printf("Charcode: %d %s  %s%n", (int)c, c, text);
				}
				if (c == 0) {
					c = ' ';
				}
				b.append(c);
			}
		}
		
		return b.toString();
	}
	/**
	 * Extract a text file from the map.
	 * @param pacs the PAC map.
	 * @param name the file name
	 * @param codes the transcoding table.
	 * @return list of lines
	 * @throws IOException on error
	 */
	public static List<String> getText(Map<String, PACEntry> pacs, String name, Pair<String, String> codes)
			throws IOException {
		List<String> result = new ArrayList<>();
		try (BufferedReader bin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(pacs.get(name).data), "CP437"))) {
			String line;
			while ((line = bin.readLine()) != null) {
				result.add(transcode(line, codes));
			}
		}
		return result;
	}
	/**
	 * Creates a rectangle from a 4-element array of position and size.
	 * @param coords the coordinates
	 * @return the rectangle
	 */
	private static Rectangle fromCoords(String[] coords) {
		return new Rectangle(
				Integer.parseInt(coords[0]),
				Integer.parseInt(coords[1]),
				Integer.parseInt(coords[2]),
				Integer.parseInt(coords[3]));
	}
	/**
	 * Creates the destination filename.
	 * @param type the data type
	 * @param dst the relative file
	 * @return the file
	 */
	private static File createDestination(String type, String dst) {
		File f = new File(destination + type + "/" + language + "/" + dst);
		f.getParentFile().mkdirs();
		return f;
	}
	/**
	 * The conversion routines.
	 * @throws IOException on error
	 * @throws XMLStreamException on error
	 */
	public static void runConversions() throws IOException, XMLStreamException {
		for (XElement xsmp : instructions.childrenWithName("smp")) {
			String src = xsmp.get("src");
			String dst = xsmp.get("dst");
			File src2 = new File(source + src);
			File dst2 = createDestination("audio", dst);
			if (src2.canRead()) {
				System.out.printf("SMP: %s -> %s%n", src, dst);
				convertSMP(src2, dst2);
			} else {
				src2 = new File(source + src + ".wav");
				if (src2.canRead()) {
					System.out.printf("COPY: %s -> %s%n", src + ".wav", dst);
					copyFile(src2, dst2);
				}
			}
		}
		for (XElement xaa : instructions.childrenWithName("ani-sound")) {
			String src = xaa.get("src");
			String dst = xaa.get("dst");
			File srcFile = new File(source + src);
			File dstFile = createDestination("audio", dst);
			if (srcFile.canRead()) {
				System.out.printf("ANI-SOUND: %s -> %s%n", src, dst);
				convertANISound(srcFile, dstFile);
			} else {
				srcFile = new File(source + src + ".wav");
				if (srcFile.canRead()) {
					System.out.printf("COPY: %s -> %s%n", src + ".wav", dst);
					copyFile(srcFile, dstFile);
				} else {
					System.err.printf("ANI-SOUND: %s missing%n", src);
					System.err.flush();
				}
			}
		}
		for (XElement xaa : instructions.childrenWithName("ani-video")) {
			String src = xaa.get("src");
			String dst = xaa.get("dst");
			File srcFile = new File(source + src);
			if (srcFile.canRead()) {
				System.out.printf("ANI-VIDEO: %s -> %s%n", src, dst);
				convertAni(srcFile, createDestination("video", dst));
			} else {
				System.err.printf("ANI-VIDEO: %s missing%n", src);
				System.err.flush();
			}
		}
		for (XElement ximg : instructions.childrenWithName("image")) {
			String src = ximg.get("src");
			BufferedImage img;
			File f = new File(source + src);
			if (f.canRead()) {
				if (src.toLowerCase().endsWith(".pcx")) {
					img = PCXImage.from(f, -1);
				} else {
					img = ImageIO.read(f);
				}
				for (XElement area : ximg.childrenWithName("area")) {
					String[] coords = U.split(area.get("coords"), ",");
					String dst = area.get("dst");
					System.out.printf("IMAGE: %s -> %s%n", src, dst);
					
					extractImage(img,
							Collections.singleton(Pair.of(fromCoords(coords), new Point(0, 0))),
							createDestination("images", dst)
					);
				}
				for (XElement xareas : ximg.childrenWithName("areas")) {
					String dst = xareas.get("dst");
					System.out.printf("IMAGE: %s -> %s%n", src, dst);
					List<Pair<Rectangle, Point>> fragments = new ArrayList<>();
					for (XElement xpart : xareas.childrenWithName("part")) {
						String[] coords = U.split(xpart.get("coords"), ",");
						String[] to = U.split(xpart.get("to"), ",");
						
						Rectangle rect = fromCoords(coords);
						Point pt = new Point(Integer.parseInt(to[0]), Integer.parseInt(to[1]));
						
						fragments.add(Pair.of(rect, pt));
					}
					extractImage(img,
							fragments,
							createDestination("images", dst)
					);
				}
			} else {
				System.out.printf("IMAGE: %s not found%n", f.getAbsolutePath());
			}
		}
		for (XElement ximg : instructions.childrenWithName("image-copy")) {
			String src = ximg.get("src");
			String dst = ximg.get("dst");
			BufferedImage img;
			File f = new File(source + src);
			if (f.canRead()) {
				if (src.toLowerCase().endsWith(".pcx")) {
					img = PCXImage.from(f, -1);
				} else {
					img = ImageIO.read(f);
				}
				System.out.printf("IMAGE-COPY: %s -> %s%n", src, dst);
				ImageIO.write(img, "png", createDestination("images", dst));
			} else {
				System.out.printf("IMAGE-COPY: %s not found%n", f.getAbsolutePath());
			}
		}
		
		convertLabels();
	}
	/**
	 * 
	 * @param args no arguments
	 * @throws Exception ignored.
	 */
	public static void main(String[] args) throws Exception {
		
		instructions = XElement.parseXML(OriginalConverter.class.getResource("originalconverter.xml"));
		
		encoding = "CP-855"; //RU: CP-855, FR: CP-863 
		source = "c:/games/igru/";
		destination = "./";
		language = "ru";
		
		runConversions();
	}
}
