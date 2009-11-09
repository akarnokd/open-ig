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
 * The spacewar graphics objects.
 * @author karnok, 2009.11.09.
 * @version $Revision 1.0$
 */
public class SpacewarGFX implements ResourceSelfLoader {
	/** The resource locator. */
	protected ResourceLocator rl;
	/** The space background. */
	@Img(name = "spacewar/battle_background")
	public BufferedImage background;
	/** The camikaze status button. */
	@Btn3(name = "spacewar/button_kamikaze")
	public BufferedImage[] kamikaze;
	/** The animations for the movie button. */
	@Img(name = "spacewar/button_label_movie")
	public BufferedImage movieStrip;
	/** The movies static button. */
	@Img(name = "spacewar/button_label_stat_5")
	public BufferedImage movies;
	/** Ship animations. */
	@Anim(name = "spacewar/button_label_stat_1", width = 20)
	public BufferedImage[] ships;
	/** Statistics animations. */
	@Anim(name = "spacewar/button_label_stat_2", width = 22)
	public BufferedImage[] statistics;
	/** Fires animation. */
	@Anim(name = "spacewar/button_label_stat_3", width = 20)
	public BufferedImage[] fires;
	/** Computers animation. */
	@Anim(name = "spacewar/button_label_stat_4", width = 21)
	public BufferedImage[] computers;
	/** Layout OK. */
	public BufferedImage layoutOk;
	/** Pause button. */
	@Btn2(name = "spacewar/button_pause")
	public BufferedImage[] pause;
	/** The button for the various statistics images. */
	@Img(name = "spacewar/button_stat_empty")
	public BufferedImage stat;
	/** The stop button. */
	@Btn3(name = "spacewar/button_stop")
	public BufferedImage[] stop;
	/** Tall stop button. */
	@Btn2(name = "spacewar/button_stop_tall")
	public BufferedImage[] stopTall;
	/** Zoom button. */
	@Btn2(name = "spacewar/button_zoom")
	public BufferedImage[] zoom;
	/** Small explosion animation. */
	@Anim(name = "spacewar/explosion_1", width = 20)
	public BufferedImage[] explosionSmall;
	/** Medium explosion animation. */
	@Anim(name = "spacewar/explosion_2", width = 30)
	public BufferedImage[] explosionMedium;
	/** Large explosion animation. */
	@Anim(name = "spacewar/explosion_3", width = 60)
	public BufferedImage[] explosionLarge;
	/** Tiny explosion animation. */
	@Anim(name = "spacewar/explosion_hit", width = 9)
	public BufferedImage[] explosionTiny;
	/** Green explosion. */
	@Anim(name = "spacewar/explosion_4", width = 40)
	public BufferedImage[] explosionGreen;
	/** The planet. */
	@Img(name = "spacewar/planet")
	public BufferedImage planet;
	/** The planet flipped. */
	@Img(name = "spacewar/planet_flipped")
	public BufferedImage planetFlipped;
	/** The landing zone animation. */
	@Anim(name = "spacewar/landing_zone", width = 76)
	public BufferedImage[] landingZone;
	/** Frame left filler. */
	@Img(name = "spacewar/frame_left_fill")
	public BufferedImage frameLeftFill;
	/** Frame right. */
	@Img(name = "spacewar/frame_right")
	public BufferedImage frameRight;
	/** Frame top filler. */
	@Img(name = "spacewar/frame_top_fill")
	public BufferedImage frameTopFill;
	/** Frame top left. */
	@Img(name = "spacewar/frame_top_left")
	public BufferedImage frameTopLeft;
	/** Frame top right. */
	@Img(name = "spacewar/frame_top_right")
	public BufferedImage frameTopRight;
	/** Panel with the IMP System. */
	@Img(name = "spacewar/panel_background_ig")
	public BufferedImage panelIg;
	/** Panel width star background. */
	@Img(name = "spacewar/panel_background_space")
	public BufferedImage panelStar;
	/** The commands panel. */
	@Img(name = "spacewar/panel_commands_left")
	public BufferedImage commands;
	/** Statistics panel filler. */
	@Img(name = "spacewar/panel_stat_fill")
	public BufferedImage panelStatFill;
	/** Panel statistics left. */
	@Img(name = "spacewar/panel_stat_left")
	public BufferedImage panelStatLeft;
	/** Panel statistics right. */
	@Img(name = "spacewar/panel_stat_right")
	public BufferedImage panelStatRight;
	/** Attack button. */
	@Btn3(name = "spacewar/button_attack")
	public BufferedImage[] attack;
	/** Command button. */
	@Btn3(name = "spacewar/button_command")
	public BufferedImage[] command;
	/** Damage button. */
	@Btn3(name = "spacewar/button_damage")
	public BufferedImage[] damage;
	/** Firerange button. */
	@Btn3(name = "spacewar/button_fire_range")
	public BufferedImage[] fireRange;
	/** Grid button. */
	@Btn3(name = "spacewar/button_grid")
	public BufferedImage[] grid;
	/** Guard button. */
	@Btn3(name = "spacewar/button_guard")
	public BufferedImage[] guard;
	/** Move button. */
	@Btn3(name = "spacewar/button_move")
	public BufferedImage[] move;
	/** Rocket button. */
	@Btn3(name = "spacewar/button_rocket")
	public BufferedImage[] rocket;
	/** Toggle layout. */
	@Img(name = "spacewar/button_layout_toggle")
	public BufferedImage layoutToggle;
	/** Retreat button. */
	@Btn2(name = "spacewar/button_retreat")
	public BufferedImage[] retreat;
	/** Sure button. */
	@Btn2(name = "spacewar/button_sure")
	public BufferedImage[] sure;
	/**
	 * Constructor.
	 * @param rl the resource locator
	 */
	public SpacewarGFX(ResourceLocator rl) {
		this.rl = rl;
	}
	/**
	 * Load resources.
	 * @param language the target language
	 */
	public void load(String language) {
		GFXLoader.loadResources(this, rl, language);
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.gfx.ResourceSelfLoader#load(hu.openig.v1.ResourceLocator, java.lang.String)
	 */
	@Override
	public void load(ResourceLocator rl, String language) {
	}
}
