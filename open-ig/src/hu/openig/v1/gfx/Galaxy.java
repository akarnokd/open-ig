/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.gfx;

import hu.openig.utils.ImageUtils;
import hu.openig.utils.XML;
import hu.openig.v1.ResourceLocator;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * @author karnok, 2009.11.18.
 * @version $Revision 1.0$
 */
public class Galaxy {
	/** The background starmap. */
	public BufferedImage background;
	/** A surface tile. */
	public class Tile {
		/** The tile reference image. */
		public BufferedImage image;
		/** The tile width in top-right angle. */
		public int width;
		/** The tile height in bottom-right angle. */
		public int height;
	}
	/** A planet type record. */
	public class Planet {
		/** The type id. */
		public String type;
		/** The type label. */
		public String label;
		/** The rotation phases. */
		public BufferedImage[] rotation;
		/** The map of tile id to tile records. */
		public final Map<Integer, Tile> tiles = new HashMap<Integer, Tile>();
	}
	/** The map of planet type to planet description. */
	public final Map<String, Planet> planets = new HashMap<String, Planet>();
	/**
	 * Load the galaxy base resources.
	 * @param rl the resource locator
	 * @param language the language
	 * @param game the game: e.g <code>campaign/main</code>
	 */
	public void load(ResourceLocator rl, String language, String game) {
		planets.clear();
		Element root = rl.getXML(language, game + "/galaxy");
		background = rl.getImage(language, XML.childValue(root, "background"));
		Element planets = XML.childElement(root, "planets");
		for (Element e : XML.childrenWithName(planets, "planet")) {
			Planet p = new Planet();
			p.type = e.getAttribute("type");
			p.label = e.getAttribute("label");
			BufferedImage img = rl.getImage(language, XML.childValue(e, "body"));
			int n = img.getWidth() / img.getHeight();
			p.rotation = new BufferedImage[n];
			for (int i = 0; i < n; i++) {
				p.rotation[i] = ImageUtils.newSubimage(img, i * img.getHeight(), 0, img.getHeight(), img.getHeight());
			}
			Element ts = XML.childElement(e, "tileset");
			String pattern = ts.getAttribute("pattern");
			for (Element t : XML.children(ts)) {
				int start = 0;
				int end = -1;
				int width = 1;
				int height = 1;
				if (t.getNodeName().equals("tile-range")) {
					start = Integer.parseInt(t.getAttribute("start"));
					end = Integer.parseInt(t.getAttribute("end"));
				} else
				if (t.getNodeName().endsWith("tile")) {
					start = Integer.parseInt(t.getAttribute("id"));
					end = start;
				} else {
					continue;
				}
				String w = t.getAttribute("width");
				if (w != null && w.length() > 0) {
					width = Integer.parseInt(w);
				}
				String h = t.getAttribute("height");
				if (h != null && h.length() > 0) {
					height = Integer.parseInt(h);
				}
				for (int k = start; k <= end; k++) {
					Tile tile = new Tile();
					tile.width = width;
					tile.height = height;
					tile.image = rl.getImage(language, String.format(pattern, k));
					p.tiles.put(k, tile);
				}
				
			}
		}
	}
}
