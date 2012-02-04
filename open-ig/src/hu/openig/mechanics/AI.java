/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.core.Location;
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIManager;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.Building;
import hu.openig.model.DiplomaticInteraction;
import hu.openig.model.ExplorationMap;
import hu.openig.model.Fleet;
import hu.openig.model.GroundwarUnit;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.ResponseMode;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarStructure.StructureType;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.SurfaceEntity;
import hu.openig.model.SurfaceEntityType;
import hu.openig.model.World;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.geom.Point2D;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
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
	/** The controls. */
	AIControls controls;
	/** The exploration map manager. */
	ExplorationMap exploration;
	/** Set of fleets which will behave as defenders in the space battle. */
	final Set<Integer> defensiveTask = U.newHashSet();
	/** The estimations about how strong the other player's fleets are. */
	final Map<String, PlayerFleetStrength> strengths = U.newHashMap();
	/** The list of actions to apply. */
	final List<Action0> applyActions = new ArrayList<Action0>();
	/** The next attack date. */
	Date nextAttack;
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
		controls = new DefaultAIControls(p);
		exploration = new ExplorationMap(p);
	}
	@Override
	public void prepare() {
		world = new AIWorld();
		world.assign(p);
		
		world.nextAttack = nextAttack;
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
			if (c) {
				for (SpacewarStructure ship : idles) {
					if (ship.canDirectFire()) {
						costAttackBehavior(world, ship);
					}
				}
			} else {
				defaultAttackBehavior(world, idles);				
			}
			return SpacewarAction.CONTINUE;
		}
		return SpacewarAction.FLEE;
	}
	/**
	 * The default group attack behavior which chooses the in-range enemy or
	 * distributes attacks among all enemies.
	 * @param world the world object
	 * @param idles the list of units to handle
	 */
	public static void defaultAttackBehavior(SpacewarWorld world,
			List<SpacewarStructure> idles) {
		Set<SpacewarStructure> ess = U.newHashSet();
		for (SpacewarStructure ship : idles) {
			defaultAttackBehavior(world, ship);
		}
		if (ess.isEmpty()) {
			for (SpacewarStructure ship : idles) {
				ess.addAll(world.enemiesOf(ship));
				break;
			}
		}
		List<SpacewarStructure> esl = new ArrayList<SpacewarStructure>(ess);
		Collections.shuffle(esl);
		if (!esl.isEmpty()) {
			int i = 0;
			for (SpacewarStructure ship : idles) {
				if (ship.attack == null 
						&& ship.type == StructureType.SHIP
						&& ship.canDirectFire()) {
					ship.attack = esl.get(i);
					i++;
					if (i >= esl.size()) {
						i = 0;
					}
				}
			}
		}
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
	public void groundBattle(GroundwarWorld war) {
		List<Object> targets = new ArrayList<Object>();
		if (war.planet().owner != p) {
			for (Building b : war.planet().surface.buildings) {
				if (b.type.kind.equals("Defensive")) {
					targets.add(b);
				}
			}
		}
		
		// the various attack categories
		List<GroundwarUnit> tanks = new ArrayList<GroundwarUnit>();
		List<GroundwarUnit> ranged = new ArrayList<GroundwarUnit>();
		List<GroundwarUnit> rocketJammers = new ArrayList<GroundwarUnit>();
		List<GroundwarUnit> minelayers = new ArrayList<GroundwarUnit>();
		List<GroundwarUnit> paralizers = new ArrayList<GroundwarUnit>();
		
		for (GroundwarUnit u : war.units()) {
			if (u.owner != p) {
				targets.add(u);
			} else {
				switch (u.model.type) {
				case TANK:
				case KAMIKAZE:
				case SELF_REPAIR_TANK:
				case ROCKET_JAMMER:
					tanks.add(u);
					break;
				case ROCKET_SLED:
				case ARTILLERY:
					tanks.add(u);
					ranged.add(u);
					break;
				case MINELAYER:
					minelayers.add(u);
					break;
				case PARALIZER:
					paralizers.add(u);
					break;
				default:
				}
			}
		}
		// center of mass
		double massx = 0d;
		double massy = 0d;
		for (GroundwarUnit u : tanks) {
			massx += u.x;
			massy += u.y;
		}
		massx /= tanks.size();
		massy /= tanks.size();
		
		final double fmassx = massx;
		final double fmassy = massy;
		
		if (!targets.isEmpty()) {
			// attack nearest target to the mass
			Object o = Collections.min(targets, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					Point2D.Double p1 = position(o1);
					Point2D.Double p2 = position(o2);
					
					double d1 = p1.distance(fmassx, fmassy);
					double d2 = p2.distance(fmassx, fmassy);
					
					return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
				}
			});
			
			
			// regular units, attack nearest value
			for (GroundwarUnit u : tanks) {
				if (!u.hasValidTarget()) {
					if (o instanceof Building) {
						war.attack(u, (Building)o);
					} else {
						war.attack(u, (GroundwarUnit)o);
					}
				}
			}
			// paralizers attack only if the rest attacks a unit
			for (GroundwarUnit u : paralizers) {
				if (!u.hasValidTarget() && (o instanceof GroundwarUnit)) {
					war.attack(u, (GroundwarUnit)o);
				}
			}
		}
		// rocket jammer, keep up with rocket sleds or artillery
		Set<GroundwarUnit> defended = new HashSet<GroundwarUnit>();
		// collect already protected units
		for (GroundwarUnit u : rocketJammers) {
			if (u.hasValidTarget()) {
				defended.add(u.attackUnit);
			}
		}
		for (GroundwarUnit u : rocketJammers) {
			if (!u.hasValidTarget()) {
				GroundwarUnit u2 = nearest(u, ranged, defended);
				// protect ranged
				if (u2 != null) {
					defended.add(u2);
					u.attackUnit = u2; // e.g. follow
				} else {
					// protect any tank
					u2 = nearest(u, tanks, defended);
					if (u2 != null) {
						defended.add(u2);
						u.attackUnit = u2; // e.g. follow
					}
				}
			}
		}
		// minelayers start placing mines onto the roads
		Set<Location> um = unmined(war);
		for (GroundwarUnit u : minelayers) {
			um.remove(u.target());
		}
		for (GroundwarUnit u : minelayers) {
			if (!u.isMoving() && u.phase == 0) {
				Location l = u.location();
				SurfaceEntity e = war.planet().surface.buildingmap.get(l);
				if (e != null && e.type == SurfaceEntityType.ROAD
						&& !war.hasMine(l.x, l.y)) {
					war.special(u);
					um.remove(u.location());
				} else {
					Location loc2 = nearest(u, um);
					if (loc2 != null) {
						war.move(u, loc2.x, loc2.y);
					}
				}
			}
		}
	}
	/**
	 * Return the nearest location.
	 * @param u the unit
	 * @param locs the locations
	 * @return the location or null if locs is empty
	 */
	Location nearest(final GroundwarUnit u, Set<Location> locs) {
		if (locs.isEmpty()) {
			return null;
		}
		return Collections.min(locs, new Comparator<Location>() {
			@Override
			public int compare(Location o1, Location o2) {
				double d1 = u.distance(o1);
				double d2 = u.distance(o2);
				return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
			}
		});
	}
	/**
	 * Return the nearest unit from the given set.
	 * @param u the base unit
	 * @param units the other units
	 * @param except the exception list
	 * @return the nearest
	 */
	GroundwarUnit nearest(final GroundwarUnit u, 
			List<GroundwarUnit> units, Set<GroundwarUnit> except) {
		List<GroundwarUnit> candidates = new ArrayList<GroundwarUnit>();
		for (GroundwarUnit u2 : units) {
			if (!except.contains(u2)) {
				candidates.add(u2);
			}
		}
		if (candidates.isEmpty()) {
			return null;
		}
		return Collections.min(candidates, new Comparator<GroundwarUnit>() {
			@Override
			public int compare(GroundwarUnit o1, GroundwarUnit o2) {
				double d1 = u.distance(o1);
				double d2 = u.distance(o2);
				return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
			}
		});
	}
	/**
	 * Generates the set of roads where no mine has been laid.
	 * @param war the war context
	 * @return the set of unminded roads
	 */
	Set<Location> unmined(GroundwarWorld war) {
		Set<Location> result = U.newHashSet();
		for (Map.Entry<Location, SurfaceEntity> e : war.planet().surface.buildingmap.entrySet()) {
			if (e.getValue().type == SurfaceEntityType.ROAD) {
				Location key = e.getKey();
				if (!war.hasMine(key.x, key.y)) {
					result.add(key);
				}
			}
		}
		return result;
	}
	/**
	 * Compute the map position of the object.
	 * @param o the object
	 * @return the position
	 */
	Point2D.Double position(Object o) {
		if (o instanceof Building) {
			Building b = (Building)o;
			return new Point2D.Double(b.location.x + b.width() / 2d, b.location.y - b.health() / 2d);
		}
		GroundwarUnit u = (GroundwarUnit)o;
		return new Point2D.Double(u.x, u.y);
	}
	
	
	@Override
	public void groundBattleDone(GroundwarWorld war) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void groundBattleInit(GroundwarWorld war) {
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
			exploration.map.clear();
			String coords = xloc.get("coords", "");
			if (coords.length() > 0) {
				for (String xys : coords.split("\\s+")) {
					String[] xy = xys.split(";");
					Location loc = Location.of(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
					exploration.map.add(loc);
				}
			}
		}
		XElement attack = in.childElement("attack");
		if (attack != null) {
			String na = attack.get("next-attack", null);
			
			if (na != null) {
				try {
					nextAttack = XElement.parseDateTime(na);
				} catch (ParseException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	@Override
	public void save(XElement out) {
		for (Integer f : defensiveTask) {
			XElement xf = out.add("task-defensive");
			xf.set("fleet", f);
		}
		XElement xloc = out.add("exploration-map");
		StringBuilder coords = new StringBuilder();
		for (Location loc : exploration.map) {
			if (coords.length() > 0) {
				coords.append(" ");
			}
			coords.append(loc.x).append(";").append(loc.y);
		}
		if (coords.length() > 0) {
			xloc.set("coords", coords);
		}
		XElement xa = out.add("attack");
		if (nextAttack != null) {
			xa.set("next-attack", XElement.formatDateTime(nextAttack));
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
		// TODO
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
	@Override
	public void onRadar() {
	}
	/**
	 * Update the exploration map by removing cells covered by current radar.
	 */
	void updateExplorationMap() {
		for (AIPlanet planet : world.ownPlanets) {
			if (planet.radar > 0) {
				int cx = planet.planet.x;
				int cy = planet.planet.y;
				int r = planet.radar;
				
				exploration.removeCoverage(cx, cy, r);
			}
		}
		for (AIFleet fleet : world.ownFleets) {
			if (fleet.radar > 0) {
				exploration.removeCoverage((int)fleet.x, (int)fleet.y, fleet.radar);
			}
		}
	}
	@Override
	public void manage() {
		// if the player is the current player
//		if (p != w.player) {
//			return;
//		}
		updateExplorationMap();
		
		List<Action0> acts = null;

		acts = new ColonyPlanner(world, controls).run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
			return;
		}

		acts = new ResearchPlanner(world, controls, exploration).run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
			return;
		}
		acts = new ExplorationPlanner(world, controls, exploration).run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
			return;
		}

		acts = new EconomyPlanner(world, controls).run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
			return;
		}
		
		acts = new OffensePlanner(world, controls).run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
			return;
		}
		
		acts = new StaticDefensePlanner(world, controls).run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
			return;
		}
		
		acts = new AttackPlanner(world, controls, exploration, new Action1<Date>() {
			@Override
			public void invoke(Date value) {
				nextAttack = value;
			}
		}).run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
			return;
		}
	}
	
}
