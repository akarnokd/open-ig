/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.ResourceLocator;

import java.awt.image.BufferedImage;

/**
 * The colony graphics.
 * @author karnok, 2009.11.13.
 * @version $Revision 1.0$
 */
public class ColonyGFX {
	/** The resource locator. */
	protected ResourceLocator rl;
	/** Building on fire animations. */
	@Anim(name = "colony/building_fire", width = 20)
	public BufferedImage[] buildingFire;
	/** Building smoke animations. */
	@Anim(name = "colony/building_smoke", width = 16)
	public BufferedImage[] buildingSmoke;
	/** Information panel on the right. */
	@Img(name = "colony/building_information_panel")
	public BufferedImage informationPanel;
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
	/** Repair animation. */
	@Anim(name = "colony/repair", width = 18)
	public BufferedImage[] repair;
	/** Sidebar left bottom. */
	@Img(name = "colony/sidebar_left_bottom")
	public BufferedImage sidebarLeftBottom;
	/** Sidebar left bottom empty. */
	@Img(name = "colony/sidebar_left_bottom_empty")
	public BufferedImage sidebarLeftBottomEmpty;
	/** Sidebar left filler. */
	@Img(name = "colony/sidebar_left_fill")
	public BufferedImage sidebarLeftFill;
	/** Sidebar left top empty. */
	@Img(name = "colony/sidebar_left_top_empty")
	public BufferedImage sidebarLeftTopEntry;
	/** Sidebar right bottom. */
	@Img(name = "colony/sidebar_right_bottom")
	public BufferedImage sidebarRightBottom;
	/** Sidebar right fill. */
	@Img(name = "colony/sidebar_right_fill")
	public BufferedImage sidebarRightFill;
	/** The default filled tile. */
	@Img(name = "colony/tile_1x1")
	public BufferedImage tileFilled;
	/** The tile edge for selection. */
	@Img(name = "colony/tile_1x1_selected")
	public BufferedImage tileEdge;
	/** The tile crossed. */
	@Img(name = "colony/tile_1x1_selected_crossed")
	public BufferedImage tileCrossed;
	/** The unpowered animation. */
	@Anim(name = "colony/unpowered", width = 20)
	public BufferedImage[] unpowered;
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
	/** Constructing. */
	@Img(name = "colony/button_constructing")
	public BufferedImage constructing;
	/** Damaged. */
	@Img(name = "colony/button_damaged")
	public BufferedImage damaged;
	/** Demolish. */
	@Btn2(name = "colony/button_demolish")
	public BufferedImage[] demolish;
	/** List button. */
	@Btn2(name = "colony/button_list")
	public BufferedImage[] list;
	/** Repairing. */
	@Img(name = "colony/button_repairing")
	public BufferedImage repairing;
	/** Planets button. */
	@Btn2(name = "colony/button_planets")
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
	/** Undamaged. */
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
	/** Offline animation. */
	@Anim(name = "colony/off", step = 2)
	public BufferedImage[] off;
	/** Sidebar left top containing the buildings. */
	@Img(name = "colony/sidebar_left_top")
	public BufferedImage sidebarLeftTop;
	/** Sidebar right top containing the status info. */
	@Img(name = "colony/sidebar_right_top")
	public BufferedImage sidebarRightTop;
	/**
	 * Constructor.
	 * @param rl the resource locator
	 */
	public ColonyGFX(ResourceLocator rl) {
		this.rl = rl;
	}
	/**
	 * Load the resources for the given language.
	 * @param language the language
	 */
	public void load(String language) {
		GFXLoader.loadResources(this, rl, language);
	}
	/**
	 * Recolor a given default tile image.
	 * @param img the original image.
	 * @param newColor the new RGBA color.
	 * @return the new RGBA image
	 */
	public BufferedImage recolor(BufferedImage img, int newColor) {
		int[] pixels = new int[img.getWidth() * img.getHeight()];
		img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
		for (int i = 0; i < pixels.length; i++) {
			int c = pixels[i];
			if (c == 0xFF000000) {
				pixels[i] = newColor;
			}
		}
		BufferedImage result = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		result.setRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
		return result;
	}
}
