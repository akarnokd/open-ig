/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
 * Contains Input / Output related functions and classes.
 * @author akarnokd, 2009.01.05.
 */
public final class IOUtils {
	/** Private constructor. */
	private IOUtils() {
		// utility class
	}
	/**
	 * An array wrapper with bit sized read operations.
	 * @author akarnokd
	 */
	public static class BitArray {
		/** The data bytes. */
		private byte[] data;
		/** The current bit offset. */
		private long offset;
		/**
		 * Constructor.
		 * @param data the data to use
		 */
		public BitArray(byte[] data) {
			this.data = data.clone();
		}
		/**
		 * Sets the current bit offset. 
		 * @param offset the offset
		 */
		public void setOffset(long offset) {
			this.offset = offset;
		}
		/**
		 * Returns the current bit offset.
		 * @return the current bit offset
		 */
		public long getOffset() {
			return offset;
		}
		/**
		 * Reads the given number of bits in LSB style.
		 * @param count the number of bits
		 * @return the value read
		 */
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
		/**
		 * Reads the given number of bits but stores it MSB.
		 * @param count the number of bits
		 * @return the value read
		 */
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
		/**
		 * Returns the length of the data in bits.
		 * @return length in bits
		 */
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
			try (RandomAccessFile fin = new RandomAccessFile(f, "r")) {
				fin.readFully(buffer);
				return buffer;
			} catch (IOException ex) {
				Exceptions.add(ex);
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
		try (FileOutputStream fout = new FileOutputStream(f)) {
			fout.write(data);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
	}
	/**
	 * RandomAccessFile to InputStream wrapper class.
	 * @author akarnokd
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
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean markSupported() {
			return true;
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized void mark(int readlimit) {
			try {
				markPosition = raf.getFilePointer();
			} catch (IOException ex) {
				// ignored, cannot do much about it
			}
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized void reset() throws IOException {
			if (markPosition >= 0) {
				raf.seek(markPosition);
			} else {
				throw new IOException("mark() not called.");
			}
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read() throws IOException {
			return raf.read();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int available() throws IOException {
			long avail = raf.length() - raf.getFilePointer();
			return avail > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)avail;
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void close() throws IOException {
			raf.close();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read(byte[] b) throws IOException {
			return raf.read(b);
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return raf.read(b, off, len);
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public long skip(long n) throws IOException {
			long newoffs = raf.getFilePointer() + n;
			long toskip = newoffs > raf.length() ? raf.length() : newoffs;
			raf.seek(toskip);
			return toskip;
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean readBoolean() throws IOException {
			return raf.readBoolean();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public byte readByte() throws IOException {
			return raf.readByte();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public char readChar() throws IOException {
			return raf.readChar();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public double readDouble() throws IOException {
			return raf.readDouble();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public float readFloat() throws IOException {
			return raf.readFloat();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void readFully(byte[] b) throws IOException {
			raf.readFully(b);
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void readFully(byte[] b, int off, int len) throws IOException {
			raf.readFully(b, off, len);
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int readInt() throws IOException {
			return raf.readInt();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public String readLine() throws IOException {
			return raf.readLine();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public long readLong() throws IOException {
			return raf.readLong();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public short readShort() throws IOException {
			return raf.readShort();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public String readUTF() throws IOException {
			return raf.readUTF();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int readUnsignedByte() throws IOException {
			return raf.readUnsignedByte();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int readUnsignedShort() throws IOException {
			return raf.readUnsignedShort();
		}
		/**
		 * {@inheritDoc}
		 */
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
	 * @author akarnokd
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
		/**
		 * {@inheritDoc}
		 */
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
						line = in.readLine();
						return line != null;
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
	 * Read bitstream from a static array.
	 * @author akarnokd, Apr 11, 2007
	 * @version 1.0
	 */
	public static class BitReader {
		/** The source byte array. */
		private byte[] stream;
		/** The current bit offset. */
		private int bitOffset;
		/** The current byte offset. */
		private int byteOffset;
		/**
		 * Constructor.
		 * @param source the source array to use
		 */
		public BitReader(byte[] source) {
			stream = source;
		}
		/**
		 * Read one bit.
		 * @return the bit value
		 */
		public int inputBit() {
			int b = (stream[byteOffset] & (1 << bitOffset)) != 0 ? 1 : 0;
			bitOffset++;
			if (bitOffset >= 8) {
				bitOffset = 0;
				byteOffset++;
			}
			return b;
		}
		/**
		 * Input number of bits.
		 * @param n input bit count 1..8
		 * @return the bits read
		 */
		public int inputBits(int n) {
			int result;
			// do we have byte wrap?
			if (bitOffset + n <= 8) {
				int b = (stream[byteOffset] & 0xFF) >> bitOffset;
				result = b & ((1 << n) - 1);
			} else {
				int b = (stream[byteOffset] & 0xFF) >> bitOffset;
				int nextoffset = n - 8 + bitOffset;
				int c = stream[byteOffset + 1] & ((1 << nextoffset) - 1);
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
	/**
	 * Write data to an output stream as bits. 
	 * @author akarnokd
	 */
	public static class BitWriter {
		/** The current logical bit offset. */
		private int bitOffset;
		/** The current logical byte offset. */
		private int currentByte;
		/** The output stream. */
		private OutputStream out;
		/**
		 * Constructor.
		 * @param out the output stream to write to
		 */
		public BitWriter(OutputStream out) {
			this.out = out;
		}
		/**
		 * Write bits.
		 * @param code the bits to write
		 * @param n length
		 * @throws IOException if the underlying OutputStream.write() throws it
		 */
		public void outputBits(int code, int n) throws IOException {
//			System.out.println("Code: "+Integer.toBinaryString(code)+" size: "+n);
			for (int i = 0; i < n; i++) {
				currentByte |= (code & (1 << i)) != 0 ? 1 << bitOffset : 0;
				bitOffset++;
				if (bitOffset >= 7) {
					bitOffset = 0;
					out.write(currentByte);
					currentByte = 0;
				}
			}
		}
		/**
		 * Flushes the underlying output stream.
		 * @throws IOException if the underlying stream throws it
		 */
		public void flush() throws IOException {
			if (bitOffset > 0) {
				out.write(currentByte);
			}
		}
	}
	/**
	 * Read an integer as Little endian.
	 * @param in the input stream to read
	 * @return the value read
	 * @throws IOException if there is not enough bytes to read for an int
	 */
	public static int readIntLE(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return ((ch1) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
	}
	/**
	 * Read an integer as Little endian.
	 * @param in the input stream to read
	 * @return the value read
	 * @throws IOException if there is not enough bytes to read for an int
	 */
	public static int readIntLE(RandomAccessFile in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return ((ch1) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
	}
	/**
	 * Copy the contents of the input stream into the output stream using the given batch
	 * sized buffer. Does not close the streams.
	 * @param in the input stream
	 * @param out the output stream
	 * @param batch the batch size
	 * @throws IOException on error
	 */
	public static void copy(InputStream in, OutputStream out, int batch) throws IOException {
		if (batch < 4096) {
			batch = 4096;
		}
		byte[] buffer = new byte[batch];
		do {
			int read = in.read(buffer);
			if (read > 0) {
				out.write(buffer, 0, read);
			} else
			if (read < 0) {
				break;
			}
		} while (!Thread.currentThread().isInterrupted());
	}
	/**
	 * Load everything from the given input stream.
	 * Does not close the input stream.
	 * @param in the input stream
	 * @return the bytes
	 */
	public static byte[] load(InputStream in) {
		byte[] buffer = new byte[64 * 1024];
		ByteArrayOutputStream bout = new ByteArrayOutputStream(64 * 1024);
		try {
			do {
				int r = in.read(buffer);
				if (r > 0) {
					bout.write(buffer, 0, r);
				} else
				if (r < 0) {
					break;
				}
			} while (true);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		return bout.toByteArray();
	}
}
