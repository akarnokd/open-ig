/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.model;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * Interface for GameRace lookup operations.
 * @author karnokd, 2009.05.11.
 * @version $Revision 1.0$
 */
public interface GamePlanetLookup {
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
	/**
	 * Returns the first game player with the given race.
	 * @param race the race object
	 * @return the game player or null if no such player
	 */
	GamePlayer getPlayerForRace(GameRace race);
	/**
	 * Returns the building prototype object for the given building
	 * prototype id.
	 * @param buildingId the prototype id
	 * @return the building
	 */
	GameBuildingPrototype getBuildingPrototype(String buildingId);
	/**
	 * Returns a map containing the rotation images for various zoom levels.
	 * @param planetString the planet surface type string
	 * @return the map from zoom level to list of rotation images
	 */
	Map<Integer, List<BufferedImage>> getRotations(String planetString);
}
