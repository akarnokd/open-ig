/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import hu.openig.utils.XML;
import hu.openig.v1.ResourceLocator.ResourcePlace;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * The label manager with option for game specific sublabels.
 * @author karnok, 2009.10.25.
 * @version $Revision 1.0$
 */
public class Labels {
	/** The label map. */
	protected final Map<String, String> map = new HashMap<String, String>();
	/**
	 * Load the language file(s).
	 * @param rl the resource locator
	 * @param language the language
	 * @param game the optional game naming
	 */
	public void load(ResourceLocator rl, String language, String game) {
		map.clear();
		ResourcePlace rp = rl.get(language, "labels", ResourceType.DATA);
		if (rp == null) {
			// TODO log
			throw new AssertionError("Missing resource: labels");
		}
		parse(rp);
		if (game != null) {
			rp = rl.get(language, game + "/labels", ResourceType.DATA);
			if (rp != null) {
				parse(rp);
			}
		}
	}
	/**
	 * Parse the given resource.
	 * @param rp the resource place
	 */
	private void parse(ResourcePlace rp) {
		InputStream in = rp.open();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(in);
			process(doc.getDocumentElement());
		} catch (IOException ex) {
			// TODO log
			ex.printStackTrace();
		} catch (SAXException ex) {
			// TODO log
			ex.printStackTrace();
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
				// TODO log
				ex.printStackTrace();
			}
		}
	}
	/**
	 * Process the document.
	 * @param root the root element
	 */
	protected void process(Element root) {
		for (Element e : XML.childrenWithName(root, "entry")) {
			map.put(e.getAttribute("key"), e.getTextContent());
		}
	}
	/**
	 * Returns a given key.
	 * @param key the key
	 * @return the associated value
	 */
	public String get(String key) {
		return map.get(key);
	}
}
