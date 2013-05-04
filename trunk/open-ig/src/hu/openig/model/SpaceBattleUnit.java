/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author akarnokd, 2013.05.02.
 *
 */
public class SpaceBattleUnit {
	/** The unit's unique id. */
	public int id;
	/** The owner. */
	public String owner;
	/** The unit type. */
	public String type;
	/** The number of units in this batch. */
	public int count;
	/** The total current hitpoints. */
	public double hp;
	/** The maximum hitpoints. */
	public int hpMax;
	/** The current shield points. */
	public double shield;
	/** The maximum shield points. */
	public int shieldMax;
	/** The current position. */
	public double x;
	/** The current position. */
	public double y;
	/** The rotation angle. */
	public double angle;
	/** The target unit if not null. */
	public Integer targetUnit;
	/** Is the unit in guard mode? */
	public boolean guard;
	/** The target position if not null. */
	public Double moveX;
	/** The target position if not null. */
	public Double moveY;
	/** Is this unit in kamikaze mode? */
	public boolean kamikaze;
	/** The list of equipment statuses. */
	public final List<EquipmentStatus> equipment = new ArrayList<EquipmentStatus>();
}
