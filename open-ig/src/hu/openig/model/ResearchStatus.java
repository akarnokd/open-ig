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
 * A concrete in-progress research status object.
 * @author akarnokd, 2013.05.05.
 */
public class ResearchStatus implements MessageObjectIO {
	/** The research should progress. */
	public ResearchState state;
	/** The thing to research. */
	public String type;
	/** The assigned money amount. */
	public int assignedMoney;
	/** The remaining money amount. */
	public int remainingMoney;
	@Override
	public void fromMessage(MessageObject mo) {
		state = mo.getEnum("state", ResearchState.values());
		type = mo.getString("type");
		assignedMoney = mo.getInt("assigned");
		remainingMoney = mo.getInt("remaining");
	}
	@Override
	public MessageObject toMessage() {
		return new MessageObject(objectName())
		.set("state", state)
		.set("type", type)
		.set("assigned", assignedMoney)
		.set("remaining", remainingMoney);
	}
	@Override
	public String objectName() {
		return "RESEARCH";
	}
}
