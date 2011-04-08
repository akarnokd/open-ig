/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import hu.openig.model.OriginalBuilding;
import hu.openig.model.OriginalPlanet;
import hu.openig.utils.XElement;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Convert the planet definitions from the pre 0.8 version.
 * @author akarnokd, Mar 29, 2011
 */
public final class ConvertPlanets {

	/** Utility class. */
	private ConvertPlanets() {
	}

	/**
	 * @param args no arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		XElement old = XElement.parseXML("data/generic/campaign/main/planets_old.xml");
		
		XElement root = new XElement("planets");
		
		for (XElement oplanet : old.childrenWithName("planet")) {
			XElement planet = root.add("planet");
			planet.set("id", oplanet.get("id"));
			planet.set("name", oplanet.childValue("name"));
			if ("Earth".equals(oplanet.get("id"))) {
				planet.set("label", "planets.earth"); // the label override for Earth
			}
			planet.set("x", Integer.parseInt(oplanet.childValue("location-x")) * 2);
			planet.set("y", Integer.parseInt(oplanet.childValue("location-y")) * 2);
			
			XElement surface = planet.add("surface");
			surface.set("type", oplanet.childValue("type").toLowerCase());
			surface.set("id", oplanet.childValue("variant"));
			
			int s = Integer.parseInt(oplanet.childValue("size"));
			if (s == 6) {
				s = 18;
			} else
			if (s == 7) {
				s = 24;
			} else {
				s = 30;
			}
			planet.set("size", s);
			planet.set("owner", oplanet.childValue("owner"));
			planet.set("race", OriginalPlanet.convertRaceTechId(oplanet.childValue("race")));
			planet.set("rotate", oplanet.childValue("rotate"));
			
			planet.set("population", oplanet.childValue("populate"));
			
			planet.set("allocation", "DEFAULT"); // resource allocation strategy
			planet.set("autobuild", "OFF"); // what to build automatically
			planet.set("morale", "100"); // morale percent
			planet.set("tax", "NORMAL"); // tax level
			planet.set("tax-income", "0"); // last day's tax income
			planet.set("trade-income", "0"); // last day's trade income

			String orbit = oplanet.childValue("in-orbit");
			
			XElement xorbit = planet.add("inventory");
			if (orbit != null && !"-".equals(orbit)) {
				for (String oe : orbit.split("\\s*,\\s*")) {
					XElement equip = xorbit.add("item");
					equip.set("id", oe);
					equip.set("tech", planet.get("race"));
					equip.set("shield", ""); // technology default
					equip.set("hp", ""); // technology default
					equip.set("count", "1");
				}
			}
			
			XElement buildings = planet.add("buildings");
			List<XElement> bs = new ArrayList<XElement>();
			for (XElement ob : oplanet.childElement("buildings").childrenWithName("building")) {
				XElement b = new XElement("building");
				b.set("id", OriginalBuilding.TRANSLATE.get(ob.childValue("id")));
				b.set("tech", planet.get("race"));
				b.set("x", Integer.parseInt(ob.childValue("x")) - 1);
				b.set("y", Integer.parseInt(ob.childValue("y")) - 1);
				b.set("enabled", ob.childValue("enabled"));
				
				b.set("repairing", "false");
				b.set("energy", "0");
				b.set("worker", "0");
				b.set("level", "0");
				b.set("build", ""); // build progress: set to type.hitpoints on load
				b.set("hp", ""); // hitpoints: set to type.hitpoints on load
				bs.add(b);
			}
			Collections.sort(bs, new Comparator<XElement>() {
				@Override
				public int compare(XElement o1, XElement o2) {
					return o1.get("id").compareToIgnoreCase(o2.get("id"));
				}
			});
			buildings.add(bs);
		}
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("data/generic/campaign/main/planets.xml"), "UTF-8"));
		try {
			out.println("<?xml version='1.0' encoding='UTF-8'?>");
			out.println(root.toString());
		} finally {
			out.close();
		}
	}

}
