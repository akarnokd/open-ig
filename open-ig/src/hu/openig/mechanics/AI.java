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
import hu.openig.core.Pair;
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIManager;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.ApproachType;
import hu.openig.model.AttackDefense;
import hu.openig.model.BattleInfo;
import hu.openig.model.BattleProjectile.Mode;
import hu.openig.model.Building;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.ExplorationMap;
import hu.openig.model.Fleet;
import hu.openig.model.GroundwarUnit;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.NegotiateType;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Player;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.ResponseMode;
import hu.openig.model.SpaceStrengths;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarStructure.StructureType;
import hu.openig.model.SpacewarWeaponPort;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.World;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
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
	final Map<String, PlayerStrength> strengths = U.newHashMap();
	/** Exponential smoothing of strength changes. */
	static final double STRENGTH_DISCOUNT = 0.2;
	/** The list of actions to apply. */
	final List<Action0> applyActions = new ArrayList<Action0>();
	/** The next attack date. */
	Date nextAttack;
	/**
	 * Knowledge about a player's typical fleet strength based on encounters.
	 * @author akarnokd, 2011.12.20.
	 */
	public static class PlayerStrength {
		/** The player ID. */
		public String playerID;
		/** The attack value. -1 means no conflict yet. */
		public double spaceAttack = -1;
		/** The defense value. -1 means no conflict yet. */
		public double spaceDefense = -1;
		/** The ground attack potential. -1 means no conflict yet. */
		public double groundAttack = -1;
		/** The ground defense potential. -1 means no conflict yet. */
		public double groundDefense = -1;
		/** Times the spacewar won. */
		public int spaceWon;
		/** Times the spacewar lost. */
		public int spaceLost;
		/** Times the groundwar won. */
		public int groundWon;
		/** Times the groundwar lost. */
		public int groundLost;
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
	public SpacewarAction spaceBattle(SpacewarWorld world, List<SpacewarStructure> idles) {
		if (idles.size() == 0) {
			return SpacewarAction.CONTINUE;
		}
		Pair<Double, Double> fh = fleetHealth(world.structures(p));
		double health = fh.first / fh.second;
		double switchToCostAttack = p.aiDefensiveRatio / (p.aiOffensiveRatio + p.aiDefensiveRatio);
		double switchToFlee = p.aiSocialRatio() * switchToCostAttack;
		
		if (health >= switchToFlee) {
			boolean c = health < switchToCostAttack || defensiveTask.contains(idles.get(0).fleet.id);
			if (c) {
				for (SpacewarStructure ship : idles) {
					if (ship.canDirectFire()) {
						costAttackBehavior(world, ship);
					}
				}
			} else {
				defaultAttackBehavior(world, idles, p);				
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
	 * @param p the player
	 */
	public static void defaultAttackBehavior(SpacewarWorld world,
			List<SpacewarStructure> idles, Player p) {
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
		for (SpacewarStructure s : esl) {
			// do not fire rocket at tagged objects unless it is the last enemy
			if (esl.size() == 1 || (s.item == null || s.item.tag == null)) {
				int found = 0;
				if (s.type == StructureType.SHIP || s.type == StructureType.STATION) {
					Pair<SpacewarStructure, SpacewarWeaponPort> w = 
							findReadyPort(world.structures(p), EnumSet.of(Mode.ROCKET, Mode.MULTI_ROCKET));
					if (w != null) {
						world.attack(s, w.first, w.second.projectile.mode);
						found++;
					}
				}
				if (s.type == StructureType.SHIELD || s.type == StructureType.PROJECTOR) {
					Pair<SpacewarStructure, SpacewarWeaponPort> w = 
							findReadyPort(world.structures(p), EnumSet.of(Mode.BOMB, Mode.VIRUS));
					if (w != null) {
						world.attack(s, w.first, w.second.projectile.mode);
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
	 * Calculate the percentage of the structure healths.
	 * @param structures the structures
	 * @return the health between 0..1
	 */
	public static Pair<Double, Double> fleetHealth(List<SpacewarStructure> structures) {
		double hpTotal = 0.0;
		double hpActual = 0.0;
		for (SpacewarStructure s : structures) {
			hpTotal += s.hpMax + s.shieldMax;
			hpActual += s.hp + s.shield;
		}
		return Pair.of(hpActual, hpTotal);
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
		double dps = 0;
		double hp = 0;
		for (GroundwarUnit u : war.units()) {
			if (u.owner != p) {
				hp += u.hp;
				dps += u.model.damage * 1.0 / u.model.delay;
			}
		}
		
		Player e = war.battle().enemy(p);

		PlayerStrength ps = getStrength(e);
		if (ps.groundAttack < 0) {
			ps.groundAttack = dps;
		} else {
			ps.groundAttack = (1 - STRENGTH_DISCOUNT) * ps.groundAttack + STRENGTH_DISCOUNT * dps;
		}
		if (ps.groundDefense < 0) {
			ps.groundDefense = hp;
		} else {
			ps.groundDefense = (1 - STRENGTH_DISCOUNT) * ps.groundDefense + STRENGTH_DISCOUNT * hp;
		}
		
		enterWar(e);
	}
	@Override
	public void spaceBattleDone(SpacewarWorld world) {
		onAutobattleFinish(world.battle());
	}
	@Override
	public void spaceBattleInit(SpacewarWorld world) {
		// buildup statistics
		double dps = 0;
		double hp = 0;
		for (SpacewarStructure s : world.structures()) {
			if (s.owner != p) {
				hp += s.hp + s.shield;
				for (SpacewarWeaponPort wp : s.ports) {
					dps += wp.count * wp.projectile.damage * 1.0 / wp.projectile.delay;
				}
			}
		}
		
		updateSpaceStrength(world.battle(), dps, hp);
	}
	/**
	 * Update the space strength based on the given parameters.
	 * @param battle the battle record
	 * @param dps the damage per second
	 * @param hp the shielded hitpoints
	 */
	protected void updateSpaceStrength(BattleInfo battle, double dps, double hp) {
		Player e = battle.enemy(p);
		
		PlayerStrength ps = getStrength(e);
		if (ps.spaceAttack < 0) {
			ps.spaceAttack = dps;
		} else {
			ps.spaceAttack = (1 - STRENGTH_DISCOUNT) * ps.spaceAttack + STRENGTH_DISCOUNT * dps;
		}
		if (ps.spaceDefense < 0) {
			ps.spaceDefense = hp;
		} else {
			ps.spaceDefense = (1 - STRENGTH_DISCOUNT) * ps.spaceDefense + STRENGTH_DISCOUNT * hp;
		}
		
		enterWar(e);
	}
	/**
	 * Enter a war in diplomacy with the enemy.
	 * @param e the enemy
	 */
	void enterWar(Player e) {
		// update diplomatic relations
		DiplomaticRelation dr = w.establishRelation(e, p);
		dr.full = true;
		dr.value = 5;
		dr.wontTalk = true;
		dr.lastContact = w.time.getTime();
		dr.alliancesAgainst.clear();
	}
	/**
	 * Returns or creates the strength estimate record for the given enemy.
	 * @param p the enemy player
	 * @return the strength
	 */
	PlayerStrength getStrength(Player p) {
		PlayerStrength ps = strengths.get(p.id);
		if (ps == null) {
			ps = new PlayerStrength();
			ps.playerID = p.id;
		}
		return ps;
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
		strengths.clear();
		for (XElement xstrengths : in.childrenWithName("strengths")) {
			for (XElement xstr : xstrengths.childrenWithName("strength")) {
				PlayerStrength ps = new PlayerStrength();
				
				ps.playerID = xstr.get("player");
				ps.spaceAttack = xstr.getDouble("space-attack");
				ps.spaceDefense = xstr.getDouble("space-defense");
				ps.groundAttack = xstr.getDouble("ground-attack");
				ps.groundDefense = xstr.getDouble("ground-defense");
				
				ps.spaceWon = xstr.getInt("space-wins");
				ps.spaceLost = xstr.getInt("space-loses");
				ps.groundWon = xstr.getInt("ground-wins");
				ps.groundLost = xstr.getInt("ground-loses");
				
				strengths.put(ps.playerID, ps);
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
		
		XElement xstrs = out.add("strengths");
		for (PlayerStrength ps : strengths.values()) {
			XElement xstr = xstrs.add("strength");
			
			xstr.set("player", ps.playerID);
			xstr.set("space-attack", ps.spaceAttack);
			xstr.set("space-defense", ps.spaceDefense);
			xstr.set("ground-attack", ps.groundAttack);
			xstr.set("ground-defense", ps.groundDefense);
			
			xstr.set("space-wins", ps.spaceWon);
			xstr.set("space-loses", ps.spaceLost);
			xstr.set("ground-wins", ps.groundWon);
			xstr.set("ground-loses", ps.groundLost);
		}
	}
	@Override
	public void onResearchStateChange(ResearchType rt, ResearchState state) {
		
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
		
	}
	@Override
	public void onPlanetLost(Planet planet) {
		
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
	
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		PlayerStrength ps = getStrength(battle.enemy(p));
		if (battle.spacewarWinner == p) {
			ps.spaceWon++;
		} else {
			if (battle.spacewarWinner != null) {
				ps.spaceLost++;
			}
		}
		if (battle.groundwarWinner == p) {
			ps.groundWon++;
		} else {
			if (battle.groundwarWinner != null) {
				ps.groundLost++;
			}
		}
		
	}
	@Override
	public void onAutoGroundwarStart(BattleInfo battle, AttackDefense attacker,
			AttackDefense defender) {
		double dps = 0;
		double hp = 0;
		if (battle.attacker.owner == p) {
			dps = attacker.attack;
			hp = attacker.defense;
		} else {
			dps = defender.attack;
			hp = defender.defense;
		}
		updateSpaceStrength(battle, dps, hp);
	}
	
	@Override
	public void onAutoSpacewarStart(BattleInfo battle, SpaceStrengths str) {
		double dps = 0;
		double hp = 0;
		if (battle.attacker.owner == p) {
			dps = str.attacker.attack;
			hp = str.attacker.defense;
		} else {
			dps = str.defender.attack;
			hp = str.defender.defense;
		}
		updateSpaceStrength(battle, dps, hp);
	}
	
	@Override
	public ResponseMode diplomacy(Player other, NegotiateType about,
			ApproachType approach, Object argument) {
		// TODO Auto-generated method stub
		DiplomaticRelation r = w.getRelation(p, other);
		if (r == null || !r.full) {
			return ResponseMode.NO;
		}
		
		PlayerStrength senderStrength = getStrength(other);
		
		PlanetStatistics ownStats = p.getPlanetStatistics(null);

		PlanetStatistics senderStas = computeVisibleStats(other);
		
		double rnd = p.world.random().nextDouble();
		
		switch (about) {
		case DIPLOMATIC_RELATIONS:
			if (rnd < 0.33) {
				return ResponseMode.YES;
			} else
			if (rnd < 0.66) {
				return ResponseMode.MAYBE;
			}
			break;
		case ALLY:
			if (rnd < 0.5) {
				return ResponseMode.YES;
			}
			break;
		case DARGSLAN:
			if (rnd < 0.1 && !p.id.equals("Dargslan")) {
				return ResponseMode.YES;
			}
			break;
		case MONEY:
			if (rnd < 0.5) {
				return ResponseMode.YES;
			}
			break;
		case SURRENDER:
			if (rnd < 0.1 && ownStats.planetCount < 2) {
				return ResponseMode.YES;
			}
			break;
		case TRADE:
			if (rnd < 0.5) {
				return ResponseMode.YES;
			}
			break;
		default:
		}
		return ResponseMode.NO;
	}
	/**
	 * Compute the statistics of the given player based
	 * on the current visibility settings.
	 * @param other the other player
	 * @return the statistics
	 */
	PlanetStatistics computeVisibleStats(Player other) {
		PlanetStatistics ps = new PlanetStatistics();
		
		for (Map.Entry<Planet, PlanetKnowledge> pl : p.planets.entrySet()) {
			int k = pl.getValue().ordinal();
			if (pl.getKey().owner == other && k >= PlanetKnowledge.OWNER.ordinal()) {
				ps.planetCount++;
				
				if (k >= PlanetKnowledge.STATIONS.ordinal()) {
					
				}
				if (k >= PlanetKnowledge.BUILDING.ordinal()) {
					
				}
			}
		}
		
		return ps;
	}
}
