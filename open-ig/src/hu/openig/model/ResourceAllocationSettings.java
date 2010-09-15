/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A per planet resource allocation settings.
 * @author karnok, 2010.09.15.
 * @version $Revision 1.0$
 */
public class ResourceAllocationSettings implements Iterable<BuildingAW> {
	/** The available worker count. */
	public final int availableWorkers;
	/** The resource allocation strategy. */
	public final ResourceAllocationStrategy strategy;
	/** The list of building workers. */
	public final List<BuildingAW> buildings;
	
	/**
	 * Constructor. Initializes the fields from a full building list.
	 * @param availableWorkers the number of available workers
	 * @param strategy the allocation strategy to use
	 * @param buildings the list of buildings to operate on
	 */
	public ResourceAllocationSettings(List<Building> buildings, int availableWorkers, ResourceAllocationStrategy strategy) {
		this.availableWorkers = availableWorkers;
		this.strategy = strategy;
		this.buildings = new ArrayList<BuildingAW>(buildings.size());
		for (Building b : buildings) {
			if (b.isReady()) {
				this.buildings.add(b.getAllocationWorker());
			} else {
				// non-ready buildings receive zero
				b.assignedEnergy = 0;
				b.assignedWorker = 0;
			}
		}

	}
	@Override
	public Iterator<BuildingAW> iterator() {
		return buildings.iterator();
	}
}
