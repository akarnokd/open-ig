/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;

/**
 * A fleet.
 * @author karnokd, 2010.01.07.
 * @version $Revision 1.0$
 */
public class Fleet {
	/** The owner of the fleet. */
	public Player owner;
	/** The X coordinate. */
	public int x;
	/** The Y coordinate. */
	public int y;
	/** The associated ship icon. */
	public BufferedImage shipIcon;
	/** The radar radius. */
	public int radar;
	/** The fleet name. */
	public String name;
}
