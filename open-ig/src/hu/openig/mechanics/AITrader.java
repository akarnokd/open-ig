/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.AIManager;
import hu.openig.model.Building;
import hu.openig.model.DiplomaticInteraction;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.ResponseMode;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.World;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
	final List<TraderFleet> fleets = U.newLinkedList();
	/** List of the planets with trader's spaceport. */ 
	final List<Planet> planets = U.newArrayList();
	// -----------------------------------------------------------------
	// State
	/** Map of the landed fleets. */
	final List<LandedFleet> landed = U.newLinkedList();
	/** Set of fleets turned back by space battle. */
	final Set<Fleet> fleetTurnedBack = U.newHashSet();
	/** The last visited planet of the fleet. */
	final Map<Fleet, Planet> lastVisitedPlanet = U.newHashMap();
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
		planets.clear();
		// get fleets
		for (Fleet f : player.fleets.keySet()) {
			if (f.owner == player) {
				TraderFleet tf = new TraderFleet();
				tf.fleet = f;
				tf.target = f.targetPlanet();
				tf.arrivedAt = f.arrivedAt;
				fleets.add(tf);
			}
		}
		for (Planet pl : world.planets.values()) {
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
	/** @return the maximum number of active fleets. */
	public int maxFleets() {
		return planets.size() / 2;
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
		while (activeCount < maxFleets()) {
			// create new fleets as landed
			Fleet nf = createFleet();
			
			LandedFleet lf = new LandedFleet();
			lf.fleet = nf;
			lf.target = world.random(planets);
			nf.x = lf.target.x;
			nf.y = lf.target.y;
			
			landed.add(lf);
			activeCount++;
			if (++actions >= actionCount) {
				break;
			}
		}
		
		if (activeCount > maxFleets()) {
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
						int nt = world.random().nextInt(planets.size());
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
	/**
	 * @return create a new random fleet
	 */
	public Fleet createFleet() {
		Fleet nf = new Fleet();
		nf.id = world.fleetIdSequence++;
		nf.owner = player;
		nf.name = traderLabel;
		List<ResearchType> rts = U.newArrayList();
		for (ResearchType rt : world.researches.values()) {
			if (rt.race.contains(player.race)) {
				rts.add(rt);
			}
		}
		InventoryItem ii = new InventoryItem();
		ii.owner = player;
		ii.count = 1;
		ii.type = world.random(rts);
		ii.hp = world.getHitpoints(ii.type);
		
		nf.inventory.add(ii);
		return nf;
	}
	/**
	 * Creates a fleet with the given ship tech id as its single inventory.
	 * @param id the tech id of the ship
	 * @return create a new fleet
	 */
	public Fleet createFleet(String id) {
		// locate research
		ResearchType type = null;
		for (ResearchType rt : world.researches.values()) {
			if (rt.race.contains(player.race) && rt.id.equals(id)) {
				type = rt;
				break;
			}
		}
		if (type != null) {
			Fleet nf = new Fleet();
			nf.owner = player;
			nf.name = traderLabel;
			InventoryItem ii = new InventoryItem();
			ii.owner = player;
			ii.count = 1;
			ii.type = type;
			ii.hp = world.getHitpoints(ii.type);
			
			nf.inventory.add(ii);
			return nf;
		} 
		throw new AssertionError("Traders missing technology " + id);
	}

	@Override
	public ResponseMode diplomacy(Player other,
			DiplomaticInteraction offer) {
		// No diplomatic relations
		return ResponseMode.NO;
	}

	@Override
	public SpacewarAction spaceBattle(SpacewarWorld world, 
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
				} else {
					s.fleet.targetPlanet(nearest(player.world.planets.values(), s.fleet));
				}
				s.fleet.mode = FleetMode.MOVE;
			}
			return SpacewarAction.FLEE;
		} else {
			// move a bit forward
			for (SpacewarStructure s : idles) {
				s.moveTo = new Point2D.Double(s.x + world.facing() * s.movementSpeed, s.y);
			}			
		}
		return SpacewarAction.CONTINUE;
	}
	/**
	 * Returns the nearest owned planet planet.
	 * @param planets the collection of planets
	 * @param fleet the fleet
	 * @return the planet
	 */
	Planet nearest(Collection<Planet> planets, final Fleet fleet) {
		return Collections.min(planets, new Comparator<Planet>() {
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
		// NO ground battle involvement
	}

	@Override
	public void groundBattleDone(GroundwarWorld battle) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void groundBattleInit(GroundwarWorld battle) {
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
		for (XElement xlf : in.childrenWithName("landed")) {
			String fid = xlf.get("fleet");
			String pid = xlf.get("planet");
			if (fid != null && pid != null) {
				Planet p = world.planets.get(pid);
				int ttl = xlf.getInt("ttl");
				Fleet f = createFleet(fid);
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
			int fid = xtb.getInt("fleet", -1);
			if (fid >= 0) {
				Fleet f = player.fleet(fid);
				if (f != null) {
					fleetTurnedBack.add(f);
				}
			}
		}
		for (XElement xlast : in.childrenWithName("last-visit")) {
			int fid = xlast.getInt("fleet", -1);
			if (fid >= 0) {
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
	}
	@Override
	public void save(XElement out) {
		for (LandedFleet lf : landed) {
			if (lf.fleet.inventory.size() > 0) {
				XElement xlf = out.add("landed");
				if (lf.target != null) {
					xlf.set("fleet", lf.fleet.inventory.get(0).type.id);
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
	@Override
	public void onRadar() {
		// TODO Auto-generated method stub
		
	}
}
