/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
	/**
	 * Returns an iterator of the child elements.
	 * (Remark: I wish we had C# yield and their iterator semantics: MoveNext, Current)
	 * @param parent the parent node
	 * @return the list of elements
	 */
	public static Iterable<Element> children(final Element parent) {
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
								current = e;
								result = true;
								i++;
								break;
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
	/** 
	 * Returns the contents of the first occurrence of the specified child. 
	 * @param e the parent element
	 * @param name the child name
	 * @return the child textual value or empty string 
	 */
	public static String childValue(Element e, String name) {
		Iterator<Element> it = childrenWithName(e, name).iterator();
		if (it.hasNext()) {
			return it.next().getTextContent();
		}
		return "";
	}
	/** 
	 * Returns the first child element with the given name.
	 * @param e the parent element
	 * @param name child name
	 * @return the child element or null if not present 
	 */
	public static Element childElement(Element e, String name) {
		Iterator<Element> it = childrenWithName(e, name).iterator();
		if (it.hasNext()) {
			return it.next();
		}
		return null;
	}
	/** 
	 * Callback interface for processing a fully parsed XML DOM document.
	 * @param <T> the type of the parsing result 
	 */
	public static interface XmlProcessor<T> {
		/** 
		 * Process the given document object and return an object.
		 * @param doc the DOM document
		 * @return a parsed object 
		 */
		T process(Document doc);
	}
	/**
	 * Parse and process the given resource using the supplied callback.
	 * @param <T> the return type of the callback
	 * @param resource the resource string, not null
	 * @param processor the processor object, not null
	 * @return the processed object
	 */
	public static <T> T parseResource(String resource, XmlProcessor<T> processor) {
		T result = null;
		try {
			InputStream in = XML.class.getResourceAsStream(resource);
			if (in != null) {
				try {
					DocumentBuilderFactory db = DocumentBuilderFactory.newInstance();
					DocumentBuilder doc = db.newDocumentBuilder();
					result = processor.process(doc.parse(in));
				} finally {
					in.close();
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (SAXException ex) {
			ex.printStackTrace();
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		}
		return result;
	}
	/**
	 * Connverts all sensitive characters to its HTML entity equivalent.
	 * @param s the string to convert, can be null
	 * @return the converted string, or an empty string
	 */
	public static String toHTML(String s) {
		if (s != null) {
			StringBuilder b = new StringBuilder(s.length());
			for (int i = 0, count = s.length(); i < count; i++) {
				char c = s.charAt(i);
				switch (c) {
				case '<':
					b.append("&lt;");
					break;
				case '>':
					b.append("&gt;");
					break;
				case '\'':
					b.append("&#39;");
					break;
				case '"':
					b.append("&quot;");
					break;
				case '&':
					b.append("&amp;");
					break;
				default:
					b.append(c);
				}
			}
			return b.toString();
		}
		return "";
	}
	/**
	 * Open an XML file from disk.
	 * @param file the file to open
	 * @return the document element of the XML file
	 * @throws IOException on IO or parsing errors
	 */
	public static Element openXML(File file) throws IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			return doc.getDocumentElement();
		} catch (SAXException ex) {
			throw new IOException(ex);
		} catch (ParserConfigurationException ex) {
			throw new IOException(ex);
		}
	}
	/**
	 * Parse an XML from the supplied string.
	 * @param xml the XML as string
	 * @return the root element
	 * @throws IOException on parsing errors
	 */
	public static Element parse(String xml) throws IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(xml)));
			return doc.getDocumentElement();
		} catch (SAXException ex) {
			throw new IOException(ex);
		} catch (ParserConfigurationException ex) {
			throw new IOException(ex);
		}
	}
}
