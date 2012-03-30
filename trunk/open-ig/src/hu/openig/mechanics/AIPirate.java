/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Pair;
import hu.openig.model.AIControls;
import hu.openig.model.AIManager;
import hu.openig.model.AIWorld;
import hu.openig.model.Building;
import hu.openig.model.DiplomaticInteraction;
import hu.openig.model.Fleet;
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
import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.List;

/**
 * AI for pirate attacks.
 * @author akarnokd, 2011.12.08.
 */
public class AIPirate implements AIManager {
	/** The player. */
	protected Player p;
	/** The copy of world state. */
	AIWorld world;
	/** The controls. */
	AIControls controls;
	/** The list of actions to apply. */
	final List<Action0> applyActions = new ArrayList<Action0>();
	/** The spacewar hitpoints. */
	double battleHP;
	@Override
	public void init(Player p) {
		this.p = p;
		controls = new DefaultAIControls(p);
	}
	
	@Override
	public void prepare() {
		world = new AIWorld();
		world.assign(p);
	}
	
	@Override
	public void manage() {
		List<Action0> acts = null;

		acts = new ColonyPlanner(world, controls).run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
			return;
		}
		
	}
	@Override
	public void apply() {
		p.money += 30 * (world.ownPlanets.size() + 1); // FIXME is enough?
		for (Action0 a : applyActions) {
			a.invoke();
		}
		applyActions.clear();

		world = null;
	}

	@Override
	public ResponseMode diplomacy(Player other,
			DiplomaticInteraction offer) {
		// No diplomatic options
		return ResponseMode.NO;
	}

	@Override
	public SpacewarAction spaceBattle(SpacewarWorld world,
			List<SpacewarStructure> idles) {
		List<SpacewarStructure> sts = world.structures(p);
		Pair<Double, Double> fh = AI.fleetHealth(sts);
		if (fh.first * 4 >= battleHP) {
			AI.defaultAttackBehavior(world, idles, p);
		} else {
			for (SpacewarStructure s : sts) {
				world.flee(s);
			}
			world.battle().enemyFlee = true;
		}
		return SpacewarAction.CONTINUE;
	}

	@Override
	public void groundBattle(GroundwarWorld battle) {
		// No ground battle
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
		battleHP = 0;		
		List<SpacewarStructure> sts = world.structures(p);
		battleHP = AI.fleetHealth(sts).second;
	}
	
	@Override
	public void load(XElement in) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void save(XElement out) {
		// TODO Auto-generated method stub
		
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
