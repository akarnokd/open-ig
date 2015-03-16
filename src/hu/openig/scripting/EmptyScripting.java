/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting;

import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Chats.Chat;
import hu.openig.model.Chats.Node;
import hu.openig.model.Fleet;
import hu.openig.model.GameScripting;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.SpacewarScriptResult;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.VideoMessage;
import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * An empty placeholder scripting for skirmish games.
 * @author akarnokd, 2012.01.12.
 */
public class EmptyScripting implements GameScripting {
	/** The main player. */
	protected Player player;
	/** Resume after win? */
	boolean resumeAfterWin;
	@Override
	public List<VideoMessage> getSendMessages() {
		return new ArrayList<>();
	}

	@Override
	public List<Objective> currentObjectives() {
		return Collections.emptyList();
	}
	
	@Override
	public void init(Player player, XElement in) {
		this.player = player;
	}

	@Override
	public void load(XElement in) {
		resumeAfterWin = in.getBoolean("resumeAfterWin", false);
	}

	@Override
	public void save(XElement out) {
		out.set("resumeAfterWin", resumeAfterWin);
	}
	@Override
	public void done() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onResearched(Player player, ResearchType rt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProduced(Player player, ResearchType rt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroyed(Fleet winner, Fleet loser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onColonized(Planet planet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConquered(Planet planet, Player previousOwner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerBeaten(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDiscovered(Player player, Planet planet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDiscovered(Player player, Player other) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDiscovered(Player player, Fleet fleet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLostSight(Player player, Fleet fleet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFleetAt(Fleet fleet, double x, double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFleetAt(Fleet fleet, Fleet other) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStance(Player first, Player second) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAllyAgainst(Player first, Player second, Player commonEnemy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBattleComplete(Player player, BattleInfo battle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTime() {
		if (!resumeAfterWin) {
			int remaining = 0;
			for (Player p : player.world.players.values()) {
				if (p != player) {
					remaining += p.statistics.planetsOwned.value;
				}
			}
			if (remaining == 0) {
				resumeAfterWin = true;
				player.world.env.pause();
				player.world.env.winGame();
			}
		}
	}

	@Override
	public void onBuildingComplete(Planet planet, Building building) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRepairComplete(Planet planet, Building building) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrading(Planet planet, Building building, int newLevel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInventoryAdd(Planet planet, InventoryItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInventoryRemove(Planet planet, InventoryItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLost(Planet planet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLost(Fleet fleet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onVideoComplete(String video) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSoundComplete(String audio) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlanetInfected(Planet planet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlanetCured(Planet planet) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onMessageSeen(String id) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onNewGame() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onLevelChanged() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public SpacewarScriptResult onSpacewarStep(SpacewarWorld war) {
		return null;
	}
	@Override
	public void onGroundwarFinish(GroundwarWorld war) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onGroundwarStart(GroundwarWorld war) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onGroundwarStep(GroundwarWorld war) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean mayControlFleet(Fleet f) {
		return true;
	}
	@Override
	public boolean mayAutoSave() {
		return true;
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onAutobattleStart(BattleInfo battle) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onTalkCompleted() {
		
	}
	@Override
	public void debug() {
		JOptionPane.showMessageDialog(null, "No scripting debug available: " + getClass());
	}
	@Override
	public boolean mayPlayerAttack(Player player) {
		return false;
	}
	@Override
	public void onDeploySatellite(Planet target, Player player,
			ResearchType satellite) {
		
	}
	@Override
	public boolean fleetBlink(Fleet f) {
		return false;
	}
	@Override
	public void onFleetsMoved() {
		
	}
	@Override
	public void onSpaceChat(SpacewarWorld world, Chat chat, Node node) {
		
	}
	@Override
	public void onRecordMessage() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onLoaded() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean mayPlayerImproveDefenses(Player player) {
		return true;
	}
	@Override
	public double playerPopulationGrowthOverride(Planet planet, double simulatorValue) {
		return simulatorValue;
	}
	@Override
	public double playerTaxIncomeOverride(Planet planet, double simulatorValue) {
		return simulatorValue;
	}
	@Override
	public int fleetSpeedOverride(Fleet fleet, int speed) {
		return speed;
	}
}
