/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.io.RandomAccessFile;

/**
 * Remove the few frames of an audio file.
 * @author akarnokd, 2012.04.20.
 *
 */
public final class RemoveBeginning {
	/** Utility class. */
	private RemoveBeginning() { }
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		try (RandomAccessFile raf = new RandomAccessFile("audio/en/messages/new_caroline_virus.wav", "rw")) {
			int len = (2205) * 1;
			if (len % 2 == 1) {
				len++;
			}
			raf.seek(4);
			int clen = Integer.reverseBytes(raf.readInt());
			raf.seek(4);
			raf.writeInt(Integer.reverseBytes(clen - len));
			raf.seek(0x28);
			int dlen = Integer.reverseBytes(raf.readInt());
			raf.seek(0x28);
			raf.writeInt(Integer.reverseBytes(dlen - len));
			
			byte[] buffer = new byte[len];
			long rp = 0x2C + buffer.length;
			long wp = 0x2C;
			int c;
			do {
				raf.seek(rp);
				c = raf.read(buffer);
				raf.seek(wp);
				raf.write(buffer, 0, c);
				rp += c;
				wp += c;
			} while (c == buffer.length);
			raf.setLength(raf.getFilePointer());
		}
	}
}
