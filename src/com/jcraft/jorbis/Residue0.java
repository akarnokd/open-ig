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
 * Residue.

 * Comments and style corrections by karnokd.
 * @author ymnk
 */
class Residue0 extends FuncResidue {
    /**
     * {@inheritDoc}
     */
    @Override
    void pack(Object vr, Buffer opb) {
        InfoResidue0 info = (InfoResidue0) vr;
        int acc = 0;
        opb.write(info.begin, 24);
        opb.write(info.end, 24);

        opb.write(info.grouping - 1, 24); /*
                                         * residue vectors to group and code
                                         * with a partitioned book
                                         */
        opb.write(info.partitions - 1, 6); /* possible partition choices */
        opb.write(info.groupbook, 8); /* group huffman book */

        /*
         * secondstages is a bitmask; as encoding progresses pass by pass, a
         * bitmask of one indicates this partition class has bits to write this
         * pass
         */
        for (int j = 0; j < info.partitions; j++) {
            int i = info.secondstages[j];
            if (Util.ilog(i) > 3) {
                /* yes, this is a minor hack due to not thinking ahead */
                opb.write(i, 3);
                opb.write(1, 1);
                opb.write(i >>> 3, 5);
            } else {
                opb.write(i, 4); /* trailing zero */
            }
            acc += Util.icount(i);
        }
        for (int j = 0; j < acc; j++) {
            opb.write(info.booklist[j], 8);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    Object unpack(Info vi, Buffer opb) {
        int acc = 0;
        InfoResidue0 info = new InfoResidue0();
        info.begin = opb.read(24);
        info.end = opb.read(24);
        info.grouping = opb.read(24) + 1;
        info.partitions = opb.read(6) + 1;
        info.groupbook = opb.read(8);

        for (int j = 0; j < info.partitions; j++) {
            int cascade = opb.read(3);
            if (opb.read(1) != 0) {
                cascade |= (opb.read(5) << 3);
            }
            info.secondstages[j] = cascade;
            acc += Util.icount(cascade);
        }

        for (int j = 0; j < acc; j++) {
            info.booklist[j] = opb.read(8);
        }

        if (info.groupbook >= vi.books) {
            freeInfo(info);
            return (null);
        }

        for (int j = 0; j < acc; j++) {
            if (info.booklist[j] >= vi.books) {
                freeInfo(info);
                return (null);
            }
        }
        return (info);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    Object look(DspState vd, InfoMode vm, Object vr) {
        InfoResidue0 info = (InfoResidue0) vr;
        LookResidue0 look = new LookResidue0();
        int acc = 0;
        int dim;
        int maxstage = 0;
        look.info = info;
//        look.map = vm.mapping;

        look.parts = info.partitions;
        look.fullbooks = vd.fullbooks;
        look.phrasebook = vd.fullbooks[info.groupbook];

        dim = look.phrasebook.dim;

        look.partbooks = new int[look.parts][];

        for (int j = 0; j < look.parts; j++) {
            int i = info.secondstages[j];
            int stages = Util.ilog(i);
            if (stages != 0) {
                if (stages > maxstage) {
                    maxstage = stages;
                }
                look.partbooks[j] = new int[stages];
                for (int k = 0; k < stages; k++) {
                    if ((i & (1 << k)) != 0) {
                        look.partbooks[j][k] = info.booklist[acc++];
                    }
                }
            }
        }

        look.partvals = (int) Math.rint(Math.pow(look.parts, dim));
        look.stages = maxstage;
        look.decodemap = new int[look.partvals][];
        for (int j = 0; j < look.partvals; j++) {
            int val = j;
            int mult = look.partvals / look.parts;
            look.decodemap[j] = new int[dim];

            for (int k = 0; k < dim; k++) {
                int deco = val / mult;
                val -= deco * mult;
                mult /= look.parts;
                look.decodemap[j][k] = deco;
            }
        }
        return (look);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    void freeInfo(Object i) {
    }
    /**
     * {@inheritDoc}
     */
    @Override
    void freeLook(Object i) {
    }
    /** Inverse partworld. is synchronized for re-using partword */
    private static int[][][] m01inversePartword = new int[2][][];

    /**
     * Inverse.
     * @param vb block
     * @param vl object
     * @param in float float array
     * @param ch int
     * @param decodepart int
     * @return int
     */
    static synchronized int f01inverse(Block vb, Object vl, float[][] in,
            int ch, int decodepart) {
        int i, j, k, l, s;
        LookResidue0 look = (LookResidue0) vl;
        InfoResidue0 info = look.info;

        // move all this setup out later
        int samplesPerPartition = info.grouping;
        int partitionsPerWord = look.phrasebook.dim;
        int n = info.end - info.begin;

        int partvals = n / samplesPerPartition;
        int partwords = (partvals + partitionsPerWord - 1)
                / partitionsPerWord;

        if (m01inversePartword.length < ch) {
            m01inversePartword = new int[ch][][];
        }

        for (j = 0; j < ch; j++) {
            if (m01inversePartword[j] == null
                    || m01inversePartword[j].length < partwords) {
                m01inversePartword[j] = new int[partwords][];
            }
        }

        for (s = 0; s < look.stages; s++) {
            // each loop decodes on partition codeword containing
            // partitions_pre_word partitions
            for (i = 0, l = 0; i < partvals; l++) {
                if (s == 0) {
                    // fetch the partition word for each channel
                    for (j = 0; j < ch; j++) {
                        int temp = look.phrasebook.decode(vb.opb);
                        if (temp == -1) {
                            return (0);
                        }
                        m01inversePartword[j][l] = look.decodemap[temp];
                        if (m01inversePartword[j][l] == null) {
                            return (0);
                        }
                    }
                }

                // now we decode residual values for the partitions
                for (k = 0; k < partitionsPerWord && i < partvals; k++, i++) {
                    for (j = 0; j < ch; j++) {
                        int offset = info.begin + i * samplesPerPartition;
                        int index = m01inversePartword[j][l][k];
                        if ((info.secondstages[index] & (1 << s)) != 0) {
                            CodeBook stagebook = look.fullbooks[look.partbooks[index][s]];
                            if (stagebook != null) {
                                if (decodepart == 0) {
                                    if (stagebook.decodevsAdd(in[j], offset,
                                            vb.opb, samplesPerPartition) == -1) {
                                        return (0);
                                    }
                                } else if (decodepart == 1) {
                                    if (stagebook.decodevAdd(in[j], offset,
                                            vb.opb, samplesPerPartition) == -1) {
                                        return (0);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return (0);
    }
    /** Inverse part word 2. */
    static int[][] m2inversePartword;
    /**
     * Inverse 2.
     * @param vb block
     * @param vl object
     * @param in float float array
     * @param ch int
     * @return int
     */
    static synchronized int f2inverse(Block vb, Object vl, float[][] in, int ch) {
        int i, k, l, s;
        LookResidue0 look = (LookResidue0) vl;
        InfoResidue0 info = look.info;

        // move all this setup out later
        int samplesPerPartition = info.grouping;
        int partitionsPerWord = look.phrasebook.dim;
        int n = info.end - info.begin;

        int partvals = n / samplesPerPartition;
        int partwords = (partvals + partitionsPerWord - 1)
                / partitionsPerWord;

        if (m2inversePartword == null || m2inversePartword.length < partwords) {
            m2inversePartword = new int[partwords][];
        }
        for (s = 0; s < look.stages; s++) {
            for (i = 0, l = 0; i < partvals; l++) {
                if (s == 0) {
                    // fetch the partition word for each channel
                    int temp = look.phrasebook.decode(vb.opb);
                    if (temp == -1) {
                        return (0);
                    }
                    m2inversePartword[l] = look.decodemap[temp];
                    if (m2inversePartword[l] == null) {
                        return (0);
                    }
                }

                // now we decode residual values for the partitions
                for (k = 0; k < partitionsPerWord && i < partvals; k++, i++) {
                    int offset = info.begin + i * samplesPerPartition;
                    int index = m2inversePartword[l][k];
                    if ((info.secondstages[index] & (1 << s)) != 0) {
                        CodeBook stagebook = look.fullbooks[look.partbooks[index][s]];
                        if (stagebook != null) {
                            if (stagebook.decodevvAdd(in, offset, ch, vb.opb,
                                    samplesPerPartition) == -1) {
                                return (0);
                            }
                        }
                    }
                }
            }
        }
        return (0);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    int inverse(Block vb, Object vl, float[][] in, int[] nonzero, int ch) {
        int used = 0;
        for (int i = 0; i < ch; i++) {
            if (nonzero[i] != 0) {
                in[used++] = in[i];
            }
        }
        if (used != 0) {
            return f01inverse(vb, vl, in, used, 0);
        }
        return 0;
    }
    /**
     * Lookup residue 0.
     * Comments and style corrections by karnokd.
     * @author ymnk
     */
    static class LookResidue0 {
        /** Info residue. */
        InfoResidue0 info;
        /** Map. */
//        int map;
        /** Parts. */
        int parts;
        /** Stages. */
        int stages;
        /** Full books. */
        CodeBook[] fullbooks;
        /** Phrase book. */
        CodeBook phrasebook;
        /** Part books. */
        int[][] partbooks;
        /** Part values. */
        int partvals;
        /** Decode map. */
        int[][] decodemap;
        /** Post bits. */
//        int postbits;
        /** Pharse bits. */
//        int phrasebits;
        /** Frames. */
//        int frames;
    }
    /**
     * Info residue 0.
     * Block-partitioned VQ coded straight residue.
     * Comments and style corrections by karnokd.
     * @author ymnk
     */
    static class InfoResidue0 {
        /** Begin. */
        int begin;
        /** End. */
        int end;

        // first stage (lossless partitioning)
        /** Group n vectors per partition. */
        int grouping;
        /** Possible codebooks for a partition. */
        int partitions;
        /** Huffbook for partitioning. */
        int groupbook;
        /** Expanded out to pointers in lookup. */
        int[] secondstages = new int[64];

        /** List of second stage books. */
        int[] booklist = new int[256];

        // encode-only heuristic settings
        /** Book entropy threshholds. */
//        float[] entmax = new float[64];
        /** Book amp threshholds. */
//        float[] ampmax = new float[64];

        /** Book heuristic subgroup size. */
//        int[] subgrp = new int[64];
        /** Subgroup position limits. */
//        int[] blimit = new int[64];
    }

}
