/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.PlanetType;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A planet.
 * @author akarnokd, 2010.01.07.
 */
public class Planet implements Named, Owned, HasInventory {
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
	/** How long the quarantine should stay in 10s ingame minutes? */
	public int quarantineTTL;
	/** The default quarnatine TTL. */
	public static final int DEFAULT_QUARANTINE_TTL = 5 * 12 * 3;
	/** The contents of the planet. */
	public PlanetSurface surface;
	/** The resource allocation strategy. */
	public ResourceAllocationStrategy allocation = ResourceAllocationStrategy.DEFAULT;
	/** The taxation level. */
	public TaxLevel tax = TaxLevel.MODERATE;
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
	public final List<InventoryItem> inventory = new ArrayList<InventoryItem>();
	/** The time to live counter for objects which need to be removed after the given simulation step (which is 10 ingame minutes. */
	public final Map<InventoryItem, Integer> timeToLive = new HashMap<InventoryItem, Integer>();
	/** The countdown for an earthquake lasting 10s of ingame minutes. */
	public int earthQuakeTTL;
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
		int radar = 0;
		int stadiumCount = 0;
		boolean buildup = false;
		boolean damage = false;
		boolean colonyHub = false;
		boolean colonyHubOperable = false;
		int nativeWorkerDemand = 0;
		
		result.vehicleMax = 8; // default per planet
		
