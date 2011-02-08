/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.xold.render;

import hu.openig.xold.core.Btn;
import hu.openig.xold.core.BtnAction;
import hu.openig.core.ImageInterpolation;
import hu.openig.core.Location;
import hu.openig.xold.core.PopularityType;
import hu.openig.core.RoadType;
import hu.openig.core.Sides;
import hu.openig.xold.core.Tile;
import hu.openig.xold.core.TileFragment;
import hu.openig.xold.core.TileStatus;
import hu.openig.xold.core.Tuple2;
import hu.openig.xold.model.GameBuilding;
import hu.openig.xold.model.GameBuildingPrototype;
import hu.openig.xold.model.GamePlanet;
import hu.openig.xold.model.GamePlayer;
import hu.openig.xold.model.GameWorld;
import hu.openig.xold.model.PlanetStatus;
import hu.openig.xold.model.ResourceAllocator;
import hu.openig.xold.model.GameBuildingPrototype.BuildingImages;
import hu.openig.xold.res.GameResourceManager;
import hu.openig.xold.res.gfx.CommonGFX;
import hu.openig.xold.res.gfx.PlanetGFX;
import hu.openig.xold.res.gfx.TextGFX;
import hu.openig.sound.SoundFXPlayer;
import hu.openig.utils.PACFile.PACEntry;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	/** Contains the rectangle to highlight. */
	Rectangle tilesToHighlight;
	/** The rendering X offset. */
	int xoff = 56;
	/** The rendering Y offset. */
	int yoff = 27;
	/** Last mouse X coordinate. */
	int lastx;
	/** Last mouse Y coordinate. */
	int lasty;
	/** Panning the screen. */
	boolean panMode;
	/** Empty surface map array. */
	private static final byte[] EMPTY_SURFACE_MAP = new byte[65 * 65 * 2 + 4];
	/** The planet graphics. */
	private final PlanetGFX gfx;
	/** The common graphics. */
	private final CommonGFX cgfx;
	/** Rectangle for. */
	private Rectangle leftTopRect = new Rectangle();
	/** Rectangle for. */
	private Rectangle leftFillerRect = new Rectangle();
	/** Rectangle for. */
	private Rectangle leftBottomRect = new Rectangle();
	
	/** Rectangle for. */
	private Rectangle rightTopRect = new Rectangle();
	/** Rectangle for. */
	private Rectangle rightFillerRect = new Rectangle();
	/** Rectangle for. */
	private Rectangle rightBottomRect = new Rectangle();
	
	/** Button for buildable building list. */
	private Btn btnBuilding;
	/** Button for. */
	private Btn btnRadar;
	/** Button for. */
	private Btn btnBuildingInfo;
	/** Button for. */
	private Btn btnButtons;
	/** Button for. */
	private Btn btnColonyInfo;
	/** Button for. */
	private Btn btnPlanet;
	/** Button for. */
	private Btn btnStarmap;
	/** Button for. */
	private Btn btnBridge;
	/** The planet details button. */
	private Btn btnPlanetDetails;
	/** The middle window for the surface drawing. */
	private Rectangle mainWindow = new Rectangle();
	
	/** Rectangle for. */
	private Rectangle buildPanelRect = new Rectangle();
	/** Rectangle for. */
	private Rectangle radarPanelRect = new Rectangle();
	/** Rectangle for the building information panel. */
	private Rectangle buildingInfoPanelRect = new Rectangle();
	/** The last width. */
	private int lastWidth;
	/** The last height. */
	private int lastHeight;
	/** The left filler painter. */
	private TexturePaint leftFillerPaint;
	/** The right filler painter. */
	private TexturePaint rightFillerPaint;
	/** 
	 * The timer to scroll the building window if the user holds down the left mouse button on the
	 * up/down arrow.
	 */
	private Timer buildScroller;
	/** The scroll interval. */
	private static final int BUILD_SCROLL_WAIT = 500;
	/** The scroll interval. */
	private static final int BUILD_SCROLL_INTERVAL = 250;
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
	/** The daylight factor for the planetary surface only. 1=No darkness, 0=Full darkness. */
	private float daylight = 1f;
	/** The text renderer. */
	private TextGFX text;
	/** The user interface sounds. */
	private SoundFXPlayer uiSound;
	/** Buttons which change state on click.*/
	private final List<Btn> toggleButtons = new ArrayList<Btn>();
	/** The press buttons. */
	private final List<Btn> pressButtons = new ArrayList<Btn>();
	/** The buttons which fire on mouse release. */
	private final List<Btn> releaseButtons = new ArrayList<Btn>();
	/** Event for starmap click. */
	private BtnAction onStarmapClicked;
	/** Event for information click. */
	private BtnAction onInformationClicked;
	/** Event for information click. */
	private BtnAction onListClicked;
	/** Event for bridge click. */
	private BtnAction onBridgeClicked;
	/** Event for planets click. */
	private BtnAction onPlanetsClicked;
	/** The game world. */
	private GameWorld gameWorld;
	/** The information bar renderer. */
	private InfobarRenderer infobarRenderer;
	/** The last rendering position. */
	private final AchievementRenderer achievementRenderer;
	/** Build image rectangle. */
	private final Rectangle buildImageRect = new Rectangle();
	/** Build image rectangle. */
	private final Rectangle buildNameRect = new Rectangle();
	/** Button for next building. */
	private Btn btnBuildNext;
	/** Button for previous building. */
	private Btn btnBuildPrev;
	/** Button for build. */
	private Btn btnBuild;
	/** Button for building list. */
	private Btn btnList;
	/** We are currently in build mode. */
	private boolean buildMode;
	/** The building info panel name region. */
	private final Rectangle buildingInfoName = new Rectangle();
	/** The demolish building button. */
	private Btn btnDemolish;
	/** The radar image cached. */
	private BufferedImage radarImage;
	/** The last planet to which the radar image belongs. */
	private GamePlanet radarImagePlanet;
	/** The minimap rectangle. */
	private Rectangle minimapRect = new Rectangle();
	/** Enable or disable bicubic interpolation on the starmap background. */
	private ImageInterpolation interpolation = ImageInterpolation.NONE;
	/** The current mouse tile. */
	private int mouseTileX;
	/** The current mouse tile. */
	private int mouseTileY;
	/** The status icon blinking rate. */
	private static final int BLINK_TIMER_VALUE = 500;
	/** The timer for blinking status icons. */
	private Timer blinkTimer;
	/** The status icon blinking phase. */
	private boolean blinkStatus;
	/** Energy percent rectangle. */
	private final Rectangle energyPercentRect = new Rectangle();
	/** Energy rectangle. */
	private final Rectangle energyRect = new Rectangle();
	/** Worker percent rectangle. */
	private final Rectangle workerPercentRect = new Rectangle();
	/** Worker rectangle. */
	private final Rectangle workerRect = new Rectangle();
	/** Operation percent rectangle. */
	private final Rectangle operationPercentRect = new Rectangle();
	/** Production rectangle. */
	private final Rectangle productionRect = new Rectangle();
	/** The active status toggle button. */
	private Btn btnActive;
	/** The repair status toggle button. */
	private Btn btnRepair;
	/**
	 * Constructor, expecting the planet graphics and the common graphics objects.
	 * @param grm the game resource manager 
	 * @param uiSound the user interface sounds.
	 * @param infobarRenderer the information bar
	 * @param achievementRenderer the achievement renderer
	 */
	public PlanetRenderer(GameResourceManager grm, 
			SoundFXPlayer uiSound, InfobarRenderer infobarRenderer,
			AchievementRenderer achievementRenderer) {
		this.gfx = grm.planetGFX;
		this.cgfx = grm.commonGFX;
		this.text = cgfx.text;
		this.uiSound = uiSound;
		this.infobarRenderer = infobarRenderer;
		this.achievementRenderer = achievementRenderer;
		buildScroller = new Timer(BUILD_SCROLL_INTERVAL, this);
		buildScroller.setInitialDelay(BUILD_SCROLL_WAIT);
		buildScroller.setActionCommand("BUILD_SCROLLER");
		fadeTimer = new Timer(FADE_INTERVAL, this);
		fadeTimer.setActionCommand("FADE");
		blinkTimer = new Timer(BLINK_TIMER_VALUE, this);
		blinkTimer.setActionCommand("BLINK");
		initButtons();
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addMouseListener(this);
		setOpaque(true);
//		int w = Tile.toScreenX(33,-33) - Tile.toScreenX(-64, -64);
//		int h = Tile.toScreenY(1, 0) - Tile.toScreenY(-32, -96);
		xOffsetMin = Tile.toScreenX(-66, -66);
		xOffsetMax = Tile.toScreenX(33, -33);
		yOffsetMin = Tile.toScreenY(1, 2);
		yOffsetMax = Tile.toScreenY(-32, -96);
		adjustOffsets();
	}
	/**
	 * Returns the given surface map based on the type and variant.
	 * @param surfaceType the type index 1-7
	 * @param variant the variant index 1-9
	 * @return the pac entry for the surface or null if not existent
	 */
	private PACEntry getSurface(int surfaceType, int variant) {
		String mapName = "MAP_" + (char)('A' + (surfaceType - 1)) + variant + ".MAP";
		return gfx.getMap(mapName);
	}
	/** Rendering X coordinates. */
	static final int[] MAP_START_X = new int[97];
	/** Rendering Y coordinates. */
	static final int[] MAP_START_Y = new int[97];
	/** Rendering X end coordinates. */
	static final int[] MAP_END_X = new int[97];
	/** Rendering Y end coordinates. */
