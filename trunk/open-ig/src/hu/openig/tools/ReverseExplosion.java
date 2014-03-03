/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Reverse an explosion image.
 * @author akarnokd, 2011.09.08.
 */
public final class ReverseExplosion {
	/** Utility class. */
	private ReverseExplosion() {
		// utility class
	}
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		BufferedImage img = ImageIO.read(new File("images/generic/groundwar/rocket_explosions.png"));
		
		
		int h = img.getHeight();
		int w = img.getWidth();
		int nw = w / h;
		
		BufferedImage img2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img2.createGraphics();
		
		for (int i = 0; i < nw; i++) {
			g2.drawImage(img, i * h, 0, i * h + h - 1, h - 1, (nw - i - 1) * h, 0, (nw - i - 1) * h + h - 1, h - 1, null);
		}
		
		g2.dispose();
		
		ImageIO.write(img2, "png", new File("images/generic/groundwar/rocket_explosions2.png"));
	}

}
