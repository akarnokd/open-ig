/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ani;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class to manage frame rate and delay constants in framerates.xml
 * @author karnokd, 2009.02.22.
 * @version $Revision 1.0$
 */
public class Framerates {
	/**
	 * The particular FPS and sound delay values for an
	 * animation file and type.
	 * @author karnokd, 2009.02.22.
	 * @version $Revision 1.0$
	 */
	public static class Rates {
		/** The frames per second. */
		public final double fps;
		/** The delay in 1/22050 seconds. */
		public final int delay;
		/**
		 * Constructor. Initializes the final fields
		 * @param fps the frame rate
		 * @param delay the sound delay in 1/22050 seconds
		 */
		public Rates(double fps, int delay) {
			this.fps = fps;
			this.delay = delay;
		}
	}
	/** The default rates. */
	private static final Rates DEFAULT_RATES = new Rates(17.89, 0);
	/** The map of rates. */
	private final Map<String, Map<Integer, Rates>> rates;
	/**
	 * Constructor. Initializes the map and fills it.
	 */
	public Framerates() {
		rates = new HashMap<String, Map<Integer, Rates>>();
		loadFromResource("/hu/openig/res/framerates.xml");
	}
	/**
	 * Load values from resource
	 * @param resource the resource path
	 */
	private void loadFromResource(String resource) {
		try {
			InputStream in = getClass().getResourceAsStream(resource);
			if (in != null) {
				try {
					DocumentBuilderFactory db = DocumentBuilderFactory.newInstance();
					DocumentBuilder doc = db.newDocumentBuilder();
					process(doc.parse(in));
				} finally {
					in.close();
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (SAXException ex) {
			ex.printStackTrace();
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Process the xml document.
	 * @param doc the DOM document
	 */
	private void process(Document doc) {
		Element root = doc.getDocumentElement();
		for (Element ani : childrenWithName(root, "ani")) {
			String name = ani.getAttribute("name");
			Map<Integer, Rates> rt = rates.get(name);
			if (rt == null) {
				rt = new HashMap<Integer, Rates>();
				rates.put(name, rt);
			}
			for (Element type : childrenWithName(ani, "type")) {
				int typeInt = Integer.parseInt(type.getAttribute("value"));
				double fps = Double.parseDouble(type.getAttribute("fps"));
				int delay = Integer.parseInt(type.getAttribute("delay"));
				rt.put(typeInt, new Rates(fps, delay));
			}
		}
	}
	/**
	 * Returns a list of the child elements with the given name
	 * @param parent the parent node
	 * @param name the name
	 * @return the list of elements with the given name
	 */
	public List<Element> childrenWithName(Element parent, String name) {
		List<Element> result = new ArrayList<Element>();
		NodeList nl = parent.getChildNodes();
		for (int i = 0, count = nl.getLength(); i < count; i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				Element e = (Element)n;
				if (name.equals(e.getNodeName())) {
					result.add(e);
				}
			}
		}
		return result;
	}
	/**
	 * Get the rates for a filename and type.
	 * @param filename the file name
	 * @param type the type index
	 * @return the rates, not null
	 */
	public Rates getRates(String filename, int type) {
		// extract last part
		int idx = filename.lastIndexOf('/');
		if (idx < 0) {
			idx = filename.lastIndexOf('\\');
		}
		if (idx >= 0) {
			filename = filename.substring(idx + 1);
		}
		Rates r = null;
		Map<Integer, Rates> typeRates = rates.get(filename.toUpperCase());
		if (typeRates != null) {
			r = typeRates.get(type);
		}
		return r != null ? r : DEFAULT_RATES;
	}
}
