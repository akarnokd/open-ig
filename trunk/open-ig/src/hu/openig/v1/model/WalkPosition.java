/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author karnok, 2009.10.09.
 * @version $Revision 1.0$
 */
public class WalkPosition {
	/** The walk position id. */
	public String id;
	/** The static image of the position. */
	public BufferedImage picture;
	/** The list of possible transitions. */
	public final List<WalkTransition> transitions = new ArrayList<WalkTransition>();
}
