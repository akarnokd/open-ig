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
 * A selectable spacewar structure with hitpoints.
 * @author akarnokd, 2011.08.16.
 */
public abstract class SpacewarStructure extends SpacewarObject {
	/** The information image. */
	public BufferedImage infoImage;
	/** Available hitpoints, single object. */
	/** Is the ship selected? */
	public boolean selected;
	/** The available hitpoints. */
	public int hp;
	/** The maximum hitpoints. */
	public int hpMax;
	/** The shield hitpoints. */
	public int shield;
	/** The maximum shield hitpoints. */
	public int shieldMax;
	/** The destruction sound. */
	public SoundType destruction;
}
