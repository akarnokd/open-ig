/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.LongField;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Response with the current own and known
 * empires, including diplomacy info.
 * @author akarnokd, 2013.04.27.
 */
public class EmpireStatuses implements MessageObjectIO {
	/** The current time in UTC milliseconds. */
	public long currentTime;
	/** The list of empires. */
	public final Map<String, EmpireStatus> empires = new LinkedHashMap<String, EmpireStatus>();
	/** The global statistics. */
	public final WorldStatistics statistics = new WorldStatistics();
	@Override
	public void fromMessage(MessageObject mo) {
		currentTime = mo.getLong("date");
		for (MessageObject moi : mo.getArray("empires").objects()) {
			EmpireStatus es = new EmpireStatus();
			es.fromMessage(moi);
			empires.put(es.id, es);
		}
		MessageObject ps = mo.getObject("statistics");
		for (String sn : ps.attributeNames()) {
			LongField f = statistics.fields.get(sn);
			if (f != null) {
				f.value = ps.getLong(sn);
			}
		}
	}
	@Override
	public MessageObject toMessage() {
		MessageObject result = new MessageObject(name());
		
		result.set("date", currentTime);
		
		MessageArray ma = new MessageArray(null);
		result.set("empires", ma);
		
		for (EmpireStatus es : empires.values()) {
			ma.add(es.toMessage());
		}
		
		MessageObject ps = new MessageObject(null);
		result.set("statistics", ps);
		for (Map.Entry<String, LongField> e : statistics.fields.entrySet()) {
			ps.set(e.getKey(), e.getValue().value);
		}
		
		return result;
	}
	@Override
	public String name() {
		return "EMPIRE_STATUSES";
	}
}
