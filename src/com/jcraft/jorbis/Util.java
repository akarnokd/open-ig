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
 * Utility functions.
 * Comments and style corrections by karnokd.
 * @author ymnk
 */
final class Util {
    /** Private constructor. */
    private Util() {
        // utility class
    }
    /**
     * Calculates the log2 of the given value.
     * @param v the value
     * @return the log2

     */
    static int ilog(int v) {
        int ret = 0;
        while (v != 0) {
            ret++;
            v >>>= 1;
        }
        return (ret);
    }
    /**
     * Calculates the log2 of the given value.
     * @param v the value
     * @return the log2

     */
    static int ilog2(int v) {
        int ret = 0;
        while (v > 1) {
            ret++;
            v >>>= 1;
        }
        return (ret);
    }
    /**
     * Calculates the number of 1 bits in the value.
     * @param v the value
     * @return the number of bits
     */
    static int icount(int v) {
//        int ret = 0;
//        while (v != 0) {
//            ret += (v & 1);
//            v >>>= 1;
//        }
//        return (ret);
        return Integer.bitCount(v);
    }
}
