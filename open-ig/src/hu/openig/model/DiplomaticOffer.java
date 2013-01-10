/*
 * Copyright 2008-2013, David Karnok 
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
	public Object value;
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
		this(callType, approach, null);
	}
	/**
	 * Initializes the offer with a call type, approach and parameter.
	 * @param callType the call type
	 * @param approach the approach
	 * @param value the value
	 */
	public DiplomaticOffer(CallType callType, ApproachType approach, Object value) {
		this.callType = callType;
		this.approach = approach;
		this.value = value;
	}
}
