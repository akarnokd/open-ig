/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * Interface for providing buildings on a planet with the environmental information about
 * the planet they're on.
 * @author karnokd
 *
 */
public interface PlanetInfo {
	/** 
	 * @return Returns the surface type. 
	 */
	SurfaceType getSurfaceType();
}
