/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Pair;
import hu.openig.core.SimulationSpeed;
import hu.openig.mechanics.DefaultAIControls;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.Message;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Production;
import hu.openig.model.Research;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.Screens;
import hu.openig.model.SelectionMode;
import hu.openig.model.SoundType;
import hu.openig.model.VideoMessage;
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
import hu.openig.ui.UITextButton;
import hu.openig.ui.VerticalAlignment;
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.Timer;



/**
 * Displays and handles the status bar screen.
 * @author akarnokd, 2010.01.11.
 */
public class StatusbarScreen extends ScreenBase {
	/** Menu icon widths. */
	private static final int MENU_ICON_WIDTH = 30;
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
		
		quickResearch = new QuickResearchPanel();
		quickResearch.visible(false);
		quickResearch.z = 2;
		
		objectivesButton = new UIImageButton(commons.statusbar().objectives);
		objectivesButton.z = 1;
		objectivesButton.onClick = new Action0() {
			@Override
			public void invoke() {
				objectives.visible(!objectives.visible());
				buttonSound(SoundType.CLICK_MEDIUM_2);
			}
		};
		
		quickResearchButton = new QuickPanelButton("00000000");
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
		quickProductionButton = new QuickPanelButton("00000");
		quickProductionButton.onLeftClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				// TODO show panel
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
		
		objectives.location(5, 30 + achievementSize);
		
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
		
