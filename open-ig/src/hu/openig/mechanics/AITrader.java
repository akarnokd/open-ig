/*
 * Copyright 2008-2011, David Karnok 
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
import hu.openig.model.World;
import hu.openig.utils.JavaUtils;
import hu.openig.utils.XElement;

import java.util.Iterator;
import java.util.List;

/**
 * AI for managing the trader's fleet.
 * @author akarnokd, 2011.12.08.
 */
public class AITrader implements AIManager {
	/** List of the trader's fleets. */
	final List<TraderFleet> fleets = JavaUtils.newLinkedList();
	/** List of the planets with trader's spaceport. */ 
	final List<Planet> planets = JavaUtils.newArrayList();
	/** Map of the landed fleets. */
	final List<LandedFleet> landed = JavaUtils.newLinkedList();
	/** The world. */
	World world;
	/** The label to use for naming a trader fleet. */
	String traderLabel;
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
	/** The player. */
	private Player player;
	@Override
	public void prepare(World w, Player p) {
		this.world = w;
		this.player = p;
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
				lf.ttl = LANDING_TTL;
				landed.add(lf);
				world.removeFleet(lf.fleet);
			}
			activeCount++;
		}
		
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
		}
		
		if (activeCount > planets.size()) {
			// remove landed fleets
			Iterator<LandedFleet> it = landed.iterator();
			while (landed.size() > 0 && activeCount > planets.size()) {
				it.next();
				it.remove();
				activeCount--;
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
				} else
				if (planets.size() == 1 && planets.get(0) != lf.target) {
					lf.fleet.owner.fleets.put(lf.fleet, FleetKnowledge.FULL);
					lf.fleet.mode = FleetMode.MOVE;
					lf.fleet.targetPlanet(planets.get(0));
					// restore hp
					for (InventoryItem ii : lf.fleet.inventory) {
						ii.hp = world.getHitpoints(ii.type);
					}
				}
				
				it.remove();
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
	public void spaceBattle(World world, Player we, BattleInfo battle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void groundBattle(World world, Player we, BattleInfo battle) {
		// NO ground battle involvement
	}

	@Override
	public void save(XElement out) {
		// TODO Auto-generated method stub

	}

	@Override
	public void load(XElement in) {
		// TODO Auto-generated method stub

	}

}
