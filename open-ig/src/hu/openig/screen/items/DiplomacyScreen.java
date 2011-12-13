/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.model.Screens;
import hu.openig.model.WalkPosition;
import hu.openig.model.WalkTransition;
import hu.openig.render.RenderTools;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Graphics2D;
import java.awt.Rectangle;



/**
 * The diplomacy screen.
 * @author akarnokd, 2010.01.11.
 */
public class DiplomacyScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();
	/** The transition the mouse is pointing at. */
	WalkTransition pointerTransition;

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
			if (e.has(Type.MOVE) || e.has(Type.DRAG)) {
				WalkTransition prev = pointerTransition;
				pointerTransition = null;
				WalkPosition position = ScreenUtils.getWalk("*diplomacy", world());
				for (WalkTransition wt : position.transitions) {
					if (wt.area.contains(e.x - base.x, e.y - base.y)) {
						pointerTransition = wt;
						break;
					}
				}
				if (prev != pointerTransition) {
					askRepaint();
				}
				return false;
			} else
			if (e.has(Type.DOWN)) {
				WalkPosition position = ScreenUtils.getWalk("*diplomacy", world());
				for (WalkTransition wt : position.transitions) {
					if (wt.area.contains(e.x - base.x, e.y - base.y)) {
						ScreenUtils.doTransition(position, wt, commons);
						break;
					}
				}
				return false;
			}
			return super.mouse(e);
		}
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.diplomacy().base, base.x, base.y, null);

		if (pointerTransition != null) {
			ScreenUtils.drawTransitionLabel(g2, pointerTransition, base, commons);
		}

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
