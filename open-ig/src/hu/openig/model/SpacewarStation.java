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
 * The space station.
 * @author akarnokd, 2011.08.16.
 */
public class SpacewarStation extends SpacewarStructure {
	/** The image on the battlefield. */
	public BufferedImage image;
	/** The referenced inventory item. */
	public InventoryItem item;
	/** The available weapon ports. */
	public final List<SpacewarWeaponPort> ports = new ArrayList<SpacewarWeaponPort>();
	/** The ECM level. */
	public int ecmLevel;
	@Override
	public BufferedImage get() {
		return image;
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
