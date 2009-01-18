/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import hu.openig.core.Tile;
import hu.openig.utils.PACFile;
import hu.openig.utils.PCXImage;
import hu.openig.utils.PACFile.PACEntry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Planetary surface renderer test file.
 * @author karnokd, 2009.01.14.
 * @version $Revision 1.0$
 */
public class Planet {
	/** 
	 * Calculates the RGB average of two RGB color.
	 * @param c1 the first color
	 * @param c2 the second color
	 * @param scale the weight (0..1) of the first color
	 * @return the averaged RGB color
	 */
	protected static int average(int c1, int c2, float scale) {
		int r = (int)((c1 & 0xFF0000) * scale + (c2 & 0xFF0000) * (1 - scale));
		int g = (int)((c1 & 0xFF00) * scale + (c2 & 0xFF00) * (1 - scale));
		int b = (int)((c1 & 0xFF) * scale  + (c2 & 0xFF) * (1 - scale));
		return (r & 0xFF0000) | (g & 0xFF00) | (b & 0xFF);
	}
	/**
	 * A point coordinate on the planetary screen.
	 * @author karnokd
	 */
	public static class Pt {
		/** The X coordinate. */
		public int x;
		/** The Y coordinate. */
		public int y;
		/**
		 * Constructor.
		 * @param x
		 * @param y
		 */
		public Pt(int x, int y) {
			this.x = x;
			this.y = 0;
		}
		public void add(Pt p) {
			x += p.x;
			y += p.y;
		}
		/** Convert to screen coordinates. */
		public Pt toScreen() {
			return new Pt(x * 30 - y * 26, - 12 * x - 15 * y);
		}
		/** Convert to tile coordinates. */
		public Pt toTile() {
			return new Pt(0, 0);
		}
		public static int toScreenX(int x, int y) {
			return x * 30 - y * 28;
		}
		public static int toScreenY(int x, int y) {
			return - 12 * x - 15 * y;
		}
		public static float toTileX(int x, int y) {
			return (x + toTileY(x, y) * 28) / 30f;
		}
		public static float toTileY(int x, int y) {
			return -(30 * y + 12 * x) / 786f;
		}
	}
	/**
	 * Planet surface renderer class.
	 * @author karnokd, 2009.01.16.
	 * @version $Revision 1.0$
	 */
	public static class PlanetRenderer extends JComponent implements MouseMotionListener, MouseWheelListener {
		/** */
		private static final long serialVersionUID = -2113448032455145733L;
		BufferedImage back1;
		BufferedImage keret1;
		BufferedImage keret2;
		BufferedImage keret3;
		BufferedImage keret4;
		BufferedImage hub1;
		BufferedImage hub1B;
		Rectangle tilesToHighlight;
		int xoff;
		int yoff;
		int lastx;
		int lasty;
		boolean panMode;
		Map<Integer, Map<Integer, Tile>> surfaceImages;
		byte[] mapBytes;
		int surfaceVariant = 1;
		Map<String, PACEntry> maps;
		float scale = 1.0f;
		int surfaceType = 7;
		/** Empty surface map array. */
		private final byte[] EMPTY_SURFACE_MAP = new byte[65 * 65 * 2 + 4];
		public PlanetRenderer(String root) throws IOException {
			Map<String, PACEntry> colony1 = PACFile.mapByName(PACFile.parseFully(root + "/DATA/COLONY1.PAC"));
			maps = PACFile.mapByName(PACFile.parseFully(root + "/DATA/MAP.PAC"));

			surfaceImages = new HashMap<Integer, Map<Integer, Tile>>();
			for (int i = 1; i < 8; i++) {
				Map<Integer, Tile> actual = new HashMap<Integer, Tile>();
				surfaceImages.put(i, actual);
				for (PACEntry e : PACFile.parseFully(root + "/DATA/FELSZIN" + i +".PAC")) {
					int idx = e.filename.indexOf('.');
					Tile t = new Tile();
					t.image = PCXImage.parse(e.data, -2);
					actual.put(Integer.parseInt(e.filename.substring(0, idx)), t);
				}
			}
			adjustTileParams();
			
			changeSurface();
			
			back1 = surfaceImages.get(1).get(27).image;

			BufferedImage keretek = PCXImage.from(root + "/GFX/KERET.PCX", -2);
			keret1 = keretek.getSubimage(0, 0, 57, 28);
			keret2 = keretek.getSubimage(58, 0, 57, 28);
			keret3 = keretek.getSubimage(116, 0, 57, 28);
			keret4 = keretek.getSubimage(174, 0, 57, 28);
			
			hub1 = PCXImage.parse(colony1.get("000.PCX").data, 0);
			hub1B = PCXImage.parse(colony1.get("000B.PCX").data, 0);
			addMouseMotionListener(this);
			addMouseWheelListener(this);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					doMousePressed(e);
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					doMouseReleased(e);
				}
			});
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					doComponentResized(e);
				}
			});
