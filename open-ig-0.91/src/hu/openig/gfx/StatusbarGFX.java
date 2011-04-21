/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.GFXLoader;
import hu.openig.core.Img;
import hu.openig.core.ResourceLocator;
import hu.openig.utils.ImageUtils;

import java.awt.image.BufferedImage;

/**
 * Statusbar graphics components.
 * @author akarnokd, 2009.11.09.
 */
public class StatusbarGFX {
	/** Ingame top left. */
	@Img(name = "statusbar_ingame_top_left")
	public BufferedImage ingameTopLeft;
	/** Ingame top filler. */
	@Img(name = "statusbar_ingame_top_fill")
	public BufferedImage ingameTopFill;
	/** Ingame top right. */
	@Img(name = "statusbar_ingame_top_right")
	public BufferedImage ingameTopRight;
	/** Ingame bottom left. */
	@Img(name = "statusbar_ingame_bottom_left")
	public BufferedImage ingameBottomLeft;
	/** Ingage bottom fill. */
	@Img(name = "statusbar_ingame_bottom_fill")
	public BufferedImage ingameBottomFill;
	/** Ingame bottom right. */
	@Img(name = "statusbar_ingame_bottom_right")
	public BufferedImage ingameBottomRight;
	/** Nongame top left. */
	@Img(name = "statusbar_nongame_top_left")
	public BufferedImage nongameTopLeft;
	/** Nongame top fill. */
	@Img(name = "statusbar_nongame_top_fill")
	public BufferedImage nongameTopFill;
	/** Nongame top right. */
	@Img(name = "statusbar_nongame_top_right")
	public BufferedImage nongameTopRight;
	/** Nongame bottom left. */
	@Img(name = "statusbar_nongame_bottom_left")
	public BufferedImage nongameBottomLeft;
	/** Nongame bottom fill. */
	@Img(name = "statusbar_nongame_bottom_fill")
	public BufferedImage nongameBottomFill;
	/** Nongame bottom right. */
	@Img(name = "statusbar_nongame_bottom_right")
	public BufferedImage nongameBottomRight;
	/** Pause animation phases. */
	public BufferedImage[] pause;
	/** Time x 1 animation phases. */
	public BufferedImage[] timex1;
	/** Time x 2 animation phases. */
	public BufferedImage[] timex2;
	/** Time x 4 animation phases. */
	public BufferedImage[] timex4;
	/** Research notification icon. */
	@Img(name = "research_notify")
	public BufferedImage researchNotify;
	/** Production notification icon. */
	@Img(name = "production_notify")
	public BufferedImage productionNotify;
	/** Money notification icon. */
	@Img(name = "money_notify")
	public BufferedImage moneyNotification;
	/**
	 * Load the resources.
	 * @param rl the resource locator
	 * @return this;
	 */
	public StatusbarGFX load(ResourceLocator rl) {
		GFXLoader.loadResources(this, rl);
		BufferedImage time = rl.getImage("time_animations");
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
		return this;
	}
}
