/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import javax.swing.RowFilter;

/**
 * Base class for filtering generic table model elements with XElement type.
 * @author akarnokd, 2012.11.02.
 * @param <T> the element type
 */
public abstract class CEXRowFilter<T> extends
		RowFilter<GenericTableModel<T>, Integer> {
	@Override
	public final boolean include(
			javax.swing.RowFilter.Entry<? extends GenericTableModel<T>, ? extends Integer> entry) {
		GenericTableModel<T> model = entry.getModel();
		int index = entry.getIdentifier();
		Object[] dv = new Object[model.getColumnCount()];
		for (int i = 0; i < dv.length; i++) {
			dv[i] = model.getValueAt(index, i);
		}
		return include(model.get(index), dv, index);
	}
	/**
	 * Returns true if the given element at the given index should be included.
	 * @param item the item
	 * @param displayValues the values diplayed by the table
	 * @param index the index
	 * @return true if should be included
	 */
	public abstract boolean include(T item, Object[] displayValues, int index);
}
