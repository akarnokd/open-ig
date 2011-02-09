/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A simplified XML element model.
 * @author karnokd
 */
public class XElement implements Iterable<XElement> {
	/** The element name. */
	public final String name;
	/** The content of a simple node. */
	public String content;
	/** The parent element. */
	public XElement parent;
	/** The attribute map. */
	protected final Map<String, String> attributes = new HashMap<String, String>();
	/** The child elements. */
	protected final List<XElement> children = new ArrayList<XElement>();
	/**
	 * Constructor. Sets the name.
	 * @param name the element name
	 */
	public XElement(String name) {
		this.name = name;
	}
	/**
	 * Retrieve an attribute.
	 * @param attributeName the attribute name
	 * @return the attribute value or null if no such attribute
	 */
	public String get(String attributeName) {
		return attributes.get(attributeName);
	}
	@Override
	public Iterator<XElement> iterator() {
		return children.iterator();
	}
	/**
	 * Returns an iterator which enumerates all children with the given name.
	 * @param name the name of the children to select
	 * @return the iterator
	 */
	public Iterable<XElement> childrenWithName(String name) {
		List<XElement> result = new ArrayList<XElement>(children.size() + 1);
		for (XElement e : children) {
			if (e.name.equals(name)) {
				result.add(e);
			}
		}
		return result;
	}
	/**
	 * Returns the content of the first child which has the given name.
	 * @param name the child name
	 * @return the content or null if no such child
	 */
	public String childValue(String name) {
		for (XElement e : children) {
			if (e.name.equals(name)) {
				return e.content;
			}
		}
		return null;
	}
	/**
	 * Returns the first child element with the given name.
	 * @param name the child name
	 * @return the XElement or null if not present
	 */
	public XElement childElement(String name) {
		for (XElement e : children) {
			if (e.name.equals(name)) {
				return e;
			}
		}
		return null;
	}
	/**
	 * XML parzolása inputstream-ből és lightweight XElement formába.
	 * Nem zárja be az inputstreamet.
	 * @param in az InputStream objektum
	 * @return az XElement objektum
	 * @throws XMLStreamException kivétel esetén
	 */
	public static XElement parseXML(InputStream in) throws XMLStreamException {
		XMLInputFactory inf = XMLInputFactory.newInstance();
		XMLStreamReader ir = inf.createXMLStreamReader(in);
		return parseXML(ir);
	}
	/**
	 * A megadott XML Stream reader alapján az XElement fa felépítése.
	 * @param in az XMLStreamReader
	 * @return az XElement objektum
	 * @throws XMLStreamException ha probléma adódik
	 */
	private static XElement parseXML(XMLStreamReader in) throws XMLStreamException {
		XElement node = null;
		XElement root = null;
		int depth = 0;
		final StringBuilder emptyBuilder = new StringBuilder();
		StringBuilder b = null;
		Deque<StringBuilder> stack = new LinkedList<StringBuilder>();
		
		while (in.hasNext()) {
			int type = in.next();
			switch(type) {
			case XMLStreamConstants.START_ELEMENT:
				if (b != null) {
					// a megkezdett szöveg elmentése
					stack.push(b);
					b = null;
				} else {
					// nem volt text elem, így az üres elmentése
					stack.push(emptyBuilder);
				}
				XElement n = new XElement(in.getName().getLocalPart());
//				n.parent = node;
//				n.depth = depth++;
				int attCount = in.getAttributeCount();
				if (attCount > 0) {
					for (int i = 0; i < attCount; i++) {
						n.attributes.put(in.getAttributeLocalName(i), in.getAttributeValue(i));
					}
				}
				if (node != null) {
					node.children.add(n);
				}
				node = n;
				if (root == null) {
					root = n;
				}
				break;
			case XMLStreamConstants.CDATA:
			case XMLStreamConstants.CHARACTERS:
				if (node != null && !in.isWhiteSpace()) {
					/*
					if (node.value == null) {
						node.value = new StringBuilder();
					}
					node.value.append(ir.getText());
					*/
					if (b == null) {
						b = new StringBuilder();
					}
					b.append(in.getText());
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				// ha volt szöveg, akkor hozzárendeljük a csomópont értékéhez
				if (b != null) {
					node.content = b.toString();
				}
				if (node != null) {
					node = node.parent;
				}
				// kiszedjük a szülőjének builderjét
				b = stack.pop();
				// ha ez az üres, akkor a szülőnek (még) nem volt szöveges tartalma
				if (b == emptyBuilder) {
					b = null;
				}
				depth--;
				break;
			default:
				// ignore others.
			}
		}
		in.close();
		return root;
	}
	/**
	 * XML parzolása reader-ből és lightweight XElement formába.
	 * Nem zárja be az inputstreamet.
	 * @param in az InputStream objektum
	 * @return az XElement objektum
	 * @throws XMLStreamException kivétel esetén
	 */
	public static XElement parseXML(Reader in) throws XMLStreamException {
		XMLInputFactory inf = XMLInputFactory.newInstance();
		XMLStreamReader ir = inf.createXMLStreamReader(in);
		return parseXML(ir);
	}
	/**
	 * XML fájl parzolása fájlnév alapján.
	 * @param fileName fálnév
	 * @return az XElement objektum
	 * @throws IOException ha hiba történt
	 * @throws XMLStreamException ha hiba történt
	 */
	public static XElement parseXML(String fileName) throws IOException, XMLStreamException {
		InputStream in = new FileInputStream(fileName);
		try {
			return parseXML(in);
		} finally {
			in.close();
		}
	}
}
