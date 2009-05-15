/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Btn;
import hu.openig.core.BtnAction;
import hu.openig.model.GameFleet;
import hu.openig.model.GamePlanet;
import hu.openig.model.GamePlayer;
import hu.openig.model.GameWorld;
import hu.openig.sound.UISounds;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.Timer;
/**
 * Component that renders the starmap.
 * @author karnokd
 */
public class StarmapRenderer extends JComponent implements MouseMotionListener, MouseListener, MouseWheelListener {
	/** The serial version UID. */
	private static final long serialVersionUID = -4832071241159647010L;
	/** The graphics objects. */
	private final StarmapGFX gfx;
	/** The area of the horizontal scrollbar. */
	private final Rectangle hscrollRect = new Rectangle();
	/** The horizontal scrollbar knobs location. */
	private final Rectangle hknobRect = new Rectangle();
	/** The area of the vertical scrollbar. */
	private final Rectangle vscrollRect = new Rectangle();
	/** The vertical scrollbar knob location. */
	private final Rectangle vknobRect = new Rectangle();
	/** The position of the ship control rectangle. */
	private final Rectangle shipControlRect = new Rectangle();
	/** The position of the minimap rectangle. */
	private final Rectangle minimapRect = new Rectangle();
	/** The position of the main map rectangle. */
	private final Rectangle mapRect = new Rectangle();
	/** The width when the last time the component was rendered. */
	private int lastWidth = -1;
	/** The height when the last time the component was rendered. */
	private int lastHeight = -1;
	/** The maximum value of the vertical scroller. Minimum is always 0. */
	private int vscrollMax = 0;
	/** Current pixel value of the vertical scrollbar. */
	private float vscrollValue = 0;
	/** The maximum value of the horizontal scroller. Minimum is always 0. */
	private int hscrollMax = 0;
	/** Current pixel value of the horizontal scrollbar. */
	private float hscrollValue = 0;
	/** Set to true if the user performs the map movement via the mouse. */
	private boolean mapDragMode;
	/** The last mouse coordinate. */
	private int lastMouseX;
	/** The last mouse coordinate. */
	private int lastMouseY;
	/** The actual pixel offset for the background map. */
//		private int mapX;
	/** The actual pixel offset for the background map. */
//		private int mapY;
	/** The vertical drag mode is on. */
	private boolean verticalDragMode;
	/** The horizontal drag mode is on. */
	private boolean horizontalDragMode;
	/** The horizontal scroll factor. */
	private float hscrollFactor;
	/** The vertical scroll factor. */
	private float vscrollFactor;
	/** The common graphics objects. */
	private final CommonGFX cgfx;
	/** Bottom left rectangle. */
	private final Rectangle bottomLeftRect = new Rectangle();
	/** Bottom filler rectangle. */
	private final Rectangle bottomFillerRect = new Rectangle();
	/** Bottom right rectangle. */
	private final Rectangle bottomRightRect = new Rectangle();
	/** Right top rectangle. */
	private final Rectangle rightTopRect = new Rectangle();
	/** Right filler rectangle. */
	private final Rectangle rightFillerRect = new Rectangle();
	/** Right bottom rectangle. */
	private final Rectangle rightBottomRect = new Rectangle();
	/** Colony previous button. */
	private Btn btnColonyPrev;
	/** COlony next button. */
	private Btn btnColonyNext;
	/** The colonies text area. */
	private final Rectangle colonies = new Rectangle();
	/** Colony button. */
	private Btn btnColony;
	/** Equipment button. */
	private Btn btnEquipment;
	/** Equipment prev button. */
	private Btn btnEquipmentPrev;
	/** Equipment next button. */
	private Btn btnEquipmentNext;
	/** The equipment text area. */
	private Rectangle equipments = new Rectangle();
	/** Info button. */
	private Btn btnInfo;
	/** Bridge button. */
	private Btn btnBridge;
	/** Magnify button. */
	private Btn btnMagnify;
	/** The various buttons. */
	private final List<Btn> buttons = new ArrayList<Btn>();
	/** The magnification direction: true-in, false-out. */
	private boolean magnifyDirection;
	/** The magnification factors. */
	private int[] magnifyFactors = { 15, 17, 20,  24, 30, 34,  40, 48, 60,  75, 90, 105, 120 };
	/** The planet sizes for magnify factors. */
	private int[] planetSizes = { 5, 6, 7,  8, 9, 10,  12, 13, 15,  17, 20, 24, 30 };
	/** Radar dot size selector for various zoom levels. */
	private int[] radarDotSizes = { 0, 0, 0,  1, 1, 1,  2, 2, 2,  3, 3, 3, 3 };
	/** The current magnification index. */
	private int magnifyIndex = magnifyFactors.length - 1;
	/** Determines zoom factor where 1.0 means the original size, 0.5 the half size. */
	private float zoomFactor = magnifyFactors[magnifyIndex] / 30f;
	/** Show ship controls. */
	private boolean showShipControls;
	/** Colonize button. */
	private Btn btnColonize;
	/** Radars button. */
	private Btn btnRadars;
	/** Fleets button. */
	private Btn btnFleets;
	/** Grids button. */
	private Btn btnGrids;
	/** Stars button. */
	private Btn btnStars;
	/** Name button. */
	private Btn btnName;
	/** Ship move button. */
	private Btn btnMove;
	/** Ship attack button. */
	private Btn btnAttack;
	/** Ship stop buttom. */
	private Btn btnStop;
	/** The display name modes: None, Colony, Fleets, Both. */
	private int nameMode = 3;
	/** Buttons which change state on click.*/
	private final List<Btn> toggleButtons = new ArrayList<Btn>();
	/** The mouse wheel scrolling amount. */
	private static final int SCROLL_AMOUNT = 20;
	/** Satellite button. */
	private Btn btnSatellite;
	/** Spy satellite 1 button. */
	private Btn btnSpySat1;
	/** Spy satellite 2 button. */
	private Btn btnSpySat2;
	/** Hubble 2 button. */
	private Btn btnHubble2;
	/** Show satellites. */
	public boolean showSatellites = true;
	/** The user interface sounds. */
	private final UISounds uiSound;
	/** The text renderer. */
	private final TextGFX text;
	/** Action for colony clicked. */
	private BtnAction onColonyClicked;
	/** Action for information clicked. */
	private BtnAction onInformationClicked;
	/** Timer used to animate fade in-out. */
	private Timer fadeTimer;
	/** Fade timer interval. */
	private static final int FADE_INTERVAL = 25;
	/** The alpha difference to use when animating the fadeoff-fadein. */
	private static final float ALPHA_DELTA = 0.15f;
	/** THe fade direction is up (true) or down (false). */
	private boolean fadeDirection;
	/** The current darkening factor for the entire UI. 0=No darkness, 1=Full darkness. */
	private float darkness = 0f;
	/** The map coordinates. */
	public final Rectangle mapCoords = new Rectangle();
	/** The animation timer. */
	private Timer animations;
	/** The animation interval. */
	private static final int ANIMATION_INTERVAL = 100;
	/**
	 * The game world object.
	 */
	private GameWorld gameWorld;
	/** The information bar renderer. */
	private InfobarRenderer infobarRenderer;
	/** The planet listing scroll offset. */
	private int planetListOffset;
	/** Star rendering starting color. */
	private int startStars = 0x685CA4;
	/** Star rendering end color. */
	private int endStars = 0xFCFCFC;
	/** Number of stars per layer. */
	private static final int STAR_COUNT = 1000;
	/** Number of layers. */
	private static final int STAR_LAYER_COUNT = 4;
	/** Precalculated star coordinates. */
	private int[] starsX = new int[STAR_COUNT * STAR_LAYER_COUNT];
	/** Precalculated star coordinates. */
	private int[] starsY = new int[STAR_COUNT * STAR_LAYER_COUNT];
	/** Precalculated star colors. */
	private int[] starsColors = new int[STAR_COUNT * STAR_LAYER_COUNT];
	/**
	 * Constructor. Sets the helper object fields.
	 * @param gfx the starmap graphics object
	 * @param cgfx the common graphics object
	 * @param uiSound the user interface
	 * @param infobarRenderer the information bar renderer
	 */
	public StarmapRenderer(StarmapGFX gfx, CommonGFX cgfx, 
			UISounds uiSound, InfobarRenderer infobarRenderer) {
		super();
		this.gfx = gfx;
		this.cgfx = cgfx;
		this.text = cgfx.text;
		this.uiSound = uiSound;
		this.infobarRenderer = infobarRenderer;
		setDoubleBuffered(true);
		setOpaque(true);
		//setCursor(gfx.cursors.target);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		initActions();
		fadeTimer = new Timer(FADE_INTERVAL, new ActionListener() { public void actionPerformed(ActionEvent e) { doFade(); } });
		animations = new Timer(ANIMATION_INTERVAL, new ActionListener() { public void actionPerformed(ActionEvent e) { doAnimate(); } });
		precalculateStars();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paint(Graphics g) {
		long t = System.nanoTime();
		Graphics2D g2 = (Graphics2D)g;
		// draw top and bottom bar four corners
		int w = getWidth();
		int h = getHeight();
		if (w != lastWidth || h != lastHeight) {
			lastWidth = w;
			lastHeight = h;
			// if the render window changes, re-zoom to update scrollbars
			updateRegions();
			zoom(zoomFactor);
			updateScrollKnobs();
		}
		
		infobarRenderer.renderInfoBars(this, g2);
		
		// draw inner area four corners
		g2.drawImage(gfx.contents.bottomLeft, bottomLeftRect.x, bottomLeftRect.y, null);
		g2.drawImage(gfx.contents.rightTop, rightTopRect.x, rightTopRect.y, null);

		g2.drawImage(gfx.contents.bottomRight, bottomRightRect.x, bottomRightRect.y, null);
		g2.drawImage(gfx.contents.rightBottom, rightBottomRect.x, rightBottomRect.y, null);
		// check if the rendering width is greater than the default 640
		// if so, draw the link lines
		if (bottomFillerRect.width > 0) {
			// inner content filler
			Paint p = g2.getPaint();
			g2.setPaint(new TexturePaint(gfx.contents.bottomFiller, new Rectangle(bottomFillerRect.x, bottomFillerRect.y, 2, bottomFillerRect.height)));
			g2.fill(bottomFillerRect);
			g2.setPaint(p);
			/*
			for (int i = gfx.contents.bottomLeft.getWidth(); i < w - gfx.contents.bottomRight.getWidth(); i += 2) {
				g2.drawImage(gfx.contents.bottomFiller, i, h - bh, null);
			}
			*/
		}
		if (h > 480) {
			Paint p = g2.getPaint();
			g2.setPaint(new TexturePaint(gfx.contents.rightFiller, new Rectangle(rightFillerRect.x, rightFillerRect.y, rightFillerRect.width, 2)));
			g2.fill(rightFillerRect);
			g2.setPaint(p);
//				for (int i = cgfx.top.right.getHeight() + gfx.contents.rightTop.getHeight(); i < h - bh - gfx.contents.rightBottom.getHeight(); i += 2) {
//					g2.drawImage(gfx.contents.rightFiller, w - gfx.contents.rightFiller.getWidth(), i, null);
//				}
		}
		// draw inner controls

		g2.setColor(Color.BLACK);
		g2.fill(hscrollRect);
		g2.fill(vscrollRect);
		
		// paint horizontal scrollbar
		g2.drawImage(gfx.contents.hscrollLeft, hknobRect.x, hknobRect.y, null);
		g2.drawImage(gfx.contents.hscrollRight, hknobRect.x + hknobRect.width - gfx.contents.hscrollRight.getWidth(), hscrollRect.y, null);
		g2.drawImage(gfx.contents.vscrollTop, vknobRect.x, vknobRect.y, null);
		g2.drawImage(gfx.contents.vscrollBottom, vknobRect.x, vknobRect.y + vknobRect.height - gfx.contents.vscrollBottom.getHeight(), null);
		
		Paint p = g2.getPaint();
		// horizontal scrollbar middle
		g2.setPaint(new TexturePaint(gfx.contents.hscrollFiller, new Rectangle(hknobRect.x + gfx.contents.hscrollLeft.getWidth(), hscrollRect.y, gfx.contents.hscrollFiller.getWidth(), gfx.contents.hscrollFiller.getHeight())));
		g2.fillRect(hknobRect.x + gfx.contents.hscrollLeft.getWidth(), hscrollRect.y, hknobRect.width - gfx.contents.hscrollLeft.getWidth() - gfx.contents.hscrollRight.getWidth(), gfx.contents.hscrollFiller.getHeight());
		g2.setPaint(new TexturePaint(gfx.contents.vscrollFiller, new Rectangle(vknobRect.x, vknobRect.y + gfx.contents.vscrollTop.getHeight(), gfx.contents.vscrollFiller.getWidth(), gfx.contents.vscrollFiller.getHeight())));
		g2.fillRect(vknobRect.x, vknobRect.y + gfx.contents.vscrollTop.getHeight(), gfx.contents.vscrollFiller.getWidth(), vknobRect.height - gfx.contents.vscrollTop.getHeight() - gfx.contents.vscrollBottom.getHeight());
		
		g2.setPaint(p);
		
		// draw minimap
		g2.drawImage(cgfx.minimap, minimapRect.x, minimapRect.y, null);
		
		// draw the entire map in a clipping rect
		g2.setColor(gfx.contents.mapBackground);
		//g2.setColor(Color.YELLOW);
		g2.fill(mapRect);
		Shape sp = g2.getClip();
		g2.setClip(mapRect);
		AffineTransform af = g2.getTransform();
		int mx = -(int)(hscrollValue * hscrollFactor);
		int my = -(int)(vscrollValue * vscrollFactor); 
		// if the viewport is much bigger than the actual image, lets center it
		if (mapRect.width > gfx.contents.fullMap.getWidth() * zoomFactor) {
			mx = (int)((mapRect.width - gfx.contents.fullMap.getWidth() * zoomFactor) / 2);
		}
		if (mapRect.height > gfx.contents.fullMap.getHeight() * zoomFactor) {
			my = (int)((mapRect.height - gfx.contents.fullMap.getHeight() * zoomFactor) / 2);
		}
		
		mapCoords.x = mapRect.x + mx;
		mapCoords.y = mapRect.y + my;
		mapCoords.width = (int)(gfx.contents.fullMap.getWidth() * zoomFactor);
		mapCoords.height = (int)(gfx.contents.fullMap.getHeight() * zoomFactor);
		
		g2.setClip(mapCoords.intersection(mapRect));
		g2.translate(mapRect.x + mx, mapRect.y + my);
		g2.scale(zoomFactor, zoomFactor);
		g2.drawImage(gfx.contents.fullMap, 0, 0, null);
		g2.setTransform(af);
		
		if (btnStars.down) {
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 1000; j++) {
					int x = (int)(mapCoords.x - hscrollValue * hscrollFactor * (i + 1) / 10 + starsX[i * STAR_COUNT + j] * zoomFactor);
					int y = (int)(mapCoords.y - vscrollValue * vscrollFactor * (i + 1) / 10 + starsY[i * STAR_COUNT + j] * zoomFactor);
					int c = starsColors[i * STAR_COUNT + j];
					g2.setColor(new Color(c));
					if (zoomFactor < 2) {
						g2.fillRect(x, y, 1, 1);
					} else {
						g2.fillRect(x, y, 2, 2);
					}
				}
			}
		}
		
