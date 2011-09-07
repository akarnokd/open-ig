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
 * A ground war gun.
 * @author akarnokd, 2011.09.05.
 */
public class GroundwarGun extends GroundwarObject {
	/** The turret model. */
	public BattleGroundTurret model;
	/** The rendering cell position of the gun. */
	public int rx;
	/** The rendering cell position of the gun. */
	public int ry;
	/** The attached building. */
	public Building building;
	/** The owner planet. */
	public Planet planet;
	/** The target unit. */
	public GroundwarUnit attack;
	/** The weapon cooldown counter. */
	public int cooldown;
	@Override
	protected double[] getAngles() {
		return model.angles;
	}
	@Override
	protected BufferedImage[][] getMatrix() {
		return model.matrix;
	}
}
