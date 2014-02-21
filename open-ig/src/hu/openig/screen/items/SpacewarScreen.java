/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Difficulty;
import hu.openig.core.Func1;
import hu.openig.core.Location;
import hu.openig.core.Pair;
import hu.openig.core.SimulationSpeed;
import hu.openig.mechanics.BattleSimulator;
import hu.openig.model.BattleEfficiencyModel;
import hu.openig.model.BattleGroundProjector;
import hu.openig.model.BattleGroundShield;
import hu.openig.model.BattleInfo;
import hu.openig.model.BattleProjectile;
import hu.openig.model.BattleProjectile.Mode;
import hu.openig.model.BattleSpaceEntity;
import hu.openig.model.BattleSpaceLayout;
import hu.openig.model.Building;
import hu.openig.model.Chats.Chat;
import hu.openig.model.Chats.Node;
import hu.openig.model.Fleet;
import hu.openig.model.FleetStatistics;
import hu.openig.model.FleetTask;
import hu.openig.model.HasInventory;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.ModelUtils;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.Screens;
import hu.openig.model.SelectionBoxMode;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarExplosion;
import hu.openig.model.SpacewarObject;
import hu.openig.model.SpacewarProjectile;
import hu.openig.model.SpacewarScriptResult;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarStructure.StructureType;
import hu.openig.model.SpacewarWeaponPort;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.Tile;
import hu.openig.model.TraitKind;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.render.TextRenderer.TextSegment;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.panels.EquipmentConfigure;
import hu.openig.screen.panels.ThreePhaseButton;
import hu.openig.screen.panels.TwoPhaseButton;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The spacewar screen.
 * @author akarnokd, 2010.01.06.
 */
