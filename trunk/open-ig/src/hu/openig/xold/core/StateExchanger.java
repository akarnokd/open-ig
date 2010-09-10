/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.core;

/**
 * Interface for classes which support load() and save() commands for
 * state transfer into a different thread.
 * @author karnokd
 */
public interface StateExchanger {
	/** Store state values into the backing objects. Should be invoked in the event thread. */
	void save();
}
