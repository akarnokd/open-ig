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

package com.jcraft.jorbis;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
/**
 * Class to manage a Vorbis file.
 * Comments and style correction by karnokd.
 * @author ymnk
 */
public class VorbisFile {
    /** Chunk size. */
    static final int CHUNKSIZE = 8500;
    /** Seek set. */
    static final int SEEK_SET = 0;
    /** Seek relative to current. */
    static final int SEEK_CUR = 1;
    /** Seek relative to end. */
    static final int SEEK_END = 2;
    /** OV false. */
    static final int OV_FALSE = -1;
    /** End of file. */
    static final int OV_EOF = -2;
    /** Hole. */
    static final int OV_HOLE = -3;
    /** OV read error. */
    static final int OV_EREAD = -128;
    /** OV fault. */
    static final int OV_EFAULT = -129;
    /** OV impl error. */
    static final int OV_EIMPL = -130;
    /** OV inval. */
    static final int OV_EINVAL = -131;
    /** OV not vorbis error. */
    static final int OV_ENOTVORBIS = -132;
    /** OV bad header. */
    static final int OV_EBADHEADER = -133;
    /** OV version error. */
    static final int OV_EVERSION = -134;
    /** OV not audio error. */
    static final int OV_ENOTAUDIO = -135;
    /** OV bad packet error. */
    static final int OV_EBADPACKET = -136;
    /** OV bad link error. */
    static final int OV_EBADLINK = -137;
    /** OV no seek error. */
    static final int OV_ENOSEEK = -138;
    /** The data source. */
    InputStream datasource;
    /** Data source is seekable. */
    boolean seekable = false;
    /** Current offset. */
    long offset;
    /** End. */
//    long end;
    /** Sync state. */
    SyncState oy = new SyncState();
    /** Links. */
    int links;
    /** Offsets. */
    long[] offsets;
    /** Data offsets. */
    long[] dataoffsets;
    /** Serial numbers. */
    int[] serialnos;
    /** PCM lengths. */
    long[] pcmlengths;
    /** Vorbis info array. */
    Info[] vi;
    /** Vorbis comments array. */
    Comment[] vc;
    /** PCM offset. */
    long pcmOffset;
    /** Decoding working state local storage. */
    boolean decodeReady = false;
    /** Current serial number. */
    int currentSerialno;
    /** Current link. */
    int currentLink;
    /** Bit track. */
    float bittrack;
    /** Sample track. */
    float samptrack;
    /** Take physical pages, weld into a logical stream of packets. */
    StreamState os = new StreamState();
    /** Central working state for the packet->PCM decoder. */
    DspState vd = new DspState();
    /** Local working space for packet->PCM decode. */
    Block vb = new Block(vd);
    /**
     * Constructor. Opens the file
     * @param file the file to open
     * @throws JOrbisException if there is something wrong
     */
    public VorbisFile(String file) throws JOrbisException {
        super();
        try (InputStream is = new SeekableInputStream(file)) {
            int ret = open(is, null, 0);
            if (ret == -1) {
                throw new JOrbisException("VorbisFile: open return -1");
            }
        } catch (IOException e) {
            throw new JOrbisException("VorbisFile: " + e.toString(), e);
        }
    }
    /**
     * Constructor with input stream.
     * @param is the input stream to use
     * @param initial the initial bytes
     * @param ibytes the number of initial bytes
     * @throws JOrbisException if any error occurs
     */
    public VorbisFile(InputStream is, byte[] initial, int ibytes)
            throws JOrbisException {
        super();
        int ret = open(is, initial, ibytes);
        if (ret == -1) {
            throw new JOrbisException("VorbisFile: Open failed");
        }
    }
    /**
     * Get the data.
     * @return the number of bytes read
     */
    private int getData() {
        int index = oy.buffer(CHUNKSIZE);
        byte[] buffer = oy.data;
        int bytes = 0;
        try {
            bytes = datasource.read(buffer, index, CHUNKSIZE);
        } catch (Exception e) {
            return OV_EREAD;
        }
        oy.wrote(bytes);
        if (bytes == -1) {
            bytes = 0;
        }
        return bytes;
    }
    /**
     * Seek helper function.
     * @param offst the offset to seek
     */
    private void seekHelper(long offst) {
        fseek(datasource, offst, SEEK_SET);
        this.offset = offst;
        oy.reset();
    }
    /**
     * Get next page.
     * @param page the page
     * @param boundary the boundary
     * @return the offset or error code OV_*
     */
    private int getNextPage(Page page, long boundary) {
        if (boundary > 0) {
            boundary += offset;
        }
        while (true) {
            int more;
            if (boundary > 0 && offset >= boundary) {
                return OV_FALSE;
            }
            more = oy.pageseek(page);
            if (more < 0) {
                offset -= more;
            } else {
                if (more == 0) {
                    if (boundary == 0) {
                        return OV_FALSE;
                    }
                    int ret = getData();
                    if (ret == 0) {
                        return OV_EOF;
                    }
                    if (ret < 0) {
                        return OV_EREAD;
                    }
                } else {
                    int ret = (int) offset; // !!!
                    offset += more;
                    return ret;
                }
            }
        }
    }
    /**
     * Get next page.
     * @param page the page
     * @return offset or OV_* error code
     * @throws JOrbisException if an error occurs
     */
    private int getPrevPage(Page page) throws JOrbisException {
        long begin = offset; // !!!
        int ret;
        int offst = -1;
        while (offst == -1) {
            begin -= CHUNKSIZE;
            if (begin < 0) {
                begin = 0;
            }
            seekHelper(begin);
            while (offset < begin + CHUNKSIZE) {
                ret = getNextPage(page, begin + CHUNKSIZE - offset);
                if (ret == OV_EREAD) {
                    return OV_EREAD;
                }
                if (ret < 0) {
                    if (offst == -1) {
                        throw new JOrbisException();
                    }
                    break;
                }
                offst = ret;
            }
        }
        seekHelper(offst); // !!!
        ret = getNextPage(page, CHUNKSIZE);
        if (ret < 0) {
            return OV_EFAULT;
        }
        return offst;
    }
    /**
     * Bisect forward serial number.
     * @param begin beginning
     * @param searched searched
     * @param end end
     * @param currentno current number
     * @param m member
     * @return success flag OV_*
     */
    int bisectForwardSerialno(long begin, long searched, long end,
            int currentno, int m) {
        long endsearched = end;
        long next = end;
        Page page = new Page();
        int ret;

        while (searched < endsearched) {
            long bisect;
            if (endsearched - searched < CHUNKSIZE) {
                bisect = searched;
            } else {
                bisect = (searched + endsearched) / 2;
            }

            seekHelper(bisect);
            ret = getNextPage(page, -1);
            if (ret == OV_EREAD) {
                return OV_EREAD;
            }
            if (ret < 0 || page.serialno() != currentno) {
                endsearched = bisect;
                if (ret >= 0) {
                    next = ret;
                }
            } else {
                searched = ret + page.headerLen + page.bodyLen;
            }
        }
        seekHelper(next);
        ret = getNextPage(page, -1);
        if (ret == OV_EREAD) {
            return OV_EREAD;
        }

        if (searched >= end || ret == -1) {
            links = m + 1;
            offsets = new long[m + 2];
            offsets[m + 1] = searched;
        } else {
            ret = bisectForwardSerialno(next, offset, end, page.serialno(),
                    m + 1);
            if (ret == OV_EREAD) {
                return OV_EREAD;
            }
        }
        offsets[m] = begin;
        return 0;
    }
    /**
     * Uses the local ogg_stream storage in vf; this is important for
     * non-streaming input sources.
     * @param vi the info block
     * @param vc comment block
     * @param serialno serial numbers
     * @param ogPtr ogg pointer page
     * @return success codes OV_*
     */
    int fetchHeaders(Info vi, Comment vc, int[] serialno, Page ogPtr) {
        Page og = new Page();
        Packet op = new Packet();
        int ret;

        if (ogPtr == null) {
            ret = getNextPage(og, CHUNKSIZE);
            if (ret == OV_EREAD) {
                return OV_EREAD;
            }
            if (ret < 0) {
                return OV_ENOTVORBIS;
            }
            ogPtr = og;
        }

        if (serialno != null) {
            serialno[0] = ogPtr.serialno();
        }

        os.init(ogPtr.serialno());

        // extract the initial header from the first page and verify that the
        // Ogg bitstream is in fact Vorbis data

        vi.init();
        vc.init();

        int i = 0;
        while (i < 3) {
            os.pagein(ogPtr);
            while (i < 3) {
                int result = os.packetout(op);
                if (result == 0) {
                    break;
                }
                if (result == -1) {
                    vi.clear();
                    vc.clear();
                    os.clear();
                    return -1;
                }
                if (vi.synthesisHeaderin(vc, op) != 0) {
                    vi.clear();
                    vc.clear();
                    os.clear();
                    return -1;
                }
                i++;
            }
            if (i < 3) {
                if (getNextPage(ogPtr, 1) < 0) {
                    vi.clear();
                    vc.clear();
                    os.clear();
                    return -1;
                }
            }
        }
        return 0;
    }
    /**
     * Last step of the OggVorbis_File initialization; get all the
     * vorbis_info structs and PCM positions. Only called by the seekable
     * initialization (local stream storage is hacked slightly; pay
     * attention to how that's done)
     * @param firstInfo first info
     * @param firstComment first comment
     * @param dataoffset data offset
     * @throws JOrbisException if an error occurs
     */
    void prefetchAllHeaders(Info firstInfo, Comment firstComment, int dataoffset)
            throws JOrbisException {
        Page og = new Page();
        int ret;

        vi = new Info[links];
        vc = new Comment[links];
        dataoffsets = new long[links];
        pcmlengths = new long[links];
        serialnos = new int[links];

        for (int i = 0; i < links; i++) {
            if (firstInfo != null && firstComment != null && i == 0) {
                // we already grabbed the initial header earlier. This just
                // saves the waste of grabbing it again
                vi[i] = firstInfo;
                vc[i] = firstComment;
                dataoffsets[i] = dataoffset;
            } else {
                // seek to the location of the initial header
                seekHelper(offsets[i]); // !!!
                vi[i] = new Info();
                vc[i] = new Comment();
                if (fetchHeaders(vi[i], vc[i], null, null) == -1) {
                    dataoffsets[i] = -1;
                } else {
                    dataoffsets[i] = offset;
                    os.clear();
                }
            }

            // get the serial number and PCM length of this link. To do this,
            // get the last page of the stream
            long end = offsets[i + 1]; // !!!
            seekHelper(end);

            while (true) {
                ret = getPrevPage(og);
                if (ret == -1) {
                    // this should not be possible
                    vi[i].clear();
                    vc[i].clear();
                    break;
                }
                if (og.granulepos() != -1) {
                    serialnos[i] = og.serialno();
                    pcmlengths[i] = og.granulepos();
                    break;
                }
            }
        }
    }
    /**
     * Makes the decoder ready only once.
     */
    private void makeDecodeReady() {
        if (decodeReady) {
            throw new RuntimeException("Reinitialized decoder");
        }

        vd.synthesisInit(vi[0]);
        vb.init(vd);
        decodeReady = true;
    }
    /**
     * Open seekable.
     * @return success value
     * @throws JOrbisException if an error occurs
     */
    int openSeekable() throws JOrbisException {
        Info initialInfo = new Info();
        Comment initialComment = new Comment();
        int serialno;
        long end;
        int ret;
        int dataoffset;
        Page og = new Page();
        // is this even vorbis...?
        int[] foo = new int[1];
        ret = fetchHeaders(initialInfo, initialComment, foo, null);
        serialno = foo[0];
        dataoffset = (int) offset; // !!
        os.clear();
        if (ret == -1) {
            return (-1);
        }
        if (ret < 0) {
            return (ret);
        }
        // we can seek, so set out learning all about this file
        seekable = true;
        fseek(datasource, 0, SEEK_END);
        offset = ftell(datasource);
        end = offset;
        // We get the offset for the last page of the physical bitstream.
        // Most OggVorbis files will contain a single logical bitstream
        end = getPrevPage(og);
        // moer than one logical bitstream?
        if (og.serialno() != serialno) {
            // Chained bitstream. Bisect-search each logical bitstream
            // section. Do so based on serial number only
            if (bisectForwardSerialno(0, 0, end + 1, serialno, 0) < 0) {
                clear();
                return OV_EREAD;
            }
        } else {
            // Only one logical bitstream
            if (bisectForwardSerialno(0, end, end + 1, serialno, 0) < 0) {
                clear();
                return OV_EREAD;
            }
        }
        prefetchAllHeaders(initialInfo, initialComment, dataoffset);
        return 0;
    }
    /**
     * Open non seekable.
     * @return success flag
     */
    int openNonseekable() {
        // we cannot seek. Set up a 'single' (current) logical bitstream entry
        links = 1;
        vi = new Info[links];
        vi[0] = new Info(); // ??
        vc = new Comment[links];
        vc[0] = new Comment(); // ?? bug?

        // Try to fetch the headers, maintaining all the storage
        int[] foo = new int[1];
        if (fetchHeaders(vi[0], vc[0], foo, null) == -1) {
            return (-1);
        }
        currentSerialno = foo[0];
        makeDecodeReady();
        return 0;
    }
    /** Clear out the current logical bitstream decoder. */
    void decodeClear() {
        os.clear();
        vd.clear();
        vb.clear();
        decodeReady = false;
        bittrack = 0.f;
        samptrack = 0.f;
    }
    /**
     * Fetch and process a packet. Handles the case where we're at a
     * bitstream boundary and dumps the decoding machine. If the decoding
     * machine is unloaded, it loads it. It also keeps pcm_offset up to
     * date (seek and read both use this. seek uses a special hack with
     * readp).
     * @param readp read pointer
     * @return
     * -1) hole in the data (lost packet)<br>
     * 0) need more date (only if readp==0)/eof<br>
     * 1) got a packet
     */
    int processPacket(int readp) {
        Page og = new Page();

        // handle one packet. Try to fetch it from current stream state
        // extract packets from page
        while (true) {
            // process a packet if we can. If the machine isn't loaded,
            // neither is a page
            if (decodeReady) {
                Packet op = new Packet();
                int result = os.packetout(op);
                long granulepos;
                // if(result==-1)return(-1); // hole in the data. For now,
                // swallow
                // and go. We'll need to add a real
                // error code in a bit.
                if (result > 0) {
                    // got a packet. process it
                    granulepos = op.granulepos;
                    if (vb.synthesis(op) == 0) { // lazy check for lazy
                        // header handling. The
                        // header packets aren't
                        // audio, so if/when we
                        // submit them,
                        // vorbis_synthesis will
                        // reject them
                        // suck in the synthesis data and track bitrate
                        int oldsamples = vd.synthesisPcmout(null, null);
                        vd.synthesisBlockin(vb);
                        samptrack += vd.synthesisPcmout(null, null)
                                - oldsamples;
                        bittrack += op.bytes * 8;

                        // update the pcm offset.
                        if (granulepos != -1 && op.endOfStream == 0) {
                            int link = (seekable ? currentLink : 0);
                            int samples;
                            // this packet has a pcm_offset on it (the last
                            // packet
                            // completed on a page carries the offset) After
                            // processing
                            // (above), we know the pcm position of the *last*
                            // sample
                            // ready to be returned. Find the offset of the
                            // *first*
                            //

                            // As an aside, this trick is inaccurate if we begin
                            // reading anew right at the last page; the
                            // end-of-stream
                            // granulepos declares the last frame in the stream,
                            // and the
                            // last packet of the last page may be a partial
                            // frame.
                            // So, we need a previous granulepos from an
                            // in-sequence page
                            // to have a reference point. Thus the !op.e_o_s
                            // clause above

                            samples = vd.synthesisPcmout(null, null);
                            granulepos -= samples;
                            for (int i = 0; i < link; i++) {
                                granulepos += pcmlengths[i];
                            }
                            pcmOffset = granulepos;
                        }
                        return (1);
                    }
                }
            }

            if (readp == 0) {
                return (0);
            }
            if (getNextPage(og, -1) < 0) {
                return (0); // eof. leave unitialized
            }
            // bitrate tracking; add the header's bytes here, the body bytes
            // are done by packet above
            bittrack += og.headerLen * 8;

            // has our decoding just traversed a bitstream boundary?
            if (decodeReady) {
                if (currentSerialno != og.serialno()) {
                    decodeClear();
                }
            }

            // Do we need to load a new machine before submitting the page?
            // This is different in the seekable and non-seekable cases.
            //

            // In the seekable case, we already have all the header
            // information loaded and cached; we just initialize the machine
            // with it and continue on our merry way.
            //

            // In the non-seekable (streaming) case, we'll only be at a
            // boundary if we just left the previous logical bitstream and
            // we're now nominally at the header of the next bitstream

            if (!decodeReady) {
                int i;
                if (seekable) {
                    currentSerialno = og.serialno();

                    // match the serialno to bitstream section. We use this
                    // rather than
                    // offset positions to avoid problems near logical bitstream
                    // boundaries
                    for (i = 0; i < links; i++) {
                        if (serialnos[i] == currentSerialno) {
                            break;
                        }
                    }
                    if (i == links) {
                        // sign of a bogus stream. error out,
                        // leave machine uninitialized
                        return (-1);

                    }
                    currentLink = i;

                    os.init(currentSerialno);
                    os.reset();

                } else {
                    // we're streaming
                    // fetch the three header packets, build the info struct
                    int[] foo = new int[1];
                    int ret = fetchHeaders(vi[0], vc[0], foo, og);
                    currentSerialno = foo[0];
                    if (ret != 0) {
                        return ret;
                    }
                    currentLink++;
                    i = 0;
                }
                makeDecodeReady();
            }
            os.pagein(og);
        }
    }
    /**
     * The helpers are over; it's all toplevel interface from here on out
     * clear out the OggVorbis_File struct.
     */
    void clear() {
        vb.clear();
        vd.clear();
        os.clear();

        if (vi != null && links != 0) {
            for (int i = 0; i < links; i++) {
                vi[i].clear();
                vc[i].clear();
            }
            vi = null;
            vc = null;
        }
        dataoffsets = null;
        pcmlengths = null;
        serialnos = null;
        offsets = null;
        oy.clear();
    }
    /**
     * Helper method to seek on an input stream.
     * @param fis the input stream
     * @param off the offset
     * @param whence the direction SEEK_*
     * @return success flag
     */
    static int fseek(InputStream fis, long off, int whence) {
        if (fis instanceof SeekableInputStream) {
            SeekableInputStream sis = (SeekableInputStream) fis;
            try {
                if (whence == SEEK_SET) {
                    sis.seek(off);
                } else if (whence == SEEK_END) {
                    sis.seek(sis.getLength() - off);
                }
            } catch (Exception e) {
                // ignored
            }
            return 0;
        }
        try {
            if (whence == 0) {
                fis.reset();
            }
            long count = off;
            while (count > 0) {
                long c = fis.skip(count);
                if (c < 0) {
                    throw new EOFException();
                }
                count -= c;
            }
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }
    /**
     * Return the current stream index.
     * @param fis the input stream
     * @return the current position
     */
    static long ftell(InputStream fis) {
        try {
            if (fis instanceof SeekableInputStream) {
                SeekableInputStream sis = (SeekableInputStream) fis;
                return (sis.tell());
            }
        } catch (Exception e) {
        }
        return 0;
    }
    // inspects the OggVorbis file and finds/documents all the logical
    // bitstreams contained in it. Tries to be tolerant of logical
    // bitstream sections that are truncated/woogie.
    //
    // return: -1) error
    // 0) OK
    /**
     * Inspects the OggVorbis file and finds/documents all the logical
     * bitstreams contained in it. Tries to be tolerant of logical
     * bitstream sections that are truncated/woogie.
     * @param is input stream
     * @param initial initial array
     * @param ibytes initial bytes
     * @return success flag
     * @throws JOrbisException if an error occurs
     */
    int open(InputStream is, byte[] initial, int ibytes) throws JOrbisException {
        return openCallbacks(is, initial, ibytes);
    }
    /**
     * Open callbacks.
     * @param is input stream
     * @param initial initial bytes
     * @param ibytes initial count
     * @return success flag
     * @throws JOrbisException if an error occurs
     */
    int openCallbacks(InputStream is, byte[] initial, int ibytes) throws JOrbisException {
        int ret;
        datasource = is;

        oy.init();

        // perhaps some data was previously read into a buffer for testing
        // against other stream types. Allow initialization from this
        // previously read data (as we may be reading from a non-seekable
        // stream)
        if (initial != null) {
            int index = oy.buffer(ibytes);
            System.arraycopy(initial, 0, oy.data, index, ibytes);
            oy.wrote(ibytes);
        }
        // can we seek? Stevens suggests the seek test was portable
        if (is instanceof SeekableInputStream) {
            ret = openSeekable();
        } else {
            ret = openNonseekable();
        }
        if (ret != 0) {
            datasource = null;
            clear();
        }
        return ret;
    }

    /**
     * How many logical bitstreams in this physical bitstream?
     * @return how many logical bitstreams in this physical bitstream
     */
    public int streams() {
        return links;
    }
    /**

     * Is the FILE * associated with vf seekable?

     * @return is the FILE * associated with vf seekable
     */
    public boolean seekable() {
        return seekable;
    }

    /**
     * Returns the bitrate for a given logical bitstream or the entire
     * physical bitstream. If the file is open for random access, it will
     * find the *actual* average bitrate. If the file is streaming, it
     * returns the nominal bitrate (if set) else the average of the
     * upper/lower bounds (if set) else -1 (unset).
     * If you want the actual bitrate field settings, get them from the
     * vorbis_info structs
     * @param i logical stream index
     * @return success flag
     */
    public int bitrate(int i) {
        if (i >= links) {
            return (-1);
        }
        if (!seekable && i != 0) {
            return (bitrate(0));
        }
        if (i < 0) {
            long bits = 0;
            for (int j = 0; j < links; j++) {
                bits += (offsets[j + 1] - dataoffsets[j]) * 8;
            }
            return ((int) Math.rint(bits / timeTotal(-1)));
        }
        if (seekable) {
            // return the actual bitrate
            return ((int) Math.rint((offsets[i + 1] - dataoffsets[i]) * 8
                    / timeTotal(i)));
        }
        // return nominal if set
        if (vi[i].bitrateNominal > 0) {
            return vi[i].bitrateNominal;
        }
        if (vi[i].bitrateUpper > 0) {
            if (vi[i].bitrateLower > 0) {
                return (vi[i].bitrateUpper + vi[i].bitrateLower) / 2;
            }
            return vi[i].bitrateUpper;
        }
        return (-1);
    }
    /**
     * Returns the actual bitrate since last call. returns -1 if no
     * additional data to offer since last call (or at beginning of stream)
     * @return actual bitrate
     */
    // Returns the actual bitrate since last call. returns -1 if no
    // additional data to offer since last call (or at beginning of stream)
    public int bitrateInstant() {
        int lLink = (seekable ? currentLink : 0);
        if (samptrack == 0) {
            return (-1);
        }
        int ret = (int) (bittrack / samptrack * vi[lLink].rate + .5);
        bittrack = 0.f;
        samptrack = 0.f;
        return (ret);
    }
    /**
     * Returns the serial number of the given stream index.
     * @param i the stream index
     * @return the serial number or error flag
     */
    public int serialnumber(int i) {
        if (i >= links) {
            return (-1);
        }
        if (!seekable && i >= 0) {
            return (serialnumber(-1));
        }
        if (i < 0) {
            return (currentSerialno);
        }
        return (serialnos[i]);
    }
    /**
     * Returns total raw (compressed) length of content if i==-1
     * raw (compressed) length of that logical bitstream for i==0 to n
     * -1 if the stream is not seekable (we can't know the length).
     * @param i stream index
     * @return the raw length
     */
    public long rawTotal(int i) {
        if (!seekable || i >= links) {
            return (-1);
        }
        if (i < 0) {
            long acc = 0; // bug?
            for (int j = 0; j < links; j++) {
                acc += rawTotal(j);
            }
            return (acc);
        }
        return (offsets[i + 1] - offsets[i]);
    }
    /**
     * Returns the total PCM length (samples) of content if i==-1
     * PCM length (samples) of that logical bitstream for i==0 to n
     * -1 if the stream is not seekable (we can't know the length).
     * @param i logical stream index
     * @return the length or error code
     */
    public long pcmTotal(int i) {
        if (!seekable || i >= links) {
            return (-1);
        }
        if (i < 0) {
            long acc = 0;
            for (int j = 0; j < links; j++) {
                acc += pcmTotal(j);
            }
            return (acc);
        }
        return (pcmlengths[i]);
    }
    /**
     * Returns the total seconds of content if i==-1
     * seconds in that logical bitstream for i==0 to n
     * -1 if the stream is not seekable (we can't know the length).
     * @param i the stream index
     * @return the length or error code
     */
    public float timeTotal(int i) {
        if (!seekable || i >= links) {
            return (-1);
        }
        if (i < 0) {
            float acc = 0;
            for (int j = 0; j < links; j++) {
                acc += timeTotal(j);
            }
            return (acc);
        }
        return ((float) (pcmlengths[i]) / vi[i].rate);
    }
    /**
     * Seek to an offset relative to the *compressed* data. This also
     * immediately sucks in and decodes pages to update the PCM cursor. It
     * will cross a logical bitstream boundary, but only if it can't get
     * any packets out of the tail of the bitstream we seek to (so no
     * surprises).
     * @param pos the position
     * @return zero on success, nonzero on failure
     */
    public int rawSeek(int pos) {
        if (!seekable) {
            return (-1); // don't dump machine if we can't seek
        }
        if (pos < 0 || pos > offsets[links]) {
            // goto seek_error;
            pcmOffset = -1;
            decodeClear();
            return -1;
        }

        // clear out decoding machine state
        pcmOffset = -1;
        decodeClear();

        // seek
        seekHelper(pos);

        // we need to make sure the pcm_offset is set. We use the
        // _fetch_packet helper to process one packet with readp set, then
        // call it until it returns '0' with readp not set (the last packet
        // from a page has the 'granulepos' field set, and that's how the
        // helper updates the offset

        switch (processPacket(1)) {
        case 0:
            // oh, eof. There are no packets remaining. Set the pcm offset to
            // the end of file
            pcmOffset = pcmTotal(-1);
            return (0);
        case -1:
            // error! missing data or invalid bitstream structure
            // goto seek_error;
            pcmOffset = -1;
            decodeClear();
            return -1;
        default:
            // all OK
            break;
        }
        while (true) {
            switch (processPacket(0)) {
            case 0:
                // the offset is set. If it's a bogus bitstream with no offset
                // information, it's not but that's not our fault. We still run
                // gracefully, we're just missing the offset
                return (0);
            case -1:
                // error! missing data or invalid bitstream structure
                // goto seek_error;
                pcmOffset = -1;
                decodeClear();
                return -1;
            default:
                // continue processing packets
                break;
            }
        }

        // seek_error:
        // dump the machine so we're in a known state
        // pcm_offset=-1;
        // decode_clear();
        // return -1;
    }
    /**
     * Seek to a sample offset relative to the decompressed pcm stream.
     * @param pos position
     * @return zero on success, nonzero on failure
     */
    public int pcmSeek(long pos) {
        int link = -1;
        long total = pcmTotal(-1);

        if (!seekable) {
            return (-1); // don't dump machine if we can't seek
        }
        if (pos < 0 || pos > total) {
            // goto seek_error;
            pcmOffset = -1;
            decodeClear();
            return -1;
        }

        // which bitstream section does this pcm offset occur in?
        for (link = links - 1; link >= 0; link--) {
            total -= pcmlengths[link];
            if (pos >= total) {
                break;
            }
        }

        // search within the logical bitstream for the page with the highest
        // pcm_pos preceeding (or equal to) pos. There is a danger here;
        // missing pages or incorrect frame number information in the
        // bitstream could make our task impossible. Account for that (it
        // would be an error condition)
        long target = pos - total;
        long end = offsets[link + 1];
        long begin = offsets[link];
        int best = (int) begin;

        Page og = new Page();
        while (begin < end) {
            long bisect;
            int ret;

            if (end - begin < CHUNKSIZE) {
                bisect = begin;
            } else {
                bisect = (end + begin) / 2;
            }

            seekHelper(bisect);
            ret = getNextPage(og, end - bisect);

            if (ret == -1) {
                end = bisect;
            } else {
                long granulepos = og.granulepos();
                if (granulepos < target) {
                    best = ret; // raw offset of packet with granulepos
                    begin = offset; // raw offset of next packet
                } else {
                    end = bisect;
                }
            }
        }
        // found our page. seek to it (call raw_seek).
        if (rawSeek(best) != 0) {
            // goto seek_error;
            pcmOffset = -1;
            decodeClear();
            return -1;
        }

        // verify result
        if (pcmOffset >= pos) {
            // goto seek_error;
            pcmOffset = -1;
            decodeClear();
            return -1;
        }
        if (pos > pcmTotal(-1)) {
            // goto seek_error;
            pcmOffset = -1;
            decodeClear();
            return -1;
        }

        // discard samples until we reach the desired position. Crossing a
        // logical bitstream boundary with abandon is OK.
        while (pcmOffset < pos) {
            int target2 = (int) (pos - pcmOffset);
            float[][][] lPcm = new float[1][][];
            int[] lIndex = new int[getInfo(-1).channels];
            int samples = vd.synthesisPcmout(lPcm, lIndex);

            if (samples > target2) {
                samples = target2;
            }
            vd.synthesisRead(samples);
            pcmOffset += samples;

            if (samples < target2) {
                if (processPacket(1) == 0) {
                    pcmOffset = pcmTotal(-1); // eof
                }
            }
        }
        return 0;

        // seek_error:
        // dump machine so we're in a known state
        // pcm_offset=-1;
        // decode_clear();
        // return -1;
    }
    /**
     * Seek to a playback time relative to the decompressed pcm stream.
     * @param seconds seek time in seconds
     * @return zero on success, nonzero on failure
     */
    int timeSeek(float seconds) {
        // translate time to PCM position and call pcm_seek

        int link = -1;
        long pcmTotal = pcmTotal(-1);
        float timeTotal = timeTotal(-1);

        if (!seekable) {
            return (-1); // don't dump machine if we can't seek
        }
        if (seconds < 0 || seconds > timeTotal) {
            // goto seek_error;
            pcmOffset = -1;
            decodeClear();
            return -1;
        }

        // which bitstream section does this time offset occur in?
        for (link = links - 1; link >= 0; link--) {
            pcmTotal -= pcmlengths[link];
            timeTotal -= timeTotal(link);
            if (seconds >= timeTotal) {
                break;
            }
        }

        // enough information to convert time offset to pcm offset
        long target = (long) (pcmTotal + (seconds - timeTotal)
                * vi[link].rate);
        return (pcmSeek(target));

        // seek_error:
        // dump machine so we're in a known state
        // pcm_offset=-1;
        // decode_clear();
        // return -1;
    }
    /**
     * Tell the current stream offset cursor. Note that seek followed by
     * tell will likely not give the set offset due to caching
     * @return the current position
     */
    public long rawTell() {
        return (offset);
    }
    /**
     * Returns the PCM offset (sample) of next PCM sample to be read.
     * @return the PCM offset (sample) of next PCM sample to be read
     */
    public long pcmTell() {
        return (pcmOffset);
    }
    /**
     * Returns the time offset (seconds) of next PCM sample to be read.
     * @return the time offset (seconds) of next PCM sample to be read
     */
    // return time offset (seconds) of next PCM sample to be read
    public float timeTell() {
        // translate time to PCM position and call pcm_seek

        int link = -1;
        long pcmTotal = 0;
        float timeTotal = 0.f;

        if (seekable) {
            pcmTotal = pcmTotal(-1);
            timeTotal = timeTotal(-1);

            // which bitstream section does this time offset occur in?
            for (link = links - 1; link >= 0; link--) {
                pcmTotal -= pcmlengths[link];
                timeTotal -= timeTotal(link);
                if (pcmOffset >= pcmTotal) {
                    break;
                }
            }
        }

        return (timeTotal + (float) (pcmOffset - pcmTotal) / vi[link].rate);
    }
    /**
     * In the case of a non-seekable bitstream, any call returns the
     * current bitstream. NULL in the case that the machine is not
     * initialized
     * @param link -1) return the vorbis_info struct for the bitstream section<br>
     * 0-n) to request information for a specific bitstream section
     * @return the info
     */
    public Info getInfo(int link) {
        if (seekable) {
            if (link < 0) {
                if (decodeReady) {
                    return vi[currentLink];
                }
                return null;
            }
            if (link >= links) {
                return null;
            }
            return vi[link];
        }
        if (decodeReady) {
            return vi[0];
        }
        return null;
    }
    /**
     * Retuns the comment for the link.
     * @param link -1) return the vorbis_info struct for the bitstream section<br>
     * 0-n) to request information for a specific bitstream section
     * @return the comment
     */
    public Comment getComment(int link) {
        if (seekable) {
            if (link < 0) {
                if (decodeReady) {
                    return vc[currentLink];
                }
                return null;
            }
            if (link >= links) {
                return null;
            }
            return vc[link];
        }
        if (decodeReady) {
            return vc[0];
        }
        return null;
    }
    /**
     * Returns 1 if the host is in big endian.
     * @return 1 if the host is in big endian.
     */
    int hostIsBigEndian() {
        return 1;
        // short pattern = 0xbabe;
        // unsigned char *bytewise = (unsigned char *)&pattern;
        // if (bytewise[0] == 0xba) return 1;
        // assert(bytewise[0] == 0xbe);
        // return 0;
    }
    /**
     * up to this point, everything could more or less hide the multiple
     * logical bitstream nature of chaining from the toplevel application
     * if the toplevel application didn't particularly care. However, at
     * the point that we actually read audio back, the multiple-section
     * nature must surface: Multiple bitstream sections do not necessarily
     * have to have the same number of channels or sampling rate.
     *

     * read returns the sequential logical bitstream number currently
     * being decoded along with the PCM data in order that the toplevel
     * application can take action on channel/sample rate changes. This
     * number will be incremented even for streamed (non-seekable) streams
     * (for seekable streams, it represents the actual logical bitstream
     * index within the physical bitstream. Note that the accessor
     * functions above are aware of this dichotomy).
     * @param buffer a buffer to hold packed PCM data for return
     * @param length the byte length requested to be placed into buffer
     * @param bigendianp should the data be packed LSB first (0) or
     * MSB first (1)
     * @param word word size for output. currently 1 (byte) or
     * 2 (16 bit short)
     * @param sgned is signed int
     * @param bitstream the bit stream
     * @return sequential logical bitstream number
     * -1) error/hole in data<br>
     * 0) EOF<br>
     * n) number of bytes of PCM actually returned.<br> The
     * below works on a packet-by-packet basis, so the
     * return length is not related to the 'length' passed
     * in, just guaranteed to fit.
     */
    int read(byte[] buffer, int length, int bigendianp, int word, int sgned,
            int[] bitstream) {
        int hostEndian = hostIsBigEndian();
        int index = 0;

        while (true) {
            if (decodeReady) {
                float[][] pcm;
                float[][][] lPcm = new float[1][][];
                int[] lIndex = new int[getInfo(-1).channels];
                int samples = vd.synthesisPcmout(lPcm, lIndex);
                pcm = lPcm[0];
                if (samples != 0) {
                    // yay! proceed to pack data into the byte buffer
                    int channels = getInfo(-1).channels;
                    int bytespersample = word * channels;
                    if (samples > length / bytespersample) {
                        samples = length / bytespersample;
                    }

                    // a tight loop to pack each size
                    int val;
                    if (word == 1) {
                        int off = (sgned != 0 ? 0 : 128);
                        for (int j = 0; j < samples; j++) {
                            for (int i = 0; i < channels; i++) {
                                val = (int) (pcm[i][lIndex[i] + j] * 128. + 0.5);
                                if (val > 127) {
                                    val = 127;
                                } else

                                if (val < -128) {
                                    val = -128;
                                }
                                buffer[index++] = (byte) (val + off);
                            }
                        }
                    } else {
                        int off = (sgned != 0 ? 0 : 32768);

                        if (hostEndian == bigendianp) {
                            if (sgned != 0) {
                                for (int i = 0; i < channels; i++) { // It's
                                                                        // faster
                                                                        // in
                                                                        // this
                                                                        // order
                                    int src = lIndex[i];
                                    int dest = i;
                                    for (int j = 0; j < samples; j++) {
                                        val = (int) (pcm[i][src + j] * 32768. + 0.5);
                                        if (val > 32767) {
                                            val = 32767;
                                        } else

                                        if (val < -32768) {
                                            val = -32768;
                                        }
                                        buffer[dest] = (byte) (val >>> 8);
                                        buffer[dest + 1] = (byte) (val);
                                        dest += channels * 2;
                                    }
                                }
                            } else {
                                for (int i = 0; i < channels; i++) {
                                    float[] src = pcm[i];
                                    int dest = i;
                                    for (int j = 0; j < samples; j++) {
                                        val = (int) (src[j] * 32768. + 0.5);
                                        if (val > 32767) {
                                            val = 32767;
                                        } else

                                        if (val < -32768) {
                                            val = -32768;
                                        }
                                        buffer[dest] = (byte) ((val + off) >>> 8);
                                        buffer[dest + 1] = (byte) (val + off);
                                        dest += channels * 2;
                                    }
                                }
                            }
                        } else if (bigendianp != 0) {
                            for (int j = 0; j < samples; j++) {
                                for (int i = 0; i < channels; i++) {
                                    val = (int) (pcm[i][j] * 32768. + 0.5);
                                    if (val > 32767) {
                                        val = 32767;
                                    } else

                                    if (val < -32768) {
                                        val = -32768;
                                    }
                                    val += off;
                                    buffer[index++] = (byte) (val >>> 8);
                                    buffer[index++] = (byte) val;
                                }
                            }
                        } else {
                            // int val;
                            for (int j = 0; j < samples; j++) {
                                for (int i = 0; i < channels; i++) {
                                    val = (int) (pcm[i][j] * 32768. + 0.5);
                                    if (val > 32767) {
                                        val = 32767;
                                    } else

                                    if (val < -32768) {
                                        val = -32768;
                                    }
                                    val += off;
                                    buffer[index++] = (byte) val;
                                    buffer[index++] = (byte) (val >>> 8);
                                }
                            }
                        }
                    }

                    vd.synthesisRead(samples);
                    pcmOffset += samples;
                    if (bitstream != null) {
                        bitstream[0] = currentLink;
                    }
                    return (samples * bytespersample);
                }
            }

            // suck in another packet
            switch (processPacket(1)) {
            case 0:
                return (0);
            case -1:
                return -1;
            default:
                break;
            }
        }
    }
    /**
     * Returns the array of info.
     * @return the array of info
     */
    public Info[] getInfo() {
        return vi;
    }
    /**
     * Returns the array of comments.
     * @return the array of comments
     */
    public Comment[] getComment() {
        return vc;
    }
    /**
     * Closes the underlying data source.
     * @throws IOException if the underlying operation throws it
     */
    public void close() throws IOException {
        datasource.close();
    }
    /**
     * A seekable input stream extension to input stream.
     * Comments and style correction by karnokd.
     * @author ymnk
     */
    static class SeekableInputStream extends InputStream {
        /** The random access file to use. */
        RandomAccessFile raf = null;
        /** Open mode. */
        static final String MODE = "r";
        /**
         * Constructor. Opens the supplied file.
         * @param file the file to open
         * @throws IOException if there is problem with the file
         */
        SeekableInputStream(String file) throws IOException {
            raf = new java.io.RandomAccessFile(file, MODE);
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
        public int read(byte[] buf) throws IOException {
            return raf.read(buf);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int read(byte[] buf, int s, int len) throws IOException {
            return raf.read(buf, s, len);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public long skip(long n) throws IOException {
            return (raf.skipBytes((int) n));
        }
        /**
         * Returns the length of the file.
         * @return the length of the file
         * @throws IOException if an I/O error occurs
         */
        public long getLength() throws IOException {
            return raf.length();
        }
        /**
         * Returns the current file pointer.
         * @return the current file pointer
         * @throws IOException if an I/O error occurs
         */
        public long tell() throws IOException {
            return raf.getFilePointer();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int available() throws IOException {
            return (raf.length() == raf.getFilePointer()) ? 0 : 1;
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
        public synchronized void mark(int m) {
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized void reset() throws IOException {
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean markSupported() {
            return false;
        }
        /**
         * Move the file pointer to the position.
         * @param pos the position to move to
         * @throws IOException if an I/O error occurs
         */
        public void seek(long pos) throws IOException {
            raf.seek(pos);
        }
    }
}
