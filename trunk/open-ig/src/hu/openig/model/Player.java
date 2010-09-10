/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author karnok, 2009.10.25.
 * @version $Revision 1.0$
 */
public class Player {
	/** The player's name. */
	public String name;
	/** The coloring used for this player. */
	public int color;
	/** The fleet icon. */
	public BufferedImage fleetIcon;
	/** The race of the player. */
	public Race race;
	/** The in-progress production list. */
	public final List<Production> production = new ArrayList<Production>();
	/** The in-progress research. */
	public final Map<ResearchType, Research> research = new HashMap<ResearchType, Research>();
	/** The completed research. */
	public final Set<ResearchType> availableResearch = new HashSet<ResearchType>();
	/** The discovered players. */
	public final List<Player> discoveredPlayers = new ArrayList<Player>();
	/** The fleets owned. */
	public final Map<Fleet, FleetKnowledge> fleets = new HashMap<Fleet, FleetKnowledge>();
	/** The planets owned. */
	public final Map<Planet, PlanetKnowledge> planets = new HashMap<Planet, PlanetKnowledge>();
	/** The list of aliens discovered this far. */
	public final List<String> discoveredAliens = new ArrayList<String>();
	/** The actual planet. */
	public Planet currentPlanet;
	/** The actual fleet. */
	public Fleet currentFleet;
	/** The actual research. */
	public ResearchType currentResearch;
	/** The actual building. */
	public BuildingType currentBuilding;
}
