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
 * Info object.
 * Comments and style correction by karnokd.
 * @author ymnk
 */
public class Info {
	/** OV error bad packed. */
	private static final int OV_EBADPACKET = -136;
	/** OV error not audio. */
	private static final int OV_ENOTAUDIO = -135;
	/** Vorbis string bytes. */
	private static final byte[] VORBIS_BYTES = "vorbis".getBytes();
	/** VI time b. */
	private static final int VI_TIMEB = 1;
	/** VI floor b. */
	private static final int VI_FLOORB = 2;
	/** VI res b. */
	private static final int VI_RESB = 3;
	/** VI map b. */
	private static final int VI_MAPB = 1;
	/** VI window b. */
	private static final int VI_WINDOWB = 1;
	/** Version. */
	public int version;
	/** Channels. */
	public int channels;
	/** Rate. */
	public int rate;

	// The below bitrate declarations are *hints*.
	// Combinations of the three values carry the following implications:
	//     
	// all three set to the same value:
	// implies a fixed rate bitstream
	// only nominal set:
	// implies a VBR stream that averages the nominal bitrate. No hard
	// upper/lower limit
	// upper and or lower set:
	// implies a VBR bitstream that obeys the bitrate limits. nominal
	// may also be set to give a nominal rate.
	// none set:
	// the coder does not care to speculate.
	/** Upper bitrate. */
	int bitrateUpper;
	/** Nominal bitrate. */
	int bitrateNominal;
	/** Lower bitrate. */
	int bitrateLower;

	// Vorbis supports only short and long blocks, but allows the
	// encoder to choose the sizes
	/** 
	 * Vorbis supports only short and long blocks, but allows the 
	 * encoder to choose the sizes.
	 */
	int[] blocksizes = new int[2];

	// modes are the primary means of supporting on-the-fly different
	// blocksizes, different channel mappings (LR or mid-side),
	// different residue backends, etc. Each mode consists of a
	// blocksize flag and a mapping (along with the mapping setup
	/** Modes. */
	int modes;
	/** Maps. */
	int maps;
	/** Times. */
	int times;
	/** Floors. */
	int floors;
	/** Residues. */
	int residues;
	/** Books. */
	int books;
	/** Info mode param. */
	InfoMode[] modeParam;
	/** Map type. */
	int[] mapType;
	/** Map param. */
	Object[] mapParam;
	/** Time type. */
	int[] timeType;
	/** Time param. */
	Object[] timeParam;
	/** Floor type. */
	int[] floorType;
	/** Floor param. */
	Object[] floorParam;
	/** Residue type. */
	int[] residueType;
	/** Residue param. */
	Object[] residueParam;
	/** Book params. */
	StaticCodeBook[] bookParam;

