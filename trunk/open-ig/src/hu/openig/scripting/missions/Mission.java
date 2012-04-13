/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.core.Pair;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.GameScriptingEvents;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarScriptResult;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.World;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * The base class for missions.
 * @author akarnokd, 2012.01.14.
 */
public abstract class Mission implements GameScriptingEvents {
	/** The world object. */
	protected World world;
	/** The player object. */
	protected Player player;
	/** The scripting helper. */
	protected MissionScriptingHelper helper;
	/**
	 * Initializes the mission object.
	 * @param player the main player
	 * @param helper the scripting helper
	 * @param settings the initialization settings
	 */
	public void init(Player player, MissionScriptingHelper helper, XElement settings) {
		this.player = player;
		this.helper = helper;
		this.world = player.world;
		initSettings(settings);
	}
	/**
	 * Initialize the mission settings from the script configuration.
	 * Can be overridden.
	 * @param in the XML node of the mission
	 */
	protected void initSettings(XElement in) {
		// default no-op
	}
	/**
	 * Create a new fleet for the given owner.
	 * @param name the fleet name
	 * @param owner the owner
	 * @param x the placement location
	 * @param y the placement location
	 * @return the fleet object
	 */
	protected Fleet createFleet(String name, Player owner, double x, double y) {
		Fleet f = new Fleet(owner);
		f.name = name;
		f.x = x;
		f.y = y;
		owner.fleets.put(f, FleetKnowledge.FULL);
		return f;
	}
	/**
	 * Find visible fleets considering the current exploration limits as well.
	 * @param toPlayer who should see the fleets
	 * @param shouldSee should the player see the ship?
	 * @param owner the owner of the fleets
	 * @param ds the distance from limits
	 * @return the list of fleets found
	 */
	protected List<Fleet> findVisibleFleets(Player toPlayer, boolean shouldSee, Player owner, int ds) {
		List<Fleet> result = U.newArrayList();
		
		for (Fleet f : owner.fleets.keySet()) {
			if (f.owner == owner 
					&& (toPlayer.fleets.containsKey(f) || !shouldSee)
					&& toPlayer.withinLimits(f.x, f.y, ds)) {
				result.add(f);
			}
		}
		
		return result;
	}
	/**
	 * Returns a planet.
	 * @param id the planet id
	 * @return the planet object
	 */
	protected Planet planet(String id) {
		return world.planets.get(id);
	}
	/**
	 * Returns a player.
	 * @param id the player id
	 * @return the player
	 */
	protected Player player(String id) {
		return world.players.get(id);
	}
	/**
	 * Find a fleet and inventory item having the given tag.
	 * @param tag the tag
	 * @param owner the owner of the fleet
	 * @return the fleet and inventory item pair
	 */
	protected Pair<Fleet, InventoryItem> findTaggedFleet(String tag, Player owner) {
		for (Fleet f : owner.fleets.keySet()) {
			if (f.owner == owner) {
				for (InventoryItem ii : f.inventory) {
					if (tag.equals(ii.tag)) {
						return Pair.of(f, ii);
					}
				}
			}
		}
		return null;
	}
	/**
	 * Retrieve a localized label.
	 * @param id the label id
	 * @return the localized text
	 */
	protected String label(String id) {
		return world.env.labels().get(id);
	}
	/**
	 * Check if the planet is under attack by a fleet.
	 * @param p the planet
	 * @return the fleet
	 */
	protected boolean isUnderAttack(Planet p) {
		for (Fleet f : player.fleets.keySet()) {
			if (f.owner != player && f.targetPlanet() == p && f.mode == FleetMode.ATTACK) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Format a localized label.
	 * @param id the label id
	 * @param params the parameters
	 * @return the localized text
	 */
	protected String format(String id, Object... params) {
		return world.env.labels().format(id, params);
	}
	/**
	 * Returns a fleet.
	 * @param id the fleet id
	 * @return the fleet object
	 */
	protected Fleet fleet(int id) {
		for (Player p : world.players.values()) {
			for (Fleet f : p.fleets.keySet()) {
				if (f.id == id) {
					return f;
				}
			}
		}
		return null;
	}
	/**
	 * Lose the current game with the given forced message.
	 * @param id the video message id
	 */
	protected void loseGameMessage(String id) {
		world.env.stopMusic();
		world.env.pause();
		world.player.messageQueue.clear();
		world.env.forceMessage(id, new Action0() {
			@Override
			public void invoke() {
				world.env.loseGame();
			}
		});
	}
	/**
	 * Lose the current game with the forced message and then full screen movie.
	 * @param message the message
	 * @param movie the movie
	 */
	protected void loseGameMessageAndMovie(String message, final String movie) {
		world.env.stopMusic();
		world.env.pause();
		world.player.messageQueue.clear();
		world.env.forceMessage(message, new Action0() {
			@Override
			public void invoke() {
				world.env.playVideo(movie, new Action0() {
					@Override
					public void invoke() {
						world.env.loseGame();
					}
				});
			}
		});
	}
	/**
	 * Lose the current game with the given forced message.
	 * @param id the video message id
	 */
	protected void loseGameMovie(String id) {
		world.env.stopMusic();
		world.env.pause();
		world.player.messageQueue.clear();
		world.env.playVideo(id, new Action0() {
			@Override
			public void invoke() {
				world.env.loseGame();
			}
		});
	}

	@Override
	public void onResearched(Player player, ResearchType rt) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onProduced(Player player, ResearchType rt) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onDestroyed(Fleet winner, Fleet loser) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onColonized(Planet planet) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onConquered(Planet planet, Player previousOwner) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onPlayerBeaten(Player player) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onDiscovered(Player player, Planet planet) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onDiscovered(Player player, Player other) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onDiscovered(Player player, Fleet fleet) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onLostSight(Player player, Fleet fleet) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onFleetAt(Fleet fleet, double x, double y) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onFleetAt(Fleet fleet, Fleet other) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onStance(Player first, Player second) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onAllyAgainst(Player first, Player second, Player commonEnemy) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onBattleComplete(Player player, BattleInfo battle) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onTime() {
		// default implementation does not react to this event
		
	}

	@Override
	public void onBuildingComplete(Planet planet, Building building) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onRepairComplete(Planet planet, Building building) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onUpgrading(Planet planet, Building building, int newLevel) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onInventoryAdd(Planet planet, InventoryItem item) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onInventoryRemove(Planet planet, InventoryItem item) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onLost(Planet planet) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onLost(Fleet fleet) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onVideoComplete(String video) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onSoundComplete(String audio) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onPlanetInfected(Planet planet) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onPlanetCured(Planet planet) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onMessageSeen(String id) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onNewGame() {
		// default implementation does not react to this event
		
	}

	@Override
	public void onLevelChanged() {
		// default implementation does not react to this event
		
	}

	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		// default implementation does not react to this event
		
	}

	@Override
	public SpacewarScriptResult onSpacewarStep(SpacewarWorld war) {
		// default implementation does not react to this event
		return null;
	}

	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		// default implementation does not react to this event
	}