		// Render Grid
		if (btnGrids.down) {
			g2.setColor(CommonGFX.GRID_COLOR);
			Stroke st = g2.getStroke();
			g2.setStroke(CommonGFX.GRID_STROKE);
			float fw = gfx.contents.fullMap.getWidth() * zoomFactor;
			float fh = gfx.contents.fullMap.getHeight() * zoomFactor;
			float dx = fw / 5;
			float dy = fh / 5;
			float y0 = dy;
			float x0 = dx;
			for (int i = 1; i < 5; i++) {
				g2.drawLine((int)(mapRect.x + x0) + mx, mapRect.y + my, (int)(mapRect.x + x0) + mx, (int)(mapRect.y + fh) + my);
				g2.drawLine(mapRect.x + mx, (int)(mapRect.y + y0) + my, (int)(mapRect.x + fw) + mx, (int)(mapRect.y + y0) + my);
				x0 += dx;
				y0 += dy;
			}
			int i = 0;
			y0 = dy - 6;
			x0 = 2;
			for (char c = 'A'; c < 'Z'; c++) {
				text.paintTo(g2, (int)(mapRect.x + x0) + mx, (int)(mapRect.y + y0) + my, 5, TextGFX.GRAY, String.valueOf(c));
				x0 += dx;
				i++;
				if (i % 5 == 0) {
					x0 = 2;
					y0 += dy;
				}
			}
			
			g2.setStroke(st);
		}
		renderPlanetsAndFleets(g2, mapRect.x + mx, mapRect.y + my);
		g2.setClip(minimapRect);
		renderMinimap(g2);
		
