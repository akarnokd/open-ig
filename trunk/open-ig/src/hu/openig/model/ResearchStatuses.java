/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.MessageArray;
import hu.openig.net.MessageObject;
import hu.openig.net.MissingAttributeException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The available and in-progress
 * research status record.
 * @author akarnokd, 2013.04.27.
 */
public class ResearchStatuses implements MessageObjectIO {
	/** Indicates that the research is globally paused. */
	public boolean paused;
	/** The current running research. */
	public String runningResearch;
	/** The completed research. */
	public final Map<String, List<String>> availableResearch = new LinkedHashMap<>();
	/** The in-progress research. */
	public final List<ResearchStatus> researches = new ArrayList<>();
	@Override
	public void fromMessage(MessageObject mo) {
		paused = mo.getBoolean("paused");
		runningResearch = mo.getStringObject("running");
		for (MessageObject o : mo.getArray("available").objects()) {
			String rid = o.getString("type");
			List<String> list = new ArrayList<>();
			for (Object lo : o.getArray("list")) {
				if (lo instanceof String) {
					list.add((String)lo);
				} else {
					throw new MissingAttributeException("list contains non-string element " + lo);
				}
			}
			availableResearch.put(rid, list);
		}
		for (MessageObject o : mo.getArray("researches").objects()) {
			ResearchStatus rs = new ResearchStatus();
			rs.fromMessage(o);
			researches.add(rs);
		}
	}
	@Override
	public MessageObject toMessage() {
		MessageObject result = new MessageObject(objectName());
		
		result.set("paused", paused);
		result.set("running", runningResearch);
		
		MessageArray avail = new MessageArray(null);
		result.set("available", avail);
		
		for (Map.Entry<String, List<String>> e : availableResearch.entrySet()) {
			MessageObject a = new MessageObject("AVAILABLE");
			a.set("type", e.getKey());
			MessageArray list = new MessageArray(null);
			a.set("list", list);
			for (String s : e.getValue()) {
				list.add(s);
			}
			avail.add(a);
		}
		MessageArray rss = new MessageArray(null);
		result.set("researches", rss);
		
		for (ResearchStatus rs : researches) {
			rss.add(rs.toMessage());
		}
		
		return result;
	}
	@Override
	public String objectName() {
		return "RESEARCHES";
	}
}
