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
 * Record representing a space projectile with alternative appearance and sound effect.
 * @author akarnokd, 2011.08.15.
 */
public class BattleProjectile {
	/** The projectile mode. */
	public enum Mode {
		/** Beam, traves straight. */
		BEAM,
		/** Rocket, may change direction. */
		ROCKET,
		/** Bomb, may change direction. */
		BOMB,
		/** Virus bomb, may change direction. */
		VIRUS,
		/** Multi rocket, may change direction. */
		MULTI_ROCKET
	}
	/** The sound effect to play when fired. */
	public SoundType sound;
	/** The normal [rotation][phase] image matrix. */
	public BufferedImage[][] matrix;
	/** The alternative [rotation][phase] image matrix. */
	public BufferedImage[][] alternative;
	/** The delay between fires. */
	public int delay;
	/** The range of the projectile, -1 means unlimited. */
	public int range;
	/** The damage caused by the projectile. */
	public int damage;
	/** The damage area. */
	public int area;
	/** The projectile mode. */
	public Mode mode;
}
