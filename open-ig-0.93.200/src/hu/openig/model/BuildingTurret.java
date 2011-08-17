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
 * A building turret graphics and location description.
 * @author akarnokd, Jul 31, 2011
 */
public class BuildingTurret {
	/** The strip index where the turret should be rendered. */
	public int strip;
	/** The rendering position relative to the left of the target strip. */
	public int dx;
	/** The rendering position relative to the bottom of the target strip. */
	public int dy;
	/** The [rotation][fire phase] images of the turret. */ 
	public BufferedImage[][] matrix;
}
