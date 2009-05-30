/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.AllocationPreference;
import hu.openig.core.Location;
import hu.openig.core.PlanetInfo;
import hu.openig.core.SurfaceType;
import hu.openig.core.TaxRate;
import hu.openig.core.TileFragment;
import hu.openig.utils.JavaUtils;
import hu.openig.utils.XML;
import hu.openig.utils.XML.XmlProcessor;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
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
public class GamePlanet implements PlanetInfo {
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
	/** Rendering helper to cache the planet name
	 * for the starmap. To re-render it, set this to null. */
	public BufferedImage nameImage;
	/** The owner of planet at the time the name image was created. */
	public GamePlayer nameOwner;
	/** 
	 * The planetary list image used to render the name.
	 * Used only by non-player planets, because they don't flash the name
	 * on a planetary problem.
	 */
	public BufferedImage planetListImage;
	/** The owner of the planet when the planetListImage was rendered. */
	public GamePlayer planetListImageOwner;
	/** 
	 * Indicator for the planet renderer to place buildings on the map when first
	 * displaying the surface.
	 */
	public boolean placeBuildings = true;
	/**
	 * The currently selected building on this planet.
	 */
	public GameBuilding selectedBuilding;
	/** The energy allocation preference. */
	public AllocationPreference energyAllocation = AllocationPreference.UNIFORM;
	/** The worker allocation preference. */
	public AllocationPreference workerAllocation = AllocationPreference.UNIFORM;
	/** 
	 * The user-buildings on the map mapped via X,Y location for each of its entire rectangular base surface (e.g a 2x2 tile will have 4 entries in this map).
	 * Moving buildings around is not allowed. 
	 */
	public final Map<Location, TileFragment> map = JavaUtils.newHashMap();
	/** The set of buildings on the planet. */
	public final Set<GameBuilding> buildings = new HashSet<GameBuilding>();
	/** Map for building prototype to instances of that building. */
	public final Map<GameBuildingPrototype, Set<GameBuilding>> buildingTypes = JavaUtils.newHashMap();
	/** Map for building kind to instances of that building. */
	public final Map<String, Set<GameBuilding>> buildingKinds = JavaUtils.newHashMap();
	/** Whenever there is a change in the contents, this counter gets incremented. */
	private long updateCounter;
	/** The current planet status record. */
	private PlanetStatus planetStatus = new PlanetStatus();
	/** The update counter value when the planet status was last evaluated. */
	private long planetStatusCounter = -1L;
	/** The rotation phases for a given zoom level. */
	public Map<Integer, List<BufferedImage>> rotations;
	/** Indicate that the planet state has changed. */
	public void update() {
		updateCounter++;
	}
	/**
	 * Parses and processes a planetary resource XML.
	 * @param resource the name of the resource
	 * @param lookup the game race lookup
	 * @return list of planets
	 */
	public static List<GamePlanet> parse(String resource, final GamePlanetLookup lookup) {
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
	private static List<GamePlanet> process(Document root, GamePlanetLookup lookup) {
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
			
			Element be = XML.childElement(planet, "buildings");
			// planets without population don't allow prebuilt planets!
			if (be != null && p.populationRace != null) {
				for (Element b : XML.childrenWithName(be, "building")) {
					GameBuilding gb = new GameBuilding();
					String bid = XML.childValue(b, "id");
					gb.prototype = lookup.getBuildingPrototype(bid);
					gb.images = gb.prototype.images.get(p.populationRace.techId);
					// keep building only if it is supported for the current planet techid
					if (gb.images != null) {
						gb.health = Integer.parseInt(XML.childValue(b, "health"));
						gb.progress = Integer.parseInt(XML.childValue(b, "progress"));
						gb.x = Integer.parseInt(XML.childValue(b, "x"));
						gb.y = Integer.parseInt(XML.childValue(b, "y"));
						gb.enabled = "true".equals(XML.childValue(b, "enabled"));
						
						p.addBuilding(gb);
					}
				}
			}
			p.rotations = lookup.getRotations(p.surfaceType.planetString);
			p.update();
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
	 * @return the energy consumption in kWh
	 */
	public int getEnergyDemand() {
		return getStatus().energyDemand;
	}
	/**
	 * @return the maximum energy output in kWh
	 */
	public int getEnergyProduction() {
		return getStatus().energyProduction;
	}
	/**
	 * @return return the radar radius
	 */
	public int getRadarRadius() {
		return getStatus().radar;
	}
	/**
	 * @return returns the total worker demand on the planet
	 */
	public int getWorkerDemand() {
		return getStatus().workerDemand;
	}
	/**
	 * @return record representing various planetary statuses
	 */
	public PlanetStatus getStatus() {
		if (planetStatusCounter != updateCounter) {
			planetStatus = new PlanetStatus();
			planetStatusCounter = updateCounter;
			planetStatus.population = population;
			for (GameBuilding b : buildings) {
				planetStatus.energyDemand += b.getEnergyDemand();
				planetStatus.workerDemand += b.getWorkerDemand();
				if (b.getOperationPercent() >= 0.5) {
					Integer value = b.prototype.values.get("energy-prod");
					if (value != null) {
						planetStatus.energyProduction += value.intValue() * b.getOperationPercent();
					}
					value = b.prototype.values.get("food");
					if (value != null && b.getOperationPercent() >= 0.5) {
						planetStatus.food += value;
					}
					value = b.prototype.values.get("hospital");
					if (value != null && b.getOperationPercent() >= 0.5) {
						planetStatus.hospital += value;
					}
					value = b.prototype.values.get("living-space");
					if (value != null && b.getOperationPercent() >= 0.5) {
						planetStatus.livingSpace += value;
					}
					value = b.prototype.values.get("radar");
					if (value != null) {
						int v = value;
						if (v > planetStatus.radar) {
							planetStatus.radar = v;
						}
					}
				}
			}
		}
		return planetStatus;
	}
	/**
	 * Convinience method to check if a planet has any problems with
	 * its statistics (e.g. low energy, living space, hospital, etc.)
	 * @return true if there is any problem
	 */
	public boolean hasProblems() {
		PlanetStatus ps = getStatus();
		return population > ps.livingSpace || population > ps.hospital
		|| population > ps.food || population < ps.workerDemand 
		|| ps.energyProduction < ps.energyDemand;
	}
	/**
	 * Returns the number of buildings on the planet belonging to
	 * the given prototype.
	 * @param bp the building prototype, not null
	 * @return the number of buildings
	 */
	public int getCountOfBuilding(GameBuildingPrototype bp) {
		int sum = 0;
		for (GameBuilding b : buildings) {
			if (b.prototype == bp) {
				sum++;
			}
		}
		return sum;
	}
	/**
	 * Adds a new building to the internal building collections.
	 * @param building the building, not null
	 */
	public void addBuilding(GameBuilding building) {
		buildings.add(building);
		
		Set<GameBuilding> bs = buildingTypes.get(building.prototype);
		if (bs == null) {
			bs = new HashSet<GameBuilding>();
			buildingTypes.put(building.prototype, bs);
		}
		bs.add(building);

		Set<GameBuilding> bk = buildingKinds.get(building.prototype.kind);
		if (bk == null) {
			bk = new HashSet<GameBuilding>();
			buildingKinds.put(building.prototype.kind, bs);
		}
		bk.add(building);
	}
	/**
	 * Removes the building from the internal collections.
	 * @param building the building to remove, not null
	 */
	public void removeBuilding(GameBuilding building) {
		buildings.remove(building);
		Set<GameBuilding> bs = buildingTypes.get(building.prototype);
		if (bs != null) {
			bs.remove(building);
		}
		Set<GameBuilding> bk = buildingKinds.get(building.prototype.kind);
		if (bk != null) {
			bk.remove(building);
		}
		for (Map.Entry<Location, TileFragment> e : new LinkedList<Map.Entry<Location, TileFragment>>(map.entrySet())) {
			if (e.getValue().provider == building) {
				map.remove(e.getKey());
			}
		}
		// notify clients that the planet needs building-re placement
		placeBuildings = true;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public SurfaceType getSurfaceType() {
		return surfaceType;
	}
}
