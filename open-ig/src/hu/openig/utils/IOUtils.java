/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author karnokd, 2009.01.05.
 * @version $Revision 1.0$
 */
public class IOUtils {
	public static class BitArray {
		private byte[] data;
		private long offset;
		public BitArray(byte[] data) {
			this.data = data;
		}
		public void setOffset(long offset) {
			this.offset = offset;
		}
		public long getOffset() {
			return offset;
		}
		public int readBits(int count) {
			int r = 0;
			int obs = (int)(offset >> 3);
			int bis = (int)(offset & 0x07);
			int obe = (int)((offset + count - 1) >> 3);
			int shf = 24;
			for (int i = obs; i <= obe; i++) {
				r |= (data[i] & 0xFF) << shf;
				shf -= 8;
			}
			offset += count;
			int s = r >> (32 - bis - count - 1);
			int f = (1 << count) - 1;
			return (s) & (f);
		}
		public int readBitsRev(int count) {
			int r = 0;
			int obs = (int)(offset >> 3);
			int bis = (int)(offset & 0x07);
			int obe = (int)((offset + count - 1) >> 3);
			int shf = 0;
			for (int i = obs; i <= obe; i++) {
				r |= (data[i] & 0xFF) << shf;
				shf += 8;
			}
			offset += count;
			int s = r >> (bis);
			int f = (1 << count) - 1;
			return (s) & (f);
		}
		public long getBitSize() {
			return data.length * 8L;
		}
	}
	/**
	 * Loads an entire file from the filesystem.
	 * @param f the file to load
	 * @return the bytes of file or an empty array
	 */
	public static byte[] load(File f) {
		if (f.canRead()) {
			byte[] buffer = new byte[(int)f.length()];
			try {
				RandomAccessFile fin = new RandomAccessFile(f, "r");
				try {
					fin.readFully(buffer);
					return buffer;
				} finally {
					fin.close();
				}
			} catch (IOException ex) {
				// ignored
			}
		}
		return new byte[0];
	}
	/**
	 * Loads an entire file from the filesystem.
	 * @param f the file name to load
	 * @return the bytes of the file or an empty array
	 */
	public static byte[] load(String f) {
		return load(new File(f));
	}
}
