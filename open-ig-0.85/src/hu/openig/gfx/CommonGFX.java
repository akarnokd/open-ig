/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Btn2;
import hu.openig.core.GFXLoader;
import hu.openig.core.Img;
import hu.openig.core.ResourceLocator;
import hu.openig.render.GenericLargeButton;
import hu.openig.render.GenericMediumButton;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * The common graphics objects.
 * @author akarnokd
 */
public class CommonGFX {
	// --------------------------------------------
	// The general images usable by multiple places
	// --------------------------------------------
	/** The achievement icon. */
	@Img(name = "achievement")
	public BufferedImage achievement;
	/** The achievement icon grayed out. */
	public BufferedImage achievementGrayed;
	/** The empty background of the info panel. */
	@Img(name = "info/info_empty")
	public BufferedImage infoEmpty;
	/** Move up arrow. */
	@Btn2(name = "button_up")
	public BufferedImage[] moveUp;
	/** Move down arrow. */
	@Btn2(name = "button_down")
	public BufferedImage[] moveDown;
	/** Move left arrow. */
	@Btn2(name = "button_left")
	public BufferedImage[] moveLeft;
	/** Move right arrow. */
	@Btn2(name = "button_right")
	public BufferedImage[] moveRight;
	/** Energy icon. */
	@Img(name = "energy-icon")
	public BufferedImage energyIcon;
	/** Food icon. */
	@Img(name = "food-icon")
	public BufferedImage foodIcon;
	/** Worker icon. */
	@Img(name = "worker-icon")
	public BufferedImage workerIcon;
	/** Hospital icon. */
	@Img(name = "hospital-icon")
	public BufferedImage hospitalIcon;
	/** Housing icon. */
	@Img(name = "house-icon")
	public BufferedImage houseIcon;
	/** A 102x39 bridge button. */
	@Btn2(name = "button_bridge")
	public BufferedImage[] bridgeButton;
	/** A 102x39 info button. */
	@Btn2(name = "button_info")
	public BufferedImage[] infoButton;
	/** The disabled pattern. */
	public BufferedImage disabledPattern;
	/** The normal button renderer. */
	public GenericMediumButton mediumButton;
	/** The pressed button renderer. */
	public GenericMediumButton mediumButtonPressed;
	/** The normal button renderer. */
	public GenericLargeButton largeButton;
	/** The pressed button renderer. */
	public GenericLargeButton largeButtonPressed;
	/** An empty 102x39 button. */
	@Img(name = "button_empty_large")
	public BufferedImage emptyButton;
	/**
	 * Initialize the common resources.
	 * @param rl the resource locator
	 * @param language the target language
	 * @return this
	 */
	public CommonGFX load(ResourceLocator rl, String language) {
		int[] disabled = { 0xFF000000, 0xFF000000, 0, 0, 0xFF000000, 0, 0, 0, 0 };
		disabledPattern = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
		disabledPattern.setRGB(0, 0, 3, 3, disabled, 0, 3);
		GFXLoader.loadResources(this, rl, language);

		achievementGrayed = new BufferedImage(achievement.getWidth(), achievement.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = achievementGrayed.createGraphics();
		g2.drawImage(achievement, 0, 0, null);
		g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, achievement.getWidth(), achievement.getHeight());
		g2.dispose();

		mediumButton = new GenericMediumButton("/hu/openig/gfx/button_medium.png");
		mediumButtonPressed = new GenericMediumButton("/hu/openig/gfx/button_medium_pressed.png");
		largeButton = new GenericLargeButton("/hu/openig/gfx/button_large.png");
		largeButtonPressed = new GenericLargeButton("/hu/openig/gfx/button_large_pressed.png");
		
		return this;
	}
}
