/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

/**
 * Generates planets for a galaxy.
 * @author akarnokd, 2014.04.01.
 */
public final class GalaxyGenerator {
	/** Utility class. */
	private GalaxyGenerator() { }
	/** Planet radiuses. */
	private static final int[] RADIUSES = { 18, 24, 30 };
	/**
	 * Generate a list of positions for planets.
	 * @param seed the initial random seed 
	 * @param planetCount the number of planets to generate
	 * @param width the starmap width
	 * @param height the starmap height
	 * @param minDensity the minimum distance between planets.
	 * @return the list of points.
	 */
	public static List<PlanetCandidate> generate(long seed, int planetCount, int width, int height, int minDensity) {
		Random rnd = new Random(seed);
		
		int xcells = width / minDensity;
		int ycells = height / minDensity;
		
		int xoffset = (width - xcells * minDensity) / 2;
		int yoffset = (height - ycells * minDensity) / 2;
		
		// if there isn't enough cells for the requested planet count
		planetCount = Math.min(xcells * ycells, planetCount);
		List<PlanetCandidate> result = new ArrayList<>(planetCount + 1);
		
		BitSet occupied = new BitSet(xcells * ycells);
		
		while (planetCount-- > 0) {
			int cx = rnd.nextInt(xcells);
			int cy = rnd.nextInt(ycells);
			int cr = RADIUSES[rnd.nextInt(RADIUSES.length)];

			while (occupied.get(cx + cy * ycells)) {
				cy++;
				if (cy >= ycells) {
					cy = 0;
					cx++;
				}
				if (cx >= xcells) {
					cx = 0;
				}
			}
			occupied.set(cx + cy * ycells);
			
			PlanetCandidate pc = new PlanetCandidate();
			pc.x = cx * minDensity + xoffset + rnd.nextInt(minDensity / 3) * 3;
			pc.y = cy * minDensity + yoffset + rnd.nextInt(minDensity / 3) * 3;
			pc.r = cr;
			
			pc.id = "PX " + (cx + 1) + "Y" + (cy + 1);
			pc.name = pc.id;
			
			result.add(pc);
		}
		
		return result;
	}
	/**
	 * The generated planet candidate.
	 * @author akarnokd, 2014.04.01.
	 */
	public static final class PlanetCandidate {
		/** Position X. */
		public int x;
		/** Position Y. */
		public int y;
		/** Radius. */
		public int r;
		/** The identifier. */
		public String id;
		/** The name. */
		public String name;
		/** The surface type. */
		public String type;
		/** The surface variant. */
		public int variant;
	}
}
