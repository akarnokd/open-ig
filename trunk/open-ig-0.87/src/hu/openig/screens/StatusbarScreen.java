/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.render.TextRenderer;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIImageFill;
import hu.openig.ui.UIImageTabButton2;
import hu.openig.ui.UILabel;

import java.awt.Graphics2D;
import java.util.GregorianCalendar;
import java.util.Locale;

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
	/** The money label. */
	UILabel money;
	/** The animation timer to show the status bar. */
	Timer animation;
	/** The helper variable for the bottom animation. */
	int bottomY;
	/** Pause the game. */
	UIImageTabButton2 pause;
	/** Set to normal speed. */
	UIImageTabButton2 speed1;
	/** Set to double speed. */
	UIImageTabButton2 speed2;
	/** Set to 4x speed. */
	UIImageTabButton2 speed4;
	/** The year. */
	UILabel year;
	/** The month. */
	UILabel month;
	/** The day. */
	UILabel day;
	/** The time. */
	UILabel time;
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
				if (bottomY < 18) {
					bottomY += 2;
					s = false;
				}
				askRepaint();
				if (s) {
					animation.stop();
				}
			}
		});
		
		money = new UILabel("", 10, commons.text());
		money.horizontally(HorizontalAlignment.CENTER);
		money.color(TextRenderer.YELLOW);

		year = new UILabel("", 10, commons.text()).horizontally(HorizontalAlignment.CENTER).color(TextRenderer.YELLOW);
		month = new UILabel("", 10, commons.text()).horizontally(HorizontalAlignment.CENTER).color(TextRenderer.YELLOW);
		day = new UILabel("", 10, commons.text()).horizontally(HorizontalAlignment.CENTER).color(TextRenderer.YELLOW);
		time = new UILabel("", 10, commons.text()).horizontally(HorizontalAlignment.CENTER).color(TextRenderer.YELLOW);
		
		pause = new UIImageTabButton2(commons.common().pause);
		pause.onPress = new Act() {
			@Override
			public void act() {
				if (world().simulator.isRunning()) {
					world().simulator.stop();
				} else {
					world().simulator.start();
				}
			}
		};
		speed1 = new UIImageTabButton2(commons.common().speed1);
		speed1.onPress = new Act() {
			@Override
			public void act() {
				world().simulator.setDelay(1000);
				world().simulator.start();
			}
		};
		speed2 = new UIImageTabButton2(commons.common().speed2);
		speed2.onPress = new Act() {
			@Override
			public void act() {
				world().simulator.setDelay(500);
				world().simulator.start();
			}
		};
		speed4 = new UIImageTabButton2(commons.common().speed4);
		speed4.onPress = new Act() {
			@Override
			public void act() {
				world().simulator.setDelay(250);
				world().simulator.start();
			}
		};
		
		
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
	}
	
	@Override
	public Screens screen() {
		return Screens.STATUSBAR;
	}
	@Override
	public void draw(Graphics2D g2) {
		bottom.y = height - bottomY;
		update();
		super.draw(g2);
	}
	/** Update the state displays. */
	public void update() {
		money.bounds(top.x + 75, top.y + 3, 66, 14);
		money.text("" + player().money);
		pause.location(top.x + 1, top.y + 2);
		speed1.location(top.x + 16, top.y + 2);
		speed2.location(top.x + 31, top.y + 2);
		speed4.location(top.x + 46, top.y + 2);
		
		pause.selected = !world().simulator.isRunning();
		speed1.selected = 1000 == world().simulator.getDelay();
		speed2.selected = 500 == world().simulator.getDelay();
		speed4.selected = 250 == world().simulator.getDelay();
		
		year.bounds(top.x + 155, top.y + 3, 34, 14);
		month.bounds(top.x + 195, top.y + 3, 74, 14);
		day.bounds(top.x + 275, top.y + 3, 18, 14);
		time.bounds(top.x + 307, top.y + 3, 42, 14);
		
		year.text("" + world().time.get(GregorianCalendar.YEAR));
		month.text("" + world().time.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG, new Locale(commons.language())));
		day.text("" + world().time.get(GregorianCalendar.DATE));
		time.text(String.format("%02d:%02d",
				world().time.get(GregorianCalendar.HOUR_OF_DAY),
				world().time.get(GregorianCalendar.MINUTE)));
	}
}
