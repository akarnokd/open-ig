/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.SurfaceType;
import hu.openig.utils.XML;
import hu.openig.utils.XML.XmlProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Game model planet record.
 * @author karnokd, 2009.02.02.
 * @version $Revision 1.0$
 */
public class GMPlanet {
	/** The surface type 1-7. */
	public SurfaceType surfaceType;
	/** The surface variant 1-9. */
	public int surfaceVariant;
	/** Rotation phase index on the starmap. */
	public int rotationPhase;
	/** The planet name. */
	public String name;
	/** Show planet name? */
	public boolean showName;
	/** Show radar region? */
	public boolean showRadar;
	/** The radar circle radius. */
	public int radarRadius;
	/** Center X coordinate on the starmap. */
	public int x;
	/** Center Y coordinate on the starmap. */
	public int y;
	/** Display planet on the starmap? */
	public boolean visible;
	/** The color of the name. */
	public int nameColor;
	/** Rotation direction: true - forward, false - backward. */
	public boolean rotationDirection;
	/** The planet's unique id. */
	public String id;
	/** Owner race name. */
	public String ownerRace;
	/** Population race name. */
	public String populationRace;
	/** Population count. */
	public int population;
	/** Last day's population growth. */
	public int populationGrowth;
	/** Object names in orbit. */
	public final List<String> inOrbit = new ArrayList<String>();
	/** The planet size relative to the largest size, e.g. -2, -1, 0 .*/
	public int size;
	/**
	 * Parses and processes a planetary resource XML.
	 * @param resource the name of the resource
	 * @return list of planets
	 */
	public static List<GMPlanet> parse(String resource) {
		List<GMPlanet> planet = XML.parseResource(resource, new XmlProcessor<List<GMPlanet>>() {
			@Override
			public List<GMPlanet> process(Document doc) {
				return GMPlanet.process(doc);
			}
		});
		return planet != null ? planet : new ArrayList<GMPlanet>();
	}
	/**
	 * Processes a planets.xml document.
	 * @param root the document 
	 * @return the list of planets
	 */
	private static List<GMPlanet> process(Document root) {
		List<GMPlanet> result = new ArrayList<GMPlanet>();
		for (Element planet : XML.childrenWithName(root.getDocumentElement(), "planet")) {
			GMPlanet p = new GMPlanet();
			p.id = planet.getAttribute("id");
			p.name = XML.childValue(planet, "name");
			p.x = Integer.parseInt(XML.childValue(planet, "location-x")) * 2;
			p.y = Integer.parseInt(XML.childValue(planet, "location-y")) * 2;
			p.surfaceType = SurfaceType.MAP.get(XML.childValue(planet, "type"));
			p.surfaceVariant = Integer.parseInt(XML.childValue(planet, "variant"));
			p.ownerRace = XML.childValue(planet, "race");
			p.populationRace = p.ownerRace;
			p.size = Integer.parseInt(XML.childValue(planet, "size")) - 8;
			p.rotationDirection = "RL".equals(XML.childValue(planet, "rotate"));
			p.population = Integer.parseInt(XML.childValue(planet, "populate"));
			String orbit = XML.childValue(planet, "in-orbit");
			p.showName = true;
			p.showRadar = true;
			p.visible = true;
			p.nameColor = 0xFCB000;
			if (!"-".equals(orbit)) {
				p.inOrbit.addAll(Arrays.asList(orbit.split("\\\\s*,\\\\s*")));
			}
			result.add(p);
		}
		return result;
	}
}
