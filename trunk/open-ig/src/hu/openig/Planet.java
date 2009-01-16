/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Planetary surface renderer.
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
	private static int average(int c1, int c2, float scale) {
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
	public static class PlanetRenderer extends JComponent implements MouseMotionListener {
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
		public PlanetRenderer(String root) throws IOException {
			Map<String, PACEntry> colony1 = PACFile.mapByName(PACFile.parseFully(root + "data\\colony1.pac"));
			Map<String, PACEntry> surface1 = PACFile.mapByName(PACFile.parseFully(root + "data\\felszin1.pac"));
			back1 = PCXImage.parse(surface1.get("021.PCX").data, -2);

			BufferedImage keretek = PCXImage.from(root + "gfx\\keret.pcx", -2);
			keret1 = keretek.getSubimage(0, 0, 57, 28);
			keret2 = keretek.getSubimage(58, 0, 57, 28);
			keret3 = keretek.getSubimage(116, 0, 57, 28);
			keret4 = keretek.getSubimage(174, 0, 57, 28);
			
			hub1 = PCXImage.parse(colony1.get("000.PCX").data, 0);
			hub1B = PCXImage.parse(colony1.get("000B.PCX").data, 0);
			addMouseMotionListener(this);
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
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			int maxx = 60;
			int maxy = 60;
			for (int j = 0; j < maxy; j++) {
				for (int k = 0; k < maxx; k++) {
					int x = xoff + Pt.toScreenX(k, j); //k * 30 - j * 27;
					int y = yoff + Pt.toScreenY(k, j); //12 * k - 15 * j;
					if (x >= -back1.getWidth() && x <= getWidth()
							&& y >= -back1.getHeight() && y <= getHeight() + back1.getHeight()) {
						g2.drawImage(back1, x, y - back1.getHeight(), null);
					}
				}
			}
			if (tilesToHighlight != null) {
				for (int j = tilesToHighlight.y; j < tilesToHighlight.y + tilesToHighlight.height; j++) {
					for (int k = tilesToHighlight.x; k < tilesToHighlight.x + tilesToHighlight.width; k++) {
						int x = xoff + Pt.toScreenX(k, j); //k * 30 - j * 27;
						int y = yoff + Pt.toScreenY(k, j); //12 * k - 15 * j;
						
						g2.drawImage(keret1, x - 1, y - keret1.getHeight(), null);
					}
				}
			}
			
			int hx = 0, hy = 0;
			Rectangle r1 = new Rectangle(xoff + Pt.toScreenX(0 + hx, 6 + hy), yoff + Pt.toScreenY(hx, hy) - hub1.getHeight(), 
					hub1.getWidth() - 1, hub1.getHeight() - 1);
//			g2.draw(r1);
			g2.drawImage(hub1, r1.x, r1.y, null);
			g2.drawImage(hub1B, r1.x + hub1.getWidth(), r1.y, null);
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
				xoff = getWidth() / 2;
				yoff = getHeight() - 1;
				once = false;
			}
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
