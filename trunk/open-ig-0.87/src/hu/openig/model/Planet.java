/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.PlanetType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A planet.
 * @author akarnokd, 2010.01.07.
 */
public class Planet implements Named, Owned {
	/** The planet's identifier. */
	public String id;
	/** The planet's display name. */
	public String name;
	/** The X coordinate on the unscaled starmap. */
	public int x;
	/** The Y coordinate on the unscaled starmap. */
	public int y;
	/** The planet's type. */
	public PlanetType type;
	/** The owner. */
	public Player owner;
	/** The inhabitant race. */
	public String race;
	/** The current population. */
	public int population;
	/** The population change since the last day. */
	public int lastPopulation;
	/** The rendered rotation phase. */
	public int rotationPhase;
	/** The rotation direction. */
	public RotationDirection rotationDirection;
	/** The radar radius. */
	public int radar;
	/** The diameter in pixels up to 30 for the maximum zoom. */
	public int diameter;
	/** The planet is under quarantine: display red frame. */
	public boolean quarantine;
	/** The contents of the planet. */
	public PlanetSurface surface;
	/** The resource allocation strategy. */
	public ResourceAllocationStrategy allocation = ResourceAllocationStrategy.DEFAULT;
	/** The taxation level. */
	public TaxLevel tax = TaxLevel.NORMAL;
	/** The morale percent in hundreds. */
	public int morale = 50;
	/** The last day's morale percent in hundreds. */
	public int lastMorale = 50;
	/** The auto build mode. */
	public AutoBuild autoBuild = AutoBuild.OFF;
	/** The last day's tax income. */
	public int taxIncome;
	/** The last day's trade income. */
	public int tradeIncome;
	/** The planet's inventory. */
	public final List<PlanetInventoryItem> inventory = new ArrayList<PlanetInventoryItem>();
	/** @return the morale label for the current morale level. */
	public String getMoraleLabel() {
		return getMoraleLabel(morale);
	}
	/**
	 * Return the morale label for the given level.
	 * @param morale the morale 0..100%
	 * @return the label
	 */
	public static String getMoraleLabel(int morale) {
		if (morale < 5) {
			return "morale.revolt";
		}
		if (morale < 20) {
			return "morale.hate";
		}
		if (morale < 40) {
			return "morale.dislike";
		}
		if (morale < 60) {
			return "morale.neutral";
		}
		if (morale < 80) {
			return "morale.like";
		}
		return "morale.supportive";
	}
	/** @return the tax label. */
	public String getTaxLabel() {
		return "taxlevel." + tax;
	}
	/** @return the race label. */
	public String getRaceLabel() {
		return "race." + race;
	}
	/** @return The auto-build label. */
	public String getAutoBuildLabel() {
		return "autobuild." + autoBuild;
	}
	/** @return the allocation label. */
	public String getAllocationLabel() {
		return "allocation." + allocation;
	}
	/**
	 * Compute the planetary statistics.
	 * @return the statistics
	 */
	public PlanetStatistics getStatistics() {
		PlanetStatistics result = new PlanetStatistics();
		radar = 0;
		int stadiumCount = 0;
		boolean damage = false;
		for (Building b : surface.buildings) {
			if (b.getEfficiency() >= 0.5) {
				if (b.hasResource("house")) {
					result.houseAvailable += b.getResource("house");
				}
				if (b.hasResource("food")) {
					result.foodAvailable += b.getResource("food");
				}
				if (b.hasResource("police")) {
					result.policeAvailable += b.getResource("police");
				}
				if (b.hasResource("hospital")) {
					result.hospitalAvailable += b.getResource("hospital");
				}
				if (b.hasResource("spaceship")) {
					result.spaceshipActive += b.getResource("spaceship");
				}
				if (b.hasResource("equipment")) {
					result.equipmentActive += b.getResource("equipment");
				}
				if (b.hasResource("weapon")) {
					result.weaponsActive += b.getResource("weapon");
				}
				if (b.hasResource("civil")) {
					result.civilLabActive += b.getResource("civil");
				}
				if (b.hasResource("mechanical")) {
					result.mechLabActive += b.getResource("mechanical");
				}
				if (b.hasResource("computer")) {
					result.compLabActive += b.getResource("computer");
				}
				if (b.hasResource("ai")) {
					result.aiLabActive += b.getResource("ai");
				}
				if (b.hasResource("military")) {
					result.milLabActive += b.getResource("military");
				}
				if (b.hasResource("radar")) {
					radar = Math.max(radar, (int)b.getResource("radar"));
				}
				if (b.type.id.equals("Stadium")) {
					stadiumCount++;
				}
			}
			if (b.hasResource("spaceship")) {
				result.spaceship += b.getResource("spaceship");
			}
			if (b.hasResource("equipment")) {
				result.equipment += b.getResource("equipment");
			}
			if (b.hasResource("weapon")) {
				result.weapons += b.getResource("weapon");
			}
			if (b.hasResource("civil")) {
				result.civilLab += b.getResource("civil");
			}
			if (b.hasResource("mechanical")) {
				result.mechLab += b.getResource("mechanical");
			}
			if (b.hasResource("computer")) {
				result.compLab += b.getResource("computer");
			}
			if (b.hasResource("ai")) {
				result.aiLab += b.getResource("ai");
			}
			if (b.hasResource("military")) {
				result.milLab += b.getResource("military");
			}
			if (b.isReady()) {
				result.workerDemand += Math.abs(b.getWorkers());
				int e = b.getEnergy();
				if (e < 0) {
					result.energyDemand += -e;
				} else {
					result.energyAvailable += e * b.getEfficiency();
				}
			}
			damage |= b.hitpoints < b.type.hitpoints;
		}
		
		if (quarantine) {
			result.hospitalAvailable /= 4;
		}
		
		result.problems.clear();
		if (Math.abs(result.workerDemand) > population * 2) {
			result.problems.add(PlanetProblems.WORKFORCE);
		}
		if (Math.abs(result.energyDemand) > Math.abs(result.energyAvailable) * 2) {
			result.problems.add(PlanetProblems.ENERGY);
		}
		if (Math.abs(population) > Math.abs(result.foodAvailable) * 2) {
			result.problems.add(PlanetProblems.FOOD);
		}
		if (Math.abs(population) > Math.abs(result.hospitalAvailable) * 2) {
			result.problems.add(PlanetProblems.HOSPITAL);
		}
		if (Math.abs(population) > Math.abs(result.houseAvailable) * 2) {
			result.problems.add(PlanetProblems.HOUSING);
		}
		if (population / 50000 > stadiumCount) {
			result.problems.add(PlanetProblems.STADIUM);
		}
		if (quarantine) {
			result.problems.add(PlanetProblems.VIRUS);
		}
		if (damage) {
			result.problems.add(PlanetProblems.REPAIR);
		}
		
		for (PlanetInventoryItem pii : inventory) {
			if (pii.owner == owner && pii.type.get("radar") != null) {
				radar = Math.max(radar, Integer.parseInt(pii.type.get("radar")));
			}
		}
		
		radar *= 35;
		
		return result;
	}
	/** @return true if the planet is populated */
	public boolean isPopulated() {
		return race != null && !race.isEmpty();
	}
	/**
	 * Test if another instance of the building type can be built on this planet.
	 * It checks for the building limits and surface type.
	 * @param bt the building type to test
	 * @return can be built here?
	 */
	public boolean canBuild(BuildingType bt) {
		if (bt.except.contains(type.type)) {
			return false;
		}
		boolean hubFound = false;
		int count = 0;
		for (Building b : surface.buildings) {
			if ("MainBuilding".equals(b.type.kind) && b.isComplete()) {
				hubFound = true;
			}
			if ((bt.limit < 0 && b.type.kind.equals(bt.kind))
					|| (bt.limit > 0 && b.type == bt)
			) {
					count++;
			}
		}
		return (hubFound != "MainBuilding".equals(bt.kind)) && count < Math.abs(bt.limit);
	}
	@Override
	public String name() {
		return name;
	}
	@Override
	public Player owner() {
		return owner;
	}
	/**
	 * @return the number of built buildings per type
	 */
	public Map<BuildingType, Integer> countBuildings() {
		Map<BuildingType, Integer> result = new HashMap<BuildingType, Integer>();
		for (Building b : surface.buildings) {
			Integer cnt = result.get(b.type);
			result.put(b.type, cnt != null ? cnt + 1 : 1);
		}
		return result;
	}
	/**
	 * Returns the invetory count of the given technology.
	 * @param rt the research technology.
	 * @return the count
	 */
	public int getInventoryCount(ResearchType rt) {
		int result = 0;
		for (PlanetInventoryItem pii : inventory) {
			if (pii.type == rt) {
				result++;
			}
		}
		return result;
	}
}