/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.core.Func1;
import hu.openig.screen.CommonResources;

import java.util.Arrays;
import java.util.List;

/**
 * List box spin box.
 * @author akarnokd, 2012.08.20.
 * @param <T> the element type
 */
public class ListSpinBox<T> extends SpinBox {
	/** The current selection index. */ 
	public int index;
	/** The list. */
	final List<T> list;
	/** The value function. */
	final Func1<T, String> valueFunc;
	/**
	 * Constructor. Initialize the fields.
	 * @param commons the common resources object
	 * @param list the backing list
	 * @param valueFunc the value function
	 */
	public ListSpinBox(CommonResources commons, List<T> list, Func1<T, String> valueFunc) {
		super(commons);
		this.list = list;
		this.valueFunc = valueFunc;
	}
	/**
	 * Constructor. Initialize the fields.
	 * @param commons the common resources object
	 * @param list the backing list
	 * @param valueFunc the value function
	 */
	@SafeVarargs
	public ListSpinBox(CommonResources commons, Func1<T, String> valueFunc, T... list) {
		super(commons);
		this.list = Arrays.asList(list);
		this.valueFunc = valueFunc;
	}
	@Override
	public void onNext(boolean shift) {
		int cnt = shift ? 10 : 1;
		index = Math.min(list.size() - 1, index + cnt);
		update();
	}
	@Override
	public void onPrev(boolean shift) {
		int cnt = shift ? 10 : 1;
		index = Math.max(0, index - cnt);
		update();
	}
	@Override
	public String onValue() {
		if (index <= 0) {
			return null;
		}
		return valueFunc.invoke(list.get(index));
	}
	@Override
	public void update() {
		spin.prev.enabled(enabled && index > 0);
		spin.next.enabled(enabled && index < list.size() - 1);
	}
	/**
	 * Replaces the list with a new lists and tries to keep the current selection.
	 * @param items the new list
	 */
	public void setList(List<T> items) {
		T selection = null;
		if (index >= 0) {
			selection = list.get(index);
		}
		list.clear();
		list.addAll(items);
		index = list.indexOf(selection);
	}
	/**
	 * Set the size to the maximum of the campaign name.
	 */
	public void setMaxSize() {
		int w = 0;
		for (T t : list) {
			w = Math.max(w, commons.text().getTextWidth(14, valueFunc.invoke(t)));
		}
		width = w + spin.prev.width + spin.next.width + 20;
		spin.width = width;
		height = spin.prev.height;
	}
	/** @return the selected index. */
	public T selected() {
		return list.get(index);
	}
	/**
	 * Sets the full width of the spin box.
	 * @param w the width in pixels
	 */
	public void width(int w) {
		width = w;
		spin.width = w;
	}
	/**
	 * @return the current item count.
	 */
	public int itemCount() {
		return list.size();
	}
}