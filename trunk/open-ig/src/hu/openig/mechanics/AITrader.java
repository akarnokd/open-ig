/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.AIManager;
import hu.openig.model.BattleInfo;
import hu.openig.model.DiplomaticInteraction;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.InventoryItem;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.ResponseMode;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.World;
import hu.openig.utils.JavaUtils;
import hu.openig.utils.XElement;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI for managing the trader's fleet.
 * @author akarnokd, 2011.12.08.
 */
public class AITrader implements AIManager {
	/** List of the trader's fleets. */
	final List<TraderFleet> fleets = JavaUtils.newLinkedList();
	/** List of the planets with trader's spaceport. */ 
	final List<Planet> planets = JavaUtils.newArrayList();
	// -----------------------------------------------------------------
	// State
	/** Map of the landed fleets. */
	final List<LandedFleet> landed = JavaUtils.newLinkedList();
	/** Set of fleets turned back by space battle. */
	final Set<Fleet> fleetTurnedBack = JavaUtils.newHashSet();
	/** The last visited planet of the fleet. */
	final Map<Fleet, Planet> lastVisitedPlanet = JavaUtils.newHashMap();
	// -----------------------------------------------------------------
	/** The world. */
	World world;
	/** The label to use for naming a trader fleet. */
	String traderLabel;
	/** How many new ships should emerge? */
	int actionCount;
	/**
	 * Constructor.
	 * @param label the fleet name label
	 */
	public AITrader(String label) {
		this.traderLabel = label;
	}
	/**
	 * A trader fleet status.
	 * @author akarnokd, Dec 8, 2011
	 */
	class TraderFleet {
		/** The fleet backreference. */
		Fleet fleet;
		/** The target planet. */
		Planet target;
		/** The target planet. */
		Planet arrivedAt;
	}
	/**
	 * Landed fleet's properties.
	 * @author akarnokd, Dec 8, 2011
	 */
	class LandedFleet {
		/** The fleet. */
		Fleet fleet;
		/** The target planet. */
		Planet target;
		/** The time to remain landed. */
		int ttl;
	}
	/** The time for staying landed. */
	public static final int LANDING_TTL = 11;
	/** The time for staying landed if the fleet was turned back by a player attack. */
	public static final int TURN_BACK_TTL = 61;
	/** The player. */
	private Player player;
	@Override
	public void prepare(World w, Player p) {
		this.world = w;
		this.player = p;
		
		switch (w.difficulty) {
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
		planets.clear();
		// get fleets
		for (Fleet f : p.fleets.keySet()) {
			if (f.owner == p) {
				TraderFleet tf = new TraderFleet();
				tf.fleet = f;
				tf.target = f.targetPlanet();
				tf.arrivedAt = f.arrivedAt;
				fleets.add(tf);
			}
		}
		for (Planet pl : w.planets.values()) {
			if (pl.owner != null) {
				planets.add(pl);
//				for (Building b : pl.surface.buildings) {
//					if ("TradersSpaceport".equals(b.type.id) && b.isOperational()) {
//						planets.add(pl);
//						break;
//					}
//				}
			}
		}
	}
	@Override
	public void manage() {
		
	}
	@Override
	public void apply() {
		// check if fleet arrived at target
		int activeCount = landed.size();
		for (TraderFleet tf : fleets) {
			if (tf.target == null) {
				LandedFleet lf = new LandedFleet();
				lf.fleet = tf.fleet;
				lf.target = tf.arrivedAt;
				lf.ttl = fleetTurnedBack.contains(lf.fleet) ? TURN_BACK_TTL : LANDING_TTL;
				landed.add(lf);
				world.removeFleet(lf.fleet);
				lastVisitedPlanet.remove(lf.fleet);
			}
			activeCount++;
		}
		int actions = 0;
		// if more planets available than fleets
		while (activeCount < planets.size()) {
			// create new fleets as landed
			Fleet nf = new Fleet();
			nf.owner = player;
			nf.name = traderLabel;
			List<ResearchType> rts = JavaUtils.newArrayList();
			for (ResearchType rt : world.researches.values()) {
				if (rt.race.contains(player.race)) {
					rts.add(rt);
				}
			}
			int rnd = world.random.get().nextInt(rts.size());
			InventoryItem ii = new InventoryItem();
			ii.owner = player;
			ii.count = 1;
			ii.type = rts.get(rnd);
			ii.hp = world.getHitpoints(ii.type);
			
			nf.inventory.add(ii);
			LandedFleet lf = new LandedFleet();
			lf.fleet = nf;
			rnd = world.random.get().nextInt(planets.size());
			lf.target = planets.get(rnd);
			nf.x = lf.target.x;
			nf.y = lf.target.y;
			
			landed.add(lf);
			activeCount++;
			if (++actions >= actionCount) {
				break;
			}
		}
		
		if (activeCount > planets.size()) {
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
		// progress landed TTL and let them emerge
		Iterator<LandedFleet> it = landed.iterator();
		while (it.hasNext()) {
			LandedFleet lf = it.next();
			// if time out, put back the fleet
			if (--lf.ttl <= 0) {
				// if there is a
				if (planets.size() > 1) {
					// select a random trader planet
					

					while (!Thread.currentThread().isInterrupted()) {
						int nt = world.random.get().nextInt(planets.size());
						Planet np = planets.get(nt);
						if (np != lf.target) {
							lf.fleet.targetPlanet(np);
							break;
						}
					}
					lf.fleet.owner.fleets.put(lf.fleet, FleetKnowledge.FULL);
					lf.fleet.mode = FleetMode.MOVE;
					// restore hp
					for (InventoryItem ii : lf.fleet.inventory) {
						ii.hp = world.getHitpoints(ii.type);
					}
					
					lastVisitedPlanet.put(lf.fleet, lf.target);
					
				} else
				if (planets.size() == 1 && planets.get(0) != lf.target) {
					lf.fleet.owner.fleets.put(lf.fleet, FleetKnowledge.FULL);
					lf.fleet.mode = FleetMode.MOVE;
					lf.fleet.targetPlanet(planets.get(0));
					// restore hp
					for (InventoryItem ii : lf.fleet.inventory) {
						ii.hp = world.getHitpoints(ii.type);
					}
					
					lastVisitedPlanet.put(lf.fleet, lf.target);
				}
				
				it.remove();
				if (++actions >= actionCount) {
					break;
				}
			}
		}
	}

	@Override
	public ResponseMode diplomacy(World world, Player we, Player other,
			DiplomaticInteraction offer) {
		// No diplomatic relations
		return ResponseMode.NO;
	}

	@Override
	public SpacewarAction spaceBattle(SpacewarWorld world, Player player,
			List<SpacewarStructure> idles) {
		double hpMax = 0;
		double hp = 0;
		for (SpacewarStructure s : idles) {
			hpMax += s.hpMax;
			hp += s.hp;
		}
		if (hp * 2 < hpMax) {
			for (SpacewarStructure s : idles) {
				world.flee(s);
				fleetTurnedBack.add(s.fleet);
				Planet pl = lastVisitedPlanet.get(s.fleet);
				if (pl != null) {
					s.fleet.targetPlanet(pl);
					s.fleet.mode = FleetMode.MOVE;
				} else {
					System.err.printf("Fleet %s of %s did not emerge from a planet!%n", s.fleet.id, s.fleet.owner.id);
				}
			}
			return SpacewarAction.FLEE;
		}
		return SpacewarAction.CONTINUE;
	}
	
	@Override
	public void groundBattle(World world, Player we, BattleInfo battle) {
		// NO ground battle involvement
	}

	@Override
	public void load(XElement in, World world, Player player) {
		for (XElement xlf : in.childrenWithName("landed")) {
			int fid = xlf.getInt("fleet");
			Fleet f = player.fleet(fid);
			if (f != null) {
				int ttl = xlf.getInt("ttl");
				String pid = xlf.get("planet");
				Planet p = world.planets.get(pid);
				
				if (p != null) {
					LandedFleet lf = new LandedFleet();
					lf.fleet = f;
					lf.target = p;
					lf.ttl = ttl;
					landed.add(lf);
				}
			}
		}
		for (XElement xtb : in.childrenWithName("turned-back")) {
			int fid = xtb.getInt("fleet");
			Fleet f = player.fleet(fid);
			if (f != null) {
				fleetTurnedBack.add(f);
			}
		}
		for (XElement xlast : in.childrenWithName("last-visit")) {
			int fid = xlast.getInt("fleet");
			Fleet f = player.fleet(fid);
			if (f != null) {
				String pid = xlast.get("planet");
				Planet p = world.planets.get(pid);
				
				if (p != null) {
					lastVisitedPlanet.put(f, p);
				}
			}
		}
	}
	@Override
	public void save(XElement out, World world, Player player) {
		for (LandedFleet lf : landed) {
			XElement xlf = out.add("landed");
			xlf.set("fleet", lf.fleet.id);
			xlf.set("planet", lf.target.id);
			xlf.set("ttl", lf.ttl);
		}
		for (Fleet tb : fleetTurnedBack) {
			XElement xtb = out.add("turned-back");
			xtb.set("fleet", tb.id);
		}
		for (Map.Entry<Fleet, Planet> last : lastVisitedPlanet.entrySet()) {
			XElement xlast = out.add("last-visit");
			xlast.set("fleet", last.getKey().id);
			xlast.set("planet", last.getValue().id);
		}
	}
}
