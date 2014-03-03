/*
 * Copyright 2008-2014, David Karnok 
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
	/** The cooldown until this port can fire again. */
	public int cooldown;
	/** The parent inventory slot. */
	public final InventorySlot is;
	/**
	 * Constructor.
	 * @param is The inventory slot.
	 */
	public SpacewarWeaponPort(InventorySlot is) {
		this.is = is;
	}
	/** @return create a copy of this record. */
	public SpacewarWeaponPort copy() {
		SpacewarWeaponPort r = new SpacewarWeaponPort(is);
		r.projectile = projectile;
		r.count = count;
		return r;
	}
	/**
	 * Returns the damage for the given owner.
	 * @param owner the owner
	 * @return The damage of this projectile. 
	 */
	public double damage(Player owner) {
		return projectile.damage(owner);
	}
}
