/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A fleet.
 * @author akarnokd, 2010.01.07.
 */
public class Fleet implements Named, Owned {
	/** The owner of the fleet. */
	public Player owner;
	/** The X coordinate. */
	public int x;
	/** The Y coordinate. */
	public int y;
	/** The associated ship icon. */
	public BufferedImage shipIcon;
	/** The radar radius. */
	public int radar;
	/** The fleet name. */
	public String name;
	/** The fleet inventory: ships and tanks. */
	public final List<FleetInventoryItem> inventory = new ArrayList<FleetInventoryItem>();
	/** @return calculate the fleet statistics. */
	public FleetStatistics getStatistics() {
		FleetStatistics result = new FleetStatistics();
		
		return result;
	}
	@Override
	public String name() {
		return name;
	}
	@Override
	public Player owner() {
		// TODO Auto-generated method stub
		return owner;
	}
}
