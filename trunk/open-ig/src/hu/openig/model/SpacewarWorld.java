/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;

import java.awt.Dimension;
import java.awt.Point;
import java.util.EnumSet;
import java.util.List;

/**
 * Interface to tell things about the spacewar state to AI players and the Scripting.
 * @author akarnokd, 2011.12.13.
 */
public interface SpacewarWorld {
	/**
	 * @return the battle object
	 */
	BattleInfo battle();
	/** @return The list of all structures. */
	List<SpacewarStructure> structures();
	/**
	 * Returns the list of structures owned by the given player.
	 * @param owner the player
	 * @return the list of structures
	 */
	List<SpacewarStructure> structures(Player owner);
	/**
	 * Returns the list of enemies to the structure.
	 * @param s the structure 
	 * @return the list of the enemies of the structure. 
	 */
	List<SpacewarStructure> enemiesOf(SpacewarStructure s);
	/**
	 * Returns the list of enemies to the player.
	 * @param p the player 
	 * @return the list of the enemies of the player. 
	 */
	List<SpacewarStructure> enemiesOf(Player p);
	/** 
	 * Returns the list of enemies in range of the structure.
	 * @param s the structure
	 * @return the list of the enemies in range of the structure. 
	 */
	List<SpacewarStructure> enemiesInRange(SpacewarStructure s);
	/**
	 * Set the structure to move outside of the screen as an indication of fleeing.
	 * @param s the target structure
	 */
	void flee(SpacewarStructure s);
	/**
	 * The player's facing in the battle.
	 * @return -1 if a forward move would decrease the X coordinate, +1 if a forward move would
	 * increase the X coordinate
	 */
	int facing();
	/**
	 * Move the specified unit to the given location.
	 * @param s the ship
	 * @param x the target location
	 * @param y the target location
	 */
	void move(SpacewarStructure s, double x, double y);
	/**
	 * Command the specified unit to attack the other unit.
	 * @param s the ship
	 * @param target the target structure
	 * @param mode the attack mode, e.g., beam, rocket, etc.
	 */
	void attack(SpacewarStructure s, SpacewarStructure target, BattleProjectile.Mode mode);
	/**
	 * Stop the given unit.
	 * <p>Disables guard mode as well.</p>
	 * @param s the unit
	 */
	void stop(SpacewarStructure s);
	/**
	 * Switch to guard mode.
	 * @param s the unit
	 */
	void guard(SpacewarStructure s);
	/**
	 * The size of the battlespace.
	 * @return the dimension
	 */
	Dimension space();
	/**
	 * Include the ships of the given fleet and re-place the ships of the same owner.
	 * @param f the fleet to include
	 * @param side which side to include
	 */
	void includeFleet(Fleet f, Player side);
	/**
	 * Add the spacewar ships from the given inventory list and category filters to the spacewar.
	 * @param inventory the inventory map with the parent for each item
	 * @param categories the categories to use
	 */
	void addStructures(HasInventory inventory,
			EnumSet<ResearchSubCategory> categories);
	/** @return the landing lace location on the battlemap or null if no such place. */
	Point landingPlace();
	/** @return the current game difficulty. */
	Difficulty difficulty();
	/**
	 * Check if the given object is fleeing.
	 * @param s the structure
	 * @return true if fleeing
	 */
	boolean isFleeing(SpacewarStructure s);
}
