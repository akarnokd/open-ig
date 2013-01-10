/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.SimulationSpeed;
import hu.openig.utils.U;

import java.util.Set;

/**
 * Defines the battle parties.
 * @author akarnokd, 2011.08.16.
 */
public class BattleInfo {
	/** The attacker fleet. */
	public Fleet attacker;
	/** The targeted fleet if non null. */
	public Fleet targetFleet;
	/** The targeted planet if non null. */
	public Planet targetPlanet;
	/** The original target planet owner. */
	public Player originalTargetPlanetOwner;
	/** The potential helper fleet if a planet is attacked. */
	public Fleet helperFleet;
	/** The potential helper planet if a fleet is attacked. */
	public Planet helperPlanet;
	/** Count the attacker's losses. */
	public int attackerLosses;
	/** Count the defender's losses. */
	public int defenderLosses;
	/** The number of lost ground units: attacker. */
	public int attackerGroundLosses;
	/** The number of lost ground units: defender. */
	public int defenderGroundLosses;
	/** Ground unit count of the attacker. */
	public int attackerGroundUnits;
	/** Ground unit count of the defender. */
	public int defenderGroundUnits;
	/** The winner of the spacewar. */
	public Player spacewarWinner;
	/** The winner of the groundwar. */
	public Player groundwarWinner;
	/** The loser retreated. */
	public boolean retreated;
	/** Indicate if the enemy (non main player) fled. */
	public boolean enemyFlee;
	/** Invert the layout. */
	public boolean invert;
	/** Allow retreat from this battle? */
	public boolean allowRetreat = true;
	/** The number of fortifications destroyed. */
	public int defenderFortificationLosses;
	/** The structure losses. */
	public final Set<SpacewarStructure> spaceLosses = U.newHashSet();
	/** The ground unit losses. */
	public final Set<GroundwarUnit> groundLosses = U.newHashSet();
	/** The original simulation speed before the battle.*/
	public SimulationSpeed originalSpeed = SimulationSpeed.NORMAL;
	/** The set of the allies of the attacker which can be controlled. */
	public final Set<Player> attackerAllies = U.newHashSet();
	/** The optional finish image resource id. */
	public String rewardImage;
	/** The optional reward display text. */
	public String rewardText;
	/** The optional message display text. */
	public String messageText;
	/** Infect the target planet at the conclusion if non-null. */
	public Planet infectPlanet;
	/** The chat option set by scripts. */
	public String chat;
	/** Show the landing marker? */
	public boolean showLanding;
	/** @return the helper planet if any. */
	public Planet getPlanet() {
		return targetPlanet != null ? targetPlanet : helperPlanet;
	}
	/** @return the helper fleet if any. */
	public Fleet getFleet() {
		return targetFleet != null ? targetFleet : helperFleet;
	}
	/**
	 * Convenience method to check if the space war is completed.
	 * @return true if completed
	 */
	public boolean isSpacewarComplete() {
		return spacewarWinner != null;
	}
	/**
	 * Convenience method to check if the ground war is completed.
	 * @return true if completed
	 */
	public boolean isGroundwarComplete() {
		return groundwarWinner != null;
	}
	/**
	 * Returns the enemy of the given player in this battle.
	 * @param p the player
	 * @return the enemy of the player
	 */
	public Player enemy(Player p) {
		if (isAlly(p, attacker.owner)) {
			if (targetPlanet != null) {
				return targetPlanet.owner;
			}
			return targetFleet.owner;
		}
		return attacker.owner;
	}
	