		g2.setClip(sp);
		
		// ----------------------------------------------------------------
		// RENDER BUTTONS
		// ----------------------------------------------------------------
		
		g2.setColor(Color.BLACK);
		if (btnColonyPrev.disabled) {
			g2.drawImage(gfx.btnPrevDisabled, btnColonyPrev.rect.x, btnColonyPrev.rect.y, null);
		} else
		if (btnColonyPrev.down) {
			Rectangle r = btnColonyPrev.rect;
			g2.drawLine(r.x, r.y, r.x + r.width, r.y);
			g2.drawLine(r.x, r.y + r.height - 1, r.x + r.width, r.y + r.height - 1);
			g2.drawImage(gfx.btnPrevDown, r.x, r.y + 1, null);
		}
		if (btnColonyNext.disabled) {
			g2.drawImage(gfx.btnNextDisabled, btnColonyNext.rect.x, btnColonyNext.rect.y + 1, null);
		} else
		if (btnColonyNext.down) {
			Rectangle r = btnColonyNext.rect;
			g2.drawLine(r.x, r.y, r.x + r.width, r.y);
			g2.drawLine(r.x, r.y + r.height - 1, r.x + r.width, r.y + r.height - 1);
			g2.drawImage(gfx.btnNextDown, r.x, r.y + 1, null);
		}
		if (btnColony.down) {
			g2.drawImage(gfx.btnColonyDown, btnColony.rect.x, btnColony.rect.y, null);
		}
		
		if (btnEquipmentPrev.disabled) {
			g2.drawImage(gfx.btnPrevDisabled, btnEquipmentPrev.rect.x, btnEquipmentPrev.rect.y, null);
		} else
		if (btnEquipmentPrev.down) {
			Rectangle r = btnEquipmentPrev.rect;
			g2.drawLine(r.x, r.y, r.x + r.width, r.y);
			g2.drawLine(r.x, r.y + r.height - 1, r.x + r.width, r.y + r.height - 1);
			g2.drawImage(gfx.btnPrevDown, r.x, r.y + 1, null);
		}
		if (btnEquipmentNext.disabled) {
			g2.drawImage(gfx.btnNextDisabled, btnEquipmentNext.rect.x, btnEquipmentNext.rect.y, null);
		} else
		if (btnEquipmentNext.down) {
			Rectangle r = btnEquipmentNext.rect;
			g2.drawLine(r.x, r.y, r.x + r.width, r.y);
			g2.drawLine(r.x, r.y + r.height - 1, r.x + r.width, r.y + r.height - 1);
			g2.drawImage(gfx.btnNextDown, r.x, r.y + 1, null);
		}
		if (btnEquipment.disabled) {
			g2.drawImage(gfx.btnEquipmentDisabled, btnEquipment.rect.x, btnEquipment.rect.y, null);
		} else
		if (btnEquipment.down) {
			g2.drawImage(gfx.btnEquipmentDown, btnEquipment.rect.x, btnEquipment.rect.y, null);
		}
		
		if (btnInfo.down) {
			g2.drawImage(gfx.btnInfoDown, btnInfo.rect.x, btnInfo.rect.y, null);
		}
		if (btnBridge.down) {
			g2.drawImage(gfx.btnBridgeDown, btnBridge.rect.x, btnBridge.rect.y, null);
		}
		if (btnMagnify.disabled) {
			g2.drawImage(gfx.btnMagnifyDisabled, btnMagnify.rect.x, btnMagnify.rect.y, null);
		} else
		if (btnMagnify.down) {
			g2.drawImage(gfx.btnMagnifyLight, btnMagnify.rect.x, btnMagnify.rect.y, null);
		}

