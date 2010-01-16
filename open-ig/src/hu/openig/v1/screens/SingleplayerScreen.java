/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.screens;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;


/**
 * The single player screen with campaign selection.
 * @author karnokd, 2010.01.11.
 * @version $Revision 1.0$
 */
public class SingleplayerScreen extends ScreenBase {
	/** The reference frame. */
	final Rectangle origin = new Rectangle();
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#doResize()
	 */
	@Override
	public void doResize() {
		origin.setBounds((parent.getWidth() - 640) / 2, (parent.getHeight() - 480) / 2, 640, 480);
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#initialize()
	 */
	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#keyTyped(int, int)
	 */
	@Override
	public void keyTyped(int key, int modifiers) {
		if (key == KeyEvent.VK_ESCAPE) {
			commons.control.displayPrimary(commons.screens.mainmenu);
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseMoved(int, int, int, int)
	 */
	@Override
	public void mouseMoved(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mousePressed(int, int, int, int)
	 */
	@Override
	public void mousePressed(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseReleased(int, int, int, int)
	 */
	@Override
	public void mouseReleased(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseScrolled(int, int, int, int)
	 */
	@Override
	public void mouseScrolled(int direction, int x, int y, int modifiers) {
		// TODO Auto-generated method stub

	}
	/** The random used for background selection. */
	final Random rnd = new Random();
	/** The background image. */
	BufferedImage background;
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter() {
		background = commons.background.difficulty[rnd.nextInt(commons.background.difficulty.length)];
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onLeave()
	 */
	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#paintTo(java.awt.Graphics2D)
	 */
	@Override
	public void paintTo(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, parent.getWidth(), parent.getHeight());
		g2.drawImage(background, origin.x, origin.y, null);
	}

}
