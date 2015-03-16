/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Btn2;
import hu.openig.core.Cat;
import hu.openig.core.Img;
import hu.openig.model.ResourceLocator;

import java.awt.image.BufferedImage;

/**
 * The equipment screen graphics resources.
 * @author akarnokd, 2009.11.09.
 */
public class EquipmentGFX {
	/** The base image. */
	@Img(name = "equipment/panel_equipment_base")
	public BufferedImage base;
	/** The base image. */
	@Img(name = "equipment/panel_top")
	public BufferedImage panelTop;
	/** The base image. */
	@Img(name = "equipment/panel_middle")
	public BufferedImage panelMiddle;
	/** The base image. */
	@Img(name = "equipment/panel_bottom")
	public BufferedImage panelBottom;
	/** The base image. */
	@Img(name = "equipment/left_stars")
	public BufferedImage leftStars;
	/** The base image. */
	@Img(name = "equipment/right_stars")
	public BufferedImage rightStars;
	/** The equipment is under development. */
	@Img(name = "equipment/under_development_huge")
	public BufferedImage underDevelopment;
	/** Add one. */
	@Btn2(name = "equipment/button_add_one")
	public BufferedImage[] addOne;
	/** Battleship category. */
	@Cat(name = "equipment/button_category_all")
	public BufferedImage[] categoryAll;
	/** Battleship category. */
	@Cat(name = "equipment/button_category_battleships")
	public BufferedImage[] categoryBattleships;
	/** Destroyers category. */
	@Cat(name = "equipment/button_category_cruisers")
	public BufferedImage[] categoryCruisers;
	/** Fighters category. */
	@Cat(name = "equipment/button_category_fighters")
	public BufferedImage[] categoryFighers;
	/** Tanks category. */
	@Cat(name = "equipment/button_category_tanks")
	public BufferedImage[] categoryTanks;
	/** Vehicles category. */
	@Cat(name = "equipment/button_category_vehicles")
	public BufferedImage[] categoryVehicles;
	/** Vehicles category. */
	@Cat(name = "equipment/button_category_weapons")
	public BufferedImage[] categoryWeapons;
	/** Vehicles category. */
	@Cat(name = "equipment/button_category_equipment")
	public BufferedImage[] categoryEquipment;
	/** Empty category. */
	@Img(name = "equipment/button_category_empty")
	public BufferedImage categoryEmpty;
	/** Space station category. */
	@Cat(name = "equipment/button_category_space_stations")
	public BufferedImage[] categorySpaceStations;
	/** An empty button for the map. */
	@Img(name = "equipment/button_empty_tall")
	public BufferedImage buttonMapEmpty;
	/** Move one unit left. */
	@Btn2(name = "equipment/button_move_left_1")
	public BufferedImage[] moveLeft1;
	/** Move several units left. */
	@Btn2(name = "equipment/button_move_left_2")
	public BufferedImage[] moveLeft2;
	/** Move all units left. */
	@Btn2(name = "equipment/button_move_left_3")
	public BufferedImage[] moveLeft3;
	/** Move one unit right. */
	@Btn2(name = "equipment/button_move_right_1")
	public BufferedImage[] moveRight1;
	/** Move several unit right. */
	@Btn2(name = "equipment/button_move_right_2")
	public BufferedImage[] moveRight2;
	/** Move all units right. */
	@Btn2(name = "equipment/button_move_right_3")
	public BufferedImage[] moveRight3;
	/** Remove one item. */
	@Btn2(name = "equipment/button_remove_one")
	public BufferedImage[] removeOne;
	/** The add button. */
	@Btn2(name = "equipment/button_add")
	public BufferedImage[] add;
	/** Delete button. */
	@Btn2(name = "equipment/button_delete")
	public BufferedImage[] delete;
	/** End join. */
	@Btn2(name = "equipment/button_end_join")
	public BufferedImage[] endJoin;
	/** End split. */
	@Btn2(name = "equipment/button_end_split")
	public BufferedImage[] endSplit;
	/** Join button. */
	@Btn2(name = "equipment/button_join")
	public BufferedImage[] join;
	/** List button. */
	@Btn2(name = "equipment/button_list")
	public BufferedImage[] list;
	/** New fleet button. */
	@Btn2(name = "equipment/button_new")
	public BufferedImage[] newFleet;
	/** Planet button. */
	@Btn2(name = "equipment/button_planet")
	public BufferedImage[] planet;
	/** Remove button. */
	@Btn2(name = "equipment/button_remove")
	public BufferedImage[] remove;
	/** Split button. */
	@Btn2(name = "equipment/button_split")
	public BufferedImage[] split;
	/** Starmap button. */
	@Btn2(name = "equipment/button_starmap")
	public BufferedImage[] starmap;
	/** Transfer button. */
	@Btn2(name = "equipment/button_transfer")
	public BufferedImage[] transfer;
	/** No planet nearby. */
	@Img(name = "equipment/label_no_planet_nearby")
	public BufferedImage noPlanetNearby;
	/** No spaceport. */
	@Img(name = "equipment/label_no_spaceport")
	public BufferedImage noSpaceport;
	/** No spaceport. */
	@Img(name = "equipment/label_no_spacestation")
	public BufferedImage noSpaceStation;
	/** Not your planet. */
	@Img(name = "equipment/label_not_your_planet")
	public BufferedImage notYourplanet;
	/** Sell button. */
	@Btn2(name = "equipment/button_sell")
	public BufferedImage[] sell;
	/** Upgrade everything button. */
	@Btn2(name = "equipment/button_upgrade_all")
	public BufferedImage[] upgradeAll;
	/** No flagship. */
	@Img(name = "equipment/label_no_flagship")
	public BufferedImage noFlagship;
	/**
	 * Load the resources.
	 * @param rl the resource locator
	 * @return this
	 */
	public EquipmentGFX load(ResourceLocator rl) {
		GFXLoader.loadResources(this, rl);
		return this;
	}
}