		if (showShipControls) {
			if (btnMove.down) {
				g2.drawImage(gfx.btnMoveLight, btnMove.rect.x, btnMove.rect.y, null);
			}
			if (btnAttack.down) {
				g2.drawImage(gfx.btnAttackLight, btnAttack.rect.x, btnAttack.rect.y, null);
			}
			if (btnStop.down) {
				g2.drawImage(gfx.btnStopLight, btnStop.rect.x, btnStop.rect.y, null);
			}
		} else 
		if (showSatellites) {
			g2.fill(shipControlRect);
			if (btnSatellite.visible) {
				g2.drawImage(gfx.btnAddSat, btnSatellite.rect.x, btnSatellite.rect.y, null);
			}
			if (btnSpySat1.visible) {
				g2.drawImage(gfx.btnAddSpySat1, btnSpySat1.rect.x, btnSpySat1.rect.y, null);
			}
			if (btnSpySat2.visible) {
				g2.drawImage(gfx.btnAddSpySat2, btnSpySat2.rect.x, btnSpySat2.rect.y, null);
			}
			if (btnHubble2.visible) {
				g2.drawImage(gfx.btnAddHubble2, btnHubble2.rect.x, btnHubble2.rect.y, null);
			}
		} else {
			g2.fill(shipControlRect);
		}

		
		if (btnColonize.visible) {
			if (btnColonize.disabled) {
				g2.drawImage(gfx.btnColoniseDisabled, btnColonize.rect.x, btnColonize.rect.y, null);
			} else {
				g2.drawImage(gfx.btnColonise, btnColonize.rect.x, btnColonize.rect.y, null);
			}
		}
		
		if (btnRadars.down) {
			g2.drawImage(gfx.btnRadarsLight, btnRadars.rect.x, btnRadars.rect.y, null);
		} else {
			g2.drawImage(gfx.btnRadars, btnRadars.rect.x, btnRadars.rect.y, null);
		}
		if (btnFleets.down) {
			g2.drawImage(gfx.btnFleetsLight, btnFleets.rect.x, btnFleets.rect.y, null);
		} else {
			g2.drawImage(gfx.btnFleets, btnFleets.rect.x, btnFleets.rect.y, null);
		}
		if (btnStars.down) {
			g2.drawImage(gfx.btnStarsLight, btnStars.rect.x, btnStars.rect.y, null);
		} else {
			g2.drawImage(gfx.btnStars, btnStars.rect.x, btnStars.rect.y, null);
		}
		if (btnGrids.down) {
			g2.drawImage(gfx.btnGridsLight, btnGrids.rect.x, btnGrids.rect.y, null);
		} else {
			g2.drawImage(gfx.btnGrids, btnGrids.rect.x, btnGrids.rect.y, null);
		}
		
		BufferedImage img = null;
		switch (nameMode) {
		case 0:
			img = gfx.btnNameOff;
			break;
		case 1:
			img = gfx.btnNameColony;
			break;
		case 2:
			img = gfx.btnNameFleets;
			break;
		case 3:
			img = gfx.btnNameBoth;
			break;
		default:
		}
		g2.drawImage(img, btnName.rect.x, btnName.rect.y, null);
		t = System.nanoTime() - t;
		//System.out.printf("%.2f frame/s%n", 1E9 / t);
		// now darken the entire screen
		
		// render planet names
		sp = g2.getClip();
		g2.setClip(colonies);
		int y = colonies.y + 2;
		List<GamePlanet> list = gameWorld.getPlayerPlanets();
		int planetTextSize = 10;
		for (int i = planetListOffset; i < list.size(); i++) {
			if (y > colonies.y + colonies.height) {
				break;
			}
			GamePlanet planet = list.get(i);
			text.paintTo(g2, colonies.x + 1, y, planetTextSize, TextGFX.GREEN, planet.name);
			y += planetTextSize + 1;
		}
		
		g2.setClip(sp);

