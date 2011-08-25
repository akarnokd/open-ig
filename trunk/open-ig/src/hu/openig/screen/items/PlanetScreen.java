/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;


import hu.openig.core.Act;
import hu.openig.core.Location;
import hu.openig.core.Tile;
import hu.openig.mechanics.Allocator;
import hu.openig.model.AutoBuild;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetProblems;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.PlanetSurface;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.model.SurfaceEntity;
import hu.openig.model.SurfaceEntityType;
import hu.openig.model.SurfaceFeature;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageFill;
import hu.openig.ui.UIImageTabButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.ui.UIMouse.Type;
import hu.openig.ui.VerticalAlignment;
import hu.openig.utils.ImageUtils;
import hu.openig.utils.Parallels;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * The planet surface rendering screen.
 * @author akarnokd, 2010.01.11.
 */
public class PlanetScreen extends ScreenBase {
	/** Indicate if a component is drag sensitive. */
	@Retention(RetentionPolicy.RUNTIME)
	@interface DragSensitive { }
	/** 
	 * The selected rectangular region. The X coordinate is the smallest, the Y coordinate is the largest
	 * the width points to +X and height points to -Y direction
	 */
	Rectangle selectedRectangle;
	/** The selection tile. */
	Tile selection;
	/** The placement tile for allowed area. */
	Tile areaAccept;
	/** The empty tile indicator. */
	Tile areaEmpty;
	/** The placement tile for denied area. */
	Tile areaDeny;
	/** The current cell tile. */
	Tile areaCurrent;
	/** Used to place buildings on the surface. */
	final Rectangle placementRectangle = new Rectangle();
	/** The building bounding box. */
	Rectangle buildingBox;
	/** Are we in placement mode? */
	boolean placementMode;
	/** The simple blinking state. */
	boolean blink;
	/** The animation index. */
	int animation;
	/** The animation timer. */
	Closeable animationTimer;
	/** The animation timer. */
	Closeable earthQuakeTimer;
	/** Enable the drawing of black boxes behind building names and percentages. */
	boolean textBackgrounds = true;
	/** Render placement hints on the surface. */
	boolean placementHints;
	/** The surface cell image. */
	static class SurfaceCell {
		/** The tile target. */
		public int a;
		/** The tile target. */
		public int b;
		/** The image to render. */
		public BufferedImage image;
		/** The Y coordinate compensation. */
		public int yCompensation;
	}
	/** The surface cell helper. */
	final SurfaceCell cell = new SurfaceCell();
	/** The last mouse coordinate. */
	int lastX;
	/** The last mouse coordinate. */
	int lastY;
	/** Is the map dragged. */
	boolean drag;
	/** Is a selection box dragged. */
	boolean sel;
	/** The originating location. */
	Location orig;
	/** The base rectangle. */
	final Rectangle base = new Rectangle();
	/** The planet view window. */
	final Rectangle window = new Rectangle();
	/** The buildings sidebar button. */
	@DragSensitive
	UIImageButton sidebarBuildings;
	/** The radar sidebar button. */
	@DragSensitive
	UIImageButton sidebarRadar;
	/** The building info sidebar button. */
	@DragSensitive
	UIImageButton sidebarBuildingInfo;
	/** The colony info sidebar button. */
	@DragSensitive
	UIImageButton sidebarColonyInfo;
	/** The empty image for the buildings. */
	@DragSensitive
	UIImage sidebarBuildingsEmpty;
	/** The empty image for the radar. */
	@DragSensitive
	UIImage sidebarRadarEmpty;
	/** Button to show/hide navigation buttons. */
	@DragSensitive
	UIImageButton sidebarNavigation;
	/** The colony info navigation button. */
	@DragSensitive
	UIImageButton colonyInfo;
	/** The bridge button. */
	@DragSensitive
	UIImageButton bridge;
	/** The planets button navigation. */
	@DragSensitive
	UIImageButton planets;
	/** The starmap button navigation. */
	@DragSensitive
	UIImageButton starmap;
	/** The surface renderer. */
	SurfaceRenderer render;
	/** The left filler region. */
	@DragSensitive
	UIImageFill leftFill;
	/** The right filler region. */
	@DragSensitive
	UIImageFill rightFill;
	/** The buildings panel. */
	@DragSensitive
	BuildingsPanel buildingsPanel;
	/** The radar panel. */
	@DragSensitive
	UIImage radarPanel;
	/** The buildingInfoPanel. */
	@DragSensitive
	BuildingInfoPanel buildingInfoPanel;
	/** The drawable radar rectangle. */
	@DragSensitive
	RadarRender radar;
	/** The last surface. */
	PlanetSurface lastSurface;
	/** The lighting level. */
	float alpha = 1.0f;
	/** The currently selected building. */
	Building currentBuilding;
	/** The information panel. */
	InfoPanel infoPanel;
	/** The upgrade panel. */
	@DragSensitive
	UpgradePanel upgradePanel;
	/** Go to next planet. */
	@DragSensitive
	UIImageButton prev;
	/** Go to previous planet. */
	@DragSensitive
	UIImageButton next;
	/** Show the building list panel? */
	boolean showBuildingList = true;
	/** Show the building info panel? */
	boolean showBuildingInfo = true;
	/** Show the information panel. */
	boolean showInfo = true;
	@Override
	public void onFinish() {
		onEndGame();
	}
	
