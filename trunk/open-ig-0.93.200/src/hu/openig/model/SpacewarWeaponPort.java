/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * A weapon port description.
 * @author akarnokd, 2011.08.15.
 */
public class SpacewarWeaponPort {
	/** The projectile ID of the weapon port. */
	public BattleProjectile projectile;
	/** The number of items. */
	public int count = 1;
	/** @return create a copy of this record. */
	public SpacewarWeaponPort copy() {
		SpacewarWeaponPort r = new SpacewarWeaponPort();
		r.projectile = projectile;
		r.count = count;
		return r;
	}
}
