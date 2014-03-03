/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.MessageArray;
import hu.openig.net.MessageObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A planet's status.
 * @author akarnokd, 2013.04.27.
 */
public class PlanetStatus implements MessageObjectIO, MessageArrayItemFactory<PlanetStatus> {
	/** The array name. */
	public static final String ARRAY_NAME = "PLANET_STATUSES";
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
	public double population;
	/** The population change since the last day. */
	public double lastPopulation;
	/** How long the quarantine should stay in 10s ingame minutes? */
	public int quarantineTTL;
	/** The taxation level. */
	public TaxLevel tax = TaxLevel.MODERATE;
	/** The morale percent in hundreds. */
	public double morale = 50;
	/** The last day's morale percent in hundreds. */
	public double lastMorale = 50;
	/** The auto build mode. */
	public AutoBuild autoBuild = AutoBuild.OFF;
	/** The last day's tax income. */
	public double taxIncome;
	/** The last day's trade income. */
	public double tradeIncome;
	/** The countdown for an earthquake lasting 10s of ingame minutes. */
	public int earthquakeTTL;
	/** The remaining time for a weather event. */
	public int weatherTTL;
	/** The planet's inventory. */
	public final List<InventoryItemStatus> inventory = new ArrayList<>();
	/** The building statuses. */
	public final List<BuildingStatus> buildings = new ArrayList<>();
	/** The inventory time to live values. */
	public final Map<Integer, Integer> timeToLive = new HashMap<>();
	@Override
	public void fromMessage(MessageObject mo) {
		id = mo.getString("id");
		knowledge = mo.getEnum("knowledge", PlanetKnowledge.values());
		owner = mo.getStringObject("owner");
		race = mo.getStringObject("race");
		population = mo.getInt("population");
		lastPopulation = mo.getInt("lastPopulation");
		quarantineTTL = mo.getInt("quarantineTTL");
		tax = mo.getEnum("tax", TaxLevel.values());
		morale = mo.getDouble("morale");
		lastMorale = mo.getDouble("lastMorale");
		autoBuild = mo.getEnum("autoBuild", AutoBuild.values());
		taxIncome = mo.getInt("taxIncome");
		tradeIncome = mo.getInt("tradeIncome");
		earthquakeTTL = mo.getInt("earthquakeTTL");
		weatherTTL = mo.getInt("weatherTTL");
		for (MessageObject mi : mo.getArray("inventory").objects()) {
			InventoryItemStatus iis = new InventoryItemStatus();
			iis.fromMessage(mi);
			inventory.add(iis);
		}
		for (MessageObject mb : mo.getArray("buildings").objects()) {
			BuildingStatus bs = new BuildingStatus();
			bs.fromMessage(mb);
			buildings.add(bs);
		}
		for (MessageObject ttl : mo.getArray("timeToLive").objects()) {
			timeToLive.put(ttl.getInt("id"), ttl.getInt("value"));
		}
	}
	@Override
	public MessageObject toMessage() {
		MessageObject result = new MessageObject(objectName());
		
		result.set("id", id)
		.set("knowledge", knowledge)
		.set("owner", owner)
		.set("race", race)
		.set("population", population)
		.set("lastPopulation", lastPopulation)
		.set("quarantineTTL", quarantineTTL)
		.set("tax", tax)
		.set("morale", morale)
		.set("lastMorale", lastMorale)
		.set("autoBuild", autoBuild)
		.set("taxIncome", taxIncome)
		.set("tradeIncome", tradeIncome)
		.set("earthquakeTTL", earthquakeTTL)
		.set("weatherTTL", weatherTTL)
		;
		
		MessageArray mi = new MessageArray(null);
		result.set("inventory", mi);
		
		for (InventoryItemStatus iis : inventory) {
			mi.add(iis.toMessage());
		}
		
		MessageArray mb = new MessageArray(null);
		result.set("buildings", mb);
		
		for (BuildingStatus bs : buildings) {
			mb.add(bs.toMessage());
		}
		
		MessageArray mt = new MessageArray(null);
		result.set("timeToLive", mt);
		
		for (Map.Entry<Integer, Integer> e : timeToLive.entrySet()) {
			MessageObject mto = new MessageObject("TTL");
			mto.set("id", e.getKey());
			mto.set("value", e.getValue());
			mt.add(mto);
		}
		
		return result;
	}
	@Override
	public PlanetStatus invoke() {
		return new PlanetStatus();
	}
	@Override
	public String arrayName() {
		return ARRAY_NAME;
	}
	@Override
	public String objectName() {
		return "PLANET_STATUS";
	}
	/**
	 * Convert the list of fleet statuses into a message array.
	 * @param list the list
	 * @return the message array
	 */
	public static MessageArray toArray(Iterable<? extends PlanetStatus> list) {
		MessageArray ma = new MessageArray(ARRAY_NAME);
		for (PlanetStatus fs : list) {
			ma.add(fs.toMessage());
		}
		return ma;
	}
}
