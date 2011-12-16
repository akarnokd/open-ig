/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.AIAttackMode;
import hu.openig.model.AIManager;
import hu.openig.model.AIWorld;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.DiplomaticInteraction;
import hu.openig.model.Fleet;
import hu.openig.model.GameEnvironment;
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
import java.util.Collections;
import java.util.Comparator;
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
	/** The world. */
	AIWorld world;
	/** 
	 * AI players won't start colonization until the player has actually researched its colony ship.
	 * To avoid the headstart problem in skirmish mode. 
	 */
	boolean playerColonyShipAvailable;
	/** Set of fleets which will behave as defenders in the space battle. */
	final Set<Fleet> defensiveTask = JavaUtils.newHashSet();
	@Override
	public void init(GameEnvironment env, Player p) {
		this.w = env.world();
		this.p = p;
	}
	@Override
	public void prepare() {
		world = new AIWorld();
		world.assign(w, p);
		playerColonyShipAvailable = w.player.colonyShipAvailable;
	}
	
	@Override
	public void manage() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
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
		} else {
			r.assignedMoney = (int)(r.remainingMoney * moneyFactor);
		}
		r.state = ResearchState.RUNNING;
	}
	/**
	 * Deploy a fleet from the inventory.
	 * @param loadFactor percentage of 0..1 about how many units to place into fighter/cruiser/battleship slots.
	 * @param powerFactor percentage of 0..1 about how well the units should be equipped
	 * @param items the sequence of items to choose from when filling in the slots
	 */
	public void actionDeployFleet(double loadFactor, double powerFactor, Iterable<ResearchType> items) {
		// TODO implement
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
	 * Place a building onto the planet (via the autobuild location mechanism).
	 * @param planet the target planet
	 * @param buildingType the building type
	 */
	public void actionPlaceBuilding(Planet planet, BuildingType buildingType) {
		// TODO implement
		TileSet ts = buildingType.tileset.get(planet.race);
		Point pt = planet.surface.findLocation(ts.normal.width + 2, ts.normal.height + 2);
		if (pt != null) {
			Simulator.doConstruct(w, planet, buildingType, pt);
		}
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
	public ResponseMode diplomacy(World world, Player we, Player other,
			DiplomaticInteraction offer) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SpacewarAction spaceBattle(SpacewarWorld world, Player player,
			List<SpacewarStructure> idles) {
		if (idles.size() == 0) {
			return SpacewarAction.CONTINUE;
		}
		// FIXME make more sophisticated
		
		double health = fleetHealth(world.structures(player));
		double switchToCostAttack = player.aiDefensiveRatio / (player.aiOffensiveRatio + player.aiDefensiveRatio);
		double switchToFlee = player.aiSocialRatio() * switchToCostAttack;
		
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
	public void groundBattle(World world, Player we, BattleInfo battle) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void load(XElement in) {
		for (XElement xf : in.childrenWithName("task-defensive")) {
			int fid = xf.getInt("fleet");
			Fleet f = p.fleet(fid);
			if (f != null) {
				defensiveTask.add(f);
			}
		}
	}
	@Override
	public void save(XElement out) {
		for (Fleet f : defensiveTask) {
			XElement xf = out.add("task-defensive");
			xf.set("fleet", f.id);
		}
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
}
