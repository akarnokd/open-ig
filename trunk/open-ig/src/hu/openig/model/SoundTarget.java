/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The target of a sound playback.
 * @author akarnokd, 2012.05.07.
 */
public enum SoundTarget {
	/** Computer announcement. */
	COMPUTER,
	/** Button press. */
	BUTTON,
	/** Generic effect. */
	EFFECT,
	/** Screen change. */
	SCREEN
}
