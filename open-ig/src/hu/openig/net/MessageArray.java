/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.utils.Exceptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Object containing other message objects.
 * @author akarnokd, 2013.04.21.
 */
public class MessageArray implements Iterable<Object>, MessageSerializable {
	/** The array's name. */
	public final String name;
	/** The list of message objects. */
	protected final List<Object> items = new ArrayList<Object>();
	/**
	 * Constructor, sets the optional object name.
	 * @param name the object name
	 */
	public MessageArray(String name) {
		if (name != null && !MessageObject.verifyName(name)) {
			throw new IllegalArgumentException("name");
		}
		this.name = name;
	}
	@Override
	public void save(Appendable out) throws IOException {
		if (name != null) {
			out.append(name);
		}
		out.append('[');
		int i = 0;
		for (Object o : items) {
			if (i > 0) {
				out.append(',');
			}
			MessageObject.appendTo(out, o);
			i++;
		}
		out.append(']');
	}
	/**
	 * Returns the count of elements in the array.
	 * @return the count
	 */
	public int size() {
		return items.size();
	}
	/**
	 * Get an object at a specified index.
	 * @param index the index
	 * @return the object
	 */
	public Object get(int index) {
		return items.get(index);
	}
	/**
	 * Add a new object to the list.
	 * @param value the value to add
	 */
	public void add(Object value) {
		items.add(value);
	}
	/**
	 * Adds a value at the specified index.
	 * @param index the index
	 * @param value the value
	 */
	public void add(int index, Object value) {
		items.add(index, value);
	}
	/**
	 * Remove a value at the specified index.
	 * @param index the index
	 */
	public void remove(int index) {
		items.remove(index);
	}
	/**
	 * Clear the array.
	 */
	public void clear() {
		items.clear();
	}
	/**
	 * Set a value at the specified index.
	 * @param index the index value
	 * @param value the value
	 */
	public void set(int index, Object value) {
		items.set(index, value);
	}
	@Override
	public Iterator<Object> iterator() {
		return items.iterator();
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
}
