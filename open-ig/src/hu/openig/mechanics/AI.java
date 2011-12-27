/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Location;
import hu.openig.model.AIAttackMode;
import hu.openig.model.AIControls;
import hu.openig.model.AIManager;
import hu.openig.model.AIWorld;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.DiplomaticInteraction;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.InventoryItem;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.Production;
import hu.openig.model.Research;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.ResponseMode;
import hu.openig.model.SelectionMode;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarStructure.StructureType;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.TileSet;
import hu.openig.model.World;
import hu.openig.utils.JavaUtils;
import hu.openig.utils.XElement;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The general artificial intelligence to run generic starmap-planet-production-research operations.
 * @author akarnokd, 2011.12.08.
 */
public class AI implements AIManager, AIControls {
	/** The world. */
	World w;
	/** The player. */
	Player p;
	/** The copy of world state. */
	AIWorld world;
	/** 
	 * AI players won't start colonization until the player has actually researched its colony ship.
	 * To avoid the headstart problem in skirmish mode. 
	 */
	boolean playerColonyShipAvailable;
	/** Set of fleets which will behave as defenders in the space battle. */
	final Set<Integer> defensiveTask = JavaUtils.newHashSet();
	/** The estimations about how strong the other player's fleets are. */
	final Map<String, PlayerFleetStrength> strengths = JavaUtils.newHashMap();
	/** The set of cells undiscovered on the starmap. */
	final Set<Location> explorationMap = JavaUtils.newHashSet();
	/** The cell size used for the exploration map cells. */
	int explorationCellSize;
	/** The number of rows of the exploration map. */
	int explorationRows;
	/** The number of columns of the exploration map. */
	int explorationColumns;
	/** The list of actions to apply. */
	final List<Action0> applyActions = new ArrayList<Action0>();
	/**
	 * Knowledge about a player's typical fleet strength based on encounters.
	 * @author akarnokd, 2011.12.20.
	 */
	public static class PlayerFleetStrength {
		/** The player ID. */
		public String playerID;
		/** The attack value. */
		public double attack;
		/** The defense value. */
		public double defense;
	}
	@Override
	public void init(Player p) {
		this.w = p.world;
		this.p = p;
		explorationCellSize = (int)Math.floor(Math.sqrt(2) * w.env.params().fleetRadarUnitSize()) - 4;
		explorationRows = (int)Math.ceil(w.galaxyModel.map.getHeight() / explorationCellSize);
		explorationColumns = (int)Math.ceil(w.galaxyModel.map.getWidth() / explorationCellSize);
		initExplorationMap();
	}
	@Override
	public void prepare() {
		world = new AIWorld();
		world.assign(p);
		playerColonyShipAvailable = w.player.colonyShipAvailable;
		updateExplorationMap();
	}
	
