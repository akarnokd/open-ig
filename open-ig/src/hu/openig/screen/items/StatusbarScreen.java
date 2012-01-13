/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.SimulationSpeed;
import hu.openig.model.Message;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Screens;
import hu.openig.model.SelectionMode;
import hu.openig.model.SoundType;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.items.LoadSaveScreen.SettingsPage;
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
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
	/** The current achievement showing. */
	public String achievementShowing;
	/** The number of pixels the achievement has moved. */
	public int achievementDescent;
	/** The time to show the achievement. */
	public int achievementTTL;
	/** The size of the achievement panel. */
	final int achievementSize = 55;
	/** The achievement animator. */
	Timer achievementAnimator;
	/** The objectives viewer. */
	public ObjectivesView objectives;
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
		pause.onPress = new Action0() {
			@Override
			public void invoke() {
				if (!commons.simulation.paused()) {
					sound(SoundType.PAUSE);
					commons.simulation.pause();
				} else {
					sound(SoundType.UI_ACKNOWLEDGE_1);
					commons.simulation.resume();
				}
			}
		};
		speed1 = new UIImageTabButton2(commons.common().speed1);
		speed1.onPress = new Action0() {
			@Override
			public void invoke() {
				sound(SoundType.CLICK_LOW_1);
				commons.simulation.speed(SimulationSpeed.NORMAL);
			}
		};
		speed2 = new UIImageTabButton2(commons.common().speed2);
		speed2.onPress = new Action0() {
			@Override
			public void invoke() {
				sound(SoundType.CLICK_LOW_1);
				commons.simulation.speed(SimulationSpeed.FAST);
			}
		};
		speed4 = new UIImageTabButton2(commons.common().speed4);
		speed4.onPress = new Action0() {
			@Override
			public void invoke() {
				sound(SoundType.CLICK_LOW_1);
				commons.simulation.speed(SimulationSpeed.ULTRA_FAST);
			}
		};
		
		notification = new MovingNotification();
		
		screenMenu = new ScreenMenu();
		screenMenu.visible(false);
		notificationHistory = new NotificationHistory();
		notificationHistory.visible(false);
		
		achievementAnimator = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (achievementDescent > 0) {
					achievementDescent -= 4;
					askRepaint();
				} else {
					if (--achievementTTL <= 0) {
						achievementShowing = null;
						achievementAnimator.stop();
						askRepaint();
					}
				}
			}
		});
		
		objectives = new ObjectivesView();
		objectives.enabled(false);
		objectives.visible(false);
		objectives.z = -1;
		
		addThis();
	}

	@Override
	public void onEnter(Screens mode) {
		top.bounds(0, -20, width, 20);
		bottomY = 0;
		bottom.bounds(0, height, width, 18);
		animationStep = 0;
		notification.currentMessage = null;
		animation = commons.register(75, new Action0() {
			@Override
			public void invoke() {
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
		achievementAnimator.stop();
	}

	@Override
	public void onFinish() {
		animation = null;
		achievementAnimator.stop();
	}

	@Override
	public void onResize() {
		top.size(width, 20);
		bottom.size(width, 18);
		
		objectives.location(5, 30 + achievementSize);
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
		
		boolean objshowing = objectives.visible();
		if (commons.nongame) {
			screenMenu.visible(false);
			objectives.visible(false);
		}
		notification.bounds(12, bottom.y + 3, width - 190, 12);
		
		screenMenu.location(width - screenMenu.width, 20);
		notificationHistory.bounds(12, bottom.y - 140, width - 190, 140);
		notificationHistory.visible(notificationHistory.visible() && !commons.nongame);
		super.draw(g2);
		if (commons.nongame) {
			objectives.visible(objshowing);
			
			String s = "Open Imperium Galactica";
			int w = commons.text().getTextWidth(10, s);
			commons.text().paintTo(g2, notification.x + (notification.width - w) / 2, notification.y + 1, 10, TextRenderer.YELLOW, s);
		}
		if (achievementShowing != null) {
			Shape clip = g2.getClip();
			
			String label = get(achievementShowing);
			String desc = get(achievementShowing + ".desc");
			
			int w = Math.max(commons.text().getTextWidth(14, label), commons.text().getTextWidth(10, desc));
			int aw = commons.common().achievement.getWidth();
			w += 15 + aw;
			g2.clipRect(0, 20, w, achievementSize - achievementDescent);
			
			g2.setColor(new Color(0xC0000000, true));
			int bottom = achievementSize - achievementDescent;
			g2.fillRect(0, 20, w, bottom);
			g2.setColor(Color.GRAY);
			g2.drawRect(0, 20, w - 1, bottom - 1);
			
			g2.drawImage(commons.common().achievement, 5, 20 + bottom - 48, null);
			commons.text().paintTo(g2, 10 + aw, 20 + bottom - 45, 14, TextRenderer.YELLOW, label);
			commons.text().paintTo(g2, 10 + aw, 20 + bottom - 20, 10, TextRenderer.LIGHT_BLUE, desc);
			
			g2.setClip(clip);
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
		
		SimulationSpeed spd = commons.simulation.speed();
		pause.selected = commons.simulation.paused();
		speed1.selected = spd == SimulationSpeed.NORMAL;
		speed2.selected = spd == SimulationSpeed.FAST;
		speed4.selected = spd == SimulationSpeed.ULTRA_FAST;
		
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

		
		// manage achievements
		if (achievementShowing == null) {
			achievementShowing = commons.achievementNotifier.poll();
			if (achievementShowing != null) {
				achievementDescent = achievementSize;
				achievementTTL = 10 * 10; // 10 seconds
				achievementAnimator.start();
			}
		}
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
					String param = "";
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
				String param = "";
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
		if (e.within(379, 3, 26, 14) && e.has(Type.DOWN)) {
			objectives.visible(!objectives.visible());
			sound(SoundType.CLICK_MEDIUM_2);
			return true;
		} else
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
			"screens.loadsave",
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
				if (isScreenDisabled(idx)) {
					c = TextRenderer.GRAY;
				}
				commons.text().paintTo(g2, 5, y, 14, c, s);
				y += 18;
				idx++;
			}
		}
		/**
		 * Check if the given screen index is disabled in the current state.
		 * @param idx the sceen index
		 * @return true if disabled
		 */
		boolean isScreenDisabled(int idx) {
			return (idx == 4 && world().level < 2) 
					|| (idx == 5 && world().level < 3)
					|| (idx == 8 && world().level < 2)
					|| (idx == 9 && world().level < 4)
					|| (idx < 12 && commons.battleMode);
		}
		@Override
		public boolean mouse(UIMouse e) {
			int idx = (e.y - 7) / 18;
			if (idx >= 0 && idx < labels.length) {
				highlight = idx;
			} else {
				highlight = -1;
			}
			if (!isScreenDisabled(highlight)) {
				if (e.has(Type.UP)) {
					switchScreen();
				}
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
				LoadSaveScreen scr = (LoadSaveScreen)displaySecondary(Screens.LOAD_SAVE);
				scr.maySave(!commons.battleMode);
				scr.displayPage(SettingsPage.LOAD_SAVE);
				break;
			case 13:
				scr = (LoadSaveScreen)displaySecondary(Screens.LOAD_SAVE);
				scr.maySave(!commons.battleMode);
				scr.displayPage(SettingsPage.AUDIO);
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
		/** Clear history. */
		UIImageButton clear;
		/** Scroll up. */
		UIImageButton scrollUp;
		/** Scroll down. */
		UIImageButton scrollDown;
		/** The event time format. */
		SimpleDateFormat sdf;
		/** Construct the buttons. */
		public NotificationHistory() {
			scrollUp = new UIImageButton(commons.common().moveUp);
			scrollUp.onClick = new Action0() {
				@Override
				public void invoke() {
					sound(SoundType.CLICK_MEDIUM_2);
					doScrollUp();
				}
			};
			scrollUp.setHoldDelay(250);
			scrollDown = new UIImageButton(commons.common().moveDown);
			scrollDown.onClick = new Action0() {
				@Override
				public void invoke() {
					sound(SoundType.CLICK_MEDIUM_2);
					doScrollDown();
				}
			};
			scrollDown.setHoldDelay(250);
			
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			sdf.setCalendar(new GregorianCalendar(TimeZone.getTimeZone("GMT")));
			clear = new UIImageButton(commons.common().delete) {
				@Override
				public boolean mouse(UIMouse e) {
					return super.mouse(e);
				}
			};
			clear.onClick = new Action0() {
				@Override
				public void invoke() {
					sound(SoundType.CLICK_MEDIUM_2);
					doClearHistory();
				}
			};
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
			bottom = Math.max(0, Math.min(bottom, player().messageHistory.size() - (height / 20)));
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
					String param = "";
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
			
			clear.location(width - 30, 1);
			scrollUp.location(width - 30, clear.y + clear.height + 5);
			scrollDown.location(width - 30, height - 30);
			
			clear.visible(!player().messageHistory.isEmpty());
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
	/**
	 * Clear the entire history.
	 */
	void doClearHistory() {
		player().messageHistory.clear();
	}
	/**
	 * The objectives viewer.
	 * @author akarnokd, Jan 12, 2012
	 */
	public class ObjectivesView extends UIComponent {
		@Override
		public void draw(Graphics2D g2) {
			
			g2.translate(16, 0);
			
			List<Objective> objs = world().scripting.currentObjectives();
			
			if (objs.size() == 0) {
				int w = commons.text().getTextWidth(14, get("no_objectives"));
				g2.setColor(new Color(0xC0000000, true));

				String hideS = get("hide_objectives");
				int hideW = commons.text().getTextWidth(7, hideS);

				w = Math.max(w, hideW);
				
				g2.fillRect(0, 0, w + 10, 33);
				commons.text().paintTo(g2, 5, 3, 14, TextRenderer.GRAY, get("no_objectives"));
				
				g2.setColor(new Color(0xFFC0C0C0));
				g2.drawLine(0, 20, Math.min(hideW, w), 20);
				commons.text().paintTo(g2, 2, 22, 7, 0xFFFFFF00, hideS);

				return;
			}
			
			int limit = StatusbarScreen.this.width - 20;
			
			int w = 0;
			int h = 0;
			
			for (Objective o : objs) {
				w = Math.max(w, objectiveWidth(o, limit));
				int oh = objectiveHeight(o, limit);
				h += 3 + oh;
			}
			h += 3;
			
			String hideS = get("hide_objectives");
			int hideW = commons.text().getTextWidth(7, hideS);
			
			w = Math.min(Math.max(hideW, w), limit);
			
			g2.setColor(new Color(0xC0000000, true));
			
			g2.fillRect(0, 0, w + 4, h + 13);
			g2.setColor(new Color(0xFFC0C0C0));
			g2.drawRect(0, 0, w + 4, h + 13);
			
			int y = 3;
			for (Objective o : objs) {
				y += drawObjective(g2, o, 2, y, w);
			}
			
			g2.setColor(new Color(0xFFC0C0C0));
			g2.drawLine(0, y + 2, Math.min(hideW, w), y + 2);
			commons.text().paintTo(g2, 2, y + 4, 7, 0xFFFFFF00, hideS);
			
			super.draw(g2);
			
			g2.translate(-16, 0);
		}
		/**
		 * Draw the objective.
		 * @param g2 the graphics context.
		 * @param o the objective
		 * @param x the left
		 * @param y the top
		 * @param w the draw width
		 * @return the y increment
		 */
		int drawObjective(Graphics2D g2, Objective o, int x, int y, int w) {
			
			g2.setColor(new Color(0xFFB0B0B0));
			g2.drawRect(x + 3, y + 3, 14, 14);
			g2.drawRect(x + 4, y + 4, 12, 12);

			int dy = 0;
			
			if (o.state == ObjectiveState.FAILURE) {
				g2.drawImage(commons.common().crossOut, x, y, null);
			} else
			if (o.state == ObjectiveState.SUCCESS) {
				g2.drawImage(commons.common().checkmarkGreen, x, y, null);
			}
			
			dy += 3;
			dy += drawText(g2, x + 25, y + dy, w - 25, 14, player().color, o.title);
			dy += 3;
			if (!o.description.isEmpty()) {
				dy += drawText(g2, x + 25, y + dy, w - 25, 10, 0xFFC0C0FF, o.description);
			}
			
			String pt = o.progressValue != null ? o.progressValue.invoke() : null;
			Double pv = o.progress != null ? o.progress.invoke() : null;
			if (pv != null || pt != null) {
				int dx = 35;
				if (pt != null) {
					dx += commons.text().getTextWidth(7, pt) + 10;
					commons.text().paintTo(g2, x + 35, y + dy, 7, 0xFFFFCC00, pt);
				}
				
				if (pv != null) {
					int rw = w - dx;
					int rwf = (int)(rw * pv.doubleValue());
					
					g2.setColor(new Color(0xFFFFCC00));
					g2.drawRect(x + dx, y + dy, rw, 7);
					g2.fillRect(x + dx, y + dy, rwf, 7);
				}
				dy += 10;
			}
			
			for (Objective o2 : o.subObjectives) {
				if (o2.visible) {
					dy += drawObjective(g2, o2, x + 25, y + dy, w - 25);
				}
			}
			
			return dy;
		}
		/**
		 * Draw a multiline text by wrapping.
		 * @param g2 the graphics context
		 * @param x the left
		 * @param y the top
		 * @param w the width
		 * @param size the text size
		 * @param color the color
		 * @param text the text
		 * @return the delta y
		 */
		int drawText(Graphics2D g2, int x, int y, int w, int size, int color, String text) {
			List<String> lines = U.newArrayList();
			commons.text().wrapText(text, w, size, lines);
			int dy = 0;
			for (String s : lines) {
				commons.text().paintTo(g2, x, y + dy, size, color, s);
				dy += size + 3;
			}
			return dy;
		}
		/**
		 * Returns the width of the objective, considering the set of sub-objectives.
		 * @param o the objective
		 * @param limit the width limit
		 * @return the width
		 */
		public int objectiveWidth(Objective o, int limit) {
			int titleWidth = commons.text().getTextWidth(14, o.title);
			int descriptionWidth = commons.text().getTextWidth(10, o.description);
			int progressGauge = (o.progress != null ? 100 : 0) + (o.progressValue != null ? commons.text().getTextWidth(7, o.progressValue.invoke()) : 0);
			
			int w = max(titleWidth, descriptionWidth, progressGauge);

			int ws = 0;
			for (Objective o2 : o.subObjectives) {
				if (o2.visible) {
					ws = Math.max(ws, objectiveWidth(o2, limit - 25));
				}
			}
			
			return Math.min(25 + Math.max(w, ws), limit);
		}
		/**
		 * Returns the height of the objective considering any sub-objectives.
		 * @param o the objective
		 * @param limit the width limit for wrapping
		 * @return the height
		 */
		public int objectiveHeight(Objective o, int limit) {
			int w = objectiveWidth(o, limit);
			int titleWidth = commons.text().getTextWidth(14, o.title);
			int descriptionWidth = commons.text().getTextWidth(10, o.description);
			int progressGauge = (o.progress != null ? 100 : 0) + (o.progressValue != null ? commons.text().getTextWidth(7, o.progressValue.invoke()) : 0);
			
			int h = 0;
			if (titleWidth > w) {
				List<String> lines = U.newArrayList();
				commons.text().wrapText(o.title, w, 14, lines);
				h += lines.size() * 17 + 3;
			} else {
				h += 20;
			}
			
			if (descriptionWidth > 0) {
				if (descriptionWidth > w) {
					List<String> lines = U.newArrayList();
					commons.text().wrapText(o.description, w, 10, lines);
					h += lines.size() * 13;
				} else {
					h += 13;
				}
			}

			if (progressGauge > 0) {
				h += 10;
			}
			
			for (Objective o2 : o.subObjectives) {
				if (o2.visible) {
					h += 3 + objectiveHeight(o2, limit - 25);
				}
			}
			
			return h;
		}
	}
	/**
	 * Returns the maximum.
	 * @param is the array ints
	 * @return the maximum
	 */
	static int max(int... is) {
		int r = is[0];
		for (int i = 1; i < is.length; i++) {
			r = Math.max(r, is[i]);
		}
		return r;
	}
}
