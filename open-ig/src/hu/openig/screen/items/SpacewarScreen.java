/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Act;
import hu.openig.model.BattleGroundProjector;
import hu.openig.model.BattleGroundShield;
import hu.openig.model.BattleInfo;
import hu.openig.model.BattleProjectile;
import hu.openig.model.Building;
import hu.openig.model.Planet;
import hu.openig.model.Screens;
import hu.openig.model.SpacewarBeam;
import hu.openig.model.SpacewarExplosion;
import hu.openig.model.SpacewarObject;
import hu.openig.model.SpacewarProjectile;
import hu.openig.model.SpacewarProjector;
import hu.openig.model.SpacewarShield;
import hu.openig.model.SpacewarShip;
import hu.openig.model.SpacewarStation;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWeaponPort;
import hu.openig.model.World;
import hu.openig.screen.ScreenBase;
import hu.openig.sound.WarEffects;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Modifier;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

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
	class AnimatedRadioButton {
		/** The x coordinate. */
		int x;
		/** The y coordinate. */
		int y;
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
		 * @param x the X coordinate
		 * @param y the Y coordinate
		 * @param phases the phases
		 */
		public AnimatedRadioButton(int x, int y, BufferedImage[] phases) {
			this.x = x;
			this.y = y;
			this.phases = phases;
		}
		/** Move to the next animation. */
		protected void doAnimate() {
			animationIndex = (animationIndex + 1) % (phases.length - 1);
			askRepaint(x, y, phases[0].getWidth(), phases[0].getHeight());
		}
		/**
		 * Render the button.
		 * @param g2 the graphics object
		 * @param x0 the reference x coordinate
		 * @param y0 the reference y coordinate
		 */
		public void paintTo(Graphics2D g2, int x0, int y0) {
			g2.drawImage(commons.spacewar().stat, x0 + x, y0 + y, null);
			if (selected) {
				g2.drawImage(phases[1 + animationIndex], x0 + x + (commons.spacewar().stat.getWidth() - phases[1 + animationIndex].getWidth()) / 2,
						y0 + y + (commons.spacewar().stat.getHeight() - phases[1 + animationIndex].getHeight()) / 2, null);
			} else {
				g2.drawImage(phases[0], x0 + x + (commons.spacewar().stat.getWidth() - phases[0].getWidth()) / 2,
						y0 + y + (commons.spacewar().stat.getHeight() - phases[0].getHeight()) / 2, null);
			}
		}
		/**
		 * Test if the mouse is within this button.
		 * @param mx the mouse X coordinate
		 * @param my the mouse Y coordinate
		 * @param x0 the reference x coordinate
		 * @param y0 the reference y coordinate
		 * @return true if within the button
		 */
		public boolean test(int mx, int my, int x0, int y0) {
			return mx >= x0 + x && my >= y0 + y && mx < x0 + x + phases[0].getWidth() && my < y0 + y + phases[0].getHeight();
		}
		/** Invoke the associated action if present. */
		public void invoke() {
			if (action != null) {
				action.act();
			}
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
	/** The left animated buttons. */
	List<AnimatedRadioButton> leftButtons;
	/** The right animated buttons. */
	List<AnimatedRadioButton> rightButtons;
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
	/** The space effects. */
	WarEffects effects;
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
	private ThreePhaseButton viewCommand;
	/** View damages. */
	private ThreePhaseButton viewDamage;
	/** View range. */
	private ThreePhaseButton viewRange;
	/** View grids. */
	private ThreePhaseButton viewGrid;
	@Override
	public void onInitialize() {
		effects = new WarEffects(rl);
		
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
		
		leftButtons = new ArrayList<AnimatedRadioButton>();
		leftButtons.add(new AnimatedRadioButton(0, 0, commons.spacewar().ships));
		leftButtons.add(new AnimatedRadioButton(0, 40, commons.spacewar().statistics));
		leftButtons.add(new AnimatedRadioButton(0, 80, commons.spacewar().fires));
		leftButtons.add(new AnimatedRadioButton(0, 120, commons.spacewar().computers));
		leftButtons.add(new AnimatedRadioButton(0, 160, commons.spacewar().movies));
		
		rightButtons = new ArrayList<AnimatedRadioButton>();
		rightButtons.add(new AnimatedRadioButton(0, 0, commons.spacewar().ships));
		rightButtons.add(new AnimatedRadioButton(0, 40, commons.spacewar().statistics));
		rightButtons.add(new AnimatedRadioButton(0, 80, commons.spacewar().fires));
		rightButtons.add(new AnimatedRadioButton(0, 120, commons.spacewar().computers));
		rightButtons.add(new AnimatedRadioButton(0, 160, commons.spacewar().movies));
	}
	/**
	 * Animate selected buttons.
	 */
	protected void doButtonAnimations() {
		for (AnimatedRadioButton arb : leftButtons) {
			if (arb.selected) {
				arb.animationIndex = (arb.animationIndex + 1) % (arb.phases.length - 1);
			}
		}
		for (AnimatedRadioButton arb : rightButtons) {
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
			if (e.x < commons.spacewar().commands.getWidth() && e.y < commons.spacewar().commands.getHeight() + 20 + commons.spacewar().frameTopLeft.getHeight()) {
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
			if (e.x < leftPanel.x && e.y > leftPanel.y) {
				for (AnimatedRadioButton arb : leftButtons) {
					if (arb.test(e.x, e.y, leftPanel.x - 28, leftPanel.y + 1) 
							&& !arb.selected) {
						arb.selected = true;
						arb.animationIndex = 0;
						for (AnimatedRadioButton arb2 : leftButtons) {
							if (arb != arb2) {
								arb2.selected = false;
							}
						}
						arb.invoke();
						needRepaint = true;
						break;
					}
				}
			}
			if (e.x > rightPanel.x && e.y > rightPanel.y) {
				for (AnimatedRadioButton arb : rightButtons) {
					if (arb.test(e.x, e.y, rightPanel.x + rightPanel.width + 5, rightPanel.y + 1) && !arb.selected) {
						arb.selected = true;
						arb.animationIndex = 0;
						for (AnimatedRadioButton arb2 : rightButtons) {
							if (arb != arb2) {
								arb2.selected = false;
							}
						}
						arb.invoke();
						needRepaint = true;
						break;
					}
				}
			}
			if (e.has(Button.RIGHT) && mainmap.contains(e.x, e.y)) {
				lastX = e.x;
				lastY = e.y;
				panning = true;
			}
			if (e.has(Button.MIDDLE) && mainmap.contains(e.x, e.y)) {
				double xscale = mainmap.width * 1.0 / space.width;
				double yscale = mainmap.height * 1.0 / space.height;
				double s = Math.min(xscale, yscale) * 20;
				scale = Math.round(s) / 20.0;
				needRepaint = true;
				pan(0, 0);
			}
			break;
		case DRAG:
			if (panning) {
				pan(lastX - e.x, lastY - e.y);
				lastX = e.x;
				lastY = e.y;
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
		return needRepaint;
	}

	@Override
	public void onEnter(Screens mode) {
		buttonTimer = commons.register(100, new Act() {
			@Override
			public void act() {
				doButtonAnimations();
			}
		});
		effects.initialize(config.audioChannels, config.effectVolume);
	}

	@Override
	public void onLeave() {
		close0(buttonTimer);
		buttonTimer = null;
		
		effects.close();
		commons.restoreMainSimulationSpeedFunction();
		commons.battleMode = false;
		commons.playRegularMusic();
	}
	@Override
	public void onResize() {
		// TODO Auto-generated method stub
		minimap.setBounds(62, 168 + 20, 110, 73);
		mainmap.setBounds(175, 23, getInnerWidth() - 3 - commons.spacewar().commands.getWidth(),
				getInnerHeight() - 38 - 3 - commons.spacewar().panelStatLeft.getHeight());
		leftPanel.setBounds(32, getInnerHeight() - 18 - 3 - 195, 286, 195);
		rightPanel.setBounds(getInnerWidth() - 33 - 286, getInnerHeight() - 18 - 3 - 195, 286, 195);
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
		pause.paintTo(g2);
		retreat.paintTo(g2);
		confirmRetreat.paintTo(g2);
		stopRetreat.paintTo(g2);

		for (AnimatedRadioButton arb : leftButtons) {
			arb.paintTo(g2, leftPanel.x - 28, leftPanel.y + 1);
		}
		for (AnimatedRadioButton arb : rightButtons) {
			arb.paintTo(g2, rightPanel.x + rightPanel.width + 5, rightPanel.y + 1);
		}
		g2.setColor(Color.BLACK);
		g2.fill(minimap);
		
		g2.fill(mainmap);
		
		g2.drawImage(commons.spacewar().panelIg, leftPanel.x, leftPanel.y, null);
		g2.drawImage(commons.spacewar().panelIg, rightPanel.x, rightPanel.y, null);
		
		drawBattle(g2);
	}
	/** 
	 * Zoom in.
	 * @param x the mouse position to keep steady
	 * @param y the mouse position to keep steady 
	 */
	protected void doZoomIn(int x, int y) {
		Point p0 = mouseToSpace(x, y);
		scale = Math.min(scale + 0.05, 1.0);
		Point p1 = mouseToSpace(x, y);
		pan(p0.x - p1.x, p0.y - p1.y);
	}
	/** 
	 * Zoom out.
	 * @param x the mouse position to keep steady
	 * @param y the mouse position to keep steady 
	 */
	protected void doZoomOut(int x, int y) {
		Point p0 = mouseToSpace(x, y);
		scale = Math.max(scale - 0.05, 0.45);
		Point p1 = mouseToSpace(x, y);
		pan(p0.x - p1.x, p0.y - p1.y);
	}
	/**
	 * Convert the mouse coordinate to space coordinates.
	 * @param x the current mouse X on the screen
	 * @param y the current mouse Y on the screen
	 * @return the space coordinates
	 */
	Point mouseToSpace(int x, int y) {
		int ox = -offsetX;
		int oy = -offsetY;
		if (space.width * scale < mainmap.width) {
			ox = (int)((mainmap.width - space.width * scale) / 2);
		}
		if (space.height * scale < mainmap.height) {
			oy = (int)((mainmap.height - space.height * scale) / 2);
		}
		
		int x0 = x - mainmap.x - ox;
		int y0 = y - mainmap.y - oy;
		return new Point(x0, y0);
	}
	/** Pause. */
	protected void doPause() {
		
	}
	/** Unpause. */
	protected void doUnpause() {
		
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
		
		Planet nearbyPlanet = battle.targetPlanet;
		if (battle.targetFleet != null) {
			// locate the nearest planet if the target is a fleet
			double minDistance = Double.MAX_VALUE;
			for (Planet p : world().planets.values()) {
				double d1 = World.dist(p.x, p.y, battle.attacker.x, battle.attacker.y);
				if (d1 < minDistance) {
					minDistance = d1;
					if (minDistance < 20) {
						nearbyPlanet = p;
					}
				}
				double d2 = World.dist(p.x, p.y, battle.targetFleet.x, battle.targetFleet.y);
				if (d2 < minDistance) {
					minDistance = d1;
					if (minDistance < 20) {
						nearbyPlanet = p;
					}
				}
			}
		}
		if (nearbyPlanet != null 
				&& (nearbyPlanet.owner == battle.attacker.owner 
				|| nearbyPlanet == battle.targetPlanet 
				|| nearbyPlanet.owner == battle.targetFleet.owner)) {
			planetVisible = true;
			
			// place planetary defenses
			boolean alien = nearbyPlanet.owner != player();
			
			List<SpacewarStructure> surface = new ArrayList<SpacewarStructure>();
			double shieldValue = 0;
			// add shields
			for (Building b : nearbyPlanet.surface.buildings) {
				if (b.getEfficiency() >= 0.5 && b.type.kind.equals("Shield")) {
					BattleGroundShield bge = world().battle.groundShields.get(b.type.id);
					
					SpacewarShield sws = new SpacewarShield();
					sws.image = alien ? bge.alternative : bge.normal;
					sws.infoImage = bge.infoImage;
					sws.hp = b.hitpoints;
					sws.hpMax = b.type.hitpoints;
					sws.owner = nearbyPlanet.owner;
					sws.destruction = bge.destruction;

					shieldValue = Math.max(shieldValue, b.getEfficiency() * bge.shields);

					shields.add(sws);
					surface.add(sws);
				}
			}
			// add projectors
			for (Building b : nearbyPlanet.surface.buildings) {
				if (b.getEfficiency() >= 0.5 && b.type.kind.equals("Gun")) {
					BattleGroundProjector bge = world().battle.groundProjectors.get(b.type.id);
					SpacewarProjector sp = new SpacewarProjector();

					sp.angles = alien ? bge.alternative : bge.normal;
					sp.angle = Math.PI;
					sp.infoImage = bge.image;
					sp.hp = b.hitpoints;
					sp.hpMax = b.type.hitpoints;
					sp.owner = nearbyPlanet.owner;
					sp.destruction = bge.destruction;

					BattleProjectile pr = world().battle.projectiles.get(bge.projectile);
					
					SpacewarWeaponPort wp = new SpacewarWeaponPort();
					wp.projectile = pr;
					
					sp.ports.add(wp);
					
					projectors.add(sp);
					surface.add(sp);
				}
			}
			if (surface.size() > 0) {
				// place and align surface objects equally
				int dy = space.height / surface.size();
				int y = dy / 2;
				for (SpacewarStructure o : surface) {
					o.x = space.width - commons.spacewar().planet.getWidth() / 2;
					o.y = y;
					o.shield = (int)(o.hp * shieldValue / 100);
					o.shieldMax = o.shield; 
					y += dy;
				}
			}

			
			
		} else {
			planetVisible = false;
		}
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
		
		g2.setColor(Color.GRAY);
		g2.fill(space);
		
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
				g2.setColor(Color.YELLOW);
				g2.drawRect((int)e.x - w2 - 1, 
						(int)e.y - h2 - 1, w + 2, h + 2);
			}
			if (viewDamage.selected) {
				int y = (int)e.y - h2 - 5;
				if (e.shieldMax > 0) {
					y -= 4;
				}
				g2.setColor(Color.BLACK);
				g2.fillRect((int)e.x - w2, y, w, 3);
				g2.setColor(Color.GREEN);
				g2.fillRect((int)e.x - w2, y, e.hp * w / e.hpMax, 3);
				if (e.shieldMax > 0) {
					g2.setColor(Color.BLACK);
					y += 4;
					g2.fillRect((int)e.x - w2, y, w, 3);
					g2.setColor(new Color(0xFFFFCC00));
					g2.fillRect((int)e.x - w2, y, e.shield * w / e.shieldMax, 3);
				}
			}
		}
	}
	/**
	 * Draw the spacewar structures symbolically onto the minimap.
	 * @param structures the sequence of structures
	 * @param g2 the graphics context
	 */
	void drawSpacewarStructuresMinimap(Iterable<? extends SpacewarStructure> structures, Graphics2D g2) {
		for (SpacewarObject e : structures) {
			int x = minimap.x + (int)(e.x * minimap.width / space.width);
			int y = minimap.y + (int)(e.y * minimap.height / space.height);
			g2.drawLine(x, y, x, y);
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
}
