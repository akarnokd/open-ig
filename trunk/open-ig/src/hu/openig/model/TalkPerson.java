/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A master record for a conversation with one of the
 * crew members in the bar.
 * @author akarnokd, 2009.10.10.
 */
public class TalkPerson {
	/** The person id. */
	public String id;
	/** The talk states. */
	public final Map<String, TalkState> states = new HashMap<>();
}
