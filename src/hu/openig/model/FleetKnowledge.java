/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The knowledge type/level about a non-player fleet.
 * @author akarnokd, 2010.01.07.
 */
public enum FleetKnowledge {
	/** The fleet is visible, but nothing else is known. */
	VISIBLE,
	/** The fleet composition is known for larger ships, but fighters are displayed as range. */
	COMPOSITION,
	/** The fleet composition, numbers and firepower is known. */
	FULL
}
