package hu.openig.gfx;

import hu.openig.core.InfoBarRegions;
import hu.openig.core.Tile;
import hu.openig.sound.UISounds;
import hu.openig.utils.PACFile.PACEntry;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Planet surface renderer class.
 * @author karnokd, 2009.01.16.
 * @version $Revision 1.0$
 */
public class PlanetRenderer extends JComponent implements MouseListener, MouseMotionListener, 
MouseWheelListener, ActionListener {
	/** */
	private static final long serialVersionUID = -2113448032455145733L;
	Rectangle tilesToHighlight;
	int xoff = 56;
	int yoff = 27;
	int lastx;
	int lasty;
	boolean panMode;
	byte[] mapBytes;
	int surfaceVariant = 1;
	float scale = 1.0f;
	int surfaceType = 1;
	/** Empty surface map array. */
	private final byte[] EMPTY_SURFACE_MAP = new byte[65 * 65 * 2 + 4];
	/** The planet graphics. */
	private final PlanetGFX gfx;
	/** The common graphics. */
	private final CommonGFX cgfx;
	
	private Rectangle buildingButtonRect = new Rectangle();
	private Rectangle radarButtonRect = new Rectangle();
	private Rectangle leftTopRect = new Rectangle();
	private Rectangle leftFillerRect = new Rectangle();
	private Rectangle leftBottomRect = new Rectangle();
	
	private Rectangle buildingInfoButtonRect = new Rectangle();
	private Rectangle screenButtonRect = new Rectangle();
	private Rectangle rightTopRect = new Rectangle();
	private Rectangle rightFillerRect = new Rectangle();
	private Rectangle rightBottomRect = new Rectangle();
	
	private Rectangle colonyInfoRect = new Rectangle();
	private Rectangle planetRect = new Rectangle();
	private Rectangle starmapRect = new Rectangle();
	private Rectangle bridgeRect = new Rectangle();
	/** The middle window for the surface drawing. */
	private Rectangle mainWindow = new Rectangle();
	
	private Rectangle buildPanelRect = new Rectangle();
	private Rectangle radarPanelRect = new Rectangle();
	private Rectangle buildingInfoPanelRect = new Rectangle();
	
	/** The last width. */
	private int lastWidth;
	/** The last height. */
	private int lastHeight;
	/** The left filler painter. */
	private TexturePaint leftFillerPaint;
	/** The right filler painter. */
	private TexturePaint rightFillerPaint;
	
	private boolean showControlButtons = true;
	private boolean showBuild = true;
	private boolean showRadar = true;
	private boolean showBuildingInfo = true;
	/** Is the colony info button pressed? */
	private boolean colonyInfoDown;
	/** Is the military info button pressed? */
	private boolean planetDown;
	/** Is the starmap button pressed? */
	private boolean starmapDown;
	/** Is the bridge button pressed? */
	private boolean bridgeDown;
	/** 
	 * The timer to scroll the building window if the user holds down the left mouse button on the
	 * up/down arrow.
	 */
	private Timer buildScroller;
	/** The scroll interval. */
	private static final int BUILD_SCROLL_INTERVAL = 500;
	/** Timer used to animate fade in-out. */
	private Timer fadeTimer;
	/** Fade timer interval. */
	private static final int FADE_INTERVAL = 50;
	/** The alpha difference to use when animating the fadeoff-fadein. */
	private static final float ALPHA_DELTA = 0.1f;
	/** THe fade direction is up (true) or down (false). */
	private boolean fadeDirection;
	/** The current darkening factor for the entire UI. 0=No darkness, 1=Full darkness. */
	private float darkness = 0f;
	/** The daylight factor for the planetary surface only. 0=No darkness, 1=Full darkness. */
	private float daylight = 0.5f;
	/** The text renderer. */
	private TextGFX text;
	/** Regions of the info bars. */
	public InfoBarRegions infoBarRects = new InfoBarRegions();
	/** The user interface sounds. */
	private UISounds uiSound;
	/**
	 * Constructor, expecting the planet graphics and the common graphics objects.
	 * @param gfx
	 * @param cgfx
	 * @throws IOException
	 */
	public PlanetRenderer(PlanetGFX gfx, CommonGFX cgfx, UISounds uiSound) throws IOException {
		this.gfx = gfx;
		this.cgfx = cgfx;
		this.text = cgfx.text;
		this.uiSound = uiSound;
		buildScroller = new Timer(BUILD_SCROLL_INTERVAL, this);
		buildScroller.setActionCommand("BUILD_SCROLLER");
		fadeTimer = new Timer(FADE_INTERVAL, this);
		fadeTimer.setActionCommand("FADE");
		changeSurface();
		
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addMouseListener(this);
		setOpaque(true);
	}
	private PACEntry getSurface(int surfaceType, int variant) {
		String mapName = "MAP_" + (char)('A' + (surfaceType - 1)) + variant + ".MAP";
		return gfx.getMap(mapName);
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		int w = getWidth();
		int h = getHeight();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, w, h);

		if (w != lastWidth || h != lastHeight) {
			lastWidth = w;
			lastHeight = h;
			// if the render window changes, re-zoom to update scrollbars
			updateRegions();
		}
		AffineTransform t = g2.getTransform();
		g2.scale(scale, scale);
		int k = 0;
		int j = 0;
		// RENDER VERTICALLY
		int k0 = 0;
		int j0 = 0;
		Map<Integer, Tile> surface = gfx.getSurfaceTiles(surfaceType);
		for (int i = 0; i < 65 * 65; i++) {
			int ii = (mapBytes[2 * i + 4] & 0xFF) - (surfaceType < 7 ? 41 : 84);
			int ff = mapBytes[2 * i + 5] & 0xFF;
			Tile tile = surface.get(ii);
			if (tile != null) {
				// 1x1 tiles can be drawn from top to bottom
				if (tile.width == 1 && tile.height == 1) {
					int x = xoff + Tile.toScreenX(k, j);
					int y = yoff + Tile.toScreenY(k, j);
					if (x >= -tile.image.getWidth() && x <= (int)(getWidth() / scale)
							&& y >= -tile.image.getHeight() && y <= (int)(getHeight() / scale) + tile.image.getHeight()) {
						g2.drawImage(tile.image, x, y - tile.image.getHeight() + tile.heightCorrection, null);
					}
				} else 
				if (ff < 255) {
					// multi spanning tiles should be cut into small rendering piece for the current strip
					// ff value indicates the stripe count
					// the entire image would be placed using this bottom left coordinate
					int j1 = ff >= tile.width ? j + tile.width - 1: j + ff;
					int k1 = ff >= tile.width ? k + (tile.width - 1 - ff): k;
					int j2 = ff >= tile.width ? j : j - (tile.width - 1 - ff);
					int x = xoff + Tile.toScreenX(k1, j1);
					int y = yoff + Tile.toScreenY(k1, j2);
					// use subimage stripe
					int x0 = ff >= tile.width ? Tile.toScreenX(ff, 0) : Tile.toScreenX(0, -ff);
					BufferedImage subimage = tile.strips[ff];
					g2.drawImage(subimage, x + x0, y - tile.image.getHeight() + tile.heightCorrection, null);
//					r.x = x;
//					r.y = y - tile.image.getHeight();
//					r.width = tile.image.getWidth();
//					r.height = tile.image.getHeight();
				}
			}				
			k--;
			j--;
			k0++;
			if (k0 > 64) {
				k0 = 0;
				j0++;
				j = - (j0 / 2);
				k = ((j0 - 1) / 2 + 1);
			}
		}
		Composite comp = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, daylight));
		g2.setColor(Color.BLACK);
		g2.fill(mainWindow);
		g2.setComposite(comp);
		
		if (tilesToHighlight != null) {
			drawIntoRect(g2, gfx.getFrame(0), tilesToHighlight);
		}
		g2.setTransform(t);
		// RENDER INFOBARS
		cgfx.renderInfoBars(this, g2);
		// RENDER LEFT BUTTONS
		g2.drawImage(gfx.buildingButton, buildingButtonRect.x, buildingButtonRect.y, null);
		g2.setColor(Color.BLACK);
		g2.drawLine(buildingButtonRect.width, buildingButtonRect.y, buildingButtonRect.width, buildingButtonRect.y + buildingButtonRect.height - 1);
		
		g2.drawImage(gfx.leftTop, leftTopRect.x, leftTopRect.y, null);
		if (leftFillerRect.height > 0) {
			Paint p = g2.getPaint();
			g2.setPaint(leftFillerPaint);
			g2.fill(leftFillerRect);
			g2.setPaint(p);
		}
		g2.drawImage(gfx.leftBottom, leftBottomRect.x, leftBottomRect.y, null);
		g2.drawLine(radarButtonRect.width, radarButtonRect.y, radarButtonRect.width, radarButtonRect.y + radarButtonRect.height - 1);
		g2.drawImage(gfx.radarButton, radarButtonRect.x, radarButtonRect.y, null);
		
		// RENDER RIGHT BUTTONS
		g2.drawImage(gfx.buildingInfoButton, buildingInfoButtonRect.x, buildingInfoButtonRect.y, null);
		g2.drawImage(gfx.rightTop, rightTopRect.x, rightTopRect.y, null);
		if (rightFillerRect.height > 0) {
			Paint p = g2.getPaint();
			g2.setPaint(rightFillerPaint);
			g2.fill(rightFillerRect);
			g2.setPaint(p);
		}
		g2.drawImage(gfx.rightBottom, rightBottomRect.x, rightBottomRect.y, null);
		g2.drawImage(gfx.screenButtons, screenButtonRect.x, screenButtonRect.y, null);
		
		if (showControlButtons) {
			if (colonyInfoDown) {
				g2.drawImage(gfx.colonyInfoButtonDown, colonyInfoRect.x, colonyInfoRect.y, null);
			} else {
				g2.drawImage(gfx.colonyInfoButton, colonyInfoRect.x, colonyInfoRect.y, null);
			}
			if (planetDown) {
				g2.drawImage(gfx.planetButtonDown, planetRect.x, planetRect.y, null);
			} else {
				g2.drawImage(gfx.planetButton, planetRect.x, planetRect.y, null);
			}
			if (starmapDown) {
				g2.drawImage(gfx.starmapButtonDown, starmapRect.x, starmapRect.y, null);
			} else {
				g2.drawImage(gfx.starmapButton, starmapRect.x, starmapRect.y, null);
			}
			if (bridgeDown) {
				g2.drawImage(gfx.bridgeButtonDown, bridgeRect.x, bridgeRect.y, null);
			} else {
				g2.drawImage(gfx.bridgeButton, bridgeRect.x, bridgeRect.y, null);
			}
		}
		if (showBuild) {
			g2.drawImage(gfx.buildPanel, buildPanelRect.x, buildPanelRect.y, null);
		}
		if (showBuildingInfo) {
			g2.drawImage(gfx.buildingInfoPanel, buildingInfoPanelRect.x, buildingInfoPanelRect.y, null);
		}
		if (showRadar) {
			g2.drawImage(gfx.radarPanel, radarPanelRect.x, radarPanelRect.y, null);
		}
		Shape sp = g2.getClip();
		g2.clip(infoBarRects.topInfoArea);
		text.paintTo(g2, infoBarRects.topInfoArea.x, infoBarRects.topInfoArea.y + 1, 14, 0xFFFFFFFF, "Surface: " + surfaceType + ", Variant: " + surfaceVariant);
		g2.setClip(sp);
		
		// now darken the entire screen
		comp = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, darkness));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, w, h);
		g2.setComposite(comp);
	}
	/**
	 * Update location of various interresting rectangles of objects.
	 */
	private void updateRegions() {
		
		cgfx.updateRegions(this, infoBarRects);
		
		buildingButtonRect.x = 0;
		buildingButtonRect.y = cgfx.top.left.getHeight();
		buildingButtonRect.width = gfx.buildingButton.getWidth();
		buildingButtonRect.height = gfx.buildingButton.getHeight();
		
		leftTopRect.x = 0;
		leftTopRect.y = buildingButtonRect.y + buildingButtonRect.height;
		leftTopRect.width = gfx.leftTop.getWidth();
		leftTopRect.height = gfx.leftTop.getHeight();
		
		radarButtonRect.x = 0;
		radarButtonRect.y = getHeight() - cgfx.bottom.left.getHeight() - gfx.radarButton.getHeight();
		radarButtonRect.width = gfx.radarButton.getWidth();
		radarButtonRect.height = gfx.radarButton.getHeight();
		
		leftBottomRect.x = 0;
		leftBottomRect.y = radarButtonRect.y - gfx.leftBottom.getHeight();
		leftBottomRect.width = gfx.leftBottom.getWidth();
		leftBottomRect.height = gfx.leftBottom.getHeight();
		
		leftFillerRect.x = 0;
		leftFillerRect.y = leftTopRect.y + leftTopRect.height;
		leftFillerRect.width = gfx.leftFiller.getWidth();
		leftFillerRect.height = leftBottomRect.y - leftFillerRect.y;
		if (leftFillerPaint == null) {
			leftFillerPaint = new TexturePaint(gfx.leftFiller, leftFillerRect);
		}
		
		buildingInfoButtonRect.x = getWidth() - gfx.buildingInfoButton.getWidth();
		buildingInfoButtonRect.y = cgfx.top.left.getHeight();
		buildingInfoButtonRect.width = gfx.buildingInfoButton.getWidth();
		buildingInfoButtonRect.height = gfx.buildingInfoButton.getHeight();
		
		rightTopRect.x = buildingInfoButtonRect.x;
		rightTopRect.y = buildingInfoButtonRect.y + buildingInfoButtonRect.height;
		rightTopRect.width = gfx.rightTop.getWidth();
		rightTopRect.height = gfx.rightTop.getHeight();
		
		screenButtonRect.x = buildingInfoButtonRect.x;
		screenButtonRect.y = getHeight() - cgfx.bottom.left.getHeight() - gfx.screenButtons.getHeight();
		screenButtonRect.width = gfx.screenButtons.getWidth();
		screenButtonRect.height = gfx.screenButtons.getHeight();
		
		rightBottomRect.x = buildingInfoButtonRect.x;
		rightBottomRect.y = screenButtonRect.y - gfx.rightBottom.getHeight();
		rightBottomRect.width = gfx.rightBottom.getWidth();
		rightBottomRect.height = gfx.rightBottom.getHeight();
		
		rightFillerRect.x = buildingInfoButtonRect.x;
		rightFillerRect.y = rightTopRect.y + gfx.rightTop.getHeight();
		rightFillerRect.width = gfx.rightFiller.getWidth();
		rightFillerRect.height = rightBottomRect.y - rightFillerRect.y;
		
		rightFillerPaint = new TexturePaint(gfx.rightFiller, rightFillerRect);
		
		// BOTTOM RIGHT CONTROL BUTTONS
		
		bridgeRect.x = getWidth() - gfx.rightBottom.getWidth() - gfx.bridgeButton.getWidth();
		bridgeRect.y = getHeight() - cgfx.bottom.right.getHeight() - gfx.bridgeButton.getHeight();
		bridgeRect.width = gfx.bridgeButton.getWidth();
		bridgeRect.height = gfx.bridgeButton.getHeight();
		
		starmapRect.x = bridgeRect.x - gfx.starmapButton.getWidth();
		starmapRect.y = bridgeRect.y;
		starmapRect.width = gfx.starmapButton.getWidth();
		starmapRect.height = gfx.starmapButton.getHeight();
		
		planetRect.x = starmapRect.x - gfx.planetButton.getWidth();
		planetRect.y = bridgeRect.y;
		planetRect.width = gfx.planetButton.getWidth();
		planetRect.height = gfx.planetButton.getHeight();

		colonyInfoRect.x = planetRect.x - gfx.colonyInfoButton.getWidth();
		colonyInfoRect.y = bridgeRect.y;
		colonyInfoRect.width = gfx.colonyInfoButton.getWidth();
		colonyInfoRect.height = gfx.colonyInfoButton.getHeight();
		
		mainWindow.x = buildingButtonRect.width + 1;
		mainWindow.y = buildingButtonRect.y;
		mainWindow.width = buildingInfoButtonRect.x - mainWindow.x;
		mainWindow.height = radarButtonRect.y + radarButtonRect.height - mainWindow.y;
		
		buildPanelRect.x = mainWindow.x - 1;
		buildPanelRect.y = mainWindow.y;
		buildPanelRect.width = gfx.buildPanel.getWidth();
		buildPanelRect.height = gfx.buildPanel.getHeight();
		
		buildingInfoPanelRect.x = mainWindow.x + mainWindow.width - gfx.buildingInfoPanel.getWidth();
		buildingInfoPanelRect.y = mainWindow.y;
		buildingInfoPanelRect.width = gfx.buildingInfoPanel.getWidth();
		buildingInfoPanelRect.height = gfx.buildingInfoPanel.getHeight();
		
		radarPanelRect.x = buildPanelRect.x;
		radarPanelRect.y = mainWindow.y + mainWindow.height - gfx.radarPanel.getHeight();
		radarPanelRect.width = gfx.radarPanel.getWidth();
		radarPanelRect.height = gfx.radarPanel.getHeight();
		
	}
	/**
	 * Changes the surface type and variant so the next rendering pass will use that.
	 */
	private void changeSurface() {
		PACEntry e = getSurface(surfaceType, surfaceVariant);
		if (e != null) {
			mapBytes = e.data;
		} else {
			mapBytes = EMPTY_SURFACE_MAP;
		}
	}
	/**
	 * Converts the tile x and y coordinates to map offset.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return the map offset
	 */
	public int toMapOffset(int x, int y) {
		return (x - y) * 65 + (x - y + 1) / 2 - x;
	}
	/**
	 * Fills the given rectangular tile area with the specified tile image.
	 * @param g2
	 * @param image
	 * @param rect
	 */
	private void drawIntoRect(Graphics2D g2, BufferedImage image, Rectangle rect) {
		for (int j = rect.y; j < rect.y + rect.height; j++) {
			for (int k = rect.x; k < rect.x + rect.width; k++) {
				int x = xoff + Tile.toScreenX(k, j); 
				int y = yoff + Tile.toScreenY(k, j); 
				g2.drawImage(image, x - 1, y - image.getHeight(), null);
			}
		}
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		if (panMode) {
			xoff -= (lastx - e.getX());
			yoff -= (lasty - e.getY());
			lastx = e.getX();
			lasty = e.getY();
			repaint();
		}
	}
	/**
	 * Returns true if the mouse event is within the
	 * visible area of the main window (e.g not over
	 * the panels or buttons).
	 * @param e
	 * @return
	 */
	private boolean eventInMainWindow(MouseEvent e) {
		Point pt = e.getPoint();
		return mainWindow.contains(pt) 
		&& (!showBuild || !buildPanelRect.contains(pt))
		&& (!showRadar || !radarPanelRect.contains(pt))
		&& (!showBuildingInfo || !buildingInfoPanelRect.contains(pt))
		&& (!showControlButtons || (
				!colonyInfoRect.contains(pt)
				&& !planetRect.contains(pt)
				&& !starmapRect.contains(pt)
				&& !bridgeRect.contains(pt)
		));

	}
	@Override
	public void mouseMoved(MouseEvent e) {
		if (eventInMainWindow(e)) {
			int x = e.getX() - xoff - 27;
			int y = e.getY() - yoff + 1;
			int a = (int)Math.floor(Tile.toTileX(x, y));
			int b = (int)Math.floor(Tile.toTileY(x, y));
			tilesToHighlight = new Rectangle(a, b, 1, 1);
			repaint();
		}
	}
	public void mousePressed(MouseEvent e) {
		Point pt = e.getPoint(); 
		if (e.getButton() == MouseEvent.BUTTON3 && eventInMainWindow(e)) {
			lastx = e.getX();
			lasty = e.getY();
			panMode = true;
		} else
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (eventInMainWindow(e)) {
				int x = e.getX() - xoff - 27;
				int y = e.getY() - yoff + 1;
				int a = (int)Math.floor(Tile.toTileX(x, y));
				int b = (int)Math.floor(Tile.toTileY(x, y));
				int offs = this.toMapOffset(a, b);
				int val = offs >= 0 && offs < 65 * 65 ? mapBytes[offs * 2 + 4] & 0xFF : 0;
				System.out.printf("%d, %d -> %d, %d%n", a, b, offs, val);
			} else
			if (showControlButtons) {
				if (colonyInfoRect.contains(pt)) {
					colonyInfoDown = true;
					repaint();
				}
				if (planetRect.contains(pt)) {
					planetDown = true;
					repaint();
				}
				if (starmapRect.contains(pt)) {
					starmapDown = true;
					repaint();
				}
				if (bridgeRect.contains(pt)) {
					bridgeDown = true;
					repaint();
				}
			}
		}
	}
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			panMode = false;
		} else
		if (e.getButton() == MouseEvent.BUTTON1) {
			colonyInfoDown = false;
			planetDown = false;
			starmapDown = false;
			bridgeDown = false;
			buildScroller.stop();
			repaint();
		}
	}
	boolean once = true;
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!e.isControlDown() && !e.isAltDown()) {
			if (e.getWheelRotation() > 0 & surfaceVariant < 9) {
				surfaceVariant++;
			} else 
			if (e.getWheelRotation() < 0 && surfaceVariant > 1){
				surfaceVariant--;
			}
			changeSurface();
		} else 
		if (e.isControlDown()) {
			if (e.getWheelRotation() < 0 & scale < 32) {
				scale *= 2;
			} else 
			if (e.getWheelRotation() > 0 && scale > 1f/32){
				scale /= 2;
			}
		} else
		if (e.isAltDown()) {
			if (e.getWheelRotation() < 0 && surfaceType > 1) {
				surfaceType--;
			} else
			if (e.getWheelRotation() > 0 && surfaceType < 7) {
				surfaceType++;
			}
			changeSurface();
		}
		repaint();
	}
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (e.getClickCount() == 1) {
				Point pt = e.getPoint();
				if (buildingButtonRect.contains(pt)) {
					showBuild = !showBuild;
					repaint();
				} else
				if (radarButtonRect.contains(pt)) {
					showRadar = !showRadar;
					repaint();
				} else
				if (buildingInfoButtonRect.contains(pt)) {
					showBuildingInfo = !showBuildingInfo;
					repaint();
				} else
				if (screenButtonRect.contains(pt)) {
					showControlButtons = !showControlButtons;
					repaint();
				} else
				if (showControlButtons && starmapRect.contains(pt)) {
					fadeDirection = false;
					fadeTimer.start();
				}
			}
		}
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("FADE".equals(e.getActionCommand())) {
			doFade();
		} else
		if ("BUILD_SCROLLER".equals(e.getActionCommand())) {
			doBuildScroller();
		}
	}
	/** Execute the fade animation. */
	private void doFade() {
		if (!fadeDirection) {
			darkness = Math.max(0.0f, Math.min(1.0f, darkness + ALPHA_DELTA));
			if (darkness >= 0.999f) {
				fadeTimer.stop();
				doFadeCompleted();
			}
		} else {
			darkness = Math.max(0.0f, Math.min(1.0f, darkness - ALPHA_DELTA));
			if (darkness <= 0.001f) {
				fadeTimer.stop();
				doFadeCompleted();
			}
		}
		repaint();
	}
	/**
	 * Invoked when the fading operation is completed.
	 */
	private void doFadeCompleted() {
		darkness = 0f;
	}
	private void doBuildScroller() {
		
	}
}