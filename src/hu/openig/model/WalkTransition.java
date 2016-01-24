/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.Polygon;

/**
 * A ship walk transition with media and the associated
 * visual area where the user can click.
 * @author akarnokd, 2009.10.09.
 */
public class WalkTransition {
	/** The associated transition media. */
	public String media;
	/** The label to display when hovered. */
	public String label;
	/** The target location. */
	public String to;
	/** The clickable polygon area. */
	public Polygon area;
	/**
	 * The optional cursor to display when the mouse pointers enters the
	 *  clickable polygon area.
	 */
	public Cursors cursor;
}
