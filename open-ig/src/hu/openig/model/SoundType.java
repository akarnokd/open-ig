/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The built-in sounds.
 * @author akarnokd, Apr 18, 2011
 */
public enum SoundType {
	/** Sound. */
	ADD_PRODUCTION("ui/add_production"),
	/** Sound. */
	INFORMATION_ALIENS("ui/aliens"),
	/** Sound. */
	BAR("ui/bar"),
	/** Sound. */
	BRIDGE("ui/bridge"),
	/** Sound. */
	INFORMATION_BUILDINGS("ui/buildings"),
	/** Sound. */
	CABIN("ui/cabin"),
	/** Sound. */
	CARRIER_UNDER_ATTACK("ui/carrier_under_attack"),
	/** Sound. */
	COLONY("ui/colony"),
	/** Sound. */
	INFORMATION_COLONY("ui/colony_info"),
	/** Sound. */
	DEL_PRODUCTION("ui/del_production"),
	/** Sound. */
	DIPLOMACY("ui/diplomacy"),
	/** Sound. */
	ENEMY_FLEET_DETECTED("ui/enemy_fleet_detected"),
	/** Sound. */
	EQUIPMENT("ui/equipment"),
	/** Sound. */
	INFORMATION_FINANCIAL("ui/financial_info"),
	/** Sound. */
	INFORMATION_FLEETS("ui/fleets"),
	/** Sound. */
	DATABASE("ui/information"),
	/** Sound. */
	INFORMATION_INVENTIONS("ui/inventions"),
	/** Sound. */
	JOIN_FLEETS("ui/join_fleets"),
	/** Sound. */
	MESSAGE("ui/message"),
	/** Sound. */
	INFORMATION_MILITARY("ui/military_info"),
	/** Sound. */
	NEW_FLEET("ui/new_fleet"),
	/** Sound. */
	NEW_MESSAGE_1("ui/new_message_1"),
	/** Sound. */
	NEW_MESSAGE_2("ui/new_message_2"),
	/** Message indicator, longer, mentions the bridge. */
	NEW_MESSAGE_3("ui/new_message_3"),
	/** Sound. */
	PHSYCHOLOGIST_WAITING("ui/phsychologist_waiting"),
	/** Sound. */
	INFORMATION_PLANETS("ui/planets"),
	/** Sound. */
	PRODUCTION("ui/production"),
	/** Sound. */
	PRODUCTION_COMPLETE("ui/production_complete"),
	/** Sound. */
	RECORD_MESSAGE_NO("ui/record_message_no"),
	/** Sound. */
	RECORD_MESSAGE_YES("ui/record_message_yes"),
	/** Sound. */
	REINFORCEMENT_ARRIVED_1("ui/reinforcement_arrived_1"),
	/** Sound. */
	REINFORCEMENT_ARRIVED_2("ui/reinforcement_arrived_2"),
	/** Sound. */
	RESEARCH("ui/research"),
	/** Sound. */
	RESEARCH_COMPLETE("ui/research_completed"),
	/** Sound. */
	REVOLT("ui/revolt"),
	/** Sound. */
	SATELLITE_DESTROYED("ui/satellite_destroyed"),
	/** Sound. */
	SHIP_DEPLOYED("ui/ship_deployed"),
	/** Sound. */
	SPLIT_FLEET("ui/split_fleet"),
	/** Sound. */
	STARMAP("ui/starmap"),
	/** Sound. */
	START_RESEARCH("ui/start_research"),
	/** Sound. */
	STOP_RESEARCH("ui/stop_research"),
	/** Sound. */
	UNKNOWN_FLEET("ui/unknown_fleet"),
	/** Sound. */
	UNKNOWN_SHIP("ui/unknown_ship"),
	/** Sound. */
	DEPLOY_BUILDING("ui/deploy_building"),
	/** Sound. */
	DEMOLISH_BUILDING("ui/demolish_building"),
	/** The welcome message. */
	WELCOME("ui/welcome")
	;
	/** The associated resource location. */
	public final String resource;
	/**
	 * Initialize with a resource location.
	 * @param resource the resource location
	 */
	SoundType(String resource) {
		this.resource = resource;
	}
}