		// FINAL OPERATION: draw darkening layer over the screen
		if (darkness > 0.0f) {
			Composite comp = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, darkness));
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, w, h);
			g2.setComposite(comp);
		}

	}
	/** Recalculate the region coordinates. */
	private void updateRegions() {
		infobarRenderer.updateRegions(this);
		int w = getWidth();
		int h = getHeight();
		int bh = cgfx.bottom.left.getHeight() + gfx.contents.bottomLeft.getHeight();

		// fix ship control area if the ship area is not visible
		shipControlRect.x = w - 355;
		shipControlRect.y = h - bh + 28;
		shipControlRect.width = 106;
		shipControlRect.height = 83;
		
		// minimap rectangle
		minimapRect.x = w - 133;
		minimapRect.y = h - 109 - cgfx.bottom.right.getHeight();
		minimapRect.width = 131;
		minimapRect.height = 108;
		
		mapRect.x = 0;
		mapRect.y = cgfx.top.left.getHeight();
		mapRect.width = w - gfx.contents.rightTop.getWidth();
		mapRect.height = h - cgfx.top.left.getHeight() - cgfx.bottom.left.getHeight() - gfx.contents.bottomLeft.getHeight();
		
		bottomLeftRect.x = 0;
		bottomLeftRect.y = h - cgfx.bottom.left.getHeight() - gfx.contents.bottomLeft.getHeight();
		bottomLeftRect.width = gfx.contents.bottomLeft.getWidth();
		bottomLeftRect.height = gfx.contents.bottomLeft.getHeight();
		
		bottomRightRect.x = w - gfx.contents.bottomRight.getWidth();
		bottomRightRect.y = bottomLeftRect.y;
		bottomRightRect.width = gfx.contents.bottomRight.getWidth();
		bottomRightRect.height = gfx.contents.bottomRight.getHeight();
		
		bottomFillerRect.x = bottomLeftRect.x + bottomLeftRect.width;
		bottomFillerRect.y = bottomLeftRect.y;
		bottomFillerRect.width = bottomRightRect.x - bottomFillerRect.x;
		bottomFillerRect.height = gfx.contents.bottomFiller.getHeight();
		
		rightTopRect.x = w - gfx.contents.rightTop.getWidth();
		rightTopRect.y = cgfx.top.right.getHeight();
		rightTopRect.width = gfx.contents.rightTop.getWidth();
		rightTopRect.height = gfx.contents.rightTop.getHeight();
		
		rightBottomRect.x = rightTopRect.x;
		rightBottomRect.y = bottomRightRect.y - gfx.contents.rightBottom.getHeight();
		rightBottomRect.width = gfx.contents.rightBottom.getWidth();
		rightBottomRect.height = gfx.contents.rightBottom.getHeight();
		
		rightFillerRect.x = rightTopRect.x;
		rightFillerRect.y = rightTopRect.y + rightTopRect.height;
		rightFillerRect.width = gfx.contents.rightFiller.getWidth();
		rightFillerRect.height = rightBottomRect.y - rightFillerRect.y;

		// fix scrollbar area colors
		hscrollRect.x = 3;
		hscrollRect.y = h - bh + 3;
		hscrollRect.width = w - 142;
		hscrollRect.height = 18;

		vscrollRect.x = rightTopRect.x + 3;
		vscrollRect.y = rightTopRect.y + 3;
		vscrollRect.width = 18;
		vscrollRect.height = h - bh - cgfx.top.right.getHeight() - 7;

		// Update button locations
		
		btnColonyPrev.rect.x = w - 105;
		btnColonyPrev.rect.y = rightTopRect.y + 5;
		btnColonyPrev.rect.width = 50;
		btnColonyPrev.rect.height = 20;
		
		btnColonyNext.rect.x = btnColonyPrev.rect.x + 52;
		btnColonyNext.rect.y = btnColonyPrev.rect.y;
		btnColonyNext.rect.width = 50;
		btnColonyNext.rect.height = 20;
		
		colonies.x = btnColonyPrev.rect.x - 2;
		colonies.y = btnColonyPrev.rect.y + btnColonyPrev.rect.height;
		colonies.width = 105;
		colonies.height = rightBottomRect.y + 16 - colonies.y;
		
		btnColony.rect.x = colonies.x + 1;
		btnColony.rect.y = colonies.y + colonies.height;
		btnColony.rect.width = colonies.width - 2;
		btnColony.rect.height = 28;
		
		btnEquipmentPrev.rect.x = btnColonyPrev.rect.x;
		btnEquipmentPrev.rect.y = btnColony.rect.y + btnColony.rect.height + 7;
		btnEquipmentPrev.rect.width = 50;
		btnEquipmentPrev.rect.height = 20;
		
		btnEquipmentNext.rect.x = btnEquipmentPrev.rect.x + 52;
		btnEquipmentNext.rect.y = btnEquipmentPrev.rect.y;
		btnEquipmentNext.rect.width = 50;
		btnEquipmentNext.rect.height = 20;
		
		equipments.x = colonies.x;
		equipments.y = btnEquipmentPrev.rect.y + btnEquipmentPrev.rect.height;
		equipments.width = 105;
		equipments.height = 33;
		
		btnEquipment.rect.x = btnColony.rect.x;
		btnEquipment.rect.y = equipments.y + equipments.height;
		btnEquipment.rect.width = 103;
		btnEquipment.rect.height = 28;
		
		btnInfo.rect.x = btnColony.rect.x + 1;
		btnInfo.rect.y = btnEquipment.rect.y + 105;
		btnInfo.rect.width = 102;
		btnInfo.rect.height = 39;
		
		btnBridge.rect.x = btnInfo.rect.x;
		btnBridge.rect.y = btnInfo.rect.y + btnInfo.rect.height;
		btnBridge.rect.width = 102;
		btnBridge.rect.height = 39;
		
		btnMagnify.rect.x = 69 + btnEquipment.rect.x;
		btnMagnify.rect.y = 35 + btnEquipment.rect.y;
		btnMagnify.rect.width = 33;
		btnMagnify.rect.height = 64;
		
		btnColonize.rect.x = w - 245;
		btnColonize.rect.y = bottomLeftRect.y + 30;
		btnColonize.rect.width = 108;
		btnColonize.rect.height = 15;
		
		btnRadars.rect.x = btnColonize.rect.x;
		btnRadars.rect.y = btnColonize.rect.y + 21;
		btnRadars.rect.width = 53;
		btnRadars.rect.height = 18;
		
		btnFleets.rect.x = btnRadars.rect.x + 55;
		btnFleets.rect.y = btnRadars.rect.y;
		btnFleets.rect.width = 53;
		btnFleets.rect.height = 18;
		
		btnStars.rect.x = btnRadars.rect.x;
		btnStars.rect.y = btnRadars.rect.y + 20;
		btnStars.rect.width = 53;
		btnStars.rect.height = 18;
		
		btnGrids.rect.x = btnFleets.rect.x;
		btnGrids.rect.y = btnStars.rect.y;
		btnGrids.rect.width = 53;
		btnGrids.rect.height = 18;
		
		btnName.rect.x = btnStars.rect.x;
		btnName.rect.y = btnStars.rect.y + 20;
		btnName.rect.width = 108;
		btnName.rect.height = 18;
		
		btnMove.rect.x = btnStars.rect.x - 105;
		btnMove.rect.y = btnColonize.rect.y + 1;
		btnMove.rect.width = 98;
		btnMove.rect.height = 23;
		
		btnAttack.rect.x = btnMove.rect.x;
		btnAttack.rect.y = btnMove.rect.y + 27;
		btnAttack.rect.width = 98;
		btnAttack.rect.height = 23;
		
		btnStop.rect.x = btnMove.rect.x;
		btnStop.rect.y = btnAttack.rect.y + 27;
		btnStop.rect.width = 98;
		btnStop.rect.height = 23;
		
		updateSatellites();
	}
	/** Update satellite button. */
	public void updateSatellites() {
		int x = btnColonize.rect.x - 91;
		int y = btnColonize.rect.y;
		if (btnSatellite.visible) {
			btnSatellite.rect.x = x;
			btnSatellite.rect.y = y;
			btnSatellite.rect.width = 84;
			btnSatellite.rect.height = 17;
			y += 20;
		}
		if (btnSpySat1.visible) {
			btnSpySat1.rect.x = x;
			btnSpySat1.rect.y = y;
			btnSpySat1.rect.width = 84;
			btnSpySat1.rect.height = 17;
			y += 20;
		}
		if (btnSpySat2.visible) {
			btnSpySat2.rect.x = x;
			btnSpySat2.rect.y = y;
			btnSpySat2.rect.width = 84;
			btnSpySat2.rect.height = 17;
			y += 20;
		}
		if (btnHubble2.visible) {
			btnHubble2.rect.x = x;
			btnHubble2.rect.y = y;
			btnHubble2.rect.width = 84;
			btnHubble2.rect.height = 17;
			y += 20;
		}
	}
	/** Update the location records for the scrollbar knobs. */
	public void updateScrollKnobs() {
		int hextsize = Math.max(gfx.contents.hscrollLeft.getWidth() + gfx.contents.hscrollRight.getWidth(), hscrollRect.width - hscrollMax);

		hknobRect.x = (int)(hscrollRect.x + hscrollValue);
		hknobRect.width = hextsize;
		hknobRect.y = hscrollRect.y;
		hknobRect.height = gfx.contents.hscrollLeft.getHeight();

		int vextsize = Math.max(gfx.contents.vscrollTop.getHeight() + gfx.contents.vscrollBottom.getHeight(), vscrollRect.height - vscrollMax);
		
		vknobRect.x = vscrollRect.x;
		vknobRect.width = gfx.contents.vscrollTop.getWidth();
		vknobRect.y = (int)(vscrollRect.y  + vscrollValue);
		vknobRect.height = vextsize;
	}
	/** 
	 * Zoom in or out on a particular region. 
	 * @param newZoomFactor the new zoom factor to set
	 */
	public void zoom(float newZoomFactor) {
		zoomAndScroll(newZoomFactor, true);
	}
	/**
	 * Zoom in an scroll to the given position.
	 * @param newZoomFactor the new zoom factor
	 * @param scroll indicate that perform the scroll correction instantly, if false, 
	 * the caller must issue a subsequenct scroll() to re-locate the viewport and redraw the screen.
	 */
	public void zoomAndScroll(float newZoomFactor, boolean scroll) {
		if (newZoomFactor > 4.0f) {
			newZoomFactor = 4.0f;
		}
		this.zoomFactor = newZoomFactor;
		// ************************************************************
		// HORIZONTAL SCROLLBAR CORRECTION
		// determine the maximum values for the horizontal scrollbar
		int zoomedWidth = (int)(gfx.contents.fullMap.getWidth() * newZoomFactor);
		// get the number of pixels differring in the size of the map and the scrollbar region
		// minus the smallest scrollbar knob
		int minKnobSize = gfx.contents.hscrollLeft.getWidth() + gfx.contents.hscrollRight.getWidth();
		int maxScrollRegion = hscrollRect.width - minKnobSize;
		int mapExcess = Math.max(zoomedWidth - mapRect.width, 0);
		// if the excess area is smaller than the the maxScrollRegion, lets grow the knob
		// with the difference
		if (mapExcess < maxScrollRegion) {
			hscrollMax = Math.max(mapExcess, 0);
			hscrollFactor = 1.0f;
		} else {
			// otherwise just hold it on the max scroll region size.
			hscrollMax = maxScrollRegion;
			hscrollFactor = mapExcess * 1.0f / maxScrollRegion;
		}
		// compensate the scroll value
		// ************************************************************
		// VERTICAL SCROLLBAR CORRECTION
		int zoomedHeight = (int)(gfx.contents.fullMap.getHeight() * newZoomFactor);
		minKnobSize = gfx.contents.vscrollTop.getHeight() + gfx.contents.vscrollBottom.getHeight();
		maxScrollRegion = vscrollRect.height - minKnobSize;
		mapExcess = Math.max(zoomedHeight - mapRect.height, 0);
		if (mapExcess < maxScrollRegion) {
			vscrollMax = Math.max(mapExcess, 0);
			vscrollFactor = 1.0f;
		} else {
			vscrollMax = maxScrollRegion;
			vscrollFactor = mapExcess * 1.0f / maxScrollRegion;
		}
		if (scroll) {
			scroll(hscrollValue, vscrollValue);
		}
	}
	/** 
	 * Scroll to a particular scrollbar position.
	 * @param xValue scroll to X value
	 * @param yValue scroll to Y value 
	 */
	public void scroll(float xValue, float yValue) {
		hscrollValue = Math.max(Math.min(xValue, hscrollMax), 0f);
		vscrollValue = Math.max(Math.min(yValue, vscrollMax), 0f);
		// calculate map offset based on the current scrollbar locations
//			mapX = (int)(hscrollValue * hscrollFactor);
//			mapY = (int)(vscrollValue * vscrollFactor);
		updateScrollKnobs();
		repaint();
	}
	/** 
	 * Scroll the map by relative coordinates given in pixels. 
	 * @param dx delta x
	 * @param dy delta y
	 */
	public void scrollByPixelRel(int dx, int dy) {
		scroll((hscrollValue + dx / hscrollFactor), 
				(vscrollValue + dy / vscrollFactor));
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		Point pt = e.getPoint();
		int dx = lastMouseX - pt.x;
		int dy = lastMouseY - pt.y;
		lastMouseX = pt.x;
		lastMouseY = pt.y;
		if (minimapRect.contains(pt)) {
			doMinimapScroll(pt);
		} else
		if (mapDragMode) {
			scrollByPixelRel(dx, dy);
		} else
		if (verticalDragMode) {
			scroll(hscrollValue, vscrollValue - dy);
		} else
		if (horizontalDragMode) {
			scroll(hscrollValue - dx, vscrollValue);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		Point pt = e.getPoint();
		if ((e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) 
				&& !btnMagnify.disabled && btnMagnify.rect.contains(pt)) {
			btnMagnify.click();
		} else {
			if (e.getButton() == MouseEvent.BUTTON1) {
				for (Btn b : buttons) {
					if (b.test(pt)) {
						b.click();
						break;
					}
				}
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		Point pt = e.getPoint();
		if (minimapRect.contains(pt)) {
			doMinimapScroll(pt);
		} else
		if (e.getButton() == MouseEvent.BUTTON3 && mapRect.contains(pt)) {
			lastMouseX = e.getX();
			lastMouseY = e.getY();
			mapDragMode = true;
		} else
		if (e.getButton() == MouseEvent.BUTTON1 && hknobRect.contains(pt)) {
			lastMouseX = e.getX();
			lastMouseY = e.getY();
			horizontalDragMode = true;
		} else
		if (e.getButton() == MouseEvent.BUTTON1 && vknobRect.contains(pt)) {
			lastMouseX = e.getX();
			lastMouseY = e.getY();
			verticalDragMode = true;
		} else 
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (!btnMagnify.disabled && btnMagnify.rect.contains(pt)) {
				magnifyDirection = true;
				btnMagnify.down = true;
				repaint(btnMagnify.rect);
			} else {
				for (Btn b : buttons) {
					if (b.test(pt)) {
						b.down = true;
						repaint(b.rect);
						break;
					}
				}
				for (Btn b : toggleButtons) {
					if (b.test(pt)) {
						b.down = !b.down;
						repaint(b.rect);
						b.click();
						break;
					}
				}
			}
		} else
		if (e.getButton() == MouseEvent.BUTTON3 && btnMagnify.test(pt)) {
			magnifyDirection = false;
			btnMagnify.down = true;
			repaint(btnMagnify.rect);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// if the user released the third button, cancel map drag mode
		mapDragMode = false;
		verticalDragMode = false;
		horizontalDragMode = false;
		boolean needRepaint = false;
		for (Btn b : buttons) {
			needRepaint |= b.down;
			b.down = false;
		}
		needRepaint |= btnMagnify.down;
		btnMagnify.down = false;
		if (needRepaint) {
			repaint();
		}
	}
	/** Initialize button actions. */
	private void initActions() {
		btnColony = new Btn(new BtnAction() { public void invoke() { doColonyClick(); } });
		buttons.add(btnColony);
		btnColonyPrev = new Btn(new BtnAction() { public void invoke() { doColonyPrev(); } });
		buttons.add(btnColonyPrev);
		btnColonyNext = new Btn(new BtnAction() { public void invoke() { doColonyNext(); } });
		buttons.add(btnColonyNext);
		btnEquipment = new Btn(new BtnAction() { public void invoke() { doEquipmentClick(); } });
		buttons.add(btnEquipment);
		btnEquipmentPrev = new Btn();
		buttons.add(btnEquipmentPrev);
		btnEquipmentNext = new Btn();
		buttons.add(btnEquipmentNext);
		btnInfo = new Btn(new BtnAction() { public void invoke() { doInfoClick(); } });
		buttons.add(btnInfo);
		btnBridge = new Btn(new BtnAction() { public void invoke() { doBridgeClick(); } });
		buttons.add(btnBridge);
		btnColonize = new Btn();
		buttons.add(btnColonize);
		btnName = new Btn(new BtnAction() { @Override public void invoke() { doNameChange(); } });
		buttons.add(btnName);
		btnMagnify = new Btn(new BtnAction() { @Override public void invoke() {	doMagnify(); } });
		btnFleets = new Btn();
		toggleButtons.add(btnFleets);
		btnGrids = new Btn(new BtnAction() { public void invoke() { doGridsClick(); } });
		toggleButtons.add(btnGrids);
		btnRadars = new Btn();
		toggleButtons.add(btnRadars);
		btnStars = new Btn();
		toggleButtons.add(btnStars);
		
		btnGrids.down = true;
		btnRadars.down = true;
		btnFleets.down = true;
		btnStars.down = true;
		
		btnMove = new Btn();
		toggleButtons.add(btnMove);
		btnAttack = new Btn();
		toggleButtons.add(btnAttack);
		btnStop = new Btn();
		toggleButtons.add(btnStop);
		btnSatellite = new Btn();
		buttons.add(btnSatellite);
		btnSpySat1 = new Btn();
		buttons.add(btnSpySat1);
		btnSpySat2 = new Btn();
		buttons.add(btnSpySat2);
		btnHubble2 = new Btn();
		buttons.add(btnHubble2);
		
		btnSatellite.visible = true;
		btnSpySat1.visible = true;
		btnSpySat2.visible = true;
		btnHubble2.visible = true;
	}
	/** Grids click action. */
	protected void doGridsClick() {
		repaint(mapRect);
	}
	/** Do magnify operation. */
	private void doMagnify() {
		doZoom(magnifyDirection, mapRect.width / 2, mapRect.y + mapRect.height / 2);
	}
	/**
	 * Do zoom operation. 
	 * @param direction zoom direction, true means in
	 * @param x the x coordinate to zoom in/out around
	 * @param y the y coordinate to zoom in/out around
	 */
	private void doZoom(boolean direction, int x, int y) {
		float currFactor = magnifyFactors[magnifyIndex];
		if (direction && magnifyIndex < magnifyFactors.length - 1) {
			magnifyIndex++;
		} else
		if (!direction && magnifyIndex > 0) {
			magnifyIndex--;
		}
		float nextFactor = magnifyFactors[magnifyIndex];
		// calculate the mouse position using the map's scroll position
		float hp = (hscrollValue * hscrollFactor + x - mapRect.x) / currFactor * 30;
		float vp = (vscrollValue * vscrollFactor + y - mapRect.y) / currFactor * 30;
		zoomAndScroll(nextFactor / 30f, false);
		// now re-position the very same point back under the cursor
		float hpp = (hp * nextFactor / 30 - x + mapRect.x) / hscrollFactor;
		float vpp = (vp * nextFactor / 30 - y + mapRect.y) / vscrollFactor;
		scroll(hpp, vpp);
	}
	/** Perform display names toggle. */
	private void doNameChange() {
		nameMode = (nameMode + 1) % 4;
		repaint(btnName.rect);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		Point pt = e.getPoint();
		if (mapRect.contains(pt)) {
			if (e.isControlDown()) {
				doZoom(e.getWheelRotation() < 0, pt.x, pt.y);
			} else
			if (e.isShiftDown()) {
				// scroll horizontally
				if (e.getWheelRotation() < 0) {
					scrollByPixelRel(-SCROLL_AMOUNT, 0);
				} else {
					scrollByPixelRel(SCROLL_AMOUNT, 0);
				}
			} else {
				// scroll vertically
				if (e.getWheelRotation() < 0) {
					scrollByPixelRel(0, -SCROLL_AMOUNT);
				} else {
					scrollByPixelRel(0, SCROLL_AMOUNT);
				}
			}
		}
	}
	/** Perform colony click action. */
	private void doColonyClick() {
		uiSound.playSound("Colony");
		fadeTimer.start();
	}
	/** Equipment colony click action. */
	private void doEquipmentClick() {
		uiSound.playSound("Equipment");
	}
	/** Info click action. */
	private void doInfoClick() {
		uiSound.playSound("Information");
		if (onInformationClicked != null) {
			onInformationClicked.invoke();
		}
	}
	/** Bridge click action. */
	private void doBridgeClick() {
		uiSound.playSound("Bridge");
	}
	/**
	 * @param onColonyClicked the onColonyClicked to set
	 */
	public void setOnColonyClicked(BtnAction onColonyClicked) {
		this.onColonyClicked = onColonyClicked;
	}
	/**
	 * @return the onColonyClicked
	 */
	public BtnAction getOnColonyClicked() {
		return onColonyClicked;
	}
	/**
	 * @param onInformationClicked the onInformationClicked to set
	 */
	public void setOnInformationClicked(BtnAction onInformationClicked) {
		this.onInformationClicked = onInformationClicked;
	}
	/**
	 * @return the onInformationClicked
	 */
	public BtnAction getOnInformationClicked() {
		return onInformationClicked;
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
		if (onColonyClicked != null) {
			onColonyClicked.invoke();
		}
		darkness = 0f;
	}
	/**
	 * Render the planets onto the starmap screen and onto the minimap.
	 * @param g2 the graphics 2d object
	 * @param xOrig map rendering original coordinate X
	 * @param yOrig map rendering original coordinate Y
	 */
	private void renderPlanetsAndFleets(Graphics2D g2, int xOrig, int yOrig) {
		BufferedImage ri = cgfx.radarDots[radarDotSizes[magnifyIndex]];
		// render radar first.
		GamePlayer player = gameWorld.player;
		if (btnRadars.down) {
			// render radar circle only for the own planets
			for (GamePlanet p : player.ownPlanets) {
				if (p.visible && p.radarRadius > 0) {
					int modifiedMagnify = magnifyIndex + p.size;
					if (modifiedMagnify < 0) {
						modifiedMagnify = 0;
					}
					BufferedImage pimg = gfx.starmapPlanets.get(p.surfaceType.planetString).get(planetSizes[modifiedMagnify]).get(p.rotationPhase);
					double fd = Math.PI / 50;
					double fm = 2 * Math.PI - fd;
					for (double f = 0.0f; f <= fm; f += fd) {
						int x = (int)((p.x + p.radarRadius * Math.sin(f)) * zoomFactor + pimg.getWidth() / 2f);
						int y = (int)((p.y + p.radarRadius * Math.cos(f)) * zoomFactor + pimg.getHeight() / 2f);
						g2.drawImage(ri, xOrig + x - 1, yOrig + y - 1, null);
					}
				}
			}
		}
		for (GamePlanet p : player.knownPlanets) {
			if (!p.visible) {
				continue;
			}
			int modifiedMagnify = magnifyIndex + p.size;
			if (modifiedMagnify < 0) {
				modifiedMagnify = 0;
			}
			BufferedImage pimg = gfx.starmapPlanets.get(p.surfaceType.planetString).get(planetSizes[modifiedMagnify]).get(p.rotationPhase);
			int x = (int)(p.x * zoomFactor /* - pimg.getWidth() / 2f*/);
			int y = (int)(p.y * zoomFactor /* - pimg.getHeight() / 2f*/);
			
			g2.drawImage(pimg, xOrig + x, yOrig + y, null);
			if ((nameMode == 1 || nameMode == 3) 
					&& player.knownPlanetsByName.contains(p)) {
				int color = TextGFX.GRAY;
				if (p.owner != null) {
					color = p.owner.race.smallColor;
				}
				y = (int)(p.y * zoomFactor + pimg.getHeight()) + 2;
				int w = text.getTextWidth(5, p.name);
				x = (int)(p.x * zoomFactor + pimg.getWidth() / 2f - w / 2f);
				text.paintTo(g2, xOrig + x, yOrig + y, 5, color, p.name);
			}
		}
		// render fleet radars
		if (btnRadars.down && btnFleets.down) {
			for (GameFleet fleet : player.ownFleets) {
				if (fleet.visible && fleet.radarRadius > 0) {
					double fd = Math.PI / 50;
					double fm = 2 * Math.PI - fd;
					for (double f = 0.0f; f <= fm; f += fd) {
						int x = (int)((fleet.x + fleet.radarRadius * Math.sin(f)) * zoomFactor);
						int y = (int)((fleet.y + fleet.radarRadius * Math.cos(f)) * zoomFactor);
						g2.drawImage(ri, xOrig + x - 1, yOrig + y - 1, null);
					}
				}
			}
		}
		// render fleet names
		if (btnFleets.down && (nameMode == 2 || nameMode == 3)) {
			for (GameFleet f : player.knownFleets) {
				if (f.visible) {
					int color = TextGFX.GRAY;
					if (f.owner != null) {
						color = f.owner.race.smallColor;
					}
					BufferedImage fleetImg = cgfx.shipImages[f.owner.fleetIcon];
					int x = (int)(f.x * zoomFactor - fleetImg.getWidth() / 2);
					int y = (int)(f.y * zoomFactor - fleetImg.getHeight() / 2);
					y = (int)(f.y * zoomFactor) + 2 + fleetImg.getHeight();
					int w = text.getTextWidth(5, f.name);
					x = (int)(f.x * zoomFactor - w / 2f);
					text.paintTo(g2, xOrig + x, yOrig + y, 5, color, f.name);
				}
			}
		}
		// render fleet icons
		if (btnFleets.down) {
			for (GameFleet f : player.knownFleets) {
				if (f.visible) {
					BufferedImage fleetImg = cgfx.shipImages[f.owner.fleetIcon];
					int x = (int)(f.x * zoomFactor - fleetImg.getWidth() / 2);
					int y = (int)(f.y * zoomFactor - fleetImg.getHeight() / 2);
					
					g2.drawImage(fleetImg, xOrig + x, yOrig + y, null);
				}
			}
		}
	}
	/**
	 * Perform animations. Rotate planets, move fleets.
	 */
	private void doAnimate() {
		for (GamePlanet p : gameWorld.planets) {
			if (!p.visible) {
				continue;
			}
			int phaseCount = gfx.starmapPlanets.get(p.surfaceType.planetString).get(planetSizes[magnifyIndex]).size();
			if (p.rotationDirection) {
				p.rotationPhase = (p.rotationPhase + 1) % phaseCount;
			} else {
				p.rotationPhase--;
				if (p.rotationPhase < 0) {
					p.rotationPhase = phaseCount - 1;
				}
			}
		}
		repaint(mapRect);
	}
	/**
	 * Start animations.
	 */
	public void startAnimations() {
		animations.start();
	}
	/** 
	 * Stop animations.
	 */
	public void stopAnimations() {
		animations.stop();
	}
	/**
	 * @param gameWorld the gameWorld to set
	 */
	public void setGameWorld(GameWorld gameWorld) {
		this.gameWorld = gameWorld;
	}
	/**
	 * @return the gameWorld
	 */
	public GameWorld getGameWorld() {
		return gameWorld;
	}
	/** Action for colony next button clicked. */
	private void doColonyNext() {
		
	}
	/** Action for colony previous button clicked. */
	private void doColonyPrev() {
		
	}
	/**
	 * Render the minimap region.
	 * @param g2 the graphics object
	 */
	private void renderMinimap(Graphics2D g2) {
		double w2 = cgfx.minimap.getWidth() * 1.0 / gfx.contents.fullMap.getWidth();
		double h2 = cgfx.minimap.getHeight() * 1.0 / gfx.contents.fullMap.getHeight();
		GamePlayer player = gameWorld.player;
		for (GamePlanet p : player.knownPlanets) {
			int color = TextGFX.GRAY;
			if (p.owner != null) {
				color = p.owner.race.smallColor;
			}
			// draw the planet dot onto the minimap
			int x2 = (int)(p.x * w2);
			int y2 = (int)(p.y * h2);
			g2.setColor(new Color(color));
			g2.fillRect(minimapRect.x + x2 - 1, minimapRect.y + y2 - 1, 3, 3);
		}
		// render viewport window
		int x0 = (int)(hscrollValue * hscrollFactor * w2 / zoomFactor);
		int y0 = (int)(vscrollValue * vscrollFactor * h2 / zoomFactor);
		int w0 = (int)(mapRect.width * w2 / zoomFactor);
		int h0 = (int)(mapRect.height * h2 / zoomFactor);
		// bound width and height to avoid the scroll rectangle to run out of the minimap area
		if (x0 + w0 >= minimapRect.width - 1) {
			w0 = minimapRect.width - x0 - 1;
		}
		if (y0 + h0 >= minimapRect.height - 1) {
			h0 = minimapRect.height - y0 - 1;
		}
		g2.setColor(Color.WHITE);
		g2.drawRect(minimapRect.x + x0, minimapRect.y + y0, w0, h0);
	}
	/**
	 * Center the main viewport around the given minimap location.
	 * @param pt the point
	 */
	private void doMinimapScroll(Point pt) {
		// get the current location
		float w2 = cgfx.minimap.getWidth() * 1.0f / gfx.contents.fullMap.getWidth();
		float h2 = cgfx.minimap.getHeight() * 1.0f / gfx.contents.fullMap.getHeight();
		float w0 = Math.min((mapRect.width * w2 / zoomFactor), minimapRect.width);
		float h0 = Math.min((mapRect.height * h2 / zoomFactor), minimapRect.height);
		
		float dx = (pt.x - minimapRect.x - w0 / 2) / hscrollFactor * zoomFactor / w2;
		float dy = (pt.y - minimapRect.y - h0 / 2) / vscrollFactor * zoomFactor / h2;
		scroll(dx, dy);
	}
	/**
	 * Mix two colors with a factor.
	 * @param c1 the first color
	 * @param c2 the second color
	 * @param rate the third color
	 * @return the mixed color
	 */
	private int mixColors(int c1, int c2, float rate) {
		return
			((int)((c1 & 0xFF0000) * rate + (c2 & 0xFF0000) * (1 - rate)) & 0xFF0000)
			| ((int)((c1 & 0xFF00) * rate + (c2 & 0xFF00) * (1 - rate)) & 0xFF00)
			| ((int)((c1 & 0xFF) * rate + (c2 & 0xFF) * (1 - rate)) & 0xFF);
	}
	/**
	 * Precalculates the star background locations and colors.
	 */
	private void precalculateStars() {
		Random random = new Random(0);
		for (int i = 0; i < STAR_LAYER_COUNT; i++) {
			int fw = gfx.contents.fullMap.getWidth() * (10 + i + 1) / 10;
			int fh = gfx.contents.fullMap.getHeight() * (10 + i + 1) / 10;
			for (int j = 0; j < STAR_COUNT; j++) {
				starsX[i * STAR_COUNT + j] = random.nextInt(fw);
				starsY[i * STAR_COUNT + j] = random.nextInt(fh);
				starsColors[i * STAR_COUNT + j] = mixColors(startStars, endStars, random.nextFloat());
			}
		}
	}
}
