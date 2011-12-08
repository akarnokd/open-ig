/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.XElement;

/**
 * Base interface to manage AI related operations.
 * @author akarnokd, 2011.12.08.
 */
public interface AIManager {
	/**
	 * Manage the world.
	 * @param world the AI world.
	 */
	void manage(AIWorld world);
	/**
	 * Calculates the diplomatic response to an offer coming from another player.
	 * @param world the world
	 * @param we the target player
	 * @param other the other player
	 * @param offer the kind of offer/request
	 * @return the response
	 */
	ResponseMode diplomacy(World world, Player we, Player other, DiplomaticInteraction offer);
	/**
	 * Handle some aspects of a space battle.
	 * TODO output?
	 * @param world the world
	 * @param we the target player
	 * @param battle the battle information
	 */
	void spaceBattle(World world, Player we, BattleInfo battle);
	/**
	 * Handle some aspects of a ground battle.
	 * TODO output?
	 * @param world the world
	 * @param we the target player
	 * @param battle the battle information
	 */
	void groundBattle(World world, Player we, BattleInfo battle);
	/**
	 * Save the state of this AI manager from a save file.
	 * @param out the output XElement
	 */
	void save(XElement out);
	/**
	 * Load the state of this AI manager from a save file.
	 * @param in the input XElement
	 */
	void load(XElement in);
}
