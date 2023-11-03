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
 * Stream state object.
 * Comments and style fixes
 * @author ymnk
 */
public class StreamState {
    /** Bytes from packet bodies. */
    byte[] bodyData;
    /** Storage elements allocated. */
    int bodyStorage;

    /** Elements stored; fill mark. */
    int bodyFill;

    /** Elements of fill returned. */
    private int bodyReturned;

    /** The values that will go to the segment table. */
    int[] lacingVals;

    /**
     * The pcm_pos values for headers. Not compact this way, but
     * it is simple coupled to the lacing fifo.
     */
    long[] granuleVals;

    /** Lacing storage. */
    int lacingStorage;
    /** Lacing fill. */
    int lacingFill;
    /** Lacing packet. */
    int lacingPacket;
    /** Lacing returned. */
    int lacingReturned;
    /** Working space for header encode. */
    byte[] header = new byte[282];

    /** Header fill. */
//    int headerFill;
    /**
     * Set when we have buffered the last packet in the logical
     * bitstream.
     */
    public int endOfStream;
    /**
     * Set after we've written the initial page of a logical
     * bitstream.
     */
    int beginOfStream;
    /** Serial number. */
    int serialno;
    /** Page number. */
    int pageno;
    /**
     * sequence number for decode; the framing knows where
     * there's a hole in the data, but we need coupling so that
     * the codec (which is in a seperate abstraction layer) also
     * knows about the gap.
     */

    long packetno;
    /** Granule pos. */
    long granulepos;
    /** Constructor. Initializes the store. */
    public StreamState() {
        init();
    }
    /**
     * Constructor. Sets the serial number.
     * @param serialno the serial number
     */
    StreamState(int serialno) {
        this();
        init(serialno);
    }
    /** Initializes the store. */
    void init() {
        bodyStorage = 16 * 1024;
        bodyData = new byte[bodyStorage];
        lacingStorage = 1024;
        lacingVals = new int[lacingStorage];
        granuleVals = new long[lacingStorage];
    }
    /**
     * Initializes the store with the given serial number.
     * @param serialno the serial number
     */
    public void init(int serialno) {
        if (bodyData == null) {
            init();
        } else {
            for (int i = 0; i < bodyData.length; i++) {
                bodyData[i] = 0;
            }
            for (int i = 0; i < lacingVals.length; i++) {
                lacingVals[i] = 0;
            }
            for (int i = 0; i < granuleVals.length; i++) {
                granuleVals[i] = 0;
            }
        }
        this.serialno = serialno;
    }
    /** Frees the internal buffers. */
    public void clear() {
        bodyData = null;
        lacingVals = null;
        granuleVals = null;
    }
    /** Frees the internal buffers. */
    void destroy() {
        clear();
    }
    /**
     * Expand body data.
     * @param needed the number of bytes needed
     */
    void bodyExpand(int needed) {
        if (bodyStorage <= bodyFill + needed) {
            bodyStorage += (needed + 1024);
            byte[] foo = new byte[bodyStorage];
            System.arraycopy(bodyData, 0, foo, 0, bodyData.length);
            bodyData = foo;
        }
    }
    /**
     * Expand the lacing data.
     * @param needed the number of bytes needed
     */
    void lacingExpand(int needed) {
        if (lacingStorage <= lacingFill + needed) {
            lacingStorage += (needed + 32);
            int[] foo = new int[lacingStorage];
            System.arraycopy(lacingVals, 0, foo, 0, lacingVals.length);
            lacingVals = foo;

            long[] bar = new long[lacingStorage];
            System.arraycopy(granuleVals, 0, bar, 0, granuleVals.length);
            granuleVals = bar;
        }
    }

