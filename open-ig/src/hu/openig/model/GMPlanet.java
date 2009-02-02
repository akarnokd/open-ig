/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.SurfaceType;

/**
 * Game model planet record.
 * @author karnokd, 2009.02.02.
 * @version $Revision 1.0$
 */
public class GMPlanet {
	/** The surface type string from StarmapGFX.SURFACETYPE_* constants. */
	public String surfaceKey;
	/** The surface type 1-7. */
	public SurfaceType surfaceType;
	/** The surface variant 1-9. */
	public int surfaceVariant;
	/** Rotation phase index on the starmap. */
	public int rotationPhase;
	/** The planet name. */
	public String name;
	/** Show planet name? */
	public boolean showName;
	/** Show radar region? */
	public boolean showRadar;
	/** The radar circle radius. */
	public int radarRadius;
	/** Center X coordinate on the starmap. */
	public int x;
	/** Center Y coordinate on the starmap. */
	public int y;
	/** Display planet on the starmap? */
	public boolean visible;
	/** The color of the name. */
	public int nameColor;
}
