/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.net.MessageTokenizer.Token;
import hu.openig.net.MessageTokenizer.TokenType;
import hu.openig.utils.Exceptions;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A message object with named attributes.
 * @author akarnokd, 2013.04.21.
 */
public class MessageObject implements MessageSerializable {
	/** The name validator pattern. */
	public static final Pattern VALID_NAME = Pattern.compile("[\\p{L}_][\\p{L}_\\d]*", Pattern.CASE_INSENSITIVE);
	/** The object name. */
	public final String name;
	/** The attribute map. */
	protected final Map<String, Object> attributes = new LinkedHashMap<String, Object>();
	/**
	 * Constructs an empty message object.
	 * @param name the name
	 */
	public MessageObject(String name) {
		if (name != null && !verifyName(name)) {
			throw new IllegalArgumentException("Invalid name");
		}
		this.name = name;
	}
	/**
	 * Tests if the given attribute is present.
	 * @param name the name
	 * @return true if present
	 */
	public boolean has(String name) {
		return attributes.containsKey(name);
	}
	/**
	 * Retrieve a specific attribute object.
	 * @param name the name
	 * @return the object value
	 */
	public Object get(String name) {
		return attributes.get(name);
	}
	/**
	 * Set an attribute value.
	 * @param name the attribute name
	 * @param value the value
	 */
	public void set(String name, Object value) {
		if (!verifyName(name)) {
			throw new IllegalArgumentException("Invalid name");
		}
		attributes.put(name, value);
	}
	/**
	 * Returns a modifiable set of the attribute names.
	 * @return the set
	 */
	public Set<String> attributeNames() {
		return attributes.keySet();
	}
	/**
	 * Remove the specified attribute.
	 * @param name the attribte name
	 */
	public void remove(String name) {
		attributes.remove(name);
	}
	@Override
	public void save(Appendable out) throws IOException {
		if (name != null) {
			out.append(name);
		}
		out.append('{');
		int i = 0;
		for (Map.Entry<String, Object> e : attributes.entrySet()) {
			if (i > 0) {
				out.append(',');
			}
			out.append(e.getKey());
			out.append('=');
			
			Object v = e.getValue();
			
			appendTo(out, v);
			
			i++;
		}
		out.append('}');
	}
	/**
	 * Verify the given name as a valid identifier.
	 * @param s the string to test
	 * @return true if matches
	 */
	public static boolean verifyName(CharSequence s) {
		return VALID_NAME.matcher(s).matches();
	}
	/**
	 * Sanitize the given string.
	 * @param s the string to sanitize
	 * @return the sanitized version
	 */
	public static String sanitize(CharSequence s) {
		if (s == null) {
			return null;
		}
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '"' || c == '\\' || c == '\r' || c == '\n' || c == '\t') {
				out.append("\\");
			}
			out.append(c);
		}
		return out.toString();
	}
	/**
	 * Append the given value to the given output, performing any
	 * necessary sanitization and conversions.
	 * @param out the output
	 * @param v the value
	 * @throws IOException on error
	 */
	public static void appendTo(Appendable out, Object v) throws IOException {
		if (v instanceof CharSequence) {
			out.append('"');
			out.append(sanitize((CharSequence)v));
			out.append('"');
		} else
		if (v instanceof MessageSerializable) {
			((MessageSerializable)v).save(out);
		} else 
		if (v != null) {
			out.append(v.toString());
		} else {
			out.append("null");
		}
	}
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		try {
			save(b);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		return b.toString();
	}
	/**
	 * Parse the contents of the given reader.
	 * @param in the input
	 * @return the message object
	 * @throws IOException on error
	 */
	public static Object parse(Reader in) throws IOException {
		return parse(new MessageTokenizer(in).parse().iterator());
	}
	/**
	 * Parse a message object from the supplied tokenizer.
	 * @param tok the tokenizer
	 * @return the parsed object
	 * @throws IOException in case of I/O or syntax error
	 */
	public static Object parse(Iterator<Token> tok) throws IOException {
		Token t = tok.next();
		if (t.type == TokenType.IDENTIFIER) {
			String name = (String)t.value;
			t = tok.next();
			if (t.type == TokenType.SYMBOL && "{".equals(t.value)) {
				MessageObject mo = new MessageObject(name);
				parse(tok, mo);
				return mo;
			} else 
			if (t.type == TokenType.SYMBOL && "[".equals(t.value)) {
				MessageArray ma = new MessageArray(name);
				parse(tok, ma);
				return ma;
			} else {
				throw new MessageSyntaxError("{ or [ expected");
			}
		} else
		if (t.type == TokenType.SYMBOL) {
			if ("{".equals(t.value)) {
				MessageObject mo = new MessageObject(null);
				parse(tok, mo);
				return mo;
			} else
			if ("[".equals(t.value)) {
				MessageArray ma = new MessageArray(null);
				parse(tok, ma);
				return ma;
			}
		}
		throw new MessageSyntaxError("Unexpected token: " + t);
	}
	/**
	 * Parse the contents of the stream tokenizer into the given message object.
	 * @param tok the stream tokenizer
	 * @param mo the object to fill in
	 * @throws IOException on error
	 */
	public static void parse(Iterator<Token> tok, MessageObject mo) throws IOException {
		boolean valueFound = false;
		while (true) {
			Token t = tok.next();
			if (t.type == TokenType.SYMBOL && "}".equals(t.value)) {
				break;
			} else
			if (t.type == TokenType.IDENTIFIER) {
				String name = (String)t.value;
				t = tok.next();
				if (t.type == TokenType.SYMBOL && "=".equals(t.value)) {
					t = tok.next();
					if (t.type == TokenType.IDENTIFIER) {
						if ("null".equals(t.value)) {
							valueFound = true;
							mo.set(name, null);
						} else
						if ("false".equals(t.value)) {
							valueFound = true;
							mo.set(name, false);
						} else
						if ("true".equals(t.value)) {
							valueFound = true;
							mo.set(name, true);
						} else {
							String name2 = (String)t.value;
							t = tok.next();
							if (t.type == TokenType.SYMBOL && "{".equals(t.value)) {
								MessageObject mo2 = new MessageObject(name2);
								mo.set(name, mo2);
								parse(tok, mo2);
								valueFound = true;
							} else
							if (t.type == TokenType.SYMBOL && "[".equals(t.value)) {
								MessageArray ma2 = new MessageArray(name2);
								mo.set(name, ma2);
								parse(tok, ma2);
								valueFound = true;
							} else {
								throw new MessageSyntaxError("{ or [ expected");
							}
						}
					} else
					if (t.type == TokenType.SYMBOL) {
						if ("{".equals(t.value)) {
							valueFound = true;
							MessageObject mo2 = new MessageObject(null);
							mo.set(name, mo2);
							parse(tok, mo2);
						} else
						if ("[".equals(t.value)) {
							valueFound = true;
							MessageArray ma = new MessageArray(null);
							mo.set(name, ma);
							parse(tok, ma);
						}
					} else
					if (t.type == TokenType.INTEGER || t.type == TokenType.DOUBLE || t.type == TokenType.STRING) {
						valueFound = true;
						mo.set(name, t.value);
					} else {
						throw new MessageSyntaxError("Unexpected token: " + t);
					}
				}
			} else
			if (t.type == TokenType.SYMBOL && ",".equals(t.value)) {
				if (!valueFound) {
					throw new MessageSyntaxError("Invalid object content.");
				}
				valueFound = false;
			} else {
				throw new MessageSyntaxError("Unexpected token: " + t);
			}
		}
	}
	/**
	 * Parse the contents of the stream tokenizer into the given array object.
	 * @param tok the stream tokenizer
	 * @param ma the object to fill in
	 * @throws IOException on error
	 */
	public static void parse(Iterator<Token> tok, MessageArray ma) throws IOException {
		boolean valueFound = false;
		while (true) {
			Token t = tok.next();
			if (t.type == TokenType.SYMBOL && "]".equals(t.value)) {
				break;
			} else
			if (t.type == TokenType.DOUBLE || t.type == TokenType.INTEGER || t.type == TokenType.STRING) {
				ma.add(t.value);
				valueFound = true;
			} else
			if (t.type == TokenType.IDENTIFIER) {
				if ("null".equals(t.value)) {
					ma.add(null);
					valueFound = true;
				} else
				if ("false".equals(t.value)) {
					ma.add(false);
					valueFound = true;
				} else
				if ("true".equals(t.value)) {
					ma.add(true);
					valueFound = true;
				} else {
					String name = (String)t.value;
					t = tok.next();
					if (t.type == TokenType.SYMBOL && "{".equals(t.value)) {
						MessageObject mo = new MessageObject(name);
						ma.add(mo);
						parse(tok, mo);
						valueFound = true;
					} else 
					if (t.type == TokenType.SYMBOL && "[".equals(t.value)) {
						MessageArray ma2 = new MessageArray(name);
						ma.add(ma2);
						parse(tok, ma2);
						valueFound = true;
					} else {
						throw new MessageSyntaxError("{ or [ expected");
					}
				}
			} else
			if (t.type == TokenType.SYMBOL && "{".equals(t.value)) {
				MessageObject mo = new MessageObject(null);
				ma.add(mo);
				parse(tok, mo);
				valueFound = true;
			} else
			if (t.type == TokenType.SYMBOL && "[".equals(t.value)) {
				MessageArray ma2 = new MessageArray(null);
				ma.add(ma2);
				parse(tok, ma2);
				valueFound = true;
			} else
			if (t.type == TokenType.SYMBOL && ",".equals(t.value)) {
				if (!valueFound) {
					throw new MessageSyntaxError("Invalid object content.");
				}
				valueFound = false;
			} else {
				throw new MessageSyntaxError("Unexpected token: " + t);
			}
		}
	}
	/**
	 * Simple test program.
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		StringReader r = new StringReader(
				"OBJECT { value=1, i=true, a=-1, b=.1, c=-.1, d=-1.1, array=[ \"str\\\"\" ] }");
		
		System.out.println(MessageObject.parse(r));
		
		r = new StringReader(
				"Array [ 1, 2, 3 ]");

		System.out.println(MessageObject.parse(r));
		
		r = new StringReader(
				"Array [ OBJ { }, OBJ { attr = OBJ { } }, 3 ]");

		System.out.println(MessageObject.parse(r));

		r = new StringReader(
				"Array [ OBJ { }, OBJ { attr = { } }, 3 ]");

		System.out.println(MessageObject.parse(r));

	}
}
