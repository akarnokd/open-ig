/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The negotiation types.
 * @author akarnokd, Apr 22, 2011
 */
public enum NegotiateType {
	/** General diplomatic relations. */
	DIPLOMATIC_RELATIONS,
	/** Send money. */
	MONEY,
	/** Improve trade. */
	TRADE,
	/** Ally against non-dargslan. */
	ALLY,
	/** Ally against dargslan. */
	DARGSLAN,
	/** Surrender. */
	SURRENDER
}
