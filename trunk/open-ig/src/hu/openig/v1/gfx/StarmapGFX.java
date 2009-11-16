/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.gfx;

import hu.openig.v1.ResourceLocator;

import java.awt.image.BufferedImage;

/**
 * The starmap graphics components.
 * @author karnok, 2009.11.16.
 * @version $Revision 1.0$
 */
public class StarmapGFX {
	/** The starmap background. */
	@Img(name = "starmap/background")
	public BufferedImage background;
	/** Zoom normal button. */
	@Cat(name = "starmap/button_sz_normal")
	public BufferedImage[] zoomNormal;
	/** Zoom zoom button. */
	@Cat(name = "starmap/button_sz_zoom")
	public BufferedImage[] zoomZoom;
	/** Zoom scroll button. */
	@Cat(name = "starmap/button_sz_scroll")
	public BufferedImage[] zoomScroll;
	/** Zoom button. */
	@Cat(name = "starmap/button_zoom")
	public BufferedImage[] zoom;
	/** Panel buttons. */
	@Img(name = "starmap/panel_buttons")
	public BufferedImage buttonsPanel;
	/** Commands separator. */
	@Img(name = "starmap/panel_commands_separator")
	public BufferedImage commandSeparator;
	/** Fleets bottom part. */
	@Img(name = "starmap/panel_fleets_bottom")
	public BufferedImage fleetsBottom;
	/** Fleets filler part. */
	@Img(name = "starmap/panel_fleets_fill")
	public BufferedImage fleetsFill;
	/** Fleets top part. */
	@Img(name = "starmap/panel_fleets_top")
	public BufferedImage fleetsTop;
	/** Info panel filler. */
	@Img(name = "starmap/panel_info_fill")
	public BufferedImage infoFill;
	/** Info panel left. */
	@Img(name = "starmap/panel_info_left")
	public BufferedImage infoLeft;
	/** Info panel right. */
	@Img(name = "starmap/panel_info_right")
	public BufferedImage infoRight;
	/** Minimap panel. */
	@Img(name = "starmap/panel_minimap")
	public BufferedImage minimap;
	/** Planets panel bottom. */
	@Img(name = "starmap/panel_planets_bottom")
	public BufferedImage planetsBottom;
	/** Planets panel filler. */
	@Img(name = "starmap/panel_planets_fill")
	public BufferedImage planetsFill;
	/** Planets panel top. */
	@Img(name = "starmap/panel_planets_top")
	public BufferedImage planetsTop;
	/** Zoom panel. */
	@Img(name = "starmap/panel_sz")
	public BufferedImage zoomPanel;
	/** Radar dots. */
	@Anim(name = "starmap/radar_dots_3x3x4", width = 3)
	public BufferedImage[] radarDots;
	/** Scrollbar horizontal filler. */
	@Img(name = "starmap/scrollbar_horizontal_fill")
	public BufferedImage hScrollFill;
	/** Scrollbar horizontal left. */
	@Img(name = "starmap/scrollbar_horizontal_left")
	public BufferedImage hScrollLeft;
	/** Scrollbar horizontal right. */
	@Img(name = "starmap/scrollbar_horizontal_right")
	public BufferedImage hScrollRight;
	/** Scrollbar vertical fill. */
	@Img(name = "starmap/scrollbar_vertical_fill")
	public BufferedImage vScrollFill;
	/** Scrollbar vertical top. */
	@Img(name = "starmap/scrollbar_vertical_top")
	public BufferedImage vScrollBottom;
	/** Scrollbar vertical bottom. */
	@Img(name = "starmap/scrollbar_vertical_bottom")
	public BufferedImage vScrollTop;
	/** Scrollknob horizontal filler. */
	@Img(name = "starmap/scrollknob_horizontal_fill")
	public BufferedImage hKnobFill;
	/** Scrollknob horizontal left. */
	@Img(name = "starmap/scrollknob_horizontal_left")
	public BufferedImage hKnobLeft;
	/** Scrollknob horizontal right. */
	@Img(name = "starmap/scrollknob_horizontal_right")
	public BufferedImage hKnobRight;
	/** Scrollknob vertical fill. */
	@Img(name = "starmap/scrollknob_vertical_fill")
	public BufferedImage vKnobFill;
	/** Scrollknob vertical top. */
	@Img(name = "starmap/scrollknob_vertical_top")
	public BufferedImage vKnobBottom;
	/** Scrollknob vertical bottom. */
	@Img(name = "starmap/scrollknob_vertical_bottom")
	public BufferedImage vKnobTop;
	/** Attack. */
	@Cat(name = "starmap/button_attack")
	public BufferedImage[] attack;
	/** Backwards. */
	@Btn2(name = "starmap/button_backwards")
	public BufferedImage[] backwards;
	/** Bridge button. */
	@Btn2(name = "starmap/button_bridge")
	public BufferedImage[] bridge;
	/** Colonize button. */
	@Img(name = "starmap/button_colonize")
	public BufferedImage colonize;
	/** Colony button. */
	@Btn2(name = "starmap/button_colony")
	public BufferedImage[] colony;
	/** Deploy hubble. */
	@Img(name = "starmap/button_deploy_hubble")
	public BufferedImage deployHubble;
	/** Deploy satellite. */
	@Img(name = "starmap/button_deploy_satellite")
	public BufferedImage deploySatellite;
	/** Deploy spy satellite 1. */
	@Img(name = "starmap/button_deploy_spy_satellite_1")
	public BufferedImage deploySpySat1;
	/** Deploy spy satellite 2. */
	@Img(name = "starmap/button_deploy_spy_satellite_2")
	public BufferedImage deploySpySat2;
	/** Equipment button. */
	@Btn2(name = "starmap/button_equipment")
	public BufferedImage[] equipment;
	/** Forwards button. */
	@Btn2(name = "starmap/button_forwards")
	public BufferedImage[] forwards;
	/** Info button. */
	@Btn2(name = "starmap/button_info")
	public BufferedImage[] info;
	/** Move button. */
	@Cat(name = "starmap/button_move")
	public BufferedImage[] move;
	/** Names both. */
	@Img(name = "starmap/button_names_both")
	public BufferedImage namesBoth;
	/** Names fleets. */
	@Img(name = "starmap/button_names_fleets")
	public BufferedImage namesFleets;
	/** Names none. */
	@Img(name = "starmap/button_names_none")
	public BufferedImage namesNone;
	/** Names planets. */
	@Img(name = "starmap/button_names_planets")
	public BufferedImage namesPlanets;
	/** Stop button. */
	@Cat(name = "starmap/button_stop")
	public BufferedImage[] stop;
	/** View border. */
	@Cat(name = "starmap/button_view_border")
	public BufferedImage[] viewBorder;
	/** View fleet. */
	@Cat(name = "starmap/button_view_fleet")
	public BufferedImage[] viewFleet;
	/** View radar. */
	@Cat(name = "starmap/button_view_radar")
	public BufferedImage[] viewRadar;
	/** View sector. */
	@Cat(name = "starmap/button_view_sector")
	public BufferedImage[] viewSector;
	/** View star. */
	@Cat(name = "starmap/button_view_star")
	public BufferedImage[] viewStar;
	/** View target. */
	@Cat(name = "starmap/button_view_target")
	public BufferedImage[] viewTarget;
	/**
	 * Load the resources from the given locator and language.
	 * @param rl the resource locator
	 * @param language the language
	 */
	public void load(ResourceLocator rl, String language) {
		GFXLoader.loadResources(this, rl, language);
	}
}
