/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.core;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * The planet prototype.
 * @author karnokd, 2010.01.07.
 * @version $Revision 1.0$
 */
public class PlanetType {
	/** The planet type. */
	public String type;
	/** The label for this planet type. */
	public String label;
	/** The body animation phases. */
	public BufferedImage[] body;
	/** The available tileset. */
	public Map<Integer, Tile> tiles = new HashMap<Integer, Tile>();
	/** The surface map variations. */
	public Map<Integer, byte[]> surfaces = new HashMap<Integer, byte[]>();
}
