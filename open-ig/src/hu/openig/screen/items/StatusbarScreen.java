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
import hu.openig.model.SoundType;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImageButton;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;



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
	/** The animation frequency. */
	double frequency = 20;
	/** The acceleration step. */ 
	final int accelerationStep = (int)(frequency * 1);
	/** The stay in center step. */
	final int stayStep = (int)(frequency * 1.5);
	/** The current animation step. */
	int animationStep;
	/** The screen menu. */
	ScreenMenu screenMenu;
	/** The notification history. */
	NotificationHistory notificationHistory;
	/** The error text to display. */
	public String errorText;
	/** The time to live for the error text. */
	public int errorTTL;
	/** The default error text display time. */
	public static final int DEFALT_ERROR_TTL = 15;
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
				if (commons.getSimulationSpeed.invoke(null) > 0) {
					sound(SoundType.PAUSE);
				} else {
					sound(SoundType.UI_ACKNOWLEDGE_1);
				}
				commons.setSimulationSpeed.invoke(-1);
			}
		};
		speed1 = new UIImageTabButton2(commons.common().speed1);
		speed1.onPress = new Act() {
			@Override
			public void act() {
				sound(SoundType.CLICK_LOW_1);
				commons.setSimulationSpeed.invoke(1000);
			}
		};
		speed2 = new UIImageTabButton2(commons.common().speed2);
		speed2.onPress = new Act() {
			@Override
			public void act() {
				sound(SoundType.CLICK_LOW_1);
				commons.setSimulationSpeed.invoke(500);
			}
		};
		speed4 = new UIImageTabButton2(commons.common().speed4);
		speed4.onPress = new Act() {
			@Override
			public void act() {
				sound(SoundType.CLICK_LOW_1);
				commons.setSimulationSpeed.invoke(250);
			}
		};
		
		notification = new MovingNotification();
		
		screenMenu = new ScreenMenu();
		screenMenu.visible(false);
		notificationHistory = new NotificationHistory();
		notificationHistory.visible(false);
		
		addThis();
	}

	@Override
	public void onEnter(Screens mode) {
		top.bounds(0, -20, width, 20);
		bottomY = 0;
		bottom.bounds(0, height, width, 18);
		animationStep = 0;
		notification.currentMessage = null;
		animation = commons.register(75, new Act() {
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
		
		screenMenu.location(width - screenMenu.width, 20);
		notificationHistory.bounds(12, bottom.y - 140, width - 190, 140);
		notificationHistory.visible(notificationHistory.visible() && !commons.nongame);
		super.draw(g2);
		if (commons.nongame) {
			String s = "Open Imperium Galactica";
			int w = commons.text().getTextWidth(10, s);
			commons.text().paintTo(g2, notification.x + (notification.width - w) / 2, notification.y + 1, 10, TextRenderer.YELLOW, s);
		}
	}
	/** Update the state displays. */
	public void update() {
		money.bounds(top.x + 75, top.y + 3, 82, 14);
		money.text("" + player().money);
		pause.location(top.x + 1, top.y + 2);
		speed1.location(top.x + 16, top.y + 2);
		speed2.location(top.x + 31, top.y + 2);
		speed4.location(top.x + 46, top.y + 2);
		
		int spd = commons.getSimulationSpeed.invoke(null);
		pause.selected = spd < 0;
		speed1.selected = 1000 == spd || -1000 == spd;
		speed2.selected = 500 == spd || -500 == spd;
		speed4.selected = 250 == spd || -250 == spd;
		
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
		notification.currentMessage = null;
		animationStep = 0;
	}
	/** The moving status indicator. */
	class MovingNotification extends UIComponent {
		/** The current message. */
		Message currentMessage;
		@Override
		public void draw(Graphics2D g2) {
			Shape save0 = g2.getClip();
			g2.clipRect(0, 0, width, height);
			if (errorText != null) {
				int w = commons.text().getTextWidth(10, errorText);
				commons.text().paintTo(g2, (width - w) / 2, 1, 10, TextRenderer.RED, errorText);
			} else
			if (currentMessage == null) {
				String s = "Open Imperium Galactica";
				int w = commons.text().getTextWidth(10, s);
				commons.text().paintTo(g2, (width - w) / 2, 1, 10, TextRenderer.YELLOW, s);
			} else {
				
				int mw = getTextWidth();
				int renderX = 0;
				int totalWidth = (mw + width) / 2;
				boolean blink = false;
				if (animationStep <= accelerationStep) {
					double time = animationStep * 1.0 / frequency;
					double v0 = 2 * totalWidth * frequency / accelerationStep;
					double acc = -2.0 * totalWidth * frequency * frequency / accelerationStep / accelerationStep;
					renderX = (int)(width / 2 + totalWidth - (v0 * time + acc / 2 * time * time));
				} else
				if (animationStep <= accelerationStep + stayStep) {
					int frame = animationStep - accelerationStep;
					blink = frame < (stayStep / 3) || frame >= (stayStep * 2 / 3);
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
					} else
					if (currentMessage.label != null) {
						param = get(currentMessage.label);
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
				} else
				if (currentMessage.label != null) {
					param = get(currentMessage.label);
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
				if (currentMessage.targetResearch != null) {
					world().selectResearch(currentMessage.targetResearch);
					displaySecondary(Screens.RESEARCH);
				}
			} else
			if (e.has(Type.DOWN) && e.has(Button.RIGHT)) {
				sound(SoundType.CLICK_MEDIUM_2);
				notificationHistory.visible(!notificationHistory.visible());
				return true;
			}
			return super.mouse(e);
		}
	}
	/**
	 * Animate the message.
	 */
	void doAnimation() {
		if (!commons.nongame) {
			if (errorText == null) {
				if (notification.currentMessage != null) {
					if (animationStep >= accelerationStep * 2 + stayStep) {
						player().messageQueue.remove(notification.currentMessage);
						player().messageHistory.add(notification.currentMessage);
						notification.currentMessage = null;
						animationStep = 0;
						askRepaint();
					} else {
						animationStep++;
						if (animationStep < accelerationStep) {
							askRepaint();
						} else
						if (animationStep < accelerationStep + stayStep) {
							int frame = animationStep - accelerationStep;
							if (frame == 0 || frame == stayStep / 3 || frame == stayStep * 2 / 3) {
								askRepaint();
							}
						} else
						if (animationStep >= accelerationStep + stayStep) {
							askRepaint();
						}
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
			} else {
				if (errorTTL > 0) {
					errorTTL--;
				} else {
					errorText = null;
				}
			}
		}
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.UP) && screenMenu.visible() && !e.within(screenMenu.x, screenMenu.y, screenMenu.width, screenMenu.height)) {
			screenMenu.visible(false);
			return true;
		}
		if (e.within(0, 0, width, 20) 
			|| e.within(0, height - 18, width, 18)
			|| (screenMenu.visible() && e.within(screenMenu.x, screenMenu.y, screenMenu.width, screenMenu.height))
			|| (notificationHistory.visible() && e.within(notificationHistory.x, notificationHistory.y, notificationHistory.width, notificationHistory.height))
		) { 
			if (e.has(Type.DOWN) && e.within(width - screenMenu.width, 0, screenMenu.width, 20)) {
				sound(SoundType.CLICK_MEDIUM_2);
				screenMenu.highlight = -1;
				screenMenu.visible(true);
				return true;
			}
			return super.mouse(e);
		}
		return false;
	}
	/**
	 * A popup menu to switch to arbitrary screen by the mouse.
	 * @author akarnokd, 2011.04.20.
	 */
	class ScreenMenu extends UIContainer {
		/** The current highlight index. */
		int highlight = -1;
		/** The screen name labels. */
		final String[] labels = {
			"screens.bridge",
			"screens.starmap",
			"screens.colony",
			"screens.equipment",
			"screens.production",
			"screens.research",
			"screens.information",
			"screens.database",
			"screens.bar",
			"screens.diplomacy",
			"screens.statistics",
			"screens.achievements",
			"screens.options",
		};
		/** Set the dimensions. */
		public ScreenMenu() {
			height = 18 * labels.length + 10;
			width = 0;
			for (String s : labels) {
				width = Math.max(width, commons.text().getTextWidth(14, get(s)));
			}
			width += 10;
		}
		@Override
		public void draw(Graphics2D g2) {
			g2.setColor(new Color(0, 0, 0, 224));
			g2.fillRect(0, 0, width, height);
			g2.setColor(Color.GRAY);
			g2.drawRect(0, 0, width - 1, height - 1);
			int y = 7;
			int idx = 0;
			for (String s0 : labels) {
				String s = get(s0);
				int c = idx == highlight ? TextRenderer.WHITE : TextRenderer.ORANGE;
				if ((idx == 5 && world().level < 2) 
						|| (idx == 6 && world().level < 3)
						|| (idx == 9 && world().level < 2)
						|| (idx == 10 && world().level < 4)
				) {
					c = TextRenderer.GRAY;
				}
				commons.text().paintTo(g2, 5, y, 14, c, s);
				y += 18;
				idx++;
			}
		}
		@Override
		public boolean mouse(UIMouse e) {
			int idx = (e.y - 7) / 18;
			if (idx >= 0 && idx < labels.length) {
				highlight = idx;
			} else {
				highlight = -1;
			}
			if (e.has(Type.UP)) {
				switchScreen();
			}
			super.mouse(e);
			return true;
		}
		/**
		 * Switch to the higlighted screen or hide the menu.
		 */
		void switchScreen() {
			switch (highlight) {
			case 0:
				displayPrimary(Screens.BRIDGE);
				break;
			case 1:
				displayPrimary(Screens.STARMAP);
				break;
			case 2:
				displayPrimary(Screens.COLONY);
				break;
			case 3:
				displaySecondary(Screens.EQUIPMENT);
				break;
			case 4:
				if (world().level >= 2) {
					displaySecondary(Screens.PRODUCTION);
				}
				break;
			case 5:
				if (world().level >= 3) {
					displaySecondary(Screens.RESEARCH);
				}
				break;
			case 6:
				displaySecondary(Screens.INFORMATION_PLANETS);
				break;
			case 7:
				displaySecondary(Screens.DATABASE);
				break;
			case 8:
				if (world().level >= 2) {
					displaySecondary(Screens.BAR);
				}
				break;
			case 9:
				if (world().level >= 4) {
					displaySecondary(Screens.DIPLOMACY);
				}
				break;
			case 10:
				displaySecondary(Screens.STATISTICS);
				break;
			case 11:
				displaySecondary(Screens.ACHIEVEMENTS);
				break;
			case 12:
				displaySecondary(Screens.LOAD_SAVE);
				break;
			default:
			}
			visible(false);
		}
	}
	
	/** The scrollable list of notification history. */
	class NotificationHistory extends UIContainer {
		/** The bottom row index counted from the last element. */
		int bottom = 0;
		/** Scroll up. */
		UIImageButton scrollUp;
		/** Scroll down. */
		UIImageButton scrollDown;
		/** The event time format. */
		SimpleDateFormat sdf;
		/** Construct the buttons. */
		public NotificationHistory() {
			scrollUp = new UIImageButton(commons.common().moveUp);
			scrollUp.onClick = new Act() {
				@Override
				public void act() {
					sound(SoundType.CLICK_MEDIUM_2);
					doScrollUp();
				}
			};
			scrollUp.setHoldDelay(250);
			scrollDown = new UIImageButton(commons.common().moveDown);
			scrollDown.onClick = new Act() {
				@Override
				public void act() {
					sound(SoundType.CLICK_MEDIUM_2);
					doScrollDown();
				}
			};
			scrollDown.setHoldDelay(250);
			
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			sdf.setCalendar(new GregorianCalendar(TimeZone.getTimeZone("GMT")));
			addThis();
		}
		@Override
		public void draw(Graphics2D g2) {
			g2.setColor(new Color(0, 0, 0, 224));
			g2.fillRect(0, 0, width, height);
			g2.setColor(Color.GRAY);
			g2.drawRect(0, 0, width - 1, height - 1);
			
			Shape save0 = g2.getClip();
			g2.clipRect(0, 0, width - 30, height);
			
			int rowHeight = 20;
			int rows = height / rowHeight;
			int start = player().messageHistory.size() - bottom - 1;
			int y = height - rowHeight; 
			for (int i = start; i >= start - rows && i >= 0; i--) {
				Message msg = player().messageHistory.get(i);
				
				// use an icon
				if ("message.yesterday_tax_income".equals(msg.text)
						|| "message.yesterday_trade_income".equals(msg.text)
				) {
					g2.drawImage(commons.statusbar().moneyNotification, 4, y + 2, null);
				} else
				if (msg.targetProduct != null) {
					g2.drawImage(commons.statusbar().productionNotify, 4, y + 2, null);
				} else
				if (msg.targetResearch != null) {
					g2.drawImage(commons.statusbar().researchNotify, 4, y + 2, null);
				} else
				if (msg.targetPlanet != null) {
					g2.drawImage(msg.targetPlanet.type.body[0], 4, y + 2, 16, 16, null);
				} else
				if (msg.targetFleet != null) {
					int dx = (20 - msg.targetFleet.owner.fleetIcon.getWidth()) / 2;
					int dy = (20 - msg.targetFleet.owner.fleetIcon.getHeight()) / 2;
					g2.drawImage(msg.targetPlanet.type.body[0], dx, y + dy, null);
				}
				
				String msgText = get(msg.text);
				int idx = msgText.indexOf("%s");
				if (idx < 0) {
					commons.text().paintTo(g2, 26, y + 5, 10, 
							TextRenderer.GREEN, msgText);
				} else {
					String pre = msgText.substring(0, idx);
					String post = msgText.substring(idx + 2);
					String param = null;
					if (msg.targetPlanet != null) {
						param = msg.targetPlanet.name;
					} else
					if (msg.targetFleet != null) {
						param = msg.targetFleet.name;
					} else
					if (msg.targetProduct != null) {
						param = msg.targetProduct.name;
					} else
					if (msg.targetResearch != null) {
						param = msg.targetResearch.name;
					} else
					if (msg.value != null) {
						param = msg.value;
					} else 
					if (msg.label != null) {
						param = get(msg.label);
					}
					int w0 = commons.text().getTextWidth(10, pre);
					int w1 = commons.text().getTextWidth(10, param);
					int w2 = commons.text().getTextWidth(10, post);
					
					commons.text().paintTo(g2, 26, y + 5, 10, 
							TextRenderer.GREEN, pre);
					commons.text().paintTo(g2, 26 + w0, y + 5, 10, 
							TextRenderer.YELLOW, param);
					commons.text().paintTo(g2, 26 + w0 + w1, y + 5, 10, 
							TextRenderer.GREEN, post);
					
					commons.text().paintTo(g2, 26 + w0 + w1 + w2 + 20, y + 7, 7, TextRenderer.GRAY, sdf.format(new Date(msg.gametime)));
					
				}
				y -= rowHeight;
			}
			g2.setClip(save0);
			
			scrollUp.location(width - 30, 1);
			scrollDown.location(width - 30, height - 30);
			
			scrollUp.visible(bottom + rows < player().messageHistory.size());
			scrollDown.visible(bottom > 0);
			
			super.draw(g2);
		}
		/** Scroll up. */
		void doScrollUp() {
			bottom = Math.max(0, Math.min(bottom + 1, player().messageHistory.size() - (height / 20)));
		}
		/** Scroll down. */
		void doScrollDown() {
			bottom = Math.max(0, Math.min(bottom - 1, player().messageHistory.size() - (height / 20)));
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.WHEEL)) {
				if (e.z < 0) {
					doScrollUp();
				} else {
					doScrollDown();
				}
				return true;
			} else
			if (e.has(Type.DOUBLE_CLICK) && e.x < width - 31) {
				int idx = player().messageHistory.size() - 1 - bottom - ((height - e.y) / 20);
				if (idx >= 0 && idx < player().messageHistory.size()) {
					Message currentMessage = player().messageHistory.get(idx);
					if (currentMessage.targetPlanet != null) {
						player().currentPlanet = currentMessage.targetPlanet;
						player().selectionMode = SelectionMode.PLANET;
						displayPrimary(Screens.COLONY);
						visible(false);
						return true;
					} else
					if (currentMessage.targetFleet != null) {
						player().currentFleet = currentMessage.targetFleet;
						player().selectionMode = SelectionMode.FLEET;
						displayPrimary(Screens.EQUIPMENT);
						visible(false);
						return true;
					} else
					if (currentMessage.targetProduct != null) {
						world().selectResearch(currentMessage.targetProduct);
						displaySecondary(Screens.PRODUCTION);
						visible(false);
						return true;
					} else
					if (currentMessage.targetResearch != null) {
						world().selectResearch(currentMessage.targetResearch);
						displaySecondary(Screens.RESEARCH);
						visible(false);
						return true;
					}
				}
			}
			return super.mouse(e);
		}
	}
}
