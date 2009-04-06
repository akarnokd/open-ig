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
 * @author karnkd
 */
public class GMRace {
	/** The race index. */
	public int index;
	/** The race identifier. */
	public String id;
	/** The race name. */
	public String name;
	/** The technology index. */
	public int techIndex;
	/** The ship icon index. */
	public int shipIndex;
	/** The normal text color for the race. */
	public int color;
	/** The small text color for the race. */
	public int smallColor;
	/**
	 * Parses and processes a race resource XML.
	 * @param resource
	 * @return the list of races
	 */
	public static List<GMRace> parse(String resource) {
		List<GMRace> planet = XML.parseResource(resource, new XmlProcessor<List<GMRace>>() {
			@Override
			public List<GMRace> process(Document doc) {
				return GMRace.process(doc);
			}
		});
		return planet != null ? planet : new ArrayList<GMRace>();
	}
	/**
	 * Process a document containing race information. 
	 * @param doc the DOM document to process
	 * @return the list of race objects. 
	 */
	private static List<GMRace> process(Document doc) {
		List<GMRace> result = new ArrayList<GMRace>();
		for (Element e : XML.childrenWithName(doc.getDocumentElement(), "race")) {
			GMRace r = new GMRace();
			r.index = Integer.parseInt(e.getAttribute("index"));
			r.id = e.getAttribute("id");
			r.shipIndex = Integer.parseInt(XML.childValue(e, "ship"));
			r.techIndex = Integer.parseInt(XML.childValue(e, "tech"));
			Element e1 = XML.childElement(e, "color");
			r.color = Integer.parseInt(e1.getAttribute("normal"), 16);
			r.smallColor = Integer.parseInt(e1.getAttribute("small"), 16);
			result.add(r);
		}
		return result;
	}
}
