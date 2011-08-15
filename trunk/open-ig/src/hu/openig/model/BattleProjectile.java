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
	/** The sound effect to play when fired. */
	public SpaceEffectsType sound;
	/** The normal [rotation][phase] image matrix. */
	public BufferedImage[][] matrix;
	/** The alternative [rotation][phase] image matrix. */
	public BufferedImage[][] alternative;
}
