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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Record for a remote battle state.
 * @author akarnokd, 2013.05.02.
 *
 */
public class BattleStatus implements MessageObjectIO, MessageArrayItemFactory<BattleStatus> {
	/** The object name. */
	public static final String OBJECT_NAME = "BATTLE";
	/** The array name. */
	public static final String ARRAY_NAME = "BATTLES";
	/** The battle identifier. */
	public int id;
	/** The nearby planet id. */
	public String planetId;
	/** The list of participating players. */
	public final List<String> players = new ArrayList<>();
	/** The list of participating fleets. */
	public final List<FleetStatus> fleets = new ArrayList<>();
	/** The winners of the space battle. */
	public final Set<String> spaceWinner = new HashSet<>();
	/** The winners of the ground battle. */
	public final Set<String> groundWinner = new HashSet<>();
	/** Is this battle in space mode? */
	public boolean inSpaceBattle;
	/** Is this battle in space retreat? */
	public boolean inSpaceRetreat;
	/** Is this battle in ground mode? */
	public boolean inGroundBattle;
	/** Is this battle in ground retreat mode. */
	public boolean inGroundRetreat;
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
	public BattleStatus invoke() {
		return new BattleStatus();
	}
	@Override
	public String arrayName() {
		return ARRAY_NAME;
	}
	@Override
	public String objectName() {
		return OBJECT_NAME;
	}
	/**
	 * Creates a message array from the sequence of battle status records.
	 * @param src the sequence of battle status records.
	 * @return the message array
	 */
	public static MessageArray toArray(Iterable<? extends BattleStatus> src) {
		MessageArray ma = new MessageArray(ARRAY_NAME);
		for (BattleStatus bs : src) {
			ma.add(bs.toMessage());
		}
		return ma;
	}
}
