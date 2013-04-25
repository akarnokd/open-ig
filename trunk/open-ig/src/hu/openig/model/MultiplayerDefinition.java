/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.util.List;

/**
 * Multiplayer definition record. 
 * @author akarnokd, 2013.04.25.
 */
public class MultiplayerDefinition extends CustomGameDefinition {
	/** The list of multiplayer users. */
	public final List<MultiplayerUser> players = U.newArrayList();
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
			for (Trait t : p.traits) {
				xtraits.add("trait").set("id", t.id);
			}
		}
	}
	/**
	 * Load the mutliplayer definition.
	 * @param xin the input
	 * @param traits the global traits
	 */
	public void load(XElement xin, Traits traits) {
		players.clear();
		xin.loadFields(this);
		for (XElement xplayers : xin.childrenWithName("players")) {
			for (XElement xplayer : xplayers.childrenWithName("player")) {
				MultiplayerUser sp = new MultiplayerUser();
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
}
