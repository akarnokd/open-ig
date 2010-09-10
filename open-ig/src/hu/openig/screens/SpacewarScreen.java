/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

/**
 * The spacewar screen.
 * @author karnokd, 2010.01.06.
 * @version $Revision 1.0$
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
				g2.setPaint(new TexturePaint(commons.disabledPattern, new Rectangle(x, y, 3, 3)));
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
			parent.repaint(x, y, phases[0].getWidth(), phases[0].getHeight());
		}
		/**
		 * Render the button.
		 * @param g2 the graphics object
		 * @param x0 the reference x coordinate
		 * @param y0 the reference y coordinate
		 */
		public void paintTo(Graphics2D g2, int x0, int y0) {
			g2.drawImage(commons.spacewar.stat, x0 + x, y0 + y, null);
			if (selected) {
				g2.drawImage(phases[1 + animationIndex], x0 + x + (commons.spacewar.stat.getWidth() - phases[1 + animationIndex].getWidth()) / 2,
						y0 + y + (commons.spacewar.stat.getHeight() - phases[1 + animationIndex].getHeight()) / 2, null);
			} else {
				g2.drawImage(phases[0], x0 + x + (commons.spacewar.stat.getWidth() - phases[0].getWidth()) / 2,
						y0 + y + (commons.spacewar.stat.getHeight() - phases[0].getHeight()) / 2, null);
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
	Timer buttonTimer;
	
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#finish()
	 */
	@Override
	public void finish() {
		buttonTimer.stop();
	}
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
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#initialize()
	 */
	@Override
	public void initialize() {
		mainCommands = new ArrayList<ThreePhaseButton>();
		mainCommands.add(new ThreePhaseButton(33, 24, commons.spacewar.stop));
		mainCommands.add(new ThreePhaseButton(33 + 72, 24, commons.spacewar.move));
		mainCommands.add(new ThreePhaseButton(33, 24 + 35, commons.spacewar.kamikaze));
		mainCommands.add(new ThreePhaseButton(33 + 72, 24 + 35, commons.spacewar.attack));
		mainCommands.add(new ThreePhaseButton(33, 24 + 35 * 2, commons.spacewar.guard));
		mainCommands.add(new ThreePhaseButton(33 + 72, 24 + 35 * 2, commons.spacewar.rocket));
		
		viewCommands = new ArrayList<ThreePhaseButton>();
		
		viewCommands.add(new ThreePhaseButton(33, 24 + 35 * 3, commons.spacewar.command));
		viewCommands.add(new ThreePhaseButton(33 + 72, 24 + 35 * 3, commons.spacewar.damage));
		viewCommands.add(new ThreePhaseButton(33, 24 + 35 * 3 + 30, commons.spacewar.fireRange));
		viewCommands.add(new ThreePhaseButton(33 + 72, 24 + 35 * 3 + 30, commons.spacewar.grid));
		
		zoom = new TwoPhaseButton(3, 24, commons.spacewar.zoom);
		zoom.visible = true;
		pause = new TwoPhaseButton(4, 19 + 170, commons.spacewar.pause);
		pause.visible = true;
		retreat = new TwoPhaseButton(33, 19 + 170, commons.spacewar.retreat);
		retreat.visible = true;
		confirmRetreat = new TwoPhaseButton(33, 19 + 170, commons.spacewar.sure);
		stopRetreat = new TwoPhaseButton(33, 19 + 170, commons.spacewar.stopTall);
		
		leftButtons = new ArrayList<AnimatedRadioButton>();
		leftButtons.add(new AnimatedRadioButton(0, 0, commons.spacewar.ships));
		leftButtons.add(new AnimatedRadioButton(0, 40, commons.spacewar.statistics));
		leftButtons.add(new AnimatedRadioButton(0, 80, commons.spacewar.fires));
		leftButtons.add(new AnimatedRadioButton(0, 120, commons.spacewar.computers));
		leftButtons.add(new AnimatedRadioButton(0, 160, commons.spacewar.movies));
		
		rightButtons = new ArrayList<AnimatedRadioButton>();
		rightButtons.add(new AnimatedRadioButton(0, 0, commons.spacewar.ships));
		rightButtons.add(new AnimatedRadioButton(0, 40, commons.spacewar.statistics));
		rightButtons.add(new AnimatedRadioButton(0, 80, commons.spacewar.fires));
		rightButtons.add(new AnimatedRadioButton(0, 120, commons.spacewar.computers));
		rightButtons.add(new AnimatedRadioButton(0, 160, commons.spacewar.movies));
		
		buttonTimer = new Timer(100, new Act() {
			@Override
			public void act() {
				doButtonAnimations();
			}
		});
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
		repaint();
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#keyTyped(int, int)
	 */
	@Override
	public void keyTyped(int key, int modifiers) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseMoved(int, int, int, int)
	 */
	@Override
	public void mouseMoved(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mousePressed(int, int, int, int)
	 */
	@Override
	public void mousePressed(int button, int x, int y, int modifiers) {
		boolean needRepaint = false;
		// the command panel
		if (x < commons.spacewar.commands.getWidth() && y < commons.spacewar.commands.getHeight() + 20 + commons.spacewar.frameTopLeft.getHeight()) {
			for (ThreePhaseButton btn : mainCommands) {
				if (btn.test(x, y)) {
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
				if (btn.test(x, y)) {
					btn.selected = !btn.selected;
					btn.pressed = true;
					needRepaint = true;
					btn.invoke();
					break;
				}
			}
			if (zoom.test(x, y)) {
				if (button == 1) {
					zoom.pressed = true;
					needRepaint = true;
					doZoomIn();
				} else
				if (button == 3) {
					zoom.pressed = true;
					needRepaint = true;
					doZoomOut();
				}
			}
			if (pause.test(x, y)) {
				if (!pause.pressed) {
					pause.pressed = true;
					needRepaint = true;
					doPause();
				} else {
					unpause = true;
				}
			}
			if (retreat.test(x, y)) {
				retreat.pressed = true;
				needRepaint = true;
			}
			if (confirmRetreat.test(x, y)) {
				confirmRetreat.pressed = true;
				needRepaint = true;
			}
			if (stopRetreat.test(x, y)) {
				stopRetreat.pressed = true;
				needRepaint = true;
			}
		}
		if (x < leftPanel.x && y > leftPanel.y) {
			for (AnimatedRadioButton arb : leftButtons) {
				if (arb.test(x, y, leftPanel.x - 28, leftPanel.y + 1) && !arb.selected) {
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
		if (x > rightPanel.x && y > rightPanel.y) {
			for (AnimatedRadioButton arb : rightButtons) {
				if (arb.test(x, y, rightPanel.x + rightPanel.width + 5, rightPanel.y + 1) && !arb.selected) {
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
		if (needRepaint) {
			repaint();
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseReleased(int, int, int, int)
	 */
	@Override
	public void mouseReleased(int button, int x, int y, int modifiers) {
		boolean needRepaint = false;
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
		if (pause.test(x, y)) {
			if (unpause) {
				unpause = false;
				pause.pressed = false;
				needRepaint = true;
				doUnpause();
			}
		}
		if (retreat.pressed) {
			retreat.pressed = false;
			if (retreat.test(x, y)) {
				doRetreat();
			}
			needRepaint = true;
		}
		if (confirmRetreat.pressed) {
			confirmRetreat.pressed = false;
			if (confirmRetreat.test(x, y)) {
				if (button == 1) {
					doConfirmRetreat();
				} else
				if (button == 3) {
					doUnconfirmRetreat();
				}
			}
			needRepaint = true;
		}
		if (stopRetreat.pressed) {
			stopRetreat.pressed = false;
			if (stopRetreat.test(x, y)) {
				doStopRetreat();
			}
			needRepaint = true;
		}
		if (needRepaint) {
			repaint();
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseScrolled(int, int, int, int)
	 */
	@Override
	public void mouseScrolled(int direction, int x, int y, int modifiers) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter() {
		buttonTimer.start();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onLeave()
	 */
	@Override
	public void onLeave() {
		buttonTimer.stop();
	}
	/** The location of the minimap. */
	Rectangle minimap = new Rectangle();
	/** The location of the main window area. */
	Rectangle mainmap = new Rectangle();
	/** The left inner panel. */
	Rectangle leftPanel = new Rectangle();
	/** The right inner panel. */
	Rectangle rightPanel = new Rectangle();
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onResize()
	 */
	@Override
	public void doResize() {
		// TODO Auto-generated method stub
		minimap.setBounds(62, 168 + 20, 110, 73);
		mainmap.setBounds(175, 23, parent.getWidth() - 3 - commons.spacewar.commands.getWidth(),
				parent.getHeight() - 38 - 3 - commons.spacewar.panelStatLeft.getHeight());
		leftPanel.setBounds(32, parent.getHeight() - 18 - 3 - 195, 286, 195);
		rightPanel.setBounds(parent.getWidth() - 33 - 286, parent.getHeight() - 18 - 3 - 195, 286, 195);
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#paintTo(java.awt.Graphics2D)
	 */
	@Override
	public void paintTo(Graphics2D g2) {
		onResize();
		g2.drawImage(commons.spacewar.frameTopLeft, 0, 20, null);
		
		g2.drawImage(commons.spacewar.frameTopRight, parent.getWidth() - commons.spacewar.frameTopRight.getWidth(), 20, null);

		g2.drawImage(commons.spacewar.commands, 0, 20 + commons.spacewar.frameTopLeft.getHeight(), null);
		g2.drawImage(commons.spacewar.frameRight, parent.getWidth() - commons.spacewar.frameRight.getWidth(), 20 + commons.spacewar.frameTopRight.getHeight(), null);
		
		g2.drawImage(commons.spacewar.panelStatLeft, 0, parent.getHeight() - commons.spacewar.panelStatLeft.getHeight() - 18, null);
		
		g2.drawImage(commons.spacewar.panelStatRight, parent.getWidth() - commons.spacewar.panelStatRight.getWidth(), parent.getHeight() - commons.spacewar.panelStatRight.getHeight() - 18, null);

		Paint p = g2.getPaint();

		TexturePaint tp = new TexturePaint(commons.spacewar.frameTopFill, new Rectangle(commons.spacewar.frameTopLeft.getWidth(), 20, 1, commons.spacewar.frameTopFill.getHeight()));
		g2.setPaint(tp);
		g2.fillRect(commons.spacewar.frameTopLeft.getWidth(), 20, parent.getWidth() - commons.spacewar.frameTopLeft.getWidth() - commons.spacewar.frameTopRight.getWidth(), commons.spacewar.frameTopFill.getHeight());
		
		tp = new TexturePaint(commons.spacewar.panelStatFill, new Rectangle(commons.spacewar.panelStatLeft.getWidth(), parent.getHeight() - commons.spacewar.panelStatLeft.getHeight() - 18, 1, commons.spacewar.panelStatFill.getHeight()));
		g2.setPaint(tp);
		g2.fillRect(commons.spacewar.panelStatLeft.getWidth(), parent.getHeight() - commons.spacewar.panelStatLeft.getHeight() - 18, parent.getWidth() - commons.spacewar.frameTopRight.getWidth() - commons.spacewar.frameTopLeft.getWidth(), commons.spacewar.panelStatFill.getHeight());
		
		tp = new TexturePaint(commons.spacewar.frameRightFill, new Rectangle(parent.getWidth() - commons.spacewar.frameRight.getWidth(), 20 + commons.spacewar.frameTopRight.getHeight() + commons.spacewar.frameRight.getHeight(), commons.spacewar.frameRightFill.getWidth(), commons.spacewar.frameRightFill.getHeight()));
		g2.setPaint(tp);
		g2.fillRect(parent.getWidth() - commons.spacewar.frameRight.getWidth(), 20 + commons.spacewar.frameTopRight.getHeight() + commons.spacewar.frameRight.getHeight(), commons.spacewar.frameRightFill.getWidth(), parent.getHeight() - 38 - commons.spacewar.frameTopRight.getHeight() - commons.spacewar.frameRight.getHeight() - commons.spacewar.panelStatRight.getHeight());
		
		tp = new TexturePaint(commons.spacewar.frameLeftFill, new Rectangle(0, 20 + commons.spacewar.frameTopLeft.getHeight() + commons.spacewar.commands.getHeight(), commons.spacewar.frameLeftFill.getWidth(), commons.spacewar.frameLeftFill.getHeight()));
		g2.setPaint(tp);
		g2.fillRect(0, 20 + commons.spacewar.frameTopLeft.getHeight() + commons.spacewar.commands.getHeight(), commons.spacewar.frameLeftFill.getWidth(), 
				parent.getHeight() - 36 - commons.spacewar.frameTopLeft.getHeight() - commons.spacewar.commands.getHeight() - commons.spacewar.panelStatLeft.getHeight());
		
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
		
		g2.drawImage(commons.spacewar.panelIg, leftPanel.x, leftPanel.y, null);
		g2.drawImage(commons.spacewar.panelIg, rightPanel.x, rightPanel.y, null);
	}
	/** Zoom in. */
	protected void doZoomIn() {
		
	}
	/** Zoom out. */
	protected void doZoomOut() {
		
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
	public void mouseDoubleClicked(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}
}
