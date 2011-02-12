/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.ResourceLocator;
import hu.openig.utils.XElement;

import java.util.HashMap;
import java.util.Map;

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
	protected void process(XElement root, ResourceLocator rl, String lang) {
		for (XElement talk : root.childrenWithName("talk")) {
			TalkPerson tp = new TalkPerson();
			tp.id = talk.get("with");
			persons.put(tp.id, tp);
			for (XElement state : talk.childrenWithName("state")) {
				TalkState ts = new TalkState();
				ts.id = state.get("id");
				ts.picture = rl.getImage(lang, state.get("picture"));
				tp.states.put(ts.id, ts);
				for (XElement tr : state.childrenWithName("transition")) {
					TalkSpeech tsp = new TalkSpeech();
					tsp.media = tr.get("media");
					tsp.text = tr.get("text");
					tsp.to = tr.get("to");
					ts.speeches.add(tsp);
				}
			}
		}
	}
}
