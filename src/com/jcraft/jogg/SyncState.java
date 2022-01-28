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
 * DECODING PRIMITIVES: packet streaming layer

 * This has two layers to place more of the multi-serialno and paging
 * control in the application's hands.  First, we expose a data buffer
 * using ogg_decode_buffer().  The app either copies into the
 * buffer, or passes it directly to read(), etc.  We then call
 * ogg_decode_wrote() to tell how many bytes we just added.
 *
 * Pages are returned (pointers into the buffer in ogg_sync_state)
 * by ogg_decode_stream().  The page is then submitted to
 * ogg_decode_page() along with the appropriate
 * ogg_stream_state* (ie, matching serialno).  We then get raw
 * packets out calling ogg_stream_packet() with a
 * ogg_stream_state.  See the 'frame-prog.txt' docs for details and
 * example code.
 * Comments and style correction by karnokd
 * @author ymnk
 */
public class SyncState {
    /** The data. */
    public byte[] data;
    /** Storage. */
    int storage;
    /** Fill. */
    int fill;
    /** Returned. */
    int returned;
    /** Unsynced. */
    int unsynced;
    /** Header bytes. */
    int headerbytes;
    /** Body bytes. */
    int bodybytes;
    /**

     * Clear internal data.
     */
    public void clear() {
        data = null;
    }
    /**
     * Renew the buffer with the given size.
     * @param size the size
     * @return the fill value
     */
    public int buffer(int size) {
        // first, clear out any space that has been previously returned
        if (returned != 0) {
            fill -= returned;
            if (fill > 0) {
                System.arraycopy(data, returned, data, 0, fill);
            }
            returned = 0;
        }

        if (size > storage - fill) {
            // We need to extend the internal buffer
            int newsize = size + fill + 4096; // an extra page to be nice
            if (data != null) {
                byte[] foo = new byte[newsize];
                System.arraycopy(data, 0, foo, 0, data.length);
                data = foo;
            } else {
                data = new byte[newsize];
            }
            storage = newsize;
        }

        return (fill);
    }
    /**

     * Returns -1 f the number of bytes added to the fill value is above the store size.
     * @param bytes the bytes to test
     * @return -1 or 0
     */
    public int wrote(int bytes) {
        if (fill + bytes > storage) {
            return (-1);
        }
        fill += bytes;
        return (0);
    }
    /** Page seek object. */
    private final Page pageseek = new Page();
    /** Checksum. */
    private final byte[] chksum = new byte[4];
    /**
     * Sync the stream. This is meant to be useful for finding page
     * boundaries.
     * @param og the page
     * @return
     * -n) skipped n bytes<br>
     * 0) page not ready; more data (no bytes skipped)<br>
     * n) page synced at current location; page length n bytes
     */
    public int pageseek(Page og) {
        int page = returned;
        int next;
        int bytes = fill - returned;

        if (headerbytes == 0) {
            int lHeaderbytes, i;
            if (bytes < 27) {
                return (0); // not enough for a header
            }

            /* verify capture pattern */
            if (data[page] != 'O' || data[page + 1] != 'g'
                    || data[page + 2] != 'g' || data[page + 3] != 'S') {
                headerbytes = 0;
                bodybytes = 0;

                // search for possible capture
                next = 0;
                for (int ii = 0; ii < bytes - 1; ii++) {
                    if (data[page + 1 + ii] == 'O') {
                        next = page + 1 + ii;
                        break;
                    }
                }
                // next=memchr(page+1,'O',bytes-1);
                if (next == 0) {
                    next = fill;
                }

                returned = next;
                return (-(next - page));
            }
            lHeaderbytes = (data[page + 26] & 0xff) + 27;
            if (bytes < lHeaderbytes) {
                return (0); // not enough for header + seg table
            }

            // count up body length in the segment table

            for (i = 0; i < (data[page + 26] & 0xff); i++) {
                bodybytes += (data[page + 27 + i] & 0xff);
            }
            headerbytes = lHeaderbytes;
        }

        if (bodybytes + headerbytes > bytes) {
            return (0);
        }

        // The whole test page is buffered. Verify the checksum
        synchronized (chksum) {
            // Grab the checksum bytes, set the header field to zero

            System.arraycopy(data, page + 22, chksum, 0, 4);
            data[page + 22] = 0;
            data[page + 23] = 0;
            data[page + 24] = 0;
            data[page + 25] = 0;

            // set up a temp page struct and recompute the checksum
            Page log = pageseek;
            log.headerBase = data;
            log.header = page;
            log.headerLen = headerbytes;

            log.bodyBase = data;
            log.body = page + headerbytes;
            log.bodyLen = bodybytes;
            log.checksum();

            // Compare
            if (chksum[0] != data[page + 22] || chksum[1] != data[page + 23]
                    || chksum[2] != data[page + 24]
                    || chksum[3] != data[page + 25]) {
                // D'oh. Mismatch! Corrupt page (or miscapture and not a page at
                // all)
                // replace the computed checksum with the one actually read in
                System.arraycopy(chksum, 0, data, page + 22, 4);
                // Bad checksum. Lose sync */

                headerbytes = 0;
                bodybytes = 0;
                // search for possible capture
                next = 0;
                for (int ii = 0; ii < bytes - 1; ii++) {
                    if (data[page + 1 + ii] == 'O') {
                        next = page + 1 + ii;
                        break;
                    }
                }
                // next=memchr(page+1,'O',bytes-1);
                if (next == 0) {
                    next = fill;
                }
                returned = next;
                return (-(next - page));
            }
        }

        // yes, have a whole page all ready to go
        page = returned;

        if (og != null) {
            og.headerBase = data;
            og.header = page;
            og.headerLen = headerbytes;
            og.bodyBase = data;
            og.body = page + headerbytes;
            og.bodyLen = bodybytes;
        }

        unsynced = 0;
        bytes = headerbytes + bodybytes;
        returned += bytes;
        headerbytes = 0;
        bodybytes = 0;
        return (bytes);
    }
    /**
     * Sync the stream and get a page. Keep trying until we find a page.
     * Supress 'sync errors' after reporting the first.
     *
     * Returns pointers into buffered data; invalidated by next call to
     * _stream, _clear, _init, or _buffer
     *

     * @param og the page
     * @return
     * -1) recapture (hole in data)<br>
     * 0) need more data<br>
     * 1) page returned
     */

    public int pageout(Page og) {
        // all we need to do is verify a page at the head of the stream
        // buffer. If it doesn't verify, we look for the next potential
        // frame

        while (true) {
            int ret = pageseek(og);
            if (ret > 0) {
                // have a page
                return (1);
            }
            if (ret == 0) {
                // need more data
                return (0);
            }

            // head did not start a synced page... skipped some bytes
            if (unsynced == 0) {
                unsynced = 1;
                return (-1);
            }
            // loop. keep looking
        }
    }
    /** Clear things to an initial state. Good to call, eg, before seeking. */
    public void reset() {
        fill = 0;
        returned = 0;
        unsynced = 0;
        headerbytes = 0;
        bodybytes = 0;
    }
    /** Initialize. */
    public void init() {
    }
    /**
     * Returns the data offset.
     * @return the data offset
     */
    public int getDataOffset() {
        return returned;
    }
    /**

     * Returns the buffer offset.
     * @return the buffer offset
     */
    public int getBufferOffset() {
        return fill;
    }
}
