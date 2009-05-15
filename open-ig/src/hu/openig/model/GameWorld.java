/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.res.Labels;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * The top level container describing an actual game.
 * @author karnokd, 2009.05.11.
 * @version $Revision 1.0$
 */
public class GameWorld implements GameRaceLookup {
	/** Show the ultra fast speed setter button? */
	public boolean showUltrafast;
	/** Current game speed. */
	public GameSpeed gameSpeed = GameSpeed.NORMAL;
	/** The speed control buttons enabled? */
	public boolean showSpeedControls = true;
	/** Show the time? */
	public boolean showTime;
	/** The list of planets. */
	public final List<GamePlanet> planets = new ArrayList<GamePlanet>();
	/** The current time of the game. Should be handled in GMT mode, no summer time. */
	public final GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	/** The player object, whose properties (fleets, planets, research, etc) are displayed by the rendering. */
	public GamePlayer player;
	/** 
	 * A flag to disable the modification of the player's assets.
	 * Could be used for diagnostic purposes when the player object is changed from
	 * the real human player to the other players. 
	 */
	public boolean readOnly;
	/** The list of all players. */
	public final List<GamePlayer> players = new ArrayList<GamePlayer>();
	/** The races in the current game. */
	public final List<GameRace> races = new ArrayList<GameRace>();
	/** All fleets in the current game. */
	public final List<GameFleet> fleets = new ArrayList<GameFleet>();
	/** The labels used in the game world. */
	public Labels labels;
	/** The current language. */
	public String language;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public GameRace getRace(int index) {
		for (GameRace gr : races) {
			if (gr.index == index) {
				return gr;
			}
		}
		throw new IllegalStateException("Unknown race index: " + index);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public GameRace getRace(String id) {
		for (GameRace gr : races) {
			if (gr.id.equals(id)) {
				return gr;
			}
		}
		throw new IllegalStateException("Unknown race id: " + id);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public GamePlayer getPlayerForRace(GameRace race) {
		for (GamePlayer player : players) {
			if (player.race == race) {
				return player;
			}
		}
		//throw new IllegalStateException("No player for race: " + race);
		return null;
	}
	/**
	 * Convinience method to return the current game year.
	 * @return the current game year
	 */
	public int getYear() {
		return calendar.get(GregorianCalendar.YEAR);
	}
	/**
	 * Convinience method to return the current game month.
	 * @return the current game month
	 */
	public int getMonth() {
		return calendar.get(GregorianCalendar.MONTH);
	}
	/**
	 * Convinience method to return the current game day.
	 * @return the current game day
	 */
	public int getDay() {
		return calendar.get(GregorianCalendar.DATE);
	}
	/**
	 * Convinience method to return the current game hour.
	 * @return the current game hour 0-23
	 */
	public int getHour() {
		return calendar.get(GregorianCalendar.HOUR_OF_DAY);
	}
	/**
	 * Convinience method to return the current game minute.
	 * @return the current game minute 0-59
	 */
	public int getMinute() {
		return calendar.get(GregorianCalendar.MINUTE);
	}
	/**
	 * Returns the translation for the current language.
	 * @param key the key
	 * @return the translation
	 */
	public String getLabel(String key) {
		return labels.get(key, language);
	}
	/**
	 * Returns an iterable of the player owned planets.
	 * @return the iterable
	 */
	public List<GamePlanet> getPlayerPlanets() {
		// i wish we had yield return
		List<GamePlanet> result = new ArrayList<GamePlanet>();
		for (GamePlanet p : planets) {
			if (p.owner == player) {
				result.add(p);
			}
		}
		return result;
	}
	/**
	 * A convinience method to associate each
	 * planet with its corresponding owner's internal
	 * known* sets. Only planets with direct ownership
	 * is assigned this way (e.g. the planets discovered
	 * by radar is not covered here).
	 */
	public void setPlanetOwnerships() {
		for (GamePlanet p : planets) {
			if (p.owner != null) {
				p.owner.possessPlanet(p);
			}
		}
	}
	/**
	 * A convinience method to assign fleets to their
	 * respective ovners ownFleet set.
	 */
	public void setFleetOwnerships() {
		for (GameFleet f : fleets) {
			if (f.owner != null) {
				f.owner.possessFleet(f);
			}
		}
	}
}