public class SpacewarScreen extends ScreenBase implements SpacewarWorld {
	/** Annotation to show a component on a specified panel mode and side. */
	@Retention(RetentionPolicy.RUNTIME)
	@interface Show {
		/** The panel mode. */
		PanelMode mode();
		/** Is the left side? */
		boolean left();
	}
	/** The panel mode. */
	enum PanelMode {
		/** Show ship status. */
		SHIP_STATUS,
		/** Show fleet statistics. */
		STATISTICS,
		/** Show ship information. */
		SHIP_INFORMATION,
		/** Show communicator. */
		COMMUNICATOR,
		/** Show movie. */
		MOVIE,
		/** The layout. */
		LAYOUT
	}
	/** Animated toggle button. */
	static class AnimatedRadioButton extends UIComponent {
		/** The phases. The 0th is the default when not selected. */
		BufferedImage[] phases;
		/** Is selected? */
		boolean selected;
		/** The current animation index. */
		int animationIndex;
		/** The action to perform on the press. */
		Action0 action;
		/**
		 * Constructor.
		 * @param phases the phases
		 */
		public AnimatedRadioButton(BufferedImage[] phases) {
			this.phases = phases;
			width = phases[0].getWidth();
			height = phases[0].getHeight();
		}
		/**
		 * Render the button.
		 * @param g2 the graphics object
		 */
		@Override
		public void draw(Graphics2D g2) {
			if (selected) {
				g2.drawImage(phases[1 + animationIndex], 
						(24 - phases[1 + animationIndex].getWidth()) / 2,
						(34 - phases[1 + animationIndex].getHeight()) / 2, null);
			} else {
				g2.drawImage(phases[0], (24 - phases[0].getWidth()) / 2,
						(34 - phases[0].getHeight()) / 2, null);
			}
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOWN)) {
				if (action != null) {
					action.invoke();
				}
				return true;
			}
			return false;
		}
	}
	/** The animation timer. */
	Closeable buttonTimer;
	/** The chat typing timer. */
	Closeable chatTimer;
	/** The animation timer. */
	long animationTimer;
	/** The group for the main buttons. */
	List<ThreePhaseButton> mainCommands;
	/** The view toggle buttons. */
	List<ThreePhaseButton> viewCommands;
	/** Zoom button. */
	TwoPhaseButton zoom;
	/** Pause button. */
	TwoPhaseButton pause;
	/** In pause mode. */
	boolean unpause;
	/** Retreat button. */
	TwoPhaseButton retreat;
	/** Confirm retreat. */
	TwoPhaseButton confirmRetreat;
	/** Stop retreat. */
	TwoPhaseButton stopRetreat;
	/** The space ships for animation. */
	final List<SpacewarStructure> structures = new ArrayList<>();
	/** The projectiles for animation. */
	final List<SpacewarProjectile> projectiles = new ArrayList<>();
	/** The space explosions for animation. */
	final List<SpacewarExplosion> explosions = new ArrayList<>();
	/** The location of the minimap. */
	final Rectangle minimap = new Rectangle();
	/** The location of the main window area. */
	final Rectangle mainmap = new Rectangle();
	/** The left inner panel. */
	final Rectangle leftPanel = new Rectangle();
	/** The right inner panel. */
	final Rectangle rightPanel = new Rectangle();
	/** The initial battle settings. */
	BattleInfo battle;
	/** Show the planet. */
	boolean planetVisible;
	/** The main map rendering offset X. */
	int offsetX;
	/** The main map rendering offset Y. */
	int offsetY;
	/** The rendering scale. */
	double scale = 1.0;
	/** The maximum scale. */
	static final double MAX_SCALE = 2;
	/** The operational space at 1:1 zoom. */
	final Rectangle space = new Rectangle(0, 0, 462 * 504 / 238, 504);
	/** Panning the view. */
	boolean panning;
	/** The last X coordinate. */
	int lastX;
	/** The last Y coordinate. */
	int lastY;
	/** View commands. */
	ThreePhaseButton viewCommand;
	/** View damages. */
	ThreePhaseButton viewDamage;
	/** View range. */
	ThreePhaseButton viewRange;
	/** View grids. */
	ThreePhaseButton viewGrid;
	/** We are drawing the selection box. */
	boolean selectionBox;
	/** The selection mode. */
	SelectionBoxMode selectionMode;
	/** The selection box start point. */
	Point selectionStart;
	/** The selection box end point. */
	Point selectionEnd;
	/** Left ship status button. */
	AnimatedRadioButton leftShipStatus;
	/** Right ship status button. */
	AnimatedRadioButton rightShipStatus;
	/** Left fleet statistics button. */
	AnimatedRadioButton leftStatistics;
	/** Right fleet statistics button. */
	AnimatedRadioButton rightStatistics;
	/** Left ship information list. */
	AnimatedRadioButton leftShipInformation;
	/** Right ship information list. */
	AnimatedRadioButton rightShipInformation;
	/** Left communicator window. */
	AnimatedRadioButton leftCommunicator;
	/** Right communicator window. */
	AnimatedRadioButton rightCommunicator;
	/** Left movie. */
	AnimatedRadioButton leftMovie;
	/** Right movie. */
	AnimatedRadioButton rightMovie;
	/** The list of animation buttons. */
	List<AnimatedRadioButton> animatedButtonsLeft = new ArrayList<>();
	/** The list of animation buttons. */
	List<AnimatedRadioButton> animatedButtonsRight = new ArrayList<>();
	/** The left equipment configuration. */
	@Show(mode = PanelMode.SHIP_STATUS, left = true)
	ShipStatusPanel leftStatusPanel;
	/** The right equipment configuration. */
	@Show(mode = PanelMode.SHIP_STATUS, left = false)
	ShipStatusPanel rightStatusPanel;
	/** We are in layout selection mode? */
	boolean layoutSelectionMode;
	/** Left statistics panel. */
	@Show(mode = PanelMode.STATISTICS, left = true)
	StatisticsPanel leftStatisticsPanel;
	/** Right statistics panel. */
	@Show(mode = PanelMode.STATISTICS, left = false)
	StatisticsPanel rightStatisticsPanel;
	/** The left ship information panel. */
	@Show(mode = PanelMode.SHIP_INFORMATION, left = true)
	ShipInformationPanel leftShipInfoPanel;
	/** The right ship information panel. */
	@Show(mode = PanelMode.SHIP_INFORMATION, left = false)
	ShipInformationPanel rightShipInfoPanel;
	/** The left ship information panel. */
	@Show(mode = PanelMode.COMMUNICATOR, left = true)
	ChatPanel leftChatPanel;
	/** The right ship information panel. */
	@Show(mode = PanelMode.COMMUNICATOR, left = false)
	ChatPanel rightChatPanel;
	/** The initial layout panel. */
	@Show(mode = PanelMode.LAYOUT, left = false)
	LayoutPanel layoutPanel;
	/** Fleet control button. */
	ThreePhaseButton stopButton;
	/** Fleet control button. */
	ThreePhaseButton moveButton;
	/** Fleet control button. */
	ThreePhaseButton kamikazeButton;
	/** Fleet control button. */
	ThreePhaseButton attackButton;
	/** Fleet control button. */
	ThreePhaseButton guardButton;
	/** Fleet control button. */
	ThreePhaseButton rocketButton;
	/** The middle selection panel. */
	SelectionPanel selectionPanel;
	/** The simulation delay on normal speed. */
	static final int SIMULATION_DELAY = 100;
	/** Keep the last info images. */
	static final int IMAGE_CACHE_SIZE = 8;
	/** Indicates if the attacker is placed on the right side. */
	boolean attackerOnRight;
	/** Scramble projectiles once. */
	final Set<SpacewarObject> scrambled = new HashSet<>();
	/** The sounds to play. */
	final Set<SoundType> soundsToPlay = new HashSet<>();
	/** The grouping of structures. */
	final Map<SpacewarStructure, Integer> groups = new HashMap<>();
	/** Info image cache. */
	final Map<String, BufferedImage> infoImages = new LinkedHashMap<String, BufferedImage>() {
		/** */
		private static final long serialVersionUID = 1723316137301684429L;

		@Override
		protected boolean removeEldestEntry(
				java.util.Map.Entry<String, BufferedImage> eldest) {
			return size() > IMAGE_CACHE_SIZE;
		}
	};
	/** The maximum right placement of units. */
	int maxRightPlacement;
	/** The current chat. */
	Chat chat;
	/** The current chat node. */
	Node node;
	/** The set of all initial players. */
	final Set<Player> allPlayerSet = new HashSet<>();
	/** The registry for fired rockets and their parents. */
	final Map<SpacewarStructure, SpacewarStructure> rocketParent = new HashMap<>();
	@Override
	public void onInitialize() {
		mainCommands = new ArrayList<>();
		
		stopButton = new ThreePhaseButton(33, 24, commons.spacewar().stop, commons.common().disabledPattern);
		stopButton.action = new Action0() {
			@Override
			public void invoke() {
				doStopSelectedShips();
				stopButton.selected = false;
			}
		};
		moveButton = new ThreePhaseButton(33 + 72, 24, commons.spacewar().move, commons.common().disabledPattern);

		kamikazeButton = new ThreePhaseButton(33, 24 + 35, commons.spacewar().kamikaze, commons.common().disabledPattern);
		kamikazeButton.action = new Action0() {
			@Override
			public void invoke() {
				doKamikaze();
				kamikazeButton.selected = false;
				enableSelectedFleetControls();
			}
		};
		attackButton = new ThreePhaseButton(33 + 72, 24 + 35, commons.spacewar().attack, commons.common().disabledPattern);
		guardButton = new ThreePhaseButton(33, 24 + 35 * 2, commons.spacewar().guard, commons.common().disabledPattern);
		guardButton.action = new Action0() {
			@Override
			public void invoke() {
				doSelectionGuard();
			}
		};
		
		rocketButton = new ThreePhaseButton(33 + 72, 24 + 35 * 2, commons.spacewar().rocket, commons.common().disabledPattern);
		
		mainCommands.add(stopButton);
		mainCommands.add(moveButton);
		mainCommands.add(kamikazeButton);
		mainCommands.add(attackButton);
		mainCommands.add(guardButton);
		mainCommands.add(rocketButton);
		
		viewCommands = new ArrayList<>();
		
		viewCommand = new ThreePhaseButton(33, 24 + 35 * 3, commons.spacewar().command, commons.common().disabledPattern);
		viewDamage = new ThreePhaseButton(33 + 72, 24 + 35 * 3, commons.spacewar().damage, commons.common().disabledPattern);
		viewRange = new ThreePhaseButton(33, 24 + 35 * 3 + 30, commons.spacewar().fireRange, commons.common().disabledPattern);
		viewGrid = new ThreePhaseButton(33 + 72, 24 + 35 * 3 + 30, commons.spacewar().grid, commons.common().disabledPattern);
		viewDamage.selected = true;
		
		viewCommands.add(viewCommand);
		viewCommands.add(viewDamage);
		viewCommands.add(viewRange);
		viewCommands.add(viewGrid);
		
		zoom = new TwoPhaseButton(3, 24, commons.spacewar().zoom, commons.common().disabledPattern);
		zoom.visible = true;
		pause = new TwoPhaseButton(4, 19 + 170, commons.spacewar().pause, commons.common().disabledPattern);
		pause.visible = true;
		retreat = new TwoPhaseButton(33, 19 + 170, commons.spacewar().retreat, commons.common().disabledPattern);
		retreat.visible = true;
		confirmRetreat = new TwoPhaseButton(33, 19 + 170, commons.spacewar().sure, commons.common().disabledPattern);
		stopRetreat = new TwoPhaseButton(33, 19 + 170, commons.spacewar().stopTall, commons.common().disabledPattern);
		
		
		leftShipStatus = createButton(commons.spacewar().ships, true, PanelMode.SHIP_STATUS);
		rightShipStatus = createButton(commons.spacewar().ships, false, PanelMode.SHIP_STATUS);
		
		leftStatistics = createButton(commons.spacewar().statistics, true, PanelMode.STATISTICS);
		rightStatistics = createButton(commons.spacewar().statistics, false, PanelMode.STATISTICS);
		
		leftShipInformation = createButton(commons.spacewar().shipInfo, true, PanelMode.SHIP_INFORMATION);
		rightShipInformation = createButton(commons.spacewar().shipInfo, false, PanelMode.SHIP_INFORMATION);
		
		leftCommunicator = createButton(commons.spacewar().computers, true, PanelMode.COMMUNICATOR);
		rightCommunicator = createButton(commons.spacewar().computers, false, PanelMode.COMMUNICATOR);
		
		leftMovie = createButton(commons.spacewar().movies, true, PanelMode.MOVIE);
		rightMovie = createButton(commons.spacewar().movies, false, PanelMode.MOVIE);
		
		leftStatusPanel = new ShipStatusPanel();
		leftStatusPanel.visible(false);
		rightStatusPanel = new ShipStatusPanel();
		rightStatusPanel.visible(false);
		
		leftStatisticsPanel = new StatisticsPanel();
		leftStatisticsPanel.visible(false);
		rightStatisticsPanel = new StatisticsPanel();
		rightStatisticsPanel.visible(false);
		
		leftShipInfoPanel = new ShipInformationPanel();
		leftShipInfoPanel.visible(false);
		rightShipInfoPanel = new ShipInformationPanel();
		rightShipInfoPanel.visible(false);
		
		layoutPanel = new LayoutPanel();
		layoutPanel.visible(false);
		
		selectionPanel = new SelectionPanel();
		
		leftChatPanel = new ChatPanel();
		leftChatPanel.visible(false);
		rightChatPanel = new ChatPanel();
		rightChatPanel.visible(false);
		
		addThis();
	}
	/**
	 * Remove units from group.
	 * @param i the group index
	 */
	void removeGroup(int i) {
		Iterator<Map.Entry<SpacewarStructure, Integer>> it = groups.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<SpacewarStructure, Integer> e = it.next();
			if (e.getValue() == i) {
				it.remove();
			}
		}
	}
	/**
	 * Creates an animation button with the panel mode settings.
	 * @param phases the animation phases
	 * @param left put it onto the left side?
	 * @param mode the panel mode
	 * @return the button
	 */
	AnimatedRadioButton createButton(BufferedImage[] phases, final boolean left, final PanelMode mode) {
		final AnimatedRadioButton btn = new AnimatedRadioButton(phases);
		btn.action = new Action0() {
			@Override
			public void invoke() {
				displayPanel(mode, left);
				selectButton(btn, left);
			}
		};
		if (left) {
			animatedButtonsLeft.add(btn);
		} else {
			animatedButtonsRight.add(btn);
		}
		return btn;
	}
	/**
	 * Select the specified radio button.
	 * @param btn the button to select
	 * @param left on the left side?
	 */
	void selectButton(AnimatedRadioButton btn, boolean left) {
		for (AnimatedRadioButton b : (left ? animatedButtonsLeft : animatedButtonsRight)) {
			b.selected = b == btn;
		}
		askRepaint();
	}
	/**
	 * Display the specified information panel on the given side.
	 * @param mode the panel mode
	 * @param left on the left side?
	 */
	void displayPanel(PanelMode mode, boolean left) {
		for (Field f : getClass().getDeclaredFields()) {
			Show a = f.getAnnotation(Show.class);
			if (a != null) {
				try {
					if (a.left() == left) {
						UIComponent.class.cast(f.get(this)).visible(a.mode() == mode);
					}
				} catch (IllegalAccessException ex) {
					Exceptions.add(ex);
				}
			}
		}
		if (mode == PanelMode.COMMUNICATOR) {
			if (left && rightChatPanel.visible()) {
				rightChatPanel.visible(false);
				rightShipInfoPanel.visible(true);
				selectButton(rightShipInformation, false);
			} else
			if (!left && leftChatPanel.visible()) {
				leftChatPanel.visible(false);
				leftShipInfoPanel.visible(true);
				selectButton(leftShipInformation, true);
			}
		}
	}
	/**
	 * Animate selected buttons.
	 */
	void doButtonAnimations() {
		for (AnimatedRadioButton arb : animatedButtonsLeft) {
			if (arb.selected) {
				arb.animationIndex = (arb.animationIndex + 1) % (arb.phases.length - 1);
			}
		}
		for (AnimatedRadioButton arb : animatedButtonsRight) {
			if (arb.selected) {
				arb.animationIndex = (arb.animationIndex + 1) % (arb.phases.length - 1);
			}
		}
		askRepaint();
	}

	@Override
	public boolean mouse(UIMouse e) {
		boolean needRepaint = false;
		switch (e.type) {
		case DOUBLE_CLICK:
			if (mainmap.contains(e.x, e.y)) {
				if (e.has(Modifier.SHIFT)) {
					selectionMode = SelectionBoxMode.ADD;
				} else
				if (e.has(Modifier.CTRL)) {
					selectionMode = SelectionBoxMode.SUBTRACT;
				} else {
					selectionMode = SelectionBoxMode.NEW;
				}
				needRepaint = doSelectType(e.x, e.y, e.z > 2);
			}
			break;
		case DOWN:
			if (e.x < commons.spacewar().commands.getWidth() 
					&& e.y < commons.spacewar().commands.getHeight() + 20 + commons.spacewar().frameTopLeft.getHeight()) {
				for (ThreePhaseButton btn : mainCommands) {
					if (btn.test(e.x, e.y)) {
						btn.selected = true;
						btn.pressed = true;
						needRepaint = true;
						for (ThreePhaseButton btn2 : mainCommands) {
							if (btn != btn2) {
								btn2.pressed = false;
								btn2.selected = false;
							}
						}
						btn.invoke();
						break;
					}
				}
				for (ThreePhaseButton btn : viewCommands) {
					if (btn.test(e.x, e.y)) {
						btn.selected = !btn.selected;
						btn.pressed = true;
						needRepaint = true;
						btn.invoke();
						break;
					}
				}
				if (zoom.test(e.x, e.y)) {
					if (e.has(Button.LEFT)) {
						zoom.pressed = true;
						needRepaint = true;
						doZoomIn(mainmap.x + mainmap.width / 2, mainmap.y + mainmap.height / 2);
					} else
					if (e.has(Button.RIGHT)) {
						zoom.pressed = true;
						needRepaint = true;
						doZoomOut(mainmap.x + mainmap.width / 2, mainmap.y + mainmap.height / 2);
					}
				}
				if (pause.test(e.x, e.y)) {
					if (!pause.pressed) {
						pause.pressed = true;
						needRepaint = true;
						doPause();
					} else {
						unpause = true;
					}
				}
				if (retreat.test(e.x, e.y)) {
					retreat.pressed = true;
					needRepaint = true;
				}
				if (confirmRetreat.test(e.x, e.y)) {
					confirmRetreat.pressed = true;
					needRepaint = true;
				}
				if (stopRetreat.test(e.x, e.y)) {
					stopRetreat.pressed = true;
					needRepaint = true;
				}
			}
			if (e.has(Button.LEFT) && mainmap.contains(e.x, e.y)) {
				if (moveButton.selected) {
					Point2D.Double p = mouseToSpace(e.x, e.y);
					doMoveSelectedShips(p.x, p.y);
					moveButton.selected = false;
				} else
				if (rocketButton.selected) {
					Point2D.Double p = mouseToSpace(e.x, e.y);
					SpacewarStructure s = enemyAt(p.x, p.y);
					if (s != null) {
						doAttackWithRockets(s);
					} else {
						selectButton(stopButton);
						mouse(e);
					}
				} else
				if (attackButton.selected) {
					Point2D.Double p = mouseToSpace(e.x, e.y);
					SpacewarStructure s = enemyAt(p.x, p.y);
					if (s != null) {
						doAttackWithShips(s);
						attackButton.selected = false;
					}
				} else {
					selectionBox = true;
					selectionStart = new Point(e.x, e.y);
					selectionEnd = selectionStart;
					if (e.has(Modifier.SHIFT)) {
						selectionMode = SelectionBoxMode.ADD;
					} else
					if (e.has(Modifier.CTRL)) {
						selectionMode = SelectionBoxMode.SUBTRACT;
					} else {
						selectionMode = SelectionBoxMode.NEW;
					}
				}
			}
			if (e.has(Button.RIGHT) && mainmap.contains(e.x, e.y)) {
				if (config.classicControls) {
					Point2D.Double p = mouseToSpace(e.x, e.y);
					SpacewarStructure s = enemyAt(p.x, p.y);
					if (s == null) {
						doMoveSelectedShips(p.x, p.y);
						moveButton.selected = false;
					} else {
						if (rocketButton.selected) {
							doAttackWithRockets(s);
						} else {
							doAttackWithShips(s);
							attackButton.selected = false;
						}
					}
					
				} else {
					if (e.has(Modifier.SHIFT)) {
						Point2D.Double p = mouseToSpace(e.x, e.y);
						doMoveSelectedShips(p.x, p.y);
					} else
					if (e.has(Modifier.CTRL)) {
						Point2D.Double p = mouseToSpace(e.x, e.y);
						SpacewarStructure s = enemyAt(p.x, p.y);
						if (s != null) {
							doAttackWithShips(s);
						}
					} else {
						lastX = e.x;
						lastY = e.y;
						panning = true;
					}
				}
			}
			if (e.has(Button.MIDDLE) && mainmap.contains(e.x, e.y)) {
				if (config.classicControls == e.has(Modifier.CTRL)) { 
					zoomToFit();
					needRepaint = true;
				} else {
					lastX = e.x;
					lastY = e.y;
					panning = true;
				}
			}
			break;
		case DRAG:
			if (panning) {
				pan(lastX - e.x, lastY - e.y);
				lastX = e.x;
				lastY = e.y;
				needRepaint = true;
			}
			if (selectionBox) {
				selectionEnd = new Point(e.x, e.y);
				needRepaint = true;
			}
			break;
		case LEAVE:
			panning = false;
			if (selectionBox) {
				doSelectStructures();
				selectionBox = false;
				needRepaint = true;
			}
			break;
		case WHEEL:
			if (selectionPanel.within(e)) {
				needRepaint = selectionPanel.mouse(e);
			} else
			if (e.has(Modifier.CTRL)) {
				if (e.z < 0) {
					doZoomIn(e.x, e.y);
				} else {
					doZoomOut(e.x, e.y);
				}
				needRepaint = true;
			} else
			if (e.has(Modifier.SHIFT)) {
				if (e.z < 0) {
					pan(-30, 0);
				} else {
					pan(30, 0);
				}
				needRepaint = true;
			} else {
				if (e.z < 0) {
					pan(0, -30);
				} else {
					pan(0, 30);
				}
				needRepaint = true;
			}
			break;
		case UP:
			if (e.has(Button.RIGHT)) {
				panning = false;
			}
			if (e.has(Button.LEFT) && selectionBox) {
				doSelectStructures();
				selectionBox = false;
			}
			for (ThreePhaseButton btn : mainCommands) {
				if (btn.pressed) {
					btn.pressed = false;
					needRepaint = true;
					break;
				}
			}
			for (ThreePhaseButton btn : viewCommands) {
				if (btn.pressed) {
					btn.pressed = false;
					needRepaint = true;
					break;
				}
			}
			if (zoom.pressed) {
				zoom.pressed = false;
				needRepaint = true;
			}
			if (pause.test(e.x, e.y)) {
				if (unpause) {
					unpause = false;
					pause.pressed = false;
					needRepaint = true;
					doUnpause();
				}
			}
			if (retreat.pressed) {
				retreat.pressed = false;
				if (retreat.test(e.x, e.y)) {
					doRetreat();
				}
				needRepaint = true;
			}
			if (confirmRetreat.pressed) {
				confirmRetreat.pressed = false;
				if (confirmRetreat.test(e.x, e.y)) {
					if (e.has(Button.LEFT)) {
						doConfirmRetreat();
					} else
					if (e.has(Button.RIGHT)) {
						doUnconfirmRetreat();
					}
				}
				needRepaint = true;
			}
			if (stopRetreat.pressed) {
				stopRetreat.pressed = false;
				if (stopRetreat.test(e.x, e.y)) {
					doStopRetreat();
				}
				needRepaint = true;
			}
			break;
		default:
		}
		if (!needRepaint) {
			needRepaint = super.mouse(e);
		}
		return needRepaint;
	}
	/**
	 * Locate the enemy at the given coordinates.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return an enemy structure or null if none
	 */
	SpacewarStructure enemyAt(double x, double y) {
		for (SpacewarStructure s : structures) {
			if (!canControl(s) && s.contains(x, y)) {
				return s;
			}
		}
		return null;
	}
	/**
	 * @return Returns a list of the currently selected structures.
	 */
	List<SpacewarStructure> getSelection() {
		List<SpacewarStructure> result = new ArrayList<>();
		for (SpacewarStructure s : structures) {
			if (s.selected) {
				result.add(s);
			}
		}
		return result;
	}
	/**
	 * Select the structures which intersect with the current selection box.
	 */
	void doSelectStructures() {
		List<SpacewarStructure> candidates = new ArrayList<>();
		List<SpacewarStructure> currentSelection = getSelection();
		boolean own = false;
		if (selectionMode == SelectionBoxMode.ADD) {
			candidates.addAll(currentSelection);
		}
		if (selectionMode != SelectionBoxMode.NEW) {
			for (SpacewarStructure s : currentSelection) {
				own |= canControl(s);
			}
		}
		int sx0 = Math.min(selectionStart.x, selectionEnd.x);
		int sy0 = Math.min(selectionStart.y, selectionEnd.y);
		int sx1 = Math.max(selectionStart.x, selectionEnd.x);
		int sy1 = Math.max(selectionStart.y, selectionEnd.y);
		
		Point2D.Double p0 = mouseToSpace(sx0, sy0);
		Point2D.Double p1 = mouseToSpace(sx1, sy1);
		
		own = testStructure(structures, candidates, own, p0, p1);
		
		if (selectionMode == SelectionBoxMode.SUBTRACT) {
			currentSelection.removeAll(candidates);
			candidates = currentSelection;
		}
		
		if (own) {
			for (SpacewarStructure s : candidates) {
				s.selected = canControl(s) && s.kamikaze == 0;
			}
		} else {
			for (SpacewarStructure s : candidates) {
				s.selected = s.kamikaze == 0;
			}
		}
		enableSelectedFleetControls();
		displaySelectedShipInfo();
	}
	/** Display information about the selected ship. */
	void displaySelectedShipInfo() {
		List<SpacewarStructure> currentSelection = getSelection();
		if (currentSelection.size() == 1) {
			SpacewarStructure sws = currentSelection.get(0);
			leftStatusPanel.update(sws);
			rightStatusPanel.update(sws);
			leftShipInfoPanel.item = sws;
			rightShipInfoPanel.item = sws;
		} else {
			leftStatusPanel.update(null);
			rightStatusPanel.update(null);
			leftShipInfoPanel.item = null;
			rightShipInfoPanel.item = null;
			
			if (currentSelection.size() > 1) {
				leftStatusPanel.displayMany();
				rightStatusPanel.displayMany();
				
				leftShipInfoPanel.isMany = true;
				rightShipInfoPanel.isMany = true;
			} else {
				leftStatusPanel.displayNone();
				rightStatusPanel.displayNone();
				
				leftShipInfoPanel.isMany = false;
				rightShipInfoPanel.isMany = false;
			}
		}
	}
	/**
	 * Test the structures in source.
	 * @param source the source structures
	 * @param candidates the candidate for selection
	 * @param own was own items?
	 * @param p0 the top-left point
	 * @param p1 the bottom-right point
	 * @return was own items?
	 */
	boolean testStructure(Iterable<? extends SpacewarStructure> source,
			List<SpacewarStructure> candidates, boolean own,
			Point2D.Double p0, Point2D.Double p1) {
		for (SpacewarStructure s : source) {
			s.selected = false;
			if (s.intersects(p0.x, p0.y, p1.x - p0.x, p1.y - p0.y)) {
				own |= canControl(s);
				candidates.add(s);
			}
		}
		return own;
	}
	/** Zoom in/out to fit the available main map space. */
	void zoomToFit() {
		double xscale = mainmap.width * 1.0 / space.width;
		double yscale = mainmap.height * 1.0 / space.height;
		double s = Math.min(xscale, yscale);
		scale = Math.min(MAX_SCALE, (int)(s * 20) / 20.0);
		pan(0, 0);
	}

	@Override
	public void onEnter(Screens mode) {
		buttonTimer = commons.register(100, new Action0() {
			@Override
			public void invoke() {
				animationTimer++;
				doButtonAnimations();
			}
		});
		chatTimer = commons.register(50, new Action0() {
			@Override
			public void invoke() {
				doChatStep();
			}
		});
		selectButton(leftShipStatus, true);
		selectButton(rightShipStatus, false);
		selectionBox = false;
		retreat.visible = true;
		confirmRetreat.visible = false;
		stopRetreat.visible = false;
		layoutPanel.okHover = false;
		layoutPanel.okDown = false;
		displaySelectedShipInfo();
	}

	@Override
	public void onLeave() {
		close0(buttonTimer);
		buttonTimer = null;
		
		close0(chatTimer);
		chatTimer = null;
		
		node = null;
		chat = null;
		
		// cleanup
		
		battle = null;
		structures.clear();
		projectiles.clear();
		explosions.clear();
		scrambled.clear();
		soundsToPlay.clear();
		
		leftStatusPanel.clear();
		rightStatusPanel.clear();
		
		leftShipInfoPanel.clear();
		rightShipInfoPanel.clear();
		layoutPanel.selected = null;
		
		selectionPanel.clear();
		groups.clear();
		
		infoImages.clear();
		
		allPlayerSet.clear();
		
		rocketParent.clear();
	}
	@Override
	public void onResize() {
		minimap.setBounds(62, 168 + 20, 110, 73);
		mainmap.setBounds(175, 23, getInnerWidth() - 3 - commons.spacewar().commands.getWidth(),
				getInnerHeight() - 38 - 3 - commons.spacewar().panelStatLeft.getHeight());
		leftPanel.setBounds(32, getInnerHeight() - 18 - 3 - 195, 286, 195);
		rightPanel.setBounds(getInnerWidth() - 33 - 286, getInnerHeight() - 18 - 3 - 195, 286, 195);
		

		leftShipStatus.location(leftPanel.x - 28, leftPanel.y + 1);
		leftStatistics.location(leftPanel.x - 28, leftPanel.y + 41);
		leftShipInformation.location(leftPanel.x - 28, leftPanel.y + 81);
		leftCommunicator.location(leftPanel.x - 28, leftPanel.y + 121);
		leftMovie.location(leftPanel.x - 28, leftPanel.y + 161);

		rightShipStatus.location(rightPanel.x + rightPanel.width + 5, rightPanel.y + 1);
		rightStatistics.location(rightPanel.x + rightPanel.width + 5, rightPanel.y + 41);
		rightShipInformation.location(rightPanel.x + rightPanel.width + 5, rightPanel.y + 81);
		rightCommunicator.location(rightPanel.x + rightPanel.width + 5, rightPanel.y + 121);
		rightMovie.location(rightPanel.x + rightPanel.width + 5, rightPanel.y + 161);

		selectionPanel.location(leftPanel.x + leftPanel.width + 3, leftPanel.y);
		selectionPanel.size(Math.max(0, rightPanel.x - 2 - selectionPanel.x), leftPanel.height);
		
		for (Field f : getClass().getDeclaredFields()) {
			Show a = f.getAnnotation(Show.class);
			if (a != null) {
				if (a.left()) {
					try {
						UIComponent.class.cast(f.get(this)).location(leftPanel.x, leftPanel.y);
					} catch (IllegalAccessException ex) {
						Exceptions.add(ex);
					}
				} else {
					try {
						UIComponent.class.cast(f.get(this)).location(rightPanel.x, rightPanel.y);
					} catch (IllegalAccessException ex) {
						Exceptions.add(ex);
					}
				}
			}
		}
		
		pan(0, 0);
	}

	@Override
	public void draw(Graphics2D g2) {
		onResize();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getInnerWidth(), getInnerHeight());
		g2.drawImage(commons.spacewar().frameTopLeft, 0, 20, null);
		
		g2.drawImage(commons.spacewar().frameTopRight, getInnerWidth() - commons.spacewar().frameTopRight.getWidth(), 20, null);

		g2.drawImage(commons.spacewar().commands, 0, 20 + commons.spacewar().frameTopLeft.getHeight(), null);
		g2.drawImage(commons.spacewar().frameRight, getInnerWidth() - commons.spacewar().frameRight.getWidth(), 20 + commons.spacewar().frameTopRight.getHeight(), null);
		
		g2.drawImage(commons.spacewar().panelStatLeft, 0, getInnerHeight() - commons.spacewar().panelStatLeft.getHeight() - 18, null);
		
		g2.drawImage(commons.spacewar().panelStatRight, getInnerWidth() - commons.spacewar().panelStatRight.getWidth(), getInnerHeight() - commons.spacewar().panelStatRight.getHeight() - 18, null);

		Paint p = g2.getPaint();

		TexturePaint tp = new TexturePaint(commons.spacewar().frameTopFill, new Rectangle(commons.spacewar().frameTopLeft.getWidth(), 20, 1, commons.spacewar().frameTopFill.getHeight()));
		g2.setPaint(tp);
		g2.fillRect(commons.spacewar().frameTopLeft.getWidth(), 20, getInnerWidth() - commons.spacewar().frameTopLeft.getWidth() - commons.spacewar().frameTopRight.getWidth(), commons.spacewar().frameTopFill.getHeight());
		
		tp = new TexturePaint(commons.spacewar().panelStatFill, new Rectangle(commons.spacewar().panelStatLeft.getWidth(), getInnerHeight() - commons.spacewar().panelStatLeft.getHeight() - 18, 1, commons.spacewar().panelStatFill.getHeight()));
		g2.setPaint(tp);
		g2.fillRect(commons.spacewar().panelStatLeft.getWidth(), getInnerHeight() - commons.spacewar().panelStatLeft.getHeight() - 18, getInnerWidth() - commons.spacewar().frameTopRight.getWidth() - commons.spacewar().frameTopLeft.getWidth(), commons.spacewar().panelStatFill.getHeight());
		
		tp = new TexturePaint(commons.spacewar().frameRightFill, new Rectangle(getInnerWidth() - commons.spacewar().frameRight.getWidth(), 20 + commons.spacewar().frameTopRight.getHeight() + commons.spacewar().frameRight.getHeight(), commons.spacewar().frameRightFill.getWidth(), commons.spacewar().frameRightFill.getHeight()));
		g2.setPaint(tp);
		g2.fillRect(getInnerWidth() - commons.spacewar().frameRight.getWidth(), 20 + commons.spacewar().frameTopRight.getHeight() + commons.spacewar().frameRight.getHeight(), commons.spacewar().frameRightFill.getWidth(), getInnerHeight() - 38 - commons.spacewar().frameTopRight.getHeight() - commons.spacewar().frameRight.getHeight() - commons.spacewar().panelStatRight.getHeight());
		
		tp = new TexturePaint(commons.spacewar().frameLeftFill, new Rectangle(0, 20 + commons.spacewar().frameTopLeft.getHeight() + commons.spacewar().commands.getHeight(), commons.spacewar().frameLeftFill.getWidth(), commons.spacewar().frameLeftFill.getHeight()));
		g2.setPaint(tp);
		g2.fillRect(0, 20 + commons.spacewar().frameTopLeft.getHeight() + commons.spacewar().commands.getHeight(), commons.spacewar().frameLeftFill.getWidth(), 
				getInnerHeight() - 36 - commons.spacewar().frameTopLeft.getHeight() - commons.spacewar().commands.getHeight() - commons.spacewar().panelStatLeft.getHeight());
		
		g2.setPaint(p);
		
		for (ThreePhaseButton btn : mainCommands) {
			btn.paintTo(g2);
		}

		for (ThreePhaseButton btn : viewCommands) {
			btn.paintTo(g2);
		}
		zoom.paintTo(g2);
		pause.pressed = commons.simulation.paused();
		pause.paintTo(g2);
		retreat.paintTo(g2);
		confirmRetreat.paintTo(g2);
		stopRetreat.paintTo(g2);

		g2.setColor(Color.BLACK);
		g2.fill(minimap);
		
		g2.fill(mainmap);
		
		g2.drawImage(commons.spacewar().panelIg, leftPanel.x, leftPanel.y, null);
		g2.drawImage(commons.spacewar().panelIg, rightPanel.x, rightPanel.y, null);
		
		drawBattle(g2);
		
		// finish layout selection
		if (layoutSelectionMode && !commons.simulation.paused()) {
			setLayoutSelectionMode(false);
			displayPanel(PanelMode.COMMUNICATOR, false);
			enableFleetControls(true);
			retreat.enabled = battle.allowRetreat;
		}
		
		super.draw(g2);
	}
	/** 
	 * Zoom in.
	 * @param x the mouse position to keep steady
	 * @param y the mouse position to keep steady 
	 */
	void doZoomIn(int x, int y) {
		Point2D.Double p0 = mouseToSpace(x, y);
		double newScale = ((int)(scale * 20) + 1) / 20d;
		scale = Math.min(newScale, MAX_SCALE);
		Point2D.Double p1 = mouseToSpace(x, y);
		pan((int)(p0.x - p1.x), (int)(p0.y - p1.y));
	}
	/** 
	 * Zoom out.
	 * @param x the mouse position to keep steady
	 * @param y the mouse position to keep steady 
	 */
	void doZoomOut(int x, int y) {
		Point2D.Double p0 = mouseToSpace(x, y);
		double newScale = ((int)(scale * 20) - 1) / 20d;
		scale = Math.max(newScale, 0.45);
		Point2D.Double p1 = mouseToSpace(x, y);
		pan((int)(p0.x - p1.x), (int)(p0.y - p1.y));
	}
	/**
	 * Convert the mouse coordinate to space coordinates.
	 * @param x the current mouse X on the screen
	 * @param y the current mouse Y on the screen
	 * @return the space coordinates
	 */
	Point2D.Double mouseToSpace(int x, int y) {
		int ox = -offsetX;
		int oy = -offsetY;
		if (space.width * scale < mainmap.width) {
			ox = (int)((mainmap.width - space.width * scale) / 2);
		}
		if (space.height * scale < mainmap.height) {
			oy = (int)((mainmap.height - space.height * scale) / 2);
		}
		
		double x0 = (x - mainmap.x - ox) / scale;
		double y0 = (y - mainmap.y - oy) / scale;
		return new Point2D.Double(x0, y0);
	}
	/** Pause. */
	void doPause() {
		commons.simulation.pause();
	}
	/** Unpause. */
	void doUnpause() {
		commons.simulation.resume();
	}
	/** Retreat mode. */
	void doRetreat() {
		retreat.visible = false;
		confirmRetreat.visible = true;
	}
	/** Confirm retreat. */
	void doConfirmRetreat() {
		confirmRetreat.visible = false;
		stopRetreat.visible = true;
		enableSelectedFleetControls();
		
		for (SpacewarStructure s : structures) {
			if (canControl(s) && s.kamikaze == 0) {
				flee(s);
			}
		}
	}
	/**
	 * Check if all ships of the player has left the screen?
	 * @param owner the owner 
	 * @return check if all ships left the screen? 
	 */
	boolean playerRetreatedBeyondScreen(Player owner) {
		for (SpacewarStructure s : structures) {
			if (s.owner == owner) {
				if (s.intersects(0, 0, space.width, space.height)) {
					return false;
				}
			}
		}
		return true;
	}
	/** Unconfirm retreat. */
	void doUnconfirmRetreat() {
		confirmRetreat.visible = false;
		retreat.visible = true;
	}
	/** Stop retreating. */
	void doStopRetreat() {
		stopRetreat.visible = false;
		retreat.visible = true;
		enableSelectedFleetControls();
		// remove structures who got beyond the screen
		Iterator<SpacewarStructure> it = structures.iterator();
		while (it.hasNext()) {
			SpacewarStructure s = it.next();
			if (canControl(s) && !s.intersects(0, 0, space.width, space.height)) {
				it.remove();
			}
		}
	}
	@Override
	public Screens screen() {
		return Screens.SPACEWAR;
	}
	@Override
	public void onEndGame() {
		
	}
	
	@Override
	public void onFinish() {
		
	}
	/**
	 * Initiate a battle with the given settings.
	 * @param battle the battle information
	 */
	public void initiateBattle(BattleInfo battle) {
		structures.clear();
		
		projectiles.clear();
		explosions.clear();
		scrambled.clear();
		
		this.battle = battle;
		battle.findHelpers();
		
		maxRightPlacement = space.width;

		Planet nearbyPlanet = battle.getPlanet();
		Fleet nearbyFleet = battle.getFleet();

		battle.attackerGroundUnits = groundUnitCount(battle.attacker);
		if (battle.targetFleet != null) {
			battle.defenderGroundUnits = groundUnitCount(battle.targetFleet);
		} else
		if (battle.helperFleet != null) {
			battle.defenderGroundUnits = groundUnitCount(battle.helperFleet);
		}
		
		if (nearbyPlanet != null) {
			planetVisible = true;
			
			boolean alien = nearbyPlanet.owner != player();
			
			// place planetary defenses
			double shieldValue = placeShields(nearbyPlanet, alien);
			placeProjectors(nearbyPlanet, alien, shieldValue);

			int planetWidth = nearbyPlanet.type.spacewar.getWidth();
//			int planetHeight = 506;
//			int defenseWidth = Math.max(maxWidth(shields()), maxWidth(projectors()));
			centerStructures(space.width - planetWidth / 2, 
					U.concat(shields(), projectors()));
			maxRightPlacement -= 3 * planetWidth / 2;
			
			// place and align stations
			placeStations(nearbyPlanet, alien);
			int stationWidth = maxWidth(stations());
			centerStructures(maxRightPlacement - stationWidth / 4, stations());
			maxRightPlacement -= 3 * stationWidth / 2;
			
			
			// add fighters of the planet
			List<SpacewarStructure> shipWall = new ArrayList<>();
			createStructures(inventoryWithParent(nearbyPlanet), 
					EnumSet.of(ResearchSubCategory.SPACESHIPS_FIGHTERS), shipWall);
			
			for (SpacewarStructure s : shipWall) {
				s.angle = Math.PI;
			}
			if (!shipWall.isEmpty()) {
				int maxw = createSingleRowBatchWall(maxRightPlacement, true, shipWall, structures);
				maxRightPlacement -= 3 * maxw / 2;
			}
		} else {
			planetVisible = false;
		}
		
		if (nearbyPlanet != null && nearbyPlanet.owner == battle.attacker.owner) {
			// place the attacker on the right side (planet side)
			
			attackerOnRight = true;
			
			placeFleet(maxRightPlacement, true, inventoryWithParent(battle.attacker));
			// place the defender on the left side
			
			if (nearbyFleet != null) {
				placeFleet(0, false, inventoryWithParent(nearbyFleet));
			}
		} else {
			// place attacker on the left side
			placeFleet(0, false, inventoryWithParent(battle.attacker));
			
			attackerOnRight = false;
			
			// place the defender on the planet side (right side)
			if (nearbyFleet != null) {
				placeFleet(maxRightPlacement, true, inventoryWithParent(nearbyFleet));
			}
		}
		
		zoomToFit();
		commons.playBattleMusic();

		setSpacewarTimeControls();

		findAllPlayers();
		
		for (Player p : allPlayerSet) {
			p.ai.spaceBattleInit(this);
		}
		world().scripting.onSpacewarStart(this);

		// fix unit placements
		if (battle.showLanding) {
			for (SpacewarStructure s : structures) {
				if (s.type == StructureType.SHIP || s.type == StructureType.STATION) {
					if (Math.abs(s.angle) > 0.95 * Math.PI && Math.abs(s.angle) < 1.05 * Math.PI) {
						s.x -= commons.spacewar().landingZone[0].getWidth();
					}
				}
			}
		}

		
		leftChatPanel.clear();
		rightChatPanel.clear();
		
		if (battle.chat != null) {
			chat = world().chats.get(battle.chat);
		} else {
			chat = null;
		}
		
		displayPanel(PanelMode.SHIP_STATUS, true);
		if (battle.attacker.owner == player() 
				&& (nearbyPlanet == null 
				|| nearbyPlanet.owner != player())) {
			displayPanel(PanelMode.LAYOUT, false);
			setLayoutSelectionMode(true);
			enableFleetControls(false);
		} else {
			displayPanel(PanelMode.COMMUNICATOR, false);
			setLayoutSelectionMode(false);
			commons.simulation.resume();
			enableFleetControls(true);
		}
		retreat.enabled = false;
		
		if (chat != null) {
			node = chat.getStart();
			
			if (node.enemy) {
				leftChatPanel.addLine(TextRenderer.YELLOW, get(node.message));
				rightChatPanel.addLine(TextRenderer.YELLOW, get(node.message));
			} else {
				leftChatPanel.options.add(node);
				rightChatPanel.options.add(node);
			}
		}
		
		// update statistics
		battle.incrementSpaceBattles();
	}
	/**
	 * Create a map from the given inventory parent.
	 * @param inv the inventory parent
	 * @return the inventory item with their parent
	 */
	Map<InventoryItem, HasInventory> inventoryWithParent(HasInventory inv) {
		Map<InventoryItem, HasInventory> result = new LinkedHashMap<>();
		for (InventoryItem ii : inv.inventory().iterable()) {
			result.put(ii, inv);
		}
		return result;
	}
	/**
	 * Find all players.
	 */
	void findAllPlayers() {
		allPlayerSet.clear();
		for (SpacewarStructure s : structures) {
			allPlayerSet.add(s.owner);
		}
	}
	/**
	 * Returns the non-human player of the current battle.
	 * @return the player
	 */
	Player nonPlayer() {
		if (battle.attacker.owner != player()) {
			return battle.attacker.owner;
		} else
		if (battle.targetFleet != null && battle.targetFleet.owner != player()) {
			return battle.targetFleet.owner;
		} else
		if (battle.targetPlanet != null && battle.targetPlanet.owner != player()) {
			return battle.targetPlanet.owner;
		}
		return null;
	}
	/** @return a list of shield structures. */
	List<SpacewarStructure> shields() {
		List<SpacewarStructure> result = new ArrayList<>();
		for (SpacewarStructure s : structures) {
			if (s.type == StructureType.SHIELD) {
				result.add(s);
			}
		}
		return result;
	}
	/** @return a list of projector structures. */
	List<SpacewarStructure> projectors() {
		List<SpacewarStructure> result = new ArrayList<>();
		for (SpacewarStructure s : structures) {
			if (s.type == StructureType.PROJECTOR) {
				result.add(s);
			}
		}
		return result;
	}
	/** @return a list of ship structures. */
	List<SpacewarStructure> ships() {
		List<SpacewarStructure> result = new ArrayList<>();
		for (SpacewarStructure s : structures) {
			if (s.type == StructureType.SHIP) {
				result.add(s);
			}
		}
		return result;
	}
	/** @return a list of station structures. */
	List<SpacewarStructure> stations() {
		List<SpacewarStructure> result = new ArrayList<>();
		for (SpacewarStructure s : structures) {
			if (s.type == StructureType.STATION) {
				result.add(s);
			}
		}
		return result;
	}
	/**
	 * Place a fleet onto the map starting from the {@code x} position and {@code angle}.
	 * @param x the starting position
	 * @param left expand to the left?
	 * @param inventory the sequence of inventory items
	 */
	void placeFleet(int x, boolean left, Map<InventoryItem, HasInventory> inventory) {
		List<SpacewarStructure> largeShipWall = new ArrayList<>();
		// place attacker on the planet side (right side)
		createStructures(inventory, 
				EnumSet.of(ResearchSubCategory.SPACESHIPS_BATTLESHIPS, ResearchSubCategory.SPACESHIPS_CRUISERS), largeShipWall);
		
		if (!largeShipWall.isEmpty()) {
			int maxw = createMultiRowWall(x, left, largeShipWall, structures);
			x = left ? (x - maxw) : (x + maxw);
		}
		
		List<SpacewarStructure> smallShipWall = new ArrayList<>();
		List<SpacewarStructure> smallShipWallOut = new ArrayList<>();
		
		createStructures(inventory, 
				EnumSet.of(ResearchSubCategory.SPACESHIPS_FIGHTERS), smallShipWall);
		if (!smallShipWall.isEmpty()) {
			createSingleRowBatchWall(x, left, smallShipWall, smallShipWallOut);
			structures.addAll(smallShipWallOut);
		}

		orientStructures(left ? Math.PI : 0, largeShipWall);
		orientStructures(left ? Math.PI : 0, smallShipWallOut);

	}
	
	/**
	 * Place the stations from the planet inventory.
	 * @param nearbyPlanet the nearby planet
	 * @param alien true if allied with the non-player
	 */
	void placeStations(Planet nearbyPlanet, boolean alien) {
		for (InventoryItem ii : nearbyPlanet.inventory.iterable()) {
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS 
					&& ii.owner == nearbyPlanet.owner) {
				
				BattleSpaceEntity bse = world().battle.spaceEntities.get(ii.type.id);
				
				SpacewarStructure st = new SpacewarStructure(ii.type);
				st.type = StructureType.STATION;
				st.item = ii;
				st.angle = Math.PI;
				st.owner = nearbyPlanet.owner;
				st.destruction = bse.destruction;
				st.angles = new BufferedImage[] { alien ? bse.alternative[0] : bse.normal[0] };
				st.infoImageName = bse.infoImageName;
				st.shield = ii.shield;
				st.shieldMax = Math.max(0, ii.shieldMax());
				
				st.hpMax = world().getHitpoints(ii.type, ii.owner);
				st.hp = ii.hp;

				st.value = ii.type.productionCost;
				st.planet = nearbyPlanet;
				
				st.ecmLevel = setWeaponPorts(ii, st.ports);
				
				st.efficiencies = bse.efficiencies;
				
				structures.add(st);
			}
		}

	}
	/**
	 * Add shields to the planet surface.
	 * @param nearbyPlanet the planet nearby
	 * @param alien true if allied with the non-player
	 * @return the shield value
	 */
	double placeShields(Planet nearbyPlanet, boolean alien) {
		double shieldValue = 0;
		
		// add shields
		for (Building b : nearbyPlanet.surface.buildings.iterable()) {
			double power = Math.abs(b.assignedEnergy * 1.0 / b.getEnergy());
			if (power >= 0.5 && b.type.kind.equals("Shield")) {
				
				double eff = power;
				
				BattleGroundShield bge = world().battle.groundShields.get(b.type.id);
				
				SpacewarStructure st = new SpacewarStructure(b.type);
				st.owner = nearbyPlanet.owner;
				st.type = StructureType.SHIELD;
				st.angles = new BufferedImage[] { alien ? bge.alternative : bge.normal };
				st.infoImageName = bge.infoImageName;
				st.hpMax = world().getHitpoints(b.type, nearbyPlanet.owner, true);
				st.hp = (1d * b.hitpoints * st.hpMax / b.type.hitpoints);
				st.value = b.type.cost;
				st.destruction = bge.destruction;
				st.building = b;
				st.planet = nearbyPlanet;

				shieldValue += eff * bge.shields;

				structures.add(st);
			}
		}
		for (SpacewarStructure sws : shields()) {
			sws.shield = (sws.hp * shieldValue / 100);
			sws.shieldMax = (sws.hpMax * shieldValue / 100);
		}
		return shieldValue;
	}
	/**
	 * Place projectors on the planet surface.
	 * @param nearbyPlanet the nearby planet
	 * @param alien true if allied with the non-player
	 * @param shieldValue the shield percentage
	 */
	void placeProjectors(Planet nearbyPlanet, boolean alien, double shieldValue) {
		for (Building b : nearbyPlanet.surface.buildings.iterable()) {
			double power = Math.abs(b.assignedEnergy * 1.0 / b.getEnergy());
			if (power >= 0.5 && b.type.kind.equals("Gun")) {
				
				BattleGroundProjector bge = world().battle.groundProjectors.get(b.type.id);

				SpacewarStructure st = new SpacewarStructure(b.type);
				st.owner = nearbyPlanet.owner;
				st.type = StructureType.PROJECTOR;
				st.angles = alien ? bge.alternative : bge.normal;
				st.angle = Math.PI;
				st.infoImageName = bge.infoImageName;
				st.hpMax = world().getHitpoints(b.type, nearbyPlanet.owner, true);
				
				st.value = b.type.cost;
				st.hp = (1d * b.hitpoints * st.hpMax / b.type.hitpoints);
				st.destruction = bge.destruction;
				st.building = b;
				st.planet = nearbyPlanet;
				
				st.shield = (st.hp * shieldValue / 100);
				st.shieldMax = (st.hpMax * shieldValue / 100);
				
				st.rotationTime = bge.rotationTime;

				BattleProjectile pr = world().battle.projectiles.get(bge.projectile);
				
				SpacewarWeaponPort wp = new SpacewarWeaponPort(null);
				wp.projectile = pr.copy();
				wp.projectile.baseDamage = bge.damage(st.owner);
				
				st.efficiencies = bge.efficiencies;
				
				st.ports.add(wp);
				
				structures.add(st);
			}
		}
	}
	/**
	 * Create a single row of batched items.
	 * @param x the starting position 
	 * @param left expand to the left?
	 * @param items the list of items to distribute
	 * @param out the output for the new distributions
	 * @return the width of the wall
	 */
	int createSingleRowBatchWall(int x, boolean left,
			Collection<SpacewarStructure> items, 
			Collection<SpacewarStructure> out) {
		int maxWidth = 0;
		int maxHeight = 0;
		// determine number of slots
		for (SpacewarStructure e : items) {
			maxWidth = Math.max(maxWidth, e.get().getWidth());
			maxHeight += e.get().getWidth();
		}
		
		LinkedList<SpacewarStructure> ships = new LinkedList<>(items);
		LinkedList<SpacewarStructure> group = new LinkedList<>();

		while (!ships.isEmpty()) {
			SpacewarStructure sws = ships.removeFirst();
			if (sws.count > 1) {
				if (maxHeight + sws.get().getHeight() <= space.height) {
					int sum = sws.count;
					SpacewarStructure sws2 = sws.copy();
					sws.count = sum / 2;
					sws2.count = sum - sum / 2;
					ships.addLast(sws);
					ships.addLast(sws2);
					evenCounts(ships, sws.item);
					maxHeight += sws.get().getHeight();
				} else {
					group.add(sws);
					break;
				}
			} else {
				group.add(sws);
			}
		}
		group.addAll(ships);
		
		centerStructures(left ? x - maxWidth / 2 : x + maxWidth / 2, group);
		
		out.addAll(group);
		
		return maxWidth;
	}
	/**
	 * Even out the counts of the ships based on the inventory item.
	 * @param ships the ship collection
	 * @param item the reference item
	 */
	void evenCounts(Collection<SpacewarStructure> ships, InventoryItem item) {
		int count = 0;
		int sum = 0;
		for (SpacewarStructure sws : ships) {
			if (sws.item == item) {
				count++;
				sum += sws.count;
			}
		}
		double n = 1.0 * sum / count;
		int i = 1;
		int alloc = 0;
		SpacewarStructure last = null;
		for (SpacewarStructure sws : ships) {
			if (sws.item == item) {
				double m = n * i;
				sws.count = (int)(m - alloc);
				alloc += sws.count;
				i++;
				last = sws;
			}
		}
		if (alloc < sum && last != null) {
			last.count += sum - alloc;
		}
	}
	/**
	 * Create multiple rows of ships based on how many fit vertically.
	 * @param x the center position of the first row
	 * @param left expand the columns to the left?
	 * @param ships the list of ships to lay out
	 * @param out where to place the ships
	 * @return the total width of the row
	 */
	int createMultiRowWall(int x, boolean left,
			Collection<? extends SpacewarStructure> ships, 
			Collection<? super SpacewarStructure> out) {
		
		List<List<SpacewarStructure>> rows = new ArrayList<>();
		int rowIndex = -1;
		int y = 0;
		List<SpacewarStructure> currentRow = null;
		
		// put ships into rows
		for (SpacewarStructure sws : ships) {
			if (y + sws.get().getHeight() >= space.height || rowIndex < 0) {
				rowIndex++;
				currentRow = new ArrayList<>();
				rows.add(currentRow);
				y = 0;
			}
			currentRow.add(sws);
			y += sws.get().getHeight();
		}

		int maxWidth = 0;
		// align all rows center
		for (List<SpacewarStructure> row : rows) {
			int w = maxWidth(row);
			centerStructures(left ? x - w / 2 : x + w / 2, row);
			x = left ? (x - w) : (x + w);
			maxWidth = maxWidth + w;
		}
		
		out.addAll(ships);
		
		return maxWidth;
	}
	@Override
	public void addStructures(HasInventory inventory,
			EnumSet<ResearchSubCategory> categories) {
		createStructures(inventoryWithParent(inventory), categories, structures);
	}
	/**
	 * Create the spacewar ships from the given inventory list and category filters.
	 * @param inventory the inventory provider
	 * @param categories the categories to use
	 * @param ships the output of ships
	 */
	void createStructures(Map<InventoryItem, HasInventory> inventory,
			EnumSet<ResearchSubCategory> categories,
			Collection<? super SpacewarStructure> ships) {
		for (Map.Entry<InventoryItem, HasInventory> e : inventory.entrySet()) {
			InventoryItem ii = e.getKey();
			HasInventory parent = e.getValue();
			// Fix for zero inventory entries
			if (ii.count <= 0) {
				continue;
			}
			if (categories.contains(ii.type.category)) {
				BattleSpaceEntity bse = world().battle.spaceEntities.get(ii.type.id);
				if (bse == null) {
					Exceptions.add(new AssertionError("Missing space entity: " + ii.type.id));
					continue;
				}
				
				SpacewarStructure st = new SpacewarStructure(ii.type);

				if (parent instanceof Planet) {
					st.planet = (Planet)parent;
				} else {
					st.fleet = (Fleet)parent;
				}
				st.type = StructureType.SHIP;
				st.item = ii;
				st.owner = ii.owner;
				st.destruction = bse.destruction;
				st.angles = ii.owner != player() ? bse.alternative : bse.normal;
				st.infoImageName = bse.infoImageName;
				st.shield = ii.shield;
				st.shieldMax = Math.max(0, ii.shieldMax());
				st.hp = ii.hp;
				st.hpMax = world().getHitpoints(ii.type, ii.owner);
				st.value = totalValue(ii);
				st.count = ii.count;
				st.rotationTime = bse.rotationTime;
				st.movementSpeed = bse.movementSpeed;
				
				st.ecmLevel = setWeaponPorts(ii, st.ports);
				st.computeMinimumRange();
				
				st.efficiencies = bse.efficiencies;
				
				ships.add(st);
			}
		}
		
	}
	/**
	 * Calculates the total value of the inventory intem based on the base production cost.
	 * @param ii the inventory item
	 * @return the value
	 */
	int totalValue(InventoryItem ii) {
		int result = ii.type.productionCost;
		for (InventorySlot is : ii.slots.values()) {
			if (is.type != null) {
				result += is.type.productionCost * is.count;
			}
		}
		return result;
	}
	/**
	 * Set the weapon ports based on the configuration of the inventory item,
	 * considering its slot and fixed-slot equipment.
	 * @param ii the inventory item object
	 * @param ports the output for weapon ports
	 * @return the ecm level
	 */
	int setWeaponPorts(InventoryItem ii, Collection<? super SpacewarWeaponPort> ports) {
		int ecmLevel = 0;
		// add weapons
		for (InventorySlot is : ii.slots.values()) {
			if (is.type != null 
					&& (is.type.category == ResearchSubCategory.WEAPONS_CANNONS
					|| is.type.category == ResearchSubCategory.WEAPONS_LASERS
					|| is.type.category == ResearchSubCategory.WEAPONS_PROJECTILES)) {
				SpacewarWeaponPort wp = new SpacewarWeaponPort(is);
				wp.count = is.count;
				BattleProjectile bp = world().battle.projectiles.get(is.type.get("projectile"));
				if (bp == null) {
					System.err.println("Missing projectile: " + is.type.id);
				}
				wp.projectile = bp;
				ports.add(wp);
			}
			if (is.type != null && is.type.has(ResearchType.PARAMETER_ECM)) {
				ecmLevel = Math.max(ecmLevel, is.type.getInt(ResearchType.PARAMETER_ECM));
			}
		}
		return ecmLevel;
	}
	/**
	 * Pan the view by the given amount.
	 * @param dx the delta X
	 * @param dy the delta Y
	 */
	void pan(int dx, int dy) {
		offsetX += dx;
		offsetY += dy;
		
		offsetX = (int)Math.max(0, Math.min(offsetX, space.width * scale - mainmap.width));
		offsetY = (int)Math.max(0, Math.min(offsetY, space.height * scale - mainmap.height));
	}
	/**
	 * Render the battle.
	 * @param g2 the graphics context.
	 */
	void drawBattle(Graphics2D g2) {
		Shape save0 = g2.getClip();
		AffineTransform af = g2.getTransform();
		
		g2.clipRect(mainmap.x, mainmap.y, mainmap.width, mainmap.height);
		
		int ox = -offsetX;
		int oy = -offsetY;
		if (space.width * scale < mainmap.width) {
			ox = (int)((mainmap.width - space.width * scale) / 2);
		}
		if (space.height * scale < mainmap.height) {
			oy = (int)((mainmap.height - space.height * scale) / 2);
		}
		g2.translate(mainmap.x + ox, mainmap.y + oy);
		g2.scale(scale, scale);
		
		g2.clip(space);

		g2.drawImage(commons.spacewar().background, 0, 0, space.width, space.height, null);
		
		if (planetVisible) {
			Planet nearbyPlanet = battle.getPlanet();

			BufferedImage pimg = nearbyPlanet.type.spacewar;
			int pw = pimg.getWidth();
			int ph = pimg.getHeight();
			g2.drawImage(pimg, space.width - pw, -ph / 3, pw, 3 * ph / 2, null);
			
			if (battle.showLanding) {
				int idx = (int)((animationTimer / 2) % commons.spacewar().landingZone.length);
				BufferedImage limg = commons.spacewar().landingZone[idx];
				int lx = space.width - pw - limg.getWidth();
				int ly = (space.height - limg.getHeight()) / 2;
				
				g2.drawImage(limg, lx, ly, null);
			}
		}

		if (viewGrid.selected) {
			g2.setColor(new Color(0, 128, 0, 128));
			int bw = commons.spacewar().background.getWidth() * 2;
			int bh = commons.spacewar().background.getHeight() * 2;
			
			int step = 20;
			for (int i = 1; i < bw / step; i++) {
				g2.drawLine(i * step, 0, i * step, bh);
			}
			for (int i = 1; i < bh / step; i++) {
				g2.drawLine(0, i * step, bw, i * step);
			}
		}

		drawRanges(g2, structures);
		drawCommands(g2, structures);
		
		for (SpacewarProjectile e : projectiles) {
			drawCenter(e.get(), e.x, e.y, g2);
		}
		drawSpacewarStructures(structures, g2);
		for (SpacewarExplosion e : explosions) {
			drawCenter(e.get(), e.x, e.y, g2);
		}
		

		g2.setTransform(af);

		if (selectionBox) {
			int sx0 = Math.min(selectionStart.x, selectionEnd.x);
			int sy0 = Math.min(selectionStart.y, selectionEnd.y);
			int sx1 = Math.max(selectionStart.x, selectionEnd.x);
			int sy1 = Math.max(selectionStart.y, selectionEnd.y);
			g2.setColor(new Color(255, 255, 255, 128));
			g2.fillRect(sx0, sy0, sx1 - sx0 + 1, sy1 - sy0 + 1);
		}

		
		g2.setClip(save0);
		
		// draw minimap
		g2.setColor(Color.GRAY);
		for (SpacewarProjectile e : projectiles) {
			int x = minimap.x + (int)(e.x * minimap.width / space.width);
			int y = minimap.y + (int)(e.y * minimap.height / space.height);
			g2.drawLine(x, y, x, y);
		}
		g2.setColor(Color.WHITE);
		
		save0 = g2.getClip();
		g2.clipRect(minimap.x, minimap.y, minimap.width, minimap.height);
		
		drawSpacewarStructuresMinimap(structures, g2);
		
		g2.setColor(Color.WHITE);
		Rectangle rect = computeMinimapViewport();
		g2.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
		g2.setClip(save0);
	}
	@Override
	public Point landingPlace() {
		if (planetVisible && battle.showLanding) {
			int pw = battle.getPlanet().type.spacewar.getWidth();
			int idx = (int)((animationTimer / 2) % commons.spacewar().landingZone.length);
			BufferedImage limg = commons.spacewar().landingZone[idx];
			int lx = space.width - pw - limg.getWidth() / 2;
			int ly = space.height / 2;
			return new Point(lx, ly);
		}
		return null;
	}
	/** @return calculates the minimap viewport rectangle coordinates. */
	Rectangle computeMinimapViewport() {
		int vx = 0;
		int vy = 0;
		int vx2 = minimap.width - 1;
		int vy2 = minimap.height - 1;
		if (space.width * scale >= mainmap.width) {
			vx = (int)(1d * offsetX * minimap.width / space.width / scale + 0.5);
			vx2 = (int)(1d * (offsetX + mainmap.width - 1) * minimap.width / space.width / scale + 0.5);
		}
		if (space.height * scale >= mainmap.height) {
			vy = (int)(1d * offsetY * minimap.height / space.height / scale + 0.5);
			vy2 = (int)(1d * (offsetY + mainmap.height - 1) * minimap.height / space.height / scale + 0.5);
		}
		return new Rectangle(minimap.x + vx, minimap.y + vy, vx2 - vx + 1, vy2 - vy + 1);
	}
	/**
	 * Draw command indicator lines.
	 * @param g2 the graphics context
	 * @param structures the structures to consider
	 */
	void drawCommands(Graphics2D g2, Iterable<? extends SpacewarStructure> structures) {
		if (viewCommand.selected) {
			for (SpacewarStructure e : structures) {
				if (e.attack != null) {
					g2.setColor(Color.RED);
					g2.drawLine((int)e.x, (int)e.y, (int)e.attack.x, (int)e.attack.y);
				} else
				if (e.moveTo != null) {
					g2.setColor(Color.WHITE);
					g2.drawLine((int)e.x, (int)e.y, (int)e.moveTo.x, (int)e.moveTo.y);
				}
			}
		}		
	}
	/**
	 * Draw weapon port ranges.
	 * @param g2 the graphics context
	 * @param structures the structures to consider
	 */
	void drawRanges(Graphics2D g2, Iterable<? extends SpacewarStructure> structures) {
		if (viewRange.selected) {
			final Color[] colors = new Color[] { Color.RED, Color.ORANGE, Color.GREEN };
			for (SpacewarStructure e : structures) {
				if (e.selected) {
					int i = 0;
					for (SpacewarWeaponPort p : e.ports) {
						if (p.projectile.mode == Mode.BEAM) {
							g2.setColor(colors[(i++) % colors.length]);
							g2.drawOval((int)(e.x - p.projectile.range), (int)(e.y - p.projectile.range), 2 * p.projectile.range, 2 * p.projectile.range);
						}
					}
				}
			}		
		}
	}
	/**
	 * Draw the spacewar structures to the main screen.
	 * @param structures the sequence of structures
	 * @param g2 the graphics context
	 */
	void drawSpacewarStructures(Iterable<? extends SpacewarStructure> structures, Graphics2D g2) {
		for (SpacewarStructure e : structures) {
			BufferedImage img = e.get();
			drawCenter(img, e.x, e.y, g2);
			int w = img.getWidth();
			int w2 = w / 2;
			int h = img.getHeight();
			int h2 = h / 2;
			if (e.selected) {
				g2.setColor(Color.GREEN);
				drawRectCorners(g2, (int)e.x, (int)e.y, w, h, 8);
//				drawRectCorners(g2, (int)e.x, (int)e.y, w + 6, h + 6, 8);
			}
			if (viewDamage.selected && e.kamikaze == 0) {
				int y = (int)e.y - h2 + 2;
				int dw = w - 6;
				g2.setColor(Color.BLACK);
				g2.fillRect((int)e.x - w2 + 3, y, dw, 4);
				g2.setColor(Color.GREEN);
				g2.fillRect((int)e.x - w2 + 3, y, (int)(e.hp * dw / e.hpMax), 4);
				g2.setColor(Color.RED);
				g2.drawRect((int)e.x - w2 + 3, y, dw, 4);
				if (e.shieldMax > 0) {
					g2.setColor(new Color(0xFFFFCC00));
					g2.fillRect((int)e.x - w2 + 3, y, (int)(e.shield * dw / e.shieldMax), 4);
				}
			}
			if (e.type == StructureType.SHIP && e.count > 1) {
				commons.text().paintTo(g2, (int)(e.x - w2), (int)(e.y + h / 2d - 8), 7, 0xFFFFFFFF, Integer.toString(e.count));
			}
		}
	}
	/**
	 * Draw corners of the specified rectangle with a given length.
	 * @param g2 the graphics context
	 * @param cx the center
	 * @param cy the center
	 * @param w the width
	 * @param h the height
	 * @param len the corner length
	 */
	void drawRectCorners(Graphics2D g2, int cx, int cy, int w, int h, int len) {
		int x0 = cx - w / 2;
		int x1 = x0 + w - 1;
		int y0 = cy - h / 2;
		int y1 = y0 + h - 1;
		
		g2.drawLine(x0, y0, x0 + len, y0); // top-left horizontal
		g2.drawLine(x0, y0, x0, y0 + len); // top-left vertical
		g2.drawLine(x1 - len, y0, x1, y0); // top-right horizontal
		g2.drawLine(x1, y0, x1, y0 + len); // top-right vertical
		
		g2.drawLine(x0, y1, x0 + len, y1); // bottom-left horizontal
		g2.drawLine(x0, y1, x0, y1 - len); // bottom-left vertical
		g2.drawLine(x1 - len, y1, x1, y1); // bottom-right horizontal
		g2.drawLine(x1, y1 - len, x1, y1); // bottom-right vertical
		
	}
	/**
	 * Draw the spacewar structures symbolically onto the minimap.
	 * @param structures the sequence of structures
	 * @param g2 the graphics context
	 */
	void drawSpacewarStructuresMinimap(Iterable<? extends SpacewarStructure> structures, Graphics2D g2) {
		for (SpacewarStructure e : structures) {
			if (canControl(e)) {
				if (e.selected) {
					g2.setColor(Color.GREEN);
				} else {
					g2.setColor(new Color(0x786cc2));
				}
			} else {
				g2.setColor(Color.ORANGE);
			}
			int x = minimap.x + (int)(e.x * minimap.width / space.width);
			int y = minimap.y + (int)(e.y * minimap.height / space.height);
			int w = 2;
			if (e.item != null) {
				if (e.item.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
					w = 2;
				} else
				if (e.item.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
					w = 3;
				} else
				if (e.item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
					w = 1;
				} else
				if (e.item.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					w = 3;
				}
			}
			g2.fillRect(x - w / 2, y - w / 2, w, w);
		}
	}
	/**
	 * Draw the image centered to the given coordinates.
	 * @param img the image to draw
	 * @param x the center X coordinate
	 * @param y the center Y coordinate
	 * @param g2 the graphics context
	 */
	void drawCenter(BufferedImage img, double x, double y, Graphics2D g2) {
		g2.drawImage(img, (int)(x - img.getWidth() / 2d), (int)(y - img.getHeight() / 2d), null);
	}
	/**
	 * Computes the maximum width of the structures.
	 * @param structures the sequence of structures
	 * @return the maximum width or 0
	 */
	int maxWidth(Iterable<? extends SpacewarStructure> structures) {
		int w = 0;
		for (SpacewarStructure s : structures) {
			w = Math.max(s.get().getWidth(), w);
		}
		return w;
	}
	/**
	 * Center structures vertically.
	 * @param x the center line
	 * @param structures the collection of structures
	 */
	void centerStructures(int x, Iterable<? extends SpacewarStructure> structures) {
		if (structures.iterator().hasNext()) {
			int sumHeight = 0;
			int count = 0;
			for (SpacewarStructure s : structures) {
				sumHeight += s.get().getHeight();
				count++;
			}
			double dy = (space.height - sumHeight) * 1.0 / count;
			double y = dy / 2;
			for (SpacewarStructure s : structures) {
				s.x = x;
				s.y = y + s.get().getHeight() / 2d;
				
				y += s.get().getHeight() + dy;
			}
		}
	}
	/**
	 * Orient structures into the given angle.
	 * @param angle the target angle in radians
	 * @param structures the sequence of structures
	 */
	void orientStructures(double angle, Iterable<? extends SpacewarStructure> structures) {
		for (SpacewarStructure s : structures) {
			s.angle = angle;
		}
	}
	/**
	 * The ship status panel.
	 * @author akarnokd, 2011.08.30.
	 *
	 */
	class ShipStatusPanel extends UIContainer {
		/** Label. */
		UILabel title;
		/** Label. */
		UILabel owner;
		/** Label. */
		UILabel unitType;
		/** Label. */
		UILabel unitName;
		/** Label. */
		UILabel type;
		/** Label. */
		UILabel count;
		/** Label. */
		UILabel damage;
		/** The associated inventory item. */
		InventoryItem item;
		/** The selected inventory slot. */
		InventorySlot selectedSlot;
		/** The image to display. */
		BufferedImage image;
		/** Constructor with layout. */
		public ShipStatusPanel() {
			width = 286;
			height = 195;
			
			title = new UILabel(get("spacewar.ship_status"), 10, commons.text());
			title.horizontally(HorizontalAlignment.CENTER);
			title.width = width;
			title.location(0, 7);
			title.color(TextRenderer.YELLOW);
			
			owner = new UILabel("", 7, commons.text());
			owner.color(TextRenderer.YELLOW);
			unitType = new UILabel("", 7, commons.text());
			unitType.color(TextRenderer.YELLOW);
			unitName = new UILabel("", 7, commons.text());
			unitName.color(TextRenderer.YELLOW);

			type = new UILabel("", 7, commons.text());
			count = new UILabel("", 7, commons.text());
			damage = new UILabel("", 7, commons.text());

			addThis();
		}
		/** Calculate locations. */
		void setLocations() {
			type.color(TextRenderer.WHITE);
			count.color(TextRenderer.WHITE);
			damage.color(TextRenderer.WHITE);
			owner.location(15, 165);
			unitType.location(15, 175);
			unitName.location(15, 185);
			type.location(15, 135);
			count.location(15, 145);
			damage.location(15, 155);
		}
		@Override
		public void draw(Graphics2D g2) {
			setLocations();
			if (image != null) {
				g2.drawImage(image, 0, 0, null);
				if (item != null) {
					g2.translate(-6, 15);
					if (selectedSlot != null) {
						updateSlot(selectedSlot);
					}
					EquipmentConfigure.drawSlots(g2, item, selectedSlot, world());
					g2.translate(6, -15);
				}
			} else {
				g2.drawImage(commons.spacewar().panelStar, 0, 0, null);
			}
			super.draw(g2);
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOWN) && e.has(Button.LEFT) && item != null) {
				int dx = -6;
				int dy = 15;
				for (InventorySlot es : item.slots.values()) {
					if (!es.slot.fixed && e.within(es.slot.x + dx, es.slot.y + dy, es.slot.width, es.slot.height)) {
						updateSlot(es);
						return true;
					}
				}
			}
			return super.mouse(e);
		}
		/**
		 * Update the display values.
		 * @param item the inventory item
		 */
		public void update(SpacewarStructure item) {

			unitType.text("", true);
			unitName.text("", true);
			type.text("", true);
			damage.text("", true);
			count.text("", true);
			owner.text("", true);
			
			if (item != null) {
				owner.text(format("spacewar.ship_owner", item.owner.name), true);
				image = null;
	
				InventoryItem lastItem = this.item;
				
				if (item.type == StructureType.SHIP) {
	
					unitType.text(format("spacewar.ship_type", item.item.type.longName), true);
					unitName.text(format("spacewar.ship_name", "-"), true);
					
					BattleSpaceEntity bse = world().battle.spaceEntities.get(item.item.type.id);
					image = getInfoImage(bse.infoImageName);
					
					this.item = item.item;
					if (lastItem != item.item) {
						selectFirstSlot();
					}
					updateSlot(selectedSlot);
				} else
				if (item.type == StructureType.STATION) {
					unitType.text(format("spacewar.ship_type", item.item.type.longName), true);
					unitName.text(format("spacewar.ship_name", "-"), true);
					BattleSpaceEntity bse = world().battle.spaceEntities.get(item.item.type.id);
					image = getInfoImage(bse.infoImageName);
					this.item = item.item;
					if (lastItem != item.item) {
						selectFirstSlot();
					}
					updateSlot(selectedSlot);
				} else
				if (item.type == StructureType.PROJECTOR) {
					unitType.text(format("spacewar.ship_type", item.building.type.name), true);
					BattleGroundProjector bgp = world().battle.groundProjectors.get(item.building.type.id);
					image = getInfoImage(bgp.infoImageName);
					this.item = null;
					this.selectedSlot = null;
				} else
				if (item.type == StructureType.SHIELD) {
					unitType.text(format("spacewar.ship_type", item.building.type.name), true);
					BattleGroundShield bgp = world().battle.groundShields.get(item.building.type.id);
					image = getInfoImage(bgp.infoImageName);
					this.item = null;
					this.selectedSlot = null;
				}
			} else {
				this.item = null;
				selectedSlot = null;
				image = null;
			}
		}
		/** Display label for no ship selected. */
		public void displayNone() {
			owner.text(get("spacewar.ship_status_none"), true);
		}
		/** Display label for too many ships selected. */
		public void displayMany() {
			owner.text(get("spacewar.ship_status_many"), true);
		}
		/** Select the first slot. */
		public void selectFirstSlot() {
			updateSlot(null);
			if (item != null) {
				for (InventorySlot is : item.slots.values()) {
					if (!is.slot.fixed) {
						updateSlot(is);
						return;
					}
				}
			}
		}
		/**
		 * Set the current inventory slot.
		 * @param is the new inventory slot
		 */
		public void updateSlot(InventorySlot is) {
			this.selectedSlot = is;
			if (is != null) {
				if (is.type != null) {
					int ihp = is.hpMax(item.owner);
					damage.text(format("spacewar.ship_weapon_damage", 
							(int)(100 * (ihp - is.hp) 
							/ ihp)), true);
					count.text(format("spacewar.ship_weapon_count", is.count), true);
					type.text(format("spacewar.ship_weapon_type", is.type.name), true);
				} else {
					count.text(format("spacewar.ship_weapon_count", 0), true);
					type.text(format("spacewar.ship_weapon_type", get("inventoryslot." + is.slot.id)), true);
					damage.text("", true);
				}
			} else {
				damage.text("", true);
				count.text("", true);
				type.text("", true);
			}
		}
		/** Clear the display. */
		@Override
		public void clear() {
			item = null;
			selectedSlot = null;
			image = null;
			update(null);
			displayNone();
		}
	}
	/** 
	 * Toggle layout selection mode.
	 * @param enabled enable? 
	 */
	void setLayoutSelectionMode(boolean enabled) {
		layoutSelectionMode = enabled;
		if (enabled) {
			leftCommunicator.visible(false);
			leftMovie.visible(false);
			for (UIComponent c : animatedButtonsRight) {
				c.visible(false);
			}
		} else {
			for (UIComponent c : animatedButtonsLeft) {
				c.visible(true);
			}
			for (UIComponent c : animatedButtonsRight) {
				c.visible(true);
			}
		}
	}
	/** The battle statistics record. */
	static class SpacebattleStatistics {
		/** The unit count. */
		public int units;
		/** The losses. */
		public int losses;
		/** The firepower. */
		public double firepower;
		/** The damage per second. */
		public double dps;
		/** The total hitpoints. */
		public int hp;
		/** The ground units. */
		public int groundUnits;
		/** The stations. */
		public int stations;
		/** The guns. */
		public int guns;
		/** The rocket count. */
		public int rockets;
		/** The bomb count. */
		public int bombs;
	}
	/** The statistics panel. */
	class StatisticsPanel extends UIComponent {
		/** Initialize. */
		public StatisticsPanel() {
			width = 286;
			height = 195;
		}
		@Override
		public void draw(Graphics2D g2) {
			SpacebattleStatistics own = new SpacebattleStatistics();
			SpacebattleStatistics other = new SpacebattleStatistics();
			calculateStatistics(own, other);
			
			String s = get("spacewar.statistics");
			int dx = (width - commons.text().getTextWidth(10, s)) / 2;
			commons.text().paintTo(g2, dx, 6, 10, TextRenderer.YELLOW, s);
			
			int y = 26;
			
			y = drawLine(g2, y, TextRenderer.GREEN, "spacewar.statistics_own_units", own.units, own.hp);
			y = drawLine(g2, y, TextRenderer.GREEN, "spacewar.statistics_losses", own.losses);
			y = drawLine(g2, y, TextRenderer.GREEN, "spacewar.statistics_firepower", (int)own.firepower, own.dps);
			y = drawLine(g2, y, TextRenderer.GREEN, "spacewar.statistics_ground", own.groundUnits);
			y = drawLine(g2, y, TextRenderer.GREEN, "spacewar.statistics_rockets", own.rockets, own.bombs);
			
			if (own.stations > 0 || own.guns > 0) {
				y += 10;
				if (own.stations > 0) {
					y = drawLine(g2, y, TextRenderer.GREEN, "spacewar.statistics_stations", own.stations);
				}
				if (own.guns > 0) {
					y = drawLine(g2, y, TextRenderer.GREEN, "spacewar.statistics_guns", own.guns);
				}
			}
			y += 16;

			y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_enemy_units", other.units, other.hp);
			y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_losses", other.losses);
			y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_firepower", (int)other.firepower, other.dps);
			y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_ground", other.groundUnits);
			y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_rockets", other.rockets, other.bombs);
			
			if (other.stations > 0 || other.guns > 0) {
				y += 10;
				if (other.stations > 0) {
					y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_stations", other.stations);
				}
				if (other.guns > 0) {
					/*y = */drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_guns", other.guns);
				}
			}
			
		}
		/**
		 * Draws a text line with the given format.
		 * @param g2 the graphics context
		 * @param y the top position
		 * @param color the text color
		 * @param labelFormat the format label
		 * @param args the optional arguments
		 * @return the new top position;
		 */
		int drawLine(Graphics2D g2, int y, int color, String labelFormat, Object... args) {
			
			commons.text().paintTo(g2, 8, y, 7, color, format(labelFormat, args));
			
			return y + 10;
		}
	}
	/**
	 * Calculate the battle statistics.
	 * @param own the own statistics
	 * @param other the other statistics
	 */
	void calculateStatistics(SpacebattleStatistics own, SpacebattleStatistics other) {
		int vehicleMaxOwn = 0;
		int vehicleMaxEnemy = 0;
		for (SpacewarStructure e : structures) {
			SpacebattleStatistics stat = (canControl(e)) ? own : other;
			if (e.count > 0) {
				stat.hp += e.hp + (e.count - 1) * e.hpMax + e.shield;
			}
			if (e.type == StructureType.PROJECTOR) {
				stat.guns++;
			} else
			if (e.type == StructureType.STATION) {
				stat.stations++;
			} else
			if (e.type == StructureType.SHIP) {
				stat.units++;
			}
			setPortStatistics(stat, e.ports, e.count, e.owner);
			if (e.item != null) {
				int vm = 0;
				if (e.item.type.has(ResearchType.PARAMETER_VEHICLES)) {
					vm += e.item.type.getInt(ResearchType.PARAMETER_VEHICLES);
					for (InventorySlot is : e.item.slots.values()) {
						if (is.type != null && is.type.has(ResearchType.PARAMETER_VEHICLES)) {
							vm += is.type.getInt(ResearchType.PARAMETER_VEHICLES);
						}
					}
				}
				if (canControl(e)) {
					vehicleMaxOwn += vm;
				} else {
					vehicleMaxEnemy += vm;
				}
			}
		}
		if (battle.attacker.owner == player()) {
			own.losses = battle.attackerLosses;
			other.losses = battle.defenderLosses;

			own.groundUnits = Math.min(battle.attackerGroundUnits, vehicleMaxOwn);
			other.groundUnits = Math.min(battle.defenderGroundUnits, vehicleMaxEnemy);
		} else {
			own.losses = battle.defenderLosses;
			other.losses = battle.attackerLosses;
			
			own.groundUnits = Math.min(battle.defenderGroundUnits, vehicleMaxOwn);
			other.groundUnits = Math.min(battle.attackerGroundUnits, vehicleMaxOwn);
		}
	}
	/**
	 * Set the weapon port statistics.
	 * @param stat the output statistics
	 * @param ports the port sequence
	 * @param count the count of units
	 * @param owner the owner
	 */
	void setPortStatistics(SpacebattleStatistics stat, 
			Iterable<? extends SpacewarWeaponPort> ports, 
			int count,
			Player owner) {
		for (SpacewarWeaponPort p : ports) {
			if (p.projectile.mode == Mode.BEAM) {
				stat.firepower += p.count * p.damage(owner) * count;
				stat.dps += p.count * p.damage(owner) * count * 1000d / p.projectile.delay;
			} else {
				if (p.projectile.mode == Mode.BOMB || p.projectile.mode == Mode.VIRUS) {
					stat.bombs += p.count;
				} else {
					stat.rockets += p.count;
				}
			}
		}
	}
	/**
	 * Count the ground units only.
	 * @param f the fleet
	 * @return the number of ground units
	 */
	int groundUnitCount(Fleet f) {
		int result = 0;
		for (InventoryItem ii : f.inventory.iterable()) {
			if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS
					|| ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
				result += ii.count;
			}
		}
		return result;
	}
	/** The ship information panel. */
	class ShipInformationPanel extends UIComponent {
		/** The selected item. */
		public SpacewarStructure item;
		/** The selected item is null due too many selection. */
		public boolean isMany;
		/** Initialize. */
		public ShipInformationPanel() {
			width = 286;
			height = 195;
		}
		@Override
		public void draw(Graphics2D g2) {
			String s = get("spacewar.ship_information");
			if (item != null) {
				if (item.type == StructureType.STATION) {
					s = get("spacewar.station_information");
				} else
				if (item.type == StructureType.SHIELD) {
					s = get("spacewar.shield_information");
				} else
				if (item.type == StructureType.PROJECTOR) {
					s = get("spacewar.projector_information");
				}
			}
			int dx = (width - commons.text().getTextWidth(10, s)) / 2;
			commons.text().paintTo(g2, dx, 6, 10, TextRenderer.YELLOW, s);
			
			int y = 26;
			
			if (item == null) {
				if (isMany) {
					commons.text().paintTo(g2, 8, y, 7, TextRenderer.GREEN, get("spacewar.ship_status_many"));
				} else {
					commons.text().paintTo(g2, 8, y, 7, TextRenderer.GREEN, get("spacewar.ship_status_none"));
				}
			} else {
				boolean isws = item.type == StructureType.SHIP;
				boolean showFixed = false;
				// draw first column
				int maxLabelWidth = 0;
				
				int c = canControl(item) ? TextRenderer.GREEN : TextRenderer.YELLOW;
				
				Point p = new Point(8, 26);
				
				maxLabelWidth = Math.max(drawLabel(g2, p, c, get("spacewar.ship_information_type")), maxLabelWidth);
				if (isws) {
					maxLabelWidth = Math.max(drawLabel(g2, p, c, get("spacewar.ship_information_name")), maxLabelWidth);
				}
				maxLabelWidth = Math.max(drawLabel(g2, p, c, get("spacewar.ship_information_damage")), maxLabelWidth);
				if (isws) {
					maxLabelWidth = Math.max(drawLabel(g2, p, c, get("spacewar.ship_information_wins")), maxLabelWidth);
					maxLabelWidth = Math.max(drawLabel(g2, p, c, get("spacewar.ship_information_crew")), maxLabelWidth);
				}
				double firepower = item.getFirepower();
				if (firepower >= 0) {
					maxLabelWidth = Math.max(drawLabel(g2, p, c, get("spacewar.ship_information_firepower")), maxLabelWidth);
				}
				p.y += 10;
				if (isws) {
					maxLabelWidth = Math.max(drawLabel(g2, p, c, get("spacewar.ship_information_equipment")), maxLabelWidth);
					
					for (InventorySlot is : item.item.slots.values()) {
						if ((!is.slot.fixed || showFixed) && is.type != null) {
							maxLabelWidth = Math.max(drawLabel(g2, p, c, "- " + is.type.name), maxLabelWidth);
						}
					}
				}
				
				// draw second column
				p.x += maxLabelWidth;
				p.y = 26;
				
				drawLabel(g2, p, c, "  : " + item.getType());
				if (isws) {
					drawLabel(g2, p, c, "  : " + "-"); // name
				}
				drawLabel(g2, p, c, "  : " + item.getDamage() + "%");
				if (isws) {
					drawLabel(g2, p, c, "  : " + item.item.kills); // wins
					drawLabel(g2, p, c, "  : " + "-"); // crew
				}
				if (firepower >= 0) {
					drawLabel(g2, p, c, "  : " + firepower);
				}
				p.y += 20;
				if (isws) {
					for (InventorySlot is : item.item.slots.values()) {
						if ((!is.slot.fixed || showFixed) &&  is.type != null) {
							drawLabel(g2, p, c, "  : " + is.count);
						}
					}
				}
			}
		}
		/**
		 * Draw the given text and return its size.
		 * @param g2 the graphics context
		 * @param p where to put the text, updates its y value once the text is written
		 * @param color the text color
		 * @param text the text
		 * @return dimension
		 */
		int drawLabel(Graphics2D g2, Point p, int color, String text) {
			commons.text().paintTo(g2, p.x, p.y, 7, color, text);
			p.y += 10;
			return commons.text().getTextWidth(7, text);
		}
		/** Clear the contents. */
		public void clear() {
			item = null;
			isMany = false;
		}
	}
	/**
	 * Place the elements of the fleet based on the supplied layout map, where
	 * the map values represent fighters (true) and non-fighters (false).
	 * @param ships the sequence of ships
	 * @param owner the owner filter
	 * @param layout the layout definition
	 */
	void applyLayout(final Iterable<? extends SpacewarStructure> ships, 
			final Player owner, final BattleSpaceLayout layout) {
		LinkedList<SpacewarStructure> fighters = new LinkedList<>();
		LinkedList<SpacewarStructure> nonFighters = new LinkedList<>();
		for (SpacewarStructure sws : ships) {
			if (sws.owner == owner && world().scripting.mayControlFleet(sws.fleet)) { // DO not layout ships from non-controllable fleets of the player
				if (sws.item.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS
						|| sws.item.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS
						) {
					nonFighters.add(sws);
				} else
				if (sws.item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
					fighters.add(sws);
				}
			}
		}
		// fill in the regular places
		List<Map.Entry<Location, Boolean>> olist = layout.order();
		List<SpacewarStructure> placed = new ArrayList<>();
		for (Map.Entry<Location, Boolean> e : olist) {
			Location p = e.getKey();
			if (e.getValue()) {
				if (!fighters.isEmpty()) {
					SpacewarStructure sws = fighters.removeFirst();
					sws.x = (p.x + 0.5) * space.width / layout.getWidth();
					sws.y = (p.y + 0.5) * space.height / layout.getHeight();
					placeNearby(placed, sws);
				}
			} else {
				if (!nonFighters.isEmpty()) {
					SpacewarStructure sws = nonFighters.removeFirst();
					sws.x = (p.x + 0.5) * space.width / layout.getWidth();
					sws.y = (p.y + 0.5) * space.height / layout.getHeight();
					placeNearby(placed, sws);
				}
			}
		}
		// take the remaining ships and place them near the slots

		while (!fighters.isEmpty() || !nonFighters.isEmpty()) {
			for (Map.Entry<Location, Boolean> e : olist) {
				Location p = e.getKey();
				if (e.getValue()) {
					if (!fighters.isEmpty()) {
						SpacewarStructure sws = fighters.removeFirst();
						sws.x = (p.x + 0.5) * space.width / layout.getWidth();
						sws.y = (p.y + 0.5) * space.height / layout.getHeight();
						placeNearby(placed, sws);
					}
				} else {
					if (!nonFighters.isEmpty()) {
						SpacewarStructure sws = nonFighters.removeFirst();
						sws.x = (p.x + 0.5) * space.width / layout.getWidth();
						sws.y = (p.y + 0.5) * space.height / layout.getHeight();
						placeNearby(placed, sws);
					}					
				}
			}
		}
	}
	/**
	 * Check if the {@code s} ship intersects with any other ships.
	 * @param ships the ship sequence
	 * @param s the ship to test
	 * @return true if intersects
	 */
	boolean intersects(Iterable<? extends SpacewarStructure> ships, SpacewarStructure s) {
		for (SpacewarStructure sws : ships) {
			if (sws.intersects(s)) {
				return true;
			}
		}
		return !s.within(0, 0, space.width, space.height);
	}
	/**
	 * Tries to find a place to put the given {@code s} ship where it does not
	 * overlap with the other ships.
	 * @param ships the list of ships
	 * @param s the ship to place
	 */
	void placeNearby(List<SpacewarStructure> ships, SpacewarStructure s) {
		double initialX = s.x; 
		double initialY = s.y;
		if (intersects(ships, s)) {
			for (int r = 10; r < space.width; r += 10) {
				for (double alpha = Math.PI / 2; alpha < 5 * Math.PI / 2; alpha += Math.PI / 36) {
					s.x = initialX + r * Math.cos(alpha);
					s.y = initialY + r * Math.sin(alpha);
					if (!intersects(ships, s)) {
						ships.add(s);
						return;
					}
				}
			}
		}
		// no choice but to overlap
		s.x = initialX;
		s.y = initialY;
		ships.add(s);
	}
	/**
	 * The layout panel.
	 * @author akarnokd, 2011.08.31.
	 */
	class LayoutPanel extends UIComponent {
		/** The selected layout. */
		BattleSpaceLayout selected;
		/** Hovering over the okay button? */
		boolean okHover;
		/** Pressing down over the okay button? */
		boolean okDown;
		/** Initialize. */
		public LayoutPanel() {
			width = 286;
			height = 195;
		}
		@Override
		public void draw(Graphics2D g2) {
			int y = 2;
			int x = 5;
			int i = 0;
			for (BattleSpaceLayout ly : world().battle.layouts) {

				g2.setColor(ly == selected ? Color.RED : Color.GREEN);
				g2.drawRect(x, y, ly.getWidth() + 1, ly.getHeight() + 1);
				g2.setColor(new Color(0, 0, 0, 128));
				g2.fillRect(x + 1, y + 1, ly.getWidth(), ly.getHeight());
				g2.drawImage(ly.image, x + 1, y + 1, null);
				
				i++;
				x += ly.getWidth() + 20;
				if (i % 3 == 0) {
					y += ly.getHeight() + 4;
					x = 5;
				}
			}
			if (okDown) {
				g2.drawImage(commons.spacewar().layoutOkPressed, 5, 175, null);
			} else
			if (okHover) {
				g2.drawImage(commons.spacewar().layoutOkHover, 5, 175, null);
			} else {
				g2.drawImage(commons.spacewar().layoutOk, 5, 175, null);
			}
//			g2.drawImage(commons.spacewar().layoutToggle, 103, 175, null);
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOWN)) {
				int row = (e.y - 2) / 43;
				int col = (e.x - 5) / 98;
				int idx = row * 3 + col;
				if (idx >= 0 && idx < world().battle.layouts.size()) {
					selected = world().battle.layouts.get(idx);
					applyLayout(ships(), player(), selected);
					return true;
				}
				if (withinOk(e)) {
					okDown = true;
					return true;
				}
			} else
			if (e.has(Type.MOVE) || e.has(Type.DRAG)) {
				if (withinOk(e)) {
					if (!okHover) {
						okHover = true;
						return true;
					}
				} else {
					if (okHover) {
						okHover = false;
						return true;
					}
				}
			}
			if (e.has(Type.UP)) {
				if (okDown && withinOk(e)) {
					okDown = false;
					displayPanel(PanelMode.COMMUNICATOR, false);
					setLayoutSelectionMode(false);
					enableFleetControls(true);
					retreat.enabled = battle.allowRetreat;
					return true;
				}
			}
			if (e.has(Type.LEAVE)) {
				okDown = false;
				okHover = false;
				return true;
			}
			return super.mouse(e);
		}
		/**
		 * Test if mouse is within the OK button.
		 * @param e the mouse event
		 * @return true if within
		 */
		boolean withinOk(UIMouse e) {
			return e.within(5, 175, commons.spacewar().layoutOk.getWidth(), commons.spacewar().layoutOk.getHeight());
		}
	}
	/** Set the spacewar time controls. */
	void setSpacewarTimeControls() {
		commons.replaceSimulation(new Action0() {
			@Override
			public void invoke() {
				doSpacewarSimulation();
			}
		},
		new Func1<SimulationSpeed, Integer>() {
			@Override
			public Integer invoke(SimulationSpeed value) {
				switch (value) {
				case NORMAL: return SIMULATION_DELAY;
				case FAST: return SIMULATION_DELAY / 2;
				case ULTRA_FAST: return SIMULATION_DELAY / 4;
				default:
					throw new AssertionError("" + value);
				}
			}
        }
		);
	}
	/**
	 * Enable/disable fleet controls.
	 * @param enabled should be enabled?
	 */
	void enableFleetControls(boolean enabled) {
		if (enabled) {
			enableSelectedFleetControls();
		} else {
			stopButton.enabled = enabled;
			moveButton.enabled = enabled;
			kamikazeButton.enabled = enabled;
			attackButton.enabled = enabled;
			rocketButton.enabled = enabled;
			guardButton.enabled = enabled;
		}
	}
	/** Enable/disable controls based on the capabilities of the current selection. */
	void enableSelectedFleetControls() {
		if (!layoutSelectionMode && !stopRetreat.visible) { 
			List<SpacewarStructure> selection = getSelection();
			if (selection.size() > 0) {
				if (canControl(selection.get(0))) {
					stopButton.enabled = false;
					moveButton.enabled = false;
					attackButton.enabled = false;
					guardButton.enabled = false;
					kamikazeButton.enabled = false;
					rocketButton.enabled = false;
					for (SpacewarStructure sws : selection) {
						if (sws.type == StructureType.SHIP) {
							kamikazeButton.enabled |= (sws.item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) && sws.kamikaze == 0 && sws.attack != null;
							moveButton.enabled = sws.kamikaze == 0;
							stopButton.enabled |= sws.kamikaze == 0;
							attackButton.enabled |= sws.kamikaze == 0 && sws.canDirectFire();
							guardButton.enabled |= sws.kamikaze == 0;
						} else
						if (sws.type == StructureType.STATION 
						|| sws.type == StructureType.PROJECTOR) {
							stopButton.enabled = true;
							attackButton.enabled = true;
						}
						for (SpacewarWeaponPort port : sws.ports) {
							rocketButton.enabled |= port.projectile.mode != Mode.BEAM 
									&& port.count > 0 && port.cooldown == 0; 
						}
						guardButton.selected &= sws.guard; // keep guard only of all of the selection is in guard mode
					}
					return;
				}
			}
		}
		stopButton.enabled = false;
		moveButton.enabled = false;
		kamikazeButton.enabled = false;
		attackButton.enabled = false;
		rocketButton.enabled = false;
		guardButton.enabled = false;
	}
	/**
	 * Move the selected ships to the new coordinates.
	 * @param x the new X
	 * @param y the new Y
	 */
	void doMoveSelectedShips(double x, double y) {
		Point2D.Double p = new Point2D.Double(x, y);
		boolean moved = false;
		for (SpacewarStructure ship : structures) {
			if (ship.type == StructureType.SHIP 
					&& ship.selected && canControl(ship)) {
				ship.moveTo = p;
				ship.attack = null;
				ship.guard = false;
				moved = true;
			}
		}
		if (moved) {
			effectSound(SoundType.ACKNOWLEDGE_2);
			enableSelectedFleetControls();
		}
	}
	/**
	 * Check if the structure can be controlled.
	 * @param s the structure
	 * @return true if can be controlled
	 */
	boolean canControl(SpacewarStructure s) {
		return battle.isAlly(s, player()) && s.kamikaze == 0d;
	}
	/**
	 * Function to locate the most appropriate rocket type.
	 * @author akarnokd, 2012.01.03.
	 */
	class RocketSelected {
		/** The remaining rocket count. */
		int count = 0;
		/** The technology. */
		ResearchType type;
		/** Who will fire the rocket. */
		SpacewarStructure fired;
		/** From which port. */
		SpacewarWeaponPort port;
		/**
		 * Find a rocket for the specified target.
		 * @param target the target
		 * @param targetTyped use rocket for that kind of target?
		 */
		void findRocket(SpacewarStructure target, boolean targetTyped) {
			// try to find rockets for ships or bombs for buildings
			for (SpacewarStructure ship : structures) {
				if ((ship.type == StructureType.SHIP 
						|| ship.type == StructureType.STATION) 
						&& ship.selected 
						&& ship.owner != target.owner) {
					findRocketPort(target, targetTyped, ship);
				}
			}
		}
		/**
		 * Find the apropriate rocket port in the ship.
		 * @param target the tartet
		 * @param targetTyped should return the proper port for the target?
		 * @param ship the ship who will fire
		 */
		public void findRocketPort(SpacewarStructure target, boolean targetTyped,
				SpacewarStructure ship) {
			for (SpacewarWeaponPort p : ship.ports) {
				if (p.count > 0 && p.projectile.mode != Mode.BEAM) {
					if (!targetTyped || (target.building != null && (p.projectile.mode == Mode.BOMB || p.projectile.mode == Mode.VIRUS)
							|| (target.building == null && (p.projectile.mode == Mode.ROCKET || p.projectile.mode == Mode.MULTI_ROCKET)))) {
						if (p.count > count) {
							count = p.count;
							port = p;
							fired = ship;
							type = world().researches.get(p.projectile.id);
						}
					}
				}
			}
		}
	}
	/**
	 * Fire a single rocket from the group.
	 * @param target the target
	 */
	void doAttackWithRockets(SpacewarStructure target) {
		RocketSelected r = new RocketSelected();
		r.findRocket(target, true);
		if (r.fired == null && !config.targetSpecificRockets) {
			r = new RocketSelected();
			r.findRocket(target, false);
		}
		if (r.fired != null) {
			fireRocketAt(target, r);
			enableSelectedFleetControls();
		}
	}
	/**
	 * Fire the selected rocket at the target.
	 * @param target the target
	 * @param r the rocket selected
	 */
	void fireRocketAt(SpacewarStructure target, RocketSelected r) {
		if (r.port.cooldown > 0) {
			return;
		}
		r.port.count--;
		if (r.port.is != null) {
			r.port.is.count--;
		}
		r.port.cooldown = r.port.projectile.delay;

		SpacewarStructure proj = new SpacewarStructure(world().researches.get(r.port.projectile.id));
		proj.owner = r.fired.owner;
		proj.attack = target;
		proj.angles = proj.owner == player() ? r.port.projectile.matrix[0] : r.port.projectile.alternative[0];
		proj.movementSpeed = r.port.projectile.movementSpeed;
		proj.rotationTime = r.port.projectile.rotationTime;
		proj.x = r.fired.x;
		proj.y = r.fired.y;
		proj.angle = r.fired.angle;
		proj.destruction = SoundType.EXPLOSION_MEDIUM;
		proj.ecmLevel = r.type.getInt("anti-ecm", 0);
		proj.kamikaze = r.port.damage(proj.owner);
		proj.hp = world().battle.getIntProperty(proj.techId, proj.owner.id, "hp");
		proj.hpMax = (int)proj.hp;
		
		rocketParent.put(proj, r.fired);
		
		switch (r.port.projectile.mode) {
		case ROCKET:
			proj.type = StructureType.ROCKET;
			break;
		case MULTI_ROCKET:
			proj.type = StructureType.MULTI_ROCKET;
			break;
		case VIRUS:
			proj.type = StructureType.VIRUS_BOMB;
			break;
		default:
			proj.type = StructureType.BOMB;

		}
		
		structures.add(proj);
		
		effectSound(r.port.projectile.sound);
	}
	/**
	 * Set to attack the specified target.
	 * @param target the target structure
	 */
	void doAttackWithShips(SpacewarStructure target) {
		boolean attack = false;
		for (SpacewarStructure ship : structures) {
			if (ship.selected && canControl(ship) && ship.canDirectFire()) {
				ship.moveTo = null;
				ship.attack = target;
				ship.guard = false;
				attack = true;
			}
		}
		if (attack) {
			effectSound(SoundType.ACKNOWLEDGE_1);
			enableSelectedFleetControls();
		}
	}
	/**
	 * Issue a kamikaze order to ship.
	 */
	void doKamikaze() {
		for (SpacewarStructure ship : structures) {
			if (ship.selected && canControl(ship)
					&& ship.attack != null
					&& ship.kamikaze == 0 && ship.type == StructureType.SHIP 
					&& ship.item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
				SpacebattleStatistics sbs = new SpacebattleStatistics();
				setPortStatistics(sbs, ship.ports, ship.count, ship.owner);
				ship.kamikaze = ship.count * kamikazeDamage(ship.techId, ship.owner.id);
				ship.selected = false;
			}
		}
	}
	/**
	 * Returns the kamikaze damage of the given ship. 
	 * @param techId the technology
	 * @param ownerId the owner identifier
	 * @return the damage
	 */
	double kamikazeDamage(String techId, String ownerId) {
		if (world().battle.hasProperty(techId, ownerId, "kamikaze-damage")) {
			return world().battle.getDoubleProperty(techId, ownerId, "kamikaze-damage");
		}
		return 500;
	}
	/**
	 * Stop the activity of the selected structures.
	 */
	void doStopSelectedShips() {
		boolean stop = false;
		for (SpacewarStructure ship : structures) {
			if (ship.selected && canControl(ship) 
					&& ship.kamikaze == 0) {
				ship.moveTo = null;
				ship.attack = null;
				ship.guard = false;
				guardButton.selected = false;
				stop = true;
			}
		}
		if (stop) {
			effectSound(SoundType.NOT_AVAILABLE);
			enableSelectedFleetControls();
		}
	}
	/**
	 * Creates a list of in-range enemy structures.
	 * @param ship the center ship
	 * @return the list of structures
	 */
	@Override
	public List<SpacewarStructure> enemiesInRange(SpacewarStructure ship) {
		List<SpacewarStructure> result = new ArrayList<>();
		for (SpacewarStructure s : structures) {
			if (!areAllies(s.owner, ship.owner) && !s.isDestroyed()) {
				if (ship.inRange(s).size() > 0) {
					result.add(s);
				}
			}
		}
		return result;
	}
	/**
	 * Rotate the structure towards the given target angle by a step.
	 * @param ship the structure
	 * @param x the target point X
	 * @param y the target point Y
	 * @return rotation done?
	 */
	boolean rotateStep(SpacewarStructure ship, double x, double y) {
		double targetAngle = Math.atan2(y - ship.y, x - ship.x);
		double currentAngle = ship.normalizedAngle();

		double diff = targetAngle - currentAngle;
		if (diff < -Math.PI) {
			diff += 2 * Math.PI;
		} else
		if (diff > Math.PI) {
			diff -= 2 * Math.PI; 
		}
		double anglePerStep = 2 * Math.PI * ship.rotationTime / ship.angles.length / SIMULATION_DELAY;
		if (Math.abs(diff) < anglePerStep) {
			ship.angle = targetAngle;
			return true;
		}
		ship.angle += Math.signum(diff) * anglePerStep;
		return false;
	}
	/**
	 * Perform a move step towards the given target point and up to the minimum distance if initially
	 * further away.
	 * @param ship the ship to move
	 * @param x the target point X
	 * @param y the target point Y
	 * @param r the target distance
	 * @return true if target distance reached
	 */
	boolean moveStep(SpacewarStructure ship, double x, double y, double r) {
		// travel until the distance
		double dist = Math.hypot(ship.x - x, ship.y - y);
		if (dist < r) {
			return true;
		}
		double angle = Math.atan2(y - ship.y, x - ship.x);
		double ds = 1.0 * SIMULATION_DELAY / ship.movementSpeed;
		if (dist - r > ds) {
			ship.x += ds * Math.cos(angle);
			ship.y += ds * Math.sin(angle);
		} else {
			ship.x = x - r * Math.cos(angle);
			ship.y = y - r * Math.sin(angle); 
			return true;
		}
		return false;
	}
	
	/**
	 * Move the projectile one animation step further.
	 * @param obj the projectile
	 * @return true if collided with the target
	 */
	boolean moveStep(SpacewarProjectile obj) {
		double ds = SIMULATION_DELAY * 1.0 / obj.movementSpeed;
		double dx = ds * Math.cos(obj.angle);
		double dy = ds * Math.sin(obj.angle);
		obj.phase++;
 		
		
		if (!obj.target.isDestroyed()) {
			double w = obj.target.get().getWidth();
			double h = obj.target.get().getHeight();
			double x0 = obj.target.x - w / 2;
			double x1 = x0 + w;
			double y0 = obj.target.y - h / 2;
			double y1 = y0 + h;
			if (RenderTools.isLineIntersectingRectangle(obj.x, obj.y, obj.x + dx, 
					obj.y + dy, x0, y0, x1, y1)) {
				// FIXME for now
				return true;
				/*
				// walk along the angle up to ds units and see if there is a pixel of the target there?
				int tx0 = (int)(obj.target.x - w / 2);
				int ty0 = (int)(obj.target.y - h / 2);
				int tx1 = (int)(tx0 + w);
				int ty1 = (int)(ty0 + h);
				for (double dds = 0; dds <= ds; dds += 0.5) {
					int px = (int)(obj.x + dds * Math.cos(obj.angle));
					int py = (int)(obj.y + dds * Math.cos(obj.angle));
					if (tx0 <= px && px < tx1 && ty0 <= py && py < ty1) {
						int c = obj.target.get().getRGB(px - tx0, py - ty0);
						if ((c & 0xFF000000) != 0) {
							obj.x = px;
							obj.y = py;
							return true;
						}
					}
				}
				*/
			}
		}
		obj.x += dx;
		obj.y += dy;
		return false;
	}
	/**
	 * Move the structure one animation step further.
	 * @param obj the projectile
	 * @return true if collided with the target
	 */
	boolean moveStep(SpacewarStructure obj) {
		// Fluctuate the movement speed within the 98%-100% range
		double ds = SIMULATION_DELAY * 1.0 / (obj.movementSpeed * (1 - ModelUtils.random() / 50));
		double dx = ds * Math.cos(obj.angle);
		double dy = ds * Math.sin(obj.angle);
		
		if (obj.attack != null && !obj.attack.isDestroyed()) {
			BufferedImage img = obj.attack.get();
			double w = img.getWidth();
			double h = img.getHeight();
			double x0 = obj.attack.x - w / 2;
			double x1 = x0 + w;
			double y0 = obj.attack.y - h / 2;
			double y1 = y0 + h;
			if (RenderTools.isLineIntersectingRectangle(obj.x, obj.y, obj.x + dx, 
					obj.y + dy, x0, y0, x1, y1)) {
				// walk along the angle up to ds units and see if there is a pixel of the target there?
				int tx0 = (int)(obj.attack.x - w / 2);
				int ty0 = (int)(obj.attack.y - h / 2);
				int tx1 = (int)(tx0 + w);
				int ty1 = (int)(ty0 + h);
				for (double dds = 0; dds <= ds; dds += 0.5) {
					int px = (int)(obj.x + dds * Math.cos(obj.angle));
					int py = (int)(obj.y + dds * Math.cos(obj.angle));
					if (tx0 <= px && px < tx1 && ty0 <= py && py < ty1) {
						int c = img.getRGB(px - tx0, py - ty0);
						if ((c & 0xFF000000) != 0) {
							obj.x = px;
							obj.y = py;
							return true;
						}
					}
				}
			}
		}
		obj.x += dx;
		obj.y += dy;
		return false;
	}
	
	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
			if (stopButton.enabled) {
				doStopSelectedShips();
				e.consume();
				return true;
			} else
			if (stopRetreat.enabled) {
				doStopRetreat();
				e.consume();
				return true;
			}
		}
		if (e.isControlDown()) {
			if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) {
				assignGroup(e.getKeyCode() - KeyEvent.VK_0);
				e.consume();
				return true;
			}
		}
		if (e.isShiftDown()) {
			if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) {
				recallGroup(e.getKeyCode() - KeyEvent.VK_0);
				e.consume();
				return true;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_A && e.isControlDown()) {
			doSelectAll();
			e.consume();
			return true;
		} else
		if (e.getKeyChar() == 'a' || e.getKeyChar() == 'A') {
			if (attackButton.enabled) {
				selectButton(attackButton);
				e.consume();
				return true;
			}
			e.consume();
		}
		if (e.getKeyChar() == 'g' || e.getKeyChar() == 'G') {
			if (guardButton.enabled) {
				doSelectionGuard();
				e.consume();
				return true;
			}
		}
		if (e.getKeyChar() == 'm' || e.getKeyChar() == 'M') {
			if (moveButton.enabled) {
				selectButton(moveButton);
				e.consume();
				return true;
			}
			e.consume();
		}
		if (e.getKeyChar() == 'r' || e.getKeyChar() == 'R') {
			if (rocketButton.enabled) {
				if (!rocketButton.selected) {
					selectButton(rocketButton);
				} else {
					selectButton(stopButton);
				}
				e.consume();
				return true;
			}
			e.consume();
		}
		if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			// back out of fight instantly
			commons.restoreMainSimulationSpeedFunction();
			commons.battleMode = false;
			commons.playRegularMusic();
			displayPrimary(Screens.STARMAP);
			return true;
		}
		return super.keyboard(e);
	}
	/** Select all player structures. */
	void doSelectAll() {
		for (SpacewarStructure s : structures) {
			s.selected = canControl(s);
		}
		enableFleetControls(true);
	}
	/**
	 * Select the specified button and deselect others.
	 * @param b the button to select
	 */
	void selectButton(ThreePhaseButton b) {
		for (ThreePhaseButton p : mainCommands) {
			p.selected = p == b;
		}
	}
	/** Switch selected ships to guard mode. */
	void doSelectionGuard() {
		for (SpacewarStructure ship : structures) {
			if (ship.selected 
					&& canControl(ship) && ship.kamikaze == 0) {
				ship.moveTo = null;
				ship.attack = null;
				ship.guard = true;
			}
		}
		enableFleetControls(true);
	}
	/**
	 * Create explosion object for the given spacewar structure.
	 * @param s the structure
	 * @param destroy should the explosion destroy the target?
	 */
	void createExplosion(SpacewarStructure s, boolean destroy) {
		SpacewarExplosion x = new SpacewarExplosion(getExplosionFor(s.destruction));
		x.owner = s.owner;
		if (destroy) {
			x.target = s;
		}
		x.x = s.x;
		x.y = s.y;
		explosions.add(x);
	}
	/**
	 * Get the explosion animation for a given sound effect.
	 * @param destruction the destruction sound
	 * @return the animation phases
	 */
	BufferedImage[] getExplosionFor(SoundType destruction) {
		switch (destruction) {
		case EXPLOSION_MEDIUM:
		case EXPLOSION_MEDIUM_2:
			return commons.spacewar().explosionMedium;
		case EXPLOSION_SHORT:
			return commons.spacewar().explosionSmall;
		default:
			return commons.spacewar().explosionLarge;
		}
	}
	/**
	 * Create explosion object for the given spatial location.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param explosion the sound effect
	 */
	void createExplosion(double x, double y, SoundType explosion) {
		SpacewarExplosion xp = new SpacewarExplosion(getExplosionFor(explosion));
		xp.x = x;
		xp.y = y;
		explosions.add(xp);
	}
	/** Perform the spacewar simulation. */
	void doSpacewarSimulation() {
		soundsToPlay.clear();
		// advance explosions
		for (SpacewarExplosion exp : new ArrayList<>(explosions)) {
			if (exp.next()) {
				explosions.remove(exp);
			} else
			if (exp.isMiddle() && exp.target != null && exp.target.isDestroyed()) {
				structures.remove(exp.target);
			}
		}
		
		handleProjectiles();
		
		List<SpacewarStructure> enemyIdles = new ArrayList<>();
		List<SpacewarStructure> playerIdles = new ArrayList<>();
		// fleet movements
		for (SpacewarStructure ship : new ArrayList<>(structures)) {
			if (!ship.isDestroyed()) {
				if (ship.kamikaze > 0) {
					handleKamikaze(ship);
					continue;
				}
				if (ship.owner.traits.has(TraitKind.COMBAT_ENGINEERS)) {
					if (ship.shieldMax > 0) {
						boolean sg = false;
						if (ship.item != null) {
							for (InventorySlot is : ship.item.slots.values()) {
								if (is.type != null && is.type.has(ResearchType.PARAMETER_SHIELD) && is.count > 0) {
									sg = true;
								}
							}
						} else {
							sg = true;
						}
						if (sg) {
							ship.shield = Math.min(ship.shieldMax, ship.shield + 1); // FIXME rate?
						}
					}
				}

				// general cooldown of weapons
				for (SpacewarWeaponPort p : ship.ports) {
					p.cooldown = Math.max(0, p.cooldown - SIMULATION_DELAY);
				}
				if (ship.moveTo != null) {
					// rotate into correct angle if needed
					if (rotateStep(ship, ship.moveTo.x, ship.moveTo.y)) {
						if (moveStep(ship, ship.moveTo.x, ship.moveTo.y, 0)) {
							ship.moveTo = null;
						}
					}
				} else
				if (ship.attack != null) {
					if (ship.attack.isDestroyed() 
							|| (ship.guard && ship.inRange(ship.attack).isEmpty())
							|| (!ship.attack.intersects(0, 0, space.width, space.height))) {
						ship.attack = null;
						ship.guard = true;
						if (ship.selected && ship.owner == player()) {
							enableSelectedFleetControls();
						}
					} else {
						if (ship.type == StructureType.STATION 
								|| rotateStep(ship, ship.attack.x, ship.attack.y)) {
							// move into minimum attack range if needed
							if (!ship.guard && ship.type == StructureType.SHIP) {
								moveStep(ship, ship.attack.x, ship.attack.y, ship.minimumRange - 5);
							}
							fireAtTargetOf(ship);
						}
					}
				} else
				if (ship.guard) {
					// pick a target
					if (canControl(ship)) {
						selectNewTarget(ship);
					} else {
						enemyIdles.add(ship);
					}
				} else
				if (!canControl(ship)) {
					enemyIdles.add(ship);
				} else {
					if (!handleAutofire(ship)) {
						playerIdles.add(ship);
					}
				}
			}
		}
		
		boolean isEnemyFleeing = battle.enemyFlee;
		SpacewarAction act1 = player().ai.spaceBattle(this, playerIdles);
		SpacewarAction act2 = nonPlayer().ai.spaceBattle(this, enemyIdles);
		
		SpacewarScriptResult r = world().scripting.onSpacewarStep(this);
		
		for (SoundType st : soundsToPlay) {
			effectSound(st);
		}
		Player winner = null;
		if (act1 == SpacewarAction.FLEE) {
			retreat.visible = false;
			confirmRetreat.visible = false;
			stopRetreat.visible = true;
			enableSelectedFleetControls();
		} else
		if (act1 == SpacewarAction.SURRENDER) {
			winner = nonPlayer();
		} else
		if (act2 == SpacewarAction.SURRENDER) {
			winner = player();
		}
		if (r == SpacewarScriptResult.PLAYER_WIN) {
			winner = player();
		} else
		if (r == SpacewarScriptResult.PLAYER_LOSE) {
			winner = nonPlayer();
		}
		if (winner == null) {
			winner = checkWinner();
		}
		if (winner != null 
				&& ((explosions.size() == 0 && projectiles.size() == 0)
						/* || r != SpacewarScriptResult.CONTINUE */)) {
			commons.simulation.pause();
			concludeBattle(winner);
			askRepaint();
			return;
		}
		
		// chat switch to flee
		if (isEnemyFleeing != battle.enemyFlee) {
			chatFlee();
		}
		
		
		enableSelectedFleetControls();
		askRepaint();
	}
	/** Toggle to chat flee. */
	void chatFlee() {
		if (chat != null && battle.enemyFlee) {
			Node fn = chat.getFlee();
			if (fn != null && node != fn) {
				node = fn;
				
				leftChatPanel.options.clear();
				rightChatPanel.options.clear();
				
				leftChatPanel.addLine(TextRenderer.YELLOW, get(fn.message));
				rightChatPanel.addLine(TextRenderer.YELLOW, get(fn.message));
			}
		}
	}
	/**
	 * Handle the automatic fire for ships and stations.
	 * @param ship the ship under evaluation
	 * @return true if action taken
	 */
	boolean handleAutofire(SpacewarStructure ship) {
		if (ship.type == StructureType.STATION || ship.type == StructureType.PROJECTOR) {
			selectNewTarget(ship);
			if (ship.attack != null) {
				return true;
			}
		} else
		if (ship.type == StructureType.SHIP && ship.kamikaze == 0) {
			for (SpacewarStructure es : enemiesInRange(ship)) {
				if (es.kamikaze > 0) {
					ship.attack = es;
					return true;
				}
			}
		}
		
		return false;
	}
	/**
	 * Select a target in range which the current ship can do most
	 * damage.
	 * @param ship the current ship
	 */
	void selectNewTarget(final SpacewarStructure ship) {
		List<SpacewarStructure> es = enemiesInRange(ship);
		ModelUtils.shuffle(es);
		SpacewarStructure best = null;
		double bestEfficiency = 0d;
		for (SpacewarStructure s : es) {
			BattleEfficiencyModel bem = ship.getEfficiency(s);
			double eff = bem != null ? bem.damageMultiplier : 1d;
			if (eff > bestEfficiency) {
				best = s;
				bestEfficiency = eff;
			}
		}
		ship.attack = best;
	}
	/**
	 * Choose a new target for the ship.
	 * @param ship the ship
	 */
	void chooseNewTarget(SpacewarStructure ship) {
		Double backfire = world().battle.backfires.get(world().difficulty);
		
		List<SpacewarStructure> sts = new ArrayList<>();
		for (SpacewarStructure s : structures) {
			if (s.type == StructureType.SHIP || s.type == StructureType.SHIELD 
					|| s.type == StructureType.STATION || s.type == StructureType.PROJECTOR) {
				if (backfire != null) {
					if (s.owner == ship.owner && backfire < ModelUtils.random()) {
						continue;
					}
				}
				sts.add(s);
			}
		}
		if (sts.size() > 0) {
			ship.attack = ModelUtils.random(sts);
		}
	}
	/**
	 * Handle kamikaze units (and rockets).
	 * @param ship the ship
	 */
	void handleKamikaze(SpacewarStructure ship) {
		if (ship.attack != null && !ship.attack.isDestroyed()) {
			rotateStep(ship, ship.attack.x, ship.attack.y);

			double d = Math.hypot(ship.x - ship.attack.x, ship.y - ship.attack.y);
			if (d < 80 && ship.type != StructureType.SHIP && ship.attack.ecmLevel > 0) {
				if (scrambled.add(ship)) {
					double p = ModelUtils.random();
					
					int anti = ship.ecmLevel;
					int ecm = ship.attack.ecmLevel;

					boolean newTarget = world().battle.getAntiECMProbability(world().difficulty, anti, ecm) <= p;
					
					if (newTarget) {
						chooseNewTarget(ship);
					}
				}
			}
		}
		if (moveStep(ship)) {
			SpacewarStructure parent = rocketParent.get(ship);
			if (parent == null) {
				parent = ship;
			}
			if (ship.type == StructureType.MULTI_ROCKET) {
				doMultiRocketExplosion(parent, ship);
			} else {
				damageTarget(
						parent,
						ship.attack, 
						ship.kamikaze, 
						ship.destruction,
						ship.techId,
						ship.owner.id);
			}
			createLoss(ship);
			createExplosion(ship, true);
			if (ship.type == StructureType.VIRUS_BOMB 
					&& (ship.attack.type == StructureType.PROJECTOR || ship.attack.type == StructureType.SHIELD)) {
				battle.infectPlanet = ship.attack.planet;
			}
		} else
		if (ship.attack != null && ship.attack.isDestroyed()) {
			ship.attack = null;
		}
		if (ship.attack == null && !ship.intersects(0, 0, space.width, space.height)) {
			createLoss(ship);
			structures.remove(ship);
		}
	}
	/**
	 * Create a multirocket explosion around the given ship's target.
	 * @param source the source of the damage
	 * @param ship the ship
	 */
	void doMultiRocketExplosion(
			SpacewarStructure source,
			SpacewarStructure ship) {
		double area = world().battle.getDoubleProperty(ship.techId, ship.owner.id, "area");
		int sn = world().battle.getIntProperty(ship.techId, ship.owner.id, "secondary-count");

		damageArea(
				source,
				ship.attack, 
				ship.kamikaze,
				area,
				SoundType.EXPLOSION_MEDIUM, 
				ship.techId, 
				ship.owner.id);
		
		if (sn > 0) {
			
			double sa = world().battle.getDoubleProperty(ship.techId, ship.owner.id, "secondary-area");
			double sr = world().battle.getDoubleProperty(ship.techId, ship.owner.id, "secondary-radius");
			double sm = world().battle.getDoubleProperty(ship.techId, ship.owner.id, "secondary-multiplier");
			
			double dalpha = Math.PI * 2 / sn;
			for (double a = 0; a < Math.PI * 2; a += dalpha) {
				double x2 = ship.attack.x + sr * Math.cos(a);
				double y2 = ship.attack.y + sr * Math.sin(a);

				createExplosion(x2, y2, SoundType.EXPLOSION_MEDIUM);
				damageArea(source, x2, y2, (ship.kamikaze * sm), sa, SoundType.EXPLOSION_MEDIUM, ship.techId, ship.owner.id);
			}
		}
	}
	/**
	 * Count the ship destruction as loss.
	 * @param ship the ship
	 */
	void createLoss(SpacewarStructure ship) {
		if (ship.type == StructureType.SHIP) {
			battle.spaceLosses.add(ship);
			if (ship.owner == battle.attacker.owner) {
				battle.attackerLosses += ship.count;
			} else {
				battle.defenderLosses += ship.count;
			}
		}
		ship.hp = 0;
		ship.shield = 0;
		ship.loss += ship.count;
		ship.count = 0;
	}
	/**
	 * Handle projectile movement, impact and rocket scrambling.
	 */
	void handleProjectiles() {
		// move projectiles
		for (SpacewarProjectile p : new ArrayList<>(projectiles)) {
			if (moveStep(p)) {
				projectiles.remove(p);
				
				damageTarget(p.source, p.target, p.damage, p.impactSound, p.model.id, p.owner.id);
			} else
			if (!p.intersects(0, 0, space.width, space.height)) {
				projectiles.remove(p);
			}
		}
	}
	/**
	 * Damage the space around the specified target.
	 * If the nearby structures represents fighters, they get uniform damage each.
	 * @param source the source of the damage
	 * @param target the target
	 * @param damage the damage
	 * @param area the affected area
	 * @param impactSound the impact sound
	 * @param techId the technology that inflicted the damage
	 * @param owner the owner of the technology
	 */
	void damageArea(
			SpacewarStructure source,
			SpacewarStructure target, 
			double damage,
			double area,
			SoundType impactSound,
			String techId,
			String owner) {
		if (area < 0) {
			damageTarget(source, target, 
					target.count * damage, impactSound, techId, owner);
		} else {
			for (SpacewarStructure s : U.newArrayList(structures)) {
				if (!isAlly(s, world().players.get(owner))) {
					double d = Math.hypot(s.x - target.x, s.y - target.y) - s.get().getWidth() / 2d;
					if (d <= area) {
						d = Math.max(0, d);
						damageTarget(source, s, 
								(s.count * damage * (area - d) / area), impactSound, techId, owner);
					}
				}
			}
		}
	}
	/**
	 * Damage the space around the specified coordinates.
	 * If the nearby structures represents fighters, they get uniform damage each.
	 * @param source the source of the damage
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param damage the damage
	 * @param area the affected area
	 * @param impactSound the impact sound
	 * @param techId the technology that inflicted the damage
	 * @param owner the owner of the technology
	 */
	void damageArea(
			SpacewarStructure source,
			double x, 
			double y, 
			double damage,
			double area,
			SoundType impactSound,
			String techId,
			String owner) {
		for (SpacewarStructure s : U.newArrayList(structures)) {
			if (!isAlly(s, world().players.get(owner))) {
				double d = Math.hypot(s.x - x, s.y - y) - s.get().getWidth() / 2d;
				if (d <= area) {
					if (area > 0) {
						d = Math.max(0, d);
						damageTarget(source, s, 
								(s.count * damage * (area - d) / area), impactSound, techId, owner);
					} else {
						damageTarget(source, s, 
								s.count * damage, impactSound, techId, owner);
					}
				}
			}
		}
	}
	
	/**
	 * Test if the given spacewar structure belongs to the player of one of its allies.
	 * @param s the structure
	 * @param p the player
	 * @return true if ally
	 */
	boolean isAlly(SpacewarStructure s, Player p) {
		return battle.isAlly(s, p);
	}
	/**
	 * Damage the target structure.
	 * @param source the source
	 * @param target the target
	 * @param damage the damage
	 * @param impactSound the impact sound
	 * @param techId the technology that inflicted the damage
	 * @param owner the owner of the technology
	 */
	void damageTarget(
			SpacewarStructure source,
			SpacewarStructure target, 
			double damage, 
			SoundType impactSound,
			String techId,
			String owner) {
		int loss0 = target.loss;
		
		if (target.damage(damage)) {
			
			battle.spaceLosses.add(target);
			soundsToPlay.add(target.destruction);
			createExplosion(target, true);
			if (target.type == StructureType.SHIELD) {
				dropGroundShields();
			}
			int d = target.loss - loss0;
			if (isAlly(target, battle.attacker.owner)) {
				battle.attackerLosses += d;
			} else {
				battle.defenderLosses += d;
			}
			
			if (source.item != null) {
				source.item.kills += d;
				source.item.killsCost += target.value * d;
			}
			
			if (target.type == StructureType.SHIP || target.type == StructureType.STATION) {
				long cost = target.item.unitSellValue() * 2;
				target.owner.statistics.shipsLost.value += d;
				target.owner.statistics.shipsLostCost.value += cost * d;

				Player p2 = world().players.get(owner);
				if (p2 != null) {
					p2.statistics.shipsDestroyed.value += d;
					p2.statistics.shipsDestroyedCost.value += cost * d;
				}
			}
		} else {
			soundsToPlay.add(impactSound);
		}
		if (target.building != null) {
			damageBuildings(source, target, techId, damage, owner, impactSound);
		}
	}
	/**
	 * Damage the buildings around the target structure.
	 * @param source the source of the damage
	 * @param target the target
	 * @param damage the original damage
	 * @param techId the impactor
	 * @param owner the owner of the impactor
	 * @param impactSound the impact sound
	 */
	void damageBuildings(
			SpacewarStructure source,
			SpacewarStructure target, 
			String techId, double damage,
			String owner, SoundType impactSound) {
		String sradius = world().battle.getProperty(techId, owner, "ground-radius");
		String spercent = world().battle.getProperty(techId, owner, "damage-percent");
		if (sradius == null || spercent == null) {
			return;
		}
		double radius = Double.parseDouble(sradius);
		double percent = Double.parseDouble(spercent) / 100;
		Point2D.Double center = buildingCenter(target.building);
		
		Map<Building, Double> damaged = new HashMap<>();
		
		for (Building b : target.planet.surface.buildings.iterable()) {
			if (b != target.building) {
				Point2D.Double loc = buildingCenter(b);
				double d = loc.distance(center); 
				if (d < radius) {
					double applyDamage = (damage * percent * (radius - d) / radius);
					damaged.put(b, applyDamage);
				}
			}
		}
		
		for (SpacewarStructure s : structures) {
			if (damaged.containsKey(s.building)) {
				damageTarget(source, s, damaged.get(s.building), impactSound, null, null);
				damaged.remove(s.building);
			}
		}
		for (Map.Entry<Building, Double> e : damaged.entrySet()) {
			double d = e.getValue();
			Building b = e.getKey();
			
			int hpMax = world().getHitpoints(b.type, target.planet.owner, true);
			b.hitpoints = (int)Math.max(0, b.hitpoints - d * 1.0 * b.type.hitpoints / hpMax);
			if (b.hitpoints <= 0) {
				target.planet.surface.removeBuilding(b);
				
				target.planet.owner.statistics.buildingsLost.value++;
				target.planet.owner.statistics.buildingsLostCost.value += b.type.cost * (1 + b.upgradeLevel);
				
				Player p2 = world().players.get(owner);
				if (p2 != null) {
					p2.statistics.buildingsDestroyed.value++;
					p2.statistics.buildingsDestroyedCost.value += b.type.cost * (1 + b.upgradeLevel);
				}
			}
		}
	}
	/**
	 * The center coordinate of the building.
	 * @param b the building
	 * @return the center location
	 */
	public Point2D.Double buildingCenter(Building b) {
		Tile ts = b.tileset.normal;
		return new Point2D.Double(b.location.x + ts.width / 2.0, b.location.y - ts.height / 2.0);
	}
	/**
	 * Drop ground shields.
	 */
	void dropGroundShields() {
		List<SpacewarStructure> ground = new ArrayList<>();
		int shieldCount = 0;
		for (SpacewarStructure s : structures) {
			if (s.type == StructureType.PROJECTOR) {
				ground.add(s);
			}
			if (s.type == StructureType.SHIELD) {
				ground.add(s);
				shieldCount++;
			}
		}
		for (SpacewarStructure s : ground) {
			if (shieldCount > 1) {
				s.shield /= 2;
			} else {
				s.shield = 0;
			}
		}			
	}
	/** @return the player who won the battle, null if nof yet finished */
	Player checkWinner() {
		int playerUnits = 0;
		int nonplayerUnits = 0;
		Player other = null;
		for (SpacewarStructure s : structures) {
			if (canControl(s)) {
				playerUnits++;
			} else {
				nonplayerUnits++;
				other = s.owner;
			}
		}
		if (playerUnits == 0) {
			return other;
		} else
		if (nonplayerUnits == 0) {
			return player();
		}
		if (/* stopRetreat.visible && */playerRetreatedBeyondScreen(player())) {
			return other;
		} else
		if (playerRetreatedBeyondScreen(other)) {
			return player();
		}
		return null;
	}
	@Override
	public List<SpacewarStructure> enemiesOf(Player p) {
		List<SpacewarStructure> result = new ArrayList<>();
		for (SpacewarStructure f : structures) {
			if (f.owner != p) {
				if (!areAllies(f.owner, p)) {
					result.add(f);
				}
			}
		}
		return result;
	}
	@Override
	public List<SpacewarStructure> enemiesOf(SpacewarStructure ship) {
		return enemiesOf(ship.owner);
	}
	/**
	 * Check if two structures are allies.
	 * @param p1 the first player
	 * @param p2 the second player
	 * @return true if allies
	 */
	boolean areAllies(Player p1, Player p2) {
		return battle.isAlly(p1, p2) || battle.isAlly(p2, p1);
	}
	/**
	 * Fire at the target of the given ship with the available weapons.
	 * @param ship the attacker ship
	 */
	void fireAtTargetOf(SpacewarStructure ship) {
		for (SpacewarWeaponPort p : ship.inRange(ship.attack)) {
			if (p.cooldown <= 0) {
				createBeam(ship, p, ship.attack.x, 
						ship.attack.y, ship.attack);
				p.cooldown = p.projectile.delay;
			}
		}
	}
	/**
	 * Create a beam aimed at (ax, ay) and should hit the target only.
	 * @param source the 
	 * @param p the projectile settings
	 * @param ax the aim X
	 * @param ay the aim Y
	 * @param target the targeted structure
	 */
	void createBeam(SpacewarStructure source, SpacewarWeaponPort p, 
			double ax, double ay, SpacewarStructure target) {
		SpacewarProjectile sp = new SpacewarProjectile(source.owner == player() ? p.projectile.alternative : p.projectile.matrix);
		sp.model = p.projectile;
		sp.owner = source.owner; 
		sp.source = source;
		sp.target = target;
		sp.movementSpeed = p.projectile.movementSpeed;
		sp.impactSound = SoundType.HIT;
		sp.x = source.x;
		sp.y = source.y;
		sp.angle = Math.atan2(ay - sp.y, ax - sp.x);
		
		sp.damage = attackDamage(p.damage(sp.owner) * p.count, target);

		BattleEfficiencyModel bem = source.getEfficiency(target);
		if (bem != null) {
			sp.damage = sp.damage * bem.damageMultiplier;
		}
		
		projectiles.add(sp);
		effectSound(p.projectile.sound);
	}
	/**
	 * Apply loss results back to the initial fleets and planets.
	 * @param winner the winner of the fight
	 */
	void concludeBattle(Player winner) {
		
		boolean groundLosses = false;
		Set<Fleet> fleets = new HashSet<>();
		Set<Planet> planets = new HashSet<>();
		
		// reset surviving stucture counts
		for (SpacewarStructure s : structures) {
			if (s.fleet != null) {
				fleets.add(s.fleet);
			}
			if (s.planet != null) {
				planets.add(s.planet);
			}
			if (s.item != null) {
				s.item.count = 0;
			}
		}
		// remove destroyed buildings or items
		for (SpacewarStructure s : battle.spaceLosses) {
			if (s.building != null) {
				s.planet.surface.removeBuilding(s.building);
				planets.add(s.planet);
				groundLosses = true;
				
				s.planet.owner.statistics.buildingsLost.value++;
				s.planet.owner.statistics.buildingsLostCost.value += s.building.type.cost * (1 + s.building.upgradeLevel);
				
				battle.attacker.owner.statistics.buildingsDestroyed.value++;
				battle.attacker.owner.statistics.buildingsDestroyedCost.value += s.building.type.cost * (1 + s.building.upgradeLevel);

			}
			if (s.item != null) {
				s.item.count = 0;
			}
			if (s.fleet != null) {
				fleets.add(s.fleet);
			}
			if (s.planet != null) {
				planets.add(s.planet);
			}
		}
		// process surviving structures
		for (SpacewarStructure s : structures) {
			if (s.item != null) {
				s.item.count += s.count;
				s.item.hp = (int)s.hp;
				s.item.shield = (int)s.shield;
			} else
			if (s.building != null) {
				s.building.hitpoints = (int)(1L * s.hp * s.building.type.hitpoints / s.hpMax);
				groundLosses = true;
			}
		}

		// rebuild roads.
		for (Planet p : planets) {
			p.rebuildRoads();
			p.cleanup();
		}
		// if the planet was fired upon
		if (groundLosses) {
			// reduce population according to the battle statistics
			if (battle.targetPlanet != null) {
				if (winner == battle.targetPlanet.owner) {
					BattleSimulator.applyPlanetDefended(battle.targetPlanet, BattleSimulator.PLANET_DEFENSE_LOSS);
				} else {
					BattleSimulator.applyPlanetConquered(battle.targetPlanet, BattleSimulator.PLANET_CONQUER_LOSS);
				}
			} else
			if (battle.helperPlanet != null) {
				BattleSimulator.applyPlanetDefended(battle.helperPlanet, BattleSimulator.PLANET_DEFENSE_LOSS / 2);
			}
		}
		
		// cleanup fleets
		for (Fleet f : fleets) {
			f.cleanup();
			int gu = f.loseVehicles(battle.enemy(f.owner));
			if (f.owner == battle.attacker.owner) {
				battle.attackerGroundLosses += gu;
			} else {
				battle.defenderGroundLosses += gu;
			}
			if (f.inventory.isEmpty()) {
				world().removeFleet(f);
				world().scripting.onLost(f);
				f.owner.statistics.fleetsLost.value++;
			}
		}
		battle.spacewarWinner = winner;
		battle.retreated = stopRetreat.visible;

		// apply statistics
		battle.incrementSpaceWin();


		player().ai.spaceBattleDone(this);
		nonPlayer().ai.spaceBattleDone(this);
		world().scripting.onSpacewarFinish(this);
		
		if (battle.infectPlanet != null) {
			int ttl0 = battle.infectPlanet.quarantineTTL; 
			battle.infectPlanet.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
			if (ttl0 == 0) {
				world().scripting.onPlanetInfected(battle.infectPlanet);
			}
		}
		
		// attacker wins
		final BattleInfo bi = battle;
		if (battle.attacker.owner == winner) {
			// originally attacking the planet
			if (battle.targetPlanet != null && battle.targetPlanet.owner != null) {
				FleetStatistics fs = battle.attacker.getStatistics();
				if (fs.vehicleCount > 0) {
					commons.stopMusic();
					
					MovieScreen ms = commons.control().getScreen(Screens.MOVIE);
					ms.transitionFinished = new Action0() {
						@Override
						public void invoke() {
							player().currentPlanet = bi.targetPlanet;
							displayPrimary(Screens.COLONY);
						}
					};
					
					commons.playVideo("groundwar/felall", new Action0() {
						@Override
						public void invoke() {
							PlanetScreen ps = (PlanetScreen)displayPrimary(Screens.COLONY);
							
							ps.initiateBattle(bi);
							commons.playBattleMusic();
						}
					});
					return;
				}
			}
		}
		
		if (!(bi.targetFleet != null 
				&& bi.targetFleet.owner.id.equals("Traders") 
				&& (bi.enemyFlee || !bi.targetFleet.inventory.isEmpty())
				&& bi.targetFleet.task != FleetTask.SCRIPT
			)) {
			BattlefinishScreen bfs = (BattlefinishScreen)displaySecondary(Screens.BATTLE_FINISH);
			bfs.displayBattleSummary(bi);
		} else {
			commons.restoreMainSimulationSpeedFunction();
			commons.battleMode = false;
			commons.playRegularMusic();
			commons.simulation.speed(battle.originalSpeed);
			bi.battleFinished();
			displayPrimary(Screens.STARMAP);
		}
	}
	/**
	 * Check if the planet has bunker.
	 * @param planet the target planet
	 * @return true if has bunker
	 */
	boolean hasBunker(Planet planet) {
		return !planet.surface.buildings.findByKind("Bunker").isEmpty();
	}
	/**
	 * Remove the structure from its parent inventory (either a planet or a fleet).
	 * @param s the structore
	 */
	void removeFromInventory(SpacewarStructure s) {
		if (s.planet != null) {
			s.planet.inventory.remove(s.item);
		} else
		if (s.fleet != null) {
			s.fleet.inventory.remove(s.item);
		} else {
			throw new AssertionError(String.format(
					"Neither planet nor fleet set on structure: Owner = %s, Type = %s", s.owner.id, s.item.type.id));
		}
	}
	/**
	 * Retrieve the info image of the specified name.
	 * @param name the name
	 * @return the image
	 */
	public BufferedImage getInfoImage(String name) {
		BufferedImage result = infoImages.get(name);
		if (result == null) {
			result = rl.getImage(name);
			infoImages.put(name, result);
		}
		return result;
	}
	@Override
	public List<SpacewarStructure> structures() {
		return structures;
	}
	@Override
	public BattleInfo battle() {
		return battle;
	}
	@Override
	public void flee(SpacewarStructure s) {
		boolean fleeLeft = false;
		if (s.owner == battle.attacker.owner) {
			if (!attackerOnRight) {
				fleeLeft = true;
			}
		} else {
			if (attackerOnRight) {
				fleeLeft = true;
			}
			// flee to the right side
		}
		fleeLeft = fleeLeft ^ battle.invert;
		if (fleeLeft) {
			s.moveTo = new Point2D.Double(-1000, s.y);
		} else {
			s.moveTo = new Point2D.Double(space.width + 1000, s.y);
		}
		
		s.attack = null;
		s.guard = false;
		if (s.selected && s.owner == player()) {
			enableSelectedFleetControls();
		}
	}
	@Override
	public List<SpacewarStructure> structures(Player owner) {
		List<SpacewarStructure> result = new ArrayList<>();
		for (SpacewarStructure s : structures) {
			if (s.owner == owner) {
				result.add(s);
			}
		}
		return result;
	}
	@Override
	public int facing() {
		return battle.attacker.owner == player() && !attackerOnRight ? -1 : 1;
	}
	/** @return view command selected */
	public boolean viewCommandSelected() {
		return viewCommand.selected;
	}
	/** @return view damage selected */
	public boolean viewDamageSelected() {
		return viewDamage.selected;
	}
	/** @return view range selected */
	public boolean viewRangeSelected() {
		return viewRange.selected;
	}
	/** @return view grid selected */
	public boolean viewGridSelected() {
		return viewGrid.selected;
	}
	/**
	 * Set view command selected.
	 * @param selected the new value
	 */
	public void viewCommandSelected(boolean selected) {
		viewCommand.selected = selected;
	}
	/**
	 * Set view damage selected.
	 * @param selected the new value
	 */
	public void viewDamageSelected(boolean selected) {
		viewDamage.selected = selected;
	}
	/**
	 * Set view range selected.
	 * @param selected the new value
	 */
	public void viewRangeSelected(boolean selected) {
		viewRange.selected = selected;
	}
	/**
	 * Set view grid selected.
	 * @param selected the new value
	 */
	public void viewGridSelected(boolean selected) {
		viewGrid.selected = selected;
	}
	/**
	 * The selection cell.
	 * @author akarnokd, 2012.01.11.
	 */
	class SelectionCell {
		/** The object's name. */
		String name;
		/** The object's owner. */
		String owner;
		/** The objects parent. */
		String parent;
		/** The color of the owner. */
		int color;
		/** The image. */
		BufferedImage image;
		/** The shield ratio. */
		double shieldRatio;
		/** The HP ratio. */
		double hpRatio;
		/** The firepower. */
		double firepower;
		/** The damage per second. */
		double dps;
		/** The rocket count. */
		int rockets;
		/** The bomb count. */
		int bombs;
		/** Count. */
		int count;
		/** The maximum image size. */
		static final int MAX_IMAGE = 50;
		/** Hitpoints. */
		int hp;
		/** Shield points. */
		int sp;
		/** The computed location of the cell. */
		Rectangle bounds;
		/** The original structure. */
		final SpacewarStructure structure;
		/**
		 * Construct a cell.
		 * @param s the original structure
		 */
		public SelectionCell(SpacewarStructure s) {
			this.structure = s;
			
			image = s.angles[0];
			if (s.item != null) {
				name = s.item.type.name;
			} else
			if (s.building != null) {
				name = s.building.type.name;
			}
			owner = s.owner.name;
			color = s.owner.color;
			if (s.fleet != null) {
				parent = s.fleet.name;
			} else
			if (s.planet != null) {
				parent = s.planet.name();
			}
			
			hp = (int)s.hp;
			
			hpRatio = 1.0 * s.hp / s.hpMax;
			if (s.shieldMax > 0) {
				shieldRatio = 1.0 * s.shield / s.shieldMax;
				sp = (int)s.shield; 
			} else {
				shieldRatio = -1;
				sp = -1;
			}
			count = s.count;
			
			for (SpacewarWeaponPort p : s.ports) {
				if (p.projectile.mode == Mode.BEAM) {
					double dmg = p.damage(s.owner);
					firepower += p.count * dmg;
					dps += p.count * dmg * 1000.0 / p.projectile.delay;
				} else
				if (p.projectile.mode == Mode.ROCKET || p.projectile.mode == Mode.MULTI_ROCKET) {
					rockets += p.count;
				} else
				if (p.projectile.mode == Mode.BOMB || p.projectile.mode == Mode.VIRUS) {
					bombs += p.count;
				}
			}
			dps = Math.round(dps);

		}
		/**
		 * Returns the rendering width.
		 * @return the rendering width
		 */
		int width() {
			int w = 0;
			if (count < 2) {
				w = Math.max(w, commons.text().getTextWidth(7, format("spacewar.selection.name", name)));
			} else {
				w = Math.max(w, commons.text().getTextWidth(7, format("spacewar.selection.name_count", name, count)));
			}
			w = Math.max(w, commons.text().getTextWidth(7, format("spacewar.selection.owner", owner)));
			w = Math.max(w, commons.text().getTextWidth(7, format("spacewar.selection.parent", parent)));
			w = Math.max(w, commons.text().getTextWidth(7, format("spacewar.selection.firepower_dps", (int)firepower, dps)));
			if (rockets > 0 || bombs > 0) {
				w = Math.max(w, commons.text().getTextWidth(7, format("spacewar.selection.bombs_rockets", bombs, rockets)));
			}
			// defense values
			int dv = commons.text().getTextWidth(7, get("spacewar.selection.defense_values"));
			dv += commons.text().getTextWidth(7, Integer.toString(hp));
			if (sp >= 0) {
				dv += commons.text().getTextWidth(7, " + ");
				dv += commons.text().getTextWidth(7, Integer.toString(sp));
				dv += commons.text().getTextWidth(7, " = ");
				dv += commons.text().getTextWidth(7, Integer.toString(sp + hp));
			}
			w = Math.max(w, dv);
			
			return w + Math.min(MAX_IMAGE, image.getWidth()) + 15;
		}
		/**
		 * The total text height.
		 * @return the total text height
		 */
		int textHeight() {
			int rows = 5;
			if (rockets > 0 || bombs > 0) {
				rows++;
			}
			return 7 * rows + (rows - 1) * 2;
		}
		/**
		 * Returns the rendering height.
		 * @return the rendering height
		 */
		int height() {
			int h = MAX_IMAGE + 10;
			h = Math.max(h, textHeight() + 2);
			return h;
		}
		/**
		 * Draws the cell.
		 * @param g2 the graphics context
		 * @param x0 the origin X
		 * @param y0 the origin Y
		 */
		public void draw(Graphics2D g2, int x0, int y0) {
			g2.translate(x0, y0);
			
			g2.setColor(Color.LIGHT_GRAY);

			int h = height();
//			int w = width();
			
//			g2.drawRect(0, 0, w, h);
			int dy = (h - 10 - Math.min(50, image.getHeight())) / 2;
			
			g2.setColor(Color.GREEN);
			int iw = Math.min(image.getWidth(), MAX_IMAGE);
			int iw2 = (int)(iw * hpRatio);
			g2.drawRect(0, dy + 0, iw, 4);
			g2.fillRect(0, dy + 0, iw2, 4);
			
			if (shieldRatio >= 0) {
				g2.setColor(Color.ORANGE);
				iw2 = (int)(iw * shieldRatio);
				g2.drawRect(0, dy + 6, iw, 4);
				g2.fillRect(0, dy + 6, iw2, 4);
			}
			
			if (image.getWidth() > MAX_IMAGE || image.getHeight() > MAX_IMAGE) {
				double scalex = 1.0 * MAX_IMAGE / image.getWidth();
				double scaley = 1.0 * MAX_IMAGE / image.getHeight();
				double scale = Math.min(scalex, scaley);
				
				int imw = (int)(image.getWidth() * scale);
				int imh = (int)(image.getHeight() * scale);
				
				int dx = (MAX_IMAGE - imw) / 2;
				dy = (MAX_IMAGE - imh) / 2 + 10;
				
				g2.drawImage(image, dx, dy, imw, imh, null);
			} else {
				g2.drawImage(image, 0, dy + 10, null);
			}
			
			int dx = Math.min(MAX_IMAGE, image.getWidth()) + 4;
			
			dy = (h - textHeight()) / 2;
			
			if (count > 1) {
				commons.text().paintTo(g2, dx, dy, 7, color, format("spacewar.selection.name_count", name, count));
			} else {
				commons.text().paintTo(g2, dx, dy, 7, color, format("spacewar.selection.name", name));
			}
			commons.text().paintTo(g2, dx, dy + 9, 7, color, format("spacewar.selection.owner", owner));
			commons.text().paintTo(g2, dx, dy + 18, 7, color, format("spacewar.selection.parent", parent));
			commons.text().paintTo(g2, dx, dy + 27, 7, color, format("spacewar.selection.firepower_dps", (int)firepower, (int)dps));
			int y2 = dy + 36;
			if (rockets > 0 || bombs > 0) {
				commons.text().paintTo(g2, dx, y2, 7, color, format("spacewar.selection.bombs_rockets", bombs, rockets));
				y2 += 9;
			}
			
			List<TextSegment> tss = new ArrayList<>();
			tss.add(new TextSegment(get("spacewar.selection.defense_values"), color));
			tss.add(new TextSegment(Integer.toString(hp), Color.GREEN.getRGB()));
			if (sp >= 0) {
				tss.add(new TextSegment(" + ", color));
				tss.add(new TextSegment(Integer.toString(sp), Color.ORANGE.getRGB()));
				tss.add(new TextSegment(" = ", color));
				tss.add(new TextSegment(Integer.toString(hp + sp), TextRenderer.YELLOW));
			}
			commons.text().paintTo(g2, dx, y2, 7, tss);
			
			g2.translate(-x0, -y0);
		}
	}
	/**
	 * The panel showing information about the current selection. 
	 * @author akarnokd, 2012.01.11.
	 */
	class SelectionPanel extends UIContainer {
		/** The current scroll offset. */
		int offset = 0;
		/** The row height. */
		static final int ROW_HEIGHT = 30;
		/** The last selection. */
		List<SpacewarStructure> lastSelection = new ArrayList<>();
		/** The group buttons. */
		final List<UIImageButton> groupButtons = new ArrayList<>();
		/** The cells currently displayable. */
		final List<SelectionCell> cells = new ArrayList<>();
		/** Constructs the selection panel buttons. */
		public SelectionPanel() {
			int x = 5;
			for (int i = -1; i < 10; i++) {
				final int j = i;
				
				UIImageButton ib = new UIImageButton(commons.common().shield) {
					@Override
					public void draw(Graphics2D g2) {
						super.draw(g2);
						String s = Integer.toString(j);
						if (j < 0) {
							s = "*";
						}
						commons.text().paintTo(g2, 7, 3, 10, TextRenderer.WHITE, s);
					}
					@Override
					public boolean mouse(UIMouse e) {
						if (e.has(Button.RIGHT) && e.has(Type.DOWN)) {
							if (j >= 0) {
								removeGroup(j);
							} else {
								deselectAll();
							}
							return true;
						}
						return super.mouse(e);
					}
				};
				ib.onClick = new Action0() {
					@Override
					public void invoke() {
						if (j >= 0) {
							recallGroup(j);
						} else {
							doSelectAll();
						}
					}
				};
				
				groupButtons.add(ib);
				add(ib);
				ib.x = x;
				
				x += 25;
				
				if (i == -1) {
					ib.tooltip(get("battle.selectall.tooltip"));
				} else {
					ib.tooltip(format("battle.selectgroup.tooltip", i));
				}
			}
		}
		@Override
		public void draw(Graphics2D g2) {
			Set<Integer> groupSet = U.newSet(groups.values());
			groupSet.add(-1);
			for (int i = 0; i < 11; i++) {
				boolean v0 = groupButtons.get(i).visible();
				groupButtons.get(i).visible(groupSet.contains(i - 1));
				if (v0 != groupButtons.get(i).visible()) {
					commons.control().moveMouse();
				}
			}
			
			List<SpacewarStructure> sel = getSelection();
			
			if (sel.size() != lastSelection.size() || !lastSelection.containsAll(sel)) {
				offset = 0;
			}
			lastSelection = sel;
			
			
			Shape save = g2.getClip();
			g2.clipRect(0, 0, width, height);
			
			g2.setColor(Color.BLACK);
			
			g2.fillRect(0, 0, width, height);
			
			
			
			offset = Math.max(0, Math.min(offset, sel.size() - 1));
			
			cells.clear();
            for (SpacewarStructure s : sel) {
                SelectionCell c = new SelectionCell(s);
                cells.add(c);
            }
			int x0 = 0;
			int y0 = 22;
			int h = 0;
			int row = 0;
			for (SelectionCell c : cells) {
				int cw = c.width();
				if (x0 > 0 && x0 + cw >= width) {
					x0 = 0;
					y0 += h;
					h = 0;
					row++;
				}
				
				if (row >= offset) {
					c.draw(g2, x0, y0);
					int ch = c.height();
					c.bounds = new Rectangle(x0, y0, cw, ch);
					h = Math.max(h, ch);
				} else {
					c.bounds = null;
				}
				x0 += cw;
			}
			if (offset > row) {
				offset = row;
			}
			
			super.draw(g2);
			
			g2.setClip(save);
		}
		/** Clear any references. */
		@Override
		public void clear() {
			lastSelection.clear();
			cells.clear();
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.WHEEL)) {
				if (e.z < 0) {
					offset--;
				} else {
					offset++;
				}
				return true;
			}
			if (e.has(Type.DOWN) && e.has(Button.RIGHT) && e.y >= commons.common().shield[0].getHeight()) {
				removeFromSelection(e.x, e.y);
				displaySelectedShipInfo();
				return true;
			}
			if (e.has(Type.DOUBLE_CLICK) && e.has(Button.LEFT)) {
				if (e.has(Modifier.SHIFT)) {
					addTypeToSelection(e.x, e.y, e.z > 2);
					displaySelectedShipInfo();
				} else
				if (e.has(Modifier.CTRL)) {
					removeTypeFromSelection(e.x, e.y, e.z > 2);
					displaySelectedShipInfo();
				} else {
					retainTypeFromSelection(e.x, e.y, e.z > 2);
					displaySelectedShipInfo();
				}
				return true;
			}
			return super.mouse(e);
		}
		/**
		 * Add units with the same type (or category) to the current selection.
		 * @param mx the mouse coordinates
		 * @param my the mouse coordinates
		 * @param category affect entire category?
		 */
		void addTypeToSelection(int mx, int my, boolean category) {
			for (SelectionCell c : cells) {
				if (c.bounds != null && c.bounds.contains(mx, my)) {
					for (SpacewarStructure s : structures) {
						if (s.owner == c.structure.owner) {
							if (category) {
								ResearchType rt0 = world().researches.get(c.structure.techId);
								ResearchType rt1 = world().researches.get(s.techId);
								s.selected = (rt0.category == rt1.category);
							} else {
								s.selected = s.techId.equals(c.structure.techId);
							}
						}
					}
				}
			}
		}
		/**
		 * Retain the type (or category) in the current selection.
		 * @param mx the mouse coordinates
		 * @param my the mouse coordinates
		 * @param category affect entire category?
		 */
		void retainTypeFromSelection(int mx, int my, boolean category) {
			for (SelectionCell c : cells) {
				if (c.bounds != null && c.bounds.contains(mx, my)) {
					for (SelectionCell c1 : cells) {
						if (c1.structure.owner == c.structure.owner) {
							if (category) {
								ResearchType rt0 = world().researches.get(c.structure.techId);
								ResearchType rt1 = world().researches.get(c1.structure.techId);
								c1.structure.selected = (rt0.category == rt1.category);
							} else {
								c1.structure.selected = c1.structure.techId.equals(c.structure.techId);
							}
						}
					}
					break;
				}
			}
			
		}
		/**
		 * Remove the type (or category) from the current selection.
		 * @param mx the mouse coordinates
		 * @param my the mouse coordinates
		 * @param category affect entire category?
		 */
		void removeTypeFromSelection(int mx, int my, boolean category) {
			for (SelectionCell c : cells) {
				if (c.bounds != null && c.bounds.contains(mx, my)) {
					for (SelectionCell c1 : cells) {
						if (c1.structure.owner == c.structure.owner) {
							if (category) {
								ResearchType rt0 = world().researches.get(c.structure.techId);
								ResearchType rt1 = world().researches.get(c1.structure.techId);
								c1.structure.selected = !(rt0.category == rt1.category);
							} else {
								c1.structure.selected = !c1.structure.techId.equals(c.structure.techId);
							}
						}
					}
					break;
				}
			}
		}
		/**
		 * Remove the cell(ship) from the selection.
		 * @param mx the mouse X
		 * @param my the mouse Y
		 */
		void removeFromSelection(int mx, int my) {
			for (SelectionCell c : cells) {
				if (c.bounds != null && c.bounds.contains(mx, my)) {
					c.structure.selected = false;
				}
			}
		}
	}
	/** Deselect everything. */
	void deselectAll() {
		for (SpacewarStructure s : structures) {
			s.selected = false;
		}
		enableSelectedFleetControls();
	}
	/**
	 * Assign the selected units to a group.
	 * @param groupNo the group number
	 */
	void assignGroup(int groupNo) {
		List<SpacewarStructure> sel = getSelection();
		// player's objects only
		for (SpacewarStructure s : sel) {
			if (!canControl(s)) {
				return;
			}
		}
		// remove previous grouping
		Iterator<Map.Entry<SpacewarStructure, Integer>> it = groups.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<SpacewarStructure, Integer> e = it.next();
			if (e.getValue() == groupNo) {
				it.remove();
			}
		}
		for (SpacewarStructure s : sel) {
			groups.put(s, groupNo);
		}
	}
	/**
	 * Reselect the units of the saved group.
	 * @param groupNo the group number
	 */
	void recallGroup(int groupNo) {
		for (SpacewarStructure s : structures) {
			Integer g = groups.get(s);
			s.selected = g != null && g == groupNo;
		}
		enableSelectedFleetControls();
	}
	@Override
	public void attack(SpacewarStructure s, SpacewarStructure target, Mode mode) {
		if (s.owner != target.owner) {
			s.guard = false;
			s.moveTo = null;
			if (mode == Mode.BEAM) {
				s.attack = target;
			} else 
			if (mode == Mode.KAMIKAZE) {
				SpacebattleStatistics sbs = new SpacebattleStatistics();
				setPortStatistics(sbs, s.ports, s.count, s.owner);
				s.kamikaze = s.count * kamikazeDamage(s.techId, s.owner.id);
				s.selected = false;
			} else {
				s.attack = null;
				RocketSelected r = new RocketSelected();
				r.findRocketPort(target, true, s);
				if (r.fired == null) {
					r.findRocketPort(target, false, s);
				}
				if (r.fired == s) {
					fireRocketAt(target, r);
				}
				if (s.selected && s.owner == player()) {
					enableSelectedFleetControls();
				}
			}
		}
	}
	@Override
	public void move(SpacewarStructure s, double x, double y) {
		s.attack = null;
		s.guard = false;
		s.moveTo = new Point2D.Double(x, y);
		if (s.selected && s.owner == player()) {
			enableSelectedFleetControls();
		}
	}
	@Override
	public void guard(SpacewarStructure s) {
		s.attack = null;
		s.guard = true;
		s.moveTo = null;
		if (s.selected && s.owner == player()) {
			enableSelectedFleetControls();
		}
	}
	@Override
	public void stop(SpacewarStructure s) {
		s.moveTo = null;
		s.attack = null;
		s.guard = false;
		if (s.selected && s.owner == player()) {
			enableSelectedFleetControls();
		}
	}
	@Override
	public Dimension space() {
		return space.getSize();
	}
	@Override
	public void includeFleet(Fleet f, Player side) {
		// remove fleet structures of the same owner as f
		for (int i = structures.size() - 1; i >= 0; i--) {
			SpacewarStructure s = structures.get(i);
			if (s.owner == side && s.fleet != null) {
				structures.remove(i);
			}
		}
		
		Map<InventoryItem, HasInventory> common = inventoryWithParent(f);
		
		if (f.owner == battle.attacker.owner) {
			common.putAll(inventoryWithParent(battle.attacker));
		}
		Fleet nearbyFleet = battle.getFleet();
		if (nearbyFleet != null && nearbyFleet.owner == f.owner) {
			common.putAll(inventoryWithParent(nearbyFleet));
		}
		
		Planet nearbyPlanet = battle.getPlanet();
		
		if (nearbyPlanet == null || nearbyPlanet.owner != f.owner) {
			if (f.owner == battle.attacker.owner) {
				placeFleet(0, false, common);
			} else {
				placeFleet(maxRightPlacement, true, common);
			}
		} else {
			if (f.owner == battle.attacker.owner) {
				placeFleet(maxRightPlacement, true, common);
			} else {
				placeFleet(0, false, common);
			}
		}
	}
	/**
	 * Print the next character.
	 */
	void doChatStep() {
		if (leftChatPanel.visible() || rightChatPanel.visible()) {
			boolean b1 = leftChatPanel.nextChar();
			boolean b2 = rightChatPanel.nextChar();
			
			if (b1 || b2) {
				if (!node.enemy) {
					if (node.transitions.size() == 1) {
						Node n2 = chat.get(node.transitions.get(0));
						leftChatPanel.addLine(n2.enemy ? TextRenderer.YELLOW : TextRenderer.GREEN, get(n2.message));
						rightChatPanel.addLine(n2.enemy ? TextRenderer.YELLOW : TextRenderer.GREEN, get(n2.message));
						node = n2;
					}
				} else {
					leftChatPanel.options.clear();
					rightChatPanel.options.clear();
					for (String n2t : node.transitions) {
						Node n2 = chat.get(n2t);
						
						leftChatPanel.options.add(n2);
						rightChatPanel.options.add(n2);
					}
					if (node.retreat) {
						if (node != chat.getFlee()) {
							achievement("achievement.do_you_chat");
						}
						battle.enemyFlee = true;
						for (SpacewarStructure sws : structures(nonPlayer())) {
							flee(sws);
						}
					}
				}
				commons.control().moveMouse();
			}
		}
	}
	/**
	 * Grant an achievement with the given ID if not already awarded.
	 * @param a the achievement id, e.g., "achievement.i_robot"
	 */
	protected void achievement(String a) {
		if (!world().env.profile().hasAchievement(a)) {
			world().env.achievementQueue().add(a);
			world().env.profile().grantAchievement(a);
		}
	}
	/** 
	 * Action to select an option.
	 * @param n the node 
	 */
	void doSelectOption(Node n) {
		leftChatPanel.options.clear();
		rightChatPanel.options.clear();
		
		leftChatPanel.addLine(n.enemy ? TextRenderer.YELLOW : TextRenderer.GREEN, get(n.message));
		rightChatPanel.addLine(n.enemy ? TextRenderer.YELLOW : TextRenderer.GREEN, get(n.message));
		
		node = n;
		
		world().scripting.onSpaceChat(this, chat, n);
	}
	/** The chat information panel. */
	class ChatPanel extends UIContainer {
		/** Indication that chat is available. */
		boolean hasChat;
		/** The nochat label. */
		UILabel nochat;
		/** The color+line that printed. */
		final List<Pair<Integer, String>> lines = new ArrayList<>();
		/** The current text or -1 if show all. */
		int currentIndex = 0;
		/** The available options. */
		final List<Node> options = new ArrayList<>();
		/** Highlight option. */
		int highlight = -1;
		/** Row height. */
		static final int ROW_HEIGHT = 16;
		/** Initialize. */
		public ChatPanel() {
			width = 286;
			height = 195;
			
			nochat = new UILabel("", 14, commons.text());
			nochat.width = 286;
			nochat.y = 20;
			nochat.wrap(true);
			nochat.horizontally(HorizontalAlignment.CENTER);
			nochat.text(get("chat.unavailable"));
			
			addThis();
		}
		@Override
		public void draw(Graphics2D g2) {
			Shape save0 = g2.getClip();
			g2.clipRect(0, 0, width, height);
			if (hasChat) {
				nochat.visible(true);
				nochat.height = nochat.getWrappedHeight();
				nochat.color(TextRenderer.YELLOW);
			} else {
				nochat.visible(false);
			}
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, width, height);
			
			int y = height - 1;
			for (int i = lines.size() - 1; i >= 0; i--) {
				Pair<Integer, String> ln = lines.get(i);
				String lns = ln.second;
				if (i == lines.size() - 1 && currentIndex >= 0) {
					lns = lns.substring(0, currentIndex);
				}
				List<String> lout = new ArrayList<>();
				commons.text().wrapText(lns, width - 4, 7, lout);
				
				y -= 9;
				for (int j = lout.size() - 1; j >= 0; j--) {
					String s0 = lout.get(j);
					commons.text().paintTo(g2, 2, y, 7, ln.first, s0);
					
					int s0w = commons.text().getTextWidth(7, s0);
					
					if (s0w + 7 < width 
							&& i == lines.size() - 1 
							&& j == lout.size() - 1) {
						g2.setColor(new Color(ln.first));
						g2.fillRect(s0w + 1, y + 0, 7, 7);
					}
					
					y -= 9;
				}
			}

			if (options.size() > 0) {
				// compute option height
				int oh = 0;
				List<List<String>> ols = new ArrayList<>();
				for (Node nso : options) {
					String so = get(nso.getOption());
					List<String> lout = new ArrayList<>();
					commons.text().wrapText(so, width - 4, 7, lout);
					oh += lout.size() * ROW_HEIGHT;
					ols.add(lout);
				}
				
				// black out existing text
				g2.setColor(Color.BLACK);
				g2.fillRect(0, 0, width, oh + 3);
				g2.setColor(new Color(0x4F6FB7));
				g2.drawLine(0, oh, width, oh);
				g2.drawLine(0, oh + 1, width, oh + 1);

				int oy = 1;
				int oi = 0;
				for (List<String> s : ols) {
					int c0 = highlight == oi ? TextRenderer.YELLOW : TextRenderer.GREEN;
					for (String s1 : s) {
						commons.text().paintTo(g2, 2, oy + (ROW_HEIGHT - 7) / 2, 7, c0, s1);
						oy += ROW_HEIGHT;
					}
					oi++;
				}
			}
			
			super.draw(g2);
			g2.setClip(save0);
		}
		@Override
		public boolean mouse(UIMouse e) {
			switch (e.type) {
			case LEAVE:
				highlight = -1;
				return true;
			case ENTER:
			case MOVE:
			case DRAG:
				highlight = getSelectedIndex(e.y);
				return true;
			case DOWN:
				highlight = getSelectedIndex(e.y);
				if (highlight >= 0) {
					player().statistics.chats.value++;
					doSelectOption(options.get(highlight));
					return true;
				}
				// skip lines
				if (lines.size() > 0) {
					int ci = lines.get(lines.size() - 1).second.length() - 1;
					if (currentIndex < ci) {
						currentIndex = ci;
					}
				}
				break;
			default:
			}
			return super.mouse(e);
		}
		/**
		 * Compute the selected text row.
		 * @param y the mouse Y
		 * @return the index or -1 if none
		 */
		public int getSelectedIndex(int y) {
			int oh = 0;
			int i = 0;
			for (Node nso : options) {
				String so = get(nso.getOption());
				List<String> lout = new ArrayList<>();
				commons.text().wrapText(so, width - 4, 7, lout);
				int oh0 = oh;
				oh += lout.size() * ROW_HEIGHT;
				if (y >= oh0 && y < oh) {
					return i;
				}
				i++;
			}
			return -1;
		}
		/**
		 * Render the next character.
		 * @return true if no more characters in the current line.
		 */
		public boolean nextChar() {
			if (lines.size() > 0) {
				Pair<Integer, String> ln = lines.get(lines.size() - 1);
				int ci1 = currentIndex + 1;
				int ci2 = Math.min(ln.second.length(), ci1);
				if (ci2 != currentIndex) {
					currentIndex = ci2;
					this.askRepaint();
				}
				if (ci1 == ln.second.length()) {
					return true;
				}
			}
			return false;
		}
		/** Clear the contents. */
		@Override
		public void clear() {
			hasChat = false;
			currentIndex = 0;
			lines.clear();
			options.clear();
		}
		/**
		 * Add a new line and start printing it.
		 * @param color the color
		 * @param text the text
		 */
		public void addLine(int color, String text) {
			lines.add(Pair.of(color, text));
			currentIndex = 0;
		}
	}
	/**
	 * @param mx the mouse X
	 * @param my the mouse Y 
	 * @param category select ships in the same category?
	 * @return true if ships were selected
	 */
	boolean doSelectType(int mx, int my, boolean category) {
		selectionStart = new Point(mx, my);
		selectionEnd = selectionStart;
		
		doSelectStructures();
		
		List<SpacewarStructure> selection = getSelection();
		for (SpacewarStructure s2 : structures) {
			s2.selected = false;
		}
		for (SpacewarStructure s : selection) {
			for (SpacewarStructure s2 : structures) {
				if (s2.owner == s.owner) {
					if (category) {
						s2.selected |= s.category == s2.category;
					} else {
						s2.selected |= s2.techId.equals(s.techId);
					}
				}
			}
		}
		
		return true;
	}
	/**
	 * Number of attackers on the same target.
	 * @param target the target
	 * @return the number of attackers
	 */
	public int attackerCount(SpacewarStructure target) {
		int n = 0;
		for (SpacewarStructure s : structures) {
			if (s.attack == target) {
				n++;
			}
		}
		return n;
	}
	/**
	 * Computes damage considering compensation factors.
	 * @param baseDamage the base damage
	 * @param target the target
	 * @return the new damage
	 */
	public double attackDamage(double baseDamage, SpacewarStructure target) {
		if (config.spacewarDiminishingAttach) {
 			int n = attackerCount(target);
			
			int c = config.spacewarDiminishingAttachCount;
			if (n > 0 && c > 1) {
				double mult;
//				if (n <= c) {
//					mult = 1 - 0.75 * (n - 1) / (c - 1);
//				} else {
//					mult = Math.pow(n, 0.65);
//				}
				mult = Math.pow(n, -3d / c);
				return baseDamage * mult;
			}
		}
		return baseDamage;
	}
	@Override
	public Difficulty difficulty() {
		return world().difficulty;
	}
	@Override
	public boolean isFleeing(SpacewarStructure s) {
		return s.moveTo != null && (s.moveTo.x < -100 || s.moveTo.x > 100 + space().width); 
	}
}
