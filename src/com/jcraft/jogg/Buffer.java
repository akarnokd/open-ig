/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/* JOrbis
 * Copyright (C) 2000 ymnk, JCraft,Inc.
 *  
 * Written by: 2000 ymnk<ymnk@jcraft.com>
 *   
 * Many thanks to 
 *   Monty <monty@xiph.org> and 
 *   The XIPHOPHORUS Company http://www.xiph.org/ .
 * JOrbis has been based on their awesome works, Vorbis codec.
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.jcraft.jogg;
/**
 * Buffer class to perform bitvise stream operations.
 * Comments and style correction by karnokd. Don't expect
 * every comment to be accurate.
 * @author ymnk
 */
public class Buffer {
	/** Buffer increment. */
	private static final int BUFFER_INCREMENT = 256;
	/** Buffer bitmask. */
	private static final int[] MASK = { 0x00000000, 0x00000001, 0x00000003,
			0x00000007, 0x0000000f, 0x0000001f, 0x0000003f, 0x0000007f,
			0x000000ff, 0x000001ff, 0x000003ff, 0x000007ff, 0x00000fff,
			0x00001fff, 0x00003fff, 0x00007fff, 0x0000ffff, 0x0001ffff,
			0x0003ffff, 0x0007ffff, 0x000fffff, 0x001fffff, 0x003fffff,
			0x007fffff, 0x00ffffff, 0x01ffffff, 0x03ffffff, 0x07ffffff,
			0x0fffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff, 0xffffffff };
	/** Pointer into the buffer. */
	int ptr = 0;
	/** The buffer. */
	byte[] buffer = null;
	/** End bit. */
	int endbit = 0;
	/** End byte. */
	int endbyte = 0;
	/** Storage. */
	int storage = 0;
	/** Initializes the buffer. */
	public void writeinit() {
		buffer = new byte[BUFFER_INCREMENT];
		ptr = 0;
		buffer[0] = (byte) '\0';
		storage = BUFFER_INCREMENT;
	}
	/**
	 * Writes the supplied byte array into the buffer.
	 * @param s the data array to write
	 */
	public void write(byte[] s) {
        for (byte value : s) {
            if (value == 0) {
                break;
            }
            write(value, 8);
        }
	}
	/**
	 * Reads into the supplied byte array the number of bytes.
	 * @param s the output byte array
	 * @param bytes the number of bytes to read
	 */
	public void read(byte[] s, int bytes) {
		int i = 0;
		while (bytes-- != 0) {
			s[i++] = (byte) (read(8));
		}
	}
	/** Resets the buffer to the beginning. */
	void reset() {
		ptr = 0;
		buffer[0] = (byte) '\0';
		endbit = 0; 
		endbyte = 0;
	}
	/** Frees the internal buffer. */
	public void writeclear() {
		buffer = null;
	}
	/**
	 * Initialize the buffer to use the supplied data.
	 * @param buf the byte array to use, shares the array
	 * @param bytes number of bytes to use
	 */
	public void readinit(byte[] buf, int bytes) {
		readinit(buf, 0, bytes);
	}
	/**
	 * Initialize the buffer to use the supplied data range.
	 * @param buf the byte array to use, shares the array
	 * @param start the start index
	 * @param bytes the number of bytes to use
	 */
	public void readinit(byte[] buf, int start, int bytes) {
		ptr = start;
		buffer = buf;
		endbit = 0; 
		endbyte = 0;
		storage = bytes;
	}
	/**
	 * Writes the specified number of bits into the buffer.
	 * @param value the value
	 * @param bits the number of bits to write
	 */
	public void write(int value, int bits) {
		if (endbyte + 4 >= storage) {
			byte[] foo = new byte[storage + BUFFER_INCREMENT];
			System.arraycopy(buffer, 0, foo, 0, storage);
			buffer = foo;
			storage += BUFFER_INCREMENT;
		}

		value &= MASK[bits];
		bits += endbit;
		buffer[ptr] |= (byte) (value << endbit);

		if (bits >= 8) {
			buffer[ptr + 1] = (byte) (value >> (8 - endbit)); // removed >>
			if (bits >= 16) {
				buffer[ptr + 2] = (byte) (value >> (16 - endbit)); // removed >>
				if (bits >= 24) {
					buffer[ptr + 3] = (byte) (value >> (24 - endbit)); // removed >>
					if (bits >= 32) {
						if (endbit > 0) {
							buffer[ptr + 4] = (byte) (value >> (32 - endbit)); // removed >>
						} else {
							buffer[ptr + 4] = 0;
						}
					}
				}
			}
		}

		endbyte += bits / 8;
		ptr += bits / 8;
		endbit = bits & 7;
	}
	/** 
	 * Looks ahead a number of bits and returns this value without moving the read pointer.
	 * @param bits to read
	 * @return the bits read
	 */
	public int look(int bits) {
		int ret;
		int m = MASK[bits];

		bits += endbit;

		if (endbyte + 4 >= storage) {
			if (endbyte + (bits - 1) / 8 >= storage) {
				return (-1);
			}
		}

		ret = ((buffer[ptr]) & 0xff) >>> endbit;
		if (bits > 8) {
			ret |= ((buffer[ptr + 1]) & 0xff) << (8 - endbit);
			if (bits > 16) {
				ret |= ((buffer[ptr + 2]) & 0xff) << (16 - endbit);
				if (bits > 24) {
					ret |= ((buffer[ptr + 3]) & 0xff) << (24 - endbit);
					if (bits > 32 && endbit != 0) {
						ret |= ((buffer[ptr + 4]) & 0xff) << (32 - endbit);
					}
				}
			}
		}
		return (m & ret);
	}
	/**
	 * Returns the next bit without moving the read pointer.
	 * @return the next bit
	 */
	public int look1() {
		if (endbyte >= storage) {
			return (-1);
		}
		return ((buffer[ptr] >> endbit) & 1);
	}
	/**
	 * Advance the read pointer by the supplied number of bits.
	 * @param bits the bits to skip
	 */
	public void adv(int bits) {
		bits += endbit;
		ptr += bits / 8;
		endbyte += bits / 8;
		endbit = bits & 7;
	}
	/** Advance the read pointer by one bit. */
	public void adv1() {
		++endbit;
		if (endbit > 7) {
			endbit = 0;
			ptr++;
			endbyte++;
		}
	}
	/**
	 * Reads the given amount of bits from the buffer.
	 * @param bits the number of bits to read
	 * @return the read value
	 */
	public int read(int bits) {
		int ret;
		int m = MASK[bits];

		bits += endbit;

		if (endbyte + 4 >= storage) {
			ret = -1;
			if (endbyte + (bits - 1) / 8 >= storage) {
				ptr += bits / 8;
				endbyte += bits / 8;
				endbit = bits & 7;
				return (ret);
			}
		}

		ret = ((buffer[ptr]) & 0xff) >>> endbit;
		if (bits > 8) {
			ret |= ((buffer[ptr + 1]) & 0xff) << (8 - endbit);
			if (bits > 16) {
				ret |= ((buffer[ptr + 2]) & 0xff) << (16 - endbit);
				if (bits > 24) {
					ret |= ((buffer[ptr + 3]) & 0xff) << (24 - endbit);
					if (bits > 32 && endbit != 0) {
						ret |= ((buffer[ptr + 4]) & 0xff) << (32 - endbit);
					}
				}
			}
		}

		ret &= m;

		ptr += bits / 8;
		endbyte += bits / 8;
		endbit = bits & 7;
		return (ret);
	}
	/**
	 * Reads bits.
	 * @param bits the number of bits to read
	 * @return the value read
	 */
	public int readB(int bits) {
		int ret;
		int m = 32 - bits;

		bits += endbit;

		if (endbyte + 4 >= storage) {
			/* not the main path */
			ret = -1;
			if (endbyte * 8 + bits > storage * 8) {
				ptr += bits / 8;
				endbyte += bits / 8;
				endbit = bits & 7;
				return (ret);
			}
		}

		ret = (buffer[ptr] & 0xff) << (24 + endbit);
		if (bits > 8) {
			ret |= (buffer[ptr + 1] & 0xff) << (16 + endbit);
			if (bits > 16) {
				ret |= (buffer[ptr + 2] & 0xff) << (8 + endbit);
				if (bits > 24) {
					ret |= (buffer[ptr + 3] & 0xff) << (endbit);
					if (bits > 32 && (endbit != 0)) {
						ret |= (buffer[ptr + 4] & 0xff) >> (8 - endbit);
					}
				}
			}
		}
		ret = (ret >>> (m >> 1)) >>> ((m + 1) >> 1);

		ptr += bits / 8;
		endbyte += bits / 8;
		endbit = bits & 7;
		return (ret);
	}
	/**
	 * Reads the next bit.
	 * @return the next bit
	 */
	public int read1() {
		int ret;
		if (endbyte >= storage) {
			ret = -1;
			endbit++;
			if (endbit > 7) {
				endbit = 0;
				ptr++;
				endbyte++;
			}
			return (ret);
		}

		ret = (buffer[ptr] >> endbit) & 1;

		endbit++;
		if (endbit > 7) {
			endbit = 0;
			ptr++;
			endbyte++;
		}
		return (ret);
	}
	/**
	 * Returns the current number of bytes from the beginning of the buffer.
	 * @return the current number of bytes from the beginning of the buffer
	 */
	public int bytes() {
		return (endbyte + (endbit + 7) / 8);
	}
	/** 
	 * Returns the current bit offset.
	 * @return the current bit offset
	 */
	public int bits() {
		return (endbyte * 8 + endbit);
	}
	/**
	 * Returns the underlying array buffer in a shared form.
	 * @return the underlying array buffer in a shared form
	 */
	public byte[] buffer() {
		return (buffer);
	}
	/**
	 * Returns the integer log 2 of the value.
	 * @param v the value
	 * @return the integer log 2 of the value
	 */
	public static int ilog(int v) {
		int ret = 0;
		while (v > 0) {
			ret++;
			v >>>= 1;
		}
		return (ret);
	}
}
