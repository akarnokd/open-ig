/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.model.AIControls;
import hu.openig.model.AIManager;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.ApproachType;
import hu.openig.model.AttackDefense;
import hu.openig.model.AutoBuild;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.Message;
import hu.openig.model.NegotiateType;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.ResponseMode;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.SpaceStrengths;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.World;
import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An AI node representing the human player.
 * <p>UI notifications and battle behavior is refactored into this class.</p>
 * @author akarnokd, 2011.12.15.
 */
public class AIUser implements AIManager {
	/** The world. */
	World w;
	/** The player. */
	Player p;
	/** The copy of world state. */
	AIWorld world;
	/** The controls. */
	AIControls controls;
	/** The list of actions to apply. */
	final List<Action0> applyActions = new ArrayList<>();
	/** The detected attacks. */
	final Set<Integer> detectedAttack = new HashSet<>();
	/** Allow building vehicles? */
	boolean aiAutoBuildProduction;
	@Override
	public void init(Player p) {
		this.p = p;
		this.w = p.world;
		controls = new DefaultAIControls(p);
	}

	@Override
	public void prepare() {
		world = new AIWorld();
		world.assign(p);
		aiAutoBuildProduction = p.world.config.aiAutoBuildProduction;
	}

	@Override
	public void manage() {
		List<Action0> acts;

		if (world.money < world.autoBuildLimit) {
			return;
		}
		
		List<AIPlanet> ownSave = new ArrayList<>(world.ownPlanets);
		// manage only AI autobuild planets
		Iterator<AIPlanet> it = world.ownPlanets.iterator();
		while (it.hasNext()) {
			AIPlanet p = it.next();
			if (p.autoBuild != AutoBuild.AI) {
				it.remove();
			}
		}
		
		acts = new ColonyPlanner(world, controls).run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
			if (world.money < world.autoBuildLimit) {
				return;
			}
		}
		acts = new EconomyPlanner(world, controls).run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
			if (world.money < world.autoBuildLimit) {
				return;
			}
		}
		
		StaticDefensePlanner staticDefensePlanner = new StaticDefensePlanner(world, controls);
		staticDefensePlanner.allowProduction = aiAutoBuildProduction;
		staticDefensePlanner.allowBuildOrbitalFactory = false;
		acts = staticDefensePlanner.run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
			if (world.money < world.autoBuildLimit) {
				return;
			}
		}
		
		world.ownPlanets.clear();
		world.ownPlanets.addAll(ownSave);
		ColonizationPlanner colonizationPlanner = new ColonizationPlanner(world, controls);
		colonizationPlanner.explicitMode = true;
		acts = colonizationPlanner.run();
		if (!acts.isEmpty()) {
			applyActions.addAll(acts);
		}

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
	public ResponseMode diplomacy(Player other, NegotiateType about,
			ApproachType approach, Object argument) {
		return ResponseMode.NO;
	}
	
	@Override
	public SpacewarAction spaceBattle(SpacewarWorld world, 
			List<SpacewarStructure> idles) {
		return SpacewarAction.CONTINUE;
	}

	@Override
	public void groundBattle(GroundwarWorld battle) {

	}

	@Override
	public void groundBattleDone(GroundwarWorld battle) {
		detectedAttack.remove(battle.battle().attacker.id);
	}
	@Override
	public void groundBattleInit(GroundwarWorld battle) {
		
	}
	@Override
	public void spaceBattleDone(SpacewarWorld world) {
		detectedAttack.remove(world.battle().attacker.id);
	}
	@Override
	public void spaceBattleInit(SpacewarWorld world) {
		
	}
	
	@Override
	public void save(XElement out) {

	}

	@Override
	public void load(XElement in) {

	}

	@Override
	public void onResearchStateChange(ResearchType rt, ResearchState state) {
		if (state == ResearchState.COMPLETE) {
			Message msg = w.newMessage("message.research_completed");
			msg.priority = 40;
			msg.sound = SoundType.RESEARCH_COMPLETE;
			msg.targetResearch = rt;
			p.addMessage(msg);
			if (rt.category.main != ResearchMainCategory.BUILDINGS) {
				p.addProductionHistory(rt);
			}
		} else
		if (state == ResearchState.LAB) {
			Message msg = w.newMessage("message.research_lab");
			msg.priority = 80;
			msg.sound = SoundType.STOP_RESEARCH;
			msg.targetResearch = rt;
			p.addMessage(msg);
		} else
		if (state == ResearchState.MONEY) {
			Message msg = w.newMessage("message.research_money");
			msg.priority = 80;
			msg.sound = SoundType.STOP_RESEARCH;
			msg.targetResearch = rt;
			p.addMessage(msg);
		}
	}

	@Override
	public void onProductionComplete(ResearchType rt) {
		Message msg = w.newMessage("message.production_completed");
		msg.priority = 40;
		msg.sound = SoundType.PRODUCTION_COMPLETE;
		msg.targetProduct = rt;
		p.addMessage(msg);
	}

	@Override
	public void onDiscoverPlanet(Planet planet) {
		Message msg = w.newMessage("message.new_planet_discovered");
		msg.targetPlanet = planet;
		msg.priority = 20;
		p.addMessage(msg);
	}

	@Override
	public void onDiscoverFleet(Fleet fleet) {
	}

	@Override
	public void onDiscoverPlayer(Player player) {
		if (!player.noDiplomacy) {
			if (!player.race.equals(this.p.race)) {
				Message msg = w.newMessage("message.new_race_discovered");
				msg.priority = 20;
				msg.label = player.getRaceLabel();
				p.addMessage(msg);
			}
			Message msg = w.newMessage("message.new_player_discovered");
			msg.priority = 20;
			msg.value = player.name;
			p.addMessage(msg);
		}
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
		if (building.type.kind.equals("MainBuilding")) {
			Message msg = w.newMessage("message.colony_hub_completed");
			msg.priority = 60;
			msg.targetPlanet = planet;
			
			p.addMessage(msg);
		}

	}

	@Override
	public void onLostSight(Fleet fleet) {
	}

	@Override
	public void onLostTarget(Fleet fleet, Fleet target) {

	}
	@Override
	public void onNewDay() {
		Message msg = w.newMessage("message.yesterday_trade_income");
		msg.priority = 20;
		msg.value = "" + w.player.yesterday.tradeIncome;
		p.addMessage(msg);

		Message msg2 = w.newMessage("message.yesterday_tax_income");
		msg2.timestamp = msg.timestamp;
		msg2.priority = 21;
		msg2.value = "" + w.player.yesterday.taxIncome;
		p.addMessage(msg2);

	}
	@Override
	public void onSatelliteDestroyed(Planet planet, InventoryItem ii) {
		Message msg = w.newMessage("message.satellite_destroyed");
		msg.priority = 50;
		msg.sound = SoundType.SATELLITE_DESTROYED;
		msg.targetPlanet = planet;
		p.addMessage(msg);
	}
	@Override
	public void onPlanetDied(Planet planet) {
		Message msg = w.newMessage("message.planet_died");
		msg.priority = 80;
		msg.targetPlanet = planet;
		p.addMessage(msg);
	}
	@Override
	public void onPlanetRevolt(Planet planet) {
		// once per transition
		if (planet.morale <= 15 && planet.lastMorale > 15) {
			Message msg = w.newMessage("message.planet_revolt");
			msg.priority = 100;
			msg.sound = SoundType.REVOLT;
			msg.targetPlanet = planet;
			p.addMessage(msg);
		}
	}
	@Override
	public void onPlanetConquered(Planet planet, Player lastOwner) {
		Message msgConq = w.newMessage("message.planet_conquered");
		msgConq.priority = 100;
		msgConq.targetPlanet = planet;
		p.addMessage(msgConq);
	}
	@Override
	public void onPlanetColonized(Planet planet) {
		Message msgLost = w.newMessage("message.planet_colonized");
		msgLost.priority = 50;
		msgLost.targetPlanet = planet;
		p.addMessage(msgLost);
	}
	@Override
	public void onPlanetLost(Planet planet) {
		Message msgLost = w.newMessage("message.planet_lost");
		msgLost.priority = 100;
		msgLost.targetPlanet = planet;
		p.addMessage(msgLost);
	}
	@Override
	public void onRadar() {
		for (Fleet fleet : p.fleets.keySet()) {
			if (fleet.owner != p) {
				if (!detectedAttack.contains(fleet.id)) {
					if (fleet.mode == FleetMode.ATTACK 
							&& ((fleet.targetFleet != null && fleet.targetFleet.owner == p)
							|| (fleet.targetPlanet() != null && fleet.targetPlanet().owner == p))) {
						if (w.env.config().slowOnEnemyAttack) {
							w.env.speed1();
						}
						w.env.playSound(SoundTarget.COMPUTER, SoundType.ENEMY_FLEET_DETECTED, null);
						if (fleet.targetFleet != null) {
							Message msg = w.newMessage("message.enemy_fleet_detected");
							msg.priority = 100;
							p.addMessage(msg);
						} else {
							Message msg = w.newMessage("message.enemy_fleet_detected_at");
							msg.priority = 100;
							msg.targetPlanet = fleet.targetPlanet();
							p.addMessage(msg);
						}
						detectedAttack.add(fleet.id);
					}
				}
			}
		}
		// cleanup detection
		Iterator<Integer> it = detectedAttack.iterator();
		while (it.hasNext()) {
			int i = it.next();
			Fleet f = w.findFleet(i);
			if (f == null) {
				it.remove();
			} else
			if (!p.fleets.containsKey(f)) {
				it.remove();
			}
		}
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
}
