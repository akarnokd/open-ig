/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.res.gfx;

import hu.openig.utils.PCXImage;
import hu.openig.utils.ResourceMapper;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * The graphics for the startup menu and options screen.
 * @author karnokd, 2009.02.08.
 * @version $Revision 1.0$
 */
public class MenuGFX {
	/** The startup menu images. */
	public BufferedImage[] startImages;
	/** The difficulty images. */
	public BufferedImage[] difficultyImages;
	/** Rectangle for normal difficulty. */
	public Rectangle rectNormal;
	/** Rectangle for hard difficulty. */
	public Rectangle rectHard;
	/**
	 * Constructor. Loads the menu related images.
	 * @param resMap the resource mapper
	 */
	public MenuGFX(ResourceMapper resMap) {
		startImages = new BufferedImage[3];
		for (int i = 0; i < startImages.length; i++) {
			startImages[i] = PCXImage.from(resMap.get("SCREENS/START" + (i + 1) + ".PCX"), -1);
		}
		// relative positions of the title options
		difficultyImages = new BufferedImage[2];
		for (int i = 0; i < difficultyImages.length; i++) {
			difficultyImages[i] = PCXImage.from(resMap.get("SCREENS/DIFF" + (i + 1) + ".PCX"), -1);
		}
		rectNormal = new Rectangle(50, 172, 540, 40);
		rectHard = new Rectangle(50, 228, 540, 40);
	}
}
