/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Base interface to support model object lookup
 * based on their ids.
 * @author akarnokd, 2013.05.05.
 */
public interface ModelLookup {
    /**
     * Returns the player for the given player id.
     * @param playerId the player id
     * @return the player object or null if not found
     */
    Player player(String playerId);
    /**
     * Returns the research type for the given id.
     * @param researchId the research id
     * @return the research object or null if not found
     */
    ResearchType research(String researchId);
    /**
     * Returns the building type for the given id.
     * @param buildingTypeId the building type id
     * @return the building type object or null if not found
     */
    BuildingType building(String buildingTypeId);
}
