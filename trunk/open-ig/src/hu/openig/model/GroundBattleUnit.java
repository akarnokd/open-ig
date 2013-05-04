/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;


/**
 * The basic ground battle unit status.
 * @author akarnokd, 2013.05.02.
 */
public class GroundBattleUnit {
	/** The unit unique id. */
	public int id;
	/** The unit research type. */
	public String type;
	/** The owner id. */
	public String owner;
	/** The firing phase index. */
	public int phase;
	/** The weapon cooldown. */
	public int cooldown;
	/** The rotation angle in radians. */
	public double angle;
	/** The current target unit. */
	public Integer attackUnit;
}
