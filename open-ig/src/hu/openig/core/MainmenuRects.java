/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.awt.Rectangle;

/**
 * Record for main menu rectangles.
 * @author karnokd, 2009.02.08.
 * @version $Revision 1.0$
 */
public class MainmenuRects {
	/** The background rectangle. */
	public Rectangle background = new Rectangle();
	/** Rectangle for start new game. */
	public Rectangle rectStartNewGame = new Rectangle();
	/** Rectangle for load game. */
	public Rectangle rectLoadGame = new Rectangle();
	/** Rectangle for title animation. */
	public Rectangle rectTitleAnimation = new Rectangle();
	/** Rectangle for view intro. */
	public Rectangle rectViewIntro = new Rectangle();
	/** Rectangle for quit. */
	public Rectangle rectQuit = new Rectangle();
	/** Rectangles as array. */
	public Rectangle[] rectMenus = {
		rectStartNewGame, rectLoadGame, rectTitleAnimation, rectViewIntro, rectQuit
	};
}
