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

package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
/**
 * Floor function abstraction.
 * Comments and style correction by karnokd.
 * @author ymnk
 */
abstract class FuncFloor {
	/** Floor fucntions. */
	public static FuncFloor[] floorP = { new Floor0(), new Floor1() };
	/**
	 * Pack the object into the buffer.
	 * @param i the object
	 * @param opb the buffer
	 */
	abstract void pack(Object i, Buffer opb);
	/**
	 * Unpack the object from the buffer.
	 * @param vi the Info
	 * @param opb the buffer
	 * @return the unpacked object
	 */
	abstract Object unpack(Info vi, Buffer opb);
	/**
	 * Look at dsp state, info mode and object.
	 * @param vd dsp state
	 * @param mi info mode
	 * @param i object
	 * @return object
	 */
	abstract Object look(DspState vd, InfoMode mi, Object i);
	/**
	 * Free info.
	 * @param i object
	 */
	abstract void freeInfo(Object i);
	/**
	 * Free look.
	 * @param i object
	 */
	abstract void freeLook(Object i);
	/**
	 * Free state.
	 * @param vs object
	 */
	abstract void freeState(Object vs);
	/**
	 * Forward.
	 * @param vb block
	 * @param i object
	 * @param in float array
	 * @param out float array
	 * @param vs object
	 * @return int
	 */
	abstract int forward(Block vb, Object i, float[] in, float[] out, Object vs);
	/**
	 * Inverse 1.
	 * @param vb block
	 * @param i object
	 * @param memo object
	 * @return object
	 */
	abstract Object inverse1(Block vb, Object i, Object memo);
	/**
	 * Inverse 2.
	 * @param vb block
	 * @param i object
	 * @param memo object
	 * @param out float array
	 * @return int
	 */
	abstract int inverse2(Block vb, Object i, Object memo, float[] out);
}
