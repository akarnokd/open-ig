/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.MessageArray;
import hu.openig.net.MessageObject;

import java.util.ArrayList;
import java.util.List;


/**
 * The basic ground battle unit status.
 * @author akarnokd, 2013.05.02.
 */
public class GroundBattleUnit implements MessageObjectIO, MessageArrayItemFactory<GroundBattleUnit> {
	/** Object name. */
	public static final String OBJECT_NAME = "GROUND_UNIT";
	/** Array name. */
	public static final String ARRAY_NAME = "GROUND_BATTLE_UNITS";
	/** The unit unique id. */
	public int id;
	/** The unit research type. */
	public String type;
	/** The owner id. */
	public String owner;
	/** The firing phase index. */
	public int phase;
	/** The weapon cooldown. */
	public int cooldown;
	/** The rotation angle in radians. */
	public double angle;
	/** The current target unit. */
	public Integer attackUnit;
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
	public GroundBattleUnit invoke() {
		return new GroundBattleUnit();
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
	 * Creates a message array from the sequence of ground battle units.
	 * @param src the sequence
	 * @return the message array
	 */
	public static MessageArray toArray(Iterable<? extends GroundBattleUnit> src) {
		MessageArray ma = new MessageArray(ARRAY_NAME);
		
		for (GroundBattleUnit u : src) {
			ma.add(u.toMessage());
		}
		
		return ma;
	}
	/**
	 * Parse a message array for ground battle unit records.
	 * @param ma the message array
	 * @return the list of ground units
	 */
	public static List<GroundBattleUnit> fromArray(MessageArray ma) {
		List<GroundBattleUnit> result = new ArrayList<>();
		
		for (MessageObject o : ma.objects()) {
			GroundBattleUnit u = new GroundBattleUnit();
			u.fromMessage(o);
			result.add(u);
		}
		
		return result;
	}
}
