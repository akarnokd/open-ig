/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.ResearchLookup;
import hu.openig.utils.JavaUtils;
import hu.openig.utils.XML;
import hu.openig.utils.XML.XmlProcessor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a research technology.
 * @author karnokd
 *
 */
public class ResearchTech {
	/** The research identifier. */
	public String id;
	/** The research/manufacturing class. */
	public String clazz;
	/** The research/manufacturing class index. */
	public int clazzIndex;
	/** The research/manufacturing type. */
	public String type;
	/** The type index. */
	public int typeIndex;
	/** The technology index. */
	public int techIndex;
	/**
	 * The technology level for this technology. If the player level is below this value, 
	 * the research won't even show up in the dialog. 
	 */
	public int level;
	/** The minimum research time when the maximum money is used. */
	public int minTime;
	/** The maximum cost to research this technology. */
	public int maxCost;
	/** The build cost of the researched technology. */
	public int buildCost;
	/** Number of civil engineering centers required for this research. */
	public int civil;
	/** Number of mechanical engineering centers required for this research. */
	public int mechanic;
	/** Number of computer engineering centers required for this research. */
	public int computer;
	/** Number of AI engineering centers required for this research. */
	public int ai;
	/** Number of military engineering centers required for this research. */
	public int military;
	/** The image index for the small, large and animation images. */
	public int imageIndex;
	/** The prerequisite tech ids stored for late bindign of the prerequisites object. */
	Set<String> prereqs = JavaUtils.newHashSet();
	/** The technologycal prerequisites. */
	public final List<ResearchTech> requires = JavaUtils.newArrayList();
	/** The type of production building required to produce this technology. */
	public String factory;
	/** List of the editable slots on this technology. */
	public final List<Slot> slots = JavaUtils.newArrayList();
	/** Fixed/uneditable slots on this technology. */
	public final List<FixedSlot> fixedSlots = JavaUtils.newArrayList();
	/** Numerical properties of this research technology. */
	public final Map<String, Integer> values = JavaUtils.newHashMap();
	/** Textual properties of this research technology. */
	public final Map<String, String> properties = JavaUtils.newHashMap();
	/** Image displayed on the equipment screen's fleet display. */
	public BufferedImage smallImage;
	/** Image displayed on the equipment screen's customization panel. */
	public BufferedImage customImage;
	/** The research/build image used. */
	public BufferedImage rbImage;
	/** The wired research/build image used. */
	public BufferedImage rbImageWired;
	/** The animation file to display in research/production screens. */
	public File animation;
	/** The set of technology ids in which this research is available. */
	public final Set<String> techIDs = JavaUtils.newHashSet();
	/** The technology display name. */
	public String name;
	/** The textual description lines. */
	public String[] description = new String[3];
	/**
	 * Parses and processes a research XML.
	 * @param resource the name of the resource
	 * @param lookup the building images lookup
	 * @return list of planets
	 */
	public static Map<String, ResearchTech> parse(String resource, final ResearchLookup lookup) {
		Map<String, ResearchTech> research = XML.parseResource(resource, new XmlProcessor<Map<String, ResearchTech>>() {
			@Override
			public Map<String, ResearchTech> process(Document doc) {
				return ResearchTech.process(doc, lookup);
			}
		});
		return research != null ? research : new HashMap<String, ResearchTech>();
	}
	/**
	 * Processes a tech.xml document.
	 * @param root the document 
	 * @param lookup callback for research names and images.
	 * @return the list of buildings
	 */
	private static Map<String, ResearchTech> process(Document root, ResearchLookup lookup) {
		Map<String, ResearchTech> research = JavaUtils.newHashMap();
		for (Element item : XML.childrenWithName(root.getDocumentElement(), "item")) {
			ResearchTech rt = new ResearchTech();
			rt.id = item.getAttribute("id");
			research.put(rt.id, rt);
			int index = Integer.parseInt(item.getAttribute("index"));
			int descIndex = Integer.parseInt(item.getAttribute("desc-index"));
			rt.name = lookup.getResearchName(index);
			rt.description = lookup.getResearchDescription(descIndex);
			for (Element e : XML.children(item)) {
				String n = e.getNodeName();
				if ("class".equals(n)) {
					rt.clazz = e.getTextContent();
				} else
				if ("type".equals(n)) {
					rt.type = e.getTextContent();
				} else
				if ("level".equals(n)) {
					rt.level = Integer.parseInt(e.getTextContent());
				} else
				if ("mintime".equals(n)) {
					rt.minTime = Integer.parseInt(e.getTextContent());
				} else
				if ("maxcost".equals(n)) {
					rt.maxCost = Integer.parseInt(e.getTextContent());
				} else
				if ("buildcost".equals(n)) {
					rt.buildCost = Integer.parseInt(e.getTextContent());
				} else
				if ("civil".equals(n)) {
					rt.civil = Integer.parseInt(e.getTextContent());
				} else
				if ("mechanic".equals(n)) {
					rt.mechanic = Integer.parseInt(e.getTextContent());
				} else
				if ("computer".equals(n)) {
					rt.computer = Integer.parseInt(e.getTextContent());
				} else
				if ("ai".equals(n)) {
					rt.ai = Integer.parseInt(e.getTextContent());
				} else
				if ("military".equals(n)) {
					rt.military = Integer.parseInt(e.getTextContent());
				} else
				if ("image-index".equals(n)) {
					rt.imageIndex = Integer.parseInt(e.getTextContent());
					rt.clazzIndex = rt.imageIndex / 100;
					rt.typeIndex = rt.imageIndex / 10 % 10;
					rt.techIndex = rt.imageIndex % 10;
				} else
				if ("factory".equals(n)) {
					rt.factory = e.getTextContent();
				} else
				if ("techids".equals(n)) {
					rt.techIDs.addAll(Arrays.asList(e.getTextContent().split("\\s*,\\s*")));
				} else
				if ("requires".equals(n)) {
					rt.prereqs.addAll(Arrays.asList(e.getTextContent().split("\\s*,\\s*")));
				} else
				if ("equipment".equals(n)) {
					for (Element e1 : XML.children(e)) {
						if ("slot".equals(e1.getNodeName())) {
							Slot sl = new Slot();
							sl.x = Integer.parseInt(e1.getAttribute("x"));
							sl.y = Integer.parseInt(e1.getAttribute("y"));
							sl.width = Integer.parseInt(e1.getAttribute("width"));
							sl.height = Integer.parseInt(e1.getAttribute("height"));
							sl.type = XML.childValue(e1, "type");
							sl.max = Integer.parseInt(XML.childValue(e1, "max"));
							sl.idset.addAll(Arrays.asList(XML.childValue(e1, "ids").split("\\s*,\\s*")));
							rt.slots.add(sl);
						} else
						if ("fixedslot".equals(e1.getNodeName())) {
							FixedSlot fs = new FixedSlot();
							fs.type = XML.childValue(e1, "type");
							fs.eqId = XML.childValue(e1, "id");
							fs.value = Integer.parseInt(XML.childValue(e1, "value"));
							rt.fixedSlots.add(fs);
						} else
						if ("property".equals(e1.getNodeName())) {
							String n1 = e1.getAttribute("name");
							String v = e1.getTextContent();
							try {
								rt.values.put(n1, Integer.parseInt(v));
							} catch (NumberFormatException ex) {
								rt.properties.put(n1, v) ;
							}
						}
					}
				}
			}
		}
		// cross bind all technologies
		for (ResearchTech rt : research.values()) {
			for (String s : rt.prereqs) {
				ResearchTech r = research.get(s);
				if (r == null) {
					throw new RuntimeException(String.format("Prerequisite '%s' for technology '%s' not found.", s, rt.id));
				}
				rt.requires.add(r);
			}
			int i = 0;
			for (Slot sl : rt.slots) {
				for (String s : sl.idset) {
					ResearchTech r = research.get(s);
					if (r == null) {
						throw new RuntimeException(String.format("Technology '%s' in '%s' slot %d not found.", s, rt.id, i));
					}
					sl.ids.add(r);
				}
				i++;
			}
			i = 0;
			for (FixedSlot fs : rt.fixedSlots) {
				ResearchTech r = research.get(fs.eqId);
				if (r == null) {
					throw new RuntimeException(String.format("Technology '%s' in '%s' fixed slot %d not found.", fs.eqId, rt.id, i));
				}
				fs.id = r;
				i++;
			}
		}
		return research;
	}
	/** Comparator to order by image index. */
	public static final Comparator<ResearchTech> BY_IMAGE_INDEX = new Comparator<ResearchTech>() {
		@Override
		public int compare(ResearchTech o1, ResearchTech o2) {
			return o1.imageIndex - o2.imageIndex;
		}
	};
}
