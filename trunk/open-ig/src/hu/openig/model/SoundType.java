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
	DATABASE("ui/cabin"),
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
	WELCOME("ui/welcome"),
	/** The function is not available. */
	NOT_AVAILABLE("ui/not_available"),
	/** Acknowledge command. */
	ACKNOWLEDGE_1("spacewar/acknowledge_1"),
	/** Acknowledge command. */
	ACKNOWLEDGE_2("groundwar/acknowledge_2"),
	/** Click response. */
	CLICK("spacewar/click_1"),
	/** Long explosion. */
	EXPLOSION_LONG("spacewar/explosion_long"),
	/** Medium explosion. */
	EXPLOSION_MEDIUM("spacewar/explosion_medium"),
	/** Medium explosion. */
	EXPLOSION_MEDIUM_2("groundwar/explosion_medium"),
	/** Short explosion. */
	EXPLOSION_SHORT("spacewar/explosion_short"),
	/** Fire type 1. */
	FIRE_1("spacewar/fire_1"),
	/** Fire type 2. */
	FIRE_2("spacewar/fire_2"),
	/** Fire type 3. */
	FIRE_3("spacewar/fire_3"),
	/** Ground fire type 1. */
	GROUND_FIRE_1("groundwar/fire_1"),
	/** Ground fire type 1. */
	GROUND_FIRE_2("groundwar/fire_2"),
	/** Ground fire type 1. */
	GROUND_FIRE_3("groundwar/fire_3"),
	/** Ground fire type 1. */
	GROUND_FIRE_4("groundwar/fire_4"),
	/** Ground fire type 1. */
	GROUND_FIRE_5("groundwar/fire_5"),
	/** Ground fire type 1. */
	GROUND_FIRE_6("groundwar/fire_6"),
	/** Ground fire type 1. */
	GROUND_FIRE_7("groundwar/fire_7"),
	/** Fire lasers. */
	FIRE_LASER("spacewar/fire_laser"),
	/** Fire meson cannon. */
	FIRE_MESON("spacewar/fire_meson"),
	/** Fire particle cannon. */
	FIRE_PARTICLE("spacewar/fire_particle"),
	/** Fire rocket v1. */
	FIRE_ROCKET_1("spacewar/fire_rocket_1"),
	/** Fire rocket v2. */
	FIRE_ROCKET_2("spacewar/fire_rocket_2"),
	/** Rocket hit? */
	HIT("spacewar/hit"),
	/** Fire ground rocket. */
	FIRE_ROCKET("groundwar/fire_rocket"),
	/** Pause the game. */
	PAUSE("ui/pause"),
	/** Unpause the game. */
	UNPAUSE("ui/acknowledge_1")
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
