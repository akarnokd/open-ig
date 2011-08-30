/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.model.BattleProjectile.Mode;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A record representing a spaceship or a spacestation.
 * @author akarnokd, 2011.08.15.
 */
public class SpacewarShip extends SpacewarStructure {
	/** The angle images of the spaceship. */
	public BufferedImage[] angles;
	/** Available hitpoints, single object. */
	/** The available weapon ports. */
	public final List<SpacewarWeaponPort> ports = new ArrayList<SpacewarWeaponPort>();
	/** The beam angle in an X-Y screen directed coordinate system, 0..2*PI. */
	public double angle;
	/** The rotation speed. */
	public int rotationSpeed;
	/** The movement speed. */
	public int movementSpeed; 
	/** The referenced inventory item. */
	public InventoryItem item;
	/** The ecm level of the ship. */
	public int ecmLevel;
	/** The number of batched fighters. Once hp reaches zero, this number is reduced, the batch will disappear when the count reaches zero. */
	public int count;
	@Override
	public BufferedImage get() {
		double a = angle / 2 / Math.PI; // angle to percentage
		return angles[((int)Math.round(angles.length * a)) % angles.length];
	}
	/**
	 * @return Creates a new deep copy of this record.
	 */
	public SpacewarShip copy() {
		SpacewarShip r = new SpacewarShip();
		r.x = x;
		r.y = y;
		r.owner = owner;
		r.angle = angle;
		r.angles = angles;
		r.count = count;
		r.destruction = destruction;
		r.ecmLevel = ecmLevel;
		r.hp = hp;
		r.hpMax = hpMax;
		r.infoImage = infoImage;
		r.item = item;
		r.movementSpeed = movementSpeed;
		for (SpacewarWeaponPort w : ports) {
			r.ports.add(w.copy());
		}
		r.rotationSpeed = rotationSpeed;
		r.selected = selected;
		r.shield = shield;
		r.shieldMax = shieldMax;
		
		return r;
	}
	@Override
	public int getFirepower() {
		int sum = 0;
		for (SpacewarWeaponPort p : ports) {
			if (p.projectile.mode == Mode.BEAM) {
				sum += p.projectile.damage * p.count;
			}
		}
		return sum;
	}
	@Override
	public String getType() {
		return item.type.name;
	}
}
