/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML processing related helper functions.
 * @author karnokd
 */
public final class XML {
	/**
	 * Utility class constructor.
	 */
	private XML() {
		// utility class
	}
	/**
	 * Returns an iterator of the child elements with the given name.
	 * (Remark: I wish we had C# yield and their iterator semantics: MoveNext, Current)
	 * @param parent the parent node
	 * @param name the name
	 * @return the list of elements with the given name
	 */
	public static Iterable<Element> childrenWithName(final Element parent, final String name) {
		return new Iterable<Element>() {
			@Override
			public Iterator<Element> iterator() {
				return new Iterator<Element>() {
					/** The loop index. */
					private int i = 0;
					/** The original node list to iterate over. */
					private NodeList nl = parent.getChildNodes();
					/** The number of elements. */
					private int count = nl.getLength();
					/** The current element. */
					private Element current;
					@Override
					public boolean hasNext() {
						boolean result = false;
						for (; i < count; i++) {
							Node n = nl.item(i);
							if (n instanceof Element) {
								Element e = (Element)n;
								if (name.equals(e.getNodeName())) {
									current = e;
									result = true;
									i++;
									break;
								}
							}
						}
						return result;
					};
					@Override
					public Element next() {
						if (current != null) {
							return current;
						}
						throw new NoSuchElementException();
					};
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					};
				};
			}
		};
	}
	/** Returns the contents of the first occurrence of the specified child. */
	public static String childValue(Element e, String name) {
		Iterator<Element> it = childrenWithName(e, name).iterator();
		if (it.hasNext()) {
			return it.next().getTextContent();
		}
		return "";
	}
}
