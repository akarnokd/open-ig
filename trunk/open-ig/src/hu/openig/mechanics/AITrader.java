/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Pair;
import hu.openig.model.AIManager;
import hu.openig.model.AISpaceBattleManager;
import hu.openig.model.ApproachType;
import hu.openig.model.AttackDefense;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.ModelUtils;
import hu.openig.model.NegotiateType;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.ResponseMode;
import hu.openig.model.SpaceStrengths;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarStructure.StructureType;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.World;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI for managing the trader's fleet.
 * @author akarnokd, 2011.12.08.
 */
public class AITrader implements AIManager {
	/** The incoming chat. */
	private static final String CHAT_VIRUS_INCOMING = "chat.virus.incoming";
	/** List of the trader's fleets. */
	final List<TraderFleet> fleets = new ArrayList<>();
	/** List of the planets with trader's spaceport. */ 
	final List<Planet> planets = new ArrayList<>();
	// -----------------------------------------------------------------
	// State
	/** Map of the landed fleets. */
	final List<LandedFleet> landed = new ArrayList<>();
	/** Set of fleets turned back by space battle. */
	final Set<Fleet> fleetTurnedBack = new HashSet<>();
	/** The last visited planet of the fleet. */
	final Map<Fleet, Planet> lastVisitedPlanet = new HashMap<>();
	/** The list of existing fleet indexes ever created. */
	final List<Integer> createdFleets = new ArrayList<>();
	// -----------------------------------------------------------------
	/** The world. */
	World world;
	/** The label to use for naming a trader fleet. */
	String traderLabel;
	/** How many new ships should emerge? */
	int actionCount;
	/**
	 * A trader fleet status.
	 * @author akarnokd, Dec 8, 2011
	 */
	static class TraderFleet {
		/** The fleet backreference. */
		Fleet fleet;
		/** The target planet. */
		Planet target;
		/** The target planet. */
		Planet arrivedAt;
		/** The current task. */
		FleetTask task;
		@Override
		public String toString() {
			return String.format("TraderFleet { Fleet = %s, Target = %s, Arrived = %s, Task = %s }", fleet, target, arrivedAt, task);
		}
	}
	/**
	 * Landed fleet's properties.
	 * @author akarnokd, Dec 8, 2011
	 */
	static class LandedFleet {
		/** The fleet. */
		Fleet fleet;
		/** The target planet. */
		Planet target;
		/** The time to remain landed. */
		int ttl;
		@Override
		public String toString() {
			return fleet.toString() + " TTL = " + ttl ;
		}
	}
	/** The time for staying landed. */
	public static final int LANDING_TTL = 11;
	/** The time for staying landed if the fleet was turned back by a player attack. */
	public static final int TURN_BACK_TTL = 61;
	/** The player. */
	private Player player;
	@Override
	public void init(Player p) {
		this.world = p.world;
		this.traderLabel = world.env.labels().get("traders.fleetname");
		this.player = p;
	}
	@Override
	public void prepare() {
		switch (world.difficulty) {
		case EASY:
			actionCount = 1;
			break;
		case NORMAL:
			actionCount = 2;
			break;
		case HARD:
			actionCount = 4;
			break;
		default:
			actionCount = 1;
			break;
		}
		
		fleets.clear();
		// get fleets
		for (Fleet f : player.fleets.keySet()) {
			if (f.owner == player && f.task != FleetTask.SCRIPT) {
				TraderFleet tf = new TraderFleet();
				tf.fleet = f;
				tf.target = f.targetPlanet();
				tf.arrivedAt = f.arrivedAt;
				tf.task = f.task;
				fleets.add(tf);
			}
		}
		Set<String> drs = new HashSet<>();
		for (DiplomaticRelation dr0 : world.relations) {
			if (dr0.first.equals(world.player.id) 
					|| dr0.second.equals(world.player.id)) {
				if (dr0.tradeAgreement) {
					if (dr0.first.equals(world.player.id)) {
						drs.add(dr0.second);
					} else {
						drs.add(dr0.first);
					}
				}
			}
		}
		planets.clear();
		for (Planet pl : world.planets.values()) {
			if (pl.owner != null) {
				if (pl.owner == world.player 
						|| (pl.owner == player)
						|| drs.contains(pl.owner.id)) {
					planets.add(pl);
				}
			}
		}
	}
	@Override
	public void manage() {
		
	}
	/** @return the maximum number of active fleets. */
	public int maxFleets() {
		return Math.max(8, planets.size() / 2);
	}
	@Override
	public void apply() {
		// check if fleet arrived at target
		int activeCount = landed.size();
		activeCount = landFleets(activeCount);
		int actions = 0;
		// if more planets available than fleets
		int maxFleets = maxFleets();
		if (!planets.isEmpty()) {
			while (activeCount < maxFleets) {
				// create new fleets as landed
				Fleet nf = createFleet();
				
				LandedFleet lf = new LandedFleet();
				lf.fleet = nf;
				
				lf.target = ModelUtils.random(planets);
				lf.ttl = LANDING_TTL;
				
				nf.x = lf.target.x;
				nf.y = lf.target.y;
				
				landed.add(lf);
				
				world.removeFleet(lf.fleet);
				
				activeCount++;
				if (++actions >= actionCount) {
					break;
				}
			}
		}
		
		if (activeCount > maxFleets) {
			// remove landed fleets
			Iterator<LandedFleet> it = landed.iterator();
			while (landed.size() > 0 && activeCount > planets.size()) {
				LandedFleet lf = it.next();
				it.remove();
				activeCount--;
				lastVisitedPlanet.remove(lf.fleet);
				fleetTurnedBack.remove(lf.fleet);
			}
		}
		if (!planets.isEmpty()) {
			emergeFleets(actions);
		}
		// notice script-created fleets
		for (Fleet f : player.ownFleets()) {
			if (f.task != FleetTask.SCRIPT) {
				if (fleetIndex(f) < 0) {
					createdFleets.add(f.id);
				}
			}
		}
		// label fix
		for (Fleet f : player.ownFleets()) {
			if (f.task != FleetTask.SCRIPT) {
				f.name(traderLabel + " " + (fleetIndex(f) + 1));
			}
		}
	}
	/**
	 * Emerge fleets based on remaining action count.
	 * @param actions the actions.
	 */
	public void emergeFleets(int actions) {
		// progress landed TTL and let them emerge
		Iterator<LandedFleet> it = landed.iterator();
		while (it.hasNext()) {
			LandedFleet lf = it.next();
			// if time out, put back the fleet
			if (--lf.ttl <= 0) {
				if (lf.target == null) {
					lf.target = ModelUtils.random(planets);
				}
				boolean infected = lf.target.quarantineTTL > 0;

				if (!planets.isEmpty()) {
					List<Planet> planets2 = U.newArrayList(planets);
					planets2.remove(lf.target);
					if (!planets2.isEmpty()) {
						Planet nt = ModelUtils.random(planets2);
						if (infected) {
							world.infectedFleets.put(lf.fleet.id, lf.target.id);
							if (nt.owner != lf.target.owner) {
								List<Planet> cand2 = lf.target.owner.ownPlanets();
								cand2.remove(lf.target);
								nt = ModelUtils.random(cand2);
							}
						}
						lf.fleet.moveTo(nt);
						if (nt == lf.target) {
							System.err.println("Going back already? " + nt.id);
						}
	
						for (InventoryItem ii : lf.fleet.inventory.iterable()) {
							ii.hp = world.getHitpoints(ii.type, ii.owner);
						}
	
						lf.fleet.owner.fleets.put(lf.fleet, FleetKnowledge.FULL);
						
						lastVisitedPlanet.put(lf.fleet, lf.target);
					} else {
						lastVisitedPlanet.remove(lf.fleet);
					}
				} else {
					lastVisitedPlanet.remove(lf.fleet);
				}
				
				it.remove();
				if (++actions >= actionCount) {
					break;
				}
			}
		}
	}
	/**
	 * Land the fleets that arrived at their target.
	 * @param activeCount the active fleet count
	 * @return the remaining active fleet count
	 */
	public int landFleets(int activeCount) {
		for (TraderFleet tf : fleets) {
			if (tf.target == null && tf.task != FleetTask.SCRIPT) {
				LandedFleet lf = new LandedFleet();
				lf.fleet = tf.fleet;
				lf.target = tf.arrivedAt;
				lf.ttl = fleetTurnedBack.contains(lf.fleet) ? TURN_BACK_TTL : LANDING_TTL;
				landed.add(lf);
				
				// infect planet
				if (world.infectedFleets.containsKey(lf.fleet.id)) {
					if (lf.target != null) {
						String source = world.infectedFleets.get(lf.fleet.id);
						// do not reinfect source
						if (!source.equals(lf.target.id)) {
							// reset quarantine ttl
							int ttl0 = lf.target.quarantineTTL;
							lf.target.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
							if (ttl0 == 0) {
								world.scripting.onPlanetInfected(lf.target);
							}
						}
					}
				} else {
					// special case if trader #4 reaches an infected planet
					if (lf.target != null && lf.target.quarantineTTL > 0) {
						int idx = fleetIndex(tf.fleet);
						
						int n = idx % filterChats(CHAT_VIRUS_INCOMING).size();
						
						if (n == 3) {
							lf.target.quarantineTTL = 1;
						}
					}
				}
				
				// hide
				hideFleet(lf.fleet);
			}
			activeCount++;
		}
		return activeCount;
	}
	/**
	 * Hide a fleet.
	 * @param lf the fleet to hide
	 */
	public void hideFleet(Fleet lf) {
		world.removeFleet(lf);

		lastVisitedPlanet.remove(lf);
		fleetTurnedBack.remove(lf);
	}
	/**
	 * Count how many fleets are landed on the target planet.
	 * @param p the target planet
	 * @return the count
	 */
	int landedCount(Planet p) {
		int count = 0;
		for (LandedFleet lf : landed) {
			if (lf.target == p) {
				count++;
			}
		}
		return count;
	}
	/**
	 * Count how many fleets target the planet.
	 * @param p the target planet
	 * @return the count
	 */
	int targetCount(Planet p) {
		int count = 0;
		for (Fleet f : this.player.fleets.keySet()) {
			if (f.targetPlanet() == p && f.owner == this.player) {
				count++;
			}
		}
		return count;
	}
	/**
	 * @return create a new random fleet
	 */
	public Fleet createFleet() {
		Fleet nf = new Fleet(player);
		nf.name(traderLabel);
		List<ResearchType> rts = new ArrayList<>();
		for (ResearchType rt : world.researches.values()) {
			if (rt.race.contains(player.race) && rt.category.main == ResearchMainCategory.SPACESHIPS) {
				rts.add(rt);
			}
		}
		InventoryItem ii = new InventoryItem(world.newId(), player, ModelUtils.random(rts));
		ii.count = 1;
		ii.init();
		
		nf.inventory.add(ii);
		
		createdFleets.add(nf.id);
		
		return nf;
	}
	/**
	 * Creates a fleet with the given ship tech id as its single inventory.
	 * @param id the ship identifier
	 * @param tech the technology identifier
	 * @return create a new fleet
	 */
	public Fleet createFleet(int id, String tech) {
		// locate research
		ResearchType type = null;
		for (ResearchType rt : world.researches.values()) {
			if (rt.race.contains(player.race) && rt.id.equals(tech)) {
				type = rt;
				break;
			}
		}
		if (type != null) {
			Fleet nf = id < 0 ? new Fleet(player) : new Fleet(id, player);
			nf.name(traderLabel);
			InventoryItem ii = new InventoryItem(world.newId(), player, type);
			ii.count = 1;
			ii.init();
			
			nf.inventory.add(ii);
			
			if (id < 0) {
				createdFleets.add(nf.id);
			}

			return nf;
		} 
		throw new AssertionError("Traders missing technology " + id);
	}

