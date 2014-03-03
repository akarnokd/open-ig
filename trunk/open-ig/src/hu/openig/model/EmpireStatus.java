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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The empire status record.
 * @author akarnokd, 2013.05.04.
 */
public class EmpireStatus implements MessageObjectIO {
	/** The empire id. */
	public String id;
	/** The current money. */
	public double money;
	/** The negotiation offers from players. */
	public final Map<String, DiplomaticOffer> offers = new LinkedHashMap<>();
	/** The player level statistics. */
	public final PlayerStatistics statistics = new PlayerStatistics();
	/** The global financial information yesterday. */
	public final PlayerFinances yesterday = new PlayerFinances();
	/** The global finalcial information today. */
	public final PlayerFinances today = new PlayerFinances();
	/** The set of colonization targets. */
	public final Set<String> colonizationTargets = new LinkedHashSet<>();
	// TODO other global player statuses?

	@Override
	public void fromMessage(MessageObject mo) {
		id = mo.getString("id");
		money = mo.getLong("money");
		for (MessageObject moffer : mo.getArray("offers").objects()) {
			String id = moffer.getString("id");
			
			DiplomaticOffer offer = new DiplomaticOffer();
			
			offer.callType = CallType.valueOf(moffer.getString("callType"));
			offer.approach = ApproachType.valueOf(moffer.getString("approach"));
			offer.value(moffer.get("value"));
			
			offers.put(id, offer);
		}
		MessageObject ps = mo.getObject("statistics");
		for (String sn : ps.attributeNames()) {
			LongField f = statistics.fields.get(sn);
			if (f != null) {
				f.value = ps.getLong(sn);
			}
		}
		setPlayerFinances(today, mo.getObject("today"));
		setPlayerFinances(yesterday, mo.getObject("yesterday"));
		
		for (Object o : mo.getArray("colonize")) {
			colonizationTargets.add((String)o);
		}
	}
	/**
	 * Sets the player finances.
	 * @param f the finance record
	 * @param o the message object
	 */
	protected void setPlayerFinances(PlayerFinances f, MessageObject o) {
		f.productionCost = o.getInt("productionCost");
		f.researchCost = o.getInt("researchCost");
		f.buildCost = o.getInt("buildCost");
		f.repairCost = o.getInt("repairCost");
		f.taxIncome = o.getInt("taxIncome");
		f.tradeIncome = o.getInt("tradeIncome");
		f.taxMorale = o.getDouble("taxMorale");
		f.taxMoraleCount = o.getInt("taxMoraleCount");
	}
	/**
	 * Sets the player finances on the message object.
	 * @param o the message object
	 * @param f the finance record
	 */
	protected void setPlayerFinances(MessageObject o, PlayerFinances f) {
		o.set("productionCost", f.productionCost);
		o.set("researchCost", f.researchCost);
		o.set("buildCost", f.buildCost);
		o.set("repairCost", f.repairCost);
		o.set("taxIncome", f.taxIncome);
		o.set("tradeIncome", f.tradeIncome);
		o.set("taxMorale", f.taxMorale);
		o.set("taxMoraleCount", f.taxMoraleCount);
	}
	@Override
	public MessageObject toMessage() {
		MessageObject result = new MessageObject(objectName());
		result.set("id", id);
		result.set("money", money);
		
		MessageArray o = new MessageArray(null);
		result.set("offers", o);
		for (Map.Entry<String, DiplomaticOffer> e : offers.entrySet()) {
			MessageObject oo = new MessageObject(null);
			
			oo.set("id", e.getKey());
			DiplomaticOffer offer = e.getValue();
			oo.set("callType", offer.callType.toString());
			oo.set("approach", offer.approach.toString());
			oo.set("value", offer.value());
			
			o.add(oo);
		}
		MessageObject ps = new MessageObject(null);
		result.set("statistics", ps);
		
		for (Map.Entry<String, LongField> e : statistics.fields.entrySet()) {
			ps.set(e.getKey(), e.getValue().value);
		}
		
		MessageObject today = new MessageObject(null);
		result.set("today", today);
		setPlayerFinances(today, this.today);
		
		MessageObject yesterday = new MessageObject(null);
		result.set("yesterday", yesterday);
		setPlayerFinances(yesterday, this.yesterday);
		
		
		result.set("colonize", new MessageArray(null, colonizationTargets));
		
		return result;
	}


	@Override
	public String objectName() {
		return "EMPIRE";
	}

}