	@Override
	public void manage() {
		// TODO Auto-generated method stub
		researchPlanner();
	}
	@Override
	public void apply() {
		for (Action0 a : applyActions) {
			a.invoke();
		}
		applyActions.clear();
		world = null;
	}
	@Override
	public void actionStartResearch(ResearchType rt, double moneyFactor) {
		if (p.runningResearch != null) {
			Research r = p.research.get(p.runningResearch);
			r.state = ResearchState.STOPPED;
			log("PauseResearch, Type = %s", r.type.id);
		}
		p.runningResearch = rt;
		Research r = p.research.get(p.runningResearch);
		if (r == null) {
			r = new Research();
			r.type = rt;
			r.remainingMoney = r.type.researchCost;
			r.assignedMoney = (int)(r.type.researchCost * moneyFactor);
			p.research.put(r.type, r);
		} else {
			r.assignedMoney = (int)(r.remainingMoney * moneyFactor);
		}
		r.state = ResearchState.RUNNING;
		log("StartResearch, Type = %s, MoneyFactor = %s", rt.id, moneyFactor);
	}
	@Override
	public void actionDeployFleet(
			Planet location,
			double loadFactor, 
			double powerFactor, 
			Iterable<ResearchType> items) {
		// TODO implement
		Fleet fleet = new Fleet();
		fleet.id = w.fleetIdSequence++;
		fleet.name = w.env.labels().get(p.race + ".new_fleet_name");
		fleet.x = location.x;
		fleet.y = location.y;

		// TODO implement

		
		
		p.fleets.put(fleet, FleetKnowledge.FULL);
	}
	@Override
	public void actionStartProduction(ResearchType rt, int count, int priority) {
		Map<ResearchType, Production> prodLine = p.production.get(rt.category.main);
		Production prod = prodLine.get(rt);
		if (prod == null) {
			prod = new Production();
			prod.type = rt;
			prodLine.put(rt, prod);
		}
		prod.priority = priority;
		prod.count += count;
		log("StartProduction, Type = %s, Count = %s, Priority = %s", rt.id, count, priority);
	}
	@Override
	public void actionPauseProduction(ResearchType rt) {
		Map<ResearchType, Production> prodLine = p.production.get(rt.category.main);
		Production prod = prodLine.get(rt);
		if (prod != null) {
			prod.priority = 0;
		}
		log("PauseProduction, Type = %s", rt.id);
	}
	@Override
	public void actionPlaceBuilding(Planet planet, BuildingType buildingType) {
		// TODO implement
		if (p.money >= buildingType.cost) {
			TileSet ts = buildingType.tileset.get(planet.race);
			Point pt = planet.surface.findLocation(ts.normal.width + 2, ts.normal.height + 2);
			if (pt != null) {
				Simulator.doConstruct(w, planet, buildingType, pt);
				log("placeBuilding, Planet = %s, Type = %s", planet.id, buildingType.id);
			} else {
				log("PlaceBuilding, Planet = %s, Type = %s, FAIL = no room", planet.id, buildingType.id);
			}
		} else {
			log("PlaceBuilding, Planet = %s, Type = %s, FAIL = no money", planet.id, buildingType.id);
		}
	}
	@Override
	public void actionDemolishBuilding(Planet planet, Building b) {
		demolishBuilding(w, planet, b);
		log("DemolishBuilding, Planet = %s, Building = %s", planet.id, b.type.id);
	}
	/**
	 * Demolish a building on the given planet, update statistics and get some money back.
	 * @param world the world object
	 * @param planet the target planet
	 * @param building the target building
	 */
	public static void demolishBuilding(World world, Planet planet, Building building) {
		planet.surface.removeBuilding(building);
		planet.surface.placeRoads(planet.race, world.buildingModel);
		
		int moneyBack = building.type.cost * (1 + building.upgradeLevel) / 2;
		
		planet.owner.money += moneyBack;
		
		planet.owner.statistics.demolishCount++;
		planet.owner.statistics.moneyDemolishIncome += moneyBack;
		planet.owner.statistics.moneyIncome += moneyBack;

		world.statistics.demolishCount++;
		world.statistics.moneyDemolishIncome += moneyBack;
		world.statistics.moneyDemolishIncome += moneyBack;

	}
	@Override
	public void actionUpgradeBuilding(Planet planet, Building building, int newLevel) {
		// TODO implement
		if (Simulator.doUpgrade(w, planet, building, newLevel)) {
			log("UpgradeBuilding, Planet = %s, Building = %s, NewLevel = %s", planet.id, building.type.id, newLevel);
		} else {
			log("UpgradeBuilding, Planet = %s, Building = %s, NewLevel = %s, FAILED = level limit", planet.id, building.type.id, newLevel);
		}
		
	}
	@Override
	public void actionDeployUnits(double loadFactor, double powerFactor, Planet planet, Iterable<ResearchType> items) {
		// TODO implement
	}
	@Override
	public void actionAttackPlanet(Fleet fleet, Planet planet, AIAttackMode mode) {
		// TODO implement
	}
	@Override
	public void actionAttackFleet(Fleet fleet, Fleet enemy, boolean defense) {
		// TODO implement
	}
	@Override
	public void actionMoveFleet(Fleet fleet, double x, double y) {
		// TODO implement
		fleet.targetFleet = null;
		fleet.targetPlanet(null);
		fleet.waypoints.clear();
		fleet.waypoints.add(new Point2D.Double(x, y));
		fleet.mode = FleetMode.MOVE;
		log("MoveFleet, Fleet = %s, Location = %s;%s", fleet.name, x, y);
	}
	@Override
	public void actionColonizePlanet(Fleet fleet, Planet planet) {
		if (fleet.getStatistics().planet == planet) {
			if (colonizeWithFleet(fleet, planet)) {
				log("ColonizePlanet, Fleet = %s, Planet = %s", fleet.name, planet.id);
			} else {
				log("ColonizePlanet, Fleet = %s, Planet = %s, FAILED = no room", fleet.name, planet.id);
			}
		} else {
			log("ColonizePlanet, Fleet = %s, Planet = %s, FAILED = not close enough", fleet.name, planet.id);
		}
	}
	/**
	 * Apply colonizationc changes to the given planet and reduce the colony ship count in the fleet.
	 * @param f the fleet
	 * @param p the planet
	 * @return true if colonization successful
	 */
	public static boolean colonizeWithFleet(Fleet f, Planet p) {
		World w = f.owner.world;
		for (BuildingType bt : w.buildingModel.buildings.values()) {
			if ("MainBuilding".equals(bt.kind)) {
				TileSet ts = bt.tileset.get(f.owner.race);
				if (ts != null) {
					Point pt = p.surface.findLocation(ts.normal.width + 2, ts.normal.height + 2);
					if (pt != null) {
						// remove colony ship from fleet
						f.changeInventory(w.researches.get("ColonyShip"), -1);
						
						// remove empty fleet
						if (f.inventory.isEmpty()) {
							w.removeFleet(f);
							List<Fleet> of = f.owner.ownFleets();
							if (of.size() > 0) {
								f.owner.currentFleet = of.iterator().next();
							} else {
								f.owner.currentFleet = null;
								f.owner.selectionMode = SelectionMode.PLANET;
							}
						}
						// place building
						Building b = new Building(bt, f.owner.race);
						p.owner = f.owner;
						p.race = f.owner.race;
						p.population = 5000;
						p.morale = 50;
						p.lastMorale = 50;
						p.lastPopulation = 5000;
						b.location = Location.of(pt.x + 1, pt.y - 1);
						
						p.surface.placeBuilding(ts.normal, b.location.x, b.location.y, b);
						p.surface.placeRoads(p.owner.race, w.buildingModel);
						
						p.owner.planets.put(p, PlanetKnowledge.BUILDING);
						p.owner.currentPlanet = p;
						
						p.owner.statistics.planetsColonized++;
						
						// uninstall satellites
						p.removeOwnerSatellites();
						
						return true;
					} else {
						System.err.printf("Could not colonize planet %s, not enough initial space for colony hub of race %s.", p.id, f.owner.race);
					}
				}
			}
		}
		return false;
	}
	@Override
	public void actionDiplomaticInteraction(Player other, DiplomaticInteraction offer) {
		// TODO implement
	}
	
