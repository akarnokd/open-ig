/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.core.Location;
import hu.openig.core.Pair;
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIManager;
import hu.openig.model.AIPlanet;
import hu.openig.model.AISpaceBattleManager;
import hu.openig.model.AIWorld;
import hu.openig.model.ApproachType;
import hu.openig.model.AttackDefense;
import hu.openig.model.BattleEfficiencyModel;
import hu.openig.model.BattleInfo;
import hu.openig.model.BattleProjectile.Mode;
import hu.openig.model.Building;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.ExplorationMap;
import hu.openig.model.Fleet;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.ModelUtils;
import hu.openig.model.NegotiateType;
import hu.openig.model.Planet;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Player;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.ResponseMode;
import hu.openig.model.SpaceStrengths;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarStructure.StructureType;
import hu.openig.model.SpacewarWeaponPort;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.Trait;
import hu.openig.model.TraitKind;
import hu.openig.model.World;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
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
	final Set<Integer> defensiveTask = new HashSet<>();
	/** Exponential smoothing of strength changes. */
	static final double STRENGTH_DISCOUNT = 0.2;
	/** The list of actions to apply. */
	final List<Action0> applyActions = new ArrayList<>();
	/** The next attack date. */
	volatile Date nextAttack;
	/** Last time a satellite was deployed. */
	volatile Date lastSatelliteDeploy;
	/** Indicates that a new day has passed. */
	boolean isNewDay;
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
		world.lastSatelliteDeploy = lastSatelliteDeploy;
		
		world.isNewDay = isNewDay;
		isNewDay = false;
	}
	
	@Override
	public void apply() {
		
		int actionLimit = Integer.MAX_VALUE;
//		if (world.difficulty != Difficulty.HARD) {
//			actionLimit = Math.max(1, (int)(w.params().speed() / 10d + 0.5));
//		}
		
		try {
			for (Action0 a : applyActions) {
				a.invoke();
				if (actionLimit-- <= 0) {
					break;
				}
			}
		} finally {
			applyActions.clear();
			world = null;
		}
	}
	
	/**
	 * The default group attack behavior which chooses the in-range enemy or
	 * distributes attacks among all enemies.
	 * @param world the world object
	 * @param idles the list of units to handle
	 * @param p the player
	 */
	public static void defaultAttackBehavior(SpacewarWorld world,
			List<SpacewarStructure> idles, Player p) {
		// select next closest target
		for (SpacewarStructure ship : idles) {
			defaultAttackBehavior(world, ship);
		}
		List<SpacewarStructure> esl = defaultAttackOutOfRange(world, idles);
		// manage rockets
		handleRockets(world, p, esl);
		// manage kamikaze
		handleKamikaze(world, p);
	}
	/**
	 * Attack out-of-range ships with the remaining idles.
	 * @param world the world object
	 * @param idles the list of idles
	 * @return all enemies
	 */
	static List<SpacewarStructure> defaultAttackOutOfRange(SpacewarWorld world,
			List<SpacewarStructure> idles) {
		List<SpacewarStructure> esl = new ArrayList<>();
        if (!idles.isEmpty()) {
            esl.addAll(world.enemiesOf(idles.get(0)));
        }
		if (!esl.isEmpty()) {
			List<SpacewarStructure> esl2 = U.newArrayList(esl);
			for (SpacewarStructure ship : idles) {
				if (ship.attack == null 
						&& ship.type == StructureType.SHIP
						&& ship.canDirectFire()) {
					SpacewarStructure t = selectNewTarget(world, ship, esl);
					if (t != null) {
						esl.remove(t);
						if (esl.isEmpty()) {
							esl.addAll(esl2);
						}
					}
				}
			}
		}
		return esl;
	}
	/**
	 * Handle rockets.
	 * @param world the world object
	 * @param p the player
	 * @param esl all enemy vessels
	 */
	static void handleRockets(SpacewarWorld world, Player p,
			List<SpacewarStructure> esl) {
		List<SpacewarStructure> rocketTargets = world.enemiesOf(p);
		Collections.shuffle(rocketTargets);
		for (SpacewarStructure s : rocketTargets) {
			if (world.isFleeing(s)) {
				continue;
			}
			// do not fire rocket at tagged objects unless it is the last enemy
			if (esl.size() == 1 || (s.item == null || s.item.tag == null)) {
				int found = 0;
				if (s.type == StructureType.SHIP || s.type == StructureType.STATION) {
					Pair<SpacewarStructure, SpacewarWeaponPort> w = 
							findReadyPort(world.structures(p), EnumSet.of(Mode.ROCKET, Mode.MULTI_ROCKET));
					if (w != null) {
						world.attack(w.first, s, w.second.projectile.mode);
						found++;
					}
				}
				if (s.type == StructureType.SHIELD || s.type == StructureType.PROJECTOR) {
					Pair<SpacewarStructure, SpacewarWeaponPort> w = 
							findReadyPort(world.structures(p), EnumSet.of(Mode.BOMB, Mode.VIRUS));
					if (w != null) {
						world.attack(w.first, s, w.second.projectile.mode);
						found++;
					}
				}
				if (found == 0) {
					// don't bother with other targets, out of ammo
					break;
				}
			}
		}
	}
	/**
	 * Handle fighter kamikaze.
	 * @param world the world
	 * @param p the player
	 */
	static void handleKamikaze(SpacewarWorld world, Player p) {
		if (!world.battle().enemyFlee) {
			for (SpacewarStructure s : world.structures(p)) {
				s.guard |= s.type == StructureType.STATION || s.type == StructureType.PROJECTOR;
				if (s.type == StructureType.SHIP 
						&& s.item != null 
						&& s.item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
						&& s.attack != null && !s.attack.isDestroyed()) {
					if (s.count == 1 && s.hp * 10 < s.hpMax) {
						world.attack(s, s.attack, Mode.KAMIKAZE);
					}
				}
			}
		}
	}
	/**
	 * Find a port ready to fire among the fleet.
	 * @param own the own fleet
	 * @param mode the weapon mode
	 * @return the pair of structure and port
	 */
	static Pair<SpacewarStructure, SpacewarWeaponPort> findReadyPort(List<SpacewarStructure> own, EnumSet<Mode> mode) {
		for (SpacewarStructure s : own) {
			for (SpacewarWeaponPort wp : s.ports) {
				if (wp.count > 0 && wp.cooldown == 0 && mode.contains(wp.projectile.mode)) {
					return Pair.of(s, wp);
				}
			}
		}
		return null;
	}
	/**
	 * Select a target in range which the current ship can do most
	 * damage.
	 * @param world the world object.
	 * @param ship the current ship
	 */
	static void selectNewTarget(SpacewarWorld world, final SpacewarStructure ship) {
		List<SpacewarStructure> es = world.enemiesInRange(ship);
		ModelUtils.shuffle(es);
		SpacewarStructure best = null;
		double bestEfficiency = 0d;
		for (SpacewarStructure s : es) {
			BattleEfficiencyModel bem = ship.getEfficiency(s);
			double eff = bem != null ? bem.damageMultiplier : 1d;
			if (eff > bestEfficiency) {
				best = s;
				bestEfficiency = eff;
			}
		}
		if (best != null) {
			world.attack(ship, best, Mode.BEAM);
		}
	}
	/**
	 * Select a target across the whole space which the current ship can do most
	 * damage.
	 * @param world the world object.
	 * @param ship the current ship
	 * @param es the list of enemies
	 * @return the selected target
	 */
	static SpacewarStructure selectNewTarget(SpacewarWorld world, 
			final SpacewarStructure ship, List<SpacewarStructure> es) {
		ModelUtils.shuffle(es);
		SpacewarStructure best = null;
		double bestEfficiency = 0d;
		for (SpacewarStructure s : es) {
			BattleEfficiencyModel bem = ship.getEfficiency(s);
			double eff = bem != null ? bem.damageMultiplier : 1d;
			if (eff > bestEfficiency) {
				best = s;
				bestEfficiency = eff;
			}
		}
		if (best != null) {
			world.attack(ship, best, Mode.BEAM);
		}
		return best;
	}
	/**
	 * Orders the given structure to attack a random enemy.
	 * @param world the world
	 * @param ship the ship
	 */
	public static void defaultAttackBehavior(SpacewarWorld world,
			SpacewarStructure ship) {
		if (ship.canDirectFire()) {
			if (ship.type == StructureType.STATION 
					|| ship.type == StructureType.PROJECTOR) {
				ship.guard = true;
				selectNewTarget(world, ship);
			} else
			if (ship.type == StructureType.SHIP) {
				selectNewTarget(world, ship);
			}
		}
	}
	@Override
	public void groundBattle(final GroundwarWorld war) {
		AIGroundwar aig = new AIGroundwar(p, war);
		aig.run();
		aig.apply();
	}
	
	@Override
	public void groundBattleDone(GroundwarWorld war) {
		onAutobattleFinish(war.battle());
	}
	@Override
	public void groundBattleInit(GroundwarWorld war) {
		Player e = war.battle().enemy(p);

		w.establishWar(p, e);
	}
	
	@Override
	public AISpaceBattleManager spaceBattle(SpacewarWorld sworld) {
		return new AIDefaultSpaceBattle(p, this, sworld);
	}
	
	@Override
	public void load(XElement in) {
		for (XElement xf : in.childrenWithName("task-defensive")) {
			int fid = xf.getInt("fleet");
			defensiveTask.add(fid);
		}
		isNewDay = in.getBoolean("is-new-day", true);
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
					Exceptions.add(ex);
				}
			}
			
			String lsd = attack.get("last-satellite-deploy", null);
			if (lsd != null) {
				try {
					lastSatelliteDeploy = XElement.parseDateTime(lsd);
				} catch (ParseException ex) {
					Exceptions.add(ex);
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
		out.set("is-new-day", isNewDay);
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
		if (lastSatelliteDeploy != null) {
			xa.set("last-satellite-deploy", XElement.formatDateTime(lastSatelliteDeploy));
		}
	}
	@Override
	public void onResearchStateChange(ResearchType rt, ResearchState state) {
		// if the user switched to this guy.
		if (state == ResearchState.COMPLETE) {
			if (rt.category.main != ResearchMainCategory.BUILDINGS) {
				p.addProductionHistory(rt);
			}
		}
	}
	@Override
	public void onProductionComplete(ResearchType rt) {
		
	}
	@Override
	public void onDiscoverPlanet(Planet planet) {
		
	}
	@Override
	public void onDiscoverFleet(Fleet fleet) {
		
	}
	@Override
	public void onDiscoverPlayer(Player player) {
		
	}
	@Override
	public void onFleetArrivedAtPoint(Fleet fleet, double x, double y) {
		
	}
	@Override
	public void onFleetArrivedAtPlanet(Fleet fleet, Planet planet) {
		
	}
	@Override
	public void onFleetArrivedAtFleet(Fleet fleet, Fleet other) {
		
	}
	@Override
	public void onBuildingComplete(Planet planet, Building building) {
		
	}
	@Override
	public void onLostSight(Fleet fleet) {
		
	}
	@Override
	public void onLostTarget(Fleet fleet, Fleet target) {
		
	}
	@Override
	public void onNewDay() {
		isNewDay = true;
	}
	@Override
	public void onSatelliteDestroyed(Planet planet, InventoryItem ii) {
		
	}
	@Override
	public void onPlanetDied(Planet planet) {
		
	}
	@Override
	public void onPlanetRevolt(Planet planet) {
		
	}
	@Override
	public void onPlanetConquered(Planet planet, Player lastOwner) {
		AttackPlanner.onPlanetConquered(planet, this);
	}
	@Override
	public void onPlanetColonized(Planet planet) {
		AttackPlanner.onPlanetConquered(planet, this);
	}
	@Override
	public void onPlanetLost(Planet planet) {
		AttackPlanner.onPlanetLost(planet, this);
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
		updateExplorationMap();
		
		List<Planner> planners = new ArrayList<>();

		planners.add(new ColonyPlanner(world, controls));

		planners.add(new ResearchPlanner(world, controls, exploration));
		planners.add(new ColonizationPlanner(world, controls));
		planners.add(new ExplorationPlanner(world, controls, exploration, new Action1<Date>() {
			@Override
			public void invoke(Date value) {
				lastSatelliteDeploy = value;
			}
		}));
		
		int mix1 = planners.size();
		planners.add(new EconomyPlanner(world, controls));
		planners.add(new OffensePlanner(world, controls));
		planners.add(new StaticDefensePlanner(world, controls));
		int mix2 = planners.size();
		
		planners.add(new AttackPlanner(world, controls, exploration, new Action1<Date>() {
			@Override
			public void invoke(Date value) {
				nextAttack = value;
			}
		}));

		ModelUtils.shuffle(planners.subList(mix1, mix2));

		for (Planner p : planners) {
			List<Action0> acts = p.run();
			if (!acts.isEmpty()) {
				applyActions.addAll(acts);
				if (p.getClass() == ColonyPlanner.class) {
					return;
				}
			}
		}
	}
	
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		
	}
	@Override
	public void onAutoGroundwarStart(BattleInfo battle, AttackDefense attacker,
			AttackDefense defender) {
		w.establishWar(p, battle.enemy(p));
	}
	
	@Override
	public void onAutoSpacewarStart(BattleInfo battle, SpaceStrengths str) {
		w.establishWar(p, battle.enemy(p));
	}
	
	@Override
	public ResponseMode diplomacy(Player other, NegotiateType about,
			ApproachType approach, Object argument) {
		DiplomaticRelation r = w.getRelation(p, other);
		if (r == null || !r.full) {
			return ResponseMode.NO;
		}
		
//		PlayerStrength senderStrength = getStrength(other);
		
		PlanetStatistics ownStats = p.getPlanetStatistics(null);

//		PlanetStatistics senderStats = computeVisibleStats(other);
		
		double rnd = ModelUtils.random();
		double rel = r.value;

		Trait t = other.traits.trait(TraitKind.DIPLOMACY);
		if (t != null) {
			rel = rel * (1 + t.value / 100);
			rel = Math.min(100, Math.max(0, rel));
		}
		
		
		switch (about) {
		case DIPLOMATIC_RELATIONS:
			if (rnd < rel / 100 && rel < p.warThreshold + 10) {
				return ResponseMode.MAYBE;
			} else
			if (rnd < rel / 100) {
				return ResponseMode.YES;
			}
			break;
		case ALLY:
			Player p3 = (Player)argument;
			if (p3.group == p.group) {
				return ResponseMode.NO;
			}
			DiplomaticRelation r2 = w.getRelation(p, p3);
			if (r2 != null) {
				if (rnd < 0.5 && rel >= p.warThreshold + 35
						&& r2.value < rel) {
					return ResponseMode.YES;
				}
			}
			break;
		case DARGSLAN:
			if (rnd < rel / 100 && rel >= 75 
				&& !p.id.equals("Dargslan")) {
				return ResponseMode.YES;
			}
			break;
		case MONEY:
			if (rnd < rel / 100 || p.money() < 100000) {
				return ResponseMode.YES;
			}
			break;
		case SURRENDER:
			if (rnd < 0.1 && ownStats.planetCount < 2 && rel < p.warThreshold) {
				return ResponseMode.YES;
			}
			break;
		case TRADE:
			if (rnd < rel / 100) {
				return ResponseMode.YES;
			}
			break;
		default:
		}
		return ResponseMode.NO;
	}
//	/**
//	 * Compute the statistics of the given player based
//	 * on the current visibility settings.
//	 * @param other the other player
//	 * @return the statistics
//	 */
//	PlanetStatistics computeVisibleStats(Player other) {
//		PlanetStatistics ps = new PlanetStatistics();
//		
//		for (Map.Entry<Planet, PlanetKnowledge> pl : p.planets.entrySet()) {
//			int k = pl.getValue().ordinal();
//			if (pl.getKey().owner == other && k >= PlanetKnowledge.OWNER.ordinal()) {
//				ps.planetCount++;
//				
//				if (k >= PlanetKnowledge.STATIONS.ordinal()) {
//					
//				}
//				if (k >= PlanetKnowledge.BUILDING.ordinal()) {
//					
//				}
//			}
//		}
//		
//		return ps;
//	}
}
