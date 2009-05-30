/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.behavior.ResourceAllocator;
import hu.openig.core.BuildLimit;
import hu.openig.core.GameSpeed;
import hu.openig.core.LabInfo;
import hu.openig.res.Labels;
import hu.openig.utils.JavaUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * The top level container describing an actual game.
 * @author karnokd, 2009.05.11.
 * @version $Revision 1.0$
 */
public class GameWorld {
	/** Show the ultra fast speed setter button? */
	public boolean showUltrafast;
	/** Current game speed. */
	public GameSpeed gameSpeed = GameSpeed.NORMAL;
	/** The speed control buttons enabled? */
	public boolean showSpeedControls = true;
	/** Show the time? */
	public boolean showTime;
	/** The list of planets. */
	public final List<GamePlanet> planets = new ArrayList<GamePlanet>();
	/** The current time of the game. Should be handled in GMT mode, no summer time. */
	public final GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	/** The player object, whose properties (fleets, planets, research, etc) are displayed by the rendering. */
	public GamePlayer player;
	/** 
	 * A flag to disable the modification of the player's assets.
	 * Could be used for diagnostic purposes when the player object is changed from
	 * the real human player to the other players. 
	 */
	public boolean readOnly;
	/** The list of all players. */
	public final List<GamePlayer> players = new ArrayList<GamePlayer>();
	/** The races in the current game. */
	public final List<GameRace> races = new ArrayList<GameRace>();
	/** All fleets in the current game. */
	public final List<GameFleet> fleets = new ArrayList<GameFleet>();
	/** The labels used in the game world. */
	public Labels labels;
	/** The current language. */
	public String language;
	/** The tech id to game building prototype list. */
	public final List<GameBuildingPrototype> buildingPrototypes = new ArrayList<GameBuildingPrototype>();
	/** The tech id to game building prototype map. */
	public final Map<String, GameBuildingPrototype> buildingPrototypesMap = new HashMap<String, GameBuildingPrototype>();
	/** Contains all research technology of the game. */
	public final Map<String, ResearchTech> research = JavaUtils.newHashMap();
	/** The current technology level. */
	public int level;
	/**
	 * {@inheritDoc}
	 */
	public GameRace getRace(int index) {
		for (GameRace gr : races) {
			if (gr.index == index) {
				return gr;
			}
		}
		throw new IllegalStateException("Unknown race index: " + index);
	}
	/**
	 * {@inheritDoc}
	 */
	public GameRace getRace(String id) {
		for (GameRace gr : races) {
			if (gr.id.equals(id)) {
				return gr;
			}
		}
		throw new IllegalStateException("Unknown race id: " + id);
	}
	/**
	 * {@inheritDoc}
	 */
	public GamePlayer getPlayerForRace(GameRace race) {
		for (GamePlayer player : players) {
			if (player.race == race) {
				return player;
			}
		}
		//throw new IllegalStateException("No player for race: " + race);
		return null;
	}
	/**
	 * Convinience method to return the current game year.
	 * @return the current game year
	 */
	public int getYear() {
		return calendar.get(GregorianCalendar.YEAR);
	}
	/**
	 * Convinience method to return the current game month.
	 * @return the current game month
	 */
	public int getMonth() {
		return calendar.get(GregorianCalendar.MONTH);
	}
	/**
	 * Convinience method to return the current game day.
	 * @return the current game day
	 */
	public int getDay() {
		return calendar.get(GregorianCalendar.DATE);
	}
	/**
	 * Convinience method to return the current game hour.
	 * @return the current game hour 0-23
	 */
	public int getHour() {
		return calendar.get(GregorianCalendar.HOUR_OF_DAY);
	}
	/**
	 * Convinience method to return the current game minute.
	 * @return the current game minute 0-59
	 */
	public int getMinute() {
		return calendar.get(GregorianCalendar.MINUTE);
	}
	/**
	 * Returns the translation for the current language.
	 * @param key the key
	 * @return the translation
	 */
	public String getLabel(String key) {
		return labels.get(key, language);
	}
	/**
	 * Returns a label which is a format specification and passes the objects
	 * to String.format.
	 * @param key the key
	 * @param params the format parameters
	 * @return the translation
	 */
	public String getLabel(String key, Object... params) {
		return String.format(getLabel(key), params);
	}
	/**
	 * Returns an iterable of the player owned planets.
	 * @return the iterable
	 */
	public List<GamePlanet> getPlayerPlanets() {
		// i wish we had yield return
		List<GamePlanet> result = new ArrayList<GamePlanet>();
		for (GamePlanet p : planets) {
			if (p.owner == player) {
				result.add(p);
			}
		}
		return result;
	}
	/**
	 * A convinience method to associate each
	 * planet with its corresponding owner's internal
	 * known* sets. Only planets with direct ownership
	 * is assigned this way (e.g. the planets discovered
	 * by radar is not covered here).
	 */
	public void setPlanetOwnerships() {
		for (GamePlanet p : planets) {
			if (p.owner != null) {
				p.owner.possessPlanet(p);
			}
		}
	}
	/**
	 * A convinience method to assign fleets to their
	 * respective ovners ownFleet set.
	 */
	public void setFleetOwnerships() {
		for (GameFleet f : fleets) {
			if (f.owner != null) {
				f.owner.possessFleet(f);
			}
		}
	}
	/**
	 * @return the list of knwon planets with owner
	 */
	public List<GamePlanet> getKnownPlanetsByWithOwner() {
		List<GamePlanet> planets = new ArrayList<GamePlanet>(player.knownPlanetsByName);
		// remove planets without owner
		for (int i = planets.size() - 1; i >= 0; i--) {
			if (planets.get(i).owner == null) {
				planets.remove(i);
			}
		}
		Collections.sort(planets, GamePlanet.BY_RACE_ID_AND_NAME);
		return planets;
	}
	/**
	 * @return the list of known planets ordered by coordinates
	 */
	public List<GamePlanet> getOwnPlanetsByCoords() {
		List<GamePlanet> planets = new ArrayList<GamePlanet>(player.ownPlanets);
		Collections.sort(planets, GamePlanet.BY_COORDINATES);
		return planets;
	}
	/**
	 * @return the list of own planets ordered by name
	 */
	public List<GamePlanet> getOwnPlanetsByName() {
		List<GamePlanet> planets = new ArrayList<GamePlanet>(player.ownPlanets);
		Collections.sort(planets, GamePlanet.BY_NAME_ASC);
		return planets;
	}
	/**
	 * @return the list of known fleets
	 */
	public List<GameFleet> getKnownFleets() {
		List<GameFleet> fleets = new ArrayList<GameFleet>(player.knownFleets);
		Collections.sort(fleets, GameFleet.BY_RACE_ID_AND_NAME);
		return fleets;
	}
	/**
	 * @return the list of own fleets ordered by coordinates
	 */
	public List<GameFleet> getOwnFleetsByCoords() {
		List<GameFleet> fleets = new ArrayList<GameFleet>(player.ownFleets);
		Collections.sort(fleets, GameFleet.BY_COORDINATES);
		return fleets;
	}
	/**
	 * @return the list of own fleets ordered by name
	 */
	public List<GameFleet> getOwnFleetsByName() {
		List<GameFleet> fleets = new ArrayList<GameFleet>(player.ownFleets);
		Collections.sort(fleets, GameFleet.BY_RACE_ID_AND_NAME);
		return fleets;
	}
	/**
	 * @return a list of building prototypes available for the current player.
	 */
	public List<GameBuildingPrototype> getPlayerBuildingPrototypes() {
		return getTechIdBuildingPrototypes(player.race.techId);
	}
	/**
	 * Returns a list of buildins which are available in the supplied technology id.
	 * @param techId the technology id
	 * @return a list of building prototypes available for the current player.
	 */
	public List<GameBuildingPrototype> getTechIdBuildingPrototypes(String techId) {
		List<GameBuildingPrototype> result = new ArrayList<GameBuildingPrototype>();
		for (GameBuildingPrototype bp : buildingPrototypes) {
			if (bp.images.containsKey(techId) && (bp.researchTech == null || player.availableTechnology.contains(bp.researchTech))) {
				result.add(bp);
			}
		}
		Collections.sort(result, GameBuildingPrototype.BY_INDEX);
		return result;
	}
	/**
	 * Check if a new instance of the given prototype can be built on the selected planet.
	 * @param bp the prototype
	 * @return true if the building can be built
	 */
	public boolean isBuildableOnPlanet(GameBuildingPrototype bp) {
		GamePlanet planet = player.selectedPlanet;
		// allow build on player owned planets
		if (planet.owner != player) {
			return false;
		}
		// planet type check
		if (bp.notBuildableSurfaces.contains(planet.surfaceType)) {
			return false;
		}
		// build only if there is a main building already
		if (!GameBuildingPrototype.MAIN_BUILDING.equals(bp.kind)) {
			Set<GameBuilding> bs = planet.buildingKinds.get(GameBuildingPrototype.MAIN_BUILDING);
			if (bs == null || bs.size() == 0) {
				return false;
			}
		}
		// for fixed number types, check limit
		if (bp.limitType == BuildLimit.FIXED_NUMBER_PER_PLANET) {
			Set<GameBuilding> bs = planet.buildingTypes.get(bp);
			return bs == null || bs.size() < bp.limitValue;
		}
		// for fixed number per kind, check limit
		if (bp.limitType == BuildLimit.FIXED_KIND_PER_PLANET) {
			Set<GameBuilding> bs = planet.buildingKinds.get(bp.kind);
			return bs == null || bs.size() < bp.limitValue;
		}
		return true;
	}
	/**
	 * {@inheritDoc}
	 */
	public GameBuildingPrototype getBuildingPrototype(String buildingId) {
		return buildingPrototypesMap.get(buildingId);
	}
	/**
	 * Allocates the resources on all planets for the first time.
	 */
	public void allocateResources() {
		for (GamePlanet p : planets) {
			p.update();
			if (p.owner != null) {
				ResourceAllocator.allocateWorkers(p);
				p.update();
				ResourceAllocator.allocateEnergy(p);
				p.update();
			}
		}
		
	}
	/**
	 * Assign the appropriate technology to various researchable buildings.
	 */
	public void assignBuildingToResearch() {
		for (GameBuildingPrototype bp : buildingPrototypes) {
			if (bp.research != null && bp.research.length() > 0) {
				bp.researchTech = research.get(bp.research);
				if (bp.researchTech == null) {
					throw new RuntimeException(String.format("Building %s research %s not found", bp.id, bp.research));
				}
			}
		}
	}
	/** Assign the technologies to the players based on the current global technology level. */
	public void assignTechnologyToPlayers() {
		for (GamePlayer player : players) {
			for (ResearchTech rt : research.values()) {
				if (rt.techIDs.contains(player.race.techId) && rt.level <= level) {
					player.knownTechnology.add(rt);
					// set only the level0 technology as available, the rest is just known
					if (rt.level == 0) {
						player.availableTechnology.add(rt);
					}
				}
			}
		}
	}
	/**
	 * Returns the current player's research list grouped by class, type.
	 * @return the list of research within a type within a class
	 */
	public List<List<List<ResearchTech>>> getPlayerResearchList() {
		List<ResearchTech> rtl = new ArrayList<ResearchTech>(player.knownTechnology);
		Collections.sort(rtl, ResearchTech.BY_IMAGE_INDEX);
		int lastClazz = 0;
		int lastType = 0;
		List<List<List<ResearchTech>>> result = JavaUtils.newArrayList();
		List<ResearchTech> currType = null;
		List<List<ResearchTech>> currClass = null;
		for (ResearchTech rt : rtl) {
			if (lastClazz != rt.clazzIndex) {
				lastClazz = rt.clazzIndex;
				lastType = 0;
				currClass = JavaUtils.newArrayList();
				result.add(currClass);
			}
			if (lastType != rt.typeIndex) {
				lastType = rt.typeIndex;
				currType = JavaUtils.newArrayList();
				currClass.add(currType);
			}
			currType.add(rt);
		}
		return result;
	}
	/**
	 * Returns the inventory count of the given technology for the current player.
	 * @param rt the technology
	 * @return the number of items in inventory
	 */
	public int getInventoryCount(ResearchTech rt) {
		return 0; // TODO inventory!
	}
	/**
	 * Checks whether the given research can be started.
	 * @param rt the research
	 * @return true if all prerequisites are already researched
	 */
	public boolean isResearchable(ResearchTech rt) {
		boolean researchable = true;
		for (ResearchTech d : rt.requires) {
			if (!player.availableTechnology.contains(d)) {
				researchable = false;
			}
		}
		return researchable;
	}
	/**
	 * Select the first planet for the current player if there is no planet selected.
	 */
	public void selectFirstPlanet() {
		if (player.selectedPlanet == null) {
			List<GamePlanet> pl = getOwnPlanetsByName();
			if (pl.size() > 0) {
				player.selectedPlanet = pl.get(0);
			}
		}

	}
	/**
	 * Convinient method to test if the given research is available for the current player.
	 * @param rt the research to test
	 * @return true if the research is available
	 */
	public boolean isAvailable(ResearchTech rt) {
		return player.availableTechnology.contains(rt);
	}
	/**
	 * Selectes a building prototype for the given research technology.
	 * @param rt the research technology
	 */
	public void selectBuildingFor(ResearchTech rt) {
		if (rt != null && "Buildings".equals(rt.clazz)) {
			for (GameBuildingPrototype bp : buildingPrototypes) {
				if (bp.researchTech == rt) {
					player.selectedBuildingPrototype = bp;
					break;
				}
			}
		}
	}
	/**
	 * Is the given research technology currently being researched?
	 * @param rt the research tech
	 * @return true if under research
	 */
	public boolean isActiveResearch(ResearchTech rt) {
		return player.activeResearch != null && player.activeResearch.research == rt;
	}
	/**
	 * Returns true if the current working lab status is more than enough to research the given technology.
	 * @param li the lab info
	 * @param rt the technology to test
	 * @return true if there is enough operational lab to research
	 */
	public boolean isEnoughWorkingLabFor(LabInfo li, ResearchTech rt) {
		return 
			li.currentCivil >= rt.civil
			&& li.currentMechanic >= rt.mechanic
			&& li.currentComputer >= rt.computer
			&& li.currentAi >= rt.ai
			&& li.currentMilitary >= rt.military
		;
	}
	/**
	 * Returns true if the current lab status is more than enough to research the given technology.
	 * @param li the lab info
	 * @param rt the technology to test
	 * @return true if there is enough lab to research
	 */
	public boolean isEnoughLabFor(LabInfo li, ResearchTech rt) {
		return 
			li.totalCivil >= rt.civil
			&& li.totalMechanic >= rt.mechanic
			&& li.totalComputer >= rt.computer
			&& li.totalAi >= rt.ai
			&& li.totalMilitary >= rt.military
		;
	}
}