		if (overlay != null) {
			g2.setColor(overlay);
			g2.fillRect(0, 0, width, height);
		}
	}
	/** @return Check if any of the technologies are available for research. */
	boolean isResearchAvailable() {
		for (ResearchType rt : world().researches.values()) {
			if (rt.race.contains(player().race) && world().canResearch(rt)) {
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
		for (Map<ResearchType, Production> prods : player().production.values()) {
			for (Production prod : prods.values()) {
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
		for (VideoMessage vm : world().scripting.getReceiveMessages()) {
			if (!vm.seen) {
				return true;
			}
		}
		return isDiplomaticCall();
	}
	/** @return Is this a diplomatic call. */
	boolean isDiplomaticCall() {
		for (String p2 : player().offers.keySet()) {
			if (!world().players.get(p2).ownPlanets().isEmpty()) {
				return true;
			}
		}
		return false;
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
				effectSound(SoundType.ACHIEVEMENT);
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
			setTooltip(quickProductionButton, "statusbar.quickproduction");
			if (world().level > 2) {
				quickResearchButton.visible(true);

				String rs = "-";
				ResearchType rt = player().runningResearch();
				boolean mayBlink = false;
				if (rt != null) {
					Research r = player().research.get(rt);
					if (r != null) {
						rs = String.format("%.1f%%", r.getPercent());
						mayBlink = r.state == ResearchState.LAB || r.state == ResearchState.MONEY || r.state == ResearchState.STOPPED;
						if (mayBlink) {
							setTooltip(quickResearchButton, "statusbar.quickresearch.problem." + r.state, rt.longName);
						}
					}
				}
				
				quickResearchButton.text = rs;
				quickResearchButton.textVisible = blink || !mayBlink; 
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
				quickResearch.update();
				quickResearch.location(quickResearchButton.x + MENU_ICON_WIDTH - quickResearch.width, quickResearchButton.y + quickResearchButton.height);
				
			} else {
				quickResearch.visible(false);
				setTooltip(quickResearchButton, null);
			}
		} else {
			quickProductionButton.visible(false);
			quickResearchButton.visible(false);
			quickResearch.visible(false);
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
		setTooltip(money, "statusbar.money.tooltip", player().money);
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
		List<Planet> result = U.newArrayList();
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
				commons.control().displayOptions();
				LoadSaveScreen scr = commons.control().getScreen(Screens.LOAD_SAVE);
				scr.maySave(!commons.battleMode);
				scr.displayPage(SettingsPage.LOAD_SAVE);
				break;
			case 13:
				commons.control().displayOptions();
				scr = commons.control().getScreen(Screens.LOAD_SAVE);
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
					buttonSound(SoundType.CLICK_MEDIUM_2);
					doScrollUp();
				}
			};
			scrollUp.setHoldDelay(250);
			scrollDown = new UIImageButton(commons.common().moveDown);
			scrollDown.onClick = new Action0() {
				@Override
				public void invoke() {
					buttonSound(SoundType.CLICK_MEDIUM_2);
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
					buttonSound(SoundType.CLICK_MEDIUM_2);
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
					g2.drawImage(msg.targetFleet.owner.fleetIcon, dx, y + dy, null);
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
			if (!commons.battleMode && e.has(Type.DOUBLE_CLICK) && e.x < width - 31) {
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
			
			int tx = 16;
			int ty = 0;
			if (commons.control().primary() == Screens.COLONY) {
				ty = 110;
			}
			
			g2.translate(tx, ty);
			try {
				int background = 0xC0000000;
				
				List<Objective> objs = world().scripting.currentObjectives();
				
				if (objs.size() == 0) {
					int w = commons.text().getTextWidth(14, get("no_objectives"));
					g2.setColor(new Color(background, true));
	
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
				
				g2.setColor(new Color(background, true));
				
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
			} finally {			
				g2.translate(-tx, -ty);
			}
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
			if (o.description != null && !o.description.isEmpty()) {
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
			int descriptionWidth = o.description != null ? commons.text().getTextWidth(10, o.description) : 0;
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
			int descriptionWidth = o.description != null ? commons.text().getTextWidth(10, o.description) : 0;
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
	/**
	 * Check player planets for statistics regarding only research matters.
	 * @return the planet statistics
	 */
	PlanetStatistics evaluatePlanetsForResearch() {
		PlanetStatistics ps = new PlanetStatistics();
		
		for (Planet p : player().ownPlanets()) {
			for (Building b : p.surface.buildings) {
				if (Building.isOperational(b.getEfficiency())) {
					if (b.hasResource("civil")) {
						ps.civilLabActive += b.getResource("civil");
					}
					if (b.hasResource("mechanical")) {
						ps.mechLabActive += b.getResource("mechanical");
					}
					if (b.hasResource("computer")) {
						ps.compLabActive += b.getResource("computer");
					}
					if (b.hasResource("ai")) {
						ps.aiLabActive += b.getResource("ai");
					}
					if (b.hasResource("military")) {
						ps.milLabActive += b.getResource("military");
					}
				}
				if (b.hasResource("civil")) {
					ps.civilLab += b.getResource("civil");
				}
				if (b.hasResource("mechanical")) {
					ps.mechLab += b.getResource("mechanical");
				}
				if (b.hasResource("computer")) {
					ps.compLab += b.getResource("computer");
				}
				if (b.hasResource("ai")) {
					ps.aiLab += b.getResource("ai");
				}
				if (b.hasResource("military")) {
					ps.milLab += b.getResource("military");
				}
			}
			
			ps.planetCount++;
		}
		
		return ps;
	}
	/**
	 * The quick research panel.
	 * @author akarnokd, 2012.06.23.
	 */
	public class QuickResearchPanel extends UIContainer {
		/** The current research status. */
		UILabel currentResearchName;
		/** The current research status. */
		UILabel currentResearchStatus;
		/** The current research money. */
		UILabel currentResearchMoney;
		/** Stop the current research. */
		UITextButton currentResearchStop;
		/** Adjust money. */
		UIImageButton moneyButton;
		/** The margin inside the panel. */
		static final int MARGIN = 6;
		/** The labels for the current available set of researches. */
		final List<List<QuickResearchLabel>> researches = U.newArrayList();
		/** The top divider y. */
		int topDivider;
		/** The middle divider. */
		int middleDivider;
		/** The middle divider. */
		int bottomDivider;
		/** The last mouse event on the funding button. */
		UIMouse moneyMouseLast;
		/** Description of the currently hovered research. */
		UILabel hoverResearchDescription;
		/** Description of the currently hovered research. */
		UILabel hoverResearchTitle;
		/** The current hover text title and details. */
		ResearchType currentText;
		/** The tip for the current research highlighted. */
		UILabel tip;
		/** The lab-active main label. */
		UILabel labActive;
		/** The lab-required main label. */
		UILabel labRequired;
		/** The lab titles. */
		final List<UILabel> labTitles = U.newArrayList();
		/** The active counts. */
		final List<UILabel> labActives = U.newArrayList();
		/** The required counts. */
		final List<UILabel> labRequireds = U.newArrayList();
		/** Initialize the fields. */
		public QuickResearchPanel() {
			currentResearchName = new UILabel("", 14, commons.text());
			currentResearchStatus = new UILabel("", 10, commons.text());
			currentResearchMoney = new UILabel("", 10, commons.text());
			currentResearchStop = new UITextButton(get("quickresearch.stop"), 14, commons.text()) {
				boolean dragOver;
				@Override
				public boolean mouse(UIMouse e) {
					if (e.has(Type.DRAG)) {
						dragOver = true;
					}
					if (e.has(Type.LEAVE)) {
						dragOver = false;
					}
					if (e.has(Type.UP) && dragOver) {
						dragOver = false;
						if (onClick != null) {
							onClick.invoke();
						}
					}
					return super.mouse(e);
				}
			};
			currentResearchStop.onClick = new Action0() {
				@Override
				public void invoke() {
					if (player().runningResearch() != null) {
						player().research.get(player().runningResearch()).state = ResearchState.STOPPED;
					}
					player().runningResearch(null);
					screenSound(SoundType.STOP_RESEARCH);
					quickResearch.visible(false);
					askRepaint();
				}
			};
			
			moneyButton = new UIImageButton(commons.research().fund) {
				@Override
				public boolean mouse(UIMouse e) {
					moneyMouseLast = e;
					super.mouse(e);
					return true;
				};
			};
			moneyButton.onClick = new Action0() {
				@Override
				public void invoke() {
					buttonSound(SoundType.CLICK_HIGH_3);
					doAdjustMoney(2.0f * (moneyMouseLast.x) / moneyButton.width - 1);
					askRepaint();
				}
			};
			moneyButton.setHoldDelay(100);
			moneyButton.setDisabledPattern(commons.common().disabledPattern);

			hoverResearchDescription = new UILabel("", 10, commons.text());
			hoverResearchDescription.wrap(true);
			
			hoverResearchTitle = new UILabel("", 10, commons.text());
			hoverResearchTitle.color(TextRenderer.RED);
			hoverResearchTitle.horizontally(HorizontalAlignment.CENTER);
			
			labActive = new UILabel(get("quickresearch.lab_available"), 10, commons.text());
			labRequired = new UILabel(get("quickresearch.lab_required"), 10, commons.text());
			
			for (String s : Arrays.asList("civ", "mech", "comp", "ai", "mil")) {
				UILabel l1 = new UILabel(get("quickresearch." + s), 10, commons.text());
				l1.horizontally(HorizontalAlignment.CENTER);
				add(l1);
				labTitles.add(l1);
				
				UILabel l2 = new UILabel("", 10, commons.text());
				l2.horizontally(HorizontalAlignment.CENTER);
				add(l2);
				labActives.add(l2);

				UILabel l3 = new UILabel("", 10, commons.text());
				l3.horizontally(HorizontalAlignment.CENTER);
				add(l3);
				labRequireds.add(l3);

			}
			
			tip = new UILabel("", 10, commons.text());
			tip.color(0xFFE0E0E0);
			tip.wrap(true);
			
			addThis();
		}
		@Override
		public void draw(Graphics2D g2) {
			g2.setColor(new Color(0, 0, 0, 192));
			g2.fillRect(0, 0, width, height);
			g2.setColor(new Color(192, 192, 192));
			g2.drawRect(0, 0, width - 1, height - 1);
			
			g2.drawLine(0, topDivider, width - 1, topDivider);
			g2.drawLine(0, middleDivider, width - 1, middleDivider);
			g2.drawLine(0, bottomDivider, width - 1, bottomDivider);
			super.draw(g2);
		}
		/** Update the contents of the panel. */
		public void update() {
			
			ResearchType ar = player().runningResearch();
			if (ar != null) {
				Research rs = player().research.get(ar);
				
				currentResearchName.text(ar.name, true);
				currentResearchName.color(TextRenderer.YELLOW);

				switch (rs.state) {
				case RUNNING:
					currentResearchStatus.text(format("researchinfo.progress.running", String.format("%.1f", rs.getPercent())), true);
					break;
				case STOPPED:
					currentResearchStatus.text(format("researchinfo.progress.paused", String.format("%.1f", rs.getPercent())), true);
					break;
				case LAB:
					currentResearchStatus.text(format("researchinfo.progress.lab", String.format("%.1f", rs.getPercent())), true);
					break;
				case MONEY:
					currentResearchStatus.text(format("researchinfo.progress.money", String.format("%.1f", rs.getPercent())), true);
					break;
				default:
					currentResearchStatus.text("");
				}
				currentResearchMoney.text(rs.assignedMoney + "/" + rs.remainingMoney + " cr", true);
				
				currentResearchStop.visible(true);
				moneyButton.visible(true);
			} else {
				currentResearchName.text(get("quickresearch.no_active"), true);
				currentResearchName.color(0xFFE0E0E0);
				currentResearchStatus.text("", true);
				currentResearchMoney.text("", true);
				currentResearchStop.visible(false);
				moneyButton.visible(false);
			}

			currentResearchStop.location(0, MARGIN);

			currentResearchName.location(MARGIN, MARGIN + (currentResearchStop.height - currentResearchName.height) / 2);
			currentResearchStatus.location(currentResearchName.x + currentResearchName.width + 3 * MARGIN, currentResearchName.y + 2);
			moneyButton.location(currentResearchStatus.x + currentResearchStatus.width + 3 * MARGIN, MARGIN + 1);
			currentResearchMoney.location(moneyButton.x + moneyButton.width + 3 * MARGIN, currentResearchName.y + 2);
			currentResearchStop.location(currentResearchMoney.x + currentResearchMoney.width + 3 * MARGIN, MARGIN);
			
			// ---------------------------------------------------------------------
			// collect startable researches
			Map<ResearchMainCategory, List<Pair<ResearchType, Integer>>> columns = U.newLinkedHashMap();
			for (ResearchMainCategory mcat : ResearchMainCategory.values()) {
				columns.put(mcat, U.<Pair<ResearchType, Integer>>newArrayList());
			}
			
			PlanetStatistics ps = evaluatePlanetsForResearch();
			
			for (ResearchType rt : world().researches.values()) {
				if (rt.race.contains(player().race) && rt != ar) {
					if (world().canResearch(rt)) {
						columns.get(rt.category.main).add(Pair.of(rt, world().getResearchColor(rt, ps)));
					}
				}
			}

			boolean newLines = false;
			boolean anyOver = false;
			// create labels for researches
			int i = 0;
			for (ResearchMainCategory mcat : ResearchMainCategory.values()) {
				List<Pair<ResearchType, Integer>> lst = columns.get(mcat);

				// reorder researches
				Collections.sort(lst, new Comparator<Pair<ResearchType, Integer>>() {
					@Override
					public int compare(Pair<ResearchType, Integer> o1,
							Pair<ResearchType, Integer> o2) {
						int c = o1.first.category.ordinal() - o2.first.category.ordinal();
						if (c == 0) {
							c = o1.first.index - o2.first.index;
						}
						
						return c;
					}
				});
				
				
				if (researches.size() == i) {
					researches.add(U.<QuickResearchLabel>newArrayList());
				}
				List<QuickResearchLabel> catlist = researches.get(i);

				for (int j = 0; j < lst.size(); j++) {
					final Pair<ResearchType, Integer> ri = lst.get(j);
					QuickResearchLabel cl = null;
					if (j == catlist.size()) {
						cl = new QuickResearchLabel("", 10, commons.text());
						catlist.add(cl);
						add(cl);
						newLines = true;
					} else {
						cl = catlist.get(j);
					}
					
					Research rs1 = player().research.get(ri.first);
					
					if (rs1 == null) {
						cl.text(ri.first.name, true);
					} else {
						cl.text(String.format("%s - %d%%", ri.first.name, (int)rs1.getPercent()), true);
					}
					cl.color(ri.second);
					cl.hoverColor(TextRenderer.WHITE);
					cl.height = cl.textSize() + MARGIN;
					cl.vertically(VerticalAlignment.MIDDLE);
					cl.onPress = new Action0() {
						@Override
						public void invoke() {
							new DefaultAIControls(player()).actionStartResearch(ri.first, world().config.researchMoneyPercent / 1000d);
							quickResearch.visible(false);
							askRepaint();
							player().currentResearch(ri.first);
							if (commons.control().secondary() == Screens.RESEARCH
									|| commons.control().secondary() == Screens.PRODUCTION) {
								ResearchProductionScreen rps = commons.control().getScreen(commons.control().secondary());
								rps.playAnim(ri.first);
							}
						}
					};
					cl.description = Pair.of(ri.first.longName + "  (" + ri.first.researchCost + " cr)", ri.first.description);
					if (cl.over) {
						currentText = ri.first;
						anyOver |= cl.over;
					}
				}
				// remove excess lines
				for (int j = catlist.size() - 1; j >= lst.size() ; j--) {
					components.remove(catlist.remove(j));
					newLines = true;
				}
				
				i++;
			}

			if (currentText == null || !anyOver) {
				if (ar != null) {
					currentText = ar;
				} else {
					currentText = null;
				}
			}

			if (newLines) {
				commons.control().moveMouse();
			}
			
			int y0 = currentResearchStop.y + currentResearchStop.height + MARGIN;
			topDivider = y0 - MARGIN / 2;
			int x = MARGIN;
			middleDivider = 0;
			for (List<QuickResearchLabel> lst : researches) {
				int y = y0;
				int mw = 0;
				for (UILabel l : lst) {
					l.x = x;
					l.y = y;
					
					mw = Math.max(mw, l.width);
					y += l.height;
				}
				
				x += MARGIN * 3 + mw;
				middleDivider = Math.max(y, middleDivider);
			}
			middleDivider += MARGIN / 2;
			
			hoverResearchTitle.location(MARGIN, middleDivider + MARGIN / 2);
			hoverResearchTitle.width = 200;

			hoverResearchDescription.location(MARGIN, hoverResearchTitle.y + hoverResearchTitle.height + MARGIN / 2);
			hoverResearchDescription.height = 1;
			// fill in lab counts

			setActives(labActives, 0, ps.civilLabActive, ps.civilLab, TextRenderer.YELLOW);
			setActives(labActives, 1, ps.mechLabActive, ps.mechLab, TextRenderer.YELLOW);
			setActives(labActives, 2, ps.compLabActive, ps.compLab, TextRenderer.YELLOW);
			setActives(labActives, 3, ps.aiLabActive, ps.aiLab, TextRenderer.YELLOW);
			setActives(labActives, 4, ps.milLabActive, ps.milLab, TextRenderer.YELLOW);
			
			if (currentText != null) {
				setRequireds(labRequireds, 0, ps.civilLab, currentText.civilLab, TextRenderer.RED);
				setRequireds(labRequireds, 1, ps.mechLab, currentText.mechLab, TextRenderer.RED);
				setRequireds(labRequireds, 2, ps.compLab, currentText.compLab, TextRenderer.RED);
				setRequireds(labRequireds, 3, ps.aiLab, currentText.aiLab, TextRenderer.RED);
				setRequireds(labRequireds, 4, ps.milLab, currentText.milLab, TextRenderer.RED);
			}
			
			// adjust size ---------------------------------------------------------------
			
			int dw0 = Math.max(labActive.width, labRequired.width) + 3 * MARGIN;
			labActive.visible(false);
			labRequired.visible(false);
			tip.visible(false);
			int dw1 = 0;
			for (UILabel lbl : U.concat(labTitles, labActives, labRequireds)) {
				lbl.visible(false);
				dw1 = Math.max(dw1, lbl.width);
			}

			int dw2 = dw0 + 5 * dw1 + 8 * MARGIN;
			hoverResearchDescription.width = Math.max(200, dw2);
			
			int mw = 0;
			int mh = 0;
			for (UIComponent comp : components) {
				if (comp.visible()) {
					mw = Math.max(mw, comp.x + comp.width);
					mh = Math.max(mh, comp.y + comp.height);
				}
			}
			
			hoverResearchDescription.width = mw - MARGIN;
			hoverResearchTitle.width = hoverResearchDescription.width;

			if (currentText != null) {
				hoverResearchDescription.text(currentText.description);
				hoverResearchTitle.text(currentText.longName);
				hoverResearchDescription.height = hoverResearchDescription.getWrappedHeight();
			} else {
				hoverResearchDescription.text("");
				hoverResearchDescription.height = 0;
				hoverResearchTitle.text(get("quickresearch.no_active"));
			}
			
			bottomDivider = hoverResearchDescription.y + hoverResearchDescription.height + MARGIN / 2;
			mh = Math.max(mh, hoverResearchDescription.y + hoverResearchDescription.height);
			
			// bottom area
			
			
			labActive.location(MARGIN, mh + labActive.height + 2 * MARGIN);
			labActive.visible(true);
			
			labRequired.location(MARGIN, labActive.y + labActive.height + MARGIN);
			labRequired.visible(currentText != null);
			
			
			int widthPart = (mw - MARGIN - Math.max(labActive.width, labRequired.width)) / labActives.size();
			int ii = 0;
			for (UILabel ul : labActives) {
				ul.location(labActive.x 
						+ Math.max(labActive.width, labRequired.width) + 3 * MARGIN + ii * widthPart, labActive.y);
				ii++;
				ul.visible(true);
			}
			
			ii = 0;
			for (UILabel ul : labRequireds) {
				ul.location(labActive.x 
						+ Math.max(labActive.width, labRequired.width) + 3 * MARGIN + ii * widthPart, labRequired.y);
				ii++;
				ul.visible(currentText != null);
			}

			ii = 0;
			for (UILabel ul : labTitles) {
				ul.location(labActive.x 
						+ Math.max(labActive.width, labRequired.width) + 3 * MARGIN + ii * widthPart, mh + MARGIN);
				ii++;
				ul.visible(true);
			}
			if (currentText != null && !currentText.hasEnoughLabs(ps)) {
				tip.location(MARGIN, labRequired.y + labRequired.height + MARGIN);
				
				int reqPlanet = currentText.labCount();
				
				if (reqPlanet > ps.planetCount) {
					tip.text(get("quickresearch.more_planets"));
				} else 
				if (currentText.hasEnoughLabsBuilt(ps)) {
					tip.text(get("quickresearch.check_labs"));
				} else {
					tip.text(get("quickresearch.reorg_labs"));
				}
				tip.width = mw - MARGIN;
				tip.height = tip.getWrappedHeight();
 				
				tip.visible(true);
			}

			// readjust bounds again
			
			mw = 0;
			mh = 0;
			for (UIComponent comp : components) {
				if (comp.visible()) {
					mw = Math.max(mw, comp.x + comp.width);
					mh = Math.max(mh, comp.y + comp.height);
				}
			}
			
			width = mw + MARGIN;
			height = mh + MARGIN;
		}
		/**
		 * Set an active label based on the numerical values.
		 * @param list the list
		 * @param index the index
		 * @param active the active count
		 * @param total the total count
		 * @param color the color to use if active < total
		 */
		void setActives(List<UILabel> list, int index, int active, int total, int color) {
			UILabel l = list.get(index);
			if (active < total) {
				l.color(color);
				l.text(active + "/" + total, true);
			} else {
				l.color(TextRenderer.GREEN);
				l.text(Integer.toString(active), true);
			}
			
		}
		/**
		 * Set an active label based on the numerical values.
		 * @param list the list
		 * @param index the index
		 * @param active the active count
		 * @param total the total count
		 * @param color the color to use if active < total
		 */
		void setRequireds(List<UILabel> list, int index, int active, int total, int color) {
			UILabel l = list.get(index);
			l.text(Integer.toString(total), true);
			if (active < total) {
				l.color(color);
			} else {
				l.color(TextRenderer.GREEN);
			}
			
		}
		/** Clear contents. */
		void clear() {
			researches.clear();
		}
		@Override
		public UIComponent visible(boolean state) {
			for (List<QuickResearchLabel> cs : researches) {
				for (QuickResearchLabel c : cs) {
					c.over &= state;
				}
			}
			return super.visible(state);
		}
		/**
		 * Adjust money based on the scale.
		 * @param scale the scale factor -1.0 ... +1.0
		 */
		void doAdjustMoney(float scale) {
			Research r = player().research.get(player().runningResearch());
			if (r != null) {
				r.assignedMoney += scale * r.type.researchCost / 20;
				r.assignedMoney = Math.max(Math.min(r.assignedMoney, r.remainingMoney), r.remainingMoney / 8);
			}
		}
	}
	/**
	 * A quick research label with extra mouse behavior. 
	 * @author akarnokd, 2012.06.23.
	 */
	class QuickResearchLabel extends UILabel {
		/** The description associated with this label. */
		public Pair<String, String> description;
		/** Mouse pressed. */
		boolean dragOver;
		/**
		 * Initialize the label.
		 * @param text the text to display
		 * @param size the text size
		 * @param tr the text renderer
		 */
		public QuickResearchLabel(String text, int size, TextRenderer tr) {
			super(text, size, tr);
		}
		/**
		 * Initialize the label.
		 * @param text the text to display
		 * @param size the text size
		 * @param width the width of the label
		 * @param tr the text renderer
		 */
		public QuickResearchLabel(String text, int size, int width, TextRenderer tr) {
			super(text, size, width, tr);
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.ENTER) || e.has(Type.LEAVE)) {
				super.mouse(e);
				return true;
			}
			if (e.has(Type.DRAG)) {
				dragOver = true;
			}
			if (e.has(Type.LEAVE)) {
				dragOver = false;
			}
			if (e.has(Type.UP) && dragOver) {
				dragOver = false;
				if (onPress != null) {
					onPress.invoke();
				}
			}
			return super.mouse(e);
		}
	}
	/** Toggle the quick research panel. */
	public void toggleQuickResearch() {
		quickResearch.visible(!quickResearch.visible());
	}
	/**
	 * A quick panel's button with a numerical value displayed. 
	 * @author akarnokd, 2012.06.25.
	 */
	class QuickPanelButton extends UIComponent {
		/** The left click action. */
		public Action0 onLeftClick;
		/** The right click action. */
		public Action0 onRightClick;
		/** The text to display. */
		public String text;
		/** The image to display. */
		public BufferedImage icon;
		/** The max text width. */
		int textMaxWidth;
		/** Is the text visible? */
		public boolean textVisible;
		/**
		 * Initialize the button.
		 * @param pattern the pattern
		 */
		public QuickPanelButton(String pattern) {
			textMaxWidth = commons.text().getTextWidth(10, pattern);
			width = textMaxWidth + MENU_ICON_WIDTH;
			height = 20;
		}
		@Override
		public void draw(Graphics2D g2) {
			if (textVisible) {
				int w = commons.text().getTextWidth(10, text);
				int x0 = MENU_ICON_WIDTH + (textMaxWidth - w) / 2;
				commons.text().paintTo(g2, x0, 0 + 4, 10, TextRenderer.YELLOW, text);
			}
			
			g2.drawImage(commons.statusbar().iconBack, 0, 0, null);
			g2.drawImage(icon, 0 + 7, 0 + 3, null);
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOWN)) {
				if (e.has(Button.LEFT) && onLeftClick != null) {
					onLeftClick.invoke();
					return true;
				}
				if (e.has(Button.RIGHT) && onRightClick != null) {
					onRightClick.invoke();
					return true;
				}
			}
			return super.mouse(e);
		}
	}
}
