/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Act;
import hu.openig.model.Screens;
import hu.openig.screen.ScreenBase;

import java.io.Closeable;
import java.io.IOException;

/**
 * The credits.
 * @author akarnokd, 2011.04.20.
 */
public class CreditsScreen extends ScreenBase {
	/** The animation timer. */
	Closeable animation;
	/** The rendering offset relative to the bottom of the screen. */
	int offset;
	@Override
	public void onInitialize() {
	}

	@Override
	public void onEnter(Screens mode) {
		animation = commons.register(50, new Act() {
			@Override
			public void act() {
				doAnimation();
			}
		});
	}

	@Override
	public void onLeave() {
		if (animation != null) {
			try { animation.close(); } catch (IOException ex) { ex.printStackTrace(); }
			animation = null;
		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResize() {
	}

	@Override
	public Screens screen() {
		return Screens.TEST;
	}

	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub

	}
	/** Perform the animation. */
	void doAnimation() {
		// TODO
	}
}
