/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.utils.XML;
import hu.openig.utils.XML.XmlProcessor;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Race related attributes.
 * Race name should be determined from the id and looked up via labels.
 * @author karnkd
 */
public class GameRace {
	/** The race index. */
	public int index;
	/** The race identifier. */
	public String id;
	/** The technology identifier. */
	public String techId;
	/** The ship icon index. */
	public int shipIndex;
	/** The normal text color for the race. */
	public int color;
	/** The small text color for the race. */
	public int smallColor;
	/**
	 * Parses and processes a race resource XML.
	 * @param resource the resource location
	 * @return the list of races
	 */
	public static List<GameRace> parse(String resource) {
		List<GameRace> planet = XML.parseResource(resource, new XmlProcessor<List<GameRace>>() {
			@Override
			public List<GameRace> process(Document doc) {
				return GameRace.process(doc);
			}
		});
		return planet != null ? planet : new ArrayList<GameRace>();
	}
	/**
	 * Process a document containing race information. 
	 * @param doc the DOM document to process
	 * @return the list of race objects. 
	 */
	private static List<GameRace> process(Document doc) {
		List<GameRace> result = new ArrayList<GameRace>();
		for (Element e : XML.childrenWithName(doc.getDocumentElement(), "race")) {
			GameRace r = new GameRace();
			r.index = Integer.parseInt(e.getAttribute("index"));
			r.id = e.getAttribute("id");
			r.shipIndex = Integer.parseInt(XML.childValue(e, "ship"));
			r.techId = XML.childValue(e, "tech");
			Element e1 = XML.childElement(e, "color");
			r.color = Integer.parseInt(e1.getAttribute("normal"), 16);
			r.smallColor = Integer.parseInt(e1.getAttribute("small"), 16);
			result.add(r);
		}
		return result;
	}
}
