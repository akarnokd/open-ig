/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author karnokd, 2009.01.05.
 * @version $Revision 1.0$
 */
public class IOUtils {
	/**
	 * An array wrapper with bit sized read operations.
	 * @author karnokd
	 */
	public static class BitArray {
		private byte[] data;
		private long offset;
		public BitArray(byte[] data) {
			this.data = data.clone();
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
				ex.printStackTrace();
			}
		} else {
			System.err.println("File inaccessible: " + f);
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
	/**
	 * Save the given byte array as the given file.
	 * @param f the file
	 * @param data the non null data to save
	 */
	public static void save(String f, byte[] data) {
		save(new File(f), data);
	}
	/**
	 * Save the given byte array as the given file.
	 * @param f the file
	 * @param data the non null data to save
	 */
	public static void save(File f, byte[] data) {
		try {
			FileOutputStream fout = new FileOutputStream(f);
			try {
				fout.write(data);
			} finally {
				fout.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * RandomAccessFile to InputStream wrapper class.
	 * @author karnokd
	 */
	private static class RAFtoInputStream extends InputStream implements DataInput {
		/** The backing random access file object. */
		private final RandomAccessFile raf;
		/** The current mark position. */
		private long markPosition = -1;
		/**
		 * Constructor. Receives a non-null opened RandomAccessFile instance.
		 * @param raf a RandomAccessFile instance
		 */
		public RAFtoInputStream(RandomAccessFile raf) {
			this.raf = raf;
		}
		@Override
		public boolean markSupported() {
			return true;
		}
		@Override
		public synchronized void mark(int readlimit) {
			try {
				markPosition = raf.getFilePointer();
			} catch (IOException ex) {
				// ignored, cannot do much about it
			}
		}
		@Override
		public synchronized void reset() throws IOException {
			if (markPosition >= 0) {
				raf.seek(markPosition);
			} else {
				throw new IOException("mark() not called.");
			}
		}
		@Override
		public int read() throws IOException {
			return raf.read();
		}
		@Override
		public int available() throws IOException {
			long avail = raf.length() - raf.getFilePointer();
			return avail > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)avail;
		}
		@Override
		public void close() throws IOException {
			raf.close();
		}
		@Override
		public int read(byte[] b) throws IOException {
			return raf.read(b);
		}
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return raf.read(b, off, len);
		}
		@Override
		public long skip(long n) throws IOException {
			long newoffs = raf.getFilePointer() + n;
			long toskip = newoffs > raf.length() ? raf.length() : newoffs;
			raf.seek(toskip);
			return toskip;
		}
		@Override
		public boolean readBoolean() throws IOException {
			return raf.readBoolean();
		}
		@Override
		public byte readByte() throws IOException {
			return raf.readByte();
		}
		@Override
		public char readChar() throws IOException {
			return raf.readChar();
		}
		@Override
		public double readDouble() throws IOException {
			return raf.readDouble();
		}
		@Override
		public float readFloat() throws IOException {
			return raf.readFloat();
		}
		@Override
		public void readFully(byte[] b) throws IOException {
			raf.readFully(b);
		}
		@Override
		public void readFully(byte[] b, int off, int len) throws IOException {
			raf.readFully(b, off, len);
		}
		@Override
		public int readInt() throws IOException {
			return raf.readInt();
		}
		@Override
		public String readLine() throws IOException {
			return raf.readLine();
		}
		@Override
		public long readLong() throws IOException {
			return raf.readLong();
		}
		@Override
		public short readShort() throws IOException {
			return raf.readShort();
		}
		@Override
		public String readUTF() throws IOException {
			return raf.readUTF();
		}
		@Override
		public int readUnsignedByte() throws IOException {
			return raf.readUnsignedByte();
		}
		@Override
		public int readUnsignedShort() throws IOException {
			return raf.readUnsignedShort();
		}
		@Override
		public int skipBytes(int n) throws IOException {
			return raf.skipBytes(n);
		}
	}
	/**
	 * Wraps the supplied RandomAccessFile into a regular InputStream allowing it to be
	 * processed sequentially. The returned inputstream supports the DataInput interface.
	 * @param raf the RandomAccessFile instance
	 * @return the created custom InputStream object.
	 */
	public static InputStream asInputStream(RandomAccessFile raf) {
		return new RAFtoInputStream(raf);
	}
	/**
	 * An iterable class that allows a string to be iterated over line by line.
	 * @author karnokd
	 */
	private static class TextIterator implements Iterable<String> {
		/** The string to operate on. */
		private final String buffer;
		/**
		 * Constructor. Sets the string to use.
		 * @param buffer the nonnul string to use
		 */
		public TextIterator(String buffer) {
			this.buffer = buffer;
		}
		@Override
		public Iterator<String> iterator() {
			return new Iterator<String>() {
				/** The buffered reader to read lines. */
				private BufferedReader in = new BufferedReader(new StringReader(buffer));
				/** The current line or null for no more lines. */
				private String line;
				@Override
				public boolean hasNext() {
					try {
						return (line = in.readLine()) != null;
					} catch (IOException ex) {
						throw new AssertionError("IOException on in-memory object.");
					}
				}
				@Override
				public String next() {
					if (line != null) {
						return line;
					}
					throw new NoSuchElementException();
				}
				@Override
				public void remove() {
					throw new UnsupportedOperationException("remove");
				}
			};
		}
	}
	/**
	 * Converts the data into a string using the specified encoding and returns an Iterable object to
	 * iterate over the text lines.
	 * @param data the data bytes to use
	 * @param encoding the encoding to use
	 * @return the iterable to iterate over the lines
	 * @throws UnsupportedEncodingException if the encoding cannot be used
	 */
	public static Iterable<String> textIterator(byte[] data, String encoding) throws UnsupportedEncodingException {
		return new TextIterator(new String(data, encoding));
	}
	/**
	 * Converts the data into a string using the specified encoding and returns an Iterable object to
	 * iterate over the text lines.
	 * @param data the data bytes to use
	 * @param encoding the encoding to use
	 * @return the iterable to iterate over the lines
	 */
	public static Iterable<String> textIterator(byte[] data, Charset encoding) {
		return new TextIterator(new String(data, encoding));
	}
	/**
	 * Skips the number of bytes from the given input stream.
	 * @param in the input stream
	 * @param count the number of bytes to skip
	 * @throws IOException if the input stream throws this exception
	 * @throws EOFException if there is less than count bytes in the stream remaining.
	 */
	public static void skipFully(InputStream in, long count) throws IOException {
		while (count > 0) {
			long c = in.skip(count);
			if (c < 0) {
				throw new EOFException();
			}
			count -= c;
		}
	}
	/**
	 * Skips the number of bytes from the given data input.
	 * @param in the input stream
	 * @param count the number of bytes to skip
	 * @throws IOException if the input stream throws this exception
	 * @throws EOFException if there is less than count bytes in the stream remaining.
	 */
	public static void skipFullyD(DataInput in, int count) throws IOException {
		while (count > 0) {
			int c = in.skipBytes(count);
			if (c < 0) {
				throw new EOFException();
			}
			count -= c;
		}
	}
	/**
	 * Read bitstream from a static array
	 * @author Karnok Dávid, Apr 11, 2007
	 * @version 1.0
	 */
	public static class BitReader {
		private byte[] stream;
		private int bitOffset;
		private int byteOffset;
		public BitReader(byte[] source) {
			stream = source;
		}
		public int inputBit() {
			int b = (stream[byteOffset] & (1 << bitOffset)) != 0?1:0;
			bitOffset++;
			if (bitOffset >= 8) {
				bitOffset = 0;
				byteOffset++;
			}
			return b;
		}
		/**
		 * Input number of bits
		 * @param n input bit count 1..8
		 * @return
		 */
		public int inputBits(int n) {
			int result = -1;
			// do we have byte wrap?
			if (bitOffset + n <= 8) {
				int b = (stream[byteOffset] & 0xFF) >> bitOffset;
				result = b & ((1 << n) - 1);
			} else {
				int b = (stream[byteOffset] & 0xFF) >> bitOffset;
				int nextoffset = n - 8 + bitOffset;
				int c = stream[byteOffset+1] & ((1 << nextoffset) - 1);
				result = b | (c << (8 - bitOffset));
			}
			bitOffset += n;
			if (bitOffset >= 8) {
				byteOffset += bitOffset >> 3;
				bitOffset &= 7;
			}
			return result;
		}
	}
	public static class BitWriter {
		private int bitOffset;
		private int currentByte;
		private OutputStream out;
		public BitWriter(OutputStream out) {
			this.out = out;
		}
		public void outputBits(int code, int n) throws IOException {
//			System.out.println("Code: "+Integer.toBinaryString(code)+" size: "+n);
			for(int i=0; i<n; i++) {
				currentByte |= (code & (1 << i)) != 0?1 << bitOffset:0;
				bitOffset++;
				if (bitOffset >= 7) {
					bitOffset = 0;
					out.write(currentByte);
					currentByte = 0;
				}
			}
		}
		public void flush() throws IOException {
			if (bitOffset > 0) {
				out.write(currentByte);
			}
		}
	}
}
