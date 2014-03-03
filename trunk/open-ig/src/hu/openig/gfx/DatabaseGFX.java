/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.model.ResourceLocator;
import hu.openig.utils.ImageUtils;

import java.awt.image.BufferedImage;

/**
 * The database graphics manager.
 * @author akarnokd, 2009.10.25.
 */
public class DatabaseGFX {
	/** The background picture. */
	public BufferedImage background;
	/** The phases of up arrow: normal, highlight, down. */
	public BufferedImage[] arrowUp;
	/** The phases of down arrow: normal, highlight, down. */
	public BufferedImage[] arrowDown;
	/** The four edges of a picture zone. top-left, top-right, bottom-left, bottom-right. */
	public BufferedImage[] pictureEdge;
	/** Record message. */
	public BufferedImage[] recordMessage;
	/** Aliens button. */
	public BufferedImage[] aliens;
	/** Bridge button. */
	public BufferedImage[] bridge;
	/** Diplomacy button. */
	public BufferedImage[] diplomacy;
	/** Exit button. */
	public BufferedImage[] exit;
	/** The help button. */
	public BufferedImage[] help;
	/** The info button. */
	public BufferedImage[] info;
	/** The map button. */
	public BufferedImage[] map;
	/** The starmap button. */
	public BufferedImage[] starmap;
	/** Text panel. */
	public BufferedImage textPanel;
	/** The ship maps. */
	public BufferedImage[] shipMap;
	/**
	 * Load graphics for the given language.
	 * @param rl the resource locator.
	 * @return this
	 */
	public DatabaseGFX load(ResourceLocator rl) {
		background = rl.getImage("database/database_background");

		BufferedImage arrows = rl.getImage("database/arrow_updown"); 
		arrowDown = new BufferedImage[] {
			ImageUtils.newSubimage(arrows, 0 * 34, 0, 34, 45),	
			ImageUtils.newSubimage(arrows, 1 * 34, 0, 34, 45),	
			ImageUtils.newSubimage(arrows, 2 * 34, 0, 34, 45),	
		};
		arrowUp = new BufferedImage[] {
			ImageUtils.newSubimage(arrows, 3 * 34, 0, 34, 45),	
			ImageUtils.newSubimage(arrows, 4 * 34, 0, 34, 45),	
			ImageUtils.newSubimage(arrows, 5 * 34, 0, 34, 45),	
		};
		BufferedImage edges = rl.getImage("database/picture_edge");
		pictureEdge = new BufferedImage[] {
			ImageUtils.newSubimage(edges, 0 * 18, 0 * 18, 18, 18),
			ImageUtils.newSubimage(edges, 1 * 18, 0 * 18, 18, 18),
			ImageUtils.newSubimage(edges, 0 * 18, 1 * 18, 18, 18),
			ImageUtils.newSubimage(edges, 1 * 18, 1 * 18, 18, 18),
		};
		int n = 5;
		recordMessage = new BufferedImage[n];
		colorScaleImage(rl.getImage("database/record_message"), recordMessage);
		aliens = new BufferedImage[n];
		colorScaleImage(rl.getImage("database/aliens"), aliens);
		map = new BufferedImage[n];
		colorScaleImage(rl.getImage("database/ship_map"), map);
		help = new BufferedImage[n];
		colorScaleImage(rl.getImage("database/help"), help);
		exit = new BufferedImage[n];
		colorScaleImage(rl.getImage("database/exit_database"), exit);
		bridge = new BufferedImage[n];
		colorScaleImage(rl.getImage("database/bridge"), bridge);
		info = new BufferedImage[n];
		colorScaleImage(rl.getImage("database/info"), info);
		starmap = new BufferedImage[n];
		colorScaleImage(rl.getImage("database/starmap"), starmap);
		diplomacy = new BufferedImage[n];
		colorScaleImage(rl.getImage("database/diplomacy"), diplomacy);
		
		textPanel = rl.getImage("database/database_textpanel");
		
		shipMap = new BufferedImage[] {
			rl.getImage("database/ship_map_level_1"),	
			rl.getImage("database/ship_map_level_2"),	
			rl.getImage("database/ship_map_level_3"),	
			rl.getImage("database/ship_map_level_4"),	
			rl.getImage("database/ship_map_level_5"),	
		};
		return this;
	}
	/**
	 * Color scale an image into the output buffer.
	 * @param src the source
	 * @param dst the destination array
	 */
	protected static void colorScaleImage(BufferedImage src, BufferedImage[] dst) {
		int replace = 0xFF007FEF;
		int start = 0xFF50A4F4;
		int end = 0xFFBCDCFC;
		for (int i = 0; i < dst.length; i++) {
			float amount = 1.0f * i / (dst.length - 1);
			dst[i] = remapColor(src, replace, scaleBetween(start, end, amount));
		}
	}
	/**
	 * Remaps a specific color of the source image.
	 * @param src the source
	 * @param replace the ARGB color to replace
	 * @param with the ARGB color to replace with
	 * @return the new image
	 */
	protected static BufferedImage remapColor(BufferedImage src, int replace, int with) {
		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < src.getHeight(); i++) {
			for (int j = 0; j < src.getWidth(); j++) {
				int c = src.getRGB(j, i);
				if (c == replace) {
					c = with;
				}
				dst.setRGB(j, i, c);
			}
		}
		return dst;
	}
	/**
	 * Scale a color between two colors.
	 * @param start the start color
	 * @param end the end color
	 * @param amount the amount
	 * @return the color
	 */
	protected static int scaleBetween(int start, int end, float amount) {
		int a1 = (start >> 24) & 0xFF; 
		int r1 = (start >> 16) & 0xFF; 
		int g1 = (start >> 8) & 0xFF; 
		int b1 = (start) & 0xFF;

		int a2 = (end >> 24) & 0xFF; 
		int r2 = (end >> 16) & 0xFF; 
		int g2 = (end >> 8) & 0xFF; 
		int b2 = (end) & 0xFF;
		
		return minmax(0, 255, (int)(a1 + (a2 - a1) * amount)) << 24
		| minmax(0, 255, (int)(r1 + (r2 - r1) * amount)) << 16
		| minmax(0, 255, (int)(g1 + (g2 - g1) * amount)) << 8
		| minmax(0, 255, (int) (b1 + (b2 - b1) * amount));
		
	}
	/**
	 * Limit values between min and max inclusive.
	 * @param min the minimum value
	 * @param max the maximum value
	 * @param value the value
	 * @return the limited value
	 */
	protected static int minmax(int min, int max, int value) {
		return value < min ? min : (value > max ? max : value);
	}
}
