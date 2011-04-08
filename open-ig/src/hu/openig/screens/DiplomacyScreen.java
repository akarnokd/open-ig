/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import hu.openig.model.Screens;
import hu.openig.render.RenderTools;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;



/**
 * The diplomacy screen.
 * @author akarnokd, 2010.01.11.
 */
public class DiplomacyScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();

	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				commons.diplomacy().base.getWidth(), commons.diplomacy().base.getHeight());
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
		RenderTools.centerScreen(base, width, height, true);
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (!base.contains(e.x, e.y) && e.has(Type.UP)) {
			hideSecondary();
			return true;
		} else {
			return super.mouse(e);
		}
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.diplomacy().base, base.x, base.y, null);
		
		super.draw(g2);
	}
	@Override
	public Screens screen() {
		return Screens.DIPLOMACY;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
}
