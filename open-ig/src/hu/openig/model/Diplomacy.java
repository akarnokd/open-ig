/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The diplomatic relation options of a particular non-user player.
 * @author akarnokd, Apr 22, 2011
 */
public class Diplomacy {
	/** A negotiation entry for the player. */
	public static class Negotiate {
		/** The negotiation's type. */
		public NegotiateType type;
		/** The available approaches. */
		public final List<Approach> approaches = new ArrayList<Approach>();
		/** The available responses. */
		public final List<Response> responses = new ArrayList<Response>();
	}
	/** An approach entry for a negotiation or call. */
	public static class Approach {
		/** The approach type. */
		public ApproachType type;
		/** The label to use. */
		public String label;
	}
	/** The alien's response. */
	public static class Response {
		/** The approach type. */
		public ApproachType type;
		/** The response mode (yes, maybe, no). */
		public ResponseMode mode;
		/** The label to use. */
		public String label;
		/** The stance change. */
		public int change;
		/** The indicator that the alien won't talk to the player for some time. */
		public boolean notalk;
	}
	/** The alien's will contact with one of this entry. */
	public static class Call {
		/** The call type. */
		public CallType type;
		/** The label to use. */
		public String label;
		/** The list of approaches. */
		public final List<Approach> approaches = new ArrayList<Approach>();
	}
	/** The negotiation topics. */
	public final List<Negotiate> negotiations = new ArrayList<Negotiate>();
	/** The call topics. */
	public final List<Call> calls = new ArrayList<Call>();
	/** The label used for terminating the negotiation. */
	public String terminateLabel;
	/**
	 * Parse the diplomacy description XML.
	 * @param root the root node of the XML
	 * @param result the target output
	 * @return the map of player ID to diplomacy
	 */
	public static Map<String, Diplomacy> parse(XElement root, Map<String, Diplomacy> result) {
		
		for (XElement xplayer : root.childrenWithName("player")) {
			Diplomacy d = new Diplomacy();
			result.put(xplayer.get("id"), d);
			for (XElement xnegotiate : xplayer.childrenWithName("negotiate")) {
				Negotiate n = new Negotiate();
				d.negotiations.add(n);
				n.type = NegotiateType.valueOf(xnegotiate.get("type"));
				for (XElement xapproach : xnegotiate.childrenWithName("approach")) {
					Approach a = new Approach();
					n.approaches.add(a);
					a.label = xapproach.content;
					String at = xapproach.get("type", null);
					a.type = at != null ? ApproachType.valueOf(at) : null;
				}
				for (XElement xresponse : xnegotiate.childrenWithName("response")) {
					Response r = new Response();
					n.responses.add(r);
					String rt = xresponse.get("type", null);
					r.type = rt != null ? ApproachType.valueOf(rt) : null;
					r.label = xresponse.content;
					r.mode = ResponseMode.valueOf(xresponse.get("mode"));
					r.change = xresponse.getInt("change");
					r.notalk = "true".equals(xresponse.get("notalk", "false"));
				}
			}
			d.terminateLabel = xplayer.childValue("terminate");
			for (XElement xcall : xplayer.childrenWithName("call")) {
				Call c = new Call();
				c.type = CallType.valueOf(xcall.get("type"));
				d.calls.add(c);
				for (XElement xapproach : xcall.childrenWithName("approach")) {
					Approach a = new Approach();
					c.approaches.add(a);
					a.label = xapproach.content;
				}
				
			}
		}
		
		return result;
	}
}
