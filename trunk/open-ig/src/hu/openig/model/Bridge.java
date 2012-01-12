/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The definition of the bridge screen and messages.
 * @author akarnokd, 2010.01.17.
 */
public class Bridge {
	/** The list of level graphics. */
	public final Map<Integer, Level> levels = new HashMap<Integer, Level>();
	/** The messages to send. */
	public final List<VideoMessage> sendMessages = new ArrayList<VideoMessage>();
	/** The messages to receive. */
	public final List<VideoMessage> receiveMessages = new ArrayList<VideoMessage>();
}
