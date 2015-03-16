/*
 * Copyright 2008-2014, David Karnok 
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
	InventoryItems inventory();
	/**
	 * Check if the given technology can be undeployed from the inventory.
	 * @param type the technology
	 * @return true if it can be undeployed
	 */
	boolean canUndeploy(ResearchType type);
	/**
	 * Check if the given technology can be deployed into this inventory.
	 * @param type the technology
	 * @return true if can be deployed
	 */
	boolean canDeploy(ResearchType type);
	/**
	 * Deploys a given amount of items with the given type if
	 * there is enough available in the main inventory
	 * and it doesn't violate capacity constraints.
	 * <p>The method does not check if the research itself
	 * is available to the user or not.</p>
	 * @param rt the technology
	 * @param owner the owner
	 * @param count the number of items to deploy
	 * @return the list of created inventory items
	 */
	List<InventoryItem> deployItem(ResearchType rt, Player owner, int count);
	/**
	 * Sells the given amount of items from the given inventory
	 * item.
	 * @param itemId item id
	 * @param count the number of items to sell
	 */
	void sell(int itemId, int count);
	/**
	 * Sells the given amount of items from the given inventory
	 * item.
	 * @param ii the inventory item
	 * @param count the number of items to sell
	 */
	void sell(InventoryItem ii, int count);
	/**
	 * Undeploy the given amount of items from the
	 * given inventory item, if there is that many.
	 * Items are placed back in the player's global
	 * inventory.
	 * @param itemId the inventory item id
	 * @param count the number of items to remove
	 */
	void undeployItem(int itemId, int count);
	/**
	 * Check if the given inventory item id is in this inventory.
	 * @param itemId the item id
	 * @return true if contains
	 */
	boolean contains(int itemId);
}
