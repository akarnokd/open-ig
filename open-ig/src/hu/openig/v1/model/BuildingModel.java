/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.v1.model;

import hu.openig.utils.XML;
import hu.openig.v1.core.ResourceLocator;
import hu.openig.v1.core.ResourceType;
import hu.openig.v1.core.Tile;
import hu.openig.v1.core.ResourceLocator.ResourcePlace;
import hu.openig.v1.model.BuildingType.Resource;
import hu.openig.v1.model.BuildingType.TileSet;
import hu.openig.v1.model.BuildingType.Upgrade;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * The building models.
 * @author karnokd
 */
public class BuildingModel {
	/** The list of all building types. */
	public final Map<String, BuildingType> buildings = new LinkedHashMap<String, BuildingType>();
	/**
	 * Process the contents of the buildings definition.
	 * @param data the buildings definition
	 * @param rl the resource locator
	 * @param language the language
	 */
	public void processBuildings(ResourceLocator rl, String language, String data) {
		Element buildings = rl.getXML(language, data);
		for (Element building : XML.childrenWithName(buildings, "building")) {
			BuildingType b = new BuildingType();
			
			b.id = building.getAttribute("id");
			b.label = building.getAttribute("label");
			b.description = b.label + ".desc";
			
			Element gfx = XML.childElement(building, "graphics");
			String pattern = gfx.getAttribute("base");
			for (Element r : XML.childrenWithName(gfx, "tech")) {
				TileSet ts = new TileSet();
				
				String rid = r.getAttribute("id");
				int width = Integer.parseInt(r.getAttribute("width"));
				int height = Integer.parseInt(r.getAttribute("height"));
				
				String normalImg = String.format(pattern, rid);
				String normalLight = normalImg + "_lights";
				String damagedImg = normalImg + "_damaged";
				
				BufferedImage lightMap = null;
				ResourcePlace rp = rl.get(language, normalLight, ResourceType.IMAGE);
				if (rp != null) {
					lightMap = rl.getImage(language, normalLight);
				}
				ts.normal = new Tile(width, height, rl.getImage(language, normalImg), lightMap);
				ts.damaged = new Tile(width, height, rl.getImage(language, damagedImg), null); // no lightmap for damaged building
				b.tileset.put(rid, ts);
			}
			Element bld = XML.childElement(building, "build");
			b.cost = Integer.parseInt(bld.getAttribute("cost"));
			b.kind = bld.getAttribute("kind");
			String limit = bld.getAttribute("limit");
			if ("*".equals(limit)) {
				b.limit = Integer.MAX_VALUE;
			} else {
				b.limit = Integer.parseInt(limit);
			}
			b.research = bld.getAttribute("research");
			String except = bld.getAttribute("except");
			if (except != null && !except.isEmpty()) {
				b.except.addAll(Arrays.asList(except.split("\\s*,\\s*")));
			}
			Element op = XML.childElement(building, "operation");
			b.percentable = "true".equals(op.getAttribute("percent"));
			for (Element re : XML.childrenWithName(op, "resource")) {
				Resource res = new Resource();
				res.type = re.getAttribute("type");
				res.amount = Float.parseFloat(re.getTextContent());
				b.resources.put(res.type, res);
				if ("true".equals(re.getAttribute("display"))) {
					b.primary = res;
				}
			}
			
			Element ug = XML.childElement(building , "upgrades");
			for (Element u : XML.childrenWithName(ug, "upgrade")) {
				Upgrade upg = new Upgrade();
				upg.description = u.getAttribute("desc");
				for (Element re : XML.childrenWithName(op, "resource")) {
					Resource res = new Resource();
					res.type = re.getAttribute("type");
					res.amount = Float.parseFloat(re.getTextContent());
					upg.resources.put(res.type, res);
				}
				b.upgrades.add(upg);
			}
			
			this.buildings.put(b.id, b);
		}
	}

}