		for (Building b : surface.buildings) {
			float eff = b.getEfficiency();
			if (b.isConstructing()) {
				result.constructing = true;
			}
			if (Building.isOperational(eff)) {
				if (b.hasResource("house")) {
					result.houseAvailable += b.getResource("house") * eff;
				}
				if (b.hasResource("food")) {
					result.foodAvailable += b.getResource("food") * eff;
				}
				if (b.hasResource("police")) {
					result.policeAvailable += b.getResource("police") * eff;
				}
				if (b.hasResource("hospital")) {
					result.hospitalAvailable += b.getResource("hospital") * eff;
				}
				if (b.hasResource("spaceship")) {
					result.spaceshipActive += b.getResource("spaceship") * eff;
				}
				if (b.hasResource("equipment")) {
					result.equipmentActive += b.getResource("equipment") * eff;
				}
				if (b.hasResource("weapon")) {
					result.weaponsActive += b.getResource("weapon") * eff;
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
				if (b.hasResource("repair")) {
					result.freeRepair = Math.max(b.getResource("repair"), result.freeRepair);
					result.freeRepairEff = Math.max(eff, result.freeRepairEff);
				}
				colonyHubOperable |= "MainBuilding".equals(b.type.kind);
				if ("TradersSpaceport".equals(b.type.id)) {
					result.hasTradersSpaceport = true;
				}
				if ("MilitarySpaceport".equals(b.type.id)) {
					result.hasMilitarySpaceport = true;
				}
				if (b.hasResource("vehicles")) {
					result.vehicleMax += b.getResource("vehicles");
				}
			}
			if ("MilitarySpaceport".equals(b.type.id)) {
				result.militarySpaceportCount = 1;
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
			float health = b.hitpoints * 1.0f / b.type.hitpoints;
			if (b.isReady()) {
				// consider the damage level
				result.workerDemand += Math.abs(b.getWorkers()) * health;
				int e = b.getEnergy();
				if (e < 0) {
					result.energyDemand += -e * health;
				} else {
					result.energyAvailable += e;
				}
			}
			nativeWorkerDemand += Math.abs(b.getWorkers()) * health;
			
			damage |= b.isDamaged();
			buildup |= b.isConstructing();
			colonyHub |= "MainBuilding".equals(b.type.kind) && !b.isConstructing();
		}
		// check if there is still a building with unallocated resources
		for (Building b : surface.buildings) {
			if (b.enabled 
					&& ((b.assignedWorker == 0 && population > 0)
							|| (result.energyAvailable > 0 && b.getEnergy() < 0 
									&& b.assignedEnergy == 0))) {
				result.constructing = true;
				break;
			}
		}
		
		if (quarantine) {
			result.hospitalAvailable /= 4;
		}
		
		result.problems.clear();
		if (Math.abs(result.workerDemand) > population * 2) {
			result.addProblem(PlanetProblems.WORKFORCE);
		} else
		if (Math.abs(result.workerDemand) > population) {
			result.addWarning(PlanetProblems.WORKFORCE);
		}
		if (nativeWorkerDemand > population) {
			result.addWarning(PlanetProblems.WORKFORCE);
		}
		if (Math.abs(result.energyDemand) > Math.abs(result.energyAvailable) * 2) {
			result.addProblem(PlanetProblems.ENERGY);
		} else
		if (Math.abs(result.energyDemand) > Math.abs(result.energyAvailable)) {
			result.addWarning(PlanetProblems.ENERGY);
		}
		
		if (Math.abs(population) > Math.abs(result.foodAvailable) * 2) {
			result.addProblem(PlanetProblems.FOOD);
		} else
		if (Math.abs(population) > Math.abs(result.foodAvailable)) {
			result.addWarning(PlanetProblems.FOOD);
		}
		
		if (Math.abs(population) > Math.abs(result.hospitalAvailable) * 2) {
			result.addProblem(PlanetProblems.HOSPITAL);
		} else
		if (Math.abs(population) > Math.abs(result.hospitalAvailable)) {
			result.addWarning(PlanetProblems.HOSPITAL);
		}
		
		if (Math.abs(population) > Math.abs(result.houseAvailable) * 2) {
			result.addProblem(PlanetProblems.HOUSING);
		} else
		if (Math.abs(population) > Math.abs(result.houseAvailable)) {
			result.addWarning(PlanetProblems.HOUSING);
		}
		
		if (Math.abs(population) > Math.abs(result.policeAvailable) * 2) {
			result.addProblem(PlanetProblems.POLICE);
		} else
		if (Math.abs(population) > Math.abs(result.policeAvailable)) {
			result.addWarning(PlanetProblems.POLICE);
		}
		
		// FIXME stadium count
		if (population > 50000 && 0 == stadiumCount && ("human".equals(race))) {
			result.addProblem(PlanetProblems.STADIUM);
		}
		
		if (quarantine) {
			result.addProblem(PlanetProblems.VIRUS);
		}
		if (damage) {
			result.addProblem(PlanetProblems.REPAIR);
		}
		if (buildup) {
			result.addWarning(PlanetProblems.REPAIR);
		}
		if (!colonyHub) {
			result.addProblem(PlanetProblems.COLONY_HUB);
		} else
		if (!colonyHubOperable) {
			result.addWarning(PlanetProblems.COLONY_HUB);
		}
		
		for (InventoryItem pii : inventory) {
			if (pii.owner == owner) {
				if (pii.type.get("radar") != null) {
					radar = Math.max(radar, Integer.parseInt(pii.type.get("radar")));
				}
				if ("OrbitalFactory".equals(pii.type.id)) {
					result.orbitalFactory++;
				}
				if (pii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
					result.fighterCount += pii.count;
				}
						
				if (pii.type.category == ResearchSubCategory.WEAPONS_TANKS 
						|| pii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
					result.vehicleCount += pii.count;
				}
				if (pii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					result.hasSpaceStation = true;
				}
			}
		}
		
		if (owner != null && radar > 0) {
			radar *= owner.world.env.params().groundRadarUnitSize();
		}
		this.radar = radar;
		
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
		return canBuild(this, surface.buildings, 
				owner != null ? this.owner.available().keySet() : Collections.<ResearchType>emptyList(), bt);
	}
	/**
	 * Test if another instance of the building type can be built on this planet.
	 * It checks for the building limits and surface type.
	 * @param planet the target planet
	 * @param buildings the list of existing buildings
	 * @param researches the set of available researches
	 * @param bt the building type to test
	 * @return can be built here?
	 */
	public static boolean canBuild(Planet planet, 
			Collection<? extends Building> buildings,
			Collection<? extends ResearchType> researches,
			BuildingType bt) {
		// check if this planet type is on the exception list
		if (bt.except.contains(planet.type.type)) {
			return false;
		}
		// check if the required research is available
		if (planet.owner != null && bt.research != null && !researches.contains(bt.research)) {
			return false;
		}
		// if the building is not available for this race
		if (!bt.tileset.containsKey(planet.race)) {
			return false;
		}
		boolean hubFound = false;
		int count = 0;
		for (Building b : buildings) {
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
		for (InventoryItem pii : inventory) {
			if (pii.type == rt) {
				result += pii.count;
			}
		}
		return result;
	}
	/**
	 * Remove everything from the planet and reset to its default stance.
	 */
	public void die() {
		// remove equipment of the owner
		Iterator<InventoryItem> pit = inventory.iterator();
		while (pit.hasNext()) {
			InventoryItem pii = pit.next();
			if (pii.owner == owner) {
				pit.remove();
			}
		}
		if (owner != null) {
			owner.planets.put(this, PlanetKnowledge.NAME);
		}
		owner = null;
		race = null;
		quarantine = false;
		quarantineTTL = 0;
		allocation = ResourceAllocationStrategy.DEFAULT;
		tax = TaxLevel.MODERATE;
		morale = 50;
		lastMorale = 50;
		population = 0;
		lastPopulation = 0;
		autoBuild = AutoBuild.OFF;
		taxIncome = 0;
		tradeIncome = 0;
		radar = 0;
		surface.buildings.clear();
		surface.buildingmap.clear();
	}
	/**
	 * Test if the given planet contains anything from the
	 * given player.
	 * @param rt the research type
	 * @param owner the owner
	 * @return the owner
	 */
	public boolean hasInventory(ResearchType rt, Player owner) {
		for (InventoryItem pii : inventory) {
			if (pii.type == rt && pii.owner == owner) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns the number of items of the give research type of the given owner.
	 * @param rt the research type to count
	 * @param owner the owner
	 * @return the count
	 */
	public int inventoryCount(ResearchType rt, Player owner) {
		int count = 0;
		for (InventoryItem pii : inventory) {
			if (pii.type == rt && pii.owner == owner) {
				count += pii.count;
			}
		}
		return count;
	}
	/**
	 * Returns the number of items of the give category of the given owner.
	 * @param cat the research sub-category
	 * @param owner the owner
	 * @return the count
	 */
	public int inventoryCount(ResearchSubCategory cat, Player owner) {
		int count = 0;
		for (InventoryItem pii : inventory) {
			if (pii.type.category == cat && pii.owner == owner) {
				count += pii.count;
			}
		}
		return count;
	}
	/** 
	 * Change the inventory amount of a given technology. 
	 * <p>Does not change the owner's inventory.</p>
	 * @param type the item type
	 * @param owner the owner
	 * @param amount the amount delta
	 */
	public void changeInventory(ResearchType type, 
			Player owner, int amount) {
		int idx = 0;
		boolean found = false;
		for (InventoryItem pii : inventory) {
			if (pii.type == type && pii.owner == owner) {
				pii.count += amount;
				if (pii.count <= 0) {
					inventory.remove(idx);
				}
				found = true;
				break;
			}
			idx++;
		}
		if (!found && amount > 0) {
			InventoryItem pii = new InventoryItem();
			pii.type = type;
			pii.owner = owner;
			pii.count = amount;
			pii.createSlots();
			pii.shield = Math.max(0, pii.shieldMax());
			
			inventory.add(pii);
		}
	}
	/** The planet orderer by coordinates. */
	public static final Comparator<Planet> PLANET_ORDER = new Comparator<Planet>() {
		@Override
		public int compare(Planet o1, Planet o2) {
			int c = o1.y < o2.y ? -1 : (o1.y > o2.y ? 1 : 0);
			if (c == 0) {
				c = o1.x < o2.x ? -1 : (o1.x > o2.x ? 1 : 0);
			}
			return c;
		}
	};
	/** The planet order by name. */
	public static final Comparator<Planet> NAME_ORDER = new Comparator<Planet>() {
		@Override
		public int compare(Planet o1, Planet o2) {
			return o1.name.compareTo(o2.name);
		}
	};
	/**
	 * Retrieve the first inventory item with the given type.
	 * @param rt the type
	 * @return the inventory item or null if not present
	 */
	public InventoryItem getInventoryItem(ResearchType rt) {
		for (InventoryItem ii : inventory) {
			if (ii.type == rt) {
				return ii;
			}
		}
		return null;
	}
	/**
	 * Retrieve the first inventory item with the given type and owner.
	 * @param rt the type
	 * @param owner the owner
	 * @return the inventory item or null if not present
	 */
	public InventoryItem getInventoryItem(ResearchType rt, Player owner) {
		for (InventoryItem ii : inventory) {
			if (ii.type == rt && ii.owner == owner) {
				return ii;
			}
		}
		return null;
	}
	/**
	 * Count the number of buildings on this planet.
	 * @param bt the building type
	 * @return the count
	 */
	public int countBuilding(BuildingType bt) {
		int count = 0;
		for (Building b : surface.buildings) {
			if (b.type == bt) {
				count++;
			}
		}
		return count;
	}
	/**
	 * Remove detector-capable satellites (e.g., spy satellites) from orbit.
	 */
	public void removeOwnerSatellites() {
		Iterator<InventoryItem> it = inventory.iterator();
		while (it.hasNext()) {
			InventoryItem ii = it.next();
			if (ii.owner == owner && ii.type.has("detector")) {
				ii.owner.changeInventoryCount(ii.type, 1);
				it.remove();
			}
		}
	}
	@Override
	public List<InventoryItem> inventory() {
		return inventory;
	}
	/**
	 * Take over of this planet.
	 * @param newOwner the new owner
	 */
	public void takeover(Player newOwner) {
		Player lastOwner = owner;
		owner = newOwner;
		newOwner.statistics.planetsConquered++;
		lastOwner.statistics.planetsLost++;
		for (Building b : surface.buildings) {
			if (b.type.research != null) {
				newOwner.setAvailable(b.type.research);
			}
		}
		newOwner.planets.put(this, PlanetKnowledge.BUILDING);
		lastOwner.planets.put(this, PlanetKnowledge.NAME);

		removeOwnerSatellites();

		// notify about ownership change
		lastOwner.ai.onPlanetLost(this);
		newOwner.ai.onPlanetConquered(this, lastOwner);
		newOwner.world.env.events().onConquered(this, lastOwner);
	}
	/**
	 * Returns the dimensions for the given building type on this planet,
	 * considering the race.
	 * @param bt the building type
	 * @return the dimensions, including +1 roads on all sides
	 */
	public Dimension getPlacementDimensions(BuildingType bt) {
		TileSet ts = bt.tileset.get(race);
		return new Dimension(ts.normal.width + 2, ts.normal.height + 2);
	}
}
