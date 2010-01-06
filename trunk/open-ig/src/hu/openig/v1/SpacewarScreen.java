/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;

/**
 * The spacewar screen.
 * @author karnokd, 2010.01.06.
 * @version $Revision 1.0$
 */
public class SpacewarScreen extends ScreenBase {

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
		g2.drawImage(commons.spacewar.frameTopLeft, 0, 20, null);
		
		g2.drawImage(commons.spacewar.frameTopRight, parent.getWidth() - commons.spacewar.frameTopRight.getWidth(), 20, null);

		g2.drawImage(commons.spacewar.commands, 0, 20 + commons.spacewar.frameTopLeft.getHeight(), null);
		g2.drawImage(commons.spacewar.frameRight, parent.getWidth() - commons.spacewar.frameRight.getWidth(), 20 + commons.spacewar.frameTopRight.getHeight(), null);
		
		g2.drawImage(commons.spacewar.panelStatLeft, 0, parent.getHeight() - commons.spacewar.panelStatLeft.getHeight() - 18, null);
		
		g2.drawImage(commons.spacewar.panelStatRight, parent.getWidth() - commons.spacewar.panelStatRight.getWidth(), parent.getHeight() - commons.spacewar.panelStatRight.getHeight() - 18, null);

		Paint p = g2.getPaint();

		TexturePaint tp = new TexturePaint(commons.spacewar.frameTopFill, new Rectangle(commons.spacewar.frameTopLeft.getWidth(), 20, 1, commons.spacewar.frameTopFill.getHeight()));
		g2.setPaint(tp);
		g2.fillRect(commons.spacewar.frameTopLeft.getWidth(), 20, parent.getWidth() - commons.spacewar.frameTopLeft.getWidth() - commons.spacewar.frameTopRight.getWidth(), commons.spacewar.frameTopFill.getHeight());
		
		tp = new TexturePaint(commons.spacewar.panelStatFill, new Rectangle(commons.spacewar.panelStatLeft.getWidth(), parent.getHeight() - commons.spacewar.panelStatLeft.getHeight() - 18, 1, commons.spacewar.panelStatFill.getHeight()));
		g2.setPaint(tp);
		g2.fillRect(commons.spacewar.panelStatLeft.getWidth(), parent.getHeight() - commons.spacewar.panelStatLeft.getHeight() - 18, parent.getWidth() - commons.spacewar.frameTopRight.getWidth() - commons.spacewar.frameTopLeft.getWidth(), commons.spacewar.panelStatFill.getHeight());
		
		tp = new TexturePaint(commons.spacewar.frameRightFill, new Rectangle(parent.getWidth() - commons.spacewar.frameRight.getWidth(), 20 + commons.spacewar.frameTopRight.getHeight() + commons.spacewar.frameRight.getHeight(), commons.spacewar.frameRightFill.getWidth(), commons.spacewar.frameRightFill.getHeight()));
		g2.setPaint(tp);
		g2.fillRect(parent.getWidth() - commons.spacewar.frameRight.getWidth(), 20 + commons.spacewar.frameTopRight.getHeight() + commons.spacewar.frameRight.getHeight(), commons.spacewar.frameRightFill.getWidth(), parent.getHeight() - 38 - commons.spacewar.frameTopRight.getHeight() - commons.spacewar.frameRight.getHeight() - commons.spacewar.panelStatRight.getHeight());
		
		tp = new TexturePaint(commons.spacewar.frameLeftFill, new Rectangle(0, 20 + commons.spacewar.frameTopLeft.getHeight() + commons.spacewar.commands.getHeight(), commons.spacewar.frameLeftFill.getWidth(), commons.spacewar.frameLeftFill.getHeight()));
		g2.setPaint(tp);
		g2.fillRect(0, 20 + commons.spacewar.frameTopLeft.getHeight() + commons.spacewar.commands.getHeight(), commons.spacewar.frameLeftFill.getWidth(), 
				parent.getHeight() - 36 - commons.spacewar.frameTopLeft.getHeight() - commons.spacewar.commands.getHeight() - commons.spacewar.panelStatLeft.getHeight());
		
		g2.setPaint(p);
	}

}
