/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.gfx;

import hu.openig.utils.ImageUtils;
import hu.openig.v1.ResourceLocator;

import java.awt.image.BufferedImage;

/**
 * Statusbar graphics components.
 * @author karnok, 2009.11.09.
 * @version $Revision 1.0$
 */
public class StatusbarGFX {
	/** The resource locator. */
	protected ResourceLocator rl;
	/** Ingame top left. */
	public BufferedImage ingameTopLeft;
	/** Ingame top filler. */
	public BufferedImage ingameTopFill;
	/** Ingame top right. */
	public BufferedImage ingameTopRight;
	/** Ingame bottom left. */
	public BufferedImage ingameBottomLeft;
	/** Ingage bottom fill. */
	public BufferedImage ingameBottomFill;
	/** Ingame bottom right. */
	public BufferedImage ingameBottomRight;
	/** Nongame top left. */
	public BufferedImage nongameTopLeft;
	/** Nongame top fill. */
	public BufferedImage nongameTopFill;
	/** Nongame top right. */
	public BufferedImage nongameTopRight;
	/** Nongame bottom left. */
	public BufferedImage nongameBottomLeft;
	/** Nongame bottom fill. */
	public BufferedImage nongameBottomFill;
	/** Nongame bottom right. */
	public BufferedImage nongameBottomRight;
	/** Pause animation phases. */
	public BufferedImage[] pause;
	/** Time x 1 animation phases. */
	public BufferedImage[] timex1;
	/** Time x 2 animation phases. */
	public BufferedImage[] timex2;
	/** Time x 4 animation phases. */
	public BufferedImage[] timex4;
	/**
	 * Constructor.
	 * @param rl the resource locator to use
	 */
	public StatusbarGFX(ResourceLocator rl) {
		this.rl = rl;
	}
	/**
	 * Load the resources.
	 * @param language the target language
	 */
	public void load(String language) {
		ingameTopLeft = rl.getImage(language, "statusbar_ingame_top_left");
		ingameTopFill = rl.getImage(language, "statusbar_ingame_top_fill");
		ingameTopRight = rl.getImage(language, "statusbar_ingame_top_right");
		ingameBottomLeft = rl.getImage(language, "statusbar_ingame_bottom_left");
		ingameBottomFill = rl.getImage(language, "statusbar_ingame_bottom_fill");
		ingameBottomRight = rl.getImage(language, "statusbar_ingame_bottom_right");

		nongameTopLeft = rl.getImage(language, "statusbar_nongame_top_left");
		nongameTopFill = rl.getImage(language, "statusbar_nongame_top_fill");
		nongameTopRight = rl.getImage(language, "statusbar_nongame_top_right");
		nongameBottomLeft = rl.getImage(language, "statusbar_nongame_bottom_left");
		nongameBottomFill = rl.getImage(language, "statusbar_nongame_bottom_fill");
		nongameBottomRight = rl.getImage(language, "statusbar_nongame_bottom_right");
		
		BufferedImage time = rl.getImage(language, "time_animations");
		
		pause = new BufferedImage[] {
			ImageUtils.newSubimage(time, 42, 16, 14, 16),
			ImageUtils.newSubimage(time, 42, 32, 14, 16)
		};
		timex1 = new BufferedImage[] {
			ImageUtils.newSubimage(time, 0, 0 * 16, 14, 16),
			ImageUtils.newSubimage(time, 0, 1 * 16, 14, 16),
			ImageUtils.newSubimage(time, 0, 2 * 16, 14, 16),
			ImageUtils.newSubimage(time, 0, 3 * 16, 14, 16),
			ImageUtils.newSubimage(time, 0, 4 * 16, 14, 16),
			ImageUtils.newSubimage(time, 0, 5 * 16, 14, 16),
			ImageUtils.newSubimage(time, 0, 6 * 16, 14, 16)
		};
		timex2 = new BufferedImage[] {
			ImageUtils.newSubimage(time, 14, 0 * 16, 14, 16),
			ImageUtils.newSubimage(time, 14, 1 * 16, 14, 16),
			ImageUtils.newSubimage(time, 14, 2 * 16, 14, 16),
			ImageUtils.newSubimage(time, 14, 3 * 16, 14, 16),
			ImageUtils.newSubimage(time, 14, 4 * 16, 14, 16),
			ImageUtils.newSubimage(time, 14, 5 * 16, 14, 16),
			ImageUtils.newSubimage(time, 14, 6 * 16, 14, 16)
		};
		timex4 = new BufferedImage[] {
			ImageUtils.newSubimage(time, 28, 0 * 16, 14, 16),
			ImageUtils.newSubimage(time, 28, 1 * 16, 14, 16),
			ImageUtils.newSubimage(time, 28, 2 * 16, 14, 16),
			ImageUtils.newSubimage(time, 28, 3 * 16, 14, 16),
			ImageUtils.newSubimage(time, 28, 4 * 16, 14, 16),
			ImageUtils.newSubimage(time, 28, 5 * 16, 14, 16),
			ImageUtils.newSubimage(time, 28, 6 * 16, 14, 16)
		};
	}
}
