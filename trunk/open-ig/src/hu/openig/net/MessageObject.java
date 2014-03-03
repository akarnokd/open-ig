/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.net.MessageTokenizer.Token;
import hu.openig.net.MessageTokenizer.TokenType;
import hu.openig.utils.Exceptions;

import java.io.EOFException;
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
	protected final Map<String, Object> attributes = new LinkedHashMap<>();
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
//	/**
//	 * Constructs an object with the given key-value pairs, which
//	 * will be turned into message objects and message arrays as
//	 * it goes.
//	 * @param name the name
//	 * @param nameValues the name-value pairs
//	 */
//	public MessageObject(String name, Object... nameValues) {
//		this(name);
//		setMany(nameValues);
//	}
//	/**
//	 * Sets several attribues from an array of name-value pairs.
//	 * @param nameValues the name-value pairs
//	 */
//	public void setMany(Object... nameValues) {
//		if (nameValues.length % 2 != 0) {
//			throw new IllegalArgumentException("nameValues needs to be even length array");
//		}
//		for (int i = 0; i < nameValues.length; i++) {
//			if (nameValues[i] instanceof CharSequence) {
//				String pn = nameValues[i].toString();
//				set(pn, MessageArray.wrap(nameValues[i + 1]));
//			} else {
//				throw new IllegalArgumentException("Entry " + i + " is not string-like");
//			}
//		}
//	}
	/**
	 * Tests if the given attribute is present.
	 * @param name the name
	 * @return true if present
	 */
	public boolean has(String name) {
		return attributes.containsKey(name);
	}
	/**
	 * Tests if the given attribute is present and is the instance of the requested
	 * type.
	 * @param name the name
	 * @param clazz the class
	 * @return true if present
	 */
	public boolean has(String name, Class<?> clazz) {
		if (attributes.containsKey(name)) {
			Object value = attributes.get(name);
			return clazz.isInstance(value);
		}
		return false;
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
	private void setRaw(String name, Object value) {
		if (!verifyName(name)) {
			throw new IllegalArgumentException("Invalid name");
		}
		attributes.put(name, value);
	}
	/**
	 * Sets an arbitrary object attribute.
	 * @param name the attribute name
	 * @param value the value
	 * @return this
	 */
	public MessageObject set(String name, Object value) {
		setRaw(name, MessageArray.wrap(value));
		return this;
	}
	/**
	 * Sets a nullable basic type value attribute.
	 * @param name the attribute name
	 * @param value the value
	 * @return this
	 */
	public MessageObject set(String name, Boolean value) {
		setRaw(name, value);
		return this;
	}
	/**
	 * Sets a nullable basic type value attribute.
	 * @param name the attribute name
	 * @param value the value
	 * @return this
	 */
	public MessageObject set(String name, Number value) {
		setRaw(name, value);
		return this;
	}
	/**
	 * Sets a nullable basic type value attribute.
	 * @param name the attribute name
	 * @param value the value
	 * @return this
	 */
	public MessageObject set(String name, Character value) {
		setRaw(name, value.toString());
		return this;
	}
	/**
	 * Sets a nullable basic type value attribute.
	 * @param name the attribute name
	 * @param value the value
	 * @return this
	 */
	public MessageObject set(String name, CharSequence value) {
		setRaw(name, value);
		return this;
	}
	/**
	 * Sets a message object attribute.
	 * @param name the attribute name
	 * @param value the value
	 * @return this
	 */
	public MessageObject set(String name, MessageObject value) {
		setRaw(name, value);
		return this;
	}
	/**
	 * Sets a message object attribute.
	 * @param name the attribute name
	 * @param value the value
	 * @return this
	 */
	public MessageObject set(String name, MessageArray value) {
		setRaw(name, value);
		return this;
	}
	/**
	 * Sets an enum attribute.
	 * @param name the attribute name
	 * @param value the value
	 * @return this
	 */
	public MessageObject set(String name, Enum<?> value) {
		if (value != null) {
			setRaw(name, value.name());
		} else {
			setRaw(name, null);
		}
		return this;
	}
	/**
	 * Sets an untyped null attribute.
	 * @param name the attribute name
	 * @return this
	 */
	public MessageObject setNull(String name) {
		setRaw(name, null);
		return this;
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
		if (v instanceof Character) {
			out.append('"');
			out.append(sanitize(v.toString()));
			out.append('"');
		} else
		if (v instanceof Enum<?>) {
			out.append('"');
			out.append(sanitize(v.toString()));
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
		return parse(new MessageTokenizer(in).iterator());
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
		} else
		if (t.type == TokenType.EOF) {
			throw new EOFException();
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
							mo.setNull(name);
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
						mo.setRaw(name, t.value);
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
					ma.addNull();
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
		
		System.out.println(MessageObject.parse(new MessageTokenizer(r).iterator()));
		
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
	/**
	 * Returns an integer value with the given attribute name.
	 * @param name the attribute name
	 * @return the integer value
	 */
	public int getInt(String name) {
		if (attributes.containsKey(name)) {
			Object v = attributes.get(name);
			if (v instanceof Number) {
				return ((Number)v).intValue();
			}
		}
		throw new MissingAttributeException(name + " missing or invalid type");
	}
	/**
	 * Returns a nullable integer value with the given attribute name.
	 * @param name the attribute name
	 * @return the integer value
	 */
	public Integer getIntObject(String name) {
		if (attributes.containsKey(name)) {
			Object v = attributes.get(name);
			if (v instanceof Number) {
				return ((Number)v).intValue();
			} else
			if (v == null) {
				return null;
			}
		}
		throw new MissingAttributeException(name + " missing or invalid type");
	}
	/**
	 * Returns a long value with the given attribute name.
	 * @param name the attribute name
	 * @return the long value
	 */
	public long getLong(String name) {
		if (attributes.containsKey(name)) {
			Object v = attributes.get(name);
			if (v instanceof Number) {
				return ((Number)v).longValue();
			}
		}
		throw new MissingAttributeException(name + " missing or invalid type");
	}
	/**
	 * Returns a nullable long value with the given attribute name.
	 * @param name the attribute name
	 * @return the long value
	 */
	public Long getLongObject(String name) {
		if (attributes.containsKey(name)) {
			Object v = attributes.get(name);
			if (v instanceof Number) {
				return ((Number)v).longValue();
			} else
			if (v == null) {
				return null;
			}
		}
		throw new MissingAttributeException(name + " missing or invalid type");
	}
	/**
	 * Returns a double value with the given attribute name.
	 * @param name the attribute name
	 * @return the double value
	 */
	public double getDouble(String name) {
		if (attributes.containsKey(name)) {
			Object v = attributes.get(name);
			if (v instanceof Number) {
				return ((Number)v).doubleValue();
			}
		}
		throw new MissingAttributeException(name + " missing or invalid type");
	}
	/**
	 * Returns a double value with the given attribute name.
	 * @param name the attribute name
	 * @return the double value
	 */
	public Double getDoubleObject(String name) {
		if (attributes.containsKey(name)) {
			Object v = attributes.get(name);
			if (v instanceof Number) {
				return ((Number)v).doubleValue();
			} else
			if (v == null) {
				return null;
			}
		}
		throw new MissingAttributeException(name + " missing or invalid type");
	}
	/**
	 * Returns a boolean value with the given attribute name.
	 * @param name the attribute name
	 * @return the boolean value
	 */
	public boolean getBoolean(String name) {
		if (attributes.containsKey(name)) {
			Object v = attributes.get(name);
			if (v instanceof Boolean) {
				return (Boolean)v;
			}
		}
		throw new MissingAttributeException(name + " missing or invalid type");
	}
	/**
	 * Returns a boolean value with the given attribute name.
	 * @param name the attribute name
	 * @return the boolean value, which might be null
	 */
	public Boolean getBooleanObject(String name) {
		if (attributes.containsKey(name)) {
			Object v = attributes.get(name);
			if (v instanceof Boolean) {
				return (Boolean)v;
			} else
			if (v == null) {
				return null;
			}
		}
		throw new MissingAttributeException(name + " missing or invalid type");
	}
	/**
	 * Returns a string attribute that is not null.
	 * @param name the attribute name
	 * @return the value
	 */
	public String getString(String name) {
		if (attributes.containsKey(name)) {
			Object v = attributes.get(name);
			if (v instanceof String) {
				return (String)v;
			}
		}
		throw new MissingAttributeException(name + " missing or invalid type");
	}
	/**
	 * Returns a string attribute that can be null.
	 * @param name the attribute name
	 * @return the value
	 */
	public String getStringObject(String name) {
		if (attributes.containsKey(name)) {
			Object v = attributes.get(name);
			if (v instanceof String) {
				return (String)v;
			} else
			if (v == null) {
				return null;
			}
		}
		throw new MissingAttributeException(name + " missing or invalid type");
	}
	/**
	 * Returns a message object or throws a MissingAttributeException.
	 * @param name the attribute name
	 * @return the message object
	 */
	public MessageObject getObject(String name) {
		if (attributes.containsKey(name)) {
			Object v = attributes.get(name);
			if (v instanceof MessageObject) {
				return (MessageObject)v;
			}
		}
		throw new MissingAttributeException(name + " missing or invalid type");
	}
	/**
	 * Returns an enum attribute, which is not null.
	 * @param <E> the enum type
	 * @param name the attribute name
	 * @param values the available enumeration values
	 * @return the enum
	 */
	public <E extends Enum<E>> E getEnum(String name, E[] values) {
		String s = getString(name);
		for (E e : values) {
			if (e.name().equals(s)) {
				return e;
			}
		}
		throw new MissingAttributeException(name + " missing or not enum");
	}
	/**
	 * Returns a nullable enum value.
	 * @param <E> the enum type
	 * @param name the attribute name
	 * @param values the available enumeration values
	 * @return the enum or null if the attribute was null
	 */
	public <E extends Enum<E>> E getEnumObject(String name, E[] values) {
		String s = getStringObject(name);
		if (s == null) {
			return null;
		}
		for (E e : values) {
			if (e.name().equals(s)) {
				return e;
			}
		}
		throw new MissingAttributeException(name + " missing or not enum");
	}
	/**
	 * Returns a message array or throws a MissingAttributeException.
	 * @param name the attribute name
	 * @return the message object
	 */
	public MessageArray getArray(String name) {
		if (attributes.containsKey(name)) {
			Object v = attributes.get(name);
			if (v instanceof MessageArray) {
				return (MessageArray)v;
			}
		}
		throw new MissingAttributeException(name + " missing or invalid type");
	}
	/**
	 * Returns the specified attribute value or the
	 * default value.
	 * @param name the attribute name
	 * @param defaultValue the default value
	 * @return the object
	 */
	public Object get(String name, Object defaultValue) {
		if (attributes.containsKey(name)) {
			return attributes.get(name);
		}
		return defaultValue;
	}
	/**
	 * Returns the specified attribute value or the
	 * default value if the attribute is missing or set to null.
	 * @param name the attribute name
	 * @param defaultValue the default value
	 * @return the object
	 */
	public Object getValue(String name, Object defaultValue) {
		if (attributes.containsKey(name)) {
			Object o = attributes.get(name);
			return o != null ? o : defaultValue;
		}
		return defaultValue;
	}
	/**
	 * Returns a string attribute value or the default value if the
	 * attribute is missing or null.
	 * @param name the attribute name
	 * @param defaultValue the default value
	 * @return the value
	 */
	public String getString(String name, String defaultValue) {
		if (attributes.containsKey(name)) {
			Object o = attributes.get(name);
			if (o instanceof String) {
				return (String)o;
			}
		}
		return defaultValue;
	}
	/**
	 * Returns an integer value with the given attribute name.
	 * @param name the attribute name
	 * @param defaultValue the default value
	 * @return the integer value
	 */
	public int getInt(String name, int defaultValue) {
		if (attributes.containsKey(name)) {
			Object o = attributes.get(name);
			if (o instanceof Number) {
				return ((Number)o).intValue();
			}
		}
		return defaultValue;
	}
	/**
	 * Returns a long value with the given attribute name.
	 * @param name the attribute name
	 * @param defaultValue the default value
	 * @return the long value
	 */
	public long getLong(String name, long defaultValue) {
		if (attributes.containsKey(name)) {
			Object o = attributes.get(name);
			if (o instanceof Number) {
				return ((Number)o).longValue();
			}
		}
		return defaultValue;
	}
	/**
	 * Returns a double value with the given attribute name.
	 * @param name the attribute name
	 * @param defaultValue the default value
	 * @return the double value
	 */
	public double getDouble(String name, double defaultValue) {
		if (attributes.containsKey(name)) {
			Object o = attributes.get(name);
			if (o instanceof Number) {
				return ((Number)o).doubleValue();
			}
		}
		return defaultValue;
	}
	/**
	 * Returns a boolean value with the given attribute name.
	 * @param name the attribute name
	 * @param defaultValue the default value
	 * @return the boolean value
	 */
	public boolean getBoolean(String name, boolean defaultValue) {
		if (attributes.containsKey(name)) {
			Object o = attributes.get(name);
			if (o instanceof Boolean) {
				return (Boolean)o;
			}
		}
		return defaultValue;
	}
}
