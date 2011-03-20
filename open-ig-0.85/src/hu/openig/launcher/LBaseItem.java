/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import hu.openig.utils.XML;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * The base item for various L* items.
 * @author akarnokd, 2010.10.31.
 */
public class LBaseItem {
	/** The descriptions in various languages. */
	public final List<LDescription> descriptions = new ArrayList<LDescription>();
	/**
	 * Retrieve the description for the given language.
	 * @param language the language code
	 * @return the description
	 */
	public String getDescription(String language) {
		for (LDescription d : descriptions) {
			if (d.language.equals(language)) {
				return d.description;
			}
		}
		return "";
	}
	/**
	 * Parse the element for &lt;desc&gt; tags.
	 * @param element the element to parse
	 */
	public void parse(Element element) {
		for (Element desc : XML.childrenWithName(element, "desc")) {
			LDescription descr = new LDescription();
			descr.language = desc.getAttribute("lang");
			descr.description = desc.getTextContent();
			descriptions.add(descr);
		}
	}
}
