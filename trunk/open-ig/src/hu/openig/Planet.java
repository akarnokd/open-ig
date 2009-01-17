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
		int type = 1;
		Map<String, PACEntry> maps;
		public PlanetRenderer(String root) throws IOException {
			Map<String, PACEntry> colony1 = PACFile.mapByName(PACFile.parseFully(root + "data\\colony1.pac"));
			maps = PACFile.mapByName(PACFile.parseFully(root + "DATA\\MAP.PAC"));

			surfaceImages = new HashMap<Integer, Map<Integer, Tile>>();
			for (int i = 1; i < 8; i++) {
				Map<Integer, Tile> actual = new HashMap<Integer, Tile>();
				surfaceImages.put(i, actual);
				for (PACEntry e : PACFile.parseFully(root + "DATA\\FELSZIN" + i +".PAC")) {
					int idx = e.filename.indexOf('.');
					Tile t = new Tile();
					t.image = PCXImage.parse(e.data, -2);
					actual.put(Integer.parseInt(e.filename.substring(0, idx)), t);
				}
			}
			adjustTileParams();
			
			mapBytes = maps.get("MAP_F" + type + ".MAP").data;
			
			back1 = surfaceImages.get(1).get(27).image;

			BufferedImage keretek = PCXImage.from(root + "gfx\\keret.pcx", -2);
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
		}
		/**
		 * Fixes width and heigth for larger tiles.
		 */
		private void adjustTileParams() {
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
		}
		private void setParams(int surface, int tile, int width, int height) {
			Tile t = surfaceImages.get(surface).get(tile);
			t.width = width;
			t.height = height;
			t.scanlines = width + height - 1;
		}
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			int k = 0;
			int j = 0;
			int k0 = 0;
			int j0 = 0;
			for (int i = 0; i < /* mapBytes.length / 2 - 2 */ 65 * 65; i++) {
				int ii = (mapBytes[2 * i + 4] & 0xFF) - 41;
				int ff = mapBytes[2 * i + 5] & 0xFF;
				Tile tile = surfaceImages.get(6).get(ii);
				if (tile != null) {
					if (ff == tile.width - 1) {
						int x = xoff + Pt.toScreenX(-k, j + tile.width - 1);
						int y = yoff + Pt.toScreenY(-k, j);
						if (x >= -tile.image.getWidth() && x <= getWidth()
								&& y >= -tile.image.getHeight() && y <= getHeight() + tile.image.getHeight()) {
							g2.drawImage(tile.image, x, y - tile.image.getHeight() + tile.heightCorrection, null);
						}
					}
//					if (corr > 1 && ff < 255) {
//						int x = xoff + Pt.toScreenX(-k, j);
//						int y = yoff + Pt.toScreenY(-k, j);
//						g2.drawImage(keret3, x - 1, y - keret3.getHeight(), null);
//					}
				}				
				k++;
				j--;
				k0++;
				if (k0 > 64) {
					k0 = 0;
					j0++;
					j = - (j0 / 2);
					k = - ((j0 - 1) / 2 + 1);
				}
			}
			if (tilesToHighlight != null) {
				for (j = tilesToHighlight.y; j < tilesToHighlight.y + tilesToHighlight.height; j++) {
					for (k = tilesToHighlight.x; k < tilesToHighlight.x + tilesToHighlight.width; k++) {
						int x = xoff + Pt.toScreenX(k, j); //k * 30 - j * 27;
						int y = yoff + Pt.toScreenY(k, j); //12 * k - 15 * j;
						
						g2.drawImage(keret1, x - 1, y - keret1.getHeight(), null);
					}
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
			if (e.getWheelRotation() > 0 & type < 9) {
				type++;
			} else 
			if (e.getWheelRotation() < 0 && type > 1){
				type--;
			}
			PACEntry pe = maps.get("MAP_F" + type + ".MAP");
			if (pe != null) {
				mapBytes = pe.data;
			} else {
				mapBytes = new byte[65 * 65 * 2 + 4];
			}
			repaint();
		}
	}
	/**
	 * Main test program
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String root = "c:\\games\\ig\\";
		
		PlanetRenderer pr = new PlanetRenderer(root);
		
		JFrame fm = new JFrame("Open-IG: Planet");
		fm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fm.getContentPane().add(pr);
		fm.setMinimumSize(new Dimension(640, 480));
		fm.pack();
		fm.setVisible(true);
	}

}
