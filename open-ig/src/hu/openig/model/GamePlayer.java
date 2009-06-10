/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.FactoryInfo;
import hu.openig.core.LabInfo;
import hu.openig.core.PlayerType;
import hu.openig.core.StarmapSelection;
import hu.openig.utils.JavaUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The players object representing all its knowledge.
 * @author karnokd, 2009.05.11.
 * @version $Revision 1.0$
 */
public class GamePlayer {
	/** The player's name. */
	public String name;
	/** The player type. */
	public PlayerType playerType;
	/** The race of the player. */
	public GameRace race;
	/** The player's money amount. */
	public long money;
	/** The icon index used to display the player's fleets. */
	public int fleetIcon;
	/** The currently selected planet. */
	public GamePlanet selectedPlanet;
	/** The currently selected fleet. */
	public GameFleet selectedFleet;
	/** The currently selected research technology. */
	public ResearchTech selectedTech;
	/** The type currently selected game object on the starmap. */
	public StarmapSelection selectionType;
	/** The currently active research progress. */
	public ResearchProgress activeResearch;
	/** The currently selected building prototype. */
	public GameBuildingPrototype selectedBuildingPrototype;
	/** The lazily initialized color object. */
	private Color colorObj;
	/**
	 * Set of known planets, which can be displayed on the starmap for the player.
	 * (Should) contain all entries of knownPlanetsByName and ownPlanets
	 */
	public final Set<GamePlanet> knownPlanets = new HashSet<GamePlanet>();
	/**
	 * Known planets by name.
	 * (Should) contain all entries of ownPlanets.
	 */
	public final Set<GamePlanet> knownPlanetsByName = new HashSet<GamePlanet>();
	/**
	 * Set of own planets.
	 */
	public final Set<GamePlanet> ownPlanets = JavaUtils.newHashSet();
	/** Set of own fleets. */
	public final Set<GameFleet> ownFleets = JavaUtils.newHashSet();
	/** Set of known fleets. (Should) contain the ownFleets too. */
	public final Set<GameFleet> knownFleets = JavaUtils.newHashSet();
	/** The set of technologies that are known. */
	public final Set<ResearchTech> knownTechnology = JavaUtils.newHashSet();
	/** The set of technologies that are researched. */
	public final Set<ResearchTech> availableTechnology = JavaUtils.newHashSet();
	/** The map of started researches and their progress. */
	public final Map<ResearchTech, ResearchProgress> researchProgresses = JavaUtils.newHashMap();
	/** The map for factory to list of production. */
	public final Map<Integer, List<ProductionProgress>> production = JavaUtils.newHashMap();
	/** The inventory counts for various research techs. */
	public final Map<ResearchTech, Integer> inventory = JavaUtils.newHashMap();
	/**
	 * Adds the planet to the own planets set and subsequently
	 * calls knowPlanet. Players can lose posession of a planet
	 * with the losePlanet() method, but the planet will be still
	 * known and known by name.
	 * @param planet the planet to posess, cannot be null
	 */
	public void possessPlanet(GamePlanet planet) {
		ownPlanets.add(planet);
		knowPlanetByName(planet);
	}
	/**
	 * Adds the planet to the known planets by name set and subsequently calls
	 * the knowPlanet method.
	 * @param planet the planet to know by name, cannot be null
	 */
	public void knowPlanetByName(GamePlanet planet) {
		if (planet == null) {
			throw new NullPointerException("planet");
		}
		knownPlanetsByName.add(planet);
		knowPlanet(planet);
	}
	/**
	 * Adds the planet to the known set of planets.
	 * @param planet the planet to know, cannot be null
	 */
	public void knowPlanet(GamePlanet planet) {
		if (planet == null) {
			throw new NullPointerException("planet");
		}
		knownPlanets.add(planet);
	}
	/**
	 * Loose a planet ownership by removing it
	 * from the ownPlanets set.
	 * @param planet the planet to loose, cannot be null
	 */
	public void loosePlanet(GamePlanet planet) {
		if (planet == null) {
			throw new NullPointerException("planet");
		}
		if (!ownPlanets.remove(planet)) {
			throw new IllegalStateException("Planet " + planet.id + " was not own by player ");
		}
	}
	/**
	 * Acquire a new fleet.
	 * @param fleet the fleet to aquire, cannot be null
	 */
	public void possessFleet(GameFleet fleet) {
		if (fleet == null) {
			throw new NullPointerException("fleet");
		}
		ownFleets.add(fleet);
		knowFleet(fleet);
	}
	/**
	 * Know a new fleet.
	 * @param fleet the fleet to aquire, cannot be null
	 */
	public void knowFleet(GameFleet fleet) {
		if (fleet == null) {
			throw new NullPointerException("fleet");
		}
		knownFleets.add(fleet);
	}
	/**
	 * Loose a fleet.
	 * @param fleet the fleet to aquire, cannot be null
	 */
	public void looseFleet(GameFleet fleet) {
		if (fleet == null) {
			throw new NullPointerException("fleet");
		}
		ownFleets.remove(fleet);
		unknowFleet(fleet);
	}
	/**
	 * Unknow a fleet, e.g. loose sight of a fleet.
	 * @param fleet the fleet to aquire, cannot be null
	 */
	public void unknowFleet(GameFleet fleet) {
		if (fleet == null) {
			throw new NullPointerException("fleet");
		}
		knownFleets.remove(fleet);
	}
	/**
	 * Returns the default text color for the player.
	 * @return the color
	 */
	public Color getColor() {
		if (colorObj == null) {
			colorObj = new Color(race.color);
		}
		return colorObj;
	}
	/**
	 * Returns the total number of buildings which belong to the given
	 * building prototype for the current player.
	 * @param bp the building prototype, not null
	 * @return the total number of buildings
	 */
	public int getTotalCountOfBuildings(GameBuildingPrototype bp) {
		int sum = 0;
		for (GamePlanet p : ownPlanets) {
			sum += p.getCountOfBuilding(bp);
		}
		return sum;
	}
	/**
	 * Returns the lab information for the player.
	 * @return the lab information containing operational and total number of various lab types
	 */
	public LabInfo getLabInfo() {
		LabInfo result = new LabInfo();
		for (GamePlanet p : ownPlanets) {
			for (GameBuilding b : p.buildings) {
				Integer value = b.prototype.values.get("civil");
				float op = b.getOperationPercent();
				if (value != null) {
					if (op >= 0.5f) {
						result.currentCivil += value;
					}
					result.totalCivil += value;
				}
				value = b.prototype.values.get("mechanic");
				if (value != null) {
					if (op >= 0.5f) {
						result.currentMechanic += value;
					}
					result.totalMechanic += value;
				}
				value = b.prototype.values.get("computer");
				if (value != null) {
					if (op >= 0.5f) {
						result.currentComputer += value;
					}
					result.totalComputer += value;
				}
				value = b.prototype.values.get("ai");
				if (value != null) {
					if (op >= 0.5f) {
						result.currentAi += value;
					}
					result.totalAi += value;
				}
				value = b.prototype.values.get("military");
				if (value != null) {
					if (op >= 0.5f) {
						result.currentMilitary += value;
					}
					result.totalMilitary += value;
				}
			}
		}
		
		return result;
	}
	/**
	 * Returns the factory information for the player.
	 * @return the factory information containing operational and total number of various lab types
	 */
	public FactoryInfo getFactoryInfo() {
		FactoryInfo result = new FactoryInfo();
		for (GamePlanet p : ownPlanets) {
			for (GameBuilding b : p.buildings) {
				Integer value = b.prototype.values.get("spaceship");
				float op = b.getOperationPercent();
				if (value != null) {
					if (op >= 0.5f) {
						result.currentShip += value * op;
					}
					result.totalShip += value;
				}
				value = b.prototype.values.get("equipment");
				op = b.getOperationPercent();
				if (value != null) {
					if (op >= 0.5f) {
						result.currentEquipment += value * op;
					}
					result.totalEquipment += value;
				}
				value = b.prototype.values.get("weapon");
				op = b.getOperationPercent();
				if (value != null) {
					if (op >= 0.5f) {
						result.currentWeapons += value * op;
					}
					result.totalWeapons += value;
				}
			}
		}
		
		return result;
	}
	/**
	 * Returns a list of researches for the given class and type indexes.
	 * @param clazz the class index
	 * @param type the type index
	 * @return the list of researches ordered by the image index
	 */
	public List<ResearchTech> getResearchFor(int clazz, int type) {
		List<ResearchTech> result = JavaUtils.newArrayList();
		for (ResearchTech rt : knownTechnology) {
			if (rt.clazzIndex == clazz && rt.typeIndex == type) {
				result.add(rt);
			}
		}
		Collections.sort(result, ResearchTech.BY_IMAGE_INDEX);
		return result;
	}
	/**
	 * Adds or removes a research technology from the current productions.
	 * @param rt the research technology
	 */
	public void addRemove(ResearchTech rt) {
		for (List<ProductionProgress> pp : production.values()) {
			List<ProductionProgress> ppl = new ArrayList<ProductionProgress>(pp);
			for (int i = 0; i < ppl.size(); i++) {
				ProductionProgress p = ppl.get(i);
				if (p.tech == rt) {
					pp.remove(i);
					rebalanceProduction(pp, rt.clazzIndex);
					return;
				}
			}
		}
		List<ProductionProgress> ppl = production.get(rt.clazzIndex);
		if (ppl == null) {
			ppl = JavaUtils.newArrayList();
			production.put(rt.clazzIndex, ppl);
		}
		ProductionProgress p = new ProductionProgress();
		p.tech = rt;
		p.priority = 50;
		ppl.add(p);
		rebalanceProduction(ppl, rt.clazzIndex);
	}
	/**
	 * Rebalances the available factory capacity between the given production progresses.
	 * @param ppl the list of production progresses
	 * @param clazzIndex the production class selector
	 */
	public void rebalanceProduction(List<ProductionProgress> ppl, int clazzIndex) {
		FactoryInfo fi = getFactoryInfo();
		int value = 0;
		if (clazzIndex == 1) {
			value = fi.currentShip;
		} else
		if (clazzIndex == 2) {
			value = fi.currentEquipment;
		} else
		if (clazzIndex == 3) {
			value = fi.currentWeapons;
		}
		int sumPrio = 0;
		for (ProductionProgress p : ppl) {
			if (p.count > 0) {
				sumPrio += p.priority;
			}
		}
		for (ProductionProgress p : ppl) {
			if (sumPrio > 0 && p.count > 0) {
				p.capacity = value * p.priority / sumPrio;
				p.capacityPercent = p.priority * 100 / sumPrio;
			} else {
				p.capacity = 0;
				p.capacityPercent = 0;
			}
		}			
	}
}
