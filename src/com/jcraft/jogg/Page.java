/*
 * Copyright 2008-present, David Karnok & Contributors
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
 * Page object.
 * Comments and style correction by karnokd
 * @author ymnk
 */
public class Page {
    /** CRC lookup table. */
    private static final int[] CRC_LOOKUP = new int[256];
    static {
        for (int i = 0; i < CRC_LOOKUP.length; i++) {
            CRC_LOOKUP[i] = crcEntry(i);
        }
    }
    /**
     * Calculates a CRC entry.
     * @param index the index
     * @return the entry value
     */
    private static int crcEntry(int index) {
        int r = index << 24;
        for (int i = 0; i < 8; i++) {
            if ((r & 0x80000000) != 0) {
                r = (r << 1) ^ 0x04c11db7;

                /*
                 * The same as the ethernet
                 * generator polynomial, although we
                 * use an unreflected alg and an
                 * init/final of 0, not 0xffffffff
                                             */
            } else {
                r <<= 1;
            }
        }
        return r;
    }
    /** Header base array. */
    public byte[] headerBase;
    /** Header start. */
    public int header;
    /** Header length. */
    public int headerLen;
    /** Body base array. */
    public byte[] bodyBase;
    /** Body start. */
    public int body;
    /** Body length. */
    public int bodyLen;
    /**
     * Returns the version.
     * @return the version
     */
    int version() {
        return headerBase[header + 4] & 0xff;
    }
    /**
     * Returns the continued flag.
     * @return the continued flag
     */
    int continued() {
        return (headerBase[header + 5] & 0x01);
    }
    /**
     * Returns the begin of stream flag.
     * @return the begin of stream flag
     */
    public int bos() {
        return (headerBase[header + 5] & 0x02);
    }
    /**

     * Returns the end of stream flag.
     * @return the end of stream flag
     */
    public int eos() {
        return (headerBase[header + 5] & 0x04);
    }
    /**
     * Returns the granule(?) position.
     * @return the granule position
     */
    public long granulepos() {
        long foo = headerBase[header + 13] & 0xff;
        foo = (foo << 8) | (headerBase[header + 12] & 0xff);
        foo = (foo << 8) | (headerBase[header + 11] & 0xff);
        foo = (foo << 8) | (headerBase[header + 10] & 0xff);
        foo = (foo << 8) | (headerBase[header + 9] & 0xff);
        foo = (foo << 8) | (headerBase[header + 8] & 0xff);
        foo = (foo << 8) | (headerBase[header + 7] & 0xff);
        foo = (foo << 8) | (headerBase[header + 6] & 0xff);
        return (foo);
    }
    /**
     * Returns the serial number.
     * @return the serial number
     */
    public int serialno() {
        return (headerBase[header + 14] & 0xff)
                | ((headerBase[header + 15] & 0xff) << 8)
                | ((headerBase[header + 16] & 0xff) << 16)
                | ((headerBase[header + 17] & 0xff) << 24);
    }
    /**
     * Returns the page number.
     * @return the page number
     */
    int pageno() {
        return (headerBase[header + 18] & 0xff)
                | ((headerBase[header + 19] & 0xff) << 8)
                | ((headerBase[header + 20] & 0xff) << 16)
                | ((headerBase[header + 21] & 0xff) << 24);
    }
    /**
     * Calculates and sets the checksum.
     */
    void checksum() {
        int crcReg = 0;

        for (int i = 0; i < headerLen; i++) {
            crcReg = (crcReg << 8)
                    ^ CRC_LOOKUP[((crcReg >>> 24) & 0xff)
                            ^ (headerBase[header + i] & 0xff)];
        }
        for (int i = 0; i < bodyLen; i++) {
            crcReg = (crcReg << 8)
                    ^ CRC_LOOKUP[((crcReg >>> 24) & 0xff)
                            ^ (bodyBase[body + i] & 0xff)];
        }
        headerBase[header + 22] = (byte) crcReg;
        headerBase[header + 23] = (byte) (crcReg >>> 8);
        headerBase[header + 24] = (byte) (crcReg >>> 16);
        headerBase[header + 25] = (byte) (crcReg >>> 24);
    }
    /**
     * Returns a copy of this page.
     * @return the copied page no array is shared
     */
    public Page copy() {
        return copy(new Page());
    }
    /**
     * Copies the values of this page into the supplied Page.
     * @param p the target page to copy to
     * @return the p
     */
    public Page copy(Page p) {
        byte[] tmp = new byte[headerLen];
        System.arraycopy(headerBase, header, tmp, 0, headerLen);
        p.headerLen = headerLen;
        p.headerBase = tmp;
        p.header = 0;
        tmp = new byte[bodyLen];
        System.arraycopy(bodyBase, body, tmp, 0, bodyLen);
        p.bodyLen = bodyLen;
        p.bodyBase = tmp;
        p.body = 0;
        return p;
    }

}
