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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Generates planets for a galaxy.
 * @author akarnokd, 2014.04.01.
 */
public final class GalaxyGenerator {
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
		/**
		 * Computes the distance to another planet candidate.
		 * @param pc the another planet candidate
		 * @return the distance
		 */
		public double distanceSqr(PlanetCandidate pc) {
			return (x - pc.x) * (x - pc.x) + (y - pc.y) * (y - pc.y);
		}
	}
	/** Planet radiuses. */
	private static final int[] RADIUSES = { 18, 24, 30 };
	/** The random seed. */
	private long seed;
	/** The number of planets. */
	private int planetCount;
	/** Galaxy width in pixels. */
	private int width;
	/** Galaxy height in pixels. */
	private int height;
	/** The minimum distance between planet cells. */
	private int minDensity;
	/** The list of planet names that should appear only once. */
	private List<String> singleNames;
	/** The list of planet names that may appear multiple times. */
	private List<String> multiNames;
	/**
	 * Set the random seed value.
	 * @param seed the seed
	 */
	public void setSeed(long seed) {
		this.seed = seed;
	}
	/**
	 * Set the total planet count.
	 * @param planetCount the total planet count
	 */
	public void setPlanetCount(int planetCount) {
		this.planetCount = planetCount;
	}
	/**
	 * Set the size of the galaxy.
	 * @param width the width
	 * @param height the height
	 */
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	/**
	 * Set the minimum planet density.
	 * @param minDensity the minimum planet density
	 */
	public void setMinDensity(int minDensity) {
		this.minDensity = minDensity;
	}
	/**
	 * Set the names which should appear only once.
	 * @param singleNames the single names
	 */
	public void setSingleNames(List<String> singleNames) {
		this.singleNames = singleNames;
	}
	/**
	 * Set the names which may appear multiple times.
	 * @param multiNames the multiple names
	 */
	public void setMultiNames(List<String> multiNames) {
		this.multiNames = multiNames;
	}
	/**
	 * Generate the planets.
	 * @return the list of generated planets
	 */
	public List<PlanetCandidate> generate() {
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
		
		// Name planets
		
		List<PlanetCandidate> shuffled = new ArrayList<>(result);
		Collections.shuffle(shuffled, rnd);
		
		List<PlanetCandidate> singleNamedPlanets = new ArrayList<>();
		List<PlanetCandidate> multiNamedPlanets = new ArrayList<>();
		
		if (singleNames != null && !singleNames.isEmpty()) {
			int singleNameCount = Math.min(shuffled.size(), singleNames.size());
			List<PlanetCandidate> sl = shuffled.subList(0, singleNameCount);
			singleNamedPlanets.addAll(sl);
			sl.clear();
			
			int i = 0;
			for (PlanetCandidate pc : singleNamedPlanets) {
				pc.id = singleNames.get(i);
				pc.name = pc.id;
				i++;
			}
		}
		Map<PlanetCandidate, List<PlanetCandidate>> multiNamesMap = new LinkedHashMap<>();
		
		if (multiNames != null && !multiNames.isEmpty()) {
			int multiNameCount = Math.min(shuffled.size(), multiNames.size());
			List<PlanetCandidate> sl = shuffled.subList(0, multiNameCount);
			multiNamedPlanets.addAll(sl);
			sl.clear();
			
			for (PlanetCandidate pc : multiNamedPlanets) {
				multiNamesMap.put(pc, new ArrayList<PlanetCandidate>());
			}
		}
		
		if (!multiNamedPlanets.isEmpty()) {
			for (final PlanetCandidate pc : shuffled) {
				PlanetCandidate parent = Collections.min(multiNamedPlanets, new CandidateDistance(pc));
				multiNamesMap.get(parent).add(pc);
			}
			
			int i = 0;
			for (Map.Entry<PlanetCandidate, List<PlanetCandidate>> e : multiNamesMap.entrySet()) {
				PlanetCandidate key = e.getKey();
				String mname = this.multiNames.get(i);
				if (e.getValue().isEmpty()) {
					key.id = mname;
					key.name = key.id;
				} else {
					key.id = mname + " 1";
					key.name = key.id;
					
					Collections.sort(e.getValue(), new CandidateDistance(key));
					int j = 2;
					for (PlanetCandidate pc : e.getValue()) {
						pc.id = mname + " " + j;
						pc.name = pc.id;
						j++;
					}
				}
				i++;
			}
		}
		
		return result;
	}
	/**
	 * Comparator to sort planets by distance from a reference planet.
	 * @author akarnokd, 2014.04.11.
	 *
	 */
	static final class CandidateDistance implements Comparator<PlanetCandidate> {
		/** The reference planet. */
		private final PlanetCandidate reference;
		/**
		 * Constructor, sets the reference.
		 * @param reference the reference planet
		 */
		public CandidateDistance(PlanetCandidate reference) {
			this.reference = reference;
		}
		@Override
		public int compare(PlanetCandidate o1, PlanetCandidate o2) {
			double dist1 = reference.distanceSqr(o1);
			double dist2 = reference.distanceSqr(o2);
			return Double.compare(dist1, dist2);		
		}
	}
}