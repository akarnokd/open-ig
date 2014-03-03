/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.LongField;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageObject;
import hu.openig.net.MissingAttributeException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Response with the current own and known
 * empires, including diplomacy info.
 * @author akarnokd, 2013.04.27.
 */
public class EmpireStatuses implements MessageObjectIO {
	/** The current time in UTC milliseconds. */
	public Date currentTime;
	/** The list of empires. */
	public final Map<String, EmpireStatus> empires = new LinkedHashMap<>();
	/** The global statistics. */
	public final WorldStatistics statistics = new WorldStatistics();
	/** The map of known other players and the diplomatic relations. */
	public final List<DiplomaticRelation> relations = new ArrayList<>();
	@Override
	public void fromMessage(MessageObject mo) {
		try {
			currentTime = ModelUtils.parse(mo.getString("date"));
		} catch (ParseException ex) {
			throw new MissingAttributeException(ex.toString());
		}
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
		for (MessageObject dro : mo.getArray("relations").objects()) {
			DiplomaticRelation dr = new DiplomaticRelation();
			
			dr.first = dro.getString("first");
			dr.second = dro.getString("second");
			dr.full = dro.getBoolean("full");
			dr.value = dro.getDouble("value");
			dr.lastContact = new Date(dro.getLong("lastContact"));
			dr.wontTalk(dro.getBoolean("wontTalk"));
			dr.tradeAgreement = dro.getBoolean("tradeAgreement");
			dr.strongAlliance = dro.getBoolean("strongAlliance");
			
			for (Object o : dro.getArray("allianceAgainst")) {
				if (o instanceof String) {
					dr.alliancesAgainst.add((String)o);
				}
			}
			
			relations.add(dr);
		}
	}
	@Override
	public MessageObject toMessage() {
		MessageObject result = new MessageObject(objectName());
		
		result.set("date", ModelUtils.format(currentTime));
		
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
		MessageArray dra = new MessageArray(null);
		result.set("relations", dra);
		for (DiplomaticRelation dr : relations) {
			MessageObject dro = new MessageObject(null);
			
			dro.set("first", dr.first);
			dro.set("second", dr.second);
			dro.set("full", dr.full);
			dro.set("value", dr.value);
			dro.set("lastContact", dr.lastContact);
			dro.set("wontTalk", dr.wontTalk());
			dro.set("tradeAgreement", dr.tradeAgreement);
			dro.set("strongAlliance", dr.strongAlliance);
			
			dro.set("allianceAgainst", new MessageArray(null, dr.alliancesAgainst));
			
			dra.add(dro);
		}
		
		return result;
	}
	@Override
	public String objectName() {
		return "EMPIRE_STATUSES";
	}
}
