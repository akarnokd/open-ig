/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.model.Screens;
import hu.openig.render.RenderTools;
import hu.openig.screen.ScreenBase;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * The screen displaying the Phsychologist test.
 * @author akarnokd, 2011.04.20.
 */
public class TestScreen extends ScreenBase {
	/** The base rectangle. */
	Rectangle base = new Rectangle();
	@Override
	public void onInitialize() {
		base.setSize(commons.background().test.getWidth(), commons.background().test.getHeight());
	}

	@Override
	public void onEnter(Screens mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResize() {
		RenderTools.centerScreen(base, getInnerWidth(), getInnerHeight(), true);
	}

	@Override
	public Screens screen() {
		return Screens.TEST;
	}

	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub

	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.background().test, base.x, base.y, null);
	}
}