	/** used by synthesis, which has a full, alloced vi. */
	public void init() {
		rate = 0;
	}
	/** Clear. */
	public void clear() {
		modeParam = null;

		for (int i = 0; i < maps; i++) { // unpack does the range checking
			FuncMapping.mappingP[mapType[i]].freeInfo(mapParam[i]);
		}
		mapParam = null;

		for (int i = 0; i < times; i++) { // unpack does the range checking
			FuncTime.timeP[timeType[i]].freeInfo(timeParam[i]);
		}
		timeParam = null;

		for (int i = 0; i < floors; i++) { // unpack does the range checking
			FuncFloor.floorP[floorType[i]].freeInfo(floorParam[i]);
		}
		floorParam = null;

		for (int i = 0; i < residues; i++) { // unpack does the range checking
			FuncResidue.residueP[residueType[i]].freeInfo(residueParam[i]);
		}
		residueParam = null;

		// the static codebooks *are* freed if you call info_clear, because
		// decode side does alloc a 'static' codebook. Calling clear on the
		// full codebook does not clear the static codebook (that's our
		// responsibility)
		for (int i = 0; i < books; i++) {
			// just in case the decoder pre-cleared to save space
			if (bookParam[i] != null) {
				bookParam[i].clear();
				bookParam[i] = null;
			}
		}
		// if(vi->book_param)free(vi->book_param);
		bookParam = null;

	}
	/**
	 * Header packing/unpacking.
	 * @param opb buffer
	 * @return success
	 */
	int unpackInfo(Buffer opb) {
		version = opb.read(32);
		if (version != 0) {
			return (-1);
		}

		channels = opb.read(8);
		rate = opb.read(32);

		bitrateUpper = opb.read(32);
		bitrateNominal = opb.read(32);
		bitrateLower = opb.read(32);

		blocksizes[0] = 1 << opb.read(4);
		blocksizes[1] = 1 << opb.read(4);

		if ((rate < 1) || (channels < 1) || (blocksizes[0] < 8)
				|| (blocksizes[1] < blocksizes[0]) || (opb.read(1) != 1)) {
			clear();
			return (-1);
		}
		return (0);
	}
	/**
	 * All of the real encoding details are here. The modes, books,
	 * everything.
	 * @param opb buffer
	 * @return success
	 */
	int unpackBooks(Buffer opb) {

		books = opb.read(8) + 1;

		if (bookParam == null || bookParam.length != books) {
			bookParam = new StaticCodeBook[books];
		}
		for (int i = 0; i < books; i++) {
			bookParam[i] = new StaticCodeBook();
			if (bookParam[i].unpack(opb) != 0) {
				clear();
				return (-1);
			}
		}

		// time backend settings
		times = opb.read(6) + 1;
		if (timeType == null || timeType.length != times) {
			timeType = new int[times];
		}
		if (timeParam == null || timeParam.length != times) {
			timeParam = new Object[times];
		}
		for (int i = 0; i < times; i++) {
			timeType[i] = opb.read(16);
			if (timeType[i] < 0 || timeType[i] >= VI_TIMEB) {
				clear();
				return (-1);
			}
			timeParam[i] = FuncTime.timeP[timeType[i]].unpack(this, opb);
			if (timeParam[i] == null) {
				clear();
				return (-1);
			}
		}

		// floor backend settings
		floors = opb.read(6) + 1;
		if (floorType == null || floorType.length != floors) {
			floorType = new int[floors];
		}
		if (floorParam == null || floorParam.length != floors) {
			floorParam = new Object[floors];
		}

		for (int i = 0; i < floors; i++) {
			floorType[i] = opb.read(16);
			if (floorType[i] < 0 || floorType[i] >= VI_FLOORB) {
				clear();
				return (-1);
			}

			floorParam[i] = FuncFloor.floorP[floorType[i]].unpack(this, opb);
			if (floorParam[i] == null) {
				clear();
				return (-1);
			}
		}

		// residue backend settings
		residues = opb.read(6) + 1;

		if (residueType == null || residueType.length != residues) {
			residueType = new int[residues];
		}

		if (residueParam == null || residueParam.length != residues) {
			residueParam = new Object[residues];
		}

		for (int i = 0; i < residues; i++) {
			residueType[i] = opb.read(16);
			if (residueType[i] < 0 || residueType[i] >= VI_RESB) {
				clear();
				return (-1);
			}
			residueParam[i] = FuncResidue.residueP[residueType[i]].unpack(
					this, opb);
			if (residueParam[i] == null) {
				clear();
				return (-1);
			}
		}

		// map backend settings
		maps = opb.read(6) + 1;
		if (mapType == null || mapType.length != maps) {
			mapType = new int[maps];
		}
		if (mapParam == null || mapParam.length != maps) {
			mapParam = new Object[maps];
		}
		for (int i = 0; i < maps; i++) {
			mapType[i] = opb.read(16);
			if (mapType[i] < 0 || mapType[i] >= VI_MAPB) {
				clear();
				return (-1);
			}
			mapParam[i] = FuncMapping.mappingP[mapType[i]].unpack(this, opb);
			if (mapParam[i] == null) {
				clear();
				return (-1);
			}
		}

		// mode settings
		modes = opb.read(6) + 1;
		if (modeParam == null || modeParam.length != modes) {
			modeParam = new InfoMode[modes];
		}
		for (int i = 0; i < modes; i++) {
			modeParam[i] = new InfoMode();
			modeParam[i].blockflag = opb.read(1);
			modeParam[i].windowtype = opb.read(16);
			modeParam[i].transformtype = opb.read(16);
			modeParam[i].mapping = opb.read(8);

			if ((modeParam[i].windowtype >= VI_WINDOWB)
					|| (modeParam[i].transformtype >= VI_WINDOWB)
					|| (modeParam[i].mapping >= maps)) {
				clear();
				return (-1);
			}
		}

		if (opb.read(1) != 1) {
			clear();
			return (-1);
		}

		return (0);
	}
	/**
	 * The Vorbis header is in three packets; the initial small packet in
	 * the first page that identifies basic parameters, a second packet
	 * with bitstream comments and a third packet that holds the codebook.
	 * @param vc comment
	 * @param op packet
	 * @return success
	 */
	public int synthesisHeaderin(Comment vc, Packet op) {
		Buffer opb = new Buffer();

		if (op != null) {
			opb.readinit(op.packetBase, op.packet, op.bytes);

			// Which of the three types of header is this?
			// Also verify header-ness, vorbis
			byte[] buffer = new byte[6];
			int packtype = opb.read(8);
			opb.read(buffer, 6);
			if (buffer[0] != 'v' || buffer[1] != 'o' || buffer[2] != 'r'
					|| buffer[3] != 'b' || buffer[4] != 'i'
					|| buffer[5] != 's') {
				// not a vorbis header
				return (-1);
			}
			switch (packtype) {
			case 0x01: // least significant *bit* is read first
				if (op.beginOfStream == 0) {
					// Not the initial packet
					return (-1);
				}
				if (rate != 0) {
					// previously initialized info header
					return (-1);
				}
				return (unpackInfo(opb));
			case 0x03: // least significant *bit* is read first
				if (rate == 0) {
					// um... we didn't get the initial header
					return (-1);
				}
				return (vc.unpack(opb));
			case 0x05: // least significant *bit* is read first
				if (rate == 0 || vc.vendor == null) {
					// um... we didn;t get the initial header or comments
					// yet
					return (-1);
				}
				return (unpackBooks(opb));
			default:
				// Not a valid vorbis header type
				// return(-1);
				break;
			}
		}
		return (-1);
	}
	/**
	 * Pack info.
	 * @param opb buffer
	 * @return int
	 */
	int packInfo(Buffer opb) {
		// preamble
		opb.write(0x01, 8);
		opb.write(VORBIS_BYTES);

		// basic information about the stream
		opb.write(0x00, 32);
		opb.write(channels, 8);
		opb.write(rate, 32);

		opb.write(bitrateUpper, 32);
		opb.write(bitrateNominal, 32);
		opb.write(bitrateLower, 32);

		opb.write(Util.ilog2(blocksizes[0]), 4);
		opb.write(Util.ilog2(blocksizes[1]), 4);
		opb.write(1, 1);
		return (0);
	}
	/**
	 * Pack books.
	 * @param opb buffer
	 * @return int
	 */
	int packBooks(Buffer opb) {
		opb.write(0x05, 8);
		opb.write(VORBIS_BYTES);

		// books
		opb.write(books - 1, 8);
		for (int i = 0; i < books; i++) {
			if (bookParam[i].pack(opb) != 0) {
				// goto err_out;
				return (-1);
			}
		}

		// times
		opb.write(times - 1, 6);
		for (int i = 0; i < times; i++) {
			opb.write(timeType[i], 16);
			FuncTime.timeP[timeType[i]].pack(this.timeParam[i], opb);
		}

		// floors
		opb.write(floors - 1, 6);
		for (int i = 0; i < floors; i++) {
			opb.write(floorType[i], 16);
			FuncFloor.floorP[floorType[i]].pack(floorParam[i], opb);
		}

		// residues
		opb.write(residues - 1, 6);
		for (int i = 0; i < residues; i++) {
			opb.write(residueType[i], 16);
			FuncResidue.residueP[residueType[i]].pack(residueParam[i], opb);
		}

		// maps
		opb.write(maps - 1, 6);
		for (int i = 0; i < maps; i++) {
			opb.write(mapType[i], 16);
			FuncMapping.mappingP[mapType[i]].pack(this, mapParam[i], opb);
		}

		// modes
		opb.write(modes - 1, 6);
		for (int i = 0; i < modes; i++) {
			opb.write(modeParam[i].blockflag, 1);
			opb.write(modeParam[i].windowtype, 16);
			opb.write(modeParam[i].transformtype, 16);
			opb.write(modeParam[i].mapping, 8);
		}
		opb.write(1, 1);
		return (0);
	}
	/**
	 * Returns the block size.
	 * @param op packet
	 * @return int
	 */
	public int blocksize(Packet op) {
		// codec_setup_info
		Buffer opb = new Buffer();

		int mode;

		opb.readinit(op.packetBase, op.packet, op.bytes);

		/* Check the packet type */
		if (opb.read(1) != 0) {
			/* Oops. This is not an audio data packet */
			return (OV_ENOTAUDIO);
		}
		int modebits = 0;
		int v = modes;
		while (v > 1) {
			modebits++;
			v >>>= 1;
		}

		/* read our mode and pre/post windowsize */
		mode = opb.read(modebits);
		if (mode == -1) {
			return (OV_EBADPACKET);
		}
		return (blocksizes[modeParam[mode].blockflag]);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "version:" + version + ", channels:"
				+ channels + ", rate:" + rate
				+ ", bitrate:" + bitrateUpper + ","
				+ bitrateNominal + ","
				+ bitrateLower;
	}
}
