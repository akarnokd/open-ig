/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.core;

import hu.openig.utils.PCXImage;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * Callback interface for looking up various building related images.
 * @author karnokd, 2009.05.21.
 * @version $Revision 1.0$
 */
public interface BuildingLookup {
	/**
	 * Returns the name label for the building index.
	 * @param index the building index
	 * @return the name
	 */
	String getNameLabel(int index);
	/**
	 * Returns the building description lines.
	 * @param index the building index
	 * @return the 3 element array of the building description
	 */
	String[] getDescriptionLabels(int index);
	/**
	 * Returns the building thumbnail image for the given
	 * building index and tech id.
	 * @param techId the tech id
	 * @param index the building index
	 * @return the thumbnail image
	 */
	BufferedImage getThumbnail(String techId, int index);
	/**
	 * Returns the building tile image for the given rech id,
	 * index, and wheter it should be the damaged or undamaged version.
	 * @param techId the technology id
	 * @param index the building index
	 * @param damaged the damaged version?
	 * @return the image in one piece
	 */
	PCXImage getBuildingTile(String techId, int index, boolean damaged);
	/**
	 * Returns a map from techid to list of building phase tiles.
	 * @return a map from techid to list of building phase tiles
	 */
	Map<String, List<Tile>> getBuildingPhases();
	/**
	 * Returns a map from techid to list of damaged building phase tiles.
	 * @return a map from techid to list of damaged building phase tiles
	 */
	Map<String, List<Tile>> getDamagedBuildingPhases();
}
