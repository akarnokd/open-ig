/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.List;

/**
 * Interface for inventory management.
 * @author akarnokd, 2012.01.06.
 */
public interface HasInventory {
	/**
	 * Returns the inventory list.
	 * @return the inventory list 
	 */
	List<InventoryItem> inventory();
}
