/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The skirmish definition.
 * @author akarnokd, 2012.08.20.
 */
public class SkirmishDefinition extends CustomGameDefinition {
	/** The list of players. */
	public final List<SkirmishPlayer> players = new ArrayList<>();
	/**
	 * Save the skirmish definition.
	 * @param xout the output
	 */
	public void save(XElement xout) {
		xout.saveFields(this);
		XElement xplayers = xout.add("players");
		for (SkirmishPlayer p : players) {
			XElement xplayer = xplayers.add("player");
			xplayer.saveFields(p);
			XElement xtraits = xplayer.add("traits");
			for (Trait t : p.traits) {
				xtraits.add("trait").set("id", t.id);
			}
		}
	}
	/**
	 * Load the skirmish definition.
	 * @param xin the input
	 * @param traits the global traits
	 */
	public void load(XElement xin, Traits traits) {
		players.clear();
		xin.loadFields(this);
		for (XElement xplayers : xin.childrenWithName("players")) {
			for (XElement xplayer : xplayers.childrenWithName("player")) {
				SkirmishPlayer sp = new SkirmishPlayer();
				xplayer.loadFields(sp);
				for (XElement xtraits : xplayer.childrenWithName("traits")) {
					for (XElement xtrait : xtraits.childrenWithName("trait")) {
						sp.traits.add(traits.trait(xtrait.get("id")));
					}
				}
				players.add(sp);
			}
		}
	}
	/**
	 * Create a game definition.
	 * @param rl the resource locator.
	 * @return the composed definition
	 */
	public GameDefinition createDefinition(ResourceLocator rl) {
		GameDefinition galaxyDef = GameDefinition.parse(rl, this.galaxy);
		
		GameDefinition playerDef = GameDefinition.parse(rl, this.race);
		
		GameDefinition techDef = GameDefinition.parse(rl, this.tech);

		Set<String> labelRefs = new HashSet<>();
		labelRefs.addAll(galaxyDef.labels);
		labelRefs.addAll(playerDef.labels);
		labelRefs.addAll(techDef.labels);

		
		GameDefinition result = new GameDefinition();
		
		result.name = "skirmish";
		result.galaxy = galaxyDef.galaxy;
		result.texts.putAll(galaxyDef.texts);
		result.battle = techDef.battle;
		result.bridge = techDef.bridge;
		result.buildings = techDef.buildings;
		result.chats = playerDef.chats;
		result.diplomacy = playerDef.diplomacy;
		result.image = galaxyDef.image;
		result.intro = playerDef.intro;
		result.labels.addAll(labelRefs);
		result.parameters.putAll(techDef.parameters);
		result.planets = galaxyDef.planets;
		result.players = playerDef.players;
		result.scripting = null;
		result.level = maxLevel;
		result.techLevel = startLevel;
		result.talks = playerDef.talks;
		result.tech = techDef.tech;
		result.test = playerDef.test;
		result.walks = techDef.walks;
		
		result.noPlanetBuildings = true;
		result.noPlanetInventory = true;
		result.noPlanetOwner = true;
		
		return result;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + super.hashCode();
		result = prime * result + Objects.hashCode(players);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SkirmishDefinition)) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		SkirmishDefinition other = (SkirmishDefinition) obj;
        return Objects.equals(players, other.players);
    }
	
}
