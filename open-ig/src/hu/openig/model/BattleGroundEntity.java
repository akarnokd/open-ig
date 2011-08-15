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
 * The rotation + fire phase images of ground entities.
 * @author akarnokd, Jul 31, 2011
 */
public class BattleGroundEntity {
	/** The normal [rotation][fire-phase] of the entity. */
	public BufferedImage[][] normal;
	/** The alternative [rotation][fire-phase] of the entity. */
	public BufferedImage[][] alternative;
	/** The destruction sound. */
	public WarEffectsType destroy;
	/** The fire sound if non null. */
	public WarEffectsType fire;
}
