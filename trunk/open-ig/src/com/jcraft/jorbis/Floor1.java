/*
 * Copyright 2008-2013, David Karnok 
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
 * Floor one.
 * Comments and style correction by karnokd.
 * @author ymnk
 */
class Floor1 extends FuncFloor {
	/** Floor range db. */
	static final int FLOOR1_RANGE_DB = 140;
	/** VIF position. */
	static final int VIF_POSIT = 63;
	/**
	 * {@inheritDoc}
	 */
	@Override
	void pack(Object i, Buffer opb) {
		InfoFloor1 info = (InfoFloor1) i;

		int count = 0;
		int rangebits;
		int maxposit = info.postlist[1];
		int maxclass = -1;

		/* save out partitions */
		opb.write(info.partitions, 5); /* only 0 to 31 legal */
		for (int j = 0; j < info.partitions; j++) {
			opb.write(info.partitionclass[j], 4); /* only 0 to 15 legal */
			if (maxclass < info.partitionclass[j]) {
				maxclass = info.partitionclass[j];
			}
		}

		/* save out partition classes */
		for (int j = 0; j < maxclass + 1; j++) {
			opb.write(info.classDim[j] - 1, 3); /* 1 to 8 */
			opb.write(info.classSubs[j], 2); /* 0 to 3 */
			if (info.classSubs[j] != 0) {
				opb.write(info.classBook[j], 8);
			}
			for (int k = 0; k < (1 << info.classSubs[j]); k++) {
				opb.write(info.classSubbook[j][k] + 1, 8);
			}
		}

		/* save out the post list */
		opb.write(info.mult - 1, 2); /* only 1,2,3,4 legal now */
		opb.write(Util.ilog2(maxposit), 4);
		rangebits = Util.ilog2(maxposit);

		for (int j = 0, k = 0; j < info.partitions; j++) {
			count += info.classDim[info.partitionclass[j]];
			for (; k < count; k++) {
				opb.write(info.postlist[k + 2], rangebits);
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	Object unpack(Info vi, Buffer opb) {
		int count = 0, maxclass = -1, rangebits;
		InfoFloor1 info = new InfoFloor1();

		/* read partitions */
		info.partitions = opb.read(5); /* only 0 to 31 legal */
		for (int j = 0; j < info.partitions; j++) {
			info.partitionclass[j] = opb.read(4); /* only 0 to 15 legal */
			if (maxclass < info.partitionclass[j]) {
				maxclass = info.partitionclass[j];
			}
		}

		/* read partition classes */
		for (int j = 0; j < maxclass + 1; j++) {
			info.classDim[j] = opb.read(3) + 1; /* 1 to 8 */
			info.classSubs[j] = opb.read(2); /* 0,1,2,3 bits */
			if (info.classSubs[j] < 0) {
				info.free();
				return (null);
			}
			if (info.classSubs[j] != 0) {
				info.classBook[j] = opb.read(8);
			}
			if (info.classBook[j] < 0 || info.classBook[j] >= vi.books) {
				info.free();
				return (null);
			}
			for (int k = 0; k < (1 << info.classSubs[j]); k++) {
				info.classSubbook[j][k] = opb.read(8) - 1;
				if (info.classSubbook[j][k] < -1
						|| info.classSubbook[j][k] >= vi.books) {
					info.free();
					return (null);
				}
			}
		}

		/* read the post list */
		info.mult = opb.read(2) + 1; /* only 1,2,3,4 legal now */
		rangebits = opb.read(4);

		for (int j = 0, k = 0; j < info.partitions; j++) {
			count += info.classDim[info.partitionclass[j]];
			for (; k < count; k++) {
				int x = opb.read(rangebits);
				info.postlist[k + 2] = x;
				int t = x;
				if (t < 0 || t >= (1 << rangebits)) {
					info.free();
					return (null);
				}
			}
		}
		info.postlist[0] = 0;
		info.postlist[1] = 1 << rangebits;

		return (info);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	Object look(DspState vd, InfoMode mi, Object i) {
		int ln = 0;

		int[] sortpointer = new int[VIF_POSIT + 2];

		// Info vi=vd.vi;

		InfoFloor1 info = (InfoFloor1) i;
		LookFloor1 look = new LookFloor1();
		look.vi = info;
		look.n = info.postlist[1];

		/*
		 * we drop each position value in-between already decoded values, and
		 * use linear interpolation to predict each new value past the edges.
		 * The positions are read in the order of the position list... we
		 * precompute the bounding positions in the lookup. Of course, the
		 * neighbors can change (if a position is declined), but this is an
		 * initial mapping
		 */

		for (int j = 0; j < info.partitions; j++) {
			ln += info.classDim[info.partitionclass[j]];
		}
		ln += 2;
		look.posts = ln;

		/* also store a sorted position index */
		for (int j = 0; j < ln; j++) {
			sortpointer[j] = j;
		}
		// qsort(sortpointer,n,sizeof(int),icomp); // !!

		int foo;
		for (int j = 0; j < ln - 1; j++) {
			for (int k = j; k < ln; k++) {
				if (info.postlist[sortpointer[j]] > info.postlist[sortpointer[k]]) {
					foo = sortpointer[k];
					sortpointer[k] = sortpointer[j];
					sortpointer[j] = foo;
				}
			}
		}

		/* points from sort order back to range number */
		for (int j = 0; j < ln; j++) {
			look.forwardIndex[j] = sortpointer[j];
		}
		/* points from range order to sorted position */
		for (int j = 0; j < ln; j++) {
			look.reverseIndex[look.forwardIndex[j]] = j;
		}
		/* we actually need the post values too */
		for (int j = 0; j < ln; j++) {
			look.sortedIndex[j] = info.postlist[look.forwardIndex[j]];
		}

		/* quantize values to multiplier spec */
		switch (info.mult) {
		case 1: /* 1024 -> 256 */
			look.quantQ = 256;
			break;
		case 2: /* 1024 -> 128 */
			look.quantQ = 128;
			break;
		case 3: /* 1024 -> 86 */
			look.quantQ = 86;
			break;
		case 4: /* 1024 -> 64 */
			look.quantQ = 64;
			break;
		default:
			look.quantQ = -1;
		}

		/*
		 * discover our neighbors for decode where we don't use fit flags (that
		 * would push the neighbors outward)
		 */
		for (int j = 0; j < ln - 2; j++) {
			int lo = 0;
			int hi = 1;
			int lx = 0;
			int hx = look.n;
			int currentx = info.postlist[j + 2];
			for (int k = 0; k < j + 2; k++) {
				int x = info.postlist[k];
				if (x > lx && x < currentx) {
					lo = k;
					lx = x;
				}
				if (x < hx && x > currentx) {
					hi = k;
					hx = x;
				}
			}
			look.loneighbor[j] = lo;
			look.hineighbor[j] = hi;
		}

		return look;
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
	void freeState(Object vs) {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	int forward(Block vb, Object i, float[] in, float[] out, Object vs) {
		return 0;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	Object inverse1(Block vb, Object ii, Object memo) {
		LookFloor1 look = (LookFloor1) ii;
		InfoFloor1 info = look.vi;
		CodeBook[] books = vb.vd.fullbooks;

		/* unpack wrapped/predicted values from stream */
		if (vb.opb.read(1) == 1) {
			int[] fitValue = null;
			if (memo instanceof int[]) {
				fitValue = (int[]) memo;
			}
			if (fitValue == null || fitValue.length < look.posts) {
				fitValue = new int[look.posts];
			} else {
				for (int i = 0; i < fitValue.length; i++) {
					fitValue[i] = 0;
				}
			}

			fitValue[0] = vb.opb.read(Util.ilog(look.quantQ - 1));
			fitValue[1] = vb.opb.read(Util.ilog(look.quantQ - 1));

			/* partition by partition */
			for (int i = 0, j = 2; i < info.partitions; i++) {
				int clss = info.partitionclass[i];
				int cdim = info.classDim[clss];
				int csubbits = info.classSubs[clss];
				int csub = 1 << csubbits;
				int cval = 0;

				/* decode the partition's first stage cascade value */
				if (csubbits != 0) {
					cval = books[info.classBook[clss]].decode(vb.opb);

					if (cval == -1) {
						return (null);
					}
				}

				for (int k = 0; k < cdim; k++) {
					int book = info.classSubbook[clss][cval & (csub - 1)];
					cval >>>= csubbits;
					if (book >= 0) {
						int fv = books[book].decode(vb.opb);
						fitValue[j + k] = fv;
						if (fv == -1) {
							return (null);
						}
					} else {
						fitValue[j + k] = 0;
					}
				}
				j += cdim;
			}

			/* unwrap positive values and reconsitute via linear interpolation */
			for (int i = 2; i < look.posts; i++) {
				int predicted = renderPoint(
						info.postlist[look.loneighbor[i - 2]],
						info.postlist[look.hineighbor[i - 2]],
						fitValue[look.loneighbor[i - 2]],
						fitValue[look.hineighbor[i - 2]], info.postlist[i]);
				int hiroom = look.quantQ - predicted;
				int loroom = predicted;
				int room = (hiroom < loroom ? hiroom : loroom) << 1;
				int val = fitValue[i];

				if (val != 0) {
					if (val >= room) {
						if (hiroom > loroom) {
							val = val - loroom;
						} else {
							val = -1 - (val - hiroom);
						}
					} else {
						if ((val & 1) != 0) {
							val = -((val + 1) >>> 1);
						} else {
							val >>= 1;
						}
					}

					fitValue[i] = val + predicted;
					fitValue[look.loneighbor[i - 2]] &= 0x7fff;
					fitValue[look.hineighbor[i - 2]] &= 0x7fff;
				} else {
					fitValue[i] = predicted | 0x8000;
				}
			}
			return (fitValue);
		}

		return (null);
	}
	/**
	 * Render a point.
	 * @param x0 x
	 * @param x1 x2
	 * @param y0 y
	 * @param y1 y2
	 * @param x value
	 * @return value
	 */
	private static int renderPoint(int x0, int x1, int y0, int y1, int x) {
		y0 &= 0x7fff; /* mask off flag */
		y1 &= 0x7fff;

		int dy = y1 - y0;
		int adx = x1 - x0;
		int ady = Math.abs(dy);
		int err = ady * (x - x0);

		int off = (err / adx);
		if (dy < 0) {
			return (y0 - off);
		}
		return (y0 + off);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	int inverse2(Block vb, Object i, Object memo, float[] out) {
		LookFloor1 look = (LookFloor1) i;
		InfoFloor1 info = look.vi;
		int n = vb.vd.vi.blocksizes[vb.mode] / 2;

		if (memo != null) {
			/* render the lines */
			int[] fitValue = (int[])memo;
			int hx = 0;
			int lx = 0;
			int ly = fitValue[0] * info.mult;
			for (int j = 1; j < look.posts; j++) {
				int current = look.forwardIndex[j];
				int hy = fitValue[current] & 0x7fff;
				if (hy == fitValue[current]) {
					hy *= info.mult;
					hx = info.postlist[current];

					renderLine(lx, hx, ly, hy, out);

					lx = hx;
					ly = hy;
				}
			}
			for (int j = hx; j < n; j++) {
				out[j] *= out[j - 1]; /* be certain */
			}
			return (1);
		}
		for (int j = 0; j < n; j++) {
			out[j] = 0.f;
		}
		return (0);
	}
	/**
	 * Floor from decibel lookup table.
	 */
	private static final float[] FLOOR_FROMDB_LOOKUP = { 1.0649863e-07F,
			1.1341951e-07F, 1.2079015e-07F, 1.2863978e-07F, 1.3699951e-07F,
			1.4590251e-07F, 1.5538408e-07F, 1.6548181e-07F, 1.7623575e-07F,
			1.8768855e-07F, 1.9988561e-07F, 2.128753e-07F, 2.2670913e-07F,
			2.4144197e-07F, 2.5713223e-07F, 2.7384213e-07F, 2.9163793e-07F,
			3.1059021e-07F, 3.3077411e-07F, 3.5226968e-07F, 3.7516214e-07F,
			3.9954229e-07F, 4.2550680e-07F, 4.5315863e-07F, 4.8260743e-07F,
			5.1396998e-07F, 5.4737065e-07F, 5.8294187e-07F, 6.2082472e-07F,
			6.6116941e-07F, 7.0413592e-07F, 7.4989464e-07F, 7.9862701e-07F,
			8.5052630e-07F, 9.0579828e-07F, 9.6466216e-07F, 1.0273513e-06F,
			1.0941144e-06F, 1.1652161e-06F, 1.2409384e-06F, 1.3215816e-06F,
			1.4074654e-06F, 1.4989305e-06F, 1.5963394e-06F, 1.7000785e-06F,
			1.8105592e-06F, 1.9282195e-06F, 2.0535261e-06F, 2.1869758e-06F,
			2.3290978e-06F, 2.4804557e-06F, 2.6416497e-06F, 2.8133190e-06F,
			2.9961443e-06F, 3.1908506e-06F, 3.3982101e-06F, 3.6190449e-06F,
			3.8542308e-06F, 4.1047004e-06F, 4.3714470e-06F, 4.6555282e-06F,
			4.9580707e-06F, 5.2802740e-06F, 5.6234160e-06F, 5.9888572e-06F,
			6.3780469e-06F, 6.7925283e-06F, 7.2339451e-06F, 7.7040476e-06F,
			8.2047000e-06F, 8.7378876e-06F, 9.3057248e-06F, 9.9104632e-06F,
			1.0554501e-05F, 1.1240392e-05F, 1.1970856e-05F, 1.2748789e-05F,
			1.3577278e-05F, 1.4459606e-05F, 1.5399272e-05F, 1.6400004e-05F,
			1.7465768e-05F, 1.8600792e-05F, 1.9809576e-05F, 2.1096914e-05F,
			2.2467911e-05F, 2.3928002e-05F, 2.5482978e-05F, 2.7139006e-05F,
			2.8902651e-05F, 3.0780908e-05F, 3.2781225e-05F, 3.4911534e-05F,
			3.7180282e-05F, 3.9596466e-05F, 4.2169667e-05F, 4.4910090e-05F,
			4.7828601e-05F, 5.0936773e-05F, 5.4246931e-05F, 5.7772202e-05F,
			6.1526565e-05F, 6.5524908e-05F, 6.9783085e-05F, 7.4317983e-05F,
			7.9147585e-05F, 8.4291040e-05F, 8.9768747e-05F, 9.5602426e-05F,
			0.00010181521F, 0.00010843174F, 0.00011547824F, 0.00012298267F,
			0.00013097477F, 0.00013948625F, 0.00014855085F, 0.00015820453F,
			0.00016848555F, 0.00017943469F, 0.00019109536F, 0.00020351382F,
			0.00021673929F, 0.00023082423F, 0.00024582449F, 0.00026179955F,
			0.00027881276F, 0.00029693158F, 0.00031622787F, 0.00033677814F,
			0.00035866388F, 0.00038197188F, 0.00040679456F, 0.00043323036F,
			0.00046138411F, 0.00049136745F, 0.00052329927F, 0.00055730621F,
			0.00059352311F, 0.00063209358F, 0.00067317058F, 0.00071691700F,
			0.00076350630F, 0.00081312324F, 0.00086596457F, 0.00092223983F,
			0.00098217216F, 0.0010459992F, 0.0011139742F, 0.0011863665F,
			0.0012634633F, 0.0013455702F, 0.0014330129F, 0.0015261382F,
			0.0016253153F, 0.0017309374F, 0.0018434235F, 0.0019632195F,
			0.0020908006F, 0.0022266726F, 0.0023713743F, 0.0025254795F,
			0.0026895994F, 0.0028643847F, 0.0030505286F, 0.0032487691F,
			0.0034598925F, 0.0036847358F, 0.0039241906F, 0.0041792066F,
			0.0044507950F, 0.0047400328F, 0.0050480668F, 0.0053761186F,
			0.0057254891F, 0.0060975636F, 0.0064938176F, 0.0069158225F,
			0.0073652516F, 0.0078438871F, 0.0083536271F, 0.0088964928F,
			0.009474637F, 0.010090352F, 0.010746080F, 0.011444421F,
			0.012188144F, 0.012980198F, 0.013823725F, 0.014722068F,
			0.015678791F, 0.016697687F, 0.017782797F, 0.018938423F,
			0.020169149F, 0.021479854F, 0.022875735F, 0.024362330F,
			0.025945531F, 0.027631618F, 0.029427276F, 0.031339626F,
			0.033376252F, 0.035545228F, 0.037855157F, 0.040315199F,
			0.042935108F, 0.045725273F, 0.048696758F, 0.051861348F,
			0.055231591F, 0.058820850F, 0.062643361F, 0.066714279F,
			0.071049749F, 0.075666962F, 0.080584227F, 0.085821044F,
			0.091398179F, 0.097337747F, 0.10366330F, 0.11039993F, 0.11757434F,
			0.12521498F, 0.13335215F, 0.14201813F, 0.15124727F, 0.16107617F,
			0.17154380F, 0.18269168F, 0.19456402F, 0.20720788F, 0.22067342F,
			0.23501402F, 0.25028656F, 0.26655159F, 0.28387361F, 0.30232132F,
			0.32196786F, 0.34289114F, 0.36517414F, 0.38890521F, 0.41417847F,
			0.44109412F, 0.46975890F, 0.50028648F, 0.53279791F, 0.56742212F,
			0.60429640F, 0.64356699F, 0.68538959F, 0.72993007F, 0.77736504F,
			0.82788260F, 0.88168307F, 0.9389798F, 1.F };
	/**
	 * Render line.
	 * @param x0 x
	 * @param x1 x2
	 * @param y0 y 
	 * @param y1 y2
	 * @param d float array
	 */
	private static void renderLine(int x0, int x1, int y0, int y1, float[] d) {
		int dy = y1 - y0;
		int adx = x1 - x0;
		int ady = Math.abs(dy);
		int base = dy / adx;
		int sy = (dy < 0 ? base - 1 : base + 1);
		int x = x0;
		int y = y0;
		int err = 0;

		ady -= Math.abs(base * adx);

		d[x] *= FLOOR_FROMDB_LOOKUP[y];
		while (++x < x1) {
			err = err + ady;
			if (err >= adx) {
				err -= adx;
				y += sy;
			} else {
				y += base;
			}
			d[x] *= FLOOR_FROMDB_LOOKUP[y];
		}
	}
	/**
	 * Info floor 1.
	 * Comments and style correction by karnokd.
	 * @author ymnk
	 */
	class InfoFloor1 {
		/** VIF position. */
		static final int VIF_POSIT = 63;
		/** VIF class. */
		static final int VIF_CLASS = 16;
		/** VIF parts. */
		static final int VIF_PARTS = 31;
		/** Partitions. */
		int partitions; /* 0 to 31 */
		/** Partition class. 0 to 15 */
		int[] partitionclass = new int[VIF_PARTS];
		/** Class dim. 1 to 8 */
		int[] classDim = new int[VIF_CLASS];
		/** Class subs. 0,1,2,3 (bits: 1&lt;&lt;n poss). */
		int[] classSubs = new int[VIF_CLASS];
		/** Subs and dim entries. */
		int[] classBook = new int[VIF_CLASS];
		/** [VIF_CLASS][subs]. */
		int[][] classSubbook = new int[VIF_CLASS][]; 
		/** 1 2 3 or 4. */
		int mult;
		/** First two implicit. */
		int[] postlist = new int[VIF_POSIT + 2]; 

		/** Encode side analysis parameters. */
		float maxover;
		/** Max under. */
		float maxunder;
		/** Max error. */
		float maxerr;
		/** Two fit min size. */
		int twofitminsize;
		/** Two fit min used. */
		int twofitminused;
		/** Two fit weight. */
		int twofitweight;
		/** Two fit at ten. */
		float twofitatten;
		/** Unused min size. */
		int unusedminsize;
		/** Unused min number. */
		int unusedminN;
		/** Number. */
		int n;
		/** Constructor. */
		InfoFloor1() {
			for (int i = 0; i < classSubbook.length; i++) {
				classSubbook[i] = new int[8];
			}
		}
		/**
		 * Free objects.
		 */
		void free() {
			partitionclass = null;
			classDim = null;
			classSubs = null;
			classBook = null;
			classSubbook = null;
			postlist = null;
		}
		/**
		 * Copy info.
		 * @return the new copy
		 */
		Object copyInfo() {
			InfoFloor1 info = this;
			InfoFloor1 ret = new InfoFloor1();

			ret.partitions = info.partitions;
			System.arraycopy(info.partitionclass, 0, ret.partitionclass, 0,
					VIF_PARTS);
			System.arraycopy(info.classDim, 0, ret.classDim, 0, VIF_CLASS);
			System.arraycopy(info.classSubs, 0, ret.classSubs, 0, VIF_CLASS);
			System.arraycopy(info.classBook, 0, ret.classBook, 0, VIF_CLASS);

			for (int j = 0; j < VIF_CLASS; j++) {
				System.arraycopy(info.classSubbook[j], 0,
						ret.classSubbook[j], 0, 8);
			}

			ret.mult = info.mult;
			System.arraycopy(info.postlist, 0, ret.postlist, 0, VIF_POSIT + 2);

			ret.maxover = info.maxover;
			ret.maxunder = info.maxunder;
			ret.maxerr = info.maxerr;

			ret.twofitminsize = info.twofitminsize;
			ret.twofitminused = info.twofitminused;
			ret.twofitweight = info.twofitweight;
			ret.twofitatten = info.twofitatten;
			ret.unusedminsize = info.unusedminsize;
			ret.unusedminN = info.unusedminN;

			ret.n = info.n;

			return (ret);
		}

	}
	/**
	 * Look floor 1.
	 * Comments and style correction by karnokd.
	 * @author ymnk
	 */
	static class LookFloor1 {
		/** VIF position. */
		static final int VIF_POSIT = 63;
		/** Sorted index. */
		int[] sortedIndex = new int[VIF_POSIT + 2];
		/** Forward index. */
		int[] forwardIndex = new int[VIF_POSIT + 2];
		/** Reverse index. */
		int[] reverseIndex = new int[VIF_POSIT + 2];
		/** Highest neighbor. */
		int[] hineighbor = new int[VIF_POSIT];
		/** Lowest neighbor. */
		int[] loneighbor = new int[VIF_POSIT];
		/** Posts. */
		int posts;
		/** Number. */
		int n;
		/** Quantization quality. */
		int quantQ;
		/** Info floor 1. */
		InfoFloor1 vi;
		/** Phase bits. */
//		int phrasebits;
		/** Post bits. */
//		int postbits;
		/** Frames. */
//		int frames;
		/** Free objects. */
		void free() {
			sortedIndex = null;
			forwardIndex = null;
			reverseIndex = null;
			hineighbor = null;
			loneighbor = null;
		}
	}
	/**
	 * LS fit acc.
	 * Comments and style correction by karnokd.
	 * @author ymnk
	 */
	static class LsfitAcc {
		/** X0. */
//		long x0;
		/** X1. */
//		long x1;
		/** XA. */
//		long xa;
		/** YA. */
//		long ya;
		/** X2A. */
//		long x2a;
		/** Y2A. */
//		long y2a;
		/** XYA. */
//		long xya;
		/** Number. */
//		long n;
		/** AN. */
//		long an;
		/** UN. */
//		long un;
		/** Edge y0. */
//		long edgey0;
		/** Edge y1. */
//		long edgey1;
	}
	/**
	 * Ech state floor 1.
	 * Comments and style correction by karnokd.
	 * @author ymnk.
	 */
	static class EchstateFloor1 {
		/** Code workds. */
//		int[] codewords;
		/** Curve. */
//		float[] curve;
		/** Frame number. */
//		long frameno;
		/** Codes. */
//		long codes;
	}
}
