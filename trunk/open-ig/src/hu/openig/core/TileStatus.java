/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * The tile status indicator for minimap rendering.
 * @author karnokd, 2009.05.24.
 * @version $Revision 1.0$
 */
public enum TileStatus {
	/** Normal status. */
	NORMAL,
	/** No energy status. */
	NO_ENERGY,
	/** Damaged status. */
	DAMAGED,
	/** Destroyed status. */
	DESTROYED
}
