/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * A simple interface for owned objects by a player.
 * @author akarnokd, 2011.04.04.
 */
public interface Owned {
	/** @return the owner, may be null */
	Player owner();
}
