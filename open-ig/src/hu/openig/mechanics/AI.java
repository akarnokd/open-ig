/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import java.util.List;

import hu.openig.model.AIManager;
import hu.openig.model.AIWorld;
import hu.openig.model.BattleInfo;
import hu.openig.model.DiplomaticInteraction;
import hu.openig.model.Player;
import hu.openig.model.ResponseMode;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.World;
import hu.openig.model.SpacewarStructure.StructureType;
import hu.openig.utils.XElement;

/**
 * The general artificial intelligence to run generic starmap-planet-production-research operations.
 * @author akarnokd, 2011.12.08.
 */
public class AI implements AIManager {
	/** The world. */
	AIWorld world;
	/** 
	 * AI players won't start colonization until the player has actually researched its colony ship.
	 * To avoid the headstart problem in skirmish mode. 
	 */
	boolean playerColonyShipAvailable;
	@Override
	public void prepare(World w, Player p) {
		world = new AIWorld();
		world.assign(w, p);
		playerColonyShipAvailable = w.player.colonyShipAvailable;
	}
	
	@Override
	public void manage() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
		world = null;
	}
	
	@Override
	public ResponseMode diplomacy(World world, Player we, Player other,
			DiplomaticInteraction offer) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SpacewarAction spaceBattle(SpacewarWorld world, Player player,
			List<SpacewarStructure> idles) {
		// FIXME make more sophisticated
		for (SpacewarStructure ship : idles) {
			defaultAttackBehavior(world, ship);
		}
		return SpacewarAction.CONTINUE;
	}

	/**
	 * Orders the given structure to attack a random enemy.
	 * @param world the world
	 * @param ship the ship
	 */
	public static void defaultAttackBehavior(SpacewarWorld world,
			SpacewarStructure ship) {
		if (ship.type == StructureType.STATION 
				|| ship.type == StructureType.PROJECTOR) {
			ship.guard = true;
		} else
		if (ship.type == StructureType.SHIP) {
			List<SpacewarStructure> es = world.enemiesInRange(ship);
			if (es.size() > 0) {
				ship.attack = world.random(es);
			} else {
				es = world.enemiesOf(ship);
				if (es.size() > 0) {
					ship.attack = world.random(es);
				}
			}
		}
	}
	/**
	 * Orders the given structure to attack a random enemy.
	 * @param world the world
	 * 
	 * @param ship the ship
	 */
	public static void costAttackBehavior(SpacewarWorld world, 
			SpacewarStructure ship) {
		if (ship.type == StructureType.STATION 
				|| ship.type == StructureType.PROJECTOR) {
			ship.guard = true;
		} else
		if (ship.type == StructureType.SHIP) {
			List<SpacewarStructure> es = world.enemiesInRange(ship);
			if (es.size() > 0) {
				ship.attack = world.random(es);
			} else {
				es = world.enemiesOf(ship);
				if (es.size() > 0) {
					ship.attack = world.random(es);
				}
			}
		}
	}
	@Override
	public void groundBattle(World world, Player we, BattleInfo battle) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void load(XElement in, World world, Player player) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void save(XElement out, World world, Player player) {
		// TODO Auto-generated method stub
		
	}
}
