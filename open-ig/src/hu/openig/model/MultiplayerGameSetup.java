/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.MessageObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The record to help initialize a concrete multiplayer game,
 * including initial inventories, fleets, planet format, etc.
 * @author akarnokd, 2013.04.27.
 */
public class MultiplayerGameSetup implements MessageObjectIO {
	/**
	 * The list of all players.
	 */
	public final List<MultiplayerUser> players = new ArrayList<>();
	/**
	 * The list of existing planets, some might be
	 * overrides.
	 */
	public final List<MultiplayerPlanet> planets = new ArrayList<>();
	/**
	 * The set of available researches.
	 */
	public final Set<String> availableResearch = new LinkedHashSet<>();
	@Override
	public void fromMessage(MessageObject mo) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public MessageObject toMessage() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String objectName() {
		return "LOAD";
	}
}
