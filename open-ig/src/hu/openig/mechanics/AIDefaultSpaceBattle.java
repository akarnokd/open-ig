/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Pair;
import hu.openig.model.AIManager;
import hu.openig.model.AISpaceBattleManager;
import hu.openig.model.BattleInfo;
import hu.openig.model.Player;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWeaponPort;
import hu.openig.model.SpacewarWorld;

import java.util.List;

/**
 * AI space battle manager implementation.
 * @author akarnokd, 2014.04.01.
 */
public class AIDefaultSpaceBattle implements AISpaceBattleManager {
	/** The current player. */
	protected final Player p;
	/** The current world. */
	protected final SpacewarWorld world;
	/** The AI record. */
	protected final AIManager ai;
	/** The initial defense value. */
	protected double initialDefense;
	/** Ratio of average current defense and initial defense where the ships should flee. */
	protected static final double FLEE_PERCENT = 0.1;
	/**
	 * Constructor with environmental info.
	 * @param p the player
	 * @param ai the AI
	 * @param world the world
	 */
	public AIDefaultSpaceBattle(Player p, AIManager ai, SpacewarWorld world) {
		this.p = p;
		this.ai = ai;
		this.world = world;
	}
	@Override
	public void spaceBattleInit() {
		p.world.establishWar(p, world.battle().enemy(p));
		
		List<SpacewarStructure> own = world.structures(p);

		initialDefense = 0;
		if (!own.isEmpty()) {
			for (SpacewarStructure s : own) {
				initialDefense += s.hp + Math.max(0, s.shield);
			}
		}
	}
	/**
	 * Calculate the percentage of the structure healths.
	 * @param structures the structures
	 * @return the health between 0..1
	 */
	public static Pair<Double, Double> health(List<SpacewarStructure> structures) {
		double hpTotal = 0.0;
		double hpActual = 0.0;
		for (SpacewarStructure s : structures) {
			hpTotal += s.hpMax + Math.max(s.shieldMax, 0);
			hpActual += s.hp + Math.max(0, s.shield);
		}
		return Pair.of(hpActual, hpTotal);
	}	/**
	 * Check if the current set of structures no longer has any weapons.
	 * @param structs the structure list
	 * @return true if the set of structures has no weapons
	 */
	public boolean haveAnyWeapons(List<SpacewarStructure> structs) {
		for (SpacewarStructure s : structs) {
			for (SpacewarWeaponPort wp : s.ports) {
				if (wp.count > 0) {
					return true;
				}
			}
		}
		return false;
	}
	@Override
	public SpacewarAction spaceBattle(List<SpacewarStructure> idles) {
		List<SpacewarStructure> own = world.structures(p);
		
		if (haveAnyWeapons(own)) {
			if (!checkFlee(own)) {
				AI.defaultAttackBehavior(world, idles, p);				
				return SpacewarAction.CONTINUE;
			}
		}
		if (world.battle().canFlee(p)) {
			BattleInfo battle = world.battle();
			if (battle.attacker.owner == p) {
				for (SpacewarStructure s : own) {
					world.flee(s);
				}
			}
			battle.enemyFlee = p != p.world.player;
			return SpacewarAction.FLEE;
		}
		return SpacewarAction.CONTINUE;
	}
	
	@Override
	public void spaceBattleDone() {
		ai.onAutobattleFinish(world.battle());
	}
	/**
	 * Check if the fleet should flee due to heavy losses.
	 * @param list the current fleet list
	 * @return true if flee should happen
	 */
	public boolean checkFlee(List<SpacewarStructure> list) {
		if (initialDefense > 0) {
			Pair<Double, Double> h = health(list);
			
			double unitHealth = h.first / initialDefense;
			
			return unitHealth < FLEE_PERCENT;
		}
		
		return false;
	}
}
