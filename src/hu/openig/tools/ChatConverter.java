/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.model.Chats.Node;
import hu.openig.utils.IOUtils;
import hu.openig.utils.U;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Take the original chat program and convert it to the chat.xml format.
 * @author akarnokd, 2012.06.02.
 */
public final class ChatConverter {
	/** Utility class. */
	private ChatConverter() {
		
	}
	/** A message. */
	public static class Message {
		/** Which party. */
		int party;
		/** The message text. */
		String text = "";
		@Override
		public String toString() {
			return party + ": " + text;
		}
	}
	/** A choice entry. */
	public static class Choice {
		/** The choice text. */
		String text;
		/** The choice procedure. */
		String proc;
		@Override
		public String toString() {
			return text + " -> " + proc;
		}
	}
	/** A procedure. */
	public static class Procedure {
		/** The procedure name. */
		String name;
		/** The procedure body text. */
		String body;
		/** List of message entries. */
		final List<Message> messages = new ArrayList<>();
		/** List of choice entries. */
		final List<Choice> choices = new ArrayList<>();
		/** Issue retreat? */
		boolean retreat;
		/** The next procedure. */
		String next;
		@Override
		public String toString() {
			if (!messages.isEmpty()) {
				return name + " M" + messages + " -> " + next;
			}
			return name + " C" + choices;
		}
	}
	/** The IG script. */
	public static class IGScript {
		/** The starting procedure. */
		String start;
		/** The map of procedures. */
		final Map<String, Procedure> procedures = new LinkedHashMap<>();
		@Override
		public String toString() {
			return "Start: " + start + "\r\n" + procedures.values();
		}
	}
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {

		Map<String, String> labels = new LinkedHashMap<>();

		for (int i = 1; i <= 8; i++) {
			IGScript scr = parseScript("c:/games/ighu/data/text/kor" + i + ".scr", "Cp850"); //hu: Cp850, de: Cp1250
	
			String chatPattern = "chat.blockade.outgoing.%d";
			String labelPattern = "chat.bo.%d.%s";
			
			
			List<Node> nodes = new ArrayList<>();
			
			for (Procedure proc : scr.procedures.values()) {
				if (!proc.messages.isEmpty()) {
					Message m0 = proc.messages.get(0);
	
					String n0n = proc.name + "-p";
					if (proc.name.equals(scr.start)) {
						n0n = "0";
					}
					
					String opt = null;
					
					for (Procedure p2 : scr.procedures.values()) {
						for (Choice c : p2.choices) {
							if (c.proc.equals(proc.name)) {
								opt = c.text;
								if (p2.name.equals(scr.start)) {
									n0n = "0";
								}
								if (m0.text.isEmpty()) {
									m0.text = c.text;
								}
							}
						}
					}
					Node n0 = new Node(n0n);
					n0.message = m0.text;
					n0.option = opt;
					
					n0.transitions.add(proc.name + "-e");
					
					nodes.add(n0);
					
					//**********************************************************
					
					Message m1 = proc.messages.get(1);
					Node n1 = new Node(proc.name + "-e");
					n1.retreat = proc.retreat;

					if (m1.text.isEmpty() && proc.next != null) {
						Procedure p2 = scr.procedures.get(proc.next);
						m1 = p2.messages.get(1);
						n1.retreat |= p2.retreat;
					}
					
					n1.enemy = true;
					n1.message = m1.text;
					
					if (proc.next != null) {
						Procedure p2 = scr.procedures.get(proc.next);
						for (Choice c : p2.choices) {
							n1.transitions.add(c.proc + "-p");
						}
					}
					
					nodes.add(n1);
				}
			}
			int idx = 0;
			Map<String, Integer> nodeMap = new HashMap<>();
			List<Node> nodes2 = U.newArrayList(nodes);
			nodes.clear();
			for (Node n : nodes2) {
				if (!n.message.isEmpty()) {
					nodeMap.put(n.id, idx);
					idx++;
					nodes.add(n);
				}
			}
			
			System.out.printf("<chat id='%s'>%n", String.format(chatPattern, i));
			
			for (Node n : nodes) {
				boolean found = false;
				outer:
				for (Node n2 : nodes) {
					for (String tr0 : n2.transitions) {
						if (tr0.equals(n.id)) {
							found = true;
							break outer;
						}
					}
				}
				if (!found && !n.id.equals("0")) {
					continue;
				}
				
				int nidx = nodeMap.get(n.id);
				
				System.out.printf("\t<node id='%s' owner='%s'", nidx, n.enemy ? "enemy" : "player");
				if (n.option != null) {
					
					String lbl = String.format(labelPattern, i, "n" + nidx + ".o");
					
					labels.put(lbl, n.option);
					
					System.out.printf(" option='%s'", lbl);
				}

				String lbl = String.format(labelPattern, i, "n" + nidx);
				labels.put(lbl, n.message);
				
				System.out.printf(" message='%s'", lbl);
				if (n.retreat) {
					System.out.printf(" action='retreat'");
				}
				if (n.transitions.isEmpty()) {
					System.out.printf("/> <!-- %s -->%n", n.option != null ? n.option : n.message);
				} else {
					System.out.printf("> <!-- %s -->%n", n.option != null ? n.option : n.message);
					for (String tr : n.transitions) {
						Integer trs = nodeMap.get(tr);
						if (trs == null) {
							System.err.printf("%s missing ", tr);
						}
						System.out.printf("\t\t<transition to='%s'/>%n", trs);
					}
					System.out.printf("\t</node>%n");
				}
			}
			System.out.printf("</chat>%n%n");
			
		}
		for (Map.Entry<String, String> lbls : labels.entrySet()) {
			System.out.printf("\t<entry key='%s'>%s</entry>%n", lbls.getKey(), lbls.getValue());
		}
		System.out.println();
	}
	/**
	 * Parse an IG script file.
	 * @param fileName the filename
	 * @param charset the charset
	 * @return the parsed script
	 * @throws IOException on error
	 */
	static IGScript parseScript(String fileName, String charset) throws IOException {
		IGScript result = new IGScript();
		String txt = new String(IOUtils.load(fileName), charset);
		int pi = 0;
		// parse procedures
		while (pi >= 0) {
			int pi2 = txt.indexOf("procedure ", pi);
			if (pi2 >= 0) {
				int pi3 = txt.indexOf('\r', pi2);
				Procedure proc = new Procedure();
				
				proc.name = txt.substring(pi2 + 10, pi3).trim();
				
				int pi2e = txt.indexOf("end\r", pi3);
				proc.body = txt.substring(pi3 + 1, pi2e).trim();
				
				result.procedures.put(proc.name, proc);

				pi = pi2e + 4;
			} else {
				break;
			}
		}
		// parse each procedures.
		for (Procedure proc : result.procedures.values()) {
			int choiceIdx = proc.body.indexOf("valaszt ");
			// manage choices
			if (choiceIdx >= 0) {
				int ce = proc.body.indexOf("\r", choiceIdx);
				String cn = proc.body.substring(choiceIdx + 8, ce).trim();
				int choiceCount = Integer.parseInt(cn);
				
				for (int i = 0; i < choiceCount; i++) {
					int ce2 = proc.body.indexOf("\r", ce + 1);
					if (ce2 < 0) {
						ce2 = proc.body.length();
					}
					
					String choiceEntryStr = proc.body.substring(ce + 1, ce2).trim();
					
					int sep = choiceEntryStr.lastIndexOf(' ');
					
					Choice c = new Choice();
					
					int qidx = choiceEntryStr.indexOf('"');
					c.text = noquot(choiceEntryStr.substring(qidx, sep));
					
					c.proc = choiceEntryStr.substring(sep + 1);
					
					proc.choices.add(c);
					
					ce = ce2 + 1;
				}
			} else {
				Message m0 = new Message();
				Message m1 = new Message();
				m1.party = 1;
				
				for (String s0 : proc.body.split("\r")) {
					s0 = s0.trim();
					
					if (s0.startsWith("message0")) {
						m0.text += " " + noquot(s0.substring(9));
					} else
					if (s0.startsWith("message1")) {
						m1.text += " " + noquot(s0.substring(9));
					} else 
					if (s0.startsWith("visszavonulas")) {
						proc.retreat = true;
					} else {
						if (result.procedures.containsKey(s0)) {
							proc.next = s0;
						}
					}
				}
				
				m0.text = m0.text.trim();
				m1.text = m1.text.trim();
				proc.messages.add(m0);
				proc.messages.add(m1);
			}
		}
		
		// find the entry point
		int pi0 = txt.indexOf("procedure ");
		
		for (String s0 : txt.substring(0, pi0).split("\r")) {
			int s0i = s0.indexOf(' ');
			if (s0i < 0) {
				s0i = s0.length();
			}
			
			String n0 = s0.substring(0, s0i).trim();
			if (result.procedures.containsKey(n0)) {
				result.start = n0;
			}
		}
		
		return result;
	}
	/**
	 * Removes the leading and trailing quotation marks.
	 * @param s the string
	 * @return the cleaned string
	 */
	static String noquot(String s) {
		if (s.startsWith("\"")) {
			s = s.substring(1);
		}
		if (s.endsWith("\"")) {
			s = s.substring(0, s.length() - 1);
		}
		return s.trim();
	}
}