	@Override
	public boolean keyboard(KeyEvent e) {
		boolean rep = false;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			render.offsetY += 28;
			rep = true;
			break;
		case KeyEvent.VK_DOWN:
			render.offsetY -= 28;
			rep = true;
			break;
		case KeyEvent.VK_LEFT:
			render.offsetX += 54;
			rep = true;
			break;
		case KeyEvent.VK_RIGHT:
			render.offsetX -= 54;
			rep = true;
			break;
		case KeyEvent.VK_ESCAPE:
			if (placementMode) {
				placementMode = false;
				buildingsPanel.build.down = false;
				e.consume();
				rep = true;
			}
			break;
		default:
		}
		return rep;
	}

	/**
	 * Zoom to 100%.
	 */
	protected void doZoomNormal() {
		render.scale = 1.0;
		askRepaint();
	}
	/**
	 * Zoom out by decreasing the scale by 0.1.
	 */
	protected void doZoomOut() {
		render.scale = Math.max(0.1, render.scale - 0.1);
		askRepaint();
	}
	/**
	 * Zoom in by increasing the scale by 0.1.
	 */
	protected void doZoomIn() {
		render.scale = Math.min(2.0, render.scale + 0.1);
		askRepaint();
	}

	@Override
	public void onEnter(Screens mode) {
		animationTimer = commons.register(500, new Act() {
			@Override
			public void act() {
				doAnimation();
			}
		});
		earthQuakeTimer = commons.register(100, new Act() {
			@Override
			public void act() {
				doEarthquake();
			}
		});
		if (surface() != null) {
			render.offsetX = -(int)((surface().boundingRectangle.width * render.scale - width) / 2);
			render.offsetY = -(int)((surface().boundingRectangle.height * render.scale - height) / 2);
		}
		focused = render;
	}

	@Override
	public void onLeave() {
		placementMode = false;
		buildingsPanel.build.down = false;

		close0(animationTimer);
		animationTimer = null;
		close0(earthQuakeTimer);
		earthQuakeTimer = null;
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
	public void getImage(SurfaceEntity se, boolean symbolic, Location loc1, SurfaceCell cell) {
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
			Tile tile = null;
			if (knowledge(planet(), PlanetKnowledge.BUILDING) < 0) {
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
		if (knowledge(planet(), PlanetKnowledge.BUILDING) < 0) {
			Tile tile =  se.building.scaffolding.normal.get(0);
			cell.yCompensation = 27 - tile.imageHeight;
			tile.alpha = alpha;
			cell.image = tile.getStrip(0);
			cell.a = loc1.x;
			cell.b = loc1.y;
			return;
		}

		if (se.building.isConstructing()) {
			Tile tile = null;
			if (se.building.isSeverlyDamaged()) {
				int constructIndex = se.building.buildProgress * se.building.scaffolding.damaged.size() / se.building.type.hitpoints;
				tile =  se.building.scaffolding.damaged.get(constructIndex);
			} else {
				int constructIndex = se.building.buildProgress * se.building.scaffolding.normal.size() / se.building.type.hitpoints;
				tile =  se.building.scaffolding.normal.get(constructIndex);
			}
			cell.yCompensation = 27 - tile.imageHeight;
			tile.alpha = alpha;
			cell.image = tile.getStrip(0);
			cell.a = loc1.x;
			cell.b = loc1.y;
		} else {
			Tile tile = null;
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
			return;
		}
			
	}
	/**
	 * Compute the bounding rectangle of the rendered building object.
	 * @param loc the location to look for a building.
	 * @return the bounding rectangle or null if the target does not contain a building
	 */
	public Rectangle getBoundingRect(Location loc) {
		SurfaceEntity se = surface().buildingmap.get(loc);
		if (se != null && se.type == SurfaceEntityType.BUILDING) {
			int a0 = loc.x - se.virtualColumn;
			int b0 = loc.y + se.virtualRow;
			
			int x = surface().baseXOffset + Tile.toScreenX(a0, b0);
			int y = surface().baseYOffset + Tile.toScreenY(a0, b0 - se.tile.height + 1) + 27;
			
			return new Rectangle(x, y - se.tile.imageHeight, se.tile.imageWidth, se.tile.imageHeight);
		}
		return null;
	}
	/**
	 * Return a building instance at the specified location.
	 * @param loc the location
	 * @return the building object or null
	 */
	public Building getBuildingAt(Location loc) {
		SurfaceEntity se = surface().buildingmap.get(loc);
		if (se != null && se.type == SurfaceEntityType.BUILDING) {
			return se.building;
		}
		return null;
	}
	/**
	 * The surface renderer component.
	 * @author karnok, Mar 27, 2011
	 */
	class SurfaceRenderer extends UIComponent {
		/** The current scaling factor. */
		double scale = 1;
		/** The offset X. */
		int offsetX;
		/** The offset Y. */
		int offsetY;
		/**
		 * Get a location based on the mouse coordinates.
		 * @param mx the mouse X coordinate
		 * @param my the mouse Y coordinate
		 * @return the location
		 */
		public Location getLocationAt(int mx, int my) {
			if (surface() != null) {
				double mx0 = mx - (surface().baseXOffset + 28) * scale - offsetX; // Half left
				double my0 = my - (surface().baseYOffset + 27) * scale - offsetY; // Half up
				int a = (int)Math.floor(Tile.toTileX((int)mx0, (int)my0) / scale);
				int b = (int)Math.floor(Tile.toTileY((int)mx0, (int)my0) / scale) ;
				return Location.of(a, b);
			}
			return null;
		}
		@Override
		public boolean mouse(UIMouse e) {
			boolean rep = false;
			switch (e.type) {
			case MOVE:
			case DRAG:
				if (drag || e.has(Button.RIGHT)) {
					if (!drag) {
						drag = true;
						lastX = e.x;
						lastY = e.y;
						doDragMode();
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
					// FIXME compensate for even sized tiles
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
				}
				break;
			case DOWN:
				if (e.has(Button.RIGHT)) {
					drag = true;
					lastX = e.x;
					lastY = e.y;
					doDragMode();
				} else
				if (e.has(Button.MIDDLE)) {
					render.offsetX = -(surface().boundingRectangle.width - width) / 2;
					render.offsetY = -(surface().boundingRectangle.height - height) / 2;
					scale = 1;
					rep = true;
				}
				if (e.has(Button.LEFT)) {
					if (placementMode) {
						placeBuilding(e.has(Modifier.SHIFT));	
					} else {
						if (knowledge(planet(), PlanetKnowledge.OWNER) >= 0 
								|| planet().owner == player()) {
							Location loc = getLocationAt(e.x, e.y);
							buildingBox = getBoundingRect(loc);
							doSelectBuilding(getBuildingAt(loc));
							if (currentBuilding != null) {
								sound(SoundType.CLICK_MEDIUM_2);
							}
							rep = true;
						} else {
							doSelectBuilding(null);
						}
					}
				}
				break;
			case UP:
				if (e.has(Button.RIGHT)) {
					drag = false;
					doDragMode();
				}
//				if (e.has(Button.LEFT)) {
//					sel = false;
//				}
				rep = true;
				break;
			case LEAVE:
				drag = false;
				doDragMode();
				break;
			case WHEEL:
				if (e.has(Modifier.CTRL)) {
					double pre = scale;
					double mx = (e.x - offsetX) * pre;
					double my = (e.y - offsetY) * pre;
					if (e.z < 0) {
						doZoomIn();
					} else {
						doZoomOut();
					}
					double mx0 = (e.x - offsetX) * scale;
					double my0 = (e.y - offsetY) * scale;
					double dx = (mx - mx0) / pre;
					double dy = (my - my0) / pre;
					offsetX += (int)(dx);
					offsetY += (int)(dy);
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
						offsetY += 28;
					} else {
						offsetY -= 28;
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
			PlanetSurface surface = surface();
			if (surface == null) {
				return;
			}

			if (lastSurface != surface) {
				buildingBox = null;
				currentBuilding = null;
				lastSurface = surface;
				placementMode = false;
				buildingsPanel.build.down = false;
				upgradePanel.hideUpgradeSelection();
			}
			buildingsPanel.visible(planet().owner == player() && showBuildingList);
			buildingInfoPanel.visible(planet().owner == player() && showBuildingInfo);
			infoPanel.visible(knowledge(planet(), PlanetKnowledge.NAME) >= 0 && showInfo);
			
			setBuildingList(0);
			buildingInfoPanel.update();
			PlanetStatistics ps = infoPanel.update();
			
			int time = world().time.get(GregorianCalendar.HOUR_OF_DAY) * 6
			+ world().time.get(GregorianCalendar.MINUTE) / 10;
			
			if (time < 6 * 4 || time >= 6 * 22) {
				alpha = (0.35f);
			} else
			if (time >= 6 * 4 && time < 6 * 10) {
				alpha = (0.35f + 0.65f * (time - 6 * 4) / 36);
			} else
			if (time >= 6 * 10 && time < 6 * 16) {
				alpha = (1.0f);
			} else 
			if (time >= 6 * 16 && time < 6 * 22) {
				alpha = (1f - 0.65f * (time - 6 * 16) / 36);
			}
			
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

			if (knowledge(planet(), PlanetKnowledge.NAME) >= 0) {
			
				Rectangle br = surface.boundingRectangle;
				g2.setColor(Color.YELLOW);
				g2.drawRect(br.x, br.y, br.width, br.height);
				
				
	//			long timestamp = System.nanoTime();
				BufferedImage empty = areaEmpty.getStrip(0);
				Rectangle renderingWindow = new Rectangle(0, 0, width, height);
				for (int i = 0; i < surface.renderingOrigins.size(); i++) {
					Location loc = surface.renderingOrigins.get(i);
					for (int j = 0; j < surface.renderingLength.get(i); j++) {
						int x = x0 + Tile.toScreenX(loc.x - j, loc.y);
						int y = y0 + Tile.toScreenY(loc.x - j, loc.y);
						Location loc1 = Location.of(loc.x - j, loc.y);
						SurfaceEntity se = surface.buildingmap.get(loc1);
						if (se == null || knowledge(planet(), PlanetKnowledge.OWNER) < 0) {
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
						} else {
							if (renderingWindow.intersects(x * scale + offsetX, y * scale + offsetY, 57 * scale, 27 * scale)) {
								g2.drawImage(empty, x, y, null);
							}
						}
					}
				}
	//			System.out.println("draw: " + (1E9 / (System.nanoTime() - timestamp)));
				if (placementHints) {
					for (Location loc : surface.basemap.keySet()) {
						if (!surface().canPlaceBuilding(loc.x, loc.y)) {
							int x = x0 + Tile.toScreenX(loc.x, loc.y);
							int y = y0 + Tile.toScreenY(loc.x, loc.y);
							g2.drawImage(areaDeny.getStrip(0), x, y, null);
						}
					}
				}
				if (!placementMode) {
					if (selectedRectangle != null) {
						for (int i = selectedRectangle.x; i < selectedRectangle.x + selectedRectangle.width; i++) {
							for (int j = selectedRectangle.y; j > selectedRectangle.y - selectedRectangle.height; j--) {
								int x = x0 + Tile.toScreenX(i, j);
								int y = y0 + Tile.toScreenY(i, j);
								g2.drawImage(selection.getStrip(0), x, y, null);
							}
						}
					}
				}
				if (placementMode) {
					if (placementRectangle.width > 0) {
						for (int i = placementRectangle.x; i < placementRectangle.x + placementRectangle.width; i++) {
							for (int j = placementRectangle.y; j > placementRectangle.y - placementRectangle.height; j--) {
								
								BufferedImage img = areaAccept.getStrip(0);
								// check for existing building
								if (!surface().canPlaceBuilding(i, j)) {
									img = areaDeny.getStrip(0);
								}
								
								int x = x0 + Tile.toScreenX(i, j);
								int y = y0 + Tile.toScreenY(i, j);
								g2.drawImage(img, x, y, null);
							}
						}
					}
				}
				if (knowledge(planet(), PlanetKnowledge.BUILDING) >= 0) {
					for (Building b : surface.buildings) {
						Rectangle r = getBoundingRect(b.location);
	//					if (r == null) {
	//						continue;
	//					}
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
						if (knowledge(planet(), PlanetKnowledge.BUILDING) >= 0) {
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
								String offline = get("buildings.offline");
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
							}
						}
					}
				}
				if (knowledge(planet(), PlanetKnowledge.OWNER) >= 0 && buildingBox != null) {
					g2.setColor(Color.RED);
					g2.drawRect(buildingBox.x, buildingBox.y, buildingBox.width, buildingBox.height);
				}
				
				g2.setTransform(at);
				g2.setColor(Color.BLACK);
				
				String pn = planet().name;
				int nameHeight = 14;
				int nameWidth = commons.text().getTextWidth(nameHeight, pn);
				int nameLeft = (width - nameWidth) / 2;
				g2.fillRect(nameLeft - 5, 0, nameWidth + 10, nameHeight + 4);
				
				int pc = TextRenderer.GRAY;
				if (knowledge(planet(), PlanetKnowledge.OWNER) >= 0 && planet().owner != null) {
					pc = planet().owner.color;
				}
				commons.text().paintTo(g2, nameLeft, 2, nameHeight, pc, pn);
	
				if (ps != null && planet().owner == player()) {
					renderProblems(g2, ps);
				}
			} else {
				g2.setTransform(at);
				
				g2.setColor(new Color(0, 0, 0, 128));
				
				String installSatellite = get("planet.install_satellite_1");
				int tw = commons.text().getTextWidth(14, installSatellite);
				int tx = (width - tw) / 2;
				int ty = (height - 14) / 2 - 12;
				g2.fillRect(tx - 5, ty - 5, tw + 10, 24);
				commons.text().paintTo(g2, tx, ty, 14, TextRenderer.WHITE, installSatellite);
				
				installSatellite = get("planet.install_satellite_2");
				tw = commons.text().getTextWidth(14, installSatellite);
				tx = (width - tw) / 2;
				ty = (height - 14) / 2 + 12;
				g2.fillRect(tx - 5, ty - 5, tw + 10, 24);
				commons.text().paintTo(g2, tx, ty, 14, TextRenderer.WHITE, installSatellite);
			}
			
			g2.setClip(save0);
			RenderTools.setInterpolation(g2, false);
			
			if (prev.visible() && next.visible()) {
				g2.setColor(Color.BLACK);
				g2.fillRect(prev.x - this.x - 1, prev.y - this.y - 1, prev.width + next.width + 4, prev.height + 2);
			}
			
		}
		/**
		 * Render the problem icons.
		 * @param g2 the graphics
		 * @param ps the statistics
		 */
		void renderProblems(Graphics2D g2, PlanetStatistics ps) {
			Set<PlanetProblems> combined = new HashSet<PlanetProblems>();
			combined.addAll(ps.problems.keySet());
			combined.addAll(ps.warnings.keySet());
			
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
		
	}
	/**
	 * Prepare the surface tiles in parallel.
	 * @param surface the target planet surface
	 */
	void prepareTilesAsync(PlanetSurface surface) {
		long time = System.nanoTime();
		List<Future<?>> futures = new LinkedList<Future<?>>();
		for (final SurfaceFeature sf : surface.features) {
			futures.add(commons.pool.submit(new Runnable() {
				@Override
				public void run() {
					sf.tile.getStrip(0);
				}
			}));
		}
		Parallels.waitForAll(futures);
		System.out.println("PrepareTilesAsync: " + (1E9 / (System.nanoTime() - time)));
	}

	/**
	 * The radar renderer component.
	 * @author akarnokd, Mar 27, 2011
	 */
	class RadarRender extends UIComponent {
		@Override
		public void draw(Graphics2D g2) {
			RenderTools.setInterpolation(g2, true);
			
			Shape save0 = g2.getClip();
			g2.clipRect(0, 0, width, height);
			
			PlanetSurface surface = surface();
			
			if (surface == null) {
				return;
			}
			Rectangle br = surface.boundingRectangle;

			AffineTransform at = g2.getTransform();
			
			float scalex = width * 1.0f / br.width;
			float scaley = height * 1.0f / br.height;
			float scale = Math.min(scalex, scaley);
			g2.translate(-(br.width * scale - width) / 2, -(br.height * scale - height) / 2);
			g2.scale(scale, scale);
			
			int x0 = surface.baseXOffset;
			int y0 = surface.baseYOffset;

			g2.setColor(new Color(96 * alpha / 255, 96 * alpha / 255, 96 * alpha / 255));
			g2.fillRect(br.x, br.y, br.width, br.height);
//			g2.setColor(Color.YELLOW);
//			g2.drawRect(br.x, br.y, br.width - 1, br.height - 1);
			
			if (knowledge(planet(), PlanetKnowledge.NAME) >= 0) {
				BufferedImage empty = areaEmpty.getStrip(0);
				Rectangle renderingWindow = new Rectangle(0, 0, width, height);
				for (int i = 0; i < surface.renderingOrigins.size(); i++) {
					Location loc = surface.renderingOrigins.get(i);
					for (int j = 0; j < surface.renderingLength.get(i); j++) {
						int x = x0 + Tile.toScreenX(loc.x - j, loc.y);
						int y = y0 + Tile.toScreenY(loc.x - j, loc.y);
						Location loc1 = Location.of(loc.x - j, loc.y);
						SurfaceEntity se = surface.buildingmap.get(loc1);
						if (se == null || knowledge(planet(), PlanetKnowledge.OWNER) < 0) {
							se = surface.basemap.get(loc1);
						}
						if (se != null) {
							getImage(se, true, loc1, cell);
							int yref = y0 + Tile.toScreenY(cell.a, cell.b) + cell.yCompensation;
							if (renderingWindow.intersects(x * scale, yref * scale, 57 * scale, se.tile.imageHeight * scale)) {
								if (cell.image != null) {
									g2.drawImage(cell.image, x, yref, null);
								}
							}
						} else {
							if (renderingWindow.intersects(x * scale, y * scale, 57 * scale, 27 * scale)) {
								g2.drawImage(empty, x, y, null);
							}
						}
					}
				}
				g2.setColor(Color.RED);
				if (knowledge(planet(), PlanetKnowledge.OWNER) >= 0) {
					if (buildingBox != null) {
						g2.drawRect(buildingBox.x, buildingBox.y, buildingBox.width, buildingBox.height);
					}
				}
				g2.setColor(Color.WHITE);
				g2.drawRect(
						(int)(-render.offsetX / render.scale), 
						(int)(-render.offsetY / render.scale), 
						(int)(render.width / render.scale - 1), 
						(int)(render.height / render.scale - 1));
			}
			g2.setTransform(at);
			
			g2.setClip(save0);
			RenderTools.setInterpolation(g2, false);
		}
		@Override
		public boolean mouse(UIMouse e) {
			switch (e.type) {
			case WHEEL:
				if (e.has(Modifier.CTRL)) {
					if (moveViewPort(e)) {
						double pre = render.scale;
						
						int ex = render.width / 2;
						int ey = render.height / 2;
						
						double mx = (ex - render.offsetX) * pre;
						double my = (ey - render.offsetY) * pre;
						if (e.z < 0) {
							doZoomIn();
						} else {
							doZoomOut();
						}
						double mx0 = (ex - render.offsetX) * render.scale;
						double my0 = (ey - render.offsetY) * render.scale;
						double dx = (mx - mx0) / pre;
						double dy = (my - my0) / pre;
						render.offsetX += (int)(dx);
						render.offsetY += (int)(dy);
					
					return true;
					}
				}
				return false;
			case DRAG:
			case DOWN:
				return moveViewPort(e);
			default:
				return super.mouse(e);
			}
		}
		/**
		 * Move the viewport based on the mouse event.
		 * @param e the mouse event
		 * @return true if the viewport was moved
		 */
		boolean moveViewPort(UIMouse e) {
			Rectangle br = surface().boundingRectangle;

			double scalex = width * 1.0 / br.width;
			double scaley = height * 1.0 / br.height;
			double scale = Math.min(scalex, scaley);
			
			double dx = -(br.width * scale - width) / 2;
			double dy = -(br.height * scale - height) / 2;
			double dw = br.width * scale;
			double dh = br.height * scale;
			
			if (e.within((int)dx, (int)dy, (int)dw, (int)dh)) {
				double rw = render.width * scale / render.scale / 2;
				double rh = render.height * scale / render.scale / 2;
				
				render.offsetX = -(int)((e.x - dx - rw) * render.scale / scale);
				render.offsetY = -(int)((e.y - dy - rh) * render.scale / scale);
				return true;
			}
			return false;
		}
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.UP) && e.has(Button.RIGHT)) {
			drag = false;
			doDragMode();
		}
		return super.mouse(e);
	}
	/** Set drag mode UI settings. */
	void doDragMode() {
		for (Field f : getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(DragSensitive.class)) {
				try {
					Object o = f.get(this);
					if (o != null) {
						UIComponent.class.cast(o).enabled(!drag);
					} else {
						System.out.println(f.getName());
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * Update the UI based on the selected building.
	 * @param b the building selected
	 */
	void doSelectBuilding(Building b) {
		currentBuilding = b;
		if (b != null) {
			player().currentBuilding = b.type;
			if (b.type.research != null) {
				research(b.type.research);
			}
			setBuildingList(0);
		}
		buildingInfoPanel.update();
	}
	/**
	 * Return the unit label for the given resource type.
	 * @param type the resource type
	 * @return the unit string
	 */
	String getUnit(String type) {
		return get("building.resource.type." + type);
	}
	/** The building preview component. */
	class BuildingPreview extends UIComponent {
		/** The building image. */
		public BufferedImage building;
		/** The building cost. */
		public int cost;
		/** The count on this planet. */
		public int count;
		@Override
		public void draw(Graphics2D g2) {
			if (building != null) {
				int dx = (width - building.getWidth()) / 2;
				int dy = (height - building.getHeight()) / 2;
				g2.drawImage(building, dx, dy, null);
				if (!enabled) {
					g2.setColor(Color.RED);
					g2.drawLine(0, 0, width - 1, height - 1);
					g2.drawLine(width - 1, 0, 0, height - 1);
				}
				String cs = cost + " cr";
				int w = commons.text().getTextWidth(10, cs);
				commons.text().paintTo(g2, width - w - 2, height - 12, 10, TextRenderer.YELLOW, cs);
				commons.text().paintTo(g2, 2, height - 12, 10, TextRenderer.GREEN, Integer.toString(count));
			}
		}
	}
	/** The building preview panel. */
	class BuildingsPanel extends UIContainer {
		/** The building preview. */
		BuildingPreview preview;
		/** The building list up button. */
		UIImageButton buildingUp;
		/** The building list up button empty. */
		UIImage buildingUpEmpty;
		/** The building list down button. */
		UIImageButton buildingDown;
		/** The building list down button empty. */
		UIImage buildingDownEmpty;
		/** The building list button. */
		UIImageButton buildingList;
		/** The build button. */
		UIImageTabButton build;
		/** The building name. */
		UILabel buildingName;
		/** Construct and place the UI. */
		public BuildingsPanel() {
			preview = new BuildingPreview();
			
			buildingUp = new UIImageButton(commons.colony().upwards);
			buildingDown = new UIImageButton(commons.colony().downwards);
			buildingUpEmpty = new UIImage(commons.colony().empty);
			buildingUpEmpty.visible(false);
			buildingDownEmpty = new UIImage(commons.colony().empty);
			buildingDownEmpty.visible(false);
			build = new UIImageTabButton(commons.colony().build);
			build.setDisabledPattern(commons.common().disabledPattern);
			build.enabled(false);
			buildingList = new UIImageButton(commons.colony().list);
			buildingName = new UILabel("", 10, commons.text());
			buildingName.color(TextRenderer.YELLOW);
			buildingName.horizontally(HorizontalAlignment.CENTER);
			
			preview.bounds(7, 7, 140, 103);
			
			buildingUp.location(153, 7);
			buildingUpEmpty.location(buildingUp.location());
			buildingDown.location(153, 62);
			buildingDownEmpty.location(buildingDown.location());
			buildingName.bounds(8, 117, 166, 18);
			buildingList.location(7, 142);
			build.location(94, 142);
			
			width = commons.colony().buildingsPanel.getWidth();
			height = commons.colony().buildingsPanel.getHeight();
			
			buildingDown.onClick = new Act() {
				@Override
				public void act() {
					sound(SoundType.CLICK_HIGH_2);
					setBuildingList(1);
				}
			};
			buildingDown.setHoldDelay(150);
			buildingUp.onClick = new Act() {
				@Override
				public void act() {
					sound(SoundType.CLICK_HIGH_2);
					setBuildingList(-1);
				}
			};
			buildingUp.setHoldDelay(150);
			
			buildingList.onClick = new Act() {
				@Override
				public void act() {
					placementMode = false;
					build.down = false;
					upgradePanel.hideUpgradeSelection();
					displaySecondary(Screens.INFORMATION_BUILDINGS);
				}
			};
			build.onPress = new Act() {
				@Override
				public void act() {
					placementMode = !placementMode;
					if (placementMode) {
						sound(SoundType.CLICK_HIGH_2);
						build.down = true;
						currentBuilding = null;
						buildingBox = null;
						Tile t = player().currentBuilding.tileset.get(race()).normal;
						placementRectangle.setSize(t.width + 2, t.height + 2);
					} else {
						build.down = false;
					}
				}
			};
			
			addThis();
		}
		@Override
		public void draw(Graphics2D g2) {
			g2.drawImage(commons.colony().buildingsPanel, 0, 0, null);
			super.draw(g2);
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.WHEEL)) {
				if (e.z < 0) {
					setBuildingList(-1);
				} else {
					setBuildingList(+1);
				}
				return true;
			}
			return super.mouse(e);
		}
	}
	/** The building info panel. */
	class BuildingInfoPanel extends UIContainer {
		/** The building name on the info panel. */
		UILabel buildingInfoName;
		/** The energy allocation percent. */
		UILabel energyPercent;
		/** The energy total. */
		UILabel energy;
		/** The worker allocated percent. */
		UILabel workerPercent;
		/** The worker total. */
		UILabel worker;
		/** The current production level. */
		UILabel production;
		/** The operation percent. */
		UILabel operationPercent;
		/** Demolish the building. */
		UIImageButton demolish;
		/** Construction Indicator with percent. */
		UIImageButton constructing;
		/** Damage indicator with percent. */
		UIImageButton damaged;
		/** Repairing indicator with percent. */
		UIImageButton repairing;
		/** The building is in normal operational condition. */
		UIImageButton undamaged;
		/** The building was manually put offline. */
		UIImageButton stateOffline;
		/** The building is inoperable due non-energy reasons. */
		UIImageButton stateInactive;
		/** The building is inoperable due low energy. */
		UIImageButton stateNoEnergy;
		/** The building is damaged. */
		UIImageButton stateDamaged;
		/** The active indicator. */
		UIImageButton stateActive;
		/** The energy label. */
		UIImage energyLabel;
		/** The worker label. */
		UIImage workerLabel;
		/** The production label. */
		UIImage productionLabel;
		/** The operation label. */
		UIImage operationLabel;
		/** The build/damage/repair percent upper. */
		UILabel progressUpper;
		/** The build/damage/repair percent lower. */
		UILabel progressLower;
		/** Construct the sub-elements. */
		public BuildingInfoPanel() {
			buildingInfoName = new UILabel("-", 10, commons.text());
			buildingInfoName.bounds(8, 6, 182, 16);
			
			energyPercent = new UILabel("-", 10, commons.text());
			energyPercent.bounds(70, 29, 28, 12);
			
			energy = new UILabel("-", 10, commons.text());
			energy.bounds(119, 29, 42, 12);
			
			workerPercent = new UILabel("-", 10, commons.text());
			workerPercent.bounds(70, 45, 28, 12);
			
			worker = new UILabel("-", 10, commons.text());
			worker.bounds(119, 45, 28, 12);
			
			operationPercent = new UILabel("-", 10, commons.text());
			operationPercent.bounds(70, 61, 28, 12);
			
			production = new UILabel("-", 10, commons.text());
			production.bounds(70, 77, 77, 12);
			
			energyLabel = new UIImage(commons.colony().energy);
			leftTo(energyPercent, energyLabel);
			
			workerLabel = new UIImage(commons.colony().workers);
			leftTo(workerPercent, workerLabel);

			operationLabel = new UIImage(commons.colony().operational);
			leftTo(operationPercent, operationLabel);

			productionLabel = new UIImage(commons.colony().production);
			leftTo(production, productionLabel);
			
			stateDamaged = new UIImageButton(commons.colony().statusDamaged);
			stateInactive = new UIImageButton(commons.colony().statusInactive);
			stateNoEnergy = new UIImageButton(commons.colony().statusNoEnergy);
			stateActive = new UIImageButton(commons.colony().active);
			stateOffline = new UIImageButton(commons.colony().statusOffline);
			constructing = new UIImageButton(commons.colony().constructing);
			damaged = new UIImageButton(commons.colony().damaged);
			repairing = new UIImageButton(commons.colony().repairing);
			undamaged = new UIImageButton(commons.colony().undamaged);

			damaged.location(8, 98);
			repairing.location(8, 98);
			undamaged.location(8, 98);

			stateActive.location(8, 122);
			stateOffline.location(8, 122);
			stateInactive.location(8, 122);
			stateNoEnergy.location(8, 122);
			stateDamaged.location(8, 122);
			constructing.location(8, 122);

			
			progressUpper = new UILabel("-", 10, commons.text());
			progressUpper.bounds(8 + 96, 98 + 3, 28, 10);
			progressUpper.visible(false);
			
			progressLower = new UILabel("-", 10, commons.text());
			progressLower.bounds(8 + 96, 122 + 3, 28, 10);
			progressLower.visible(false);
			
			demolish = new UIImageButton(commons.colony().demolish);
			demolish.location(161, 50);
			demolish.setDisabledPattern(commons.common().disabledPattern);
			stateActive.setDisabledPattern(commons.common().disabledPattern);
			stateInactive.setDisabledPattern(commons.common().disabledPattern);
			stateNoEnergy.setDisabledPattern(commons.common().disabledPattern);
			stateDamaged.setDisabledPattern(commons.common().disabledPattern);
			stateOffline.setDisabledPattern(commons.common().disabledPattern);

			centerYellow(buildingInfoName, energyPercent, energy, 
					workerPercent, worker, operationPercent, production,
					progressUpper, progressLower);

			width = commons.colony().buildingInfoPanel.getWidth();
			height = commons.colony().buildingInfoPanel.getHeight();
			
			addThis();
		}
		/**
		 * Set the location of the target component so that it is left
		 * to the source component directly and is centered along the same line.
		 * @param source the source component
		 * @param target the target component to set the location
		 */
		void leftTo(UIComponent source, UIComponent target) {
			target.location(source.x - target.width - 4, source.y + (source.height - target.height) / 2);
		}
		/**
		 * Center and set yellow color on the labels.
		 * @param labels the labels
		 */
		void centerYellow(UILabel... labels) {
			for (UILabel l : labels) {
				l.color(TextRenderer.YELLOW);
				l.horizontally(HorizontalAlignment.CENTER);
			}
		}
		@Override
		public void draw(Graphics2D g2) {
			g2.drawImage(commons.colony().buildingInfoPanel, 0, 0, null);
			super.draw(g2);
		}
		/** Hide the state and progress buttons. */
		public void hideStates() {
			stateActive.visible(false);
			stateDamaged.visible(false);
			stateInactive.visible(false);
			stateNoEnergy.visible(false);
			stateOffline.visible(false);
			
			
			constructing.visible(false);
			damaged.visible(false);
			repairing.visible(false);
			progressUpper.visible(false);
			progressLower.visible(false);
			undamaged.visible(false);
		}
		/**
		 * Update the UI based on the current status.
		 */
		public void update() {
			Building b = currentBuilding;
			if (b != null) {
				buildingInfoName.text(b.type.name);
				
				Set<UIComponent> tohide = new HashSet<UIComponent>(Arrays.asList(
						undamaged,
						damaged,		
						repairing,		
						constructing,		
						stateActive,		
						stateDamaged,		
						stateInactive,		
						stateOffline,		
						stateNoEnergy,		
						progressLower,		
						progressUpper		
				));
				
				if (b.isConstructing()) {
					energy.text("-");
					energyPercent.text("-");
					worker.text("-");
					workerPercent.text("-");
					operationPercent.text("-");
					production.text("-");
					
					constructing.visible(true);
					progressLower.visible(true);
					progressLower.text(Integer.toString(b.buildProgress * 100 / b.type.hitpoints));
					
					tohide.remove(constructing);
					tohide.remove(progressLower);
					
					if (b.isDamaged()) {
						buildingInfoPanel.damaged.visible(true);
						buildingInfoPanel.progressUpper.visible(true);
						
						tohide.remove(buildingInfoPanel.damaged);
						tohide.remove(buildingInfoPanel.progressUpper);
						
						if (b.hitpoints > 0) {
							buildingInfoPanel.progressUpper.text(Integer.toString((b.hitpoints - b.buildProgress) * 100 / b.hitpoints));
						} else {
							buildingInfoPanel.progressUpper.text("0");
						}
					} else {
						buildingInfoPanel.undamaged.visible(true);
						tohide.remove(buildingInfoPanel.undamaged);
						
					}
				} else {
					if (!b.enabled || b.isSeverlyDamaged()) {
						buildingInfoPanel.energy.text("-");
						buildingInfoPanel.energyPercent.text("-");
						buildingInfoPanel.worker.text("-");
						buildingInfoPanel.workerPercent.text("-");
						buildingInfoPanel.operationPercent.text("-");
						buildingInfoPanel.production.text("-");
					} else {
						if (b.getEnergy() < 0) {
							buildingInfoPanel.energy.text(Integer.toString(-b.getEnergy()));
							buildingInfoPanel.energyPercent.text(Integer.toString(Math.abs(b.assignedEnergy * 100 / b.getEnergy())));
						} else {
							buildingInfoPanel.energy.text("-");
							buildingInfoPanel.energyPercent.text("-");
						}
						if (b.getWorkers() < 0) {
							buildingInfoPanel.worker.text(Integer.toString(-b.getWorkers()));
							buildingInfoPanel.workerPercent.text(Integer.toString(Math.abs(b.assignedWorker)));
						} else {
							buildingInfoPanel.worker.text("-");
							buildingInfoPanel.workerPercent.text("-");
						}
						buildingInfoPanel.operationPercent.text(Integer.toString((int)(b.getEfficiency() * 100)));
						if (b.type.primary != null) {
							buildingInfoPanel.production.text(((int)b.getPrimary()) + getUnit(b.type.primary));
						} else {
							buildingInfoPanel.production.text("");
						}
					}
					
					// set the upper status indicators
					
					if (b.isDamaged()) {
						if (b.repairing) {
							buildingInfoPanel.repairing.visible(true);
							tohide.remove(buildingInfoPanel.repairing);
						} else {
							buildingInfoPanel.damaged.visible(true);
							tohide.remove(buildingInfoPanel.damaged);
						}
						buildingInfoPanel.progressUpper.visible(true);
						tohide.remove(buildingInfoPanel.progressUpper);
						buildingInfoPanel.progressUpper.text(Integer.toString((b.type.hitpoints - b.hitpoints) * 100 / b.type.hitpoints));
					} else {
						buildingInfoPanel.undamaged.visible(true);
						tohide.remove(buildingInfoPanel.undamaged);
					}
					
					// set the lower status indicator
					
					if (b.enabled) {
						if (b.isDamaged()) {
							buildingInfoPanel.stateDamaged.visible(true);
							tohide.remove(buildingInfoPanel.stateDamaged);
						} else
						if (b.isEnergyShortage()) {
							buildingInfoPanel.stateNoEnergy.visible(true);
							tohide.remove(buildingInfoPanel.stateNoEnergy);
						} else
						if (b.isWorkerShortage()) {
							buildingInfoPanel.stateInactive.visible(true);
							tohide.remove(buildingInfoPanel.stateInactive);
						} else {
							buildingInfoPanel.stateActive.visible(true);
							tohide.remove(buildingInfoPanel.stateActive);
						}

					} else {
						buildingInfoPanel.stateOffline.visible(true);
						tohide.remove(buildingInfoPanel.stateOffline);
					}
				}
				for (UIComponent c : tohide) {
					c.visible(false);
				}
				
			} else {
				buildingInfoPanel.buildingInfoName.text("-");
				buildingInfoPanel.energy.text("-");
				buildingInfoPanel.energyPercent.text("-");
				buildingInfoPanel.worker.text("-");
				buildingInfoPanel.workerPercent.text("-");
				buildingInfoPanel.operationPercent.text("-");
				buildingInfoPanel.production.text("-");
				buildingInfoPanel.undamaged.visible(true);
				buildingInfoPanel.stateInactive.visible(true);
				buildingInfoPanel.progressLower.visible(false);
				buildingInfoPanel.progressUpper.visible(false);
			}
			buildingInfoPanel.demolish.enabled(planet().owner == player() && currentBuilding != null);
			buildingInfoPanel.stateActive.enabled(planet().owner == player() && currentBuilding != null);
			buildingInfoPanel.stateDamaged.enabled(planet().owner == player() && currentBuilding != null);
			buildingInfoPanel.stateNoEnergy.enabled(planet().owner == player() && currentBuilding != null);
			buildingInfoPanel.stateInactive.enabled(planet().owner == player() && currentBuilding != null);
			buildingInfoPanel.stateOffline.enabled(planet().owner == player() && currentBuilding != null);
			buildingInfoPanel.repairing.enabled(planet().owner == player());
			buildingInfoPanel.damaged.enabled(planet().owner == player());
			
			upgradePanel.visible(
					b != null && b.type.upgrades.size() > 0 
					&& buildingInfoPanel.visible() 
					&& b.isComplete()
					&& !b.isSeverlyDamaged()
					&& planet().owner == player()
			);
		}
	}
	/** Perform the animation. */
	void doAnimation() {
		//wrap animation index
		if (animation == Integer.MAX_VALUE) {
			animation = -1;
		}
		animation++;
		blink = animation % 2 == 0;
		
		askRepaint();
	}
	/** Animate the shaking during an earthquake. */
	void doEarthquake() {
		if (planet().earthQuakeTTL > 0) {
			if (!commons.paused()) {
				render.offsetX += (2 - world().random.get().nextInt(5)) * 2;
				render.offsetY += (1 - world().random.get().nextInt(3));
				askRepaint();
			}
		}
	}
	/** Demolish the selected building. */
	void doDemolish() {
		surface().removeBuilding(currentBuilding);
		surface().placeRoads(planet().race, commons.world().buildingModel);
		
		int moneyBack = currentBuilding.type.cost * (1 + currentBuilding.upgradeLevel) / 2;
		
		player().money += moneyBack;
		
		player().statistics.demolishCount++;
		player().statistics.moneyDemolishIncome += moneyBack;
		player().statistics.moneyIncome += moneyBack;

		world().statistics.demolishCount++;
		world().statistics.moneyDemolishIncome += moneyBack;
		world().statistics.moneyDemolishIncome += moneyBack;

		doAllocation();
		buildingBox = null;
		doSelectBuilding(null);
		commons.sounds.play(SoundType.DEMOLISH_BUILDING);
	}
	/** Action for the Active button. */
	void doActive() {
		doAllocation();
		currentBuilding.enabled = false;
		doSelectBuilding(currentBuilding);
	}
	/** Action for the Offline button. */
	void doOffline() {
		doAllocation();
		currentBuilding.enabled = true;
		doSelectBuilding(currentBuilding);
	}
	/** Toggle the repair state. */
	void doToggleRepair() {
		currentBuilding.repairing = !currentBuilding.repairing;
		buildingInfoPanel.repairing.visible(currentBuilding.repairing);
		buildingInfoPanel.damaged.visible(!currentBuilding.repairing);
		doSelectBuilding(currentBuilding);
	}
	/** Perform the resource allocation now! */
	void doAllocation() {
		Allocator.computeNow(planet());
	}
	/**
	 * The information panel showing some details.
	 * @author akarnokd
	 */
	class InfoPanel extends UIContainer {
		/** The planet name. */
		UILabel planet;
		/** Label field. */
		UILabel owner;
		/** Label field. */
		UILabel race;
		/** Label field. */
		UILabel surface;
		/** Label field. */
		UILabel population;
		/** Label field. */
		UILabel housing;
		/** Label field. */
		UILabel worker;
		/** Label field. */
		UILabel hospital;
		/** Label field. */
		UILabel food;
		/** Label field. */
		UILabel energy;
		/** Label field. */
		UILabel police;
		/** Label field. */
		UILabel taxTradeIncome;
		/** Label field. */
		UILabel taxInfo;
		/** Label field. */
		UILabel allocation;
		/** Label field. */
		UILabel autobuild;
		/** Label field. */
		UILabel other;
		/** The labels. */
		List<UILabel> lines;
		/** Construct the label elements. */
		public InfoPanel() {
			int textSize = 10;
			planet = new UILabel("-", 14, commons.text());
			planet.location(10, 5);
			owner = new UILabel("-", textSize, commons.text());
			race = new UILabel("-", textSize, commons.text());
			surface = new UILabel("-", textSize, commons.text());
			population = new UILabel("-", textSize, commons.text());
			housing = new UILabel("-", textSize, commons.text());
			worker = new UILabel("-", textSize, commons.text());
			hospital = new UILabel("-", textSize, commons.text());
			food = new UILabel("-", textSize, commons.text());
			energy = new UILabel("-", textSize, commons.text());
			police = new UILabel("-", textSize, commons.text());
			taxTradeIncome = new UILabel("-", textSize, commons.text());
			taxInfo = new UILabel("-", textSize, commons.text());
			allocation = new UILabel("-", textSize, commons.text());
			autobuild = new UILabel("-", textSize, commons.text());
			other = new UILabel("-", 7, commons.text());
			other.wrap(true);
			
			lines = Arrays.asList(
					owner, race, surface, population, housing, worker, hospital, food, energy, police,
					taxTradeIncome, taxInfo, allocation, autobuild
			);
			
			enabled(false);
			addThis();
		}
		@Override
		public void draw(Graphics2D g2) {
			Composite c = g2.getComposite();
			g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
			g2.setColor(Color.BLACK);
			g2.fillRoundRect(0, 0, width, height, 10, 10);
			g2.setComposite(c);
			
			super.draw(g2);
		}
		/** Compute the panel size based on its visible component sizes. */
		public void computeSize() {
			int textSize = 10;
			int w = 0;
			int h = 0;
			int i = 0;
			for (UILabel c : lines) {
				if (c.visible()) {
					c.x = 10;
					c.y = 25 + (textSize + 3) * i;
					c.size(textSize);
					c.height = textSize;
					w = Math.max(w, c.x + c.width);
					h = Math.max(h, c.y + c.height);
					i++;
				}
			}
			w = Math.max(w, this.planet.x + this.planet.width);
			other.bounds(10, 25 + (textSize + 3) * i, w, 0);
			other.size(w, other.getWrappedHeight());
			h = Math.max(h, other.y + other.height);
			width = w + 10;
			height = h + 5;
		}
		/**
		 * Update the display values based on the current planet's settings.
		 * @return the planet statistics used
		 */
		public PlanetStatistics update() {
			Planet p = planet();
			
			planet.text(p.name, true);
			
			if (knowledge(p, PlanetKnowledge.OWNER) >= 0) {
				String s = p.owner != null ? p.owner.name : "-";
				owner.text(format("colonyinfo.owner", s), true);
				
				if (p.owner != null) {
					planet.color(p.owner.color);
					owner.color(TextRenderer.GREEN);
				} else {
					planet.color(TextRenderer.GRAY);
					owner.color(TextRenderer.GREEN);
				}
				s = p.isPopulated() ? get(p.getRaceLabel()) : "-";
				race.text(format("colonyinfo.race", s), true);
				owner.visible(true);
				race.visible(true);
			} else {
				owner.visible(false);
				race.visible(false);
				planet.color(TextRenderer.GRAY);
			}
			
			surface.text(format("colonyinfo.surface", firstUpper(get(p.type.label))), true);
			
			population.visible(false);
			housing.visible(false);
			worker.visible(false);
			hospital.visible(false);
			food.visible(false);
			energy.visible(false);
			police.visible(false);
			taxTradeIncome.visible(false);
			taxInfo.visible(false);
			allocation.visible(false);
			autobuild.visible(false);
			
			PlanetStatistics ps = null;
			
			if (p.isPopulated()) {
			
				if (p.owner == player()) {
					population.text(format("colonyinfo.population", 
							p.population, get(p.getMoraleLabel()), withSign(p.population - p.lastPopulation)
					), true).visible(true);
					
					ps = p.getStatistics();
					
					setLabel(housing, "colonyinfo.housing", ps.houseAvailable, p.population).visible(true);
					setLabel(worker, "colonyinfo.worker", p.population, ps.workerDemand).visible(true);
					setLabel(hospital, "colonyinfo.hospital", ps.hospitalAvailable, p.population).visible(true);
					setLabel(food, "colonyinfo.food", ps.foodAvailable, p.population).visible(true);
					setLabel(energy, "colonyinfo.energy", ps.energyAvailable, ps.energyDemand).visible(true);
					setLabel(police, "colonyinfo.police", ps.policeAvailable, p.population).visible(true);
					
					taxTradeIncome.text(format("colonyinfo.tax-trade", 
							p.taxIncome, p.tradeIncome
					), true).visible(true);
					
					taxInfo.text(format("colonyinfo.tax-info",
							get(p.getTaxLabel()), p.morale, withSign(p.morale - p.lastMorale)
					), true).visible(true);
					
					allocation.text(format("colonyinfo.allocation",
							get(p.getAllocationLabel())
					), true).visible(true);
					
					autobuild.text(format("colonyinfo.autobuild",
							get(p.getAutoBuildLabel())
					), true).visible(true);
					if (p.autoBuild != AutoBuild.OFF) {
						autobuild.color(TextRenderer.YELLOW);
					} else {
						autobuild.color(TextRenderer.GREEN);
					}
				} else {
					if (knowledge(p, PlanetKnowledge.BUILDING) >= 0) {
						population.text(format("colonyinfo.population.alien", 
								p.population
						), true).visible(true);
					}
				}
			}
			other.text(format("colonyinfo.other", world().getOtherItems()), true);

			computeSize();
			location(sidebarColonyInfo.x - width - 2, sidebarColonyInfo.y + sidebarColonyInfo.height - height - 2);
			
			return ps;
		}
		/** 
		 * Color the label according to the relation between the demand and available.
		 * @param label the target label
		 * @param format the format string to use
		 * @param demand the demand amount
		 * @param avail the available amount
		 * @return the label
		 */
		UILabel setLabel(UILabel label, String format, int avail, int demand) {
			label.text(format(format, avail, demand), true);
			if (demand <= avail) {
				label.color(TextRenderer.GREEN);
			} else
			if (demand < avail * 2) {
				label.color(TextRenderer.YELLOW);
			} else {
				label.color(TextRenderer.RED);
			}
			return label;
		}
		/**
		 * Add the +/- sign for the given integer value.
		 * @param i the value
		 * @return the string
		 */
		String withSign(int i) {
			if (i < 0) {
				return Integer.toString(i);
			} else
			if (i > 0) {
				return "+" + i;
			}
			return "0";
		}
	}
	/** 
	 * First letter to uppercase.
	 * @param s the string
	 * @return the modified string
	 */
	String firstUpper(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	/**
	 * The upgrade panel.
	 * @author akarnokd, 2011.03.31.
	 */
	class UpgradePanel extends UIContainer {
		/** The upgrade static label. */
		UIImage upgradeLabel;
		/** The upgrade description. */
		UILabel upgradeDescription;
		/** The upgrade steps. */
		final List<UIImageButton> steps = new ArrayList<UIImageButton>();
		/** No upgrades. */
		private UIImageButton none;
		/** Construct the panel. */
		public UpgradePanel() {
			size(commons.colony().upgradePanel.getWidth(), commons.colony().upgradePanel.getHeight());
			upgradeLabel = new UIImage(commons.colony().upgradeLabel);
			upgradeLabel.location(8, 3);
			upgradeDescription = new UILabel("-", 7, 178, commons.text());
			upgradeDescription.location(10, 21);
			upgradeDescription.height = 26;
			upgradeDescription.vertically(VerticalAlignment.TOP);
			upgradeDescription.color(TextRenderer.YELLOW);
			
			none = new UIImageButton(commons.colony().upgradeNone);
			none.onClick = new Act() {
				@Override
				public void act() {
					doUpgrade(0);
				}
			};
			none.location(upgradeLabel.x + upgradeLabel.width + 16, upgradeLabel.y - 2);
			for (int i = 1; i < 5; i++) {
				UIImageButton up = new UIImageButton(commons.colony().upgradeDark);
				up.location(upgradeLabel.x + upgradeLabel.width + i * 16 + 16, upgradeLabel.y - 2);
				steps.add(up);
				final int j = i;
				up.onClick = new Act() {
					@Override
					public void act() {
						sound(SoundType.CLICK_MEDIUM_2);
						doUpgrade(j);
					}
				};
				add(up);
			}
			none.visible(false);
			addThis();
		}
		@Override
		public void draw(Graphics2D g2) {
			g2.drawImage(commons.colony().upgradePanel, 0, 0, null);
			int over = -1;
			for (int i = 0; i < steps.size(); i++) {
				UIImageButton up = steps.get(i);
				up.visible(i + 1 <= currentBuilding.type.upgrades.size());
				if (currentBuilding.upgradeLevel <= i) {
					up.normal(commons.colony().upgradeDark);
					up.hovered(commons.colony().upgradeDark);
					up.pressed(commons.colony().upgradeDark);
				} else {
					up.normal(commons.colony().upgrade);
					up.hovered(commons.colony().upgrade);
					up.pressed(commons.colony().upgrade);
				}
				if (up.over && up.visible()) {
					over = i + 1;
				}
			}
			if (over >= 1) {
				upgradeDescription.text(
					currentBuilding.type.upgrades.get(over - 1).description
				);
			} else
			if (over == 0) {
				upgradeDescription.text(get("buildings.upgrade.default.description"));
			} else {
				upgradeDescription.text("");
			}
			super.draw(g2);
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.LEAVE)) {
				hideUpgradeSelection();
				return true;
			}
			return super.mouse(e);
		}
		/** Hide the selection of the upgrade panel. */
		public void hideUpgradeSelection() {
			for (UIImageButton img : upgradePanel.steps) {
				img.over = false;
			}
			upgradeDescription.text("");
		}
		@Override
		public UIComponent visible(boolean state) {
			if (!state) {
				hideUpgradeSelection();
			}
			return super.visible(state);
		}
	}
	/** 
	 * Upgrade the current building. 
	 * @param j level
	 */
	void doUpgrade(int j) {
		if (currentBuilding != null && currentBuilding.upgradeLevel < j) {
			int delta = (j - currentBuilding.upgradeLevel) * currentBuilding.type.cost;
			if (player().money >= delta) {
				player().money -= delta;
				player().today.buildCost += delta;
				
				currentBuilding.buildProgress = currentBuilding.type.hitpoints * 1 / 4;
				currentBuilding.hitpoints = currentBuilding.buildProgress;
				
				doAllocation();
				
				upgradePanel.hideUpgradeSelection();
				
				player().statistics.upgradeCount += j - currentBuilding.upgradeLevel;
				player().statistics.moneyUpgrade += delta;
				player().statistics.moneySpent += delta;
				
				world().statistics.upgradeCount += j - currentBuilding.upgradeLevel;
				world().statistics.moneyUpgrade += delta;
				world().statistics.moneySpent += delta;

				currentBuilding.setLevel(j);
			}
		}
	}
	@Override
	public void onInitialize() {
		selection = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileEdge, 0xFFFFFF00), null);
		areaAccept = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileEdge, 0xFF00FFFF), null);
		areaEmpty = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileEdge, 0xFF808080), null);
		areaDeny = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileCrossed, 0xFFFF0000), null);
		areaCurrent  = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileCrossed, 0xFFFFCC00), null);
		
		selection.alpha = 1.0f;
		areaAccept.alpha = 1.0f;
		areaDeny.alpha = 1.0f;
		areaCurrent.alpha = 1.0f;
		
		sidebarBuildings = new UIImageButton(commons.colony().sidebarBuildings);
		sidebarBuildingsEmpty = new UIImage(commons.colony().sidebarBuildingsEmpty);
		sidebarBuildingsEmpty.visible(false);
		sidebarRadar = new UIImageButton(commons.colony().sidebarRadar);
		sidebarRadarEmpty = new UIImage(commons.colony().sidebarRadarEmpty);
		sidebarRadarEmpty.visible(false);
		sidebarBuildingInfo = new UIImageButton(commons.colony().sidebarBuildingInfo);
		sidebarColonyInfo = new UIImageButton(commons.colony().sidebarColonyInfo);
		sidebarNavigation = new UIImageButton(commons.colony().sidebarButtons);
		
		colonyInfo = new UIImageButton(commons.colony().colonyInfo);
		bridge = new UIImageButton(commons.colony().bridge);
		planets = new UIImageButton(commons.colony().planets);
		starmap = new UIImageButton(commons.colony().starmap);
		
		render = new SurfaceRenderer();
		render.z = -1;
		
		leftFill = new UIImageFill(commons.colony().sidebarLeftTop, commons.colony().sidebarLeftFill, commons.colony().sidebarLeftBottom, false);
		rightFill = new UIImageFill(commons.colony().sidebarRightTop, commons.colony().sidebarRightFill, commons.colony().sidebarRightBottom, false);
		
		radarPanel = new UIImage(commons.colony().radarPanel);
		radar = new RadarRender();
		radar.z = 1;
		
		buildingsPanel = new BuildingsPanel();
		buildingsPanel.z = 1;
		buildingInfoPanel = new BuildingInfoPanel();
		buildingInfoPanel.z = 1;
		
		infoPanel = new InfoPanel();
		
		sidebarNavigation.onClick = new Act() {
			@Override
			public void act() {
				sound(SoundType.GROUNDWAR_TOGGLE_PANEL);
				colonyInfo.visible(!colonyInfo.visible());
				bridge.visible(!bridge.visible());
				planets.visible(!planets.visible());
				starmap.visible(!starmap.visible());
			}
		};
		sidebarRadar.onClick = new Act() {
			@Override
			public void act() {
				sound(SoundType.GROUNDWAR_TOGGLE_PANEL);
				radar.visible(!radar.visible());
				radarPanel.visible(!radarPanel.visible());
			}
		};
		sidebarBuildingInfo.onClick = new Act() {
			@Override
			public void act() {
				if (planet().owner == player()) {
					sound(SoundType.GROUNDWAR_TOGGLE_PANEL);
					showBuildingInfo = !showBuildingInfo;
					upgradePanel.visible(currentBuilding != null 
							&& currentBuilding.type.upgrades.size() > 0 
							&& buildingInfoPanel.visible());
				}
			}
		};
		sidebarBuildings.onClick = new Act() {
			@Override
			public void act() {
				if (planet().owner == player()) {
					sound(SoundType.GROUNDWAR_TOGGLE_PANEL);
					showBuildingList = !showBuildingList;
				}
			}
		};
		colonyInfo.onClick = new Act() {
			@Override
			public void act() {
				placementMode = false;
				buildingsPanel.build.down = false;
				upgradePanel.hideUpgradeSelection();
				displaySecondary(Screens.INFORMATION_COLONY);
			}
		};
		planets.onClick = new Act() {
			@Override
			public void act() {
				placementMode = false;
				buildingsPanel.build.down = false;
				upgradePanel.hideUpgradeSelection();
				displaySecondary(Screens.INFORMATION_PLANETS);
			}
		};
		starmap.onClick = new Act() {
			@Override
			public void act() {
				upgradePanel.hideUpgradeSelection();
				displayPrimary(Screens.STARMAP);
			}
		};
		bridge.onClick = new Act() {
			@Override
			public void act() {
				upgradePanel.hideUpgradeSelection();
				displayPrimary(Screens.BRIDGE);
			}
		};
		
		buildingInfoPanel.demolish.onClick = new Act() {
			@Override
			public void act() {
				upgradePanel.hideUpgradeSelection();
				doDemolish();
			}
		};
		buildingInfoPanel.stateActive.onClick = new Act() {
			@Override
			public void act() {
				sound(SoundType.CLICK_MEDIUM_2);
				doActive();
			}
		};
		buildingInfoPanel.stateNoEnergy.onClick = new Act() {
			@Override
			public void act() {
				sound(SoundType.CLICK_MEDIUM_2);
				doActive();
			}
		};
		buildingInfoPanel.stateDamaged.onClick = new Act() {
			@Override
			public void act() {
				sound(SoundType.CLICK_MEDIUM_2);
				doActive();
			}
		};
		buildingInfoPanel.stateInactive.onClick = new Act() {
			@Override
			public void act() {
				sound(SoundType.CLICK_MEDIUM_2);
				doActive();
			}
		};

		buildingInfoPanel.stateOffline.onClick = new Act() {
			@Override
			public void act() {
				sound(SoundType.CLICK_MEDIUM_2);
				doOffline();
			}
		};
		buildingInfoPanel.repairing.onClick = new Act() {
			@Override
			public void act() {
				sound(SoundType.CLICK_MEDIUM_2);
				doToggleRepair();
			}
		};
		buildingInfoPanel.damaged.onClick = new Act() {
			@Override
			public void act() {
				sound(SoundType.CLICK_MEDIUM_2);
				doToggleRepair();
			}
		};
		
		sidebarColonyInfo.onClick = new Act() {
			@Override
			public void act() {
				if (knowledge(planet(), PlanetKnowledge.VISIBLE) > 0) {
					sound(SoundType.GROUNDWAR_TOGGLE_PANEL);
					showInfo = !showInfo;
				}
			}
		};
		upgradePanel = new UpgradePanel();
		
		prev = new UIImageButton(commons.starmap().backwards);
		prev.setHoldDelay(250);
		prev.onClick = new Act() {
			@Override
			public void act() {
				sound(SoundType.CLICK_HIGH_2);
				player().movePrevPlanet();
			}
		};
		next = new UIImageButton(commons.starmap().forwards);
		next.setHoldDelay(250);
		next.onClick = new Act() {
			@Override
			public void act() {
				sound(SoundType.CLICK_HIGH_2);
				player().moveNextPlanet();
			}
		};
		
		addThis();
	}
	@Override
	public void onResize() {
		base.setBounds(0, 20, getInnerWidth(), getInnerHeight() - 38);
		window.setBounds(base.x + 20, base.y, base.width - 40, base.height);
		
		sidebarBuildings.location(base.x, base.y);
		sidebarBuildingsEmpty.location(base.x, base.y);
		sidebarRadar.location(base.x, base.y + base.height - sidebarRadar.height);
		sidebarRadarEmpty.location(sidebarRadar.location());
		
		sidebarBuildingInfo.location(base.x + base.width - sidebarBuildingInfo.width, base.y);
		sidebarNavigation.location(base.x + base.width - sidebarNavigation.width, base.y + base.height - sidebarNavigation.height);
		sidebarColonyInfo.location(sidebarNavigation.x, sidebarNavigation.y - sidebarColonyInfo.height);
		
		bridge.location(sidebarNavigation.x - bridge.width, base.y + base.height - bridge.height);
		starmap.location(bridge.x - starmap.width, base.y + base.height - starmap.height);
		planets.location(starmap.x - planets.width, base.y + base.height - planets.height);
		colonyInfo.location(planets.x - colonyInfo.width, base.y + base.height - colonyInfo.height);

		render.bounds(window.x, window.y, window.width, window.height);
		
		leftFill.bounds(sidebarBuildings.x, sidebarBuildings.y + sidebarBuildings.height, 
				sidebarBuildings.width, sidebarRadar.y - sidebarBuildings.y - sidebarBuildings.height);
		rightFill.bounds(sidebarBuildingInfo.x, sidebarBuildingInfo.y + sidebarBuildingInfo.height, 
				sidebarBuildingInfo.width, sidebarColonyInfo.y - sidebarBuildingInfo.y - sidebarBuildingInfo.height);
		
		radarPanel.location(sidebarRadar.x + sidebarRadar.width - 1, sidebarRadar.y);
		radar.bounds(radarPanel.x + 13, radarPanel.y + 13, 154, 134);
		buildingsPanel.location(sidebarBuildings.x + sidebarBuildings.width - 1, sidebarBuildings.y);
		buildingInfoPanel.location(sidebarBuildingInfo.x - buildingInfoPanel.width, sidebarBuildingInfo.y);
		
		upgradePanel.location(buildingInfoPanel.x, buildingInfoPanel.y + buildingInfoPanel.height);
		
		prev.location(sidebarRadar.x + sidebarRadar.width + 2, sidebarRadar.y - prev.height - 2);
		next.location(prev.x + prev.width + 2, prev.y);
		
	}
	/**
	 * @return the current planet surface or selects one from the player's list.
	 */
	public PlanetSurface surface() {
		if (planet() != null) {
			return planet().surface;
		}
		Planet p = player().moveNextPlanet();
		return p != null ? p.surface : null;
	}
	/** 
	 * Prepare the controls of the buildings panel. 
	 * @param delta to go to the previous or next
	 */
	void setBuildingList(int delta) {
		List<BuildingType> list = commons.world().listBuildings();
		int idx = list.indexOf(building());
		if (list.size() > 0) {
			BuildingType bt = null; 
			if (idx < 0) {
				idx = 0;
				bt = list.get(idx);
				player().currentBuilding = bt;
			} else {
				idx = Math.min(Math.max(0, idx + delta), list.size() - 1);
				bt = list.get(idx);
			}
			if (delta != 0) {
				player().currentBuilding = bt;
			}
			String race = race();
			Tile t = player().currentBuilding.tileset.get(race).normal;
			placementRectangle.setSize(t.width + 2, t.height + 2);
			
			buildingsPanel.preview.building = bt.tileset.get(race).preview;
			buildingsPanel.preview.cost = bt.cost;
			buildingsPanel.preview.count = planet().countBuilding(bt);
			buildingsPanel.preview.enabled(planet().canBuild(bt) && planet().owner == player());
			placementMode &= planet().canBuild(bt);
			buildingsPanel.build.down &= planet().canBuild(bt);
			buildingsPanel.buildingName.text(bt.name);
			buildingsPanel.build.enabled(buildingsPanel.preview.enabled());
			
			if (bt.research != null && delta != 0) {
				research(bt.research);
			}
		} else {
			buildingsPanel.build.enabled(false);
			buildingsPanel.preview.building = null;
		}
		
		buildingsPanel.buildingDown.visible(idx < list.size() - 1);
		buildingsPanel.buildingUp.visible(idx > 0);
		buildingsPanel.buildingDownEmpty.visible(!buildingsPanel.buildingDown.visible());
		buildingsPanel.buildingUpEmpty.visible(!buildingsPanel.buildingUp.visible());
	}
	/**
	 * Try placing a building to the current placementRectange.
	 * @param more cancel the building mode on successful place?
	 */
	void placeBuilding(boolean more) {
		if (surface().canPlaceBuilding(placementRectangle)
				&& player().money >= player().currentBuilding.cost
				&& planet().canBuild(player().currentBuilding)
		) {
				
				Building b = new Building(player().currentBuilding, race());
				b.location = Location.of(placementRectangle.x + 1, placementRectangle.y - 1);
				
				planet().surface.placeBuilding(b.tileset.normal, b.location.x, b.location.y, b);
	
				planet().surface.placeRoads(race(), commons.world().buildingModel);
				
				placementMode = more && planet().canBuild(building());
				buildingsPanel.build.down = placementMode;
	
				buildingBox = getBoundingRect(b.location);
				doSelectBuilding(b);
				
				buildingInfoPanel.update();
				setBuildingList(0);

				player().money -= player().currentBuilding.cost;
				player().today.buildCost += player().currentBuilding.cost;
				
				player().statistics.buildCount++;
				player().statistics.moneyBuilding += player().currentBuilding.cost;
				player().statistics.moneySpent += player().currentBuilding.cost;
				
				world().statistics.buildCount++;
				world().statistics.moneyBuilding += player().currentBuilding.cost;
				world().statistics.moneySpent += player().currentBuilding.cost;
				
				commons.sounds.play(SoundType.DEPLOY_BUILDING);
		} else {
			if (player().money < player().currentBuilding.cost) {
				sound(SoundType.NOT_AVAILABLE);
				
				commons.control().displayError(get("message.not_enough_money"));
			} else
			if (!surface().canPlaceBuilding(placementRectangle)) {
				sound(SoundType.NOT_AVAILABLE);
				
				commons.control().displayError(get("message.cant_build_there"));
			}
		}
	}
	@Override
	public Screens screen() {
		return Screens.COLONY;
	}
	@Override
	public void onEndGame() {
		currentBuilding = null;
		buildingBox = null;
		lastSurface = null;
	}
	/**
	 * Initiate a battle with the given settings.
	 * @param battle the battle information
	 */
	public void initiateBattle(BattleInfo battle) {
		// TODO prepare battle
	}
}
