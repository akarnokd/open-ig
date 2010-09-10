/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.core;

/**
 * Enumeration for a fleet's status.
 * @author karnokd
 */
public enum FleetStatus {
	/** Stopped. */
	STOP("Stop"),
	/** Moving. */
	MOVE("Move"),
	/** Attacking. */
	ATTACK("Attack")
	;
	/** The status id for label lookup. */
	public final String id;
	/**
	 * Constructor.
	 * @param id the status id for label lookup
	 */
	FleetStatus(String id) {
		this.id = id;
	}
}
