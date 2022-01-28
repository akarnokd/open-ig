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

import com.jcraft.jogg.Buffer;
/**
 * Mapping zero.
 * Comments and style corrections by karnokd.
 * @author ymnk
 *
 */
class Mapping0 extends FuncMapping {
    /** Sequence. */
    static int seq = 0;
    /**
     * {@inheritDoc}
     */
    @Override
    void freeInfo(Object imap) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void freeLook(Object imap) {
    }
    /**
     * {@inheritDoc}
     */
    @Override
    Object look(DspState vd, InfoMode vm, Object m) {
        // System.err.println("Mapping0.look");
        Info vi = vd.vi;
        LookMapping0 look = new LookMapping0();
        InfoMapping0 info = (InfoMapping0)m;
        look.map = info;
        look.mode = vm;

        look.timeLook = new Object[info.submaps];
        look.floorLook = new Object[info.submaps];
        look.residueLook = new Object[info.submaps];

        look.timeFunc = new FuncTime[info.submaps];
        look.floorFunc = new FuncFloor[info.submaps];
        look.residueFunc = new FuncResidue[info.submaps];

        for (int i = 0; i < info.submaps; i++) {
            int timenum = info.timesubmap[i];
            int floornum = info.floorsubmap[i];
            int resnum = info.residuesubmap[i];

            look.timeFunc[i] = FuncTime.timeP[vi.timeType[timenum]];
            look.timeLook[i] = look.timeFunc[i].look(vd, vm,
                    vi.timeParam[timenum]);
            look.floorFunc[i] = FuncFloor.floorP[vi.floorType[floornum]];
            look.floorLook[i] = look.floorFunc[i].look(vd, vm,
                    vi.floorParam[floornum]);
            look.residueFunc[i] = FuncResidue.residueP[vi.residueType[resnum]];
            look.residueLook[i] = look.residueFunc[i].look(vd, vm,
                    vi.residueParam[resnum]);

        }

//        if (vi.psys != 0 && vd.analysisp != 0) {
//            // ??
//        }

//        look.ch = vi.channels;

        return (look);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    void pack(Info vi, Object imap, Buffer opb) {
        InfoMapping0 info = (InfoMapping0) imap;

        /*
         * another 'we meant to do it this way' hack... up to beta 4, we packed
         * 4 binary zeros here to signify one submapping in use. We now redefine
         * that to mean four bitflags that indicate use of deeper features;
         * bit0:submappings, bit1:coupling, bit2,3:reserved. This is backward
         * compatable with all actual uses of the beta code.
         */

        if (info.submaps > 1) {
            opb.write(1, 1);
            opb.write(info.submaps - 1, 4);
        } else {
            opb.write(0, 1);
        }

        if (info.couplingSteps > 0) {
            opb.write(1, 1);
            opb.write(info.couplingSteps - 1, 8);
            for (int i = 0; i < info.couplingSteps; i++) {
                opb.write(info.couplingMag[i], Util.ilog2(vi.channels));
                opb.write(info.couplingAng[i], Util.ilog2(vi.channels));
            }
        } else {
            opb.write(0, 1);
        }

        opb.write(0, 2); /* 2,3:reserved */

        /* we don't write the channel submappings if we only have one... */
        if (info.submaps > 1) {
            for (int i = 0; i < vi.channels; i++) {
                opb.write(info.chmuxlist[i], 4);
            }
        }
        for (int i = 0; i < info.submaps; i++) {
            opb.write(info.timesubmap[i], 8);
            opb.write(info.floorsubmap[i], 8);
            opb.write(info.residuesubmap[i], 8);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    Object unpack(Info vi, Buffer opb) {
        InfoMapping0 info = new InfoMapping0();

        if (opb.read(1) != 0) {
            info.submaps = opb.read(4) + 1;
        } else {
            info.submaps = 1;
        }

        if (opb.read(1) != 0) {
            info.couplingSteps = opb.read(8) + 1;

            for (int i = 0; i < info.couplingSteps; i++) {
                int testM = opb.read(Util
                        .ilog2(vi.channels));
                info.couplingMag[i] = testM;
                int testA = opb.read(Util
                        .ilog2(vi.channels));
                info.couplingAng[i] = testA;
                if (testM < 0 || testA < 0 || testM == testA
                        || testM >= vi.channels || testA >= vi.channels) {
                    // goto err_out;
                    info.free();
                    return (null);
                }
            }
        }

        if (opb.read(2) > 0) { /* 2,3:reserved */
            info.free();
            return (null);
        }

        if (info.submaps > 1) {
            for (int i = 0; i < vi.channels; i++) {
                info.chmuxlist[i] = opb.read(4);
                if (info.chmuxlist[i] >= info.submaps) {
                    info.free();
                    return (null);
                }
            }
        }

        for (int i = 0; i < info.submaps; i++) {
            info.timesubmap[i] = opb.read(8);
            if (info.timesubmap[i] >= vi.times) {
                info.free();
                return (null);
            }
            info.floorsubmap[i] = opb.read(8);
            if (info.floorsubmap[i] >= vi.floors) {
                info.free();
                return (null);
            }
            info.residuesubmap[i] = opb.read(8);
            if (info.residuesubmap[i] >= vi.residues) {
                info.free();
                return (null);
            }
        }
        return info;
    }
    /** PCM bundle. */
    float[][] pcmbundle;
    /** Zero bundle. */
    int[] zerobundle;
    /** Non zero. */
    int[] nonzero;
    /** Flor memo. */
    Object[] floormemo;
    /**
     * {@inheritDoc}
     */
    @Override
    synchronized int inverse(Block vb, Object l) {
        DspState vd = vb.vd;
        Info vi = vd.vi;
        LookMapping0 look = (LookMapping0) l;
        InfoMapping0 info = look.map;
        InfoMode mode = look.mode;
        int n = vi.blocksizes[vb.w];
        vb.pcmend = n;

        float[] window = vd.window[vb.w][vb.lW][vb.nW][mode.windowtype];
        if (pcmbundle == null || pcmbundle.length < vi.channels) {
            pcmbundle = new float[vi.channels][];
            nonzero = new int[vi.channels];
            zerobundle = new int[vi.channels];
            floormemo = new Object[vi.channels];
        }

        // time domain information decode (note that applying the
        // information would have to happen later; we'll probably add a
        // function entry to the harness for that later
        // NOT IMPLEMENTED

        // recover the spectral envelope; store it in the PCM vector for now
        for (int i = 0; i < vi.channels; i++) {
            float[] pcm = vb.pcm[i];
            int submap = info.chmuxlist[i];

            floormemo[i] = look.floorFunc[submap].inverse1(vb,
                    look.floorLook[submap], floormemo[i]);
            if (floormemo[i] != null) {
                nonzero[i] = 1;
            } else {
                nonzero[i] = 0;
            }
            for (int j = 0; j < n / 2; j++) {
                pcm[j] = 0;
            }

        }

        for (int i = 0; i < info.couplingSteps; i++) {
            if (nonzero[info.couplingMag[i]] != 0
                    || nonzero[info.couplingAng[i]] != 0) {
                nonzero[info.couplingMag[i]] = 1;
                nonzero[info.couplingAng[i]] = 1;
            }
        }

        // recover the residue, apply directly to the spectral envelope

        for (int i = 0; i < info.submaps; i++) {
            int chInBundle = 0;
            for (int j = 0; j < vi.channels; j++) {
                if (info.chmuxlist[j] == i) {
                    if (nonzero[j] != 0) {
                        zerobundle[chInBundle] = 1;
                    } else {
                        zerobundle[chInBundle] = 0;
                    }
                    pcmbundle[chInBundle++] = vb.pcm[j];
                }
            }

            look.residueFunc[i].inverse(vb, look.residueLook[i], pcmbundle,
                    zerobundle, chInBundle);
        }

        for (int i = info.couplingSteps - 1; i >= 0; i--) {
            float[] pcmM = vb.pcm[info.couplingMag[i]];
            float[] pcmA = vb.pcm[info.couplingAng[i]];

            for (int j = 0; j < n / 2; j++) {
                float mag = pcmM[j];
                float ang = pcmA[j];

                if (mag > 0) {
                    if (ang > 0) {
                        pcmM[j] = mag;
                        pcmA[j] = mag - ang;
                    } else {
                        pcmA[j] = mag;
                        pcmM[j] = mag + ang;
                    }
                } else {
                    if (ang > 0) {
                        pcmM[j] = mag;
                        pcmA[j] = mag + ang;
                    } else {
                        pcmA[j] = mag;
                        pcmM[j] = mag - ang;
                    }
                }
            }
        }

        // /* compute and apply spectral envelope */

        for (int i = 0; i < vi.channels; i++) {
            float[] pcm = vb.pcm[i];
            int submap = info.chmuxlist[i];
            look.floorFunc[submap].inverse2(vb, look.floorLook[submap],
                    floormemo[i], pcm);
        }

        // transform the PCM data; takes PCM vector, vb; modifies PCM vector
        // only MDCT right now....

        for (int i = 0; i < vi.channels; i++) {
            float[] pcm = vb.pcm[i];
            // _analysis_output("out",seq+i,pcm,n/2,0,0);
            ((Mdct) vd.transform[vb.w][0]).backward(pcm, pcm);
        }

        // now apply the decoded pre-window time information
        // NOT IMPLEMENTED

        // window the data
        for (int i = 0; i < vi.channels; i++) {
            float[] pcm = vb.pcm[i];
            if (nonzero[i] != 0) {
                for (int j = 0; j < n; j++) {
                    pcm[j] *= window[j];
                }
            } else {
                for (int j = 0; j < n; j++) {
                    pcm[j] = 0.f;
                }
            }
        }

        // now apply the decoded post-window time information
        // NOT IMPLEMENTED
        // all done!
        return (0);
    }
    /**
     * Info mapping 0.
     * Comments and style corrections by karnokd.
     * @author ymnk
     */
    static class InfoMapping0 {
        /** Sub maps. */
        int submaps; // <= 16
        /** Up to 256 channels in a Vorbis stream. */
        int[] chmuxlist = new int[256]; // up to 256 channels in a Vorbis stream
        /** Time submap. */
        int[] timesubmap = new int[16]; // [mux]
        /** Submap to floors. */
        int[] floorsubmap = new int[16]; // [mux]

        /** Submap to residue. */
        int[] residuesubmap = new int[16]; // [mux]

        /** Encode only. */
//        int[] psysubmap = new int[16]; // [mux];

        /** Coupling steps. */
        int couplingSteps;
        /** Coupling mag. */
        int[] couplingMag = new int[256];
        /** Coupling ang. */
        int[] couplingAng = new int[256];
        /** Free. */
        void free() {
            chmuxlist = null;
            timesubmap = null;
            floorsubmap = null;
            residuesubmap = null;
//            psysubmap = null;

            couplingMag = null;
            couplingAng = null;
        }
    }
    /**
     * Look mapping 0.

     * Comments and style corrections by karnokd.
     * @author ymnk
     */
    static class LookMapping0 {
        /** Mode. */
        InfoMode mode;
        /** Info mapping. */
        InfoMapping0 map;
        /** Time look. */
        Object[] timeLook;
        /** Floor look. */
        Object[] floorLook;
        /** Flor state. */
//        Object[] floorState;
        /** Residue look. */
        Object[] residueLook;
        /** Psy look. */
//        PsyLook[] psyLook;
        /** Time functions. */
        FuncTime[] timeFunc;
        /** Floor functions. */
        FuncFloor[] floorFunc;
        /** Residue functions. */
        FuncResidue[] residueFunc;
        /** Channel. */
//        int ch;
        /** Decay. */
//        float[][] decay;
        /**
         * If a different mode is called, we need to
         * invalidate decay and floor state.
         */
//        int lastframe;
    }
}
