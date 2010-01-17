/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author karnok, 2009.10.10.
 * @version $Revision 1.0$
 */
public class TalkPerson {
	/** The person id. */
	public String id;
	/** The talk states. */
	public final Map<String, TalkState> states = new HashMap<String, TalkState>();
}
