/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.core.Location;
import hu.openig.core.SimulationSpeed;
import hu.openig.mechanics.BattleSimulator;
import hu.openig.model.BattleGroundProjector;
import hu.openig.model.BattleGroundShield;
import hu.openig.model.BattleInfo;
import hu.openig.model.BattleProjectile;
import hu.openig.model.BattleProjectile.Mode;
import hu.openig.model.BattleSpaceEntity;
import hu.openig.model.BattleSpaceLayout;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.FleetStatistics;
import hu.openig.model.FleetTask;
import hu.openig.model.HasInventory;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.Owned;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.Screens;
import hu.openig.model.SelectionBoxMode;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarExplosion;
import hu.openig.model.SpacewarProjectile;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarStructure.StructureType;
import hu.openig.model.SpacewarWeaponPort;
import hu.openig.model.SpacewarWorld;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.EquipmentConfigure;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.JavaUtils;

import java.awt.Color;
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
	/** A three phase button. */
	class ThreePhaseButton {
		/** The X coordinate. */
		int x;
		/** The Y coordinate. */
		int y;
		/** The three phases: normal, selected, selected and pressed. */
		BufferedImage[] phases;
		/** Selected state. */
		boolean selected;
		/** Pressed state. */
		boolean pressed;
		/** The action to perform on the press. */
		Action0 action;
		/** Is the button disabled? */
		boolean enabled = true;
		/**
		 * Constructor.
		 * @param phases the phases
		 */
		public ThreePhaseButton(BufferedImage[] phases) {
			this.phases = phases;
		}
		/**
		 * Constructor.
		 * @param x the x coordinate
		 * @param y the y coordinat
		 * @param phases the phases
		 */
		public ThreePhaseButton(int x, int y, BufferedImage[] phases) {
			this.x = x;
			this.y = y;
			this.phases = phases;
		}
		/** 
		 * Render the button.
		 * @param g2 the graphics object
		 */
		public void paintTo(Graphics2D g2) {
			if (!enabled) {
				g2.drawImage(phases[0], x, y, null);
				RenderTools.fill(g2, x, y, phases[0].getWidth(), phases[0].getHeight(), commons.common().disabledPattern);
			} else
			if (pressed) {
				g2.drawImage(phases[1], x, y, null);
			} else
			if (selected) {
				g2.drawImage(phases[2], x, y, null);
			} else {
				g2.drawImage(phases[0], x, y, null);
			}
		}
		/**
		 * Test if the mouse is within this button.
		 * @param mx the mouse X coordinate
		 * @param my the mouse Y coordinate
		 * @return true if within the button
		 */
		public boolean test(int mx, int my) {
			return enabled && visible && mx >= x && my >= y && mx < x + phases[0].getWidth() && my < y + phases[0].getHeight();
		}
		/** Invoke the associated action if present. */
		public void invoke() {
			if (action != null) {
				action.invoke();
			}
		}
	}
	/** A two phase toggle button. */
	class TwoPhaseButton {
		/** The X coordinate. */
		int x;
		/** The Y coordinate. */
		int y;
		/** The pressed state. */
		boolean pressed;
		/** The phases. */
		BufferedImage[] phases;
		/** The action to perform on the press. */
		Action0 onPress;
		/** The action to perform on release. */
		Action0 onRelease;
		/** Is this button visible. */
		boolean visible;
		/** Is the button enabled? */
		boolean enabled = true;
		/**
		 * Constructor.
		 * @param x the X coordinate
		 * @param y the Y coordinate
		 * @param phases the two phases
		 */
		public TwoPhaseButton(int x, int y, BufferedImage[] phases) {
			this.x = x;
			this.y = y;
			this.phases = phases;
		}
		/**
		 * Paint the button.
		 * @param g2 the graphics
		 */
		public void paintTo(Graphics2D g2) {
			if (visible) {
				if (!enabled) {
					g2.drawImage(phases[0], x, y, null);
					RenderTools.fill(g2, x, y, phases[0].getWidth(), phases[0].getHeight(), commons.common().disabledPattern);
				} else
				if (pressed) {
					g2.drawImage(phases[1], x, y, null);
				} else {
					g2.drawImage(phases[0], x, y, null);
				}
			}
		}
		/**
		 * Test if the mouse is within this button.
		 * @param mx the mouse X coordinate
		 * @param my the mouse Y coordinate
		 * @return true if within the button
		 */
		public boolean test(int mx, int my) {
			return enabled && visible && mx >= x && my >= y && mx < x + phases[0].getWidth() && my < y + phases[0].getHeight();
		}
		/**
		 * Invoke the onPress action.
		 */
		public void pressed() {
			if (onPress != null) {
				onPress.invoke();
			}
		}
		/** Invoke the onRelease action. */
		public void released() {
			if (onRelease != null) {
				onRelease.invoke();
			}
		}
	}
	/** Animated toggle button. */
	class AnimatedRadioButton extends UIComponent {
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
	final List<SpacewarStructure> structures = new ArrayList<SpacewarStructure>();
	/** The projectiles for animation. */
	final List<SpacewarProjectile> projectiles = new ArrayList<SpacewarProjectile>();
	/** The space explosions for animation. */
	final List<SpacewarExplosion> explosions = new ArrayList<SpacewarExplosion>();
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
	final double maxScale = 1.0;
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
	List<AnimatedRadioButton> animatedButtonsLeft = new ArrayList<AnimatedRadioButton>();
	/** The list of animation buttons. */
	List<AnimatedRadioButton> animatedButtonsRight = new ArrayList<AnimatedRadioButton>();
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
	/** The simulation delay on normal speed. */
	static final int SIMULATION_DELAY = 100;
	/** Keep the last info images. */
	static final int IMAGE_CACHE_SIZE = 8;
	/** Indicates if the attacker is placed on the right side. */
	boolean attackerOnRight;
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
	@Override
	public void onInitialize() {
		mainCommands = new ArrayList<ThreePhaseButton>();
		
		stopButton = new ThreePhaseButton(33, 24, commons.spacewar().stop);
		stopButton.action = new Action0() {
			@Override
			public void invoke() {
				doStopSelectedShips();
				stopButton.selected = false;
			}
		};
		moveButton = new ThreePhaseButton(33 + 72, 24, commons.spacewar().move);
		kamikazeButton = new ThreePhaseButton(33, 24 + 35, commons.spacewar().kamikaze);
		attackButton = new ThreePhaseButton(33 + 72, 24 + 35, commons.spacewar().attack);
		guardButton = new ThreePhaseButton(33, 24 + 35 * 2, commons.spacewar().guard);
		guardButton.action = new Action0() {
			@Override
			public void invoke() {
				doSelectionGuard();
			}
		};
		
		rocketButton = new ThreePhaseButton(33 + 72, 24 + 35 * 2, commons.spacewar().rocket);
		
		mainCommands.add(stopButton);
		mainCommands.add(moveButton);
		mainCommands.add(kamikazeButton);
		mainCommands.add(attackButton);
		mainCommands.add(guardButton);
		mainCommands.add(rocketButton);
		
		viewCommands = new ArrayList<ThreePhaseButton>();
		
		viewCommand = new ThreePhaseButton(33, 24 + 35 * 3, commons.spacewar().command);
		viewDamage = new ThreePhaseButton(33 + 72, 24 + 35 * 3, commons.spacewar().damage);
		viewRange = new ThreePhaseButton(33, 24 + 35 * 3 + 30, commons.spacewar().fireRange);
		viewGrid = new ThreePhaseButton(33 + 72, 24 + 35 * 3 + 30, commons.spacewar().grid);
		
		viewCommands.add(viewCommand);
		viewCommands.add(viewDamage);
		viewCommands.add(viewRange);
		viewCommands.add(viewGrid);
		
		zoom = new TwoPhaseButton(3, 24, commons.spacewar().zoom);
		zoom.visible = true;
		pause = new TwoPhaseButton(4, 19 + 170, commons.spacewar().pause);
		pause.visible = true;
		retreat = new TwoPhaseButton(33, 19 + 170, commons.spacewar().retreat);
		retreat.visible = true;
		confirmRetreat = new TwoPhaseButton(33, 19 + 170, commons.spacewar().sure);
		stopRetreat = new TwoPhaseButton(33, 19 + 170, commons.spacewar().stopTall);
		
		
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
		
		addThis();
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
					ex.printStackTrace();
				}
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
			if (e.has(Button.MIDDLE) && mainmap.contains(e.x, e.y)) {
				zoomToFit();
				needRepaint = true;
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
			if (s.owner != player() && s.contains(x, y)) {
				return s;
			}
		}
		return null;
	}
	/**
	 * @return Returns a list of the currently selected structures.
	 */
	List<SpacewarStructure> getSelection() {
		List<SpacewarStructure> result = JavaUtils.newArrayList();
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
		List<SpacewarStructure> candidates = new ArrayList<SpacewarStructure>();
		List<SpacewarStructure> currentSelection = getSelection();
		boolean own = false;
		if (selectionMode == SelectionBoxMode.ADD) {
			candidates.addAll(currentSelection);
		}
		if (selectionMode != SelectionBoxMode.NEW) {
			for (SpacewarStructure s : currentSelection) {
				own |= s.owner == player();
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
				if (s.owner == player()) {
					s.selected = true;
				} else {
					s.selected = false;
				}
			}
		} else {
			for (SpacewarStructure s : candidates) {
				s.selected = true;
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
				own |= s.owner == player();
				candidates.add(s);
			}
		}
		return own;
	}
	/** Zoom in/out to fit the available main map space. */
	void zoomToFit() {
		double xscale = mainmap.width * 1.0 / space.width;
		double yscale = mainmap.height * 1.0 / space.height;
		double s = Math.min(xscale, yscale) * 20;
		scale = Math.min(maxScale, Math.round(s) / 20.0);
		pan(0, 0);
	}

	@Override
	public void onEnter(Screens mode) {
		buttonTimer = commons.register(100, new Action0() {
			@Override
			public void invoke() {
				doButtonAnimations();
			}
		});
		selectButton(leftShipStatus, true);
		selectButton(rightShipStatus, false);
		selectionBox = false;
		retreat.visible = true;
		confirmRetreat.visible = false;
		stopRetreat.visible = false;
		displaySelectedShipInfo();
	}

	@Override
	public void onLeave() {
		close0(buttonTimer);
		buttonTimer = null;
		
		// cleanup
		
		battle = null;
		structures.clear();
		projectiles.clear();
		explosions.clear();
		
		leftStatusPanel.clear();
		rightStatusPanel.clear();
		
		leftShipInfoPanel.clear();
		rightShipInfoPanel.clear();
		layoutPanel.selected = null;
		
		infoImages.clear();
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

		for (Field f : getClass().getDeclaredFields()) {
			Show a = f.getAnnotation(Show.class);
			if (a != null) {
				if (a.left()) {
					try {
						UIComponent.class.cast(f.get(this)).location(leftPanel.x, leftPanel.y);
					} catch (IllegalAccessException ex) {
						ex.printStackTrace();
					}
				} else {
					try {
						UIComponent.class.cast(f.get(this)).location(rightPanel.x, rightPanel.y);
					} catch (IllegalAccessException ex) {
						ex.printStackTrace();
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
			displayPanel(PanelMode.SHIP_STATUS, false);
			enableFleetControls(true);
			retreat.enabled = true;
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
		scale = Math.min(scale + 0.05, maxScale);
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
		scale = Math.max(scale - 0.05, 0.45);
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
			if (s.owner == player()) {
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
			if (s.owner == player() && !s.intersects(0, 0, space.width, space.height)) {
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
		// TODO Auto-generated method stub
		
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
		
		this.battle = battle;
		BattleSimulator.findHelpers(battle, world());
		
		int xmax = space.width;

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

//			int defenseWidth = Math.max(maxWidth(shields()), maxWidth(projectors()));
			centerStructures(space.width - commons.spacewar().planet.getWidth() / 2, 
					JavaUtils.concat(shields(), projectors()));
			xmax -= 3 * commons.spacewar().planet.getWidth() / 2;
			
			// place and align stations
			placeStations(nearbyPlanet, alien);
			int stationWidth = maxWidth(stations());
			centerStructures(xmax - stationWidth / 4, stations());
			xmax -= 3 * stationWidth / 2;
			
			
			// add fighters of the planet
			List<SpacewarStructure> shipWall = JavaUtils.newArrayList();
			createSpacewarStructures(nearbyPlanet, 
					EnumSet.of(ResearchSubCategory.SPACESHIPS_FIGHTERS), shipWall);
			
			if (!shipWall.isEmpty()) {
				int maxw = createSingleRowBatchWall(xmax, true, shipWall, structures);
				xmax -= 3 * maxw / 2;
			}
		} else {
			planetVisible = false;
		}
		
		// FIXME for now, place fleets via the wall formation
		
		if (nearbyPlanet != null && nearbyPlanet.owner == battle.attacker.owner) {
			// place the attacker on the right side (planet side)
			
			attackerOnRight = true;
			
			placeFleet(xmax, true, battle.attacker);
			// place the defender on the left side
			
			if (nearbyFleet != null) {
				placeFleet(0, false, nearbyFleet);
			}
		} else {
			// place attacker on the left side
			placeFleet(0, false, battle.attacker);
			
			attackerOnRight = false;
			
			// place the defender on the planet side (right side)
			if (nearbyFleet != null) {
				placeFleet(xmax, true, nearbyFleet);
			}
		}
		
		zoomToFit();
		commons.playBattleMusic();

		setSpacewarTimeControls();

		player().ai.spaceBattleInit(this);
		nonPlayer().ai.spaceBattleInit(this);
		
		displayPanel(PanelMode.SHIP_STATUS, true);
		if (battle.attacker.owner == player() && (nearbyPlanet == null || nearbyPlanet.owner != player())) {
			displayPanel(PanelMode.LAYOUT, false);
			setLayoutSelectionMode(true);
			enableFleetControls(false);
		} else {
			setLayoutSelectionMode(false);
			commons.simulation.resume();
			enableFleetControls(true);
		}
		retreat.enabled = false;
		
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
		List<SpacewarStructure> result = JavaUtils.newArrayList();
		for (SpacewarStructure s : structures) {
			if (s.type == StructureType.SHIELD) {
				result.add(s);
			}
		}
		return result;
	}
	/** @return a list of projector structures. */
	List<SpacewarStructure> projectors() {
		List<SpacewarStructure> result = JavaUtils.newArrayList();
		for (SpacewarStructure s : structures) {
			if (s.type == StructureType.PROJECTOR) {
				result.add(s);
			}
		}
		return result;
	}
	/** @return a list of ship structures. */
	List<SpacewarStructure> ships() {
		List<SpacewarStructure> result = JavaUtils.newArrayList();
		for (SpacewarStructure s : structures) {
			if (s.type == StructureType.SHIP) {
				result.add(s);
			}
		}
		return result;
	}
	/** @return a list of station structures. */
	List<SpacewarStructure> stations() {
		List<SpacewarStructure> result = JavaUtils.newArrayList();
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
	 * @param fleet the fleet to place
	 */
	void placeFleet(int x, boolean left, Fleet fleet) {
		List<SpacewarStructure> largeShipWall = JavaUtils.newArrayList();
		// place attacker on the planet side (right side)
		createSpacewarStructures(fleet, 
				EnumSet.of(ResearchSubCategory.SPACESHIPS_BATTLESHIPS, ResearchSubCategory.SPACESHIPS_CRUISERS), largeShipWall);
		
		if (!largeShipWall.isEmpty()) {
			int maxw = createMultiRowWall(x, left, largeShipWall, structures);
			x = left ? (x - maxw) : (x + maxw);
		}
		
		List<SpacewarStructure> smallShipWall = JavaUtils.newArrayList();
		List<SpacewarStructure> smallShipWallOut = JavaUtils.newArrayList();
		
		createSpacewarStructures(fleet, 
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
		
		for (InventoryItem ii : nearbyPlanet.inventory) {
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS 
					&& ii.owner == nearbyPlanet.owner) {
				
				BattleSpaceEntity bse = world().battle.spaceEntities.get(ii.type.id);
				
				SpacewarStructure st = new SpacewarStructure();
				st.type = StructureType.STATION;
				st.item = ii;
				st.owner = nearbyPlanet.owner;
				st.destruction = bse.destruction;
				st.angles = new BufferedImage[] { alien ? bse.alternative[0] : bse.normal[0] };
				st.infoImageName = bse.infoImageName;
				st.shield = ii.shield;
				st.shieldMax = Math.max(0, ii.shieldMax());
				st.hp = ii.hp;
				st.hpMax = world().getHitpoints(ii.type);
				st.value = ii.type.productionCost;
				st.planet = nearbyPlanet;
				
				st.ecmLevel = setWeaponPorts(ii, st.ports);
				
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
		for (Building b : nearbyPlanet.surface.buildings) {
			float eff = b.getEfficiency();
			if (Building.isOperational(eff) && b.type.kind.equals("Shield")) {
				BattleGroundShield bge = world().battle.groundShields.get(b.type.id);
				
				SpacewarStructure st = new SpacewarStructure();
				st.owner = nearbyPlanet.owner;
				st.type = StructureType.SHIELD;
				st.angles = new BufferedImage[] { alien ? bge.alternative : bge.normal };
				st.infoImageName = bge.infoImageName;
				st.hpMax = world().getHitpoints(b.type, nearbyPlanet.owner, true);
				st.hp = b.hitpoints * st.hpMax / b.type.hitpoints;
				st.value = b.type.cost;
				st.destruction = bge.destruction;
				st.building = b;
				st.planet = nearbyPlanet;

				shieldValue = Math.max(shieldValue, eff * bge.shields);

				structures.add(st);
			}
		}
		for (SpacewarStructure sws : shields()) {
			sws.shield = (int)(sws.hp * shieldValue / 100);
			sws.shieldMax = (int)(sws.hpMax * shieldValue / 100);
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
		for (Building b : nearbyPlanet.surface.buildings) {
			float eff = b.getEfficiency();
			if (Building.isOperational(eff) && b.type.kind.equals("Gun")) {
				BattleGroundProjector bge = world().battle.groundProjectors.get(b.type.id);

				SpacewarStructure st = new SpacewarStructure();
				st.owner = nearbyPlanet.owner;
				st.type = StructureType.PROJECTOR;
				st.angles = alien ? bge.alternative : bge.normal;
				st.angle = Math.PI;
				st.infoImageName = bge.infoImageName;
				st.hpMax = world().getHitpoints(b.type, nearbyPlanet.owner, true);
				st.value = b.type.cost;
				st.hp = b.hitpoints * st.hpMax / b.type.hitpoints;
				st.destruction = bge.destruction;
				st.building = b;
				st.planet = nearbyPlanet;
				
				st.shield = (int)(st.hp * shieldValue / 100);
				st.shieldMax = (int)(st.hpMax * shieldValue / 100);
				
				st.rotationTime = bge.rotationTime;

				BattleProjectile pr = world().battle.projectiles.get(bge.projectile);
				
				SpacewarWeaponPort wp = new SpacewarWeaponPort(null);
				wp.projectile = pr.copy();
				wp.projectile.damage = bge.damage;
				
				
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
		
		LinkedList<SpacewarStructure> ships = new LinkedList<SpacewarStructure>(items);
		LinkedList<SpacewarStructure> group = new LinkedList<SpacewarStructure>();

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
		if (alloc < sum) {
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
		
		List<List<SpacewarStructure>> rows = new ArrayList<List<SpacewarStructure>>();
		int rowIndex = -1;
		int y = 0;
		List<SpacewarStructure> currentRow = null;
		
		// put ships into rows
		for (SpacewarStructure sws : ships) {
			if (y + sws.get().getHeight() >= space.height || rowIndex < 0) {
				rowIndex++;
				currentRow = new ArrayList<SpacewarStructure>();
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
	/**
	 * Create the spacewar ships from the given inventory list and category filters.
	 * @param <T> the inventory and owner
	 * @param inventory the inventory provider
	 * @param categories the categories to use
	 * @param ships the output of ships
	 */
	<T extends Owned & HasInventory> void createSpacewarStructures(
			T inventory,
			EnumSet<ResearchSubCategory> categories,
			Collection<? super SpacewarStructure> ships
			) {
		for (InventoryItem ii : inventory.inventory()) {
			// Fix for zero inventory entries
			if (ii.count <= 0) {
				continue;
			}
			if (categories.contains(ii.type.category) && ii.owner == inventory.owner()) {
				BattleSpaceEntity bse = world().battle.spaceEntities.get(ii.type.id);
				if (bse == null) {
					System.err.println("Missing space entity: " + ii.type.id);
				}
				
				SpacewarStructure st = new SpacewarStructure();

				if (inventory instanceof Planet) {
					st.planet = (Planet)inventory;
				} else {
					st.fleet = (Fleet)inventory;
				}
				
				st.type = StructureType.SHIP;
				st.item = ii;
				st.owner = inventory.owner();
				st.destruction = bse.destruction;
				st.angles = inventory.owner() != player() ? bse.alternative : bse.normal;
				st.infoImageName = bse.infoImageName;
				st.shield = ii.shield;
				st.shieldMax = Math.max(0, ii.shieldMax());
				st.hp = ii.hp;
				st.hpMax = world().getHitpoints(ii.type);
				st.value = totalValue(ii);
				st.count = ii.count;
				st.rotationTime = bse.rotationTime;
				st.movementSpeed = bse.movementSpeed;
				
				st.ecmLevel = setWeaponPorts(ii, st.ports);
				st.computeMinimumRange();
				
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
		for (InventorySlot is : ii.slots) {
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
		for (InventorySlot is : ii.slots) {
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
			if (is.type != null && is.type.has("ecm")) {
				ecmLevel = Math.max(ecmLevel, is.type.getInt("ecm"));
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
		// TODO space battle surface
		
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
			g2.drawImage(commons.spacewar().planet, space.width - commons.spacewar().planet.getWidth(), 0, null);
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
			int x = (int)(e.x * minimap.width / space.width);
			int y = (int)(e.y * minimap.height / space.height);
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
	/** @return calculates the minimap viewport rectangle coordinates. */
	Rectangle computeMinimapViewport() {
		int vx = 0;
		int vy = 0;
		int vx2 = minimap.width - 1;
		int vy2 = minimap.height - 1;
		if (space.width * scale >= mainmap.width) {
			vx = (int)(offsetX * minimap.width / space.width / scale + 0.5);
			vx2 = (int)((offsetX + mainmap.width - 1) * minimap.width / space.width / scale + 0.5);
		}
		if (space.height * scale >= mainmap.height) {
			vy = (int)(offsetY * minimap.height / space.height / scale + 0.5);
			vy2 = (int)((offsetY + mainmap.height - 1) * minimap.height / space.height / scale + 0.5);
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
			if (viewDamage.selected) {
				int y = (int)e.y - h2 + 2;
				int dw = w - 6;
				g2.setColor(Color.BLACK);
				g2.fillRect((int)e.x - w2 + 3, y, dw, 4);
				g2.setColor(Color.GREEN);
				g2.fillRect((int)e.x - w2 + 3, y, e.hp * dw / e.hpMax, 4);
				g2.setColor(Color.RED);
				g2.drawRect((int)e.x - w2 + 3, y, dw, 4);
				if (e.shieldMax > 0) {
					g2.setColor(new Color(0xFFFFCC00));
					g2.fillRect((int)e.x - w2 + 3, y, e.shield * dw / e.shieldMax, 4);
				}
			}
			if (e.type == StructureType.SHIP && e.count > 1) {
				commons.text().paintTo(g2, (int)(e.x - w2), (int)(e.y + h / 2 - 8), 7, 0xFFFFFFFF, Integer.toString(e.count));
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
			if (e.owner == player()) {
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
		g2.drawImage(img, (int)(x - img.getWidth() / 2), (int)(y - img.getHeight() / 2), null);
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
				s.y = y + s.get().getHeight() / 2;
				
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
				for (InventorySlot es : item.slots) {
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
				item = null;
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
				for (InventorySlot is : item.slots) {
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
					damage.text(format("spacewar.ship_weapon_damage", 
							100 * (world().getHitpoints(is.type) - is.hp) 
							/ world().getHitpoints(is.type)), true);
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
	class SpacebattleStatistics {
		/** The unit count. */
		public int units;
		/** The losses. */
		public int losses;
		/** The firepower. */
		public int firepower;
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
			
			y = drawLine(g2, y, TextRenderer.GREEN, "spacewar.statistics_own_units", own.units);
			y = drawLine(g2, y, TextRenderer.GREEN, "spacewar.statistics_losses", own.losses);
			y = drawLine(g2, y, TextRenderer.GREEN, "spacewar.statistics_firepower", own.firepower);
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

			y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_enemy_units", other.units);
			y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_losses", other.losses);
			y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_firepower", other.firepower);
			y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_ground", other.groundUnits);
			y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_rockets", other.rockets, other.bombs);
			
			if (other.stations > 0 || other.guns > 0) {
				y += 10;
				if (other.stations > 0) {
					y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_stations", other.stations);
				}
				if (other.guns > 0) {
					y = drawLine(g2, y, TextRenderer.YELLOW, "spacewar.statistics_guns", other.guns);
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
		for (SpacewarStructure e : structures) {
			SpacebattleStatistics stat = (e.owner == player()) ? own : other;
			if (e.type == StructureType.PROJECTOR) {
				stat.guns++;
			} else
			if (e.type == StructureType.STATION) {
				stat.stations++;
			} else
			if (e.type == StructureType.SHIP) {
				stat.units++;
			}
			setPortStatistics(stat, e.ports);
		}
		if (battle.attacker.owner == player()) {
			own.losses = battle.attackerLosses;
			own.groundUnits = battle.attackerGroundUnits;
			other.losses = battle.defenderLosses;
			other.groundUnits = battle.defenderGroundUnits;
		} else {
			own.losses = battle.defenderLosses;
			own.groundUnits = battle.defenderLosses;
			other.losses = battle.attackerLosses;
			other.groundUnits = battle.attackerLosses;
		}
	}
	/**
	 * Set the weapon port statistics.
	 * @param stat the output statistics
	 * @param ports the port sequence
	 */
	void setPortStatistics(SpacebattleStatistics stat, Iterable<? extends SpacewarWeaponPort> ports) {
		for (SpacewarWeaponPort p : ports) {
			if (p.projectile.mode == Mode.BEAM) {
				stat.firepower += p.count * p.projectile.damage;
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
		for (InventoryItem ii : f.inventory) {
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
				
				int c = item.owner == player() ? TextRenderer.GREEN : TextRenderer.YELLOW;
				
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
				int firepower = item.getFirepower();
				if (firepower >= 0) {
					maxLabelWidth = Math.max(drawLabel(g2, p, c, get("spacewar.ship_information_firepower")), maxLabelWidth);
				}
				p.y += 10;
				if (isws) {
					maxLabelWidth = Math.max(drawLabel(g2, p, c, get("spacewar.ship_information_equipment")), maxLabelWidth);
					
					for (InventorySlot is : item.item.slots) {
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
					drawLabel(g2, p, c, "  : " + "0"); // wins
					drawLabel(g2, p, c, "  : " + "-"); // crew
				}
				if (firepower >= 0) {
					drawLabel(g2, p, c, "  : " + firepower);
				}
				p.y += 20;
				if (isws) {
					for (InventorySlot is : item.item.slots) {
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
		LinkedList<SpacewarStructure> fighters = JavaUtils.newLinkedList();
		LinkedList<SpacewarStructure> nonFighters = JavaUtils.newLinkedList();
		for (SpacewarStructure sws : ships) {
			if (sws.owner == owner) {
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
		List<SpacewarStructure> placed = new ArrayList<SpacewarStructure>();
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
					displayPanel(PanelMode.SHIP_STATUS, false);
					setLayoutSelectionMode(false);
					enableFleetControls(true);
					retreat.enabled = true;
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
			};
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
				if (selection.get(0).owner == player()) {
					stopButton.enabled = true;
					moveButton.enabled = false;
					attackButton.enabled = true;
					guardButton.enabled = true;
					kamikazeButton.enabled = false;
					rocketButton.enabled = false;
					guardButton.selected = true;
					for (SpacewarStructure sws : selection) {
						if (sws.type == StructureType.SHIP) {
							kamikazeButton.enabled |= (sws.item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS);
							for (SpacewarWeaponPort port : sws.ports) {
								rocketButton.enabled |= port.projectile.mode != Mode.BEAM && port.count > 0; 
							}
							moveButton.enabled = true;
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
		for (SpacewarStructure ship : structures) {
			if (ship.type == StructureType.SHIP && ship.selected && ship.owner == player()) {
				ship.moveTo = p;
				ship.attack = null;
				ship.guard = false;
			}
		}
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
				if (ship.type == StructureType.SHIP && ship.selected && ship.owner != target.owner) {
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
		}
	}
	/**
	 * Fire a single rocket from the group.
	 * @param target the target
	 */
	void doAttackWithRockets(SpacewarStructure target) {
		RocketSelected r = new RocketSelected();
		r.findRocket(target, true);
		if (r.fired == null) {
			r = new RocketSelected();
			r.findRocket(target, false);
		}
		if (r.fired != null) {
			r.port.count--;
			if (r.port.is != null) {
				r.port.is.count--;
			}

			SpacewarProjectile proj = new SpacewarProjectile();
			proj.model = r.port.projectile;
			proj.damage = r.port.projectile.damage;
			proj.owner = r.fired.owner;
			proj.target = target;
			proj.matrix = r.port.projectile.matrix;
			proj.movementSpeed = r.port.projectile.movementSpeed;
			proj.rotationTime = r.port.projectile.rotationTime;
			proj.x = r.fired.x;
			proj.y = r.fired.y;
			proj.angle = r.fired.angle;
			proj.impactSound = SoundType.HIT;
			proj.steering = true;
			proj.ecmLimit = r.type.getInt("anti-ecm", 0);
			
			projectiles.add(proj);
			
			sound(r.port.projectile.sound);
		}
	}
	/**
	 * Set to attack the specified target.
	 * @param target the target structure
	 */
	void doAttackWithShips(SpacewarStructure target) {
		for (SpacewarStructure ship : structures) {
			if (ship.type != StructureType.SHIELD 
					&& ship.selected && ship.owner == player()
					&& !ship.ports.isEmpty()) {
				ship.moveTo = null;
				ship.attack = target;
				ship.guard = false;
			}
		}
	}
	/**
	 * Stop the activity of the selected structures.
	 */
	void doStopSelectedShips() {
		for (SpacewarStructure ship : structures) {
			if (ship.selected && ship.owner == player()) {
				ship.moveTo = null;
				ship.attack = null;
				ship.guard = false;
				guardButton.selected = false;
			}
		}
		// TODO stop command for other structures
	}
	/**
	 * Creates a list of in-range enemy structures.
	 * @param ship the center ship
	 * @return the list of structures
	 */
	@Override
	public List<SpacewarStructure> enemiesInRange(SpacewarStructure ship) {
		List<SpacewarStructure> result = JavaUtils.newArrayList();
		for (SpacewarStructure s : structures) {
			if (s.owner != ship.owner) {
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
			diff = 2 * Math.PI - diff;
		} else
		if (diff > Math.PI) {
			diff -= 2 * Math.PI; 
		}
		double anglePerStep = 2 * Math.PI * ship.rotationTime / ship.angles.length / SIMULATION_DELAY;
		if (Math.abs(diff) < anglePerStep) {
			ship.angle = targetAngle;
			return true;
		} else {
			ship.angle += Math.signum(diff) * anglePerStep;
		}
		return false;
	}
	/**
	 * Rotate the projectile towards the given target angle by a step.
	 * @param proj the structure
	 * @param x the target point X
	 * @param y the target point Y
	 * @return rotation done?
	 */
	boolean rotateStep(SpacewarProjectile proj, double x, double y) {
		double targetAngle = Math.atan2(y - proj.y, x - proj.x);
		double currentAngle = proj.normalizedAngle();

		double diff = targetAngle - currentAngle;
		if (diff < -Math.PI) {
			diff = 2 * Math.PI - diff;
		} else
		if (diff > Math.PI) {
			diff -= 2 * Math.PI; 
		}
		double anglePerStep = 2 * Math.PI * proj.rotationTime / proj.matrix.length / SIMULATION_DELAY;
		if (Math.abs(diff) < anglePerStep) {
			proj.angle = targetAngle;
			return true;
		} else {
			proj.angle += Math.signum(diff) * anglePerStep;
		}
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
			if (obj.steering) {
				// adjust angle to match target
				rotateStep(obj, obj.target.x, obj.target.y);
			}
			double w = obj.target.get().getWidth();
			double h = obj.target.get().getHeight();
			double x0 = obj.target.x - w / 2;
			double x1 = x0 + w;
			double y0 = obj.target.y - h / 2;
			double y1 = y0 + h;
			if (obj.owner.id.equals("Empire")) {
				System.out.print("");
			}
			if (RenderTools.isLineIntersectingRectangle(obj.x, obj.y, obj.x + dx, 
					obj.y + dy, x0, y0, x1, y1)) {
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
			displayPrimary(Screens.STARMAP);
			commons.playRegularMusic();
			return true;
		}
		return super.keyboard(e);
	}
	/** Select all player structures. */
	void doSelectAll() {
		for (SpacewarStructure s : structures) {
			s.selected = s.owner == player();
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
			if (ship.selected && ship.owner == player()) {
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
		SpacewarExplosion x = new SpacewarExplosion();
		x.owner = s.owner;
		if (destroy) {
			x.target = s;
		}
		x.x = s.x;
		x.y = s.y;
		switch (s.destruction) {
		case EXPLOSION_MEDIUM:
		case EXPLOSION_MEDIUM_2:
			x.phases = commons.spacewar().explosionMedium;
			break;
		case EXPLOSION_SHORT:
			x.phases = commons.spacewar().explosionSmall;
			break;
		default:
			x.phases = commons.spacewar().explosionLarge;
		}
		explosions.add(x);
	}
	/**
	 * Adjust the damage based on the target and projectile.
	 * @param p the projectile
	 * @return the target
	 */
	int damageAdjust(SpacewarProjectile p) {
		if (p.model.mode != Mode.BEAM) {
			if (p.model.mode == Mode.BOMB || p.model.mode == Mode.VIRUS) {
				if (p.target.building == null) {
					return p.damage / 5;
				}
			}
			if (p.model.mode == Mode.ROCKET || p.model.mode == Mode.MULTI_ROCKET) {
				if (p.target.building != null) {
					return p.damage / 5;
				}
			}
		}
		return p.damage;
	}
	/** Perform the spacewar simulation. */
	void doSpacewarSimulation() {
		Set<SoundType> soundsToPlay = new HashSet<SoundType>();
		// advance explosions
		for (SpacewarExplosion exp : new ArrayList<SpacewarExplosion>(explosions)) {
			if (exp.next()) {
				explosions.remove(exp);
			} else
			if (exp.isMiddle() && exp.target != null && exp.target.isDestroyed()) {
				structures.remove(exp.target);
			}
		}
		// move projectiles
		for (SpacewarProjectile p : new ArrayList<SpacewarProjectile>(projectiles)) {
			if (moveStep(p)) {
				projectiles.remove(p);
				
				int loss0 = p.target.loss;
				int damage = damageAdjust(p);
				
				if (p.target.damage(damage)) {
					if (p.model.mode == Mode.VIRUS && p.target.building != null) {
						p.target.planet.quarantine |= true;
						p.target.planet.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL; 
					}
					battle.spaceLosses.add(p.target);
					soundsToPlay.add(p.target.destruction);
					createExplosion(p.target, true);
					if (p.target.type == StructureType.SHIELD) {
						dropGroundShields();
					}
					if (p.target.owner == battle.attacker.owner) {
						battle.attackerLosses += p.target.loss - loss0;
					} else {
						battle.defenderLosses += p.target.loss - loss0;
					}
				} else {
					soundsToPlay.add(p.impactSound);
					if (p.steering) {
						createExplosion(p.target, false);
					}
				}
			} else
			if (!p.intersects(0, 0, space.width, space.height)) {
				projectiles.remove(p);
			} else {
				if (p.steering) {
					double d = Math.hypot(p.x - p.target.x, p.y - p.target.y);
					if (p.target.isDestroyed() || (p.target.ecmLevel > p.ecmLimit && d < 100 && d > 80)) {
						// choose a new target
						if (structures.size() > 0) {
							p.target = world().random(structures);
						}
					}
				}
			}
		}
		List<SpacewarStructure> enemyIdles = JavaUtils.newArrayList();
		List<SpacewarStructure> playerIdles = JavaUtils.newArrayList();
		// fleet movements
		for (SpacewarStructure ship : structures) {
			if (!ship.isDestroyed()) {
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
					if (ship.owner == player()) {
						List<SpacewarStructure> es = enemiesInRange(ship);
						if (es.size() > 0) {
							ship.attack = random(es);
						}
					} else {
						enemyIdles.add(ship);
					}
				} else
				if (ship.owner != player()) {
					enemyIdles.add(ship);
				} else {
					playerIdles.add(ship);
				}
			}
		}
		
		SpacewarAction act = SpacewarAction.CONTINUE;
		act = player().ai.spaceBattle(this, playerIdles);
		act = nonPlayer().ai.spaceBattle(this, enemyIdles);
		
		for (SoundType st : soundsToPlay) {
			sound(st);
		}
		Player winner = act == SpacewarAction.SURRENDER ? player() : checkWinner();
		
		if (winner != null && explosions.size() == 0 && projectiles.size() == 0) {
			commons.simulation.pause();
			concludeBattle(winner);
		}
		askRepaint();
	}
	/**
	 * Drop ground shields.
	 */
	void dropGroundShields() {
		for (SpacewarStructure s : structures) {
			if (s.type == StructureType.PROJECTOR) {
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
			if (s.owner == player()) {
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
		if (stopRetreat.visible && playerRetreatedBeyondScreen(player())) {
			return other;
		} else
		if (playerRetreatedBeyondScreen(other)) {
			return player();
		}
		return null;
	}
	/**
	 * Returns a list of structures which are the enemies of {@code s}.
	 * @param s the structure
	 * @return the list of enemies
	 */
	@Override
	public List<SpacewarStructure> enemiesOf(SpacewarStructure s) {
		List<SpacewarStructure> result = JavaUtils.newArrayList();
		for (SpacewarStructure f : structures) {
			if (f.owner != s.owner) {
				result.add(f);
			}
		}
		return result;
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
		SpacewarProjectile sp = new SpacewarProjectile();
		sp.model = p.projectile;
		sp.owner = source.owner; 
		sp.target = target;
		sp.movementSpeed = p.projectile.movementSpeed;
		sp.impactSound = SoundType.HIT; // FIXME
		sp.x = source.x;
		sp.y = source.y;
		sp.angle = Math.atan2(ay - sp.y, ax - sp.x);
		sp.matrix = sp.owner == player() ? p.projectile.alternative : p.projectile.matrix;
		sp.damage = p.projectile.damage * p.count;
		
		projectiles.add(sp);
		sound(p.projectile.sound);
	}
	/**
	 * Apply loss results back to the initial fleets and planets.
	 * @param winner the winner of the fight
	 */
	void concludeBattle(Player winner) {
		boolean groundLosses = false;
		for (SpacewarStructure s : battle.spaceLosses) {
			if (s.item != null) {
				if (s.count > 0) {
					s.item.count = s.count;
					s.item.hp = s.hp;
					s.item.shield = s.shield;
					if (s.item.hp <= 0) {
						removeFromInventory(s);
					}
				} else {
					removeFromInventory(s);
				}
			} else
			if (s.building != null) {
				s.building.hitpoints = s.hp * s.building.type.hitpoints / s.hpMax;
				if (s.building.hitpoints <= 0) {
					s.planet.surface.removeBuilding(s.building);
					s.planet.surface.placeRoads(s.planet.race, world().buildingModel);
				}
				groundLosses = true;
			}
		}
		Set<Fleet> fleets = new HashSet<Fleet>();
		fleets.add(battle.attacker);

		// if the planet was fired upon
		if (groundLosses) {
			// reduce population according to the battle statistics
			if (battle.targetPlanet != null) {
				if (winner == battle.targetPlanet.owner) {
					BattleSimulator.applyPlanetDefended(battle.targetPlanet, 1000);
				} else {
					BattleSimulator.applyPlanetConquered(battle.targetPlanet, 1500);
				}
			} else
			if (battle.helperPlanet != null) {
				BattleSimulator.applyPlanetDefended(battle.helperPlanet, 500);
			}
		}
		
		if (battle.helperFleet != null) {
			fleets.add(battle.helperFleet);
		}
		if (battle.targetFleet != null) {
			fleets.add(battle.targetFleet);
		}
		for (Fleet f : fleets) {
			f.task = FleetTask.IDLE;
			int gu = f.adjustVehicleCounts();
			if (f.owner == battle.attacker.owner) {
				battle.attackerGroundLosses += gu;
			} else {
				battle.defenderGroundLosses += gu;
			}
			if (f.inventory.size() == 0) {
				world().removeFleet(f);
			}
		}
		battle.spacewarWinner = winner;
		battle.retreated = stopRetreat.visible;
		
		player().ai.spaceBattleDone(this);
		nonPlayer().ai.spaceBattleDone(this);
		// attacker wins
		final BattleInfo bi = battle;
		if (battle.attacker.owner == winner) {
			// originally attacking the planet
			if (battle.targetPlanet != null && battle.targetPlanet.owner != null) {
				FleetStatistics fs = battle.attacker.getStatistics();
				if (fs.vehicleCount > 0) {
					commons.stopMusic();
					commons.control().playVideos(new Action0() {
						@Override
						public void invoke() {
							PlanetScreen ps = (PlanetScreen)displayPrimary(Screens.COLONY);
							
							ps.initiateBattle(bi);
							commons.playBattleMusic();
						}
					}, "groundwar/felall");
					return;
				}
			}
		}
		BattlefinishScreen bfs = (BattlefinishScreen)displaySecondary(Screens.BATTLE_FINISH);
		bfs.displayBattleSummary(bi);
	}
	/**
	 * Check if the planet has bunker.
	 * @param planet the target planet
	 * @return true if has bunker
	 */
	boolean hasBunker(Planet planet) {
		for (Building b : planet.surface.buildings) {
			if (b.type.kind.equals("Bunker")) {
				return true;
			}
		}
		return false;
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
	public <T> T random(List<T> list) {
		return world().random(list);
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
		if (s.owner == battle.attacker.owner && !attackerOnRight) {
			// flee to the left side
			s.moveTo = new Point2D.Double(-1000, s.y);
		} else {
			// flee to the right side
			s.moveTo = new Point2D.Double(space.width + 1000, s.y);
		}
		s.attack = null;
		s.guard = false;
	}
	@Override
	public List<SpacewarStructure> structures(Player owner) {
		List<SpacewarStructure> result = JavaUtils.newArrayList();
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
}