//	static int[] mapEndY = new int[97];
	static {
		// initialize map rendering stripe coordinates
		int idx = 0;
		for (int i = 1; i <= 32; i++) {
			MAP_START_X[idx] = i;
			MAP_START_Y[idx] = 1 - i;
			idx++;
		}
		int y = -32;
		for (int i = 32; i >= -32; i--) {
			MAP_START_X[idx] = i;
			MAP_START_Y[idx] = y;
			idx++;
			y--;
		}
		idx = 0;
		for (int i = 0; i >= -64; i--) {
			MAP_END_X[idx] = i;
//			mapEndY[idx] = i;
			idx++;
		}
		y = -65;
		for (int i = -63; i <= -32; i++) {
			MAP_END_X[idx] = i;
//			mapEndY[idx] = y;
			y--;
			idx++;
		}
	}
	/**
	 * Paints the entire planet window.
	 * @param g the graphics object
	 */
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
		GamePlanet planet = gameWorld.player.selectedPlanet;
		if (planet == null) {
			List<GamePlanet> pl = gameWorld.getOwnPlanetsByName();
			if (pl.size() > 0) {
				planet = pl.get(0);
				gameWorld.player.selectedPlanet = planet;
			}
		}
		if (planet.placeBuildings) {
			for (GameBuilding b : planet.buildings) {
				placeBuilding(b);
			}
			planet.placeBuildings = false;
			fixRoads();
			radarImage = null;
		}
		reallocateResources();
		AffineTransform at = g2.getTransform();
		g2.translate(mainWindow.x, mainWindow.y);
		renderContents(g2, new SurfaceRendering(xoff, yoff, true, false));
		Composite comp = null;
		
		if (tilesToHighlight != null) {
			drawIntoRect(g2);
		}
		
		// ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// render textual labels, dama indicators and energy/status icons
		
		for (GameBuilding b : planet.buildings) {
			int pw = b.images.regularTile.image.getWidth();
			int ph = b.images.regularTile.image.getHeight();
			int tx = b.x;
			int ty = b.y - b.images.regularTile.width;
			int mx = xoff + Tile.toScreenX(b.x, b.y);
			int my = yoff + Tile.toScreenY(tx, ty + 1);
			
			int py = (my - ph) + (ph - 7) / 2;
			
			if (planet.selectedBuilding == b) {
				g2.setColor(Color.RED);
				g2.drawRect(mx, my - ph, pw, ph);
			}
			
			int nameLen = text.getTextWidth(7, b.prototype.name);
			text.paintTo(g2, mx + (pw - nameLen) / 2 + 1, py + 1, 7, TextGFX.LIGHT_BLUE, b.prototype.name);
			text.paintTo(g2, mx + (pw - nameLen) / 2, py, 7, 0xD4FC84, b.prototype.name);
			
			// render status icons
			int x = mx + (pw - gfx.buildingOff[0].getWidth()) / 2;
			int y = py + 15;
			int y1 = 0;
			if (b.enabled) {
				if (!b.repairing) {
//					g2.drawImage(gfx.buildingRepair[blinkStatus ? 1 : 0], x, y, null);
//					y1 = 10;
//				} else {
					float ep = b.getEnergyPercent();
					float wp = b.getWorkerPercent();
					if (ep >= 0 && ep < 0.5 && b.getEnergyDemand() > 0) {
						g2.drawImage(gfx.buildingNoEnergy[blinkStatus ? 1 : 0], x, y, null);
						x += gfx.buildingNoEnergy[0].getWidth() + 3;
						y1 = gfx.buildingNoEnergy[0].getHeight() + 5;
					}
					if (wp >= 0 && wp < 0.5 && b.getWorkerDemand() > 0) {
						g2.drawImage(cgfx.workerIcon, x, y, cgfx.workerIcon.getWidth() * 3, cgfx.workerIcon.getHeight() * 3, null);
						y1 = Math.max(cgfx.workerIcon.getHeight() * 3 + 5, y1);
					}
				}
			} else {
				g2.drawImage(gfx.buildingOff[blinkStatus ? 1 : 0], x, py + 10, null);
			}
			if (b.health < 100) {
				String s = (100 - b.health) + "%";
				int len = text.getTextWidth(7, s);
				if (b.repairing && blinkStatus) {
					text.paintTo(g2, mx + (pw - len) / 2 + 1, y + y1 + 1, 7, TextGFX.LIGHT_BLUE, s);
					text.paintTo(g2, mx + (pw - len) / 2, y + y1, 7, TextGFX.RED, s);
				} else {
					text.paintTo(g2, mx + (pw - len) / 2 + 1, y + y1 + 1, 7, TextGFX.LIGHT_BLUE, s);
					text.paintTo(g2, mx + (pw - len) / 2, y + y1, 7, TextGFX.YELLOW, s);
				}
			}
		}
		g2.setTransform(at);
		
		// ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		
		// RENDER INFOBARS
		infobarRenderer.renderInfoBars(this, g2);
		// RENDER LEFT BUTTONS
		g2.drawImage(gfx.buildingButton, btnBuilding.rect.x, btnBuilding.rect.y, null);
		g2.setColor(Color.BLACK);
		g2.drawLine(btnBuilding.rect.width, btnBuilding.rect.y, btnBuilding.rect.width, btnBuilding.rect.y + btnBuilding.rect.height - 1);
		
		g2.drawImage(gfx.leftTop, leftTopRect.x, leftTopRect.y, null);
		if (leftFillerRect.height > 0) {
			Paint p = g2.getPaint();
			g2.setPaint(leftFillerPaint);
			g2.fill(leftFillerRect);
			g2.setPaint(p);
		}
		g2.drawImage(gfx.leftBottom, leftBottomRect.x, leftBottomRect.y, null);
		g2.drawLine(btnRadar.rect.width, btnRadar.rect.y, btnRadar.rect.width, 
				btnRadar.rect.y + btnRadar.rect.height - 1);
		g2.drawImage(gfx.radarButton, btnRadar.rect.x, btnRadar.rect.y, null);
		
		// RENDER RIGHT BUTTONS
		g2.drawImage(gfx.buildingInfoButton, btnBuildingInfo.rect.x, btnBuildingInfo.rect.y, null);
		g2.drawImage(gfx.rightTop, rightTopRect.x, rightTopRect.y, null);
		if (rightFillerRect.height > 0) {
			Paint p = g2.getPaint();
			g2.setPaint(rightFillerPaint);
			g2.fill(rightFillerRect);
			g2.setPaint(p);
		}
		g2.drawImage(gfx.rightBottom, rightBottomRect.x, rightBottomRect.y, null);
		g2.drawImage(gfx.screenButtons, btnButtons.rect.x, btnButtons.rect.y, null);
		
		if (btnColonyInfo.visible) {
			if (btnColonyInfo.down) {
				g2.drawImage(gfx.colonyInfoButtonDown, btnColonyInfo.rect.x, btnColonyInfo.rect.y, null);
			} else {
				g2.drawImage(gfx.colonyInfoButton, btnColonyInfo.rect.x, btnColonyInfo.rect.y, null);
			}
		}
		if (btnPlanet.visible) {
			if (btnPlanet.down) {
				g2.drawImage(gfx.planetButtonDown, btnPlanet.rect.x, btnPlanet.rect.y, null);
			} else {
				g2.drawImage(gfx.planetButton, btnPlanet.rect.x, btnPlanet.rect.y, null);
			}
		}
		if (btnStarmap.visible) {
			if (btnStarmap.down) {
				g2.drawImage(gfx.starmapButtonDown, btnStarmap.rect.x, btnStarmap.rect.y, null);
			} else {
				g2.drawImage(gfx.starmapButton, btnStarmap.rect.x, btnStarmap.rect.y, null);
			}
		}
		if (btnBridge.visible) {
			if (btnBridge.down) {
				g2.drawImage(gfx.bridgeButtonDown, btnBridge.rect.x, btnBridge.rect.y, null);
			} else {
				g2.drawImage(gfx.bridgeButton, btnBridge.rect.x, btnBridge.rect.y, null);
			}
		}
		if (btnBuilding.down) {
			renderBuildingsPanel(g2);
		}
		if (btnBuildingInfo.down) {
			g2.drawImage(gfx.buildingInfoPanel, buildingInfoPanelRect.x, buildingInfoPanelRect.y, null);
			renderBuildingInfo(g2);
		}
		if (btnRadar.down) {
			g2.drawImage(gfx.radarPanel, radarPanelRect.x, radarPanelRect.y, null);
			renderMinimap(g2);
		}
		Shape sp = g2.getClip();
		g2.clip(infobarRenderer.topInfoArea);
		text.paintTo(g2, infobarRenderer.topInfoArea.x, infobarRenderer.topInfoArea.y + 1, 14, 0xFFFFFFFF, 
				planet != null ? planet.name : "");
		g2.setClip(sp);
		
		renderQuickInfo(g2);
		if (btnPlanetDetails.down) {
			renderPlanetDetails(g2);
		}
		
		// now darken the entire screen
		if (darkness > 0.0f) {
			comp = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, darkness));
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, w, h);
			g2.setComposite(comp);
		}
		achievementRenderer.renderAchievements(g2, this);
	}
	/**
	 * Renders the information available on the colony info page in a smaller form bottom left of the planet screen.
	 * @param g2 the graphics object
	 */
	private void renderPlanetDetails(Graphics2D g2) {
		GamePlayer p = gameWorld.player;
		GamePlanet planet = p.selectedPlanet;
		int x0 = btnButtons.rect.x - 1;
		int y0 = btnButtons.rect.y - 1;
		g2.setColor(Color.BLACK);
		Shape sp = g2.getClip();
		Composite cmp = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
		if (planet.owner != p) {
			// TODO information depends on if there is a radar3 or spy satellite around the planet to show exactly whats on the planet
			int x = x0 - 250;
			int y = y0 - 60;
			g2.fillRoundRect(x, y, 250, 60, 10, 10);
			g2.setClip(x, y, 250, 60);
			
			text.paintTo(g2, x + 5, y + 5, 7, TextGFX.GREEN,
					gameWorld.getLabel("ColonyInfoEntry",
						gameWorld.getLabel("ColonyInfo.Owner"),
						planet.owner != null ? planet.owner.name : gameWorld.getLabel("EmpireNames.Empty")
					));
				
				text.paintTo(g2, x + 5, y + 15, 7, TextGFX.GREEN,
						gameWorld.getLabel("ColonyInfoEntry",
								gameWorld.getLabel("ColonyInfo.Race"),
							(planet.populationRace != null && gameWorld.player.knownPlanetsByName.contains(planet)
								? gameWorld.getLabel("RaceNames." + planet.populationRace.id)
								: "?")
						));
				
				text.paintTo(g2, x + 5, y + 25, 7, TextGFX.GREEN,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.Surface"),
							gameWorld.getLabel("SurfaceTypeNames." + planet.surfaceType.planetXmlString)
						));
				text.paintTo(g2, x + 5, y + 35, 7, TextGFX.GREEN,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.Population"),
							(planet.populationRace != null && gameWorld.player.knownPlanetsByName.contains(planet)
							? planet.population + " " + gameWorld.getLabel("Aliens")
							: "?")
						));

				StringBuilder b = new StringBuilder();
				for (String s1 : planet.inOrbit) {
					if (b.length() > 0) {
						b.append(", ");
					}
					b.append(s1);
				}
				text.paintTo(g2, x + 5, y + 45, 7,  TextGFX.GREEN,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.Deployed2"),
							b
						));
		} else {
			int x = x0 - 300;
			int y = y0 - 170;
			g2.fillRoundRect(x, y, 300, 170, 10, 10);
			g2.setClip(x, y, 300, 170);
			text.paintTo(g2, x + 5, y + 5, 7, TextGFX.GREEN,
					gameWorld.getLabel("ColonyInfoEntry",
						gameWorld.getLabel("ColonyInfo.Owner"),
						planet.owner.name
					));
				
				text.paintTo(g2, x + 5, y + 15, 7, TextGFX.GREEN,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.Race"),
							gameWorld.getLabel("RaceNames." + planet.populationRace.id)
						));
				
				text.paintTo(g2, x + 5, y + 25, 7, TextGFX.GREEN,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.Surface"),
							gameWorld.getLabel("SurfaceTypeNames." + planet.surfaceType.planetXmlString)
						));
				text.paintTo(g2, x + 5, y + 35, 7, TextGFX.GREEN,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.Population"),
							gameWorld.getLabel("PopulationStatus",
									planet.population,
									gameWorld.getLabel("PopulatityName." + PopularityType.find(planet.popularity).id), 
									planet.populationGrowth)
						));
				PlanetStatus ps = planet.getStatus();
				int color = InformationRenderer.getColorForRelation(planet.population, ps.livingSpace, 1.1f);
				text.paintTo(g2, x + 5, y + 45, 7, color,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.LivingSpace"),
							planet.population + "/" + ps.livingSpace + " " + gameWorld.getLabel("ColonyInfo.Dweller")
						));

				color = InformationRenderer.getColorForRelation(ps.workerDemand, planet.population, 1.1f);
				text.paintTo(g2, x + 5, y + 55, 7, color,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.Worker"),
							planet.population + "/" + ps.workerDemand + " " + gameWorld.getLabel("ColonyInfo.Dweller")
						));

				color = InformationRenderer.getColorForRelation(planet.population, ps.hospital, 1.1f);
				text.paintTo(g2, x + 5, y + 65, 7, color,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.Hospital"),
							planet.population + "/" + ps.hospital + " " + gameWorld.getLabel("ColonyInfo.Dweller")
						));
				
				color = InformationRenderer.getColorForRelation(planet.population, ps.food, 1.1f);
				text.paintTo(g2, x + 5, y + 75, 7, color,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.Food"),
							planet.population + "/" + ps.food + " " + gameWorld.getLabel("ColonyInfo.Dweller")
						));
				
				color = InformationRenderer.getColorForRelation(ps.energyDemand, ps.energyProduction, 2f);
				text.paintTo(g2, x + 5, y + 85, 7, color,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.Energy"),
							ps.energyProduction + " " + gameWorld.getLabel("ColonyInfo.KWH")
							+ "   " + gameWorld.getLabel("ColonyInfo.Demand") + " : " + ps.energyDemand
							+ " " + gameWorld.getLabel("ColonyInfo.KWH")
						));
				
				text.paintTo(g2, x + 5, y + 95, 7, TextGFX.GREEN,
						gameWorld.getLabel("ColonyInfoEntry2",
							gameWorld.getLabel("ColonyInfo.TaxIncome"),
							planet.taxIncome
						));
				text.paintTo(g2, x + 5, y + 105, 7, TextGFX.GREEN,
						gameWorld.getLabel("ColonyInfoEntry2",
							gameWorld.getLabel("ColonyInfo.TradeIncome"),
							planet.tradeIncome
						));
				text.paintTo(g2, x + 5, y + 115, 7,  TextGFX.GREEN,
						gameWorld.getLabel("ColonyInfoEntry2",
							gameWorld.getLabel("ColonyInfo.TaxMorale"),
							planet.taxMorale + "%"
						));
				// render tax buttons
				text.paintTo(g2, x + 5, y + 125, 7,  TextGFX.GREEN,
						gameWorld.getLabel("ColonyInfoEntry2",
							gameWorld.getLabel("ColonyInfo.Taxation"),
							gameWorld.getLabel("TaxRate." + planet.tax.id)
						));
				
				// allocation preference settings
				
				text.paintTo(g2, x + 5, y + 135, 7,  TextGFX.YELLOW,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.AllocationPreference.EnergyAlloc"),
							gameWorld.getLabel("ColonyInfo.AllocationPreference." + planet.energyAllocation.id)
						));
				text.paintTo(g2, x + 5, y + 145, 7,  TextGFX.YELLOW,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.AllocationPreference.WorkerAlloc"),
							gameWorld.getLabel("ColonyInfo.AllocationPreference." + planet.workerAllocation.id)
						));
				
				StringBuilder b = new StringBuilder();
				for (String s1 : planet.inOrbit) {
					if (b.length() > 0) {
						b.append(", ");
					}
					b.append(s1);
				}
				text.paintTo(g2, x + 5, y + 155, 7,  TextGFX.GREEN,
						gameWorld.getLabel("ColonyInfoEntry",
							gameWorld.getLabel("ColonyInfo.Deployed2"),
							b
						));
		}
		g2.setComposite(cmp);
		g2.setClip(sp);
	}
	/**
	 * Render a quick info between the building list and info panels.
	 * @param g2 the graphics object
	 */
	private void renderQuickInfo(Graphics2D g2) {
		int h = 21;
		int len = text.getTextWidth(7, gameWorld.player.selectedPlanet.name) + h;
		int x0 = (getWidth() - len) / 2;
		int y0 = btnBuilding.rect.y;
		int[] x = { x0 - h, x0, x0 + len, x0 + len + h};
		int[] y = { y0, y0 + h, y0 + h, y0 };
		g2.setColor(Color.BLACK);
		g2.fillPolygon(x, y, x.length);
		g2.setColor(Color.WHITE);
		g2.drawPolyline(x, y, x.length);
		text.paintTo(g2, x0 + h / 2, y0 + 2, 7, 
				gameWorld.player.selectedPlanet.owner != null ? gameWorld.player.selectedPlanet.owner.getColor().getRGB() : TextGFX.GRAY, 
						gameWorld.player.selectedPlanet.name);
		if (gameWorld.player.selectedPlanet.owner == gameWorld.player) {
			int count = 0;
			PlanetStatus ps = gameWorld.player.selectedPlanet.getStatus();
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
			int x1 = getWidth() / 2;
			int y1 = y0 + 10;
			if (count > 0) {
				x1 -= (count * 12) / 2;
				if (count == 1) {
					x1++;
				}
				if (energyIcon) {
					g2.drawImage(cgfx.energyIcon, x1, y1, null);
					x1 += 12;
				}
				if (foodIcon) {
					g2.drawImage(cgfx.foodIcon, x1, y1, null);
					x1 += 12;
				}
				if (hospitalIcon) {
					g2.drawImage(cgfx.hospitalIcon, x1, y1, null);
					x1 += 12;
				}
				if (workerIcon) {
					g2.drawImage(cgfx.workerIcon, x1, y1, null);
					x1 += 12;
				}
				if (livingspaceIcon) {
					g2.drawImage(cgfx.livingSpaceIcon, x1, y1, null);
					x1 += 12;
				}
			}
		}

	}
	/**
	 * Helper class to store invariants for the surface rendering.
	 * @author karnokd, 2009.05.25.
	 * @version $Revision 1.0$
	 */
	class SurfaceRendering {
		/** The surface type. */
		final int surfaceType;
		/** The surface map. */
		final byte[] mapBytes;
		/** The map. */
		final Map<Location, TileFragment> map;
		/** The daylight value. */
		final float daylight;
		/** The X offset. */
		final int xoffset;
		/** The Y offset. */
		final int yoffset;
		/** Clip on screen edges? */
		final boolean clip;
		/** Draw building skeletons only? */
		final boolean skeleton;
		/**
		 * Constructor. Captures the current planet's properties.
		 * @param xoffset the x offset
		 * @param yoffset the y offset
		 * @param clip do clip?
		 * @param skeleton draw skeletons only?
		 */
		SurfaceRendering(int xoffset, int yoffset, boolean clip, boolean skeleton) {
			GamePlanet p = gameWorld.player.selectedPlanet;
			this.surfaceType = p.surfaceType.index;
			this.mapBytes = getSelectedPlanetSurface();
			this.daylight = PlanetRenderer.this.daylight;
			this.map = p.map; //new HashMap<Location, TileFragment>(p.map);
			this.xoffset = xoffset;
			this.yoffset = yoffset;
			this.clip = clip;
			this.skeleton = skeleton;
		}
	}
	/**
	 * Renders the the surface and buildings.
	 * @param g2 the graphics object
	 * @param config the rendering configuration
	 */
	private void renderContents(Graphics2D g2, SurfaceRendering config) {
		int k = 0;
		int j = 0;
		int surfaceType = config.surfaceType;
		byte[] mapBytes = config.mapBytes;
		// RENDER VERTICALLY
		Map<Integer, Tile> surface = gfx.getSurfaceTiles(surfaceType);
		for (int mi = 0; mi < MAP_START_X.length; mi++) {
			k = MAP_START_X[mi];
			j = MAP_START_Y[mi];
			int i = toMapOffset(k, j);
			int kmin = MAP_END_X[mi];
			while (i >= 0 && k >= kmin) {
				int tileId = (mapBytes[2 * i + 4] & 0xFF) - (surfaceType < 7 ? 41 : 84);
				int stripeId = mapBytes[2 * i + 5] & 0xFF;
				Tile tile = surface.get(tileId);
				Location l = Location.of(k, j);
				TileFragment tf = config.map.get(l);
				if (tf != null && tf.fragment >= 0) {
					tile = tf.provider.getTile(l);
					// select appropriate stripe
					if (tile != null) {
						tile.createImage(config.daylight);
						stripeId = tf.fragment % tile.strips.length; // wrap around for safety
					}
				}
				if (config.skeleton && tf != null && !tf.isRoad) {
					BufferedImage tileImg;
					TileStatus ts = tf.provider.getStatus();
					switch (ts) {
					case DAMAGED:
						tileImg = gfx.redTile;
						break;
					case DESTROYED:
						tileImg = gfx.blackTile;
						break;
					case NO_ENERGY:
						tileImg = gfx.yellowTile;
						break;
					default:
						tileImg = gfx.greenTile; 
					}
					int x = config.xoffset + Tile.toScreenX(k, j);
					int y = config.yoffset + Tile.toScreenY(k, j);
					
					int w = tileImg.getWidth();
					int h = tileImg.getHeight();
					if (!config.clip || (x >= -w && x <= getWidth()
							&& y >= -h 
							&& y <= getHeight() + h)) {
//						g2.drawImage(tileImg, x, y - h, null);
						draw(g2, tileImg, x, y - h);
					}
				} else
				if (tile != null) {
					tile.createImage(config.daylight);
					// 1x1 tiles can be drawn from top to bottom
					if (tile.width == 1 && tile.height == 1) {
						int x = config.xoffset + Tile.toScreenX(k, j);
						int y = config.yoffset + Tile.toScreenY(k, j);
						if (!config.clip || (x >= -tile.image.getWidth() && x <= getWidth()
								&& y >= -tile.image.getHeight() 
								&& y <= getHeight() + tile.image.getHeight())) {
							BufferedImage subimage = tile.strips[stripeId];
//							g2.drawImage(subimage, x, y - tile.image.getHeight() + tile.heightCorrection, null);
							draw(g2, subimage, x, y - tile.image.getHeight() + tile.heightCorrection);
						}
					} else 
					if (stripeId < 255) {
						// multi spanning tiles should be cut into small rendering piece for the current strip
						// ff value indicates the stripe count
						// the entire image would be placed using this bottom left coordinate
						int j1 = stripeId >= tile.width ? j + tile.width - 1 : j + stripeId;
						int k1 = stripeId >= tile.width ? k + (tile.width - 1 - stripeId) : k;
						int j2 = stripeId >= tile.width ? j : j - (tile.width - 1 - stripeId);
						int x = config.xoffset + Tile.toScreenX(k1, j1);
						int y = config.yoffset + Tile.toScreenY(k1, j2);
						// use subimage stripe
						int x0 = stripeId >= tile.width ? Tile.toScreenX(stripeId, 0) : Tile.toScreenX(0, -stripeId);
						BufferedImage subimage = tile.strips[stripeId];
//						g2.drawImage(subimage, x + x0, y - tile.image.getHeight() + tile.heightCorrection, null);
						draw(g2, subimage, x + x0, y - tile.image.getHeight() + tile.heightCorrection);
					}
				}
				k--;
				i = toMapOffset(k, j);
			}
		}
	}
	/**
	 * Renders the given image to the given location.
	 * @param g2 the graphics object
	 * @param img the image
	 * @param x the X coordinate
	 * @param y the y coordinate
	 */
	private void draw(Graphics2D g2, BufferedImage img, int x, int y) {
		g2.drawImage(img, x, y, null);
	}
	/**
	 * Returns the currently selected planet's surface base.
	 * @return the map bytes
	 */
	private byte[] getSelectedPlanetSurface() {
		byte[] mapBytes;
		PACEntry e = getSurface(gameWorld.player.selectedPlanet.surfaceType.index, gameWorld.player.selectedPlanet.surfaceVariant);
		if (e != null) {
			mapBytes = e.data;
		} else {
			mapBytes = EMPTY_SURFACE_MAP;
		}
		return mapBytes;
	}
	/** Initialize buttons. */
	private void initButtons() {
		btnPlanet = new Btn(new BtnAction() { @Override public void invoke() { doPlanetClick(); } });
		releaseButtons.add(btnPlanet);
		btnColonyInfo = new Btn(new BtnAction() { @Override public void invoke() { doColonyInfoClick(); } });
		releaseButtons.add(btnColonyInfo);
		btnStarmap = new Btn(new BtnAction() { @Override public void invoke() { doStarmapRecClick(); } });
		releaseButtons.add(btnStarmap);
		btnBridge = new Btn(new BtnAction() { @Override public void invoke() { doBridgeClick(); } });
		releaseButtons.add(btnBridge);
		
		btnBuilding = new Btn(new BtnAction() { @Override public void invoke() { doBuildingClick(); } });
		toggleButtons.add(btnBuilding);
		btnRadar = new Btn(new BtnAction() { @Override public void invoke() { doRadarClick(); } });
		toggleButtons.add(btnRadar);
		btnBuildingInfo = new Btn(new BtnAction() { @Override public void invoke() { doBuildingInfoClick(); } });
		toggleButtons.add(btnBuildingInfo);
		btnButtons = new Btn(new BtnAction() { @Override public void invoke() { doScreenClick(); } });
		toggleButtons.add(btnButtons);
		
		btnPlanetDetails = new Btn(new BtnAction() { @Override public void invoke() { repaint(); } });
		btnPlanetDetails.down = true;
		toggleButtons.add(btnPlanetDetails);
		
		btnBuildNext = new Btn(new BtnAction() { @Override public void invoke() { doBuildNext(); } });
		pressButtons.add(btnBuildNext);
		btnBuildPrev = new Btn(new BtnAction() { @Override public void invoke() { doBuildPrev(); } });
		pressButtons.add(btnBuildPrev);
		btnBuild = new Btn(new BtnAction() { @Override public void invoke() { doBuild(); } });
		toggleButtons.add(btnBuild);
		btnList = new Btn(new BtnAction() { @Override public void invoke() { doList(); } });
		releaseButtons.add(btnList);

		btnDemolish = new Btn(new BtnAction() { @Override public void invoke() { doDemolish(); } });
		releaseButtons.add(btnDemolish);
		
		btnActive = new Btn(new BtnAction() { @Override public void invoke() { doActiveClick(); } });
		btnRepair = new Btn(new BtnAction() { @Override public void invoke() { doRepairClick(); } });
		releaseButtons.add(btnActive);
		releaseButtons.add(btnRepair);
		
		btnBuilding.down = true;
		btnRadar.down = true;
		btnBuildingInfo.down = true;
		btnButtons.down = true;
	}
	/**
	 * Perform actions on active button click.
	 */
	protected void doActiveClick() {
		GameBuilding b = gameWorld.player.selectedPlanet.selectedBuilding;
		if (b != null) {
			b.enabled = !b.enabled;
			gameWorld.player.selectedPlanet.update();
			radarImage = null;
			repaint();
		}
	}
	/**
	 * Perform actions on repair button click.
	 */
	protected void doRepairClick() {
		GameBuilding b = gameWorld.player.selectedPlanet.selectedBuilding;
		if (b != null) {
			b.repairing = !b.repairing;
			gameWorld.player.selectedPlanet.update();
			radarImage = null;
			repaint();
		}
	}
	/**
	 * Demolish the selected building.
	 */
	public void doDemolish() {
		GameBuilding b = gameWorld.player.selectedPlanet.selectedBuilding;
		if (b != null) {
			uiSound.playSound("DemolishBuilding");
			gameWorld.player.selectedPlanet.removeBuilding(b);
			gameWorld.player.selectedPlanet.update();
			gameWorld.player.selectedPlanet.selectedBuilding = null;
			fixRoads();
			radarImage = null;
			repaint();
		}
	}
	/**
	 * Perform action on list button click.
	 */
	protected void doList() {
		uiSound.playSound("Buildings");
		if (getOnListClicked() != null) {
			getOnListClicked().invoke();
		}
		cancelBuildMode();
	}
	/**
	 * Perform action on the build button click.
	 */
	public void doBuild() {
		// small fix when called from outside
		btnBuild.down = true;
		GameBuildingPrototype bp = gameWorld.player.selectedBuildingPrototype;
		if (bp == null) {
			return;
		}
		BuildingImages bi = bp.images.get(getTechId());
		if (bi == null) {
			return;
		}
		buildMode = !buildMode;
		// if build mode, resize the selection rectangle
		if (buildMode) {
			// adjust the highlight rectangle's origin to the current mouse position.
			Point pt = MouseInfo.getPointerInfo().getLocation();
			Point abs = getLocationOnScreen();
			// relative to this component
			updateMouseTileCoords(new Point(pt.x - abs.x, pt.y - abs.y));
			
			tilesToHighlight = new Rectangle(mouseTileX, mouseTileY, bi.regularTile.height + 2, bi.regularTile.width + 2);
		} else {
			cancelBuildMode();
		}
		repaint(btnBuild.rect);
		repaint(mainWindow);
	}
	/**
	 * Perform action on the previous button click.
	 */
	protected void doBuildPrev() {
		List<GameBuildingPrototype> list = getBuildingList();
		GameBuildingPrototype bp = gameWorld.player.selectedBuildingPrototype;
		int idx = Math.max(0, list.indexOf(bp) - 1);
		if (idx < list.size()) {
			gameWorld.player.selectedBuildingPrototype = list.get(idx);
			if (gameWorld.player.selectedBuildingPrototype.researchTech != null) {
				gameWorld.player.selectedTech = gameWorld.player.selectedBuildingPrototype.researchTech;
			}
		}
		repaint(buildPanelRect);
		buildScroller.setActionCommand("SCROLL-UP");
		if (!buildScroller.isRunning()) {
			buildScroller.start();
		}
		cancelBuildMode();
	}
	/**
	 * Cancel the build mode.
	 */
	public void cancelBuildMode() {
		buildMode = false;
		tilesToHighlight = null;
		btnBuild.down = false;
		repaint(btnBuild.rect);
		repaint(mainWindow);
	}
	/**
	 * @return a list of buildings for the selected planet's race or the player's race.
	 */
	private List<GameBuildingPrototype> getBuildingList() {
		String techId = getTechId();
		List<GameBuildingPrototype> list = gameWorld.getTechIdBuildingPrototypes(techId);
		return list;
	}
	/**
	 * @return the technology id for the current planet buildings
	 */
	private String getTechId() {
		String techId;
		if (gameWorld.player.selectedPlanet != null && gameWorld.player.selectedPlanet.populationRace != null) {
			techId = gameWorld.player.selectedPlanet.populationRace.techId; 
		} else {
			techId = gameWorld.player.race.techId;
		}
		return techId;
	}
	/**
	 * Perform action on the building next button click.
	 */
	protected void doBuildNext() {
		List<GameBuildingPrototype> list = getBuildingList();
		GameBuildingPrototype bp = gameWorld.player.selectedBuildingPrototype;
		int idx = Math.min(list.size(), list.indexOf(bp) + 1);
		if (idx < list.size()) {
			gameWorld.player.selectedBuildingPrototype = list.get(idx);
			if (gameWorld.player.selectedBuildingPrototype.researchTech != null) {
				gameWorld.player.selectedTech = gameWorld.player.selectedBuildingPrototype.researchTech;
			}
		}
		repaint(buildPanelRect);
		buildScroller.setActionCommand("SCROLL-DOWN");
		if (!buildScroller.isRunning()) {
			buildScroller.start();
		}
		cancelBuildMode();
	}
	/**
	 * Update location of various interresting rectangles of objects.
	 */
	private void updateRegions() {
		
		infobarRenderer.updateRegions(this);
		
		btnBuilding.rect.x = 0;
		btnBuilding.rect.y = cgfx.top.left.getHeight();
		btnBuilding.rect.width = gfx.buildingButton.getWidth();
		btnBuilding.rect.height = gfx.buildingButton.getHeight();
		
		leftTopRect.x = 0;
		leftTopRect.y = btnBuilding.rect.y + btnBuilding.rect.height;
		leftTopRect.width = gfx.leftTop.getWidth();
		leftTopRect.height = gfx.leftTop.getHeight();
		
		btnRadar.rect.x = 0;
		btnRadar.rect.y = getHeight() - cgfx.bottom.left.getHeight() - gfx.radarButton.getHeight();
		btnRadar.rect.width = gfx.radarButton.getWidth();
		btnRadar.rect.height = gfx.radarButton.getHeight();
		
		leftBottomRect.x = 0;
		leftBottomRect.y = btnRadar.rect.y - gfx.leftBottom.getHeight();
		leftBottomRect.width = gfx.leftBottom.getWidth();
		leftBottomRect.height = gfx.leftBottom.getHeight();
		
		leftFillerRect.x = 0;
		leftFillerRect.y = leftTopRect.y + leftTopRect.height;
		leftFillerRect.width = gfx.leftFiller.getWidth();
		leftFillerRect.height = leftBottomRect.y - leftFillerRect.y;
		if (leftFillerPaint == null) {
			leftFillerPaint = new TexturePaint(gfx.leftFiller, leftFillerRect);
		}
		
		btnBuildingInfo.rect.x = getWidth() - gfx.buildingInfoButton.getWidth();
		btnBuildingInfo.rect.y = cgfx.top.left.getHeight();
		btnBuildingInfo.rect.width = gfx.buildingInfoButton.getWidth();
		btnBuildingInfo.rect.height = gfx.buildingInfoButton.getHeight();
		
		rightTopRect.x = btnBuildingInfo.rect.x;
		rightTopRect.y = btnBuildingInfo.rect.y + btnBuildingInfo.rect.height;
		rightTopRect.width = gfx.rightTop.getWidth();
		rightTopRect.height = gfx.rightTop.getHeight();
		
		btnButtons.rect.x = btnBuildingInfo.rect.x;
		btnButtons.rect.y = getHeight() - cgfx.bottom.left.getHeight() - gfx.screenButtons.getHeight();
		btnButtons.rect.width = gfx.screenButtons.getWidth();
		btnButtons.rect.height = gfx.screenButtons.getHeight();
		
		rightBottomRect.x = btnBuildingInfo.rect.x;
		rightBottomRect.y = btnButtons.rect.y - gfx.rightBottom.getHeight();
		rightBottomRect.width = gfx.rightBottom.getWidth();
		rightBottomRect.height = gfx.rightBottom.getHeight();
		
		rightFillerRect.x = btnBuildingInfo.rect.x;
		rightFillerRect.y = rightTopRect.y + gfx.rightTop.getHeight();
		rightFillerRect.width = gfx.rightFiller.getWidth();
		rightFillerRect.height = rightBottomRect.y - rightFillerRect.y;
		
		rightFillerPaint = new TexturePaint(gfx.rightFiller, rightFillerRect);
		
		// BOTTOM RIGHT CONTROL BUTTONS
		
		btnBridge.rect.x = getWidth() - gfx.rightBottom.getWidth() - gfx.bridgeButton.getWidth();
		btnBridge.rect.y = getHeight() - cgfx.bottom.right.getHeight() - gfx.bridgeButton.getHeight();
		btnBridge.rect.width = gfx.bridgeButton.getWidth();
		btnBridge.rect.height = gfx.bridgeButton.getHeight();
		
		btnStarmap.rect.x = btnBridge.rect.x - gfx.starmapButton.getWidth();
		btnStarmap.rect.y = btnBridge.rect.y;
		btnStarmap.rect.width = gfx.starmapButton.getWidth();
		btnStarmap.rect.height = gfx.starmapButton.getHeight();
		
		btnPlanet.rect.x = btnStarmap.rect.x - gfx.planetButton.getWidth();
		btnPlanet.rect.y = btnBridge.rect.y;
		btnPlanet.rect.width = gfx.planetButton.getWidth();
		btnPlanet.rect.height = gfx.planetButton.getHeight();

		btnColonyInfo.rect.x = btnPlanet.rect.x - gfx.colonyInfoButton.getWidth();
		btnColonyInfo.rect.y = btnBridge.rect.y;
		btnColonyInfo.rect.width = gfx.colonyInfoButton.getWidth();
		btnColonyInfo.rect.height = gfx.colonyInfoButton.getHeight();
		
		mainWindow.x = btnBuilding.rect.width + 1;
		mainWindow.y = btnBuilding.rect.y;
		mainWindow.width = btnBuildingInfo.rect.x - mainWindow.x;
		mainWindow.height = btnRadar.rect.y + btnRadar.rect.height - mainWindow.y;
		
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
		
		buildImageRect.setBounds(26, buildPanelRect.y + 7, 140, 103);
		buildNameRect.setBounds(27, buildPanelRect.y + 117, 166, 18);
		btnList.setBounds(26, buildPanelRect.y + 142, 81, 21);
		btnBuild.setBounds(113, buildPanelRect.y + 142, 81, 21);
		btnBuildPrev.setBounds(172, buildPanelRect.y + 7, 22, 48);
		btnBuildNext.setBounds(172, buildPanelRect.y + 62, 22, 48);
		
		buildingInfoName.setBounds(buildingInfoPanelRect.x + 9, buildingInfoPanelRect.y + 7, 182, 16);
		btnDemolish.setBounds(buildingInfoPanelRect.x + 161, buildingInfoPanelRect.y + 50, 29, 90);
		
		minimapRect.setBounds(radarPanelRect.x + 14, radarPanelRect.y + 13, 152, 135);
		
		// building information fields
		energyPercentRect.setBounds(buildingInfoPanelRect.x + 70, buildingInfoPanelRect.y + 29, 28, 12);
		energyRect.setBounds(buildingInfoPanelRect.x + 119, buildingInfoPanelRect.y + 29, 42, 12);
		workerPercentRect.setBounds(buildingInfoPanelRect.x + 70, buildingInfoPanelRect.y + 45, 28, 12);
		workerRect.setBounds(buildingInfoPanelRect.x + 119, buildingInfoPanelRect.y + 45, 28, 12);
		operationPercentRect.setBounds(buildingInfoPanelRect.x + 70, buildingInfoPanelRect.y + 61, 28, 12);
		productionRect.setBounds(buildingInfoPanelRect.x + 70, buildingInfoPanelRect.y + 77, 77, 12);
		
		btnActive.setBounds(buildingInfoPanelRect.x + 8, buildingInfoPanelRect.y + 98, 145, 18);
		btnRepair.setBounds(buildingInfoPanelRect.x + 8, buildingInfoPanelRect.y + 122, 145, 18);
		
		btnPlanetDetails.setBounds(btnBuildingInfo.rect.x, btnBuildingInfo.rect.y + btnBuildingInfo.rect.height, btnBuildingInfo.rect.width, btnButtons.rect.y - btnBuildingInfo.rect.y - btnBuildingInfo.rect.height);
		
		// readjust rendering offset
		adjustOffsets();
	}
	/**
	 * Adjust offsets to limits.
	 */
	private void adjustOffsets() {
		xoff = Math.max(Math.min(xoff, -xOffsetMin), -xOffsetMax + mainWindow.width);
		yoff = Math.max(Math.min(yoff, -yOffsetMin), -yOffsetMax + mainWindow.height);
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
	 * @param g2 the graphics object
	 */
	private void drawIntoRect(Graphics2D g2) {
		Rectangle rect = tilesToHighlight;
		allowBuild = true;
		GamePlanet planet = gameWorld.player.selectedPlanet;
		int surfaceType = planet.surfaceType.index;
		Map<Integer, Tile> surface = gfx.getSurfaceTiles(surfaceType);
		byte[] mapBytes = getSelectedPlanetSurface();
		BufferedImage okImage = gfx.getFrame(0);
		BufferedImage noImage = gfx.getFrame(2);
		
		for (int j = rect.y; j >= rect.y - rect.height + 1; j--) {
			for (int k = rect.x; k < rect.x + rect.width; k++) {
				int x = xoff + Tile.toScreenX(k, j); 
				int y = yoff + Tile.toScreenY(k, j);
				
				BufferedImage image = okImage;
				// test logical screen bounds
				if (-j + 2 <= k || -j - 129 >= k) {
					image = noImage;
					allowBuild = false;
				} else {
					// test for other object in the
					Location l = Location.of(k, j);
					TileFragment tf = planet.map.get(l);
					if (tf != null && !tf.isRoad) {
						image = noImage;
						allowBuild = false;
					} else {
						// test surface for a multi tile feature
						int i = toMapOffset(k, j);
						if (i >= 0 && i < 65 * 65) {
							int tileId = (mapBytes[2 * i + 4] & 0xFF) - (surfaceType < 7 ? 41 : 84);
		//					int stripeId = mapBytes[2 * i + 5] & 0xFF;
							Tile tile = surface.get(tileId);
							if (tile == null || tile.strips.length > 1) {
								image = noImage;
								allowBuild = false;
							}
						} else {
							image = gfx.getFrame(2);
							allowBuild = false;
						}
					}
				}
				g2.drawImage(image, x - 1, y - image.getHeight(), null);
			}
		}
	}
	/** Light value change. */
	boolean lightMode;
	/** Set to true if the user drags the minimap. */
	private boolean minimapScroll;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		Point pt = e.getPoint();
		updateMouseTileCoords(pt);
		if (lightMode) {
			// adjust daylight value based on the vertical mouse position
			daylight = pt.y / (float)getHeight();
			radarImage = null;
			repaint();
		} else
		if (panMode) {
			xoff -= (lastx - pt.x);
			yoff -= (lasty - pt.y);
			
			adjustOffsets();
			
			lastx = pt.x;
			lasty = pt.y;
			repaint();
		} else
		if (minimapScroll) {
			doScrollMinimap(pt);
		}
	}
	/**
	 * Returns true if the mouse event is within the
	 * visible area of the main window (e.g not over
	 * the panels or buttons).
	 * @param e the mouse event
	 * @return true if the event was on the surface
	 */
	private boolean eventInMainWindow(MouseEvent e) {
		Point pt = e.getPoint();
		return mainWindow.contains(pt) 
		&& (!btnBuilding.down || !buildPanelRect.contains(pt))
		&& (!btnRadar.down || !radarPanelRect.contains(pt))
		&& (!btnBuildingInfo.down || !buildingInfoPanelRect.contains(pt))
		&& (!btnColonyInfo.test(pt)
				&& !btnPlanet.test(pt)
				&& !btnStarmap.test(pt)
				&& !btnBridge.test(pt)
		);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		Point pt = e.getPoint();
		if (eventInMainWindow(e)) {
			updateMouseTileCoords(pt);
			if (tilesToHighlight != null) {
				tilesToHighlight.x = mouseTileX;
				tilesToHighlight.y = mouseTileY;
				repaint();
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		Point pt = e.getPoint(); 
		if (e.getButton() == MouseEvent.BUTTON3 && eventInMainWindow(e)) {
			lastx = e.getX();
			lasty = e.getY();
			panMode = true;
		} else
		if (e.getButton() == MouseEvent.BUTTON2 && eventInMainWindow(e)) {
			daylight = e.getY() / (float)getHeight();
			lightMode = true;
			radarImage = null;
			repaint();
		} else
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (eventInMainWindow(e)) {
				doMainWindowClick(e);
			} else 
			if (btnRadar.down && minimapRect.contains(pt)) {
				minimapScroll = true;
				doScrollMinimap(pt);
			} else {
				for (Btn b : pressButtons) {
					if (b.test(pt)) {
						b.down = true;
						repaint(b.rect);
						b.click();
					}
				}
				for (Btn b : releaseButtons) {
					if (b.test(pt)) {
						b.down = true;
						repaint(b.rect);
					}
				}
				for (Btn b : toggleButtons) {
					if (b.test(pt)) {
						b.down = !b.down;
						b.click();
						repaint(b.rect);
					}
				}
			}
		}
	}
	/**
	 * Scroll the main window to the point, centered around the given point.
	 * @param pt the point to center around
	 */
	private void doScrollMinimap(Point pt) {
		double xs = minimapRect.width / (double)(xOffsetMax - xOffsetMin);
		double ys = minimapRect.height / (double)(yOffsetMax - yOffsetMin);
		
		double x = (pt.x - minimapRect.x) / xs - mainWindow.width / 2.0;
		double y = (pt.y - minimapRect.y) / ys - mainWindow.height / 2.0;
		
		xoff = (int)(-xOffsetMin - x);  
		yoff = (int)(-yOffsetMin - y);
		adjustOffsets();
		repaint();
	}
	/**
	 * Do action when the user clicks on the maon window.
	 * @param e the mouse event
	 */
	private void doMainWindowClick(MouseEvent e) {
		GamePlanet planet = gameWorld.player.selectedPlanet;
		if (!buildMode) {
			updateMouseTileCoords(e.getPoint());
			int a = mouseTileX;
			int b = mouseTileY;
			
			TileFragment tf = planet.map.get(Location.of(a, b));
			if (tf != null && !tf.isRoad) {
				planet.selectedBuilding = (GameBuilding)tf.provider;
				gameWorld.player.selectedBuildingPrototype = planet.selectedBuilding.prototype;
				if (gameWorld.player.selectedBuildingPrototype.researchTech != null) {
					gameWorld.player.selectedTech = gameWorld.player.selectedBuildingPrototype.researchTech;
				}
			} else {
				planet.selectedBuilding = null;
			}
			repaint(mainWindow);
			repaint(buildingInfoPanelRect);
			repaint(buildPanelRect);
			
//			int offs = this.toMapOffset(a, b);
//			int val = offs >= 0 && offs < 65 * 65 ? getSelectedPlanetSurface()[offs * 2 + 4] & 0xFF : 0;
//			System.out.printf("%d, %d -> %d, %d%n", a, b, offs, val);
		} else {
			if (!allowBuild) {
				return;
			}
			uiSound.playSound("PlaceBuilding");
			String techid = getTechId();
			// place the selected building onto the planet
			GameBuildingPrototype bp = gameWorld.player.selectedBuildingPrototype;
			BuildingImages bi = bp.images.get(techid);
			
			GameBuilding b = new GameBuilding();
			b.prototype = bp;
			b.images = bi;
			b.progress = 100;
			b.health = 100;
			b.x = tilesToHighlight.x + 1;
			b.y = tilesToHighlight.y - 1;
			
			placeBuilding(b);
			planet.addBuilding(b);

			fixRoads();
			
			radarImage = null;
			planet.selectedBuilding = b;
			repaint(mainWindow);
			repaint(buildingInfoPanelRect);
			// if not multiple placement
			if (!e.isShiftDown()) {
				cancelBuildMode();
			}
		}
	}
	/**
	 * Places the given game building onto the game map of the current planet.
	 * @param b the building to place
	 */
	public void placeBuilding(GameBuilding b) {
		GamePlanet planet = gameWorld.player.selectedPlanet;
		Rectangle rect = b.getRectWithRoad();
		addRoadAround(planet, b.images.techId, rect);
		// place roads around the base
		TileFragment tf;
		// place the actual building fragments
		int fragment = 0;
		for (int i = rect.y - 1; i >= rect.y - rect.height + 2; i--) {
			tf = TileFragment.of(fragment, b, false);
			planet.map.put(Location.of(rect.x + 1, i), tf);
			fragment++;
		}
		for (int i = rect.x + 2; i < rect.x + rect.width - 1; i++) {
			tf = TileFragment.of(fragment, b, false);
			planet.map.put(Location.of(i, rect.y - rect.height + 2), tf);
			fragment++;
		}
		// place secondary tiles to the remaingin region
		for (int i = rect.y - 1; i >= rect.y - rect.height + 3; i--) {
			for (int j = rect.x + 2; j < rect.x + rect.width - 1; j++) {
				tf = TileFragment.of(255, b, false);
				planet.map.put(Location.of(j, i), tf);
			}
		}
		planet.update();
	}
	/**
	 * Places a road frame around the tilesToHighlight rectangle.
	 * @param planet the planet to place the road
	 * @param techid the technology id
	 * @param rect the rectangle to use
	 * @return the four corner tile fragment location, top-left, top-right, bottom-left, bottom-right
	 */
	private Location[] addRoadAround(GamePlanet planet, String techid, Rectangle rect) {
		Map<RoadType, Tile> rts = gfx.roadTiles.get(techid);
		Location[] result = new Location[4];
		result[0] = Location.of(rect.x, rect.y);
		result[1] = Location.of(rect.x + rect.width - 1, rect.y);
		result[2] = Location.of(rect.x, rect.y - rect.height + 1);
		result[3] = Location.of(rect.x + rect.width - 1, rect.y - rect.height + 1);
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		TileFragment tf = TileFragment.of(0, rts.get(RoadType.RIGHT_TO_BOTTOM), true);
		planet.map.put(result[0], tf);
		tf = TileFragment.of(0, rts.get(RoadType.LEFT_TO_BOTTOM), true);
		planet.map.put(result[1], tf);
		tf = TileFragment.of(0, rts.get(RoadType.TOP_TO_RIGHT), true);
		planet.map.put(result[2], tf);
		tf = TileFragment.of(0, rts.get(RoadType.TOP_TO_LEFT), true);
		planet.map.put(result[3], tf);
		// add linear segments
		Tile ht = rts.get(RoadType.HORIZONTAL);
		for (int i = rect.x + 1; i < rect.x + rect.width - 1; i++) {
			tf = TileFragment.of(0, ht, true);
			planet.map.put(Location.of(i, rect.y), tf);
			tf = TileFragment.of(0, ht, true);
			planet.map.put(Location.of(i, rect.y - rect.height + 1), tf);
		}
		Tile vt = rts.get(RoadType.VERTICAL);
		for (int i = rect.y - 1; i > rect.y - rect.height + 1; i--) {
			tf = TileFragment.of(0, vt, true);
			planet.map.put(Location.of(rect.x, i), tf);
			tf = TileFragment.of(0, vt, true);
			planet.map.put(Location.of(rect.x + rect.width - 1, i), tf);
		}
		return result;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		Point pt = e.getPoint();
		if (e.getButton() == MouseEvent.BUTTON3) {
			panMode = false;
		} else
		if (e.getButton() == MouseEvent.BUTTON2) {
			lightMode = false;
		} else
		if (e.getButton() == MouseEvent.BUTTON1) {
			minimapScroll = false;
			boolean needRepaint = buildScroller.isRunning();
			buildScroller.stop();
			for (Btn b : pressButtons) {
				needRepaint |= b.down;
				b.down = false;
			}
			for (Btn b : releaseButtons) {
				needRepaint |= b.down;
				b.down = false;
				if (b.test(pt)) {
					b.click();
				}
			}
			if (needRepaint) {
				repaint();
			}
		}
	}
	/** Execute once flag. */
	boolean once = true;
	/** Indicator to allow building on the currently selected spot. */
	private boolean allowBuild;
	/** The minimum X offset for the panning. */
	private int xOffsetMin;
	/** The maximum X offset for the panning. */
	private int xOffsetMax;
	/** The minimum y offset for panning. */
	private int yOffsetMin;
	/** The maximum y offset for panning. */
	private int yOffsetMax;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
//		if (!e.isControlDown() && !e.isAltDown()) {
//			if (e.getWheelRotation() > 0 & surfaceVariant < 9) {
//				surfaceVariant++;
//			} else 
//			if (e.getWheelRotation() < 0 && surfaceVariant > 1) {
//				surfaceVariant--;
//			}
//			changeSurface();
//		} else 
//		if (e.isControlDown()) {
//			if (e.getWheelRotation() < 0 & scale < 32) {
//				scale *= 2;
//			} else 
//			if (e.getWheelRotation() > 0 && scale > 1f / 32) {
//				scale /= 2;
//			}
//		} else
//		if (e.isAltDown()) {
//			if (e.getWheelRotation() < 0 && surfaceType > 1) {
//				surfaceType--;
//			} else
//			if (e.getWheelRotation() > 0 && surfaceType < 7) {
//				surfaceType++;
//			}
//			changeSurface();
//		}
//		repaint();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// no op
		if (e.getButton() == MouseEvent.BUTTON3 && buildMode) {
			cancelBuildMode();
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		Point pt = e.getPoint();
		updateMouseTileCoords(pt);
	}
	/**
	 * Updates the mouse tile coordinates based on the given mouse position.
	 * @param pt the point
	 */
	private void updateMouseTileCoords(Point pt) {
		int x = pt.x - xoff - mainWindow.x - 27;
		int y = pt.y - yoff - mainWindow.y - 1;
		int a = (int)Math.floor(Tile.toTileX(x, y));
		int b = (int)Math.floor(Tile.toTileY(x, y));
		mouseTileX = a;
		mouseTileY = b;
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
	public void actionPerformed(ActionEvent e) {
		if ("FADE".equals(e.getActionCommand())) {
			doFade();
		} else
		if ("SCROLL-UP".equals(e.getActionCommand())) {
			doBuildScroller(false);
		} else
		if ("SCROLL-DOWN".equals(e.getActionCommand())) {
			doBuildScroller(true);
		} else
		if ("BLINK".equals(e.getActionCommand())) {
			doBlink();
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
		repaint(); //FIXME	repaint performance
	}
	/**
	 * Invoked when the fading operation is completed.
	 */
	private void doFadeCompleted() {
		if (onStarmapClicked != null) {
			onStarmapClicked.invoke();
		}
		darkness = 0f;
	}
	/** 
	 * Action for build list scrolling.
	 * @param direction true: down, false: up 
	 */
	private void doBuildScroller(boolean direction) {
		if (direction) {
			doBuildNext();
		} else {
			doBuildPrev();
		}
	}
	/** Perform action on bridge button click. */
	protected void doBridgeClick() {
		uiSound.playSound("Bridge");
		if (onBridgeClicked != null) {
			onBridgeClicked.invoke();
		}
	}
	/** Perform action on starmap button click. */
	protected void doStarmapRecClick() {
		uiSound.playSound("Starmap");
		fadeDirection = false;
		fadeTimer.start();
	}
	/** Perform colony button click. */
	protected void doColonyInfoClick() {
		uiSound.playSound("ColonyInformation");
		if (onInformationClicked != null) {
			onInformationClicked.invoke();
		}
	}
	/** Do planet click. */
	protected void doPlanetClick() {
		uiSound.playSound("Planets");
		if (onPlanetsClicked != null) {
			onPlanetsClicked.invoke();
		}
	}
	/** Do Screen buttons click. */
	protected void doScreenClick() {
		btnColonyInfo.visible = btnButtons.down;
		btnPlanet.visible = btnButtons.down;
		btnStarmap.visible = btnButtons.down;
		btnBridge.visible = btnButtons.down;
		repaint();
	}
	/** Do building info button click. */
	protected void doBuildingInfoClick() {
		btnDemolish.disabled = !btnBuildingInfo.down;
		repaint(buildingInfoPanelRect);
	}
	/** Do radar button click. */
	protected void doRadarClick() {
		repaint(radarPanelRect);
	}
	/** Do building button click. */
	protected void doBuildingClick() {
		btnBuild.disabled = !btnBuilding.down;
		btnList.disabled = !btnBuilding.down;
		btnBuildPrev.disabled = !btnBuilding.down;
		btnBuildNext.disabled = !btnBuilding.down;
		repaint(buildPanelRect);
	}
	/**
	 * Sets the onStarmapClicked action.
	 * @param onStarmapClicked the onStarmapClick to set
	 */
	public void setOnStarmapClicked(BtnAction onStarmapClicked) {
		this.onStarmapClicked = onStarmapClicked;
	}
	/**
	 * @return the onStarmapClick
	 */
	public BtnAction getOnStarmapClicked() {
		return onStarmapClicked;
	}
	/**
	 * @param onInformationClicked the onInformationClick to set
	 */
	public void setOnInformationClicked(BtnAction onInformationClicked) {
		this.onInformationClicked = onInformationClicked;
	}
	/**
	 * @return the onInformationClick
	 */
	public BtnAction getOnInformationClicked() {
		return onInformationClicked;
	}
	/**
	 * @param onBridgeClicked the onBridgeClicked to set
	 */
	public void setOnBridgeClicked(BtnAction onBridgeClicked) {
		this.onBridgeClicked = onBridgeClicked;
	}
	/**
	 * @return the onBridgeClicked
	 */
	public BtnAction getOnBridgeClicked() {
		return onBridgeClicked;
	}
	/**
	 * @param onPlanetsClicked the onPlanetsClicked to set
	 */
	public void setOnPlanetsClicked(BtnAction onPlanetsClicked) {
		this.onPlanetsClicked = onPlanetsClicked;
	}
	/**
	 * @return the onPlanetsClicked
	 */
	public BtnAction getOnPlanetsClicked() {
		return onPlanetsClicked;
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
	/**
	 * Render buildings panel.
	 * @param g2 the graphics object
	 */
	private void renderBuildingsPanel(Graphics2D g2) {
		Shape sp = g2.getClip();
		g2.drawImage(gfx.buildPanel, buildPanelRect.x, buildPanelRect.y, null);
		List<GameBuildingPrototype> list = getBuildingList();
		GameBuildingPrototype bp = gameWorld.player.selectedBuildingPrototype;
		int idx = list.indexOf(bp);
		if (idx < 0 && list.size() > 0) {
			idx = 0;
			gameWorld.player.selectedBuildingPrototype = list.get(0);
			bp = list.get(0);
		}
		if (bp != null) {
			BuildingImages bi = bp.images.get(getTechId());
			if (bp != null && bi != null) {
				g2.setClip(buildImageRect);
				g2.drawImage(bi.thumbnail, buildImageRect.x + (buildImageRect.width - bi.thumbnail.getWidth()) / 2,
						buildImageRect.y + (buildImageRect.height - bi.thumbnail.getHeight()) / 2,  null);
				
				if (!gameWorld.isBuildableOnPlanet(bp)) {
					g2.setColor(Color.RED);
					g2.drawLine(buildImageRect.x, buildImageRect.y, 
							buildImageRect.x + buildImageRect.width - 1, 
							buildImageRect.y + buildImageRect.height - 1
					);
					g2.drawLine(buildImageRect.x + buildImageRect.width - 1, buildImageRect.y, 
							buildImageRect.x, 
							buildImageRect.y + buildImageRect.height - 1 
					);
				}
				
				String costStr = bp.cost + " " + gameWorld.getLabel("BuildingInfo.ProductionUnitFor.credit");
				int len0 = text.getTextWidth(7, costStr);
				text.paintTo(g2, buildImageRect.x + (buildImageRect.width - len0) - 2,
						buildImageRect.y + (buildImageRect.height - 10),
						7, TextGFX.YELLOW, costStr);
				
				g2.setClip(buildNameRect);
				int len = text.getTextWidth(10, bp.name);
				text.paintTo(g2, buildNameRect.x + (buildNameRect.width - len) / 2,
						buildNameRect.y + (buildNameRect.height - 10) / 2,
						10, TextGFX.YELLOW, bp.name);
			}
		}
		g2.setClip(sp);
		if (idx <= 0) {
			g2.drawImage(gfx.buildScrollNone, btnBuildPrev.rect.x, btnBuildPrev.rect.y, null);
		} else
		if (btnBuildPrev.down) {
			g2.drawImage(gfx.buildScrollUpDown, btnBuildPrev.rect.x, btnBuildPrev.rect.y, null);
		}
		if (idx >= list.size() - 1) {
			g2.drawImage(gfx.buildScrollNone, btnBuildNext.rect.x, btnBuildNext.rect.y, null);
		} else
		if (btnBuildNext.down) {
			g2.drawImage(gfx.buildScrollDownDown, btnBuildNext.rect.x, btnBuildNext.rect.y, null);
		}
		if (btnList.down) {
			g2.drawImage(gfx.listDown, btnList.rect.x, btnList.rect.y, null);
		}
		if (btnBuild.down) {
			g2.drawImage(gfx.buildDown, btnBuild.rect.x, btnBuild.rect.y, null);
		}
		g2.setClip(sp);
	}
	/**
	 * @param onListClicked the onListClicked to set
	 */
	public void setOnListClicked(BtnAction onListClicked) {
		this.onListClicked = onListClicked;
	}
	/**
	 * @return the onListClicked
	 */
	public BtnAction getOnListClicked() {
		return onListClicked;
	}
	/**
	 * Fixes the road joints and unnecessary roads on the planet map.
	 */
	private void fixRoads() {
		GamePlanet planet = gameWorld.player.selectedPlanet;
		String techid = getTechId();
		Map<RoadType, Tile> rts = gfx.roadTiles.get(techid);
		Map<Tile, RoadType> trs = gfx.tileRoads.get(techid);
		Map<Location, TileFragment> map = planet.map;
		// remove all roads from the planet
		for (Map.Entry<Location, TileFragment> e : new LinkedList<Map.Entry<Location, TileFragment>>(map.entrySet())) {
			if (e.getValue().isRoad) {
				map.remove(e.getKey());
			}
		}
		Set<Location> corners = new HashSet<Location>();
		for (GameBuilding b : planet.buildings) {
			Rectangle rect = b.getRectWithRoad();
			Location[] locations = addRoadAround(planet, b.images.techId, rect);
			corners.addAll(Arrays.asList(locations));
			// add neighboring locations to the corners
//			corners.add(Location.of(locations[0].x, locations[0].y + 1));
//			corners.add(Location.of(locations[0].x - 1, locations[0].y));
//
//			corners.add(Location.of(locations[1].x, locations[1].y + 1));
//			corners.add(Location.of(locations[1].x + 1, locations[1].y));
//
//			corners.add(Location.of(locations[2].x, locations[2].y - 1));
//			corners.add(Location.of(locations[2].x - 1, locations[2].y));
//
//			corners.add(Location.of(locations[3].x, locations[3].y - 1));
//			corners.add(Location.of(locations[3].x + 1, locations[3].y));
		}
		TileFragment[] neighbors = new TileFragment[9];
		for (Location l : corners) {
			TileFragment tf = map.get(l);
			if (tf == null || !tf.isRoad) {
				continue;
			}
			setNeighbors(l.x, l.y, map, neighbors);
			int pattern = 0;
			RoadType rt1 = null;
			if (neighbors[1] != null && neighbors[1].isRoad) {
				pattern |= Sides.TOP;
				rt1 = trs.get(neighbors[1].provider);
			}
			RoadType rt3 = null;
			if (neighbors[3] != null && neighbors[3].isRoad) {
				pattern |= Sides.LEFT;
				rt3 = trs.get(neighbors[3].provider);
			}
			RoadType rt5 = null;
			if (neighbors[5] != null && neighbors[5].isRoad) {
				pattern |= Sides.RIGHT;
				rt5 = trs.get(neighbors[5].provider);
			}
			RoadType rt7 = null;
			if (neighbors[7] != null && neighbors[7].isRoad) {
				pattern |= Sides.BOTTOM;
				rt7 = trs.get(neighbors[7].provider);
			}
			RoadType rt = RoadType.get(pattern);
			// place the new tile fragment onto the map
			// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
			tf = TileFragment.of(0, rts.get(rt), true);
			map.put(l, tf);
			// alter the four neighboring tiles to contain road back to this
			if (rt1 != null) {
				rt1 = RoadType.get(rt1.pattern | Sides.BOTTOM);
				map.put(l.delta(0, 1), TileFragment.of(0, rts.get(rt1), true));
			}
			if (rt3 != null) {
				rt3 = RoadType.get(rt3.pattern | Sides.RIGHT);
				map.put(l.delta(-1, 0), TileFragment.of(0, rts.get(rt3), true));
			}
			if (rt5 != null) {
				rt5 = RoadType.get(rt5.pattern | Sides.LEFT);
				map.put(l.delta(1, 0), TileFragment.of(0, rts.get(rt5), true));
			}
			if (rt7 != null) {
				rt7 = RoadType.get(rt7.pattern | Sides.TOP);
				map.put(l.delta(0, -1), TileFragment.of(0, rts.get(rt7), true));
			}
			
		}
//		repaint(mainWindow);
	}
	/**
	 * Fills the fragment array of the 3x3 rectangle centered around x and y.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param map the map
	 * @param fragments the fragments
	 */
	private void setNeighbors(int x, int y, Map<Location, TileFragment> map, TileFragment[] fragments) {
		fragments[0] = map.get(Location.of(x - 1, y + 1));
		fragments[1] = map.get(Location.of(x, y + 1));
		fragments[2] = map.get(Location.of(x + 1, y + 1));
		
		fragments[3] = map.get(Location.of(x - 1, y));
		fragments[4] = map.get(Location.of(x, y));
		fragments[5] = map.get(Location.of(x + 1, y));
		
		fragments[6] = map.get(Location.of(x - 1, y - 1));
		fragments[7] = map.get(Location.of(x, y - 1));
		fragments[8] = map.get(Location.of(x + 1, y - 1));
	}
	/**
	 * Render the minimap.
	 * @param g2 the graphics object.
	 */
	private void renderMinimap(Graphics2D g2) {
		int x = Tile.toScreenX(0, 0);
		double xs = minimapRect.width / (double)(xOffsetMax - xOffsetMin);
		double ys = minimapRect.height / (double)(yOffsetMax - yOffsetMin);
		
		if (radarImage == null || radarImagePlanet != gameWorld.player.selectedPlanet) {
			radarImage = new BufferedImage(minimapRect.width, minimapRect.height, BufferedImage.TYPE_INT_ARGB);
			radarImagePlanet = gameWorld.player.selectedPlanet;
			
			Graphics2D rg = radarImage.createGraphics();
			rg.scale(xs, ys);
			if (interpolation != ImageInterpolation.NONE) {
				rg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation.hint);
			}
			renderContents(rg, new SurfaceRendering((x - xOffsetMin), -yOffsetMin, false, true));
			rg.dispose();
//			rebuildRadarAsync();
		}
		if (radarImage != null) {
			g2.drawImage(radarImage, minimapRect.x, minimapRect.y, null);
		}
		Shape sp = g2.getClip();
		g2.setClip(minimapRect);
		g2.setColor(Color.RED);
		g2.drawRect((int)(minimapRect.x + (-xoff - xOffsetMin) * xs), 
				(int)(minimapRect.y + (-yoff - yOffsetMin) * ys), 
				(int)(mainWindow.width * xs), (int)(mainWindow.height * ys));
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
	/**
	 * Clear radar cache.
	 */
	public void clearRadarCache() {
		radarImage = null;
		radarImagePlanet = null;
		repaint(minimapRect);
	}
	/**
	 * Start animation timers.
	 */
	public void startTimers() {
		blinkTimer.start();
	}
	/**
	 * Stop animation timers.
	 */
	public void stopTimers() {
		blinkTimer.stop();
	}
	/**
	 * Render the building information panel contents.
	 * @param g2 the graphics object
	 */
	private void renderBuildingInfo(Graphics2D g2) {
		GameBuilding b = gameWorld.player.selectedPlanet.selectedBuilding;
		if (b == null) {
			return;
		}
		Shape sp = g2.getClip();
		g2.setClip(buildingInfoName);
		
		int nameLen = text.getTextWidth(10, b.prototype.name);
		text.paintTo(g2, buildingInfoName.x + (buildingInfoName.width - nameLen) / 2, 
				buildingInfoName.y + (buildingInfoName.height - 10) / 2, 10, TextGFX.YELLOW, b.prototype.name);
		
		g2.setClip(buildingInfoPanelRect);
		if (btnDemolish.down) {
			g2.drawImage(gfx.demolishDown, btnDemolish.rect.x, btnDemolish.rect.y, null);
		}
		// energy
		float ep = b.getEnergyPercent();
		String value = ep > 0 ? Integer.toString((int)(ep * 100)) : " -";
		g2.setClip(energyPercentRect);
		text.paintTo(g2, energyPercentRect.x + 1, energyPercentRect.y + 1, 10, TextGFX.YELLOW, value);
		int ev = b.energy;
		value = ev > 0 ? Integer.toString(ev) : "  -";
		g2.setClip(energyRect);
		text.paintTo(g2, energyRect.x + 1, energyRect.y + 1, 10, TextGFX.YELLOW, value);
		// worker
		int wp = b.workers;
		value = wp > 0 ? Integer.toString(wp) : " -";
		g2.setClip(workerPercentRect);
		text.paintTo(g2, workerPercentRect.x + 1, workerPercentRect.y + 1, 10, TextGFX.YELLOW, value);
		int wv = b.getWorkerDemand();
		value = wv > 0 ? Integer.toString(wv) : " -";
		g2.setClip(workerRect);
		text.paintTo(g2, workerRect.x + 1, workerRect.y + 1, 10, TextGFX.YELLOW, value);
		// aggregated percent
		float op = b.getOperationPercent();
		value = op > 0 ? Integer.toString((int)(op * 100)) : " -";
		g2.setClip(operationPercentRect);
		text.paintTo(g2, operationPercentRect.x + 1, operationPercentRect.y + 1, 10, TextGFX.YELLOW, value);
		
		Tuple2<String, Object> primaryProduction = b.getPrimaryProduction();
		if (primaryProduction != null) {
			g2.setClip(productionRect);
			text.paintTo(g2, productionRect.x + 1, productionRect.y + 1, 10, TextGFX.YELLOW, 
				primaryProduction.second + " " 
				+ gameWorld.getLabel("BuildingInfo.ProductionUnitFor." + primaryProduction.first)
			);
		}
		g2.setClip(btnActive.rect);
		btnActive.disabled = b.progress < 100;
		if (b.progress == 100) {
			if (b.enabled) {
				if (b.getOperationPercent() < 0.5f) {
					g2.drawImage(gfx.inoperational, btnActive.rect.x, btnActive.rect.y, null);
				}
			} else {
				g2.drawImage(gfx.offline, btnActive.rect.x, btnActive.rect.y, null);
			}
		} else {
			g2.drawImage(gfx.completedPercent, btnActive.rect.x, btnActive.rect.y, null);
			String s = Integer.toString(b.progress);
			int l = text.getTextWidth(10, s);
			text.paintTo(g2, btnActive.rect.x + 96 + (28 - l) / 2, btnActive.rect.y + 3, 10, TextGFX.YELLOW, s);
		}
		g2.setClip(btnRepair.rect);
		btnRepair.disabled = true;
		if (b.progress == 100) {
			if (b.health < 100) {
				btnRepair.disabled = false;
				if (b.repairing) {
					g2.drawImage(gfx.repairPercent, btnRepair.rect.x, btnRepair.rect.y, null);
				} else {
					g2.drawImage(gfx.damagedPercent, btnRepair.rect.x, btnRepair.rect.y, null);
				}
				String s = Integer.toString(100 - b.health);
				int l = text.getTextWidth(10, s);
				text.paintTo(g2, btnRepair.rect.x + 96 + (28 - l) / 2, btnRepair.rect.y + 3, 10, TextGFX.YELLOW, s);
			}
		}		
		g2.setClip(sp);
	}
	/**
	 * Perform actions on a blink event.
	 */
	private void doBlink() {
		blinkStatus = !blinkStatus;
		// FIXME simulation related stuff!
		reallocateResources();
		repaint(mainWindow); //FIXME repaint performance
	}
	/**
	 * Reallocates resources and asks for radar redraw on demand.
	 */
	private void reallocateResources() {
		GamePlanet p = gameWorld.player.selectedPlanet;
		boolean changes = false;
		if (p != null) {
			changes = ResourceAllocator.allocateWorkers(p);
			changes |= ResourceAllocator.allocateEnergy(p);
			if (changes) {
				p.update();
				radarImage = null;
			}
		}
	}
}
