/*
 * Copyright 2008-2014, David Karnok 
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
import java.util.Objects;

/**
 * The diplomatic relation options of a particular non-user player.
 * @author akarnokd, Apr 22, 2011
 */
public class Diplomacy {
	/** A negotiation entry for the player. */
	public static class Negotiate {
		/** The negotiation's type. */
		public final NegotiateType type;
		/** The available approaches. */
		public final List<Approach> approaches = new ArrayList<>();
		/** The available responses. */
		public final List<Response> responses = new ArrayList<>();
        /**
         * Constructor, initializes the negotiation type.
         * @param type the negotiation type
         */
        public Negotiate(NegotiateType type) {
            this.type = Objects.requireNonNull(type);
        }
		/**
		 * Find the approaches for the specified type.
		 * @param type the approach type
		 * @return the list of available approach definitions
		 */
		public List<Approach> approachFor(ApproachType type) {
			List<Approach> result = new ArrayList<>();
			for (Approach a : approaches) {
				if (a.type == type) {
					result.add(a);
				}
			}
			return result;
		}
		/**
		 * Find the available responses for the given approach type and mode.
		 * @param type the approach type
		 * @param mode the general response
		 * @return the list of concrete responses
		 */
		public List<Response> responseFor(ApproachType type, ResponseMode mode) {
			List<Response> result = new ArrayList<>();
			for (Response r : responses) {
				if (r.type == type && r.mode == mode) {
					result.add(r);
				}
			}
			return result;
		}
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
		/** The list of approaches. */
		public final List<Approach> approaches = new ArrayList<>();
	}
	/** The negotiation topics. */
	public final List<Negotiate> negotiations = new ArrayList<>();
	/** The call topics. */
	public final List<Call> calls = new ArrayList<>();
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
				Negotiate n = new Negotiate(NegotiateType.valueOf(xnegotiate.get("type")));
				d.negotiations.add(n);
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
					r.type = rt != null ? ApproachType.valueOf(rt) : ApproachType.NEUTRAL;
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
					String rt = xapproach.get("type", null);
					a.type = rt != null ? ApproachType.valueOf(rt) : ApproachType.NEUTRAL;
					a.label = xapproach.content;
				}
				
			}
		}
		
		return result;
	}
}
