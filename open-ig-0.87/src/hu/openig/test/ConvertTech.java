/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import hu.openig.model.ResearchSubCategory;
import hu.openig.utils.XElement;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Convert the old technology XML to the new format.
 * @author akarnokd, Mar 31, 2011
 */
public final class ConvertTech {

	/**
	 * Utility class.
	 */
	private ConvertTech() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		XElement old = XElement.parseXML("data/generic/campaign/main/tech_old.xml");
		
		XElement root = new XElement("tech");

		for (XElement oitem : old.childrenWithName("item")) {
			XElement item = root.add("item");
			
			String id = oitem.get("id");
			item.set("id", id);
			item.set("name", "tech." + id.toLowerCase() + ".name");
			item.set("long-name", "tech." + id.toLowerCase() + ".longname");
			item.set("description", "tech." + id.toLowerCase() + ".description");

			String clazz = oitem.childValue("class");
			String type = oitem.childValue("type");
			if ("Spaceships".equals(clazz)) {
				if (type.equals("Fighters")) {
					item.set("category", ResearchSubCategory.SPACESHIPS_FIGHTERS);
					item.set("image", "inventions/spaceships/fighters/" + underscore(id));
				} else
				if (type.equals("Cruisers")) {
					item.set("category", ResearchSubCategory.SPACESHIPS_CRUISERS);
					item.set("image", "inventions/spaceships/cruisers/" + underscore(id));
				} else
				if (type.equals("Battleships")) {
					item.set("category", ResearchSubCategory.SPACESHIPS_BATTLESHIPS);
					item.set("image", "inventions/spaceships/battleships/" + underscore(id));
				} else
				if (type.equals("Satellites")) {
					item.set("category", ResearchSubCategory.SPACESHIPS_SATELLITES);
					item.set("image", "inventions/spaceships/satellites/" + underscore(id));
				} else
				if (type.equals("SpaceStations")) {
					item.set("category", ResearchSubCategory.SPACESHIPS_STATIONS);
					item.set("image", "inventions/spaceships/stations/" + underscore(id));
				}
			} else
			if ("Equipments".equals(clazz)) {
				if (type.equals("HyperDrives")) {
					item.set("category", ResearchSubCategory.EQUIPMENT_HYPERDRIVES);
					item.set("image", "inventions/equipments/hyperdrives/" + underscore(id));
				} else
				if (type.equals("Modules")) {
					item.set("category", ResearchSubCategory.EQUIPMENT_MODULES);
					item.set("image", "inventions/equipments/modules/" + underscore(id));
				} else
				if (type.equals("Radars")) {
					item.set("category", ResearchSubCategory.EQUIPMENT_RADARS);
					item.set("image", "inventions/equipments/radars/" + underscore(id));
				} else
				if (type.equals("Shields")) {
					item.set("category", ResearchSubCategory.EQUIPMENT_SHIELDS);
					item.set("image", "inventions/equipments/shields/" + underscore(id));
				}
			} else
			if ("Weapons".equals(clazz)) {
				if (type.equals("Lasers")) {
					item.set("category", ResearchSubCategory.WEAPONS_LASERS);
					item.set("image", "inventions/weapons/lasers/" + underscore(id));
				} else
				if (type.equals("Cannons")) {
					item.set("category", ResearchSubCategory.WEAPONS_CANNONS);
					item.set("image", "inventions/weapons/cannons/" + underscore(id));
				} else
				if (type.equals("Projectiles")) {
					item.set("category", ResearchSubCategory.WEAPONS_PROJECTILES);
					item.set("image", "inventions/weapons/projectiles/" + underscore(id));
				} else
				if (type.equals("Tanks")) {
					item.set("category", ResearchSubCategory.WEAPONS_TANKS);
					item.set("image", "inventions/weapons/tanks/" + underscore(id));
				} else
				if (type.equals("Vehicles")) {
					item.set("category", ResearchSubCategory.WEAPONS_VEHICLES);
					item.set("image", "inventions/weapons/vehicles/" + underscore(id));
				}
			} else
			if ("Buildings".equals(clazz)) {
				if (type.equals("Civil")) {
					item.set("category", ResearchSubCategory.BUILDINGS_CIVIL);
					item.set("image", "inventions/buildings/civil/" + underscore(id));
				} else
				if (type.equals("Military")) {
					item.set("category", ResearchSubCategory.BUILDINGS_MILITARY);
					item.set("image", "inventions/buildings/military/" + underscore(id));
				} else
				if (type.equals("Radars")) {
					item.set("category", ResearchSubCategory.BUILDINGS_RADARS);
					item.set("image", "inventions/buildings/radars/" + underscore(id));
				} else
				if (type.equals("Guns")) {
					item.set("category", ResearchSubCategory.BUILDINGS_GUNS);
					item.set("image", "inventions/buildings/guns/" + underscore(id));
				}
			}
			item.set("factory", oitem.childValue("factory"));
			item.set("research-cost", oitem.childValue("maxcost"));
			item.set("production-cost", oitem.childValue("buildcost"));
			item.set("level", oitem.childValue("level"));
			item.set("race", "human");
			
			item.set("requires", oitem.childValue("requires"));
			
			item.set("civil", oitem.childValue("civil"));
			item.set("mech", oitem.childValue("mechanic"));
			item.set("comp", oitem.childValue("computer"));
			item.set("ai", oitem.childValue("ai"));
			item.set("mil", oitem.childValue("military"));
			
			XElement oequipment = oitem.childElement("equipment");
			if (oequipment != null && oequipment.hasChildren()) {
				for (XElement oslot : oequipment.childrenWithName("slot")) {
					XElement slot = item.add("slot");
					
					slot.set("x", oslot.get("x"));
					slot.set("y", oslot.get("y"));
					slot.set("width", oslot.get("width"));
					slot.set("height", oslot.get("height"));
					slot.set("max", oslot.childValue("max"));
					slot.set("items", oslot.childValue("ids"));
				}
				for (XElement props : oequipment.childrenWithName("property")) {
					XElement prop = item.add("property");
					prop.set("name", props.get("name"));
					prop.set("value", props.content);
				}
			}
		}
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream("data/generic/campaign/main/tech.xml"), "UTF-8"));
		try {
			out.println("<?xml version='1.0' encoding='UTF-8'?>");
			out.println(root.toString());
		} finally {
			out.close();
		}

	}
	/**
	 * If the <code>id</code> ends in a digit, then it is prefixed by an underscore.
	 * @param id the input text
	 * @return the modified text
	 */
	static String underscore(String id) {
		if (Character.isDigit(id.charAt(id.length() - 1))) {
			return id.substring(0, id.length() - 1).toLowerCase() + "_" + id.substring(id.length() - 1);
		}
		return id.toLowerCase();
	}
}
