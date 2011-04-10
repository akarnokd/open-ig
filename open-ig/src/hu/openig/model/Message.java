/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.XElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	@Override
	public int compareTo(Message o) {
		int c = priority - o.priority;
		if (c == 0) {
			c = (timestamp < o.timestamp ? -1 : (timestamp > o.timestamp ? 1 : 0));
		}
		return c;
	}
	/** 
	 * Save the message.
	 * @param xmessage the target element 
	 * @param sdf the date formatter
	 */
	public void save(XElement xmessage, SimpleDateFormat sdf) {
		xmessage.set("priority", priority);
		xmessage.set("gametime", sdf.format(new Date(gametime)));
		xmessage.set("timestamp", sdf.format(new Date(timestamp)));
		// FIXME other properties
	}
	/** 
	 * Load the message. 
	 * @param xmessage the source element
	 * @param sdf the date formatter 
	 */
	public void load(XElement xmessage, SimpleDateFormat sdf) {
		priority = xmessage.getInt("priority");
		try {
			gametime = sdf.parse(xmessage.get("gametime")).getTime();
			timestamp = sdf.parse(xmessage.get("timestamp")).getTime();
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		// FIXME other properties
	}
}
