/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.render;

import hu.openig.model.ResourceLocator;
import hu.openig.utils.Exceptions;
import hu.openig.utils.LRUHashMap;
import hu.openig.utils.XElement;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

/**
 * Text graphics drawer and container class.
 * @author akarnokd
 */
public class TextRenderer {
	/**
	 * Record to store a particularly sized character series's width and height.
	 * @author akarnokd
	 */
	private static class SizedCharImages {
		/** The uniform character width. */
		public int width;
		/** The uniform character height. */
//		public int height;
		/** The map of characters to its images. */
		public final Map<Character, BufferedImage> chars = new HashMap<>();
	}
	/** The entire backing image. */
	private BufferedImage charImage;
	/** Text height constant helper. */
	public static final int SIZE_5 = 5;
	/** Text height constant helper. */
	public static final int SIZE_7 = 7;
	/** Text height constant helper. */
	public static final int SIZE_10 = 10;
	/** Text height constant helper. */
	public static final int SIZE_14 = 14;
	/** Predefined color constant. */
	public static final int YELLOW = 0xFFFCFC58;
	/** Predefined color constant. */
	public static final int GREEN = 0xFF6CB068;
	/** Predefined color constant. */
	public static final int GRAY = 0xFF949494;
	/** Predefined color constant. */
	public static final int DARK_GRAY = 0xFF6C6C6C;
	/** Predefined color constant. */
	public static final int RED = 0xFFFC2828;
	/** Predefined color constant. */
	public static final int DARK_GREEN = 0xFF009800;
	/** Predefined color constant. */
	public static final int ORANGE = 0xFFFCB000;
	/** Predefined color constant. */
	public static final int WHITE = 0xFFFCFCFC;
	/** Predefined color constant. */
	public static final int CYAN = 0xFF00FCFC;
	/** Predefined color constant. */
	public static final int PURPLE = 0xFFFC00FC;
	/** Predefined color constant. */
	public static final int LIGHT_GREEN = 0xFFB0FC6C;
	/** Predefined color constant. */
	public static final int BLUE = 0xFF3C3CFC;
	/** Predefined color constant. */
	public static final int LIGHT_BLUE = 0xFF94A4FC;
	/** Predefined color constant for a race. */
	public static final int GALACTIC_EMPIRE = ORANGE;
	/** Predefined color constant for a race and small text. */
	public static final int GALACTIC_EMPIRE_ST = scaleColor(GALACTIC_EMPIRE, 1 / 1.3f);
	/** Predefined color constant for a race. */
	public static final int GARTHOG_REPUBLIC = RED;
	/** Predefined color constant for a race. */
	public static final int GARTHOG_REPUBLIC_ST = scaleColor(RED, 1 / 1.3f);
	/** Predefined color constant for a race. */
	public static final int MORGATH_EMPIRE = WHITE;
	/** Predefined color constant for a race. */
	public static final int MORGATH_EMPIRE_ST = scaleColor(WHITE, 1 / 1.3f);
	/** Predefined color constant for a race. */
	public static final int YCHOM_EMPIRE = WHITE;
	/** Predefined color constant for a race. */
	public static final int YCHOM_EMPIRE_ST = scaleColor(WHITE, 1 / 1.3f);
	/** Predefined color constant for a race. */
	public static final int DRIBS_EMPIRE = PURPLE;
	/** Predefined color constant for a race. */
	public static final int DRIBS_EMPIRE_ST = scaleColor(PURPLE, 1 / 1.3f);
	/** Predefined color constant for a race. */
	public static final int SULLEP_EMPIRE = YELLOW;
	/** Predefined color constant for a race. */
	public static final int SULLEP_EMPIRE_ST = scaleColor(YELLOW, 1 / 1.3f);
	/** Predefined color constant for a race. */
	public static final int DARGSLAN_KINGDOM = DARK_GREEN;
	/** Predefined color constant for a race. */
	public static final int DARGSLAN_KINGDOM_ST = scaleColor(DARK_GREEN, 1 / 1.3f);
	/** Predefined color constant for a race. */
	public static final int ECALEP_REPUBLIC = LIGHT_GREEN;
	/** Predefined color constant for a race. */
	public static final int ECALEP_REPUBLIC_ST = scaleColor(LIGHT_GREEN, 1 / 1.3f);
	/** Predefined color constant for a race. */
	public static final int FREE_TRADERS = BLUE;
	/** Predefined color constant for a race. */
	public static final int FREE_TRADERS_ST = scaleColor(BLUE, 1 / 1.3f);
	/** Predefined color constant for a race. */
	public static final int FREE_NATIONS_SOCIETY = LIGHT_BLUE;
	/** Predefined color constant for a race. */
	public static final int FREE_NATIONS_SOCIETY_ST = scaleColor(LIGHT_BLUE, 1 / 1.3f);
	/** The cache for color-remaped charImages. */
	private Map<Integer, Map<Integer, SizedCharImages>> coloredCharImages = LRUHashMap.create(64);
	/** The character width on a particular character size. */
	private Map<Integer, Integer> charsetWidths = new HashMap<>();
	/** The character space for a particular character size. */
	private Map<Integer, Integer> charsetSpaces = new HashMap<>();
	/** Use standard Java fonts instead of the original bitmap fonts. */
	private boolean useStandardFonts;
	/** The default font rendering context. */
	private final FontRenderContext frc;
	/**
	 * Constructor. Initializes the internal tables by processing the given file.
	 * @param rl the resource locator
	 * @param useStandardFonts instead of the fixed size?
	 */
	public TextRenderer(ResourceLocator rl, boolean useStandardFonts) {
		frc = new FontRenderContext(null, false, false);
		this.useStandardFonts = useStandardFonts; 
		charImage = rl.getImage("charset");
		// map some colors
		split(YELLOW);
		split(GREEN);
		split(GRAY);
		split(RED);
	}
	/**
	 * A line definition.
	 * @author akarnokd, 2012.10.22.
	 */
	static class LineDefinition {
		/** The characters. */
		String characters;
		/** The width. */
		int width;
		/** The height. */
		int height;
		/** The X spacing. */
		int spaceX;
		/** The Y spacing. */
		int spaceY;
	}
	/** The characters. */
	static final List<LineDefinition> CHARACTERS;
	static {
		CHARACTERS = new ArrayList<>();
		try {
			XElement charset = XElement.parseXML(TextRenderer.class.getResource("charset.xml"));
			for (XElement xline : charset.childrenWithName("line")) {
				LineDefinition def = new LineDefinition();
				def.characters = xline.content;
				def.width = xline.getInt("width");
				def.height = xline.getInt("height");
				def.spaceX = xline.getInt("spacing-x");
				def.spaceY = xline.getInt("spacing-y");
				CHARACTERS.add(def);
			}
		} catch (XMLStreamException | IOException ex) {
			Exceptions.add(ex);
                }		
	}
	/** Characters in various lines. */
	static final String[] LINE_CHARACTERS = {
			/* Size: 7 */
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn",
			"opqrstuvwxyz?!()'\"+-:;.,1234567890%& /\u00DF",
			"\u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF\u00EA\u00E9\u00E8\u00E0\u00C9\u00C1\u00C7\u00E7\u00F4\u00FB\u00F9\u00F2\u00EC\u00E1\u00F3\u00F1\u00D1\u00A1\u00BF\u00FA\u00ED\u00CD\u00D3\u00F3\u0150\u0151\u0170\u0171\u00DA=<>*",
			/* Size: 10 */
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn",
			"opqrstuvwxyz?!()'\"+-:;.,1234567890%& /\u00DF",
			"\u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF\u00EA\u00E9\u00E8\u00E0\u00C9\u00C1\u00C7\u00E7\u00F4\u00FB\u00F9\u00F2\u00EC\u00E1\u00F3\u00F1\u00D1\u00A1\u00BF\u00FA\u00ED\u00CD\u00D3\u00F3\u0150\u0151\u0170\u0171\u00DA=<>*",
			/* Size: 14 */
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ",
			"abcdefghijklmnopqrstuvwxyz",
			"?!()'\"+-:;.,1234567890%& /",
			"\u00DF \u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF\u00EA\u00E9\u00E8\u00E0\u00C9\u00C1\u00C7\u00E7\u00F4\u00FB\u00F9\u00F2\u00EC\u00E1\u00F3\u00F1\u00D1",
			"\u00A1\u00BF\u00FA\u00ED\u00CD\u00D3\u00F3\u0150\u0151\u0170\u0171\u00DA=<>*",
			/* Size: 5 */
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
			"?!()'\"+-:;.,1234567890%& /\u00DF \u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF\u00EA\u00E9\u00E8\u00E0\u00C9\u00C1\u00C7\u00E7\u00F4\u00FB\u00F9\u00F2\u00EC\u00E1\u00F3\u00F1\u00D1",
			"\u00A1\u00BF\u00FA\u00ED\u00CD\u00D3\u00F3\u0150\u0151\u0170\u0171\u00DA=<>*"
	};
	/** 
	 * Print original characters.
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		for (String s : LINE_CHARACTERS) {
			System.out.println(s);
		}
	}
	/** The maximum text size available. */
	int maxSize = 14;
	/**
	 * Split the entire images into character sizes and characters.
	 * @param color the target color.
	 * @return the generated sized map of the character images
	 */
	private Map<Integer, SizedCharImages> split(int color) {
		// first sequence for siz
		Map<Integer, SizedCharImages> charMap = new HashMap<>();
		coloredCharImages.put(color, charMap);
		BufferedImage workImage = colorRemap(charImage, color);
		int y = 0;
		for (LineDefinition def : CHARACTERS) {
			charsetWidths.put(def.height, def.width);
			SizedCharImages charToImg = charMap.get(def.height);
			if (charToImg == null) {
				charToImg = new SizedCharImages();
				charToImg.width = def.width;
//				charToImg.height = HEIGHTS[j];
				charMap.put(def.height, charToImg);
			}
			int x = 0;
			String s = def.characters;
			for (int i = 0; i < s.length(); i++) {
				BufferedImage ci = workImage.getSubimage(x, y, def.width, def.height);
				charToImg.chars.put(s.charAt(i), ci);
				
				x += def.width + def.spaceX;
			}
			y += def.height + def.spaceY;
		}
		charsetSpaces.put(5, 1);
		charsetSpaces.put(7, 1);
		charsetSpaces.put(10, 1);
		charsetSpaces.put(14, 0);
		return charMap;
	}
	/**
	 * Remap the base colors using the given target color.
	 * @param src the source image
	 * @param color the color value
	 * @return the new image
	 */
	private static BufferedImage colorRemap(BufferedImage src, int color) {
		BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		int c0 = 0xFF000000 
		| ((int)Math.min(((color & 0xFF0000) >> 16) * 1.3f, 0xFC) << 16) 
		| ((int)Math.min(((color & 0xFF00) >> 8) * 1.3f, 0xFC) << 8) 
		| ((int)Math.min(((color & 0xFF)) * 1.3f, 0xFC));
		int c1 = 0xFF000000 | color;
		int c2 = 0xFF000000 
		| ((int)(((color & 0xFF0000) >> 16) * 0.6f) << 16) 
		| ((int)(((color & 0xFF00) >> 8) * 0.6f) << 8) 
		| ((int)(((color & 0xFF)) * 0.6f));
		
		for (int i = 0; i < src.getHeight(); i++) {
			for (int j = 0; j < src.getWidth(); j++) {
				int c = src.getRGB(j, i);
				int d = c;
				if (c == 0xFF9AC9FF) {
					d = c0;
				} else
				if (c == 0xFF4D7099) {
					d = c1;
				} else
				if (c == 0xFF25364B) {
					d = c2;
				}
				result.setRGB(j, i, d);
			}
		}
		return result;
	}
	/**
	 * Returns the expected text width for the given character size and string.
	 * @param size the character size
	 * @param text the text to test
	 * @return the width in pixels
	 */
	public int getTextWidth(int size, String text) {
		if (text.length() > 0) {
			if (useStandardFonts) {
				Font font = new Font(Font.MONOSPACED, Font.PLAIN, size /* + 4 */);
				return (int)font.getStringBounds(text, frc).getWidth();
			}
			Integer widths = charsetWidths.get(size);
			Integer spaces = charsetSpaces.get(size);
			if (widths == null) {
				double widths2 = (size * 1.0 / maxSize * charsetWidths.get(maxSize));
				double spaces2 = (size * 1.0 / maxSize * charsetSpaces.get(maxSize));
				return (int)((widths2 + spaces2) * (text.length()) - spaces2);
			}
			return (widths + spaces) * (text.length()) - spaces;
		}
		return 0;
	}
	/**
	 * Draw the given text at the given location on the supplied graphics object.
	 * @param g the graphics object to draw to
	 * @param x the starting X coordinate
	 * @param y the starting Y coordinate
	 * @param size the font size
	 * @param color the color to use, alpha values are ignored at this level. If you need alpha, set the graphics object appropriately.
	 * @param text the text to print
	 */
	public void paintTo(Graphics2D g, int x, int y, int size, int color, String text) {
		if (useStandardFonts) {
			Font f = g.getFont();
			Color c = g.getColor();
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, size /* + 4 */));
			g.setColor(new Color(color));
			FontMetrics fm = g.getFontMetrics();
			g.drawString(text, x, y + fm.getAscent() - fm.getDescent() / 2 /* - 4 */);
//			g.drawLine(x, y, x + 5, y);
//			g.drawLine(x, y + size - 1, x + 5, y + size - 1);
//			g.drawLine(x, y + fm.getAscent() + fm.getDescent() - 1, x + 5, y + fm.getAscent() + fm.getDescent() - 1);
			g.setColor(c);
			g.setFont(f);
			return;
		}
		Map<Integer, SizedCharImages> charMap = coloredCharImages.get(color);
		if (charMap == null) {
			charMap = split(color);
		}
		AffineTransform tf = g.getTransform();
		g.translate(x, y);
		SizedCharImages charToImage = charMap.get(size);
		
		if (charToImage == null) {
			g.scale(size * 1.0 / maxSize, size * 1.0 / maxSize);
			charToImage = charMap.get(maxSize);
			size = maxSize;
		}
		int spc = charsetSpaces.get(size);
		int x1 = 0;
		for (int i = 0; i < text.length(); i++) {
			BufferedImage ci = charToImage.chars.get(text.charAt(i));
			if (ci != null) {
				g.drawImage(ci, x1, 0, null);
			}
			x1 += charToImage.width + spc;
		}
		g.setTransform(tf);
	}
	/**
	 * Scale the color according to the given factor.
	 * @param color the original color
	 * @param scale the scale amount
	 * @return the scaled color
	 */
	public static int scaleColor(int color, float scale) {
		return ((int)Math.min(((color & 0xFF0000) >> 16) * scale, 0xFC) << 16)
		| ((int)Math.min(((color & 0xFF00) >> 8) * scale, 0xFC) << 8)
		| ((int) Math.min(((color & 0xFF)) * scale, 0xFC))
		| (color & 0xFF000000);
	}
	/**
	 * Wrap the text along the given width.
	 * @param text the text to wrap
	 * @param width the target width
	 * @param size the text size
	 * @param linesOut the output lines
	 * @return the maximum width
	 */
	public int wrapText(String text, int width, int size, List<String> linesOut) {
		linesOut.clear();
		int maxWidth = 0;
		String[] par = text.split("\n");
		for (String s : par) {
			if (s.trim().isEmpty()) {
				linesOut.add("");
				continue;
			}
			String[] words = s.split("\\s+");
			StringBuilder line = new StringBuilder();
			StringBuilder lineTest = new StringBuilder();
			for (int i = 0; i < words.length; i++) {
				if (lineTest.length() > 0) {
					lineTest.append(" ");
				}
				lineTest.append(words[i]);
				int tw = getTextWidth(size, lineTest.toString());
				if (tw > width) {
					if (line.length() > 0) {
						String t = line.toString();
						maxWidth = Math.max(maxWidth, getTextWidth(size, t));
						linesOut.add(t);
						line.setLength(0);
						lineTest.setLength(0);
						i--;
					} else {
						// the case when even word itself desnt fit in the width at all
						String t = words[i];
						maxWidth = Math.max(maxWidth, getTextWidth(size, t));
						linesOut.add(t);
						lineTest.setLength(0);
					}
				} else {
					if (line.length() > 0) {
						line.append(" ");
					}
					line.append(words[i]);
				}
			}
			if (line.length() > 0) {
				String t = line.toString();
				maxWidth = Math.max(maxWidth, getTextWidth(size, t));
				linesOut.add(t);
			}
		}
		return maxWidth;
	}
	/**
	 * Set the standard font usage.
	 * @param value use?
	 */
	public void setUseStandardFonts(boolean value) {
		this.useStandardFonts = value;
	}
	/**
	 * @return is standard fonts in use?
	 */
	public boolean isUseStandardFonts() {
		return useStandardFonts;
	}
	/**
	 * Is the given character supported by the charmap?
	 * @param c the character to test
	 * @return true if supported
	 */
	public boolean isSupported(char c) {
		if (Character.isWhitespace(c)) {
			return true;
		}
		return coloredCharImages.values().iterator().next().values().iterator().next().chars.containsKey(c);		
	}
	/**
	 * A text segment with custom coloring.
	 * @author akarnokd, 2012.05.24.
	 */
	public static class TextSegment {
		/** The text segment. */
		public final String text;
		/** The color. */
		public final int color;
		/** 
		 * Constructor with initial parameters.
		 * @param text the text
		 * @param color the color
		 */
		public TextSegment(String text, int color) {
			this.text = text;
			this.color = color;
		}
		@Override
		public String toString() {
			return String.format("%08X: %s", color, text);
		}
	}
	/**
	 * Paint a sequence of colored text segments.
	 * @param g2 the graphics context
	 * @param x the render origin
	 * @param y the render origin
	 * @param size the common text size
	 * @param segments the segments
	 */
	public void paintTo(Graphics2D g2, int x, int y, int size, Iterable<TextSegment> segments) {
		int dx = 0;
		Integer spaces = charsetSpaces.get(size);
		if (spaces == null) {
			spaces = charsetSpaces.get(maxSize);
		}
		for (TextSegment ts : segments) {
			paintTo(g2, x + dx, y, size, ts.color, ts.text);
			dx += getTextWidth(size, ts.text) + spaces;
		}
	}
}
