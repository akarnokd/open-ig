/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Container for all chat options.
 * @author akarnokd, 2012.04.08.
 */
public class Chats {
	/** The chat options. */
	protected final Map<String, Chat> chats = U.newLinkedHashMap();
	/**
	 * @return The available chat option identifiers.
	 */
	public Set<String> keys() {
		return Collections.unmodifiableSet(chats.keySet());
	}
	/**
	 * Get a chat definition for a given id.
	 * @param id the identifier
	 * @return the chat
	 */
	public Chat get(String id) {
		Chat c = chats.get(id);
		if (c == null) {
			new AssertionError("Missing chat settings: " + id).printStackTrace();
		}
		return c;
	}
	/**
	 * Load the chat.xml file.
	 * @param xml the XML.
	 */
	public void load(XElement xml) {
		for (XElement xchat : xml.childrenWithName("chat")) {
			Chat c = new Chat(xchat.get("id"));
			
			for (XElement xnode : xchat.childrenWithName("node")) {
				Node n = new Node();
				
				n.enemy = "enemy".equals(xnode.get("owner"));
				n.option = xnode.get("option", null);
				n.message = xnode.get("message");
				n.retreat = "retreat".equals(xnode.get("action", null));
				
				c.nodes.put(xnode.get("id"), n);
			}
			
			
			chats.put(c.id, c);
		}
	}
	/**
	 * An individual chat element.
	 * @author akarnokd, 2012.04.08.
	 */
	public static class Chat {
		/** The identifier. */
		protected final String id;
		/** The node map. */
		protected Map<String, Node> nodes = U.newLinkedHashMap();
		/**
		 * Creates the chat entry.
		 * @param id the identifier
		 */
		public Chat(String id) {
			this.id = id;
		}
		/**
		 * @return the start node of the chat
		 */
		public Node getStart() {
			return nodes.get("0");
		}
		/**
		 * Returns a node with the given ID.
		 * @param id the identifier
		 * @return the node or null if not present
		 */
		public Node get(String id) {
			Node n = nodes.get(id);
			if (n == null) {
				new AssertionError("Missing node " + id + " for chat entry " + this.id).printStackTrace();
			}
			return n;
		}
		/**
		 * @return returns the flee node or null if no such node is available.
		 */
		public Node getFlee() {
			return nodes.get("-1");
		}
		@Override
		public String toString() {
			return id + " " + nodes;
		}
	}
	/**
	 * A chat node.
	 * @author akarnokd, 2012.04.08.
	 */
	public static class Node {
		/** Is this a line of the enemy? */
		public boolean enemy;
		/** The choice to present. */
		public String option;
		/** The messag et o print. */
		public String message;
		/** Should the enemy retreat? */
		public boolean retreat;
		/** The available transitions. */
		public final List<String> transitions = U.newArrayList();
		@Override
		public String toString() {
			return "enemy = " + enemy + ", option = " + option + ", message = " + message + ", retreat = " + retreat;
		}
	}
	
}
