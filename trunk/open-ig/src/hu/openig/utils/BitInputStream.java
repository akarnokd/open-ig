/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * The BitInputStream allows reading individual bits from a general Java
 * InputStream. Like the various Stream-classes from Java, the BitInputStream
 * has to be created based on another Input stream. It provides a function to
 * read the next bit from the sream, as well as to read multiple bits at once
 * and write the resulting data into an integer value.
 * It is not threadsafe!
 * @author Andreas Jakl
 */
public class BitInputStream {
	/**
	 * The Java InputStream this class is working on.
	 */
	private InputStream iIs;

	/**
	 * The buffer containing the currently processed byte of the input stream.
	 */
	private int iBuffer;

	/**
	 * Next bit of the current byte value that the user will get. If it's 8, the
	 * next bit will be read from the next byte of the InputStream.
	 */
	private int iNextBit = 8;

	/**
	 * Create a new bit input stream based on an existing Java InputStream.
	 * 
	 * @param aIs
	 *            the input stream this class should read the bits from.
	 */
	public BitInputStream(InputStream aIs) {
		iIs = aIs;
	}

	/**
	 * Read a specified number of bits and return them combined as an integer
	 * value. The bits are written to the integer starting at the highest bit (
	 * << aNumberOfBits ), going down to the lowest bit ( << 0 )
	 * 
	 * @param aNumberOfBits
	 *            defines how many bits to read from the stream.
	 * @return integer value containing the bits read from the stream.
	 * @throws IOException if the underlying stream throws it
	 */
	public int readBits(final int aNumberOfBits)
			throws IOException {
		int value = 0;
		for (int i = aNumberOfBits - 1; i >= 0; i--) {
			value |= (readBit() << i);
		}
		return value;
	}

	/**
	 * Read the next bit from the stream.
	 * 
	 * @return 0 if the bit is 0, 1 if the bit is 1.
	 * @throws IOException if the underlying stream throws it
	 */
	public int readBit() throws IOException {
		if (iIs == null) {
			throw new IOException("Already closed");
		}
		if (iNextBit == 8) {
			iBuffer = iIs.read();

			if (iBuffer == -1) {
				throw new EOFException();
			}

			iNextBit = 0;
		}

		int bit = iBuffer & (1 << iNextBit);
		iNextBit++;

		bit = (bit == 0) ? 0 : 1;

		return bit;
	}

	/**
	 * Close the underlying input stream.
	 * 
	 * @throws IOException if the underlying stream throws it
	 */
	public void close() throws IOException {
		iIs.close();
		iIs = null;
	}
}
