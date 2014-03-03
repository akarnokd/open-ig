/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.XElement;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main record that stores various ship types
 * and their available walking routes and stages.
 * @author akarnokd, 2009.10.09.
 */
public class Walks {
	/** The map of ships to be walked. */
	public final Map<String, WalkShip> ships = new HashMap<>();
	/**
	 * Load the shipwalk.xml from the resoure locator.
	 * @param rl the resource locator
	 * @param data the path to the resource
	 */
	public void load(ResourceLocator rl, String data) {
		process(rl.getXML(data), rl);
	}
	/**
	 * Process the document.
	 * @param root the root element
	 * @param rl the resource locator
	 */
	protected void process(XElement root, ResourceLocator rl) {
		for (XElement ship : root.childrenWithName("ship")) {
			WalkShip ws = new WalkShip();
			ws.level = ship.get("id");
			String ref = ship.get("ref", null);
			if (ref != null) {
				ships.put(ws.level, ships.get(ref));
			} else {
				ships.put(ws.level, ws);
				for (XElement position : ship.childrenWithName("position")) {
					WalkPosition wp = new WalkPosition(position.get("id"), ws);
					ws.positions.put(wp.id, wp);
					wp.pictureName = position.get("picture");
					for (XElement transition : position.childrenWithName("transition")) {
						WalkTransition wt = new WalkTransition();
						wp.transitions.add(wt);
						wt.media = transition.get("media", null);
						wt.label = transition.get("label");
						wt.to = transition.get("to");
						String area = transition.get("area");
						List<Integer> xs = new ArrayList<>();
						List<Integer> ys = new ArrayList<>();
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
}
