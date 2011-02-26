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
import hu.openig.core.ResourceSelfLoader;

import java.awt.image.BufferedImage;

/**
 * The background images for various screens,
 * including menu, options and battle end screens.
 * @author akarnokd, 2009.11.09.
 */
public class BackgroundGFX implements ResourceSelfLoader {
	/** The resource locator. */
	protected ResourceLocator rl;
	/** The start backgrounds. */
	public BufferedImage[] start;
	/** The options backgrounds. */
	public BufferedImage[] options;
	/** The difficulty backgrounds. */
	public BufferedImage[] difficulty;
	/** The test screen background. */
	@Img(name = "phsychologist_test")
	public BufferedImage test;
	/** The setup screen background. */
	@Img(name = "setup")
	public BufferedImage setup;
	/** The gameover image. */
	@Img(name = "gameover")
	public BufferedImage gameover;
	/**
	 * Constructor.
	 * @param rl the resource locator.
	 */
	public BackgroundGFX(ResourceLocator rl) {
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
		start = new BufferedImage[] {
			rl.getImage(language, "start_1"),
			rl.getImage(language, "start_2"),
			rl.getImage(language, "start_3")
		};
		options = new BufferedImage[] {
			rl.getImage(language, "options_1"),
			rl.getImage(language, "options_2"),
			rl.getImage(language, "options_3")
		};
		difficulty = new BufferedImage[] {
			rl.getImage(language, "difficulty_1"),
			rl.getImage(language, "difficulty_2")
		};
	}
}
