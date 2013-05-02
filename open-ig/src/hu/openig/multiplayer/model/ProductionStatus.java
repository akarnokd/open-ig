/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer.model;

import hu.openig.model.Production;
import hu.openig.net.MessageObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The production status.
 * @author akarnokd, 2013.05.01.
 */
public class ProductionStatus implements MessageObjectIO {
	/** Is the production paused? */
	public boolean paused;
	/**
	 * The list of production line statuses.
	 */
	public final List<Production> productions = new ArrayList<Production>();
	@Override
	public void fromMessage(MessageObject mo) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public MessageObject toMessage() {
		// TODO Auto-generated method stub
		return null;
	}
}
