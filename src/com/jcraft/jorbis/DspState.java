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
/**
 * DSP state record.
 * Comments and style correction by karnokd
 * @author ymnk
 *
 */
public class DspState {
    /** PI constant. */
    static final float M_PI = 3.1415926539f;
    /** VI transform b. */
    static final int VI_TRANSFORMB = 1;
    /** VI widnow b. */
    static final int VI_WINDOWB = 1;
    /** Analysis P. */
    int analysisp;
    /** Info. */
    Info vi;
    /** Mode bits. */
    int modebits;
    /** PCM. */
    float[][] pcm;
    /** PCM storage. */
    int pcmStorage;
    /** PCM current. */
    int pcmCurrent;
    /** PCM returned. */
    int pcmReturned;
    /** Multipliers. */
//    float[] multipliers;
    /** Envelope storage. */
//    int envelopeStorage;
    /** Envelope current. */
//    int envelopeCurrent;
    /** EOF flag. */
//    int eofflag;
    /** LW. */
    int lW;
    /** W. */
    int w;
    /** NW. */
//    int nW;
    /** CenterW. */
    int centerW;
    /** Granule pos. */
    long granulepos;
    /** Sequence. */
    long sequence;
    /** Glue bits. */
    long glueBits;
    /** Time bits. */
    long timeBits;
    /** Floor bits. */
    long floorBits;
    /** Res bits. */
    long resBits;

    /** Local lookup storage. block, leadin, leadout, type */
    float[][][][][] window;
    /** Transform. */
    Object[][] transform;
    /** Full blocks. */
    CodeBook[] fullbooks;
    /** backend lookups are tied to the mode, not the backend or naked mapping. */
    Object[] mode;

