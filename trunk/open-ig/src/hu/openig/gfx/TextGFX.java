/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.gfx;

import hu.openig.utils.PCXImage;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Text graphics drawer and container class.
 * @author karnokd
 */
public class TextGFX {
	/**
	 * Record to store a particularly sized character series's width and height.
	 * @author karnokd
	 */
	private static class SizedCharImages {
		/** The uniform character width. */
		public int width;
		/** The uniform character height. */
		public int height;
		/** The map of characters to its images. */
		public final Map<Character, BufferedImage> chars = new HashMap<Character, BufferedImage>();
	}
	/** The entire backing image. */
	private BufferedImage charImage;
	/** The map of text size to character to actual image. */
	private Map<Integer, SizedCharImages> charMap 
	= new HashMap<Integer, SizedCharImages>();
	/** The colors used in the charset image to indicate aliased text. */
	private final int[] IMAGE_COLORS = { 0xFF9AC9FF, 0xFF4D7099, 0xFF25364B };
	/**
	 * Constructor. Initializes the internal tables by processing the given file.
	 * @param charsetFile the character set .PCX file
	 */
	public TextGFX(String charsetFile) {
		charImage = PCXImage.from(charsetFile, -2);
		split();
	}
	/**
	 * Split the entire images into character sizes and characters
	 */
	private void split() {
		// first sequence for siz
		String[] lineCharacters = {
				/* Size: 7 */
				"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn",
				"opqrstuvwxyz?!()'\"+-:;.,1234567890%& /\u00DF",
				"\u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF\u00EA\u00E9\u00E8\u00E0\u00C9\u00C1\u00C7\u00E7\u00F4\u00FB\u00F9\u00F2\u00EC\u00E1\u00F3\u00F1\u00D1\u00A1\u00BF\u00FA\u00ED\u00CD\u00D3\u00F3\u0150\u0151\u0170\u0171\u00DA",
				/* Size: 10 */
				"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn",
				"opqrstuvwxyz?!()'\"+-:;.,1234567890%& /\u00DF",
				"\u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF\u00EA\u00E9\u00E8\u00E0\u00C9\u00C1\u00C7\u00E7\u00F4\u00FB\u00F9\u00F2\u00EC\u00E1\u00F3\u00F1\u00D1\u00A1\u00BF\u00FA\u00ED\u00CD\u00D3\u00F3\u0150\u0151\u0170\u0171\u00DA",
				/* Size: 14 */
				"ABCDEFGHIJKLMNOPQRSTUVWXYZ",
				"abcdefghijklmnopqrstuvwxyz",
				"?!()'\"+-:;.,1234567890%& /",
				"\u00DF \u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF\u00EA\u00E9\u00E8\u00E0\u00C9\u00C1\u00C7\u00E7\u00F4\u00FB\u00F9\u00F2\u00EC\u00E1\u00F3\u00F1\u00D1",
				"\u00A1\u00BF\u00FA\u00ED\u00CD\u00D3\u00F3\u0150\u0151\u0170\u0171\u00DA",
				/* Size: 5 */
				"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
				"?!()'\"+-:;.,1234567890%& /\u00DF \u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF\u00EA\u00E9\u00E8\u00E0\u00C9\u00C1\u00C7\u00E7\u00F4\u00FB\u00F9\u00F2\u00EC\u00E1\u00F3\u00F1\u00D1",
				"\u00A1\u00BF\u00FA\u00ED\u00CD\u00D3\u00F3\u0150\u0151\u0170\u0171\u00DA"
		};
		int[] height = {
				7, 7, 7, 
				10, 10, 10, 
				14, 14, 14, 14, 14,
				5, 5, 5
		};
		int[] width = {
				5, 5, 5, 
				7, 7, 7, 
				12, 12, 12, 12, 12,
				5, 5, 5
		};
		int[] spacingX = {
				3, 3, 3, 
				1, 1, 1, 
				0, 0, 0, 0, 0,
				1, 1, 1
		};
		int[] spacingY = {
				1, 1, 2, 
				0, 0, 1, 
				0, 0, 0, 0, 1,
				1, 1, 1
		};
		int y = 0;
		for (int j = 0; j < lineCharacters.length; j++) {
			SizedCharImages charToImg = charMap.get(height[j]);
			if (charToImg == null) {
				charToImg = new SizedCharImages();
				charToImg.width = width[j];
				charToImg.height = height[j];
				charMap.put(height[j], charToImg);
			}
			int x = 0;
			String s = lineCharacters[j];
			for (int i = 0; i < s.length(); i++) {
				BufferedImage ci = charImage.getSubimage(x, y, width[j], height[j]);
				charToImg.chars.put(lineCharacters[j].charAt(i), ci);
				
				x += width[j] + spacingX[j];
			}
			y += height[j] + spacingY[j];
		}
	}
	/**
	 * Draw the given text at the given location on the supplied graphics object.
	 * @param g the graphics object to draw to
	 * @param x the starting X coordinate
	 * @param y the starting Y coordinate
	 * @param size the font size
	 * @param color the color to use
	 * @param text the text to print
	 */
	public void paintTo(Graphics2D g, int x, int y, int size, int color, String text) {
		SizedCharImages charToImage = charMap.get(size);
		if (charToImage != null) {
			BufferedImage workImage = new BufferedImage(charToImage.width, charToImage.height, BufferedImage.TYPE_INT_ARGB);
			int[] localImage = new int[charToImage.width * charToImage.height];
			for (int i = 0; i < text.length(); i++) {
				BufferedImage ci = charToImage.chars.get(text.charAt(i));
				if (ci != null) {
					ci.getRGB(0, 0, charToImage.width, charToImage.height, localImage, 0, charToImage.width);
					colorRewrite(localImage, color);
					workImage.setRGB(0, 0, charToImage.width, charToImage.height, localImage, 0, charToImage.width);
					g.drawImage(workImage, x, y, null);
				}
				x += charToImage.width + 2;
			}
		}
	}
	/**
	 * Rewrites the original char pixel colors into the properly shaded color of the supplied value.
	 * @param pixels the array of pixels
	 * @param color the color to rewrite to
	 */
	private void colorRewrite(int[] pixels, int color) {
		color &= 0x00FFFFFF;
		int[] newcolor = { 0x99000000 | color, 0xFF000000 | color, 0x4B000000 | color };
		for (int i = 0; i < pixels.length; i++) {
			int c = pixels[i];
			for (int j = 0; j < newcolor.length; j++) {
				if (c == IMAGE_COLORS[j]) {
					c = newcolor[j];
					break;
				}
			}
			pixels[i] = c;
		}
		
	}
}
