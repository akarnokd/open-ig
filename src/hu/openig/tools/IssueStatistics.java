/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Download issue statistics.
 * @author akarnokd, 2012.06.10.
 */
public final class IssueStatistics {
	/** Utility class. */
	private IssueStatistics() { }
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		
//		download();
		
		XElement comments = XElement.parseXML("issues.xml");
		
		Set<String> users = U.newSet("akarn", "norbert", "Jozsef");

		int commentsTotal = 0;
		int commentsNonEmpty = 0;
		Map<String, Integer> counters = new HashMap<>();
		for (String s : users) {
			counters.put(s, 0);
		}
		
		for (XElement iss : comments.childrenWithName("issue")) {
			for (XElement e : iss.childrenWithName("entry")) {
				commentsTotal++;
				XElement t = e.childElement("title");
				XElement ct = e.childElement("content");
				if (ct.content == null) {
					continue;
				}
				commentsNonEmpty++;
				for (String u : users) {
					if (t.content != null && t.content.contains(u)) {
						Integer i = counters.get(u);
						counters.put(u, i + 1);
					}
				}
			}
		}
		for (Map.Entry<String, Integer> me : counters.entrySet()) {
			System.out.printf("%s: %d%n", me.getKey(), me.getValue());
		}
		System.out.printf("Total comments: %d, non-empty: %d%n", commentsTotal, commentsNonEmpty);
		
	}
	/**
	 * Download the issue comments.
	 * @throws Exception ignored
	 */
	static void download() throws Exception {
		XElement comments = new XElement("comments");
		for (int i = 1; i < 548; i++) {
			System.out.println("Issue #" + i);
			URL u = new URL("https://github.com/akarnokd/open-ig/issues/" + i + "/comments/full");

			try (InputStream in = u.openStream()) {
				XElement ce = XElement.parseXML(in);
				XElement issue = comments.add("issue");
				issue.set("id", i);
				issue.add(ce.childrenWithName("entry"));
			}
		}
		comments.save("issues.xml");
		System.out.println("Done");
	}

}
