/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.Timer;

/**
 * In progress loading screen.
 * @author akarnokd, 2010.01.17.
 */
public class LoadingScreen extends ScreenBase {
	/** The rotating cd icon location. */
	final Rectangle cd = new Rectangle();
	/** The text location. */
	final Rectangle text = new Rectangle();
	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#doResize()
	 */
	@Override
	public void doResize() {
	}
	/** The animation timer. */
	Timer animation;
	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#finish()
	 */
	@Override
	public void finish() {
		animation.stop();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#initialize()
	 */
	@Override
	public void initialize() {
		animation = new Timer(100, new Act() {
			@Override
			public void act() {
				doAnimate();
			}
		});
	}
	/** The rolling phase. */
	int rollingPhase;
	/** The number of dots. */
	int dots;
	/** Perform the next animation phase. */
	void doAnimate() {
		rollingPhase = (rollingPhase + 1) % commons.research.rolling.length;
		dots = (dots + 1) % 40;
		repaint();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#keyTyped(int, int)
	 */
	@Override
	public void keyTyped(int key, int modifiers) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#mouseDoubleClicked(int, int, int, int)
	 */
	@Override
	public void mouseDoubleClicked(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#mouseMoved(int, int, int, int)
	 */
	@Override
	public void mouseMoved(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#mousePressed(int, int, int, int)
	 */
	@Override
	public void mousePressed(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#mouseReleased(int, int, int, int)
	 */
	@Override
	public void mouseReleased(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#mouseScrolled(int, int, int, int)
	 */
	@Override
	public void mouseScrolled(int direction, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter() {
		doResize();
		animation.start();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#onLeave()
	 */
	@Override
	public void onLeave() {
		animation.stop();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#paintTo(java.awt.Graphics2D)
	 */
	@Override
	public void paintTo(Graphics2D g2) {
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.7f));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, parent.getWidth(), parent.getHeight());
		g2.setComposite(cp);
		
		cd.height = commons.research.rolling[0].getHeight() * 2;
		cd.width = commons.research.rolling[0].getWidth() * 2;
		cd.y = (parent.getHeight() - cd.height) / 2;
		text.height = 20;
		text.width = commons.text.getTextWidth(text.height, commons.labels.get("loading") + "...");
		text.y = (parent.getHeight() - text.height) / 2;
		int tw = cd.width + 10 + text.width;
		
		cd.x = (parent.getWidth() - tw) / 2;
		text.x = cd.x + cd.width + 10;
		
		g2.drawImage(commons.research.rolling[rollingPhase], cd.x, cd.y, cd.width, cd.height, null);
		
		StringBuilder sb = new StringBuilder(commons.labels.get("loading"));
		for (int i = 0; i < dots / 10; i++) {
			sb.append('.');
		}
		commons.text.paintTo(g2, text.x, text.y, text.height, 0xFFFFFF00, sb.toString());
	}

}
