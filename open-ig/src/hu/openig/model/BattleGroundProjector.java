/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;

/**
 * The space battle's ground projectors (e.g., planetary guns).
 * @author akarnokd, 2011.08.16.
 */
public class BattleGroundProjector {
	/** The normal image. */
	public BufferedImage[] normal;
	/** The alien/alternative image. */
	public BufferedImage[] alternative;
	/** The destruction sound. */
	public SoundType destruction;
	/** The projectile used. */
	public String projectile;
	/** The information image. */
	public String infoImageName;
	/** The inflicted damage. */
	public int damage;
	/** The rotation speed. */
	public int rotationTime;
}