	/**
	 * Check if space battle will happen.
	 * @return true if space battle will happen
	 */
	public boolean spaceBattleNeeded() {
		if (targetFleet != null || helperFleet != null) {
			return true;
		}
		Planet p = targetPlanet;
		if (p == null) {
			p = helperPlanet;
		}
		if (p != null) {
			for (InventoryItem ii : p.inventory) {
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
						|| ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					return true;
				}
			}
			for (Building b : p.surface.buildings) {
				if (b.isOperational()) {
					if (b.type.kind.equals("Gun") || b.type.kind.equals("Shield")) {
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * Find helper fleet or planet for the battle.
	 */
	public void findHelpers() {
		World world = attacker.owner.world;
		final double minDistance = world.params().nearbyDistance();
		if (targetFleet != null) {
			// locate the nearest planet
			double dmin = Double.MAX_VALUE;
			Planet pmin = null;
			for (Planet p : world.planets.values()) {
				if (p.owner == attacker.owner || p.owner == targetFleet.owner) {
					double d = World.dist(targetFleet.x, targetFleet.y, p.x, p.y);
					if (d < dmin && d <= minDistance * minDistance) {
						dmin = d;
						pmin = p;
					}
				}
				
			}
			helperPlanet = pmin;
		} else 
		if (targetPlanet != null && targetPlanet.owner != null) {
			// locate the nearest fleet with the same owner
			double dmin = Double.MAX_VALUE;
			Fleet fmin = null;
			for (Fleet f : targetPlanet.owner.fleets.keySet()) {
				if (f.owner == targetPlanet.owner) {
					double d = World.dist(f.x, f.y, targetPlanet.x, targetPlanet.y);
					if (d < dmin && d <= minDistance) {
						dmin = d;
						fmin = f;
					}
				}
			}
			helperFleet = fmin;
		} else {
			throw new AssertionError("No target in battle settings.");
		}
	}
	/**
	 * Check if the given structure is the ally of the player.
	 * @param s the structure to test
	 * @param p the player to test
	 * @return true if ally
	 */
	public boolean isAlly(SpacewarStructure s, Player p) {
		return isAlly(s.owner, p);
	}
	/**
	 * Check if the given structure is the ally of the player.
	 * @param s the structure to test
	 * @param p the player to test
	 * @return true if ally
	 */
	public boolean isAlly(Player s, Player p) {
		if (s == p) {
			return true;
		}
		if (p == attacker.owner) {
			return attackerAllies.contains(s);
		}
		// P is the defender
		if (s == attacker.owner) {
			return attackerAllies.contains(p);
		}
		return false;
	}
	/**
	 * Add one to the space battle counters.
	 */
	public void incrementSpaceBattles() {
		attacker.owner.statistics.spaceBattles++;
		if (targetFleet != null) {
			targetFleet.owner.statistics.spaceBattles++;
		}
		if (targetPlanet != null) {
			targetPlanet.owner.statistics.spaceBattles++;
		}
		for (Player p0 : attackerAllies) {
			p0.statistics.spaceBattles++;
		}
	}
	/**
	 * Add one to the ground war counters.
	 */
	public void incrementGroundBattles() {
		attacker.owner.statistics.groundBattles++;
		if (targetPlanet != null) {
			targetPlanet.owner.statistics.groundBattles++;
		}
	}
	/**
	 * Increment the space winner statistics.
	 */
	public void incrementSpaceWin() {
		spacewarWinner.statistics.spaceWins++;
		if (attacker.owner == spacewarWinner) {
			for (Player p0 : attackerAllies) {
				p0.statistics.spaceWins++;
			}
			if (targetFleet != null) {
				targetFleet.owner.statistics.spaceLoses++;
			}
			if (targetPlanet != null) {
				targetPlanet.owner.statistics.spaceLoses++;
			}
		} else {
			attacker.owner.statistics.spaceLoses++;
			if (retreated) {
				attacker.owner.statistics.spaceRetreats++;
			}
			for (Player p0 : attackerAllies) {
				p0.statistics.spaceLoses++;
				if (retreated) {
					p0.statistics.spaceRetreats++;
				}
			}
		}
	}
	/** Increment the ground war winner statistics. */
	public void incrementGroundWin() {
		groundwarWinner.statistics.groundWins++;
		if (groundwarWinner == targetPlanet.owner) {
			attacker.owner.statistics.groundLoses++;
		} else {
			targetPlanet.owner.statistics.groundLoses++;
		}
	}
}
