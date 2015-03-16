/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * A generic table model backed by a list of type T elements.
 * @author akarnokd, 2012.11.01.
 * @param <T> the element type.
 */
public abstract class GenericTableModel<T> extends AbstractTableModel implements Iterable<T> {
	/**	 */
	private static final long serialVersionUID = 9135016542736539537L;
	/** The items. */
	protected final List<T> items;
	/** The column names. */
	protected String[] columnNames;
	/** The column types. */
	protected Class<?>[] columnTypes;
	/**
	 * Initializes the table with an empty list.
	 */
	public GenericTableModel() {
		this(new ArrayList<T>());
	}
	/**
	 * Initializes the table by taking over the supplied item list.
	 * @param items the item list
	 */
	public GenericTableModel(List<T> items) {
		this.items = items;
	}
	@Override
	public int getRowCount() {
		return items.size();
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnTypes[columnIndex];
	}
	/**
	 * Set the column names.
	 * @param names the names
	 */
	public void setColumnNames(String... names) {
		columnNames = names.clone();
	}
	/**
	 * Set the column classes.
	 * @param classes the classes
	 */
	public void setColumnTypes(Class<?>... classes) {
		columnTypes = classes.clone();
	}
	@Override
	public int getColumnCount() {
		return columnNames != null ? columnNames.length : 0;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return getValueFor(items.get(rowIndex), rowIndex, columnIndex);
	}
	/**
	 * Returns the element in the specified row.
	 * @param rowIndex the row index
	 * @return the element
	 */
	public T get(int rowIndex) {
		return items.get(rowIndex);
	}
	/**
	 * Returns a value for the specified row, column and object at that row.
	 * @param item the item in that row
	 * @param rowIndex the row index
	 * @param columnIndex the column index
	 * @return the value
	 */
	public abstract Object getValueFor(T item, int rowIndex, int columnIndex);
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		setValueFor(items.get(rowIndex), rowIndex, columnIndex, aValue);
	}
	/**
	 * Set a specific field value.
	 * @param item the object at the row
	 * @param rowIndex the row index
	 * @param columnIndex the column index
	 * @param value the value
	 */
	public void setValueFor(T item, int rowIndex, int columnIndex, Object value) {
		
	}
	/**
	 * Clear the contents of the model.
	 */
	public void clear() {
		int size = items.size();
		if (size > 0) {
			items.clear();
			fireTableRowsDeleted(0, size - 1);
		}
	}
	/**
	 * Add new items to the table.
	 * @param newItems the new items to add
	 */
	@SafeVarargs
	public final void add(T... newItems) {
		add(Arrays.asList(newItems));
	}
	/**
	 * Add items to the table.
	 * @param newItems the new items
	 */
	public void add(Iterable<? extends T> newItems) {
		int size0 = items.size();
		for (T t : newItems) {
			items.add(t);
		}
		fireTableRowsInserted(size0, items.size() - 1);
	}
	/**
	 * Insert new records at a specified index.
	 * @param rowIndex the row index
	 * @param newItems the items
	 */
	@SafeVarargs
	public final void insert(int rowIndex, T... newItems) {
		insert(rowIndex, Arrays.asList(newItems));
	}
	/**
	 * Insert new records at a specified index.
	 * @param rowIndex the row index
	 * @param newItems the items
	 */
	public void insert(int rowIndex, Iterable<? extends T> newItems) {
		int ri = rowIndex;
		int ri2 = rowIndex - 1;
		for (T t : newItems) {
			ri2++;
			items.add(ri2, t);
		}
		fireTableRowsInserted(ri, ri2);
	}
	/**
	 * Delete the given given entries.
	 * @param indices the indices.
	 */
	public void delete(int... indices) {
		int[] idx2 = indices.clone();
		if (idx2.length > 0) {
			Arrays.sort(idx2);
			for (int i = idx2.length - 1; i >= 0; i--) {
				items.remove(idx2[i]);
			}
			fireTableRowsDeleted(idx2[0], idx2[idx2.length - 1]);
		}
	}
	/** 
	 * Remove a specific item.
	 * @param item the item to remove
	 */
	public void delete(T item) {
		int idx = items.indexOf(item);
		if (idx >= 0) {
			items.remove(idx);
			fireTableRowsDeleted(idx, idx);
		}
	}
	/**
	 * Notify a change in the given row.
	 * @param index the row index
	 */
	public void update(int index) {
		if (index >= 0 && index < getRowCount()) {
			fireTableRowsUpdated(index, index);
		}
	}
	/**
	 * Notify a change in the given item.
	 * @param item the item
	 */
	public void update(T item) {
		update(items.indexOf(item));
	}
	/**
	 * Replace an old item with a new item.
	 * @param oldItem the old item
	 * @param newItem the new item
	 */
	public void replace(T oldItem, T newItem) {
		int idx = items.indexOf(oldItem);
		if (idx >= 0) {
			items.set(idx, newItem);
			update(idx);
		}
	}
	@Override
	public Iterator<T> iterator() {
		return items.iterator();
	}
}
