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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to check whether all labels are present in both languages.
 * @author akarnokd, 2011.12.25.
 */
public final class LabelVerifier {
	/**
	 * Utility class.
	 */
	private LabelVerifier() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Main program.
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		XElement xhu = XElement.parseXML("data/hu/labels.xml");
		Set<String> langs = U.newSet("en", "fr", "ru", "de", "es");
		for (String l : langs) {
			XElement xen = XElement.parseXML("data/" + l + "/labels.xml");
			
			Map<String, String> labelsHu = new LinkedHashMap<>();
			Map<String, String> labelsEn = new LinkedHashMap<>();
			
			load(xhu, labelsHu);
			load(xen, labelsEn);
			
			System.out.printf("Missing from %s:%n---%n", l);
			for (String s : sort(labelsHu.keySet())) {
				if (!labelsEn.containsKey(s)) {
					System.out.printf("\t<entry key='%s'>%s</entry>%n", s, labelsHu.get(s));
				}
			}
			System.out.printf("Missing from hungarian:%n---%n");
			for (String s : sort(labelsEn.keySet())) {
				if (!labelsHu.containsKey(s)) {
					System.out.printf("\t<entry key='%s'>%s</entry>%n", s, labelsEn.get(s));
				}
			}
		}
	}
	/**
	 * Creates a copy of the collection and sorts it according to their natural order.
	 * @param <T> something self comparable
	 * @param src the source collection
	 * @return the sorted list
	 */
	static <T extends Comparable<? super T>> List<T> sort(Collection<T> src) {
		List<T> result = new ArrayList<>(src);
		Collections.sort(result);
		return result;
	}
	/**
	 * Load a the labels.
	 * @param src the source XElement
	 * @param out the output map
	 */
	static void load(XElement src, Map<String, String> out) {
		for (XElement e : src.childrenWithName("entry")) {
			out.put(e.get("key"), e.content);
		}
	}
}