    /**
     *  Submit data to the internal buffer of the framing engine .
     *  @param op the packet
     */
    public void packetin(Packet op) {
        int lacingVal = op.bytes / 255 + 1;

        if (bodyReturned != 0) {
            /*
             * advance packet data according to the body_returned pointer. We
             * had to keep it around to return a pointer into the buffer last
             * call
             */

            bodyFill -= bodyReturned;
            if (bodyFill != 0) {
                System.arraycopy(bodyData, bodyReturned, bodyData, 0,
                        bodyFill);
            }
            bodyReturned = 0;
        }

        /* make sure we have the buffer storage */
        bodyExpand(op.bytes);
        lacingExpand(lacingVal);

        /*
         * Copy in the submitted packet. Yes, the copy is a waste; this is the
         * liability of overly clean abstraction for the time being. It will
         * actually be fairly easy to eliminate the extra copy in the future
         */

        System.arraycopy(op.packetBase, op.packet, bodyData, bodyFill,
                op.bytes);
        bodyFill += op.bytes;

        /* Store lacing vals for this packet */
        int j;
        for (j = 0; j < lacingVal - 1; j++) {
            lacingVals[lacingFill + j] = 255;
            granuleVals[lacingFill + j] = granulepos;
        }
        lacingVals[lacingFill + j] = (op.bytes) % 255;
        granulepos = op.granulepos;
        granuleVals[lacingFill + j] = op.granulepos;

        /* flag the first segment as the beginning of the packet */
        lacingVals[lacingFill] |= 0x100;

        lacingFill += lacingVal;

        /* for the sake of completeness */
        packetno++;

        if (op.endOfStream != 0) {
            endOfStream = 1;
        }
    }
    /**
     * Sets the last packet.
     * @param op the packet
     * @return state
     */
    public int packetout(Packet op) {

        /*
         * The last part of decode. We have the stream broken into packet
         * segments. Now we need to group them into packets (or return the out
         * of sync markers)
         */

        int ptr = lacingReturned;

        if (lacingPacket <= ptr) {
            return (0);
        }

        if ((lacingVals[ptr] & 0x400) != 0) {
            /* We lost sync here; let the app know */
            lacingReturned++;

            /*
             * we need to tell the codec there's a gap; it might need to handle
             * previous packet dependencies.
             */
            packetno++;
            return (-1);
        }

        /* Gather the whole packet. We'll have no holes or a partial packet */
        int size = lacingVals[ptr] & 0xff;
        int bytes = 0;

        op.packetBase = bodyData;
        op.packet = bodyReturned;
        op.endOfStream = lacingVals[ptr] & 0x200; /* last packet of the stream? */
        op.beginOfStream = lacingVals[ptr] & 0x100; /* first packet of the stream? */
        bytes += size;

        while (size == 255) {
            int val = lacingVals[++ptr];
            size = val & 0xff;
            if ((val & 0x200) != 0) {
                op.endOfStream = 0x200;
            }
            bytes += size;
        }

        op.packetno = packetno;
        op.granulepos = granuleVals[ptr];
        op.bytes = bytes;

        bodyReturned += bytes;

        lacingReturned = ptr + 1;

        packetno++;
        return (1);
    }
    /**
     * Adds the incoming page to the stream state; we decompose the page
     * into packet segments here as well.
     * @param og the Page
     * @return zero
     */
    public int pagein(Page og) {
        byte[] headerBase = og.headerBase;
        int header = og.header;
        byte[] bodyBase = og.bodyBase;
        int body = og.body;
        int bodysize = og.bodyLen;
        int segptr = 0;

        int version = og.version();
        int continued = og.continued();
        int bos = og.bos();
        int eos = og.eos();
        long granulepos = og.granulepos();
        int aSerialno = og.serialno();
        int aPageno = og.pageno();
        int segments = headerBase[header + 26] & 0xff;

        // clean up 'returned data'
        int lr = lacingReturned;
        int br = bodyReturned;

        // body data
        if (br != 0) {
            bodyFill -= br;
            if (bodyFill != 0) {
                System.arraycopy(bodyData, br, bodyData, 0, bodyFill);
            }
            bodyReturned = 0;
        }

        if (lr != 0) {
            // segment table
            if ((lacingFill - lr) != 0) {
                System.arraycopy(lacingVals, lr, lacingVals, 0,
                        lacingFill - lr);
                System.arraycopy(granuleVals, lr, granuleVals, 0,
                        lacingFill - lr);
            }
            lacingFill -= lr;
            lacingPacket -= lr;
            lacingReturned = 0;
        }

        // check the serial number
        if (aSerialno != serialno) {
            return (-1);
        }
        if (version > 0) {
            return (-1);
        }

        lacingExpand(segments + 1);

        // are we in sequence?
        if (aPageno != pageno) {
            int i;

            // unroll previous partial packet (if any)
            for (i = lacingPacket; i < lacingFill; i++) {
                bodyFill -= lacingVals[i] & 0xff;
                // System.out.println("??");
            }
            lacingFill = lacingPacket;

            // make a note of dropped data in segment table
            if (pageno != -1) {
                lacingVals[lacingFill++] = 0x400;
                lacingPacket++;
            }

            // are we a 'continued packet' page? If so, we'll need to skip
            // some segments
            if (continued != 0) {
                bos = 0;
                for (; segptr < segments; segptr++) {
                    int val = (headerBase[header + 27 + segptr] & 0xff);
                    body += val;
                    bodysize -= val;
                    if (val < 255) {
                        segptr++;
                        break;
                    }
                }
            }
        }

        if (bodysize != 0) {
            bodyExpand(bodysize);
            System.arraycopy(bodyBase, body, bodyData, bodyFill, bodysize);
            bodyFill += bodysize;
        }

        int saved = -1;
        while (segptr < segments) {
            int val = (headerBase[header + 27 + segptr] & 0xff);
            lacingVals[lacingFill] = val;
            granuleVals[lacingFill] = -1;

            if (bos != 0) {
                lacingVals[lacingFill] |= 0x100;
                bos = 0;
            }

            if (val < 255) {
                saved = lacingFill;
            }

            lacingFill++;
            segptr++;

            if (val < 255) {
                lacingPacket = lacingFill;
            }
        }

        /* set the granulepos on the last pcmval of the last full packet */
        if (saved != -1) {
            granuleVals[saved] = granulepos;
        }

        if (eos != 0) {
            endOfStream = 1;
            if (lacingFill > 0) {
                lacingVals[lacingFill - 1] |= 0x200;
            }
        }

        pageno = aPageno + 1;
        return (0);
    }

