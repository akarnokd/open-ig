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
import hu.openig.model.Building;
import hu.openig.model.DiplomaticInteraction;
import hu.openig.model.Fleet;
import hu.openig.model.GameEnvironment;
import hu.openig.model.InventoryItem;
import hu.openig.model.Message;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.ResponseMode;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.World;
import hu.openig.utils.XElement;

import java.util.List;

/**
 * An AI node representing the human player.
 * <p>UI notifications and battle behavior is refactored into this class.</p>
 * @author akarnokd, 2011.12.15.
 */
public class AIUser implements AIManager {
	/** The game environment. */
	GameEnvironment env;
	/** The world. */
	World w;
	/** The player. */
	Player p;
	@Override
	public void init(Player p) {
		this.p = p;
		this.w = p.world;
		this.env = w.env;
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub

	}

	@Override
	public void manage() {
		// TODO Auto-generated method stub

	}

	@Override
	public void apply() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResponseMode diplomacy(Player we, Player other,
			DiplomaticInteraction offer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpacewarAction spaceBattle(SpacewarWorld world, 
			List<SpacewarStructure> idles) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void groundBattle(BattleInfo battle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void groundBattleDone(BattleInfo battle) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void groundBattleInit(BattleInfo battle) {
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
	public void save(XElement out) {
		// TODO Auto-generated method stub

	}

	@Override
	public void load(XElement in) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResearchStateChange(ResearchType rt, ResearchState state) {
		if (state == ResearchState.COMPLETE) {
			Message msg = w.newMessage("message.research_completed");
			msg.priority = 40;
			msg.sound = SoundType.RESEARCH_COMPLETE;
			msg.targetResearch = rt;
			p.messageQueue.add(msg);
		}
	}

	@Override
	public void onProductionComplete(ResearchType rt) {
		Message msg = w.newMessage("message.production_completed");
		msg.priority = 40;
		msg.sound = SoundType.PRODUCTION_COMPLETE;
		msg.targetProduct = rt;
		p.messageQueue.add(msg);
	}

	@Override
	public void onDiscoverPlanet(Planet planet) {
		Message msg = w.newMessage("message.new_planet_discovered");
		msg.targetPlanet = planet;
		msg.priority = 20;
		p.messageQueue.add(msg);
	}

	@Override
	public void onDiscoverFleet(Fleet fleet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDiscoverPlayer(Player player) {
		Message msg = w.newMessage("message.new_race_discovered");
		msg.priority = 20;
		msg.label = player.getRaceLabel();
		p.messageQueue.add(msg);
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
		if (building.type.kind.equals("MainBuilding")) {
			Message msg = w.newMessage("message.colony_hub_completed");
			msg.priority = 60;
			msg.targetPlanet = planet;
			
			p.messageQueue.add(msg);
		}

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
		Message msg = w.newMessage("message.yesterday_tax_income");
		msg.priority = 20;
		msg.value = "" + w.player.yesterday.taxIncome;
		p.messageQueue.add(msg);

		msg = w.newMessage("message.yesterday_trade_income");
		msg.priority = 20;
		msg.value = "" + w.player.yesterday.tradeIncome;
		p.messageQueue.add(msg);
	}
	@Override
	public void onSatelliteDestroyed(Planet planet, InventoryItem ii) {
		Message msg = w.newMessage("message.satellite_destroyed");
		msg.priority = 50;
		msg.sound = SoundType.SATELLITE_DESTROYED;
		msg.targetPlanet = planet;
		p.messageQueue.add(msg);
	}
	@Override
	public void onPlanetDied(Planet planet) {
		Message msg = w.newMessage("message.planet_died");
		msg.priority = 80;
		msg.targetPlanet = planet;
		p.messageQueue.add(msg);
	}
	@Override
	public void onPlanetRevolt(Planet planet) {
		Message msg = w.newMessage("message.planet_revolt");
		msg.priority = 100;
		msg.sound = SoundType.REVOLT;
		msg.targetPlanet = planet;
		p.messageQueue.add(msg);
	}
	@Override
	public void onPlanetConquered(Planet planet, Player lastOwner) {
		Message msgConq = w.newMessage("message.planet_conquered");
		msgConq.priority = 100;
		msgConq.targetPlanet = planet;
		p.messageQueue.add(msgConq);
	}
	@Override
	public void onPlanetLost(Planet planet) {
		Message msgLost = w.newMessage("message.planet_lost");
		msgLost.priority = 100;
		msgLost.targetPlanet = planet;
		p.messageQueue.add(msgLost);
	}
	@Override
	public void onRadar() {
		// TODO Auto-generated method stub
		
	}
}