	@Override
	public ResponseMode diplomacy(Player other, NegotiateType about,
			ApproachType approach, Object argument) {
		return ResponseMode.NO;
	}
	
	@Override
	public AISpaceBattleManager spaceBattle(SpacewarWorld sworld) {
		return new AITraderSpaceBattle(player, this, sworld);
	}
	
	/**
	 * The Trader's space battle behavior. 
	 * @author akarnokd, 2014.04.01.
	 */
	final class AITraderSpaceBattle extends AIDefaultSpaceBattle {
		/**
		 * Constructor.
		 * @param p the player
		 * @param ai the AI
		 * @param world the spacewar world
		 */
		public AITraderSpaceBattle(Player p, AIManager ai, SpacewarWorld world) {
			super(p, ai, world);
		}
		@Override
		public void spaceBattleInit() {
			super.spaceBattleInit();
			
			BattleInfo battle = world.battle();
			Fleet our = battle.getFleet();
			
			if (our != null) {
				int idx = fleetIndex(our);
				if (idx < 0) {
					idx = our.id;
				}
				
				String filter = "chat.merchant";
				
				if (player.world.infectedFleets.containsKey(our.id)) {
					filter = "chat.virus.outgoing";
					if (battle.helperPlanet != null) {
						if (battle.helperPlanet == lastVisitedPlanet.get(our)) {
							battle.showLanding = true;
						}					
					}
				} else {
					Planet p = our.targetPlanet();
					if (p == null) {
						p = our.arrivedAt;
					}
					if (p != null && p.quarantineTTL > 0) {
						filter = CHAT_VIRUS_INCOMING;
						battle.showLanding = p == battle.helperPlanet;
					} else
					if (battle.helperPlanet != null) {
						if (p == battle.helperPlanet) {
							filter = "chat.blockade.incoming";
							battle.showLanding = true;
						} else
						if (battle.helperPlanet == lastVisitedPlanet.get(our)) {
							filter = "chat.blockade.outgoing";
							battle.showLanding = true;
						}
					}
				}
				
				List<String> chats = filterChats(filter);
				
				int comm = idx % chats.size();
				
				battle.chat = chats.get(comm);
				
				if (battle.helperPlanet != null && battle.helperPlanet.owner == battle.attacker.owner) {
					if (lastVisitedPlanet.get(our) == battle.helperPlanet) {
						
						// flip positions
						
						Dimension d = world.space();
						for (SpacewarStructure s : world.structures()) {
							if (s.type == StructureType.SHIP) {
								s.x = d.width - s.x - 100;
								if (battle.showLanding && s.owner == player) {
									s.x -= 100;
								}
								s.angle -= Math.PI;
							}
						}
						
						battle.invert = true;
					}
				}
				
			}
		}
		
