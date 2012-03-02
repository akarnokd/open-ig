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
import hu.openig.model.ResearchType;

/**
 * Mission 22: enter level 4. Colonize or capture 7 more planets.
 * @author akarnokd, 2012.02.23.
 */
public class Mission22 extends Mission {

	@Override
	public boolean applicable() {
		return world.level == 4;
	}
	@Override
	public void onLevelChanged() {
		if (world.level < 4) {
			return;
		}
		removeMissions(1, 25);
		
		createMainShip();
		// achievement
		String a = "achievement.admiral";
		if (!world.env.profile().hasAchievement(a)) {
			world.env.achievementQueue().add(a);
			world.env.profile().grantAchievement(a);
		}
	}
	
	/**
	 * Creates the main ship for level 4.
	 */
	void createMainShip() {
		Pair<Fleet, InventoryItem> own = findTaggedFleet("CampaignMainShip4", player);
		if (own != null) {
			return;
		}
		own = findTaggedFleet("CampaignMainShip3", player);
		if (own == null) {
			own = findTaggedFleet("CampaignMainShip2", player);
		}
		if (own == null) {
			own = findTaggedFleet("CampaignMainShip1", player);
		}
		Fleet f = null;
		if (own != null 
				&& own.first.getStatistics().battleshipCount < 3 
				&& own.first.getStatistics().cruiserCount < 25
				&& own.first.inventoryCount(research("Fighter2")) < 30 - 6) {
			f = own.first;
		} else {
			Planet ach = planet("Achilles");
			f = createFleet(label("Empire.main_fleet"), player, ach.x + 5, ach.y + 5);
		}			
		ResearchType rt = research("Flagship");
		f.addInventory(rt, 1);
		f.addInventory(research("LightTank"), 6);
		
		f.addInventory(research("Cruiser1"), 1);
		f.addInventory(research("Fighter2"), 6);
		
		InventoryItem ii = f.getInventoryItem(rt);
		ii.tag = "CampaignMainShip4";

		// loadout
		setSlot(ii, "laser", "Laser2", 24);
		setSlot(ii, "bomb", "Bomb1", 8);
		setSlot(ii, "rocket", "Rocket1", 16);
		setSlot(ii, "radar", "Radar1", 1);
		setSlot(ii, "cannon1", "IonCannon", 12);
		setSlot(ii, "cannon2", "IonCannon", 12);
		setSlot(ii, "shield", "Shield1", 14);
		setSlot(ii, "hyperdrive", "HyperDrive1", 1);

	}

}
