/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;


import hu.openig.core.Act;
import hu.openig.core.Location;
import hu.openig.core.PlanetType;
import hu.openig.core.RoadType;
import hu.openig.core.Sides;
import hu.openig.core.Tile;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.OriginalBuilding;
import hu.openig.model.OriginalPlanet;
import hu.openig.model.PlanetSurface;
import hu.openig.model.SurfaceEntity;
import hu.openig.model.SurfaceEntityType;
import hu.openig.model.SurfaceFeature;
import hu.openig.model.TileSet;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageFill;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.ImageUtils;
import hu.openig.utils.XElement;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.Timer;

/**
 * The planet surface rendering screen.
 * @author akarnokd, 2010.01.11.
 */
public class PlanetScreen extends ScreenBase {
	/** Indicate if a component is drag sensitive. */
	@Retention(RetentionPolicy.RUNTIME)
	@interface DragSensitive { }
	/** The planet surface definition. */
	PlanetSurface surface;
	/** The current location based on the mouse pointer. */
	Location current;
	/** 
	 * The selected rectangular region. The X coordinate is the smallest, the Y coordinate is the largest
	 * the width points to +X and height points to -Y direction
	 */
	Rectangle selectedRectangle;
	/** Show the buildings? */
	boolean showBuildings = true;
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
	Timer animationTimer;
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
	/** The lighting level. */
	float alpha = 1.0f;
	@Override
	public void onFinish() {
		if (animationTimer != null) {
			animationTimer.stop();
			animationTimer = null;
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#keyTyped(int, int)
	 */
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
	public void onEnter(Object mode) {
		animationTimer.start();
		render.offsetX = -(surface.boundingRectangle.width - width) / 2;
		render.offsetY = -(surface.boundingRectangle.height - height) / 2;
		focused = render;
		importEarth(); // FIXME for testing purposes only
	}

	@Override
	public void onLeave() {
		animationTimer.stop();
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
				cell.image = se.tile.getStrip(se.virtualRow);
				return;
			} else
			if (se.virtualRow == se.tile.height - 1) {
				cell.image = se.tile.getStrip(se.tile.height - 1 + se.virtualColumn);
				return;
			}
			cell.image = null;
			return;
		}
		if (symbolic) {
			Tile tile = null;

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
				if (se.building.isSeverlyDamaged()) {
					tile = se.building.type.minimapTiles.damaged;
				} else
				if (se.building.getEfficiency() < 0.5f) {
					tile = se.building.type.minimapTiles.inoperable;
				} else {
					tile = se.building.type.minimapTiles.normal;
				}
			}
			
			cell.yCompensation = 27 - tile.imageHeight;
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
			cell.image = tile.getStrip(0);
			cell.a = loc1.x;
			cell.b = loc1.y;
		} else {
			Tile tile = null;
			if (se.building.isSeverlyDamaged()) {
				tile = se.building.tileset.damaged;
			} else 
			if (se.building.getEfficiency() < 0.5f) {
				tile = se.building.tileset.nolight;
			} else {
				tile = se.building.tileset.normal;
			}
			tile.alpha = se.tile.alpha;
			cell.yCompensation = 27 - tile.imageHeight;
			cell.a = loc1.x - se.virtualColumn;
			cell.b = loc1.y + se.virtualRow - se.tile.height + 1;
			if (se.virtualColumn == 0 && se.virtualRow < se.tile.height) {
				cell.image = tile.getStrip(se.virtualRow);
				return;
			} else
			if (se.virtualRow == se.tile.height - 1) {
				cell.image = tile.getStrip(se.tile.height - 1 + se.virtualColumn);
				return;
			}
			cell.image = null;
			return;
		}
			
	}
	/**
	 * Test if the given rectangular region is eligible for building placement, e.g.:
	 * all cells are within the map's boundary, no other buildings are present within the given bounds,
	 * no multi-tile surface object is present at the location.
	 * @param rect the surface rectangle
	 * @return true if the building can be placed
	 */
	public boolean canPlaceBuilding(Rectangle rect) {
		for (int i = rect.x; i < rect.x + rect.width; i++) {
			for (int j = rect.y; j > rect.y - rect.height; j--) {
				if (!canPlaceBuilding(i, j)) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * Test if the coordinates are suitable for building placement.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return true if placement is allowed
	 */
	public boolean canPlaceBuilding(int x, int y) {
		if (!surface.cellInMap(x, y)) {
			return false;
		} else {
			SurfaceEntity se = surface.buildingmap.get(Location.of(x, y));
			if (se != null && se.type == SurfaceEntityType.BUILDING) {
				return false;
			} else {
				se = surface.basemap.get(Location.of(x, y));
				if (se != null && (se.tile.width > 1 || se.tile.height > 1)) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * Compute the bounding rectangle of the rendered building object.
	 * @param loc the location to look for a building.
	 * @return the bounding rectangle or null if the target does not contain a building
	 */
	public Rectangle getBoundingRect(Location loc) {
		SurfaceEntity se = surface.buildingmap.get(loc);
		if (se != null && se.type == SurfaceEntityType.BUILDING) {
			int a0 = loc.x - se.virtualColumn;
			int b0 = loc.y + se.virtualRow;
			
			int x = surface.baseXOffset + Tile.toScreenX(a0, b0);
			int y = surface.baseYOffset + Tile.toScreenY(a0, b0 - se.tile.height + 1) + 27;
			
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
		SurfaceEntity se = surface.buildingmap.get(loc);
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
				} 
//				else
//				if (sel) {
//					Location loc = getLocationAt(e.x, e.y);
//					current = loc;
//					placementRectangle.x = current.x - placementRectangle.width / 2;
//					placementRectangle.y = current.y + placementRectangle.height / 2;
//					selectedRectangle.x = Math.min(orig.x, loc.x);
//					selectedRectangle.y = Math.max(orig.y, loc.y);
//					selectedRectangle.width = Math.max(orig.x, loc.x) - selectedRectangle.x + 1;
//					selectedRectangle.height = - Math.min(orig.y, loc.y) + selectedRectangle.y + 1;
//					rep = true;
//				} else {
//					current = getLocationAt(e.x, e.y);
//					if (current != null) {
//						placementRectangle.x = current.x - placementRectangle.width / 2;
//						placementRectangle.y = current.y + placementRectangle.height / 2;
//						rep = true;
//					}
//				}
				break;
			case DOWN:
				if (e.has(Button.RIGHT)) {
					drag = true;
					lastX = e.x;
					lastY = e.y;
					doDragMode();
				} else
				if (e.has(Button.MIDDLE)) {
					render.offsetX = -(surface.boundingRectangle.width - width) / 2;
					render.offsetY = -(surface.boundingRectangle.height - height) / 2;
					scale = 1;
					rep = true;
				}
				if (e.has(Button.LEFT) && surface != null) {
					Location loc = getLocationAt(e.x, e.y);
					buildingBox = getBoundingRect(loc);
					doSelectBuilding(getBuildingAt(loc));
					rep = true;
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
				} else {
					if (e.z < 0) {
						offsetY += 28;
					} else {
						offsetY -= 28;
					}
				}
				break;
			default:
			}
			return rep;
		}
		@Override
		public void draw(Graphics2D g2) {
			RenderTools.setInterpolation(g2, true);
			
			Shape save0 = g2.getClip();
			g2.clipRect(0, 0, width, height);
			
			g2.setColor(new Color(96, 96, 96));
			g2.fillRect(0, 0, width, height);
			
			if (surface == null) {
				return;
			}
			AffineTransform at = g2.getTransform();
			g2.translate(offsetX, offsetY);
			g2.scale(scale, scale);
			
			int x0 = surface.baseXOffset;
			int y0 = surface.baseYOffset;

			Rectangle br = surface.boundingRectangle;
			g2.setColor(new Color(128, 0, 0));
			g2.fillRect(br.x, br.y, br.width, br.height);
			g2.setColor(Color.YELLOW);
			g2.drawRect(br.x, br.y, br.width, br.height);
			
			
			BufferedImage empty = areaEmpty.getStrip(0);
			Rectangle renderingWindow = new Rectangle(0, 0, width, height);
			for (int i = 0; i < surface.renderingOrigins.size(); i++) {
				Location loc = surface.renderingOrigins.get(i);
				for (int j = 0; j < surface.renderingLength.get(i); j++) {
					int x = x0 + Tile.toScreenX(loc.x - j, loc.y);
					int y = y0 + Tile.toScreenY(loc.x - j, loc.y);
					Location loc1 = Location.of(loc.x - j, loc.y);
					SurfaceEntity se = surface.buildingmap.get(loc1);
					if (se == null || !showBuildings) {
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
			if (placementHints) {
				for (Location loc : surface.basemap.keySet()) {
					if (!canPlaceBuilding(loc.x, loc.y)) {
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
				if (current != null) {
					int x = x0 + Tile.toScreenX(current.x, current.y);
					int y = y0 + Tile.toScreenY(current.x, current.y);
					g2.drawImage(areaCurrent.getStrip(0), x, y, null);
				}
			}
			if (placementMode) {
				if (placementRectangle.width > 0) {
					for (int i = placementRectangle.x; i < placementRectangle.x + placementRectangle.width; i++) {
						for (int j = placementRectangle.y; j > placementRectangle.y - placementRectangle.height; j--) {
							
							BufferedImage img = areaAccept.getStrip(0);
							// check for existing building
							if (!canPlaceBuilding(i, j)) {
								img = areaDeny.getStrip(0);
							}
							
							int x = x0 + Tile.toScreenX(i, j);
							int y = y0 + Tile.toScreenY(i, j);
							g2.drawImage(img, x, y, null);
						}
					}
				}
			}
			if (showBuildings) {
				for (Building b : surface.buildings) {
					Rectangle r = getBoundingRect(b.location);
//					if (r == null) {
//						continue;
//					}
					String label = commons.labels().get(b.type.label);
					int nameLen = commons.text().getTextWidth(7, label);
					int h = (r.height - 7) / 2;
					int nx = r.x + (r.width - nameLen) / 2;
					int ny = r.y + h;
					
					Composite compositeSave = null;
					Composite a1 = null;
					
					if (textBackgrounds) {
						compositeSave = g2.getComposite();
						a1 = AlphaComposite.SrcOver.derive(0.8f);
						g2.setComposite(a1);
						g2.setColor(Color.BLACK);
						g2.fillRect(nx - 2, ny - 2, nameLen + 4, 12);
						g2.setComposite(compositeSave);
					}
					
					commons.text().paintTo(g2, nx + 1, ny + 1, 7, 0xFF8080FF, label);
					commons.text().paintTo(g2, nx, ny, 7, 0xD4FC84, label);

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
							g2.drawImage(commons.colony().repair[(animation / 3) % 3], ex, ey, null);
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
					}
				}
			}
			if (showBuildings && buildingBox != null) {
				g2.setColor(Color.RED);
				g2.drawRect(buildingBox.x, buildingBox.y, buildingBox.width, buildingBox.height);
			}
			
			g2.setTransform(at);
			g2.setColor(Color.WHITE);
			
			g2.setClip(save0);
			RenderTools.setInterpolation(g2, false);
		}
		
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
			
			g2.setColor(new Color(96, 96, 96));
			g2.fillRect(0, 0, width, height);
			
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

			g2.setColor(new Color(128, 0, 0));
			g2.fillRect(br.x, br.y, br.width, br.height);
			g2.setColor(Color.YELLOW);
			g2.drawRect(br.x, br.y, br.width - 1, br.height - 1);
			
			BufferedImage empty = areaEmpty.getStrip(0);
			Rectangle renderingWindow = new Rectangle(0, 0, width, height);
			for (int i = 0; i < surface.renderingOrigins.size(); i++) {
				Location loc = surface.renderingOrigins.get(i);
				for (int j = 0; j < surface.renderingLength.get(i); j++) {
					int x = x0 + Tile.toScreenX(loc.x - j, loc.y);
					int y = y0 + Tile.toScreenY(loc.x - j, loc.y);
					Location loc1 = Location.of(loc.x - j, loc.y);
					SurfaceEntity se = surface.buildingmap.get(loc1);
					if (se == null || !showBuildings) {
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
			if (showBuildings) {
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
			Rectangle br = surface.boundingRectangle;

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
	/** Import the earth map. */
	void importEarth() {
		
		OriginalPlanet planet = parseOriginalPlanet().get("Earth");
		
		placeTilesFromOriginalMap("colony/" + planet.getMapName(), planet.surfaceType.toLowerCase(), -1, -1);

		for (OriginalBuilding ob : planet.buildings) {
			BuildingType bt = commons.world.buildingModel.buildings.get(ob.getName()); 
			String r = planet.getRaceTechId();
			TileSet t = bt.tileset.get(r);
			Building bld = new Building(bt, r);
			bld.makeFullyBuilt();
			bld.location = Location.of(ob.location.x + -1, ob.location.y + -1);
			surface.buildings.add(bld);
			placeTile(t.normal, ob.location.x + -1, ob.location.y + -1, SurfaceEntityType.BUILDING, bld);
		}
		placeRoads(planet.getRaceTechId());

	}
	/**
	 * Place an original map tiles onto the current surface.
	 * @param path the path to the original map file
	 * @param surfaceType the surface type
	 * @param shiftX the shift in X coordinates to place the map elements
	 * @param shiftY the shift in Y coordinates to place the map elements
	 */
	private void placeTilesFromOriginalMap(String path, String surfaceType, int shiftX, int shiftY) {
		byte[] map = rl.getData("en", path);
		PlanetType pt = commons.world.galaxyModel.planetTypes.get(surfaceType);
		int bias = 41; 
		if ("neptoplasm".equals(surfaceType)) {
			bias = 84;
		}
		for (int i = 0; i < 65 * 65; i++) {
			int tile = (map[4 + i * 2] & 0xFF) - bias;
			int strip = map[5 + i * 2] & 0xFF;
			if (strip == 0 && tile != 255) {
				Location loc = toOriginalLocation(i);
				Tile t = pt.tiles.get(tile);
				if (t != null) {
					SurfaceFeature sf = new SurfaceFeature();
					sf.location = Location.of(loc.x + shiftX, loc.y + shiftY);
					sf.id = tile;
					sf.type = surfaceType;
					sf.tile = t;
					surface.features.add(sf);
					placeTile(t, loc.x + shiftX, loc.y + shiftY, SurfaceEntityType.BASE, null);
				}
			}
		}
	}
	/**
	 * Place a tile onto the current surface map.
	 * @param tile the tile
	 * @param x the tile's leftmost coordinate
	 * @param y the tile's leftmost coordinate
	 * @param type the tile type
	 * @param building the building object to assign
	 */
	void placeTile(Tile tile, int x, int y, SurfaceEntityType type, Building building) {
		for (int a = x; a < x + tile.width; a++) {
			for (int b = y; b > y - tile.height; b--) {
				SurfaceEntity se = new SurfaceEntity();
				se.type = type;
				se.virtualRow = y - b;
				se.virtualColumn = a - x;
				se.tile = tile;
//				se.tile.alpha = alpha;
				se.building = building;
				if (type != SurfaceEntityType.BASE) {
					surface.buildingmap.put(Location.of(a, b), se);
				} else {
					surface.basemap.put(Location.of(a, b), se);
				}
			}
		}
	}
	/**
	 * Convert the original index location of the map to actual (x, y) location.
	 * @param index the index into the map block, starting at 0
	 * @return the location
	 */
	public Location toOriginalLocation(int index) {
		int row = index % 65;
		int col = index / 65;
		
		int x0 = (col + 1) / 2;
		int y0 = - col / 2;
		
		int x = x0 - row;
		int y = y0 - row;
		return Location.of(x, y);
	}
	/**
	 * Place roads around buildings for the given race.
	 * @param raceId the race who builds the roads
	 */
	void placeRoads(String raceId) {
		Map<RoadType, Tile> rts = commons.world.buildingModel.roadTiles.get(raceId);
		Map<Tile, RoadType> trs = commons.world.buildingModel.tileRoads.get(raceId);
		// remove all roads
		Iterator<SurfaceEntity> it = surface.buildingmap.values().iterator();
		while (it.hasNext()) {
			SurfaceEntity se = it.next();
			if (se.type == SurfaceEntityType.ROAD) {
				it.remove();
			}
		}
		Set<Location> corners = new HashSet<Location>();
		for (Building bld : surface.buildings) {
			Rectangle rect = new Rectangle(bld.location.x - 1, bld.location.y + 1, bld.tileset.normal.width + 2, bld.tileset.normal.height + 2);
			addRoadAround(rts, rect, corners);
		}
		SurfaceEntity[] neighbors = new SurfaceEntity[9];
		for (Location l : corners) {
			SurfaceEntity se = surface.buildingmap.get(l);
			if (se == null || se.type != SurfaceEntityType.ROAD) {
				continue;
			}
			setNeighbors(l.x, l.y, surface.buildingmap, neighbors);
			int pattern = 0;
			
			RoadType rt1 = null;
			if (neighbors[1] != null && neighbors[1].type == SurfaceEntityType.ROAD) {
				pattern |= Sides.TOP;
				rt1 = trs.get(neighbors[1].tile);
			}
			RoadType rt3 = null;
			if (neighbors[3] != null && neighbors[3].type == SurfaceEntityType.ROAD) {
				pattern |= Sides.LEFT;
				rt3 = trs.get(neighbors[3].tile);
			}
			RoadType rt5 = null;
			if (neighbors[5] != null && neighbors[5].type == SurfaceEntityType.ROAD) {
				pattern |= Sides.RIGHT;
				rt5 = trs.get(neighbors[5].tile);
			}
			RoadType rt7 = null;
			if (neighbors[7] != null && neighbors[7].type == SurfaceEntityType.ROAD) {
				pattern |= Sides.BOTTOM;
				rt7 = trs.get(neighbors[7].tile);
			}
			RoadType rt = RoadType.get(pattern);
			// place the new tile fragment onto the map
			// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
			se = createRoadEntity(rts.get(rt));
			surface.buildingmap.put(l, se);
			// alter the four neighboring tiles to contain road back to this
			if (rt1 != null) {
				rt1 = RoadType.get(rt1.pattern | Sides.BOTTOM);
				surface.buildingmap.put(l.delta(0, 1), createRoadEntity(rts.get(rt1)));
			}
			if (rt3 != null) {
				rt3 = RoadType.get(rt3.pattern | Sides.RIGHT);
				surface.buildingmap.put(l.delta(-1, 0), createRoadEntity(rts.get(rt3)));
			}
			if (rt5 != null) {
				rt5 = RoadType.get(rt5.pattern | Sides.LEFT);
				surface.buildingmap.put(l.delta(1, 0), createRoadEntity(rts.get(rt5)));
			}
			if (rt7 != null) {
				rt7 = RoadType.get(rt7.pattern | Sides.TOP);
				surface.buildingmap.put(l.delta(0, -1), createRoadEntity(rts.get(rt7)));
			}
			
		}
	}
	/**
	 * Create a road entity for the tile.
	 * @param tile the tile
	 * @return the entity
	 */
	SurfaceEntity createRoadEntity(Tile tile) {
		SurfaceEntity result = new SurfaceEntity();
		result.tile = tile;
		result.tile.alpha = alpha;
		result.type = SurfaceEntityType.ROAD;
		return result;
	}
	/**
	 * Fills the fragment array of the 3x3 rectangle centered around x and y.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param map the map
	 * @param fragments the fragments
	 */
	void setNeighbors(int x, int y, Map<Location, SurfaceEntity> map, SurfaceEntity[] fragments) {
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
	 * Places a road frame around the tilesToHighlight rectangle.
	 * @param rts the road to tile map for a concrete race
	 * @param rect the rectangle to use
	 * @param corners where to place the created corners
	 */
	void addRoadAround(Map<RoadType, Tile> rts, Rectangle rect, Collection<Location> corners) {
		Location la = Location.of(rect.x, rect.y);
		Location lb = Location.of(rect.x + rect.width - 1, rect.y);
		Location lc = Location.of(rect.x, rect.y - rect.height + 1);
		Location ld = Location.of(rect.x + rect.width - 1, rect.y - rect.height + 1);
		
		corners.add(la);
		corners.add(lb);
		corners.add(lc);
		corners.add(ld);
		
		surface.buildingmap.put(la, createRoadEntity(rts.get(RoadType.RIGHT_TO_BOTTOM)));
		surface.buildingmap.put(lb, createRoadEntity(rts.get(RoadType.LEFT_TO_BOTTOM)));
		surface.buildingmap.put(lc, createRoadEntity(rts.get(RoadType.TOP_TO_RIGHT)));
		surface.buildingmap.put(ld, createRoadEntity(rts.get(RoadType.TOP_TO_LEFT)));
		// add linear segments
		
		Tile ht = rts.get(RoadType.HORIZONTAL);
		for (int i = rect.x + 1; i < rect.x + rect.width - 1; i++) {
			surface.buildingmap.put(Location.of(i, rect.y), createRoadEntity(ht));
			surface.buildingmap.put(Location.of(i, rect.y - rect.height + 1), createRoadEntity(ht));
		}
		Tile vt = rts.get(RoadType.VERTICAL);
		for (int i = rect.y - 1; i > rect.y - rect.height + 1; i--) {
			surface.buildingmap.put(Location.of(rect.x, i), createRoadEntity(vt));
			surface.buildingmap.put(Location.of(rect.x + rect.width - 1, i), createRoadEntity(vt));
		}
	}
	/** @return Parse the original planet definitions. */
	Map<String, OriginalPlanet> parseOriginalPlanet() {
		final Map<String, OriginalPlanet> originalPlanets = new LinkedHashMap<String, OriginalPlanet>();
		XElement e = rl.getXML("en", "campaign/main/planets_old");
		for (XElement planet : e.childrenWithName("planet")) {
			OriginalPlanet op = new OriginalPlanet();
			op.name = planet.get("id");
			op.surfaceType = planet.childValue("type");
			op.location.x = Integer.parseInt(planet.childValue("location-x"));
			op.location.y = Integer.parseInt(planet.childValue("location-y"));
			op.surfaceVariant = Integer.parseInt(planet.childValue("variant"));
			op.race = planet.childValue("race");
			for (XElement building : planet.childElement("buildings").childrenWithName("building")) {
				OriginalBuilding ob = new OriginalBuilding();
				ob.name = building.childValue("id");
				ob.location = Location.of(Integer.parseInt(building.childValue("x")), 
						Integer.parseInt(building.childValue("y")));
				op.buildings.add(ob);
			}
			originalPlanets.put(op.name, op);
		}
		return originalPlanets;
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
		// TODO add code
		if (b != null) {
			buildingsPanel.preview.building = b.tileset.preview;
			buildingsPanel.preview.cost = b.type.cost;
			buildingsPanel.buildingName.text(commons.labels().get(b.type.label));
			
			buildingInfoPanel.buildingInfoName.text(buildingsPanel.buildingName.text());
			buildingInfoPanel.hideStates();
//			b.hitpoints = b.type.hitpoints * 3 / 4;
			
			if (b.isConstructing()) {
				buildingInfoPanel.energy.text("-");
				buildingInfoPanel.energyPercent.text("-");
				buildingInfoPanel.worker.text("-");
				buildingInfoPanel.workerPercent.text("-");
				buildingInfoPanel.operationPercent.text("-");
				buildingInfoPanel.production.text("-");
				
				buildingInfoPanel.constructing.visible(true);
				buildingInfoPanel.progressLower.visible(true);
				buildingInfoPanel.progressLower.text(Integer.toString(b.buildProgress * 100 / b.type.hitpoints));
				
				if (b.isDamaged()) {
					buildingInfoPanel.damaged.visible(true);
					buildingInfoPanel.progressUpper.visible(true);
					if (b.hitpoints > 0) {
						buildingInfoPanel.progressUpper.text(Integer.toString((b.hitpoints - b.buildProgress) * 100 / b.hitpoints));
					} else {
						buildingInfoPanel.progressUpper.text("0");
					}
				} else {
					buildingInfoPanel.undamaged.visible(true);
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
						buildingInfoPanel.production.text(((int)b.getResource(b.type.primary)) + getUnit(b.type.primary));
					} else {
						buildingInfoPanel.production.text("");
					}
				}
				if (!b.enabled) {
					buildingInfoPanel.stateOffline.visible(true);
				} else {
					if (b.isDamaged()) {
						buildingInfoPanel.stateDamaged.visible(true);
					} else {
						buildingInfoPanel.undamaged.visible(true);
						if (b.isEnergyShortage()) {
							buildingInfoPanel.stateNoEnergy.visible(true);
						} else
						if (b.isWorkerShortage()) {
							buildingInfoPanel.stateInactive.visible(true);
						}
					}
				}
					
				if (b.isDamaged()) {
					if (b.repairing) {
						buildingInfoPanel.repairing.visible(true);
					} else {
						buildingInfoPanel.damaged.visible(true);
					}
					buildingInfoPanel.progressUpper.visible(true);
					buildingInfoPanel.progressUpper.text(Integer.toString((b.type.hitpoints - b.hitpoints) * 100 / b.type.hitpoints));
				}
			}
			
		} else {
//			buildingsPanel.preview.building = null;
//			buildingsPanel.buildingName.text("");
			buildingInfoPanel.hideStates();
			buildingInfoPanel.buildingInfoName.text("-");
			buildingInfoPanel.energy.text("-");
			buildingInfoPanel.energyPercent.text("-");
			buildingInfoPanel.worker.text("-");
			buildingInfoPanel.workerPercent.text("-");
			buildingInfoPanel.operationPercent.text("-");
			buildingInfoPanel.production.text("-");
			buildingInfoPanel.undamaged.visible(true);
			buildingInfoPanel.stateInactive.visible(true);
		}
	}
	/**
	 * Return the unit label for the given resource type.
	 * @param type the resource type
	 * @return the unit string
	 */
	String getUnit(String type) {
		return commons.labels().get("building.resource.type." + type);
	}
	/** The building preview component. */
	class BuildingPreview extends UIComponent {
		/** The building image. */
		public BufferedImage building;
		/** Building can be built. */
		public boolean enabled;
		/** The building cost. */
		public int cost;
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
		UIImageButton build;
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
			build = new UIImageButton(commons.colony().build);
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
			
			addThis();
		}
		@Override
		public void draw(Graphics2D g2) {
			g2.drawImage(commons.colony().buildingsPanel, 0, 0, null);
			super.draw(g2);
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
		UIImageButton active;
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
			active = new UIImageButton(commons.colony().active);
			stateOffline = new UIImageButton(commons.colony().statusOffline);
			constructing = new UIImageButton(commons.colony().constructing);
			damaged = new UIImageButton(commons.colony().damaged);
			repairing = new UIImageButton(commons.colony().repairing);
			undamaged = new UIImageButton(commons.colony().undamaged);

			damaged.location(8, 98);
			repairing.location(8, 98);
			undamaged.location(8, 98);

			active.location(8, 122);
			stateOffline.location(8, 122);
			stateInactive.location(8, 122);
			stateNoEnergy.location(8, 122);
			stateDamaged.location(8, 122);
			constructing.location(8, 122);

			
			progressUpper = new UILabel("-", 7, commons.text());
			progressUpper.bounds(8 + 96, 98 + 3, 28, 10);
			progressUpper.visible(false);
			
			progressLower = new UILabel("-", 7, commons.text());
			progressLower.bounds(8 + 96, 122 + 3, 28, 10);
			progressLower.visible(false);
			
			demolish = new UIImageButton(commons.colony().demolish);
			demolish.location(161, 50);

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
			stateDamaged.visible(false);
			stateInactive.visible(false);
			stateNoEnergy.visible(false);
			stateOffline.visible(false);
			
			
			constructing.visible(false);
			damaged.visible(false);
			repairing.visible(false);
			active.visible(false);
			progressUpper.visible(false);
			progressLower.visible(false);
			undamaged.visible(false);
		}
	}
	/** Perform the animation. */
	void doAnimation() {
		//wrap animation index
		if (animation == Integer.MAX_VALUE) {
			animation = -1;
		}
		animation++;
		boolean blink0 = blink;
		blink = (animation % 10) >= 5;
		if (blink0 != blink || (animation % 3 == 0)) {
			askRepaint();
		}
	}
	@Override
	public void onInitialize() {
		surface = new PlanetSurface();
		surface.width = 33;
		surface.height = 66;
		surface.computeRenderingLocations();
		animationTimer = new Timer(100, new Act() {
			@Override
			public void act() {
				doAnimation();
			}
		});
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
		
		sidebarNavigation.onClick = new Act() {
			@Override
			public void act() {
				colonyInfo.visible(!colonyInfo.visible());
				bridge.visible(!bridge.visible());
				planets.visible(!planets.visible());
				starmap.visible(!starmap.visible());
			}
		};
		sidebarRadar.onClick = new Act() {
			@Override
			public void act() {
				radar.visible(!radar.visible());
				radarPanel.visible(!radarPanel.visible());
			}
		};
		sidebarBuildingInfo.onClick = new Act() {
			@Override
			public void act() {
				buildingInfoPanel.visible(!buildingInfoPanel.visible());
			}
		};
		sidebarBuildings.onClick = new Act() {
			@Override
			public void act() {
				buildingsPanel.visible(!buildingsPanel.visible());
			}
		};
		colonyInfo.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displaySecondary(Screens.INFORMATION);
			}
		};
		planets.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displaySecondary(Screens.INFORMATION);
			}
		};
		starmap.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displayPrimary(Screens.STARMAP);
			}
		};
		bridge.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displayPrimary(Screens.BRIDGE);
			}
		};
		
		radarPanel = new UIImage(commons.colony().radarPanel);
		radar = new RadarRender();
		radar.z = 1;
		
		buildingsPanel = new BuildingsPanel();
		buildingsPanel.z = 1;
		buildingInfoPanel = new BuildingInfoPanel();
		buildingInfoPanel.z = 1;
		
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
		
	}
}
