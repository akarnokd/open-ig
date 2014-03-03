/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Img;
import hu.openig.model.ResourceLocator;

import java.awt.image.BufferedImage;

/**
 * The diplomacy graphics objects.
 * @author akarnokd
 */
public class DiplomacyGFX {
	/** The diplomacy base screen. */
	@Img(name = "flagship/diplomacy_normal")
	public BufferedImage base;
	/** The diplomacy showing a hologram. */
	@Img(name = "flagship/diplomacy_active")
	public BufferedImage active;
	/** The diplomacy with deployed monitor. */
	@Img(name = "flagship/diplomacy_panel_level_4")
	public BufferedImage monitor;
	/**
	 * Initialize the common resources.
	 * @param rl the resource locator
	 * @return this
	 */
	public DiplomacyGFX load(ResourceLocator rl) {
		GFXLoader.loadResources(this, rl);
		return this;
	}
}
