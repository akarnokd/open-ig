/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import hu.openig.model.Screens;
import hu.openig.render.RenderTools;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

/**
 * The trading management screen.
 * @author akarnokd, 2013.10.02.
 */
public class TradeScreen extends ScreenBase {
	/** The screen origin. */
	final Rectangle base = new Rectangle(0, 0, 620, 442);

	@Override
	public void onInitialize() {
		// TODO Auto-generated method stub

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
		scaleResize(base, margin());
		// TODO Auto-generated method stub

	}

	@Override
	public Screens screen() {
		return Screens.TRADE;
	}

	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub

	}
	@Override
	public void draw(Graphics2D g2) {
		AffineTransform savea = scaleDraw(g2, base, margin());
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);

		commons.info().drawInfoPanel(g2, base.x, base.y);

		super.draw(g2);
		
		g2.setTransform(savea);
	}
	@Override
	public boolean mouse(UIMouse e) {
		scaleMouse(e, base, margin());
		if (!base.contains(e.x, e.y) && e.has(Type.DOWN)) {
			hideSecondary();
			return true;
		}
		return super.mouse(e);
	}
}
