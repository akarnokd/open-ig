/*
 * Copyright 2008-2014, David Karnok 
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
import hu.openig.model.AISpaceBattleManager;
import hu.openig.model.AIWorld;
import hu.openig.model.ApproachType;
import hu.openig.model.AttackDefense;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.NegotiateType;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.ResponseMode;
import hu.openig.model.SpaceStrengths;
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
	final List<Action0> applyActions = new ArrayList<>();
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
		List<Action0> acts;

		acts = new ColonyPlanner(world, controls).run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
        }
		
	}
	@Override
	public void apply() {
		p.addMoney(30 * (world.ownPlanets.size() + 1)); // FIXME is enough?
		for (Action0 a : applyActions) {
			a.invoke();
		}
		applyActions.clear();

		world = null;
	}

	@Override
	public ResponseMode diplomacy(Player other, NegotiateType about,
			ApproachType approach, Object argument) {
		return ResponseMode.NO;
	}

	@Override
	public AISpaceBattleManager spaceBattle(SpacewarWorld sworld) {
		return new AIPirateSpaceBattle(p, this, sworld);
	}
	
	/**
	 * Pirate space battle manager.
	 * @author akarnokd, 2014.04.01.
	 */
	final class AIPirateSpaceBattle extends AIDefaultSpaceBattle {
		/**
		 * Constructor.
		 * @param p the player
		 * @param ai the AI
		 * @param world the spacewar world
		 */
		public AIPirateSpaceBattle(Player p, AIManager ai, SpacewarWorld world) {
			super(p, ai, world);
		}
		@Override
		public SpacewarAction spaceBattle(List<SpacewarStructure> idles) {
			List<SpacewarStructure> sts = world.structures(p);
			Pair<Double, Double> fh = health(sts);
			
			if (fh.first * 4 >= initialDefense) {
				AI.defaultAttackBehavior(world, idles, p);
			} else {
				for (SpacewarStructure s : sts) {
					world.flee(s);
				}
				world.battle().enemyFlee = true;
			}
			return SpacewarAction.CONTINUE;
		}
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
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onAutoGroundwarStart(BattleInfo battle, AttackDefense attacker,
			AttackDefense defender) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onAutoSpacewarStart(BattleInfo battle, SpaceStrengths str) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPlanetColonized(Planet planet) {
		// TODO Auto-generated method stub
		
	}
}
