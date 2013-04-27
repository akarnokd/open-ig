/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer.model;

import hu.openig.model.AutoBuild;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.TaxLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * A planet's status.
 * @author akarnokd, 2013.04.27.
 */
public class PlanetStatus {
	/** The planet's identifier. */
	public String id;
	/** 
	 * Knowledge about the planet, if certain knowledge is not
	 * available, the other fields are set to null or to zero.
	 */
	public PlanetKnowledge knowledge;
	/** The owner. */
	public String owner;
	/** The inhabitant race. */
	public String race;
	/** The current population. */
	public int population;
	/** The population change since the last day. */
	public int lastPopulation;
	/** How long the quarantine should stay in 10s ingame minutes? */
	public int quarantineTTL;
	/** The taxation level. */
	public TaxLevel tax = TaxLevel.MODERATE;
	/** The morale percent in hundreds. */
	public double morale = 50;
	/** The last day's morale percent in hundreds. */
	public double lastMorale = 50;
	/** The auto build mode. */
	public AutoBuild autoBuild;
	/** The last day's tax income. */
	public int taxIncome;
	/** The last day's trade income. */
	public int tradeIncome;
	/** The planet's inventory. */
	public final List<InventoryItemStatus> inventory = new ArrayList<InventoryItemStatus>();
	/** The countdown for an earthquake lasting 10s of ingame minutes. */
	public int earthQuakeTTL;
	/** The remaining time for a weather event. */
	public int weatherTTL;
	/** The building statuses. */
	public final List<BuildingStatus> buildings = new ArrayList<BuildingStatus>();
}
