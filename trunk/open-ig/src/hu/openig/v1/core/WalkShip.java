/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author karnok, 2009.10.09.
 * @version $Revision 1.0$
 */
public class WalkShip {
	/** The ship walk level. */
	public String level;
	/** The map of positions in the ship. */
	public final Map<String, WalkPosition> positions = new HashMap<String, WalkPosition>();
}
