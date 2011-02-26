/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A location and static image of a position within the ship
 * walks.
 * @author akarnokd, 2009.10.09.
 */
public class WalkPosition {
	/** The walk position id. */
	public String id;
	/** The static image of the position. */
	public BufferedImage picture;
	/** The list of possible transitions. */
	public final List<WalkTransition> transitions = new ArrayList<WalkTransition>();
}