		@Override
		public SpacewarAction spaceBattle(List<SpacewarStructure> idles) {
			List<SpacewarStructure> sts = world.structures(player);
			Pair<Double, Double> fh = health(sts);
			BattleInfo battle = world.battle();
			if (fh.first * 4 < initialDefense * 3) {
				if (!battle.enemyFlee) {
					for (SpacewarStructure s : sts) {
						world.flee(s);
					}
					battle.enemyFlee = true;
				}
				if (battle.showLanding) {
					Point lp = world.landingPlace();
					for (SpacewarStructure s : sts) {
						double d1 = lp.distance(s.x, s.y);
						if (d1 <= 5 || s.x >= lp.x) {
							return SpacewarAction.SURRENDER;
						}
					}
				}
				return SpacewarAction.FLEE;
			}
			// move a bit forward
			int facing = world.facing();
			
			if (battle.invert) {
				facing = -facing;
			}
			
			for (SpacewarStructure s : idles) {
				s.moveTo = new Point2D.Double(s.x + facing * s.movementSpeed, s.y);
			}			
			
			if (battle.showLanding) {
				Point lp = world.landingPlace();
				for (SpacewarStructure s : sts) {
					double d1 = lp.distance(s.x, s.y);
					if (d1 <= 5 || s.x >= lp.x) {
						return SpacewarAction.SURRENDER;
					}
				}
			}
			
			return SpacewarAction.CONTINUE;
		}
		
