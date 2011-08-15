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
 * The definition of a battle entity having a normal and alien
 * rotation images and a huge-image.
 * @author akarnokd, Jul 31, 2011
 */
public class BattleSpaceEntity {
	/** The image used for displaying details of the selected entity. */
	public BufferedImage image;
	/** The normal rotation image. */
	public BufferedImage[] normal;
	/** The alternative rotation image. */
	public BufferedImage[] alternative;
	/** The sound effect for explosion. */
	public SpaceEffectsType sound;
}
