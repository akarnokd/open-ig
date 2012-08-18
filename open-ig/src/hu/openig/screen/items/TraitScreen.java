/*
 * Copyright 2008-2012, David Karnok 
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

/**
 * The trait editing screen.
 * @author akarnokd, 2012.08.18.
 */
public class TraitScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle(0, 0, 640, 480);
	@Override
	public void onResize() {
		scaleResize(base, margin());
	}
	@Override
	public Screens screen() {
		return Screens.TRAITS;
	}

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
	public void onEndGame() {
		// TODO Auto-generated method stub

	}
	@Override
	public void draw(Graphics2D g2) {
		AffineTransform savea = scaleDraw(g2, base, margin());
		
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);

		super.draw(g2);
		
		g2.setTransform(savea);
		
	}
	@Override
	public boolean mouse(UIMouse e) {
		scaleMouse(e, base, margin());
		
		return super.mouse(e);
	}
}
