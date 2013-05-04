/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.LongField;
import hu.openig.net.MessageObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
	public long money;
	/** The map of known other players and the diplomatic relations. */
	public final List<DiplomaticRelation> knownPlayers = new ArrayList<DiplomaticRelation>();
	/** The negotiation offers from players. */
	public final Map<String, DiplomaticOffer> offers = new LinkedHashMap<String, DiplomaticOffer>();
	/** The player level statistics. */
	public final PlayerStatistics statistics = new PlayerStatistics();
	/** The global financial information yesterday. */
	public final PlayerFinances yesterday = new PlayerFinances();
	/** The global finalcial information today. */
	public final PlayerFinances today = new PlayerFinances();
	/** Pause the production without changing the production line settings. */
	public boolean pauseProduction;
	/** Pause the research without changing the current research settings. */
	public boolean pauseResearch;
	/** The set of colonization targets. */
	public final Set<String> colonizationTargets = new LinkedHashSet<String>();
	// TODO other global player statuses?

	@Override
	public void fromMessage(MessageObject mo) {
		id = mo.getString("id");
		money = mo.getLong("money");
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
			
			knownPlayers.add(dr);
		}
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
		
		pauseProduction = mo.getBoolean("pauseProduction");
		pauseResearch = mo.getBoolean("pauseResearch");
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
	@Override
	public MessageObject toMessage() {
		MessageObject result = new MessageObject(name());
		// TODO Auto-generated method stub
		return result;
	}


	@Override
	public String name() {
		return "EMPIRE";
	}

}