    // local storage, only used on the encoding side. This way the
    // application does not need to worry about freeing some packets'
    // memory and not others'; packet storage is always tracked.
    // Cleared next call to a _dsp_ function
    /** Header. */
//    byte[] header;
    /** Header 1. */
//    byte[] header1;
    /** Header 2. */
//    byte[] header2;
    /** Constructor. */
    public DspState() {
        transform = new Object[2][];
        window = new float[2][][][][];
        window[0] = new float[2][][][];
        window[0][0] = new float[2][][];
        window[0][1] = new float[2][][];
        window[0][0][0] = new float[2][];
        window[0][0][1] = new float[2][];
        window[0][1][0] = new float[2][];
        window[0][1][1] = new float[2][];
        window[1] = new float[2][][][];
        window[1][0] = new float[2][][];
        window[1][1] = new float[2][][];
        window[1][0][0] = new float[2][];
        window[1][0][1] = new float[2][];
        window[1][1][0] = new float[2][];
        window[1][1][1] = new float[2][];
    }
    /**
     * Window calculation.
     * @param type the type
     * @param window the window
     * @param left the left
     * @param right the right
     * @return the window array
     */
    static float[] window(int type, int window, int left, int right) {
        float[] ret = new float[window];
        switch (type) {
        case 0:
            // The 'vorbis window' (window 0) is sin(sin(x)*sin(x)*2pi)
            int leftbegin = window / 4 - left / 2;
            int rightbegin = window - window / 4 - right / 2;

            for (int i = 0; i < left; i++) {
                float x = (float) ((i + .5) / left * M_PI / 2.);
                x = (float) Math.sin(x);
                x *= x;
                x *= M_PI / 2.;
                x = (float) Math.sin(x);
                ret[i + leftbegin] = x;
            }

            for (int i = leftbegin + left; i < rightbegin; i++) {
                ret[i] = 1.f;
            }

            for (int i = 0; i < right; i++) {
                float x = (float) ((right - i - .5) / right * M_PI / 2.);
                x = (float) Math.sin(x);
                x *= x;
                x *= M_PI / 2.;
                x = (float) Math.sin(x);
                ret[i + rightbegin] = x;
            }
            break;
        default:
            // free(ret);
            return (null);
        }
        return (ret);
    }
    /**
     * Analysis side code, but directly related to blocking. Thus it's
     * here and not in analysis.c (which is for analysis transforms only).
     * The init is here because some of it is shared
     * @param vi the info
     * @param encp encode

     * @return the code
     */
    int init(Info vi, boolean encp) {
        this.vi = vi;
        modebits = Util.ilog2(vi.modes);

        transform[0] = new Object[VI_TRANSFORMB];
        transform[1] = new Object[VI_TRANSFORMB];

        // MDCT is tranform 0

        transform[0][0] = new Mdct();
        transform[1][0] = new Mdct();
        ((Mdct) transform[0][0]).init(vi.blocksizes[0]);
        ((Mdct) transform[1][0]).init(vi.blocksizes[1]);

        window[0][0][0] = new float[VI_WINDOWB][];
        window[0][0][1] = window[0][0][0];
        window[0][1][0] = window[0][0][0];
        window[0][1][1] = window[0][0][0];
        window[1][0][0] = new float[VI_WINDOWB][];
        window[1][0][1] = new float[VI_WINDOWB][];
        window[1][1][0] = new float[VI_WINDOWB][];
        window[1][1][1] = new float[VI_WINDOWB][];

        for (int i = 0; i < VI_WINDOWB; i++) {
            window[0][0][0][i] = window(i, vi.blocksizes[0],
                    vi.blocksizes[0] / 2, vi.blocksizes[0] / 2);
            window[1][0][0][i] = window(i, vi.blocksizes[1],
                    vi.blocksizes[0] / 2, vi.blocksizes[0] / 2);
            window[1][0][1][i] = window(i, vi.blocksizes[1],
                    vi.blocksizes[0] / 2, vi.blocksizes[1] / 2);
            window[1][1][0][i] = window(i, vi.blocksizes[1],
                    vi.blocksizes[1] / 2, vi.blocksizes[0] / 2);
            window[1][1][1][i] = window(i, vi.blocksizes[1],
                    vi.blocksizes[1] / 2, vi.blocksizes[1] / 2);
        }

        fullbooks = new CodeBook[vi.books];
        for (int i = 0; i < vi.books; i++) {
            fullbooks[i] = new CodeBook();
            fullbooks[i].initDecode(vi.bookParam[i]);
        }

        // initialize the storage vectors to a decent size greater than the
        // minimum

        pcmStorage = 8192; // we'll assume later that we have
        // a minimum of twice the blocksize of
        // accumulated samples in analysis
        pcm = new float[vi.channels][];
        for (int i = 0; i < vi.channels; i++) {
            pcm[i] = new float[pcmStorage];
        }

        // all 1 (large block) or 0 (small block)
        // explicitly set for the sake of clarity
        lW = 0; // previous window size
        w = 0; // current window size

        // all vector indexes; multiples of samples_per_envelope_step
        centerW = vi.blocksizes[1] / 2;

        pcmCurrent = centerW;

        // initialize all the mapping/backend lookups
        mode = new Object[vi.modes];
        for (int i = 0; i < vi.modes; i++) {
            int mapnum = vi.modeParam[i].mapping;
            int maptype = vi.mapType[mapnum];
            mode[i] = FuncMapping.mappingP[maptype].look(this,
                    vi.modeParam[i], vi.mapParam[mapnum]);
        }
        return (0);
    }
    /**
     * Synthesis initialize.
     * @param vi the info
     * @return value
     */
    public int synthesisInit(Info vi) {
        init(vi, false);
        // Adjust centerW to allow an easier mechanism for determining output
        pcmReturned = centerW;
        centerW -= vi.blocksizes[w] / 4 + vi.blocksizes[lW] / 4;
        granulepos = -1;
        sequence = -1;
        return (0);
    }
    /**
     * Constructor.
     * @param vi the info
     */
    DspState(Info vi) {
        this();
        init(vi, false);
        // Adjust centerW to allow an easier mechanism for determining output
        pcmReturned = centerW;
        centerW -= vi.blocksizes[w] / 4 + vi.blocksizes[lW] / 4;
        granulepos = -1;
        sequence = -1;
    }
    /**
     * Unike in analysis, the window is only partially applied for each
     * block. The time domain envelope is not yet handled at the point of
     * calling (as it relies on the previous block).
     * @param vb the block
     * @return value
     */
    public int synthesisBlockin(Block vb) {
        // Shift out any PCM/multipliers that we returned previously
        // centerW is currently the center of the last block added
        if (centerW > vi.blocksizes[1] / 2 && pcmReturned > 8192) {
            // don't shift too much; we need to have a minimum PCM buffer of
            // 1/2 long block

            int shiftPCM = centerW - vi.blocksizes[1] / 2;
            shiftPCM = (pcmReturned < shiftPCM ? pcmReturned : shiftPCM);

            pcmCurrent -= shiftPCM;
            centerW -= shiftPCM;
            pcmReturned -= shiftPCM;
            if (shiftPCM != 0) {
                for (int i = 0; i < vi.channels; i++) {
                    System.arraycopy(pcm[i], shiftPCM, pcm[i], 0, pcmCurrent);
                }
            }
        }

        lW = w;
        w = vb.w;
//        nW = -1;

        glueBits += vb.glueBits;
        timeBits += vb.timeBits;
        floorBits += vb.floorBits;
        resBits += vb.resBits;

        if (sequence + 1 != vb.sequence) {
            granulepos = -1; // out of sequence; lose count
        }
        sequence = vb.sequence;

        int sizeW = vi.blocksizes[w];
        int lCenterW = centerW + vi.blocksizes[lW] / 4 + sizeW / 4;
        int beginW = lCenterW - sizeW / 2;
        int endW = beginW + sizeW;
        int beginSl = 0;
        int endSl = 0;

        // Do we have enough PCM/mult storage for the block?
        if (endW > pcmStorage) {
            // expand the storage
            pcmStorage = endW + vi.blocksizes[1];
            for (int i = 0; i < vi.channels; i++) {
                float[] foo = new float[pcmStorage];
                System.arraycopy(pcm[i], 0, foo, 0, pcm[i].length);
                pcm[i] = foo;
            }
        }

        // overlap/add PCM
        switch (w) {
        case 0:
            beginSl = 0;
            endSl = vi.blocksizes[0] / 2;
            break;
        case 1:
            beginSl = vi.blocksizes[1] / 4 - vi.blocksizes[lW] / 4;
            endSl = beginSl + vi.blocksizes[lW] / 2;
            break;
        default:
        }

        for (int j = 0; j < vi.channels; j++) {
            int lPcm = beginW;
            // the overlap/add section
            int i = 0;
            for (i = beginSl; i < endSl; i++) {
                pcm[j][lPcm + i] += vb.pcm[j][i];
            }
            // the remaining section
            for (; i < sizeW; i++) {
                pcm[j][lPcm + i] = vb.pcm[j][i];
            }
        }

        // track the frame number... This is for convenience, but also
        // making sure our last packet doesn't end with added padding. If
        // the last packet is partial, the number of samples we'll have to
        // return will be past the vb->granulepos.
        //

        // This is not foolproof! It will be confused if we begin
        // decoding at the last page after a seek or hole. In that case,
        // we don't have a starting point to judge where the last frame
        // is. For this reason, vorbisfile will always try to make sure
        // it reads the last two marked pages in proper sequence

        if (granulepos == -1) {
            granulepos = vb.granulepos;
        } else {
            granulepos += (lCenterW - centerW);
            if (vb.granulepos != -1 && granulepos != vb.granulepos) {
                if (granulepos > vb.granulepos && vb.eofflag != 0) {
                    // partial last frame. Strip the padding off
                    lCenterW -= (granulepos - vb.granulepos);
                } // else{ Shouldn't happen *unless* the bitstream is out of
                // spec. Either way, believe the bitstream }
                granulepos = vb.granulepos;
            }
        }

        // Update, cleanup

        centerW = lCenterW;
        pcmCurrent = endW;
//        if (vb.eofflag != 0) {
//            eofflag = 1;
//        }
        return (0);
    }
    /**
     * Synthesis PCM out.
     * @param pPcm NULL indicates we just want the pending samples, no more
     * @param index the index
     * @return value
     */
    public int synthesisPcmout(float[][][] pPcm, int[] index) {
        if (pcmReturned < centerW) {
            if (pPcm != null) {
                for (int i = 0; i < vi.channels; i++) {
                    index[i] = pcmReturned;
                }
                pPcm[0] = pcm;
            }
            return (centerW - pcmReturned);
        }
        return (0);
    }
    /**
     * Synthesis read.
     * @param bytes number of bytes
     * @return success
     */
    public int synthesisRead(int bytes) {
        if (bytes != 0 && pcmReturned + bytes > centerW) {
            return (-1);
        }
        pcmReturned += bytes;
        return (0);
    }
    /** Clear. */
    public void clear() {
    }
}
