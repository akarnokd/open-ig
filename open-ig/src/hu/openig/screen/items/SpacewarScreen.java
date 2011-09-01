/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Act;
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
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.Screens;
import hu.openig.model.SpacewarBeam;
import hu.openig.model.SpacewarExplosion;
import hu.openig.model.SpacewarProjectile;
import hu.openig.model.SpacewarProjector;
import hu.openig.model.SpacewarShield;
import hu.openig.model.SpacewarShip;
import hu.openig.model.SpacewarStation;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWeaponPort;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The spacewar screen.
 * @author akarnokd, 2010.01.06.
 */
public class SpacewarScreen extends ScreenBase {
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
		Act action;
		/** Is the button disabled? */
		boolean disabled;
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
			if (disabled) {
				g2.drawImage(phases[0], x, y, null);
				Paint p = g2.getPaint();
				g2.setPaint(new TexturePaint(commons.common().disabledPattern, new Rectangle(x, y, 3, 3)));
				g2.fillRect(x, y, phases[0].getWidth(), phases[0].getHeight());
				g2.setPaint(p);
			} else
			if (pressed) {
				g2.drawImage(phases[2], x, y, null);
			} else
			if (selected) {
				g2.drawImage(phases[1], x, y, null);
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
			return !disabled && mx >= x && my >= y && mx < x + phases[0].getWidth() && my < y + phases[0].getHeight();
		}
		/** Invoke the associated action if present. */
		public void invoke() {
			if (action != null) {
				action.act();
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
		Act onPress;
		/** The action to perform on release. */
		Act onRelease;
		/** Is this button visible. */
		boolean visible;
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
			return visible && mx >= x && my >= y && mx < x + phases[0].getWidth() && my < y + phases[0].getHeight();
		}
		/**
		 * Invoke the onPress action.
		 */
		public void pressed() {
			if (onPress != null) {
				onPress.act();
			}
		}
		/** Invoke the onRelease action. */
		public void released() {
			if (onRelease != null) {
				onRelease.act();
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
		Act action;
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
					action.act();
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
	final List<SpacewarShip> ships = new ArrayList<SpacewarShip>();
	/** The space stations for animation. */
	final List<SpacewarStation> stations = new ArrayList<SpacewarStation>();
	/** The shields on the planet below for animation. */
	final List<SpacewarShield> shields = new ArrayList<SpacewarShield>();
	/** The the projectors on the planet below for animation. */
	final List<SpacewarProjector> projectors = new ArrayList<SpacewarProjector>();
	/** The beams for animation. */
	final List<SpacewarBeam> beams = new ArrayList<SpacewarBeam>();
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
	/** The selection box mode. */
	enum SelectionBoxMode {
		/** New selection. */
		NEW,
		/** Additive selection. */
		ADD,
		/** Subtractive selection. */
		SUBTRACT
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
	/** Annotation to show a component on a specified panel mode and side. */
	@Retention(RetentionPolicy.RUNTIME)
	@interface Show {
		/** The panel mode. */
		PanelMode mode();
		/** Is the left side? */
		boolean left();
	}
	
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
	@Override
	public void onInitialize() {
		mainCommands = new ArrayList<ThreePhaseButton>();
		mainCommands.add(new ThreePhaseButton(33, 24, commons.spacewar().stop));
		mainCommands.add(new ThreePhaseButton(33 + 72, 24, commons.spacewar().move));
		mainCommands.add(new ThreePhaseButton(33, 24 + 35, commons.spacewar().kamikaze));
		mainCommands.add(new ThreePhaseButton(33 + 72, 24 + 35, commons.spacewar().attack));
		mainCommands.add(new ThreePhaseButton(33, 24 + 35 * 2, commons.spacewar().guard));
		mainCommands.add(new ThreePhaseButton(33 + 72, 24 + 35 * 2, commons.spacewar().rocket));
		
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
		btn.action = new Act() {
			@Override
			public void act() {
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
	protected void doButtonAnimations() {
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
			if (e.has(Button.RIGHT) && mainmap.contains(e.x, e.y)) {
				lastX = e.x;
				lastY = e.y;
				panning = true;
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
	 * @return Returns a list of the currently selected structures.
	 */
	List<SpacewarStructure> getSelection() {
		List<SpacewarStructure> result = JavaUtils.newArrayList();
		getSelection(stations, result);
		getSelection(shields, result);
		getSelection(projectors, result);
		getSelection(ships, result);
		return result;
	}
	/**
	 * Get the selected structures from the {@code source} list.
	 * @param source the source list
	 * @param dest the destination list
	 */
	void getSelection(List<? extends SpacewarStructure> source, List<? super SpacewarStructure> dest) {
		for (SpacewarStructure s : source) {
			if (s.selected) {
				dest.add(s);
			}
		}
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
		
		own = testStructure(stations, candidates, own, p0, p1);
		own = testStructure(shields, candidates, own, p0, p1);
		own = testStructure(projectors, candidates, own, p0, p1);
		own = testStructure(ships, candidates, own, p0, p1);
		
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
		scale = Math.min(1.0, Math.round(s) / 20.0);
		pan(0, 0);
	}

	@Override
	public void onEnter(Screens mode) {
		buttonTimer = commons.register(100, new Act() {
			@Override
			public void act() {
				doButtonAnimations();
			}
		});
		selectButton(leftShipStatus, true);
		selectButton(rightShipStatus, false);
		selectionBox = false;
		displaySelectedShipInfo();
	}

	@Override
	public void onLeave() {
		close0(buttonTimer);
		buttonTimer = null;
		
		commons.restoreMainSimulationSpeedFunction();
		commons.battleMode = false;
		commons.playRegularMusic();
		
		// cleanup
		
		battle = null;
		stations.clear();
		shields.clear();
		projectiles.clear();
		projectors.clear();
		ships.clear();
		beams.clear();
		explosions.clear();
		
		leftStatusPanel.clear();
		rightStatusPanel.clear();
		
		leftShipInfoPanel.clear();
		rightShipInfoPanel.clear();
		layoutPanel.selected = null;
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
		super.draw(g2);
	}
	/** 
	 * Zoom in.
	 * @param x the mouse position to keep steady
	 * @param y the mouse position to keep steady 
	 */
	protected void doZoomIn(int x, int y) {
		Point2D.Double p0 = mouseToSpace(x, y);
		scale = Math.min(scale + 0.05, 1.0);
		Point2D.Double p1 = mouseToSpace(x, y);
		pan((int)(p0.x - p1.x), (int)(p0.y - p1.y));
	}
	/** 
	 * Zoom out.
	 * @param x the mouse position to keep steady
	 * @param y the mouse position to keep steady 
	 */
	protected void doZoomOut(int x, int y) {
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
		
		double x0 = (x - mainmap.x - ox) * scale;
		double y0 = (y - mainmap.y - oy) * scale;
		return new Point2D.Double(x0, y0);
	}
	/** Pause. */
	protected void doPause() {
		commons.simulation.pause();
	}
	/** Unpause. */
	protected void doUnpause() {
		commons.simulation.resume();
	}
	/** Retreat mode. */
	protected void doRetreat() {
		retreat.visible = false;
		confirmRetreat.visible = true;
	}
	/** Confirm retreat. */
	protected void doConfirmRetreat() {
		confirmRetreat.visible = false;
		stopRetreat.visible = true;
		for (ThreePhaseButton btn : mainCommands) {
			btn.disabled = true;
		}
	}
	/** Unconfirm retreat. */
	protected void doUnconfirmRetreat() {
		confirmRetreat.visible = false;
		retreat.visible = true;
	}
	/** Stop retreating. */
	protected void doStopRetreat() {
		stopRetreat.visible = false;
		retreat.visible = true;
		for (ThreePhaseButton btn : mainCommands) {
			btn.disabled = false;
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
		shields.clear();
		projectors.clear();

		ships.clear();
		stations.clear();
		
		projectiles.clear();
		beams.clear();
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

			int defenseWidth = Math.max(maxWidth(shields), maxWidth(projectors));
			centerStructures(space.width - commons.spacewar().planet.getWidth() / 2, JavaUtils.concat(shields, projectors));
			xmax -= 3 * defenseWidth / 2;
			
			// place and align stations
			placeStations(nearbyPlanet, alien);
			int stationWidth = maxWidth(stations);
			centerStructures(xmax - stationWidth, stations);
			xmax -= 3 * stationWidth / 2;
			
			
			// add fighters of the planet
			List<SpacewarShip> shipWall = JavaUtils.newArrayList();
			createSpacewarShips(nearbyPlanet.inventory, nearbyPlanet.owner, 
					EnumSet.of(ResearchSubCategory.SPACESHIPS_FIGHTERS), shipWall);
			
			if (!shipWall.isEmpty()) {
				int maxw = createSingleRowBatchWall(xmax, true, shipWall, ships);
				xmax -= 3 * maxw / 2;
			}
		} else {
			planetVisible = false;
		}
		
		// FIXME for now, place fleets via the wall formation
		
		if (nearbyPlanet != null && nearbyPlanet.owner == battle.attacker.owner) {
			// place the attacker on the right side (planet side)
			
			placeFleet(xmax, true, battle.attacker);
			// place the defender on the left side
			
			if (nearbyFleet != null) {
				placeFleet(0, false, nearbyFleet);
			}
		} else {
			// place attacker on the left side
			placeFleet(0, false, battle.attacker);
			
			// place the defender on the planet side (right side)
			if (nearbyFleet != null) {
				placeFleet(xmax, true, nearbyFleet);
			}
		}
		
		zoomToFit();
		commons.playBattleMusic();

		setSpacewarTimeControls();

		displayPanel(PanelMode.SHIP_STATUS, true);
		if (battle.attacker.owner == player() && (nearbyPlanet == null || nearbyPlanet.owner != player())) {
			displayPanel(PanelMode.LAYOUT, false);
			setLayoutSelectionMode(true);
		} else {
			setLayoutSelectionMode(false);
			commons.simulation.resume();
		}
		
	}
	/**
	 * Place a fleet onto the map starting from the {@code x} position and {@code angle}.
	 * @param x the starting position
	 * @param left expand to the left?
	 * @param fleet the fleet to place
	 */
	void placeFleet(int x, boolean left, Fleet fleet) {
		List<SpacewarShip> largeShipWall = JavaUtils.newArrayList();
		// place attacker on the planet side (right side)
		createSpacewarShips(fleet.inventory, fleet.owner, 
				EnumSet.of(ResearchSubCategory.SPACESHIPS_BATTLESHIPS, ResearchSubCategory.SPACESHIPS_CRUISERS), largeShipWall);
		
		if (!largeShipWall.isEmpty()) {
			int maxw = createMultiRowWall(x, left, largeShipWall, ships);
			x = left ? (x - maxw) : (x + maxw);
		}
		
		List<SpacewarShip> smallShipWall = JavaUtils.newArrayList();
		List<SpacewarShip> smallShipWallOut = JavaUtils.newArrayList();
		
		createSpacewarShips(fleet.inventory, fleet.owner, 
				EnumSet.of(ResearchSubCategory.SPACESHIPS_FIGHTERS), smallShipWall);
		if (!smallShipWall.isEmpty()) {
			createSingleRowBatchWall(x, left, smallShipWall, smallShipWallOut);
			ships.addAll(smallShipWallOut);
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
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS && ii.owner == nearbyPlanet.owner) {
				
				BattleSpaceEntity bse = world().battle.spaceEntities.get(ii.type.id);
				
				SpacewarStation st = new SpacewarStation();
				st.item = ii;
				st.owner = nearbyPlanet.owner;
				st.destruction = bse.destruction;
				st.image = alien ? bse.alternative[0] : bse.normal[0];
				st.infoImage = bse.infoImage;
				st.shield = ii.shield;
				st.shieldMax = Math.max(0, ii.shieldMax());
				st.hp = ii.hp;
				st.hpMax = ii.type.hitpoints();
				
				st.ecmLevel = setWeaponPorts(ii, st.ports);
				
				stations.add(st);
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
				
				SpacewarShield sws = new SpacewarShield();
				sws.image = alien ? bge.alternative : bge.normal;
				sws.infoImage = bge.infoImage;
				sws.hp = b.battleHitpoints();
				sws.hpMax = b.type.hitpoints();
				sws.owner = nearbyPlanet.owner;
				sws.destruction = bge.destruction;
				sws.building = b;

				shieldValue = Math.max(shieldValue, eff * bge.shields);

				shields.add(sws);
			}
		}
		for (SpacewarShield sws : shields) {
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
				SpacewarProjector sp = new SpacewarProjector();

				sp.angles = alien ? bge.alternative : bge.normal;
				sp.angle = Math.PI;
				sp.infoImage = bge.infoImage;
				sp.hp = b.battleHitpoints();
				sp.hpMax = b.type.hitpoints();
				sp.owner = nearbyPlanet.owner;
				sp.destruction = bge.destruction;
				sp.building = b;
				
				sp.shield = (int)(sp.hp * shieldValue / 100);
				sp.shieldMax = (int)(sp.hpMax * shieldValue / 100);
				
				sp.rotationSpeed = bge.rotationSpeed;

				BattleProjectile pr = world().battle.projectiles.get(bge.projectile);
				
				SpacewarWeaponPort wp = new SpacewarWeaponPort();
				wp.projectile = pr.copy();
				wp.projectile.damage = bge.damage;
				
				
				sp.ports.add(wp);
				
				projectors.add(sp);
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
			Collection<SpacewarShip> items, 
			Collection<SpacewarShip> out) {
		int maxWidth = 0;
		int maxHeight = 0;
		// determine number of slots
		for (SpacewarShip e : items) {
			maxWidth = Math.max(maxWidth, e.get().getWidth());
			maxHeight += e.get().getWidth();
		}
		
		LinkedList<SpacewarShip> ships = new LinkedList<SpacewarShip>(items);
		LinkedList<SpacewarShip> group = new LinkedList<SpacewarShip>();

		while (!ships.isEmpty()) {
			SpacewarShip sws = ships.removeFirst();
			if (sws.count > 1) {
				if (maxHeight + sws.get().getHeight() <= space.height) {
					int sum = sws.count;
					SpacewarShip sws2 = sws.copy();
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
	void evenCounts(Collection<SpacewarShip> ships, InventoryItem item) {
		int count = 0;
		int sum = 0;
		for (SpacewarShip sws : ships) {
			if (sws.item == item) {
				count++;
				sum += sws.count;
			}
		}
		double n = 1.0 * sum / count;
		int i = 1;
		int alloc = 0;
		SpacewarShip last = null;
		for (SpacewarShip sws : ships) {
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
			Collection<? extends SpacewarShip> ships, 
			Collection<? super SpacewarShip> out) {
		
		List<List<SpacewarShip>> rows = new ArrayList<List<SpacewarShip>>();
		int rowIndex = -1;
		int y = 0;
		List<SpacewarShip> currentRow = null;
		
		// put ships into rows
		for (SpacewarShip sws : ships) {
			if (y + sws.get().getHeight() >= space.height || rowIndex < 0) {
				rowIndex++;
				currentRow = new ArrayList<SpacewarShip>();
				rows.add(currentRow);
				y = 0;
			}
			currentRow.add(sws);
			y += sws.get().getHeight();
		}

		int maxWidth = 0;
		// align all rows center
		for (List<SpacewarShip> row : rows) {
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
	 * @param inventory the sequence of inventory
	 * @param owner the owner filter
	 * @param categories the categories to use
	 * @param ships the output of ships
	 */
	void createSpacewarShips(Iterable<? extends InventoryItem> inventory,
			Player owner,
			EnumSet<ResearchSubCategory> categories,
			Collection<? super SpacewarShip> ships
			) {
		for (InventoryItem ii : inventory) {
			if (categories.contains(ii.type.category) && ii.owner == owner) {
				BattleSpaceEntity bse = world().battle.spaceEntities.get(ii.type.id);
				if (bse == null) {
					System.err.println("Missing space entity: " + ii.type.id);
				}
				
				SpacewarShip sws = new SpacewarShip();

				sws.item = ii;
				sws.owner = owner;
				sws.destruction = bse.destruction;
				sws.angles = owner != player() ? bse.alternative : bse.normal;
				sws.infoImage = bse.infoImage;
				sws.shield = ii.shield;
				sws.shieldMax = Math.max(0, ii.shieldMax());
				sws.hp = ii.hp;
				sws.hpMax = ii.type.hitpoints();
				sws.count = ii.count;
				sws.rotationSpeed = bse.rotationSpeed;
				sws.movementSpeed = bse.movementSpeed;
				
				sws.ecmLevel = setWeaponPorts(ii, sws.ports);
				
				ships.add(sws);
			}
		}
		
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
				SpacewarWeaponPort wp = new SpacewarWeaponPort();
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

		g2.drawImage(commons.spacewar().background, 0, 0, space.width, space.height, null);
		
		if (planetVisible) {
			g2.drawImage(commons.spacewar().planet, space.width - commons.spacewar().planet.getWidth(), 0, null);
		}
		
		drawSpacewarStructures(ships, g2);
		drawSpacewarStructures(stations, g2);
		drawSpacewarStructures(shields, g2);
		drawSpacewarStructures(projectors, g2);
		
		for (SpacewarProjectile e : projectiles) {
			drawCenter(e.get(), e.x, e.y, g2);
		}
		for (SpacewarBeam e : beams) {
			drawCenter(e.get(), e.x, e.y, g2);
		}
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
		g2.setColor(Color.LIGHT_GRAY);
		for (SpacewarBeam e : beams) {
			int x = (int)(e.x * minimap.width / space.width);
			int y = (int)(e.y * minimap.height / space.height);
			g2.drawLine(x, y, x, y);
		}
		g2.setColor(Color.WHITE);
		
		save0 = g2.getClip();
		g2.clipRect(minimap.x, minimap.y, minimap.width, minimap.height);
		
		drawSpacewarStructuresMinimap(ships, g2);
		drawSpacewarStructuresMinimap(stations, g2);
		drawSpacewarStructuresMinimap(shields, g2);
		drawSpacewarStructuresMinimap(projectors, g2);
		
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
				g2.fillRect((int)e.x - w2 + 3, y, dw, 3);
				g2.setColor(Color.GREEN);
				g2.fillRect((int)e.x - w2 + 3, y, e.hp * dw / e.hpMax, 3);
				if (e.shieldMax > 0) {
					g2.setColor(new Color(0xFFFFCC00));
					g2.fillRect((int)e.x - w2 + 3, y, e.shield * dw / e.shieldMax, 3);
				}
			}
			if (e instanceof SpacewarShip) {
				SpacewarShip sws = (SpacewarShip)e;
				if (sws.count != 1) {
					commons.text().paintTo(g2, (int)(e.x - w2), (int)(e.y + h / 2 - 8), 7, 0xFFFFFFFF, Integer.toString(sws.count));
				}
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
			if (e instanceof SpacewarShip) {
				SpacewarShip sws = (SpacewarShip) e;
				if (sws.item.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
					w = 2;
				} else
				if (sws.item.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
					w = 3;
				} else
				if (sws.item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
					w = 1;
				}
			} else
			if (e instanceof SpacewarStation) {
				w = 3;
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
	void orientStructures(double angle, Iterable<? extends SpacewarShip> structures) {
		for (SpacewarShip s : structures) {
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
					EquipmentConfigure.drawSlots(g2, item, selectedSlot);
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
				
				if (item instanceof SpacewarShip) {
					SpacewarShip s = (SpacewarShip) item;
	
					unitType.text(format("spacewar.ship_type", s.item.type.longName), true);
					unitName.text(format("spacewar.ship_name", "-"), true);
					
					BattleSpaceEntity bse = world().battle.spaceEntities.get(s.item.type.id);
					image = bse.infoImage;
					
					this.item = s.item;
					if (lastItem != s.item) {
						selectFirstSlot();
					}
					updateSlot(selectedSlot);
				} else
				if (item instanceof SpacewarStation) {
					SpacewarStation s = (SpacewarStation) item;
					unitType.text(format("spacewar.ship_type", s.item.type.longName), true);
					unitName.text(format("spacewar.ship_name", "-"), true);
					BattleSpaceEntity bse = world().battle.spaceEntities.get(s.item.type.id);
					image = bse.infoImage;
					this.item = s.item;
					if (lastItem != s.item) {
						selectFirstSlot();
					}
					updateSlot(selectedSlot);
				} else
				if (item instanceof SpacewarProjector) {
					SpacewarProjector s = (SpacewarProjector) item;
					unitType.text(format("spacewar.ship_type", s.building.type.name), true);
					BattleGroundProjector bgp = world().battle.groundProjectors.get(s.building.type.id);
					image = bgp.infoImage;
					
				} else
				if (item instanceof SpacewarShield) {
					SpacewarShield s = (SpacewarShield) item;
					unitType.text(format("spacewar.ship_type", s.building.type.name), true);
					BattleGroundShield bgp = world().battle.groundShields.get(s.building.type.id);
					image = bgp.infoImage;
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
					damage.text(format("spacewar.ship_weapon_damage", 100 * (is.type.hitpoints() - is.hp) / is.type.hitpoints()), true);
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
		for (SpacewarProjector e : projectors) {
			SpacebattleStatistics stat = (e.owner == player()) ? own : other;  
			stat.guns++;
			setPortStatistics(stat, e.ports);
		}
		for (SpacewarStation e : stations) {
			SpacebattleStatistics stat = (e.owner == player()) ? own : other;  
			stat.stations++;
			setPortStatistics(stat, e.ports);
		}
		for (SpacewarShip e : ships) {
			SpacebattleStatistics stat = (e.owner == player()) ? own : other;  
			stat.units++;
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
			String s = "";
			if (item instanceof SpacewarStation) {
				s = get("spacewar.station_information");
			} else
			if (item instanceof SpacewarShield) {
				s = get("spacewar.shield_information");
			} else
			if (item instanceof SpacewarProjector) {
				s = get("spacewar.projector_information");
			} else {
				s = get("spacewar.ship_information");				
			}
			int dx = (width - commons.text().getTextWidth(10, s)) / 2;
			commons.text().paintTo(g2, dx, 6, 10, TextRenderer.YELLOW, s);
			
			int y = 26;
			
			boolean isws = item instanceof SpacewarShip;
			if (item == null) {
				if (isMany) {
					commons.text().paintTo(g2, 8, y, 7, TextRenderer.GREEN, get("spacewar.ship_status_many"));
				} else {
					commons.text().paintTo(g2, 8, y, 7, TextRenderer.GREEN, get("spacewar.ship_status_none"));
				}
			} else {
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
					
					SpacewarShip sws = (SpacewarShip)item;
					for (InventorySlot is : sws.item.slots) {
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
					SpacewarShip sws = (SpacewarShip)item;
					for (InventorySlot is : sws.item.slots) {
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
	void applyLayout(Iterable<? extends SpacewarShip> ships, Player owner, BattleSpaceLayout layout) {
		LinkedList<SpacewarShip> fighters = JavaUtils.newLinkedList();
		LinkedList<SpacewarShip> nonFighters = JavaUtils.newLinkedList();
		for (SpacewarShip sws : ships) {
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
		List<SpacewarShip> placed = new ArrayList<SpacewarShip>();
		for (Map.Entry<Location, Boolean> e : olist) {
			Location p = e.getKey();
			if (e.getValue()) {
				if (!fighters.isEmpty()) {
					SpacewarShip sws = fighters.removeFirst();
					sws.x = (p.x + 0.5) * space.width / layout.getWidth();
					sws.y = (p.y + 0.5) * space.height / layout.getHeight();
					placeNearby(placed, sws);
				}
			} else {
				if (!nonFighters.isEmpty()) {
					SpacewarShip sws = nonFighters.removeFirst();
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
						SpacewarShip sws = fighters.removeFirst();
						sws.x = (p.x + 0.5) * space.width / layout.getWidth();
						sws.y = (p.y + 0.5) * space.height / layout.getHeight();
						placeNearby(placed, sws);
					}
				} else {
					if (!nonFighters.isEmpty()) {
						SpacewarShip sws = nonFighters.removeFirst();
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
	boolean intersects(Iterable<? extends SpacewarShip> ships, SpacewarShip s) {
		for (SpacewarShip sws : ships) {
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
	void placeNearby(List<SpacewarShip> ships, SpacewarShip s) {
		double initialX = s.x; 
		double initialY = s.y;
		if (intersects(ships, s)) {
			outer:
			for (int r = 2; r < space.width; r += 2) {
				for (double alpha = Math.PI / 2; alpha < 3 * Math.PI / 2; alpha += Math.PI / 36) {
					s.x = initialX + r * Math.cos(alpha);
					s.y = initialY + r * Math.sin(alpha);
					if (!intersects(ships, s)) {
						break outer;
					}
				}
			}
		}
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
					applyLayout(ships, player(), selected);
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
				if (okDown && within(e)) {
					displayPanel(PanelMode.SHIP_STATUS, false);
					setLayoutSelectionMode(false);
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
		commons.replaceSimulation(new Act() {
			@Override
			public void act() {
				doSpacewarSimulation();
			}
		},
		new Func1<SimulationSpeed, Integer>() {
			@Override
			public Integer invoke(SimulationSpeed value) {
				switch (value) {
				case NORMAL: return 200;
				case FAST: return 100;
				case ULTRA_FAST: return 50;
				default:
					throw new AssertionError("" + value);
				}
			};
		}
		);
	}
	/** Perform the spacewar simulation. */
	void doSpacewarSimulation() {
		
	}
}