	@Override
	public void actionRepairBuilding(Planet planet, Building building,
			boolean repair) {
		building.repairing = repair;
		log("RepairBuilding, Planet = %s, Building = %s, Repair = %s", planet.id, building.type.id, repair);
	}
	
	/**
	 * Display the action log.
	 * @param message the message
	 */
	void log(String message) {
		System.out.printf("AI:%s:%s%n", p.id, message);
	}
	/**
	 * Display the action log.
	 * @param message the message
	 * @param values the message parameters
	 */
	void log(String message, Object... values) {
		System.out.printf("AI:%s:");
		System.out.printf(message, values);
		System.out.println();
	}
	
	@Override
	public ResponseMode diplomacy(Player other,
			DiplomaticInteraction offer) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SpacewarAction spaceBattle(SpacewarWorld world, List<SpacewarStructure> idles) {
		if (idles.size() == 0) {
			return SpacewarAction.CONTINUE;
		}
		// FIXME make more sophisticated
		
		double health = fleetHealth(world.structures(p));
		double switchToCostAttack = p.aiDefensiveRatio / (p.aiOffensiveRatio + p.aiDefensiveRatio);
		double switchToFlee = p.aiSocialRatio() * switchToCostAttack;
		
		if (health >= switchToFlee) {
			boolean c = health < switchToCostAttack || defensiveTask.contains(idles.get(0).fleet);
			for (SpacewarStructure ship : idles) {
				if (c) {
					costAttackBehavior(world, ship);
				} else {
					defaultAttackBehavior(world, ship);
				}
			}
			return SpacewarAction.CONTINUE;
		}
		return SpacewarAction.FLEE;
	}
	/**
	 * Calculate the percentage of the structure healths.
	 * @param structures the structures
	 * @return the health between 0..1
	 */
	public static double fleetHealth(List<SpacewarStructure> structures) {
		double hpTotal = 0.0;
		double hpActual = 0.0;
		for (SpacewarStructure s : structures) {
			hpTotal += s.hpMax + s.shieldMax;
			hpActual += s.hp + s.shield;
		}
		return hpActual / hpTotal;
	}
	/**
	 * Orders the given structure to attack a random enemy.
	 * @param world the world
	 * @param ship the ship
	 */
	public static void defaultAttackBehavior(SpacewarWorld world,
			SpacewarStructure ship) {
		if (ship.type == StructureType.STATION 
				|| ship.type == StructureType.PROJECTOR) {
			ship.guard = true;
			List<SpacewarStructure> es = world.enemiesInRange(ship);
			if (es.size() > 0) {
				ship.attack = world.random(es);
			}
		} else
		if (ship.type == StructureType.SHIP) {
			List<SpacewarStructure> es = world.enemiesInRange(ship);
			if (es.size() > 0) {
				ship.attack = world.random(es);
			} else {
				es = world.enemiesOf(ship);
				if (es.size() > 0) {
					ship.attack = world.random(es);
				}
			}
		}
	}
	/**
	 * Orders the given structure to attack the highest cost target.
	 * <p>May be employed by a defender AI to stop a fleet which tries to conquer a planet.</p>
	 * @param world the world
	 * @param ship the ship
	 */
	public static void costAttackBehavior(SpacewarWorld world, 
			SpacewarStructure ship) {
		if (ship.type == StructureType.STATION 
				|| ship.type == StructureType.PROJECTOR) {
			ship.guard = true;
			ship.attack = highestCost(world.enemiesInRange(ship), ship);
		} else
		if (ship.type == StructureType.SHIP) {
			ship.attack = highestCost(world.enemiesOf(ship), ship);
		}
	}
	/**
	 * Find the highest cost enemy target.
	 * @param enemies the list of enemies
	 * @param ship the current ship
	 * @return the new target or null if no targets remain
	 */
	public static SpacewarStructure highestCost(List<SpacewarStructure> enemies, SpacewarStructure ship) {
		if (enemies.isEmpty()) {
			return null;
		}
		return Collections.max(enemies, new Comparator<SpacewarStructure>() {
			@Override
			public int compare(SpacewarStructure o1, SpacewarStructure o2) {
				double d1 = o1.hp + o1.shield;
				double d1m = o1.hpMax + o1.shieldMax;
				double d2 = o2.hp + o2.shield;
				double d2m = o2.hpMax + o2.shieldMax;
				
				double c1 = o1.value * d1m * d2;
				double c2 = o2.value * d2m * d1;
				return c1 < c2 ? -1 : (c1 > c2 ? 1 : 0);
			}
		});
	}
	@Override
	public void groundBattle(BattleInfo battle) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void groundBattleDone(BattleInfo battle) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void groundBattleInit(BattleInfo battle) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void spaceBattleDone(SpacewarWorld world) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void spaceBattleInit(SpacewarWorld world) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void load(XElement in) {
		for (XElement xf : in.childrenWithName("task-defensive")) {
			int fid = xf.getInt("fleet");
			defensiveTask.add(fid);
		}
		// restore exploration map
		for (XElement xloc : in.childrenWithName("exploration-map")) {
			explorationMap.clear();
			String coords = xloc.get("coords");
			for (String xys : coords.split("\\s+")) {
				String[] xy = xys.split(";");
				Location loc = Location.of(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
				explorationMap.add(loc);
			}
		}
	}
	@Override
	public void save(XElement out) {
		for (Integer f : defensiveTask) {
			XElement xf = out.add("task-defensive");
			xf.set("fleet", f);
		}
		XElement xloc = out.add("exporation-map");
		StringBuilder coords = new StringBuilder();
		for (Location loc : explorationMap) {
			if (coords.length() > 0) {
				coords.append(" ");
			}
			coords.append(loc.x).append(";").append(loc.y);
		}
		xloc.set("coords", coords);
	}
	@Override
	public void onResearchStateChange(ResearchType rt, ResearchState state) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProductionComplete(ResearchType rt) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onDiscoverPlanet(Planet planet) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onDiscoverFleet(Fleet fleet) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onDiscoverPlayer(Player player) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onFleetArrivedAtPoint(Fleet fleet, double x, double y) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onFleetArrivedAtPlanet(Fleet fleet, Planet planet) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onFleetArrivedAtFleet(Fleet fleet, Fleet other) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onBuildingComplete(Planet planet, Building building) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onLostSight(Fleet fleet) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onLostTarget(Fleet fleet, Fleet target) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onNewDay() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSatelliteDestroyed(Planet planet, InventoryItem ii) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPlanetDied(Planet planet) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPlanetRevolt(Planet planet) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPlanetConquered(Planet planet, Player lastOwner) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPlanetLost(Planet planet) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Set all cells to undiscovered.
	 */
	void initExplorationMap() {
		for (int x = 0; x < explorationColumns; x++) {
			for (int y = 0; y < explorationRows; y++) {
				Location loc = Location.of(x, y);
				explorationMap.add(loc);
			}
		}
	}
	@Override
	public void onRadar() {
	}
	/**
	 * Update the exploration map by removing cells covered by current radar.
	 */
	void updateExplorationMap() {
		for (Planet planet : p.planets.keySet()) {
			if (planet.owner == p && planet.radar > 0) {
				int cx = planet.x;
				int cy = planet.y;
				int r = planet.radar;
				
				removeCoverage(cx, cy, r);
			}
		}
		for (Fleet fleet : p.fleets.keySet()) {
			if (fleet.owner == p && fleet.radar > 0) {
				removeCoverage((int)fleet.x, (int)fleet.y, fleet.radar);
			}
		}
	}
	/**
	 * Remove the covered exploration cells.
	 * @param cx the circle center
	 * @param cy the circle center
	 * @param r the circle radius
	 */
	void removeCoverage(int cx, int cy, int r) {
		// inner rectangle
		int ux1 = (int)Math.ceil(cx - Math.sqrt(2) * r);
		int uy1 = (int)Math.ceil(cy - Math.sqrt(2) * r);
		int ux2 = (int)Math.floor(cx + Math.sqrt(2) * r);
		int uy2 = (int)Math.floor(cy + Math.sqrt(2) * r);
		
		int colStart = (int)Math.ceil(1.0 * ux1 / explorationCellSize);
		int colEnd = (int)Math.floor(1.0 * ux2 / explorationCellSize);
		int rowStart = (int)Math.ceil(1.0 * uy1 / explorationCellSize);
		int rowEnd = (int)Math.floor(1.0 * uy2 / explorationCellSize);
		// remove whole enclosed cells
		for (int x = colStart; x < colEnd; x++) {
			for (int y = rowStart; y < rowEnd; y++) {
				Location loc = Location.of(x, y);
				explorationMap.remove(loc);
			}
		}
	}
	/**
	 * Simple next research planner.
	 */
	void researchPlanner() {
		applyActions.addAll(new ResearchPlanner(world, this).run());
	}
}
