/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.ui.UIImageFill;

import javax.swing.Timer;



/**
 * Displays and handles the status bar screen.
 * @author akarnokd, 2010.01.11.
 */
public class StatusbarScreen extends ScreenBase {
	/** The top bar. */
	UIImageFill top;
	/** The bottom bar. */
	UIImageFill bottom;
	/** The animation timer to show the status bar. */
	Timer animation;
	@Override
	public void onInitialize() {
		top = new UIImageFill(
				commons.statusbar().ingameTopLeft, 
				commons.statusbar().ingameTopFill,
				commons.statusbar().ingameTopRight, true);
		top.z = -1;
		bottom = new UIImageFill(
				commons.statusbar().ingameBottomLeft, 
				commons.statusbar().ingameBottomFill,
				commons.statusbar().ingameBottomRight, true);
		bottom.z = -1;
		
		animation = new Timer(50, new Act() {
			@Override
			public void act() {
				boolean s = true;
				if (top.y < 0) {
					top.y += 2;
					s = false;
				}
				if (bottom.y > height - 18) {
					bottom.y -= 2;
					s = false;
				}
				askRepaint();
				if (s) {
					animation.stop();
				}
			}
		});
		
		addThis();
	}

	@Override
	public void onEnter(Screens mode) {
		top.bounds(0, -20, width, 20);
		bottom.bounds(0, height, width, 18);
		animation.start();
	}

	@Override
	public void onLeave() {
		animation.stop();
	}

	@Override
	public void onFinish() {
		animation = null;
	}

	@Override
	public void onResize() {
		top.size(width, 20);
		bottom.size(width, 18);
		if (!animation.isRunning()) {
			bottom.location(0, height - 18);
		}
	}
	
	@Override
	public Screens screen() {
		return Screens.STATUSBAR;
	}
}
