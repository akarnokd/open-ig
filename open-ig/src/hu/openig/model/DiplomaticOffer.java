/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;


/**
 * Represents the parameters of a diplomatic offer.
 * @author akarnokd, 2012.05.26.
 */
public class DiplomaticOffer {
	/**  The call type. */
	public CallType callType;
	/** The approach type. */
	public ApproachType approach;
	/** The optional parameter. */
	private Object value;
	/** 
	 * Empty constructor.
	 */
	public DiplomaticOffer() {
		
	}
	/**
	 * Initializes the offer with a call type and approach.
	 * @param callType the call type
	 * @param approach the approach
	 */
	public DiplomaticOffer(CallType callType, ApproachType approach) {
		this.callType = callType;
		this.approach = approach;
	}
	/**
	 * Creates a copy of this object.
	 * @return the copy
	 */
	public DiplomaticOffer copy() {
		return new DiplomaticOffer(callType, approach).value(value);
	}
	/**
	 * The optional offer value.
	 * @return the value
	 */
	public Object value() {
		return value;
	}
	/**
	 * Sets a money value.
	 * @param money the money
	 * @return this
	 */
	public DiplomaticOffer value(long money) {
		this.value = money;
		return this;
	}
	/**
	 * Sets a player value.
	 * @param player a layer
	 * @return this
	 */
	public DiplomaticOffer value(Player player) {
		this.value = player.id;
		return this;
	}
	/**
	 * Sets the value from the given object,
	 * or throws an IllegalArgumentException if 
	 * the type is not supported.
	 * @param o the object
	 * @return this
	 */
	public DiplomaticOffer value(Object o) {
		if (o == null) {
			value = null;
			return this;
		} else
		if (o instanceof Number) {
			value(((Number)o).longValue());
			return this;
		} else
		if (o instanceof String) {
			value = o;
			return this;
		} else
		if (o instanceof Player) {
			value((Player)o);
			return this;
		}
		throw new IllegalArgumentException("Unsupported type: " + o.getClass().toString());
	}
}
