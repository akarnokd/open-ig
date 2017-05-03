/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.utils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to handle Imperium Galactica's PAC files.
 * <p>
 * The format:<br>
 * <pre>
 * WORD itemcount
 * Entries {
 *   0x00 BYTE filename_length
 *        BYTE(* filename_length) filename
 *        BYTE(* 13 - filename_length) 0x2E padding
 *   0x0E WORD data_length
 *   0x10 DWORD data_absolute_offset
 * }
 * </pre> 
 * @author akarnokd, 2009.01.10
 */
public final class PACFile {
	/** Private constructor. */
	private PACFile() {
		// utility class
	}
	/**
	 * Record to store entry information about various entries in the Pack file.
	 * @author akarnokd
	 */
	public static class PACEntry {
		/** The entry's filename. */
		public String filename;
		/** The starting offset of the data. */
		public long offset;
		/** The size of the data. */
		public int size;
		/** The binary data of the entry. */
		public byte[] data;
	}
	/**
	 * Parses the given file fully and loads all entries into the memory.
	 * @param f the file to parse fully
	 * @return the non-null list of pac entry records filled with data
	 */
	public static List<PACEntry> parseFully(File f) {
		try (RandomAccessFile fin = new RandomAccessFile(f, "r")) {
			// Parse header
			byte[] entry = new byte[20];
			fin.readFully(entry, 0, 2);
			int count = (entry[1] & 0xFF) << 8 | (entry[0] & 0xFF);
			List<PACEntry> result = new ArrayList<>(count);
			// parse entries
			for (int i = 0 ; i < count; i++) {
				PACEntry pe = new PACEntry();
				fin.readFully(entry);
				pe.filename = new String(entry, 1, entry[0], "ISO-8859-1");
				pe.size = (entry[0x0E] & 0xFF) | (entry[0x0F] & 0xFF) << 8;
				pe.offset = (entry[0x10] & 0xFF) | (entry[0x11] & 0xFF) << 8 | (entry[0x12] & 0xFF) << 16 | (entry[0x13] & 0xFF) << 24;
				result.add(pe);
			}
			// load entries
			for (int i = 0; i < count; i++) {
				PACEntry e = result.get(i);
				fin.seek(e.offset);
				e.data = new byte[e.size];
				fin.readFully(e.data);
			}
			return result;
		} catch (IOException ex) {
			// ignored
			return Collections.emptyList();
		}
	}
	/**
	 * Parses the given filename fully and loads all entries into the memory.
	 * @param filename the filename to parse fully
	 * @return the non-null list of pac entry records filled with data
	 */
	public static List<PACEntry> parseFully(String filename) {
		return parseFully(new File(filename));
	}
	/**
	 * Parse a PAC file from the given non-seekable input stream. The stream should
	 * be at the beginning of the PAC header. The stream will be positioned after the last
	 * entry's data. Does not close the stream
	 * @param in the input stream to parse from
	 * @return the nonnull list of PACEntry objects filled in with data.
	 * @throws IOException if an I/O error occurs during the operation
	 */
	public static List<PACEntry> parseFully(InputStream in) throws IOException {
		// Parse header
		DataInputStream din = new DataInputStream(in);
		// the current offset
		long offset = 0;
		byte[] entry = new byte[20];
		din.readFully(entry, 0, 2);
		offset += 2;
		int count = (entry[1] & 0xFF) << 8 | (entry[0] & 0xFF);
		List<PACEntry> result = new ArrayList<>(count);
		// parse entries
		for (int i = 0 ; i < count; i++) {
			PACEntry pe = new PACEntry();
			din.readFully(entry);
			offset += entry.length;
			pe.filename = new String(entry, 1, entry[0], "ISO-8859-1");
			pe.size = (entry[0x0E] & 0xFF) | (entry[0x0F] & 0xFF) << 8;
			pe.offset = (entry[0x10] & 0xFF) | (entry[0x11] & 0xFF) << 8 | (entry[0x12] & 0xFF) << 16 | (entry[0x13] & 0xFF) << 24;
			result.add(pe);
		}
		// load entries
		for (int i = 0; i < count; i++) {
			PACEntry e = result.get(i);
			while (offset < e.offset) {
				long n = din.skip(e.offset - offset);
				if (n < 0) {
					throw new EOFException("Incomplete PAC file at entry #" + i);
				}
				offset += n;
			}
			e.data = new byte[e.size];
			din.readFully(e.data);
			offset += e.size;
		}
		return result;
	}
	/**
	 * Creates a map from filename to entry based on the supplied collection of PACEntry objects.
	 * @param it the collection of PACEntry objects
	 * @return a map from PACEntry.filename to PACEntry objects
	 */
	public static Map<String, PACEntry> mapByName(Collection<? extends PACEntry> it) {
		Map<String, PACEntry> result = new LinkedHashMap<>(it.size());
		for (PACEntry e : it) {
			result.put(e.filename, e);
		}
		return result;
	}
}
