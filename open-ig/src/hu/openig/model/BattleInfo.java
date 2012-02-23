/*
 * Copyright 2008-2012, David Karnok 
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
	/** Indicate if the defender fleed. */
	public boolean defenderFlee;
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
}
