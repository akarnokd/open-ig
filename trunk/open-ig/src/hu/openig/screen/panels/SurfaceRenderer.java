/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.core.Location;
import hu.openig.core.Pair;
import hu.openig.model.Building;
import hu.openig.model.Configuration;
import hu.openig.model.ExplosionType;
import hu.openig.model.GroundwarExplosion;
import hu.openig.model.GroundwarGun;
import hu.openig.model.GroundwarManager;
import hu.openig.model.GroundwarRocket;
import hu.openig.model.GroundwarUnit;
import hu.openig.model.Mine;
import hu.openig.model.Planet;
import hu.openig.model.PlanetGround;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetProblems;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.PlanetSurface;
import hu.openig.model.Player;
import hu.openig.model.SelectionBoxMode;
import hu.openig.model.SurfaceCell;
import hu.openig.model.SurfaceEntity;
import hu.openig.model.SurfaceEntityType;
import hu.openig.model.SurfaceFeature;
import hu.openig.model.Tile;
import hu.openig.model.World;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;
import hu.openig.screen.api.SurfaceEvents;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.utils.ImageUtils;
import hu.openig.utils.U;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The surface renderer component.
 * @author akarnokd, Mar 27, 2011
 */
public class SurfaceRenderer extends UIComponent {
	/** The current scaling factor. */
	public double scale = 1;
	/** The offset X. */
	public int offsetX;
	/** The offset Y. */
	public int offsetY;
	/** The common resources. */
	private final CommonResources commons;
	/** The current configuration. */
	private final Configuration config;
	/** The last mouse coordinate. */
	int lastX;
	/** The last mouse coordinate. */
	int lastY;
	/** Is the map dragged. */
	public boolean drag;
	/** The building bounding box. */
	public Rectangle buildingBox;
	/** Used to place buildings on the surface. */
	public final Rectangle placementRectangle = new Rectangle();
	/** Are we in placement mode? */
	public boolean placementMode;
	/** The event callback object. */
	final SurfaceEvents events;
	/** Enable the drawing of black boxes behind building names and percentages. */
	public boolean textBackgrounds = true;
	/** Render placement hints on the surface. */
	public boolean placementHints;
	/** The unit selection rectangle start. */
	public Point selectionStart;
	/** The unit selection rectangle end. */
	public Point selectionEnd;
	/** The selection tile. */
	public Tile selection;
	/** The placement tile for allowed area. */
	public Tile areaAccept;
	/** The empty tile indicator. */
	public Tile areaEmpty;
	/** The area where to deploy. */
	public Tile areaDeploy;
	/** The placement tile for denied area. */
	public Tile areaDeny;
	/** The current cell tile. */
	public Tile areaCurrent;
	/** The simple blinking state. */
	public boolean blink;
	/** The animation index. */
	public int animation;
	/** The lighting level. */
	public float alpha = 1.0f;
	/** The user is dragging a selection box. */
	public boolean selectionMode;
	/** Indicate the deploy spray mode. */
	public boolean deploySpray;
	/** Indicate the undeploy spray mode. */
	public boolean undeploySpray;
	/** Display the commands given to units. */
	public boolean showCommand;
	/** Indicate the left click will select an attack target. */
	public boolean attackSelect;
	/** Indicate the left click will select a move target. */
	public boolean moveSelect;
	/** Contains the buildings panel's bounds. */
	public Rectangle buildingsPanelLocation;
	/** The weather overlay. */
	public WeatherOverlay weatherOverlay;
	/** The surface cell helper. */
	final SurfaceCell cell = new SurfaceCell();
	/** The cached zigzag map. */
	static final Map<Pair<Integer, Integer>, Pair<int[], int[]>> ZIGZAGS = new HashMap<>();
	/**
	 * Compute the X, Y coordinates of the linear address in the zig-zagged coordinate system
	 * enclosed by a rectangle of width and height.
	 * @param linear the linear address, &lt; width * height;
	 * @param width the width of the enclosing rectangle
	 * @param height the height of the rectangle
	 * @return the point
	 */
	static Point deZigZag(int linear, int width, int height) {
		
		Pair<Integer, Integer> key = Pair.of(width, height);
		
		int[] xs;
		int[] ys;
		
		Pair<int[], int[]> value = ZIGZAGS.get(key);
		if (value == null) {
			int wh = width * height;
			
			xs = new int[wh];
			ys = new int[wh];
			
			int base = width <= height ? width : height;
			int s = 0;
			for (int i = 0; i < base; i++) {
				int s2 = s + i + 1;
				for (int j = s; j < s2; j++) {
					xs[j] = i - j + s;
					ys[j] = j - s;
					
					xs[wh - 1 - j] = (width - 1) - xs[j];
					ys[wh - 1 - j] = (height - 1) - ys[j];
				}
				s = s2;
			}
			if (width <= height) {
				for (int i = 0; i < height - width - 1; i++) {
					for (int j = 0; j < width; j++) {
						xs[s] = width - 1 - j;
						ys[s] = i + 1 + j;
						s++;
					}
				}
			} else {
				for (int i = 0; i < width - height - 1; i++) {
					for (int j = 0; j < height; j++) {
						xs[s] = height + i - j;
						ys[s] = j;
						s++;
					}
				}
			}
			ZIGZAGS.put(key, Pair.of(xs, ys));
		} else {
			xs = value.first;
			ys = value.second;
		}
		return new Point(xs[linear], ys[linear]);
	}
	/**
	 * Constructor, sets the common resources.
	 * @param commons the common resources
	 * @param events the events callback object
	 */
	public SurfaceRenderer(
			CommonResources commons,
			SurfaceEvents events) {
		this.commons = Objects.requireNonNull(commons);
		this.events = Objects.requireNonNull(events);
		this.config = commons.config;

		selection = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileEdge, 0xFFFFFF00), null);
		areaAccept = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileEdge, 0xFF00FFFF), null);
		areaEmpty = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileEdge, 0xFF808080), null);
		areaDeploy = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileEdge, 0xFF00FFFF), null);
		areaDeny = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileCrossed, 0xFFFF0000), null);
		areaCurrent  = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileCrossed, 0xFFFFCC00), null);
		
		selection.alpha = 1.0f;
		areaAccept.alpha = 1.0f;
		areaDeny.alpha = 1.0f;
		areaCurrent.alpha = 1.0f;
		
		weatherOverlay = new WeatherOverlay(new Dimension(640, 480));
	}
	/**
	 * Returns the world object.
	 * @return the world object
	 */
	public World world() {
		return commons.world();
	}
	/**
	 * Returns the current planet of the player.
	 * @return the current planet of the player
	 */
	public Planet planet() {
		return player().currentPlanet;
	}
	/**
	 * Returns the current player.
	 * @return the current player
	 */
	public Player player() {
		return commons.player();
	}
	/**
	 * Surface of the current planet.
	 * @return the surface of the current planet
	 */
	public PlanetSurface surface() {
		return planet().surface;
	}
	/**
	 * The ground object of the current planet.
	 * @return the ground object
	 */
	public PlanetGround ground() {
		return planet().ground;
	}
	/**
	 * The war manager for the current planet.
	 * @return the current planet
	 */
	public GroundwarManager war() {
		return planet().war;
	}
	/**
	 * Get a location based on the mouse coordinates.
	 * @param mx the mouse X coordinate
	 * @param my the mouse Y coordinate
	 * @return the location
	 */
	public Location getLocationAt(int mx, int my) {
		PlanetSurface surface = surface();
		if (surface != null) {
			double mx0 = mx - (surface.baseXOffset + 28) * scale - offsetX; // Half left
			double my0 = my - (surface.baseYOffset + 27) * scale - offsetY; // Half up
			int a = (int)Math.floor(Tile.toTileX((int)mx0, (int)my0) / scale);
			int b = (int)Math.floor(Tile.toTileY((int)mx0, (int)my0) / scale) ;
			return Location.of(a, b);
		}
		return null;
	}
	@Override
	public boolean mouse(UIMouse e) {
		Planet planet = planet();
		PlanetSurface surface = surface();
		Player player = player();
		
		boolean rep = false;
		switch (e.type) {
		case MOVE:
		case DRAG:
			if (drag || commons.isPanningEvent(e)) {
				if (!drag) {
					drag = true;
					lastX = e.x;
					lastY = e.y;
					events.onDrag(true);
				}
				offsetX += e.x - lastX;
				offsetY += e.y - lastY;

				lastX = e.x;
				lastY = e.y;
				rep = true;
			} else
				if (placementMode) {
					int mx = e.x;
					int my = e.y;
					if (placementRectangle.width % 2 == 0) {
						mx += 28;
					}
					if (placementRectangle.height % 2 == 0) {
						my += 13;
					}
					Location current = getLocationAt(mx, my);
					if (current != null) {
						placementRectangle.x = current.x - placementRectangle.width / 2;
						placementRectangle.y = current.y + placementRectangle.height / 2;
						rep = true;
					}
				} else
					if (selectionMode) {
						selectionEnd = new Point(e.x, e.y);
						rep = true;
					}
			if (deploySpray) {
				events.placeUnitAt(e.x, e.y);
				rep = true;
			} else
				if (undeploySpray) {
					events.removeUnitAt(e.x, e.y);
					rep = true;
				}
			break;
		case DOUBLE_CLICK:
			if (e.has(Button.LEFT)) {
				SelectionBoxMode mode = SelectionBoxMode.NEW;
				if (e.has(Modifier.SHIFT)) {
					mode = SelectionBoxMode.ADD;
				} else
					if (e.has(Modifier.CTRL)) {
						mode = SelectionBoxMode.SUBTRACT;
					}
				events.selectUnitType(e.x, e.y, mode);
				rep = true;
			}
			break;
		case DOWN:
			if (e.has(Button.LEFT)) {
				if (moveSelect) {
					events.unitsMove(e.x, e.y);
					rep = true;
					break;
				} else
					if (attackSelect) {
						if (e.has(Modifier.CTRL)) {
							events.unitsAttackMove(e.x, e.y);
						} else {
							events.unitsAttack(e.x, e.y);
						}
						rep = true;
						break;
					} else {
						events.toggleUnitPlacement(e.x, e.y);
					}
			} else
				if (e.has(Button.RIGHT)) {
					if (commons.config.classicControls) {
						Building b = buildingAt(e.x, e.y);
						if (b != null) {
							if (planet.owner == selectionOwner()) {
								events.unitsMove(e.x, e.y);
								rep = true;
							} else {
								events.unitsAttack(e.x, e.y);
								rep = true;
							}
						} else {
							GroundwarUnit u = unitAt(e.x, e.y);
							if (u != null && u.owner != selectionOwner()) {
								events.unitsAttack(e.x, e.y);
								rep = true;
							} else {
								if (e.has(Modifier.CTRL)) {
									events.unitsAttackMove(e.x, e.y);
								} else {
									events.unitsMove(e.x, e.y);
								}
								rep = true;
							}
						}
					} else {
					if (e.has(Modifier.SHIFT) && !e.has(Modifier.CTRL)) {
						events.unitsMove(e.x, e.y);
						rep = true;
					} else 
						if (e.has(Modifier.CTRL) && !e.has(Modifier.SHIFT)) {
							events.unitsAttack(e.x, e.y);
							rep = true;
						} else 
							if (e.has(Modifier.CTRL) && e.has(Modifier.SHIFT)) {
								events.unitsAttackMove(e.x, e.y);
								rep = true;
							} else {
								drag = true;
								lastX = e.x;
								lastY = e.y;
								events.onDrag(true);
							}
				}
			}
			if (e.has(Button.MIDDLE) && config.classicControls == e.has(Modifier.CTRL)) {
				offsetX = -(surface.boundingRectangle.width - width) / 2;
				offsetY = -(surface.boundingRectangle.height - height) / 2;
				scale = 1;
				rep = true;
			}
			if (e.has(Button.LEFT)) {
				if (placementMode) {
					events.placeBuilding(e.has(Modifier.SHIFT));	
				} else {
					if (player.knowledge(planet, PlanetKnowledge.OWNER) >= 0 
							|| planet.owner == player) {
						Location loc = getLocationAt(e.x, e.y);
						buildingBox = surface.getBoundingRect(loc);
						events.selectBuilding(surface.getBuildingAt(loc));
						rep = true;
					} else {
						events.selectBuilding(null);
					}
					if (planet.war != null) {
						if (!deploySpray && !undeploySpray) {
							selectionMode = true;
							selectionStart = new Point(e.x, e.y);
							selectionEnd = selectionStart;
							rep = true;
							events.onDrag(true);
						}
					} 
				}
			}
			break;
		case UP:
			if (e.has(Button.RIGHT) || e.has(Button.MIDDLE)) {
				drag = false;
				events.onDrag(false);
			}
			if (e.has(Button.LEFT) && selectionMode) {
				selectionMode = false;
				selectionEnd = new Point(e.x, e.y);
				SelectionBoxMode mode = SelectionBoxMode.NEW;
				if (e.has(Modifier.SHIFT)) {
					mode = SelectionBoxMode.ADD;
				} else
					if (e.has(Modifier.CTRL)) {
						mode = SelectionBoxMode.SUBTRACT;
					}
				events.selectUnits(mode);
				events.onDrag(false);
			}
			if (e.has(Button.LEFT)) {
				deploySpray = false;
				undeploySpray = false;
			}
			rep = true;
			break;
		case LEAVE:
			drag = false;
			events.onDrag(false);
			if (selectionMode) {
				selectionMode = false;
				selectionEnd = new Point(e.x, e.y);
				SelectionBoxMode mode = SelectionBoxMode.NEW;
				if (e.has(Modifier.SHIFT)) {
					mode = SelectionBoxMode.ADD;
				} else
					if (e.has(Modifier.CTRL)) {
						mode = SelectionBoxMode.SUBTRACT;
					}
				events.selectUnits(mode);
				rep = true;
			}
			break;
		case WHEEL:
			if (e.has(Modifier.CTRL)) {
				double pre = scale;
				int ox = offsetX;
				int oy = offsetY;
				double mx = (e.x - ox) / pre;
				double my = (e.y - oy) / pre;
				if (e.z < 0) {
					zoomIn();
				} else {
					zoomOut();
				}
				offsetX = (int)(e.x - scale * mx);
				offsetY = (int)(e.y - scale * my);
				rep = true;
			} else
				if (e.has(Modifier.SHIFT)) {
					if (e.z < 0) {
						offsetX += 54;
					} else {
						offsetX -= 54;
					}
					rep = true;
				} else {
					if (e.z < 0) {
						offsetY += 28 * 3;
					} else {
						offsetY -= 28 * 3;
					}
					rep = true;
				}
			break;
		default:
		}
		return rep;
	}
	@Override
	public void draw(Graphics2D g2) {
		Player player = player();
		Planet planet = planet();
		PlanetSurface surface = surface();
		PlanetGround ground = ground();
		
		if (surface == null) {
			return;
		}

		PlanetStatistics ps = events.update(surface);

		computeAlpha();

		RenderTools.setInterpolation(g2, true);

		Shape save0 = g2.getClip();
		g2.clipRect(0, 0, width, height);

		g2.setColor(new Color(96 * alpha / 255, 96 * alpha / 255, 96 * alpha / 255));
		g2.fillRect(0, 0, width, height);


		AffineTransform at = g2.getTransform();
		g2.translate(offsetX, offsetY);
		g2.scale(scale, scale);

		int x0 = surface.baseXOffset;
		int y0 = surface.baseYOffset;

		if (player.knowledge(planet, PlanetKnowledge.NAME) >= 0) {

			drawTiles(g2, surface, x0, y0);
			if (placementHints) {
				for (Location loc : surface.basemap.keySet()) {
					if (!surface.placement.canPlaceBuilding(loc.x, loc.y)) {
						int x = x0 + Tile.toScreenX(loc.x, loc.y);
						int y = y0 + Tile.toScreenY(loc.x, loc.y);
						g2.drawImage(areaDeny.getStrip(0), x, y, null);
					}
				}
			}
			if (planet.war != null) {
				for (Location loc : planet.war.battlePlacements) {
					int x = x0 + Tile.toScreenX(loc.x, loc.y);
					int y = y0 + Tile.toScreenY(loc.x, loc.y);
					g2.drawImage(areaDeploy.getStrip(0), x, y, null);
				}
			}
			//				if (!placementMode) {
				//					if (selectedRectangle != null) {
			//						for (int i = selectedRectangle.x; i < selectedRectangle.x + selectedRectangle.width; i++) {
			//							for (int j = selectedRectangle.y; j > selectedRectangle.y - selectedRectangle.height; j--) {
			//								int x = x0 + Tile.toScreenX(i, j);
			//								int y = y0 + Tile.toScreenY(i, j);
			//								g2.drawImage(selection.getStrip(0), x, y, null);
			//							}
			//						}
			//					}
			//				}
			if (placementMode) {
				if (placementRectangle.width > 0) {
					for (int i = placementRectangle.x; i < placementRectangle.x + placementRectangle.width; i++) {
						for (int j = placementRectangle.y; j > placementRectangle.y - placementRectangle.height; j--) {

							BufferedImage img = areaAccept.getStrip(0);
							// check for existing building
							if (!surface.placement.canPlaceBuilding(i, j)) {
								img = areaDeny.getStrip(0);
							}

							int x = x0 + Tile.toScreenX(i, j);
							int y = y0 + Tile.toScreenY(i, j);
							g2.drawImage(img, x, y, null);
						}
					}
				}
			}
			drawBattleHelpers(g2, x0, y0);
			if (config.showBuildingName 
					&& (player.knowledge(planet, PlanetKnowledge.BUILDING) >= 0 
					|| (planet.war != null && planet.war.hasUnits(player)))) {
				drawBuildingHelpers(g2, surface);
			}
			// paint red on overlapping images of buildings, land-features and vehicles

			if (!ground.units.isEmpty()) {
				drawHiddenUnitIndicators(g2);
			}

			drawWeather(g2, surface);

			if (player.knowledge(planet, PlanetKnowledge.OWNER) >= 0 && buildingBox != null) {
				g2.setColor(Color.RED);
				g2.drawRect(buildingBox.x, buildingBox.y, buildingBox.width, buildingBox.height);
			}

			g2.setTransform(at);
			g2.setColor(Color.BLACK);

			String pn = planet.name();
			int nameHeight = 14;
			int nameWidth = commons.text().getTextWidth(nameHeight, pn);
			int nameLeft = (width - nameWidth) / 2;
			g2.fillRect(nameLeft - 5, 0, nameWidth + 10, nameHeight + 4);

			int pc = TextRenderer.GRAY;
			if (player.knowledge(planet, PlanetKnowledge.OWNER) >= 0 
					&& planet.owner != null) {
				pc = planet.owner.color;
			}
			commons.text().paintTo(g2, nameLeft, 2, nameHeight, pc, pn);

			if (ps != null && planet.owner == player) {
				renderProblems(g2, ps);
			}
		} else {
			g2.setTransform(at);

			g2.setColor(new Color(0, 0, 0, 128));

			String installSatellite = commons.labels().get("planet.install_satellite_1");
			int tw = commons.text().getTextWidth(14, installSatellite);
			int tx = (width - tw) / 2;
			int ty = (height - 14) / 2 - 12;
			g2.fillRect(tx - 5, ty - 5, tw + 10, 24);
			commons.text().paintTo(g2, tx, ty, 14, TextRenderer.WHITE, installSatellite);

			installSatellite = commons.labels().get("planet.install_satellite_2");
			tw = commons.text().getTextWidth(14, installSatellite);
			tx = (width - tw) / 2;
			ty = (height - 14) / 2 + 12;
			g2.fillRect(tx - 5, ty - 5, tw + 10, 24);
			commons.text().paintTo(g2, tx, ty, 14, TextRenderer.WHITE, installSatellite);
		}

		if (selectionMode) {
			g2.setColor(new Color(255, 255, 255, 128));
			g2.fill(selectionRectangle());
		}

		g2.setClip(save0);
		RenderTools.setInterpolation(g2, false);

		if (planet.war != null) {
			drawNextVehicleToDeploy(g2);
		}
	}
	/**
	 * Draw the next vehicle's image to deploy.
	 * @param g2 the graphics context
	 */
	void drawNextVehicleToDeploy(Graphics2D g2) {
		Player player = player();
		PlanetGround ground = ground();
		GroundwarManager war = war();
		
		LinkedList<GroundwarUnit> unitsToPlace = war.unitsToPlace;
		if (!unitsToPlace.isEmpty()) {
			BufferedImage img = commons.colony().smallInfoPanel;
            // FIXME buildingsPanelLocation is not set anywhere!
			int x = buildingsPanelLocation.x;
			int y = buildingsPanelLocation.y;
			int w = img.getWidth();
			int h = img.getHeight();
			g2.drawImage(img, x, y, null);

			String s = unitsToPlace.size() + " / " + (war.unitsToPlace.size() + ground.countUnits(player));
			int sx = x + (w - commons.text().getTextWidth(7, s)) / 2;
			commons.text().paintTo(g2, sx, y + 10, 7, TextRenderer.YELLOW, s);


			GroundwarUnit u = unitsToPlace.getFirst();

			BufferedImage ui = u.staticImage();
			int ux = x + (w - ui.getWidth()) / 2;
			int uy = y + 24;

			g2.drawImage(ui, ux, uy, null);

			s = u.item.type.name;
			sx = x + (w - commons.text().getTextWidth(7, s)) / 2;
			commons.text().paintTo(g2, sx, y + h - 14, 7, TextRenderer.YELLOW, s);
		}
	}
	/**
	 * Draw hidden unit indicator red pixels.
	 * @param g2 the graphics context
	 */
	void drawHiddenUnitIndicators(Graphics2D g2) {
		PlanetSurface surface = surface();
		PlanetGround ground = ground();
		for (GroundwarUnit u : ground.units) {
			Rectangle ur = unitRectangle(u);


			// compensate rectangle to have only the trimmed image
			BufferedImage bi = u.get();
			int tx = (u.model.width - bi.getWidth()) / 2;
			int ty = (u.model.height - bi.getHeight()) / 2;

			ur.x += tx;
			ur.y += ty;
			ur.width = bi.getWidth();
			ur.height = bi.getHeight();

			for (Building b : surface.buildings.iterable()) {
				if (u.y <= b.location.y - b.tileset.normal.height
						|| u.x < b.location.x
						) {
					continue;
				}
				Rectangle bur = surface.buildingRectangle(b);
				if (ur.intersects(bur)) {
					Rectangle is = ur.intersection(bur);
					BufferedImage ci = collisionImage(u, ur, b.tileset.normal, bur, is);
					if (ci != null) {
						g2.drawImage(ci, is.x, is.y, null);
					}
				}
			}
			for (SurfaceFeature sf : surface.features) {
				if (sf.tile.width > 1 || sf.tile.height > 1) {
					if (u.y <= sf.location.y - sf.tile.height
							|| u.x < sf.location.x
							) {
						continue;
					}
					Rectangle fur = surface.featureRectangle(sf);
					if (ur.intersects(fur)) {
						Rectangle is = ur.intersection(fur);
						BufferedImage ci = collisionImage(u, ur, sf.tile, fur, is);
						if (ci != null) {
							g2.drawImage(ci, is.x, is.y, null);
						}
					}
				}
			}
		}
	}
	/**
	 * Draw building name, upgrade level, damage and allocation status.
	 * @param g2 the graphics context
	 * @param surface the surface object
	 */
	void drawBuildingHelpers(Graphics2D g2, PlanetSurface surface) {
		Player player = player();
		Planet planet = planet();
		for (Building b : surface.buildings.iterable()) {
			Rectangle r = surface.getBoundingRect(b.location);
			int nameSize = 10;
			int nameLen = commons.text().getTextWidth(nameSize, b.type.name);
			int h = (r.height - nameSize) / 2;
			int nx = r.x + (r.width - nameLen) / 2;
			int ny = r.y + h;

			Composite compositeSave = null;
			Composite a1 = null;

			if (textBackgrounds) {
				compositeSave = g2.getComposite();
				a1 = AlphaComposite.SrcOver.derive(0.8f);
				g2.setComposite(a1);
				g2.setColor(Color.BLACK);
				g2.fillRect(nx - 2, ny - 2, nameLen + 4, nameSize + 5);
				g2.setComposite(compositeSave);
			}

			commons.text().paintTo(g2, nx + 1, ny + 1, nameSize, 0xFF8080FF, b.type.name);
			commons.text().paintTo(g2, nx, ny, nameSize, 0xD4FC84, b.type.name);

			// paint upgrade level indicator
			int uw = b.upgradeLevel * commons.colony().upgrade.getWidth();
			int ux = r.x + (r.width - uw) / 2;
			int uy = r.y + h - commons.colony().upgrade.getHeight() - 4; 

			String percent = null;
			int color = 0xFF8080FF;
			if (b.isConstructing()) {
				percent = (b.buildProgress * 100 / b.type.hitpoints) + "%";
			} else
				if (b.hitpoints < b.type.hitpoints) {
					percent = ((b.type.hitpoints - b.hitpoints) * 100 / b.type.hitpoints) + "%";
					if (!blink) {
						color = 0xFFFF0000;
					}
				}
			if (percent != null) {
				int pw = commons.text().getTextWidth(10, percent);
				int px = r.x + (r.width - pw) / 2;
				int py = uy - 14;

				if (textBackgrounds) {
					g2.setComposite(a1);
					g2.setColor(Color.BLACK);
					g2.fillRect(px - 2, py - 2, pw + 4, 15);
					g2.setComposite(compositeSave);
				}

				commons.text().paintTo(g2, px + 1, py + 1, 10, color, percent);
				commons.text().paintTo(g2, px, py, 10, 0xD4FC84, percent);
			}
			if (player.knowledge(planet, PlanetKnowledge.BUILDING) >= 0) {
				for (int i = 1; i <= b.upgradeLevel; i++) {
					g2.drawImage(commons.colony().upgrade, ux, uy, null);
					ux += commons.colony().upgrade.getWidth();
				}

				if (b.enabled) {
					int ey = r.y + h + 11;
					int w = 0;
					if (b.isEnergyShortage()) {
						w += commons.colony().unpowered[0].getWidth();
					}
					if (b.isWorkerShortage()) {
						w += commons.colony().worker[0].getWidth();
					}
					if (b.repairing) {
						w += commons.colony().repair[0].getWidth();
					}
					int ex = r.x + (r.width - w) / 2;

					// paint power shortage
					if (b.isEnergyShortage()) {
						g2.drawImage(commons.colony().unpowered[blink ? 0 : 1], ex, ey, null);
						ex += commons.colony().unpowered[0].getWidth();
					}
					if (b.isWorkerShortage()) {
						g2.drawImage(commons.colony().worker[blink ? 0 : 1], ex, ey, null);
						ex += commons.colony().worker[0].getWidth();
					}
					if (b.repairing) {
						g2.drawImage(commons.colony().repair[(animation % 3)], ex, ey, null);
						ex += commons.colony().repair[0].getWidth();
					}
				} else {
					int ey = r.y + h + 13;
					String offline = commons.labels().get("buildings.offline");
					int w = commons.text().getTextWidth(10, offline);
					color = 0xFF8080FF;
					if (!blink) {
						color = 0xFFFF0000;
					}
					int ex = r.x + (r.width - w) / 2;
					if (textBackgrounds) {
						g2.setComposite(a1);
						g2.setColor(Color.BLACK);
						g2.fillRect(ex - 2, ey - 2, w + 4, 15);
						g2.setComposite(compositeSave);
					}

					commons.text().paintTo(g2, ex + 1, ey + 1, 10, color, offline);
					commons.text().paintTo(g2, ex, ey, 10, 0xD4FC84, offline);

					if (b.repairing) {
						g2.drawImage(commons.colony().repair[(animation % 3)], ex + w + 3, ey, null);
					}

				}
			}
		}
	}
	/**
	 * Draw battle helper objects such as paths and attack targets.
	 * @param g2 the graphics context
	 * @param x0 the base x offset
	 * @param y0 the base y offset
	 */
	void drawBattleHelpers(Graphics2D g2, int x0, int y0) {
		PlanetGround ground = ground();
		// unit selection boxes}
		BufferedImage selBox = commons.colony().selectionBoxLight;
		for (GroundwarUnit u : ground.units) {
			if (showCommand) {
				g2.setColor(u.attackMove != null ? Color.RED : Color.WHITE);
				for (int i = 0; i < u.path.size() - 1; i++) {
					Location l0 = u.path.get(i);
					Location l1 = u.path.get(i + 1);

					int xa = x0 + Tile.toScreenX(l0.x, l0.y) + 27;
					int ya = y0 + Tile.toScreenY(l0.x, l0.y) + 14;
					int xb = x0 + Tile.toScreenX(l1.x, l1.y) + 27;
					int yb = y0 + Tile.toScreenY(l1.x, l1.y) + 14;

					g2.drawLine(xa, ya, xb, yb);
				}
				if (u.attackBuilding != null) {
					Point gp = centerOf(u.attackBuilding);
					Point up = centerOf(u);
					g2.setColor(Color.RED);
					g2.drawLine(gp.x, gp.y, up.x, up.y);

				} else
					if (u.attackUnit != null) {
						Point gp = centerOf(u.attackUnit);
						Point up = centerOf(u);
						g2.setColor(Color.RED);
						g2.drawLine(gp.x, gp.y, up.x, up.y);
					}
			}
			if (ground.isSelected(u)) {
				Point p = unitPosition(u);
				g2.drawImage(selBox, p.x, p.y, null);
			}
		}
		for (GroundwarGun g : ground.selectedGuns) {
			Rectangle r = gunRectangle(g);
			g2.drawImage(selBox, r.x + (r.width - selBox.getWidth()) / 2, r.y + (r.height - selBox.getHeight()) / 2, null);
		}
	}
	/**
	 * Render the weather effects.
	 * @param g2 the graphics context
	 * @param surface the current surface
	 */
	void drawWeather(Graphics2D g2, PlanetSurface surface) {
		Planet planet = planet();
		if (planet.weatherTTL > 0 && planet.type.weatherDrop != null) {
			weatherOverlay.updateBounds(surface.boundingRectangle.width, surface.boundingRectangle.height);
			weatherOverlay.type = planet.type.weatherDrop;
			weatherOverlay.alpha = alpha + 0.3;
			weatherOverlay.draw(g2);
		}
	}
	/**
	 * Draw surface tiles and battle units.
	 * @param g2 the graphics context
	 * @param surface the surface object
	 * @param x0 the base x offset
	 * @param y0 the base y offset
	 */
	void drawTiles(Graphics2D g2, PlanetSurface surface, int x0, int y0) {
		Player player = player();
		Planet planet = planet();
		
		Rectangle br = surface.boundingRectangle;
		g2.setColor(Color.YELLOW);
		g2.drawRect(br.x, br.y, br.width, br.height);

		Rectangle renderingWindow = new Rectangle(0, 0, width, height);
		for (int i = 0; i < surface.renderingOrigins.size(); i++) {
			Location loc = surface.renderingOrigins.get(i);
			for (int j = 0; j < surface.renderingLength.get(i) + 2; j++) {
				int x = x0 + Tile.toScreenX(loc.x - j, loc.y);
				Location loc1 = Location.of(loc.x - j, loc.y);
				SurfaceEntity se = surface.buildingmap.get(loc1);
				if (se == null || player.knowledge(planet, PlanetKnowledge.OWNER) < 0) {
					se = surface.basemap.get(loc1);
				}
				if (se != null) {
					getImage(se, false, loc1, cell);
					int yref = y0 + Tile.toScreenY(cell.a, cell.b) + cell.yCompensation;
					if (renderingWindow.intersects(x * scale + offsetX, yref * scale + offsetY, 57 * scale, se.tile.imageHeight * scale)) {
						if (cell.image != null) {
							g2.drawImage(cell.image, x, yref, null);
						}
					}
					// add smoke
					if (se.building != null) {
						drawBuildingSmokeFire(g2, x0, y0, loc1, se);
					}
					// place guns on buildings or roads
					if ((se.building != null 
							&& "Defensive".equals(se.building.type.kind))
							|| se.type == SurfaceEntityType.ROAD) {
						drawGuns(g2, loc.x - j, loc.y);
					}
				}
				drawMine(g2, loc.x - j, loc.y);
				drawUnits(g2, loc.x - j, loc.y);
				drawExplosions(g2, loc.x - j, loc.y);
				drawRockets(g2, loc.x - j, loc.y);
			}
		}
	}
	/**
	 * Draws the smoke and fire on damaged buildings.
	 * @param g2 the graphics context
	 * @param x0 the render origin
	 * @param y0 the render origin
	 * @param loc1 the cell location
	 * @param se the surface entity
	 */
	void drawBuildingSmokeFire(Graphics2D g2, int x0, int y0,
			Location loc1, SurfaceEntity se) {
		int dr = se.building.hitpoints * 100 / se.building.type.hitpoints;
		if (dr < 100 && se.building.isComplete()) {
			if (se.virtualColumn == 0 && se.virtualRow + 1 == se.building.height()) {
				int len = se.building.width() * se.building.height();
				int nsmoke;
				if (dr < 50) {
					nsmoke = (50 - dr) / 5 + 1;
				} else {
					nsmoke = (100 - dr) / 10 + 1;
				}

				double sep = 1.0 * len  / (nsmoke + 1);
				int cnt = 0;
				for (double sj = sep; sj < len; sj += sep) {
					int si = (int)Math.round(sj);
					if (si < len) {
						Point zz = deZigZag(si, se.building.width(), se.building.height());

						int mix = Math.abs(loc1.x + zz.x) + Math.abs(loc1.y + zz.y) + animation;
						BufferedImage[] animFrames;
						if (dr < 50) {
							if ((cnt % 3) == (mix / 320 % 3)) {
								animFrames = commons.colony().buildingSmoke;
							} else {
								animFrames = commons.colony().buildingFire;
							}
						} else {
							animFrames = commons.colony().buildingSmoke;
						}

						BufferedImage smokeFire = animFrames[mix % animFrames.length];

						int smx = x0 + Tile.toScreenX(loc1.x + se.virtualColumn + zz.x, loc1.y + se.virtualRow - zz.y);
						int smy = y0 + Tile.toScreenY(loc1.x + se.virtualColumn + zz.x, loc1.y + se.virtualRow - zz.y);
						int dx = 27 - smokeFire.getWidth() / 2;
						g2.drawImage(smokeFire, smx + dx, smy - 14, null);
					}
					cnt++;
				}
			}
		}
	}
	/**
	 * Render any mine in the specified location.
	 * @param g2 the graphics context
	 * @param cx the cell coordinete
	 * @param cy the cell coordinate
	 */
	void drawMine(Graphics2D g2, int cx, int cy) {
		PlanetSurface surface = surface();
		PlanetGround ground = ground();
		Mine m = ground.mines.get(Location.of(cx, cy));
		if (m != null) {
			int x0 = surface.baseXOffset;
			int y0 = surface.baseYOffset;
			int px = (x0 + Tile.toScreenX(cx, cy));
			int py = (y0 + Tile.toScreenY(cx, cy));
			BufferedImage bimg = commons.colony().mine[0][0];
			g2.drawImage(bimg, px + 21, py + 12, null);
		}
	}
	/**
	 * Render the problem icons.
	 * @param g2 the graphics
	 * @param ps the statistics
	 */
	void renderProblems(Graphics2D g2, PlanetStatistics ps) {
		Set<PlanetProblems> combined = new HashSet<>();
		combined.addAll(ps.problems);
		combined.addAll(ps.warnings);

		if (combined.size() > 0) {
			int w = combined.size() * 11 - 1;
			g2.setColor(Color.BLACK);
			g2.fillRect((width - w) / 2 - 2, 18, w + 4, 15);
			int i = 0;
			for (PlanetProblems pp : combined) {
				BufferedImage icon = null;
				BufferedImage iconDark = null;
				switch (pp) {
				case HOUSING:
					icon = commons.common().houseIcon;
					iconDark = commons.common().houseIconDark;
					break;
				case FOOD:
					icon = commons.common().foodIcon;
					iconDark = commons.common().foodIconDark;
					break;
				case HOSPITAL:
					icon = commons.common().hospitalIcon;
					iconDark = commons.common().hospitalIconDark;
					break;
				case ENERGY:
					icon = commons.common().energyIcon;
					iconDark = commons.common().energyIconDark;
					break;
				case WORKFORCE:
					icon = commons.common().workerIcon;
					iconDark = commons.common().workerIconDark;
					break;
				case STADIUM:
					icon = commons.common().stadiumIcon;
					iconDark = commons.common().stadiumIconDark;
					break;
				case VIRUS:
					icon = commons.common().virusIcon;
					iconDark = commons.common().virusIconDark;
					break;
				case REPAIR:
					icon = commons.common().repairIcon;
					iconDark = commons.common().repairIconDark;
					break;
				case COLONY_HUB:
					icon = commons.common().colonyHubIcon;
					iconDark = commons.common().colonyHubIconDark;
					break;
				case POLICE:
					icon = commons.common().policeIcon;
					iconDark = commons.common().policeIconDark;
					break;
				case FIRE_BRIGADE:
					icon = commons.common().fireBrigadeIcon;
					iconDark = commons.common().fireBrigadeIcon;
					break;
				default:
				}
				if (ps.hasProblem(pp)) {
					g2.drawImage(icon, (width - w) / 2 + i * 11, 20, null);
				} else
					if (ps.hasWarning(pp)) {
						g2.drawImage(iconDark, (width - w) / 2 + i * 11, 20, null);
					}
				i++;
			}
		}

	}
	/**
	 * Retrive the unit at the given location.
	 * @param mx the mouse X
	 * @param my the mouse Y
	 * @return the unit or null if empty
	 */
	public GroundwarUnit unitAt(int mx, int my) {
		PlanetGround ground = ground();
		Location lm = getLocationAt(mx, my);
		return ground.unitAt(lm.x, lm.y);
	}
	/**
	 * Retrieve the building at the given mouse location.
	 * @param mx the x coodinate
	 * @param my the y coordinate
	 * @return the building or null
	 */
	Building buildingAt(int mx, int my) {
		PlanetSurface surface = surface();
		return surface.getBuildingAt(getLocationAt(mx, my));
	}
	/**
	 * Returns the common owner of the currently selected unit(s).
	 * @return the player or null if no units are selected
	 */
	Player selectionOwner() {
		return war().selectionOwner();
	}
	/**
	 * Return the image (strip) representing this surface entry.
	 * The default behavior returns the tile strips along its lower 'V' arc.
	 * This method to be overridden to handle the case of damaged or in-progress buildings
	 * @param se the surface entity
	 * @param symbolic display a symbolic tile instead of the actual building image.
	 * @param loc1 the the target cell location
	 * @param cell the output for the image and the Y coordinate compensation
	 */
	public void getImage(SurfaceEntity se, boolean symbolic, Location loc1, 
			SurfaceCell cell) {
		Player player = player();
		Planet planet = planet();
		if (se.type != SurfaceEntityType.BUILDING) {
			cell.yCompensation = 27 - se.tile.imageHeight;
			cell.a = loc1.x - se.virtualColumn;
			cell.b = loc1.y + se.virtualRow - se.tile.height + 1;
			if (se.virtualColumn == 0 && se.virtualRow < se.tile.height) {
				se.tile.alpha = alpha;
				cell.image = se.tile.getStrip(se.virtualRow);
				return;
			} else
			if (se.virtualRow == se.tile.height - 1) {
				se.tile.alpha = alpha;
				cell.image = se.tile.getStrip(se.tile.height - 1 + se.virtualColumn);
				return;
			}
			cell.image = null;
			return;
		}
		if (symbolic) {
			Tile tile;
			if (player.knowledge(planet, PlanetKnowledge.BUILDING) < 0) {
				tile = se.building.type.minimapTiles.destroyed;
			} else
			if (se.building.isConstructing()) {
				if (se.building.isSeverlyDamaged()) {
					tile = se.building.type.minimapTiles.constructingDamaged;
				} else {
					tile = se.building.type.minimapTiles.constructing;
				}
			} else {
				if (se.building.isDestroyed()) {
					tile = se.building.type.minimapTiles.destroyed;
				} else
				if (se.building.isDamaged()) {
					tile = se.building.type.minimapTiles.damaged;
				} else
				if (se.building.isOperational()) {
					tile = se.building.type.minimapTiles.normal;
				} else {
					tile = se.building.type.minimapTiles.inoperable;
				}
			}
			
			cell.yCompensation = 27 - tile.imageHeight;
			cell.image = tile.getStrip(0);
			cell.a = loc1.x;
			cell.b = loc1.y;
			
			return;
		} else
		if (player.knowledge(planet, PlanetKnowledge.BUILDING) < 0 
				&& (planet.war == null || !planet.war.hasUnits(player))) {
			Tile tile =  se.building.scaffolding.normal.get(0);
			cell.yCompensation = 27 - tile.imageHeight;
			tile.alpha = alpha;
			cell.image = tile.getStrip(0);
			cell.a = loc1.x;
			cell.b = loc1.y;
			return;
		}

		if (se.building.isConstructing()) {
			Tile tile;
			List<Tile> scaffolding;
			if (se.building.isSeverlyDamaged()) {
				scaffolding = se.building.scaffolding.damaged;
			} else {
				scaffolding = se.building.scaffolding.normal;
			}
			
			int index0 = (int)(se.building.hitpoints * 1L * scaffolding.size() / se.building.type.hitpoints);
			int area = se.building.width() * se.building.height();
			
			int index1 = (se.building.width() * se.virtualRow + se.virtualColumn) * scaffolding.size() / area;
			
			if (se.building.hitpoints * 100 < se.building.type.hitpoints) {
				index0 = 0;
				index1 = 0;
			}
			
			tile = scaffolding.get((index0 + index1) % scaffolding.size());
			
			cell.yCompensation = 27 - tile.imageHeight;
			tile.alpha = alpha;
			cell.image = tile.getStrip(0);
			cell.a = loc1.x;
			cell.b = loc1.y;
		} else {
			Tile tile;
			if (se.building.isSeverlyDamaged()) {
				tile = se.building.tileset.damaged;
			} else 
			if (se.building.isOperational()) {
				tile = se.building.tileset.normal;
			} else {
				tile = se.building.tileset.nolight;
			}
			cell.yCompensation = 27 - tile.imageHeight;
			cell.a = loc1.x - se.virtualColumn;
			cell.b = loc1.y + se.virtualRow - se.tile.height + 1;
			if (se.virtualColumn == 0 && se.virtualRow < se.tile.height) {
				tile.alpha = alpha;
				cell.image = tile.getStrip(se.virtualRow);
				return;
			} else
			if (se.virtualRow == se.tile.height - 1) {
				tile.alpha = alpha;
				cell.image = tile.getStrip(se.tile.height - 1 + se.virtualColumn);
				return;
			}
			cell.image = null;
		}
			
	}
	/**
	 * Zoom to 100%.
	 */
	public void zoomNormal() {
		zoom(1.0 - scale);
		askRepaint();
	}
	/**
	 * Zoom in by increasing the scale by 0.1.
	 */
	public void zoomIn() {
		zoom(0.1);
		askRepaint();
	}
	/**
	 * Zoom out by decreasing the scale by 0.1.
	 */
	public void zoomOut() {
		zoom(-0.1);
		askRepaint();
	}
	/**
	 * Perform a central zoom with a zoom delta.
	 * @param scaleDelta the delta
	 */
	public void zoom(double scaleDelta) {
		double pre = scale;
		
		int ex = width / 2;
		int ey = height / 2;
		
		double mx = (ex - offsetX) * pre;
		double my = (ey - offsetY) * pre;
		
		scale = Math.max(0.1, Math.min(2, scale + scaleDelta));
		
		double mx0 = (ex - offsetX) * scale;
		double my0 = (ey - offsetY) * scale;
		double dx = (mx - mx0) / pre;
		double dy = (my - my0) / pre;
		offsetX += (int)(dx);
		offsetY += (int)(dy);
	}
	/**
	 * Compute the alpha level for the current time of day.
	 */
	protected void computeAlpha() {
		int time = commons.world().time.get(GregorianCalendar.HOUR_OF_DAY) * 6
		+ commons.world().time.get(GregorianCalendar.MINUTE) / 10;
		
		if (time < 6 * 4 || time >= 6 * 22) {
			alpha = Tile.MIN_ALPHA;
		} else
		if (time >= 6 * 4 && time < 6 * 10) {
			alpha = (Tile.MIN_ALPHA + (1f - Tile.MIN_ALPHA) * (time - 6 * 4) / 36);
		} else
		if (time >= 6 * 10 && time < 6 * 16) {
			alpha = (1.0f);
		} else 
		if (time >= 6 * 16 && time < 6 * 22) {
			alpha = (1f - (1f - Tile.MIN_ALPHA) * (time - 6 * 16) / 36);
		}
		int tcs = config.tileCacheSize > 0 ? config.tileCacheSize : 16;
		if (tcs > 0 && (alpha > Tile.MIN_ALPHA && alpha < 1f)) {
			float step = (1f - Tile.MIN_ALPHA) / tcs;
			float a2 = (alpha - Tile.MIN_ALPHA);
			int n = (int)(a2 / step);
			alpha = step * n + Tile.MIN_ALPHA;
		}
		if (planet().weatherTTL > 0) {
			alpha = Math.max(Tile.MIN_ALPHA, alpha - 0.3f);
		}
	}
	/**
	 * @return Computes the selection rectangle.
	 */
	public Rectangle selectionRectangle() {
		int x0 = Math.min(selectionStart.x, selectionEnd.x);
		int y0 = Math.min(selectionStart.y, selectionEnd.y);
		int x1 = Math.max(selectionStart.x, selectionEnd.x);
		int y1 = Math.max(selectionStart.y, selectionEnd.y);
		
		return new Rectangle(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
	}
	/**
	 * Draw units into the cell.
	 * @param g2 the graphics context
	 * @param cx the cell coordinates
	 * @param cy the cell coordinates
	 */
	void drawUnits(Graphics2D g2, int cx, int cy) {
		Player player = player();
		PlanetGround ground = ground();
		Set<GroundwarUnit> unitsAt = ground.unitsAtLocation.get(Location.of(cx, cy));
		if (unitsAt == null) {
			return;
		}
		List<GroundwarUnit> multiple = new ArrayList<>(unitsAt);
		// order by y first, x later
		Collections.sort(multiple, new Comparator<GroundwarUnit>() {
			@Override
			public int compare(GroundwarUnit o1, GroundwarUnit o2) {
				if (o1.y > o2.y) {
					return -1; 
				} else
				if (o1.y < o2.y) {
					return 1;
				} else
				if (o1.x > o2.x) {
					return -1;
				} else
				if (o1.x < o2.x) {
					return 1;
				}
				return 0;
			}
		});
		
		for (GroundwarUnit u : multiple) {
			Point p = unitPosition(u);
			BufferedImage img = u.get();
			
			// compensate for trimmed image
			int tx = (u.model.width - img.getWidth()) / 2;
			int ty = (u.model.height - img.getHeight()) / 2;
			
			g2.drawImage(img, p.x + tx, p.y + ty, null);
			
			if (u.paralizedTTL > 0) {
				// draw green paralization effect
				BufferedImage[] expl = world().battle.groundExplosions.get(ExplosionType.GROUND_GREEN);
				int idx = u.paralizedTTL % expl.length;

				BufferedImage icon = expl[idx];
				int dx = p.x + tx + (img.getWidth() - icon.getWidth()) / 2;
				int dy = p.y + ty + (img.getHeight() - icon.getHeight()) / 2;
				g2.drawImage(icon, dx, dy, null);
			}
			// paint health bar
			g2.setColor(Color.BLACK);
			g2.fillRect(p.x + 4, p.y + 3, u.model.width - 7, 5);
			if (u.owner == player) {
				g2.setColor(new Color(0x458AAA));
			} else {
				g2.setColor(new Color(0xAE6951));
			}
			g2.fillRect(p.x + 5, p.y + 4, (int)(u.hp * (u.model.width - 9) / u.model.hp), 3);
			
			BufferedImage smokeFire = null;
			if (u.hp * 3 <= u.model.hp) {
				smokeFire = commons.colony().buildingFire[animation % commons.colony().buildingFire.length];
			} else
			if (u.hp * 3 <= u.model.hp * 2) {
				smokeFire = commons.colony().buildingSmoke[animation % commons.colony().buildingSmoke.length];
			}
			if (smokeFire != null) {
				int dx = p.x + tx + (img.getWidth() - smokeFire.getWidth()) / 2;
				int dy = p.y + ty + (img.getHeight() / 2 - smokeFire.getHeight());
				g2.drawImage(smokeFire, dx, dy, null);
			}
		}
	}
	/**
	 * Draw rockets into the cell.
	 * @param g2 the graphics context
	 * @param cx the cell coordinates
	 * @param cy the cell coordinates
	 */
	void drawRockets(Graphics2D g2, int cx, int cy) {
		PlanetGround ground = ground();
		List<GroundwarRocket> multiple = new ArrayList<>();
		for (GroundwarRocket u : ground.rockets) {
			if ((int)Math.floor(u.x - 1) == cx && (int)Math.floor(u.y - 1) == cy) {
				multiple.add(u);
			}
		}
		// order by y first, x later
		Collections.sort(multiple, new Comparator<GroundwarRocket>() {
			@Override
			public int compare(GroundwarRocket o1, GroundwarRocket o2) {
				if (o1.y > o2.y) {
					return -1; 
				} else
				if (o1.y < o2.y) {
					return 1;
				} else
				if (o1.x > o2.x) {
					return -1;
				} else
				if (o1.x < o2.x) {
					return 1;
				}
				return 0;
			}
		});
		
		for (GroundwarRocket u : multiple) {
			Point p = unitPosition(u);
			BufferedImage img = u.get();
			g2.drawImage(img, p.x, p.y, null);
//			g2.drawRect(p.x, p.y, img.getWidth(), img.getHeight());
		}
	}
	/**
	 * Computes the unit bounding rectangle's left-top position.
	 * @param u the unit
	 * @return the position
	 */
	public Point unitPosition(GroundwarRocket u) {
		PlanetSurface surface = surface();
		return new Point(
				(int)(surface.baseXOffset + Tile.toScreenX(u.x + 0.5, u.y)), 
				(int)(surface.baseYOffset + Tile.toScreenY(u.x + 0.5, u.y)));
	}
	/**
	 * The on-screen rectangle of the ground unit.
	 * @param u the unit to test
	 * @return the rectangle
	 */
	public Rectangle unitRectangle(GroundwarUnit u) {
		PlanetSurface surface = surface();
		Rectangle r = u.rectangle();
		r.x += surface.baseXOffset;
		r.y += surface.baseYOffset;
		return r;
	}
	/**
	 * Computes the unit bounding rectangle's left-top position.
	 * @param u the unit
	 * @return the position
	 */
	Point unitPosition(GroundwarUnit u) {
		PlanetSurface surface = surface();
		Point p = u.position();
		p.x += surface.baseXOffset;
		p.y += surface.baseYOffset;
		return p;
	}
	/** 
	 * Draw guns at the specified location.
	 * @param g2 the graphics context
	 * @param cx the cell X coordinate
	 * @param cy the cell Y coordinate 
	 */
	void drawGuns(Graphics2D g2, int cx, int cy) {
		PlanetGround ground = ground();
		int x0 = planet().surface.baseXOffset;
		int y0 = planet().surface.baseYOffset;
		for (GroundwarGun u : ground.guns) {
			if (u.rx - 1 == cx && u.ry - 1 == cy) {
				int px = (x0 + Tile.toScreenX(u.rx, u.ry));
				int py = (y0 + Tile.toScreenY(u.rx, u.ry));
				BufferedImage img = u.get();
				
				int ux = px + (54 - 90) / 2 + u.model.px;
				int uy = py + (28 - 55) / 2 + u.model.py;
				
				
				g2.drawImage(img, ux, uy, null);
				
				if (u.attack != null && showCommand) {
					Point gp = centerOf(u.attack);
					Point up = centerOf(u);
					g2.setColor(Color.RED);
					g2.drawLine(gp.x, gp.y, up.x, up.y);
				}
			}
		}

	}
	/**
	 * Draw explosions for the given cell.
	 * @param g2 the graphics context
	 * @param cx the cell coordinate
	 * @param cy the cell coordinate
	 */
	void drawExplosions(Graphics2D g2, int cx, int cy) {
		PlanetSurface surface = surface();
		PlanetGround ground = ground();
		int x0 = surface.baseXOffset;
		int y0 = surface.baseYOffset;
		int px = (x0 + Tile.toScreenX(cx + 1, cy + 1));
		int py = (y0 + Tile.toScreenY(cx + 1, cy + 1));
		for (GroundwarExplosion exp : ground.explosions) {
			
			// FIXME might need additional offsets
			int ex = (int)(x0 + Tile.toScreenX(exp.x, exp.y));
			int ey = (int)(y0 + Tile.toScreenY(exp.x, exp.y));

			if (U.within(px, py, 54, 28, ex, ey)) {
				BufferedImage img = exp.get();
				g2.drawImage(img, ex - img.getWidth() / 2, ey - img.getHeight() / 2, null);
			}
		}
	}
	/**
	 * Compute the collision image.
	 * @param u the ground unit object
	 * @param ur the ground unit bounding rectangle
	 * @param t the tile
	 * @param tr the tile bounding rectangle
	 * @param is the intersection rectangle
	 * @return the collision image
	 */
	BufferedImage collisionImage(GroundwarUnit u, Rectangle ur, Tile t, Rectangle tr, Rectangle is) {
		BufferedImage bi = u.get();
		BufferedImage result = new BufferedImage(is.width, is.height, BufferedImage.TYPE_INT_ARGB);
		boolean wasCollision = false;
		
		for (int y = is.y; y < is.y + is.height; y++) {
			for (int x = is.x; x < is.x + is.width; x++) {
				int urgb = bi.getRGB(x - ur.x, y - ur.y);
				if ((urgb & 0xFF000000) != 0) {
					int trgb = t.image[(y - tr.y) * t.imageWidth + (x - tr.x)];
					if ((trgb & 0xFF000000) != 0) {
						result.setRGB(x - is.x, y - is.y, 0xFFFF0000);
						wasCollision = true;
					}
				}
			}
			
		}
		return wasCollision ? result : null;
	}
	/**
	 * Returns the unscaled bounding rectangle of the gun in reference to the surface map.
	 * @param g the gun object
	 * @return the bounding rectangle
	 */
	public Rectangle gunRectangle(GroundwarGun g) {
		Rectangle r = g.rectangle();
		PlanetSurface surface = surface();
		r.x += surface.baseXOffset;
		r.y += surface.baseYOffset;
		return r;
	}
	/**
	 * Gives the center point of the gun rectangle in the unscaled pixel space.
	 * @param g the gun in question
	 * @return the center point
	 */
	public Point centerOf(GroundwarGun g) {
		Point p = g.center();
		PlanetSurface surface = surface();
		p.x += surface.baseXOffset;
		p.y += surface.baseYOffset;
		return p;
	}
	/**
	 * Gives the center point of the unit rectangle in the unscaled pixel space.
	 * @param g the unit in question
	 * @return the center point
	 */
	public Point centerOf(GroundwarUnit g) {
		Point p = g.center();
		PlanetSurface surface = surface();
		p.x += surface.baseXOffset;
		p.y += surface.baseYOffset;
		return p;
	}
	/**
	 * Gives the center point of the unit rectangle in the unscaled pixel space.
	 * @param b the building object
	 * @return the center point
	 */
	Point centerOf(Building b) {
		Point p = b.center();
		PlanetSurface surface = surface();
		p.x += surface.baseXOffset;
		p.y += surface.baseYOffset;
		return p;
	}
	/**
	 * Sacle and position the given rectangle according to the current offset and scale.
	 * @param r the target rectangle
	 */
	public void scaleToScreen(Rectangle r) {
		r.x = (int)(r.x * scale + offsetX);
		r.y = (int)(r.y * scale + offsetY);
		r.width *= scale;
		r.height *= scale;
	}
}
