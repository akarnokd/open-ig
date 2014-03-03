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
import java.util.List;

/**
 * The space battle unit status record.
 * @author akarnokd, 2013.05.02.
 */
public class SpaceBattleUnit implements MessageObjectIO, MessageArrayItemFactory<SpaceBattleUnit> {
	/** */
	private static final String OBJECT_NAME = "SPACE_UNIT";
	/** */
	private static final String ARRAY_NAME = "SPACE_BATTLE_UNITS";
	/** The unit's unique id. */
	public int id;
	/** The owner. */
	public String owner;
	/** The unit type. */
	public String type;
	/** The number of units in this batch. */
	public int count;
	/** The total current hitpoints. */
	public double hp;
	/** The maximum hitpoints. */
	public int hpMax;
	/** The current shield points. */
	public double shield;
	/** The maximum shield points. */
	public int shieldMax;
	/** The current position. */
	public double x;
	/** The current position. */
	public double y;
	/** The rotation angle. */
	public double angle;
	/** The target unit if not null. */
	public Integer targetUnit;
	/** Is the unit in guard mode? */
	public boolean guard;
	/** The target position if not null. */
	public Double moveX;
	/** The target position if not null. */
	public Double moveY;
	/** Is this unit in kamikaze mode? */
	public boolean kamikaze;
	/** The list of equipment statuses. */
	public final List<EquipmentStatus> equipment = new ArrayList<>();
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
	public SpaceBattleUnit invoke() {
		return new SpaceBattleUnit();
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
	 * Create a message array from the sequence of space battle units.
	 * @param src the source sequence
	 * @return the message array
	 */
	public static MessageArray toArray(Iterable<? extends SpaceBattleUnit> src) {
		MessageArray o = new MessageArray(ARRAY_NAME);
		for (SpaceBattleUnit u : src) {
			o.add(u.toMessage());
		}
		return o;
	}
	/**
	 * Parses a message array of space battle unit elements.
	 * @param ma the message array
	 * @return the list of units
	 */
	public static List<SpaceBattleUnit> fromArray(MessageArray ma) {
		List<SpaceBattleUnit> list = new ArrayList<>();
		for (MessageObject o : ma.objects()) {
			SpaceBattleUnit u = new SpaceBattleUnit();
			u.fromMessage(o);
			list.add(u);
		}
		return list;
	}
}
