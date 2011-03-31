/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchSubCategory;
import hu.openig.utils.XElement;

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
				item.set("category", ResearchMainCategory.SPACESHIPS);
				if (type.equals("Fighters")) {
					item.set("sub-category", ResearchSubCategory.SPACESHIPS_FIGHTERS);
					item.set("image", "inventions/spaceships/fighters/" + id.toLowerCase());
				} else
				if (type.equals("Cruisers")) {
					item.set("sub-category", ResearchSubCategory.SPACESHIPS_CRUISERS);
					item.set("image", "inventions/spaceships/cruisers/" + id.toLowerCase());
				} else
				if (type.equals("Battleships")) {
					item.set("sub-category", ResearchSubCategory.SPACESHIPS_BATTLESHIPS);
					item.set("image", "inventions/spaceships/battleships/" + id.toLowerCase());
				} else
				if (type.equals("Satellites")) {
					item.set("sub-category", ResearchSubCategory.SPACESHIPS_SATELLITES);
					item.set("image", "inventions/spaceships/satellites/" + id.toLowerCase());
				} else
				if (type.equals("SpaceStations")) {
					item.set("sub-category", ResearchSubCategory.SPACESHIPS_STATIONS);
					item.set("image", "inventions/spaceships/stations/" + id.toLowerCase());
				}
			} else
			if ("Equipments".equals(clazz)) {
				item.set("category", ResearchMainCategory.EQUIPMENT);
				if (type.equals("HyperDrives")) {
					item.set("sub-category", ResearchSubCategory.EQUIPMENT_HYPERDRIVES);
					item.set("image", "inventions/equipments/hyperdrives/" + id.toLowerCase());
				} else
				if (type.equals("Modules")) {
					item.set("sub-category", ResearchSubCategory.EQUIPMENT_MODULES);
					item.set("image", "inventions/equipments/modules/" + id.toLowerCase());
				} else
				if (type.equals("Radars")) {
					item.set("sub-category", ResearchSubCategory.EQUIPMENT_RADARS);
					item.set("image", "inventions/equipments/radars/" + id.toLowerCase());
				} else
				if (type.equals("Shields")) {
					item.set("sub-category", ResearchSubCategory.EQUIPMENT_SHIELDS);
					item.set("image", "inventions/equipments/shields/" + id.toLowerCase());
				}
			} else
			if ("Weapons".equals(clazz)) {
				item.set("category", ResearchMainCategory.WEAPONS);
				if (type.equals("Lasers")) {
					item.set("sub-category", ResearchSubCategory.WEAPONS_LASERS);
					item.set("image", "inventions/weapons/lasers/" + id.toLowerCase());
				} else
				if (type.equals("Cannons")) {
					item.set("sub-category", ResearchSubCategory.WEAPONS_CANNONS);
					item.set("image", "inventions/weapons/cannons/" + id.toLowerCase());
				} else
				if (type.equals("Projectiles")) {
					item.set("sub-category", ResearchSubCategory.WEAPONS_PROJECTILES);
					item.set("image", "inventions/weapons/projectiles/" + id.toLowerCase());
				} else
				if (type.equals("Tanks")) {
					item.set("sub-category", ResearchSubCategory.WEAPONS_TANKS);
					item.set("image", "inventions/weapons/tanks/" + id.toLowerCase());
				} else
				if (type.equals("Vehicles")) {
					item.set("sub-category", ResearchSubCategory.WEAPONS_VEHICLES);
					item.set("image", "inventions/weapons/vehicles/" + id.toLowerCase());
				}
			} else
			if ("Buildings".equals("clazz")) {
				item.set("category", ResearchMainCategory.BUILDINS);
				if (type.equals("Civil")) {
					item.set("sub-category", ResearchSubCategory.BUILDINGS_CIVIL);
					item.set("image", "inventions/buildings/civil/" + id.toLowerCase());
				} else
				if (type.equals("Military")) {
					item.set("sub-category", ResearchSubCategory.BUILDINGS_MILITARY);
					item.set("image", "inventions/buildings/military/" + id.toLowerCase());
				} else
				if (type.equals("Radars")) {
					item.set("sub-category", ResearchSubCategory.BUILDINGS_RADARS);
					item.set("image", "inventions/buildings/radars/" + id.toLowerCase());
				} else
				if (type.equals("Guns")) {
					item.set("sub-category", ResearchSubCategory.BUILDINGS_GUNS);
					item.set("image", "inventions/buildings/guns/" + id.toLowerCase());
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
			if (oequipment != null) {
				XElement equipment = item.add("equipment");
				for (XElement oslot : oequipment.childrenWithName("slot")) {
					XElement slot = equipment.add("slot");
					
					slot.set("x", oslot.get("x"));
					slot.set("y", oslot.get("y"));
					slot.set("width", oslot.get("width"));
					slot.set("height", oslot.get("height"));
					slot.set("max", oslot.childValue("max"));
					slot.set("items", oslot.childValue("ids"));
				}
				for (XElement props : oequipment.childrenWithName("property")) {
					XElement prop = equipment.add("property");
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

}
