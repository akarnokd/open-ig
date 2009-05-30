/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.render;

import hu.openig.core.Btn;
import hu.openig.core.BtnAction;
import hu.openig.core.ImageInterpolation;
import hu.openig.core.PopularityType;
import hu.openig.core.StarmapSelection;
import hu.openig.model.GameFleet;
import hu.openig.model.GamePlanet;
import hu.openig.model.GamePlayer;
import hu.openig.model.GameWorld;
import hu.openig.model.PlanetStatus;
import hu.openig.res.GameResourceManager;
import hu.openig.res.gfx.CommonGFX;
import hu.openig.res.gfx.StarmapGFX;
import hu.openig.res.gfx.TextGFX;
import hu.openig.sound.SoundFXPlayer;
import hu.openig.utils.Parallels;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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
import java.util.TreeSet;

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
	/** The buttons that fire on mouse press. */
	private final List<Btn> releaseButtons = new ArrayList<Btn>();
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
	private final SoundFXPlayer uiSound;
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
	/** The game world object. */
	private GameWorld gameWorld;
	/** The information bar renderer. */
	private final InfobarRenderer infobarRenderer;
	/** The planet listing scroll offset. */
	private int planetListOffset;
	/** The current displayed fleet index. */
	private int fleetListOffset = -1;
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
	private Color[] starsColors = new Color[STAR_COUNT * STAR_LAYER_COUNT];
	/** Scroll animation timer. */
	private Timer scrollAnimTimer;
	/** Scroll animation step interval. */
	private static final int SCROLL_ANIM_INTERVAL = 25;
	/** Scroll animation speed delta / second. */
	private static final int SCROLL_ANIM_SPEED = 30;
	/** The starting x scrollbar position. */
	/** The ending x scrollbar position. */
	private float scrollDestX;
	/** The ending y scrollbar position. */
	private float scrollDestY;
	/** The details rectangle without the ship control rect area. */
	private final Rectangle detailsRect = new Rectangle();
	/** If true, the currently selected planet on the minimap will be displayed/blinked. */
	private boolean minimapBlink;
	/** Counter for minimap blinking. */
	private int minimapBlinkCount;
	/** Minimap blink rate in animation loop count. */
	private static final int MINIMAP_BLINK_RATE = 2;
	/** The alternate selection color for selected but not focused planet or fleet. */
	private final Color alternateSelection = new Color(124, 124, 180);
	/** Enable or disable bicubic interpolation on the starmap background. */
	private ImageInterpolation interpolation = ImageInterpolation.NONE;
	/** The last rendering position. */
	private final AchievementRenderer achievementRenderer;
	/** Radar cosine table. */
	private final double[] radarSineTable;
	/** Radar sine table. */
	private final double[] radarCosineTable;
	/** Flag to indicate whether the planetary status icons should be drawn onto the starmap window. */
	public boolean displayPlanetStatusOnStarmap = true;
	/**
	 * Constructor. Sets the helper object fields.
	 * @param grm the game resource manager
	 * @param uiSound the user interface
	 * @param infobarRenderer the information bar renderer
	 * @param achievementRenderer the achievement renderer
	 */
	public StarmapRenderer(GameResourceManager grm, 
			SoundFXPlayer uiSound, InfobarRenderer infobarRenderer,
			AchievementRenderer achievementRenderer) {
		super();
		this.gfx = grm.starmapGFX;
		this.cgfx = grm.commonGFX;
		this.text = cgfx.text;
		this.uiSound = uiSound;
		this.infobarRenderer = infobarRenderer;
		this.achievementRenderer = achievementRenderer;
		setDoubleBuffered(true);
		setOpaque(true);
		//setCursor(gfx.cursors.target);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		initActions();
		fadeTimer = new Timer(FADE_INTERVAL, new ActionListener() { public void actionPerformed(ActionEvent e) { doFade(); } });
		animations = new Timer(ANIMATION_INTERVAL, new ActionListener() { public void actionPerformed(ActionEvent e) { doAnimate(); } });
		scrollAnimTimer = new Timer(SCROLL_ANIM_INTERVAL, new ActionListener() { public void actionPerformed(ActionEvent e) { doScrollAnimate(); } });
		precalculateStars();
		radarSineTable = new double[51];
		radarCosineTable = new double[51];
		precalculateRadar();
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
			float sx = hscrollValue * hscrollFactor;
			float sy = vscrollValue * vscrollFactor;
			updateRegions();
			zoom(zoomFactor);
			updateScrollKnobs();
			// scroll back to the original position
			scroll(sx / hscrollFactor, sy / vscrollFactor);
		}
		
		infobarRenderer.renderInfoBars(this, g2);
		
		Paint p = g2.getPaint();
		renderScrollbars(g2, h);
		
		g2.setPaint(p);
		
		// draw minimap
		g2.drawImage(cgfx.minimap, minimapRect.x, minimapRect.y, null);
		
		// draw the entire map in a clipping rect
		g2.setColor(cgfx.mapBackground);
		//g2.setColor(Color.YELLOW);
		g2.fill(mapRect);
		Shape sp = g2.getClip();
		g2.setClip(mapRect);
		AffineTransform af = g2.getTransform();
		int mx = -(int)(hscrollValue * hscrollFactor);
		int my = -(int)(vscrollValue * vscrollFactor); 
		// if the viewport is much bigger than the actual image, lets center it
		if (mapRect.width > cgfx.fullMap.getWidth() * zoomFactor) {
			mx = (int)((mapRect.width - cgfx.fullMap.getWidth() * zoomFactor) / 2);
		}
		if (mapRect.height > cgfx.fullMap.getHeight() * zoomFactor) {
			my = (int)((mapRect.height - cgfx.fullMap.getHeight() * zoomFactor) / 2);
		}
		
		mapCoords.x = mapRect.x + mx;
		mapCoords.y = mapRect.y + my;
		mapCoords.width = (int)(cgfx.fullMap.getWidth() * zoomFactor);
		mapCoords.height = (int)(cgfx.fullMap.getHeight() * zoomFactor);

		renderStarmapBackground(g2, mx, my);
		
		g2.setTransform(af);
		
		if (btnStars.down) {
			renderStars(g2);
		}
		
		// Render Grid
		if (btnGrids.down) {
			renderGrids(g2, mx, my);
		}
		renderPlanetsAndFleets(g2, mapRect.x + mx, mapRect.y + my);
		g2.setClip(minimapRect);
		renderMinimap(g2);
		
		g2.setClip(sp);
		
		// ----------------------------------------------------------------
		// RENDER BUTTONS
		// ----------------------------------------------------------------
		
		renderButtons(g2);
		t = System.nanoTime() - t;
		//System.out.printf("%.2f frame/s%n", 1E9 / t);
		// now darken the entire screen
		
		// render planet names
		sp = g2.getClip();
		renderPlanetListing(g2);
		
		renderFleetListing(g2);
		g2.setClip(sp);

		
		// FINAL OPERATION: draw darkening layer over the screen
		renderAlphaLayer(g2, w, h);

		achievementRenderer.renderAchievements(g2, this);

	}
	/**
	 * Renders the button images.
	 * @param g2 the graphics object
	 */
	private void renderButtons(Graphics2D g2) {
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
		
		btnEquipmentPrev.disabled = fleetListOffset == 0 || gameWorld.player.ownFleets.size() == 0;
		if (btnEquipmentPrev.disabled) {
			g2.drawImage(gfx.btnPrevDisabled, btnEquipmentPrev.rect.x, btnEquipmentPrev.rect.y, null);
		} else
		if (btnEquipmentPrev.down) {
			Rectangle r = btnEquipmentPrev.rect;
			g2.drawLine(r.x, r.y, r.x + r.width, r.y);
			g2.drawLine(r.x, r.y + r.height - 1, r.x + r.width, r.y + r.height - 1);
			g2.drawImage(gfx.btnPrevDown, r.x, r.y + 1, null);
		}
		btnEquipmentNext.disabled = fleetListOffset == gameWorld.player.ownFleets.size() - 1 || gameWorld.player.ownFleets.size() == 0;
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

		if (gameWorld.player.selectionType == StarmapSelection.FLEET && gameWorld.player.selectedFleet != null) {
			renderFleetDetails(g2, gameWorld.player.selectedFleet);
		} else 
		if (gameWorld.player.selectionType == StarmapSelection.PLANET && gameWorld.player.selectedPlanet != null) {
			renderPlanetDetails(g2, gameWorld.player.selectedPlanet);
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
	}
	/**
	 * Renders the grids.
	 * @param g2 the graphics object
	 * @param mx the x offset
	 * @param my the y offset
	 */
	private void renderGrids(Graphics2D g2, int mx, int my) {
		g2.setColor(CommonGFX.GRID_COLOR);
		Stroke st = g2.getStroke();
		//FIXME the dotted line rendering is somehow very slow
		//g2.setStroke(CommonGFX.GRID_STROKE);
		float fw = cgfx.fullMap.getWidth() * zoomFactor;
		float fh = cgfx.fullMap.getHeight() * zoomFactor;
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
	/**
	 * Renders the star pixels.
	 * @param g2 the graphics object
	 */
	private void renderStars(Graphics2D g2) {
		for (int i = 0; i < STAR_LAYER_COUNT; i++) {
			for (int j = 0; j < STAR_COUNT; j++) {
				int x = (int)(mapCoords.x - hscrollValue * hscrollFactor * (i + 1) / 10 + starsX[i * STAR_COUNT + j] * zoomFactor);
				int y = (int)(mapCoords.y - vscrollValue * vscrollFactor * (i + 1) / 10 + starsY[i * STAR_COUNT + j] * zoomFactor);
				Color c = starsColors[i * STAR_COUNT + j];
				g2.setColor(c);
				if (zoomFactor < 2) {
					g2.fillRect(x, y, 1, 1);
				} else {
					g2.fillRect(x, y, 2, 2);
				}
			}
		}
	}
	/**
	 * Renders an alpha overlay of the entire screen background.
	 * @param g2 the graphics object
	 * @param w the width
	 * @param h the height
	 */
	private void renderAlphaLayer(Graphics2D g2, int w, int h) {
		if (darkness > 0.0f) {
			Composite comp = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, darkness));
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, w, h);
			g2.setComposite(comp);
		}
	}
	/**
	 * Render fleet name listing.
	 * @param g2 the graphics object.
	 */
	private void renderFleetListing(Graphics2D g2) {
		if (gameWorld.player.selectedFleet != null) {
			g2.setClip(equipments);
			String name = gameWorld.player.selectedFleet.name;
			if (gameWorld.player.selectedFleet.owner != gameWorld.player) {
				GameFleet f =  getUISelectedFleet();
				if (f != null) {
					name = f.name;
				} else
				if (fleetListOffset < 0) {
					name = "";
				}
			}
			int fleetTextSize = 10;
			
			text.paintTo(g2, equipments.x + 1, equipments.y + (equipments.height - fleetTextSize) / 2, fleetTextSize, 
					(gameWorld.player.selectionType == StarmapSelection.FLEET
					&& gameWorld.player.selectedFleet.owner == gameWorld.player
					? TextGFX.RED : TextGFX.GREEN), 
							name);
		}
	}
	/**
	 * Renders the planet name listing.
	 * @param g2 the graphics object
	 */
	private void renderPlanetListing(Graphics2D g2) {
		g2.setClip(colonies);
		int y = colonies.y + 2;
		TreeSet<GamePlanet> plist = new TreeSet<GamePlanet>(GamePlanet.BY_NAME_ASC);
		plist.addAll(gameWorld.player.ownPlanets);
		int planetTextSize = 10;
		int pidx = 0;
		for (GamePlanet planet : plist) {
			if (y > colonies.y + colonies.height) {
				break;
			}
			if (pidx >= planetListOffset) {
				text.paintTo(g2, colonies.x + 1, y, planetTextSize, 
						planet == gameWorld.player.selectedPlanet ? TextGFX.RED : TextGFX.GREEN, planet.name);
				y += planetTextSize + 1;
			}
			pidx++;
		}
	}
	/**
	 * Render the scrollbars.
	 * @param g2 the graphics object
	 * @param h the current height
	 */
	private void renderScrollbars(Graphics2D g2, int h) {
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
		}
		if (h > 480) {
			Paint p = g2.getPaint();
			g2.setPaint(new TexturePaint(gfx.contents.rightFiller, new Rectangle(rightFillerRect.x, rightFillerRect.y, rightFillerRect.width, 2)));
			g2.fill(rightFillerRect);
			g2.setPaint(p);
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
		
		// horizontal scrollbar middle
		g2.setPaint(new TexturePaint(gfx.contents.hscrollFiller, new Rectangle(hknobRect.x + gfx.contents.hscrollLeft.getWidth(), hscrollRect.y, gfx.contents.hscrollFiller.getWidth(), gfx.contents.hscrollFiller.getHeight())));
		g2.fillRect(hknobRect.x + gfx.contents.hscrollLeft.getWidth(), hscrollRect.y, hknobRect.width - gfx.contents.hscrollLeft.getWidth() - gfx.contents.hscrollRight.getWidth(), gfx.contents.hscrollFiller.getHeight());
		g2.setPaint(new TexturePaint(gfx.contents.vscrollFiller, new Rectangle(vknobRect.x, vknobRect.y + gfx.contents.vscrollTop.getHeight(), gfx.contents.vscrollFiller.getWidth(), gfx.contents.vscrollFiller.getHeight())));
		g2.fillRect(vknobRect.x, vknobRect.y + gfx.contents.vscrollTop.getHeight(), gfx.contents.vscrollFiller.getWidth(), vknobRect.height - gfx.contents.vscrollTop.getHeight() - gfx.contents.vscrollBottom.getHeight());
	}
	/**
	 * Render starmap background image.
	 * @param g2 the graphics object
	 * @param mx the offset x
	 * @param my the offset y
	 */
	private void renderStarmapBackground(Graphics2D g2, int mx, int my) {
		Rectangle mapRectClip = mapCoords.intersection(mapRect);
		g2.setClip(mapRectClip);
		g2.translate(mapRect.x + mx, mapRect.y + my);
		
		g2.scale(zoomFactor, zoomFactor);
		// smooth scaled background
		if (interpolation != ImageInterpolation.NONE) {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation.hint);
		}
		g2.drawImage(cgfx.fullMap, 0, 0, null);
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
		
		detailsRect.setBounds(2, h - bh + 28, w - 357, 83);
		
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
		int zoomedWidth = (int)(cgfx.fullMap.getWidth() * newZoomFactor);
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
		int zoomedHeight = (int)(cgfx.fullMap.getHeight() * newZoomFactor);
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
		if (e.getButton() == MouseEvent.BUTTON3 && minimapRect.contains(pt)) {
			doMinimapScrollAnimated(pt);
		} else
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
		// if an animated scrolling is running, disable user scrolling
		if (scrollAnimTimer.isRunning()) {
			return;
		}
		Point pt = e.getPoint();
		if (e.getButton() == MouseEvent.BUTTON1 && minimapRect.contains(pt)) {
			doMinimapScroll(pt);
		} else
		if (equipments.contains(pt)) {
			doEquipmentSelect(e.getButton());
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
				for (Btn b : releaseButtons) {
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
				// check for clicks in the main map area
				if (mapCoords.contains(pt)) {
					checkGameObjects(e);
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
		Point pt = e.getPoint();
		for (Btn b : releaseButtons) {
			needRepaint |= b.down;
			b.down = false;
			if (b.test(pt)) {
				b.click();
			}
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
		releaseButtons.add(btnColony);
		btnColonyPrev = new Btn(new BtnAction() { public void invoke() { doColonyPrev(); } });
		releaseButtons.add(btnColonyPrev);
		btnColonyNext = new Btn(new BtnAction() { public void invoke() { doColonyNext(); } });
		releaseButtons.add(btnColonyNext);
		btnEquipment = new Btn(new BtnAction() { public void invoke() { doEquipmentClick(); } });
		releaseButtons.add(btnEquipment);
		btnEquipmentPrev = new Btn(new BtnAction() { public void invoke() { doEquipmentPrevClick(); } });
		releaseButtons.add(btnEquipmentPrev);
		btnEquipmentNext = new Btn(new BtnAction() { public void invoke() { doEquipmentNextClick(); } });
		releaseButtons.add(btnEquipmentNext);
		btnInfo = new Btn(new BtnAction() { public void invoke() { doInfoClick(); } });
		releaseButtons.add(btnInfo);
		btnBridge = new Btn(new BtnAction() { public void invoke() { doBridgeClick(); } });
		releaseButtons.add(btnBridge);
		btnColonize = new Btn();
		releaseButtons.add(btnColonize);
		btnName = new Btn(new BtnAction() { @Override public void invoke() { doNameChange(); } });
		releaseButtons.add(btnName);
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
		repaint(); // FIXME repaint performance
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
			renderPlanetRadar(g2, xOrig, yOrig, ri, player);
		}
		// render known planets by name labels
		if ((nameMode == 1 || nameMode == 3)) {
			renderPlanetNames(g2, xOrig, yOrig, player);
		}
		
		for (GamePlanet p : player.knownPlanets) {
			if (!p.visible) {
				continue;
			}
			BufferedImage pimg = getPlanetImage(p, p.rotationPhase);
			int x = (int)(p.x * zoomFactor - pimg.getWidth() / 2f);
			int y = (int)(p.y * zoomFactor - pimg.getHeight() / 2f);
			
			// if this planet is selected
			if (player.selectedPlanet == p) {
				int len = (int)Math.max(1, 1 * zoomFactor);
				g2.setColor(player.selectionType == StarmapSelection.PLANET ? Color.WHITE : alternateSelection);
				g2.drawLine(xOrig + x - 1, yOrig + y - 1, xOrig + x + len - 1, yOrig + y - 1);
				g2.drawLine(xOrig + x + pimg.getWidth(), yOrig + y - 1, xOrig + x + pimg.getWidth() - len, yOrig + y - 1);
				g2.drawLine(xOrig + x - 1, yOrig + y + pimg.getHeight(), xOrig + x + len - 1, yOrig + y + pimg.getHeight());
				g2.drawLine(xOrig + x + pimg.getWidth(), yOrig + y + pimg.getHeight(), 
						xOrig + x + pimg.getWidth() - len, yOrig + y + pimg.getHeight());
				
				g2.drawLine(xOrig + x - 1, yOrig + y - 1, xOrig + x - 1, yOrig + y + len - 1);
				g2.drawLine(xOrig + x + pimg.getWidth(), yOrig + y - 1, xOrig + x + pimg.getWidth(), yOrig + y  + len - 1);
				g2.drawLine(xOrig + x - 1, yOrig + y + pimg.getHeight(), xOrig + x - 1, yOrig + y + pimg.getHeight() - len);
				g2.drawLine(xOrig + x + pimg.getWidth(), yOrig + y + pimg.getHeight(),
						xOrig + x + pimg.getWidth(), yOrig + y + pimg.getHeight() - len);
			}
			
			g2.drawImage(pimg, xOrig + x, yOrig + y, null);
		}
		// render fleet radars
		if (btnRadars.down && btnFleets.down) {
			renderFleetRadar(g2, xOrig, yOrig, ri, player);
		}
		// render fleet names
		if (btnFleets.down && (nameMode == 2 || nameMode == 3)) {
			renderFleetNames(g2, xOrig, yOrig, player);
		}
		// render fleet icons
		if (btnFleets.down) {
			renderFleetIcons(g2, xOrig, yOrig, player);
		}
	}
	/**
	 * Renders the fleet icons onto the starmap surface.
	 * @param g2 the graphics object
	 * @param xOrig the rendering X origin
	 * @param yOrig the rendering Y origin
	 * @param player the player object
	 */
	private void renderFleetIcons(Graphics2D g2, int xOrig, int yOrig,
			GamePlayer player) {
		for (GameFleet f : player.knownFleets) {
			if (f.visible) {
				BufferedImage fleetImg = cgfx.shipImages[f.owner.fleetIcon];
				int x = (int)(f.x * zoomFactor - fleetImg.getWidth() / 2);
				int y = (int)(f.y * zoomFactor - fleetImg.getHeight() / 2);
				g2.drawImage(fleetImg, xOrig + x, yOrig + y, null);
				if (player.selectedFleet == f) {
					g2.setColor(player.selectionType == StarmapSelection.FLEET ? Color.WHITE : alternateSelection);
					g2.drawRect(xOrig + x - 1, yOrig + y - 1,
							fleetImg.getWidth() + 2, fleetImg.getHeight() + 2);
				}					
			}
		}
	}
	/**
	 * Renders the fleet names onto the starmap screen.
	 * @param g2 the graphics object
	 * @param xOrig the rendering X origin
	 * @param yOrig the rendering Y origin
	 * @param player the player object
	 */
	private void renderFleetNames(Graphics2D g2, int xOrig, int yOrig,
			GamePlayer player) {
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
				// update cached name image if necessary
				if (f.nameImage == null) {
					int len = text.getTextWidth(5, f.name);
					f.nameImage = new BufferedImage(len, 5, BufferedImage.TYPE_INT_ARGB);
					Graphics2D tg = f.nameImage.createGraphics();
					text.paintTo(tg, 0, 0, 5, color, f.name);
					tg.dispose();
				}
				g2.drawImage(f.nameImage, xOrig + x, yOrig + y, null);
				//text.paintTo(g2, xOrig + x, yOrig + y, 5, color, f.name);
			}
		}
	}
	/**
	 * Renders the fleet radar.
	 * @param g2 the graphics object
	 * @param xOrig the origin X
	 * @param yOrig the origin Y
	 * @param ri the radar dot image for the current zoom level
	 * @param player the player
	 */
	private void renderFleetRadar(Graphics2D g2, int xOrig, int yOrig,
			BufferedImage ri, GamePlayer player) {
		for (GameFleet fleet : player.ownFleets) {
			float radarRadius = fleet.getRadarRadius();
			if (fleet.visible && radarRadius > 0) {
				for (int i = 0; i < radarSineTable.length; i++) {
					int x = (int)((fleet.x + radarRadius * radarCosineTable[i]) * zoomFactor);
					int y = (int)((fleet.y + radarRadius * radarSineTable[i]) * zoomFactor);
					g2.drawImage(ri, xOrig + x - 1, yOrig + y - 1, null);
				}
			}
		}
	}
	/**
	 * Renders planet names onto the starmap.
	 * @param g2 the graphics object
	 * @param xOrig the origin X
	 * @param yOrig the origin Y
	 * @param player the player object
	 */
	private void renderPlanetNames(Graphics2D g2, int xOrig, int yOrig,
			GamePlayer player) {
		for (GamePlanet p : player.knownPlanetsByName) {
			BufferedImage pimg = getPlanetImage(p, p.rotationPhase);
			int y = (int)(p.y * zoomFactor + pimg.getHeight() / 2f) + 2;
			int w = text.getTextWidth(5, p.name);
			int x = (int)(p.x * zoomFactor - w / 2f);
			if (p.nameImage == null || p.nameOwner != p.owner) {
				p.nameOwner = p.owner;
				int color = TextGFX.GRAY;
				if (p.owner != null) {
					color = p.owner.race.smallColor;
				}
				int len = text.getTextWidth(5, p.name);
				p.nameImage = new BufferedImage(len, 5, BufferedImage.TYPE_INT_ARGB);
				Graphics2D tg = p.nameImage.createGraphics();
				text.paintTo(tg, 0, 0, 5, color, p.name);
				tg.dispose();
			}
			g2.drawImage(p.nameImage, xOrig + x, yOrig + y, null);
//			text.paintTo(g2, xOrig + x, yOrig + y, 5, color, p.name);
			// render problematic area icons backwards
			if (displayPlanetStatusOnStarmap && p.owner == gameWorld.player) {
				y = yOrig + (int)(p.y * zoomFactor - pimg.getHeight() / 2f) - 12;
				x = xOrig + (int)(p.x * zoomFactor);
				if (mapCoords.intersects(new Rectangle(x - 30, y, 60, 12))) {
					int count = 0;
					PlanetStatus ps = p.getStatus();
					boolean energyIcon = ps.energyDemand > ps.energyProduction;
					count += energyIcon ? 1 : 0;
					boolean foodIcon = ps.population > ps.food;
					count += foodIcon ? 1 : 0;
					boolean hospitalIcon = ps.population > ps.hospital;
					count += hospitalIcon ? 1 : 0;
					boolean workerIcon = ps.population < ps.workerDemand;
					count += workerIcon ? 1 : 0;
					boolean livingspaceIcon = ps.population > ps.livingSpace;
					count += livingspaceIcon ? 1 : 0;
					if (count > 0) {
						x -= (count * 12) / 2;
						if (count == 1) {
							x++;
						}
						if (energyIcon) {
							g2.drawImage(cgfx.energyIcon, x, y, null);
							x += 12;
						}
						if (foodIcon) {
							g2.drawImage(cgfx.foodIcon, x, y, null);
							x += 12;
						}
						if (hospitalIcon) {
							g2.drawImage(cgfx.hospitalIcon, x, y, null);
							x += 12;
						}
						if (workerIcon) {
							g2.drawImage(cgfx.workerIcon, x, y, null);
							x += 12;
						}
						if (livingspaceIcon) {
							g2.drawImage(cgfx.livingSpaceIcon, x, y, null);
							x += 12;
						}
					}
				}
			}
		}
	}
	/**
	 * Renders the planetary radar onto the starmap.
	 * @param g2 the graphics object
	 * @param xOrig the X origin
	 * @param yOrig the Y origin
	 * @param ri the radar dot for the current zoom level
	 * @param player the player object
	 */
	private void renderPlanetRadar(Graphics2D g2, int xOrig, int yOrig,
			BufferedImage ri, GamePlayer player) {
		// render radar circle only for the own planets
		for (GamePlanet p : player.ownPlanets) {
			int rr = p.getRadarRadius();
			if (p.visible && rr > 0) {
				for (int i = 0; i < radarSineTable.length; i++) {
					int x = (int)((p.x + rr * radarCosineTable[i]) * zoomFactor);
					int y = (int)((p.y + rr * radarSineTable[i]) * zoomFactor);
					if (mapCoords.contains(xOrig + x - 1, yOrig + y - 1, 3, 3)) {
						g2.drawImage(ri, xOrig + x - 1, yOrig + y - 1, null);
					}
				}
			}
		}
	}
	/**
	 * Perform animations. Rotate planets, move fleets.
	 */
	private void doAnimate() {
		// animate rotations on starmap.
		for (GamePlanet p : gameWorld.planets) {
			if (!p.visible) {
				continue;
			}
			int phaseCount = /* gfx.starmapPlanets.get(p.surfaceType.planetString) */ p.rotations.get(planetSizes[magnifyIndex]).size();
			if (p.rotationDirection) {
				p.rotationPhase = (p.rotationPhase + 1) % phaseCount;
			} else {
				p.rotationPhase--;
				if (p.rotationPhase < 0) {
					p.rotationPhase = phaseCount - 1;
				}
			}
		}
		// blink toggle minimap planet selection indicator display
		minimapBlinkCount++;
		if (minimapBlinkCount >= MINIMAP_BLINK_RATE) {
			minimapBlinkCount = 0;
			minimapBlink = !minimapBlink;
		}
		repaint(); // repaint performance
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
		double w2 = cgfx.minimap.getWidth() * 1.0 / cgfx.fullMap.getWidth();
		double h2 = cgfx.minimap.getHeight() * 1.0 / cgfx.fullMap.getHeight();
		GamePlayer player = gameWorld.player;
		for (GamePlanet p : player.knownPlanets) {
			int color = TextGFX.GRAY;
			if (player.knownPlanetsByName.contains(p) && p.owner != null) {
				color = p.owner.race.smallColor;
			}
			// draw the planet dot onto the minimap
			int x2 = (int)(p.x * w2);
			int y2 = (int)(p.y * h2);
			g2.setColor(new Color(color));
			if (p != player.selectedPlanet || minimapBlink) {
				g2.fillRect(minimapRect.x + x2 - 1, minimapRect.y + y2 - 1, 3, 3);
			}
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
		float w2 = cgfx.minimap.getWidth() * 1.0f / cgfx.fullMap.getWidth();
		float h2 = cgfx.minimap.getHeight() * 1.0f / cgfx.fullMap.getHeight();
		float w0 = Math.min((mapRect.width * w2 / zoomFactor), minimapRect.width);
		float h0 = Math.min((mapRect.height * h2 / zoomFactor), minimapRect.height);
		
		float dx = (pt.x - minimapRect.x - w0 / 2) / hscrollFactor * zoomFactor / w2;
		float dy = (pt.y - minimapRect.y - h0 / 2) / vscrollFactor * zoomFactor / h2;
		scroll(dx, dy);
	}
	/**
	 * Precalculates the star background locations and colors.
	 */
	private void precalculateStars() {
		Random random = new Random(0);
		for (int i = 0; i < STAR_LAYER_COUNT; i++) {
			int fw = cgfx.fullMap.getWidth() * (10 + i + 1) / 10;
			int fh = cgfx.fullMap.getHeight() * (10 + i + 1) / 10;
			for (int j = 0; j < STAR_COUNT; j++) {
				starsX[i * STAR_COUNT + j] = random.nextInt(fw);
				starsY[i * STAR_COUNT + j] = random.nextInt(fh);
				starsColors[i * STAR_COUNT + j] = new Color(cgfx.mixColors(startStars, endStars, random.nextFloat()));
			}
		}
	}
	/**
	 * Returns the logical coordinates denoted by
	 * the given mouse/screen coordinates of the full map.
	 * @param p the screen point
	 * @return the logical equivalent point.
	 */
	public Point screenToLogical(Point p) {
		Rectangle rect = mapCoords.intersection(mapRect);
		float x = (p.x - rect.x + hscrollValue * hscrollFactor) / zoomFactor;
		float y = (p.y - rect.y + hscrollValue * hscrollFactor) / zoomFactor;
		return new Point((int)x, (int)y);
	}
	/**
	 * Convert logical coordinate to current screen rendering coordinate.
	 * @param p the logical point
	 * @param viewPort the actual rendering viewport
	 * @return the screen point
	 */
	public Point logicalToScreen(Point p, Rectangle viewPort) {
		float x = (p.x) * zoomFactor - hscrollValue * hscrollFactor + viewPort.x;
		float y = (p.y) * zoomFactor - vscrollValue * vscrollFactor + viewPort.y;
		return new Point((int)x, (int)y);
	}
	/**
	 * Returns a planet image phase based on the current magnification settings.
	 * @param planet the planet
	 * @param phase the rotation phase
	 * @return the image
	 */
	public BufferedImage getPlanetImage(GamePlanet planet, int phase) {
		int modifiedMagnify = magnifyIndex + planet.size;
		if (modifiedMagnify < 0) {
			modifiedMagnify = 0;
		}
		return /* gfx.starmapPlanets.get(planet.surfaceType.planetString) */planet.rotations.get(planetSizes[modifiedMagnify]).get(phase);
	}
	/** 
	 * Check if the point is on a fleet or planet. 
	 * @param e the original mouse event
	 */
	public void checkGameObjects(MouseEvent e) {
		Point point = e.getPoint();
		Rectangle viewPort = mapCoords.intersection(mapRect);
		if (viewPort.contains(point)) {
			if (btnFleets.down && !e.isShiftDown()) {
				// fleets can overlap
				// check for own fleets
				if (!e.isControlDown()) {
					for (GameFleet f : gameWorld.player.ownFleets) {
						if (checkFleetSelected(point, viewPort, f)) {
							gameWorld.player.selectionType = StarmapSelection.FLEET;
							gameWorld.player.selectedFleet = f;
							fleetListOffset = gameWorld.getOwnFleetsByName().indexOf(f);
							return;
						}
					}
				}
				// check for other fleets
				for (GameFleet f : gameWorld.player.knownFleets) {
					if (checkFleetSelected(point, viewPort, f)) {
						gameWorld.player.selectionType = StarmapSelection.FLEET;
						gameWorld.player.selectedFleet = f;
						return;
					}
				}
			}
			if (!e.isControlDown()) {
				for (GamePlanet p : gameWorld.player.knownPlanets) {
					Point pt = logicalToScreen(p.getPoint(), viewPort);
					BufferedImage planetImg = getPlanetImage(p, 0);
					Rectangle rect = new Rectangle(pt.x - planetImg.getWidth() / 2, 
							pt.y - planetImg.getHeight() / 2, planetImg.getWidth(), planetImg.getHeight());
					if (rect.contains(point)) {
						gameWorld.player.selectionType = StarmapSelection.PLANET;
						gameWorld.player.selectedPlanet = p;
						if (e.getClickCount() == 2) {
							doColonyClick();
						}
						return;
					}
				}
			}
			gameWorld.player.selectionType = null;
			repaint();
		}
	}
	/**
	 * Check if the given fleet was selected.
	 * @param point the screen point
	 * @param viewPort the viewport
	 * @param f the fleet to check
	 * @return true if this fleet was selected
	 */
	private boolean checkFleetSelected(Point point, Rectangle viewPort, GameFleet f) {
		Point pt = logicalToScreen(new Point(f.x, f.y), viewPort);
		BufferedImage fleetImg = cgfx.shipImages[f.owner.fleetIcon];
		Rectangle rect = new Rectangle(pt.x - fleetImg.getWidth() / 2 - 1, 
				pt.y - fleetImg.getHeight() / 2, fleetImg.getWidth() + 2, fleetImg.getHeight() + 3);
		if (rect.contains(point)) {
			return true;
		}
		return false;
	}
	/**
	 * Center the main viewport around the given logical location.
	 * @param pt the point
	 */
	public void scrollToLogical(final Point pt) {
		Rectangle viewPort = mapCoords.intersection(mapRect);
		if (viewPort.width > 0) {
			float dx = (pt.x * zoomFactor - viewPort.width / 2f) / hscrollFactor;
			float dy = (pt.y * zoomFactor - viewPort.height / 2f) / vscrollFactor;
			scroll(dx, dy);
		} else {
			Parallels.runDelayedInEDT(100, new Runnable() {
				@Override
				public void run() {
					scrollToLogical(pt);							
				}
			});
		}
	}
	/**
	 * Center the main viewport around the given logical location.
	 * @param pt the point
	 */
	public void scrollToLogicalAnimated(final Point pt) {
		Rectangle viewPort = mapCoords.intersection(mapRect);
		if (viewPort.width > 0) {
			float dx = (pt.x * zoomFactor - viewPort.width / 2f) / hscrollFactor;
			float dy = (pt.y * zoomFactor - viewPort.height / 2f) / vscrollFactor;
			if (!scrollAnimTimer.isRunning()) {
				scrollDestX = Math.max(0, Math.min(hscrollMax, dx));
				scrollDestY = Math.max(0, Math.min(vscrollMax, dy));
				scrollAnimTimer.start();
			}
		} else {
			// schedule a re-invoke for this operation if the starmap renderer is not ready.
			Parallels.runDelayedInEDT(100, new Runnable() {
				@Override
				public void run() {
					scrollToLogicalAnimated(pt);							
				}
			});
		}
	}
	/** Perform the animated scrolling. */
	public void doScrollAnimate() {
		float dx = scrollDestX - hscrollValue;
		float dy = scrollDestY - vscrollValue;
		float distanceToDest = (dx * dx + dy * dy);
		if (distanceToDest < SCROLL_ANIM_SPEED * SCROLL_ANIM_SPEED) {
			scroll(scrollDestX, scrollDestY);
			scrollAnimTimer.stop();
		} else {
			double theta = Math.atan2(dy, dx);
			scroll(hscrollValue + (float)Math.cos(theta) * SCROLL_ANIM_SPEED, vscrollValue + (float)Math.sin(theta) * SCROLL_ANIM_SPEED);
		}
	}
	/**
	 * Center the main viewport around the given minimap location.
	 * @param pt the point
	 */
	private void doMinimapScrollAnimated(Point pt) {
		// get the current location
		float dx = (pt.x - minimapRect.x) * cgfx.fullMap.getWidth() / minimapRect.width;
		float dy = (pt.y - minimapRect.y) * cgfx.fullMap.getHeight() / minimapRect.height;
		scrollToLogicalAnimated(new Point((int)dx, (int)dy));
	}
	/** Close all animation timers. */
	public void close() {
		// close all all animations
		fadeTimer.stop();
		animations.stop();
		scrollAnimTimer.stop();
	}
	/**
	 * Scroll through the fleet names.
	 */
	public void doEquipmentPrevClick() {
		fleetListOffset = Math.max(0, fleetListOffset - 1);
		gameWorld.player.selectedFleet = gameWorld.getOwnFleetsByName().get(fleetListOffset);
		repaint();
	}
	/** Scroll through the fleet names forward. */
	public void doEquipmentNextClick() {
		fleetListOffset = Math.min(gameWorld.player.ownFleets.size() - 1, fleetListOffset + 1);
		gameWorld.player.selectedFleet = gameWorld.getOwnFleetsByName().get(fleetListOffset);
		repaint();
	}
	/**
	 * Perform equipment selection operations.
	 * @param button the current mouse button
	 */
	private void doEquipmentSelect(int button) {
		if (button == MouseEvent.BUTTON1) {
			// select the fleet
			gameWorld.player.selectionType = StarmapSelection.FLEET;
			// if the currently selected fleet is not the player's fleet
			if (gameWorld.player.selectedFleet != null
					&& gameWorld.player.selectedFleet.owner != gameWorld.player) {
				// then set the selected fleet back to the currently displayed fleet
				gameWorld.player.selectedFleet = getUISelectedFleet();
			}
		} else
		if (button == MouseEvent.BUTTON2) {
			if (gameWorld.player.selectedFleet != null) {
				if (gameWorld.player.selectedFleet.owner != gameWorld.player) {
					GameFleet f = getUISelectedFleet();
					if (f != null) {
						scrollToLogicalAnimated(f.getPoint());
					}
				} else {
					scrollToLogicalAnimated(gameWorld.player.selectedFleet.getPoint());
				}
			}
		} else
		if (button == MouseEvent.BUTTON3) {
			if (gameWorld.player.selectedFleet != null) {
				if (gameWorld.player.selectedFleet.owner != gameWorld.player) {
					GameFleet f = getUISelectedFleet();
					if (f != null) {
						scrollToLogical(f.getPoint());
					}
				} else {
					scrollToLogical(gameWorld.player.selectedFleet.getPoint());
				}
			}
		}
	}
	/**
	 * @return the game fleet currently in the equipment list box or null
	 */
	public GameFleet getUISelectedFleet() {
		if (fleetListOffset >= 0 && fleetListOffset < gameWorld.player.ownFleets.size()) {
			return gameWorld.getOwnFleetsByName().get(fleetListOffset);
		}
		return null;
	}
	/**
	 * Render planet details.
	 * @param g2 the graphics object
	 * @param selectedPlanet the selected planet
	 */
	private void renderPlanetDetails(Graphics2D g2, GamePlanet selectedPlanet) {
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
		
		Shape sp = g2.getClip();
		g2.setClip(detailsRect);
		
		text.paintTo(g2, detailsRect.x + 6, detailsRect.y + 4, 14, TextGFX.RED, selectedPlanet.name);
		if (selectedPlanet.owner != null) {
			text.paintTo(g2, detailsRect.x + 6, detailsRect.y + 23, 10, TextGFX.GREEN, selectedPlanet.owner.name);
			// if planet is own by the current player
			if (gameWorld.player == selectedPlanet.owner) {
				// or planet has any satellite
				text.paintTo(g2, detailsRect.x + 6, detailsRect.y + 40, 10, TextGFX.GREEN, 
						gameWorld.getLabel("PopulationRaceOnSurface", 
						gameWorld.getLabel("RaceNames." + selectedPlanet.populationRace.id), 
						gameWorld.getLabel("SurfaceTypeNames." + selectedPlanet.surfaceType.planetXmlString)));
				// has hubble or spy sattellite 2
				text.paintTo(g2, detailsRect.x + 6, detailsRect.y + 57, 10, TextGFX.GREEN, 
						gameWorld.getLabel("PopulationStatus",
						selectedPlanet.population,
						gameWorld.getLabel("PopulatityName." + PopularityType.find(selectedPlanet.popularity).id), 
						selectedPlanet.populationGrowth)
						+ ", " + gameWorld.getLabel("TaxRate." + selectedPlanet.tax.id)
				);
				
				// render problematic area icons backwards
				int x = detailsRect.x + detailsRect.width - 15;
				int y = detailsRect.y + 69;
				PlanetStatus ps = selectedPlanet.getStatus();
				if (ps.energyDemand > ps.energyProduction) {
					g2.drawImage(cgfx.energyIcon, x, y, null);
					x -= 12;
				}
				if (ps.population > ps.food) {
					g2.drawImage(cgfx.foodIcon, x, y, null);
					x -= 12;
				}
				if (ps.population > ps.hospital) {
					g2.drawImage(cgfx.hospitalIcon, x, y, null);
					x -= 12;
				}
				if (ps.population < ps.workerDemand) {
					g2.drawImage(cgfx.workerIcon, x, y, null);
					x -= 12;
				}
				if (ps.population > ps.livingSpace) {
					g2.drawImage(cgfx.livingSpaceIcon, x, y, null);
					x -= 12;
				}
			}
		} else {
			text.paintTo(g2, detailsRect.x + 6, detailsRect.y + 23, 10, TextGFX.GREEN, gameWorld.getLabel("EmpireNames.Empty"));
			text.paintTo(g2, detailsRect.x + 6, detailsRect.y + 40, 10, TextGFX.GREEN, 
					gameWorld.getLabel("NoRaceOnSurface", 
					gameWorld.getLabel("SurfaceTypeNames." + selectedPlanet.surfaceType.planetXmlString)));
		}
		StringBuilder b = new StringBuilder();
		for (String s : selectedPlanet.inOrbit) {
			if (b.length() > 0) {
				b.append(", ");
			}
			b.append(s);
		}
		text.paintTo(g2, detailsRect.x + 6, detailsRect.y + 72, 7, TextGFX.GREEN, b.toString());
		
		g2.setClip(sp);
	}
	/**
	 * Render fleet details.
	 * @param g2 the graphics object
	 * @param selectedFleet the selected fleet
	 */
	private void renderFleetDetails(Graphics2D g2, GameFleet selectedFleet) {
		if (btnMove.down) {
			g2.drawImage(gfx.btnMoveLight, btnMove.rect.x, btnMove.rect.y, null);
		}
		if (btnAttack.down) {
			g2.drawImage(gfx.btnAttackLight, btnAttack.rect.x, btnAttack.rect.y, null);
		}
		if (btnStop.down) {
			g2.drawImage(gfx.btnStopLight, btnStop.rect.x, btnStop.rect.y, null);
		}
		
		Shape sp = g2.getClip();
		g2.setClip(detailsRect);
		
		text.paintTo(g2, detailsRect.x + 6, detailsRect.y + 4, 14, TextGFX.RED, selectedFleet.name);
		text.paintTo(g2, detailsRect.x + 6, detailsRect.y + 23, 10, TextGFX.GREEN, selectedFleet.owner.name);
		String speedStr = gameWorld.getLabel("SpeedValue", selectedFleet.getSpeed());
		int speedLen = text.getTextWidth(7, speedStr);
		String firepowerStr = gameWorld.getLabel("FirepowerValue", selectedFleet.getFirepower());
		int firepowerLen = text.getTextWidth(7, firepowerStr);
		int len = Math.max(speedLen, firepowerLen);
		text.paintTo(g2, detailsRect.x + detailsRect.width - len - 10, detailsRect.y + 23, 7, TextGFX.GREEN, speedStr);
		if (selectedFleet.owner == gameWorld.player) {
			text.paintTo(g2, detailsRect.x + detailsRect.width - len - 10, detailsRect.y + 32, 7, TextGFX.GREEN, firepowerStr);
			
			text.paintTo(g2, detailsRect.x + 6, detailsRect.y + 40, 10, TextGFX.GREEN, 
					gameWorld.getLabel("FleetStatus." + selectedFleet.status.id));
			
			text.paintTo(g2, detailsRect.x + 6, detailsRect.y + 57, 10, TextGFX.GREEN, 
					gameWorld.getLabel("PlanetNearBy", "----")); // TODO find nearby planet
			
			int battleships = selectedFleet.getBattleshipCount();
			int destroyers = selectedFleet.getDestroyerCount();
			int fighters = selectedFleet.getFighterCount();
			int tanks = selectedFleet.getTankCount();
			
			text.paintTo(g2, detailsRect.x + 6, detailsRect.y + 72, 7, TextGFX.GREEN, 
				gameWorld.getLabel("FleetStatistics",
					battleships > 0 ? String.valueOf(battleships) : "-",
					destroyers > 0 ? String.valueOf(destroyers) : "-",
					fighters > 0 ? String.valueOf(fighters) : "-",
					tanks > 0 ? String.valueOf(tanks) : "-"
			));
		}
		
		g2.setClip(sp);
	}
	/**
	 * @param interpolation the image interpolation to set
	 */
	public void setInterpolation(ImageInterpolation interpolation) {
		this.interpolation = interpolation;
		repaint();
	}
	/**
	 * @return the current image interpolation
	 */
	public ImageInterpolation getInterpolation() {
		return interpolation;
	}
	/** Precalculates the radar circle's sine and cosine table. */
	private void precalculateRadar() {
		double angle = 0;
		for (int i = 0; i < radarSineTable.length; i++) {
			radarSineTable[i] = Math.sin(angle);
			radarCosineTable[i] = Math.cos(angle);
			angle = i * Math.PI / 25;
		}
		
	}
}
