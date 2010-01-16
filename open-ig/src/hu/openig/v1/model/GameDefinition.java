/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.model;

import java.awt.image.BufferedImage;

/**
 * The game definition used on the single player screen.
 * @author karnok, 2010.01.16.
 * @version $Revision 1.0$
 */
public class GameDefinition {
	/**
	 * The teaser image to display.
	 */
	public BufferedImage image;
	/** The intro media to play on start. */
	public String intro;
	/** The title text. */
	public String title;
	/** The campaign description. */
	public String description;
	/** The game name. */
	public String name;
}
