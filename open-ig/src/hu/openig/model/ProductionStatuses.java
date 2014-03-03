/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.MessageArray;
import hu.openig.net.MessageObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The production status.
 * @author akarnokd, 2013.05.01.
 */
public class ProductionStatuses implements MessageObjectIO {
	/** Is the production paused? */
	public boolean paused;
	/**
	 * The list of production line statuses.
	 */
	public final List<ProductionStatus> productions = new ArrayList<>();
	@Override
	public void fromMessage(MessageObject mo) {
		paused = mo.getBoolean("paused");
		for (MessageObject o : mo.getArray("productions").objects()) {
			ProductionStatus ps = new ProductionStatus();
			ps.fromMessage(o);
			productions.add(ps);
		}
	}
	@Override
	public MessageObject toMessage() {
		MessageObject result = new MessageObject(objectName());
		result.set("paused", paused);
		MessageArray arr = new MessageArray(null);
		result.set("productions", arr);
		
		for (ProductionStatus ps : productions) {
			arr.add(ps.toMessage());
		}
		
		return result;
	}
	@Override
	public String objectName() {
		return "PRODUCTIONS";
	}
}