		@Override
		public void spaceBattleDone() {
			Fleet f = world.battle().targetFleet;
			if (f != null && world.battle().enemyFlee) {
				returnToPreviousPlanet(f);
			}
		}
	}

	/**
	 * Force the fleet to return to its previous planet.
	 * @param f the fleet
	 */
	public void returnToPreviousPlanet(Fleet f) {
		fleetTurnedBack.add(f);
		Planet pl = lastVisitedPlanet.get(f);
		if (pl != null) {
			f.targetPlanet(pl);
		} else {
			Exceptions.add(new AssertionError("Fleet " + f.id + " has no previous planet to return to!"));
			f.targetPlanet(nearest(player.world.planets.values(), f));
		}
		f.mode = FleetMode.MOVE;
	}
	/**
	 * Returns the nearest owned planet planet.
	 * @param planets the collection of planets
	 * @param fleet the fleet
	 * @return the planet
	 */
	Planet nearest(Collection<Planet> planets, final Fleet fleet) {
		List<Planet> ps = U.newArrayList(planets);
		ps.remove(fleet.targetPlanet());
		return Collections.min(ps, new Comparator<Planet>() {
			@Override
			public int compare(Planet o1, Planet o2) {
				if (o1.owner != null && o2.owner == null) {
					return -1;
				} else
				if (o1.owner == null && o2.owner != null) {
					return 1;
				} else
				if (o1.owner == null && o2.owner == null) {
					return 0;
				}
				double d1 = World.dist(o1.x, o1.y, fleet.x, fleet.y);
				double d2 = World.dist(o2.x, o2.y, fleet.x, fleet.y);
				return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
			}
		});
	}
	@Override
	public void groundBattle(GroundwarWorld battle) {
	}

