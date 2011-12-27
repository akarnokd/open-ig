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
import hu.openig.model.AIManager;
import hu.openig.model.AIPlanet;
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
import hu.openig.model.Player;
import hu.openig.model.Production;
import hu.openig.model.Research;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.ResponseMode;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The general artificial intelligence to run generic starmap-planet-production-research operations.
 * @author akarnokd, 2011.12.08.
 */
public class AI implements AIManager {
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
	
	/**
	 * Action to start the research.
	 * @param rt the research type
	 * @param moneyFactor the money multiplier for the research
	 */
	public void actionStartResearch(ResearchType rt, double moneyFactor) {
		if (p.runningResearch != null) {
			Research r = p.research.get(p.runningResearch);
			r.state = ResearchState.STOPPED;
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
	}
	/**
	 * Deploy a fleet from the inventory.
	 * @param location the planet where the fleet will emerge
	 * @param loadFactor percentage of 0..1 about how many units to place into fighter/cruiser/battleship slots.
	 * @param powerFactor percentage of 0..1 about how well the units should be equipped
	 * @param items the sequence of items to choose from when filling in the slots
	 */
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
	
	/**
	 * Start the production of the given technology with the amount and priority.
	 * @param rt the technology
	 * @param count number of items to produce
	 * @param priority the production priority
	 */
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
	}
	/**
	 * Pause a production by setting its priority to zero.
	 * @param rt the research type to pause
	 */
	public void actionPauseProduction(ResearchType rt) {
		Map<ResearchType, Production> prodLine = p.production.get(rt.category.main);
		Production prod = prodLine.get(rt);
		if (prod != null) {
			prod.priority = 0;
		}		
	}
	/**
	 * Place a building onto the planet (via the autobuild location mechanism).
	 * @param planet the target planet
	 * @param buildingType the building type
	 */
	public void actionPlaceBuilding(Planet planet, BuildingType buildingType) {
		// TODO implement
		if (p.money >= buildingType.cost) {
			TileSet ts = buildingType.tileset.get(planet.race);
			Point pt = planet.surface.findLocation(ts.normal.width + 2, ts.normal.height + 2);
			if (pt != null) {
				Simulator.doConstruct(w, planet, buildingType, pt);
			}
		}
	}
	/**
	 * Demolish a building.
	 * @param planet the target planet
	 * @param b the building
	 */
	public void actionDemolishBuilding(Planet planet, Building b) {
		demolishBuilding(w, planet, b);
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
	/**
	 * Upgrade a building on the given planet to the given level.
	 * @param planet the target planet
	 * @param building the building
	 * @param newLevel the new level
	 */
	public void actionUpgradeBuilding(Planet planet, Building building, int newLevel) {
		// TODO implement
		Simulator.doUpgrade(w, planet, building, newLevel);
	}
	/**
	 * Deploy units such as tanks, fighters and stations into a planet.
	 * @param loadFactor percentage of 0..1 about how many units to place into slots.
	 * @param powerFactor percentage of 0..1 about how well the units should be equipped
	 * @param planet the target planet
	 * @param items the item types to deploy
	 */
	public void actionDeployUnits(double loadFactor, double powerFactor, Planet planet, Iterable<ResearchType> items) {
		// TODO implement
	}
	
	/**
	 * Use the fleet to attack the planet.
	 * @param fleet the fleet
	 * @param planet the target planet
	 * @param mode the aim of the attack
	 */
	public void actionAttackPlanet(Fleet fleet, Planet planet, AIAttackMode mode) {
		// TODO implement
	}
	/**
	 * Use the fleet to attack the enemy fleet.
	 * @param fleet the fleet
	 * @param enemy the enemy fleet
	 * @param defense indicate if the attack is to stop an advancing enemy
	 */
	public void actionAttackFleet(Fleet fleet, Fleet enemy, boolean defense) {
		// TODO implement
	}
	/**
	 * Move the fleet to the designated coordinates.
	 * @param fleet the fleet
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 */
	public void actionMoveFleet(Fleet fleet, double x, double y) {
		// TODO implement
		fleet.targetFleet = null;
		fleet.targetPlanet(null);
		fleet.waypoints.clear();
		fleet.waypoints.add(new Point2D.Double(x, y));
		fleet.mode = FleetMode.MOVE;
	}
	/**
	 * Send the fleet to colonize the target planet.
	 * @param fleet the fleet
	 * @param planet the planet
	 */
	public void actionColonizePlanet(Fleet fleet, Planet planet) {
		// TODO implement
	}
	/**
	 * Send a diplomatic request/statement to the other player. 
	 * @param other the other player
	 * @param offer the offer
	 */
	public void actionDiplomaticInteraction(Player other, DiplomaticInteraction offer) {
		// TODO implement
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
		if (!p.id.equals("Empire")) {
			return;
		}
		if (world.runningResearch != null) {
			return;
		}
		final Map<ResearchType, Integer> enablesCount = new HashMap<ResearchType, Integer>();
		final Map<ResearchType, Integer> rebuildCount = new HashMap<ResearchType, Integer>();
		List<ResearchType> candidatesImmediate = new ArrayList<ResearchType>();
		List<ResearchType> candidatesReconstruct = new ArrayList<ResearchType>();
		List<ResearchType> candidatesGetMorePlanets = new ArrayList<ResearchType>();
		for (ResearchType rt : world.remainingResearch) {
			if (rt.hasEnoughLabs(world.global)) {
				candidatesImmediate.add(rt);
				setResearchEnables(rt, enablesCount);
			} else
			if (rt.labCount() <= world.ownPlanets.size()) {
				candidatesReconstruct.add(rt);
				setResearchEnables(rt, enablesCount);
				rebuildCount.put(rt, rebuildCount(rt));
			} else {
				candidatesGetMorePlanets.add(rt);
				setResearchEnables(rt, enablesCount);
			}
		}
		if (candidatesImmediate.size() > 0) {
			Collections.sort(candidatesImmediate, new CompareFromMap<ResearchType>(enablesCount));
			final ResearchType rt = candidatesImmediate.get(0);
			double mf = 1.0;
			if (rt.researchCost * 5 >= world.money) {
				mf = 2.0;
			}
			final double moneyFactor = mf; // TODO decision variable
			applyActions.add(new Action0() {
				@Override
				public void invoke() {
					actionStartResearch(rt, moneyFactor);
				}
			});
			return;
		}
		if (candidatesReconstruct.size() > 0) {
			// find the research that requires the fewest lab rebuilds
			Collections.sort(candidatesReconstruct, new CompareFromMap<ResearchType>(rebuildCount));

			final ResearchType rt = candidatesReconstruct.get(candidatesReconstruct.size() - 1);
			
			for (AIPlanet planet : world.ownPlanets) {
				if (planet.statistics.labCount() != planet.statistics.activeLabCount()
						&& !planet.statistics.constructing) {
					buildMorePowerPlant(planet.planet);
					return;
				}
			}
			// find an empty planet
			for (AIPlanet planet : world.ownPlanets) {
				if (planet.statistics.labCount() == 0 && !planet.statistics.constructing) {
					buildOneLabFor(rt, planet);
					return;
				}
			}
			for (AIPlanet planet : world.ownPlanets) {
				if (demolishOneLabFor(rt, planet)) {
					return;
				}
			}
			return;
		}
		if (candidatesGetMorePlanets.size() > 0) {
			Collections.sort(candidatesGetMorePlanets, new CompareFromMap<ResearchType>(rebuildCount));
			// TODO this is more complicated
		}
	}
	/**
	 * Build or upgade a power plant on the planet.
	 * @param planet the target planet
	 */
	void buildMorePowerPlant(final Planet planet) {
		applyActions.add(new Action0() {
			@Override
			public void invoke() {
				// scan for buildings
				for (Building b : planet.surface.buildings) {
					if (b.type.resources.containsKey("energy")) {
						// if damaged, repair
						if (b.isDamaged()) {
							b.repairing = true;
							return;
						} else
						// if upgradable and can afford upgrade
						if (b.upgradeLevel < b.type.upgrades.size()) {
							int newLevel = Math.min(b.upgradeLevel + (int)(p.money / b.type.cost), b.type.upgrades.size());
							if (newLevel != b.upgradeLevel) {
								actionUpgradeBuilding(planet, b, newLevel);
								return;
							}
						}
					}
				}
				// if no existing building found
				// find the most expensive still affordable building
				BuildingType target = null;
				for (BuildingType bt : w.buildingModel.buildings.values()) {
					if (bt.resources.containsKey("energy") && planet.canBuild(bt)) {
						if (target == null || (bt.cost <= p.money && bt.cost > target.cost)) {
							target = bt;
						}
					}					
				}
				if (target != null) {
					actionPlaceBuilding(planet, target);
				}
			}
		});
	}
	/**
	 * Demolish one of the excess labs on the planet to make room.
	 * @param rt the research type
	 * @param planet the target planet
	 * @return true if demolish added
	 */
	boolean demolishOneLabFor(ResearchType rt, AIPlanet planet) {
		if (demolishOneLabIf(rt.aiLab, world.global.aiLab, planet.statistics.aiLab, planet.planet, "ai")) {
			return true;
		}
		if (demolishOneLabIf(rt.civilLab, world.global.civilLab, planet.statistics.civilLab, planet.planet, "civil")) {
			return true;
		}
		if (demolishOneLabIf(rt.compLab, world.global.compLab, planet.statistics.compLab, planet.planet, "computer")) {
			return true;
		}
		if (demolishOneLabIf(rt.mechLab, world.global.mechLab, planet.statistics.mechLab, planet.planet, "mechanical")) {
			return true;
		}
		if (demolishOneLabIf(rt.milLab, world.global.milLab, planet.statistics.milLab, planet.planet, "military")) {
			return true;
		}
		return false;
	}
	/**
	 * Demolish one lab of the given resource.
	 * @param lab the required lab count
	 * @param global the global lab count
	 * @param local the local lab count
	 * @param planet the planet
	 * @param resource the lab resource name
	 * @return true if action added
	 */
	boolean demolishOneLabIf(int lab, int global, int local, final Planet planet, final String resource) {
		if (lab < global && local > 0) {
			applyActions.add(new Action0() {
				@Override
				public void invoke() {
					for (Building b : planet.surface.buildings) {
						if (b.type.resources.containsKey(resource)) {
							actionDemolishBuilding(planet, b);
							return;
						}
					}
				}
			});
			return true;
		}
		return false;
	}
	/**
	 * Build one of the required labs.
	 * @param rt the research type
	 * @param planet the target planet
	 */
	void buildOneLabFor(final ResearchType rt, final AIPlanet planet) {
		if (buildOneLabIf(rt.aiLab, world.global.aiLab, planet.planet, "ai")) {
			return;
		}
		if (buildOneLabIf(rt.civilLab, world.global.civilLab, planet.planet, "civil")) {
			return;
		}
		if (buildOneLabIf(rt.compLab, world.global.compLab, planet.planet, "computer")) {
			return;
		}
		if (buildOneLabIf(rt.mechLab, world.global.mechLab, planet.planet, "mechanical")) {
			return;
		}
		if (buildOneLabIf(rt.milLab, world.global.milLab, planet.planet, "military")) {
			return;
		}
	}
	/**
	 * Build one of the labs if the prerequisite counts match.
	 * @param required the required count of lab
	 * @param available the available count of lab
	 * @param planet the target planet
	 * @param resource the building type identification resource
	 * @return true if successful
	 */
	boolean buildOneLabIf(int required, int available, final Planet planet, String resource) {
		if (required > available) {
			final BuildingType bt = findBuildingType(resource);
			if (bt != null) {
				if (bt.cost <= world.money) {
					applyActions.add(new Action0() {
						@Override
						public void invoke() {
							actionPlaceBuilding(planet, bt);
						}
					});
					return true;
				}
			} else {
				new AssertionError("Can't find building for resource " + resource).printStackTrace();
			}
		}
		return false;
	}
	/**
	 * Find the first building who provides the given resource.
	 * @param resource the resource name
	 * @return the building type or null
	 */
	BuildingType findBuildingType(String resource) {
		for (BuildingType bt : w.buildingModel.buildings.values()) {
			if (bt.resources.containsKey(resource)) {
				return bt;
			}
		}
		return null;
	}
	/**
	 * Comparator which takes an integer index from the supplied map. 
	 * @author akarnokd, 2011.12.26.
	 * @param <T> the element type
	 */
	class CompareFromMap<T> implements Comparator<T> {
		/** The backing map. */
		final Map<T, Integer> map;
		/**
		 * Constructor.
		 * @param map the backing map to use
		 */
		public CompareFromMap(Map<T, Integer> map) {
			this.map = map;
		}
		@Override
		public int compare(T o1, T o2) {
			int count1 = map.get(o1);
			int count2 = map.get(o2);
			return count1 < count2 ? 1 : (count1 > count2 ? -1 : 0);
		}
	}
	/**
	 * Count how many labs need to be built in addition to the current settings.
	 * @param rt the research type
	 * @return the total number of new buildings required
	 */
	int rebuildCount(ResearchType rt) {
		return 
				rebuildRequiredCount(rt.aiLab, world.global.aiLab)
				+ rebuildRequiredCount(rt.civilLab, world.global.civilLab)
				+ rebuildRequiredCount(rt.compLab, world.global.compLab)
				+ rebuildRequiredCount(rt.mechLab, world.global.mechLab)
				+ rebuildRequiredCount(rt.milLab, world.global.milLab)
		;
	}
	/**
	 * If the lab count is greater than the active count, return the difference.
	 * @param lab the research required lab count
	 * @param active the active research counts
	 * @return zero or the difference
	 */
	int rebuildRequiredCount(int lab, int active) {
		if (lab > active) {
			return lab - active;
		}
		return 0;
	}
	/**
	 * Counts how many further research becomes available when the research is completed.
	 * @param rt the current research
	 * @param map the map for research to count
	 */
	void setResearchEnables(ResearchType rt, Map<ResearchType, Integer> map) {
		int count = 0;
		for (ResearchType rt2 : world.remainingResearch) {
			if (rt2.prerequisites.contains(rt)) {
				count++;
			}
		}
		for (ResearchType rt2 : world.furtherResearch) {
			if (rt2.prerequisites.contains(rt)) {
				count++;
			}
		}
		map.put(rt, count);
	}
}
