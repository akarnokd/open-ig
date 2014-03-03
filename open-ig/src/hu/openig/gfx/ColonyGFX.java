/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Anim;
import hu.openig.core.Btn2;
import hu.openig.core.Cat;
import hu.openig.core.Img;
import hu.openig.model.ResourceLocator;

import java.awt.image.BufferedImage;

/**
 * The colony graphics.
 * @author akarnokd, 2009.11.13.
 */
public class ColonyGFX {
	/** Building on fire animations. */
	@Anim(name = "colony/building_fire", width = 20)
	public BufferedImage[] buildingFire;
	/** Building smoke animations. */
	@Anim(name = "colony/building_smoke", width = 16)
	public BufferedImage[] buildingSmoke;
	/** Building information panel on the right. */
	@Img(name = "colony/building_information_panel")
	public BufferedImage buildingInfoPanel;
	/** Buildings list panel on the left. */
	@Img(name = "colony/buildings_panel")
	public BufferedImage buildingsPanel;
	/** Next building button. */
	@Btn2(name = "colony/button_buildings_downwards")
	public BufferedImage[] downwards;
	/** Empty building button. */
	@Img(name = "colony/button_buildings_empty")
	public BufferedImage empty;
	/** Previous building button. */
	@Btn2(name = "colony/button_buildings_upwards")
	public BufferedImage[] upwards;
	/** Information panel. */
	@Img(name = "colony/info_panel")
	public BufferedImage infoPanel;
	/** Small information panel. */
	@Img(name = "colony/info_panel_small")
	public BufferedImage smallInfoPanel;
	/** Radar panel. */
	@Img(name = "colony/radar_panel_slashed")
	public BufferedImage radarPanel;
	/** The tile edge for selection. */
	@Img(name = "colony/tile_1x1_selected")
	public BufferedImage tileEdge;
	/** The tile crossed. */
	@Img(name = "colony/tile_1x1_selected_crossed")
	public BufferedImage tileCrossed;
	/** Upgrade panel. */
	@Img(name = "colony/upgrade_panel")
	public BufferedImage upgradePanel;
	/** Building is active. */
	@Img(name = "colony/button_active")
	public BufferedImage active;
	/** Bridge button. */
	@Btn2(name = "colony/button_bridge")
	public BufferedImage[] bridge;
	/** Build button. */
	@Btn2(name = "colony/button_build")
	public BufferedImage[] build;
	/** Colony info button. */
	@Btn2(name = "colony/button_colony_info")
	public BufferedImage[] colonyInfo;
	/** Constructing with percent. */
	@Img(name = "colony/button_constructing")
	public BufferedImage constructing;
	/** Damaged with percent. */
	@Img(name = "colony/button_damaged")
	public BufferedImage damaged;
	/** Demolish. */
	@Btn2(name = "colony/button_demolish")
	public BufferedImage[] demolish;
	/** List button. */
	@Btn2(name = "colony/button_list")
	public BufferedImage[] list;
	/** Repairing with percent. */
	@Img(name = "colony/button_repairing")
	public BufferedImage repairing;
	/** Planets button. */
	@Btn2(name = "colony/button_planets")
	public BufferedImage[] planets;
	/** Starmap button. */
	@Btn2(name = "colony/button_starmap")
	public BufferedImage[] starmap;
	/** Start battle. */
	@Btn2(name = "colony/button_start_battle")
	public BufferedImage[] startBattle;
	/** Damaged status. */
	@Img(name = "colony/button_status_damaged")
	public BufferedImage statusDamaged;
	/** Inactive status. */
	@Img(name = "colony/button_status_inactive")
	public BufferedImage statusInactive;
	/** No energy status. */
	@Img(name = "colony/button_status_no_energy")
	public BufferedImage statusNoEnergy;
	/** Offline status. */
	@Img(name = "colony/button_status_offline")
	public BufferedImage statusOffline;
	/** Undamaged without a percent indicator. */
	@Img(name = "colony/button_undamaged")
	public BufferedImage undamaged;
	/** Energy label. */
	@Img(name = "colony/label_energy")
	public BufferedImage energy;
	/** Operational label. */
	@Img(name = "colony/label_operational")
	public BufferedImage operational;
	/** Production label. */
	@Img(name = "colony/label_production")
	public BufferedImage production;
	/** Workers label. */
	@Img(name = "colony/label_workers")
	public BufferedImage workers;
	/** The upgrade star. */
	@Img(name = "colony/upgrade")
	public BufferedImage upgrade;
	/** The unpowered animation. */
	@Anim(name = "colony/worker", width = 20)
	public BufferedImage[] worker;
	/** Repair animation. */
	@Anim(name = "colony/repair", width = 24)
	public BufferedImage[] repair;
	/** The unpowered animation. */
	@Anim(name = "colony/unpowered", width = 20)
	public BufferedImage[] unpowered;
	/** Sidebar left top. */
	@Img(name = "colony/sidebar_left_top")
	public BufferedImage sidebarLeftTop;
	/** Sidebar left bottom. */
	@Img(name = "colony/sidebar_left_bottom")
	public BufferedImage sidebarLeftBottom;
	/** Sidebar right top. */
	@Img(name = "colony/sidebar_right_top")
	public BufferedImage sidebarRightTop;
	/** Sidebar right top. */
	@Img(name = "colony/sidebar_right_bottom")
	public BufferedImage sidebarRightBottom;
	/** Sidebar left filler. */
	@Img(name = "colony/sidebar_left_fill")
	public BufferedImage sidebarLeftFill;
	/** Sidebar right fill. */
	@Img(name = "colony/sidebar_right_fill")
	public BufferedImage sidebarRightFill;
	/** Buildings button. */
	@Img(name = "colony/button_buildings")
	public BufferedImage sidebarBuildings;
	/** Buildings info button. */
	@Img(name = "colony/button_building_info")
	public BufferedImage sidebarBuildingInfo;
	/** Radar button. */
	@Img(name = "colony/button_radar")
	public BufferedImage sidebarRadar;
	/** Colony info. */
	@Img(name = "colony/button_info")
	public BufferedImage sidebarColonyInfo;
	/** Buttons. */
	@Img(name = "colony/button_navigation")
	public BufferedImage sidebarButtons;
	/** Buildings empty. */
	@Img(name = "colony/sidebar_buildings_empty")
	public BufferedImage sidebarBuildingsEmpty;
	/** Radar empty. */
	@Img(name = "colony/sidebar_radar_empty")
	public BufferedImage sidebarRadarEmpty;
	/** Dark upgrade. */
	@Img(name = "colony/upgrade_dark")
	public BufferedImage upgradeDark;
	/** The upgrade label. */
	@Img(name = "colony/label_upgrade")
	public BufferedImage upgradeLabel;
	/** The no upgrades button. */
	@Img(name = "colony/upgrade_none")
	public BufferedImage upgradeNone;
	/** The unit selection box. */
	@Img(name = "groundwar/selection_box_light")
	public BufferedImage selectionBoxLight;
	/** The unit selection box. */
	@Img(name = "groundwar/selection_box_dark")
	public BufferedImage selectionBoxDark;
	/** The laid mine. */
	public BufferedImage[][] mine;
	/** The tank info panel. */
	@Img(name = "colony/tank_panel")
	public BufferedImage tankPanel;
	/** The zoom button (horizontal). */
	@Cat(name = "colony/button_zoom")
	public BufferedImage[] zoom;
	/**
	 * Load the resources for the given language.
	 * @param rl the resource locator
	 * @return this
	 */
	public ColonyGFX load(ResourceLocator rl) {
		GFXLoader.loadResources(this, rl);
		
		mine = new BufferedImage[][] {
			{ rl.getImage("groundwar/mine") }
		};
		return this;
	}
}