	@Override
	public void groundBattleDone(GroundwarWorld battle) {
		
	}
	@Override
	public void groundBattleInit(GroundwarWorld battle) {
		
	}
	/**
	 * Returns the index of the given fleet.
	 * @param our the fleet to test
	 * @return the index
	 */
	public int fleetIndex(Fleet our) {
		return createdFleets.indexOf(our.id);
	}
	/**
	 * Filter the chat settings.
	 * @param filter the prefix
	 * @return the list of chats with the prefix
	 */
	public List<String> filterChats(String filter) {
		List<String> chats = new ArrayList<>();
		for (String c : player.world.chats.keys()) {
			if (c.startsWith(filter)) {
				chats.add(c);
			}
		}
		return chats;
	}
	
	@Override
	public void load(XElement in) {
		// restore fleet registry
		createdFleets.clear();
		for (XElement ef : in.childrenWithName("created-fleet")) {
			createdFleets.add(ef.getInt("id"));
		}
		
		// restore landed fleets
		landed.clear();
		for (XElement xlf : in.childrenWithName("landed")) {
			String fid = xlf.get("fleet", null);
			String pid = xlf.get("planet", null);
			int oid = xlf.getInt("id", -1);
			if (fid != null && pid != null) {
				Planet p = world.planets.get(pid);
				int ttl = xlf.getInt("ttl");
				Fleet f = createFleet(oid, fid);
				f.x = p.x;
				f.y = p.y;
				LandedFleet lf = new LandedFleet();
				lf.fleet = f;
				lf.target = p;
				lf.ttl = ttl;
				
				hideFleet(lf.fleet);

				landed.add(lf);
			}
		}
		fleetTurnedBack.clear();
		for (XElement xtb : in.childrenWithName("turned-back")) {
			int fid = xtb.getInt("fleet", -1);
			if (fid >= 0) {
				Fleet f = player.fleet(fid);
				if (f != null) {
					fleetTurnedBack.add(f);
				}
			}
		}
		lastVisitedPlanet.clear();
		for (XElement xlast : in.childrenWithName("last-visit")) {
			int fid = xlast.getInt("fleet", -1);
			String pid = xlast.get("planet");
			if (fid >= 0) {
				Fleet f = player.fleet(fid);
				if (f != null) {
					Planet p = world.planets.get(pid);
					if (p != null) {
						lastVisitedPlanet.put(f, p);
					} else {
						Exceptions.add(new AssertionError(String.format("Player last-visit fleet %s refers to an unknown planet %s", fid, pid)));
					}
				} else {
					System.out.println(String.format("Player last-visit refers to unknown fleet %s", fid));
				}
			}
		}
		
		// add existing but unregistered fleets
		for (Fleet f : player.ownFleets()) {
			if (!createdFleets.contains(f.id)) {
				createdFleets.add(f.id);
			}
		}
	}
	@Override
	public void save(XElement out) {
		for (LandedFleet lf : landed) {
			if (lf.fleet.inventory.size() > 0) {
				XElement xlf = out.add("landed");
				if (lf.target != null) {
					xlf.set("id", lf.fleet.id);
					xlf.set("fleet", lf.fleet.inventory.iterable().iterator().next().type.id);
					xlf.set("planet", lf.target.id);
					xlf.set("ttl", lf.ttl);
				}
			}
		}
		for (Fleet tb : fleetTurnedBack) {
			XElement xtb = out.add("turned-back");
			xtb.set("fleet", tb.id);
		}
		for (Map.Entry<Fleet, Planet> last : lastVisitedPlanet.entrySet()) {
			if (last.getValue() != null) {
				XElement xlast = out.add("last-visit");
				xlast.set("fleet", last.getKey().id);
				xlast.set("planet", last.getValue().id);
			}
		}
		for (Integer i : createdFleets) {
			out.add("created-fleet").set("id", i);
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
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		
	}
	@Override
	public void onAutoGroundwarStart(BattleInfo battle, AttackDefense attacker,
			AttackDefense defender) {
		
	}
	@Override
	public void onAutoSpacewarStart(BattleInfo battle, SpaceStrengths str) {
		
	}
	/**
	 * Set the last visited planet.
	 * @param f the fleet
	 * @param p the planet
	 */
	public void setLastVisited(Fleet f, Planet p) {
		lastVisitedPlanet.put(f, p);
	}
	@Override
	public void onPlanetColonized(Planet planet) {
		
	}
}
