/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.model;

import hu.openig.utils.JavaUtils;

import java.util.List;
import java.util.Set;

/**
 * Equipment slot used on medium and large spaceships.
 * @author karnokd
 */
public class Slot {
	/** Slot X location on the image. */
	public int x;
	/** Slot Y location on the image. */
	public int y;
	/** Slot width on the image. */
	public int width;
	/** Slot height location on the image. */
	public int height;
	/** The type of the slot. */
	public String type;
	/** The maximum allowed number of equipment in this slot. */
	public int max;
	/** The set of allowed ids. Stored here until the late binding of ids occurs. */
	Set<String> idset = JavaUtils.newHashSet();
	/** The list of allowed equipment ids for this slot. */
	public final List<ResearchTech> ids = JavaUtils.newArrayList();
}
