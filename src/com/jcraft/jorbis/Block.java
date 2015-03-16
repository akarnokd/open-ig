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
import com.jcraft.jogg.Packet;
/**
 * Block object.
 * Comments and style fix by karnokd. 
 * @author ymnk
 */
public class Block {
	/** 
	 * Necessary stream state for linking to the framing abstraction. 
	 * This is a pointer into local storage.
	 */
	float[][] pcm = new float[0][];
	/** Buffer. */
	Buffer opb = new Buffer();
	/** LW. */
	int lW;
	/** W. */
	int w;
	/** NW. */
	int nW;
	/** PCM end. */
	int pcmend;
	/** Mode. */
	int mode;
	/** EOF flag. */
	int eofflag;
	/** Granule position. */
	long granulepos;
	/** Sequence. */
	long sequence;
	/** For read-only access of configuration. */
	DspState vd;

	/** Glue bit metrics for the frame. */
	int glueBits;
	/** Time bit metrics for the frame. */
	int timeBits;
	/** Floor bit metrics for the frame. */
	int floorBits;
	/** Res bit metrics for the frame. */
	int resBits;
	/**
	 * Constructor. Sets the DspState.
	 * @param vd the dsp state
	 */
	public Block(DspState vd) {
		this.vd = vd;
		if (vd.analysisp != 0) {
			opb.writeinit();
		}
	}
	/**
	 * Initializes the dsp state.
	 * @param vd the dsp state
	 */
	public void init(DspState vd) {
		this.vd = vd;
	}
	/** Clears the internal state. */
	public void clear() {
		if (vd != null) {
			if (vd.analysisp != 0) {
				opb.writeclear();
			}
		}
	}
	/**
	 * Sinthesis.
	 * @param op packet
	 * @return success value
	 */
	public int synthesis(Packet op) {
		Info vi = vd.vi;

		// first things first. Make sure decode is ready
		opb.readinit(op.packetBase, op.packet, op.bytes);

		// Check the packet type
		if (opb.read(1) != 0) {
			// Oops. This is not an audio data packet
			return (-1);
		}

		// read our mode and pre/post windowsize
		int lMode = opb.read(vd.modebits);
		if (lMode == -1) {
			return (-1);
		}

		mode = lMode;
		w = vi.modeParam[mode].blockflag;
		if (w != 0) {
			lW = opb.read(1);
			nW = opb.read(1);
			if (nW == -1) {
				return (-1);
			}
		} else {
			lW = 0;
			nW = 0;
		}

		// more setup
		granulepos = op.granulepos;
		sequence = op.packetno - 3; // first block is third packet
		eofflag = op.endOfStream;

		// alloc pcm passback storage
		pcmend = vi.blocksizes[w];
		if (pcm.length < vi.channels) {
			pcm = new float[vi.channels][];
		}
		for (int i = 0; i < vi.channels; i++) {
			if (pcm[i] == null || pcm[i].length < pcmend) {
				pcm[i] = new float[pcmend];
			} else {
				for (int j = 0; j < pcmend; j++) {
					pcm[i][j] = 0;
				}
			}
		}

		// unpack_header enforces range checking
		int type = vi.mapType[vi.modeParam[mode].mapping];
		return (FuncMapping.mappingP[type].inverse(this, vd.mode[mode]));
	}
}
