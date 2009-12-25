/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * The main menu rendering and actions.
 * @author karnokd, 2009.12.25.
 * @version $Revision 1.0$
 */
public class MainMenu extends ScreenBase {
	/** The screen index to display. */
	int screenIndex;
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
		// TODO Auto-generated method stub

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

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter() {
		// TODO Auto-generated method stub
		screenIndex = new Random().nextInt(commons.background.start.length);
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onLeave()
	 */
	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onResize()
	 */
	@Override
	public void onResize() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#paintTo(java.awt.Graphics2D)
	 */
	@Override
	public void paintTo(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, parent.getWidth(), parent.getHeight());
		BufferedImage back = commons.background.start[screenIndex];
		int x = (parent.getWidth() - back.getWidth()) / 2;
		int y = (parent.getHeight() - back.getHeight()) / 2;
		g2.drawImage(back, x, y, null);
	
		commons.text.paintTo(g2, x + 121, y + 21, 14, 0xFF000000, "Open");
		commons.text.paintTo(g2, x + 120, y + 20, 14, 0xFFFFFF00, "Open");
		commons.text.paintTo(g2, x + 501, y + 65, 14, 0xFF000000, Configuration.VERSION);
		commons.text.paintTo(g2, x + 500, y + 64, 14, 0xFFFF0000, Configuration.VERSION);
		
		Composite c0 = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.8f));
		
		g2.fillRoundRect(x + 60, y + 100, 640 - 120, 442 - 100 - 20, 40, 40);
		g2.setComposite(c0);
	
		commons.text.paintTo(g2, x + 120, y + 120, 20, 0xFFFFCC00, commons.labels.get("mainmenu.singleplayer"));
		commons.text.paintTo(g2, x + 140, y + 140, 14, 0xFFFFCC00, commons.labels.get("mainmenu.newcampaign"));
		commons.text.paintTo(g2, x + 140, y + 160, 14, 0xFFFFCC00, commons.labels.get("mainmenu.newskirmish"));
		commons.text.paintTo(g2, x + 140, y + 180, 14, 0xFFFFCC00, commons.labels.get("mainmenu.load"));
		commons.text.paintTo(g2, x + 120, y + 200, 14, 0xFFFFCC00, commons.labels.get("mainmenu.multiplayer"));
		commons.text.paintTo(g2, x + 120, y + 220, 14, 0xFFFFCC00, commons.labels.get("mainmenu.settings"));
		commons.text.paintTo(g2, x + 120, y + 240, 14, 0xFFFFCC00, commons.labels.get("mainmenu.videos"));
		commons.text.paintTo(g2, x + 140, y + 260, 14, 0xFFFFCC00, commons.labels.get("mainmenu.videos.intro"));
		commons.text.paintTo(g2, x + 140, y + 280, 14, 0xFFFFCC00, commons.labels.get("mainmenu.videos.title"));
		commons.text.paintTo(g2, x + 120, y + 300, 14, 0xFFFFCC00, commons.labels.get("mainmenu.exit"));
		
	}

}
