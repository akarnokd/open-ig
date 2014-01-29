/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.SimulationSpeed;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.Message;
import hu.openig.model.Planet;
import hu.openig.model.Production;
import hu.openig.model.Research;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.Screens;
import hu.openig.model.SelectionMode;
import hu.openig.model.SoundType;
import hu.openig.model.VideoMessage;
import hu.openig.model.World;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.panels.NotificationHistory;
import hu.openig.screen.panels.ObjectivesView;
import hu.openig.screen.panels.QuickPanelButton;
import hu.openig.screen.panels.QuickProductionPanel;
import hu.openig.screen.panels.QuickResearchPanel;
import hu.openig.screen.panels.ScreenMenu;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageFill;
import hu.openig.ui.UIImageTabButton2;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.swing.Timer;



/**
 * Displays and handles the status bar screen.
 * @author akarnokd, 2010.01.11.
 */
public class StatusbarScreen extends ScreenBase {
	/** Menu icon widths. */
	static final int MENU_ICON_WIDTH = 30;
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
	static final int ACHIEVEMENT_SIZE = 55;
	/** The achievement animator. */
	Timer achievementAnimator;
	/** The objectives viewer. */
	public ObjectivesView objectives;
	/** If non-null, defines a full-screen overlay painted. */
	public Color overlay;
	/** Incoming message rectangle. */
	final Rectangle incomingMessage = new Rectangle();
	/** The blink flag. */
	protected boolean blink;
	/** The blink counter. */
	protected int blinkCounter;
	/** The index to show an attack target. */
	protected int attackListIndex;
	/** The quick research panel. */
	QuickResearchPanel quickResearch;
	/** The show-hide objectives button. */
	UIImageButton objectivesButton;
	/** The quick research button. */
	QuickPanelButton quickResearchButton;
	/** The quick production button. */
	QuickPanelButton quickProductionButton;
	/** The menu image. */
	UIImageButton menu;
	/** The quick production panel. */
	QuickProductionPanel quickProduction;
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
					buttonSound(SoundType.PAUSE);
					commons.simulation.pause();
				} else {
					buttonSound(SoundType.UI_ACKNOWLEDGE_1);
					commons.simulation.resume();
				}
			}
		};
		speed1 = new UIImageTabButton2(commons.common().speed1);
		speed1.onPress = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_LOW_1);
				commons.simulation.speed(SimulationSpeed.NORMAL);
			}
		};
		speed2 = new UIImageTabButton2(commons.common().speed2);
		speed2.onPress = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_LOW_1);
				commons.simulation.speed(SimulationSpeed.FAST);
			}
		};
		speed4 = new UIImageTabButton2(commons.common().speed4);
		speed4.onPress = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_LOW_1);
				commons.simulation.speed(SimulationSpeed.ULTRA_FAST);
			}
		};
		
		notification = new MovingNotification();
		
		screenMenu = new ScreenMenu(commons);
		screenMenu.visible(false);
		notificationHistory = new NotificationHistory(commons);
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
		
		objectives = new ObjectivesView(commons, this);
		objectives.enabled(false);
		objectives.visible(false);
		objectives.z = -1;
		
		quickResearch = new QuickResearchPanel(commons);
		quickResearch.visible(false);
		quickResearch.z = 2;
		
		quickProduction = new QuickProductionPanel(commons);
		quickProduction.visible(false);
		quickProduction.z = 2;
		
		objectivesButton = new UIImageButton(commons.statusbar().objectives);
		objectivesButton.z = 1;
		objectivesButton.onClick = new Action0() {
			@Override
			public void invoke() {
				objectives.visible(!objectives.visible());
				buttonSound(SoundType.CLICK_MEDIUM_2);
			}
		};
		
		quickResearchButton = new QuickPanelButton(commons, "00000000", MENU_ICON_WIDTH);
		quickResearchButton.onLeftClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				quickResearch.visible(true);
			}
		};
		quickResearchButton.onRightClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				displaySecondary(Screens.RESEARCH);
			}
		};
		quickProductionButton = new QuickPanelButton(commons, "00000", MENU_ICON_WIDTH);
		quickProductionButton.onLeftClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				quickProduction.visible(true);
			}
		};
		quickProductionButton.onRightClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				displaySecondary(Screens.PRODUCTION);
			}
		};
		
		menu = new UIImageButton(commons.statusbar().menu);
		
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
		
		objectives.location(5, 30 + ACHIEVEMENT_SIZE);
		
		incomingMessage.setBounds(width - 176, height - 16, 164, 18);
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
		
		objectivesButton.x = 379;
		objectivesButton.y = top.y + 3;
		
		menu.location(getInnerWidth() - menu.width, top.y);
		
		objectivesButton.visible(!commons.nongame);
		boolean objshowing = objectives.visible();
		if (commons.nongame) {
			screenMenu.visible(false);
			objectives.visible(false);
			quickResearch.visible(false);
			quickProduction.visible(false);
		}
		notification.bounds(12, bottom.y + 3, width - 190, 12);
		incomingMessage.y = bottom.y + 2;
		
		screenMenu.location(width - screenMenu.width, 20);
		notificationHistory.bounds(12, bottom.y - 140, width - 190, 140);
		notificationHistory.visible(notificationHistory.visible() && !commons.nongame);
		
		super.draw(g2);
		if (commons.nongame) {
			objectives.visible(objshowing);
			
			String s = "Open Imperium Galactica";
			int w = commons.text().getTextWidth(10, s);
			commons.text().paintTo(g2, notification.x + (notification.width - w) / 2, notification.y + 1, 10, TextRenderer.YELLOW, s);
		} else {
			if (hasUnseenMessage()) {
				String s = get("incoming_message");
				int w = commons.text().getTextWidth(10, s);
				commons.text().paintTo(g2, incomingMessage.x + (incomingMessage.width - w) / 2, incomingMessage.y + 1, 10, TextRenderer.RED, s);
			}
		}
		if (achievementShowing != null) {
			Shape clip = g2.getClip();
			
			String label = get(achievementShowing);
			String desc = get(achievementShowing + ".desc");
			
			int w = Math.max(commons.text().getTextWidth(14, label), commons.text().getTextWidth(10, desc));
			int aw = commons.common().achievement.getWidth();
			w += 15 + aw;
			g2.clipRect(0, 20, w, ACHIEVEMENT_SIZE - achievementDescent);
			
			g2.setColor(new Color(0xC0000000, true));
			int bottom = ACHIEVEMENT_SIZE - achievementDescent;
			g2.fillRect(0, 20, w, bottom);
			g2.setColor(Color.GRAY);
			g2.drawRect(0, 20, w - 1, bottom - 1);
			
			g2.drawImage(commons.common().achievement, 5, 20 + bottom - 48, null);
			commons.text().paintTo(g2, 10 + aw, 20 + bottom - 45, 14, TextRenderer.YELLOW, label);
			commons.text().paintTo(g2, 10 + aw, 20 + bottom - 20, 10, TextRenderer.LIGHT_BLUE, desc);
			
			g2.setClip(clip);
		}
		
		if (overlay != null) {
			g2.setColor(overlay);
			g2.fillRect(0, 0, width, height);
		}
	}
	/** @return Check if any of the technologies are available for research. */
	boolean isResearchAvailable() {
		for (ResearchType rt : world().researches.values()) {
			if (rt.race.contains(player().race) && player().canResearch(rt)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * @return computes the total production progress in 0..100% or -1 if no production
	 */
	int computeTotalProduction() {
		int remainingCost = 0;
		for (ResearchMainCategory mcat : World.PRODUCTION_CATEGORIES) {
			for (Production prod : player().productionLines(mcat)) {
				remainingCost += prod.count;
			}
		}
		return remainingCost;
	}
	/**
	 * Check if there is unseen message.
	 * @return true if unseen message is available
	 */
	boolean hasUnseenMessage() {
		for (VideoMessage vm : world().receivedMessages) {
			if (!vm.seen) {
				return true;
			}
		}
		return isDiplomaticCall();
	}
	/** @return Is this a diplomatic call. */
	boolean isDiplomaticCall() {
		for (String p2 : player().offers.keySet()) {
			if (!world().players.get(p2).isDefeated()) {
				return true;
			}
		}
		return false;
	}
	/** Update the state displays. */
	public void update() {
		money.bounds(top.x + 75, top.y + 3, 82, 14);
		money.text("" + player().money());
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
				achievementDescent = ACHIEVEMENT_SIZE;
				achievementTTL = 10 * 10; // 10 seconds
				achievementAnimator.start();
				effectSound(SoundType.ACHIEVEMENT);
			}
		} else {
			if (!achievementAnimator.isRunning() && achievementTTL > 0) {
				achievementAnimator.start();
			}
		}
		if (world().level > 1 && config.quickRNP) {
			quickProductionButton.visible(true);
			
			int prodCount = computeTotalProduction();
			if (prodCount > 0) {
				quickProductionButton.icon = commons.statusbar().gearLight;
				quickProductionButton.text = String.format("%d", prodCount);
			} else {
				quickProductionButton.icon = commons.statusbar().gearNormal;
				quickProductionButton.text = "-";
			}
			
			if (quickProduction.visible()) {
				quickProduction.update();
				quickProduction.location(quickProductionButton.x + MENU_ICON_WIDTH - quickProduction.width, quickProductionButton.y + quickProductionButton.height);
				if (quickProduction.x < 0) {
					quickProduction.x = 0;
				}
			}
			
			setTooltip(quickProductionButton, "statusbar.quickproduction");
			
			if (player().pauseProduction) {
				quickProductionButton.textVisible = blink;
				quickProductionButton.textColor = TextRenderer.RED;
			} else {
				quickProductionButton.textVisible = true;
				quickProductionButton.textColor = TextRenderer.YELLOW;
			}
			
			quickResearchButton.visible(world().level > 2);
			if (world().level > 2) {

				String rs = "-";
				ResearchType rt = player().runningResearch();
				boolean mayBlink = false;
				if (rt != null) {
					Research r = player().getResearch(rt);
					if (r != null) {
						rs = String.format("%.1f%%", r.getPercent(player().traits));
						mayBlink |= r.state == ResearchState.LAB || r.state == ResearchState.MONEY || r.state == ResearchState.STOPPED;
						if (mayBlink) {
							setTooltip(quickResearchButton, "statusbar.quickresearch.problem." + r.state, rt.longName);
						}
					}
				}
				
				quickResearchButton.text = rs;
				if (player().pauseResearch) {
					quickResearchButton.textVisible = blink;
					quickResearchButton.textColor = TextRenderer.RED;
				} else {
					quickResearchButton.textVisible = blink || !mayBlink;
					quickResearchButton.textColor = TextRenderer.YELLOW;
				}
				if (rt != null || isResearchAvailable()) {
					quickResearchButton.icon = commons.statusbar().researchLight;
					if (!mayBlink) {
						if (rt != null) {
							setTooltip(quickResearchButton, "statusbar.quickresearch.running", rt.longName);
						} else {
							setTooltip(quickResearchButton, "statusbar.quickresearch.available");
						}
					}
				} else {
					quickResearchButton.icon = commons.statusbar().researchNormal;
					if (!mayBlink) {
						setTooltip(quickResearchButton, "statusbar.quickresearch.none");
					}
				}
				if (quickResearch.visible()) {
					quickResearch.update();
					quickResearch.location(quickResearchButton.x + MENU_ICON_WIDTH - quickResearch.width, quickResearchButton.y + quickResearchButton.height);
					if (quickResearch.x < 0) {
						quickResearch.x = 0;
					}
				}
			} else {
				quickResearch.visible(false);
				setTooltip(quickResearchButton, null);
			}
		} else {
			quickProductionButton.visible(false);
			quickResearchButton.visible(false);
			quickResearch.visible(false);
			quickProduction.visible(false);
			setTooltip(quickResearchButton, null);
			setTooltip(quickProductionButton, null);
		}
		quickProductionButton.location(getInnerWidth() - MENU_ICON_WIDTH - quickProductionButton.width, top.y);
		quickResearchButton.location(quickProductionButton.x - quickResearchButton.width, top.y);
		
		// tooltips
		setTooltip(pause, "statusbar.pause.tooltip");
		setTooltip(speed1, "statusbar.speed1.tooltip");
		setTooltip(speed2, "statusbar.speed2.tooltip");
		setTooltip(speed4, "statusbar.speed4.tooltip");
		setTooltip(money, "statusbar.money.tooltip", player().money());
		setTooltip(objectivesButton, "statusbar.objectives.tooltip");
		if (commons.battleMode) {
			setTooltip(notification, null);
		} else {
			setTooltip(notification, "statusbar.notification.tooltip");
		}
		setTooltip(notificationHistory.clear, "statusbar.notification.clear.tooltip");
		setTooltip(menu, "statusbar.menu.tooltip");
	}
	@Override
	public void onEndGame() {
		notification.currentMessage = null;
		animationStep = 0;
		quickResearch.clear();
		quickProduction.clear();
	}
	/** The moving status indicator. */
	class MovingNotification extends UIComponent {
		/** The current message. */
		Message currentMessage;
		@Override
		public void draw(Graphics2D g2) {
			Shape save0 = g2.getClip();
			g2.clipRect(0, 0, width, height);
			List<Planet> attacks = playerUnderAttack();
			if (!attacks.isEmpty()) {
				Planet p = attacks.get(attackListIndex);
				String txt = format("message.enemy_fleet_detected_at", p.name);
				int w = commons.text().getTextWidth(10, txt);
				commons.text().paintTo(g2, (width - w) / 2, 1, 10, blink ? TextRenderer.RED : TextRenderer.ORANGE, txt);
			} else
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
					renderX = (int)(width / 2d - acc / 2 * time * time);
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
			}
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
				buttonSound(SoundType.CLICK_MEDIUM_2);
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
			List<Planet> pa = playerUnderAttack();
			if (errorText == null && pa.isEmpty()) {
				if (notification.currentMessage != null) {
					if (animationStep >= accelerationStep * 2 + stayStep) {
						player().removeMessage(notification.currentMessage);
						player().addHistory(notification.currentMessage);
						notification.currentMessage = null;
						animationStep = 0;
//						askRepaint();
						doAnimation();
						return;
					}
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
				} else
				if (animationStep == 0) {
					Message msg = player().peekMessage();
					if (msg != null) {
						notification.currentMessage = msg;
//						if (msg.sound != null) {
//							computerSound(msg.sound);
//						}
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
			blinkCounter++;
			if (blinkCounter % 6 == 0) {
				blink = !blink;
				askRepaint();
			}
			if (blinkCounter % 24 == 0) {
				attackListIndex++;
				if (attackListIndex >= pa.size()) {
					attackListIndex = 0;
				}
				askRepaint();
			}
		}
	}
	/**
	 * Collect the current player's attack targets.
	 * @return the list of planet ids under attack
	 */
	List<Planet> playerUnderAttack() {
		List<Planet> result = new ArrayList<>();
		for (Fleet f : player().fleets.keySet()) {
			if (f.owner != player() 
					&& f.targetPlanet() != null
					&& f.targetPlanet().owner == player()
					&& f.mode == FleetMode.ATTACK) {
				result.add(f.targetPlanet());
			}
		}
		return result;
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (commons.force) {
			return false;
		}
		if (e.has(Type.UP) && screenMenu.visible() 
				&& !e.within(screenMenu.x, screenMenu.y, screenMenu.width, screenMenu.height)) {
			screenMenu.visible(false);
			return true;
		}
		if (e.has(Type.DOWN) && quickResearch.visible()
				&& !e.within(quickResearch.x, quickResearch.y, quickResearch.width, quickResearch.height)) {
			quickResearch.visible(false);
			boolean rep = mouse(e);
			boolean reshown = quickResearch.visible();
			quickResearch.visible(false);
			return reshown || rep;
		}
		if (e.has(Type.DOWN) && quickProduction.visible()
				&& !e.within(quickProduction.x, quickProduction.y, quickProduction.width, quickProduction.height)) {
			quickProduction.visible(false);
			boolean rep = mouse(e);
			boolean reshown = quickProduction.visible();
			quickProduction.visible(false);
			return reshown || rep;
		}
		if (e.has(Type.DOWN) 
				&& incomingMessage.contains(e.x, e.y) 
				&& hasUnseenMessage()
				&& !commons.battleMode) {
			if (isDiplomaticCall()) {
				DiplomacyScreen bs = (DiplomacyScreen)displayPrimary(Screens.DIPLOMACY);
				bs.receive();
			} else {
				BridgeScreen bs = (BridgeScreen)displayPrimary(Screens.BRIDGE);
				bs.resumeAfterVideo = !commons.simulation.paused();
				commons.pause();
				bs.displayReceive();
			}
			return true;
		}
		if (e.has(Type.DOWN) && menu.within(e)) {
			buttonSound(SoundType.CLICK_MEDIUM_2);
			screenMenu.highlight = -1;
			screenMenu.visible(true);
			return true;
		}
		return super.mouse(e);
	}
	/**
	 * Check if the given panel is visible and the mouse action is inside it.
	 * @param e the mouse event
	 * @param comp the component to test
	 * @return true if inside the visible panel
	 */
	boolean inPanel(UIMouse e, UIComponent comp) {
		return comp.visible() && e.within(comp.x, comp.y, comp.width, comp.height);
	}
	/** Toggle the quick research panel. */
	public void toggleQuickResearch() {
		quickResearch.visible(!quickResearch.visible());
		if (quickResearch.visible()) {
			quickProduction.visible(false);
		}
	}
	/** Toggle the quick research panel. */
	public void toggleQuickProduction() {
		quickProduction.visible(!quickProduction.visible());
		if (quickProduction.visible()) {
			quickResearch.visible(false);
		}
	}
	/**
	 * Check if the event is over one of the quick panels?
	 * @param e the event
	 * @return true if over an event
	 */
	public boolean overPanel(UIMouse e) {
		if (quickResearch.visible()) {
			if (quickResearch.within(e)) {
				return true;
			}
		}
		if (quickProduction.visible()) {
			if (quickProduction.within(e)) {
				return true;
			}
		}
		if (screenMenu.visible()) {
			if (screenMenu.within(e)) {
				return true;
			}
		}
		if (notificationHistory.visible()) {
			if (notificationHistory.within(e)) {
				return true;
			}
		}
		return false;
	}
}
