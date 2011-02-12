/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.XML;
import hu.openig.core.ResourceLocator;
import hu.openig.core.ResourceType;
import hu.openig.core.ResourceLocator.ResourcePlace;

import java.awt.Polygon;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author karnok, 2009.10.09.
 * @version $Revision 1.0$
 */
public class Walks {
	/** The map of ships to be walked. */
	public final Map<String, WalkShip> ships = new HashMap<String, WalkShip>();
	/**
	 * Load the shipwalk.xml from the resoure locator.
	 * @param rl the resource locator
	 * @param lang the current language
	 * @param data the path to the resource
	 */
	public void load(ResourceLocator rl, String lang, String data) {
		ResourcePlace rp = rl.get(lang, data, ResourceType.DATA);
		if (rp == null) {
			// TODO log
			throw new AssertionError("Missing resource: " + data);
		}
		InputStream in = rp.open();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(in);
			process(doc.getDocumentElement(), rl, lang);
		} catch (IOException ex) {
			// TODO log
			ex.printStackTrace();
		} catch (SAXException ex) {
			// TODO log
			ex.printStackTrace();
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
				// TODO log
				ex.printStackTrace();
			}
		}
	}
	/**
	 * Process the document.
	 * @param root the root element
	 * @param rl the resource locator
	 * @param lang the current language
	 */
	protected void process(Element root, ResourceLocator rl, String lang) {
		for (Element ship : XML.childrenWithName(root, "ship")) {
			WalkShip ws = new WalkShip();
			ws.level = ship.getAttribute("id");
			ships.put(ws.level, ws);
			for (Element position : XML.childrenWithName(ship, "position")) {
				WalkPosition wp = new WalkPosition();
				wp.id = position.getAttribute("id");
				ws.positions.put(wp.id, wp);
				wp.picture = rl.getImage(lang, position.getAttribute("picture"));
				for (Element transition : XML.childrenWithName(position, "transition")) {
					WalkTransition wt = new WalkTransition();
					wp.transitions.add(wt);
					wt.media = transition.getAttribute("media");
					wt.label = transition.getAttribute("label");
					wt.to = transition.getAttribute("to");
					String area = transition.getAttribute("area");
					List<Integer> xs = new ArrayList<Integer>();
					List<Integer> ys = new ArrayList<Integer>();
					String[] pairs = area.split("\\s+");
					for (String p : pairs) {
						String[] xy = p.split(",");
						if (xy.length != 2) {
							// TODO log
							throw new AssertionError("Coordinate pair error: " + p + " of " + area);
						}
						xs.add(Integer.parseInt(xy[0]));
						ys.add(Integer.parseInt(xy[1]));
					}
					int[] xsi = new int[xs.size()];
					int[] ysi = new int[ys.size()];
					for (int i = 0; i < xsi.length; i++) {
						xsi[i] = xs.get(i);
						ysi[i] = ys.get(i);
					}
					wt.area = new Polygon(xsi, ysi, xsi.length);
				}
			}
		}
	}
}
