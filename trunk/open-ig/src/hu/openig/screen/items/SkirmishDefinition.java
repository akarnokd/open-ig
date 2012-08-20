/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

/**
 * The skirmish definition.
 * @author akarnokd, 2012.08.20.
 */
public class SkirmishDefinition {
	/** The game from where the galaxy and planet definitions should be taken. */
	public String galaxy;
	/** Randomize surface types. */
	public boolean galaxyRandomSurface;
	/** Randomize layout. */
	public boolean galaxyRandomLayout;
	/** Custom planet count. */
	public boolean galaxyCustomPlanets;
	/** The number of custom planets. */
	public int galaxyPlanetCount;
	/** The race definitions to use. */
	public String race;
	/** The initial money. */
	public int initialMoney;
	/** The initial planets per player. */
	public int initialPlanets;
	/** Place colony hubs on the planets? */
	public boolean placeColonyHubs;
	/** Initial population on planets. */
	public int initialPopulation;
	/** Grant colony ship technology. */
	public boolean grantColonyShip;
	/** Initial colony ship counts. */
	public int initialColonyShips;
	/** Grant orbital factory technology. */
	public boolean grantOrbitalFactory;
	/** Initial orbital factory counts. */ 
	public int initialOrbitalFactories;
}
