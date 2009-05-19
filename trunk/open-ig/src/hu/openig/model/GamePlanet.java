/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.core.SurfaceType;
import hu.openig.core.TaxRate;
import hu.openig.core.TileFragment;
import hu.openig.utils.JavaUtils;
import hu.openig.utils.XML;
import hu.openig.utils.XML.XmlProcessor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Game model planet record.
 * @author karnokd, 2009.02.02.
 * @version $Revision 1.0$
 */
public class GamePlanet {
	/** The surface type 1-7. */
	public SurfaceType surfaceType;
	/** The surface variant 1-9. */
	public int surfaceVariant;
	/** Rotation phase index on the starmap. */
	public int rotationPhase;
	/** The planet name. */
	public String name;
//	/** The radar circle radius. If zero, no radar should be displayed */
//	public int radarRadius;
	/** Center X coordinate on the 1:1 zoomed starmap. Means (currently) the top-left corner of the planet rectangle. */
	public int x;
	/** Center Y coordinate on the 1:1 zoomed starmap.  Means (currently) the top-left corner of the planet rectangle.*/
	public int y;
	/** Display planet on the starmap? Used to globally enable/disable a planet's visibility */
	public boolean visible;
	/** Rotation direction: true - forward, false - backward. */
	public boolean rotationDirection;
	/** The planet's unique id. */
	public String id;
	/** Owner race. Null indicates no owner. */
	public GamePlayer owner;
	/** Population race name. Null indicates an uninhabited planet. */
	public GameRace populationRace;
	/** Current popularity. */
	public float popularity;
	/** Population count. */
	public int population;
	/** Last day's population growth. */
	public int populationGrowth;
	/** Object names in orbit. */
	public final List<String> inOrbit = new ArrayList<String>();
	/** The planet size relative to the largest size, e.g. -2, -1, 0 .*/
	public int size;
	/** Lazy initialized location point object. */
	private Point locationPoint;
	/** The tax rate. */
	public TaxRate tax = TaxRate.MODERATE;
	/** The last days tax income. */
	public int taxIncome;
	/** The last days trade income. */
	public int tradeIncome;
	/** The current tax morale percent. */
	public int taxMorale;
	/** 
	 * The user-buildings on the map mapped via X,Y location for each of its entire rectangular base surface (e.g a 2x2 tile will have 4 entries in this map).
	 * Moving buildings around is not allowed. 
	 */
	public final Map<Location, TileFragment> map = new HashMap<Location, TileFragment>();
	/** The set of buildings on the planet. */
	public final Set<GameBuilding> buildings = new HashSet<GameBuilding>();
	/**
	 * Parses and processes a planetary resource XML.
	 * @param resource the name of the resource
	 * @param lookup the game race lookup
	 * @return list of planets
	 */
	public static List<GamePlanet> parse(String resource, final GameRaceLookup lookup) {
		List<GamePlanet> planet = XML.parseResource(resource, new XmlProcessor<List<GamePlanet>>() {
			@Override
			public List<GamePlanet> process(Document doc) {
				return GamePlanet.process(doc, lookup);
			}
		});
		return planet != null ? planet : new ArrayList<GamePlanet>();
	}
	/**
	 * Processes a planets.xml document.
	 * @param root the document 
	 * @param lookup the game race lookup
	 * @return the list of planets
	 */
	private static List<GamePlanet> process(Document root, GameRaceLookup lookup) {
		List<GamePlanet> result = new ArrayList<GamePlanet>();
		for (Element planet : XML.childrenWithName(root.getDocumentElement(), "planet")) {
			GamePlanet p = new GamePlanet();
			p.id = planet.getAttribute("id");
			p.name = XML.childValue(planet, "name");
			p.x = Integer.parseInt(XML.childValue(planet, "location-x")) * 2;
			p.y = Integer.parseInt(XML.childValue(planet, "location-y")) * 2;
			p.surfaceType = SurfaceType.MAP.get(XML.childValue(planet, "type"));
			p.surfaceVariant = Integer.parseInt(XML.childValue(planet, "variant"));
			String ownerRaceStr = XML.childValue(planet, "race");
			if (!ownerRaceStr.isEmpty()) {
				p.populationRace = lookup.getRace(ownerRaceStr);
			}
			p.owner = lookup.getPlayerForRace(p.populationRace);
			
			p.size = Integer.parseInt(XML.childValue(planet, "size")) - 8;
			p.rotationDirection = "RL".equals(XML.childValue(planet, "rotate"));
			p.population = Integer.parseInt(XML.childValue(planet, "populate"));
			String orbit = XML.childValue(planet, "in-orbit");
			p.visible = true;
			p.popularity = 0.5f;
			if (!"-".equals(orbit)) {
				p.inOrbit.addAll(Arrays.asList(orbit.split("\\\\s*,\\\\s*")));
			}
			result.add(p);
		}
		return result;
	}
	/** 
	 * Returns the planet's logical coordinates as point.
	 * @return the logical location as point
	 */
	public Point getPoint() {
		if (locationPoint == null) {
			locationPoint = new Point(x, y);
		}
		return locationPoint;
	}
	/** Planet comparator by name ascending. */
	public static final Comparator<GamePlanet> BY_NAME_ASC = new Comparator<GamePlanet>() {
		@Override
		public int compare(GamePlanet o1, GamePlanet o2) {
			return o1.name.compareTo(o2.name);
		}
	};
	/** Planet comparator by name descending. */
	public static final Comparator<GamePlanet> BY_NAME_DESC = new Comparator<GamePlanet>() {
		@Override
		public int compare(GamePlanet o1, GamePlanet o2) {
			return o2.name.compareTo(o1.name);
		}
	};
	/** Orders by race id then by planet name. */
	public static final Comparator<GamePlanet> BY_RACE_ID_AND_NAME = new Comparator<GamePlanet>() {
		@Override
		public int compare(GamePlanet o1, GamePlanet o2) {
			int diff = o1.owner.race.index - o2.owner.race.index;
			if (diff == 0) {
				diff = JavaUtils.naturalCompare(o1.name, o2.name, false);
			}
			return diff;
		}
	};
	/** Orders planets by Y coordinates, then by X coordinates. */
	public static final Comparator<GamePlanet> BY_COORDINATES = new Comparator<GamePlanet>() {
		@Override
		public int compare(GamePlanet o1, GamePlanet o2) {
			int dy = o1.y - o2.y;
			if (dy == 0) {
				return o1.x - o2.x;
			}
			return dy;
		}
	};
	/**
	 * @return the living space amount in persons
	 */
	public int getLivingSpace() {
		return 0; // TODO evaluate actual living space
	}
	/**
	 * @return the hospital capacity in persons
	 */
	public int getHospital() {
		return 0; // TODO evaluate actual hospital room
	}
	/**
	 * @return the food capacity in persons
	 */
	public int getFood() {
		return 0; // TODO evaluate actual food limit
	}
	/**
	 * @return the energy consumption in kWh
	 */
	public int getEnergy() {
		return 0; // TODO evaluate energy consumption
	}
	/**
	 * @return the maximum energy output in kWh
	 */
	public int getEnergyMax() {
		return 0; // TODO evaluate maximum energy output
	}
	/**
	 * @return return the radar radius
	 */
	public int getRadarRadius() {
		return 50; // properly determine radar radius based on the buildings
	}
	/**
	 * @return returns the total worker demand on the planet
	 */
	public int getWorkers() {
		return 0; // determine total worker count required for buildings
	}
}