    /**
     * This will flush remaining packets into a page (returning nonzero), even
     * if there is not enough data to trigger a flush normally (undersized
     * page). If there are no packets or partial packets to flush,
     * ogg_stream_flush returns 0. Note that ogg_stream_flush will try to flush
     * a normal sized page like ogg_stream_pageout; a call to ogg_stream_flush
     * does not gurantee that all packets have flushed. Only a return value of 0
     * from ogg_stream_flush indicates all packet data is flushed into pages.
     *

     * ogg_stream_page will flush the last page in a stream even if it's
     * undersized; you almost certainly want to use ogg_stream_pageout (andnot
     * ogg_stream_flush) unless you need to flush an undersized page in the
     * middle of a stream for some reason.
     * @param og the page
     * @return int
     */
    public int flush(Page og) {

        int i;
        int vals = 0;
        int maxvals = (lacingFill > 255 ? 255 : lacingFill);
        int bytes = 0;
        int acc = 0;
        long granulePos = granuleVals[0];

        if (maxvals == 0) {
            return (0);
        }

        /* construct a page */
        /* decide how many segments to include */

        /*
         * If this is the initial header case, the first page must only include
         * the initial header packet
         */
        if (beginOfStream == 0) { /* 'initial header page' case */
            granulePos = 0;
            for (vals = 0; vals < maxvals; vals++) {
                if ((lacingVals[vals] & 0x0ff) < 255) {
                    vals++;
                    break;
                }
            }
        } else {
            for (vals = 0; vals < maxvals; vals++) {
                if (acc > 4096) {
                    break;
                }
                acc += (lacingVals[vals] & 0x0ff);
                granulePos = granuleVals[vals];
            }
        }

        /* construct the header in temp storage */
        System.arraycopy("OggS".getBytes(), 0, header, 0, 4);

        /* stream structure version */
        header[4] = 0x00;

        /* continued packet flag? */
        header[5] = 0x00;
        if ((lacingVals[0] & 0x100) == 0) {
            header[5] |= 0x01;
        }
        /* first page flag? */
        if (beginOfStream == 0) {
            header[5] |= 0x02;
        }
        /* last page flag? */
        if (endOfStream != 0 && lacingFill == vals) {
            header[5] |= 0x04;
        }
        beginOfStream = 1;

        /* 64 bits of PCM position */
        for (i = 6; i < 14; i++) {
            header[i] = (byte) granulePos;
            granulePos >>>= 8;
        }

        /* 32 bits of stream serial number */
        int lSerialno = serialno;
        for (i = 14; i < 18; i++) {
            header[i] = (byte) lSerialno;
            lSerialno >>>= 8;
        }

        /*
         * 32 bits of page counter (we have both counter and page header because
         * this val can roll over)
         */
        if (pageno == -1) {
            /*
             * because someone called stream_reset; this would be a
             * strange thing to do in an encode stream, but it has
             * plausible uses
             */
            pageno = 0;

        }
        int lPageno = pageno++;
        for (i = 18; i < 22; i++) {
            header[i] = (byte) lPageno;
            lPageno >>>= 8;
        }

        /* zero for computation; filled in later */
        header[22] = 0;
        header[23] = 0;
        header[24] = 0;
        header[25] = 0;

        /* segment table */
        header[26] = (byte) vals;
        for (i = 0; i < vals; i++) {
            header[i + 27] = (byte) lacingVals[i];
            bytes += (header[i + 27] & 0xff);
        }

        /* set pointers in the ogg_page struct */
        og.headerBase = header;
        og.header = 0;
        og.headerLen = vals + 27;

//        headerFill = vals + 27;
        og.bodyBase = bodyData;
        og.body = bodyReturned;
        og.bodyLen = bytes;

        /* advance the lacing data and set the body_returned pointer */

        lacingFill -= vals;
        System.arraycopy(lacingVals, vals, lacingVals, 0, lacingFill * 4);
        System.arraycopy(granuleVals, vals, granuleVals, 0, lacingFill * 8);
        bodyReturned += bytes;

        /* calculate the checksum */

        og.checksum();

        /* done */
        return (1);
    }

    /**
     * This constructs pages from buffered packet segments. The pointers
     * returned are to static buffers; do not free. The returned buffers are
     * good only until the next call (using the same ogg_stream_state)
     * @param og Page
     * @return int
     */
    public int pageout(Page og) {
        if ((endOfStream != 0 && lacingFill != 0) /* 'were done, now flush' case */
                || bodyFill - bodyReturned > 4096 /* 'page nominal size' case */
                || lacingFill >= 255  /* 'segment table full' case */
                || (lacingFill != 0 && beginOfStream == 0)) { /* 'initial header page' case */
            return flush(og);
        }
        return 0;
    }
    /**
     * Returns the end of stream flag.
     * @return the end of stream flag
     */
    public int eof() {
        return endOfStream;
    }
    /**
     * Resets the internal buffers and pointers.
     */
    public void reset() {
        bodyFill = 0;
        bodyReturned = 0;

        lacingFill = 0;
        lacingPacket = 0;
        lacingReturned = 0;

//        headerFill = 0;

        endOfStream = 0;
        beginOfStream = 0;
        pageno = -1;
        packetno = 0;
        granulepos = 0;
    }
}
