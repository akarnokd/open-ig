/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;

/**
 * The space station.
 * @author akarnokd, 2011.08.16.
 */
public class SpacewarShield extends SpacewarStructure {
	/** The image on the battlefield. */
	public BufferedImage image;
	/** The building reference. */
	public Building building;
	@Override
	public BufferedImage get() {
		return image;
	}
	@Override
	public int getFirepower() {
		return -1;
	}
	@Override
	public String getType() {
		return building.type.name;
	}
}
