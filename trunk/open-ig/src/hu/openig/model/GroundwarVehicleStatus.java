/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A ground battle vehicle.
 * @author akarnokd, 2013.05.02.
 */
public class GroundwarVehicleStatus extends GroundwarUnitStatus {
	/** Vehicle hitpoints.*/
	public double hp;
	/** Vehicle maximum hitpoints. */
	public double hpMax;
	/** The other vehicle who paralized this vehicle. */
	public Integer paralizedBy;
	/** Current position.*/
	public double x;
	/** Current position. */
	public double y;
	/** The target building if not null. */
	public Integer attackBuilding;
	/** The attack move target coordinate. */
	public Integer attackMoveX;
	/** The attack move target coordinate. */
	public Integer attackMoveY;
	/** The next movement cell. */
	public Integer moveX;
	/** The next movement cell. */
	public Integer moveY;
	/** The path points. */
	public final List<Point> path = new ArrayList<>();

}
