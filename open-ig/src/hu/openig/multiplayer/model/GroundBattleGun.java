/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer.model;

/**
 * A ground battle gun.
 * @author akarnokd, 2013.05.02.
 */
public class GroundBattleGun extends GroundBattleUnit {
	/** The parent building id. */
	public int parentBuilding;
	/** The index within the building's guns. */
	public int index;
}
