/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author karnok, 2009.10.25.
 * @version $Revision 1.0$
 */
public class Player {
	/** The list of aliens discovered this far. */
	public final List<String> discoveredAliens = new ArrayList<String>();
}