//			setOpaque(true);
		}
		private PACEntry getSurface(int surfaceType, int variant) {
			String mapName = "MAP_" + (char)('A' + (surfaceType - 1)) + variant + ".MAP";
			return maps.get(mapName);
		}
		/**
		 * Fixes width and heigth for larger tiles.
		 */
		private void adjustTileParams() {
			// DESERT TYPE SURFACE TILE ADJUSTMENTS
			for (int i = 55; i <= 68; i++) {
				setParams(1, i, 2, 2);
			}
			setParams(1, 69, 2, 1);
			setParams(1, 70, 3, 2);
			for (int i = 71; i <= 74; i++) {
				setParams(1, i, 3, 3);
			}
			setParams(1, 75, 4, 3);
			for (int i = 76; i <= 79; i++) {
				setParams(1, i, 4, 4);
			}
			// ICE TYPE SURFACE TILE ADJUSTMENTS
			for (int i = 33; i <= 37; i++) {
				setParams(2, i, 1, 2);
			}
			for (int i = 38; i <= 39; i++) {
				setParams(2, i, 2, 1);
			}
			for (int i = 40; i <= 54; i++) {
				setParams(2, i, 2, 2);
			}
			for (int i = 55; i <= 59; i++) {
				setParams(2, i, 2, 3);
			}
			for (int i = 60; i <= 62; i++) {
				setParams(2, i, 2, 4);
			}
			for (int i = 63; i <= 67; i++) {
				setParams(2, i, 3, 2);
			}
			setParams(2, 68, 3, 3);
			for (int i = 69; i <= 73; i++) {
				setParams(2, i, 4, 2);
			}
			for (int i = 74; i <= 78; i++) {
				setParams(2, i, 4, 4);
			}
			setParams(2, 79, 2, 2);
			// CRATER TYPE SURFACE TILE ADJUSTMENTS
			setParams(3, 61, 1, 2);
			setParams(3, 62, 2, 1);
			for (int i = 63; i <= 86; i++) {
				setParams(3, i, 2, 2);
			}
			for (int i = 87; i <= 89; i++) {
				setParams(3, i, 2, 3);
			}
			setParams(3, 91, 3, 2);
			for (int i = 92; i <= 94; i++) {
				setParams(3, i, 3, 3);
			}
			setParams(3, 95, 3, 4);
			setParams(3, 96, 4, 2);
			setParams(3, 97, 4, 4);
			// ROCKY TYPE SURFACE TILE ADJUSTMENTS
			for (int i = 42; i <= 45; i++) {
				setParams(4, i, 1, 2);
			}
			for (int i = 46; i <= 58; i++) {
				setParams(4, i, 2, 2);
			}
			for (int i = 59; i <= 64; i++) {
				setParams(4, i, 2, 4);
			}
			for (int i = 65; i <= 71; i++) {
				setParams(4, i, 3, 3);
			}
			for (int i = 72; i <= 73; i++) {
				setParams(4, i, 4, 4);
			}
			surfaceImages.get(4).get(72).heightCorrection = 1;
			setParams(4, 74, 5, 5);
			// LIQUID TYPE SURFACE TILE ADJUSTMENTS
			for (int i = 70; i <= 72; i++) {
				setParams(5, i, 1, 2);
			}
			setParams(5, 73, 2, 1);
			for (int i = 74; i <= 79; i++) {
				setParams(5, i, 2, 2);
			}
			for (int i = 86; i <= 103; i++) {
				setParams(5, i, 2, 2);
			}
			for (int i = 104; i <= 105; i++) {
				setParams(5, i, 3, 3);
			}
			setParams(5, 106, 2, 3);
			for (int i = 107; i <= 109; i++) {
				setParams(5, i, 3, 2);
			}
			for (int i = 110; i <= 116; i++) {
				setParams(5, i, 3, 3);
			}
			setParams(5, 117, 4, 3);
			setParams(5, 118, 3, 3);
			for (int i = 119; i <= 121; i++) {
				setParams(5, i, 4, 4);
			}
			for (int i = 122; i <= 123; i++) {
				setParams(5, i, 3, 3);
			}
			
			// EARTH TYPE SURFACE TILE ADJUSTMENTS
			setParams(6, 108, 2, 1);
			for (int i = 109; i <= 130; i++) {
				setParams(6, i, 2, 2);
			}
			for (int i = 131; i <= 139; i++) {
				setParams(6, i, 2, 3);
			}
			for (int i = 140; i <= 147; i++) {
				setParams(6, i, 3, 2);
			}
			for (int i = 148; i <= 149; i++) {
				setParams(6, i, 3, 3);
			}
			for (int i = 150; i <= 158; i++) {
				setParams(6, i, 4, 4);
			}
			surfaceImages.get(6).get(157).heightCorrection = 1;
			setParams(6, 159, 5, 3);
			setParams(6, 160, 6, 5);
			
			setParams(6, 161, 6, 6);
			// NECTOPLASM TYPE SURFACE TILE ADJUSTMENTS
			for (int i = 27; i <= 42; i++) {
				setParams(7, i, 2, 2);
			}
			setParams(7, 43, 2, 3);
			setParams(7, 44, 3, 2);
			setParams(7, 45, 3, 3);
			for (int i = 46; i <= 48; i++) {
				setParams(7, i, 3, 2);
			}
			for (int i = 49; i <= 57; i++) {
				setParams(7, i, 4, 4);
			}
			surfaceImages.get(7).get(57).heightCorrection = 1;
		}
		private void setParams(int surface, int tile, int width, int height) {
			Tile t = surfaceImages.get(surface).get(tile);
			t.width = width;
			t.height = height;
			t.scanlines = width + height - 1;
			t.strips = new BufferedImage[width + height - 1];
			for (int i = 0; i < t.strips.length; i++) {
				int x0 = i >= t.width ? Pt.toScreenX(i, 0) : Pt.toScreenX(0, -i);
				int w0 = Math.min(57, t.image.getWidth() - x0);
				t.strips[i] = t.image.getSubimage(x0, 0, w0, t.image.getHeight());
			}
		}
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			g2.scale(scale, scale);
			g2.setColor(Color.RED);
