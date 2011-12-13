/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.JavaUtils;

import java.util.Map;

/**
 * Represents a goal in terms of world properties and their expected values.
 * @author akarnokd, 2011.12.13.
 */
public class AIGoal {
	/**
	 * The map of world properties containing the expected value mapped to the current value in the world.
	 */
	public final Map<AIWorldProperty, Object> goals = JavaUtils.newHashMap();
}
