/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.MessageObject;

/**
 * A concrete in-progress production.
 * @author akarnokd, 2013.05.05.
 */
public class ProductionStatus implements MessageObjectIO {
	/** The research type. */
	public String type;
	/** The number of items to produce. */
	public int count;
	/** The progress into the current item. */
	public int progress;
	/** The priority value. */
	public int priority;
	@Override
	public MessageObject toMessage() {
		return new MessageObject(objectName())
		.set("type", type)
		.set("count", count)
		.set("progress", progress)
		.set("priority", priority);
	}
	@Override
	public void fromMessage(MessageObject mo) {
		type = mo.getString("type");
		count = mo.getInt("count");
		progress = mo.getInt("progress");
		priority = mo.getInt("priority");
	}
	@Override
	public String objectName() {
		return "PRODUCTION";
	}
	
}
