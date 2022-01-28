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
 * Time zero.
 * Comments and style correction by karnokd.
 * @author ymnk
 */
class Time0 extends FuncTime {
    /**
     * {@inheritDoc}
     */
    @Override
    void pack(Object i, Buffer opb) {
    }
    /**
     * {@inheritDoc}
     */
    @Override
    Object unpack(Info vi, Buffer opb) {
        return "";
    }
    /**
     * {@inheritDoc}
     */
    @Override
    Object look(DspState vd, InfoMode mi, Object i) {
        return "";
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
    /**
     * {@inheritDoc}
     */
    @Override
    int inverse(Block vb, Object i, float[] in, float[] out) {
        return 0;
    }
}
