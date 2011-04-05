/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A fleet's inventory item description.
 * @author akarnokd, 2011.04.05.
 */
public class FleetInventoryItem {
	/** The inventory type. */
	public ResearchType type;
	/** The count. */
	public int count;
	/** The current hitpoints. */
	public int hp;
	/** The current shield points. */
	public int shield;
	/** The fleet's inventory slots. */
	public final List<FleetInventorySlot> slots = new ArrayList<FleetInventorySlot>();
}
