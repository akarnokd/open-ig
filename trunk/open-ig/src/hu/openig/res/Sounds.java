/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.res;

import java.util.HashMap;
import java.util.Map;

import hu.openig.utils.IOUtils;
import hu.openig.utils.ResourceMapper;

/**
 * Record to manage the original game's sound effects.
 * @author karnokd
 */
public class Sounds {
	/** The audio sample map. */
	public final Map<String, byte[]> samples = new HashMap<String, byte[]>();
	/**
	 * Constructor. Initializes the samples map.
	 * @param resMap the resource mapper
	 */
	public Sounds(ResourceMapper resMap) {
		samples.put("IncomingMessage", IOUtils.load(resMap.get("SOUND/NOI01.SMP")));
		samples.put("CommanderMessage", IOUtils.load(resMap.get("SOUND/NOI02.SMP")));
		samples.put("Message", IOUtils.load(resMap.get("SOUND/NOI03.SMP")));
		samples.put("MessageBridge", IOUtils.load(resMap.get("SOUND/NOI04.SMP")));
		samples.put("DrAwaitsOnBridge", IOUtils.load(resMap.get("SOUND/NOI08.SMP")));
		samples.put("AlienVesselsDetected", IOUtils.load(resMap.get("SOUND/NOI09.SMP")));
		samples.put("TransportUnderAttack", IOUtils.load(resMap.get("SOUND/NOI10.SMP")));
		samples.put("BackupReceived", IOUtils.load(resMap.get("SOUND/NOI15.SMP")));
		samples.put("ReinforcementsArrived", IOUtils.load(resMap.get("SOUND/NOI16.SMP")));
		samples.put("Bridge", IOUtils.load(resMap.get("SOUND/NOI24.SMP")));
		samples.put("Starmap", IOUtils.load(resMap.get("SOUND/NOI25.SMP")));
		samples.put("Colony", IOUtils.load(resMap.get("SOUND/NOI26.SMP")));
		samples.put("Equipment", IOUtils.load(resMap.get("SOUND/NOI27.SMP")));
		samples.put("Production", IOUtils.load(resMap.get("SOUND/NOI28.SMP")));
		samples.put("Research", IOUtils.load(resMap.get("SOUND/NOI29.SMP")));
		samples.put("Information", IOUtils.load(resMap.get("SOUND/NOI30.SMP")));
		samples.put("StateRoom", IOUtils.load(resMap.get("SOUND/NOI31.SMP")));
		samples.put("Local", IOUtils.load(resMap.get("SOUND/NOI32.SMP")));
		samples.put("Diplomacy", IOUtils.load(resMap.get("SOUND/NOI33.SMP")));
		samples.put("Inventions", IOUtils.load(resMap.get("SOUND/NOI34.SMP")));
		samples.put("AlienRaces", IOUtils.load(resMap.get("SOUND/NOI35.SMP")));
		samples.put("FinancialInformation", IOUtils.load(resMap.get("SOUND/NOI36.SMP")));
		samples.put("MilitaryInformation", IOUtils.load(resMap.get("SOUND/NOI37.SMP")));
		samples.put("ColonyInformation", IOUtils.load(resMap.get("SOUND/NOI38.SMP")));
		samples.put("Fleets", IOUtils.load(resMap.get("SOUND/NOI39.SMP")));
		samples.put("Buildings", IOUtils.load(resMap.get("SOUND/NOI40.SMP")));
		samples.put("Planets", IOUtils.load(resMap.get("SOUND/NOI41.SMP")));
		samples.put("NewShipAdded", IOUtils.load(resMap.get("SOUND/NOI42.SMP")));
		samples.put("SplitFleet", IOUtils.load(resMap.get("SOUND/NOI49.SMP")));
		samples.put("JoinFleet", IOUtils.load(resMap.get("SOUND/NOI50.SMP")));
		samples.put("NewFleetCreated", IOUtils.load(resMap.get("SOUND/NOI51.SMP")));
		samples.put("AddedToProductionList", IOUtils.load(resMap.get("SOUND/NOI52.SMP")));
		samples.put("DeletedFromProductionList", IOUtils.load(resMap.get("SOUND/NOI53.SMP")));
		samples.put("ResearchStarted", IOUtils.load(resMap.get("SOUND/NOI54.SMP")));
		samples.put("ResearchStopped", IOUtils.load(resMap.get("SOUND/NOI55.SMP")));
		samples.put("ItemsProduced", IOUtils.load(resMap.get("SOUND/NOI56.SMP")));
		samples.put("ResearchCompleted", IOUtils.load(resMap.get("SOUND/NOI57.SMP")));
		samples.put("SatelliteDestroyed", IOUtils.load(resMap.get("SOUND/NOI58.SMP")));
		samples.put("UnidentifiedShipDetected", IOUtils.load(resMap.get("SOUND/NOI59.SMP")));
		samples.put("PlanetRevolveInProgress", IOUtils.load(resMap.get("SOUND/NOI60.SMP")));
		samples.put("NewFleetDetected", IOUtils.load(resMap.get("SOUND/NOI61.SMP")));
		samples.put("CampaignRecordMessageNotNow", IOUtils.load(resMap.get("SOUND/NOI80.SMP")));
		samples.put("CampaignRecordMessage", IOUtils.load(resMap.get("SOUND/NOI81.SMP")));
		samples.put("PlaceBuilding", IOUtils.load(resMap.get("SOUND/NOI82.SMP")));
		samples.put("DemolishBuilding", IOUtils.load(resMap.get("SOUND/NOI83.SMP")));
		samples.put("WelcomeToIG", IOUtils.load(resMap.get("SOUND/NOI84.SMP")));
		samples.put("GoodBye", IOUtils.load(resMap.get("SOUND/NOI85.SMP")));
		samples.put("DiplomacyShow", IOUtils.load(resMap.get("SOUND/NOI86.SMP")));
		samples.put("DiplomacyHide", IOUtils.load(resMap.get("SOUND/NOI87.SMP")));
		samples.put("SoundTest", IOUtils.load(resMap.get("MUSIC/SAMPLE.SMP")));
	}
}
