/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.XElement;

import java.util.List;

/**
 * API to interact with a campaign scripting.
 * @author akarnokd, 2012.01.12.
 */
public interface GameScripting extends GameScriptingEvents {
	/**
	 * Returns the list of video messages to send.
	 * @return the list of video messages to send.
	 */
	List<VideoMessage> getSendMessages();
	/**
	 * Returns the list of current objectives.
	 * @return List of current objectives.
	 */
	List<Objective> currentObjectives();
	/**
	 * Initialize the scripting for the world and configuration.
	 * @param player the main player
	 * @param in the script configuration
	 */
	void init(Player player, XElement in);
	/**
	 * Close the scripting and release resources.
	 */
	void done();
	/**
	 * Load the scripting state from XML.
	 * @param in the input XML
	 */
	void load(XElement in);
	/**
	 * Save the scripting state into an XML.
	 * @param out the output XML
	 */
	void save(XElement out);
	/**
	 * Test if the user should be able to control a particular own fleet.
	 * <p>Used by escort-like missions.</p>
	 * @param f the fleet
	 * @return true if may control
	 */
	boolean mayControlFleet(Fleet f);
	/**
	 * Returns true if the game can be auto saved.
	 * @return true if can auto saved
	 */
	boolean mayAutoSave();
	/** Activate debugging behavior. */
	void debug();
	/**
	 * Does the scripting allow the attacks of the given player?
	 * @param player the player to test
	 * @return true if attack is allowed
	 */
	boolean mayPlayerAttack(Player player);
	/**
	 * Script modifier to prevent the AI from building additional
	 * space and ground defenses.
	 * @param player the target player
	 * @return true if the AI is allowed to build defenses.
	 */
	boolean mayPlayerImproveDefenses(Player player);
	/**
	 * Modify the population growth
	 * numbers for the given player and planet.
	 * @param planet the target planet 
	 * @param simulatorValue the new population value as computed by the simulator.
	 * The current value is still in the planet record
	 * @return the modified population delta.
	 */
	double playerPopulationGrowthOverride(Planet planet, double simulatorValue);
	/**
	 * Modify the tax income for the given planet.
	 * @param planet the target planet 
	 * @param simulatorValue the tax income as computed by the simulator
	 * @return the modified tax income
	 */
	double playerTaxIncomeOverride(Planet planet, double simulatorValue);
	/**
	 * Modify the fleet's speed.
	 * @param fleet the target fleet
	 * @param speed the default computed speed
	 * @return the overridden speed
	 */
	int fleetSpeedOverride(Fleet fleet, int speed);
}