//			g2.fill(this.getBounds());
			Rectangle r = new Rectangle();
			int k = 0;
			int j = 0;
			// RENDER VERTICALLY
			int k0 = 0;
			int j0 = 0;
			Map<Integer, Tile> surface = surfaceImages.get(surfaceType);
			for (int i = 0; i < 65 * 65; i++) {
				int ii = (mapBytes[2 * i + 4] & 0xFF) - (surfaceType < 7 ? 41 : 84);
				int ff = mapBytes[2 * i + 5] & 0xFF;
				Tile tile = surface.get(ii);
				if (tile != null) {
					// 1x1 tiles can be drawn from top to bottom
					if (tile.width == 1 && tile.height == 1) {
						int x = xoff + Pt.toScreenX(k, j);
						int y = yoff + Pt.toScreenY(k, j);
						if (x >= -tile.image.getWidth() && x <= (int)(getWidth() / scale)
								&& y >= -tile.image.getHeight() && y <= (int)(getHeight() / scale) + tile.image.getHeight()) {
							g2.drawImage(tile.image, x, y - tile.image.getHeight() + tile.heightCorrection, null);
						}
					} else 
					if (ff < 255){
						// multi spanning tiles should be cut into small rendering piece for the current strip
						// ff value indicates the stripe count
						// the entire image would be placed using this bottom left coordinate
						int j1 = ff >= tile.width ? j + tile.width - 1: j + ff;
						int k1 = ff >= tile.width ? k + (tile.width - 1 - ff): k;
						int j2 = ff >= tile.width ? j : j - (tile.width - 1 - ff);
						int x = xoff + Pt.toScreenX(k1, j1);
						int y = yoff + Pt.toScreenY(k1, j2);
						// use subimage stripe
						int x0 = ff >= tile.width ? Pt.toScreenX(ff, 0) : Pt.toScreenX(0, -ff);
						BufferedImage subimage = tile.strips[ff];
						g2.drawImage(subimage, x + x0, y - tile.image.getHeight() + tile.heightCorrection, null);
						r.x = x;
						r.y = y - tile.image.getHeight();
						r.width = tile.image.getWidth();
						r.height = tile.image.getHeight();
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
			// RENDER HORIZONTALLY
			/*
			for (int sy = 0; sy < 132; sy++) {
				boolean iseven = sy % 2 == 0;
				j = - (sy / 2);
				k = - (sy + 1) / 2;
				for (int sx = 0; sx < 33; sx++) {
					int i = iseven ? 65 : 0;
					i += sx * 130 + sy / 2;
					if (i * 2 + 5 >= mapBytes.length) {
						continue;
					}
					int ii = (mapBytes[2 * i + 4] & 0xFF) - 41;
					int ff = mapBytes[2 * i + 5] & 0xFF;
					Tile tile = surfaceImages.get(6).get(ii);
					if (tile != null && tile.width == 1 && tile.height == 1) {
						if (ff == tile.width - 1) {
							int x = xoff + Pt.toScreenX(k, j + tile.width - 1);
							int y = yoff + Pt.toScreenY(k, j);
							if (x >= -tile.image.getWidth() && x <= getWidth()
									&& y >= -tile.image.getHeight() && y <= getHeight() + tile.image.getHeight()) {
								g2.drawImage(tile.image, x, y - tile.image.getHeight() + tile.heightCorrection, null);
							}
						}
					}
					k++;
					j--;
				}
			}
			*/
//			if (r != null) {
//				g2.draw(r);
//			}
			
			if (tilesToHighlight != null) {
				drawIntoRect(g2, keret1, tilesToHighlight);
			}
			
		}
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
					int x = xoff + Pt.toScreenX(k, j); 
					int y = yoff + Pt.toScreenY(k, j); 
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
		@Override
		public void mouseMoved(MouseEvent e) {
			int x = e.getX() - xoff - 27;
			int y = e.getY() - yoff + 1;
			int a = (int)Math.floor(Pt.toTileX(x, y));
			int b = (int)Math.floor(Pt.toTileY(x, y));
			tilesToHighlight = new Rectangle(a, b, 1, 1);
			repaint();
		}
		public void doMousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				lastx = e.getX();
				lasty = e.getY();
				panMode = true;
			} else
			if (e.getButton() == MouseEvent.BUTTON1) {
				int x = e.getX() - xoff - 27;
				int y = e.getY() - yoff + 1;
				int a = (int)Math.floor(Pt.toTileX(x, y));
				int b = (int)Math.floor(Pt.toTileY(x, y));
				int offs = this.toMapOffset(a, b);
				int val = offs >= 0 && offs < 65 * 65 ? mapBytes[offs * 2 + 4] & 0xFF : 0;
				System.out.printf("%d, %d -> %d, %d%n", a, b, offs, val);
			}
		}
		public void doMouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				panMode = false;
			}
		}
		boolean once = true;
		private void doComponentResized(ComponentEvent e) {
			if (once) {
				xoff = 0; //getWidth() / 2;
				yoff = 27; //getHeight() - 1;
				once = false;
			}
		}
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
	}
	/**
	 * Main test program
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String root = ".";
		if (args.length > 0) {
			root = args[0];
		}
		final PlanetRenderer pr = new PlanetRenderer(root);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
				public void run() {
					JFrame fm = new JFrame("Open-IG: Planet");
					fm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					fm.getContentPane().add(pr);
					fm.setMinimumSize(new Dimension(640, 480));
					fm.pack();
					fm.setVisible(true);
				}
		});
	}

}
