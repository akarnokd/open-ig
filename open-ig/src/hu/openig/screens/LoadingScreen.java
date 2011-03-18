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
	public void onResize() {
	}
	/** The animation timer. */
	Timer animation;
	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#finish()
	 */
	@Override
	public void onFinish() {
		animation.stop();
	}

	@Override
	public void onInitialize() {
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
		rollingPhase = (rollingPhase + 1) % commons.research().rolling.length;
		dots = (dots + 1) % 40;
		askRepaint();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter(Object mode) {
		resize();
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
	public void draw(Graphics2D g2) {
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.7f));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, commons.control.getInnerWidth(), commons.control.getInnerHeight());
		g2.setComposite(cp);
		
		cd.height = commons.research().rolling[0].getHeight() * 2;
		cd.width = commons.research().rolling[0].getWidth() * 2;
		cd.y = (commons.control.getInnerHeight() - cd.height) / 2;
		text.height = 20;
		text.width = commons.text().getTextWidth(text.height, commons.labels().get("loading") + "...");
		text.y = (commons.control.getInnerHeight() - text.height) / 2;
		int tw = cd.width + 10 + text.width;
		
		cd.x = (commons.control.getInnerWidth() - tw) / 2;
		text.x = cd.x + cd.width + 10;
		
		g2.drawImage(commons.research().rolling[rollingPhase], cd.x, cd.y, cd.width, cd.height, null);
		
		StringBuilder sb = new StringBuilder(commons.labels().get("loading"));
		for (int i = 0; i < dots / 10; i++) {
			sb.append('.');
		}
		commons.text().paintTo(g2, text.x, text.y, text.height, 0xFFFFFF00, sb.toString());
	}

}
