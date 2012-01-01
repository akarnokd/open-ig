/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.res;

import hu.openig.utils.XML;
import hu.openig.utils.XML.XmlProcessor;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author karnokd, 2009.05.11.
 * @version $Revision 1.0$
 */
public class Labels {
	/** The entire translations, key to language to text. */
	public final Map<String, Map<String, String>> texts = new HashMap<String, Map<String, String>>();
	/** The list of languages. */
	public String[] languages;
	/**
	 * Parses and processes a race resource XML.
	 * @param resource the resource location
	 * @return the list of races
	 */
	public static Labels parse(String resource) {
		Labels planet = XML.parseResource(resource, new XmlProcessor<Labels>() {
			@Override
			public Labels process(Document doc) {
				return Labels.process(doc);
			}
		});
		return planet != null ? planet : new Labels();
	}
	/**
	 * Process a document containing race information. 
	 * @param doc the DOM document to process
	 * @return the list of race objects. 
	 */
	private static Labels process(Document doc) {
		Labels labels = new Labels();
		Element de = doc.getDocumentElement();
		labels.languages = XML.childValue(de, "langs").split(",");
		for (Element e : XML.childrenWithName(de, "entry")) {
			String key = e.getAttribute("key");
			Map<String, String> values = new HashMap<String, String>();
			for (String l : labels.languages) {
				String translation = XML.childValue(e, l);
				if (translation.isEmpty()) {
					throw new AssertionError(String.format("Missing translation for %s in language %s", key, l));
				}
				values.put(l, translation);
			}
			labels.texts.put(key, values);
		}
		return labels;
	}
	/**
	 * Returns the translation for the given key and language.
	 * @param key the key
	 * @param language the language
	 * @return the translation
	 */
	public String get(String key, String language) {
		Map<String, String> values = texts.get(key);
		if (values == null) {
			throw new AssertionError("Missing translations for " + key);
		}
		String l = values.get(language);
		if (l == null) {
			throw new AssertionError("Missing translation of " + key + " in language " + language);
		}
		return l;
	}
}
