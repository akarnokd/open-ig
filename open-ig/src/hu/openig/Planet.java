/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import hu.openig.gfx.TextGFX;
import hu.openig.utils.PACFile;
import hu.openig.utils.PCXImage;
import hu.openig.utils.PACFile.PACEntry;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

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
	 * Main test program
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String root = "c:\\games\\ig\\";
		Map<String, PACEntry> colony1 = PACFile.mapByName(PACFile.parseFully(root + "data\\colony1.pac"));
		BufferedImage back1 = PCXImage.parse(colony1.get("A.PCX").data, -2);

		BufferedImage hub1 = PCXImage.parse(colony1.get("000.PCX").data, 0);
		BufferedImage hub1B = PCXImage.parse(colony1.get("000B.PCX").data, 0);
		BufferedImage hub2 = PCXImage.parse(colony1.get("000.PCX").data, 0);
		BufferedImage hub2B = PCXImage.parse(colony1.get("000B.PCX").data, 0);
		
		Rectangle r1 = new Rectangle(50, 0, hub1.getWidth(), hub2.getHeight());
		Rectangle r2 = new Rectangle(0, 50, hub1.getWidth(), hub2.getHeight());
		BufferedImage gfx = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		
		
		
		Graphics2D g2 = (Graphics2D)gfx.getGraphics();
		
		TextGFX txt = new TextGFX(root + "gfx\\charset1.pcx");
		
		int y = - gfx.getWidth() * 3;
		int x = -gfx.getHeight() * 2;
		while (y < gfx.getHeight()) {
			int y0 = y;
			int x0 = x;
			while(x < gfx.getWidth()) {
				g2.drawImage(back1, x, y, null);
				g2.drawImage(back1, x + back1.getWidth() / 2, y + back1.getHeight() / 2 + 2, null);
				x += back1.getWidth() + 2;
				y += 3;
			}
			y = y0 + back1.getHeight();
			x = x0;
			x -= 2;
		}
		
		g2.drawImage(hub1, r1.x, r1.y, null);
		g2.drawImage(hub1B, r1.x + hub1.getWidth(), r1.y, null);
		g2.drawImage(hub2, r2.x, r2.y, null);
		g2.drawImage(hub2B, r2.x + hub2.getWidth(), r2.y, null);

		Rectangle r3 = r1.intersection(r2);
		for (y = r3.y; y < r3.y + r3.height; y++) {
			for (x = r3.x; x < r3.x + r3.width; x++) {
				int c1 = hub1.getRGB(x - r1.x, y - r1.y);
				int c2 = hub2.getRGB(x - r2.x, y - r2.y);
				if ((c1 & 0xFF000000) != 0 && (c2 & 0xFF000000) != 0) {
					int c3 = average(c2, 0xFF0000, 0.75f);
					gfx.setRGB(x, y, 0xFF000000 | c3);
				}
			}
		}
		txt.paintTo((Graphics2D)gfx.getGraphics(), 0, 350, 7, 0xFF000000, "ÁRVÍZTÛRÕ TÜKÖRFÚRÓGÉP árvíztûrõ tükörfúrógép");
		txt.paintTo((Graphics2D)gfx.getGraphics(), 0, 360, 10, 0xFF000000, "ÁRVÍZTÛRÕ TÜKÖRFÚRÓGÉP árvíztûrõ tükörfúrógép");
		txt.paintTo((Graphics2D)gfx.getGraphics(), 0, 380, 14, 0xFFFF0000, "ÁRVÍZTÛRÕ TÜKÖRFÚRÓGÉP árvíztûrõ tükörfúrógép");
		txt.paintTo((Graphics2D)gfx.getGraphics(), 0, 400, 5, 0xFF000000, "ÁRVÍZTÛRÕ TÜKÖRFÚRÓGÉP árvíztûrõ tükörfúrógép");
		
		JFrame fm = new JFrame("Overlap test");
		fm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fm.getContentPane().add(new JLabel(new ImageIcon(gfx)));
		fm.pack();
		fm.setVisible(true);
	}

}
