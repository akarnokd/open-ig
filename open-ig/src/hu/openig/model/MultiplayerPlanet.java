/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The definition to initialize planets in a multiplayer match.
 * @author akarnokd, 2013.04.27.
 */
public class MultiplayerPlanet {
	/** The planet ID. */
	public String id;
	/** The coordinate override if not null. */
	public Double x;
	/** The coordinate override if not null. */
	public Double y;
	/** The size override if not null. */
	public Integer size;
	/** The surface type override if not null. */
	public String surface;
	/** The surface variant override if not null. */
	public Integer variant;
	/** Is this planet owned by the player? */
	public boolean owned;
	/** Does this planet have a deployed colony hub? */
	public boolean colonyHub;
	/** Does this planet have an orbital factory? */
	public boolean orbitalFactory;
}
