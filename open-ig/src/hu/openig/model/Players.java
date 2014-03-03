/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The map of players.
 * @author akarnokd, Oct 19, 2011
 */
public class Players {
	/** The map of player-id to player object. */
	public final Map<String, Player> players = new LinkedHashMap<>();
	/**
	 * Retrieve a player.
	 * @param id the player id
	 * @return the player
	 */
	public Player get(String id) {
		return players.get(id);
	}
	/**
	 * Set a new player object with the given ID and return the previous entry.
	 * @param id the id
	 * @param p the player
	 * @return the previous player or null
	 */
	public Player put(String id, Player p) {
		return players.put(id, p);
	}
	/** 
	 * @return the players map
	 */
	public Map<String, Player> map() {
		return players;
	}
	/**
	 * @return The players as collection.
	 */
	public Collection<Player> values() {
		return players.values();
	}
}
