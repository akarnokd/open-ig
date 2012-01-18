/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Pair;
import hu.openig.model.Fleet;
import hu.openig.model.InventoryItem;
import hu.openig.model.Planet;

/**
 * Mission 6: Defend Achilles.
 * @author akarnokd, 2012.01.18.
 */
public class Mission6 extends Mission {
	@Override
	public void onLevelChanged() {
		if (world.level == 2) {
			removeMissions(6, 17);
			helper.send("Centronom-Check").visible = true;
			helper.send("New Caroline-Check").visible = true;

			// ensure the initial fleet conditions are met
			player.setAvailable(research("Fighter2"));
			createMainShip();
		}
	}
	/**
	 * Creates the main ship for level 2.
	 */
	void createMainShip() {
		Pair<Fleet, InventoryItem> own = findTaggedFleet("CampaignMainShip2", player);
		if (own == null) {
			own = findTaggedFleet("CampaignMainShip1", player);
			Fleet f = null;
			if (own != null) {
				f = own.first;
			} else {
				Planet ach = planet("Achilles");
				f = createFleet(label("Empire.main_fleet"), player, ach.x + 5, ach.y + 5);
			}
			f.addInventory(research("Fighter2"), 2);
			f.addInventory(research("Cruiser1"), 1);
			for (InventoryItem ii : f.inventory) {
				if (ii.type.id.equals("Cruiser1")) {
					ii.tag = "CampaignMainShip2";
					setSlot(ii, "laser", "Laser1", 6);
					setSlot(ii, "shield", "Shield1", 1);
					setSlot(ii, "cannon", "IonCannon", 1);
					setSlot(ii, "hyperdrive", "HyperDrive1", 1);
				}
			}
		}
	}
	@Override
	public void onTime() {
		if (world.level != 2) {
			return;
		}
		checkMainShip();
	}
	/**
	 * Check if the main ship still exists.
	 */
	void checkMainShip() {
		Pair<Fleet, InventoryItem> ft = findTaggedFleet("CampaignMainShip2", player);
		if (ft == null) {
			if (!helper.hasTimeout("MainShip-Lost")) {
				helper.setTimeout("MainShip-Lost", 3000);
			}
			if (helper.isTimeout("MainShip-Lost")) {
				helper.gameover();
				loseGameMovie("loose/destroyed_level_2");
			}
		}
	}
}
