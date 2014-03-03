/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The fleet statistics.
 * @author akarnokd, 2011.04.01.
 */
public class FleetStatistics {
	/** The battleship count. */
	public int battleshipCount;
	/** The cruiser count. */
	public int cruiserCount;
	/** The fighter count. */
	public int fighterCount;
	/** The vehicle count. */
	public int vehicleCount;
	/** Maximum vehicle capacity. */
	public int vehicleMax;
	/** The maximum speed. */
	public int speed;
	/** The firepower. */
	public double firepower;
	/** The ground firepower. */
	public double groundFirepower;
	/** The nearby planet, where distance < 45 units. */
	public Planet planet;
}
