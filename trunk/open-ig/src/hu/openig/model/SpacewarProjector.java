/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A record representing a spaceship or a spacestation.
 * @author akarnokd, 2011.08.15.
 */
public class SpacewarProjector extends SpacewarStructure {
	/** The angle images of the spaceship. */
	public BufferedImage[] angles;
	/** The available weapon ports. */
	public final List<SpacewarWeaponPort> ports = new ArrayList<SpacewarWeaponPort>();
	/** The beam angle in an X-Y screen directed coordinate system, 0..2*PI. */
	public double angle;
	/** The building reference. */
	public Building building;
	@Override
	public BufferedImage get() {
		double a = angle / 2 / Math.PI; // angle to percentage
		return angles[((int)Math.round(angles.length * a)) % angles.length];
	}
}
