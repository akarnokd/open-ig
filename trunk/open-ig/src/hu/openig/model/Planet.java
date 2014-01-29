/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.model.PlanetStatistics.LabStatistics;
import hu.openig.model.PlanetStatistics.ProductionStatistics;
import hu.openig.utils.Exceptions;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A planet.
 * @author akarnokd, 2010.01.07.
 */
public class Planet implements Named, Owned, HasInventory {
	/** The planet's identifier. */
	public final String id;
	/** The world object. */
	public final World world;
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
	private double population;
	/** The population change since the last day. */
	private double lastPopulation;
	/** The rendered rotation phase. */
	public int rotationPhase;
	/** The rotation direction. */
	public RotationDirection rotationDirection;
	/** The radar radius. */
	public int radar;
	/** The diameter in pixels up to 30 for the maximum zoom. */
	public int diameter;
	/** How long the quarantine should stay in 10s ingame minutes? */
	public int quarantineTTL;
	/** The default quarnatine TTL in ingame minutes. */
	public static final int DEFAULT_QUARANTINE_TTL = 5 * 24 * 60;
	/** The contents of the planet. */
	public PlanetSurface surface;
	/** The resource allocation strategy. */
	public ResourceAllocationStrategy allocation = ResourceAllocationStrategy.DEFAULT;
	/** The taxation level. */
	public TaxLevel tax = TaxLevel.MODERATE;
	/** The morale percent in hundreds. */
	public double morale = 50;
	/** The last day's morale percent in hundreds. */
	public double lastMorale = 50;
	/** The auto build mode. */
	public AutoBuild autoBuild = AutoBuild.OFF;
	/** The last day's tax income. */
	private double taxIncome;
	/** The last day's trade income. */
	private double tradeIncome;
	/** The planet's inventory. */
	public final InventoryItems inventory = new InventoryItems();
	/** The time to live counter for objects which need to be removed after the given simulation step (which is 10 ingame minutes. */
	public final Map<InventoryItem, Integer> timeToLive = new HashMap<>();
	/** The countdown for an earthquake lasting 10s of ingame minutes. */
	public int earthQuakeTTL;
	/** The remaining time for a weather event. */
	public int weatherTTL;
	/** The persistent deployed ground units and turrets. */
	public final PlanetGround ground;
	/** The ground war manager for this planet. */
	public final GroundwarManager war;
	/** @return The total income. */
	public int getTotalIncome() {
		return (int)(taxIncome + tradeIncome);
	}
	/** @return the morale label for the current morale level. */
	public String getMoraleLabel() {
		return getMoraleLabel(morale);
	}
	/**
	 * Constructor, sets the planet id and the world object.
	 * @param id the planet id
	 * @param world the world object
	 */
	public Planet(String id, World world) {
		this.id = id;
		this.world = world;
		this.ground = new PlanetGround();
		this.war = new GroundwarManager(this);
	}
	/**
	 * Return the morale label for the given level.
	 * @param morale the morale 0..100%
	 * @return the label
	 */
	public static String getMoraleLabel(double morale) {
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
	 * Add the building's statistics to the production statistics.
	 * @param out the output statistics to add to
	 * @param b the building
	 * @param eff the efficiency
	 * @param trs the traits
	 */
	protected void addProduction(ProductionStatistics out, Building b, 
			double eff, Traits trs) {
		if (b.hasResource("spaceship")) {
			double value = b.getResource("spaceship") * eff;
			out.spaceship += trs.apply(TraitKind.SHIP_PRODUCTION, 0.01d, value);
		}
		if (b.hasResource("equipment")) {
			double value = b.getResource("equipment") * eff;
			out.equipment += trs.apply(TraitKind.EQUIPMENT_PRODUCTION, 0.01d, value);
		}
		if (b.hasResource("weapon")) {
			double value = b.getResource("weapon") * eff;
			out.weapons += trs.apply(TraitKind.WEAPON_PRODUCTION, 0.01d, value);
		}
	}
	/**
	 * @return Compute the production statistics only.
	 */
	public PlanetStatistics getProductionStatistics() {
		PlanetStatistics result = new PlanetStatistics();
		for (Building b : surface.buildings.iterable()) {
			double eff = b.getEfficiency();
			if (Building.isOperational(eff)) {
				addProduction(result.activeProduction, b, eff, owner.traits);
			}
			addProduction(result.production, b, 1d, owner.traits);
		}
		return result;
	}
	/**
	 * Add the lab statistics of the given buildings.
	 * @param out the the output lab statistics
	 * @param b the buildings
	 */
	protected void addLabs(LabStatistics out, Building b) {
		if (b.hasResource("civil")) {
			out.civil += b.getResource("civil");
		}
		if (b.hasResource("mechanical")) {
			out.mech += b.getResource("mechanical");
		}
		if (b.hasResource("computer")) {
			out.comp += b.getResource("computer");
		}
		if (b.hasResource("ai")) {
			out.ai += b.getResource("ai");
		}
		if (b.hasResource("military")) {
			out.mil += b.getResource("military");
		}
	}
	/**
	 * @return computes only the research related statistics.
	 */
	public PlanetStatistics getResearchStatistics() {
		PlanetStatistics ps = new PlanetStatistics();
		for (Building b : surface.buildings.iterable()) {
			if (Building.isOperational(b.getEfficiency())) {
				addLabs(ps.activeLabs, b);
			}
			addLabs(ps.labs, b);
		}
		return ps;
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
		int fireBrigadeCount = 0;
		
		result.populationGrowthModifier = 1d;
		
		result.vehicleMax = 8; // default per planet
		
		for (Building b : surface.buildings.iterable()) {
			double eff = b.getEfficiency();
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
				
				addProduction(result.activeProduction, b, eff, owner.traits);
				
				addLabs(result.activeLabs, b);
				
				if (b.hasResource("radar")) {
					radar = Math.max(radar, (int)b.getResource("radar"));
				}
				if (b.type.id.equals("Stadium")) {
					stadiumCount++;
				}
				if (b.type.id.equals("FireBrigade")) {
					fireBrigadeCount++;
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
				if (b.hasResource(BuildingType.RESOURCE_VEHICLES)) {
					result.vehicleMax += b.getResource(BuildingType.RESOURCE_VEHICLES);
				}
				if (b.hasResource("population-growth")) {
					result.populationGrowthModifier = 1 + b.getResource("population-growth") / 100;
				}

			}
			if ("MilitarySpaceport".equals(b.type.id)) {
				result.militarySpaceportCount = 1;
			}
			
			addProduction(result.production, b, 1d, owner.traits);
			
			addLabs(result.labs, b);
			
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
			result.nativeWorkerDemand += Math.abs(b.getWorkers()) * health;
			
			damage |= b.isDamaged();
			buildup |= b.isConstructing();
			colonyHub |= "MainBuilding".equals(b.type.kind) && !b.isConstructing();
		}
		// check if there is still a building with unallocated resources
		for (Building b : surface.buildings.iterable()) {
			if (b.enabled 
					&& ((b.assignedWorker == 0 && population > 0)
							|| (result.energyAvailable > 0 && b.getEnergy() < 0 
									&& b.assignedEnergy == 0))) {
				result.constructing = true;
				break;
			}
		}
		
		if (quarantineTTL > 0) {
			result.hospitalAvailable /= 4;
		}
		
		result.problems.clear();
		if (Math.abs(result.workerDemand) > population * 2) {
			result.addProblem(PlanetProblems.WORKFORCE);
		} else
		if (Math.abs(result.workerDemand) > population) {
			result.addWarning(PlanetProblems.WORKFORCE);
		}
        
		if (result.nativeWorkerDemand > population) {
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
		
		if (owner != null) {
			if (population > 50000 && 0 == stadiumCount && canBuild("Stadium")) {
				result.addProblem(PlanetProblems.STADIUM);
			}
			
			if (population > 30000 && 0 == fireBrigadeCount && canBuild("FireBrigade")) {
				result.addProblem(PlanetProblems.FIRE_BRIGADE);
			}
		}
		
		if (quarantineTTL > 0) {
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
		
		for (InventoryItem pii : inventory.iterable()) {
			if (pii.owner == owner) {
				if (pii.type.has(ResearchType.PARAMETER_RADAR)) {
					radar = Math.max(radar, pii.type.getInt(ResearchType.PARAMETER_RADAR));
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
			radar *= world.params().groundRadarUnitSize();
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
		return canBuild(this, surface.buildings.iterable(), 
				owner != null ? this.owner.availableSet() : Collections.<ResearchType>emptyList(), 
						bt, true);
	}
	/**
	 * Test if another instance of the building type can be built on this planet
	 * as replacement.
	 * It checks for the building limits and surface type.
	 * @param bt the building type to test
	 * @return can be built here?
	 */
	public boolean canBuildReplacement(BuildingType bt) {
		return canBuild(this, surface.buildings.iterable(), 
				owner != null ? this.owner.availableSet() : Collections.<ResearchType>emptyList(), 
						bt, false);
	}
	/**
	 * Check if the given building type can be built on this planet.
	 * @param buildingType the building type identifier
	 * @return true if it can be built here
	 */
	public boolean canBuild(String buildingType) {
		if (owner != null) {
			BuildingType bt = world.building(buildingType);
			if (bt != null) {
				return canBuild(bt);
			}
		}
		return false;
	}
	/**
	 * Test if another instance of the building type can be built on this planet.
	 * It checks for the building limits and surface type.
	 * @param planet the target planet
	 * @param buildings the list of existing buildings
	 * @param researches the set of available researches
	 * @param bt the building type to test
	 * @param checkLimit check if the build count limit has been reached?
	 * @return can be built here?
	 */
	public static boolean canBuild(Planet planet, 
			Iterable<? extends Building> buildings,
			Collection<? extends ResearchType> researches,
			BuildingType bt,
			boolean checkLimit) {
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
			if (checkLimit) {
				if ((bt.limit < 0 && b.type.kind.equals(bt.kind))
						|| (bt.limit > 0 && b.type == bt)
				) {
						count++;
				}
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
		Map<BuildingType, Integer> result = new HashMap<>();
		for (Building b : surface.buildings.iterable()) {
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
		for (InventoryItem pii : inventory.findByType(rt.id)) {
			result += pii.count;
		}
		return result;
	}
	/**
	 * Remove everything from the planet and reset to its default stance.
	 * Does not affect the statistics.
	 */
	public void die() {
		if (owner != null) {
			// remove equipment of the owner
			inventory.removeByOwner(owner.id);
			owner.planets.put(this, PlanetKnowledge.NAME);
		}
		owner = null;
		race = null;
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
		for (InventoryItem pii : inventory.findByOwner(owner.id)) {
			if (pii.type == rt && pii.count > 0) {
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
		for (InventoryItem pii : inventory.findByOwner(owner.id)) {
			if (pii.type == rt) {
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
		for (InventoryItem pii : inventory.findByOwner(owner.id)) {
			if (pii.type.category == cat) {
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
		int idx = -1;
		boolean found = false;
		for (InventoryItem pii : inventory.findByOwner(owner.id)) {
			if (pii.type == type) {
				pii.count += amount;
				if (pii.count <= 0) {
					idx = pii.id;
				}
				found = true;
				break;
			}
		}
		if (idx >= 0) {
			inventory.remove(idx);
		}
		if (!found && amount > 0) {
			InventoryItem pii = new InventoryItem(world.newId(), owner, type);
			pii.count = amount;
			pii.init();
			
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
		for (InventoryItem ii : inventory.findByType(rt.id)) {
			return ii;
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
		for (InventoryItem ii : inventory.findByOwner(owner.id)) {
			if (ii.type == rt) {
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
		return surface.buildings.findByType(bt.id).size();
	}
	/**
	 * Remove detector-capable satellites (e.g., spy satellites) from orbit.
	 */
	public void removeOwnerSatellites() {
		List<InventoryItem> set = new ArrayList<>(inventory.findByOwner(owner.id));
		for (InventoryItem ii : set) {
			if (ii.type.has(ResearchType.PARAMETER_DETECTOR)) {
				ii.owner.changeInventoryCount(ii.type, 1);
			}
		}
		inventory.removeAll(set);
	}
	@Override
	public InventoryItems inventory() {
		return inventory;
	}
	/**
	 * Take over of this planet.
	 * @param newOwner the new owner
	 */
	public void takeover(Player newOwner) {
		Player lastOwner = owner;
		owner = newOwner;
		newOwner.statistics.planetsConquered.value++;
		if (!newOwner.id.equals("Pirates")) {
			lastOwner.statistics.planetsLostAlien.value++;
		}
		lastOwner.statistics.planetsLost.value++;
		for (Building b : surface.buildings.iterable()) {
			if (b.type.research != null) {
				newOwner.setAvailable(b.type.research);
			}
		}
		newOwner.planets.put(this, PlanetKnowledge.BUILDING);
		lastOwner.planets.put(this, PlanetKnowledge.NAME);

		removeOwnerSatellites();

		autoBuild = AutoBuild.OFF;
		allocation = ResourceAllocationStrategy.DEFAULT;
		tax = TaxLevel.MODERATE;
		
		// notify about ownership change
		lastOwner.ai.onPlanetLost(this);
		newOwner.ai.onPlanetConquered(this, lastOwner);
		world.scripting.onConquered(this, lastOwner);
	}
	/**
	 * Returns the dimensions for the given building type on this planet,
	 * considering the race.
	 * @param bt the building type
	 * @return the dimensions, including +1 roads on all sides
	 */
	public Dimension getPlacementDimensions(BuildingType bt) {
		TileSet ts = bt.tileset.get(race);
		if (ts != null) {
			return new Dimension(ts.normal.width + 2, ts.normal.height + 2);
		}
		return null;
	}
	/** Remove inventory items with zero counts. */
	public void cleanup() {
		inventory.removeIf(InventoryItem.CLEANUP);
	}
	@Override
	public String toString() {
		return String.format("Id = %s, Owner = %s, Race = %s", id, owner != null ? owner.id : null, race);
	}
	/** @return true if this planet has a military spaceport. */
	public boolean hasMilitarySpaceport() {
		for (Building b : surface.buildings.findByType("MilitarySpaceport")) {
			if (b.isOperational()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Rebuild all roads of the planet.
	 */
	public void rebuildRoads() {
		if (race != null && owner != null) {
			surface.placeRoads(race, world.buildingModel);
		}
	}
	/**
	 * Remove excess fighters and vehicles.
	 */
	public void removeExcess() {
		// remove fighters if no space stations present
		if (inventoryCount(ResearchSubCategory.SPACESHIPS_STATIONS, owner) == 0) {
			for (InventoryItem ii2 : inventory.list()) {
				if (ii2.owner == owner && ii2.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
					owner.changeInventoryCount(ii2.type, ii2.count);
					inventory.remove(ii2);
				}
			}
		}
		InventoryItem.removeExcessFighters(inventory);
		// remove excess vehicles
		PlanetStatistics ps = getStatistics();
		InventoryItem.removeExcessTanks(inventory, owner, ps.vehicleCount, ps.vehicleMax);
	}
	/**
	 * Refills the equimpent of the space stations around the planet.
	 */
	public void refillEquipment() {
		if (owner == world.player && world.env.config().reequipBombs) {
			for (InventoryItem ii : inventory.findByOwner(owner.id)) {
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					for (InventorySlot is : ii.slots.values()) {
						is.refill(owner);
					}
				}
			}
		}
	}
	/**
	 * Find a building with the specified id.
	 * @param id the building id
	 * @return the building or null
	 */
	public Building findBuilding(int id) {
		return surface.buildings.findById(id);
	}
	/**
	 * Places a building onto the surface.
	 * @param b the building object
	 */
	private void placeBuilding(Building b) {
		surface.placeBuilding(b.tileset.normal, b.location.x, b.location.y, b);
	}
	/**
	 * Start building by auto-placing a building.
	 * @param type the building type
	 * @param race the race
	 * @return the building id or -1 if no room left
	 */
	public Building build(String type, String race) {
		if (owner == null) {
			throw new IllegalStateException("owner is null");
		}
		if (!canBuild(type)) {
			throw new IllegalArgumentException("Building type not supported here: " + id + " - " + type);
		}
		BuildingType bt = owner.world.building(type);
		if (bt.cost > owner.money()) {
			throw new IllegalArgumentException("Not enough money");
		}
		TileSet ts = bt.tileset.get(race);
		if (ts == null) {
			throw new IllegalArgumentException("Building has no race-specific version: " + type + " - " + race);
		}
		
		Point pt = findLocation(ts);
		if (pt != null) {
			Building bid = placeNewBuilding(bt, race, pt.x + 1, pt.y - 1);
			rebuildRoads();
			return bid;
		}
		return null;
	}
	/**
	 * Internally places a building of the given type and at a specified location.
	 * <p>Note that no validity check is performed here.</p>
	 * <p>Updates the statistics and removes money from the owner.</p>
	 * @param bt the building type
	 * @param race the race
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return the building id
	 */
	private Building placeNewBuilding(BuildingType bt, String race, int x, int y) {
		int bid = world.newId();
		Building b = new Building(bid, bt, race);
		b.location = Location.of(x, y);
		placeBuilding(b);

		owner.addMoney(-bt.cost);
		owner.today.buildCost += bt.cost;
		
		owner.statistics.buildCount.value++;
		owner.statistics.moneyBuilding.value += bt.cost;
		owner.statistics.moneySpent.value += bt.cost;
		
		world.statistics.buildCount.value++;
		world.statistics.moneyBuilding.value += bt.cost;
		world.statistics.moneySpent.value += bt.cost;

		return b;
	}
	/**
	 * Start a building at the specified location.
	 * @param type the building type
	 * @param race the race
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return the building unique id
	 */
	public Building build(String type, String race, int x, int y) {
		if (owner == null) {
			throw new IllegalStateException("owner is null");
		}
		if (!canBuild(type)) {
			throw new IllegalArgumentException("Building type not supported here: " + id + " - " + type);
		}
		BuildingType bt = owner.world.building(type);
		if (bt.cost > owner.money()) {
			throw new IllegalArgumentException("Not enough money");
		}
		if (!bt.tileset.containsKey(race)) {
			throw new IllegalArgumentException("Building has no race-specific version: " + type + " - " + race);
		}
		if (canPlace(type, race, x, y)) {
			Building bid = placeNewBuilding(bt, race, x, y);
			rebuildRoads();
			return bid;
		}
		throw new IllegalArgumentException("Can't place building at " + x + ", " + y);
	}
	/**
	 * Colonize the planet by the given owner.
	 * @param owner the new owner
	 * @return true if colonization successfull
	 */
	public boolean colonize(Player owner) {
		if (this.owner != null) {
			throw new IllegalStateException("Planet is owned by " + this.owner.id);
		}
		this.owner = owner;

		this.race = owner.race;
		this.population = 5000;
		this.morale = 50;
		this.lastMorale = 50;
		this.lastPopulation = 5000;
		this.owner.planets.put(this, PlanetKnowledge.BUILDING);
		this.owner.currentPlanet = this;
		
		this.owner.statistics.planetsColonized.value++;
		
		this.owner.colonizationTargets.remove(this.id);
		
		// uninstall satellites
		this.removeOwnerSatellites();

		return buildColonyHub();
	}
	/**
	 * Build a colony hub on this planet.
	 * @return true if successfully built
	 */
	public boolean buildColonyHub() {
		BuildingType bt = world.buildingModel.find("MainBuilding");
		if (bt != null) {
			TileSet ts = bt.tileset.get(owner.race);
			if (ts != null) {
				Point pt = findLocation(ts);
				if (pt != null) {
					Building b = new Building(owner.world.newId(), bt, owner.race);
					b.location = Location.of(pt.x + 1, pt.y - 1);
					placeBuilding(b);
					rebuildRoads();
					
					world.statistics.buildCount.value++;
					owner.statistics.buildCount.value++;
					
					return true;
				}				
			}
		}
		return false;
	}
	/**
	 * Find a location for the given tileset-building.
	 * @param ts the tileset
	 * @return the location or null if no room
	 */
	private Point findLocation(TileSet ts) {
		return surface.placement.findLocation(ts.normal.width + 2, ts.normal.height + 2);
	}
	/**
	 * Check if the given building type can be built at the specified location.
	 * @param type the building type
	 * @param race the building race
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return true if can be placed
	 */
	public boolean canPlace(String type, String race, int x, int y) {
		BuildingType bt = owner.world.building(type);
		if (bt != null) {
			TileSet ts = bt.tileset.get(race);
			if (ts != null) {
				return surface.placement.canPlaceBuilding(x - 1, y + 1, ts.normal.width + 2, ts.normal.height + 2);
			}
		}
		return false;
	}
	/**
	 * Returns the planet status copy of this planet.
	 * @param player the player whose perspective needs to be established
	 * @return the planet status copy
	 */
	public PlanetStatus toPlanetStatus(Player player) {
		PlanetStatus result = new PlanetStatus();
		result.id = id;
		result.knowledge = player.planets.get(this);
		
		if (result.knowledge != null) {
			if (result.knowledge.compareTo(PlanetKnowledge.OWNER) >= 0) {
				result.owner = owner.id;
				result.race = race;
			}
			if (result.knowledge.compareTo(PlanetKnowledge.STATIONS) >= 0) {
				result.population = population;
				result.lastPopulation = population;
			}
			if (owner == player) {
				result.lastPopulation = lastPopulation;
				result.quarantineTTL = quarantineTTL;
				result.tax = tax;
				result.morale = morale;
				result.lastMorale = lastMorale;
				result.taxIncome = taxIncome;
				result.tradeIncome = tradeIncome;
				result.earthquakeTTL = earthQuakeTTL;
				result.weatherTTL = weatherTTL;
				result.autoBuild = autoBuild;
			}
			// add in-orbit objects belonging to the player
			for (InventoryItem ii : inventory.iterable()) {
				InventoryItemStatus iis = ii.toInventoryItemStatus();
				Integer ttl = timeToLive.get(ii);
				if (ttl != null) {
					result.timeToLive.put(ii.id, ttl);
				}
				if (ii.owner == player) {
					result.inventory.add(iis);
				} else
				if (result.knowledge.compareTo(PlanetKnowledge.STATIONS) >= 0) {
					if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
						// enemy space station, drop knowledge
						iis.clearEnemyInfo();
						result.inventory.add(iis);
					}
				}
			}
			for (Building b : surface.buildings.iterable()) {
				BuildingStatus bs = b.toBuildingStatus();
				if (owner == player || result.knowledge.compareTo(PlanetKnowledge.BUILDING) >= 0) {
					result.buildings.add(bs);
				} else
				if (result.knowledge.compareTo(PlanetKnowledge.OWNER) >= 0) {
					bs.clearEnemyInfo();
					result.buildings.add(bs);
				}
			}
			
		}
		
		return result;
	}
	/**
	 * Loads the status info from the planet status record.
	 * @param ps the planet status record
	 * @param lookup the model lookup
	 */
	public void fromPlanetStatus(PlanetStatus ps, ModelLookup lookup) {
		if (ps.owner != null) {
			owner = lookup.player(ps.owner);
			race = ps.race;
			
			population = ps.population;
			lastPopulation = ps.lastPopulation;
			quarantineTTL = ps.quarantineTTL;
			tax = ps.tax;
			morale = ps.morale;
			lastMorale = ps.lastMorale;
			taxIncome = ps.taxIncome;
			tradeIncome = ps.tradeIncome;
			earthQuakeTTL = ps.earthquakeTTL;
			weatherTTL = ps.weatherTTL;
			autoBuild = ps.autoBuild;
			
			Set<Integer> current = new HashSet<>();
			// merge inventory
			for (InventoryItemStatus iis : ps.inventory) {
				current.add(iis.id);
				InventoryItem ii = inventory.findById(iis.id);
				if (ii == null) {
					ii = new InventoryItem(iis.id,
							lookup.player(iis.owner), lookup.research(iis.type));
					inventory.add(ii);
				}
				ii.fromInventoryItemStatus(iis, lookup);
				Integer ttl = ps.timeToLive.get(iis.id);
				if (ttl != null) {
					timeToLive.put(ii, ttl);
				}
			}
			
			inventory.retainAllById(current);
            Set<Integer> iids = inventory.inventoryIds();
            
			Iterator<InventoryItem> iit = timeToLive.keySet().iterator();
            while (iit.hasNext()) {
                InventoryItem ii = iit.next();
                if (!iids.contains(ii.id)) {
                    iit.remove();
                }
            }
			
			// merge buildings
			current.clear();
			boolean roads = false;
			for (BuildingStatus bs : ps.buildings) {
				current.add(bs.id);
				Building b = findBuilding(bs.id);
				if (b == null) {
					b = new Building(bs.id, lookup.building(bs.type), bs.race);
					b.fromBuildingStatus(bs);
					placeBuilding(b);
					roads = true;
				} else {
					b.fromBuildingStatus(bs);
				}
			}
			for (Building b : surface.buildings.list()) {
				if (!current.contains(b.id)) {
					surface.removeBuilding(b);
					roads = true;
				}
			}
			if (roads) {
				rebuildRoads();
			}
		} else {
			if (owner != null) {
				die();
			}
		}
	}
	/**
	 * Creates a new fleet for the owner of this planet
	 * around this planet.
	 * @return the new fleet
	 */
	public Fleet newFleet() {
		if (owner == null) {
			Exceptions.add(new AssertionError("Planet has no owner: " + id));
			return null;
		}
		
		Fleet f = new Fleet(owner);
		f.name = world.labels.get("newfleet.name");
		
		double r = Math.max(0, ModelUtils.random() * world.params().nearbyDistance() - 1);
		double k = ModelUtils.random() * 2 * Math.PI;
		f.x = (x + Math.cos(k) * r);
		f.y = (y + Math.sin(k) * r);

		return f;
	}
	@Override
	public boolean canDeploy(ResearchType type) {
		return type.category == ResearchSubCategory.WEAPONS_TANKS
				|| type.category == ResearchSubCategory.WEAPONS_VEHICLES
				|| type.category == ResearchSubCategory.SPACESHIPS_STATIONS
				|| type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
				|| type.category == ResearchSubCategory.SPACESHIPS_SATELLITES;
	}
	@Override
	public boolean canUndeploy(ResearchType type) {
		return type.category == ResearchSubCategory.WEAPONS_TANKS
				|| type.category == ResearchSubCategory.WEAPONS_VEHICLES
				|| type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
				|| type.category == ResearchSubCategory.SPACESHIPS_SATELLITES;
	}
	/**
	 * Compute how many of the supplied items can be added without violating the limit constraints. 
	 * @param rt the item type
	 * @param owner the owner
	 * @return the currently alloved
	 */
	public int getAddLimit(ResearchType rt, Player owner) {
		PlanetStatistics fs = getStatistics();
		switch (rt.category) {
		case SPACESHIPS_FIGHTERS:
			if (owner != this.owner) {
				return 0;
			}
			return owner.world.params().fighterLimit() - inventoryCount(rt, owner);
		case WEAPONS_TANKS:
		case WEAPONS_VEHICLES:
			if (owner != this.owner) {
				return 0;
			}
			return fs.vehicleMax - fs.vehicleCount;
		case SPACESHIPS_STATIONS:
			if (owner != this.owner) {
				return 0;
			}
			return owner.world.params().stationLimit() - inventoryCount(ResearchSubCategory.SPACESHIPS_STATIONS, owner);
		case SPACESHIPS_SATELLITES:
			int c = inventoryCount(rt, owner);
			if (owner == this.owner && rt.has(ResearchType.PARAMETER_DETECTOR)) {
				return 0;
			}
			return c > 0 ? 0 : 1; 
		default:
			return 0;
		}
	}
	@Override
	public List<InventoryItem> deployItem(ResearchType rt, Player owner, int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("count > 0 required");
		}
		int fleetLimit = getAddLimit(rt, owner);
		boolean single = false;
		switch (rt.category) {
		case WEAPONS_TANKS:
		case WEAPONS_VEHICLES:
		case SPACESHIPS_FIGHTERS:
			single = true;
			break;
		default:
		}

		int invAvail = owner.inventoryCount(rt);
		
		int toDeploy = Math.min(count, Math.min(fleetLimit, invAvail));
		if (toDeploy > 0) {
			owner.changeInventoryCount(rt, -toDeploy);

			if (single) {
				InventoryItem ii = getInventoryItem(rt);
				if (ii == null) {
					ii = new InventoryItem(owner.world.newId(), owner, rt);
					ii.init();
					inventory.add(ii);
				}
				ii.count += toDeploy;
				
				if (owner != this.owner) {
					timeToLive.put(ii, world.getSatelliteTTL(rt));
				}
				
				return Collections.singletonList(ii);
			}
			List<InventoryItem> r = new ArrayList<>();
			for (int i = 0; i < toDeploy; i++) {
				InventoryItem ii = new InventoryItem(owner.world.newId(), owner, rt);
				ii.init();
				ii.count = 1;
				inventory.add(ii);
				r.add(ii);

				if (owner != this.owner) {
					timeToLive.put(ii, world.getSatelliteTTL(rt));
				}
			}
			return r;
		}
		return Collections.emptyList();
	}
	@Override
	public void sell(int itemId, int count) {
		InventoryItem ii = inventory.findById(itemId);
		if (ii != null) {
			sell(ii, count);
		}
	}
	@Override
	public boolean contains(int itemId) {
		return inventory.contains(itemId);
	}
	@Override
	public void sell(InventoryItem ii, int count) {
		ii.sell(count);
		if (ii.count <= 0) {
			inventory.remove(ii);
		}
	}
	@Override
	public void undeployItem(int itemId, int count) {
		InventoryItem ii = inventory.findById(itemId);
		if (ii != null) {
			if (canUndeploy(ii.type)) {
				int n = Math.min(count, ii.count);
				ii.count -= n;
				ii.owner.changeInventoryCount(ii.type, n);
				if (ii.count <= 0) {
					inventory.remove(ii);
				}
			} else {
				throw new IllegalArgumentException("inventory item can't be undeployed: " + ii.type);
			}
		} else {
			throw new IllegalArgumentException("inventory item not found: " + itemId);
		}
	}
	/**
	 * Demolish the given building.
	 * <p>Updates the statistics and player money.</p>
	 * @param b the building to demolish
	 */
	public void demolish(Building b) {
		if (surface.removeBuilding(b)) {
			rebuildRoads();
			
			int moneyBack = b.type.cost * (1 + b.upgradeLevel) / 2;
			
			owner.addMoney(moneyBack);
			
			owner.statistics.demolishCount.value++;
			owner.statistics.moneyDemolishIncome.value += moneyBack;
			owner.statistics.moneyIncome.value += moneyBack;
	
			world.statistics.demolishCount.value++;
			world.statistics.moneyDemolishIncome.value += moneyBack;
			world.statistics.moneyIncome.value += moneyBack;
		} else {
			throw new IllegalStateException("Failed to remove building " + b.id + " (" + b.type.id + " on " + id + ")");
		}
	}
	/**
	 * Upgrade the specified building.
	 * @param building the building object
	 * @param newLevel the new level
	 * @return true if successful
	 */
	public boolean upgrade(Building building, int newLevel) {
		if (!building.canUpgrade(newLevel)) {
			return false;
		}
		int buildCost = building.upgradeCost(newLevel);

		if (owner.money() < buildCost) {
			return false;
		}
		
		building.setLevel(newLevel);
		building.buildProgress = building.type.hitpoints * 1 / 4;
		building.hitpoints = building.buildProgress;
		
		owner.today.buildCost += buildCost;
		
		owner.addMoney(-buildCost);
		owner.statistics.upgradeCount.value++;
		owner.statistics.moneySpent.value += buildCost;
		owner.statistics.moneyUpgrade.value += buildCost;
		
		world.statistics.upgradeCount.value++;
		world.statistics.moneySpent.value += buildCost;
		world.statistics.moneyUpgrade.value += buildCost;
		
		return true;
	}
	/**
	 * Upgrade fighters, vehicles and starbases to the most enhanced version, 
	 * if the inventory permits it.
	 */
	public void upgradeAll() {
		ResearchType bestStation = null;
		ResearchType orbitalFactory = world.research("OrbitalFactory");
		for (ResearchType rt : owner.available()) {
			if (rt.category == ResearchSubCategory.SPACESHIPS_STATIONS && rt != orbitalFactory) {
				if (owner.inventoryCount(rt) > 0) {
					if (bestStation == null || bestStation.productionCost < rt.productionCost) {
						bestStation = rt;
					}
				}
			}
		}
		int stations = inventoryCount(ResearchSubCategory.SPACESHIPS_STATIONS, owner);
		// deploy stations if possible
		if (bestStation != null) {
			while (stations < world.params().stationLimit() && owner.inventoryCount(bestStation) > 0) {
				deployItem(bestStation, owner, 1);
				stations++;
			}
			// replace old stations with newer ones if possible
			for (InventoryItem ii : inventory.list()) {
				if (ii.owner == owner 
						&& ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS
						&& ii.type != orbitalFactory) {
					if (ii.type.productionCost < bestStation.productionCost 
							&& owner.inventoryCount(bestStation) > 0) {
						ii.strip();
						ii.sell(ii.count);
						inventory.remove(ii);
						deployItem(bestStation, owner, 1);
					}
				}
			}
		}
		
		// if we have stations, deploy any available fighters
		if (stations > 0) {
			for (ResearchType rt : owner.available()) {
				if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
					int placed = inventoryCount(rt, owner);
					if (placed < world.params().fighterLimit()) {
						int avail = owner.inventoryCount(rt);
						if (avail > 0) {
							int n = Math.min(world.params().fighterLimit() - placed, avail);
							deployItem(rt, owner, n);
						}
					}
				}
			}			
		}
		
		// upgrade station equipment: strip current settings
		List<InventoryItem> uis = new ArrayList<>();
		for (InventoryItem ii : inventory.findByOwner(owner.id)) {
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS
					&& ii.type != orbitalFactory) {
				ii.strip();
				uis.add(ii);
			}
		}
		// allocate equipment
		Collections.sort(uis, new Comparator<InventoryItem>() {
			@Override
			public int compare(InventoryItem o1, InventoryItem o2) {
				return ResearchType.EXPENSIVE_FIRST.compare(o1.type, o2.type);
			}
		});
		for (InventoryItem ii : uis) {
			ii.upgradeSlots();
		}
	}
	/**
	 * Test if the planet can be auto-upgraded.
	 * @return true if auto-upgrade is possible
	 */
	public boolean canUpgrade() {
		boolean newFighters = false;
		ResearchType bestStation = null;
		
		ResearchType orbitalFactory = owner.world.researches.get("OrbitalFactory");

		for (ResearchType rt : owner.available()) {
			if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
				if (inventoryCount(rt, owner) < world.params().fighterLimit() && owner.inventoryCount(rt) > 0) {
					newFighters = true;
				}
			}
			if (rt.category == ResearchSubCategory.SPACESHIPS_STATIONS && rt != orbitalFactory) {
				if (owner.inventoryCount(rt) > 0) {
					if (bestStation == null || bestStation.productionCost < rt.productionCost) {
						
						bestStation = rt;
					}
				}
			}
		}
		boolean hasSpaceStation = false;
		for (InventoryItem ii : inventory.findByOwner(owner.id)) {
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS 
					/* && ii.type != orbitalFactory*/) {
				hasSpaceStation = true;
				break;
			}
		}

		// more fighters may be added (after possibly adding a station)
		if (newFighters && (hasSpaceStation || bestStation != null)) {
			return true;
		}
		
		// room for new space stations?
		int stationCount = inventoryCount(ResearchSubCategory.SPACESHIPS_STATIONS, owner);
		
		if (stationCount < world.params().stationLimit() && bestStation != null) {
			return true;
		}
		
		// existing stations may be replaced?
		if (bestStation != null) {
			for (InventoryItem ii : inventory.findByOwner(owner.id)) {
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS && ii.type != orbitalFactory) {
					if (ii.type.productionCost < bestStation.productionCost) {
						return true;
					}
				}
			}
		}
		// station inventory can be refilled?
		for (InventoryItem ii : inventory.findByOwner(owner.id)) {
			if (ii.checkSlots()) {
				return true;
			}
		}
		
		return false;
	}
	/**
	 * Returns the tax income.
	 * @return the tax income
	 */
	public double taxIncome() {
		return taxIncome;
	}
	/**
	 * Returns the trade income.
	 * @return the trade income
	 */
	public double tradeIncome() {
		return tradeIncome;
	}
	/**
	 * Set a new tax income.
	 * @param newTaxIncome the new tax income
	 */
	public void taxIncome(double newTaxIncome) {
		this.taxIncome = newTaxIncome;
	}
	/**
	 * Set a new trade income.
	 * @param newTradeIncome the new trade income
	 */
	public void tradeIncome(double newTradeIncome) {
		this.tradeIncome = newTradeIncome;
	}
	/**
	 * Add value to the tax income.
	 * @param value the value to add
	 */
	public void addTaxIncome(double value) {
		this.taxIncome += value;
	}
	/**
	 * Add value to the trade income.
	 * @param value the value to add
	 */
	public void addTradeIncome(double value) {
		this.tradeIncome += value;
	}
	/**
	 * Returns the current population.
	 * @return the population
	 */
	public double population() {
		return population;
	}
	/**
	 * Returns the last day's population.
	 * @return the last population
	 */
	public double lastPopulation() {
		return lastPopulation;
	}
	/**
	 * Set a new population value.
	 * @param newPopulation the new population value
	 */
	public void population(double newPopulation) {
		this.population = newPopulation;
	}
	/**
	 * Set a new last population value.
	 * @param newLastPopulation the new last population value
	 */
	public void lastPopulation(double newLastPopulation) {
		this.lastPopulation = newLastPopulation;
	}
	/**
	 * Add to the population.
	 * @param value the value to add
	 */
	public void addPopulation(double value) {
		this.population += value;
	}
	/**
	 * Add to the last population.
	 * @param value the value to add
	 */
	public void addLastPopulation(double value) {
		this.lastPopulation += value;
	}
}
