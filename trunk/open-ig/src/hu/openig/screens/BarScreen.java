/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.render.RenderTools;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Graphics2D;
import java.awt.Rectangle;



/**
 * The bar screen.
 * @author akarnokd, 2010.01.11.
 */
public class BarScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();

	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				640, 480 - 38);
		
	}

	@Override
	public void onEnter(Object mode) {
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
		RenderTools.centerScreen(base, width, height, true);
		
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		
		super.draw(g2);
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (!base.contains(e.x, e.y) && e.has(Type.UP)) {
			commons.control.hideSecondary();
			return true;
		} else {
			return super.mouse(e);
		}
	}

}
