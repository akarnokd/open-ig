/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * Contains status information of the factory capacities.
 * @author karnokd, 2009.06.10.
 * @version $Revision 1.0$
 */
public class FactoryInfo {
	/** The number of orbital factories of the player. */
	public int orbital;
	/** The current capacity of the ship production factories. */
	public int currentShip;
	/** The total theoretical capacity of the ship production factories. */
	public int totalShip;
	/** The current capacity of the equipment factories. */
	public int currentEquipment;
	/** The total capacity of the equipment factories. */
	public int totalEquipment;
	/** The current capacity of the weapons factories. */
	public int currentWeapons;
	/** The total capacity of the weapons factories. */
	public int totalWeapons;
}
