/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.res.gfx;

import java.awt.image.BufferedImage;

/** 
 * Record to store the top or bottom bars of the starmap graphics.
 * @author karnokd
 */
public class StarmapBar {
	/** The left image part. */
	public BufferedImage left;
	/** The right image part. */
	public BufferedImage right;
	/** The link image betwen the left and right. */
	public BufferedImage link;
}
