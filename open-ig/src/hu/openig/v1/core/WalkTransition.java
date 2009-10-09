/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.core;

import java.awt.Polygon;

/**
 * @author karnok, 2009.10.09.
 * @version $Revision 1.0$
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
}
