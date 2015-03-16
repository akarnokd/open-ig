/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

import java.text.ParseException;
import java.util.Date;

/**
 * A global message to display in the status bar.
 * @author akarnokd, Apr 10, 2011
 */
public class Message implements Comparable<Message> {
	/** The message priority. */
	public int priority;
	/** The game time this message was submitted. */
	public long gametime;
	/** The real time this message was submitted. */
	public long timestamp;
	/** The optional sound to play. */
	public SoundType sound;
	/** The targeted planet. */
	public Planet targetPlanet;
	/** The targeted fleet. */
	public Fleet targetFleet;
	/** The targeted product. */
	public ResearchType targetProduct;
	/** The targeted fleet. */
	public ResearchType targetResearch;
	/** The formatted text to display. */
	public String text;
	/** The parameter value. */
	public String value;
	/** The parameter value label to lookup. */
	public String label;
	@Override
	public int compareTo(Message o) {
		int c = priority < o.priority ? -1 : (priority > o.priority ? 1 : 0);
		if (c == 0) {
			c = (timestamp < o.timestamp ? -1 : (timestamp > o.timestamp ? 1 : 0));
		}
		return -c;
	}

	/** 
	 * Save the message.
	 * @param xmessage the target element 
	 */
	public void save(XElement xmessage) {
		xmessage.set("priority", priority);
		xmessage.set("gametime", ModelUtils.format(new Date(gametime)));
		xmessage.set("timestamp", ModelUtils.format(new Date(timestamp)));
		if (sound != null) {
			xmessage.set("sound", sound.toString());
		}
		if (targetPlanet != null) {
			xmessage.set("planet", targetPlanet.id);
		}
		if (targetFleet != null) {
			xmessage.set("fleet", targetFleet.id);
		}
		if (targetResearch != null) {
			xmessage.set("research", targetResearch.id);
		}
		if (targetProduct != null) {
			xmessage.set("product", targetProduct.id);
		}
		if (value != null) {
			xmessage.set("value", value);
		}
		if (label != null) {
			xmessage.set("label", label);
		}
		xmessage.set("text", text);
	}
	/** 
	 * Load the message. 
	 * @param xmessage the source element
	 * @param world the world
	 */
	public void load(XElement xmessage, World world) {
		priority = xmessage.getInt("priority");
		try {
			gametime = ModelUtils.parse(xmessage.get("gametime")).getTime();
			timestamp = ModelUtils.parse(xmessage.get("timestamp")).getTime();
		} catch (ParseException ex) {
			Exceptions.add(ex);
		}
		String s = xmessage.get("sound", null);
        if (s != null) {
            sound = SoundType.valueOf(s);
        }
		
		s = xmessage.get("planet", null);
		if (s != null) {
			targetPlanet = world.planets.get(s);
		}
		s = xmessage.get("fleet", null);
		if (s != null) {
			targetFleet = world.findFleet(Integer.parseInt(s));
		}
		s = xmessage.get("product", null);
		if (s != null) {
			targetProduct = world.researches.get(s);
		}
		
		s = xmessage.get("research", null);
		if (s != null) {
			targetResearch = world.researches.get(s);
		}
		value = xmessage.get("value", null);
		
		label = xmessage.get("label", null);
		
		text = xmessage.get("text");
	}
}
