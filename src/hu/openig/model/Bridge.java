/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The definition of the bridge screen and messages.
 * @author akarnokd, 2010.01.17.
 */
public class Bridge {
	/** The list of level graphics. */
	public final Map<Integer, Level> levels = new HashMap<>();
	/** The messages to send. */
	public final Map<String, VideoMessage> sendMessages = new LinkedHashMap<>();
	/** The messages to receive. */
	public final Map<String, VideoMessage> receiveMessages = new LinkedHashMap<>();
}
