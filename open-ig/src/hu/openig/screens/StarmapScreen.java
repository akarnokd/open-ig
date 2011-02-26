/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;


import hu.openig.core.Act;
import hu.openig.model.Fleet;
import hu.openig.model.Planet;
import hu.openig.render.TextRenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javax.swing.Timer;

/**
 * The starmap screen.
 * @author akarnokd, 2010.01.11.
 */
public class StarmapScreen extends ScreenBase {
	/** The minimum zoom level. */
	private int minimumZoom = 2;
	/** The maximum zoom level. */
	private int zoomLevelCount = 14;
	/** The zoom step. */
	/** The current zoom index. The actual zoom is (minimumZoom + zoomIndex) / 4.0 . */
	private int zoomIndex = 0;
	/** The current horizontal pixel offset for the starmap contents. */
	private int xOffset;
	/** The current vertical pixel offset for the starmap contents. */
	private int yOffset;
	/** Option to hide the right panel. */
	private boolean rightPanelVisible = true;
	/** Option to hide the left panel. */
	private boolean bottomPanelVisible = true;
	/** Option to hide the minimap. */
	private boolean minimapVisible = true;
	/** Are the scrollbars visible? */
	private boolean scrollbarsVisible = true;
	/** The main starmap window's coordinates. */
	final Rectangle starmapWindow = new Rectangle();
	/** The rendering rectangle of the starmap. */
	final Rectangle starmapRect = new Rectangle();
	/** The minimap rectangle. */
	final Rectangle minimapRect = new Rectangle();
	/** The minimap's inner rectangle. */
	final Rectangle minimapInnerRect = new Rectangle();
	/** The current minimap viewport rectangle. */
	final Rectangle minimapViewportRect = new Rectangle();
	/** The minimap small image. */
	private BufferedImage minimapBackground;
	/** The visible list of planets. */
	private List<Planet> planets = new ArrayList<Planet>();
	/** The visible list of fleets. */
	private List<Fleet> fleets = new ArrayList<Fleet>();
	/** The current radar dot. */
	private BufferedImage radarDot;
	/** Show the radar? */
	private boolean showRadar = true;
	/** The rotation animation timer. */
	Timer rotationTimer;
	/** The starmap clipping rectangle. */
	Rectangle starmapClip;
	/** The scrollbar painter. */
	final ScrollBarPainter scrollbarPainter;
	/** The right panel rectangle. */
	final Rectangle rightPanel = new Rectangle();
	/** The bottom panel rectangle. */
	final Rectangle bottomPanel = new Rectangle();
	/** To blink the currently selected planet on the minimap. */
	boolean minimapPlanetBlink;
	/** The currently selected planet. */
	Planet currentPlanet;
	/** The currently selected fleet. */
	Fleet currentFleet;
	/** The blink counter. */
	int blinkCounter;
	/** Show fleets. */
	private boolean showFleets = true;
	/** Show navigation grid? */
	private boolean showGrid = true;
	/** Show star background? */
	private boolean showStars = true;
	/** Star rendering starting color. */
	private int startStars = 0x685CA4;
	/** Star rendering end color. */
	private int endStars = 0xFCFCFC;
	/** Number of stars per layer. */
	private static final int STAR_COUNT = 512;
	/** Number of layers. */
	private static final int STAR_LAYER_COUNT = 4;
	/** The divident ratio between the planet listing and the fleet listing. */
	private double planetFleetSplitter = 0.5;
	/** The planets listing entire subpanel. */
	final Rectangle planetsListPanel = new Rectangle();
	/** The fleets listing entire subpanel. */
	final Rectangle fleetsListPanel = new Rectangle();
	/** The zooming entire subpanel. */
	final Rectangle zoomingPanel = new Rectangle();
	/** The screen selection buttons subpanel. */
	final Rectangle buttonsPanel = new Rectangle();
	/** Planet fleet splitter. */
	final Rectangle planetFleetSplitterRect = new Rectangle();
	/** The planets/fleets splitter movement range. */
	final Rectangle planetFleetSplitterRange = new Rectangle();
	/** The planets listing. */
	final Rectangle planetsList = new Rectangle();
	/** The fleets listings. */
	final Rectangle fleetsList = new Rectangle();
	/** The planet scrolled index. */
	int planetsOffset;
	/** The fleets scroled index. */
	int fleetsOffset;
	/** A star object. */
	class Star {
		/** The star proportional position. */
		public double x;
		/** The star proportional position. */
		public double y;
		/** The star color. */
		public Color color;
	}
	/** The list of stars. */
	private List<Star> stars = new ArrayList<Star>();
	/** Button. */
	Button2 prevPlanet;
	/** Button. */
	Button2 nextPlanet;
	/** Button. */
	Button2 colony;
	/** Button. */
	Button2 prevFleet;
	/** Button. */
	Button2 nextFleet;
	/** Button. */
	Button2 equipment;
	/** Button. */
	Button2 info;
	/** Button. */
	Button2 bridge;
	/** The list of buttons. */
	final List<Button2> rightPanelButtons = new ArrayList<Button2>();
	/** In panning mode? */
	boolean panning;
	/** Mouse down. */
	boolean mouseDown;
	/** Moving the planets fleets splitter. */
	boolean pfSplitter;
	/** Last X. */
	int lastX;
	/** Last Y. */
	int lastY;
	/** Construct the screen. */
	public StarmapScreen() {
		scrollbarPainter = new ScrollBarPainter();
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#doResize()
	 */
	@Override
	public void doResize() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#finish()
	 */
	@Override
	public void finish() {
		rotationTimer.stop();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#initialize()
	 */
	@Override
	public void initialize() {
		rotationTimer = new Timer(75, new Act() {
			@Override
			public void act() {
				rotatePlanets();
				repaint();
			}
		});
		precalculateStars();
		
		int[] disabled = { 0xFF000000, 0xFF000000, 0, 0, 0xFF000000, 0, 0, 0, 0 };
		BufferedImage disabledPattern = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
		disabledPattern.setRGB(0, 0, 3, 3, disabled, 0, 3);

		prevPlanet = new Button2(commons.starmap.backwards[0], commons.starmap.backwards[1], disabledPattern);
		nextPlanet = new Button2(commons.starmap.forwards[0], commons.starmap.forwards[1], disabledPattern);
		prevFleet = new Button2(commons.starmap.backwards[0], commons.starmap.backwards[1], disabledPattern);
		nextFleet = new Button2(commons.starmap.forwards[0], commons.starmap.forwards[1], disabledPattern);
		colony = new Button2(commons.starmap.colony[0], commons.starmap.colony[1], disabledPattern);
		equipment = new Button2(commons.starmap.equipment[0], commons.starmap.equipment[1], disabledPattern);
		info = new Button2(commons.starmap.info[0], commons.starmap.info[1], disabledPattern);
		bridge = new Button2(commons.starmap.bridge[0], commons.starmap.bridge[1], disabledPattern);
		
		rightPanelButtons.clear();
		
		rightPanelButtons.add(prevPlanet);
		rightPanelButtons.add(nextPlanet);
		rightPanelButtons.add(prevFleet);
		rightPanelButtons.add(nextFleet);
		rightPanelButtons.add(colony);
		rightPanelButtons.add(equipment);
		rightPanelButtons.add(info);
		rightPanelButtons.add(bridge);
		
		prevPlanet.onClick = new Act() {
			@Override 
			public void act() {
				int idx = planets.indexOf(currentPlanet);
				if (idx > 0 && planets.size() > 0) {
					currentPlanet = planets.get(idx - 1);
					planetsOffset = limitScrollBox(idx - 1, planets.size(), planetsList.height, 10);
				}
			}
		};
		nextPlanet.onClick = new Act() {
			@Override
			public void act() {
				int idx = planets.indexOf(currentPlanet);
				if (idx + 1 < planets.size()) {
					currentPlanet = planets.get(idx + 1);
					planetsOffset = limitScrollBox(idx + 1, planets.size(), planetsList.height, 10);
				}
			}
		};
		prevFleet.onClick = new Act() {
			@Override 
			public void act() {
				int idx = fleets.indexOf(currentFleet);
				if (idx > 0 && fleets.size() > 0) {
					currentFleet = fleets.get(idx - 1);
					fleetsOffset = limitScrollBox(idx - 1, fleets.size(), fleetsList.height, 10);
				}
			}
		};
		nextFleet.onClick = new Act() {
			@Override
			public void act() {
				int idx = fleets.indexOf(currentFleet);
				if (idx + 1 < fleets.size()) {
					currentFleet = fleets.get(idx + 1);
					fleetsOffset = limitScrollBox(idx + 1, fleets.size(), fleetsList.height, 10);
				}
			}
		};

	}
	/**
	 * Rotate the planets on screen.
	 */
	protected void rotatePlanets() {
		for (Planet p : planets) {
			if (p.rotationDirection) {
				p.rotationPhase = (p.rotationPhase + 1) % p.type.body.length;
			} else {
				if (p.rotationPhase > 0) {
					p.rotationPhase = (p.rotationPhase - 1) % p.type.body.length;
				} else {
					p.rotationPhase = p.type.body.length - 1;
				}
			}
		}
		minimapPlanetBlink = blinkCounter < 5;
		blinkCounter = (blinkCounter + 1) % 10;
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
		if (panning) {
			if (starmapWindow.contains(x, y)) {
				int dx = x - lastX;
				int dy = y - lastY;
				
				pan(dx, dy);
				requestRepaint();
				lastX = x;
				lastY = y;
			}
		}
		if (mouseDown) {
			if (minimapVisible && minimapInnerRect.contains(x, y)) {
				scrollMinimapTo(x - minimapInnerRect.x, y - minimapInnerRect.y);
				requestRepaint();
			}
			if (pfSplitter && planetFleetSplitterRange.contains(x, y)) {
				planetFleetSplitter = 1.0 * (y - planetFleetSplitterRange.y) / (planetFleetSplitterRange.height);
				fleetsOffset = limitScrollBox(fleetsOffset, fleets.size(), fleetsList.height, 10);
				planetsOffset = limitScrollBox(planetsOffset, planets.size(), planetsList.height, 10);
				requestRepaint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mousePressed(int, int, int, int)
	 */
	@Override
	public void mousePressed(int button, int x, int y, int modifiers) {
		if (isRightButton(button)) {
			panning = true;
			lastX = x;
			lastY = y;
		}
		mouseDown = true;
		if (minimapVisible && minimapInnerRect.contains(x, y)) {
			scrollMinimapTo(x - minimapInnerRect.x, y - minimapInnerRect.y);
			requestRepaint();
		}
		if (isLeftButton(button)) { 
			if (starmapWindow.contains(x, y) && !isCtrl(modifiers) && !isShift(modifiers)) {
				currentPlanet = getPlanetAt(x, y);
				currentFleet = getFleetAt(x, y);
				requestRepaint();
			} else
			if (planetFleetSplitterRect.contains(x, y) && planetFleetSplitterRange.height > 0) {
				pfSplitter = true;
			}
			if (rightPanelVisible) {
				for (Button2 btn : rightPanelButtons) {
					if (btn.containsPoint(x, y)) {
						btn.down = true;
						requestRepaint();
						break;
					}
				}
				if (planetsList.contains(x, y)) {
					int idx = planetsOffset + (y - planetsList.y) / 10;
					if (idx < planets.size()) {
						currentPlanet = planets.get(idx);
					}
				}
				if (fleetsList.contains(x, y)) {
					int idx = fleetsOffset + (y - fleetsList.y) / 10;
					if (idx < fleets.size()) {
						currentFleet = fleets.get(idx);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseReleased(int, int, int, int)
	 */
	@Override
	public void mouseReleased(int button, int x, int y, int modifiers) {
		panning = false;
		mouseDown = false;
		pfSplitter = false;
		for (Button2 btn : rightPanelButtons) {
			if (btn.containsPoint(x, y)) {
				btn.click();
				requestRepaint();
			}
			if (btn.down) {
				btn.down = false;
				requestRepaint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseScrolled(int, int, int, int)
	 */
	@Override
	public void mouseScrolled(int direction, int x, int y, int modifiers) {
		if (isCtrl(modifiers) && starmapWindow.contains(x, y)) {
			if (direction < 0) {
				doZoomIn(x, y);
				requestRepaint();
			} else {
				doZoomOut(x, y);
				requestRepaint();
			}
		} else
		if (fleetsList.contains(x, y)) {
			if (direction < 0) {
				fleetsOffset--;
			} else {
				fleetsOffset++;
			}
			fleetsOffset = limitScrollBox(fleetsOffset, fleets.size(), fleetsList.height, 10);
		} else
		if (planetsList.contains(x, y)) {
			if (direction < 0) {
				planetsOffset--;
			} else {
				planetsOffset++;
			}
			planetsOffset = limitScrollBox(planetsOffset, planets.size(), planetsList.height, 10);
		}
	}
	/** Button with normal, pressed and disabled states. */
	class Button2 {
		/** The X coordinate. */
		public int x;
		/** The Y coordinate. */
		public int y;
		/** Is it down? */
		public boolean down;
		/** Enabled? */
		public boolean enabled = true;
		/** Normal image. */
		public BufferedImage normalImage;
		/** Down image. */
		public BufferedImage downImage;
		/** Disabled pattern. */
		public BufferedImage disabledPattern;
		/** The action to perform on clicking. */
		public Act onClick;
		/**
		 * Constructor.
		 * @param normal the normal image
		 * @param downImage the down image
		 * @param disabledPattern the disabled pattern
		 */
		public Button2(BufferedImage normal, BufferedImage downImage, BufferedImage disabledPattern) {
			this.normalImage = normal;
			this.downImage = downImage;
			this.disabledPattern = disabledPattern;
		}
		/**
		 * @return the width
		 */
		public int getWidth() {
			return normalImage.getWidth();
		}
		/**
		 * @return the height
		 */
		public int getHeight() {
			return normalImage.getHeight();
		}
		/**
		 * Render the button.
		 * @param g2 the graphics object
		 */
		public void paint(Graphics2D g2) {
			if (!enabled) {
				g2.drawImage(normalImage, x, y, null);
				TexturePaint tp = new TexturePaint(disabledPattern, new Rectangle(x, y, 3, 3));
				Paint sp = g2.getPaint();
				g2.setPaint(tp);
				g2.fillRect(x, y, normalImage.getWidth(), normalImage.getHeight());
				g2.setPaint(sp);
			} else
			if (down) {
				g2.drawImage(downImage, x, y, null);
			} else {
				g2.drawImage(normalImage, x, y, null);
			}
		}
		/**
		 * Check if the point is within this button.
		 * @param px the x coordinate
		 * @param py the y coordinate
		 * @return contains the point
		 */
		public boolean containsPoint(int px, int py) {
			return enabled && x <= px && y <= py && px < x + getWidth() && py < y + getHeight();
		}
		/** Execute the click action. */
		public void click() {
			if (onClick != null) {
				onClick.act();
			}
		}
	}
	/**
	 * Limit the scroll box offset value based on the size of the box and rowheight.
	 * @param offset the current offset
	 * @param count the total item count
	 * @param height the box height
	 * @param rowHeight the rowheight
	 * @return the corrected offset
	 */
	int limitScrollBox(int offset, int count, int height, int rowHeight) {
		int visibleRows = height / rowHeight;
		if (count <= visibleRows || offset <= 0) {
			return 0;
		}
		if (offset > count - visibleRows) {
			return count - visibleRows;
		}
		return offset; 
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter() {
		rotationTimer.start();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onLeave()
	 */
	@Override
	public void onLeave() {
		rotationTimer.stop();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#paintTo(java.awt.Graphics2D)
	 */
	@Override
	public void paintTo(Graphics2D g2) {

		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());

		computeRectangles();
//		// leave space for the status bars
//		g2.setColor(Color.RED);
//		g2.drawRect(starmapWindow.x, starmapWindow.y, starmapWindow.width - 1, starmapWindow.height - 1);
		
		
		Shape defaultClip = g2.getClip();
		g2.setClip(starmapClip);
		g2.drawImage(commons.starmap.background, starmapRect.x, starmapRect.y, starmapRect.width, starmapRect.height, null);
		
		double zoom = getZoom();
		
		if (showStars) {
			paintStars(g2, starmapRect, starmapClip);
		}
		
		if (showGrid) {
			paintGrid(g2, starmapRect);
		}
		
		// render radar circles
		if (showRadar) {
			for (Planet p : planets) {
				if (p.radar > 0) {
					paintRadar(g2, p.x, p.y, p.radar, zoom);
				}
			}
			if (showFleets) {
				for (Fleet f : fleets) {
					if (f.radar > 0) {
						paintRadar(g2, f.x, f.y, f.radar, zoom);
					}
				}
			}
		}

		for (Planet p : planets) {
			BufferedImage phase = p.type.body[p.rotationPhase];
			double d = p.diameter * zoom / 4;
			int di = (int)d;
			int x0 = (int)(starmapRect.x + p.x * zoom - d / 2);
			int y0 = (int)(starmapRect.y + p.y * zoom - d / 2);
			g2.drawImage(phase, x0, y0, (int)d, (int)d, null);
			
			int tw = commons.text.getTextWidth(5, p.name);
			int xt = (int)(starmapRect.x + p.x * zoom - tw / 2);
			int yt = (int)(starmapRect.y + p.y * zoom + d / 2) + 4;
			commons.text.paintTo(g2, xt, yt, 5, p.owner.color, p.name);
			if (p == currentPlanet) {
				g2.setColor(Color.WHITE);
				g2.drawLine(x0 - 1, y0 - 1, x0 + 2, y0 - 1);
				g2.drawLine(x0 - 1, y0 + di + 1, x0 + 2, y0 + di + 1);
				g2.drawLine(x0 + di - 2, y0 - 1, x0 + di + 1, y0 - 1);
				g2.drawLine(x0 + di - 2, y0 + di + 1, x0 + di + 1, y0 + di + 1);
				
				g2.drawLine(x0 - 1, y0 - 1, x0 - 1, y0 + 2);
				g2.drawLine(x0 + di + 1, y0 - 1, x0 + di + 1, y0 + 2);
				g2.drawLine(x0 - 1, y0 + di - 2, x0 - 1, y0 + di + 1);
				g2.drawLine(x0 + di + 1, y0 + di - 2, x0 + di + 1, y0 + di + 1);
			}
			if (p.quarantine && minimapPlanetBlink) {
				g2.setColor(Color.RED);
				g2.drawRect(x0 - 1, y0 - 1, 2 + (int)d, 2 + (int)d);
			}
			if (p.problems.size() > 0) {
				int w = p.problems.size() * 11 - 1;
				for (int i = 0; i < p.problems.size(); i++) {
					BufferedImage icon = null;
					switch (p.problems.get(i)) {
					case HOUSING:
						icon = commons.houseIcon;
						break;
					case FOOD:
						icon = commons.foodIcon;
						break;
					case HOSPITAL:
						icon = commons.hospitalIcon;
						break;
					case ENERGY:
						icon = commons.energyIcon;
						break;
					case WORKFORCE:
						icon = commons.workerIcon;
						break;
					default:
					}
					g2.drawImage(icon, (int)(starmapRect.x + p.x * zoom - w / 2 + i * 11), y0 - 13, null);
				}
			}
		}
		if (showFleets) {
			for (Fleet f : fleets) {
				int x0 = (int)(starmapRect.x + f.x * zoom - f.shipIcon.getWidth() / 2);
				int y0 = (int)(starmapRect.y + f.y * zoom - f.shipIcon.getHeight() / 2);
				g2.drawImage(f.shipIcon, x0, y0, null);
				int tw = commons.text.getTextWidth(5, f.name);
				int xt = (int)(starmapRect.x + f.x * zoom - tw / 2);
				int yt = (int)(starmapRect.y + f.y * zoom + f.shipIcon.getHeight() / 2) + 3;
				commons.text.paintTo(g2, xt, yt, 5, f.owner.color, f.name);
				if (f == currentFleet) {
					g2.setColor(Color.WHITE);
					g2.drawRect(x0 - 1, y0 - 1, f.shipIcon.getWidth() + 2, f.shipIcon.getHeight() + 2);
				}
			}
		}
		
		g2.setClip(defaultClip);

		// TODO panel rendering
		
		if (rightPanelVisible) {
			paintVertically(g2, rightPanel, commons.starmap.panelVerticalTop, commons.starmap.panelVerticalFill, commons.starmap.panelVerticalFill);
			
//			g2.setColor(Color.GRAY);
//			g2.fill(planetsListPanel);
//			g2.setColor(Color.LIGHT_GRAY);
//			g2.fill(fleetsListPanel);
////			g2.setColor(Color.YELLOW);
////			g2.fill(planetFleetSplitterRange);
			
			g2.drawImage(commons.starmap.panelVerticalSeparator, planetFleetSplitterRect.x, planetFleetSplitterRect.y, null);
			g2.drawImage(commons.starmap.panelVerticalSeparator, zoomingPanel.x, zoomingPanel.y - 2, null);
			g2.drawImage(commons.starmap.panelVerticalSeparator, buttonsPanel.x, buttonsPanel.y - 2, null);
			
			for (Button2 btn : rightPanelButtons) {
				btn.paint(g2);
			}
			Shape sp = g2.getClip();
			g2.setClip(planetsList);
			for (int i = planetsOffset; i < planets.size(); i++) {
				Planet p = planets.get(i);
				int color = TextRenderer.GREEN;
				if (p == currentPlanet) {
					color = TextRenderer.RED;
				}
				commons.text.paintTo(g2, planetsList.x + 3, planetsList.y + (i - planetsOffset) * 10 + 2, 7, color, p.name);
			}
			g2.setClip(fleetsList);
			for (int i = fleetsOffset; i < fleets.size(); i++) {
				Fleet p = fleets.get(i);
				int color = TextRenderer.GREEN;
				if (p == currentFleet) {
					color = TextRenderer.RED;
				}
				commons.text.paintTo(g2, fleetsList.x + 3, fleetsList.y + (i - fleetsOffset) * 10 + 2, 7, color, p.name);
			}
			
			
			g2.setClip(sp);
		}
		if (bottomPanelVisible) {
			paintHorizontally(g2, bottomPanel, commons.starmap.infoLeft, commons.starmap.infoRight, commons.starmap.infoFill);
		}
		
		if (scrollbarsVisible) {
			scrollbarPainter.paint(g2);
		}
		
		if (minimapVisible) {
			g2.drawImage(commons.starmap.minimap, minimapRect.x, minimapRect.y, null);
			g2.drawImage(minimapBackground, minimapInnerRect.x, minimapInnerRect.y, null);
			g2.setColor(Color.WHITE);
			g2.drawRect(minimapViewportRect.x, minimapViewportRect.y, minimapViewportRect.width - 1, minimapViewportRect.height - 1);
			g2.setClip(minimapInnerRect);
			// render planets
			for (Planet p : planets) {
				if (p != currentPlanet || minimapPlanetBlink) {
					int x0 = minimapInnerRect.x + (p.x * minimapInnerRect.width / commons.starmap.background.getWidth());
					int y0 = minimapInnerRect.y + (p.y * minimapInnerRect.height / commons.starmap.background.getHeight());
					g2.setColor(new Color(p.owner.color));
					g2.fillRect(x0 - 1, y0 - 1, 3, 3);
				}
			}
			g2.setClip(defaultClip);
		}
//		long frame = System.nanoTime() - fps;
//		fps = System.nanoTime();
//		commons.text.paintTo(g2, 10, 3, 14, TextRenderer.RED, getWidth() + " x " + getHeight() + " " + language + " Repaint/Seconds: " + String.format("%.3f", (1E9 / frame)));
	}
	@Override
	public void mouseDoubleClicked(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}
	/** Given the current panel visibility settings, set the map rendering coordinates. */
	void computeRectangles() {
		starmapWindow.x = 0;
		starmapWindow.y = 20;
		starmapWindow.width = getWidth();
		starmapWindow.height = getHeight() - 37;
		if (scrollbarsVisible) {
			starmapWindow.width -= commons.starmap.vScrollFill.getWidth();
			starmapWindow.height -= commons.starmap.hScrollFill.getHeight();
		}
		if (rightPanelVisible) {
			starmapWindow.width -= commons.starmap.panelVerticalFill.getWidth();
			if (scrollbarsVisible) {
				starmapWindow.width -= 3;
			}
		} else {
			if (scrollbarsVisible) {
				starmapWindow.width -= 1;
			}
		}
		if (bottomPanelVisible) {
			starmapWindow.height -= commons.starmap.infoFill.getHeight();
			if (scrollbarsVisible) {
				starmapWindow.height -= 3;
			}
		} else {
			if (scrollbarsVisible) {
				starmapWindow.height -= 1;
			}
		}

		minimapRect.x = getWidth() - commons.starmap.minimap.getWidth();
		minimapRect.y = getHeight() - commons.starmap.minimap.getHeight() - 17;
		minimapRect.width = commons.starmap.minimap.getWidth();
		minimapRect.height = commons.starmap.minimap.getHeight();

		int saveX = 0;
		int saveY = 0;
		if (minimapVisible) {
			if (!rightPanelVisible) {
				saveX += minimapRect.width + 1;
				if (scrollbarsVisible) {
					saveX -= commons.starmap.vScrollFill.getWidth() + 1;
				}
			} else {
				if (!scrollbarsVisible) {
					saveX += commons.starmap.vScrollFill.getWidth() + 3;
				}
			}
			if (!bottomPanelVisible) {
				saveY += minimapRect.height + 1;
				if (scrollbarsVisible) {
					saveY -= commons.starmap.hScrollFill.getHeight() + 1;
				}
			} else {
				if (!scrollbarsVisible) {
					saveY += commons.starmap.hScrollFill.getHeight() + 3;
				}
			}
		}
		rightPanel.x = getWidth() - commons.starmap.panelVerticalFill.getWidth();
		rightPanel.y = starmapWindow.y;
		rightPanel.width = commons.starmap.panelVerticalFill.getWidth();
		rightPanel.height = starmapWindow.height - saveY;

		bottomPanel.x = starmapWindow.x;
		bottomPanel.y = getHeight() - 18 - commons.starmap.infoFill.getHeight();
		bottomPanel.width = starmapWindow.width - saveX;
		bottomPanel.height = commons.starmap.infoFill.getHeight();

		scrollbarPainter.setBounds(starmapWindow, saveX, saveY);
		// ..............................................................
		// the right subpanels
		buttonsPanel.width = commons.starmap.panelVerticalFill.getWidth() - 4;
		buttonsPanel.height = commons.starmap.info[0].getHeight() + commons.starmap.bridge[0].getHeight() + 2;
		buttonsPanel.x = rightPanel.x + 2;
		buttonsPanel.y = rightPanel.y + rightPanel.height - buttonsPanel.height;
		
		zoomingPanel.width = buttonsPanel.width;
		zoomingPanel.height = commons.starmap.zoom[0].getHeight() + 2;
		zoomingPanel.x = buttonsPanel.x;
		zoomingPanel.y = buttonsPanel.y - zoomingPanel.height - 2;
		
		planetFleetSplitterRange.x = zoomingPanel.x;
		planetFleetSplitterRange.width = zoomingPanel.width;
		planetFleetSplitterRange.y = rightPanel.y + 16 + commons.starmap.backwards[0].getHeight()
			+ commons.starmap.colony[0].getHeight();
		planetFleetSplitterRange.height = zoomingPanel.y - planetFleetSplitterRange.y
			- 16 - commons.starmap.backwards[0].getHeight() - commons.starmap.equipment[0].getHeight();
		
		planetFleetSplitterRect.x = planetFleetSplitterRange.x;
		planetFleetSplitterRect.width = planetFleetSplitterRange.width;
		planetFleetSplitterRect.height = 4;
		planetFleetSplitterRect.y = (int)(planetFleetSplitterRange.y + (planetFleetSplitter * planetFleetSplitterRange.height));

		planetsListPanel.x = zoomingPanel.x;
		planetsListPanel.y = rightPanel.y + 2;
		planetsListPanel.width = zoomingPanel.width;
		planetsListPanel.height = planetFleetSplitterRect.y - planetsListPanel.y;
		
		fleetsListPanel.x = planetsListPanel.x;
		fleetsListPanel.y = planetsListPanel.y + planetsListPanel.height + 2;
		fleetsListPanel.width = planetsListPanel.width;
		fleetsListPanel.height = zoomingPanel.y - planetFleetSplitterRect.y - 4;
		
		prevPlanet.x = planetsListPanel.x + 2;
		prevPlanet.y = planetsListPanel.y + 1;
		nextPlanet.x = planetsListPanel.x + prevPlanet.getWidth() + 4;
		nextPlanet.y = planetsListPanel.y + 1;

		prevFleet.x = fleetsListPanel.x + 2;
		prevFleet.y = fleetsListPanel.y + 1;
		nextFleet.x = fleetsListPanel.x + prevFleet.getWidth() + 4;
		nextFleet.y = fleetsListPanel.y + 1;

		colony.x = planetsListPanel.x + 1;
		colony.y = planetsListPanel.y + planetsListPanel.height - colony.getHeight() - 1;

		equipment.x = fleetsListPanel.x + 1;
		equipment.y = fleetsListPanel.y + fleetsListPanel.height - equipment.getHeight() - 1;
		
		info.x = buttonsPanel.x + 1;
		info.y = buttonsPanel.y + 1;
		bridge.x = buttonsPanel.x + 1;
		bridge.y = info.y + info.getHeight() + 1;
		
		if (planets.size() > 0) {
			int idx = planets.indexOf(currentPlanet);
			prevPlanet.enabled = idx > 0;
			nextPlanet.enabled = idx + 1 < planets.size();
		} else {
			prevPlanet.enabled = false;
			nextPlanet.enabled = false;
		}
		
		if (fleets.size() > 0) {
			int idx = fleets.indexOf(currentFleet);
			prevFleet.enabled = idx > 0;
			nextFleet.enabled = idx + 1 < fleets.size();
		} else {
			prevFleet.enabled = false;
			nextFleet.enabled = false;
		}

		// ..............................................................

		planetsList.x = planetsListPanel.x;
		planetsList.y = planetsListPanel.y + prevPlanet.getHeight() + 1;
		planetsList.width = planetsListPanel.width;
		planetsList.height = colony.y - planetsList.y;

		fleetsList.x = fleetsListPanel.x;
		fleetsList.y = fleetsListPanel.y + prevFleet.getHeight() + 1;
		fleetsList.width = fleetsListPanel.width;
		fleetsList.height = equipment.y - fleetsList.y;

		
		
		// TODO fleet and planet listings
		// ..............................................................
		minimapRect.x = getWidth() - commons.starmap.minimap.getWidth();
		minimapRect.y = getHeight() - commons.starmap.minimap.getHeight() - 17;
		minimapRect.width = commons.starmap.minimap.getWidth();
		minimapRect.height = commons.starmap.minimap.getHeight();
//		if ((!rightPanelVisible || ! bottomPanelVisible)) {
//			minimapRect.x = starmapWindow.x + starmapWindow.width - gfx.minimap.getWidth();
//			minimapRect.y = starmapWindow.y + starmapWindow.height - gfx.minimap.getHeight();
//		}
		
		minimapInnerRect.setBounds(minimapRect);
		minimapInnerRect.x += 2;
		minimapInnerRect.y += 2;
		minimapInnerRect.width -= 4;
		minimapInnerRect.height -= 3;
		// ..............................................................

		computeViewport();
	}
	/**
	 * Compute the current viewport's coordinates.
	 */
	private void computeViewport() {
		double zoom = getZoom();

		starmapRect.width = (int)(commons.starmap.background.getWidth() * zoom);
		starmapRect.height = (int)(commons.starmap.background.getHeight() * zoom);
		
		if (starmapRect.width < starmapWindow.width) {
			xOffset = -(starmapWindow.width - starmapRect.width) / 2;
		}
		if (starmapRect.height < starmapWindow.height) {
			yOffset = -(starmapWindow.height - starmapRect.height) / 2;
		}
		starmapRect.x = starmapWindow.x - xOffset;
		starmapRect.y = starmapWindow.y - yOffset;
		
		// center if smaller
		limitOffsets();
		// ..............................................................
		int miniw = minimapInnerRect.width;
		int minih = minimapInnerRect.height;
		
		int x2 = 0;
		if (xOffset < 0) {
			minimapViewportRect.x = 0;
			x2 = miniw;
		}  else {
			minimapViewportRect.x = miniw * xOffset / starmapRect.width;
			x2 = miniw * (xOffset + starmapWindow.width) / starmapRect.width;
		}
		int y2 = 0;
		if (yOffset < 0) {
			minimapViewportRect.y = 0;
			y2 = minih;
		} else {
			minimapViewportRect.y = minih * yOffset / starmapRect.height;
			y2 = minih * (yOffset + starmapWindow.height) / starmapRect.height;
		}
		
		if (x2 > miniw) {
			x2 = miniw;
		}
		if (y2 > minih) {
			y2 = minih;
		}
		
		
		minimapViewportRect.width = x2 - minimapViewportRect.x;
		minimapViewportRect.height = y2 - minimapViewportRect.y;
		minimapViewportRect.x += minimapRect.x + 2;
		minimapViewportRect.y += minimapRect.y + 2;
		
		starmapClip = starmapWindow.intersection(starmapRect);
	}
	/**
	 * Paint the multiple layer of stars.
	 * @param g2 the graphics object
	 * @param rect the target rectanlge
	 * @param view the viewport rectangle
	 */
	private void paintStars(Graphics2D g2, Rectangle rect, Rectangle view) {
		int starsize = zoomIndex < zoomLevelCount / 2.5 ? 1 : 2;
		double xf = (view.x - rect.x) * 1.0 / rect.width;
		double yf = (view.y - rect.y) * 1.0 / rect.height;
		Color last = null;
		for (int i = 0; i < stars.size(); i++) {
			Star s = stars.get(i);
			double layer = 0.9 - (i / STAR_COUNT) * 0.10;
			double w = rect.width * layer;
			double h = rect.height * layer;
			double lx = rect.x;
			double ly = rect.y;
			
			
			int x = (int)(lx + xf * (rect.width - w) + s.x * rect.width);
			int y = (int)(ly + yf * (rect.height - h) + s.y * rect.height);
			if (starmapClip.contains(x, y)) {
				if (last != s.color) {
					g2.setColor(s.color);
					last = s.color;
				}
				g2.fillRect(x, y, starsize, starsize);
			}
		}
	}
	/**
	 * Paint a 5x5 grid on the given rectanlge.
	 * @param g2 the graphics object
	 * @param rect the rectangle
	 */
	private void paintGrid(Graphics2D g2, Rectangle rect) {
		g2.setColor(commons.starmap.gridColor);
		Stroke st = g2.getStroke();
		//FIXME the dotted line rendering is somehow very slow
//		g2.setStroke(gfx.gridStroke);
		
		float fw = rect.width;
		float fh = rect.height;
		float dx = fw / 5;
		float dy = fh / 5;
		float y0 = dy;
		float x0 = dx;
		for (int i = 1; i < 5; i++) {
			g2.drawLine((int)(rect.x + x0), rect.y, (int)(rect.x + x0), (int)(rect.y + fh));
			g2.drawLine(rect.x, (int)(rect.y + y0), (int)(rect.x + fw), (int)(rect.y + y0));
			x0 += dx;
			y0 += dy;
		}
		int i = 0;
		y0 = dy - 6;
		x0 = 2;
		for (char c = 'A'; c < 'Z'; c++) {
			commons.text.paintTo(g2, (int)(rect.x + x0), (int)(rect.y + y0), 5, TextRenderer.GRAY, String.valueOf(c));
			x0 += dx;
			i++;
			if (i % 5 == 0) {
				x0 = 2;
				y0 += dy;
			}
		}
		
		g2.setStroke(st);
		
	}
	/**
	 * Paint a horizontal resizable area from images.
	 * @param g2 the graphics
	 * @param origin the bounding rectangle
	 * @param left the left image
	 * @param right the right image
	 * @param fill the filler between the two images
	 */
	static void paintHorizontally(Graphics2D g2, Rectangle origin, BufferedImage left, BufferedImage right, BufferedImage fill) {
		g2.drawImage(left, origin.x, origin.y, null);
		g2.drawImage(right, origin.x + origin.width - right.getWidth(), origin.y, null);
		Paint pt = g2.getPaint();
	    g2.setPaint(new TexturePaint(fill, 
	    		new Rectangle(origin.x + left.getWidth(), origin.y
	    				, fill.getWidth(), fill.getHeight())));
	    g2.fillRect(origin.x + left.getWidth(), origin.y, 
	    		origin.width - left.getWidth() - right.getWidth(), fill.getHeight());
	    g2.setPaint(pt);
	}
	/**
	 * Paint a vertically resizable area from images.
	 * @param g2 the graphics
	 * @param origin the bounding rectangle
	 * @param top the left image
	 * @param bottom the right image
	 * @param fill the filler between the two images
	 */
	static void paintVertically(Graphics2D g2, Rectangle origin, BufferedImage top, BufferedImage bottom, BufferedImage fill) {
		g2.drawImage(top, origin.x, origin.y, null);
		g2.drawImage(bottom, origin.x, origin.y + origin.height - bottom.getHeight(), null);
		Paint pt = g2.getPaint();
	    
	    g2.setPaint(new TexturePaint(fill, 
	    		new Rectangle(origin.x, origin.y  + top.getHeight()
	    				, fill.getWidth(), fill.getHeight())));
	    
	    g2.fillRect(origin.x, origin.y + top.getHeight(), 
	    		origin.width, origin.height - top.getHeight() - bottom.getHeight());
	    
	    g2.setPaint(pt);
	}
	/** The horizontal/vertical scrollbar painter. */
	public class ScrollBarPainter {
		/** Horizontal scroll rectangle. */
		final Rectangle hscrollRect = new Rectangle();
		/** Horizontal scroll inner rectangle. */
		final Rectangle hscrollInnerRect = new Rectangle();
		/** Horizontal scrollknob rectangle. */
		final Rectangle hscrollKnobRect = new Rectangle();
		/** Vertical scroll rectangle. */
		final Rectangle vscrollRect = new Rectangle();
		/** Vertical scroll inner rectangle. */
		final Rectangle vscrollInnerRect = new Rectangle();
		/** Vertical scrollknob rectangle. */
		final Rectangle vscrollKnobRect = new Rectangle();
		/**
		 * Reposition the scrollbar graphics.
		 * @param rectangle the window to enclose
		 * @param saveX how many pixels to save on the horizontal scrollbar's right side
		 * @param saveY how many pixels to save on the vertical scrollbar's bottom side
		 */
		public void setBounds(Rectangle rectangle, int saveX, int saveY) {
			hscrollRect.x = rectangle.x;
			hscrollRect.y = rectangle.y + rectangle.height + 1;
			hscrollRect.width = rectangle.width - saveX;
			hscrollRect.height = commons.starmap.hScrollFill.getHeight();
			
			vscrollRect.x = rectangle.x + rectangle.width + 1;
			vscrollRect.y = rectangle.y;
			vscrollRect.width = commons.starmap.vScrollFill.getWidth();
			vscrollRect.height = rectangle.height - saveY;
			
			hscrollInnerRect.setLocation(hscrollRect.x + 2, hscrollRect.y + 2);
			hscrollInnerRect.setSize(hscrollRect.width - 4, hscrollRect.height - 4);
			vscrollInnerRect.setLocation(vscrollRect.x + 2, vscrollRect.y + 2);
			vscrollInnerRect.setSize(vscrollRect.width - 4, vscrollRect.height - 4);
		}
		/**
		 * Paint the scrollbars to the given graphics.
		 * @param g2 the graphics
		 */
		public void paint(Graphics2D g2) {
			paintHorizontally(g2, hscrollRect, commons.starmap.hScrollLeft, commons.starmap.hScrollRight, commons.starmap.hScrollFill);
			paintVertically(g2, vscrollRect, commons.starmap.vScrollTop, commons.starmap.vScrollBottom, commons.starmap.vScrollFill);
		}
	}
	/**
	 * Paint the radar circle.
	 * @param g2 the graphics object.
	 * @param x the center coordinate
	 * @param y the center coordinate
	 * @param radius the radius
	 * @param zoom the zoom factor
	 */
	void paintRadar(Graphics2D g2, int x, int y, int radius, double zoom) {
		double angle = 0;
		int n = (int)(2 * radius * Math.PI * zoom / 10);
		double dangle = Math.PI * 2 / n;
		while (angle < 2 * Math.PI) {
			
			double rx = (x + Math.cos(angle) * radius) * zoom + starmapRect.x;
			double ry = (y + Math.sin(angle) * radius) * zoom + starmapRect.y;
			
			g2.drawImage(radarDot, (int)rx, (int)ry, null);
			
			angle += dangle;
		}
	}
	/**
	 * @return computes the current zoom factor.
	 */
	public double getZoom() {
		return (minimumZoom + zoomIndex) / 4.0;
	}
	/**
	 * @param rightPanelVisible the rightPanelVisible to set
	 */
	public void setRightPanelVisible(boolean rightPanelVisible) {
		this.rightPanelVisible = rightPanelVisible;
	}
	/**
	 * @return the rightPanelVisible
	 */
	public boolean isRightPanelVisible() {
		return rightPanelVisible;
	}
	/**
	 * @param bottomPanelVisible the bottomPanelVisible to set
	 */
	public void setBottomPanelVisible(boolean bottomPanelVisible) {
		this.bottomPanelVisible = bottomPanelVisible;
	}
	/**
	 * @return the bottomPanelVisible
	 */
	public boolean isBottomPanelVisible() {
		return bottomPanelVisible;
	}
	/**
	 * @param minimapVisible the minimapVisible to set
	 */
	public void setMinimapVisible(boolean minimapVisible) {
		this.minimapVisible = minimapVisible;
	}
	/**
	 * @return the minimapVisible
	 */
	public boolean isMinimapVisible() {
		return minimapVisible;
	}
	/**
	 * @param scrollbarsVisible the scrollbarsVisible to set
	 */
	public void setScrollbarsVisible(boolean scrollbarsVisible) {
		this.scrollbarsVisible = scrollbarsVisible;
	}
	/**
	 * @return the scrollbarsVisible
	 */
	public boolean isScrollbarsVisible() {
		return scrollbarsVisible;
	}
	/**
	 * Pan the starmap with the given pixels.
	 * @param dx the horizontal difference
	 * @param dy the vertical difference
	 */
	public void pan(int dx, int dy) {
		xOffset -= dx;
		yOffset -= dy;
		// limit the offset
		limitOffsets();
	}
	/**
	 * Zoom in and keep try to keep the given pixel under the mouse.
	 * @param x the X coordinate within the starmapWindow
	 * @param y the Y coordinate within the starmapWindow
	 */
	public void doZoomIn(int x, int y) {
		if (zoomIndex < zoomLevelCount) {
			readjustZoom(x, y, zoomIndex + 1);
		}
	}
	/**
	 * @param x the mouse coordinate
	 * @param y the mouse coordinate
	 * @param newIndex the new zoom index
	 */
	private void readjustZoom(int x, int y, int newIndex) {
		double pre = getZoom();
		
		double vx = (x - starmapRect.x) / pre;
		double vy = (y - starmapRect.y) / pre;
		
		zoomIndex = newIndex;
		
		double post = getZoom();
		
		selectRadarDot();
		limitOffsets();
		computeViewport();
		
		double vx2 = (x - starmapRect.x) / post;
		double vy2 = (y - starmapRect.y) / post;
		
		xOffset -= (vx2 - vx) * post;
		yOffset -= (vy2 - vy) * post;
		
		limitOffsets();
		computeViewport();

	}
	/**
	 * Select the radar dot image for the current zoom level.
	 */
	void selectRadarDot() {
		radarDot = commons.starmap.radarDots[commons.starmap.radarDots.length * zoomIndex / (zoomLevelCount + 1)];
	}
	/**
	 * Zoom out and keep try to keep the given pixel under the mouse.
	 * @param x the X coordinate within the starmapWindow
	 * @param y the Y coordinate within the starmapWindow
	 */
	public void doZoomOut(int x, int y) {
		if (zoomIndex > 0) {
			readjustZoom(x, y, zoomIndex - 1);
		}
	}
	/**
	 * Scroll to the given minimap relative coordinate.
	 * @param x the click on the minimap
	 * @param y the click on the minimap
	 */
	public void scrollMinimapTo(int x, int y) {
		xOffset = x * starmapRect.width / minimapInnerRect.width - starmapWindow.width / 2;
		yOffset = y * starmapRect.height / minimapInnerRect.height - starmapWindow.height / 2;
		limitOffsets();
		computeViewport();
	}
	/** 
	 * Get a planet at the given absolute location. 
	 * @param x the absolute x
	 * @param y the absolute y
	 * @return a planet or null if not found
	 */
	public Planet getPlanetAt(int x, int y) {
		double zoom = getZoom();
		for (Planet p : planets) {
			double d = p.diameter * zoom / 4;
			int di = (int)d;
			int x0 = (int)(starmapRect.x + p.x * zoom - d / 2);
			int y0 = (int)(starmapRect.y + p.y * zoom - d / 2);
			if (x0 <= x && x <= x0 + di && y0 <= y && y <= y0 + di) {
				return p;
			}
		}
		return null;
	}
	/** 
	 * Get a planet at the given absolute location. 
	 * @param x the absolute x
	 * @param y the absolute y
	 * @return a planet or null if not found
	 */
	public Fleet getFleetAt(int x, int y) {
		double zoom = getZoom();
		for (Fleet f : fleets) {
			int w = f.shipIcon.getWidth();
			int h = f.shipIcon.getHeight();
			int x0 = (int)(starmapRect.x + f.x * zoom - w * 0.5);
			int y0 = (int)(starmapRect.y + f.y * zoom - h * 0.5);
			if (x0 <= x && x <= x0 + w && y0 <= y && y <= y0 + h) {
				return f;
			}
		}
		return null;
	}
	/**
	 * Precalculates the star background locations and colors.
	 */
	private void precalculateStars() {
		Random random = new Random(0);
		Color[] colors = new Color[8];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = new Color(commons.starmap.mixColors(startStars, endStars, random.nextFloat()));
		}
		for (int i = 0; i < STAR_COUNT * STAR_LAYER_COUNT; i++) {
			Star s = new Star();
			s.x = random.nextDouble();
			s.y = random.nextDouble();
			s.color = colors[random.nextInt(colors.length)];
			stars.add(s);
		}
		Collections.sort(stars, new Comparator<Star>() {
			@Override
			public int compare(Star o1, Star o2) {
				int c1 = o1.color.getRGB() & 0xFFFFFF;
				int c2 = o2.color.getRGB() & 0xFFFFFF;
				return c1 - c2;
			}
		});
	}
	/** Limit the offsets how far they can go. */
	public void limitOffsets() {
		int maxX = starmapRect.width - starmapWindow.width;
		int maxY = starmapRect.height - starmapWindow.height;
		if (xOffset > maxX) {
			xOffset = maxX;
		}
		if (xOffset < 0) {
			xOffset = 0;
		}
		if (yOffset > maxY) {
			yOffset = maxY;
		}
		if (yOffset < 0) {
			yOffset = 0;
		}
	}
}
