/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer.model;

import hu.openig.model.DiplomaticOffer;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.PlayerFinances;
import hu.openig.model.PlayerStatistics;
import hu.openig.utils.U;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Response with the current own and known
 * empires, including diplomacy info.
 * @author akarnokd, 2013.04.27.
 */
public class EmpireStatuses {
	/** The current time in UTC milliseconds. */
	public long currentTime;
	/** The current money. */
	public long money;
	/** The map of known other players and the diplomatic relations. */
	public final Map<String, DiplomaticRelation> knownPlayers = new HashMap<String, DiplomaticRelation>();
	/** The negotiation offers from players. */
	public final Map<String, DiplomaticOffer> offers = U.newLinkedHashMap();
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

}
