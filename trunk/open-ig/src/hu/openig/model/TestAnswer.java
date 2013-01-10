/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * A answer for the Phsychologist's test.
 * @author akarnokd, 2011.04.20.
 */
public class TestAnswer {
	/** The answer id. */
	public String id;
	/** The label to display. */
	public String label;
	/** Is this option selected? */
	public boolean selected;
	/** The points awarded for this answer. */
	public int points;
}
