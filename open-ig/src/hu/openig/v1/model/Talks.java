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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * The record for person talks.
 * @author karnok, 2009.10.10.
 * @version $Revision 1.0$
 */
public class Talks {
	/** The map of talk persons. */
	public final Map<String, TalkPerson> persons = new HashMap<String, TalkPerson>();
	/**
	 * Load the talks from the given game.
	 * @param rl the resource locator
	 * @param language the language
	 * @param data the data resource
	 */
	public void load(ResourceLocator rl, String language, String data) {
		process(rl.getXML(language, data), rl, language);
	}
	/**
	 * Process the document.
	 * @param root the root element
	 * @param rl the resource locator
	 * @param lang the current language
	 */
	protected void process(Element root, ResourceLocator rl, String lang) {
		for (Element talk : XML.childrenWithName(root, "talk")) {
			TalkPerson tp = new TalkPerson();
			tp.id = talk.getAttribute("with");
			persons.put(tp.id, tp);
			for (Element state : XML.childrenWithName(talk, "state")) {
				TalkState ts = new TalkState();
				ts.id = state.getAttribute("id");
				ts.picture = rl.getImage(lang, state.getAttribute("picture"));
				tp.states.put(ts.id, ts);
				for (Element tr : XML.childrenWithName(state, "transition")) {
					TalkSpeech tsp = new TalkSpeech();
					tsp.media = tr.getAttribute("media");
					tsp.text = tr.getAttribute("text");
					tsp.to = tr.getAttribute("to");
					ts.speeches.add(tsp);
				}
			}
		}
	}
}
