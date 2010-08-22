/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.gui;

import hu.openig.v1.core.Act;
import hu.openig.v1.core.Labels;
import hu.openig.v1.core.PlanetType;
import hu.openig.v1.core.ResourceLocator;
import hu.openig.v1.gfx.StarmapGFX;
import hu.openig.v1.model.Fleet;
import hu.openig.v1.model.Planet;
import hu.openig.v1.model.Player;
import hu.openig.v1.render.TextRenderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * The test version of the starmap renderer.
 * @author karnokd, 2010.08.21.
 * @version $Revision 1.0$
 */
public class StarmapPainter extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3263402820975161594L;
	/** The graphics objects. */
	private final StarmapGFX gfx;
	/** The resource locator. */
	private final ResourceLocator rl;
	/** The labels. */
	private final Labels labels;
	/** The text renderer. */
	private final TextRenderer txt;
	/** The current language. */
	private String language;
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
	/** The mouse event handler wrapper. */
	class MouseHandler extends MouseAdapter {
		/** In panning mode? */
		boolean panning;
		/** Last X. */
		int lastX;
		/** Last Y. */
		int lastY;
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isControlDown() && starmapWindow.contains(e.getPoint())) {
				if (e.getUnitsToScroll() < 0) {
					doZoomIn(e.getX(), e.getY());
					repaint();
				} else {
					doZoomOut(e.getX(), e.getY());
					repaint();
				}
			}
		};
		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				panning = true;
				lastX = e.getX();
				lastY = e.getY();
			}
			if (minimapVisible && minimapInnerRect.contains(e.getPoint())) {
				scrollMinimapTo(e.getX() - minimapInnerRect.x, e.getY() - minimapInnerRect.y);
				repaint();
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			panning = false;
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			if (panning) {
				if (starmapWindow.contains(e.getPoint())) {
					int dx = e.getX() - lastX;
					int dy = e.getY() - lastY;
					
					pan(dx, dy);
					repaint();
					lastX = e.getX();
					lastY = e.getY();
				}
			}
			if (minimapVisible && minimapInnerRect.contains(e.getPoint())) {
				scrollMinimapTo(e.getX() - minimapInnerRect.x, e.getY() - minimapInnerRect.y);
				repaint();
			}
		}
	};
	/**
	 * @param rl the resource locator
	 * @param language the language
	 */
	public StarmapPainter(ResourceLocator rl, String language) {
		this.rl = rl;
		this.language = language;
		gfx = new StarmapGFX();
		labels = new Labels();
		txt = new TextRenderer(rl);
		MouseHandler handler = new MouseHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);
		addMouseWheelListener(handler);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				computeRectangles();
				limitOffsets();
				repaint();
			}
		});
		
		loadTestImages();
		
		rotationTimer = new Timer(100, new Act() {
			@Override
			public void act() {
				rotatePlanets();
				repaint();
			}
		});
		rotationTimer.start();
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
	}
	/**
	 * Load some test images usually used in game mode.
	 */
	void loadTestImages() {
		changeLanguage(language);
		
		// create a minimal version of the image
		minimapBackground = new BufferedImage(gfx.minimap.getWidth() - 4, gfx.minimap.getHeight() - 3, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = minimapBackground.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(gfx.background, 0, 0, minimapBackground.getWidth(), minimapBackground.getHeight(), null);
		g2.dispose();
		
		// create test objects
		
		Planet p = new Planet();
		p.x = 500;
		p.y = 400;
		p.radar = 25;
		p.type = new PlanetType();
		p.type.body = rl.getAnimation(language, "starmap/planets/planet_earth_30x30", 30, -1);
		p.name = "Earth";
		p.owner = new Player();
		p.owner.color = TextRenderer.ORANGE;
		
		planets.add(p);
		
		Fleet f = new Fleet();
		f.x = 550;
		f.y = 400;
		f.radar = 15;
		f.shipIcon = rl.getImage(language, "starmap/fleets/human_fleet");
		f.owner = p.owner;
		f.name = "Fleet";
		fleets.add(f);
		
		selectRadarDot();
	}
	/**
	 * Change the language of the displayed screen.
	 * @param newLanguage the new language
	 */
	public void changeLanguage(String newLanguage) {
		gfx.load(rl, newLanguage);
		labels.load(rl, newLanguage, "campaign/main");
		language = newLanguage;
		repaint();
	}
	/** Given the current panel visibility settings, set the map rendering coordinates. */
	void computeRectangles() {
		starmapWindow.x = 0;
		starmapWindow.y = 20;
		starmapWindow.width = getWidth();
		starmapWindow.height = getHeight() - 38;
		if (rightPanelVisible) {
			starmapWindow.width -= gfx.fleetsFill.getWidth();
		}
		if (bottomPanelVisible) {
			starmapWindow.height -= gfx.infoFill.getHeight();
		}
		if (scrollbarsVisible) {
			starmapWindow.width -= gfx.vScrollFill.getWidth();
			starmapWindow.height -= gfx.hScrollFill.getHeight();
		}
		// ..............................................................
		minimapRect.x = getWidth() - gfx.minimap.getWidth();
		minimapRect.y = getHeight() - gfx.minimap.getHeight() - 18;
		minimapRect.width = gfx.minimap.getWidth();
		minimapRect.height = gfx.minimap.getHeight();
		
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

		starmapRect.width = (int)(gfx.background.getWidth() * zoom);
		starmapRect.height = (int)(gfx.background.getHeight() * zoom);
		
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
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		txt.paintTo(g2, 10, 3, 14, TextRenderer.RED, getWidth() + " x " + getHeight() + " " + language);
		
		computeRectangles();
		// leave space for the status bars
		g2.setColor(Color.RED);
		g2.drawRect(starmapWindow.x, starmapWindow.y, starmapWindow.width - 1, starmapWindow.height - 1);
		
		g2.setColor(Color.BLACK);
		g2.fill(starmapWindow);
		
		Shape defaultClip = g2.getClip();
		g2.setClip(starmapClip);
		g2.drawImage(gfx.background, starmapRect.x, starmapRect.y, starmapRect.width, starmapRect.height, null);
		
		double zoom = getZoom();
		
		// render radar circles
		if (showRadar) {
			for (Planet p : planets) {
				if (p.radar > 0) {
					paintRadar(g2, p.x, p.y, p.radar, zoom);
				}
			}
			for (Fleet f : fleets) {
				if (f.radar > 0) {
					paintRadar(g2, f.x, f.y, f.radar, zoom);
				}
			}
		}

		for (Planet p : planets) {
			BufferedImage phase = p.type.body[p.rotationPhase];
			double d = phase.getWidth() * zoom / 4;
			int x0 = (int)(starmapRect.x + p.x * zoom - d / 2);
			int y0 = (int)(starmapRect.y + p.y * zoom - d / 2);
			g2.drawImage(phase, x0, y0, (int)d, (int)d, null);
			
			int tw = txt.getTextWidth(5, p.name);
			int xt = (int)(starmapRect.x + p.x * zoom - tw / 2);
			int yt = (int)(starmapRect.y + p.y * zoom + d / 2) + 3;
			txt.paintTo(g2, xt, yt, 5, p.owner.color, p.name);
		}
		for (Fleet f : fleets) {
			int x0 = (int)(starmapRect.x + f.x * zoom - f.shipIcon.getWidth() / 2);
			int y0 = (int)(starmapRect.y + f.y * zoom - f.shipIcon.getHeight() / 2);
			g2.drawImage(f.shipIcon, x0, y0, null);
			int tw = txt.getTextWidth(5, f.name);
			int xt = (int)(starmapRect.x + f.x * zoom - tw / 2);
			int yt = (int)(starmapRect.y + f.y * zoom + f.shipIcon.getHeight() / 2) + 3;
			txt.paintTo(g2, xt, yt, 5, f.owner.color, f.name);
		}
		
		g2.setClip(defaultClip);

		if (minimapVisible) {
			g2.drawImage(gfx.minimap, minimapRect.x, minimapRect.y, null);
			g2.drawImage(minimapBackground, minimapInnerRect.x, minimapInnerRect.y, null);
			g2.setColor(Color.WHITE);
			g2.drawRect(minimapViewportRect.x, minimapViewportRect.y, minimapViewportRect.width - 1, minimapViewportRect.height - 1);
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
		radarDot = gfx.radarDots[gfx.radarDots.length * zoomIndex / (zoomLevelCount + 1)];
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
	/** Stop any timers. */
	public void stop() {
		rotationTimer.stop();
	}
}