	@Override
	public void onGroundwarStart(GroundwarWorld war) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onGroundwarStep(GroundwarWorld war) {
		// default implementation does not react to this event
		
	}

	@Override
	public void onGroundwarFinish(GroundwarWorld war) {
		// default implementation does not react to this event
	}
	/**
	 * Check if the given spacewar is a mission-related spacewar.
	 * <ul>
	 * <li>The given mission is active (visible and state == ACTIVE)</li>
	 * <li>The attacker is the player</li>
	 * <li>The target is a fleet</li>
	 * <li>The target appears in the scripted fleet set</li>
	 * </ul>
	 * @param battle the battle settings
	 * @param mission the mission
	 * @return true if it is the related spacewar
	 */
	protected boolean isMissionSpacewar(BattleInfo battle, String mission) {
		if (helper.isActive(mission) 
				&& battle.attacker.owner == player 
				&& battle.targetFleet != null) {
			return helper.scriptedFleets().contains(battle.targetFleet.id);
		}
		return false;
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		// default implementation does not react to this event
		
	}
	@Override
	public void onAutobattleStart(BattleInfo battle) {
		// default implementation does not react to this event
		
	}
	/**
	 * Notify the user about the incoming message.
	 * @param messageId the message id
	 */
	void incomingMessage(String messageId) {
		helper.receive(messageId).visible = true;
		helper.receive(messageId).seen = false;
		
		SoundType snd = world.random(Arrays.asList(SoundType.MESSAGE, SoundType.NEW_MESSAGE_1, SoundType.NEW_MESSAGE_2, SoundType.NEW_MESSAGE_3));
		world.env.computerSound(snd);
	}
	/**
	 * Remove any non-existent scripted fleets.
	 */
	void cleanupScriptedFleets() {
		// cleanup scripted fleets
		for (int i : U.newArrayList(helper.scriptedFleets())) {
			if (fleet(i) == null) {
				helper.scriptedFleets().remove(i);
			}
		}
	}
	/**
	 * Returns the research of the given id.
	 * @param id the research id
	 * @return the research type
	 */
	ResearchType research(String id) {
		return world.researches.get(id);
	}
	@Override
	public void onTalkCompleted() {
		
	}
	/**
	 * Set the slot contents.
	 * @param ii the inventory item
	 * @param slotId the slot
	 * @param technology the technology
	 * @param count the item count
	 */
	protected void setSlot(InventoryItem ii, String slotId, String technology, int count) {
		for (InventorySlot is : ii.slots) {
			if (is.slot.id.equals(slotId) && !is.slot.fixed) {
				is.type = research(technology);
				is.hp = world.getHitpoints(is.type);
				is.count = Math.min(is.slot.max, count);
			}
		}		
	}
	/**
	 * Remove the mission related variables between the given range.
	 * @param idxs the array of mission indexes
	 */
	protected void removeMissions(final int... idxs) {
		Func1<String, Boolean> filter = new Func1<String, Boolean>() {
			@Override
			public Boolean invoke(String value) {
				for (int i = 0; i < idxs.length; i++) {
					if (value.equals("Mission-" + idxs[i])) {
						return true;
					}
					if (value.startsWith("Mission-" + idxs[i] + "-")) {
						return true;
					}
				}
				return false;
			}
		};
		helper.clearMissionTimes(filter);
		helper.clearTimeouts(filter);
		helper.clearObjectives(filter);
	}
	/**
	 * Remove all mission related variables between the given range.
	 * @param start the start index inclusive
	 * @param end the end index inclusive
	 */
	protected void removeMissions(final int start, final int end) {
		Func1<String, Boolean> filter = new Func1<String, Boolean>() {
			@Override
			public Boolean invoke(String value) {
				for (int i = start; i <= end; i++) {
					if (value.equals("Mission-" + i)) {
						return true;
					}
					if (value.startsWith("Mission-" + i + "-")) {
						return true;
					}
				}
				return false;
			}
		};
		helper.clearMissionTimes(filter);
		helper.clearTimeouts(filter);
		helper.clearObjectives(filter);
	}
	/**
	 * Checks if the given mission + "-Timeout" timer is due, clears it and returns true.
	 * @param missionId the mission id
	 * @return trie of timeout
	 */
	boolean checkTimeout(String missionId) {
		if (helper.isTimeout(missionId)) {
			helper.clearTimeout(missionId);
			return true;
		}
		return false;
	}
	/**
	 * Checks if the given mission is due, clears it and returns true.
	 * @param missionId the mission id
	 * @return trie of timeout
	 */
	boolean checkMission(String missionId) {
		String id = missionId;
		if (helper.isMissionTime(id)) {
			helper.clearMissionTime(id);
			return true;
		}
		return false;
	}
	/**
	 * Update the send and receive messages based on the planet statuses.
	 * @param planets the list of planet ids
	 */
	protected void setPlanetMessages(String... planets) {
		boolean anyAttack = false;
		for (String p : planets) {
			Planet planet = planet(p);
			if (planet.quarantineTTL > 0) {
				helper.send(p + "-Check").visible = false;
				helper.send(p + "-Come-Quickly").visible = false;
				helper.receive(p + "-Virus").visible = true;
			} else {
				helper.receive(p + "-Virus").visible = false;
				anyAttack |= isUnderAttack(planet);
			}
		}
		for (String p : planets) {
			Planet planet = planet(p);
			if (planet.quarantineTTL == 0) {
				boolean thisTarget = isUnderAttack(planet);
				if (thisTarget) {
					helper.send(p + "-Check").visible = false;
					helper.send(p + "-Come-Quickly").visible = true;
					helper.send(p + "-Not-Under-Attack").visible = false;
				} else
				if (anyAttack) {
					helper.send(p + "-Check").visible = false;
					helper.send(p + "-Come-Quickly").visible = false;
					helper.send(p + "-Not-Under-Attack").visible = true;
				} else {
					helper.send(p + "-Check").visible = true;
					helper.send(p + "-Come-Quickly").visible = false;
					helper.send(p + "-Not-Under-Attack").visible = false;
				}
			}
		}

	}
	/**
	 * Check if the fleet has a concrete tag.
	 * @param f the fleet
	 * @param tag the expected tag
	 * @return true if fleet has tag
	 */
	protected boolean hasTag(Fleet f, String tag) {
		for (InventoryItem ii : f.inventory) {
			if (tag.equals(ii.tag)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Check if any of the fleets of {@code p} is following the given fleet.
	 * @param target the target fleet
	 * @param p the player
	 * @return the first fleet that is following the target
	 */
	Fleet getFollower(Fleet target, Player p) {
		for (Fleet f2 : p.fleets.keySet()) {
			if (f2.owner == p && f2.targetFleet == target) {
				return f2;
			}
		}
		return null;
	}
	/**
	 * Load the state.
	 * @param xmission the mission XML
	 */
	public void load(XElement xmission) {
		
	}
	/**
	 * Save the state.
	 * @param xmission the mission XML
	 */
	public void save(XElement xmission) {
		
	}
	/** @return Check if this mission is still applicable (e.g., level check). */
	public abstract boolean applicable();
	/** Reset state to default. */
	public void reset() {
		
	}
	/**
	 * Tag all items of the given fleet.
	 * @param f the fleet
	 * @param tag the new tag, null to untag
	 */
	public void tagFleet(Fleet f, String tag) {
		for (InventoryItem ii : f.inventory) {
			ii.tag = tag;
		}
	}
	/**
	 * Start a joint space battle with the given ally and enemy.
	 * @param war the war context
	 * @param allyTag the ally fleet's tag
	 * @param allyPlayer the ally fleets owner
	 * @param enemyTag the enemy fleets tag
	 * @param enemyPlayer the enemy fleets owner
	 */
	void startJointSpaceBattle(SpacewarWorld war, String allyTag, Player allyPlayer, String enemyTag, Player enemyPlayer) {
		BattleInfo battle = war.battle();
		Pair<Fleet, InventoryItem> f1 = findTaggedFleet(allyTag, allyPlayer);
		Pair<Fleet, InventoryItem> f2 = findTaggedFleet(enemyTag, enemyPlayer);

		if (battle.targetFleet == f1.first) {
			// thorin attacked?
			war.includeFleet(f2.first, f2.first.owner);
			battle.targetFleet = f2.first;
		} else {
			// garthog attacked
			war.addStructures(f1.first.inventory, EnumSet.of(
					ResearchSubCategory.SPACESHIPS_BATTLESHIPS,
					ResearchSubCategory.SPACESHIPS_CRUISERS,
					ResearchSubCategory.SPACESHIPS_FIGHTERS));
		}
		// center pirate
		Dimension d = war.space();
		List<SpacewarStructure> structures = war.structures();
		int maxH = 0;
		for (SpacewarStructure s : structures) {
			if (s.item != null && allyTag.equals(s.item.tag)) {
				maxH += s.get().getHeight();
			}
		}
		int dy = (d.height - maxH) / 2;
		for (SpacewarStructure s : structures) {
			if (s.item != null && allyTag.equals(s.item.tag)) {
				s.x = d.width / 2;
				s.y = dy;
				s.angle = 0.0;
				s.owner = f1.first.owner;
				s.guard = true;
				dy += s.get().getHeight();
			}
		}
		battle.allowRetreat = false;
	}
	/**
	 * Start a joint autobattle with an ally and enemy fleet.
	 * @param battle the battle info
	 * @param allyTag the ally fleet's tag
	 * @param allyPlayer the ally fleets owner
	 * @param enemyTag the enemy fleets tag
	 * @param enemyPlayer the enemy fleets owner
	 */
	void startJointAutoSpaceBattle(BattleInfo battle, String allyTag, Player allyPlayer, String enemyTag, Player enemyPlayer) {
		Pair<Fleet, InventoryItem> f1 = findTaggedFleet(allyTag, allyPlayer);
		Pair<Fleet, InventoryItem> f2 = findTaggedFleet(enemyTag, enemyPlayer);
		if (battle.targetFleet == f1.first) {
			battle.targetFleet = f2.first;
		}
		battle.attacker.inventory.addAll(f1.first.inventory);
	}
	/**
	 * Remove the ally from the attacker's inventory.
	 * @param battle the battle info
	 * @param allyTag the ally tag
	 */
	void finishJointAutoSpaceBattle(BattleInfo battle, String allyTag) {
		for (InventoryItem ii : new ArrayList<InventoryItem>(battle.attacker.inventory)) {
			if (allyTag.equals(ii.tag)) {
				battle.attacker.inventory.remove(ii);
			}
		}
	}
	/**
	 * Convenience method to add a timeout value.
	 * @param name the timeout value
	 * @param value the value in milliseconds
	 */
	void addTimeout(String name, int value) {
		helper.setTimeout(name, value);
	}
	/**
	 * Convenience method to add a mission time relative to now.
	 * @param name the mission name
	 * @param hoursRelative the hours relative to now
	 */
	void addMission(String name, int hoursRelative) {
		helper.setMissionTime(name, helper.now() + hoursRelative);
	}
	/**
	 * Add the given fleet as a scripted fleet.
	 * @param f the fleet
	 */
	void addScripted(Fleet f) {
		helper.scriptedFleets().add(f.id);
	}
	/**
	 * Remove the given scripted fleet.
	 * @param f the fleet
	 */
	void removeScripted(Fleet f) {
		helper.scriptedFleets().remove(f.id);
	}
	/**
	 * Remove the given scripted fleet.
	 * @param f the fleet
	 */
	void removeScripted(Pair<Fleet, ?> f) {
		removeScripted(f.first);
	}
	/**
	 * Grant an achievement with the given ID if not already awarded.
	 * @param a the achievement id, e.g., "achievement.i_robot"
	 */
	protected void achievement(String a) {
		if (!world.env.profile().hasAchievement(a)) {
			world.env.achievementQueue().add(a);
			world.env.profile().grantAchievement(a);
		}
	}
	@Override
	public void onDeploySatellite(Planet target, Player player,
			ResearchType satellite) {
		// default implementation does not react to this event
	}
}
