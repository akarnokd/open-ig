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
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Modifier;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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
	public void onResize() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#finish()
	 */
	@Override
	public void onFinish() {
		rotationTimer.stop();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#initialize()
	 */
	@Override
	public void onInitialize() {
		rotationTimer = new Timer(75, new Act() {
			@Override
			public void act() {
				rotatePlanets();
				askRepaint();
			}
		});
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

	@Override
	public boolean keyboard(KeyEvent e) {
		boolean rep = false;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			pan(0, -30);
			rep = true;
			break;
		case KeyEvent.VK_DOWN:
			pan(0, 30);
			rep = true;
			break;
		case KeyEvent.VK_LEFT:
			pan(-30, 0);
			rep = true;
			break;
		case KeyEvent.VK_RIGHT:
			pan(30, 0);
			rep = true;
			break;
		default:
		}
		return rep;
	}

	@Override
	public boolean mouse(UIMouse e) {
		boolean rep = false;
		switch (e.type) {
		case MOVE:
		case DRAG:
			if (panning) {
				if (starmapWindow.contains(e.x, e.y)) {
					int dx = e.x - lastX;
					int dy = e.y - lastY;
					
					pan(dx, dy);
					lastX = e.x;
					lastY = e.y;
					rep = true;
				}
			}
			if (mouseDown) {
				if (minimapVisible && minimapInnerRect.contains(e.x, e.y)) {
					scrollMinimapTo(e.x - minimapInnerRect.x, e.y - minimapInnerRect.y);
					rep = true;
				}
				if (pfSplitter && planetFleetSplitterRange.contains(e.x, e.y)) {
					planetFleetSplitter = 1.0 * (e.y - planetFleetSplitterRange.y) / (planetFleetSplitterRange.height);
					fleetsOffset = limitScrollBox(fleetsOffset, fleets.size(), fleetsList.height, 10);
					planetsOffset = limitScrollBox(planetsOffset, planets.size(), planetsList.height, 10);
					rep = true;
				}
			}
			break;
		case DOWN:
			if (e.has(Button.RIGHT)) {
				panning = true;
				lastX = e.x;
				lastY = e.y;
			}
			mouseDown = true;
			if (minimapVisible && minimapInnerRect.contains(e.x, e.y)) {
				scrollMinimapTo(e.x - minimapInnerRect.x, e.y - minimapInnerRect.y);
				rep = true;
			}
			if (e.has(Button.LEFT)) { 
				if (starmapWindow.contains(e.x, e.y) 
						&& !e.has(Modifier.CTRL) && !e.has(Modifier.SHIFT)) {
					currentPlanet = getPlanetAt(e.x, e.y);
					currentFleet = getFleetAt(e.x, e.y);
					rep = true;
				} else
				if (planetFleetSplitterRect.contains(e.x, e.y) && planetFleetSplitterRange.height > 0) {
					pfSplitter = true;
				}
				if (rightPanelVisible) {
					for (Button2 btn : rightPanelButtons) {
						if (btn.containsPoint(e.x, e.y)) {
							btn.down = true;
							rep = true;
							break;
						}
					}
					if (planetsList.contains(e.x, e.y)) {
						int idx = planetsOffset + (e.y - planetsList.y) / 10;
						if (idx < planets.size()) {
							currentPlanet = planets.get(idx);
						}
					}
					if (fleetsList.contains(e.x, e.y)) {
						int idx = fleetsOffset + (e.y - fleetsList.y) / 10;
						if (idx < fleets.size()) {
							currentFleet = fleets.get(idx);
						}
					}
				}
			}
			break;
		case UP:
			panning = false;
			mouseDown = false;
			pfSplitter = false;
			for (Button2 btn : rightPanelButtons) {
				if (btn.containsPoint(e.x, e.y)) {
					btn.click();
					rep = true;
				}
				if (btn.down) {
					btn.down = false;
					rep = true;
				}
			}
			break;
		case WHEEL:
			if (e.has(Modifier.CTRL) && starmapWindow.contains(e.x, e.y)) {
				if (e.z < 0) {
					doZoomIn(e.x, e.y);
					rep = true;
				} else {
					doZoomOut(e.x, e.y);
					rep = true;
				}
			} else
			if (fleetsList.contains(e.x, e.y)) {
				if (e.z < 0) {
					fleetsOffset--;
				} else {
					fleetsOffset++;
				}
				fleetsOffset = limitScrollBox(fleetsOffset, fleets.size(), fleetsList.height, 10);
			} else
			if (planetsList.contains(e.x, e.y)) {
				if (e.z < 0) {
					planetsOffset--;
				} else {
					planetsOffset++;
				}
				planetsOffset = limitScrollBox(planetsOffset, planets.size(), planetsList.height, 10);
			}
			break;
		default:
		}
		return rep;
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
	public void draw(Graphics2D g2) {

		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, width, height);

		computeRectangles();
//		// leave space for the status bars
//		g2.setColor(Color.RED);
//		g2.drawRect(starmapWindow.x, starmapWindow.y, starmapWindow.width - 1, starmapWindow.height - 1);
		
		
		Shape defaultClip = g2.getClip();
		g2.setClip(starmapClip);
		g2.drawImage(commons.starmap.background, starmapRect.x, starmapRect.y, starmapRect.width, starmapRect.height, null);
		
		double zoom = getZoom();
		
		if (showStars) {
			RenderTools.paintStars(g2, starmapRect, starmapClip, starmapClip, zoomIndex, zoomLevelCount);
		}
		
		if (showGrid) {
			RenderTools.paintGrid(g2, starmapRect, commons.starmap.gridColor, commons.text);
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
	}
	/** Given the current panel visibility settings, set the map rendering coordinates. */
	void computeRectangles() {
		starmapWindow.x = 0;
		starmapWindow.y = 20;
		starmapWindow.width = width;
		starmapWindow.height = height - 37;
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

		minimapRect.x = width - commons.starmap.minimap.getWidth();
		minimapRect.y = height - commons.starmap.minimap.getHeight() - 17;
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
		rightPanel.x = getSwingWidth() - commons.starmap.panelVerticalFill.getWidth();
		rightPanel.y = starmapWindow.y;
		rightPanel.width = commons.starmap.panelVerticalFill.getWidth();
		rightPanel.height = starmapWindow.height - saveY;

		bottomPanel.x = starmapWindow.x;
		bottomPanel.y = height - 18 - commons.starmap.infoFill.getHeight();
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
		minimapRect.x = width - commons.starmap.minimap.getWidth();
		minimapRect.y = height - commons.starmap.minimap.getHeight() - 17;
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
