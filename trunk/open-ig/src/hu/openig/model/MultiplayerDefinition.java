/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;
import hu.openig.core.SimulationSpeed;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageObject;
import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiplayer definition record. 
 * @author akarnokd, 2013.04.25.
 */
public class MultiplayerDefinition extends CustomGameDefinition implements MessageObjectIO {
	/** Allow server quicksaving. */
	public boolean allowQuickSave;
	/** Allow server autosave. */
	public boolean allowAutoSave;
	/** Allow server pause. */
	public boolean allowPause;
	/** Allow cheats. */
	public boolean allowCheat;
	/** Base simulation speed. */
	public SimulationSpeed speed;
	/** Base timestep. */
	public int timestep;
	/** The list of multiplayer users. */
	public final List<MultiplayerUser> players = new ArrayList<>();
	/**
	 * Save the multiplayer definition.
	 * @param xout the output
	 */
	public void save(XElement xout) {
		xout.saveFields(this);
		XElement xplayers = xout.add("players");
		for (MultiplayerUser p : players) {
			XElement xplayer = xplayers.add("player");
			xplayer.saveFields(p);
			XElement xtraits = xplayer.add("traits");
			for (String t : p.traits) {
				xtraits.add("trait").set("id", t);
			}
		}
	}
	/**
	 * Load the mutliplayer definition.
	 * @param xin the input
	 */
	public void load(XElement xin) {
		players.clear();
		xin.loadFields(this);
		for (XElement xplayers : xin.childrenWithName("players")) {
			for (XElement xplayer : xplayers.childrenWithName("player")) {
				MultiplayerUser sp = new MultiplayerUser();
				xplayer.loadFields(sp);
				for (XElement xtraits : xplayer.childrenWithName("traits")) {
					for (XElement xtrait : xtraits.childrenWithName("trait")) {
						sp.traits.add(xtrait.get("id"));
					}
				}
				players.add(sp);
			}
		}
	}
	@Override
	public void fromMessage(MessageObject mo) {
		galaxy = mo.getString("galaxy");
		galaxyRandomSurface = mo.getBoolean("galaxyRandomSurface");
		galaxyRandomLayout = mo.getBoolean("galaxyRandomLayout");
		galaxyCustomPlanets = mo.getBoolean("galaxyCustomPlanets");
		galaxyPlanetCount = mo.getInt("galaxyPlanetCount");
		race = mo.getString("race");
		tech = mo.getString("tech");
		startLevel = mo.getInt("startLevel");
		maxLevel = mo.getInt("maxLevel");
		initialMoney = mo.getInt("initialMoney");
		initialPlanets = mo.getInt("initialPlanets");
		placeColonyHubs = mo.getBoolean("placeColonyHubs");
		initialPopulation = mo.getInt("initialPopulation");
		grantColonyShip = mo.getBoolean("grantColonyShip");
		initialColonyShips = mo.getInt("initialColonyShips");
		grantOrbitalFactory = mo.getBoolean("grantOrbitalFactory");
		initialOrbitalFactories = mo.getInt("initialOrbitalFactories");
		initialDiplomaticRelation = mo.getEnum("initialDiplomaticRelation", SkirmishDiplomaticRelation.values());
		initialDifficulty = mo.getEnum("initialDifficulty", Difficulty.values());
		victoryConquest = mo.getBoolean("victoryConquest");
		victoryOccupation = mo.getBoolean("victoryOccupation");
		victoryOccupationPercent = mo.getInt("victoryOccupationPercent");
		victoryOccupationTime = mo.getInt("victoryOccupationTime");
		victoryEconomic = mo.getBoolean("victoryEconomic");
		victoryEconomicMoney = mo.getInt("victoryEconomicMoney");
		victoryTechnology = mo.getBoolean("victoryTechnology");
		victorySocial = mo.getBoolean("victorySocial");
		victorySocialMorale = mo.getInt("victorySocialMorale");
		victorySocialPlanets = mo.getInt("victorySocialPlanets");

		allowQuickSave = mo.getBoolean("allowQuickSave");
		allowAutoSave = mo.getBoolean("allowAutoSave");
		allowPause = mo.getBoolean("allowPause");
		allowCheat = mo.getBoolean("allowCheat");
		speed = mo.getEnum("speed", SimulationSpeed.values());
		timestep = mo.getInt("timestep");
		
		players.clear();
		for (MessageObject o : mo.getArray("players").objects()) {
			MultiplayerUser mu = new MultiplayerUser();
			mu.fromMessage(o);
			players.add(mu);
		}
	}
	@Override
	public MessageObject toMessage() {
		MessageObject r = new MessageObject(objectName());
		
		r.set("galaxy", galaxy);
		r.set("galaxyRandomSurface", galaxyRandomSurface);
		r.set("galaxyRandomLayout", galaxyRandomLayout);
		r.set("galaxyCustomPlanets", galaxyCustomPlanets);
		r.set("galaxyPlanetCount", galaxyPlanetCount);
		r.set("race", race);
		r.set("tech", tech);
		r.set("startLevel", startLevel);
		r.set("maxLevel", maxLevel);
		r.set("initialMoney", initialMoney);
		r.set("initialPlanets", initialPlanets);
		r.set("placeColonyHubs", placeColonyHubs);
		r.set("initialPopulation", initialPopulation);
		r.set("grantColonyShip", grantColonyShip);
		r.set("initialColonyShips", initialColonyShips);
		r.set("grantOrbitalFactory", grantOrbitalFactory);
		r.set("initialOrbitalFactories", initialOrbitalFactories);
		r.set("initialDiplomaticRelation", initialDiplomaticRelation);
		r.set("initialDifficulty", initialDifficulty);
		r.set("victoryConquest", victoryConquest);
		r.set("victoryOccupation", victoryOccupation);
		r.set("victoryOccupationPercent", victoryOccupationPercent);
		r.set("victoryOccupationTime", victoryOccupationTime);
		r.set("victoryEconomic", victoryEconomic);
		r.set("victoryEconomicMoney", victoryEconomicMoney);
		r.set("victoryTechnology", victoryTechnology);
		r.set("victorySocial", victorySocial);
		r.set("victorySocialMorale", victorySocialMorale);
		r.set("victorySocialPlanets", victorySocialPlanets);

		r.set("allowQuickSave", allowQuickSave);
		r.set("allowAutoSave", allowAutoSave);
		r.set("allowPause", allowPause);
		r.set("allowCheat", allowCheat);
		r.set("speed", speed);
		r.set("timestep", timestep);

		MessageArray pa = new MessageArray(null);
		r.set("players", pa);
		for (MultiplayerUser mu : players) {
			pa.add(mu.toMessage());
		}
		
		return r;
	}
	@Override
	public String objectName() {
		return "GAME_DEFINITION";
	}
}
