/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Interface for GameRace lookup operations.
 * @author karnokd, 2009.05.11.
 * @version $Revision 1.0$
 */
public interface GameRaceLookup {
	/**
	 * Returns the indexth game race object.
	 * @param index the index
	 * @return the game race
	 */
	GameRace getRace(int index);
	/**
	 * Returns the game race with the given id.
	 * @param id the race id
	 * @return the game race
	 */
	GameRace getRace(String id);
}
