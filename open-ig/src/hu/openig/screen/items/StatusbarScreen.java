/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Act;
import hu.openig.model.Message;
import hu.openig.model.Screens;
import hu.openig.model.SelectionMode;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIImageFill;
import hu.openig.ui.UIImageTabButton2;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.io.Closeable;
import java.util.GregorianCalendar;
import java.util.Locale;



/**
 * Displays and handles the status bar screen.
 * @author akarnokd, 2010.01.11.
 */
public class StatusbarScreen extends ScreenBase {
	/** The top bar. */
	UIImageFill top;
	/** The bottom bar. */
	UIImageFill bottom;
	/** The top bar non-game. */
	UIImageFill top2;
	/** The bottom bar non-game. */
	UIImageFill bottom2;
	/** The money label. */
	UILabel money;
	/** The animation timer to show the status bar. */
	Closeable animation;
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
	/** The moving notification text at the bottom bar. */
	MovingNotification notification;
	/** The acceleration step. */ 
	final int accelerationStep = 40;
	/** The stay in center step. */
	final int stayStep = 60;
	/** The current animation step. */
	int animationStep;
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
		top2 = new UIImageFill(
				commons.statusbar().nongameTopLeft, 
				commons.statusbar().nongameTopFill,
				commons.statusbar().nongameTopRight, true);
		top2.z = 1;
		top2.visible(false);
		bottom2 = new UIImageFill(
				commons.statusbar().nongameBottomLeft, 
				commons.statusbar().nongameBottomFill,
				commons.statusbar().nongameBottomRight, true);
		bottom2.z = 1;
		bottom2.visible(false);
		
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
				if (commons.paused()) {
					commons.resume();
				} else {
					commons.pause();
				}
			}
		};
		speed1 = new UIImageTabButton2(commons.common().speed1);
		speed1.onPress = new Act() {
			@Override
			public void act() {
				commons.speed(1000);
			}
		};
		speed2 = new UIImageTabButton2(commons.common().speed2);
		speed2.onPress = new Act() {
			@Override
			public void act() {
				commons.speed(500);
			}
		};
		speed4 = new UIImageTabButton2(commons.common().speed4);
		speed4.onPress = new Act() {
			@Override
			public void act() {
				commons.speed(250);
			}
		};
		
		notification = new MovingNotification();
		
		addThis();
	}

	@Override
	public void onEnter(Screens mode) {
		top.bounds(0, -20, width, 20);
		bottomY = 0;
		bottom.bounds(0, height, width, 18);
		animation = commons.register(50, new Act() {
			@Override
			public void act() {
				if (top.y < 0) {
					top.y += 2;
					askRepaint();
				}
				if (bottomY < 18) {
					bottomY += 2;
					askRepaint();
				}
				doAnimation();
			}
		});
	}

	@Override
	public void onLeave() {
		close0(animation);
		animation = null;
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
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, width, 20);
		g2.fillRect(0, height - 18, width, 18);
		bottom.y = height - bottomY;
		update();
		
		top2.bounds(top.bounds());
		bottom2.bounds(bottom.bounds());
		
		top2.visible(commons.nongame);
		bottom2.visible(commons.nongame);
		
		notification.bounds(12, bottom.y + 3, width - 190, 12);
		
		super.draw(g2);
	}
	/** Update the state displays. */
	public void update() {
		money.bounds(top.x + 75, top.y + 3, 82, 14);
		money.text("" + player().money);
		pause.location(top.x + 1, top.y + 2);
		speed1.location(top.x + 16, top.y + 2);
		speed2.location(top.x + 31, top.y + 2);
		speed4.location(top.x + 46, top.y + 2);
		
		pause.selected = commons.paused();
		speed1.selected = 1000 == commons.speed();
		speed2.selected = 500 == commons.speed();
		speed4.selected = 250 == commons.speed();
		
		year.bounds(top.x + 171, top.y + 3, 34, 14);
		month.bounds(top.x + 211, top.y + 3, 82, 14);
		day.bounds(top.x + 299, top.y + 3, 18, 14);
		time.bounds(top.x + 331, top.y + 3, 42, 14);
		
		year.text("" + world().time.get(GregorianCalendar.YEAR));
		month.text("" + world().time.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG, new Locale(commons.language())));
		day.text("" + world().time.get(GregorianCalendar.DATE));
		time.text(String.format("%02d:%02d",
				world().time.get(GregorianCalendar.HOUR_OF_DAY),
				world().time.get(GregorianCalendar.MINUTE)));
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
	/** The moving status indicator. */
	class MovingNotification extends UIComponent {
		/** The current message. */
		Message currentMessage;
		@Override
		public void draw(Graphics2D g2) {
			Shape save0 = g2.getClip();
			g2.clipRect(0, 0, width, height);
			
			if (currentMessage == null) {
				String s = "Open Imperium Galactica";
				int w = commons.text().getTextWidth(10, s);
				commons.text().paintTo(g2, (width - w) / 2, 1, 10, TextRenderer.YELLOW, s);
			} else {
				
				int mw = getTextWidth();
				int renderX = 0;
				int totalWidth = (mw + width) / 2;
				double frequency = 20;
				boolean blink = false;
				if (animationStep <= accelerationStep) {
					double time = animationStep * 1.0 / frequency;
					double v0 = 2 * totalWidth * frequency / accelerationStep;
					double acc = -2.0 * totalWidth * frequency * frequency / accelerationStep / accelerationStep;
					renderX = (int)(width / 2 + totalWidth - (v0 * time + acc / 2 * time * time));
				} else
				if (animationStep <= accelerationStep + stayStep) {
					int time = animationStep - accelerationStep;
					blink = time % 10 < 5;
					renderX = width / 2;
				} else
				if (animationStep <= accelerationStep * 2 + stayStep) {
					double time = 1.0 * (animationStep - accelerationStep - stayStep) / frequency;
					double acc = 2.0 * totalWidth * frequency * frequency / accelerationStep / accelerationStep;
					renderX = (int)(width / 2 - acc / 2 * time * time);
				}
				
				String msgText = get(currentMessage.text);
				int idx = msgText.indexOf("%s");
				if (idx < 0) {
					int w = commons.text().getTextWidth(10, msgText);
					commons.text().paintTo(g2, renderX - w / 2, 1, 10, 
							blink ? TextRenderer.YELLOW : TextRenderer.GREEN, msgText);
				} else {
					String pre = msgText.substring(0, idx);
					String post = msgText.substring(idx + 2);
					String param = null;
					if (currentMessage.targetPlanet != null) {
						param = currentMessage.targetPlanet.name;
					} else
					if (currentMessage.targetFleet != null) {
						param = currentMessage.targetFleet.name;
					} else
					if (currentMessage.targetProduct != null) {
						param = currentMessage.targetProduct.name;
					} else
					if (currentMessage.targetResearch != null) {
						param = currentMessage.targetResearch.name;
					} else
					if (currentMessage.value != null) {
						param = currentMessage.value;
					}
					int w0 = commons.text().getTextWidth(10, pre);
					int w1 = commons.text().getTextWidth(10, param);
					int w2 = commons.text().getTextWidth(10, post);
					
					int dx = (w0 + w1 + w2) / 2;
					int x = renderX - dx;

					commons.text().paintTo(g2, x, 1, 10, 
							blink ? TextRenderer.YELLOW : TextRenderer.GREEN, pre);
					commons.text().paintTo(g2, x + w0, 1, 10, 
							blink ? TextRenderer.RED : TextRenderer.YELLOW, param);
					commons.text().paintTo(g2, x + w0 + w1, 1, 10, 
							blink ? TextRenderer.YELLOW : TextRenderer.GREEN, post);
					
				}
			}
			g2.setClip(save0);
		}
		/** @return the current message's text width or zero if no message is present. */
		public int getTextWidth() {
			if (currentMessage == null) {
				return 0;
			}
			String msgText = get(currentMessage.text);
			int idx = msgText.indexOf("%s");
			if (idx < 0) {
				return commons.text().getTextWidth(10, msgText);
			} else {
				String pre = msgText.substring(0, idx);
				String post = msgText.substring(idx + 2);
				String param = null;
				if (currentMessage.targetPlanet != null) {
					param = currentMessage.targetPlanet.name;
				} else
				if (currentMessage.targetFleet != null) {
					param = currentMessage.targetFleet.name;
				} else
				if (currentMessage.targetProduct != null) {
					param = currentMessage.targetProduct.name;
				} else
				if (currentMessage.targetResearch != null) {
					param = currentMessage.targetResearch.name;
				} else
				if (currentMessage.value != null) {
					param = currentMessage.value;
				}
				int w0 = commons.text().getTextWidth(10, pre);
				int w1 = commons.text().getTextWidth(10, param);
				int w2 = commons.text().getTextWidth(10, post);
				return w0 + w1 + w2;
			}
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (currentMessage != null && e.has(Button.LEFT) && e.has(Type.DOUBLE_CLICK)) {
				if (currentMessage.targetPlanet != null) {
					player().currentPlanet = currentMessage.targetPlanet;
					player().selectionMode = SelectionMode.PLANET;
					displayPrimary(Screens.COLONY);
				} else
				if (currentMessage.targetFleet != null) {
					player().currentFleet = currentMessage.targetFleet;
					player().selectionMode = SelectionMode.FLEET;
					displayPrimary(Screens.EQUIPMENT);
				} else
				if (currentMessage.targetProduct != null) {
					world().selectResearch(currentMessage.targetProduct);
					displaySecondary(Screens.PRODUCTION);
				} else
				if (currentMessage.targetProduct != null) {
					world().selectResearch(currentMessage.targetResearch);
					displaySecondary(Screens.RESEARCH);
				}
//			} else
//			if (e.has(Type.DOWN) && e.has(Button.RIGHT)) {
				
			}
			return super.mouse(e);
		}
	}
	/**
	 * Animate the message.
	 */
	void doAnimation() {
		if (!commons.nongame) {
			if (notification.currentMessage != null) {
				if (animationStep == accelerationStep * 2 + stayStep) {
					player().messageQueue.remove(notification.currentMessage);
					player().messageHistory.add(notification.currentMessage);
					notification.currentMessage = null;
					animationStep = 0;
					askRepaint();
				} else {
					animationStep++;
					askRepaint();
				}
			} else
			if (animationStep == 0) {
				Message msg = player().messageQueue.peek();
				if (msg != null) {
					notification.currentMessage = msg;
					if (msg.sound != null && config.computerVoiceNotify) {
						commons.sounds.play(msg.sound);
					}
					askRepaint();
				}
			}
		}
	}
}
