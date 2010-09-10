/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.v1.editors;

import hu.openig.core.Location;
import hu.openig.v1.core.Tile;
import hu.openig.v1.model.PlanetSurface;
import hu.openig.v1.model.SurfaceEntity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/** The map renderer. */
public class MapRenderer extends JComponent {
	/** */
	private static final long serialVersionUID = 5058274675379681602L;
	/** The planet surface definition. */
	PlanetSurface surface;
	/** The offset X. */
	int offsetX;
	/** The offset Y. */
	int offsetY;
	/** The current location based on the mouse pointer. */
	Location current;
	/** The currently selected locations. */
	final Set<Location> selected = new HashSet<Location>();
	/** The selected rectangular region. */
	Rectangle selectedRectangle;
	/** Show the buildings? */
	boolean showBuildings = true;
	/** The selection tile. */
	Tile selection;
	/** The placement tile for allowed area. */
	Tile areaAccept;
	/** The placement tile for denied area. */
	Tile areaDeny;
	/** The current cell tile. */
	Tile areaCurrent;
	/** Right click-drag. */
	MouseAdapter ma = new MouseAdapter() {
		int lastX;
		int lastY;
		boolean drag;
		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				drag = true;
				lastX = e.getX();
				lastY = e.getY();
			} else
			if (SwingUtilities.isMiddleMouseButton(e)) {
				offsetX = 0;
				offsetY = 0;
				repaint();
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				drag = false;
			}
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			if (drag) {
				offsetX += e.getX() - lastX;
				offsetY += e.getY() - lastY;
				
				lastX = e.getX();
				lastY = e.getY();
				repaint();
			}
		}
		@Override
		public void mouseMoved(MouseEvent e) {
			current = getLocationAt(e.getX(), e.getY());
			repaint();
		}
	};
	/** Selection handler. */
	MouseAdapter sma = new MouseAdapter() {
		boolean sel;
		Location orig;
		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && surface != null) {
				sel = true;
				selectedRectangle = new Rectangle();
				orig = getLocationAt(e.getX(), e.getY());
				selectedRectangle.x = orig.x;
				selectedRectangle.y = orig.y;
				selectedRectangle.width = 1;
				selectedRectangle.height = 1;
				repaint();
			}
		}			
		@Override
		public void mouseReleased(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				sel = false;
			}
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			if (sel) {
				Location loc = getLocationAt(e.getX(), e.getY());
				current = loc;
				selectedRectangle.x = Math.min(orig.x, loc.x);
				selectedRectangle.y = Math.min(orig.y, loc.y);
				selectedRectangle.width = Math.max(orig.x, loc.x) - selectedRectangle.x + 1;
				selectedRectangle.height = Math.max(orig.y, loc.y) - selectedRectangle.y + 1;
				repaint();
			}
		}
	};
	/** Preset. */
	public MapRenderer() {
		addMouseListener(ma);
		addMouseMotionListener(ma);
		addMouseListener(sma);
		addMouseMotionListener(sma);
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;

		g2.setColor(new Color(96, 96, 96));
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		if (surface == null) {
			return;
		}
		int x0 = surface.baseXOffset;
		int y0 = surface.baseYOffset;

		Rectangle br = surface.boundingRectangle;
		g2.setColor(new Color(128, 128, 128));
		g2.fillRect(br.x + offsetX, br.y + offsetY, br.width, br.height);
		g2.setColor(Color.YELLOW);
		g2.drawRect(br.x + offsetX, br.y + offsetY, br.width, br.height);
		BufferedImage empty = areaAccept.alphaBlendImage();
		Rectangle renderingWindow = new Rectangle(0, 0, getWidth(), getHeight());
		for (int i = 0; i < surface.renderingOrigins.size(); i++) {
			Location loc = surface.renderingOrigins.get(i);
			for (int j = 0; j < surface.renderingLength.get(i); j++) {
				int x = offsetX + x0 + Tile.toScreenX(loc.x - j, loc.y);
				int y = offsetY + y0 + Tile.toScreenY(loc.x - j, loc.y);
				Location loc1 = Location.of(loc.x - j, loc.y);
				SurfaceEntity se = surface.buildingmap.get(loc1);
				if (se == null || !showBuildings) {
					se = surface.basemap.get(loc1);
				}
				if (se != null) {
					int a = loc1.x - se.virtualColumn;
					int b = loc1.y + se.virtualRow - se.bottomRow;
					int yref = offsetY + y0 + Tile.toScreenY(a, b) + 27 - se.tile.imageHeight;
					if (renderingWindow.intersects(x, yref, 57, se.tile.imageHeight)) {
						BufferedImage img = se.getImage();
						if (img != null) {
							g2.drawImage(img, x, yref, null);
						}
					}
				} else {
					if (renderingWindow.intersects(x, y, 57, 27)) {
						g2.drawImage(empty, x, y, null);
					}
				}
			}
		}
		if (selectedRectangle != null) {
			for (int i = selectedRectangle.x; i < selectedRectangle.x + selectedRectangle.width; i++) {
				for (int j = selectedRectangle.y; j < selectedRectangle.y + selectedRectangle.height; j++) {
					int x = offsetX + x0 + Tile.toScreenX(i, j);
					int y = offsetY + y0 + Tile.toScreenY(i, j);
					g2.drawImage(selection.alphaBlendImage(), x, y, null);
				}
			}
		}
		if (current != null) {
			int x = offsetX + x0 + Tile.toScreenX(current.x, current.y);
			int y = offsetY + y0 + Tile.toScreenY(current.x, current.y);
			g2.drawImage(areaCurrent.alphaBlendImage(), x, y, null);
		}
	}
	/**
	 * Get a location based on the mouse coordinates.
	 * @param mx the mouse X coordinate
	 * @param my the mouse Y coordinate
	 * @return the location
	 */
	public Location getLocationAt(int mx, int my) {
		if (surface != null) {
			int mx0 = mx - offsetX - surface.baseXOffset - 28; // Half left
			int my0 = my - offsetY - surface.baseYOffset - 27; // Half up
			int a = (int)Math.floor(Tile.toTileX(mx0, my0));
			int b = (int)Math.floor(Tile.toTileY(mx0, my0));
			return Location.of(a, b);
		}
		return null;
	}
}
